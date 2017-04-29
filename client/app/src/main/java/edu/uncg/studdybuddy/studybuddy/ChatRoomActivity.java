
package edu.uncg.studdybuddy.studybuddy;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import StudyBuddy.Chatrooms;
import StudyBuddy.Student;
import edu.uncg.studdybuddy.client.StudyBuddyConnector;

public class ChatRoomActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();
    private static final int MY_REQUEST_CODE = 1000;

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
    private BuddyListAdapter buddyAdapter;
    private final AtomicBoolean waiting = new AtomicBoolean(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
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
                switch (pieces[0]) {
                    case "06": {
                        if (pieces[1].equalsIgnoreCase("BUDDYONLINE") && pieces[3].equals(className) && pieces[4].equals(sec)) {
                            for (int c = 0; c < studentList.size(); c++) {
                                Student stud = studentList.get(c);
                                if (stud.getStudentName().equals(pieces[5])) {
                                    stud.setOnlineStatus(true);
                                }
                            }
                            updateBuddyAdapter();
                        } else if (pieces[1].equalsIgnoreCase("BUDDYOFFLINE") && pieces[3].equals(className) && pieces[4].equals(sec)) {
                            for (int c = 0; c < studentList.size(); c++) {
                                Student stud = studentList.get(c);
                                if (stud.getStudentName().equals(pieces[5])) {
                                    stud.setOnlineStatus(false);
                                }
                            }
                            updateBuddyAdapter();
                        }
                        break;
                    }

                    case "11": {
                        if (pieces[1].equals("CHATMESS") && pieces[2].equals(className) && pieces[3].equals(sec)) {
                            String senderName = MainMenu.chatrooms.getStudent(pieces[2], pieces[3], pieces[4]).getStudentName();
                            String chatMessage = "";
                            if (pieces.length > 8) {
                                for (int c = 7; c < pieces.length; c++) {
                                    chatMessage += pieces[c] + ":";
                                }
                                chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
                            } else {
                                chatMessage = pieces[7];
                            }
                            chatMessList.add(new ChatRoomMessage(senderName, chatMessage));
                            updateAdapter();
                        }
                        break;
                    }
                    case "13": {
                        if (pieces[5].equalsIgnoreCase("INCOMING") && pieces[1].equalsIgnoreCase("SENDFILE")) {
                            String senderName = MainMenu.chatrooms.getStudent(pieces[3], pieces[4], pieces[8]).getStudentName();
                            String chatMessage = "INCOMING FILE:  " + pieces[2] + "  - " + pieces[7] + " bytes";
                            chatMessList.add(new ChatRoomMessage(senderName, chatMessage));
                            updateAdapter();
                        } else if (pieces[5].equalsIgnoreCase("ACCEPTED") && pieces[1].equalsIgnoreCase("SENDFILE")) {
                            String chatMessage = "INCOMING FILE:  " + pieces[2] + "  - " + pieces[7] + " bytes";
                            chatMessList.add(new ChatRoomMessage(myName, chatMessage));
                            updateAdapter();
                            server.addMessageToChatrooms(pieces[3], pieces[4], server.getUserEmail(), chatMessage);
                        }
                        break;
                    }
                }
            }
        });

        // Read in objects from xml
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
        messagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String reply = chatMessList.get(position).getMessage();
                if (reply.length() >= 15) {
                    String substring = reply.substring(0, 14);
                    if (substring.equals("INCOMING FILE:")) {
                        String[] parts = reply.split(" ");
                        String fileName = parts[3];
                        File clickedFile = new File(Environment.getExternalStorageDirectory() + "/" + className + "_" + sec + "/" + fileName);
                        if (clickedFile.isFile() && isStoragePermissionGranted()) {
                            showPhoto(clickedFile);
                        }
                    }
                }

            }
        });

        mBtnSend = (Button) findViewById(R.id.btnSend);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    private void sendMessage() {
        server.sendToChatroom(this.className, this.sec, mTxtTextBody.getText().toString());
        mTxtTextBody.setText("");
    }

    private void updateAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateBuddyAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buddyAdapter.notifyDataSetChanged();
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
        updateList();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_sendpic:
                dispatchTakePictureIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateList() {
        studentList = new ArrayList<>();
        allChats = server.getChatrooms();
        students = allChats.getStudents(className, sec);
        for (Student student : students) {
            studentList.add(student);
        }
        studentListView = (ListView) findViewById(R.id.list_classmates);
        buddyAdapter = new BuddyListAdapter(this, studentList);
        studentListView.setAdapter(buddyAdapter);
    }

    // On click listeners for the top-right menu buttons.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            dispatchTakePictureIntent();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Permissions for camera and external storage.

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 2);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void dispatchTakePictureIntent() {
        if (this.isStoragePermissionGranted()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, MyFileContentProvider.CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                File out = new File(getFilesDir(), "newImage.jpg");

                if (!out.exists()) {
                    return;
                }
                File finalFile = createFile(out);
                String mCurrentPhotoPath = finalFile.getAbsolutePath();
                server.sendFileToChatroom(getApplicationContext(), this.className, this.sec, finalFile);

            }
        }
    }

    private void showPhoto(File entry) {
        Uri uri = Uri.parse("file://" + entry.getPath());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        String mime = "*/*";
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        if (mimeTypeMap.hasExtension(
                mimeTypeMap.getFileExtensionFromUrl(uri.toString())))
            mime = mimeTypeMap.getMimeTypeFromExtension(
                    mimeTypeMap.getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri, mime);
        startActivity(intent);
    }

    private File createFile(File source) {

        File directory = new File(Environment.getExternalStorageDirectory() + "/" + this.className + "_" + this.sec);

        //if it doesn't exist the folder will be created
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image_file = null;

        try {
            image_file = File.createTempFile(imageFileName, ".jpg", directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(image_file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long sourceLength = source.length();
        // Transfer bytes from in to out
        byte[] buf = new byte[(int) sourceLength];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image_file;
    }

}


