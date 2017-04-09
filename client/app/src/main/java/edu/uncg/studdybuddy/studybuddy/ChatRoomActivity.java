
package edu.uncg.studdybuddy.studybuddy;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import butterknife.InjectView;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ChatRoomActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    private TextView titleBanner;
    private EditText mTxtTextBody;
    private Button mBtnSend;
    private String className, sec, professor, professorEmail, classDescription;
    private StudyBuddyConnector server;
    private Student[] students;
    private ArrayList<Student> studentList;
    private Chatrooms allChats;
    private List<ChatRoomMessage> chatMessList = new ArrayList<>();
    private ChatAdapter adapter;
    private ListView messagesList;
    private String myName;
    private TextView txtClass;
    private TextView txtProfessor;
    private TextView txtDescript;
    private ListView studentListView;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    @InjectView(R.id.drawer_group) Menu drawer_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer == null){
            System.out.println("Drawer is null");
        }
        if (toggle == null){
            System.out.println("toggle is null.");
        }
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.server = StartActivity.server.getInstance();
        Bundle extras = getIntent().getExtras();
        this.className = extras.getString("className");
        this.sec = extras.getString("section");
        this.myName = extras.getString("studentName");
        this.professor = MainMenu.chatrooms.getProfessorName(this.className, this.sec);
        this.professorEmail = MainMenu.chatrooms.getProfessorEmail(this.className, this.sec);
        this.classDescription = MainMenu.chatrooms.getChatroom(this.className, this.sec).getDescription();
        server.setCustomObjectListener(new StudyBuddyConnector.MyCustomObjectListener() {

                                                @Override
                                                public void onObjectReady(String title) {

                                                }

                                                @Override
                                                public void onDataLoaded(String data) {
                                                    String[] pieces = data.split(":");
                                                    if (pieces[0].equals("11") && pieces[1].equals("CHATMESS") && pieces[2].equals(className) && pieces[3].equals(sec)){
                                                        String senderName = MainMenu.chatrooms.getStudent(pieces[2], pieces[3], pieces[4]).getStudentName();
                                                        String chatMessage = "";
                                                        if (pieces.length > 8){
                                                            for (int c = 7; c < pieces.length;c++){
                                                                chatMessage += pieces[c] + ":";
                                                            }
                                                            chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                                                        } else {
                                                            chatMessage = pieces[7];
                                                        }
                                                        chatMessList.add(new ChatRoomMessage(senderName, chatMessage));
                                                        updateAdapter();
                                                    }
                                                }
                                            });

        titleBanner = (TextView) findViewById(R.id.txtRecipient);
        titleBanner.setText(this.className + "-" + this.sec);
        mTxtTextBody = (EditText) findViewById(R.id.txtTextBody);


        messagesList = (ListView) findViewById(R.id.lstMessages);
        chatMessList = new ArrayList<>();

        // Get all current messages and add them to the listview.
        Chatrooms currentRooms = server.getChatrooms();
        Chatrooms.Chatroom thisRoom = currentRooms.getChatroom(this.className, this.sec);
        String[][] roomMessages = thisRoom.getMessages();
        for (String[] roomMessage : roomMessages) {
            String senderName = currentRooms.getStudent(className, sec, roomMessage[0]).getStudentName();
            chatMessList.add(new ChatRoomMessage(senderName, roomMessage[1], roomMessage[2]));
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        txtClass = (TextView) findViewById(R.id.textView1);
        txtProfessor = (TextView) findViewById(R.id.textView2);
        txtDescript = (TextView) findViewById(R.id.textView3);
        txtClass.setText(this.className + "-" + this.sec);
        txtProfessor.setText(this.professor);
        txtDescript.setText(this.classDescription);
        studentList = new ArrayList<>();
        allChats = server.getChatrooms();
        students = allChats.getStudents(className, sec);

        for (Student student : students) {
            studentList.add(student);
        }
        studentListView = (ListView) findViewById(R.id.list_classmates);
        BuddyListAdapter adapter  = new BuddyListAdapter(this, studentList);
        studentListView.setAdapter(adapter);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        mImageView.setImageBitmap(imageBitmap);
    }
}
    */
}
