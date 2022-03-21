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
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class LogIn implements Initializable {
    @FXML
    private Label errorMessage;

    @FXML
    private TextField userIdTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label locationLabel;

    @FXML
    private Button logInButton;

    @FXML
    private Label welcomeLabel;

    private final ZoneId zoneId = ZoneId.systemDefault();

    @FXML
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public ObservableList<Appointment> appointmentData = FXCollections.observableArrayList();

    /**
     * method that gets all appointments from database and inserts into observable list.
     */
    @FXML
    public void getAllAppointments() {
        try {
            Connection connect = DBconnection.getConnection();
            String query = "SELECT * FROM appointments";

            PreparedStatement preparedStatement = connect.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            appointmentData.clear();
            ZoneId utcZone = ZoneId.of("UTC");

            while (rs.next()) {
                Appointment appointment = new Appointment();

                appointment.setApptId(rs.getInt("Appointment_ID"));
                String startAppointmentUTC = rs.getString("Start");
                String endAppointmentUTC = rs.getString("End");

                LocalDateTime startApptLocal = LocalDateTime.parse(startAppointmentUTC, dateFormatter);
                LocalDateTime endApptLocal = LocalDateTime.parse(endAppointmentUTC, dateFormatter);

                ZonedDateTime localStartZone = startApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);
                ZonedDateTime localEndZone = endApptLocal.atZone(utcZone).withZoneSameInstant(zoneId);

                appointment.setApptStart(localStartZone.format(dateFormatter));
                appointment.setApptEnd(localEndZone.format(dateFormatter));

                appointmentData.addAll(appointment);
            }
        } catch (SQLException e) {
            e.getMessage();

        }
    }

    /**
     * method that writes to the login activity text file.
     *
     * @param s
     * @throws IOException
     */
    private void write(final String s) throws IOException {
        Files.writeString(
                Paths.get("login_activity.txt"),
                s + System.lineSeparator(),
                CREATE, APPEND
        );
    }


    /**
     * method that logs in the user if they have passed the verification process and directs
     * to the main screen.
     *
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    void onLogInButtonClick(ActionEvent event) throws IOException, SQLException {

        if (verifiedUser()) {
            String log = String.format("%s,%s,true",
                    userIdTextField.getText(), ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            write(log);
            Parent add_product = FXMLLoader.load(getClass().getResource("../Views/MainMenu.fxml"));
            add_product.setStyle("-fx-font-family: sans-serif");
            Scene add_product_scene = new Scene(add_product);
            Stage add_product_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            add_product_stage.setScene(add_product_scene);
            add_product_stage.show();
            appointmentAlert();
        } else {
            Locale locale = Locale.getDefault();
            String log = String.format("%s,%s,false",
                    userIdTextField.getText(), ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
            write(log);
            if (locale == Locale.FRANCE) {
                errorMessage.setText("Désolé, votre nom d'utilisateur ou votre mot de passe n'est pas correct. Veuillez réessayer.");
            } else {
                errorMessage.setText("Sorry, your username or password is incorrect. Try Again.");
            }
        }
    }

    /**
     * Method that verifies the user directly against the database based on input from textfield.
     *
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private boolean verifiedUser() throws SQLException, IOException {
        String userId = userIdTextField.getText();
        String userPassword = passwordTextField.getText();
        System.out.println(passwordTextField);

        Statement statement = DBconnection.getConnection().createStatement();
        String sqlStatement = "Select * FROM users WHERE User_Name  ='" + userId + "' " +
                "AND Password = '" + userPassword + "'";
        ResultSet result = statement.executeQuery(sqlStatement);

        while (result.next()) {
            System.out.println(userId + userPassword);
            if (userId.equals(result.getString("User_Name")) & userPassword.equals(result.getString("Password"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * LAMBDA
     * filters appointments that are acitve within the next 15 minutes and displays alert.
     */
    @FXML
    void appointmentAlert() {
        System.out.println(appointmentData.get(0));
        FilteredList<Appointment> upcomingAppointments = new FilteredList<>(appointmentData, a -> {
            LocalDateTime withinMinutes = LocalDateTime.parse(a.getApptStart(), dateFormatter);
            System.out.println(withinMinutes);
            return withinMinutes.isAfter(LocalDateTime.now().minusMinutes(3)) && withinMinutes.isBefore(LocalDateTime.now().plusMinutes(15));

        });
        if (upcomingAppointments.isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APPOINTMENT INFORMATION");
            alert.setContentText("Good Morning! You have no appointments at this time.");
            alert.showAndWait();
        } else {
            int appointmentId = upcomingAppointments.get(0).getApptId();
            String appointmentStartTime = upcomingAppointments.get(0).getApptStart();
            String appointmentEndTime = upcomingAppointments.get(0).getApptEnd();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("APPOINTMENT INFORMATION");
            alert.setContentText("Welcome! You have an Appointment in 15 minutes! Appointment ID: " + appointmentId + "Starting at: " + appointmentStartTime + "To: " + appointmentEndTime);
            alert.showAndWait();
        }
    }

    /**
     * sets the Zone id from the system.
     */
    private void setZoneId() {
        locationLabel.setText(ZoneId.systemDefault().toString());
    }

    /**
     * method for language exchange on labels, buttons, and error messages if locale is french.
     *
     * @return
     */
    private Locale getLocaleSetting() {

        Locale locale = Locale.getDefault();

        if (locale == Locale.FRANCE) {
            logInButton.setText("Se connecter");
            userIdTextField.setPromptText("Nom d'utilisateur");
            passwordTextField.setPromptText("Mot de passe");
            welcomeLabel.setText("Bienvenue!");

        }
        return locale;
    }

    /**
     * initializes all the methods.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        setZoneId();
        getLocaleSetting();
        getAllAppointments();

    }
}
