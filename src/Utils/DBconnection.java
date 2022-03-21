package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * class for connecting to database
 */
public class DBconnection {

    private static Connection connection;
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//wgudb.ucertify.com/WJ08vZi";

    private static final String jdbcURL = protocol + vendorName + ipAddress;

    private static final String MYSQLJDBCDRIVER = "com.mysql.jdbc.Driver";
    private static Connection conn = null;

    private static final String userName = "U08vZi";

    private static final String password = "53689409307";

    /**
     * method that starts the connection to database.
     * @return
     */
    public static Connection startConnection() {
        try {
            Class.forName(MYSQLJDBCDRIVER);
            conn = (Connection) DriverManager.getConnection(jdbcURL, userName, password);
            connection = conn;
            System.out.println("Connection Successful");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    /**
     * method that closes the connection.
     * @throws SQLException
     */
    public static void closeConnection() throws SQLException {
        try {
            conn.close();
            System.out.println("close");
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * methdod that returns the connection.
     * @return
     */
    public static Connection getConnection(){
        return connection;
    }
}

