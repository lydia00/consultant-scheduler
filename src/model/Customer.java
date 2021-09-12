package model;

/** Class to model a customer. */
public class Customer {
    private int custId;
    private String custName;
    private String custAddress;
    private String custPostCode;
    private String custPhone;
    private String custDiv;
    private String custCountry;

    /** Constructor for a customer */
    public Customer(int custId, String custName, String custAddress, String custPostCode, String custPhone, int custDivId, String custDiv, int custCountryId, String custCountry) {
        this.custId = custId;
        this.custName = custName;
        this.custAddress = custAddress;
        this.custPostCode = custPostCode;
        this.custPhone = custPhone;
        this.custDiv = custDiv;
        this.custCountry = custCountry;
    }

    /** Gets the customer ID. */
    public int getCustId () {
        return custId;
    }

    /** Gets the customer name. */
    public String getCustName() {
        return custName;
    }

    /** Gets the customer address. */
    public String getCustAddress() {
        return custAddress;
    }

    /** Gets the customer postal code. */
    public String getCustPostCode() {
        return custPostCode;
    }

    /** Gets the customer phone number. */
    public String getCustPhone() {
        return custPhone;
    }

    /** Gets the customer division. */
    public String getCustDiv() { return custDiv; }

    /** Gets the customer country. */
    public String getCustCountry() {
        return custCountry;
    }
}
