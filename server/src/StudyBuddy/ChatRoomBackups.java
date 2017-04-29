package StudyBuddy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatRoomBackups {

    // private class fields
    private final File file = new File("SavedRooms.dat");

    // public class methods.
    // Save the current state of the chatrooms to file.
    public boolean saveChatRoomStatus(Chatrooms rooms) {
        boolean success = false;
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(this.file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(rooms);
            success = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatRoomBackups.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatRoomBackups.class.getName()).log(Level.SEVERE, null, ex);
        }

        return success;
    }

    // Load the current state of the chatrooms from file.
    public Chatrooms loadChatRoomStatus() {
        Chatrooms rooms = null;
        FileInputStream fin;
        try {
            fin = new FileInputStream(this.file);
            ObjectInputStream ois = new ObjectInputStream(fin);
            rooms = (Chatrooms) ois.readObject();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatRoomBackups.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ChatRoomBackups.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rooms;
    }

    // Determine if a file exists.
    public boolean fileExists() {
        return file.exists();
    }
}
