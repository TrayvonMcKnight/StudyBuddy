package StudyBuddy;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class OnlineClientList {

    // Private class fields.
    private ClientNode header;
    private int numElements;

    //create an empty list
    public OnlineClientList() {
        header = new ClientNode();  // Creates empty node for header.
        numElements = 0;
    }

    public void addClient(String user, String mail, String addy, int stat, ObjectInputStream in, ObjectOutputStream out, Object session) {
        ClientNode curr = header.next;  // Create a pointer named curr.
        ClientNode add = new ClientNode(user, mail, addy, stat, in, out, session);   // Create a DNode with the data.

        // if the list is empty, then just insert the item as the first item in the list.
        if (curr == header) {
            // Just attach the newly created node to the header.
            add.next = header;
            add.prev = header;
            header.next = add;
            header.prev = add;
            numElements = 1;
        } // However, if the list is not empty, iterate through the list until you find the correct
        // location to add the new item.
        else {
            // Iterate through the list until the proper location to place the data has been reached.
            while (curr != header) {
                int result = add.email.compareTo(curr.email);
                if (result < 0) {
                    // Once the proper location has been found, add the new item.
                    curr.prev.next = add;
                    add.prev = curr.prev;
                    add.next = curr;
                    curr.prev = add;
                    numElements++;
                    curr = header;  // once the item has been added, break the loop.
                } else if (curr.next == header) {
                    // If the proper location is at the end of the list, add it here.
                    curr.next.prev = add;
                    curr.next = add;
                    add.prev = curr;
                    add.next = header;
                    numElements++;
                    curr = header;  // once the item has been added, break the loop.
                } else {
                    curr = curr.next; // Else, advance the pointer to the next location and try there.
                }
            }
        }
    }

    public boolean removeClient(String user) {
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (!curr.email.equals(user)) {
                curr = curr.next; // if data element not found, advance through the list.
            } // else data element is found and we un-link it from the list.
            else {
                curr.prev.next = curr.next;
                curr.next.prev = curr.prev;
                numElements--;
                return true;    // return true after un-linking of the node.
            }
        }
        return false;   // return false if it makes it through the list without finding 'val'.
    }

    public int getNumberOfOnlineClients() {
        return numElements;
    }

    public String getClientEmail(String user) {
        String temp = "";
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (!curr.name.equals(user)) {
                curr = curr.next;
            } // if data element not found, advance through the list.
            else { // else data element is found and we un-link it from the list.
                return curr.email;
            }
        }
        return temp;
    }

    public String getClientIP(String user) {
        String temp = "";
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (!curr.email.equals(user)) {
                curr = curr.next; // if data element not found, advance through the list.
            } // else data element is found and we un-link it from the list.
            else {
                return curr.IP;
            }
        }
        return temp;
    }

    public int getClientStatus(String user) {
        int temp = 9;
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (!curr.email.equals(user)) {
                curr = curr.next; // if data element not found, advance through the list.
            } // else data element is found and we un-link it from the list.
            else {
                return curr.status;
            }
        }
        return temp;
    }

    public String[] onlineClientsToArray() {
        int counter = 0;
        String[] tempArray = new String[this.numElements];
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            tempArray[counter] = curr.email;
            counter++;
            curr = curr.next;
        }
        return tempArray;
    }

    public boolean contains(String username) {
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (!curr.email.equals(username)) {
                curr = curr.next; // if data element not found, advance through the list.
            } // else data element is found and we un-link it from the list.
            else {
                return true;
            }
        }
        return false;
    }

    public Object returnUserSession(String username) {
        ClientNode curr = header.next;   // Create a pointer node.
        // Loop through the list
        while (curr != header) {
            if (curr.email.equals(username)) {
                return curr.getSession();
            }
            curr = curr.next;
        }
        return null;
    }

    //inner ClientNode class
    private class ClientNode {

        private ClientNode next, prev;
        private String name, email, IP;
        private int status;
        private final ObjectInputStream in;
        private final ObjectOutputStream out;
        private final Object session;

        private ClientNode() {
            this.name = null;
            this.email = null;
            this.IP = null;
            this.status = 9;
            this.in = null;
            this.out = null;
            this.session = null;
            next = prev = this;
        }

        private ClientNode(String user, String mail, String addy, int stat, ObjectInputStream objIn, ObjectOutputStream objOut, Object sess) {
            this.name = user;
            this.email = mail;
            this.IP = addy;
            this.status = stat;
            this.in = objIn;
            this.out = objOut;
            this.session = sess;
            next = prev = this;
        }

        private Object getSession() {
            return this.session;
        }
    }
}
