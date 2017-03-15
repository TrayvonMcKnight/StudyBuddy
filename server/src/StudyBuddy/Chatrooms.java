package StudyBuddy;

import java.io.Serializable;
import java.util.Arrays;

public class Chatrooms implements Serializable {
    // Private class fields
    private Chatroom[] rooms;
    private int numElements;
    
    // Class Constructor
    public Chatrooms(){
        this.numElements = 0;
        this.rooms = new Chatroom[10];
    }
    
    // Class methods.
    private void ensureCapacity(){
        if (this.numElements == this.rooms.length){
            Chatroom[] temp = new Chatroom[this.rooms.length * 2];
            System.arraycopy(this.rooms, 0, temp, 0, this.numElements);
            this.rooms = temp;
        }
    }
    
    private int getIndex(String classname){
        int temp = -1;
        for (int c = 0;c < this.numElements;c++){
            if (this.rooms[c].getClassName().equalsIgnoreCase(classname)){
                temp = c;
                break;
            }
        }
        return temp;
    }
    
    public void addChatroom(String chatName, String profName){
        this.ensureCapacity();
        this.rooms[this.numElements++] = new Chatroom(chatName, profName);
    }
    public void addStudent(String chatName, String studName){
        this.rooms[getIndex(chatName)].addStudent(studName);
    }
    
    public void addMessage(String chatName, String studName, String time, String message){
        this.rooms[getIndex(chatName)].addMessage(studName, time, message);
    }
    
    public String[] getStudents(String chatName){
        return this.rooms[this.getIndex(chatName)].getStudents();
    }
    
    public String[][] getMessages(String chatName){
        return this.rooms[this.getIndex(chatName)].getMessages();
    }
    
    public String getProfessorName(String chatName){
        return this.rooms[this.getIndex(chatName)].getProfessorName();
    }
    
    
    private class Chatroom implements Serializable {
        // Private class fields.
        private final String className;
        private final String professorName;
        private String[] students;
        private String[][] messages;
        private int studentElements;
        private int messageElements;
        
        // Class constructor
        private Chatroom(String classname, String profname){
            this.className = classname;
            this.professorName = profname;
            this.students = new String[10];
            this.messages = new String[10][3];
            this.studentElements = 0;
            this.messageElements = 0;
        }
        
        private void studentEnsureCapacity(){
            if (this.studentElements == this.students.length){
                String[] temp = new String[students.length * 2];
                System.arraycopy(this.students, 0, temp, 0, this.studentElements);
                this.students = temp;
            }
        }
        
        private void messageEnsureCapacity(){
            if (this.messageElements == this.messages.length){
                String[][] temp = new String[this.messages.length * 2][3];
                for (int i = 0;i < this.messageElements;i++){
                    System.arraycopy(this.messages[i], 0, temp[i], 0, 3);
                }
                this.messages = temp;
            }
        }
        
        private String getClassName(){
            return this.className;
        }
        
        private String getProfessorName(){
            return this.professorName;
        }
        
        private int returnNumberOfStudents(){
            return this.studentElements;
        }
        
        private int returnNumberOfMessages(){
            return this.messageElements;
        }
        
        private String[] getStudents(){
            return this.students;
        }
        
        private String[][] getMessages(){
            return this.messages;
        }
        
        private void addStudent(String name){
            this.studentEnsureCapacity();
            this.students[this.studentElements++] = name;
            Arrays.sort(this.students);
        }
        
        private void addMessage(String sender, String time, String message){
            this.messageEnsureCapacity();
            this.messages[this.messageElements][0] = sender;
            this.messages[this.messageElements][1] = time;
            this.messages[this.messageElements++][2] = message;
        }
    }
}