package Utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * class for executing sql statements
 */
public class DBquery {

    private static Statement statement;


    /**
     * creates a statement.
     * @param con
     * @throws SQLException
     */

    public static void setStatement(Connection con) throws SQLException {
        statement = con.createStatement();
    }

    /**
     * return statement object.
     * @return
     */
    public static Statement getStatement() {
        return statement;
    }
}
