package StudyBuddy;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final LinkedBlockingQueue<Object> messages;
    private Thread messageHandling;
    private Thread objectMessageListener;
    private final Database database;
    private final Chatrooms mainChatrooms;
    private Chatrooms userChatrooms;
    private final ChatRoomBackups backup;
    private final AtomicBoolean waiting = new AtomicBoolean(false);

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
        this.messages = new LinkedBlockingQueue<>();
        this.database = database;
        this.mainChatrooms = mainChats;
        this.userChatrooms = null;
        this.backup = new ChatRoomBackups();
    }

    @Override
    public void run() {
        // Build the user's copy of the Chatrooms class when a new session is created for a user.
        this.userChatrooms = this.buildUserChatrooms(this.userName);
        // Notify all of the user's classmates that the user has logged in.
        this.broadcastOnlineBuddies(true);
        // Create the user's message listener and handler threads.
        this.messageHandling = new Thread(new MessageQueue());
        this.messageHandling.start();
        this.objectMessageListener = new Thread(new ObjectMessageHandler());
        this.objectMessageListener.start();
    }

    private void sendFile(byte[] file) {
        try {
            dataOut.writeInt(file.length);
            dataOut.write(file);
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendChatMessage(String name, String section, String sender, String message) {
        if (this.onlineList.contains(sender) && this.mainChatrooms.classContainsStudent(name, section, sender)) {
            this.mainChatrooms.addMessage(name, section, sender, message);  // Add the new message to the main chat rooms list.
            this.backup.saveChatRoomStatus(this.mainChatrooms); // Save the change to hard drive.
            Student[] students = this.mainChatrooms.getStudents(name, section);
            // Send the message out to all students who are online.
            for (Student student : students) {
                if (student.getOnlineStatus()) {
                    Session theirSession = (Session) this.onlineList.returnUserSession(student.getStudentEmail());
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
            // If there is an attempt to send to a student who is not in this particular class, notify of failure.
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

    private void sendChatFile(String name, String section, String sender, File file) {
        if (this.onlineList.contains(sender) && this.mainChatrooms.classContainsStudent(name, section, sender)) {
            Student[] students = this.mainChatrooms.getStudents(name, section);
            byte[] buffer = getBytes(file);
            for (Student student : students) {
                if (student.getOnlineStatus()) {
                    Session theirSession = (Session) this.onlineList.returnUserSession(student.getStudentEmail());
                    String serverMessage = "13:SENDFILE:" + file.getName() + ":" + name + ":" + section + ":INCOMING:00:" + file.length() + ":" + sender;
                    if (!student.getStudentEmail().equalsIgnoreCase(userName)) {
                        theirSession.sendData(serverMessage);
                        theirSession.sendFile(buffer);
                    }
                }
            }
        }
    }

    private byte[] getBytes(File file) {
        FileInputStream input = null;
        if (file.exists()) {
            try {
                input = new FileInputStream(file);
                int len = (int) file.length();
                byte[] data = new byte[len];
                int count, total = 0;
                while ((count = input.read(data, total, len - total)) > 0) {
                    total += count;
                }
                return data;
            } catch (IOException ex) {
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return null;
    }

    public void sendMessage(String mess) {
        try {
            messages.put(mess);
        } catch (InterruptedException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getClientIP() {
        return this.clientIP;
    }

    private void sendData(String mess) {
        try {
            dataOut.writeUTF(mess);
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendInteger(int input) {
        try {
            dataOut.writeInt(input);
        } catch (IOException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean sharesClassWith(String otherEmail) {
        boolean success = false;
        // Loop through all the classes.
        for (int c = 0; c < this.userChatrooms.getNumberOfClasses(); c++) {
            // Get all the students from a particular class.
            Student[] students = this.userChatrooms.getChatroom(c).getStudents();
            // loop through all of the students.
            for (int d = 0; d < students.length; d++) {
                // if any match is found, return true.
                if (students[d].getStudentEmail().equalsIgnoreCase(otherEmail)) {
                    return true;
                }
            }
        }
        // otherwise return false.
        return success;
    }

    private void sendTextMessage(String to, String from, String message) {
        // If 'to' and 'from' are actually members 
        if (database.getUserID(to) != 0 && database.getUserID(from) != 0) {
            // If 'to' is currently online and not unavailable, then attempt to send message directly. 
            if (onlineList.contains(to) && onlineList.getClientStatus(to) != 2) {
                // Create a temp session reference from 'to's currrent session. 
                Session toSession = (Session) onlineList.returnUserSession(to);
                // Check 'to's buddy list to verify 'to' and 'from' are actually buddies. 

                if (toSession.sharesClassWith(from)) {
                    // At this point, 'to' is online and ready to receive a message. 
                    String mess = "08:TEXTMESSAGE:" + to + ":" + from + ":00:INCOMING:00";
                    String mess2 = message + ":08";
                    String mess3 = "08:TEXTMESSAGE:" + to + ":" + from + ":00:SENT:00:" + message;
                    try {
                        toSession.sendData(mess);
                        toSession.sendData(mess2);
                        messages.put(mess3);
                        Date curDate = new Date();
                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Message:::: Request from: " + from + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " to " + to + " @ " + toSession.con.getRemoteSocketAddress().toString().substring(1) + " - Message Sent - Message sent directly.");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    // Reply to sender the buddy 'to' is not in their list. 
                    String mess = "08:TEXTMESSAGE:" + to + ":" + from + ":01:NONBUDDY:01";
                    try {
                        messages.put(mess);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } // If 'to' is not online, then process the message with the database. 
            else {
                // This is where to add code to store message in the database for later sending. 

                if (database.addOfflineMessage(to, from, message)) {
                    String mess3 = "08:TEXTMESSAGE:" + to + ":" + from + ":00:STORED:00:" + message;
                    try {
                        messages.put(mess3);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Date curDate = new Date();
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Message:::: Request from: " + from + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " to " + to + " - Message Stored - Stored in Database.");
                } else {
                    Session toSession = (Session) onlineList.returnUserSession(to);
                    Date curDate = new Date();
                    System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Message:::: Request from: " + from + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " to " + to + " - Message Rejected - Message was disreguared.");
                }
            }
        } else {
            // Reply to sender the 'to' and 'from' fields were populated with non-members. 
            String mess = "08:TEXTMESSAGE:" + to + ":" + from + ":01:NONMEMBER:01";
            try {
                messages.put(mess);
            } catch (InterruptedException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        sendData(reply);
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
            // Need to create a new chatrooms.
            this.messages.put(this.userChatrooms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void broadcastOnlineBuddies(boolean online) {
        //this.userChatrooms = this.buildUserChatrooms(userName);
        String realName = "";
        int availability = 0;
        // Pull the user's information from the database.
        ResultSet studentInfo = this.database.returnUserInfo(this.userName);
        try {
            while (studentInfo.next()) {
                realName = studentInfo.getString(4) + " " + studentInfo.getString(5);
                availability = this.database.getUserStatus(studentInfo.getString(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int c = 0; c < this.userChatrooms.getNumberOfClasses(); c++) {
            String[] roomList = this.userChatrooms.getClassNamesAndSection();
            String[] pieces = roomList[c].split(":");
            Student[] students = this.userChatrooms.getStudents(pieces[0], pieces[1]);
            for (Student student : students) {
                if (student.getStudentEmail().equalsIgnoreCase(userName)) {
                } else {
                    if (student.getOnlineStatus()) {
                        Session tempSess = (Session) onlineList.returnUserSession(student.getStudentEmail());
                        String newBuddy;
                        if (online) {
                            newBuddy = "06:BUDDYONLINE:" + userName + ":" + pieces[0] + ":" + pieces[1] + ":" + realName + ":00:" + availability;
                        } else {
                            newBuddy = "06:BUDDYOFFLINE:" + userName + ":" + pieces[0] + ":" + pieces[1] + "::00";
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
                    String[] pieces = message.split(":");
                    if (pieces[1].equalsIgnoreCase("SENDFILE") && pieces[5].equalsIgnoreCase("INCOMING")) {
                        waiting.set(true);
                        messages.put(message);
                        while (waiting.get());
                    } else {
                        messages.put(message);
                    }

                } catch (IOException ex) {
                    // Client dropped the connection
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
                                    case "08": {
                                        if (pieces[1].equals("TEXTMESSAGE") && pieces[2].length() > 0 && pieces[3].length() > 0 && pieces[5].equals("INCOMING")) {
                                            String inText = (String) messages.take();
                                            if (inText.substring(inText.length() - 3, inText.length()).equals(":08")) {
                                                String mess = inText.substring(0, inText.length() - 3);
                                                sendTextMessage(pieces[2], pieces[3], mess);
                                            } else {
                                                invalid();
                                            }
                                        } else {
                                            invalid();
                                        }
                                    }
                                    break;

                                    case "09": {
                                        break;
                                    }

                                    case "10": {
                                        if (pieces[1].equals("CHATMESS")) {
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
                                            sendChatMessage(pieces[2], pieces[3], pieces[4], chatMessage);
                                            break;
                                        } else {
                                            invalid();
                                            break;
                                        }
                                    }
                                    case "13": {
                                        if (pieces[1].equalsIgnoreCase("SENDFILE") && pieces[5].equalsIgnoreCase("INCOMING")) {
                                            String clientMessage = "13:SENDFILE:" + pieces[2] + ":" + pieces[3] + ":" + pieces[4] + ":ACCEPTED:00";
                                            sendData(clientMessage);
                                            Thread thread = new Thread() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        String AbsolutePath = System.getProperty("user.dir");
                                                        File directory = new File(AbsolutePath + "/" + pieces[3]);
                                                        directory.mkdirs();
                                                        File tempFile = new File(AbsolutePath + "/" + pieces[3] + "/" + pieces[2]);
                                                        tempFile.createNewFile();
                                                        FileOutputStream fos = new FileOutputStream(tempFile, false);
                                                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                                                        int length = dataIn.readInt();
                                                        byte[] fileArray = new byte[length];
                                                        dataIn.readFully(fileArray, 0, fileArray.length);
                                                        bos.write(fileArray, 0, fileArray.length);
                                                        bos.close();
                                                        Date curDate = new Date();
                                                        System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::SendFile::: Request from: " + userName + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - File Received.");
                                                        fos.close();
                                                        waiting.set(false);

                                                        // Update main chat list with new image.
                                                        // Send image back to every one online in this room.
                                                        sendChatFile(pieces[3], pieces[4], pieces[8], tempFile);

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };

                                            thread.start();
                                        }
                                        break;
                                    }
                                    default:
                                        invalid();

                                }
                                // If the message was sent by the server....
                            } else if (pieces[6].equals("00")) {
                                // If the user wishes to log out of the server, then call logout.
                                if (pieces[0].equals("00") && pieces[1].equals("DISCONNECT") && pieces[4].equals("00")) {
                                    sendData(message);
                                    userLogout();
                                    // If the user requests their buddy list, then send the list.
                                } else if (pieces[0].equals("02") && pieces[1].equals("GETLIST") && pieces[4].equals("00")) {
                                    sendData(message);
                                    sendChatrooms();
                                } else if (pieces[0].equals("08") && pieces[1].equals("TEXTMESSAGE") && pieces[5].equals("INCOMING")) {
                                    System.out.println("We are sending a message out to: " + message);
                                    sendData(message);

                                } // If none of the above conditions are met, just send the message back to client.
                                else {
                                    sendData(message);
                                }
                                // If message does not end in either 00 or 01, then an invalid response is received and the session is terminated.
                            } else {
                                invalid();
                            }
                            break;
                        }
                        // If object placed on message queue is of type Chatrooms, then send as object.
                        case "Chatrooms":
                            objectOut.flush();
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
