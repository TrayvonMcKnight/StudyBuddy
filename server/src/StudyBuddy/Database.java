package StudyBuddy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Database{
    // Private class fields

    private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/"; //javachat?zeroDateTimeBehavior=convertToNull";
    private final String DB_USER = "root";
    private final String DB_PASS = "password";
    private Connection db_con;
    private PreparedStatement statement;
    private CallableStatement callable;
    private String sql;
    private boolean connected;
    
    // Class constructor
    public Database(){
        this.connected = false;
    }
    
    // Public getters and setters
    public boolean isConnected() {
        return this.connected;
    }
}