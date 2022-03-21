package Model;
import Utils.DBconnection;
import Utils.DBquery;
import java.sql.SQLException;
import java.sql.Statement;

public class Customer {
    private long id;
    private String name;
    private String address;
    private String postalCode;
    private String phoneNumber;
    private int divisionId;
    private String divisionName;
    private String countryName;

    /**
     * constructor.
     * @param name
     * @param address
     * @param postalCode
     * @param phoneNumber
     * @param divisionId
     */
    public Customer(String name, String address, String postalCode, String phoneNumber, int divisionId) {
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.divisionId = divisionId;
    }

    /**
     * default constructor.
     */
    public Customer() {
    }

    /**
     * method to insert into customer table in database.
     */
    public void insertCustomerToTable() {
        try {
            DBquery.setStatement(DBconnection.getConnection());
            Statement statement = DBquery.getStatement();
            String insertStatement = String.format("INSERT INTO customers(Customer_Name, Address, Postal_Code, Phone, Created_By, Last_Updated_By, Division_ID) VALUES('%s', '%s', '%s', '%s', 'admin', 'admin', %d)",
                     name, address, postalCode, phoneNumber, divisionId);
            statement.execute(insertStatement);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * method to pass in customer to insertCustomerToTable();
     * @param customer
     */
    public void addCustomer(Customer customer) {
        customer.insertCustomerToTable();
    }

    /**
     * getter for name.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter for address.
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * setter for address.
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * getter for postal code.
     * @return
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * setter for postal code.
     * @param postalCode
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * getter for phone number.
     * @return
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * setter for phone number.
     * @param phoneNumber
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * setter for customer ID.
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * getter for Customer id.
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * getter for division name.
     * @return
     */
    public String getDivisionName() {
        return divisionName;
    }

    /**
     * setter for division name.
     * @param divisionName
     */
    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    /**
     * getter for country name.
     * @return
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * setter for country name.
     * @param countryName
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
