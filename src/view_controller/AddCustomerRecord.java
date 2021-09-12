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
import java.util.ResourceBundle;

/** Controller for the Add Customer Record form. */
public class AddCustomerRecord implements Initializable {
    public TextField addCustId;
    public TextField addName;
    public TextField addAddress;
    public TextField addPostCode;
    public ComboBox addCountry;
    public ComboBox addDivision;
    public TextField addPhone;
    public Button addNewRecordButton;
    public Button backToRecordsButton;
    public Button exitButton;
    public Label dynamicMsg;
    int divisionId;

    /** Initializes the Add Customer Record screen and populates the Country combo box. */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addCountry.setItems(listCountries());
    }

    /** Handles the Country combo box and updates Division list when a new country is selected. */
    public void addCountryHandler(ActionEvent actionEvent) {
        addCountry.getSelectionModel().getSelectedItem();
        addDivisionHandler(actionEvent);

    }

    /** Handles the Division combo box, dependent on Country selection. */
    public void addDivisionHandler(ActionEvent actionEvent) {
        addDivision.setItems(listDivisions());
    }

    /** Verifies fields are complete and adds the new record to the database. */
    public void SaveNewRecordHandler(ActionEvent actionEvent) throws SQLException, IOException {
        // Clears the UI message
        dynamicMsg.setText("");

        // Gets user input
        String name = addName.getText();
        String address = addAddress.getText();
        String postCode = addPostCode.getText();
        String phone = addPhone.getText();
        String country = addCountry.getValue().toString();
        String division = addDivision.getValue().toString();

        // Verifies that the form is complete. Displays a message on the screen if it isn't
        if (name.isEmpty() || address.isEmpty() || postCode.isEmpty() || phone.isEmpty() || country.isEmpty() || division.isEmpty()) {
            dynamicMsg.setText("Please complete all fields.");
        }

        // Updates the database.
        else {
            // Gets the division ID for the selected division
            DBQuery.sendQuery("SELECT Division_ID FROM first_level_divisions WHERE Division = \"" + division + "\";");
            ResultSet divResultSet = DBQuery.getQueryResult();
            while (divResultSet.next()) {
                try {
                    this.divisionId = divResultSet.getInt("Division_ID");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            // Sends the query to insert the new customer
            DBQuery.sendUpdate("INSERT INTO customers " +
                    "(Customer_Name, Address, Postal_Code, Phone, Division_ID)" +
                    "VALUES ( \"" + name + "\", \"" + address + "\", \"" + postCode + "\", \"" + phone + "\", " + divisionId + ");");

            // Informs user that the update was successful and returns to the Customer Records screen.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Record added!");
            alert.setContentText("New customer record successfully added to database.");
            alert.showAndWait();
            toRecords(actionEvent);
        }

    }

    /** Sends the user back to the Customer Records screen. */
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
            String selectedCountry = addCountry.getSelectionModel().getSelectedItem().toString();
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
