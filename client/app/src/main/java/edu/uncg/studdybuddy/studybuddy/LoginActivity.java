package edu.uncg.studdybuddy.studybuddy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private int loopCounter;
    private boolean loginAttempted = false;

    @InjectView(R.id.input_email) EditText emailText;
    @InjectView(R.id.input_password) EditText passwordText;
    @InjectView(R.id.btn_login) Button loginButton;
    @InjectView(R.id.link_signup) TextView signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        this.loopCounter = 0;

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean connected = StartActivity.server.hasConnection();
                if (!StartActivity.server.hasConnection()){
                    StartActivity.server.handshake();
                }
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // If link was clicked during login, break the login loop and perform another handshake.
                if (loginAttempted) {
                    StartActivity.server.login("null", "null");
                    loginAttempted = false;
                    loopCounter = 0;
                    StartActivity.server.handshake();
                }
                // Start the Signup activity
               Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

        if(!StartActivity.server.hasConnection()){
            StartActivity.server.handshake();
        }

    }

    public void login() {
        Log.d(TAG, "Login");
        this.loginAttempted = true;
        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        loginButton.setEnabled(true);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (loopCounter < 3) {
                            String email = emailText.getText().toString();
                            String password = passwordText.getText().toString();

                            switch (StartActivity.server.login(email, password)) {
                                case 0: {
                                    //Login Successful
                                    onLoginSuccess();
                                    break;
                                }
                                case 1: {
                                    // Handshake must occur first.
                                    Toast.makeText(getBaseContext(), "Incorrect Login Method.  Handshake first.", Toast.LENGTH_LONG).show();
                                    loopCounter++;
                                    break;
                                }
                                case 2: {
                                    // No such username in database.
                                    Toast.makeText(getBaseContext(), "No such user.  Please check for errors and try again.", Toast.LENGTH_LONG).show();
                                    loopCounter++;
                                    emailText.setText("");
                                    passwordText.setText("");
                                    if(emailText.requestFocus()) {
                                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }
                                    //setContentView(R.layout.activity_login);
                                    break;
                                }
                                case 3: {
                                    // Incorrect password.
                                    Toast.makeText(getBaseContext(), "Incorrect Password.  Try again.", Toast.LENGTH_LONG).show();
                                    loopCounter++;
                                    passwordText.setText("");
                                    if(passwordText.requestFocus()) {
                                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }
                                    break;
                                }
                                case 4: {
                                    // 3 bad attempts.  Server Rejected.  Handshake must happen again.
                                    Toast.makeText(getBaseContext(), "Server Rejected.  Three incorrect attempts.", Toast.LENGTH_LONG).show();
                                    StartActivity.server.handshake();
                                    loopCounter = 0;
                                    emailText.setText("");
                                    passwordText.setText("");
                                    if(emailText.requestFocus()) {
                                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }
                                    break;
                                }
                                default: {
                                    break;
                                }
                            }
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the LoginActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        //Start Main menu activity
        Intent intent = null;
        if (StartActivity.server.isProfessor()){
            intent = new Intent(getApplicationContext(), TeacherMenu.class);
        } else {
            intent = new Intent(getApplicationContext(), MainMenu.class);
        }
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {

        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
