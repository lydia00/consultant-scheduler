package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.DBQuery;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/** Controller for the Login screen */
public class LoginScreen implements Initializable {
    public Button loginButton;
    public TextField loginUsername;
    public TextField loginPassword;
    public Label loginLocationDetected;
    public Button exitButton;
    public Label loginMsg;
    public Label loginHeader;
    public Label loginUsernameLabel;
    public Label loginPasswordLabel;
    public Label loginLocationLabel;
    public Label timeZoneLabel;
    public Label timeZoneDetected;

    /** Initializes login screen based on language and location settings. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Displays user's time zone, location and language ID.
            TimeZone timeZone = TimeZone.getDefault();
            timeZoneDetected.setText(timeZone.getID());
            loginLocationDetected.setText(String.valueOf(Locale.getDefault()));

            // Determines the default language for the login screen.
            if (Locale.getDefault().getLanguage().equals("fr")) {
                Locale.setDefault(new Locale("fr"));
            }

            // Gets login screen text from the lang resource bundle.
            ResourceBundle rb = ResourceBundle.getBundle("languages/lang", Locale.getDefault());
            loginHeader.setText(rb.getString("headerTrans"));
            loginUsernameLabel.setText(rb.getString("usernameTrans"));
            loginPasswordLabel.setText(rb.getString("passwordTrans"));
            loginLocationLabel.setText(rb.getString("locationTrans"));
            timeZoneLabel.setText(rb.getString("timeZoneTrans"));
            loginButton.setText(rb.getString("signInTrans"));
            exitButton.setText(rb.getString("exitTrans"));
            loginMsg.setText(rb.getString("errorTrans"));

            // Hides login error message.
            loginMsg.setVisible(false);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Verifies username and password, displays a message if not valid.
     * Notifies user if there is an appointment in the next 15 minutes.
     * Opens the Appointments screen.
     * Records results of login attempt in login_activity.txt.
     * */
    public void loginHandler(ActionEvent actionEvent) throws IOException, SQLException {
        // Gets user input
        String usernameIn = loginUsername.getText();
        String passwordIn = loginPassword.getText();

        // Verifies login
        boolean validUser = verifyLogin(usernameIn, passwordIn);

        // Open the Appointments screen if the login is valid.
        if (validUser) {
            // Record successful login
            FileWriter fileWriter = new FileWriter("login_activity.txt", true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Successful Login: " + usernameIn + " on " + LocalDateTime.now());
            printWriter.close();

            // Notify user whether there is an appointment starting within 15 minutes
            upcomingAppointment();

            // Open the Appointments screen
            Parent root = FXMLLoader.load(getClass().getResource("/view_controller/Appointments.fxml"));
            Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Appointments");
            stage.setScene(scene);
            stage.show();
        }
        // Show error message if the login is not valid.
        else {
            // Record failed login
            FileWriter fileWriter = new FileWriter("login_activity.txt", true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Failed Login: " + usernameIn + " on " + LocalDateTime.now());
            printWriter.close();

            // Show error message
            loginMsg.setVisible(true);
        }
    }

    /** Function to verify username and password against users stored in the database. */
    private boolean verifyLogin(String username, String password) throws SQLException {
        try {
            // Query the users table
            DBQuery.sendQuery("SELECT * FROM users;");
            ResultSet resultSet = DBQuery.getQueryResult();

            // Cycle through users table and check input against stored data
            while (resultSet.next()) {
                if (resultSet.getString("User_Name").equals(username) &&
                        resultSet.getString("Password").equals(password)) {
                    return true;
                }
                else { return false; }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /** Function to check if there is an appointment within 15 minutes from login. */
    public void upcomingAppointment() {
        LocalDateTime currentUtcDT = LocalDateTime.now(ZoneId.of("UTC"));
        LocalDateTime timeIn15Minutes = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(15);

        try {
            // Query start times in the appointments table
            DBQuery.sendQuery("SELECT * FROM appointments " +
                    "WHERE Start BETWEEN \"" + currentUtcDT + "\" AND \"" + timeIn15Minutes + "\";");
            ResultSet resultSet = DBQuery.getQueryResult();
            resultSet.next();
            LocalDateTime apptStart = resultSet.getTimestamp("Start").toLocalDateTime();

            // Display a custom alert if there are upcoming appointments
            int apptId = resultSet.getInt("Appointment_ID");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Upcoming Appointment");
            alert.setHeaderText("Appointment starting soon!");
            alert.setContentText("Appointment " + apptId + " starts at " + apptStart.getHour() + ":" + apptStart.getMinute() + " on " + apptStart.getMonth() + " " + apptStart.getDayOfMonth() + ".");
            alert.showAndWait();
        } catch (SQLException e) {
            // Notify the user if there aren't any upcoming appointments
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Appointments");
            alert.setHeaderText("No Upcoming Appointments");
            alert.setContentText("There are no appointments starting in the next fifteen minutes.");
            alert.showAndWait();
        }

    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
