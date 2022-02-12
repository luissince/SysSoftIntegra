package controller.tools.modelticket;

import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
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
import model.CompraADO;
import model.CompraCreditoTB;
import model.CompraTB;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketCuentasPorPagar {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketCuentasPorPagar(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir(String idCompra, String idCompraCredito) {
        if (!Session.ESTADO_IMPRESORA_CUENTA_POR_PAGAR && Tools.isText(Session.NOMBRE_IMPRESORA_CUENTA_POR_PAGAR) && Tools.isText(Session.FORMATO_IMPRESORA_CUENTA_POR_PAGAR)) {
            Tools.AlertMessageWarning(node, "Abono", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }

        if (Session.FORMATO_IMPRESORA_CUENTA_POR_PAGAR.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_CUENTA_POR_PAGAR_ID == 0 && Session.TICKET_CUENTA_POR_PAGAR_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Abono", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterCuentaPorPagar(
                        idCompra,
                        idCompraCredito,
                        Session.DESING_IMPRESORA_CUENTA_POR_PAGAR,
                        Session.TICKET_CUENTA_POR_PAGAR_ID,
                        Session.TICKET_CUENTA_POR_PAGAR_RUTA,
                        Session.NOMBRE_IMPRESORA_CUENTA_POR_PAGAR,
                        Session.CORTAPAPEL_IMPRESORA_CUENTA_POR_PAGAR
                );
            }
        } else if (Session.FORMATO_IMPRESORA_CUENTA_POR_PAGAR.equalsIgnoreCase("a4")) {

        } else {
            Tools.AlertMessageWarning(node, "Abono", "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterCuentaPorPagar(String idCompra, String idCompraCredito, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                if (!Tools.isText(idCompra) && !Tools.isText(idCompraCredito)) {
                    Object object = CompraADO.ImprimirCompraCreditoById(idCompra, idCompraCredito);
                    if (object instanceof CompraCreditoTB) {
                        CompraCreditoTB compraCreditoTB = (CompraCreditoTB) object;
                        try {
                            if (desing.equalsIgnoreCase("withdesing")) {
                                return printTicketWithDesingCuentaPagarUnico(compraCreditoTB, ticketId, ticketRuta, nombreImpresora, cortaPapel);
                            } else {
                                return "empty";
                            }
                        } catch (PrinterException | IOException | PrintException ex) {
                            return "Error en imprimir: " + ex.getLocalizedMessage();
                        }
                    } else {
                        return (String) object;
                    }
                } else {
                    Object object = CompraADO.Obtener_Compra_ById_For_Credito(idCompra);
                    if (object instanceof CompraTB) {
                        CompraTB compraTB = (CompraTB) object;
                        try {
                            if (desing.equalsIgnoreCase("withdesing")) {
                                return printTicketWithDesingCuentaPagar(compraTB, ticketId, ticketRuta, nombreImpresora, cortaPapel);
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
                        Tools.newLineString("Se producto un problema de la impresora: " + result),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });
        task.setOnFailed(w -> {
            Tools.showAlertNotification("/view/image/error_large.png",
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

    private String printTicketWithDesingCuentaPagarUnico(CompraCreditoTB compraCreditoTB, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "AMORTIZAR",
                    compraCreditoTB.getIdCompraCredito(),
                    compraCreditoTB.getCompraTB().getProveedorTB().getNumeroDocumento(),
                    compraCreditoTB.getCompraTB().getProveedorTB().getRazonSocial(),
                    compraCreditoTB.getCompraTB().getProveedorTB().getCelular(),
                    compraCreditoTB.getCompraTB().getProveedorTB().getDireccion(),
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
                    "",
                    "",
                    "",
                    Tools.roundingValue(compraCreditoTB.getCompraTB().getMontoTotal(), 2),
                    Tools.roundingValue(compraCreditoTB.getCompraTB().getMontoPagado(), 2),
                    Tools.roundingValue(compraCreditoTB.getCompraTB().getMontoRestante(), 2),
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
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        AnchorPane hbDetalle = new AnchorPane();
        ObservableList<CompraCreditoTB> arrList = FXCollections.observableArrayList(compraCreditoTB);
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleCuentaPagar(hBox, box, arrList, m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        for (int i = 0; i < hbPie.getChildren().size(); i++) {
            HBox box = ((HBox) hbPie.getChildren().get(i));

            billPrintable.hbPie(box,
                    "M",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    compraCreditoTB.getCompraTB().getProveedorTB().getNumeroDocumento(),
                    compraCreditoTB.getCompraTB().getProveedorTB().getRazonSocial(),
                    "",
                    compraCreditoTB.getCompraTB().getProveedorTB().getCelular(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        billPrintable.generateTicketPrint(hbEncabezado, hbDetalle, hbPie);

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

    private String printTicketWithDesingCuentaPagar(CompraTB compraTB, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "AMORTIZAR",
                    "CP",
                    compraTB.getProveedorTB().getNumeroDocumento(),
                    compraTB.getProveedorTB().getRazonSocial(),
                    compraTB.getProveedorTB().getCelular(),
                    compraTB.getProveedorTB().getDireccion(),
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
                    "",
                    "",
                    "",
                    Tools.roundingValue(compraTB.getMontoTotal(), 2),
                    Tools.roundingValue(compraTB.getMontoPagado(), 2),
                    Tools.roundingValue(compraTB.getMontoRestante(), 2),
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
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        AnchorPane hbDetalle = new AnchorPane();
        ObservableList<CompraCreditoTB> arrList = FXCollections.observableArrayList(compraTB.getCompraCreditoTBs());
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleCuentaPagar(hBox, box, arrList, m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        for (int i = 0; i < hbPie.getChildren().size(); i++) {
            HBox box = ((HBox) hbPie.getChildren().get(i));

            billPrintable.hbPie(box,
                    "M",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    compraTB.getProveedorTB().getNumeroDocumento(),
                    compraTB.getProveedorTB().getRazonSocial(),
                    "",
                    compraTB.getProveedorTB().getCelular(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        billPrintable.generateTicketPrint(hbEncabezado, hbDetalle, hbPie);

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

    public void mostrarReporte(String idCompra, String idCompraCredito) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                Object object = CompraADO.Obtener_Compra_ById_For_Credito(idCompra);
                if (object instanceof CompraTB) {
                    try {
                        CompraTB compraTB = (CompraTB) object;

                        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_ICON);
                        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

                        if (Session.COMPANY_IMAGE != null) {
                            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                        }
                        InputStream dir = getClass().getResourceAsStream("/report/CuentasPorPagarDetalle.jasper");

                        Map map = new HashMap();
                        map.put("LOGO", imgInputStream);
                        map.put("ICON", imgInputStreamIcon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("EMAIL", Session.COMPANY_EMAIL);
                        map.put("TELEFONOCELULAR", Session.COMPANY_TELEFONO + " - " + Session.COMPANY_CELULAR);
                        map.put("DOCUMENTOEMPRESA", "R.U.C " + Session.COMPANY_NUMERO_DOCUMENTO);
                        map.put("CLIENTE", compraTB.getProveedorTB().getRazonSocial());
                        map.put("MONTO_TOTAL", Tools.roundingValue(compraTB.getMontoTotal(), 2));
                        map.put("MONTO_PAGADO", Tools.roundingValue(compraTB.getMontoPagado(), 2));
                        map.put("MONTO_RESTANTE", Tools.roundingValue(compraTB.getMontoRestante(), 2));

                        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, compraTB.getCompraCreditoTBs().isEmpty() ? new JREmptyDataSource() : new JRBeanCollectionDataSource(compraTB.getCompraCreditoTBs()));

                        return jasperPrint;
                    } catch (JRException ex) {
                        return ex.getLocalizedMessage();
                    }
                } else {
                    return (String) object;
                }
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
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Generando reporte",
                    Tools.newLineString("Se produjo un problema al generar."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });
        task.setOnSucceeded(w -> {
            try {
                Object result = task.getValue();
                if (result instanceof JasperPrint) {
                    printA4WithDesingCuentaPorPagar((JasperPrint) result);
                    Tools.showAlertNotification("/view/image/succes_large.png",
                            "Generando reporte",
                            Tools.newLineString("Se generó correctamente el reporte."),
                            Duration.seconds(5),
                            Pos.BOTTOM_RIGHT);
                } else {
                    Tools.showAlertNotification("/view/image/warning_large.png",
                            "Generando reporte",
                            Tools.newLineString((String) result),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);
                }
            } catch (IOException ex) {
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

    private void printA4WithDesingCuentaPorPagar(JasperPrint jasperPrint) throws IOException {
        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxReportViewController controller = fXMLLoader.getController();
        controller.setJasperPrint(jasperPrint);
        controller.show();
        Stage stage = WindowStage.StageLoader(parent, "Cuenta Por Pagar");
        stage.setResizable(true);
        stage.show();
        stage.requestFocus();
    }

//    private String printTicketNoDesingCuentaPagarUnico(CompraCreditoTB compraCreditoTB, String nombreImpresora, boolean cortaPapel) {
//        ArrayList<HBox> object = new ArrayList<>();
//        int rows = 0;
//        int lines = 0;
//        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
//            object.add((HBox) hbEncabezado.getChildren().get(i));
//            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
//            rows++;
//            lines += billPrintable.hbEncebezado(box,
//                    "AMORTIZAR",
//                    compraCreditoTB.getIdCompraCredito(),
//                    compraCreditoTB.getProveedorTB().getNumeroDocumento(),
//                    compraCreditoTB.getProveedorTB().getRazonSocial(),
//                    compraCreditoTB.getProveedorTB().getCelular(),
//                    compraCreditoTB.getProveedorTB().getDireccion(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "0",
//                    "0",
//                    "0",
//                    "",
//                    "0",
//                    "0",
//                    "0",
//                    "0",
//                    "0");
//        }
//
//        ObservableList<CompraCreditoTB> arrList = FXCollections.observableArrayList(compraCreditoTB);
//        for (int m = 0; m < arrList.size(); m++) {
//            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
//                HBox hBox = new HBox();
//                hBox.setId("dc_" + m + "" + i);
//                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
//                rows++;
//                lines += billPrintable.hbDetalleCuentaPagar(hBox, box, arrList, m);
//                object.add(hBox);
//            }
//        }
//
//        for (int i = 0; i < hbPie.getChildren().size(); i++) {
//            object.add((HBox) hbPie.getChildren().get(i));
//            HBox box = ((HBox) hbPie.getChildren().get(i));
//            rows++;
//            lines += billPrintable.hbPie(box,
//                    "M",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    compraCreditoTB.getProveedorTB().getNumeroDocumento(),
//                    compraCreditoTB.getProveedorTB().getRazonSocial(),
//                    "",
//                    compraCreditoTB.getProveedorTB().getCelular(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "");
//        }
//        return billPrintable.modelTicket(rows + lines + 1 + 10, lines, object, nombreImpresora, cortaPapel);
//    }
//    private String printTicketNoDesingCuentaPagar(CompraTB compraTB, String nombreImpresora, boolean cortaPapel) {
//        ArrayList<HBox> object = new ArrayList<>();
//        int rows = 0;
//        int lines = 0;
//        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
//            object.add((HBox) hbEncabezado.getChildren().get(i));
//            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
//            rows++;
//            lines += billPrintable.hbEncebezado(box,
//                    "AMORTIZAR",
//                    "CP",
//                    compraTB.getProveedorTB().getNumeroDocumento(),
//                    compraTB.getProveedorTB().getRazonSocial(),
//                    compraTB.getProveedorTB().getCelular(),
//                    compraTB.getProveedorTB().getDireccion(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "0",
//                    "0",
//                    "0",
//                    "",
//                    "0",
//                    "0",
//                    "0",
//                    "0",
//                    "0");
//        }
//
//        ObservableList<CompraCreditoTB> arrList = FXCollections.observableArrayList(compraTB.getCompraCreditoTBs());
//        for (int m = 0; m < arrList.size(); m++) {
//            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
//                HBox hBox = new HBox();
//                hBox.setId("dc_" + m + "" + i);
//                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
//                rows++;
//                lines += billPrintable.hbDetalleCuentaPagar(hBox, box, arrList, m);
//                object.add(hBox);
//            }
//        }
//
//        for (int i = 0; i < hbPie.getChildren().size(); i++) {
//            object.add((HBox) hbPie.getChildren().get(i));
//            HBox box = ((HBox) hbPie.getChildren().get(i));
//            rows++;
//            lines += billPrintable.hbPie(box,
//                    "M",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    "0.00",
//                    compraTB.getProveedorTB().getNumeroDocumento(),
//                    compraTB.getProveedorTB().getRazonSocial(),
//                    "",
//                    compraTB.getProveedorTB().getCelular(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "");
//        }
//        return billPrintable.modelTicket(rows + lines + 1 + 10, lines, object, nombreImpresora, cortaPapel);
//    }
}
