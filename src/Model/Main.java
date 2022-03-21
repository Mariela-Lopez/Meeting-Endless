package Model;

import Utils.DBconnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * main class
 */
public class Main extends Application {

    /**
     * launches the login screen.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("../Views/LogIn.fxml"));
        root.setStyle("-fx-font-family: sans-serif");
        primaryStage.setTitle("Scheduling System");
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.show();
    }

    /**
     * establishes connection to database and closes connection.
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        DBconnection.startConnection();

        launch(args);
        DBconnection.closeConnection();
    }
}


