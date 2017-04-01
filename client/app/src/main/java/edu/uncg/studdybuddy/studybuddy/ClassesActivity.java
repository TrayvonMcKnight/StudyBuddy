package edu.uncg.studdybuddy.studybuddy;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import StudyBuddy.Chatrooms;

public class ClassesActivity extends ListActivity {

    private String[] chatrooms;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    ListView classList = (ListView) findViewById(R.id.classList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        this.chatrooms = MainMenu.chatrooms.getClassNamesAndSection();
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);

        for (int c = 0;c < this.chatrooms.length;c++){
            String[] pieces = this.chatrooms[c].split(":");
            listItems.add(pieces[0]);
            adapter.notifyDataSetChanged();
        }

    }
}
