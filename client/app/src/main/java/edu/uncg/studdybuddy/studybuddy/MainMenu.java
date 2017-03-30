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
import android.widget.Toast;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;
import edu.uncg.studdybuddy.events.Event;
import edu.uncg.studdybuddy.events.IEventHandler;

public class MainMenu extends AppCompatActivity {
    public static final String TAG = "MainMenu";
    private Chatrooms chatrooms;

    @InjectView(R.id.classesButton) Button classesButton;
    @InjectView(R.id.profileButton) Button profileButton;
    @InjectView(R.id.settingsButton) Button settingsButton;
    @InjectView(R.id.logOut) Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        ButterKnife.inject(this);
        StudyBuddyConnector ourConnector = StartActivity.server.getInstance();
        ourConnector.addEventListener(Event.CHATROOMS, new IEventHandler() {

            @Override
            public void callback(Event event) {
                //Toast.makeText(getBaseContext(), "Incoming message: ", Toast.LENGTH_LONG).show();
                //logOutButton.setText("Chatrooms Here");
                chatrooms = event.getChatrooms();
                logOutButton.setText("There are " + chatrooms.getNumberOfClasses());

                /*
                String[] classSections = chatrooms.getClassNamesAndSection();
                for(int i = 0; i < classSections.length; i++){
                    String[] pieces = classSections[i].split(":");
                }

                String[] pieces = classSections[0].split(":");
                //Access students
                Student[] students = chatrooms.getStudents(pieces[0], pieces[1]);
                */
            }
        });

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
}
