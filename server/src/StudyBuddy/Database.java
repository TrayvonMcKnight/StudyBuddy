package StudyBuddy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database{
    // Private class fields

    private final String DB_URL = "jdbc:mysql://127.0.0.1:3306/?autoReconnect=true&useSSL=false"; //javachat?zeroDateTimeBehavior=convertToNull";
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
            // Code 0 means there is no driver.
            // Code 1045 means the login was not accepted bad password.
            int error = ex.getErrorCode();
            if (error == 0){
                System.out.println("Database Error:  The Java SQL connector is not available.  Please install and try again.");
            }
            if (error == 1045){
                System.out.println("Database Error:  The Username or Password used to access the database is incorrect or not accepted.");
            }
            System.out.println("The Database does not appear to exist.  Can not continue without this major component.");
            System.exit(1);
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
    
    // Private Class methods.
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
                + "  `user_status` VARCHAR(50) NULL,"
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
    
    // Public Methods.
    
    /*
     The registerUser method will accept 4 string representing an email, password,
     first name, and last name. The mehtod will return an integer which
     represents the status of attempting to create a new account.
     0 = Account Created.
     1 = Account Not Created. User already exists in database.
     2 = Account Not Created. Bad password requirements.
     3 = Account Not Created. Bad email requirements.
     4 = Account Not Created. Database error.
     */
    public int registerUser(String email, String pass, String fName, String lName){
        int temp = 0;
        if (this.getUserID(email) == 0){
            // Check the password.
            if (pass.length() > 3){
                if (email.contains("@") && email.contains(".")){
                try {
                    // Add new user to database.
                    
                    // the mysql insert statement
                    this.sql = " insert into members (email, pass_word, first_name, last_name, user_status, date_joined, last_login, logged_in)"
                            + " values (?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    // Read the current date and time.
                    Date curDate = new Date();
                    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
                    // create the mysql insert prepared statement
                    this.statement = db_con.prepareStatement(this.sql);
                    this.statement.setString(1, email);
                    this.statement.setString(2, pass);
                    this.statement.setString(3, fName);
                    this.statement.setString(4, lName);
                    this.statement.setString(5, "Available");
                    this.statement.setString(6, ft.format(curDate));
                    this.statement.setString(7, null);
                    this.statement.setInt(8, 0);
                    
                    // Execute the query.
                    this.statement.execute();
                    temp = 0;
                } catch (SQLException ex) {
                    Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                    temp = 4;
                }
                }
                else {
                    temp = 3;
                }
            } else {
                // Return password error.
                temp = 2;
            }
        } else {
            // Return user already exists.
            temp = 1;
        }
        return temp;
    }
    
    public ResultSet returnUserInfo(String username) {
        ResultSet result = null;
        this.sql = "SELECT * FROM members WHERE email= ?";
        try {
            this.statement = db_con.prepareStatement(this.sql);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.statement.setString(1, username);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            result = statement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean updateLastLoginTime(String username) {
        boolean result = false;
        if (this.getUserID(username) != 0) {
            Date curDate = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            this.sql = "UPDATE members SET last_login= ? WHERE email= ?";
            try {
                statement = db_con.prepareStatement(sql);
                statement.setString(1, ft.format(curDate));
                statement.setString(2, username);
                statement.executeUpdate();
                result = true;

            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                return result;
            }
        }
        return result;
    }

    public boolean updateUserPassword(String username, String pass) {
        boolean success = false;
        if (this.getUserID(username) != 0) {
            try {
                this.sql = "UPDATE members SET pass_word= ? WHERE email= ?";
                statement = db_con.prepareStatement(this.sql);
                statement.setString(1, pass);
                statement.setString(2, username);
                statement.executeUpdate();
                success = true;
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }

    public boolean updateUserLoggedIn(String username, boolean status) {
        boolean success = false;
        if (this.getUserID(username) != 0) {
            int stat;
            if (status) {
                stat = 1;
            } else {
                stat = 0;
            }
            try {
                this.sql = "UPDATE members SET logged_in= ? WHERE email= ?";
                statement = db_con.prepareStatement(this.sql);
                statement.setInt(1, stat);
                statement.setString(2, username);
                statement.executeUpdate();
                success = true;
            } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }
    
    public int getUserID(String username) {
        int id = 0;
        this.sql = "SELECT user_id_number FROM members WHERE email= ?";
        try {
            this.statement = db_con.prepareStatement(this.sql);
            this.statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                id = result.getInt("user_id_number");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }
    
    /*
     The getUserStatus method will accept a string representing a username and
     will return an integer which represents the status of the corresponding
     user.
     0 = Available
     1 = Away
     2 = Unavailable
     3 = Invisible
     */
    public int getUserStatus(String username) {
        int stat = 9;
        this.sql = "SELECT user_status FROM members WHERE email= ?";
        try {
            this.statement = db_con.prepareStatement(this.sql);
            this.statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                if (result.getString("user_status").equals("Available")) {
                    stat = 0;
                }
                if (result.getString("user_status").equals("Away")) {
                    stat = 1;
                }
                if (result.getString("user_status").equals("Unavailable")) {
                    stat = 2;
                }
                if (result.getString("user_status").equals("Invisible")) {
                    stat = 3;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stat;

    }

    public ResultSet returnAllClasses() {
        ResultSet result = null;
        this.sql = "SELECT * FROM classes";
        try {
            this.statement = db_con.prepareStatement(this.sql);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            result = statement.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public ResultSet returnAllClassesByStudent(String email){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResultSet returnAllStudents(String className, String section) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}