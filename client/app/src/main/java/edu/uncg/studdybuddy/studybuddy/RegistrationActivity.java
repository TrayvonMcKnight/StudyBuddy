package edu.uncg.studdybuddy.studybuddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";

    @InjectView(R.id.input_fname) EditText _fnameText;
    @InjectView(R.id.input_lname) EditText _lnameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.input_password2) EditText _password2Text;
    @InjectView(R.id.btn_register) Button _registerButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.inject(this);

        _registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void register() {
        Log.d(TAG, "Register");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _registerButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this,
                R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String fname = _fnameText.getText().toString();
        String lname = _lnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String password2 = _password2Text.getText().toString();

        if (password.equals(password2)){
            switch (StartActivity.server.createNewUser(email, password, password2, fname, lname)){
                case 0: {
                    Toast.makeText(getBaseContext(), "Account created successfully.  Please login.", Toast.LENGTH_LONG).show();
                    this.onSignupSuccess();
                    break;
                }
                case 1: {
                    Toast.makeText(getBaseContext(), "Account creation failed.  A user with that email has already registered.  Please try again.", Toast.LENGTH_LONG).show();
                    this.onSignupFailed();
                    break;
                }
                case 2: {
                    Toast.makeText(getBaseContext(), "Account creation failed.  Invalid password format.  Please try again.", Toast.LENGTH_LONG).show();
                    this.onSignupFailed();
                    break;
                }
                case 3: {
                    Toast.makeText(getBaseContext(), "Account creation failed.  Invalid email format.  Please try again.", Toast.LENGTH_LONG).show();
                    this.onSignupFailed();
                    break;
                }
                case 4: {
                    Toast.makeText(getBaseContext(), "Account creation failed.  Database error.  Please try again.", Toast.LENGTH_LONG).show();
                    this.onSignupFailed();
                    break;
                }
                case 5: {
                    Toast.makeText(getBaseContext(), "Account creation failed.  There is a user currently loggin in from this device.  Sign out first and try again.", Toast.LENGTH_LONG).show();
                    this.onSignupFailed();
                    break;
                }
                default: {
                    // Server disconnected due to invalid response.  Perform reconnect.
                }
            }
        }

        // TODO: Implement Registration logic here. SQL

        new Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        //onSignupSuccess();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    public void onSignupSuccess() {
        _registerButton.setEnabled(true);
        setResult(RESULT_OK, null);
        startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    public void onSignupFailed() {
        _registerButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String fname = _fnameText.getText().toString();
        String lname = _lnameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String password2 = _password2Text.getText().toString();

        //Invalid name
        if (fname.isEmpty() || fname.length() < 3) {
            _fnameText.setError("at least 3 characters");
            valid = false;
        }
        else if((lname.isEmpty() || lname.length() < 3)) {
            _lnameText.setError("at least 3 characters");
            valid = false;
        }
        else {
            _fnameText.setError(null);
            _lnameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        //password not long enough
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        }
        else if(!password.equals(password2)){
            _password2Text.setError("Password does not match");
        }
        else {
            _passwordText.setError(null);
            _password2Text.setError(null);
        }

        return valid;
    }
}
