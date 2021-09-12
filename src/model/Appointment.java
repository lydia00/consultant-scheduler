package model;

import java.time.LocalDateTime;

/** Class to model an appointment. */
public class Appointment {
    private int apptId;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String startDateTime;
    private String endDateTime;
    private int custId;
    private int userId;

    /** Constructor for an appointment. **/
    public Appointment(int apptId, String title, String description, String location, String contact, String type, String startDateTime, String endDateTime, int custId, int userId) {
        this.apptId = apptId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.type = type;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.custId = custId;
    }

    /** Gets the appointment ID. */
    public int getApptId () {return apptId;}

    /** Gets the appointment title. */
    public String getTitle() {
        return title;
    }

    /** Gets the appointment description. */
    public String getDescription() {
        return description;
    }

    /** Gets the appointment location. */
    public String getLocation() {
        return location;
    }

    /** Gets the appointment contact. */
    public String getContact() {
        return contact;
    }

    /** Gets the appointment type. */
    public String getType() {
        return type;
    }

    /** Gets the appointment start timestamp. */
    public String getStartDateTime() {
        return startDateTime;
    }

    /** Gets the appointment end timestamp. */
    public String getEndDateTime() {
        return endDateTime;
    }

    /** Gets the appointment customer ID. */
    public int getCustId() {
        return custId;
    }

    /** Function to convert a timestamp to a zoned time that calls a lambda. */
    public interface dateTimeLambda {
        LocalDateTime localDateTimeConverter(String dateTime);
    }
}
