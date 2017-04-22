package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class TeacherMenu extends AppCompatActivity {

    private StudyBuddyConnector ourConnector;

    @InjectView(R.id.settingsButton) Button settingsBtn;
    @InjectView(R.id.logOut) Button logOutBtn;
    @InjectView(R.id.attendanceButton) Button attendanceBtn;
    //Button attendanceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_menu);
        ButterKnife.inject(this);
        this.ourConnector = StartActivity.server.getInstance();
        //This should bring up all of the classes the professor teaches
        attendanceBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InstructorClassesActivity.class);
                intent.putExtra("myName", ourConnector.getUserName());
                startActivity(intent);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
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
}
