package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangePasswordActivity extends AppCompatActivity {

    @InjectView(R.id.currentPassword) EditText currPass;
    @InjectView(R.id.newPassword) EditText newPass;
    @InjectView(R.id.confirmPassword) EditText confirmPass;
    @InjectView(R.id.submitButton) Button submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.inject(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newPass.getText().toString().equals(confirmPass.getText().toString())) {
                    int error = StartActivity.server.changePassword(currPass.getText().toString(), newPass.getText().toString());

                    switch (error){
                        case 0: {
                            Toast.makeText(getBaseContext(), "Password successfully changed.", Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        }
                        case 1: {
                            Toast.makeText(getBaseContext(), "Incorrect Password.  Please try again.", Toast.LENGTH_LONG).show();
                            currPass.setText("");
                            if (currPass.requestFocus()){
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                        default:{

                        }
                    }

                } else {
                    Toast.makeText(getBaseContext(), "Passwords do not match.  Try again.", Toast.LENGTH_LONG).show();
                    newPass.setText("");
                    confirmPass.setText("");
                    if (newPass.requestFocus()){
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });
    }
}
