package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import util.DBQuery;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Controller for the Generate Reports screen. **/
public class GenerateReports {
    public TextArea reportField;
    public Label reportsMsg;

    /** Reports the count for each appointment type per month. **/
    public void reportAppointmentsHandler(ActionEvent actionEvent) throws SQLException {
        StringBuilder buildReport = new StringBuilder();
        reportField.setText("");

        // Query to get the count of each appointment type per month
        DBQuery.sendQuery("SELECT MONTH(Start) AS \"Month\", Type, COUNT(Type) AS \"Count\" " +
                "FROM appointments " +
                "GROUP BY MONTH(Start), Type;");
        ResultSet resultSet = DBQuery.getQueryResult();
        while (resultSet.next()) {
            int month = resultSet.getInt("Month");
            String type = resultSet.getString("Type");
            int count = resultSet.getInt("Count");
            buildReport.append("Month: " + month + " | Type: " + type + " | Count: " + count + "\n");
        }

        // Displays the report in the text area.
        reportField.setText(buildReport.toString());
    }

    /** Reports the scheduled appointments for each contact. **/
    public void reportContactSchedulesHandler(ActionEvent actionEvent) throws SQLException {
        StringBuilder buildReport = new StringBuilder();
        reportField.setText("");

        // Query to get appointments and order them by contact ID
        DBQuery.sendQuery("SELECT Appointment_ID, Title, Description, Type, Start, End, Customer_ID, Contact_ID " +
                "FROM appointments " +
                "ORDER BY Contact_ID;");
        ResultSet resultSet = DBQuery.getQueryResult();
        while (resultSet.next()) {
            int contactId = resultSet.getInt("Contact_ID");
            int appointmentId = resultSet.getInt("Appointment_ID");
            String title = resultSet.getString("Title");
            String description = resultSet.getString("Description");
            String type = resultSet.getString("Type");
            String start = resultSet.getString("Start");
            String end = resultSet.getString("End");
            int customerId = resultSet.getInt("Customer_ID");
            buildReport.append("Contact " + contactId + " | Appt ID: " + appointmentId + " | Title: " + title + " | Desc: " + description + " | Start: " + start + " | End: " + end + " | Cust ID: " + customerId + "\n\n ");
        }

        // Displays the report in the text area.
        reportField.setText(buildReport.toString());
    }

    /** Reports the number of customers in each country. **/
    public void reportCustomersHandler(ActionEvent actionEvent) throws SQLException {
        StringBuilder buildReport = new StringBuilder();
        reportField.setText("");
        DBQuery.sendQuery("SELECT COUNT(c.Customer_ID) AS \"Count\", y.Country " +
                "FROM customers AS c " +
                "JOIN first_level_divisions AS d ON c.Division_ID = d.Division_ID " +
                "JOIN countries AS y ON d.COUNTRY_ID = y.Country_ID " +
                "GROUP BY y.Country;");
        ResultSet resultSet = DBQuery.getQueryResult();
        while (resultSet.next()) {
            int customerCount = resultSet.getInt("Count");
            String country = resultSet.getString("Country");
            buildReport.append(country + " | Customers: " + customerCount + "\n");
        }

        // Displays the report in the text area.
        reportField.setText(buildReport.toString());
    }

    /** Opens the Appointments screen. */
    public void goToApptsHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/Appointments.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Appointments");
        stage.setScene(scene);
        stage.show();
    }

    /** Opens the Customer Records screen. */
    public void goToCustRecordsHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/CustomerRecords.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Customer Records");
        stage.setScene(scene);
        stage.show();
    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
