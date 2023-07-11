package controller.tools;

import java.util.ArrayList;

import javafx.scene.layout.AnchorPane;
import model.ClienteTB;

public class ObjectGlobal {

   
    public static String ADDRES;

    public static String PORT;

    public static String DATABASENAME;

    public static String USER;

    public static String PASSWORD;

   
    public static AnchorPane PANE_PRINCIPAL;

   
    public static String QR_PERU_DATA = "|0|0|0|0|0|0|0|0|";

   
    public static ArrayList<String> DATA_CLIENTS = new ArrayList<>();

  
    public static ArrayList<ClienteTB> DATA_INFO_CLIENTS = new ArrayList<>();
    
   
    public static String ID_CONCEPTO_VENTA = "CP0001";

   
    public static String ID_CONCEPTO_COMPRA = "CP0002";

    
    public static String ID_CONCEPTO_COBRO_CREDITO = "CP0003";
    
    
    public static String ID_CONCEPTO_PAGO_CREDITO = "CP0004";

    
    static {
        PANE_PRINCIPAL = new AnchorPane();
    }

}
