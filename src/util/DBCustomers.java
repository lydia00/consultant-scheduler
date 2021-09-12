package util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Customer;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Utility class to query the customers table in the database. */
public class DBCustomers {
    private static ObservableList<Customer> customerList = FXCollections.observableArrayList();

    /** Gets all customer data from the database. */
    public static ObservableList<Customer> getCustomerList() throws SQLException {
        try {
            // Clears the list to prevent duplication
            customerList.removeAll(customerList);

            // Queries the customers table, get all customers
            DBQuery.sendQuery("SELECT * FROM customers");
            ResultSet resultSet = DBQuery.getQueryResult();
            while (resultSet.next()) {
                int custId = resultSet.getInt("Customer_ID");
                String custName = resultSet.getString("Customer_Name");
                String custAddress = resultSet.getString("Address");
                String custPostCode = resultSet.getString("Postal_Code");
                String custPhone = resultSet.getString("Phone");
                int custDivId = resultSet.getInt("Division_ID");

                // Query first_level_divisions, get the division listed for each customer
                try {
                    DBQuery.sendQuery("select Division, Country_ID " +
                            "from first_level_divisions " +
                            "where Division_ID = " + custDivId + ";");
                    ResultSet divResultSet = DBQuery.getQueryResult();
                    while (divResultSet.next()) {
                        String custDiv = divResultSet.getString("Division");
                        int custCountryId = divResultSet.getInt("Country_ID");

                        // Query countries, get the country listed for each division
                        try {
                            DBQuery.sendQuery("select Country from countries where Country_ID = " + custCountryId + ";");
                            ResultSet countryResultSet = DBQuery.getQueryResult();
                            while (countryResultSet.next()) {
                                String custCountry = countryResultSet.getString("Country");
                                Customer customer = new Customer(custId, custName, custAddress, custPostCode, custPhone, custDivId, custDiv, custCountryId, custCountry);
                                customerList.add(customer);
                            }
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return customerList;
    }
}
