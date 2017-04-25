package edu.uncg.studdybuddy.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import edu.uncg.studdybuddy.encryption.AES128CBC;
import edu.uncg.studdybuddy.encryption.ECDHKeyExchange;

/**
 * Created by Anthony Ratliff, Trayvon McKnight and Jlesa Carr on 2/10/2017.
 */

public class StudyBuddyConnector {
    // Private class fields
    private final String IP = "studybuddy.uncg.edu";   // byte array to hold server IP address.
    private final int port = 8008; // integer to hold server port number.
    private final String VERSION = "1.40";
    private InetAddress address;    // InetAddress comprised of IP and port.
    private final String greetString = "05:HANDSHAKE:STUDYBUDDY:1.00:::01";   // String to hold the handshake greeting.
    private final int handshakeTimeout = 5000; // integer to hold the server timeout for the handshake.
    private Socket client;  // TCP Connection Socket.
    private OutputStream outToServer;   // TCP Output Stream.
    private ObjectOutputStream outToServerObj;
    private ObjectInputStream inFromServerObj;
    private DataOutputStream out;   // TCP Output Data Stream.
    private InputStream inFromServer;   // TCP Input Stream.
    private DataInputStream in; // TCP Input Data Stream.
    private boolean loggedIn;  // Boolean to hold if the user has actually logged into the server.
    private boolean connected;  // Boolean to hold the connection status.
    private boolean isProfessor;
    private String userName;    // String to represent the user's real name.
    private String userEmail;   // String to represent the user's email address.
    private LinkedBlockingQueue<Object> messages;   // Queue for outgoing and incoming server messages.
    private Thread messageHandler;  // Private thread which listens for incoming messages from the server.
    private Thread messageQueue;     // Private thread which handles all incoming and outgoing messages.
    private int passwordError;  // integer which returns the last error from login attempt.
    private int chatError;
    private Chatrooms chatrooms;    // The main chat rooms data structure which stores all data about all available chat rooms.
    private ArrayList<MyCustomObjectListener> listeners;    // Array list which holds all available event listeners registered.
    private ArrayList<File> files;
    private AtomicBoolean waiter;
    //private AES128CBC aes128;
    private ArrayList<String> attendance;


    // Class constructor
    public StudyBuddyConnector() {

        this.loggedIn = false;
        this.connected = false;
        this.messageHandler = null;
        this.messageQueue = null;
        this.listeners = new ArrayList<>();
        this.files = new ArrayList<>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            this.address = InetAddress.getByName(IP);   // Bind the InetAddress to the server IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.waiter = new AtomicBoolean(false);
        this.isProfessor = false;
    }

    // Public class methods

    // Assign the listener implementing events interface that will receive the events
    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listeners.add(listener);
    }


    public StudyBuddyConnector getInstance() {
        return this;
    }
    public String getVersion(){
        return this.VERSION;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    public boolean hasConnection() {
        return this.connected;
    }

    public boolean isProfessor(){
        return this.isProfessor;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public Chatrooms getChatrooms() {
        return this.chatrooms;
    }


    public void close() {
        try {
            this.client.close();
            this.loggedIn = false;
            this.connected = false;
            this.messageQueue.interrupt();
            this.messageHandler.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     Public Class 'handshake' accepts no arguments but returns an integer to
     which represents the status of the handshake to the chat server.
     0 = No Errors / handshake accepted.
     1 = Client Rejected / Port in use.
     2 = Server Rejected / Bad Client.
     3 = Server Rejected / Outdated Client.
     4 = Server timeout / server did not respond in the allotted time.
     */
    public int handshake() {
        this.client = new Socket(); // No-arg constructor used to change the timeout.
        try {
            this.client.connect(new InetSocketAddress(address, port), handshakeTimeout); // Attempt to locate the server and timeout if not found.
        } catch (IOException e) {
            System.out.println(e.toString());
            return 4;   // Return if socket can not connect due to timeout.
        }
        try {
            this.outToServer = client.getOutputStream();    // Instantiate output stream from socket.
            this.out = new DataOutputStream(outToServer);   // Instantiate data stream from output stream.
            this.inFromServer = client.getInputStream(); // Instantiate input stream from socket.
            this.in = new DataInputStream(inFromServer); // Instantiate data stream from input stream.

            // Perform Key Agreement
            //ECDHKeyExchange keyXchanger = new ECDHKeyExchange();
            //byte[] myPublic = keyXchanger.returnMyPublicKey();
            //out.writeUTF(new String(Base64.encodeToString(myPublic, Base64.DEFAULT)));
            //out.flush();
            //String key = in.readUTF();
           // byte[] theirPublic =Base64.decode(key, Base64.DEFAULT);
            //keyXchanger.setTheirPublicKey(theirPublic);
            //this.aes128 = new AES128CBC(keyXchanger.computeSharedSecret());

            out.writeUTF(this.greetString); // Send handshake string though data stream.
            out.flush();


            if (in.readUTF().equals("05:HANDSHAKE:STUDYBUDDY:1.00:00:HELLO:00")) { // Read the incoming packet and verify the server said 'HELLO' to ensure proper handshake.
                // Possibly consider not getting these until the session has been created.
                outToServerObj = new ObjectOutputStream(this.client.getOutputStream());
                inFromServerObj = new ObjectInputStream(this.client.getInputStream());
                this.connected = true;
                return 0;   // Read from input stream and return success if server responds positive.
            }
        } catch (IOException ex) {
            System.out.println(ex);
            try {
                this.inFromServer.close();
                this.outToServer.close();
            } catch (IOException ex1) {
                System.out.println(ex1);
            }
            return 1;   // Return rejection if error is received.
        }
        try {
            this.inFromServer.close();
            this.outToServer.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return 2;   // Return rejection if server responds negative.
    }

    /*
     Public method 'login' accepts two Strings to represent the username
     and password of the client attempting to login the server.  This method
     returns an integer to represent the status of the login attempt.
     0 = No Errors / login accepted.
     1 = Client Rejected / handshake has not occurred first.
     2 = Server Rejected / username not found in database.
     3 = Server Rejected / incorrect password for the user.
     4 = Server timeout / server rejection - more than 3 failed attempts.
     5 = Server Rejected / Cannot create a new account while a user is logged in.
     */
    public int login(String email, String pass) {
        String answer = ""; // initialize the reply string so scope will fall outside of Try/Catch block.
        String[] pieces = {};
        try {
            String login = "01:LOGIN:" + email + ":" + pass + ":::01";
            this.out.writeUTF(login);
            String reply = (String) in.readUTF();
            pieces = reply.split(":");
            answer = pieces[5];

        } catch (IOException ex) {
            System.out.println("Message from the outToServerObj not being created.");
            // This should log out of the server.
            ex.printStackTrace();
        }
        switch (answer) {
            // If the login is accepted...
            case "ACCEPTED":
                if (pieces[7].equals("01")){
                    this.isProfessor = true;
                } else {
                    this.isProfessor = false;
                }
                loggedIn = true;
                this.userEmail = pieces[2];
                this.messages = new LinkedBlockingQueue();
                this.messageHandler = new Thread(new MessageListener());
                this.messageQueue = new Thread(new MessageQueue());
                this.messageQueue.start();
                this.messageHandler.start();
                this.getChatroomsFromServer();
                //alertClient(new ActionEvent(this, 1, "03:" + pieces[2]));
                return 0;   // Return 0 if username and password were accepted.
            case "REJECTED":
                return 1;   // return 1 if credentials were rejected.
            case "NOUSER":
                return 2;   // return 2 if the username does not exist in the database.
            case "BADPASS":
                return 3;   // return 3 if the password is incorrect.
            case "GOODBYE":
                this.connected = false;

                return 4;   // return 4 if there were too many failed attempts.
            case "LOGINCANCELED":
                return 5;   // return 5 if user canceled the login request.
        }
        return 1;
    }

    private void getChatroomsFromServer() {
        if (this.loggedIn) {
            try {
                String requestList = "02:GETLIST:::::01";
                this.messages.put(requestList);
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

    public int createNewUser(String email, String pass1, String pass2, String fName, String lName) {
        int temp = 5;
        if (!this.loggedIn) {
            String answer = ""; // initialize the reply string so scope will fall outside of Try/Catch block.
            String[] pieces = {};
            String create = "09:CREATEACCOUNT:" + email + ":" + pass1 + ":" + pass2 + ":" + fName + ":" + lName;
            try {
                this.out.writeUTF(create);
                String reply = (String) in.readUTF();
                pieces = reply.split(":");
                answer = pieces[4];
                switch (answer) {
                    case "00": {
                        temp = 0;
                        break;
                    }
                    case "01": {
                        temp = 1;
                        break;
                    }
                    case "02": {
                        temp = 2;
                        break;
                    }
                    case "03": {
                        temp = 3;
                        break;
                    }
                    case "04": {
                        temp = 4;
                        break;
                    }
                    default: {
                        // Goodbye was received so handle a disconnect.
                        temp = 6;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return temp;
    }

    public int changePassword(String oldPass, String newPass) {
        if (this.loggedIn) {
            try {
                String changePassMess = "04:CHANGEPASS:" + oldPass + ":" + newPass + ":::01";
                messages.put(changePassMess);
                // Possible semaphore
                Thread.sleep(500);
                return passwordError;
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
        return 2;
    }

    public boolean sendToChatroom(String name, String section, String message) {
        if (this.loggedIn && section != null & message != null && message.length() > 0) {
            try {
                String sendChatMess = "10:CHATMESS:" + name + ":" + section + ":" + this.userEmail + "::01:" + message;
                messages.put(sendChatMess);
                // possible semaphore
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
        if (chatError == 0) return true;
        return false;
    }

    public boolean sendFileToChatroom(Context context, String cName, String cSection, File file) {
        boolean success = false;
        if (loggedIn) {
            if (file.exists()) {
                long fileLength = file.length();
                files.add(file);
                String serverMessage = "13:SENDFILE:" + file.getName() + ":" + cName + ":" + cSection + ":INCOMING:01:" + fileLength + ":" + userEmail;
                try {
                    messages.put(serverMessage);
                    success = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        return success;
    }

    public void addMessageToChatrooms(String cName, String cSection, String sender, String message){
        this.chatrooms.addMessage(cName, cSection, sender, message);
    }

    public boolean updateAttendance(String cName, String section, ArrayList<String> attendance){
        boolean success = false;
        if (loggedIn && isProfessor){
            this.attendance = attendance;
            String serverMessage = "14:ATTENDANCE:" + cName + ":" + section + ":" + this.getUserEmail() + ":INCOMING:01";
            try {
                messages.put(serverMessage);
                success = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    public void sendPrivateTextMessage(String to, String message) {
        if (loggedIn && to.length() > 0 && message.length() > 0) {
            try {
                String serverMessage = "08:TEXTMESSAGE:" + to + ":" + this.userEmail + ":00:INCOMING:01";
                String mess = message + ":08";
                messages.put(serverMessage);
                messages.put(mess);
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }

        }
    }

    // public method which sends a logout request to the server.  Response is handled in the reply.
    public boolean logout() {
        if (loggedIn) {
            String logoutMess = "00:DISCONNECT:::::01";
            try {
                this.messages.put(logoutMess);
                this.connected = false;
                this.loggedIn = false;
                // Need a semaphore here.
                Thread.sleep(500);
                this.messageQueue.interrupt();
                this.messageHandler.interrupt();
                return true;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return false;
        } else {
            return false;
        }
    }

    // Private thread which listens for incoming messages from the server.
    private class MessageListener extends Thread {
        // private class fields

        // constructor
        public MessageListener() {

        }

        // run method
        @Override
        public void run() {
            boolean iterate = true;
            while (iterate) {
                try {
                    String mess = (String) in.readUTF();
                    String[] pieces = mess.split(":");
                    if (pieces[0].equals("00") && pieces[5].equals("GOODBYE") && pieces[6].equals("00")) {
                        break;
                    } else if (pieces[0].equals("02") && pieces[1].equals("GETLIST") && pieces[5].equals("INCOMING") && pieces[6].equals("00")) {
                        try {
                            Object obj = (Object) inFromServerObj.readObject();
                            try {
                                messages.put(obj);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        } catch (ClassNotFoundException ex) {
                            ex.printStackTrace();
                        }
                    } else if (pieces[1].equalsIgnoreCase("SENDFILE")) {
                        messages.put(mess);
                        waiter.set(true);
                        while (waiter.get()) {

                        }
                    } else if (pieces[1].equalsIgnoreCase("ATTENDANCE")) {
                        messages.put(mess);
                        waiter.set(true);
                        while (waiter.get()) {
                        }
                    } else {
                        try {
                            messages.put(mess);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Notify all that server is gone.
                    for (int c = 0; c < listeners.size();c++){
                        listeners.get(c).onDataLoaded("20:SERVERGONE:DISCONNECT::::00");
                    }

                    iterate = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Private thread which handles all incoming and outgoing messages.
    private class MessageQueue extends Thread {

        // class fields
        int listType;

        // class constructor
        public MessageQueue() {
            this.listType = 9;
        }

        // Thread start
        @Override
        public void run() {
            boolean iterate = true;
            while (iterate) {
                try {
                    Object object = (Object) messages.take();
                    switch (object.getClass().getSimpleName()) {
                        case "String": {
                            final String message = (String) object;
                            String[] pieces = message.split(":");
                            if (pieces[6].equals("00")) {   // incoming messages from server.
                                switch (pieces[0]) {
                                    case "00": {
                                        if (message.contains("GOODBYE")) {
                                            iterate = false;
                                            loggedIn = false;
                                            connected = false;
                                            messageHandler.interrupt();
                                            messageQueue.interrupt();
                                            client.close();
                                        }
                                        break;
                                    }
                                    case "01": {
                                        break;
                                    }
                                    case "02": {
                                        if (pieces[1].equals("GETLIST") && pieces[4].equals("00") && pieces[5].equals("INCOMING")) {
                                        }
                                        break;
                                    }
                                    case "03": {
                                        if (pieces[1].equals("CHANGESTATUS") && pieces[4].equals("00")) {
                                            // Here we need to actually change the person's status in connector since it has been verified.
                                        }
                                        break;
                                    }
                                    case "04": {
                                        if (pieces[5].equals("SUCCESS") && pieces[4].equals("00")) {
                                            passwordError = 0;
                                        } else if (pieces[5].equals("BADPASS") && pieces[4].equals("01")) {
                                            passwordError = 1;
                                        }
                                        break;
                                    }
                                    case "05": {
                                        break;
                                    }
                                    case "06": {
                                        if (pieces[1].equals("BUDDYONLINE")) {
                                            System.out.println(pieces[3] + " " + pieces[4] + " " + pieces[2]);
                                            // set buddy online for the chatroom passed in.
                                            Student stud = chatrooms.getStudent(pieces[3], pieces[4], pieces[2]);
                                            if (stud != null) {
                                                stud.setOnlineStatus(true);
                                            } else {
                                                // add the person to the list.
                                                chatrooms.addStudent(pieces[3], pieces[4], pieces[5], pieces[2], true, Integer.valueOf(pieces[7]));
                                            }
                                            for (int c = 0; c < listeners.size(); c++) {
                                                listeners.get(c).onDataLoaded(message);
                                            }

                                        } else if (pieces[1].equals("BUDDYOFFLINE")) {
                                            // set buddy offline for the chatroom passed in.
                                            Student stud = chatrooms.getStudent(pieces[3], pieces[4], pieces[2]);
                                            stud.setOnlineStatus(false);
                                            for (int c = 0; c < listeners.size(); c++) {
                                                listeners.get(c).onDataLoaded(message);
                                            }
                                        }
                                        break;
                                    }
                                    case "07": {
                                        if (pieces[5].equals("AVAILABLE")) {
                                            //buddies.setBuddyStatus(pieces[2], 0);
                                            //alertClient(new ActionEvent(this, 1, "07:" + pieces[2] + ":0"));
                                        } else if (pieces[5].equals("AWAY")) {
                                            //buddies.setBuddyStatus(pieces[2], 1);
                                            //alertClient(new ActionEvent(this, 1, "07:" + pieces[2] + ":1"));
                                        } else if (pieces[5].equals("UNAVAILABLE")) {
                                            //buddies.setBuddyStatus(pieces[2], 2);
                                            //alertClient(new ActionEvent(this, 1, "07:" + pieces[2] + ":2"));
                                        }
                                        break;
                                    }
                                    case "08": {
                                        if (pieces[1].equals("TEXTMESSAGE") && pieces[2].length() > 0 && pieces[3].length() > 0 && pieces[4].equals("00") && pieces[5].equals("INCOMING")) {
                                            String mess = (String) messages.take();
                                            if (mess.substring(mess.length() - 3, mess.length()).equals(":08")) {
                                                String sender = "08:" + pieces[2] + ":" + pieces[3] + ":" + pieces[5] + ":" + mess.substring(0, mess.length() - 3);
                                                for (int c = 0; c < listeners.size(); c++) {
                                                    listeners.get(c).onDataLoaded(sender);
                                                }
                                            }
                                        } else if (pieces[1].equals("TEXTMESSAGE") && pieces[2].length() > 0 && pieces[3].length() > 0 && pieces[4].equals("00")) {
                                            String chatMessage = "";
                                            if (pieces.length > 8) {
                                                for (int c = 7; c < pieces.length; c++) {
                                                    chatMessage += pieces[c] + ":";
                                                }
                                                chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                                            } else if (pieces.length < 8) {
                                                System.out.println(pieces.length);
                                                chatMessage = "Invalid Message!!!";
                                            } else {
                                                chatMessage = pieces[7];
                                            }
                                            String sender = "08:" + pieces[2] + ":" + pieces[3] + ":" + pieces[5] + ":" + chatMessage;
                                            for (int c = 0; c < listeners.size(); c++) {
                                                listeners.get(c).onDataLoaded(sender);
                                            }
                                        }
                                        break;
                                    }

                                    case "09": {
                                        break;
                                    }

                                    case "10": {
                                        if (pieces[5].equals("SUCCESS") && pieces[4].equals("00")) {
                                            chatError = 0;
                                        } else if (pieces[5].equals("FAILURE") && pieces[4].equals("01")) {
                                            chatError = 1;
                                        }
                                        break;
                                    }

                                    case "11": {
                                        if (pieces[1].equals("CHATMESS")) {
                                            // add to local copy of chatrooms and notify activity.
                                            String chatMessage = "";
                                            if (pieces.length > 8) {
                                                for (int c = 7; c < pieces.length; c++) {
                                                    chatMessage += pieces[c] + ":";
                                                }
                                                chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                                            } else {
                                                chatMessage = pieces[7];
                                            }
                                            chatrooms.addMessage(pieces[2], pieces[3], pieces[4], chatMessage);
                                            for (int c = 0; c < listeners.size(); c++) {
                                                listeners.get(c).onDataLoaded(message);
                                            }
                                        }
                                    }
                                    case "12": {
                                        if (pieces[1].equals("NEWSTUDENT")) {
                                            // pull new chat rooms from the server
                                            getChatroomsFromServer();
                                            System.out.println("New Chatrooms received.");
                                        }
                                    }
                                    case "13": {
                                        if (pieces[1].equalsIgnoreCase("SENDFILE") && pieces[5].equalsIgnoreCase("INCOMING")) {

                                            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), pieces[3] + "_" + pieces[4]);
                                            //File mediaStorageDir = new File("/data/data/edu.uncg.studdybuddy.studybuddy", pieces[3] + "_" + pieces[4]);
                                            if (!mediaStorageDir.exists()) {
                                                if (!mediaStorageDir.mkdirs()) {
                                                    Log.d("App", "failed to create directory");
                                                }
                                            }
                                            // Create local folder from class name.
                                            File imageFile = new File(mediaStorageDir.getAbsolutePath() + "/" + pieces[2]);
                                            if (!imageFile.exists()){
                                                imageFile.createNewFile();
                                            }

                                            FileOutputStream fos = new FileOutputStream(imageFile, false);
                                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                                            int fileSize = in.readInt();
                                            byte[] incomingFile = new byte[fileSize];
                                            in.readFully(incomingFile, 0, incomingFile.length);
                                            bos.write(incomingFile, 0, incomingFile.length);
                                            bos.close();
                                            fos.close();

                                            // Store file in the folder.
                                            if (imageFile.exists()){
                                                System.out.println("File");
                                                System.out.println(imageFile.getPath());
                                            } else {
                                                System.out.println("No File");
                                            }


                                            for (int c = 0; c < listeners.size(); c++) {
                                                listeners.get(c).onDataLoaded(message);
                                            }
                                            waiter.set(false);
                                        } else if (pieces[1].equalsIgnoreCase("SENDFILE") && pieces[5].equalsIgnoreCase("ACCEPTED")) {
                                            // Create a new thread here that listens for an incoming connection from server.
                                            Thread thread = new Thread() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        File file = files.get(0);
                                                        files.remove(file);
                                                        files.trimToSize();
                                                        FileInputStream fis = new FileInputStream(file);
                                                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bmOptions);
                                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                                        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                                                        byte[] array = bos.toByteArray();
                                                        out.writeInt(array.length);
                                                        out.write(array, 0, array.length);
                                                        out.flush();
                                                        fis.close();
                                                        waiter.set(false);
                                                        for (int c = 0; c < listeners.size();c++){
                                                            listeners.get(c).onDataLoaded(message);
                                                        }
                                                    } catch (IOException ex) {
                                                        System.out.println(ex);
                                                    }
                                                }
                                            };

                                            thread.start();
                                        }
                                    }
                                    case "14": {
                                        if (pieces[1].equalsIgnoreCase("ATTENDANCE") && pieces[5].equalsIgnoreCase("ACCEPTED")) {
                                            out.writeInt(attendance.size());
                                            for (int c = 0;c < attendance.size();c++){
                                                out.writeUTF(attendance.get(c));
                                            }
                                            waiter.set(false);
                                        }
                                    }

                                    default: {
                                    }
                                }
                                break;
                            } else if (pieces[6].equals("01")) {    // Outgoing messages from client.
                                if (pieces[1].equals("TEXTMESSAGE") && pieces[5].equals("INCOMING")) {
                                    String textMessage = (String) messages.take();
                                    out.writeUTF(message);
                                    out.writeUTF(textMessage);
                                } else {
                                    out.writeUTF(message);
                                }
                            } else {
                                logout();
                            }
                            break;
                        }
                        case "Chatrooms": {
                            chatrooms = null;
                            chatrooms = (Chatrooms) object;
                            if (!isProfessor) {
                                Student stud = chatrooms.getStudent(userEmail);
                                if (stud.getStudentName() != null) {
                                    userName = stud.getStudentName();
                                } else {
                                    userName = userEmail;
                                }
                            } else {
                                userName = userEmail;
                            }


                            Thread.sleep(500);
                            if (!listeners.isEmpty()) {
                                listeners.get(0).onObjectReady("Chatrooms");
                            }
                            break;
                        }
                        default: {
                            logout();
                        }
                    }
                } catch (InterruptedException | IOException | IllegalMonitorStateException ex) {
                    messageQueue.interrupt();
                    messageHandler.interrupt();
                }
            }
        }
    }

    public interface MyCustomObjectListener {
        // need to pass relevant arguments related to the event triggered
        public void onObjectReady(String title);

        // or when data has been loaded
        public void onDataLoaded(String data);
    }
}
