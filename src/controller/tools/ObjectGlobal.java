package controller.tools;

import java.util.ArrayList;
import javafx.scene.layout.AnchorPane;

public class ObjectGlobal {

    /*
    Objecto de conexion
     */
    public static String ADDRES;
    public static String PORT;
    public static String DATABASENAME;
    public static String USER;
    public static String PASSWORD;
    /*
     */
    public static AnchorPane PANE_PRINCIPAL;

    public static String QR_PERU_DATA = "|0|0|0|0|0|0|0|0|";

    public static ArrayList<String> DATA_CLIENTS = new ArrayList<>();

    static {
        PANE_PRINCIPAL = new AnchorPane();
    }

   
}
