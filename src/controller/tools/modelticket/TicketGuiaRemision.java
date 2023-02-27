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
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;

import model.GuiaRemisionDetalleTB;
import model.GuiaRemisionTB;
import model.VentaTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import service.GuiaRemisionADO;
import service.VentaADO;

public class TicketGuiaRemision {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketGuiaRemision(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado,
            AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir(String idGuiaRemision) {
        if (!Session.ESTADO_IMPRESORA_GUIA_REMISION && Tools.isText(Session.NOMBRE_IMPRESORA_GUIA_REMISION)
                && Tools.isText(Session.FORMATO_IMPRESORA_GUIA_REMISION)) {
            Tools.AlertMessageWarning(node, "Guía de Remisión",
                    "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_GUIA_REMISION.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_GUIA_REMISION_ID == 0 && Session.TICKET_GUIA_REMISION_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Guía de Remisión",
                        "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterGuiaRemision(
                        idGuiaRemision,
                        Session.DESING_IMPRESORA_GUIA_REMISION,
                        Session.TICKET_GUIA_REMISION_ID,
                        Session.TICKET_GUIA_REMISION_RUTA,
                        Session.NOMBRE_IMPRESORA_GUIA_REMISION,
                        Session.CORTAPAPEL_IMPRESORA_GUIA_REMISION,
                        Session.FORMATO_IMPRESORA_GUIA_REMISION);
            }
        } else if (Session.FORMATO_IMPRESORA_GUIA_REMISION.equalsIgnoreCase("a4")) {
            executeProcessPrinterGuiaRemision(
                    idGuiaRemision,
                    Session.DESING_IMPRESORA_GUIA_REMISION,
                    Session.TICKET_GUIA_REMISION_ID,
                    Session.TICKET_GUIA_REMISION_RUTA,
                    Session.NOMBRE_IMPRESORA_GUIA_REMISION,
                    Session.CORTAPAPEL_IMPRESORA_GUIA_REMISION,
                    Session.FORMATO_IMPRESORA_GUIA_REMISION);
        } else {
            Tools.AlertMessageWarning(node, "Guía de Remisión",
                    "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterGuiaRemision(String idGuiaRemision, String desing, int ticketId,
            String ticketRuta, String nombreImpresora, boolean cortaPapel, String format) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = GuiaRemisionADO.ObtenerGuiaRemisionById(idGuiaRemision);
                if (object instanceof GuiaRemisionTB) {
                    try {
                        GuiaRemisionTB guiaRemisionTB = (GuiaRemisionTB) object;
                        if (format.equalsIgnoreCase("a4")) {

                            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
                            printRequestAttributeSet.add(new Copies(1));

                            PrinterName pn = new PrinterName(nombreImpresora, null);

                            PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
                            printServiceAttributeSet.add(pn);

                            JRPrintServiceExporter exporter = new JRPrintServiceExporter();

                            exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportJasper(guiaRemisionTB));
                            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                                    printRequestAttributeSet);
                            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                                    printServiceAttributeSet);
                            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
                            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
                            exporter.exportReport();
                            return "completed";

                        } else {
                            if (desing.equalsIgnoreCase("withdesing")) {
                                return printTicketWithDesingGuiaRemision(guiaRemisionTB, ticketId, ticketRuta,
                                        nombreImpresora, cortaPapel);
                            } else {
                                return "empty";
                                // billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado,
                                // hbDetalleCabecera, hbPie);
                                // return printTicketNoDesingCorteCaja(cajaTB, (ArrayList<Double>) object[1],
                                // nombreImpresora, cortaPapel);
                            }
                        }
                    } catch (PrinterException | IOException | PrintException | JRException ex) {
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
                        Tools.newLineString(
                                "Error en encontrar el nombre de la impresión por problemas de puerto o driver."),
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
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Envío de impresión",
                    Tools.newLineString(
                            "Se produjo un problema en el proceso de envío, intente nuevamente o comuníquese con su proveedor del sistema."),
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

    private String printTicketWithDesingGuiaRemision(GuiaRemisionTB guiaRemisionTB, int ticketId, String ticketRuta,
            String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "GUÍA DE REMISIÓN",
                    guiaRemisionTB.getSerie() + "-" + guiaRemisionTB.getNumeracion(),
                    guiaRemisionTB.getClienteTB().getNumeroDocumento(),
                    guiaRemisionTB.getClienteTB().getInformacion(),
                    guiaRemisionTB.getClienteTB().getCelular(),
                    guiaRemisionTB.getClienteTB().getDireccion(),
                    "",
                    "",
                    guiaRemisionTB.getFechaTraslado(),
                    guiaRemisionTB.getHoraTraslado(),
                    guiaRemisionTB.getFechaTraslado(),
                    guiaRemisionTB.getHoraTraslado(),
                    "0",
                    "0",
                    "0",
                    guiaRemisionTB.getEmpleadoTB().getNumeroDocumento(),
                    guiaRemisionTB.getEmpleadoTB().getApellidos(),
                    guiaRemisionTB.getEmpleadoTB().getCelular(),
                    guiaRemisionTB.getEmpleadoTB().getDireccion(),
                    "0",
                    "0",
                    "0",
                    "",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    "0",
                    // guiaRemisionTB.getComprobanteAsociado(),
                    "",
                    // guiaRemisionTB.getSerieFactura() + "-" +
                    // guiaRemisionTB.getNumeracionFactura(),
                    "",
                    guiaRemisionTB.getDireccionPartida(),
                    guiaRemisionTB.getUbigeoPartidaTB().getDepartamento() + " - "
                            + guiaRemisionTB.getUbigeoPartidaTB().getProvincia() + " - "
                            + guiaRemisionTB.getUbigeoPartidaTB().getDistrito(),
                    guiaRemisionTB.getDireccionLlegada(),
                    guiaRemisionTB.getUbigeoLlegadaTB().getDepartamento() + " - "
                            + guiaRemisionTB.getUbigeoLlegadaTB().getProvincia() + " - "
                            + guiaRemisionTB.getUbigeoLlegadaTB().getDistrito(),
                    // guiaRemisionTB.getNumeroDocumentoConductor(),
                    // guiaRemisionTB.getNombresConductor(),
                    // guiaRemisionTB.getNumeroLicencia(),
                    // guiaRemisionTB.getNumeroPlaca(),
                    "",
                    "",
                    "",
                    "",
                    // guiaRemisionTB.getMotivoTrasladoDescripcion(),
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        AnchorPane hbDetalle = new AnchorPane();
        ObservableList<GuiaRemisionDetalleTB> arrList = guiaRemisionTB.getListGuiaRemisionDetalle();
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleGuiaRemision(hBox, box, arrList, m);
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
                    "",
                    "",
                    "",
                    "",
                    "",
                    guiaRemisionTB.getEmpleadoTB().getNumeroDocumento(),
                    guiaRemisionTB.getEmpleadoTB().getApellidos() + " " + guiaRemisionTB.getEmpleadoTB().getNombres(),
                    guiaRemisionTB.getEmpleadoTB().getCelular(),
                    guiaRemisionTB.getEmpleadoTB().getDireccion(),
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

    public void mostrarReporte(String idGuiaRemision) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return GuiaRemisionADO.ObtenerGuiaRemisionById(idGuiaRemision);
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
                if (object instanceof GuiaRemisionTB) {
                    GuiaRemisionTB remisionTB = (GuiaRemisionTB) object;
                    reportGuiaRemision(remisionTB);
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
                            Pos.CENTER);
                }
            } catch (IOException | JRException ex) {
                Tools.showAlertNotification("/view/image/error_large.png",
                        "Generando reporte",
                        Tools.newLineString(ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.CENTER);
            }
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void reportGuiaRemision(GuiaRemisionTB guiaRemisionTB) throws JRException, IOException {
        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxReportViewController controller = fXMLLoader.getController();
        controller.setJasperPrint(reportJasper(guiaRemisionTB));
        controller.show();
        Stage stage = WindowStage.StageLoader(parent, "Guia de remisión");
        stage.setResizable(true);
        stage.show();
        stage.requestFocus();
    }

    private JasperPrint reportJasper(GuiaRemisionTB guiaRemisionTB) throws JRException, IOException {
        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        if (Session.COMPANY_IMAGE != null) {
            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
        }
        InputStream dir = getClass().getResourceAsStream("/report/GuiadeRemision.jasper");
        Map<String, Object> map = new HashMap<>();
        map.put("LOGO", imgInputStream);
        map.put("ICON", imgInputStreamIcon);
        map.put("RUC_EMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);
        map.put("NOMBRE_EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DIRECCION_EMPRESA", Session.COMPANY_DOMICILIO);
        map.put("TELEFONO_EMPRESA", Session.COMPANY_TELEFONO);
        map.put("CELULAR_EMPRESA", Session.COMPANY_CELULAR);
        map.put("EMAIL_EMPRESA", Session.COMPANY_EMAIL);

        map.put("NUMERACION_GUIA_REMISION", guiaRemisionTB.getSerie() + "-" + guiaRemisionTB.getNumeracion());
        map.put("FECHA_TRASLADO", guiaRemisionTB.getFechaTraslado());
        // map.put("NUMERO_FACTURA", guiaRemisionTB.getSerieFactura() + "-" +
        // guiaRemisionTB.getNumeracionFactura());
        map.put("NUMERO_FACTURA", "");

        map.put("DIRECCION_PARTIDA", guiaRemisionTB.getDireccionPartida());
        map.put("UBIGEO_PARTIDA",
                guiaRemisionTB.getUbigeoPartidaTB().getDepartamento() + " - "
                        + guiaRemisionTB.getUbigeoPartidaTB().getProvincia() + " - "
                        + guiaRemisionTB.getUbigeoPartidaTB().getDistrito());

        map.put("DIRECCION_LLEGAGA", guiaRemisionTB.getDireccionLlegada());
        map.put("UBIGEO_LLEGADA",
                guiaRemisionTB.getUbigeoLlegadaTB().getDepartamento() + " - "
                        + guiaRemisionTB.getUbigeoLlegadaTB().getProvincia() + " - "
                        + guiaRemisionTB.getUbigeoLlegadaTB().getDistrito());

        map.put("NOMBRE_COMERCIAL", Session.COMPANY_NOMBRE_COMERCIAL);
        map.put("RUC_EMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);

        map.put("NOMBRE_CLIENTE", guiaRemisionTB.getClienteTB().getInformacion());
        map.put("RUC_CLIENTE", guiaRemisionTB.getClienteTB().getNumeroDocumento());

        // map.put("DATOS_TRANSPORTISTA", guiaRemisionTB.getNombresConductor());
        // map.put("DOCUMENTO_TRANSPORTISTA",
        // guiaRemisionTB.getNumeroDocumentoConductor());
        // map.put("MARCA_VEHICULO", guiaRemisionTB.getNumeroLicencia());
        // map.put("NUMERO_PLACA", guiaRemisionTB.getNumeroPlaca());

        map.put("DATOS_TRANSPORTISTA", "");
        map.put("DOCUMENTO_TRANSPORTISTA", "");
        map.put("MARCA_VEHICULO", "");
        map.put("NUMERO_PLACA", "");

        // map.put("MOTIVO_TRANSPORTE", guiaRemisionTB.getMotivoTrasladoDescripcion());
        map.put("MOTIVO_TRANSPORTE", "");

        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map,
                new JRBeanCollectionDataSource(guiaRemisionTB.getListGuiaRemisionDetalle()));
        return jasperPrint;
    }

    public void imprimirGuiaLibre(String idVenta) {
        if (!Session.ESTADO_IMPRESORA_GUIA_REMISION && Tools.isText(Session.NOMBRE_IMPRESORA_GUIA_REMISION)
                && Tools.isText(Session.FORMATO_IMPRESORA_GUIA_REMISION)) {
            Tools.AlertMessageWarning(node, "Guía de Remisión",
                    "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_GUIA_REMISION.equalsIgnoreCase("a4")) {
            printGuiaRemisionLibre(idVenta, Session.NOMBRE_IMPRESORA_GUIA_REMISION);
        } else {
            Tools.AlertMessageWarning(node, "Guía de Remisión",
                    "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void printGuiaRemisionLibre(String idVenta, String nombreImpresora) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = VentaADO.Obtener_Venta_ById(idVenta);
                if (object instanceof VentaTB) {
                    try {
                        VentaTB ventaTB = (VentaTB) object;

                        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
                        printRequestAttributeSet.add(new Copies(1));

                        PrinterName pn = new PrinterName(nombreImpresora, null);

                        PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
                        printServiceAttributeSet.add(pn);

                        JRPrintServiceExporter exporter = new JRPrintServiceExporter();

                        exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportLibre(ventaTB));
                        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                                printRequestAttributeSet);
                        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                                printServiceAttributeSet);
                        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
                        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
                        exporter.exportReport();
                        return "completed";

                    } catch (IOException | JRException ex) {
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
                        Tools.newLineString(
                                "Error en encontrar el nombre de la impresión por problemas de puerto o driver."),
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
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Envío de impresión",
                    Tools.newLineString(
                            "Se produjo un problema en el proceso de envío, intente nuevamente o comuníquese con su proveedor del sistema."),
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

    private JasperPrint reportLibre(VentaTB ventaTB) throws JRException, IOException {
        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        if (Session.COMPANY_IMAGE != null) {
            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
        }
        InputStream dir = getClass().getResourceAsStream("/report/GuiadeRemisionLibre.jasper");
        Map<String, Object> map = new HashMap<>();
        map.put("LOGO", imgInputStream);
        map.put("ICON", imgInputStreamIcon);
        map.put("RUC_EMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);
        map.put("NOMBRE_EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DIRECCION_EMPRESA", Session.COMPANY_DOMICILIO);
        map.put("TELEFONO_EMPRESA", Session.COMPANY_TELEFONO);
        map.put("CELULAR_EMPRESA", Session.COMPANY_CELULAR);
        map.put("EMAIL_EMPRESA", Session.COMPANY_EMAIL);

        // map.put("NUMERACION_GUIA_REMISION", guiaRemisionTB.getSerie() + "-" +
        // guiaRemisionTB.getNumeracion());
        // map.put("FECHA_TRASLADO", guiaRemisionTB.getFechaTraslado());
        // map.put("NUMERO_FACTURA", guiaRemisionTB.getSerieFactura() + "-" +
        // guiaRemisionTB.getNumeracionFactura());
        //
        // map.put("DIRECCION_PARTIDA", guiaRemisionTB.getDireccionPartida());
        // map.put("UBIGEO_PARTIDA", guiaRemisionTB.getUbigeoPartidaDescripcion());
        //
        // map.put("DIRECCION_LLEGAGA", guiaRemisionTB.getDireccionLlegada());
        // map.put("UBIGEO_LLEGADA", guiaRemisionTB.getUbigeoLlegadaDescripcion());
        map.put("NOMBRE_COMERCIAL", Session.COMPANY_NOMBRE_COMERCIAL);
        map.put("RUC_EMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);

        // map.put("NOMBRE_CLIENTE", guiaRemisionTB.getClienteTB().getInformacion());
        // map.put("RUC_CLIENTE", guiaRemisionTB.getClienteTB().getNumeroDocumento());
        //
        // map.put("DATOS_TRANSPORTISTA", guiaRemisionTB.getNombreConductor());
        // map.put("DOCUMENTO_TRANSPORTISTA", guiaRemisionTB.getNumeroConductor());
        // map.put("MARCA_VEHICULO", guiaRemisionTB.getMarcaVehiculo());
        // map.put("NUMERO_PLACA", guiaRemisionTB.getNumeroPlaca());
        //
        // map.put("MOTIVO_TRANSPORTE", guiaRemisionTB.getMotivoTrasladoDescripcion());
        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map,
                new JRBeanCollectionDataSource(ventaTB.getSuministroTBs()));
        return jasperPrint;
    }
}
