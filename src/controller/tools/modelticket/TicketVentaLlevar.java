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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import model.DetalleVentaTB;
import model.HistorialSuministroSalidaTB;
import model.VentaADO;
import model.VentaTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketVentaLlevar {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private String fileName;

    public TicketVentaLlevar(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir(String idVenta, String idSuministro) {
        if (!Session.ESTADO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS && Tools.isText(Session.NOMBRE_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS) && Tools.isText(Session.FORMATO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS)) {
            Tools.AlertMessageWarning(node, "Salida del Producto", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID == 0 && Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Salida del Producto", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessHistorialSalidaProducto(
                        idVenta,
                        idSuministro,
                        Session.DESING_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS,
                        Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID,
                        Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA,
                        Session.NOMBRE_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS,
                        Session.CORTAPAPEL_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS
                );
            }
        } else {
            Tools.AlertMessageWarning(node, "Salida del Producto", "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessHistorialSalidaProducto(String idVenta, String idSuministro, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                if (!Tools.isText(idVenta) && !Tools.isText(idSuministro)) {
                    Object object = VentaADO.Listar_Historial_Suministro_Llevar(idVenta, idSuministro);

                    if (object instanceof Object[]) {
                        Object[] objects = (Object[]) object;
                        try {
                            VentaTB ventaTB = (VentaTB) objects[0];
                            DetalleVentaTB detalleVentaTB = (DetalleVentaTB) objects[1];
                            ArrayList<HistorialSuministroSalidaTB> suministroSalidas = (ArrayList<HistorialSuministroSalidaTB>) objects[2];
                            if (desing.equalsIgnoreCase("withdesing")) {
                                return printTicketWithDesingHistorialSalidaProducto(ventaTB, detalleVentaTB, suministroSalidas, ticketId, ticketRuta, nombreImpresora, cortaPapel);
                            } else {
                                billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);
                                return printTicketNoDesingHistorialSalidaProducto(ventaTB, detalleVentaTB, suministroSalidas, nombreImpresora, cortaPapel);
                            }
                        } catch (PrinterException | IOException | PrintException ex) {
                            return "Error en imprimir: " + ex.getLocalizedMessage();
                        }
                    } else {
                        return (String) object;
                    }
                } else {
                    return "id no inicializados";
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

    private String printTicketWithDesingHistorialSalidaProducto(VentaTB ventaTB, DetalleVentaTB detalleVentaTB, ArrayList<HistorialSuministroSalidaTB> suministroSalidas, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        double cantidadActual = 0;
        cantidadActual = suministroSalidas.stream().map((hs) -> hs.getCantidad()).reduce(cantidadActual, (accumulator, _item) -> accumulator + _item);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "HISTORIAL DE SALIDA",
                    ventaTB.getSerie() + "-" + ventaTB.getNumeracion(),
                    ventaTB.getClienteTB().getNumeroDocumento(),
                    ventaTB.getClienteTB().getInformacion(),
                    ventaTB.getClienteTB().getCelular(),
                    ventaTB.getClienteTB().getDireccion(),
                    "",
                    "",
                    ventaTB.getFechaVenta(),
                    ventaTB.getHoraVenta(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    Tools.roundingValue(detalleVentaTB.getCantidad(), 2),
                    Tools.roundingValue(cantidadActual, 2),
                    Tools.roundingValue(detalleVentaTB.getCantidad() - cantidadActual, 2),
                    detalleVentaTB.getSuministroTB().getNombreMarca(),
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
                    ""
            );
        }

        AnchorPane hbDetalle = new AnchorPane();
        ArrayList<HistorialSuministroSalidaTB> arrList = suministroSalidas;
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleHistorialSumistroSalida(hBox, box, arrList, m);
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
                    ventaTB.getClienteTB().getNumeroDocumento(),
                    ventaTB.getClienteTB().getInformacion(),
                    "",
                    ventaTB.getClienteTB().getCelular(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }

        billPrintable.generateTicketPrint(hbEncabezado, hbDetalle, hbPie);
        DocPrintJob job = billPrintable.findPrintService(nombreImpresora, PrinterJob.lookupPrintServices()).createPrintJob();
        if (job != null) {
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

    private String printTicketNoDesingHistorialSalidaProducto(VentaTB ventaTB, DetalleVentaTB detalleVentaTB, ArrayList<HistorialSuministroSalidaTB> suministroSalidas, String nombreImpresora, boolean cortaPapel) {
        ArrayList<HBox> object = new ArrayList<>();

        double cantidadActual = 0;
        cantidadActual = suministroSalidas.stream().map((hs) -> hs.getCantidad()).reduce(cantidadActual, (accumulator, _item) -> accumulator + _item);

        int rows = 0;
        int lines = 0;
        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            object.add((HBox) hbEncabezado.getChildren().get(i));
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            rows++;
            lines += billPrintable.hbEncebezado(box,
                    "",
                    "HISTORIAL DE SALIDA",
                    ventaTB.getSerie() + "-" + ventaTB.getNumeracion(),
                    ventaTB.getClienteTB().getNumeroDocumento(),
                    ventaTB.getClienteTB().getInformacion(),
                    ventaTB.getClienteTB().getCelular(),
                    ventaTB.getClienteTB().getDireccion(),
                    "",
                    "",
                    ventaTB.getFechaVenta(),
                    ventaTB.getHoraVenta(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    Tools.roundingValue(detalleVentaTB.getCantidad(), 2),
                    Tools.roundingValue(cantidadActual, 2),
                    Tools.roundingValue(detalleVentaTB.getCantidad() - cantidadActual, 2),
                    detalleVentaTB.getSuministroTB().getNombreMarca(),
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
                    ""
            );
        }

        ArrayList<HistorialSuministroSalidaTB> arrList = suministroSalidas;
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                rows++;
                lines += billPrintable.hbDetalleHistorialSumistroSalida(hBox, box, arrList, m);
                object.add(hBox);
            }
        }

        for (int i = 0; i < hbPie.getChildren().size(); i++) {
            object.add((HBox) hbPie.getChildren().get(i));
            HBox box = ((HBox) hbPie.getChildren().get(i));
            rows++;
            lines += billPrintable.hbPie(box,
                    "M",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    ventaTB.getClienteTB().getNumeroDocumento(),
                    ventaTB.getClienteTB().getInformacion(),
                    "",
                    ventaTB.getClienteTB().getCelular(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");
        }
        return billPrintable.modelTicket(rows + lines + 1 + 10, lines, object, nombreImpresora, cortaPapel);
    }

    public void mostrarReporte(String idVenta, String idSuministro) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                Object object = VentaADO.Listar_Historial_Suministro_Llevar(idVenta, idSuministro);

                if (object instanceof Object[]) {
                    Object[] objects = (Object[]) object;
                    try {
                        VentaTB ventaTB = (VentaTB) objects[0];
                        DetalleVentaTB detalleVentaTB = (DetalleVentaTB) objects[1];
                        ArrayList<HistorialSuministroSalidaTB> suministroSalidas = (ArrayList<HistorialSuministroSalidaTB>) objects[2];

                        double cantidad = 0;
                        cantidad = suministroSalidas.stream().map(sd -> sd.getCantidad()).reduce(cantidad, (accumulator, _item) -> accumulator + _item);

                        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

                        if (Session.COMPANY_IMAGE != null) {
                            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                        }

                        InputStream dir = getClass().getResourceAsStream("/report/ReporteHistorialMovimiento.jasper");

                        Map map = new HashMap();
                        map.put("LOGO", imgInputStream);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO) + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
                        map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL));
                        map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));

                        map.put("CLIENTE_INFORMACION", ventaTB.getClienteTB().getInformacion());
                        map.put("CLIENTE_CELULAR", ventaTB.getClienteTB().getCelular());
                        map.put("CLIENTE_EMAIL", ventaTB.getClienteTB().getEmail());
                        map.put("PRODUCTO", detalleVentaTB.getSuministroTB().getNombreMarca());

                        map.put("COMPROBANTE", ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                        map.put("FECHA", ventaTB.getFechaVenta() + " " + ventaTB.getHoraVenta());
                        map.put("CANTIDAD", Tools.roundingValue(cantidad, 2));

                        fileName = "HISTORIAL DE MOVIMIENTOS DE " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion();

                        return JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(suministroSalidas));
                    } catch (JRException ex) {
                        return "Error en imprimir: " + ex.getLocalizedMessage();
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
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Generando reporte",
                    Tools.newLineString("Se produjo un problema al generar."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });
        task.setOnSucceeded(w -> {
            try {
                Object object = task.getValue();
                if (object instanceof JasperPrint) {
                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName(fileName);
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Historial de Salida");
                    stage.setResizable(true);
                    stage.show();
                    stage.requestFocus();

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
            } catch (IOException ex) {
                Tools.showAlertNotification("/view/image/error_large.png",
                        "Generando reporte",
                        Tools.newLineString("Error en generar el reporte: " + ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

}
