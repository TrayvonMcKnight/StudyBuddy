package StudyBuddy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database{
    // Private class fields

    private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/"; //javachat?zeroDateTimeBehavior=convertToNull";
    private final String DB_USER = "studybuddy";
    private final String DB_PASS = "TheStudyBuddyPassword";
    private Connection db_con;
    private PreparedStatement statement;
    private CallableStatement callable;
    private String sql;
    private boolean connected;
    
    // Class constructor
    public Database(){
        this.connected = false;
        try {
            this.db_con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.sql = "use studybuddy";
            this.statement = db_con.prepareStatement(sql);
        } catch (SQLException ex) {
            System.out.println("Unable to Create the statement.  Bad input.");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ResultSet resultSet = statement.executeQuery();
        } catch (SQLException ex) {
            System.out.println("First run detected.  Attempting to create the database tables.");
            this.createDatabaseSchema();
        }
        System.out.println("Connected to Database");
        this.connected = true;
    }
    
    // Public getters and setters
    public boolean isConnected() {
        return this.connected;
    }

    private boolean createDatabaseSchema() {
        boolean success = false;
        this.sql = "CREATE SCHEMA IF NOT EXISTS `studybuddy` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;";
        try {
            this.callable = db_con.prepareCall(this.sql);
        } catch (SQLException ex) {
            System.out.println("Could not create the schema statement.");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }
        try {
            this.callable.execute();
        } catch (SQLException ex) {
            System.out.println("Could not execute the create schema statement.");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }
        this.sql = "CREATE TABLE IF NOT EXISTS `studybuddy`.`members` ("
                + "  `user_id_number` INT(12) NOT NULL AUTO_INCREMENT,"
                + "  `email` VARCHAR(50) NOT NULL,"
                + "  `pass_word` VARCHAR(50) NOT NULL,"
                + "  `first_name` VARCHAR(50) NULL,"
                + "  `last_name` VARCHAR(50) NULL,"
                + "  `date_joined` DATE NOT NULL,"
                + "  `last_login` DATE NULL,"
                + "  `logged_in` TINYINT(1) NOT NULL,"
                + "  PRIMARY KEY (`user_id_number`),"
                + "  UNIQUE INDEX `id_number_UNIQUE` (`user_id_number` ASC),"
                + "  UNIQUE INDEX `user_name_UNIQUE` (`email` ASC)) "
                + "ENGINE = InnoDB;";
        try {
            this.callable = db_con.prepareCall(this.sql);
        } catch (SQLException ex) {
            System.out.println("Could not create members table statement");
            System.out.println(ex);
            //Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }
        try {
            this.callable.execute();
        } catch (SQLException ex) {
            System.out.println("Could not execute create members table");
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }
        this.sql = "use studybuddy;";
        try {
            this.callable = db_con.prepareCall(sql);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }
        try {
            this.callable.execute();
            success = true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            return success;
        }

        return success;
    }
    
    
}