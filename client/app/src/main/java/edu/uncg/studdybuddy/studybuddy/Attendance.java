package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;


public class Attendance extends AppCompatActivity {


    ListView simpleList;
    String[] questions;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        // get the string array from string.xml file
        questions = getResources().getStringArray(R.array.questions);
        // get the reference of ListView and Button
        simpleList = (ListView) findViewById(R.id.simpleListView);
        submit = (Button) findViewById(R.id.submit);
        // set the adapter to fill the data in the ListView
        AttendanceAdapter attendanceAdapter = new AttendanceAdapter(getApplicationContext(), questions);
        simpleList.setAdapter(attendanceAdapter);
        // perform setOnClickListerner event on Button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "";
                // get the value of selected answers from custom adapter
                for (int i = 0; i < AttendanceAdapter.selectedAnswers.size(); i++) {
                    message = message + "\n" + (i + 1) + " " + AttendanceAdapter.selectedAnswers.get(i);
                }
                // display the message on screen with the help of Toast.
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}






    /*

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

    */

