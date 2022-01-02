package controller.tools;

public class Session {

    /*
    connection status
     */
    public static boolean CONNECTION_SESSION;
    public static boolean CONFIGURATION_STATE;
    /*
    user session data 
     */
    public static int USER_ROL;
    public static String USER_ID;
    public static String USER_NAME;
    public static String USER_PUESTO;
   
    /*
    my company session data
     */
    public static String COMPANY_GIRO_COMERCIAL = "";
    public static String COMPANY_REPRESENTANTE = "";
    public static String COMPANY_TELEFONO = "";
    public static String COMPANY_CELULAR = "";
    public static String COMPANY_PAGINAWEB = "";
    public static String COMPANY_EMAIL = "";
    public static String COMPANY_DOMICILIO = "";
    public static String COMPANY_NUMERO_DOCUMENTO = "";
    public static String COMPANY_RAZON_SOCIAL = "";
    public static String COMPANY_NOMBRE_COMERCIAL = "";
    public static byte[] COMPANY_IMAGE = null;

    /*
    impresora session
     */
    public static int TICKET_VENTA_ID = 0;
    public static String TICKET_VENTA_RUTA = "";

    public static int TICKET_CORTE_CAJA_ID = 0;
    public static String TICKET_CORTE_CAJA_RUTA = "";

    public static int TICKET_PRE_VENTA_ID = 0;
    public static String TICKET_PRE_VENTA_RUTA = "";

    public static int TICKET_COTIZACION_ID = 0;
    public static String TICKET_COTIZACION_RUTA = "";
    
    public static int TICKET_PEDIDO_ID = 0;
    public static String TICKET_PEDIDO_RUTA = "";

    public static int TICKET_CUENTA_POR_COBRAR_ID = 0;
    public static String TICKET_CUENTA_POR_COBRAR_RUTA = "";

    public static int TICKET_CUENTA_POR_PAGAR_ID = 0;
    public static String TICKET_CUENTA_POR_PAGAR_RUTA = "";

    public static int TICKET_GUIA_REMISION_ID = 0;
    public static String TICKET_GUIA_REMISION_RUTA = "";

    public static int TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID = 0;
    public static String TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA = "";

    public static boolean ESTADO_IMPRESORA_VENTA;
    public static String NOMBRE_IMPRESORA_VENTA = "";
    public static boolean CORTAPAPEL_IMPRESORA_VENTA;
    public static String FORMATO_IMPRESORA_VENTA = "";
    public static String DESING_IMPRESORA_VENTA = "";

    public static boolean ESTADO_IMPRESORA_PRE_VENTA;
    public static String NOMBRE_IMPRESORA_PRE_VENTA = "";
    public static boolean CORTAPAPEL_IMPRESORA_PRE_VENTA;
    public static String FORMATO_IMPRESORA_PRE_VENTA = "";
    public static String DESING_IMPRESORA_PRE_VENTA = "";

    public static boolean ESTADO_IMPRESORA_COTIZACION;
    public static String NOMBRE_IMPRESORA_COTIZACION = "";
    public static boolean CORTAPAPEL_IMPRESORA_COTIZACION;
    public static String FORMATO_IMPRESORA_COTIZACION = "";
    public static String DESING_IMPRESORA_COTIZACION = "";

    public static boolean ESTADO_IMPRESORA_PEDIDO;
    public static String NOMBRE_IMPRESORA_PEDIDO = "";
    public static boolean CORTAPAPEL_IMPRESORA_PEDIDO;
    public static String FORMATO_IMPRESORA_PEDIDO = "";
    public static String DESING_IMPRESORA_PEDIDO = "";
    
    public static boolean ESTADO_IMPRESORA_CORTE_CAJA;
    public static String NOMBRE_IMPRESORA_CORTE_CAJA = "";
    public static boolean CORTAPAPEL_IMPRESORA_CORTE_CAJA;
    public static String FORMATO_IMPRESORA_CORTE_CAJA = "";
    public static String DESING_IMPRESORA_CORTE_CAJA = "";

    public static boolean ESTADO_IMPRESORA_CUENTA_POR_COBRAR;
    public static String NOMBRE_IMPRESORA_CUENTA_POR_COBRAR = "";
    public static boolean CORTAPAPEL_IMPRESORA_CUENTA_POR_COBRAR;
    public static String FORMATO_IMPRESORA_CUENTA_POR_COBRAR = "";
    public static String DESING_IMPRESORA_CUENTA_POR_COBRAR = "";

    public static boolean ESTADO_IMPRESORA_CUENTA_POR_PAGAR;
    public static String NOMBRE_IMPRESORA_CUENTA_POR_PAGAR = "";
    public static boolean CORTAPAPEL_IMPRESORA_CUENTA_POR_PAGAR;
    public static String FORMATO_IMPRESORA_CUENTA_POR_PAGAR = "";
    public static String DESING_IMPRESORA_CUENTA_POR_PAGAR = "";

    public static boolean ESTADO_IMPRESORA_GUIA_REMISION;
    public static String NOMBRE_IMPRESORA_GUIA_REMISION = "";
    public static boolean CORTAPAPEL_IMPRESORA_GUIA_REMISION;
    public static String FORMATO_IMPRESORA_GUIA_REMISION = "";
    public static String DESING_IMPRESORA_GUIA_REMISION = "";

    public static boolean ESTADO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS;
    public static String NOMBRE_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = "";
    public static boolean CORTAPAPEL_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS;
    public static String FORMATO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = "";
    public static String DESING_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = "";

    /*
    variable de cliente
     */
    public static String CLIENTE_ID;
    public static int CLIENTE_TIPO_DOCUMENTO;
    public static String CLIENTE_DATOS;
    public static String CLIENTE_NUMERO_DOCUMENTO;
    public static String CLIENTE_CELULAR;
    public static String CLIENTE_EMAIL;
    public static String CLIENTE_DIRECCION;

    /*
    varialble de moneda
     */
    public static int MONEDA_ID;
    public static String MONEDA_NOMBRE;
    public static String MONEDA_SIMBOLO;

    /*
    variables de caja/banco
     */
    public static String CAJA_ID;

    public static String BANCO_EFECTIVO_ID;
    public static String BANCO_EFECTIVO_NOMBRE;

    public static String BANCO_TARJETA_ID;
    public static String BANCO_TARJETA_NOMBRE;
}
