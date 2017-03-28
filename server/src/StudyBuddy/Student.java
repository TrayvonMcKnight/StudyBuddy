package StudyBuddy;


import java.io.Serializable;

public class Student implements Serializable {
        // private class fields.
        private String name, email;
        private boolean online;
        private int status;
        
        public Student(String name, String email){
            this.name = name;
            this.email = email;
            this.online = false;
        }
        
        public Student(String name, String email, Boolean online, int status){
            this.name = name;
            this.email = email;
            this.online = online;
            this.status = status;
        }
        
        public void setOnlineStatus(Boolean online){
            this.online = online;
        }
        
        public boolean getOnlineStatus(){
            return this.online;
        }
        
        public void setAvailability(int status){
            this.status = status;
        }
        
        public String getAvailability(){
            switch (this.status){
                case 0: {
                    return "Available";
                }
                case 1: {
                    return "Away";
                }
                case 2: {
                    return "Unavilable";
                }
            }
            return null;
        }
        
        public String getStudentName(){
            return this.name;
        }
        
        public String getStudentEmail(){
            return this.email;
        }
        
    }