package controller.tools.modelticket;

import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.print.Book;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import model.CotizacionADO;
import model.CotizacionTB;
import model.SuministroTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketCotizacion {

    private final Node node;

    private final BillPrintable billPrintable;

    private final ConvertMonedaCadena monedaCadena;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketCotizacion(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idCotizacion) {
        if (!Session.ESTADO_IMPRESORA_COTIZACION && Tools.isText(Session.NOMBRE_IMPRESORA_COTIZACION) && Tools.isText(Session.FORMATO_IMPRESORA_COTIZACION)) {
            Tools.AlertMessageWarning(node, "Cotización", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_COTIZACION.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_COTIZACION_ID == 0 && Session.TICKET_COTIZACION_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Cotización", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessCotizacionTicket(
                        idCotizacion,
                        Session.DESING_IMPRESORA_COTIZACION,
                        Session.TICKET_COTIZACION_ID,
                        Session.TICKET_COTIZACION_RUTA,
                        Session.NOMBRE_IMPRESORA_COTIZACION,
                        Session.CORTAPAPEL_IMPRESORA_COTIZACION
                );
            }
        } else {
            Tools.AlertMessageWarning(node, "Cotización", "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessCotizacionTicket(String idCotizacion, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = CotizacionADO.CargarCotizacionById(idCotizacion);
                if (object instanceof CotizacionTB) {
                    try {
                        CotizacionTB cotizacionTB = (CotizacionTB) object;
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingCotizacion(cotizacionTB, ticketId, ticketRuta, nombreImpresora, cortaPapel);
                        } else {
                            return "empty";
                        }
                    } catch (PrinterException | IOException | PrintException ex) {
                        return "Error en imprimir: " + ex.getLocalizedMessage();
                    }
                } else {
                    return (String) object;
                }
            }
        };

        task.setOnSucceeded(w -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("completed")) {
                Tools.showAlertNotification("/view/image/information_large.png",
                        "Envío de impresión",
                        Tools.newLineString("Se completo el proceso de impresión correctamente."),
                        Duration.seconds(5),
                        Pos.BOTTOM_RIGHT);
            } else if (result.equalsIgnoreCase("error_name")) {
                Tools.showAlertNotification("/view/image/warning_large.png",
                        "Envío de impresión",
                        Tools.newLineString("Error en encontrar el nombre de la impresión por problemas de puerto o driver."),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else if (result.equalsIgnoreCase("empty")) {
                Tools.showAlertNotification("/view/image/warning_large.png",
                        "Envío de impresión",
                        Tools.newLineString("No hay registros para mostrar en el reporte."),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else {
                Tools.showAlertNotification("/view/image/error_large.png",
                        "Envío de impresión",
                        Tools.newLineString("Se producto un problema de la impresora " + result),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });
        task.setOnFailed(w -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Envío de impresión",
                    Tools.newLineString("Se produjo un problema en el proceso de envío, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification("/view/image/print.png",
                    "Envío de impresión",
                    Tools.newLineString("Se envió la impresión a la cola, este proceso puede tomar unos segundos."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private String printTicketWithDesingCotizacion(CotizacionTB cotizacionTB, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "COTIZACIÓN",
                    "N° " + cotizacionTB.getIdCotizacion(),
                    cotizacionTB.getClienteTB().getNumeroDocumento(),
                    cotizacionTB.getClienteTB().getInformacion(),
                    cotizacionTB.getClienteTB().getCelular(),
                    cotizacionTB.getClienteTB().getDireccion(),
                    "CODIGO PROCESO",
                    "IMPORTE EN LETRAS",
                    cotizacionTB.getFechaCotizacion(),
                    cotizacionTB.getHoraCotizacion(),
                    cotizacionTB.getFechaCotizacion(),
                    cotizacionTB.getHoraCotizacion(),
                    "0",
                    "0",
                    "0",
                    "-",
                    cotizacionTB.getEmpleadoTB().getApellidos() + " " + cotizacionTB.getEmpleadoTB().getNombres(),
                    "-",
                    "-",
                    "",
                    "",
                    "",
                    "",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        AnchorPane hbDetalle = new AnchorPane();
        ObservableList<SuministroTB> arrList = FXCollections.observableArrayList(cotizacionTB.getDetalleSuministroTBs());
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleCotizacion(hBox, box, arrList, m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        double totalBruto = 0;
        double totalDescuento = 0;
        double totalSubTotal = 0;
        double totalImpuesto = 0;
        double totalNeto = 0;

        for (SuministroTB suministroTB : arrList) {
            double descuento = suministroTB.getDescuento();
            double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
            double subPrecio = Tools.calculateTaxBruto(suministroTB.getImpuestoValor(), precioDescuento);
            double precioBruto = subPrecio + descuento;
            totalBruto += precioBruto * suministroTB.getCantidad();
            totalDescuento += suministroTB.getCantidad() * descuento;
            totalSubTotal += suministroTB.getCantidad() * subPrecio;
            double impuesto = Tools.calculateTax(suministroTB.getImpuestoValor(), subPrecio);
            totalImpuesto += suministroTB.getCantidad() * impuesto;
            totalNeto = totalSubTotal + totalImpuesto;
        }

        for (int i = 0; i < hbPie.getChildren().size(); i++) {
            HBox box = ((HBox) hbPie.getChildren().get(i));
            billPrintable.hbPie(box,
                    cotizacionTB.getMonedaTB().getSimbolo(),
                    Tools.roundingValue(totalBruto, 2),
                    Tools.roundingValue(totalDescuento, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalImpuesto, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalNeto, 2),
                    "TARJETA",
                    "EFECTIVO",
                    "VUELTO",
                    cotizacionTB.getClienteTB().getNumeroDocumento(),
                    cotizacionTB.getClienteTB().getInformacion(),
                    "CODIGO VENTA",
                    cotizacionTB.getClienteTB().getCelular(),
                    "IMPORTE EN LETRAS",
                    "DOCUMENTO EMPLEADO",
                    cotizacionTB.getEmpleadoTB().getApellidos() + " " + cotizacionTB.getEmpleadoTB().getNombres(),
                    "CELULAR EMPLEADO",
                    "DIRECCION EMPLEADO",
                    cotizacionTB.getObservaciones());
        }

        billPrintable.generatePDFPrint(hbEncabezado, hbDetalle, hbPie);
        PrintService printService = billPrintable.findPrintService(nombreImpresora, PrinterJob.lookupPrintServices());
        if (printService != null) {
            DocPrintJob job = printService.createPrintJob();
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setPrintService(job.getPrintService());
            pj.setJobName(nombreImpresora);
            Book book = new Book();
            book.append(billPrintable, billPrintable.getPageFormat(pj));
            pj.setPageable(book);
            pj.print();
            if (cortaPapel) {
                billPrintable.printCortarPapel(nombreImpresora);
            }
            return "completed";
        } else {
            return "error_name";
        }
    }

    public void mostrarReporte(String idCotizacion) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CotizacionADO.CargarCotizacionById(idCotizacion);
            }
        };
        task.setOnScheduled(w -> {
            Tools.showAlertNotification("/view/image/information_large.png",
                    "Generando reporte",
                    Tools.newLineString("Se envió los datos para generar el reporte."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        task.setOnFailed(w -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Generando reporte",
                    Tools.newLineString("Se produjo un problema al generar."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });
        task.setOnSucceeded(w -> {
            try {
                Object object = task.getValue();
                if (object instanceof CotizacionTB) {
                    CotizacionTB cotizacionTB = (CotizacionTB) object;
                    printA4WithDesingCotizacion(cotizacionTB);
                    Tools.showAlertNotification("/view/image/succes_large.png",
                            "Generando reporte",
                            Tools.newLineString("Se genero correctamente el reporte."),
                            Duration.seconds(5),
                            Pos.BOTTOM_RIGHT);

                } else {
                    Tools.showAlertNotification("/view/image/error_large.png",
                            "Generando reporte",
                            Tools.newLineString((String) object),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);
                }
            } catch (IOException | JRException ex) {
                Tools.showAlertNotification("/view/image/error_large.png",
                        "Generando reporte",
                        Tools.newLineString("Error en mostrar el contenido: " + ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void printA4WithDesingCotizacion(CotizacionTB cotizacionTB) throws JRException, IOException {
        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        if (Session.COMPANY_IMAGE != null) {
            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
        }

        InputStream dir = getClass().getResourceAsStream("/report/Cotizacion.jasper");

        Map map = new HashMap();
        map.put("LOGO", imgInputStream);
        map.put("ICON", imgInputStreamIcon);
        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
        map.put("TELEFONOCELULAR", "TELÉFONO: " + Session.COMPANY_TELEFONO + " CELULAR: " + Session.COMPANY_CELULAR);
        map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);

        map.put("DOCUMENTOEMPRESA", "R.U.C " + Session.COMPANY_NUMERO_DOCUMENTO);
        map.put("NOMBREDOCUMENTO", "COTIZACIÓN");
        map.put("NUMERODOCUMENTO", "N° " + cotizacionTB.getIdCotizacion());

        map.put("DATOSCLIENTE", cotizacionTB.getClienteTB().getInformacion());
        map.put("DOCUMENTOCLIENTE", "");
        map.put("NUMERODOCUMENTOCLIENTE", cotizacionTB.getClienteTB().getNumeroDocumento());
        map.put("CELULARCLIENTE", cotizacionTB.getClienteTB().getCelular());
        map.put("EMAILCLIENTE", cotizacionTB.getClienteTB().getEmail());
        map.put("DIRECCIONCLIENTE", cotizacionTB.getClienteTB().getDireccion());

        map.put("FECHAEMISION", cotizacionTB.getFechaCotizacion());
        map.put("MONEDA", cotizacionTB.getMonedaTB().getNombre());
        map.put("CONDICIONPAGO", "");

        map.put("SIMBOLO", cotizacionTB.getMonedaTB().getSimbolo());
        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(cotizacionTB.getImporteNeto(), 2), true, cotizacionTB.getMonedaTB().getNombre()));

        map.put("VALOR_VENTA", Tools.roundingValue(cotizacionTB.getImporteBruto(), 2));
        map.put("DESCUENTO", Tools.roundingValue(cotizacionTB.getDescuento(), 2));
        map.put("SUB_IMPORTE", Tools.roundingValue(cotizacionTB.getSubImporteNeto(), 2));
        map.put("IMPUESTO_TOTAL", Tools.roundingValue(cotizacionTB.getImpuesto(), 2));
        map.put("IMPORTE_TOTAL", Tools.roundingValue(cotizacionTB.getImporteNeto(), 2));
        map.put("OBSERVACION", cotizacionTB.getObservaciones());

        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(cotizacionTB.getDetalleSuministroTBs()));

        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxReportViewController controller = fXMLLoader.getController();
        controller.setFileName("COTIZACION N° " + cotizacionTB.getIdCotizacion());
        controller.setJasperPrint(jasperPrint);
        controller.show();
        Stage stage = WindowStage.StageLoader(parent, "Cotizacion");
        stage.setResizable(true);
        stage.show();
        stage.requestFocus();
    }

}
