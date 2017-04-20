package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import StudyBuddy.Chatrooms;
import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ProfileActivity extends AppCompatActivity {

    @InjectView(R.id.get_name) TextView get_name;
    @InjectView(R.id.get_email) TextView get_email;
    @InjectView(R.id.studentName) TextView studentName;
    @InjectView(R.id.studentEmail) TextView studentEmail;

    private ListView common_classList;
    ArrayList<String> student1_classes;
    ArrayList<String> student2_classes;
    private Chatrooms classes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);

        Bundle extras = getIntent().getExtras();

        StudyBuddyConnector connector = StartActivity.server.getInstance();
        classes = connector.getChatrooms();
        final String[] classArray = classes.getClassNamesAndSection();



        common_classList = (ListView) findViewById(R.id.common_classes_list);

        studentName.setText(extras.get("otherName").toString());
        studentEmail.setText(extras.get("otherEmail").toString());



    }

    private String[] compareClasses(String[] student1 , String email){
        String[] compared;

        return compared;
    }
}
