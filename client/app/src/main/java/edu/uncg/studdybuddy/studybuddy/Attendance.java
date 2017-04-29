package edu.uncg.studdybuddy.studybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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


    ListView studentList;
    Button submit;
    private Chatrooms classes;
    private ListView classList;
    private String myName;
    private String cName, section;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        this.builder = new AlertDialog.Builder(Attendance.this);

        Bundle extras = getIntent().getExtras();
        this.myName = extras.getString("myName");
        studentList = (ListView) findViewById(R.id.list_classes);

        StudyBuddyConnector connector = StartActivity.server.getInstance();
        classes = connector.getChatrooms();
        this.cName = extras.getString("className");
        this.section = extras.getString("section");

        final Student[] students = classes.getStudents(cName, section);

        final List<String> arrayList = new ArrayList<>();
        for (int i = 0; i < students.length; i++) {
            arrayList.add(students[i].getStudentName() + ":" + students[i].getStudentEmail());
        }

        // get the reference of ListView and Button
        studentList = (ListView) findViewById(R.id.studentListView);
        submit = (Button) findViewById(R.id.submit);
        // set the adapter to fill the data in the ListView

        AttendanceAdapter attendanceAdapter = new AttendanceAdapter(getApplicationContext(), arrayList);
        studentList.setAdapter(attendanceAdapter);

        // perform setOnClickListerner event on Button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> attendance = new ArrayList<String>();

                // get the value of selected answers from custom adapter
                for (int i = 0; i < AttendanceAdapter.selectedAnswers.size(); i++) {
                    attendance.add(cName + ":" + section + ":" + students[i].getStudentEmail() + ":" + AttendanceAdapter.selectedAnswers.get(i));
                }

                // display the message on screen with the help of Dialog.
                builder.setTitle("Attendance for Today");

                builder.setMessage("Are you sure this is attendance you want to submit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // TODO: handle the OK
                                StartActivity.server.updateAttendance(cName, section, attendance);

                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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