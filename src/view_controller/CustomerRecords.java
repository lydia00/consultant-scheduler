package view_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Customer;
import util.DBCustomers;
import util.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/** Controller for the Customer Records screen. */
public class CustomerRecords implements Initializable {
    public TableView customerRecordsTable;
    public TableColumn customerRecordsIdCol;
    public TableColumn customerRecordsNameCol;
    public TableColumn customerRecordsAddressCol;
    public TableColumn customerRecordsPostCodeCol;
    public TableColumn customerRecordsDivCol;
    public TableColumn customerRecordsPhoneCol;
    public Label customerRecordsMsg;
    public Button addRecordButton;
    public Button updateRecordButton;
    public Button deleteRecordButton;
    public Button exitButton;
    public Button goToApptsButton;

    private static Customer selectedCustomer;

    /** Initializes the Customer Records screen and populates the table. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            displayCustomerRecords();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Opens the Add Customer Record screen. */
    public void addRecordHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/AddCustomerRecord.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Add Customer Record");
        stage.setScene(scene);
        stage.show();
    }

    /** Opens the Update Customer Record screen with data from the selected customer. */
    public void updateRecordHandler(ActionEvent actionEvent) throws IOException {
        selectedCustomer = (Customer) customerRecordsTable.getSelectionModel().getSelectedItem();

        // Checks if a customer is selected, if not, displays a message.
        if (selectedCustomer == null) {
            customerRecordsMsg.setText("Customer not selected!");
        }

        // Opens the Update Customer Record window.
        else {
            Parent root = FXMLLoader.load(getClass().getResource("/view_controller/UpdateCustomerRecord.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("Update Customer Record");
            stage.setScene(scene);
            stage.show();
        }
    }

    /** Gets the selected customer. */
    public static Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    /** Deletes the selected customer. */
    public void deleteRecordHandler(ActionEvent actionEvent) throws SQLException {
        selectedCustomer = (Customer) customerRecordsTable.getSelectionModel().getSelectedItem();

        // Checks if a customer is selected, if not, displays a message.
        if (selectedCustomer == null) {
            customerRecordsMsg.setText("Customer not selected!");
        }

        // Checks if selected customer has an associated appointment. Displays an alert and prevents deletion if so.
        else if (customerHasAppointments(selectedCustomer)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Deletion Attempt");
            alert.setHeaderText("Cannot Delete Customer");
            alert.setContentText("This customer has associated appointments and cannot be deleted.");
            alert.showAndWait();
        }

        else {
            // Confirms deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete customer record?");
            alert.setContentText("Are you sure you want to delete this customer record?");
            Optional<ButtonType> result = alert.showAndWait();

            // Deletes the customer and all associated appointments from the database and refreshes the customer records table.
            if (result.get() == ButtonType.OK){
                int deleteCustomerId = selectedCustomer.getCustId();
                DBQuery.sendUpdate("DELETE FROM customers " +
                        "WHERE Customer_ID = \"" + deleteCustomerId + "\";");
                displayCustomerRecords();
                customerRecordsMsg.setText("Customer record deleted.");
            }
            else {
                // Clears message if user decides not to delete
                customerRecordsMsg.setText("");
            }
        }
    }

    /** Function to check if the selected customer has associated appointments. */
    public boolean customerHasAppointments(Customer selectedCustomer) throws SQLException {
        int selectedCustomerId = selectedCustomer.getCustId();
        boolean hasAppt = false;
        DBQuery.sendQuery("SELECT Customer_ID FROM appointments;");
        ResultSet resultSet = DBQuery.getQueryResult();
        while (resultSet.next()) {
            int appointmentCustId = resultSet.getInt("Customer_ID");
            if (selectedCustomerId == appointmentCustId) {
                hasAppt = true;
                break;
            }
            else { hasAppt = false; }
        }
        return hasAppt;
    }

    /** Function to get all customer records and display them in the Customer Records table. */
    public void displayCustomerRecords() throws SQLException {
        customerRecordsTable.setItems(DBCustomers.getCustomerList());
        customerRecordsIdCol.setCellValueFactory(new PropertyValueFactory<>("custId"));
        customerRecordsNameCol.setCellValueFactory(new PropertyValueFactory<>("custName"));
        customerRecordsAddressCol.setCellValueFactory(new PropertyValueFactory<>("custAddress"));
        customerRecordsPostCodeCol.setCellValueFactory(new PropertyValueFactory<>("custPostCode"));
        customerRecordsPhoneCol.setCellValueFactory(new PropertyValueFactory<>("custPhone"));
        customerRecordsDivCol.setCellValueFactory(new PropertyValueFactory<>("custDiv"));
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

    /** Opens the Appointments screen. */
    public void goToApptsHandler(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/Appointments.fxml"));
        Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Appointments");
        stage.setScene(scene);
        stage.show();
    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
