package util;

import java.sql.ResultSet;
import java.sql.Statement;

/** Utility class to send general queries to the database and get results. */
public class DBQuery {
    private static ResultSet resultSet;

    /** Creates a statement object to query the database. */
    public static void sendQuery(String query) {
        try {
            Statement statement = DBConnection.getDbConnection().createStatement();
            resultSet = statement.executeQuery(query);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Gets the results of a query. */
    public static ResultSet getQueryResult() {
        return resultSet;
    }

    /** Creates a statement object to update the database. */
    public static void sendUpdate(String query) {
        try {
            Statement statement = DBConnection.getDbConnection().createStatement();
            statement.executeUpdate(query);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
