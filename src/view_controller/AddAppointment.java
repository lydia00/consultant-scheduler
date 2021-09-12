package view_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.TimeZone;

/** Controller for the Add Appointment screen. */
public class AddAppointment implements Initializable {
    public TextField appointmentId;
    public TextField addTitle;
    public TextArea addDesc;
    public TextField addLocation;
    public ComboBox addContact;
    public TextField addType;
    public DatePicker addStartDate;
    public TextField addStartTime;
    public DatePicker addEndDate;
    public TextField addEndTime;
    public TextField addCustId;
    public Label dynamicMsg;
    int contactId;

    /** Initializes the Update Appointment form. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Clears the UI message
        dynamicMsg.setText("");

        // Populates the Contact combo box
        addContact.setItems(listContacts());
    }

    public void saveHandler(ActionEvent actionEvent) throws SQLException, IOException {
        // Clears the UI message
        dynamicMsg.setText("");

        // Gets user input
        String apptId = appointmentId.getText();
        String title = addTitle.getText();
        String description = addDesc.getText();
        String location = addLocation.getText();
        String contact = addContact.getValue().toString();
        String type = addType.getText();
        String custId = addCustId.getText();
        LocalDate startDate = addStartDate.getValue();
        String startTimeString = addStartTime.getText();
        LocalDate endDate = addEndDate.getValue();
        String endTimeString = addEndTime.getText();

        // Validates that start and end times are in the correct format. Displays a warning if not.
        try {
            LocalTime.parse(startTimeString);
            LocalTime.parse(endTimeString);
        } catch (DateTimeParseException | NullPointerException e) {
            dynamicMsg.setText("Invalid time format entered.");
        }

        // Converts start time and end time to LocalTime.
        LocalTime startTime = LocalTime.parse(startTimeString);
        LocalTime endTime = LocalTime.parse(endTimeString);

        // Converts start date/time and end date/time to LocalDateTime.
        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        // Checks if there are overlapping appointments. Displays a message if so.
        if (overlappingAppointments(startDateTime, endDateTime)) {
            dynamicMsg.setText("Overlapping appointment already scheduled.");
        }
        else {
            // Converts LocalDateTime to ZonedDateTime
            ZoneId userZone = ZoneId.of(TimeZone.getDefault().getID());
            ZonedDateTime startZonedDT = ZonedDateTime.of(startDateTime, userZone);
            ZonedDateTime endZonedDT = ZonedDateTime.of(endDateTime, userZone);

            // Calculates the UTC time from the zoned start and end date-times
            ZonedDateTime startUtcDT = startZonedDT.withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime endUtcDT = endZonedDT.withZoneSameInstant(ZoneId.of("UTC"));

            // Checks if the entered times are outside of business hours (8am - 10pm EST). Displays a message if so.
            ZonedDateTime startEstDT = startUtcDT.withZoneSameInstant(ZoneId.of("America/New_York"));
            ZonedDateTime endEstDT = endUtcDT.withZoneSameInstant(ZoneId.of("America/New_York"));
            int startEstHour = startEstDT.getHour();
            int endEstHour = endEstDT.getHour();
            int endEstMinute = endEstDT.getMinute();

            if (startEstHour < 8 || startEstHour > 22 || endEstHour > 22 || endEstHour < 8) {
                dynamicMsg.setText("Time is outside of 8am–10pm EST.");
            } else if (endEstHour == 22 && endEstMinute > 0) {
                dynamicMsg.setText("Time is outside of 8am–10pm EST.");
            }

            // Prepares input for database and sends an insert query
            else {
                // Converts UTC times to LocalDateTime for database storage
                LocalDateTime startLocalUtc = startUtcDT.toLocalDateTime();
                LocalDateTime endLocalUtc = endUtcDT.toLocalDateTime();

                // Gets the contact ID for the selected contact
                DBQuery.sendQuery("SELECT Contact_ID FROM contacts WHERE Contact_Name = \"" + contact + "\";");
                ResultSet resultSet = DBQuery.getQueryResult();
                while (resultSet.next()) {
                    try {
                        this.contactId = resultSet.getInt("Contact_ID");
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }

                // Adds the appointment to the database
                DBQuery.sendUpdate("INSERT INTO appointments " +
                        "(Title, Description, Location, Type, Start, End, Customer_ID, Contact_ID) " +
                        "VALUES (\"" + title + "\", \"" + description + "\", \"" + location + "\", \"" + type + "\", \"" + startLocalUtc + "\", \"" + endLocalUtc + "\", " + custId + ", " + contactId + ");");

                // Informs user that the update was successful and returns to the Appointments screen.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Appointment added!");
                alert.setContentText("Appointment successfully saved to database.");
                alert.showAndWait();
                toAppointments(actionEvent);
            }
        }
    }

    /** Sends the user back to the Appointments screen. */
    public void backToApptsHandler(ActionEvent actionEvent) throws IOException {
        toAppointments(actionEvent);
    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    /** Function to check if there are overlapping appointments. */
    public boolean overlappingAppointments(LocalDateTime startNewAppt, LocalDateTime endNewAppt) throws SQLException {
        boolean overlapExists = false;
        DBQuery.sendQuery("SELECT * FROM appointments");
        ResultSet resultSet = DBQuery.getQueryResult();
        while (resultSet.next()) {
            LocalDateTime startExistingAppt = resultSet.getTimestamp("Start").toLocalDateTime();
            LocalDateTime endExistingAppt = resultSet.getTimestamp("End").toLocalDateTime();
            if (startNewAppt.isEqual(startExistingAppt) || startNewAppt.isEqual(endExistingAppt)) {
                overlapExists = true;
                break;
            }
            else if (startNewAppt.isAfter(startExistingAppt) && startNewAppt.isBefore(endExistingAppt)) {
                overlapExists = true;
                break;
            }
            else if (endNewAppt.isEqual(startExistingAppt) || endNewAppt.isEqual(endExistingAppt)) {
                overlapExists = true;
                break;
            }
            else if (endNewAppt.isAfter(startExistingAppt) && endNewAppt.isBefore(endExistingAppt)) {
                overlapExists = true;
                break;
            }
            else {
                overlapExists = false;
            }
        }

        return overlapExists;
    }

    /** Function to populate the Contact combo box.*/
    public ObservableList listContacts() {
        ObservableList<String> contacts = FXCollections.observableArrayList();

        try {
            // Clears list to prevent duplication.
            contacts.removeAll(contacts);

            // Gets contact names from the database
            DBQuery.sendQuery("SELECT Contact_Name FROM contacts");
            ResultSet resultSet = DBQuery.getQueryResult();
            while (resultSet.next()) {
                contacts.add(resultSet.getString("Contact_Name"));
            }

        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return contacts;
    }

    /** Function to send the user back to the Appointments screen. */
    public void toAppointments(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/Appointments.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Appointments");
        stage.setScene(scene);
        stage.show();
    }
}
