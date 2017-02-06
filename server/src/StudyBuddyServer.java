
import StudyBuddy.Database;
import StudyBuddy.OnlineClientList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

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
    }
}