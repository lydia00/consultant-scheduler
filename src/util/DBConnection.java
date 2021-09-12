package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Utility class to connect to the database. */
public class DBConnection {
    //JDBC URL parts
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String ipAddress = "//wgudb.ucertify.com/";
    private static final String dbName = "WJ08CZ3";

    //JDBC URL parts
    private static final String jdbcUrl = protocol + vendor + ipAddress + dbName;

    //Driver and connection interface reference
    private static final String mySqlJdbcDriver = "com.mysql.cj.jdbc.Driver";
    private static Connection dbConn = null;

    //JDBC driver username and password
    private static final String username = "U08CZ3";
    private static final String password = "53689249539";

    public static Connection startDbConnection() {
        try{
            Class.forName(mySqlJdbcDriver);
            dbConn = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Connected to database.");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return dbConn;
    }

    public static Connection getDbConnection() {
        return dbConn;
    }

    public static void closeDbConnection(){
        try {
            dbConn.close();
        } catch (Exception e) {
            // Do nothing if connection is already closed.
        }
        System.out.println("Disconnected from database.");
    }

}
