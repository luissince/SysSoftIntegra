package controller.tools.modelticket;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import javafx.util.Duration;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;

import model.NotaCreditoDetalleTB;
import model.NotaCreditoTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import service.NotaCreditoADO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketNotaCredito {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private final ConvertMonedaCadena monedaCadena;

    private String fileName;

    public TicketNotaCredito(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idNotaCredito) {
        if (!Session.ESTADO_IMPRESORA_NOTA_CREDITO && Tools.isText(Session.NOMBRE_IMPRESORA_NOTA_CREDITO) && Tools.isText(Session.FORMATO_IMPRESORA_NOTA_CREDITO)) {
            Tools.AlertMessageWarning(node, "Venta", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }

        if (Session.FORMATO_IMPRESORA_NOTA_CREDITO.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_NOTA_CREDITO_ID == 0 && Tools.isText(Session.TICKET_NOTA_CREDITO_RUTA)) {
                Tools.AlertMessageWarning(node, "Venta", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterNotaCredito(
                        idNotaCredito,
                        Session.DESING_IMPRESORA_NOTA_CREDITO,
                        Session.TICKET_NOTA_CREDITO_ID,
                        Session.TICKET_NOTA_CREDITO_RUTA,
                        Session.NOMBRE_IMPRESORA_NOTA_CREDITO,
                        Session.CORTAPAPEL_IMPRESORA_NOTA_CREDITO);
            }
        } else if (Session.FORMATO_IMPRESORA_NOTA_CREDITO.equalsIgnoreCase("a4")) {
//            executeProcessPrinterVenta(idNotaCredito, Session.NOMBRE_IMPRESORA_VENTA, Session.CORTAPAPEL_IMPRESORA_VENTA, Session.FORMATO_IMPRESORA_VENTA);
        } else {
            Tools.AlertMessageWarning(node, "Venta", "Error al validar el formato de impresión, configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterNotaCredito(String idNotaCredito, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = NotaCreditoADO.DetalleNotaCreditoById(idNotaCredito);
                if (object instanceof NotaCreditoTB) {
                    try {
                        NotaCreditoTB notaCreditoTB = (NotaCreditoTB) object;
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingCotizacion(notaCreditoTB, ticketId, ticketRuta, nombreImpresora, cortaPapel);
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
            } else if (result.equalsIgnoreCase("no_config")) {
                Tools.showAlertNotification("/view/image/warning_large.png",
                        "Envío de impresión",
                        Tools.newLineString("Error al validar el tipo de impresión, cofigure nuevamente la impresora."),
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
                        Tools.newLineString("Se producto un problema por problemas de la impresora: " + result),
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

    private String printTicketWithDesingCotizacion(NotaCreditoTB notaCreditoTB, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    notaCreditoTB.getNombreComprobante(),
                    notaCreditoTB.getSerie() + "-" + Tools.formatNumber(notaCreditoTB.getNumeracion()),
                    notaCreditoTB.getClienteTB().getNumeroDocumento(),
                    notaCreditoTB.getClienteTB().getInformacion(),
                    notaCreditoTB.getClienteTB().getCelular(),
                    notaCreditoTB.getClienteTB().getDireccion(),
                    "CODIGO PROCESO",
                    "IMPORTE EN LETRAS",
                    notaCreditoTB.getFechaRegistro(),
                    notaCreditoTB.getHoraRegistro(),
                    notaCreditoTB.getFechaRegistro(),
                    notaCreditoTB.getHoraRegistro(),
                    "0",
                    "0",
                    "0",
                    "-",
                    notaCreditoTB.getEmpleadoTB().getApellidos() + " " + notaCreditoTB.getEmpleadoTB().getNombres(),
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
                    "",
                    notaCreditoTB.getVentaTB().getComprobanteName(),
                    notaCreditoTB.getVentaTB().getSerie(),
                    notaCreditoTB.getVentaTB().getNumeracion(),
                    notaCreditoTB.getNombreMotivo());
        }

        AnchorPane hbDetalle = new AnchorPane();
        ObservableList<NotaCreditoDetalleTB> arrList = FXCollections.observableArrayList(notaCreditoTB.getNotaCreditoDetalleTBs());
        for (int m = 0; m < arrList.size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleNotaCredito(hBox, box, arrList, m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        double totalBruto = 0;
        double totalDescuento = 0;
        double totalSubTotal = 0;
        double totalImpuesto = 0;
        double totalNeto = 0;

        for (NotaCreditoDetalleTB ocdtb : notaCreditoTB.getNotaCreditoDetalleTBs()) {
            double importeBruto = ocdtb.getPrecio() * ocdtb.getCantidad();
            double descuento = ocdtb.getDescuento();
            double subImporteBruto = importeBruto - descuento;
            double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(), subImporteBruto);
            double impuesto = Tools.calculateTax(ocdtb.getImpuestoTB().getValor(), subImporteNeto);
            double importeNeto = subImporteNeto + impuesto;

            totalBruto += importeBruto;
            totalDescuento += descuento;
            totalSubTotal += subImporteNeto;
            totalImpuesto += impuesto;
            totalNeto += importeNeto;
        }

        for (int i = 0; i < hbPie.getChildren().size(); i++) {
            HBox box = ((HBox) hbPie.getChildren().get(i));
            billPrintable.hbPie(box,
                    notaCreditoTB.getMonedaTB().getSimbolo(),
                    Tools.roundingValue(totalBruto, 2),
                    Tools.roundingValue(totalDescuento, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalImpuesto, 2),
                    Tools.roundingValue(totalNeto, 2),
                    "TARJETA",
                    "EFECTIVO",
                    "VUELTO",
                    notaCreditoTB.getClienteTB().getNumeroDocumento(),
                    notaCreditoTB.getClienteTB().getInformacion(),
                    "CODIGO VENTA",
                    notaCreditoTB.getClienteTB().getCelular(),
                    "IMPORTE EN LETRAS",
                    "DOCUMENTO EMPLEADO",
                    notaCreditoTB.getEmpleadoTB().getApellidos() + " " + notaCreditoTB.getEmpleadoTB().getNombres(),
                    "CELULAR EMPLEADO",
                    "DIRECCION EMPLEADO",
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

    public void mostrarReporte(String idNotaCredito) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                try {
                    Object object = NotaCreditoADO.DetalleNotaCreditoById(idNotaCredito);
                    if (object instanceof NotaCreditoTB) {
                        NotaCreditoTB notaCreditoTB = (NotaCreditoTB) object;

                        double importeBrutoTotal = 0;
                        double descuentoTotal = 0;
                        double subImporteNetoTotal = 0;
                        double impuestoTotal = 0;
                        double importeNetoTotal = 0;
                        JSONArray array = new JSONArray();
                        for (NotaCreditoDetalleTB ocdtb : notaCreditoTB.getNotaCreditoDetalleTBs()) {
                            JSONObject jsono = new JSONObject();
                            jsono.put("id", ocdtb.getId());
                            jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
                            jsono.put("unidad", ocdtb.getSuministroTB().getUnidadCompraName());
                            jsono.put("producto", ocdtb.getSuministroTB().getClave() + "\n" + ocdtb.getSuministroTB().getNombreMarca());
                            jsono.put("precio", Tools.roundingValue(ocdtb.getPrecio(), 2));
                            jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
                            jsono.put("importe", Tools.roundingValue(ocdtb.getPrecio() * (ocdtb.getCantidad() - ocdtb.getDescuento()), 2));
                            array.add(jsono);

                            double importeBruto = ocdtb.getPrecio() * ocdtb.getCantidad();
                            double descuento = ocdtb.getDescuento();
                            double subImporteBruto = importeBruto - descuento;
                            double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(), subImporteBruto);
                            double impuesto = Tools.calculateTax(ocdtb.getImpuestoTB().getValor(), subImporteNeto);
                            double importeNeto = subImporteNeto + impuesto;

                            importeBrutoTotal += importeBruto;
                            descuentoTotal += descuento;
                            subImporteNetoTotal += subImporteNeto;
                            impuestoTotal += impuesto;
                            importeNetoTotal += importeNeto;
                        }

                        String json = new String(array.toJSONString().getBytes(), "UTF-8");
                        ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(json.getBytes());

                        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
                        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

                        if (Session.COMPANY_IMAGE != null) {
                            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                        }

                        InputStream dir = getClass().getResourceAsStream("/report/NotaCredito.jasper");
                        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode("|" + Session.COMPANY_NUMERO_DOCUMENTO + "|"
                                + notaCreditoTB.getCodigoAlterno() + "|"
                                + notaCreditoTB.getSerie() + "|"
                                + notaCreditoTB.getNumeracion() + "|"
                                + Tools.roundingValue(impuestoTotal, 2) + "|"
                                + Tools.roundingValue(importeNetoTotal, 2) + "|"
                                + notaCreditoTB.getFechaRegistro() + "|"
                                + notaCreditoTB.getClienteTB().getIdAuxiliar() + "|"
                                + notaCreditoTB.getClienteTB().getNumeroDocumento() + "|", BarcodeFormat.QR_CODE, 800, 800));

                        /*
                        **
                         */
                        Map map = new HashMap();
                        map.put("LOGO", imgInputStream);
                        map.put("ICON", imgInputStreamIcon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("TELEFONOCELULAR", "TELÉFONO: " + Session.COMPANY_TELEFONO + " CELULAR: " + Session.COMPANY_CELULAR);
                        map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);

                        map.put("DOCUMENTOEMPRESA", "R.U.C " + Session.COMPANY_NUMERO_DOCUMENTO);
                        map.put("NOMBREDOCUMENTO", notaCreditoTB.getNombreComprobante());
                        map.put("NUMERODOCUMENTO", notaCreditoTB.getSerie() + "-" + Tools.formatNumber(notaCreditoTB.getNumeracion()));

                        map.put("DOCUMENTOCLIENTE", notaCreditoTB.getClienteTB().getTipoDocumentoName() + ":");
                        map.put("NUMERODOCUMENTOCLIENTE", notaCreditoTB.getClienteTB().getNumeroDocumento());
                        map.put("DATOSCLIENTE", notaCreditoTB.getClienteTB().getInformacion());
                        map.put("DIRECCIONCLIENTE", notaCreditoTB.getClienteTB().getDireccion());
                        map.put("CELULARCLIENTE", notaCreditoTB.getClienteTB().getCelular());
                        map.put("EMAILCLIENTE", notaCreditoTB.getClienteTB().getEmail());

                        map.put("FECHAEMISION", notaCreditoTB.getFechaRegistro());
                        map.put("MONEDA", notaCreditoTB.getMonedaTB().getNombre() + "-" + notaCreditoTB.getMonedaTB().getAbreviado());

                        map.put("DOCUMENTO_REFERENCIA", notaCreditoTB.getVentaTB().getSerie() + "-" + Tools.formatNumber(notaCreditoTB.getVentaTB().getNumeracion()));
                        map.put("MOTIVO_ANULACION", notaCreditoTB.getNombreMotivo());

                        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(0, 2), true, notaCreditoTB.getMonedaTB().getNombre()));
                        map.put("QR_DATA", qrImage);

                        map.put("IMPORTE_BRUTO", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
                        map.put("DESCUENTO", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(descuentoTotal, 2));
                        map.put("SUB_IMPORTE", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
                        map.put("IMPUESTO", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
                        map.put("IMPORTE_NETO", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));

                        fileName = notaCreditoTB.getNombreComprobante() + " " + notaCreditoTB.getSerie() + "-" + notaCreditoTB.getNumeracion();
                        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
                        return jasperPrint;
                    } else {
                        return (String) object;
                    }
                } catch (JRException | WriterException | UnsupportedEncodingException ex) {
                    return ex.getLocalizedMessage();
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
                    Stage stage = WindowStage.StageLoader(parent, "Nota de Crédito");
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

}
