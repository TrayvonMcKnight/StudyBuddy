package edu.uncg.studdybuddy.studybuddy;

import android.media.Image;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class MainMenu extends AppCompatActivity {
    public static final String TAG = "MainMenu";
    protected static Chatrooms chatrooms;
    private StudyBuddyConnector ourConnector;

    @InjectView(R.id.classesButton) Button classesButton;
    @InjectView(R.id.profileButton) Button profileButton;
    @InjectView(R.id.settingsButton) Button settingsButton;
    @InjectView(R.id.logOut) Button logOutButton;
    @InjectView(R.id.txtWelcome) TextView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        ButterKnife.inject(this);
        ourConnector = StartActivity.server.getInstance();

        ourConnector.setCustomObjectListener(new StudyBuddyConnector.MyCustomObjectListener() {
            @Override
            public void onObjectReady(String title) {
                // Code to handle if object ready.
                if (title.equalsIgnoreCase("Chatrooms")) {
                    chatrooms = (Chatrooms) ourConnector.getChatrooms();
                    setWelcomeMessage(returnUserName(ourConnector.getUserName()));
                }
            }

            @Override
            public void onDataLoaded(String data) {
                // Code to handle data loaded from network.
            }
        });


        welcome.setText("");

        profileButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });
        classesButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClassesActivity.class);
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.server.logout();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });

        //StartActivity.server.changePassword("test1234", "test4321");

    }
    private void setWelcomeMessage(final String userName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welcome.setText("Logged in as: " + userName);
            }
        });
    }

    private String returnUserName(String email){
        String[] allClasses = chatrooms.getClassNamesAndSection();
        String[] pieces = allClasses[0].split(":");
        Student student = chatrooms.getStudent(pieces[0], pieces[1], email);
        return student.getStudentName();
    }
}
