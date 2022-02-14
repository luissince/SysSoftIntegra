package controller.tools.modelticket;

import com.google.zxing.WriterException;
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
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
import javax.print.PrintService;
import model.CompraADO;
import model.CompraTB;
import model.DetalleCompraTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketCompra {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private final ConvertMonedaCadena monedaCadena;

    private String fileName;

    public TicketCompra(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idCompra) {
        if (!Session.ESTADO_IMPRESORA_COMPRA && Tools.isText(Session.NOMBRE_IMPRESORA_COMPRA) && Tools.isText(Session.FORMATO_IMPRESORA_COMPRA)) {
            Tools.AlertMessageWarning(node, "Orden de Compra", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_COMPRA.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_COMPRA_ID == 0 && Session.TICKET_COMPRA_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Orden de Compra", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessCompraTicket(idCompra,
                        Session.DESING_IMPRESORA_COMPRA,
                        Session.TICKET_COMPRA_ID,
                        Session.TICKET_COMPRA_RUTA,
                        Session.NOMBRE_IMPRESORA_COMPRA,
                        Session.CORTAPAPEL_IMPRESORA_COMPRA
                );
            }
        } else {
            Tools.AlertMessageWarning(node, "Orden de Compra", "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessCompraTicket(String idCompra, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = CompraADO.Obtener_Compra_ById(idCompra);
                if (object instanceof CompraTB) {
                    try {
                        CompraTB compraTB = (CompraTB) object;
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingCompra(compraTB, ticketId, ticketRuta, nombreImpresora, cortaPapel);
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

    private String printTicketWithDesingCompra(CompraTB compraTB, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    compraTB.getTipoName(),
                    compraTB.getComprobante(),
                    compraTB.getSerie() + "-" + compraTB.getNumeracion(),
                    compraTB.getProveedorTB().getNumeroDocumento(),
                    compraTB.getProveedorTB().getRazonSocial(),
                    compraTB.getProveedorTB().getCelular(),
                    compraTB.getProveedorTB().getRazonSocial(),
                    "CODIGO PROCESO",
                    "IMPORTE EN LETRAS",
                    compraTB.getFechaCompra(),
                    compraTB.getHoraCompra(),
                    compraTB.getFechaVencimiento(),
                    compraTB.getHoraVencimiento(),
                    "0",
                    "0",
                    "0",
                    "-",
                    "-",
                    "-",
                    "-",
                    "0",
                    "0",
                    "0",
                    compraTB.getNotas(),
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
        for (int m = 0; m < compraTB.getDetalleCompraTBs().size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleCompra(hBox, box, compraTB.getDetalleCompraTBs(), m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        double totalBruto = 0;
        double totalDescuento = 0;
        double totalSubTotal = 0;
        double totalImpuesto = 0;
        double totalNeto = 0;

        for (DetalleCompraTB ocdtb : compraTB.getDetalleCompraTBs()) {
            double importeBruto = ocdtb.getPrecioCompra() * ocdtb.getCantidad();
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
                    Session.MONEDA_SIMBOLO,
                    Tools.roundingValue(totalBruto, 2),
                    Tools.roundingValue(totalDescuento, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalImpuesto, 2),
                    Tools.roundingValue(totalNeto, 2),
                    "TARJETA",
                    "EFECTIVO",
                    "VUELTO",
                    compraTB.getProveedorTB().getNumeroDocumento(),
                    compraTB.getProveedorTB().getRazonSocial(),
                    "CODIGO VENTA",
                    compraTB.getProveedorTB().getCelular(),
                    "IMPORTE EN LETRAS",
                    "DOCUMENTO EMPLEADO",
                    "-",
                    "CELULAR EMPLEADO",
                    "DIRECCION EMPLEADO",
                    compraTB.getNotas());
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

    private JasperPrint reportA4(CompraTB compraTB) throws JRException, WriterException, UnsupportedEncodingException {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;
        JSONArray array = new JSONArray();

        for (DetalleCompraTB ocdtb : compraTB.getDetalleCompraTBs()) {
            JSONObject jsono = new JSONObject();
            jsono.put("id", ocdtb.getId());
            jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
            jsono.put("unidad", ocdtb.getSuministroTB().getUnidadCompraName());
            jsono.put("producto", ocdtb.getSuministroTB().getClave() + "\n" + ocdtb.getSuministroTB().getNombreMarca());
            jsono.put("costo", Tools.roundingValue(ocdtb.getPrecioCompra(), 2));
            jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
            jsono.put("importe", Tools.roundingValue(ocdtb.getPrecioCompra() * (ocdtb.getCantidad() - ocdtb.getDescuento()), 2));
            array.add(jsono);

            double importeBruto = ocdtb.getPrecioCompra() * ocdtb.getCantidad();
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

        InputStream dir = getClass().getResourceAsStream("/report/CompraRealizada.jasper");
        InputStream icon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
        InputStream logo = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
        if (Session.COMPANY_IMAGE != null) {
            logo = new ByteArrayInputStream(Session.COMPANY_IMAGE);
        }

        Map map = new HashMap();
        map.put("LOGO", logo);
        map.put("ICON", icon);
        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
        map.put("TELEFONOCELULAR", Tools.textShow("TEL.: ", Session.COMPANY_TELEFONO) + " " + Tools.textShow("CEL.: ", Session.COMPANY_CELULAR));
        map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL.toUpperCase()));
        map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
        map.put("PAGINAWEB", Tools.textShow("", Session.COMPANY_PAGINAWEB));

        map.put("FECHAREGISTRO", compraTB.getFechaCompra().toUpperCase());
        map.put("MONEDA", compraTB.getMonedaTB().getNombre() + "-" + compraTB.getMonedaTB().getAbreviado());
        map.put("DOCUMENTOPROVEEDOR", compraTB.getProveedorTB().getNumeroDocumento());
        map.put("DATOSPROVEEDOR", compraTB.getProveedorTB().getRazonSocial());
        map.put("DIRECCIONPROVEEDOR", Tools.textShow("", compraTB.getProveedorTB().getDireccion()));
        map.put("PROVEEDORTELEFONOS", Tools.textShow("TEL.: ", compraTB.getProveedorTB().getTelefono()) + " " + Tools.textShow("CEL.: ", compraTB.getProveedorTB().getCelular()));
        map.put("PROVEEDOREMAIL", Tools.textShow("", compraTB.getProveedorTB().getEmail()));
        map.put("COMPROBANTE", compraTB.getComprobante() + "\n" + compraTB.getSerie().toUpperCase() + "-" + compraTB.getNumeracion());

        map.put("NOTAS", compraTB.getNotas());
        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true, compraTB.getMonedaTB().getNombre()));

        map.put("VALOR_VENTA", compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
        map.put("DESCUENTO", compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(descuentoTotal, 2));
        map.put("SUB_IMPORTE", compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        map.put("IMPUESTO_TOTAL", compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
        map.put("IMPORTE_TOTAL", compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));

        fileName = "COMPRA " + compraTB.getComprobante() + " " + compraTB.getSerie().toUpperCase() + "-" + compraTB.getNumeracion();
        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
        return jasperPrint;
    }

    public void mostrarReporte(String idCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                Object object = CompraADO.Obtener_Compra_ById(idCompra);
                if (object instanceof CompraTB) {
                    try {
                        return reportA4((CompraTB) object);
                    } catch (JRException | WriterException | UnsupportedEncodingException ex) {
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
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Generando reporte",
                    Tools.newLineString("Se produjo un problema al generar."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });
        task.setOnSucceeded(w -> {
            try {
                Object result = task.getValue();
                if (result instanceof JasperPrint) {
                    Tools.showAlertNotification("/view/image/succes_large.png",
                            "Generando reporte",
                            Tools.newLineString("Se generó correctamente el reporte."),
                            Duration.seconds(5),
                            Pos.BOTTOM_RIGHT);

                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName(fileName);
                    controller.setJasperPrint((JasperPrint) result);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, fileName);
                    stage.setResizable(true);
                    stage.show();
                    stage.requestFocus();

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
                        Tools.newLineString(ex.getLocalizedMessage()),
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
