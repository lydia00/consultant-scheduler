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
import model.Customer;
import util.DBQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/** Controller for the Update Customer Record screen. */
public class UpdateCustomerRecord implements Initializable {
    public TextField customerId;
    public TextField updateName;
    public TextField updateAddress;
    public TextField updatePostCode;
    public ComboBox updateCountry;
    public ComboBox updateDivision;
    public TextField updatePhone;
    public Label dynamicMsg;
    public int divisionId;

    /** Initializes the Update Customer Record form and populates it with data from the selected customer. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populates the form with selected customer data
        Customer selectedCustomer = CustomerRecords.getSelectedCustomer();
        customerId.setText(String.valueOf(selectedCustomer.getCustId()));
        updateName.setText(String.valueOf(selectedCustomer.getCustName()));
        updateAddress.setText(String.valueOf(selectedCustomer.getCustAddress()));
        updatePostCode.setText(String.valueOf(selectedCustomer.getCustPostCode()));
        updateCountry.setValue(selectedCustomer.getCustCountry());
        updateDivision.setValue(selectedCustomer.getCustDiv());
        updatePhone.setText(String.valueOf(selectedCustomer.getCustPhone()));

        // Populates combo boxes
        updateCountry.setItems(listCountries());
        updateDivision.setItems(listDivisions());
    }

    /** Handles Country combo box and updates Division list when a new country is selected. */
    public void updateCountryHandler(ActionEvent actionEvent) {
        updateCountry.getSelectionModel().getSelectedItem();
        updateDivisionHandler(actionEvent);
    }

    /** Handles Division combo box, dependent on Country selection. */
    public void updateDivisionHandler(ActionEvent actionEvent) {
        updateDivision.setItems(listDivisions());
    }

    /** Verifies fields are complete and updates the database. */
    public void saveHandler(ActionEvent actionEvent) throws SQLException, IOException {
        // Clears the UI message
        dynamicMsg.setText("");

        // Gets user input
        String custId = customerId.getText();
        String name = updateName.getText();
        String address = updateAddress.getText();
        String postCode = updatePostCode.getText();
        String phone = updatePhone.getText();
        String country = updateCountry.getValue().toString();
        String division = updateDivision.getValue().toString();

        // Verifies that the form is complete. Displays a message on the screen if it isn't
        if (name.isEmpty() || address.isEmpty() || postCode.isEmpty() || phone.isEmpty() || country.isEmpty() || division.isEmpty()) {
            dynamicMsg.setText("Please complete all fields.");
        }

        // Updates the database
        else {
            // Gets the division ID for the selected division
            DBQuery.sendQuery("SELECT Division_ID FROM first_level_divisions WHERE Division = \"" + division + "\";");
            ResultSet divResultSet = DBQuery.getQueryResult();
            while (divResultSet.next()) {
                try {
                    this.divisionId = divResultSet.getInt("Division_ID");
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }

                // Sends the update query.
                DBQuery.sendUpdate("UPDATE customers " +
                        "SET Customer_Name = \"" + name + "\", " +
                        "Address = \"" + address + "\", " +
                        "Postal_Code = \"" + postCode + "\", " +
                        "Phone = \"" + phone + "\", " +
                        "Division_ID = \"" + divisionId + "\"" +
                        "WHERE Customer_ID = " + custId + ";");

                // Informs user that the update was successful and returns to the Customer Records screen.
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Record updated!");
                alert.setContentText("Customer record successfully saved to database.");
                alert.showAndWait();
                toRecords(actionEvent);
            }
        }
    }

    /** Button that sends the user back to the Customer Records screen. */
    public void backToRecordsHandler(ActionEvent actionEvent) throws IOException {
        toRecords(actionEvent);
    }

    /** Exits the application. */
    public void exitHandler(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    /** Function to send the user back to the Customer Records screen. */
    public void toRecords(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view_controller/CustomerRecords.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setTitle("Customer Records");
        stage.setScene(scene);
        stage.show();
    }

    /** Function to populate the Country combo box.*/
    public ObservableList listCountries() {
        ObservableList<String> countries = FXCollections.observableArrayList();

        try {
            // Clears list to prevent duplication.
            countries.removeAll(countries);

            // Gets country names from the database
            DBQuery.sendQuery("SELECT Country FROM countries");
            ResultSet resultSet = DBQuery.getQueryResult();
            while (resultSet.next()) {
                countries.add(resultSet.getString("Country"));
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return countries;
    }

    /** Function to populate the Division combo box based on the selected country. */
    public ObservableList listDivisions() {
        ObservableList<String> divisions = FXCollections.observableArrayList();

        try {
            // Clears list to prevent duplication.
            divisions.removeAll(divisions);

            // Gets the country ID for the selected country.
            String selectedCountry = updateCountry.getSelectionModel().getSelectedItem().toString();
            DBQuery.sendQuery("SELECT Country_ID FROM countries WHERE Country = \"" + selectedCountry + "\";");
            ResultSet countryResultSet = DBQuery.getQueryResult();
            while (countryResultSet.next()) {
                try {
                    int countryId = countryResultSet.getInt("Country_ID");

                    // Gets divisions that match the country ID for the selected country
                    DBQuery.sendQuery("SELECT Division FROM first_level_divisions WHERE COUNTRY_ID = \"" + countryId + "\";");
                    ResultSet divResultSet = DBQuery.getQueryResult();
                    while (divResultSet.next()) {
                        divisions.add(divResultSet.getString("Division"));
                    }
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return divisions;
    }
}