
package edu.uncg.studdybuddy.studybuddy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    private TextView titleBanner;
    private EditText mTxtTextBody;
    private Button mBtnSend;
    private String className, sec, professor, professorEmail;
    private StudyBuddyConnector server;
    private ArrayList<Student> students;
    private Chatrooms allChats;
    private List<ChatRoomMessage> chatMessList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView messagesList;
    private String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        this.server = StartActivity.server.getInstance();
        Bundle extras = getIntent().getExtras();
        this.className = extras.getString("className");
        this.sec = extras.getString("section");
        this.myName = extras.getString("studentName");
        this.professor = MainMenu.chatrooms.getProfessorName(this.className, this.sec);
        this.professorEmail = MainMenu.chatrooms.getProfessorEmail(this.className, this.sec);
        server.setCustomObjectListener(new StudyBuddyConnector.MyCustomObjectListener() {

                                                @Override
                                                public void onObjectReady(String title) {

                                                }

                                                @Override
                                                public void onDataLoaded(String data) {
                                                    String[] pieces = data.split(":");
                                                    if (pieces[0].equals("11") && pieces[1].equals("CHATMESS") && pieces[2].equals(className) && pieces[3].equals(sec)){
                                                        if (myName.equals("No Classes")) {
                                                            chatMessList.add(new ChatRoomMessage(pieces[4], pieces[7]));
                                                        } else {
                                                            chatMessList.add(new ChatRoomMessage(pieces[4], pieces[7]));
                                                        }
                                                        updateAdapter();
                                                    }
                                                }
                                            });

        titleBanner = (TextView) findViewById(R.id.txtRecipient);
        titleBanner.setText(this.className + "-" + this.sec + "    " + this.professor);
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
            //arrayList.add(mTxtTextBody.getText().toString());
            server.sendToChatroom(this.className, this.sec, mTxtTextBody.getText().toString());
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
/*

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().removeMessageClientListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onServiceConnected() {
        getSinchServiceInterface().addMessageClientListener(this);
        setButtonEnabled(true);
    }

    @Override
    public void onServiceDisconnected() {
        setButtonEnabled(false);
    }

    private void sendMessage() {
        String recipient = mTxtRecipient.getText().toString();
        String textBody = mTxtTextBody.getText().toString();
        if (recipient.isEmpty()) {
            Toast.makeText(this, "No recipient added", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textBody.isEmpty()) {
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
            return;
        }

        getSinchServiceInterface().sendMessage(recipient, textBody);
        mTxtTextBody.setText("");
    }

    private void setButtonEnabled(boolean enabled) {
        mBtnSend.setEnabled(enabled);
    }

    @Override
    public void onIncomingMessage(MessageClient client, Message message) {
        mMessageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
    }

    @Override
    public void onMessageSent(MessageClient client, Message message, String recipientId) {
        mMessageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
    }

    @Override
    public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {
        // Left blank intentionally
    }

    @Override
    public void onMessageFailed(MessageClient client, Message message,
                                MessageFailureInfo failureInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sending failed: ")
                .append(failureInfo.getSinchError().getMessage());

        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        Log.d(TAG, sb.toString());
    }

    @Override
    public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
        Log.d(TAG, "onDelivered");
    }*/

}
