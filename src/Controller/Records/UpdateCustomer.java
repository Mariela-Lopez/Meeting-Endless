package Controller.Records;

import Model.Customer;
import Utils.DBconnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateCustomer implements Initializable {
    @FXML
    private int divisionId = -1;

    /**
     * textfield for customer name input.
     */
    @FXML
    private TextField customerNameTextField;

    /**
     * text field for customer ID.
     */
    @FXML
    public TextField customerIdTextField;

    /**
     * text field for street address.
     */
    @FXML
    private TextField streetAddressTextField;

    /**
     * combobox for list of countries..
     */
    @FXML
    private ComboBox<String> countryComboBox;

    /**
     * text field for zip code input.
     */
    @FXML
    private TextField zipCodeTextField;

    /**
     * textfield for phone number.
     */
    @FXML
    private TextField phoneNumberTextField;

    /**
     * combobox for states or region selection.
     */
    @FXML
    private ComboBox<String> stateComboBox;

    /**
     * initializes all of the
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Customer selectedCustomer = CustomersMenu.selectedCustomer;

        customerIdTextField.setText(String.valueOf(selectedCustomer.getId()));
        customerNameTextField.setText(selectedCustomer.getName());
        streetAddressTextField.setText(selectedCustomer.getAddress());
        zipCodeTextField.setText(String.valueOf(selectedCustomer.getPostalCode()));
        phoneNumberTextField.setText(selectedCustomer.getPhoneNumber());
        countryComboBox.getSelectionModel().select(selectedCustomer.getCountryName());
        stateComboBox.getSelectionModel().select(selectedCustomer.getDivisionName());
        updateCountry();

    }

    /**
     * method that deletes the customer record from the database.
     * @param event
     * @throws IOException
     */
    @FXML
    void onDeleteCustomer(ActionEvent event) throws IOException {
        int customerId = Integer.parseInt(customerIdTextField.getText());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Are you sure you want to delete this customer?");

        Optional<ButtonType> result = alert.showAndWait();
        try {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Statement statement = DBconnection.getConnection().createStatement();
                String sqlStatement = " DELETE FROM customers WHERE Customer_ID = '" + customerId + "'";

                statement.executeUpdate(sqlStatement);


                Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/Records/CustomersMenu.fxml"));
                add_part.setStyle("-fx-font-family: sans-serif");
                Scene add_part_scene = new Scene(add_part);

                Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                add_part_stage.setScene(add_part_scene);
                add_part_stage.show();
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information");
                alert1.setContentText("The customer ID " + customerId + " has been deleted! ");
                alert1.showAndWait();

            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                Alert dialog = new Alert(Alert.AlertType.ERROR, "Error", ButtonType.OK);
                dialog.setContentText("You must delete all appointments associated with this customer before deletion!");
                dialog.show();
                System.out.println(dialog);
            });
        }
    }

    /**
     * updates the record in the database based on the customer ID and changes in the textfield.
     * @param event
     * @throws SQLException
     * @throws IOException
     */
    @FXML
    void onSaveCustomer(ActionEvent event) throws SQLException, IOException {
        String name = customerNameTextField.getText();
        String address = streetAddressTextField.getText();
        String zipcode = zipCodeTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        int customerId = Integer.parseInt(customerIdTextField.getText());
        convertDivisionId();
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
        } else {
            Statement statement = DBconnection.getConnection().createStatement();

            String sqlStatement = "UPDATE customers SET Customer_ID ='" + customerId + "', " +
                    "Customer_Name = '" + name + "', Address = '" + address + "', Postal_Code = '" + zipcode + "', " +
                    "Phone = '" + phoneNumber + "', Division_ID = '" + divisionId + "' WHERE Customer_ID = '" + customerId + "'";

            statement.executeUpdate(sqlStatement);

            Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/Records/CustomersMenu.fxml"));
            add_part.setStyle("-fx-font-family: sans-serif");
            Scene add_part_scene = new Scene(add_part);

            Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            add_part_stage.setScene(add_part_scene);
            add_part_stage.show();
        }
    }

    /**
     * method that converts the selection from states combobox into it's corresponding id from the database
     */
    @FXML
    private void convertDivisionId() {

        String divisionName = stateComboBox.getValue();
        try {
            Statement statement = DBconnection.getConnection().createStatement();

            String sqlStatement = "SELECT Division_ID FROM first_level_divisions WHERE first_level_divisions.Division ='" + divisionName + "'";

            ResultSet result = statement.executeQuery(sqlStatement);

            while (result.next()) {
                divisionId = result.getInt("Division_ID");
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    /**
     * populates list of countries from database into country combobox.
     */
    @FXML
    private void updateCountry() {
        updateState();
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
    private void updateState() {
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
}
