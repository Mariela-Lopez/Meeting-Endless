
package Model;
import Utils.DBconnection;
import Utils.DBquery;
import java.sql.SQLException;
import java.sql.Statement;

public class Appointment {

    private int apptId;
    private String title;
    private String description;
    private String location;
    private int contactId;
    private String contactName;
    private String type;
    private String apptStart;
    private String apptEnd;
    private int userId;
    private int customerId;

    /**
     * Constructor
     * @param title
     * @param description
     * @param location
     * @param contactId
     * @param type
     * @param apptStart
     * @param apptEnd
     * @param userId
     * @param customerId
     */
    public Appointment(String title, String description, String location, int contactId, String type, String apptStart, String apptEnd, int userId, int customerId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.contactId = contactId;
        this.type = type;
        this.apptStart = apptStart;
        this.apptEnd = apptEnd;
        this.userId = userId;
        this.customerId = customerId;

    }

    public Appointment() {
    }

    /**
     * method for Insertion into appointments table.
     */
    public void insertAppointmentToTable() {
        try {
            DBquery.setStatement(DBconnection.getConnection());
            Statement statement = DBquery.getStatement();
            String insertStatement = String.format("INSERT INTO appointments( Title, Description, Location, Type, Start, End, Created_By, Customer_ID, User_ID, Contact_ID) VALUES('%s', '%s', '%s', '%s', '%s', '%s', 'admin', '%d', '%d', %d)",
                    title, description, location, type, apptStart, apptEnd, customerId, userId, contactId);
            statement.execute(insertStatement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * method for passing appointment into insertAppointmentToTable method.
     * @param appointment
     */
    public void addAppointment (Appointment appointment) {
        appointment.insertAppointmentToTable();
    }

    /**
     * getter for appointment ID
     * @return
     */
    public int getApptId() {
        return apptId;
    }

    /**
     * setter for appointment ID
     * @param apptId
     */
    public void setApptId(int apptId) {
        this.apptId = apptId;
    }

    /**
     * getter for title
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * setter for title.
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * getter for description.
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * setter for description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * gets location.
     * @return
     */
    public String getLocation() {
        return location;
    }

    /**
     * setter for location.
     * @param location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * getter for types.
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * setter for types.
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * getter for appointment start dates and times.
     * @return
     */
    public String getApptStart() {
        return apptStart;
    }

    /**
     * setter for appointment start dates and times.
     * @param apptStart
     */
    public void setApptStart(String apptStart) {
        this.apptStart = apptStart;
    }

    /**
     * getter for appointment end dates and times.
     * @return
     */
    public String getApptEnd() {
        return apptEnd;
    }

    /**
     * setter for appointment end dates and times.
     * @param apptEnd
     */
    public void setApptEnd(String apptEnd) {
        this.apptEnd = apptEnd;
    }

    /**
     * getter for User ID.
     * @return
     */
    public int getUserId() {
        return userId;
    }

    /**
     * setter for user ID.
     * @param userId
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * getter for customer ID.
     * @return
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * setter for customer ID.
     * @param customerId
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * getter for contact name.
     * @return
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * setter for contact name.
     * @param contactName
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
