package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import StudyBuddy.Chatrooms;

public class ClassesActivity extends AppCompatActivity {
    Chatrooms classes;
    private ListView classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        classList = (ListView) findViewById(R.id.list_classes);

        Bundle bundle = getIntent().getExtras();
        classes = (Chatrooms) bundle.getSerializable("classes");

        List<String> arrayList = new ArrayList<String>();
        arrayList.add(classes.getClass().toString());

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arrayList );

        classList.setAdapter(arrayAdapter);
        //ListView listView = (ListView) findViewById()
    }
}
