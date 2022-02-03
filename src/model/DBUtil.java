package model;

import controller.tools.ObjectGlobal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    private static Connection connection = null;
    private static final String ADDRES = ObjectGlobal.ADDRES;
    private static final String PORT = ObjectGlobal.PORT;
    private static final String DATABASENAME = ObjectGlobal.DATABASENAME;
    private static final String USER = ObjectGlobal.USER;
    private static final String PASSWORD = ObjectGlobal.PASSWORD;//Qz0966lb
    private static final String URL = "jdbc:sqlserver://" + ADDRES + ":" + PORT + ";databaseName=" + DATABASENAME + "";
    ///private static final String URL = "jdbc:mysql://"+ ADDRES +":"+PORT+"/"+DATABASENAME+"";

    public static String dbConnect() {
        try {
            if (connection == null) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                //Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } else if (connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                //Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
            return "ok";
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        }
    }

    public static String validateConnect(String addres, String port, String database, String user, String password) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection valid = DriverManager.getConnection("jdbc:sqlserver://" + addres + ":" + port + ";databaseName=" + database, user, password);
            valid.close();
            return "ok";
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        }

    }

    public static void dbDisconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
//                connection.close();               
            }
        } catch (SQLException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public static Connection getConnection() {
        return connection;
    }

}
