package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class StartActivity extends AppCompatActivity {

    public static StudyBuddyConnector server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        server  = new StudyBuddyConnector();

        Intent splashIntent = new Intent(StartActivity.this, SplashActivity.class);
        StartActivity.this.startActivity(splashIntent);
    }
}
