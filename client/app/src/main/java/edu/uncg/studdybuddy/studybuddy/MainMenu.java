package edu.uncg.studdybuddy.studybuddy;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class MainMenu extends AppCompatActivity {
    protected static Chatrooms chatrooms;
    private StudyBuddyConnector ourConnector;
    private StudyBuddyConnector.MyCustomObjectListener listener;
    private final int MY_REQUEST_CODE = 1000;
    protected static ArrayList<String> privateChats;

    @InjectView(R.id.classesButton)
    Button classesButton;
    @InjectView(R.id.profileButton)
    Button profileButton;
    @InjectView(R.id.settingsButton)
    Button settingsButton;
    @InjectView(R.id.logOut)
    Button logOutButton;
    @InjectView(R.id.txtWelcome)
    TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        ButterKnife.inject(this);
        this.listener = null;
        ourConnector = StartActivity.server.getInstance();
        privateChats = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 23) {
            this.handlePermissions();
        }

        ourConnector.setCustomObjectListener(new StudyBuddyConnector.MyCustomObjectListener() {
            @Override
            public void onObjectReady(String title) {
                // Code to handle if object ready.
                if (title.equalsIgnoreCase("Chatrooms")) {
                    chatrooms = ourConnector.getChatrooms();
                    setWelcomeMessage(ourConnector.getUserName());
                }
            }

            @Override
            public void onDataLoaded(String data) {
                // Code to handle data loaded from network.
                String[] pieces = data.split(":");
                switch (pieces[0]) {
                    case "08": {


                        String temp;
                        String[] listChatpieces;
                        for (int c = 0; c < privateChats.size(); c++) {
                            temp = privateChats.get(c);
                            listChatpieces = temp.split(":");
                            // If there is any combination of the users between the two strings that are equal, then remove this from the list.
                            if ((pieces[1].equals(listChatpieces[0]) || pieces[1].equals(listChatpieces[1])) && (pieces[2].equals(listChatpieces[0]) || pieces[2].equals(listChatpieces[1]))) {
                                return; // If chat is open, do nothing.
                            }

                        }
                        // Else, open a new chat.
                        // Strip out the message.
                        String chatMessage = "";
                        if (pieces.length > 5) {
                            for (int c = 4; c < pieces.length; c++) {
                                chatMessage += pieces[c] + ":";
                            }
                            chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                        } else if (pieces.length < 5) {
                            System.out.println(pieces.length);
                            chatMessage = "Invalid Message!!!";
                        } else {
                            chatMessage = pieces[4];
                        }
                        // Add to the list of open chats.
                        privateChats.add(pieces[1] + ":" + pieces[2]);
                        // Create the intent, pass in fields, and start new activity.
                        Intent intent = new Intent(MainMenu.this, PrivateChatActivity.class);
                        intent.putExtra("SENDER_CLASS_NAME", "MAINMENU");
                        intent.putExtra("otherEmail", pieces[2]);
                        intent.putExtra("myEmail", pieces[3]);
                        intent.putExtra("MESSAGE", chatMessage);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        break;
                    }
                    case "13": {
                        if (pieces[5].equalsIgnoreCase("INCOMING") && pieces[1].equalsIgnoreCase("SENDFILE")) {
                            String senderName = MainMenu.chatrooms.getStudent(pieces[3], pieces[4], pieces[8]).getStudentName();
                            String chatMessage = "INCOMING FILE:  " + pieces[2] + "  - " + pieces[7] + " bytes";
                            ourConnector.addMessageToChatrooms(pieces[3], pieces[4], pieces[8], chatMessage);
                        }
                        break;
                    }
                    case "20": {
                        ourConnector.close();
                        Intent splashIntent = new Intent(MainMenu.this, SplashActivity.class);
                        MainMenu.this.startActivity(splashIntent);
                        finish();
                    }

                }
            }
        });


        welcome.setText("");

        profileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BasicProfile.class);
                startActivity(intent);
            }
        });
        classesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClassesActivity.class);
                intent.putExtra("myName", ourConnector.getUserName());
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ourConnector.logout();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                System.exit(0);
            }
        });

    }

    public static void removePrivateChat(String chat) {
        String[] querryChatPieces = chat.split(":");
        String temp;
        String[] listChatpieces;
        for (int c = 0; c < privateChats.size(); c++) {
            temp = privateChats.get(c);
            listChatpieces = temp.split(":");
            // If there is any combination of the users between the two strings that are equal, then remove this from the list.
            if ((querryChatPieces[0].equals(listChatpieces[0]) || querryChatPieces[0].equals(listChatpieces[1])) && (querryChatPieces[1].equals(listChatpieces[0]) || querryChatPieces[1].equals(listChatpieces[1]))) {
                privateChats.remove(c);
                break;
            }
        }
    }

    public static void addPrivateChat(String chat) {
        privateChats.add(chat);
    }

    public static String getRealName(String email) {
        String realName = "";
        Chatrooms currentRooms = StartActivity.server.getChatrooms();
        for (int c = 0; c < currentRooms.getNumberOfClasses(); c++) {
            Chatrooms.Chatroom room = currentRooms.getChatroom(c);
            Student[] students = room.getStudents();
            for (int d = 0; d < students.length; d++) {
                if (students[d].getStudentEmail().equalsIgnoreCase(email)) {
                    return students[d].getStudentName();
                }
            }
        }
        return realName;
    }

    @Override
    public void onBackPressed() {
    }

    public void setCustomObjectListener(StudyBuddyConnector.MyCustomObjectListener listener) {
        this.listener = listener;
    }

    private void setWelcomeMessage(final String userName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welcome.setText("Logged in as: " + userName);
            }
        });
    }


    // Handle Permissions
    @TargetApi(Build.VERSION_CODES.M)
    private void handlePermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_REQUEST_CODE);
        } else {
            System.out.println("We got the damned thing...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode) {
            case MY_REQUEST_CODE:
                for (int res : grantResults) {
                    // if user granted all required permissions then 'allowed' will return true.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;
            default:
                // if user denied then 'allowed' return false.
                allowed = false;
                break;
        }
        if (allowed) {
            // if user granted permissions then do your work.
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
