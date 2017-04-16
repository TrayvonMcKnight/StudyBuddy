package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;


public class Attendance extends AppCompatActivity {

    @InjectView(R.id.studentList) ListView studentList;
    private Chatrooms classes;
    private ListView classList;
    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        Bundle extras = getIntent().getExtras();
        this.myName = extras.getString("myName");
        studentList = (ListView) findViewById(R.id.list_classes);

        StudyBuddyConnector connector = StartActivity.server.getInstance();
        classes = connector.getChatrooms();


        Student[] students = classes.getStudents(extras.getString("className"),
                extras.getString("section"));

        final List<String> arrayList = new ArrayList<>();
        for(int i = 0; i < students.length; i++){
            arrayList.add(students[i].getStudentName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, arrayList);

        studentList.setAdapter(arrayAdapter);
        studentList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }
}
