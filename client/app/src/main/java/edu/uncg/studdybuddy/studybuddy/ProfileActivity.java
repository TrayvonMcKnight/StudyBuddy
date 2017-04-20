package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private Chatrooms classes;
    String[] pieces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.inject(this);
        Bundle extras = getIntent().getExtras();
        common_classList = (ListView) findViewById(R.id.common_classes_list);
        StudyBuddyConnector connector = StartActivity.server.getInstance();

        classes = connector.getChatrooms();
        final String[] classArray = classes.getClassNamesAndSection();

        for (String aClassArray : classArray) {
            pieces = aClassArray.split(":");
        }

        student1_classes = compareClasses(pieces, classes, extras.get("otherEmail").toString());

        studentName.setText(extras.get("otherName").toString());
        studentEmail.setText(extras.get("otherEmail").toString());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,student1_classes);

        common_classList.setAdapter(arrayAdapter);
    }

    private ArrayList<String> compareClasses(String[] classPieces, Chatrooms sClasses, String email){
        ArrayList<String> compared = null;

        int i = 0;
        while(i < classPieces.length){
            if(sClasses.getStudent(pieces[i], pieces[i+1], email) != null){
                compared.add(pieces[i] + ":" + pieces[i+1] );
            }
        }

        return compared;
    }
}
