package Controller.Appointments;

import Controller.MainMenu;
import Model.Appointment;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class UpdateAppointment implements Initializable {

    /**
     * Label that generates the error message for user id combobox if it's empty.
     */
    @FXML
    private Label userIdErrorMessage;

    /**
     * Label that generates the error message for type combobox if it's empty.
     */
    @FXML
    private Label typeErrorMessage;

    /**
     * Label that generates the error message for contact combobox if it's empty.
     */
    @FXML
    private Label contactErrorMessage;

    /**
     * Label that generates the error message for title textfield if it's empty
     */
    @FXML
    private Label titleErrorMessage;

    /**
     * Label that generates the error message for customer id combobox if it's empty.
     */
    @FXML
    private Label customerErrorMessage;

    /**
     * Label that generates the error message for different error controls for time selection.
     */
    @FXML
    private Label timesErrorMessage;

    /**
     * title textfield.
     */
    @FXML
    private TextField titleTextField;

    /**
     * id text field.
     */
    @FXML
    private TextField IdTextField;

    /**
     * contact combobox.
     */
    @FXML
    private ComboBox<String> contactComboBox;

    /**
     * appointment date picker.
     */
    @FXML
    private DatePicker startDatePicker;

    /**
     * description textfield.
     */
    @FXML
    private TextArea descriptionTextField;

    /**
     * combobox for start hours.
     */
    @FXML
    private ComboBox<String> startHour;

    /**
     * location combobox.
     */
    @FXML
    ComboBox<String> locationComboBox;

    /**
     * combobox end hours.
     */
    @FXML
    private ComboBox<String> endHour;

    /**
     * user id combobox.
     */
    @FXML
    private ComboBox<Integer> userIDComboBox;

    /**
     * customer combobox.
     */
    @FXML
    private ComboBox<Integer> customerComboBox;

    /**
     * Label that generates the error message for title location combobox if it's empty.
     */
    @FXML
    private Label locationErrorMessage;

    @FXML
    private ObservableList<String> startTime = FXCollections.observableArrayList();

    @FXML
    private ObservableList<String> endTime = FXCollections.observableArrayList();

    @FXML
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private int contactId = -1;

    /**
     * initializes all the methods and sets tableview.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        Appointment selectedAppointment = MainMenu.selectedAppointment;
        System.out.println("HERE IS USER ID" + selectedAppointment.getUserId());

        IdTextField.setText(String.valueOf(selectedAppointment.getApptId()));
        titleTextField.setText(selectedAppointment.getTitle());
        descriptionTextField.setText(selectedAppointment.getDescription());
        typeComboBox.getSelectionModel().select(selectedAppointment.getType());
        userIDComboBox.getSelectionModel().select(Integer.valueOf(selectedAppointment.getUserId()));
        locationComboBox.getSelectionModel().select(selectedAppointment.getLocation());
        contactComboBox.getSelectionModel().select(selectedAppointment.getContactName());
        customerComboBox.getSelectionModel().select(Integer.valueOf(selectedAppointment.getCustomerId()));

        String startLocal = selectedAppointment.getApptStart();
        String endLocal = selectedAppointment.getApptEnd();

        LocalDateTime localDateTimeStart = LocalDateTime.parse(startLocal, dateTimeFormatter);
        System.out.println(localDateTimeStart);
        LocalDateTime localDateTimeEnd = LocalDateTime.parse(endLocal, dateTimeFormatter);

        startDatePicker.setValue(LocalDate.from(localDateTimeStart.toLocalDate()));
        startHour.getSelectionModel().select(localDateTimeStart.toLocalTime().format(timeFormatter));
        System.out.println(localDateTimeStart.toLocalTime().format(timeFormatter));
        endHour.getSelectionModel().select(localDateTimeEnd.toLocalTime().format(timeFormatter));

        fillLocationMeeting();
        fillTypesOfMeetings();
        appointmentTimes();
        getCustomerId();
        getContactName();
        getUserId();

    }

    /**
     * method that directs the user back to the main screen.
     * @param event
     * @throws IOException
     */
    @FXML
    void onCancel(ActionEvent event) throws IOException {
        Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
        add_part.setStyle("-fx-font-family: sans-serif");
        Scene add_part_scene = new Scene(add_part);
        Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_part_stage.setScene(add_part_scene);
        add_part_stage.show();
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
     * updates row into appointments Table if all fields are valid.
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    void onSaveAppointment(ActionEvent event) throws IOException, SQLException {

        if (appointmentValidation()) {
            convertContactName();
            int appointmentId = Integer.parseInt(IdTextField.getText());
            String title = titleTextField.getText();
            String description = descriptionTextField.getText();
            String location = locationComboBox.getValue();
            String type = typeComboBox.getValue();
            LocalDate startDate = startDatePicker.getValue();
            LocalTime startAppointmentHour = LocalTime.parse(startHour.getSelectionModel().getSelectedItem());
            LocalTime endAppointmentHour = LocalTime.parse(endHour.getSelectionModel().getSelectedItem());
            LocalDate endDate = startDatePicker.getValue();
            int customerId = customerComboBox.getValue();
            int userID = userIDComboBox.getValue();


            ZoneId zoneId = ZoneId.systemDefault();

            LocalDateTime startAppointment = LocalDateTime.of(startDate, startAppointmentHour);
            ZonedDateTime utcStartAppointment = startAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
            LocalDateTime endAppointment = LocalDateTime.of(endDate, endAppointmentHour);
            ZonedDateTime utcEndAppointment = endAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));

            Statement statement = DBconnection.getConnection().createStatement();

            String sqlStatement = "UPDATE appointments SET Title ='" + title + "', " +
                    "Description = '" + description + "', Location = '" + location + "', Type = '" + type + "', " +
                    "Contact_ID = '" + contactId + "', Customer_ID = '" + customerId + "', User_ID = '" + userID + "', " +
                    "Start = '" + dateTimeFormatter.format(utcStartAppointment) + "', End = '" + dateTimeFormatter.format(utcEndAppointment) + "' " +
                    "WHERE Appointment_ID = '" + appointmentId + "'";

            statement.executeUpdate(sqlStatement);

            Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
            add_part.setStyle("-fx-font-family: sans-serif");
            Scene add_part_scene = new Scene(add_part);

            Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            add_part_stage.setScene(add_part_scene);
            add_part_stage.show();
        }
    }

    /**
     * converts the contact name selection from combobox into database contact id.
     */
    @FXML
    private void convertContactName() {

        String contactName = contactComboBox.getValue();
        try {
            Statement statement = DBconnection.getConnection().createStatement();

            String sqlStatement = "SELECT Contact_ID FROM contacts WHERE Contact_Name ='" + contactName + "'";

            ResultSet result = statement.executeQuery(sqlStatement);

            while (result.next()) {
                contactId = result.getInt("Contact_ID");
            }
        } catch (SQLException e) {
            e.getMessage();
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
            customerComboBox.getItems().clear();

            while (rs.next()) {
                customerComboBox.getItems().add(rs.getInt("Customer_ID"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
     * validates all fields on the form before updating appointment.
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

        if (contactComboBox.getItems().isEmpty()) {
            contactErrorMessage.setText("Please select a contact.");
            return false;
        } if (titleTextField.getText().isEmpty()) {
            titleErrorMessage.setText("Please fill in a title.");
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
        } if (estEndAppointment.getMinute() >= 15 && estEndAppointment.getHour() >= 22|| estEndAppointment.getMinute() >= 0 && estEndAppointment.getHour() > 22 || estEndAppointment.getHour() <= 8 || endHour.getSelectionModel().isEmpty()) {
            timesErrorMessage.setText("Please select another time between 08:00 - 22:00 EST");
            return false;
        } if (!conflictingTimes()) {
            timesErrorMessage.setText("This customer already has an appointment at this time!");
            return false;
        } if (customerComboBox.getItems().isEmpty()) {
            customerErrorMessage.setText("Please select a Customer ID.");
            return false;
        } if (userIDComboBox.getItems().isEmpty()) {
            userIdErrorMessage.setText("Please select User ID.");
            return false;
        } if (startAppointmentHour.isAfter(endAppointmentHour)) {
            timesErrorMessage.setText("Your end time cannot be before your start time!");
            return false;
        }
        return true;
    }

    /**
     * compares the appointment records to all records that are
     * @return
     * @throws SQLException
     */
    @FXML
    private boolean conflictingTimes() throws SQLException {

        int customerId = customerComboBox.getValue();
        int appointmentId = Integer.parseInt(IdTextField.getText());

        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate startDate = startDatePicker.getValue();
        LocalTime startAppointmentHour = LocalTime.parse(startHour.getSelectionModel().getSelectedItem());
        LocalTime endAppointmentHour = LocalTime.parse(endHour.getSelectionModel().getSelectedItem());

        LocalDateTime startAppointment = LocalDateTime.of(startDate, startAppointmentHour);
        ZonedDateTime utcStartAppointment = startAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        LocalDateTime endAppointment = LocalDateTime.of(startDate, endAppointmentHour);
        ZonedDateTime utcEndAppointment = endAppointment.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
        Statement statement = DBconnection.getConnection().createStatement();
        String sqlStatement = "Select * From appointments Where (Customer_ID = '" + customerId + "' AND Appointment_ID != '" + appointmentId + "') AND " +
                "('" + dateTimeFormatter.format(utcStartAppointment) + "' between Start AND End OR '" + dateTimeFormatter.format(utcEndAppointment) + "' BETWEEN Start AND End " +
                "OR '" + dateTimeFormatter.format(utcStartAppointment) + "' < Start AND '" + dateTimeFormatter.format(utcEndAppointment) + "' > End) ";

        ResultSet result = statement.executeQuery(sqlStatement);
        while (result.next()) {
            return false;
        }
        return true;
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
     * populates list of user id's from database into user id combobox.
     */
    @FXML
    private void getUserId() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT User_ID FROM users ORDER BY User_ID";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            userIDComboBox.getItems().clear();
            while (rs.next()) {
                userIDComboBox.getItems().add(rs.getInt("User_ID"));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * method that deletes the appointment record from the database.
     * @param event
     */
    @FXML
    void onDeleteAppointment(ActionEvent event) {
        int appointmentId = Integer.parseInt(IdTextField.getText());
        String type = typeComboBox.getValue();
        System.out.println(appointmentId);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Are you sure you want to delete this appointment?");
        Optional<ButtonType> result = alert.showAndWait();
        try {
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Statement statement = DBconnection.getConnection().createStatement();
                String sqlStatement = " DELETE FROM appointments WHERE Appointment_ID = '" + appointmentId + "'";
                statement.executeUpdate(sqlStatement);
                Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
                add_part.setStyle("-fx-font-family: sans-serif");
                Scene add_part_scene = new Scene(add_part);
                Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                add_part_stage.setScene(add_part_scene);
                add_part_stage.show();
                Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
                alert1.setTitle("Information");
                alert1.setContentText("The Appointment: " + appointmentId + " Type: " + type + "has been deleted! ");
                alert1.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.getMessage();
        }
    }
}
