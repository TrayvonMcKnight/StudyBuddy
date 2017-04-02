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

        List<String> arrayClass = new ArrayList<>();
        List<String> arraySections = new ArrayList<>();

        String[] classSections = classes.getClassNamesAndSection();
        for (String classSection : classSections) {
            String[] pieces = classSection.split(":");
            arrayClass.add(pieces[0] + "-" + pieces[1]);

            arraySections.add(classes.getProfessorName(pieces[0], pieces[1]));
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, arrayClass);

        ArrayAdapter<String> arrAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_2, arraySections);

        classList.setAdapter(arrayAdapter);
        classList.setAdapter(arrAdapter);
        //ListView listView = (ListView) findViewById()
    }
}
