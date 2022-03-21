package Controller;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ResourceBundle;


public class MainMenu implements Initializable {

    @FXML
    public static Appointment selectedAppointment;

    @FXML
    private TableView<Appointment> appointmentsTableview;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdColumn;

    @FXML
    private TableColumn<Appointment, String> titleColumn;

    @FXML
    private TableColumn<Appointment, String> descriptionColumn;

    @FXML
    private TableColumn<Appointment, String> locationColumn;

    @FXML
    private TableColumn<Appointment, String> contactColumn;

    @FXML
    private TableColumn<Appointment, String> typeColumn;

    @FXML
    private TableColumn<Appointment, String> startColumn;

    @FXML
    private TableColumn<Appointment, String> endColumn;

    @FXML
    private TableColumn<Appointment, Integer> customerIdColumn;

    @FXML
    private TableColumn<Appointment, Integer> userId;

    @FXML
    private RadioButton allAppointmentsRadioButton;

    @FXML
    private final ZoneId zoneId = ZoneId.systemDefault();

    @FXML
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private DateTimeFormatter apppointmentStartDTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    /**
     * initializes all the methods and tableviw columns.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        allAppointmentsRadioButton.setSelected(true);
        appointmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("apptId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("apptStart"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("apptEnd"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        setAppointmentsTableview();
    }

    /**
     * sets the tableview for all appointments.
     */
    @FXML
    public void setAppointmentsTableview() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT appointments.Appointment_ID, appointments.Title, appointments.Description, " +
                    "appointments.Location, appointments.Type, appointments.Start, appointments.End, appointments.User_ID, appointments.Customer_ID, contacts.Contact_Name " +
                    "FROM appointments " +
                    "INNER JOIN contacts ON appointments.Contact_ID = contacts.Contact_ID " +
                    "Order by Start";
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
                appointment.setContactName(rs.getString("Contact_Name"));
                appointment.setType(rs.getString("Type"));
                appointment.setUserId(rs.getInt("User_ID"));
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
            appointmentsTableview.setItems(appointmentData);
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    /**
     * directs user to customers menu.
     * @param event
     * @throws IOException
     */
    @FXML
    void onCustomerRecordsClick(ActionEvent event) throws IOException {
        Parent add_product = FXMLLoader.load(getClass().getResource("../Views/Records/CustomersMenu.fxml"));
        add_product.setStyle("-fx-font-family: sans-serif");
        Scene add_product_scene = new Scene(add_product);
        Stage add_product_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_product_stage.setScene(add_product_scene);
        add_product_stage.show();
    }

    /**
     * directs user to new appointments screen.
     * @param event
     * @throws IOException
     */
    @FXML
    void onNewClick(ActionEvent event) throws IOException {
        Parent add_product = FXMLLoader.load(getClass().getResource("../Views/Appointments/NewAppointment.fxml"));
        add_product.setStyle("-fx-font-family: sans-serif");
        Scene add_product_scene = new Scene(add_product);
        Stage add_product_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_product_stage.setScene(add_product_scene);
        add_product_stage.show();
    }

    /**
     * stores value of selection from tableview and directs to udpate appontments screen when button is pressed.
     * @param event
     * @throws IOException
     */
    @FXML
    void onUpdateClick(ActionEvent event) throws IOException {
        selectedAppointment = appointmentsTableview.getSelectionModel().getSelectedItem();

        if (selectedAppointment == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No Appointment was selected!");
            alert.showAndWait();
        } else {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
            Parent addPartScreenParent = FXMLLoader.load(getClass().getResource("../Views/Appointments/UpdateAppointment.fxml"));
            addPartScreenParent.setStyle("-fx-font-family: sans-serif");
            Scene addPartScreenScene = new Scene(addPartScreenParent);
            Stage stage = new Stage();
            stage.setScene(addPartScreenScene);
            stage.show();
        }
    }

    /**
     * LAMBDA
     * when selected the tableview is filtered to show appointments for the rest of the month.
     * @param event
     */
    @FXML
    void onMonthRadioButton(ActionEvent event) {

        FilteredList<Appointment> filteredByThisMonth = new FilteredList<>(appointmentData, a -> {
            LocalDate monthData = LocalDate.parse(a.getApptStart(), apppointmentStartDTF);
            return monthData.isAfter(LocalDate.now()) && monthData.isBefore(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()));
        });
        appointmentsTableview.setItems(filteredByThisMonth);
    }

    /**
     * LAMBDA
     * when selected the tableview is filtered to show appointments by upcoming week.
     * @param event
     */
    @FXML
    void onWeekRadioButton(ActionEvent event) {
        FilteredList<Appointment> filteredByThisWeek = new FilteredList<>(appointmentData, a -> {
            LocalDate weekData = LocalDate.parse(a.getApptStart(), apppointmentStartDTF);
            return weekData.isAfter(LocalDate.now()) && weekData.isBefore(LocalDate.now().plusDays(7));

        });
        appointmentsTableview.setItems(filteredByThisWeek);
    }

    /**
     * populates tableivew with all appointments when radiobutton is selected "all appointments"
     * @param event
     */
    @FXML
    void onAllAppointments(ActionEvent event) {
        appointmentsTableview.setItems(appointmentData);
    }

    /**
     * closes out entire application.
     * @param event
     */
    @FXML
    void onExit(ActionEvent event) {
        System.exit(0);
    }

    /**
     * directs user to reports screen when clicked.
     * @param event
     * @throws IOException
     */
    @FXML
    void onReportsButtonClick(ActionEvent event) throws IOException {
        Parent add_product = FXMLLoader.load(getClass().getResource("../Views/Appointments/Records.fxml"));
        add_product.setStyle("-fx-font-family: sans-serif");
        Scene add_product_scene = new Scene(add_product);
        Stage add_product_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        add_product_stage.setScene(add_product_scene);
        add_product_stage.show();
    }
}
