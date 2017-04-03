package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import StudyBuddy.Chatrooms;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ClassesActivity extends AppCompatActivity {
    Chatrooms classes;
    private ListView classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        classList = (ListView) findViewById(R.id.list_classes);

        StudyBuddyConnector connector = StartActivity.server.getInstance();
        classes = connector.getChatrooms();
        String[] classArray = classes.getClassNamesAndSection();

        List<String> arrayList = new ArrayList<String>();
        for (int c = 0;c < classArray.length;c++) {
            String[] pieces = classArray[c].split(":");
            arrayList.add(pieces[0] + "-" + pieces[1] + " Professor: " + classes.getProfessorName(pieces[0], pieces[1]));
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, arrayList );

        classList.setAdapter(arrayAdapter);
        //ListView listView = (ListView) findViewById()
    }
}
