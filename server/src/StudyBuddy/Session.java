package StudyBuddy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Session extends Thread {

    // Private class fields.
    private Socket con;
    private final ObjectInputStream objectIn;
    private final ObjectOutputStream objectOut;
    private final DataInputStream dataIn;
    private final DataOutputStream dataOut;
    private final String clientIP;
    private final String userName;
    private final OnlineClientList onlineList;
    //private BuddyList buddies;
    private final LinkedBlockingQueue<Object> messages;
    private Thread messageHandling;
    private Thread objectMessageListener;
    private final Database database;
    private Chatrooms mainChatrooms;
    private Chatrooms userChatrooms;

    // Public constructor.
    public Session(Socket con, DataInputStream in, DataOutputStream out, ObjectInputStream inObject, ObjectOutputStream outObject, OnlineClientList list, String user, Database database, Chatrooms mainChats) {
        this.con = con;
        this.onlineList = list;
        this.clientIP = con.getRemoteSocketAddress().toString().substring(1);
        this.dataIn = in;
        this.dataOut = out;
        this.objectIn = inObject;
        this.objectOut = outObject;
        this.userName = user;
        this.messages = new LinkedBlockingQueue<Object>();
        this.database = database;
        this.mainChatrooms = mainChats;
        this.userChatrooms = null;
    }

    @Override
    public void run() {
        this.userChatrooms = this.buildUserChatrooms(this.userName);
        //this.populateBuddyList();
        this.broadcastOnlineBuddies(true);
        this.messageHandling = new Thread(new MessageQueue());
        this.messageHandling.start();
        this.objectMessageListener = new Thread(new ObjectMessageHandler());
        this.objectMessageListener.start();
    }

    private void sendChatMessage(String name, String section, String sender, String message) {
        if (this.onlineList.contains(sender) && this.mainChatrooms.classContainsStudent(name, section, sender)) {
            this.mainChatrooms.addMessage(name, section, sender, message);
            Student[] students = this.mainChatrooms.getStudents(name, section);
            for (int c = 0; c < students.length; c++) {
                if (students[c].getOnlineStatus()) {
                    Session theirSession = (Session) this.onlineList.returnUserSession(students[c].getStudentEmail());
                    String mess = "11:CHATMESS:" + name + ":" + section + ":" + sender + "::00:" + message;
                    theirSession.sendMessage(mess);
                    String error = "10:CHATMESS:" + name + ":" + section + ":00:SUCCESS:00";
                    try {
                        this.messages.put(error);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            String error = "10:CHATMESS:" + name + ":" + section + ":01:FAILURE:00";
            try {
                this.messages.put(error);
            } catch (InterruptedException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
            Date curDate = new Date();
            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::ChatMess::: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Send message FAILED");
        }
    }

    private void sendMessage(String mess) {
        try {
            messages.put(mess);
        } catch (InterruptedException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendData(String mess) {
        try {
            dataOut.writeUTF(mess);
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void userLogout() {
        // Remove user from the online list.
        onlineList.removeClient(userName);
        // Update user status in the database.
        database.updateUserLoggedIn(this.userName, false);
        // Set Offline in Chatroom class.
        ResultSet rooms = database.returnAllClassesByStudent(userName);
        try {
            while (rooms.next()) {
                Student stud = this.mainChatrooms.getStudent(rooms.getString(1), rooms.getString(2), userName);
                stud.setOnlineStatus(false);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Notify the server log that a user is logging out.
        Date curDate = new Date();
        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Disconnect: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Disconnected.");
        // Notify other online users that a buddy is logging off the network.
        // Check to see if the user has actually logged out or has just vanished and close.
        if (messageHandling.getState().toString().equals("WAITING")) {
            try {
                messageHandling.stop();
                objectMessageListener.stop();
                this.objectIn.close();
                this.objectOut.close();
                this.con.close();
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            objectMessageListener.interrupt();

            try {
                this.objectIn.close();
                this.objectOut.close();
                this.con.close();
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
            messageHandling.interrupt();
        }
    }

    private void invalid() {
        String reply = "09:INVALIDINPUT:::01:INVALID:00";
        try {
            this.dataOut.writeUTF(reply);
        } catch (IOException ex) {
            System.out.println("Invalid packet send exception.");
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
        onlineList.removeClient(userName);
        Date curDate = new Date();
        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
        this.con = null;
        this.objectMessageListener.stop();
        this.messageHandling.stop();
    }

    private void changePassword(String oldPass, String newPass) {
        try {
            ResultSet result = database.returnUserInfo(userName);
            result.next();
            if (!result.getString("sPass").equals(oldPass)) {
                String error = "04:CHANGEPASS:" + oldPass + ":" + newPass + ":01:BADPASS:00";
                this.messages.put(error);
                Date curDate = new Date();
                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::ChangePass: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Unsuccessful due to bad password.");
            } else {
                database.updateUserPassword(userName, newPass);
                String error = "04:CHANGEPASS:" + oldPass + ":" + newPass + ":00:SUCCESS:00";
                this.messages.put(error);
                Date curDate = new Date();
                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::ChangePass: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Password Changed.");
            }
        } catch (SQLException | InterruptedException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendChatrooms() {
        if (this.userChatrooms == null) {
            System.out.println("Chatrooms were not populated.");
            return;
        }
        Date curDate = new Date();
        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Chatrooms:: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Sending Chatrooms.");
        try {
            this.messages.put(this.userChatrooms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void broadcastOnlineBuddies(boolean online) {
        //this.userChatrooms = this.buildUserChatrooms(userName);
        for (int c = 0; c < this.userChatrooms.getNumberOfClasses(); c++) {
            String[] roomList = this.userChatrooms.getClassNamesAndSection();
            String[] pieces = roomList[c].split(":");
            Student[] students = this.userChatrooms.getStudents(pieces[0], pieces[1]);
            for (int d = 0; d < students.length; d++) {
                if (students[d].getStudentEmail().equalsIgnoreCase(userName)) {
                } else {
                    if (students[d].getOnlineStatus()) {
                        Session tempSess = (Session) onlineList.returnUserSession(students[d].getStudentEmail());
                        String newBuddy;
                        if (online) {
                            newBuddy = "06:BUDDYONLINE:" + userName + ":" + pieces[0] + ":" + pieces[1] + "::00";
                            System.out.println("User " + userName + " is online for user " + students[d].getStudentEmail());
                        } else {
                            newBuddy = "06:BUDDYOFFLINE:" + userName + ":" + pieces[0] + ":" + pieces[1] + "::00";
                            System.out.println("User " + userName + " went offline for user " + students[d].getStudentEmail());
                        }
                        tempSess.sendMessage(newBuddy);
                    }

                }
            }
        }
    }

    private Chatrooms buildUserChatrooms(String email) {
        Chatrooms temp = new Chatrooms();
        ResultSet rooms = database.returnAllClassesByStudent(email);
        try {
            while (rooms.next()) {
                temp.addChatroom(mainChatrooms.getChatroom(rooms.getString(1), rooms.getString(2)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }

        return temp;
    }

    private class ObjectMessageHandler extends Thread {
        // Class Fields

        // Overridden run method
        public void run() {
            boolean iterate = true;
            while (iterate) {
                try {
                    String message = (String) dataIn.readUTF();
                    messages.put(message);
                } catch (IOException ex) {
                    //System.out.println(ex);
                    //System.out.println("Client Vanished");
                    if (onlineList.contains(userName)) {
                        userLogout();
                    }
                    iterate = false;
                    break;
                } catch (InterruptedException ex) {
                    System.out.println("Message put error.");
                    Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class MessageQueue extends Thread {
        // Class Fields

        // Overridden run method
        @Override
        public void run() {
            boolean iterate = true;
            while (iterate) {
                try {
                    Object object = (Object) messages.take();
                    switch (object.getClass().getSimpleName()) {
                        case "String": {
                            String message = (String) object;
                            String[] pieces = message.split(":");
                            //System.out.println(pieces[0] + "\t" + pieces[1] + "\t" + pieces[5] + "\t" + pieces[6]);
                            if (pieces[6].equals("01")) {
                                switch (pieces[0]) {
                                    // Disconnect from the server.
                                    case "00": {
                                        if (pieces[1].equals("DISCONNECT")) {
                                            message = pieces[0] + ":" + pieces[1] + ":" + pieces[2] + ":" + pieces[3] + ":00:GOODBYE:00";
                                            messages.put(message);
                                            broadcastOnlineBuddies(false);
                                            break;
                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    // Login to the server.
                                    case "01": {
                                        if (pieces[1].equals("LOGIN")) {
                                            message = pieces[0] + ":" + pieces[1] + ":" + pieces[2] + ":" + pieces[3] + ":01:USER ALREADY LOGGED IN:00";
                                            messages.put(message);
                                            break;
                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    // Retrieve the user's list of chat rooms.
                                    case "02": {
                                        if (pieces[1].equals("GETLIST")) {
                                            message = pieces[0] + ":" + pieces[1] + ":" + pieces[2] + ":" + pieces[3] + ":00:INCOMING:00";
                                            messages.put(message);
                                            break;

                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    // Change the user's status.
                                    case "03":/*
                                            if (pieces[1].equals("CHANGESTATUS")) {
                                                if (database.updateUserStatus(userName, Integer.valueOf(pieces[2]))) {
                                                    message = pieces[0] + ":" + pieces[1] + ":" + pieces[2] + ":" + pieces[3] + ":00:STATUSCHANGED:00";
                                                    messages.put(message);
                                                    Date curDate = new Date();
                                                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::StatusChg:: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Status Updated.");
                                                    broadcastStatusChangeToBuddies(Integer.valueOf(pieces[2]));
                                                } else {
                                                    message = pieces[0] + ":" + pieces[1] + ":" + pieces[2] + ":" + pieces[3] + ":01:ERROR:00";
                                                    messages.put(message);
                                                    Date curDate = new Date();
                                                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::StatusChg:: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Status NOT Updated.");
                                                }
                                                break;
                                            } else {
                                                invalid();
                                                break;
                                            }*/
                                        break;
                                    // Change the user's password.
                                    case "04": {
                                        if (pieces[1].equals("CHANGEPASS")) {
                                            changePassword(pieces[2], pieces[3]);
                                            break;
                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    // Handshake
                                    case "05":
                                        break;
                                    // Broadcast when buddies come and go offline.
                                    case "06":
                                        break;
                                    // Broadcast when buddies change their status.
                                    case "07":
                                        break;
                                    case "08":
                                        /*{
                                            if (pieces[1].equals("TEXTMESSAGE") && pieces[2].length() > 0 && pieces[3].length() > 0 && pieces[5].equals("INCOMING")) {
                                                String inText = (String) messages.take();
                                                if (inText.substring(inText.length() - 3, inText.length()).equals(":08")) {
                                                    String mess = inText.substring(0, inText.length() - 3);
                                                    sendTextMessage(pieces[2], pieces[3], mess);
                                                } else {
                                                    invalid();
                                                }
                                                break;
                                            } else {
                                                invalid();
                                                break;
                                            }
                                        }*/
                                        break;

                                    case "09": {
                                        break;
                                    }

                                    case "10": {
                                        if (pieces[1].equals("CHATMESS")) {
                                            sendChatMessage(pieces[2], pieces[3], pieces[4], pieces[7]);
                                            break;
                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    default:
                                        invalid();

                                }
                                // If the message was sent by the server....
                            } else if (pieces[6].equals("00")) {
                                // If the user wishes to log out of the server, then call logout.
                                if (pieces[0].equals("00") && pieces[1].equals("DISCONNECT") && pieces[4].equals("00")) {
                                    dataOut.writeUTF(message);
                                    userLogout();
                                    // If the user requests their buddy list, then send the list.
                                } else if (pieces[0].equals("02") && pieces[1].equals("GETLIST") && pieces[4].equals("00")) {
                                    dataOut.writeUTF(message);
                                    sendChatrooms();
                                } else if (pieces[0].equals("08") && pieces[1].equals("TEXTMESSAGE") && pieces[5].equals("INCOMING")) {
                                    System.out.println("We are sending a message out to: " + message);

                                } else if (pieces[0].equals("06")) {
                                    dataOut.writeUTF(message);
                                } // If none of the above conditions are met, just send the message back to client.
                                else {
                                    dataOut.writeUTF(message);
                                }
                                // If message does not end in either 00 or 01, then an invalid response is received and the session is terminated.
                            } else {
                                invalid();
                            }
                            break;
                        }
                        // If object placed on message queue is of type Chatrooms, then send as object.
                        case "Chatrooms":
                            objectOut.writeObject((Chatrooms) object);
                            break;
                        // If any of type of object, then an invalid response is received and the session is terminated.
                        default: {
                            invalid();
                        }

                    }
                } catch (InterruptedException | IOException e) {
                    this.stop();
                    iterate = false;
                }
            }
        }
    }
}
