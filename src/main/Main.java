package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DBConnection;

import java.sql.SQLException;

/** Main class to launch and stage the application. */
public class Main extends Application {

    /** Shows the Scheduler login screen. */
    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/LoginScreen.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Consultant Scheduler Application");
        stage.setScene(scene);
        stage.show();
    }

    /** Main function that launches the application and opens the database connection.
     * Closes the database connection when the session ends. */
    public static void main(String[] args) throws SQLException {
        // Open connection to DB
        DBConnection.startDbConnection();

        // Run application
        launch(args);

        // Close connection to DB
        DBConnection.closeDbConnection();
    }
}
