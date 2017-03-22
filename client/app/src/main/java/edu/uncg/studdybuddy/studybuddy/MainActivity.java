package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

import edu.uncg.studdybuddy.client.StudyBuddyConnector;

/**
 * Created by Metalaxe on 3/5/2017.
 */

public class MainActivity extends AppCompatActivity {
    private final String VERSION = "1.00";
    TextView versionText;
    TextView statusText;
    Button clk;
    private StudyBuddyConnector server;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.versionText = (TextView) findViewById(R.id.versionText);
        this.statusText = (TextView) findViewById(R.id.statusText);
        this.server = new StudyBuddyConnector();
        clk = (Button) findViewById(R.id.button);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (performHandshake()) {


                    statusText.setText("Server Found! Authenticating..");

                } else {
                    statusText.setText("Server Not Found! Try Again Later.");
                }
            }
        }, 5000);
        this.versionText.setText("Study Buddy Version: " + VERSION);
        this.statusText.setText("Looking For Server...");

    }

    public void openLogin(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.putExtra("server", (Serializable) server);
        startActivity(intent);
    }

    public boolean performHandshake(){
        boolean success = false;
        if (!server.hasConnection()){
            switch (server.handshake()){
                case 0: {
                    success = true;
                    break;
                }
                case 1: {
                    Toast.makeText(getBaseContext(), "Case 1 Fired", Toast.LENGTH_LONG).show();
                }
                case 2:{
                    Toast.makeText(getBaseContext(), "Case 2 Fired", Toast.LENGTH_LONG).show();
                }
                case 3: {
                    Toast.makeText(getBaseContext(), "Case 3 Fired", Toast.LENGTH_LONG).show();
                }
                case 4: {
                    Toast.makeText(getBaseContext(), "Case 4 Fired", Toast.LENGTH_LONG).show();
                    break;
                }
                default: {
                    Toast.makeText(getBaseContext(), "Default Fired", Toast.LENGTH_LONG).show();
                }
            }
        }
        return success;
    }
}
