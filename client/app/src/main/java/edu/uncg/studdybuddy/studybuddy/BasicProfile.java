package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import StudyBuddy.Student;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class BasicProfile extends AppCompatActivity {


    @InjectView(R.id.displayName) TextView displayName;
    @InjectView(R.id.displayEmail) TextView displayEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_profile);
        ButterKnife.inject(this);

        displayName.setText(StartActivity.server.getUserName());
        displayEmail.setText(StartActivity.server.getUserEmail());
    }
}
