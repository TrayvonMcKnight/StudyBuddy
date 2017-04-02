package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangePasswordActivity extends AppCompatActivity {

    @InjectView(R.id.currentPassword) EditText currPass;
    @InjectView(R.id.newPassword) EditText newPass;
    @InjectView(R.id.confirmPassword) EditText confirmPass;
    @InjectView(R.id.submitButton) EditText submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.inject(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newPass.getText().toString() == confirmPass.getText().toString()) {
                    StartActivity.server.changePassword(currPass.getText().toString(),
                            newPass.getText().toString());
                    
                }
            }
        });
    }
}
