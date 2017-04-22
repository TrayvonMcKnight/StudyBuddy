
import Encryption.AES128CBC;
import Encryption.ECDHKeyExchange;
import StudyBuddy.ChatRoomBackups;
import StudyBuddy.Chatrooms;
import StudyBuddy.Chatrooms.Chatroom;
import StudyBuddy.Database;
import StudyBuddy.OnlineClientList;
import StudyBuddy.Session;
import StudyBuddy.Student;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudyBuddyServer extends Thread {
    // private class fields.
    private final String version = "1.34";
    private ServerSocket serverSocket;
    private Database database;
    private final OnlineClientList onlineList;
    private Chatrooms chatrooms;
    private final ChatRoomBackups backups;
    private AES128CBC aes128;

    // Server constructor
    public StudyBuddyServer(int port) {
        this.database = null;
        this.serverSocket = null;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("FATAL ERROR:  Unable to listen on port " + port + ".  May already be in use. Cannot start server without this required component.");
            System.exit(1);
        }
        this.database = new Database(); // Start the database.
        this.cleanDatabase();   // Logout all users left online due to server crash or maintainance.
        this.onlineList = new OnlineClientList();   // Create a linked list of all online clients.
        this.backups = new ChatRoomBackups();
        if (backups.fileExists()){
            this.chatrooms = backups.loadChatRoomStatus();  // If there is a backup, load it.
            this.cleanChatrooms();  // Mark all users as offline when the server restarts from a crash or maintainance.
        } else {
            this.buildChatrooms();  // If there is not a backup, create the initial Chatrooms object and make backup.
        }
    }

    @Override
    public void run() {
        System.out.println("Starting the Study Buddy server v." + this.version);
        System.out.println("Listening on TCP port " + serverSocket.getLocalPort() + "...");

        while (true) {
            try {
                Socket server = serverSocket.accept();
                DataInputStream in = new DataInputStream(server.getInputStream());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                
                // Start key agreement
                ECDHKeyExchange keyXchanger = new ECDHKeyExchange();
                String key = in.readUTF();
                byte[] theirKey = Base64.getMimeDecoder().decode(key);
                keyXchanger.setTheirPublicKey(theirKey);
                byte[] myKey = keyXchanger.returnMyPublicKey();
                out.writeUTF(new String(Base64.getMimeEncoder().encode(myKey)));
                // Compute Symmetric Key and create symmetric cipher.
                aes128 = new AES128CBC(keyXchanger.computeSharedSecret());
                
                
                
                Date curDate = new Date();
                if (aes128.decrypt(in.readUTF()).equals("05:HANDSHAKE:STUDYBUDDY:1.00:::01")) {
                    out.writeUTF(aes128.encrypt("05:HANDSHAKE:STUDYBUDDY:1.00:00:HELLO:00"));
                    
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Handshake:: Request from: " + server.getRemoteSocketAddress().toString().substring(1) + " - HandShake Accepted.");
                    Thread auth = new Thread(new AuthenticationThread(server, this.onlineList));
                    auth.start();
                } else {
                    out.writeUTF("GOODBYE");
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Handshake:: Request from: " + server.getRemoteSocketAddress().toString().substring(1) + " - HandShake Rejected.");
                    server.close();
                }

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;

            } catch (IOException e) {
                break;
            }
        }
    }
    
    private void cleanDatabase(){
        ResultSet temp = this.database.returnAllOnlineStudents();
        try {
            while (temp.next()){
                this.database.updateUserLoggedIn(temp.getString(1), false);
            }
        } catch (SQLException ex) {
            Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void cleanChatrooms(){
        
        for (int c = 0; c < this.chatrooms.getNumberOfClasses();c++){
            Chatroom room = this.chatrooms.getChatroom(c);
            int numStudents = room.returnNumberOfStudents();
            for (int d = 0;d < numStudents;d++){
                Student student = room.getStudent(d);
                student.setOnlineStatus(Boolean.FALSE);
            }
        }
    }

    private boolean buildChatrooms() {
        boolean success = false;
        this.chatrooms = new Chatrooms();   // Create and build the master list of chat rooms available.
        ResultSet allClasses = this.database.returnAllClasses();
        ResultSet students;
        try {
            while (allClasses.next()) {
                // Fix this is add new information retrieved from the database.
                this.chatrooms.addChatroom(allClasses.getString(2), allClasses.getString(3), allClasses.getString(8), allClasses.getString(9), allClasses.getString(4), allClasses.getTime(5), allClasses.getTime(6), allClasses.getString(7));
                students = this.database.returnAllStudents(allClasses.getString(2), allClasses.getString(3));
                Boolean online;
                int status;
                while (students.next()) {
                    if (students.getInt(5) == 1) {
                        online = true;
                    } else {
                        online = false;
                    }
                    if (students.getString(4).equalsIgnoreCase("Available")) {
                        status = 0;
                    } else {
                        status = 1;
                    }
                    this.chatrooms.addStudent(allClasses.getString(2), allClasses.getString(3), students.getString(2) + " " + students.getString(3), students.getString(1), online, status);
                }
            }
            backups.saveChatRoomStatus(this.chatrooms); // Save chat room status to file after building.
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    public static void main(String[] args) {
        int port = 8008;
        Thread t = new StudyBuddyServer(port);
        t.start();
    }

    private class AuthenticationThread extends Thread {

        // Private thread fields
        private final Socket con;
        private final OnlineClientList onlineList;
        private final Thread authenticationThread;
        private boolean loggedIn = false;
        DataInputStream inStream;
        DataOutputStream outStream;
        ObjectInputStream inFromClient;
        ObjectOutputStream outToClient;

        // Thread Constructor
        public AuthenticationThread(Socket con, OnlineClientList list) {
            this.con = con;
            this.onlineList = list;
            this.authenticationThread = this;

        }

        @Override
        public void run() {
            try {
                inStream = new DataInputStream(con.getInputStream());
                outStream = new DataOutputStream(con.getOutputStream());
                inFromClient = new ObjectInputStream(con.getInputStream());
                outToClient = new ObjectOutputStream(con.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {

                while (con.isConnected()) {
                    if (loggedIn) {
                        break;
                    }
                    String authType = (String) aes128.decrypt(inStream.readUTF());
                    String[] pieces = authType.split(":");
                    if (pieces[1].equals("CREATEACCOUNT") && pieces[0].equals("09")) {
                        this.createAccount(inStream, outStream, pieces);
                    } else if (pieces[1].equals("LOGIN") && pieces[0].equals("01")) {
                        this.login(inStream, outStream, pieces);
                    } else {
                        // Invalid response received and session terminated.
                        outStream.writeUTF("GOODBYE");
                        Date curDate = new Date();
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Authentication:: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Authentication Rejected - Incorrect Protocol.  Session Terminated.");
                        inStream.close();
                        outStream.close();
                    }
                }
            } catch (IOException ex) {
                if (con.isConnected()) {
                    Date curDate = new Date();
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
                    this.authenticationThread.stop();
                }
            }
        }

        private void createAccount(DataInputStream inStream, DataOutputStream outStream, String[] pieces) {
            if (!pieces[2].equals("") && !pieces[3].equals("") && !pieces[4].equals("") && !pieces[5].equals("") && !pieces[6].equals("")) {
                try {
                    Date curDate = new Date();
                    switch (database.registerUser(pieces[2], pieces[3], pieces[5], pieces[6])) {
                        case 0: {
                            outStream.writeUTF(aes128.encrypt("09:CREATEACCOUNT:" + pieces[2] + ":ACCEPTED:00::00"));
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Accepted - New user added.");
                            this.assignClasses(pieces[2]);
                            // Need to notify all other students who are online, that someone has been added.
                            //this.notifyOfNewStudent(pieces[2]);
                            break;
                        }
                        case 1: {
                            outStream.writeUTF(aes128.encrypt("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:01::00"));
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - User with email already exists.");
                            break;
                        }
                        case 2: {
                            outStream.writeUTF(aes128.encrypt("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:02::00"));
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - Incorrect password format.");
                            break;
                        }
                        case 3: {
                            outStream.writeUTF(aes128.encrypt("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:03::00"));
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - Incorrect email format.");
                            break;
                        }
                        case 4: {
                            outStream.writeUTF(aes128.encrypt("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:04::00"));
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - Unknown database error.");
                            break;
                        }
                        default: {
                            outStream.writeUTF("GOODBYE");
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
                            inStream.close();
                            outStream.close();
                            this.authenticationThread.stop();
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        
        private void assignClasses(String studentEmail){
            ResultSet student = database.returnUserInfo(studentEmail);
            ResultSet allClasses = database.returnAllClassesByStudent(studentEmail);
            try {
                if (student.next()){
                        String studentName = student.getString(4) + " " + student.getString(5);
                        int available = database.getUserStatus(studentEmail);
                        int loggedIn = student.getInt(9);
                        boolean online;
                        if (loggedIn == 0){
                            online = false;
                        } else online = true;
                        while (allClasses.next()){
                            chatrooms.addStudent(allClasses.getString(1), allClasses.getString(2), studentName, studentEmail, online, available);
                        }
                        backups.saveChatRoomStatus(chatrooms);  // Update the Chatrooms object file with a new student change.
                }   } catch (SQLException ex) {
                Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void login(DataInputStream inStream, DataOutputStream outStream, String[] pieces) {
            String userName, passWord;
            ResultSet result;
            int attempts = 0;
            String login = "";
            try {
                while (attempts != 4) {
                    // If this is not the first call to this method.....
                    if (attempts != 0) {
                        // Get another login attempt.
                        login = (String) aes128.decrypt(inStream.readUTF());
                        pieces = login.split(":");
                    }
                    if (!pieces[2].equals("null") && !pieces[3].equals("null")) {

                        userName = pieces[2];
                        passWord = pieces[3];
                        result = database.returnUserInfo(userName);

                        if (!result.next()) {
                            result = database.returnInstructorInfo(userName);
                            
                            if(!result.next()){
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - No such user.");
                                attempts++;
                                if (attempts != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":01:NOUSER:00";
                                    outStream.writeUTF(aes128.encrypt(reply));
                                }
                            } else if (result.getString("pass").equals(passWord)){
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + result.getString("email") + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Accepted.");
                                String reply = "01:LOGIN:" + result.getString("email") + ":" + pieces[3] + ":00:ACCEPTED:00:01";
                                outStream.writeUTF(aes128.encrypt(reply));
                                Session sess = new Session(con, inStream, outStream, inFromClient, outToClient, this.onlineList, result.getString("email"), database, chatrooms, aes128, true);
                                Thread session = new Thread(sess);
                                session.start();
                                this.loggedIn = true;
                                break;
                            } else {
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Incorrect password.");
                                attempts++;
                                if (attempts != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":02:BADPASS:00";
                                    outStream.writeUTF(aes128.encrypt(reply));
                                }
                            }
                            
                            
                            
                        } else if (result.getString("sPass").equals(passWord)) {
                            Date curDate = new Date();
                            database.updateLastLoginTime(result.getString("sEmail"));
                            database.updateUserLoggedIn(result.getString("sEmail"), true);
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + result.getString("sEmail") + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Accepted.");
                            String reply = "01:LOGIN:" + result.getString("sEmail") + ":" + pieces[3] + ":00:ACCEPTED:00:00";
                            outStream.writeUTF(aes128.encrypt(reply));
                            // Update chat rooms.
                            ResultSet rooms = database.returnAllClassesByStudent(userName);
                            while (rooms.next()) {
                                Student stud = chatrooms.getStudent(rooms.getString(1), rooms.getString(2), result.getString("sEmail"));
                                stud.setOnlineStatus(true);
                            }
                            Session sess = new Session(con, inStream, outStream, inFromClient, outToClient, this.onlineList, result.getString("sEmail"), database, chatrooms, aes128, false);
                            Thread session = new Thread(sess);
                            String first = result.getString("sFName");
                            String last = result.getString("sLName");
                            String mail = result.getString("sEmail");
                            String ip = con.getRemoteSocketAddress().toString().substring(1);
                            int stat = database.getUserStatus(userName);
                            onlineList.addClient(first + " " + last, mail, ip, stat, inFromClient, outToClient, sess);

                            session.start();
                            this.loggedIn = true;
                            break;
                        } else if (!result.getString("sPass").equals(passWord)) {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Incorrect password.");
                            attempts++;
                            if (attempts != 3) {
                                String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":02:BADPASS:00";
                                outStream.writeUTF(aes128.encrypt(reply));

                            }
                        }
                        if (attempts == 3) {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Too many failed attempts.");
                            String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":03:GOODBYE:00";
                            outStream.writeUTF(aes128.encrypt(reply));
                            attempts++;
                            con.close();
                        }

                    } else {
                        Date curDate = new Date();
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - User canceled login.");
                        String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":05:LOGINCANCELED:00";
                        outStream.writeUTF(aes128.encrypt(reply));
                        this.con.close();
                        attempts = 4;
                    }
                }
            } catch (IOException ex) {
                Date curDate = new Date();
                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
                this.authenticationThread.stop();
            } catch (SQLException ex) {
                System.out.println("Database not present");
                System.out.println(ex.toString());
                Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
