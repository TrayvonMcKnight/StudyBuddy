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
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ClassesActivity extends AppCompatActivity {
    private Chatrooms classes;
    private ListView classList;
    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        Bundle extras = getIntent().getExtras();
        this.myName = extras.getString("myName");
        classList = (ListView) findViewById(R.id.list_classes);

        StudyBuddyConnector connector = StartActivity.server.getInstance();
        classes = connector.getChatrooms();
        final String[] classArray = classes.getClassNamesAndSection();

        final List<String> arrayList = new ArrayList<>();
        for (String aClassArray : classArray) {
            String[] pieces = aClassArray.split(":");
            arrayList.add(pieces[0] + "-" + pieces[1] + " Professor: " + classes.getProfessorName(pieces[0], pieces[1]));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, arrayList);

        classList.setAdapter(arrayAdapter);
        classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ChatRoomActivity.class);
                String chatInfo = classArray[position];
                String[] pieces = chatInfo.split(":");
                intent.putExtra("className", pieces[0]);
                intent.putExtra("section", pieces[1]);
                intent.putExtra("studentName", myName);
                startActivity(intent);
            }
        });
    }
}
