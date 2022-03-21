package Controller.Records;


import Model.Customer;
import Utils.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class CustomersMenu implements Initializable {

    @FXML
    public static Customer selectedCustomer;

    /**
     * tableview for customer records.
     */
    @FXML
    private TableView<Customer> customersTableView;

    /**
     * customer id tableview column.
     */
    @FXML
    private TableColumn<Customer, Integer> customerId;

    /**
     * customer name tableview column.
     */
    @FXML
    private TableColumn<Customer, String> customerName;

    /**
     * customer address tableview column.
     */
    @FXML
    private TableColumn<Customer, String> customerAddress;

    /**
     * customer division tableview column.
     */
    @FXML
    private TableColumn<Customer, String> customerState;

    /**
     * customer customer zip tableview column.
     */
    @FXML
    private TableColumn<Customer, String> customerZip;

    /**
     * customer phone number tableview column.
     */
    @FXML
    private TableColumn<Customer, String> customerPhoneNumber;

    /**
     * observable list that stores all the customers records from the database.
     */
    @FXML
    private ObservableList<Customer> customerData = FXCollections.observableArrayList();

    /**
     * directs the user to the new customer screen when button is pressed.
     * @param event
     * @throws IOException
     */
    @FXML
    void onNewClick(ActionEvent event) throws IOException {
        Parent add_product = FXMLLoader.load(getClass().getResource("../../Views/Records/NewCustomer.fxml"));
        add_product.setStyle("-fx-font-family: sans-serif");
        Scene add_product_scene = new Scene(add_product);
        Stage add_product_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_product_stage.setTitle("New Customer");
        add_product_stage.setScene(add_product_scene);
        add_product_stage.show();
    }

    /**
     * method that stores the value of the selected customer and sends information to the update customer screen.
     * @param event - directs the customer to the update cusomer screen.
     * @throws IOException
     */
    @FXML
    void onUpdateClick(ActionEvent event) throws IOException {
        selectedCustomer = customersTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No Customer was selected!");
            alert.showAndWait();
        } else {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
            Parent addPartScreenParent = FXMLLoader.load(getClass().getResource("../../Views/Records/UpdateCustomer.fxml"));
            addPartScreenParent.setStyle("-fx-font-family: sans-serif");
            Scene addPartScreenScene = new Scene(addPartScreenParent);
            Stage stage = new Stage();
            stage.setScene(addPartScreenScene);
            stage.show();
        }
    }

    /**
     * directs the user back to the main screen if the button is pressed.
     * @param event
     * @throws IOException
     */
    @FXML
    void onCancelClick(ActionEvent event) throws IOException {
        Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
        add_part.setStyle("-fx-font-family: sans-serif");
        Scene add_part_scene = new Scene(add_part);
        Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_part_stage.setScene(add_part_scene);
        add_part_stage.show();
    }

    /**
     * gets all customer records from the database and inserts into an observable list.
     */
    @FXML
    public void setCustomersTableView() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT customers.Customer_ID, customers.Customer_Name, customers.Address, customers.Postal_Code, customers.Phone, first_level_divisions.Division, countries.Country " +
                    "FROM customers " +
                    "INNER JOIN first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID " +
                    "INNER JOIN countries ON first_level_divisions.COUNTRY_ID = countries.Country_ID";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            customerData.clear();

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getInt("Customer_ID"));
                customer.setName(rs.getString("Customer_Name"));
                customer.setAddress(rs.getString("Address"));
                customer.setPostalCode(rs.getString("Postal_Code"));
                customer.setPhoneNumber(rs.getString("Phone"));
                customer.setDivisionName(rs.getString("Division"));
                customer.setCountryName(rs.getString("Country"));

                customerData.addAll(customer);
            }
            customersTableView.setItems(customerData);
        } catch (SQLException e) {
            e.getMessage();

        }
    }

    /**
     * initializes all the methods and sets the tableview columns for customers.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        customerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        customerState.setCellValueFactory(new PropertyValueFactory<>("divisionName"));
        customerZip.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        customerPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        setCustomersTableView();

    }
}
