
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

public class StudyBuddyServer extends Thread{
    private ServerSocket serverSocket;
    private Database database;
    private OnlineClientList onlineList;
    
    public StudyBuddyServer(int port){
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
                    Thread login = new Thread(new LoginThread(server, this.onlineList));
                    login.start();
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
    
    private class LoginThread extends Thread {
        private ResultSet result;
        private final Socket con;
        private final OnlineClientList onlineList;
        private String userName, passWord;
        private final Thread loginThread;

        public LoginThread(Socket con, OnlineClientList list) {
            this.con = con;
            this.onlineList = list;
            this.loginThread = this;
        }

        @Override
        public void run() {
            try {
                // Create and Instanciate both the object input and output streams from the connection object.
                ObjectInputStream inFromClient = new ObjectInputStream(con.getInputStream());
                ObjectOutputStream outToClient = new ObjectOutputStream(con.getOutputStream());
                DataInputStream inStream = new DataInputStream(con.getInputStream());
                DataOutputStream outStream = new DataOutputStream(con.getOutputStream());

                // Set up a loop which will iterate 3 times in an attempt to allow the user to log into the server.
                for (int counter = 1; counter <= 3; counter++) {
                    String login = (String) inStream.readUTF();
                    String[] pieces = login.split(":");
                    if (pieces[1].equals("LOGIN") && pieces[0].equals("01")) {
                        if (!pieces[2].equals("null") && !pieces[3].equals("null")){                           
                            this.userName = pieces[2];
                            this.passWord = pieces[3];
                            result = database.returnUserInfo(this.userName);

                            if (!result.next()) {
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - No such user.");
                                if (counter != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":01:NOUSER:00";
                                    outStream.writeUTF(reply);
                                }
                            } else if (result.getString("pass_word").equals(passWord)) {
                                Date curDate = new Date();
                                database.updateLastLoginTime(this.userName);
                                database.updateUserLoggedIn(this.userName, true);
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + result.getString("user_name") + " @ " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Accepted.");
                                String reply = "01:LOGIN:" + database.getUserStatus(userName) + ":" + pieces[3] + ":00:ACCEPTED:00";
                                outStream.writeUTF(reply);
                                //Session sess = new Session(con, inStream, outStream, inFromClient, outToClient, this.onlineList, result.getString("user_name"));
                                //Thread session = new Thread(sess);
                                //onlineList.addClient(result.getString("user_name"), result.getString("email"), con.getRemoteSocketAddress().toString().substring(1), database.getUserStatus(userName), inFromClient, outToClient, sess);
                                //session.start();
                                break;
                            } else if (!result.getString("pass_word").equals(passWord)) {
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Incorrect password.");
                                if (counter != 3) {
                                    String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":02:BADPASS:00";
                                    outStream.writeUTF(reply);
                                }
                            }
                            if (counter == 3) {
                                Date curDate = new Date();
                                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - Too many failed attempts.");
                                String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":03:GOODBYE:00";
                                outStream.writeUTF(reply);
                                con.close();
                            }
                        } else {
                            Date curDate = new Date();
                            System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::Login:::::: Request from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Login Rejected - User canceled login.");
                            String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":05:LOGINCANCELED:00";
                            outStream.writeUTF(reply);
                            this.con.close();
                            counter = 3;
                          }
                    } else {
                        String reply = "01:LOGIN:" + pieces[2] + ":" + pieces[3] + ":04:INVALIDTYPE:00";
                        outStream.writeUTF(reply);
                        this.con.close();
                    }
                }

            } catch (IOException ex) {
                //Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
                Date curDate = new Date();
                System.out.println("RECEIVED: " + DateFormat.getInstance().format(curDate) + "  ::INVALID:::: Packet from: " + con.getRemoteSocketAddress().toString().substring(1) + " - Session Terminated.");
                loginThread.stop();
            } catch (SQLException ex) {
                System.out.println("Database not present");
                System.out.println(ex.toString());
                Logger.getLogger(StudyBuddyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}