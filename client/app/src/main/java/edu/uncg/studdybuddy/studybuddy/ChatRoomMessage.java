package edu.uncg.studdybuddy.studybuddy;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Anthony Ratliff on 4/4/2017.
 */

public class ChatRoomMessage {
    private String sender;
    private String message;
    private String time;

    public ChatRoomMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
        // Set time object to now.
        this.time = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
