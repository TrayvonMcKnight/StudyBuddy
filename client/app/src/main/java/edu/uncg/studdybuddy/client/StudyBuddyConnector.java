package edu.uncg.studdybuddy.client;

import android.os.StrictMode;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EventListener;
import java.util.concurrent.LinkedBlockingQueue;

import StudyBuddy.Chatrooms;
import edu.uncg.studdybuddy.events.Event;
import edu.uncg.studdybuddy.events.EventDispatcher;

/**
 * Created by Anthony Ratliff, Trayvon McKnight and Jlesa Carr on 2/10/2017.
 */

public class StudyBuddyConnector extends EventDispatcher {
    // Private class fields
    private final String IP = "192.168.0.5";   // byte array to hold server IP address.
    private final int port = 6000; // integer to hold server port number.
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
    private boolean loggedIn;  // Boolean to hold connection status.
    private boolean connected;
    private String userName;    // String to represent the username.
    private LinkedBlockingQueue<Object> messages;
    private Thread messageHandler;
    private Thread messageQueue;
    private int passwordError;
    private Chatrooms chatrooms;

    // Class constructor
    public StudyBuddyConnector(){

        this.loggedIn = false;
        this.connected = false;
        this.messageHandler = null;
        this.messageQueue = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            this.address = InetAddress.getByName(IP);   // Bind the InetAddress to the server IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // Public class methods

    public StudyBuddyConnector getInstance(){
        return this;
    }

    public void myCallback(){
        Event event = new Event(Event.CHATROOMS);
        event.setMessage("The Chat rooms are here.");
        event.setChatrooms(this.chatrooms);
        dispatchEvent(event);
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    public boolean hasConnection(){
        return this.connected;
    }

    public String getUserName() {
        return this.userName;
    }

    public void close() {
        try {
            this.client.close();
            this.loggedIn = false;
            this.connected = false;
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
            out.writeUTF(this.greetString); // Send handshake string though data stream.
            out.flush();
            this.inFromServer = client.getInputStream(); // Instantiate input stream from socket.
            this.in = new DataInputStream(inFromServer); // Instantiate data stream from input stream.

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
    public int login(String user, String pass) {
        String answer = ""; // initialize the reply string so scope will fall outside of Try/Catch block.
        String[] pieces = {};
        try {
            String login = "01:LOGIN:" + user + ":" + pass + ":::01";
            this.out.writeUTF(login);
            String reply = (String) in.readUTF();
            pieces = reply.split(":");
            answer = pieces[5];

        } catch (IOException ex) {
            System.out.println("Message from the outToServerObj not being created.");
            ex.printStackTrace();
        }
        switch (answer) {
            // If the login is accepted...
            case "ACCEPTED":
                loggedIn = true;
                this.userName = user;
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

    public int createNewUser(String email, String pass1, String pass2, String fName, String lName){
        int temp = 5;
        if (!this.loggedIn){
            String answer = ""; // initialize the reply string so scope will fall outside of Try/Catch block.
            String[] pieces = {};
            String create = "09:CREATEACCOUNT:" + email + ":" + pass1 + ":" + pass2 +":"+ fName + ":" + lName;
            try {
                this.out.writeUTF(create);
                String reply = (String) in.readUTF();
                pieces = reply.split(":");
                answer = pieces[4];
                switch (answer){
                    case "00":{
                        temp =0;
                        break;
                    }
                    case "01":{
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
                Thread.sleep(500);
                return passwordError;
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
        return 2;
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


    public boolean logout() {
        if (loggedIn) {
            String logoutMess = "00:DISCONNECT:::::01";
            try {
                this.messages.put(logoutMess);
                this.connected = false;
                Thread.sleep(500);
                return true;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return false;
        } else {
            return false;
        }
    }

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
                    } else {
                        try {
                            messages.put(mess);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    iterate = false;
                }
            }
        }
    }

















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
                    //System.out.println(object.getClass().getSimpleName());
                    switch (object.getClass().getSimpleName()) {
                        case "String": {
                            String message = (String) object;
                            String[] pieces = message.split(":");
                            //System.out.println(pieces[0] + "\t" + pieces[1] + "\t" + pieces[5] + "\t" + pieces[6]);
                            if (pieces[pieces.length - 1].equals("00")) {
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
                                            // Here we need to actually change the person's status in IRC since it has been verified.
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
                                            //buddies.setOnlineStatus(pieces[2], true);
                                            //alertClient(new ActionEvent(this, 1, pieces[1] + ":" + pieces[2]));

                                        } else if (pieces[1].equals("BUDDYOFFLINE")) {
                                            //buddies.setOnlineStatus(pieces[2], false);
                                            //alertClient(new ActionEvent(this, 1, pieces[1] + ":" + pieces[2]));
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
                                        if (pieces[1].equals("TEXTMESSAGE") && pieces[2].length() > 0 && pieces[3].length() > 0 && pieces[4].equals("00") && pieces[5].equals("INCOMING")){
                                            String mess = (String) messages.take();
                                            if (mess.substring(mess.length() - 3, mess.length()).equals(":08")){
                                                String sender = "08:" + pieces[3] + ":" + mess.substring(0, mess.length() - 3);
                                                //alertClient(new ActionEvent(this, 1, sender));
                                            }

                                        }
                                        break;
                                    }
                                    default: {
                                    }
                                }
                                break;
                            } else if (pieces[pieces.length - 1].equals("01")) {
                                if (pieces[1].equals("TEXTMESSAGE") && pieces[5].equals("INCOMING")){
                                    String textMessage = (String)messages.take();
                                    out.writeUTF(message);
                                    out.writeUTF(textMessage);
                                } else{
                                    out.writeUTF(message);
                                }
                            } else {
                                logout();
                            }
                            break;
                        }
                        case "Chatrooms": {
                            chatrooms = (Chatrooms) object;
                            myCallback();
                            //alertClient(new ActionEvent(this, 1, "INCOMING:BUDDYLIST"));
                            break;
                        }
                        default: {
                            logout();
                        }
                    }
                } catch (InterruptedException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
