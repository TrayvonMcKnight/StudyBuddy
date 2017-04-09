package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import StudyBuddy.Chatrooms;
import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class MainMenu extends AppCompatActivity {
    protected static Chatrooms chatrooms;
    private StudyBuddyConnector ourConnector;
    private StudyBuddyConnector.MyCustomObjectListener listener;

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
        this.listener = null;
        ourConnector = StartActivity.server.getInstance();

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
                switch(pieces[0]){

                }
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
                intent.putExtra("myName", ourConnector.getUserName());
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
                ourConnector.logout();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                System.exit(0);
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    public void setCustomObjectListener(StudyBuddyConnector.MyCustomObjectListener listener) {
        this.listener = listener;
    }

    private void setWelcomeMessage(final String userName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                welcome.setText("Logged in as: " + userName);
            }
        });
    }
}
