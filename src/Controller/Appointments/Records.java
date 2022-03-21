package Controller.Appointments;

import Model.Appointment;
import Utils.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class Records implements Initializable {
    /**
     * textarea to display record feedback.
     */
    @FXML
    private TextArea textAreaReport;

    /**
     * date formatter.
     */
    @FXML
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     *  date formatter.
     */
    @FXML
    private DateTimeFormatter apppointmentStartDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * type combobox.
     */
    @FXML
    private ComboBox<String> typeComboBox;

    /**
     * combobox for months.
     */
    @FXML
    private ComboBox<Integer> monthComboBox;

    /**
     * tableveiw for contacts schedules.
     */
    @FXML
    private TableView<Appointment> tableviewContacts;

    /**
     * appointment ID column in tableview.
     */
    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;

    /**
     * title column for tableview.
     */
    @FXML
    private TableColumn<Appointment, String> titleIdColumn;

    /**
     * type column for tableview.
     */
    @FXML
    private TableColumn<Appointment, String> typeColumn;

    /**
     * start date column for appointments tableview.
     */
    @FXML
    private TableColumn<Appointment, String> startColumn;

    /**
     * end date column for appointments tableview.
     */
    @FXML
    private TableColumn<Appointment, String> endColumn;

    /**
     * Customer ID column for appointments based on contacts.
     */
    @FXML
    private TableColumn<Appointment, Integer> customerIdColumn;

    /**
     * description column for appointments.
     */
    @FXML
    private TableColumn<Appointment, String> descriptionColumn;

    /**
     * combobox for contacts selection.
     */
    @FXML
    private ComboBox<String> contactComboBox;

    /**
     * combobox for location selection.
     */
    @FXML
    private ComboBox<String> locationComboBox;

    /**
     * tableview for appointments based on locations.
     */
    @FXML
    private TableView<Appointment> tableviewLocation;

    /**
     * Appointment ID column for location tableview.
     */
    @FXML
    private TableColumn<Appointment, String> AppointmentIdLocationColumn;

    /**
     * start dates for appointments in tableview.
     */
    @FXML
    private TableColumn<Appointment, String> startColumnLocation;

    /**
     * end dates for appointments in tableview.
     */
    @FXML
    private TableColumn<Appointment, String> EndColumnLocation;

    /**
     * value of current Zone ID
     */
    @FXML
    private final ZoneId zoneId = ZoneId.systemDefault();

    @FXML
    private int contactId = -1;

    /**
     * observable list for appointments based on contacts..
     */
    @FXML
    public ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    /**
     * observable list for all appointments for later filtering based on types and dates.
     */
    @FXML
    public ObservableList<Appointment> allAppointmentData = FXCollections.observableArrayList();

    /**
     * observable list for appointments with that specific location data.
     */
    @FXML
    public ObservableList<Appointment> locationData = FXCollections.observableArrayList();

    /**
     *initializes methods.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fillTypesOfMeetings();
        getContactName();
        fillMonths();
        fillLocationMeeting();
        getAllInfo();
    }

    /**
     * inserts appointment info based on contactID value into observable list and into tableview.
     */
    @FXML
    public void setAppointmentsTableview() {

        try {
            convertContactName();
            Connection connect = DBconnection.getConnection();
            String query = "SELECT * FROM appointments WHERE Contact_ID = '" + contactId + "'";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            appointmentData.clear();
            ZoneId utcZone = ZoneId.of("UTC");
            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setApptId(rs.getInt("Appointment_ID"));
                appointment.setTitle(rs.getString("Title"));
                appointment.setDescription(rs.getString("Description"));
                appointment.setLocation(rs.getString("Location"));
                appointment.setType(rs.getString("Type"));
                appointment.setCustomerId(rs.getInt("Customer_ID"));
                String startAppointmentUTC = rs.getString("Start");
                String endAppointmentUTC = rs.getString("End");

                LocalDateTime startApptLocal = LocalDateTime.parse(startAppointmentUTC, dateFormatter);
                LocalDateTime endApptLocal = LocalDateTime.parse(endAppointmentUTC, dateFormatter);
                ZonedDateTime localStartZone = startApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
                ZonedDateTime localEndZone = endApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
                appointment.setApptStart(localStartZone.format(apppointmentStartDTF));
                appointment.setApptEnd(localEndZone.format(apppointmentStartDTF));
                appointmentData.addAll(appointment);
            }
            tableviewContacts.setItems(appointmentData);
            appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("apptId"));
            titleIdColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            startColumn.setCellValueFactory(new PropertyValueFactory<>("apptStart"));
            endColumn.setCellValueFactory(new PropertyValueFactory<>("apptEnd"));
            customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    /**
     * converts the contact name selected into the Contact_ID in the database for easier filtering.
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
     * populates contact combobox for selection.
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
     *populates type combobox for selection.
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
     * observable list that populates months based on number of the month.
     */
    @FXML
    private void fillMonths() {
        ObservableList<Integer> months = FXCollections.observableArrayList();
        months.add(1);
        months.add(2);
        months.add(3);
        months.add(4);
        months.add(5);
        months.add(6);
        months.add(7);
        months.add(8);
        months.add(9);
        months.add(10);
        months.add(11);
        months.add(12);
        monthComboBox.setItems(months);
    }

    /**
     * gets appointments data based on combobox selection of location for the 3rd report.
     * @throws SQLException
     */
    @FXML
    private void setAppointmentLocations() throws SQLException {
        String location = locationComboBox.getValue();
        ZoneId utcZone = ZoneId.of("UTC");
        Connection connect = DBconnection.getConnection();
        String query = "SELECT * From appointments Where Location =  '" + location + "'";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        locationData.clear();
        while (rs.next()) {
            Appointment appointment = new Appointment();
            appointment.setLocation(rs.getString("Location"));
            appointment.setApptId(rs.getInt("Appointment_ID"));
            String startAppointmentUTC = rs.getString("Start");
            String endAppointmentUTC = rs.getString("End");

            LocalDateTime startApptLocal = LocalDateTime.parse(startAppointmentUTC, dateFormatter);
            LocalDateTime endApptLocal = LocalDateTime.parse(endAppointmentUTC, dateFormatter);

            ZonedDateTime localStartZone = startApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
            ZonedDateTime localEndZone = endApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);

            appointment.setApptStart(localStartZone.format(apppointmentStartDTF));
            appointment.setApptEnd(localEndZone.format(apppointmentStartDTF));
            locationData.addAll(appointment);
        }
        startColumnLocation.setCellValueFactory(new PropertyValueFactory<>("apptStart"));
        EndColumnLocation.setCellValueFactory(new PropertyValueFactory<>("apptEnd"));
        AppointmentIdLocationColumn.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        tableviewLocation.setItems(locationData);
    }

    /**
     * observable list that populates location combobox.
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
     * when user clicks on exit, it redirects them to main menu screen.
     * @param event
     * @throws IOException
     */
    @FXML
    void onExit(ActionEvent event) throws IOException {
        Parent add_part = FXMLLoader.load(getClass().getResource("../../Views/MainMenu.fxml"));
        add_part.setStyle("-fx-font-family: sans-serif");
        Scene add_part_scene = new Scene(add_part);
        Stage add_part_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_part_stage.setScene(add_part_scene);
        add_part_stage.show();
    }

    /**
     * inserts all appointments into observalble list for filtering later on based on types and dates.
     */
    @FXML
    public void getAllInfo() {
        try {
            convertContactName();
            Connection connect = DBconnection.getConnection();
            String query = "SELECT * FROM appointments";
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            allAppointmentData.clear();
            ZoneId utcZone = ZoneId.of("UTC");

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setApptId(rs.getInt("Appointment_ID"));
                appointment.setTitle(rs.getString("Title"));
                appointment.setDescription(rs.getString("Description"));
                appointment.setLocation(rs.getString("Location"));
                appointment.setType(rs.getString("Type"));
                appointment.setCustomerId(rs.getInt("Customer_ID"));
                String startAppointmentUTC = rs.getString("Start");
                String endAppointmentUTC = rs.getString("End");

                LocalDateTime startApptLocal = LocalDateTime.parse(startAppointmentUTC, dateFormatter);
                LocalDateTime endApptLocal = LocalDateTime.parse(endAppointmentUTC, dateFormatter);
                ZonedDateTime localStartZone = startApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
                ZonedDateTime localEndZone = endApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
                appointment.setApptStart(localStartZone.format(dateFormatter));
                appointment.setApptEnd(localEndZone.format(dateFormatter));

                allAppointmentData.addAll(appointment);
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    /**
     * LAMBDA EXPRESSION- filters each appointment and inserts into filtered list depending on parameters that are passed.
     * filteres list depending on paramaters that are passed and type of appointment.
     * @param month - based on month integer
     * @param day - based on days of the month
     */
    @FXML
    void filteredListByMonth(int month, int day) {

        textAreaReport.clear();
        FilteredList<Appointment> filteredByThisMonth = new FilteredList<>(allAppointmentData, a -> {
            LocalDate monthData = LocalDate.parse(a.getApptStart(), apppointmentStartDTF);
            String type = a.getType();
            return type.contains(typeComboBox.getValue()) && monthData.isAfter(LocalDate.of(2021, month, 1)) && monthData.isBefore(LocalDate.of(2021, month, day));
        });

        System.out.println(filteredByThisMonth);
        if (filteredByThisMonth.isEmpty()) {
            textAreaReport.clear();
            textAreaReport.setText("no appointments");
            filteredByThisMonth.clear();
        } else {
            textAreaReport.clear();
            int number = filteredByThisMonth.size();
            textAreaReport.setText("Number of appointments by this type and month: " + number);
            filteredByThisMonth.clear();
        }
    }

    /**
     * passes parameters to filteredListByMonth() depending on month selection
     */
    @FXML
    private void getAppointmentsByMonth() {
        if (monthComboBox.getValue().equals(1) || monthComboBox.getValue().equals(3) || monthComboBox.getValue().equals(5) || monthComboBox.getValue().equals(7) ||
                monthComboBox.getValue().equals(8) || monthComboBox.getValue().equals(10) || monthComboBox.getValue().equals(12)) {
            int day = 31;
            Integer month = monthComboBox.getValue();
            filteredListByMonth(month, day);
        } else if (monthComboBox.getValue().equals(2)) {
            int day = 28;
            Integer month = monthComboBox.getValue();
            filteredListByMonth(month, day);
        } else if (monthComboBox.getValue().equals(4) || monthComboBox.getValue().equals(6) || monthComboBox.getValue().equals(9) || monthComboBox.getValue().equals(11)) {
            int day = 30;
            Integer month = monthComboBox.getValue();
            filteredListByMonth(month, day);
        }
    }
}
