package edu.uncg.studdybuddy.events;

/**
 * Created by Anthony Ratliff on 3/26/2017.
 */

public class Listener {
    private String type;
    private IEventHandler handler;

    public Listener(String type, IEventHandler handler){
        this.type = type;
        this.handler = handler;
    }

    public String getType(){
        return this.type;
    }

    public IEventHandler getHandler(){
        return this.handler;
    }
}
