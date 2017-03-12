package edu.uncg.studdybuddy.studybuddy;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainMenu extends Activity {
    public static final String TAG = "MainMenu";

    @InjectView(R.id.classesButton) Button classesButton;
    @InjectView(R.id.profileButton) Button profileButton;
    @InjectView(R.id.settingsButton) Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        ButterKnife.inject(this);

        classesButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ClassesActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

}
