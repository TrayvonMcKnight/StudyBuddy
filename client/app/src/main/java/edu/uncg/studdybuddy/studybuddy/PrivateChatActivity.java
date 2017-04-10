
package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class PrivateChatActivity extends AppCompatActivity {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    private TextView titleBanner;
    private EditText mTxtTextBody;
    private Button mBtnSend;
    private StudyBuddyConnector server;
    private ArrayList<Student> studentList;
    private Chatrooms allChats;
    private List<ChatRoomMessage> chatMessList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView messagesList;
    private String myName;
    private String myEmail;
    private String otherName;
    private String otherEmail;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        this.server = StartActivity.server.getInstance();
        Bundle extras = getIntent().getExtras();
        this.otherEmail = extras.getString("otherEmail");
        this.otherName = extras.getString("otherName");
        this.myName = server.getUserName();
        this.myEmail = server.getUserEmail();
        server.setCustomObjectListener(new StudyBuddyConnector.MyCustomObjectListener() {

            @Override
            public void onObjectReady(String title) {

            }

            @Override
            public void onDataLoaded(String data) {
                String[] pieces = data.split(":");
                switch (pieces[0]){
                    case "08":{
                        if (pieces[1].equalsIgnoreCase(otherEmail) || pieces[1].equalsIgnoreCase(myEmail)){
                            String chatMessage="";
                            if (pieces.length > 4) {
                                for (int c = 3; c < pieces.length; c++) {
                                    chatMessage += pieces[c] + ":";
                                }
                                chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                            } else if (pieces.length < 4) {
                                System.out.println(pieces.length);
                                chatMessage = "Invalid Message!!!";
                            } else {
                                chatMessage = pieces[3];
                            }
                            chatMessList.add(new ChatRoomMessage(pieces[1], chatMessage));
                            updateAdapter();
                        }
                        // Message is coming in.  Check to see if it belongs in this activity
                        // If so, add it to the list view and update the adapter.
                        // if not, check to see if there is another open, if not, open it but if so, do nothing.
                    }
                }
            }
        });

        titleBanner = (TextView) findViewById(R.id.txtRecipient);
        titleBanner.setText(this.otherName);
        mTxtTextBody = (EditText) findViewById(R.id.txtTextBody);


        messagesList = (ListView) findViewById(R.id.lstMessages);
        chatMessList = new ArrayList<>();

        // init adapter
        adapter = new ChatAdapter(getApplicationContext(), chatMessList);
        messagesList.setAdapter(adapter);

        mBtnSend = (Button) findViewById(R.id.btnSend);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    private void sendMessage(){
        server.sendPrivateTextMessage(otherEmail, mTxtTextBody.getText().toString());
        mTxtTextBody.setText("");
    }

    private void updateAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
