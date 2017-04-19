package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProfileActivity extends AppCompatActivity {

    @InjectView(R.id.get_name) TextView get_name;
    @InjectView(R.id.get_email) TextView get_email;
    @InjectView(R.id.studentName) TextView studentName;
    @InjectView(R.id.studentEmail) TextView studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();

        studentName.setText(extras.get("otherName").toString());
        studentEmail.setText(extras.get("otherEmail").toString());

    }
}
