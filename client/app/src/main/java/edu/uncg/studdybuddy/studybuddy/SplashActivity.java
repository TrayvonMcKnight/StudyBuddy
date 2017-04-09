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

import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

/**
 * Created by Metalaxe on 3/5/2017.
 */

public class SplashActivity extends AppCompatActivity {
    private final String VERSION = "1.00";
    TextView versionText;
    TextView statusText;
    private Handler Handler1 = new Handler();
    private Handler Handler2 = new Handler();
    private StudyBuddyConnector connector;
    @InjectView(R.id.tryAgainbtn) Button try_again;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        this.connector = StartActivity.server.getInstance();

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
                            finish();
                        }
                    }, 1000);


                } else {
                    statusText.setText("Server Not Found! Try Again Later.");
                }
            }
        }, 1000);
        this.versionText.setText("Study Buddy Version: " + VERSION);
        this.statusText.setText("Looking For Server...");

        try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
            }
        });

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
                default: {
                    Toast.makeText(getBaseContext(), "Server Unavailable", Toast.LENGTH_LONG).show();
                    try_again.setVisibility(View.VISIBLE);

                }
            }
        }
        return success;
    }
}
