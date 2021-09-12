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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import util.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/** Controller for the Appointments screen. **/
public class Appointments implements Initializable {
    public ToggleGroup durationToggleGroup;
    public RadioButton allToggle;
    public RadioButton weekToggle;
    public RadioButton monthToggle;
    public TableView apptsTable;
    public TableColumn apptsIdCol;
    public TableColumn apptsTitleCol;
    public TableColumn apptsDescCol;
    public TableColumn apptsLocationCol;
    public TableColumn apptsContactCol;
    public TableColumn apptsTypeCol;
    public TableColumn apptsStartCol;
    public TableColumn apptsEndCol;
    public TableColumn apptsCustIdCol;
    public Button addApptButton;
    public Button updateApptButton;
    public Button deleteApptButton;
    public Button exitButton;
    public Button goToCustRecordsButton;
    public Label apptsMsg;

    private static ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private static Appointment selectedAppointment;
    private static Boolean showAll;
    private static Boolean showWeek;
    private static Boolean showMonth;

    /** Initializes the Appointments screen and populates the table. Displays all appointments by default. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Establishes the toggle group. Displays all appointments on initialization.
        allToggle.setToggleGroup(durationToggleGroup);
        weekToggle.setToggleGroup(durationToggleGroup);
        monthToggle.setToggleGroup(durationToggleGroup);

        allToggle.setSelected(true);
        showAll = true;
        showMonth = false;
        showWeek = false;

        apptsMsg.setText("");

        try {
            getAllAppointments();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    /** Displays all appointments when toggled. */
    public void displayAllHandler(ActionEvent actionEvent) throws SQLException {
        showAll = true;
        showMonth = false;
        showWeek = false;
        apptsMsg.setText("");
        getAllAppointments();
    }

    /** Displays this week's appointments when toggled. */
    public void displayWeekHandler(ActionEvent actionEvent) throws SQLException {
        showAll = false;
        showMonth = false;
        showWeek = true;
        apptsMsg.setText("");
        getAppointmentsByWeek();
    }

    /** Displays this month's appointments when toggled. */
    public void displayMonthHandler(ActionEvent actionEvent) throws SQLException {
        showAll = false;
        showMonth = true;
        showWeek = false;
        apptsMsg.setText("");
        getAppointmentsByMonth();
    }

    /** Opens the Add Appointment screen. */
    public void addApptHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/AddAppointment.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Add Appointment");
        stage.setScene(scene);
        stage.show();
    }

    /** Opens the Update Appointment screen with data from the selected appointment. */
    public void updateApptHandler(ActionEvent actionEvent) throws IOException {
        selectedAppointment = (Appointment) apptsTable.getSelectionModel().getSelectedItem();
        apptsMsg.setText("");

        // Checks if an appointment is selected, if not, displays a message.
        if (selectedAppointment == null) {
            apptsMsg.setText("Appointment not selected!");
        }

        // Opens the Update Appointment window.
        else {
            Parent root = FXMLLoader.load(getClass().getResource("/view_controller/UpdateAppointment.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Update Appointment");
            stage.setScene(scene);
            stage.show();
        }
    }

    /** Cancels the selected appointment after confirming with the user. Displays a custom cancellation message.
     * Calls a lambda that, on confirmation, runs a query to delete the appointment from the database and displays a custom message.
     * This lambda condensed the code for a complex set of results when the user cancels an appointment. */
    public void cancelApptHandler(ActionEvent actionEvent) throws SQLException {
        selectedAppointment = (Appointment) apptsTable.getSelectionModel().getSelectedItem();
        apptsMsg.setText("");

        // Checks if an appointment is selected, if not, displays a message.
        if (selectedAppointment == null) {
            apptsMsg.setText("Appointment not selected!");
        } else {
            // Confirms cancellation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Cancellation");
            alert.setHeaderText("Cancel appointment?");
            alert.setContentText("Click OK if you want to cancel this appointment.");

            // Lambda that, on confirmation, runs a query to delete the appointment from the database and displays a custom message.
            alert.showAndWait().ifPresent((userResponse -> {
                if (userResponse == ButtonType.OK) {
                    int deletedApptId = selectedAppointment.getApptId();
                    String deletedApptType = selectedAppointment.getType();
                    DBQuery.sendUpdate("DELETE FROM appointments " +
                            "WHERE Appointment_ID = " + deletedApptId + ";");
                    // Refreshes the appointments list based on the selected view
                    try {
                        if (showMonth) {
                            getAppointmentsByMonth();
                        } else if (showWeek) {
                            getAppointmentsByWeek();
                        } else {
                            getAllAppointments();
                        }
                        apptsMsg.setText("Cancelled Appointment " + deletedApptId + ": " + deletedApptType);
                    }
                    catch (SQLException e){
                        System.out.println(e.getMessage());
                    }
                } else {
                    // Clears message if user decides not to cancel
                    apptsMsg.setText("");
                }
            }));
        }
    }

    /** Gets the selected appointment. */
    public static Appointment getSelectedAppointment() { return selectedAppointment; }

    /** Function to get all appointments and display them in the Appointments table. */
    public void getAllAppointments() throws SQLException {
       // Clears the list to prevent duplication
       appointmentList.removeAll(appointmentList);

       // Queries the appointments table, gets all appointments, converts to local time
       DBQuery.sendQuery("SELECT * FROM appointments;");
       ResultSet resultSet = DBQuery.getQueryResult();
       populateAppts(resultSet);

        // Populates the table with the appointments list.
        apptsTable.setItems(appointmentList);
        apptsIdCol.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        apptsTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        apptsDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        apptsLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        apptsContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        apptsTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        apptsStartCol.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        apptsEndCol.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        apptsCustIdCol.setCellValueFactory(new PropertyValueFactory<>("custId"));
    }

    /** Function to get appointments for the next week and display them in the Appointments table. */
    public void getAppointmentsByWeek() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime weekOut = localDateTime.plusDays(7);

        // Clears appointments list to prevent duplication.
        appointmentList.removeAll(appointmentList);

        // Queries the appointments table for all appointments that are one month out.
        DBQuery.sendQuery("SELECT * FROM appointments " +
                "WHERE Start >= \"" + localDateTime + "\" AND End <= \"" + weekOut + "\";");
        ResultSet resultSet = DBQuery.getQueryResult();
        populateAppts(resultSet);

        // Displays a message if there are no appointments this week
        if (appointmentList.isEmpty()) {
            apptsMsg.setText("No appointments to display for this week.");
        }
    }

    /** Function to get appointments for the next month and display them in the Appointments table. */
    public void getAppointmentsByMonth() throws SQLException {
        // Gets the current month and year
        LocalDateTime localDateTime = LocalDateTime.now();
        int localMonth = localDateTime.getMonthValue();
        int localYear = localDateTime.getYear();

        // Clears appointments list to prevent duplication.
        appointmentList.removeAll(appointmentList);

        // Queries the appointments table for all appointments that match the current month and year.
        DBQuery.sendQuery("SELECT * FROM appointments " +
                "WHERE MONTH(Start) = \"" + localMonth + "\" AND YEAR(Start) = \"" + localYear + "\";");
        ResultSet resultSet = DBQuery.getQueryResult();
        populateAppts(resultSet);

        // Displays a message if there are no appointments this week
        if (appointmentList.isEmpty()) {
            apptsMsg.setText("No appointments to display for this month.");
        }
    }

    // Lambda expression that converts a time string to a LocalDateTime object and then converts it from UTC to the user's local time.
    Appointment.dateTimeLambda convertToZonedTime = (String dateTime) -> {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dateTimeFormat).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime;
    };

    /** Function to read appointments from a query result set, convert to local time, and display the appointments in the Appointments table.
     * This method uses a lambda expression that converts a time string to a LocalDateTime object and then converts it from UTC to the user's local time.
     * The lambda simplified the code for time zone conversions, making it more readable. */
    public void populateAppts(ResultSet resultSet) {
        try {
            // Creates a new Appointment for each table row, converts datetime fields to local time.
            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                String location = resultSet.getString("Location");
                int contactId = resultSet.getInt("Contact_ID");
                String type = resultSet.getString("Type");
                String startDateTime = String.valueOf(convertToZonedTime.localDateTimeConverter(resultSet.getString("Start")));
                String endDateTime = String.valueOf(convertToZonedTime.localDateTimeConverter(resultSet.getString("End")));
                int customerId = resultSet.getInt("Customer_ID");
                int userId = resultSet.getInt("User_ID");

                // Query contacts, get the contact name for each appointment
                DBQuery.sendQuery("SELECT Contact_Name, Contact_ID " +
                        "FROM contacts " +
                        "WHERE Contact_ID = " + contactId + ";");
                ResultSet contactResultSet = DBQuery.getQueryResult();
                try {
                    while (contactResultSet.next()) {
                        String contact = contactResultSet.getString("Contact_Name");
                        Appointment appointment = new Appointment(appointmentId, title, description, location, contact, type, startDateTime, endDateTime, customerId, userId);
                        appointmentList.add(appointment);
                    }
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        // Populates the table with the appointments list.
        apptsTable.setItems(appointmentList);
    }

    /** Opens the Customer Records screen. */
    public void goToCustRecordsHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/CustomerRecords.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Customer Records");
        stage.setScene(scene);
        stage.show();
    }

    /** Opens the Generate Reports screen. */
    public void generateReportsHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/GenerateReports.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Generate Reports");
        stage.setScene(scene);
        stage.show();
    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
