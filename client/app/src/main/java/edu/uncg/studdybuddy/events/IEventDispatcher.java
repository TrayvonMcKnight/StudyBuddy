package edu.uncg.studdybuddy.events;

/**
 * Created by Anthony Ratliff on 3/26/2017.
 */

public interface IEventDispatcher {
    public void addEventListener(String type, IEventHandler cbInterface);
    public void removeEventListener(String type);
    public void dispatchEvent(Event event);
    public Boolean hasEventListener(String type);
    public void removeAllListeners();
}
