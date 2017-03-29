package edu.uncg.studdybuddy.events;

import StudyBuddy.Chatrooms;

/**
 * Created by Anthony Ratliff on 3/26/2017.
 */

public class Event {
    public final static String CHATROOMS = "Chatrooms";
    protected String strType = "";
    private String message = "";
    private Chatrooms chatrooms;

    public Event(String type){
        initProperties(type);
    }

    protected void initProperties(String type){
        strType = type;
    }

    public String getStrType(){
        return strType;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String mess){
        this.message = mess;
    }

    public Chatrooms getChatrooms(){
        return this.chatrooms;
    }

    public void setChatrooms(Chatrooms rooms){
        this.chatrooms = rooms;
    }
}
