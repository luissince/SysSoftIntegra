package service;

import controller.tools.ObjectGlobal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private Connection connection = null;
    private final String ADDRES = ObjectGlobal.ADDRES;
    private final String PORT = ObjectGlobal.PORT;
    private final String DATABASENAME = ObjectGlobal.DATABASENAME;
    private final String USER = ObjectGlobal.USER;
    private final String PASSWORD = ObjectGlobal.PASSWORD;// Qz0966lb
    private final String URL = "jdbc:sqlserver://" + ADDRES + ":" + PORT + ";databaseName=" + DATABASENAME + "";
    /// private static final String URL = "jdbc:mysql://"+ ADDRES
    /// +":"+PORT+"/"+DATABASENAME+"";

    public DBUtil() {

    }

    public void dbConnect() throws SQLException, ClassNotFoundException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        // Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String validateConnect(String addres, String port, String database, String user, String password) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection valid = DriverManager.getConnection(
                    "jdbc:sqlserver://" + addres + ":" + port + ";databaseName=" + database, user, password);
            valid.close();
            return "ok";
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void dbDisconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

}
