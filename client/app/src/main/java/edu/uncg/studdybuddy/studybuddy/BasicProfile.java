package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.InjectView;

public class BasicProfile extends AppCompatActivity {

    @InjectView(R.id.displayName) TextView displayName;
    @InjectView(R.id.displayEmail) TextView displayEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_profile);

        Bundle extras = getIntent().getExtras();

        displayName.setText("");
        displayEmail.setText("");
    }
}
