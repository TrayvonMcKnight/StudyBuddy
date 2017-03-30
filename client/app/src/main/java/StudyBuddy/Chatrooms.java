package StudyBuddy;

import java.io.Serializable;

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

    private int getIndex(String classname, String section){
        int temp = -1;
        for (int c = 0;c < this.numElements;c++){
            if (this.rooms[c].getClassName().equalsIgnoreCase(classname) && this.rooms[c].getSection().equalsIgnoreCase(section)){
                temp = c;
                break;
            }
        }
        return temp;
    }

    public int getNumberOfClasses(){
        return this.numElements;
    }

    public String[] getClassNamesAndSection(){
        String temp[] = new String[this.getNumberOfClasses()];
        for (int c = 0; c < this.getNumberOfClasses();c++){
            temp[c] = this.rooms[c].getClassName() + ":" + this.rooms[c].getSection();
        }
        return temp;
    }

    public int getNumberOfStudents(String className, String section){
        return this.rooms[this.getIndex(className, section)].returnNumberOfStudents();
    }

    public void addChatroom(String chatName, String section, String profName, String email){
        this.ensureCapacity();
        this.rooms[this.numElements++] = new Chatroom(chatName, profName, section, email);
    }

    public void addChatroom(Chatroom room){
        this.ensureCapacity();
        this.rooms[this.numElements++] = room;
    }

    public Chatroom getChatroom(String chatName, String section){
        return this.rooms[this.getIndex(chatName, section)];
    }

    public void addStudent(String chatName, String section, String studName, String email, Boolean online, int status){
        this.rooms[getIndex(chatName, section)].addStudent(studName, email, online, status);
    }

    public void addMessage(String chatName, String section, String studName, String time, String message){
        this.rooms[getIndex(chatName, section)].addMessage(studName, time, message);
    }

    public Student[] getStudents(String chatName, String section){
        return this.rooms[this.getIndex(chatName, section)].getStudents();
    }

    public String[][] getMessages(String chatName, String section){
        return this.rooms[this.getIndex(chatName, section)].getMessages();
    }

    public String getProfessorName(String chatName, String section){
        return this.rooms[this.getIndex(chatName, section)].getProfessorName();
    }

    public String getClassSection(String chatName, String section) {
        return this.rooms[this.getIndex(chatName, section)].getSection();
    }

    public String getProfessorEmail(String chatName, String section){
        return this.rooms[this.getIndex(chatName, section)].getProfessorEmail();
    }


    private class Chatroom implements Serializable {
        // Private class fields.
        private final String className;
        private final String section;
        private final String professorName;
        private final String professorEmail;
        private Student[] students;
        private String[][] messages;
        private int studentElements;
        private int messageElements;

        // Class constructor
        private Chatroom(String classname, String profname, String section, String email){
            this.className = classname;
            this.section = section;
            this.professorName = profname;
            this.professorEmail = email;
            this.students = new Student[10];
            this.messages = new String[10][3];
            this.studentElements = 0;
            this.messageElements = 0;
        }

        private void studentEnsureCapacity(){
            if (this.studentElements == this.students.length){
                Student[] temp = new Student[students.length * 2];
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

        private String getSection(){
            return this.section;
        }

        private String getProfessorEmail(){
            return this.professorEmail;
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

        private Student[] getStudents(){
            return this.students;
        }

        private String[][] getMessages(){
            return this.messages;
        }

        private void addStudent(String name, String email, Boolean online, int status){
            this.studentEnsureCapacity();
            this.students[this.studentElements++] = new Student(name, email, online, status);
        }

        private void addMessage(String sender, String time, String message){
            this.messageEnsureCapacity();
            this.messages[this.messageElements][0] = sender;
            this.messages[this.messageElements][1] = time;
            this.messages[this.messageElements++][2] = message;
        }
    }


}