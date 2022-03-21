package Controller.Records;

import Model.Customer;
import Utils.DBconnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class NewCustomer implements Initializable {

    /**
     * customer name textfield.
     */
    @FXML
    private TextField customerNameTextField;

    /**
     * customer Id textfield.
     */
    @FXML
    private TextField customerIdTextField;

    /**
     * address textfield.
     */
    @FXML
    private TextField streetAddressTextField;

    /**
     * zip code text field.
     */
    @FXML
    private TextField zipCodeTextField;

    /**
     * phone number text field.
     */
    @FXML
    private TextField phoneNumberTextField;

    /**
     * country combobox.
     */
    @FXML
    private ComboBox<String> countryComboBox;

    /**
     * state combobox.
     */
    @FXML
    private ComboBox<String> stateComboBox;

    /**
     * redirects user to customers menu if cancel is pressed.
     * @param event
     * @throws IOException
     */
    @FXML
    void onCancel(ActionEvent event) throws IOException {
        Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/Records/CustomersMenu.fxml"));
        add_part.setStyle("-fx-font-family: sans-serif");
        Scene add_part_scene = new Scene(add_part);
        Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_part_stage.setScene(add_part_scene);
        add_part_stage.show();

    }

    /**
     * inserts the record in the database if al the fields are complete.
     * @param event
     * @throws Exception
     */
    @FXML
    void onSaveCustomer(ActionEvent event) throws Exception {
        String name = customerNameTextField.getText();
        String address = streetAddressTextField.getText();
        String zipcode = zipCodeTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        String divisionName = stateComboBox.getValue();


        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Please input Full Name.");
            alert.showAndWait();
        } else if (address.isEmpty() || address.length() < 5) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Please input Address as Street and City.");
            alert.showAndWait();
        } else if (zipcode.isEmpty() || zipcode.length() < 3 || zipcode.length() > 8) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Plase input correct Postal Code.");
            alert.showAndWait();
        } else if (phoneNumber.isEmpty() || phoneNumber.length() < 10 || phoneNumber.length() > 16) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setContentText("Please input Correct Phone number Starting with Area Code.");
            alert.showAndWait();
        } else if(divisionName == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setContentText("Please select Country, state or region");
                alert.showAndWait();
            } else {
            int divisionId = -1;
            Statement statement = DBconnection.getConnection().createStatement();

            String sqlStatement = "SELECT Division_ID FROM first_level_divisions WHERE first_level_divisions.Division ='" + divisionName + "'";

            ResultSet result = statement.executeQuery(sqlStatement);

            while (result.next()) {
                divisionId = result.getInt("Division_ID");
            }

            Customer customer = new Customer(name, address, zipcode, phoneNumber, divisionId);
            customer.addCustomer(customer);
            System.out.println(customer);


            Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/Records/CustomersMenu.fxml"));
            add_part.setStyle("-fx-font-family: sans-serif");
            Scene add_part_scene = new Scene(add_part);

            Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            add_part_stage.setScene(add_part_scene);
            add_part_stage.show();
        }

    }

    /**
     * populates list of countries from database into country combobox.
     */
    private void getCountry() {

        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT Country FROM countries";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                countryComboBox.getItems().add(rs.getString(1));
            }
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();

        }

    }


    /**
     * method that updates the state combobox with lists of states or regions based on country comobobx selection.
     */
    @FXML
    private void getState() {

        String country = countryComboBox.getValue();
        System.out.println(country);

        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT first_level_divisions.Division " +
                    "FROM first_level_divisions, countries " +
                    "WHERE first_level_divisions.COUNTRY_ID = countries.Country_ID " +
                    "AND countries.Country = \"" + country + "\"";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            stateComboBox.getItems().clear();

            while (rs.next()) {
                stateComboBox.getItems().add(rs.getString(1));
            }

            preparedStatement.close();
        } catch (SQLException e) {
            e.getMessage();

        }
    }

    /**
     * initializes the methods
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customerIdTextField.setEditable(false);
        getCountry();
    }
}
