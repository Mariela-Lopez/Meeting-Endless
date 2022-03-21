package Controller.Appointments;

import Model.*;
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
import javafx.scene.control.*;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class NewAppointment implements Initializable {

    /**
     * textfield for title input.
     */
    @FXML
    private TextField titleTextField;

    /**
     * combobox for location selection.
     */
    @FXML
    private ComboBox<String> locationComboBox;

    /**
     * datepicker for Appointment date.
     */
    @FXML
    private DatePicker startDatePicker;

    /**
     * textarea for description.
     */
    @FXML
    private TextArea descriptionTextField;

    /**
     * combobox for selecting the start hour.
     */
    @FXML
    private ComboBox<String> startHour;

    /**
     * combobox for selecting the end hour.
     */
    @FXML
    private ComboBox<String> endHour;

    /**
     * combobox for selecting the contact name.
     */
    @FXML
    private ComboBox<String> contactComboBox;

    /**
     * combobox for user ID selection.
     */
    @FXML
    private ComboBox<Integer> userIdComboBox;

    /**
     * combobox for customerId selection.
     */
    @FXML
    private ComboBox<Integer> customerIdCombobox;

    /**
     * Label that generates the error message for different error controls for time selection.
     */
    @FXML
    private Label timesErrorMessage;

    /**
     * Label that generates the error message for empty contact selection.
     */
    @FXML
    private Label contactErrorMessage;

    /**
     * Label that generates the error message for title textfield if it's empty.
     */
    @FXML
    private Label titleErrorMesssage;

    /**
     * Label that generates the error message for title location combobox if it's empty.
     */
    @FXML
    private Label locationErrorMessage;

    /**
     * Label that generates the error message for Type combobox if it's empty.
     */
    @FXML
    private Label typeErrorMessage;

    /**
     * combobox for type selection.
     */
    @FXML
    private ComboBox<String> typeComboBox;

    /**
     * Label that generates the error message for customer id combobox if it's empty.
     */
    @FXML
    private Label customerErrorMessage;

    /**
     * Label that generates the error message for user id combobox if it's empty.
     */
    @FXML
    private Label userIdErrorMessage;

    /**
     * observable list full of hours
     */
    @FXML
    private ObservableList<String> startTime = FXCollections.observableArrayList();

    /**
     * observable list full of hours.
     */
    @FXML
    private ObservableList<String> endTime = FXCollections.observableArrayList();

    /**
     * time formatter
     */
    @FXML
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * date time formatter for insertion
     */
    @FXML
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * initializes all the methods needed to generate the list of elements in all comboboxes.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startDatePicker.setEditable(false);
        appointmentTimes();
        getContactName();
        getCustomerId();
        getUserId();
        fillTypesOfMeetings();
        fillLocationMeeting();
    }

    /**
     * Method used to add hours to observable list and display in start and end combobox.
     */
    @FXML
    private void appointmentTimes() {
        LocalTime time = LocalTime.of(1, 0, 0);
        do {
            time = time.plusMinutes(15);
            startTime.add(time.format(timeFormatter));
            endTime.add(time.format(timeFormatter));
        } while (!time.equals(LocalTime.of(23, 0, 0)));
        startHour.setItems(startTime);
        endHour.setItems(endTime);
    }

    /**
     *
     * Inserts new row into appointments Table if all fields are valid.
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    void onSaveAppointment(ActionEvent event) throws IOException, SQLException {
        if (appointmentValidation()) {
            String title = titleTextField.getText();
            String description = descriptionTextField.getText();
            String location = locationComboBox.getValue();
            String type = typeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalTime startAppointmentHour = LocalTime.parse(startHour.getSelectionModel().getSelectedItem());
            LocalTime endAppointmentHour = LocalTime.parse(endHour.getSelectionModel().getSelectedItem());
            LocalDate endDate = startDatePicker.getValue();
            String contactName = contactComboBox.getValue();
            int customerId = customerIdCombobox.getValue();
            int userID = userIdComboBox.getValue();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime startAppointment = LocalDateTime.of(startDate, startAppointmentHour);
            ZonedDateTime utcStartAppointment = startAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime endAppointment = LocalDateTime.of(endDate, endAppointmentHour);
            ZonedDateTime utcEndAppointment = endAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
            try {
                int contactId = -1;
                Statement statement = DBconnection.getConnection().createStatement();
                String sqlStatement = "SELECT Contact_ID FROM contacts WHERE contacts.Contact_Name ='" + contactName + "'";
                ResultSet result = statement.executeQuery(sqlStatement);
                while (result.next()) {
                    contactId = result.getInt("Contact_ID");
                }
                Appointment appointment = new Appointment(title, description,
                        location, contactId, type, dateFormatter.format(utcStartAppointment), dateFormatter.format(utcEndAppointment), userID, customerId);
                appointment.addAppointment(appointment);
                System.out.println(appointment);
            } catch (SQLException e) {
                e.getMessage();
            }
            Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
            add_part.setStyle("-fx-font-family: sans-serif");
            Scene add_part_scene = new Scene(add_part);

            Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            add_part_stage.setScene(add_part_scene);
            add_part_stage.show();
        }
    }

    /**
     * checks database before a new appointment is made in order to not have overlapping appointments.
     * @return
     * @throws SQLException
     */
    @FXML
    private boolean conflictingTimes() throws SQLException {
        int customerId = customerIdCombobox.getValue();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startDate = startDatePicker.getValue();
        LocalTime startAppointmentHour = LocalTime.parse(startHour.getSelectionModel().getSelectedItem());
        LocalTime endAppointmentHour = LocalTime.parse(endHour.getSelectionModel().getSelectedItem());
        LocalDateTime startAppointment = LocalDateTime.of(startDate, startAppointmentHour);
        ZonedDateTime utcStartAppointment = startAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime endAppointment = LocalDateTime.of(startDate, endAppointmentHour);
        ZonedDateTime utcEndAppointment = endAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        Statement statement = DBconnection.getConnection().createStatement();
        String sqlStatement = "Select * From appointments Where Customer_ID = '" + customerId + "' AND " +
                "('" + dateFormatter.format(utcStartAppointment) + "' between Start AND End OR '" + dateFormatter.format(utcEndAppointment) + "' BETWEEN Start AND End " +
                "OR '" + dateFormatter.format(utcStartAppointment) + "' < Start AND '" + dateFormatter.format(utcEndAppointment) + "' > End) ";
        ResultSet result = statement.executeQuery(sqlStatement);
        while (result.next()) {
            return false;
        }
        return true;
    }

    /**
     * validates all fields on the form before creating new appointment.
     * validates eastern times and scheduling conflicts.
     * @return
     * @throws SQLException
     */
    @FXML
    private boolean appointmentValidation() throws SQLException {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate startDate = startDatePicker.getValue();
        LocalTime startAppointmentHour = LocalTime.parse(startHour.getSelectionModel().getSelectedItem());
        LocalTime endAppointmentHour = LocalTime.parse(endHour.getSelectionModel().getSelectedItem());
        LocalDateTime startAppointment = LocalDateTime.of(startDate, startAppointmentHour);
        ZonedDateTime estStartAppointment = startAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("America/New_York"));
        LocalDateTime endAppointment = LocalDateTime.of(startDate, endAppointmentHour);
        ZonedDateTime estEndAppointment = endAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("America/New_York"));
        System.out.println("This is eastern end appointment:" + estEndAppointment);
        System.out.println("this is easter start appointment:" + estStartAppointment);

        if (contactComboBox.getItems().isEmpty()) {

            contactErrorMessage.setText("Please select a contact.");
            return false;
        } if (titleTextField.getText().isEmpty()) {
            titleErrorMesssage.setText("Please fill in a title.");
            return false;
        } if (locationComboBox.getSelectionModel().isEmpty()) {
            locationErrorMessage.setText("Please fill in a location.");
            return false;
        } if (typeComboBox.getSelectionModel().isEmpty()) {
            typeErrorMessage.setText("Please fill in the type of meeting.");
            return false;
        } if (startDatePicker.getValue().isBefore(LocalDate.now())) {
            timesErrorMessage.setText("Meeting cannot start before Today.");
            return false;
        } if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            timesErrorMessage.setText("Please select a date from Monday-Friday.");
            return false;
        } if (estStartAppointment.getHour() >= 22 || estStartAppointment.getHour() < 8 || startHour.getSelectionModel().isEmpty()) {
            timesErrorMessage.setText("Please select another time between 08:00 - 22:00 EST");
            return false;
        } if (estEndAppointment.getMinute() >= 15 && estEndAppointment.getHour() >= 22 || estEndAppointment.getMinute() >= 0 && estEndAppointment.getHour() > 22 || estEndAppointment.getHour() <= 8 || endHour.getSelectionModel().isEmpty()) {
            timesErrorMessage.setText("Please select another time between 08:00 - 22:00 EST");
            return false;
        } if (!conflictingTimes()) {
            timesErrorMessage.setText("This customer already has an appointment at this time!");
            return false;
        } if (customerIdCombobox.getItems().isEmpty()) {
            customerErrorMessage.setText("Please select a Customer ID.");
            return false;
        } if (userIdComboBox.getItems().isEmpty()) {
            userIdErrorMessage.setText("Please select User ID.");
            return false;
        } if (startAppointmentHour.isAfter(endAppointmentHour)) {
            timesErrorMessage.setText("Your end time cannot be before your start time!");
            return false;
        }
        return true;
    }

    /**
     * gets contact names from database and inserts it into contacts combobox.
     */
    @FXML
    private void getContactName() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT Contact_Name FROM contacts";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            contactComboBox.getItems().clear();
            while (rs.next()) {
                contactComboBox.getItems().add(rs.getString(1));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets customer id's from database and inserts it into customers id combobox.
     */
    @FXML
    private void getCustomerId() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT Customer_ID FROM customers ORDER BY Customer_ID";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            customerIdCombobox.getItems().clear();
            while (rs.next()) {
                customerIdCombobox.getItems().add(rs.getInt("Customer_ID"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * gets UserIDs from database and inserts it into user ID combobox.
     */
    @FXML
    private void getUserId() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT User_ID FROM users ORDER BY User_ID";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            userIdComboBox.getItems().clear();
            while (rs.next()) {
                userIdComboBox.getItems().add(rs.getInt("User_ID"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a list of types of meetings to types combobox.
     */
    @FXML
    private void fillTypesOfMeetings() {
        ObservableList<String> types = FXCollections.observableArrayList();
        types.add("New Project");
        types.add("StandUp");
        types.add("Status Update");
        types.add("Team Building");
        typeComboBox.setItems(types);
    }

    /**
     * adds a list of locations to location combobox
     */
    @FXML
    private void fillLocationMeeting() {
        ObservableList<String> location = FXCollections.observableArrayList();
        location.add("Phoenix");
        location.add("Montreal");
        location.add("White Plains");
        location.add("London");
        locationComboBox.setItems(location);
    }

    /**
     * redirects to Main Screen when cancel button is pressed.
     * @param event
     * @throws IOException
     */
    @FXML
    private void onCancel(ActionEvent event) throws IOException {
        Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
        add_part.setStyle("-fx-font-family: sans-serif");
        Scene add_part_scene = new Scene(add_part);
        Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_part_stage.setScene(add_part_scene);
        add_part_stage.show();
    }
}
