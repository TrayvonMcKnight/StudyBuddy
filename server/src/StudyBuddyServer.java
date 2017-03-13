
import StudyBuddy.Database;
import StudyBuddy.OnlineClientList;
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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StudyBuddyServer extends Thread {

    private ServerSocket serverSocket;
    private Database database;
    private OnlineClientList onlineList;

    public StudyBuddyServer(int port) {
        this.database = null;
        this.serverSocket = null;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("FATAL ERROR:  Unable to listen on port " + port + ".  May already be in use. Cannot start server without this required component.");
            System.exit(1);
        }
        this.database = new Database();
        //serverSocket.setSoTimeout(10000);
    }

    @Override
    public void run() {
        System.out.println("Listening on TCP port " + serverSocket.getLocalPort() + "...");

        while (true) {
            try {
                Socket server = serverSocket.accept();
                DataInputStream in = new DataInputStream(server.getInputStream());
                Date curDate = new Date();
                if (in.readUTF().equals("05:HANDSHAKE:STUDYBUDDY:1.00:::01")) {
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    out.writeUTF("05:HANDSHAKE:STUDYBUDDY:1.00:00:HELLO:00");
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Handshake:: Request from: " + server.getRemoteSocketAddress().toString().substring(1) + " - HandShake Accepted.");
                    Thread auth = new Thread(new AuthenticationThread(server, this.onlineList));
                    auth.start();
                } else {
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
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

    public static void main(String[] args) {
        int port = 6000;
        Thread t = new StudyBuddyServer(port);
        t.start();
    }

    private class AuthenticationThread extends Thread {

        // Private thread fields
        private final Socket con;
        private final OnlineClientList onlineList;
        private final Thread authenticationThread;
        private boolean loggedIn = false;

        // Thread Constructor
        public AuthenticationThread(Socket con, OnlineClientList list) {
            this.con = con;
            this.onlineList = list;
            this.authenticationThread = this;
            
        }

        @Override
        public void run() {
            DataInputStream inStream = null;
            DataOutputStream outStream = null;
            try {
                inStream = new DataInputStream(con.getInputStream());
                outStream = new DataOutputStream(con.getOutputStream());
                ObjectInputStream inFromClient = new ObjectInputStream(con.getInputStream());
                ObjectOutputStream outToClient = new ObjectOutputStream(con.getOutputStream());
                while (con.isConnected()){
                    if (loggedIn) break;
                String authType = (String) inStream.readUTF();
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
                if (con.isConnected()){
                Date curDate = new Date();
                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
                this.authenticationThread.stop();
                }
            } finally {
                try {
                    inStream.close();
                    outStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private void createAccount(DataInputStream inStream, DataOutputStream outStream, String[] pieces) {
            if (!pieces[2].equals("") && !pieces[3].equals("") && !pieces[4].equals("") && !pieces[5].equals("") && !pieces[6].equals("")){
                try {
                Date curDate = new Date();
                switch (database.registerUser(pieces[2], pieces[3], pieces[5], pieces[6])){
                    case 0: {
                        outStream.writeUTF("09:CREATEACCOUNT:" + pieces[2] + ":ACCEPTED:00::00");
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Accepted - New user added.");
                        break;
                    }
                    case 1: {
                        outStream.writeUTF("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:01::00");
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - User with email already exists.");
                        break;
                    }
                    case 2: {
                        outStream.writeUTF("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:02::00");
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - Incorrect password format.");
                        break;
                    }
                    case 3: {
                        outStream.writeUTF("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:03::00");
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Register:::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Registration Rejected - Incorrect email format.");
                        break;
                    }
                    case 4: {
                        outStream.writeUTF("09:CREATEACCOUNT:" + pieces[2] + ":REJECTED:04::00");
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
            }catch (IOException ex) {
                        Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
            
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
                        login = (String) inStream.readUTF();
                        pieces = login.split(":");
                    }
                    if (!pieces[2].equals("null") && !pieces[3].equals("null")) {

                        userName = pieces[2];
                        passWord = pieces[3];
                        result = database.returnUserInfo(userName);

                        if (!result.next()) {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - No such user.");
                            attempts++;
                            if (attempts != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":01:NOUSER:00";
                                    outStream.writeUTF(reply);
                            }
                        } else if (result.getString("pass_word").equals(passWord)) {
                            Date curDate = new Date();
                            database.updateLastLoginTime(userName);
                            database.updateUserLoggedIn(userName, true);
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + result.getString("email") + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Accepted.");
                            String reply = "01:LOGIN:" + database.getUserStatus(userName) + ":" + pieces[3] + ":00:ACCEPTED:00";
                            outStream.writeUTF(reply);
                            //Session sess = new Session(con, inStream, outStream, inFromClient, outToClient, this.onlineList, result.getString("user_name"));
                            //Thread session = new Thread(sess);
                            //onlineList.addClient(result.getString("user_name"), result.getString("email"), con.getRemoteSocketAddress().toString().substring(1), database.getUserStatus(userName), inFromClient, outToClient, sess);
                            //session.start();
                            this.loggedIn = true;
                            break;
                        } else if (!result.getString("pass_word").equals(passWord)) {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Incorrect password.");
                            attempts++;
                            if (attempts != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":02:BADPASS:00";
                                    outStream.writeUTF(reply);
                                    
                                }
                        }
                        if (attempts == 3) {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Too many failed attempts.");
                            String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":03:GOODBYE:00";
                            outStream.writeUTF(reply);
                            attempts++;
                            con.close();
                        }

                    } else {
                        Date curDate = new Date();
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - User canceled login.");
                        String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":05:LOGINCANCELED:00";
                        outStream.writeUTF(reply);
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
