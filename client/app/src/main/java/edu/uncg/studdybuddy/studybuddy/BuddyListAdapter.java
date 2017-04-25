package edu.uncg.studdybuddy.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import StudyBuddy.Student;

/**
 * Created by Anthony Ratliff on 4/9/2017.
 */

public class BuddyListAdapter extends BaseAdapter {
    private Context c;
    private ArrayList<Student> students;
    private AppCompatActivity newActivity;

    public BuddyListAdapter(Context c, ArrayList<Student> studentList) {
        this.c = c;
        this.students = studentList;
    }


    @Override
    public int getCount() {
        return students.size();
    }

    @Override
    public Object getItem(int position) {
        return students.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView= LayoutInflater.from(c).inflate(R.layout.model_buddy_list,parent,false);
        }
        final Student s = (Student) this.getItem(position);
        ImageView img = (ImageView) convertView.findViewById(R.id.smileyImg);
        TextView nameTxt = (TextView) convertView.findViewById(R.id.nameTxt);
        TextView statTxt = (TextView) convertView.findViewById(R.id.statusTxt);

        img.setImageResource(R.drawable.smiley_yellow);
        nameTxt.setText(s.getStudentName());
        boolean online = s.getOnlineStatus();
        String stat;
        if (online) {
            stat = "Online";
            statTxt.setTextColor(Color.GREEN);
        }
        else {
            stat = "Offline";
            statTxt.setTextColor(Color.RED);
        }
        statTxt.setText(stat);

        boolean absent = s.getAbsent();
        if (absent){
            String name = s.getStudentName();
            nameTxt.setTextColor(Color.BLACK);
        } else {
            nameTxt.setTextColor(Color.BLUE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, PrivateChatActivity.class);
                intent.putExtra("otherEmail", s.getStudentEmail());
                intent.putExtra("otherName", s.getStudentName());
                intent.putExtra("SENDER_CLASS_NAME", "DRAWER");
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                c.startActivity(intent);
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(c, ProfileActivity.class);
                intent.putExtra("otherEmail", s.getStudentEmail());
                intent.putExtra("otherName", s.getStudentName());
                c.startActivity(intent);
                return false;
            }
        });

        return convertView;
    }
}
