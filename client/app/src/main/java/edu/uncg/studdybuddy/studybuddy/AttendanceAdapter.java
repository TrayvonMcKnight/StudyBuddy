package edu.uncg.studdybuddy.studybuddy;

/**
 * Created by Trayvon on 4/16/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends BaseAdapter {
    Context context;
    List<String> studentList;
    LayoutInflater inflter;
    public static ArrayList<String> selectedAnswers;

    public AttendanceAdapter(Context applicationContext, List<String> studentList) {
        this.studentList = studentList;
        // initialize arraylist and add static string for all the students
        selectedAnswers = new ArrayList<>();
        for (int i = 0; i < studentList.size(); i++) {
            selectedAnswers.add("yes");
        }
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return studentList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final String[] pieces = studentList.get(i).split(":");
        view = inflter.inflate(R.layout.list_items_attendance, null);
        // get the reference of TextView and Button's
        TextView question = (TextView) view.findViewById(R.id.question);
        RadioButton yes = (RadioButton) view.findViewById(R.id.yes);
        RadioButton no = (RadioButton) view.findViewById(R.id.no);
        yes.setChecked(true);
        // perform setOnCheckedChangeListener event on yes button
        yes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // set Yes values in ArrayList if RadioButton is checked
                if (isChecked)
                    selectedAnswers.set(i, "Yes");
            }
        });
        // perform setOnCheckedChangeListener event on no button
        no.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // set No values in ArrayList if RadioButton is checked
                if (isChecked)
                    selectedAnswers.set(i, "No");

            }
        });
        // set the value in TextView

        question.setText(pieces[0]);
        return view;
    }
}
