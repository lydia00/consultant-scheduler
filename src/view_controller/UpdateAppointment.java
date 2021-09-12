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
import model.Appointment;
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

/** Controller for the Update Appointment screen. */
public class UpdateAppointment implements Initializable {
    public TextField appointmentId;
    public TextField updateTitle;
    public TextArea updateDesc;
    public TextField updateLocation;
    public ComboBox updateContact;
    public TextField updateType;
    public DatePicker updateStartDate;
    public TextField updateStartTime;
    public DatePicker updateEndDate;
    public TextField updateEndTime;
    public TextField updateCustomerId;
    public Label dynamicMsg;
    DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    int contactId;

    /** Initializes the Update Appointment form and populates it with data from the selected appointment. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Clears the UI message
        dynamicMsg.setText("");

        // Populates the form with selected appointment data
        Appointment selectedAppointment = Appointments.getSelectedAppointment();
        appointmentId.setText(String.valueOf(selectedAppointment.getApptId()));
        updateTitle.setText(String.valueOf(selectedAppointment.getTitle()));
        updateDesc.setText(String.valueOf(selectedAppointment.getDescription()));
        updateLocation.setText(String.valueOf(selectedAppointment.getLocation()));
        updateContact.setValue(selectedAppointment.getContact());
        updateType.setText(String.valueOf(selectedAppointment.getType()));
        updateCustomerId.setText(String.valueOf(selectedAppointment.getCustId()));

        // Populates the Contact combo box
        updateContact.setItems(listContacts());

        // Sets the start and end dates and times
        LocalDateTime startDT = LocalDateTime.parse(selectedAppointment.getStartDateTime(), dtf);
        LocalDateTime endDT = LocalDateTime.parse(selectedAppointment.getEndDateTime(), dtf);

        LocalDate startDate = startDT.toLocalDate();
        LocalTime startTime = startDT.toLocalTime();
        LocalDate endDate = endDT.toLocalDate();
        LocalTime endTime = endDT.toLocalTime();

        updateStartDate.setValue(startDate);
        updateStartTime.setText(String.valueOf(startTime));
        updateEndDate.setValue(endDate);
        updateEndTime.setText(String.valueOf(endTime));
    }

    /** Verifies fields are complete, times are within business hours, and updates the database. */
    public void saveHandler(ActionEvent actionEvent) throws SQLException, IOException {
        // Clears the UI message
        dynamicMsg.setText("");

        // Gets user input
        String apptId = appointmentId.getText();
        String title = updateTitle.getText();
        String description = updateDesc.getText();
        String location = updateLocation.getText();
        String contact = updateContact.getValue().toString();
        String type = updateType.getText();
        String custId = updateCustomerId.getText();
        LocalDate startDate = updateStartDate.getValue();
        String startTimeString = updateStartTime.getText();
        LocalDate endDate = updateEndDate.getValue();
        String endTimeString = updateEndTime.getText();

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
            dynamicMsg.setText("Overlapping appointments scheduled. Pick a different time.");
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

            // Prepares input for database and sends an update query
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

                // Updates the database with the entered values
                DBQuery.sendUpdate("UPDATE appointments " +
                        "SET Title = \"" + title + "\", " +
                        "Description = \"" + description + "\", " +
                        "Location = \"" + location + "\", " +
                        "Type = \"" + type + "\", " +
                        "Start = \"" + startLocalUtc + "\", " +
                        "End = \"" + endLocalUtc + "\", " +
                        "Customer_ID = " + custId + ", " +
                        "Contact_ID = " + contactId + " " +
                        "WHERE Appointment_ID = " + apptId + ";");

                // Informs user that the update was successful and returns to the Appointments screen.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Appointment updated!");
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
    public void exitHandler(ActionEvent actionEvent) {
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
