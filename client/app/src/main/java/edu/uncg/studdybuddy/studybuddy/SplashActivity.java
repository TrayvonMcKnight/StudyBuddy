package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Metalaxe on 3/5/2017.
 */

public class SplashActivity extends ActionBarActivity {
    private final String VERSION = "1.00";
    TextView versionText;
    TextView statusText;
    private Handler Handler1 = new Handler();
    private Handler Handler2 = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.versionText = (TextView) findViewById(R.id.versionText);
        this.statusText = (TextView) findViewById(R.id.statusText);
        Handler1.postDelayed(new Runnable() {
            public void run() {
                if (performHandshake()) {


                    statusText.setText("Server Found! Authenticating..");
                    Handler2.postDelayed(new Runnable() {
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    }, 2000);


                } else {
                    statusText.setText("Server Not Found! Try Again Later.");
                }
            }
        }, 2000);
        this.versionText.setText("Study Buddy Version: " + VERSION);
        this.statusText.setText("Looking For Server...");

    }

    public void openLogin() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public boolean performHandshake(){
        boolean success = false;
        if (!StartActivity.server.hasConnection()){
            switch (StartActivity.server.handshake()){
                case 0: {
                    success = true;
                    break;
                }
                case 1: {
                    Toast.makeText(getBaseContext(), "Case 1 Fired", Toast.LENGTH_LONG).show();
                    break;
                }
                case 2:{
                    Toast.makeText(getBaseContext(), "Case 2 Fired", Toast.LENGTH_LONG).show();
                    break;
                }
                case 3: {
                    Toast.makeText(getBaseContext(), "Case 3 Fired", Toast.LENGTH_LONG).show();
                    break;
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
