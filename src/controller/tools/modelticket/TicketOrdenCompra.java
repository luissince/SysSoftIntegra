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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import model.OrdenCompraDetalleTB;
import model.OrdenCompraTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import service.OrdenCompraADO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketOrdenCompra {

    private final Node node;

    private final BillPrintable billPrintable;

    private final ConvertMonedaCadena monedaCadena;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private String fileName = "";

    private final String filePath = "./report/OrdendeCompra.jasper";

    public TicketOrdenCompra(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado,
            AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idOrdenCompra) {
        if (!Session.ESTADO_IMPRESORA_ORDEN_COMPRA && Tools.isText(Session.NOMBRE_IMPRESORA_ORDEN_COMPRA)
                && Tools.isText(Session.FORMATO_IMPRESORA_ORDEN_COMPRA)) {
            Tools.AlertMessageWarning(node, "Orden de Compra",
                    "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_ORDEN_COMPRA.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_ORDEN_COMPRA_ID == 0 && Session.TICKET_ORDEN_COMPRA_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Orden de Compra",
                        "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessOrdenCompraTicket(idOrdenCompra,
                        Session.DESING_IMPRESORA_ORDEN_COMPRA,
                        Session.TICKET_ORDEN_COMPRA_ID,
                        Session.TICKET_ORDEN_COMPRA_RUTA,
                        Session.NOMBRE_IMPRESORA_ORDEN_COMPRA,
                        Session.CORTAPAPEL_IMPRESORA_ORDEN_COMPRA);
            }
        } else {
            Tools.AlertMessageWarning(node, "Orden de Compra",
                    "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessOrdenCompraTicket(String idOrdenCompra, String desing, int ticketId, String ticketRuta,
            String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = OrdenCompraADO.Obtener_Orden_Compra_ById(idOrdenCompra);
                if (object instanceof OrdenCompraTB) {
                    try {
                        OrdenCompraTB ordenCompraTB = (OrdenCompraTB) object;
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingOrdenCompra(ordenCompraTB, ticketId, ticketRuta,
                                    nombreImpresora, cortaPapel);
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
                        Tools.newLineString("Se producto un problema de la impresora " + result),
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

    private String printTicketWithDesingOrdenCompra(OrdenCompraTB ordenCompraTB, int ticketId, String ticketRuta,
            String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "", // tipoVenta
                    "ORDEN DE COMPRA", // nombre_impresion_comprobante
                    "N° - " + Tools.formatNumber(ordenCompraTB.getNumeracion()), // numeracion_serie_comprobante
                    ordenCompraTB.getProveedorTB().getNumeroDocumento(), // nummero_documento_cliente
                    ordenCompraTB.getProveedorTB().getRazonSocial(), // informacion_cliente
                    ordenCompraTB.getProveedorTB().getCelular(), // celular_cliente
                    ordenCompraTB.getProveedorTB().getRazonSocial(), // direccion_cliente
                    "CODIGO PROCESO", // codigoVenta
                    "IMPORTE EN LETRAS", // importe_total_letras
                    ordenCompraTB.getFechaRegistro(), // fechaInicioOperacion
                    ordenCompraTB.getHoraVencimiento(), // horaInicioOperacion
                    ordenCompraTB.getFechaRegistro(), // fechaTerminoOperaciona
                    ordenCompraTB.getHoraVencimiento(), // horaTerminoOperacion
                    "0", // calculado
                    "0", // contado
                    "0", // diferencia
                    "-", // empleadoNumeroDocumento
                    ordenCompraTB.getEmpleadoTB().getApellidos() + " " + ordenCompraTB.getEmpleadoTB().getNombres(), // empleadoInformacion
                    "-", // empleadoCelular
                    "-", // empleadoDireccion
                    "0", // montoTotal
                    "0", // montoPagado
                    "0", // montoDiferencial
                    "", // obsevacion_descripción
                    "0", // monto_inicial_caja
                    "0", // monto_efectivo_caja
                    "0", // monto_tarjeta_caja
                    "0", // monto_deposito_caja
                    "0", // monto_ingreso_caja
                    "0", // monto_egreso_caja
                    "", // nombre_impresion_comprobante_guia
                    "", // numeracion_serie_comprobante_guia
                    "", // direccion_partida_guia
                    "", // ubigeo_partida_guia
                    "", // direccion_llegada_guia
                    "", // ubigeo_llegada_guia
                    "", // movito_traslado_guia
                    "", // comprobante_anulado_nombre
                    "", // comprobante_anulado_serie
                    "", // comprobante_anulado_numeracion
                    "", // nota_credito_motivo_anulacion
                    "", // modalidad_traslado_guia
                    "", // fecha_traslado_guia
                    "", // peso_cargar_guia
                    "", // numero_placa_vehiculo_guia
                    "", // numero_documento_conductor_guia
                    "", // informacion_conductor_guia
                    "", // licencia_conductor_guia
                    "", // comprobante_referencia_guia
                    ""// serie_numeracion_referencia_guia
            );

            // billPrintable.hbEncebezado(box,
            // "",
            // "ORDEN DE COMPRA",
            // "N° - " + Tools.formatNumber(ordenCompraTB.getNumeracion()),
            // ordenCompraTB.getProveedorTB().getNumeroDocumento(),
            // ordenCompraTB.getProveedorTB().getRazonSocial(),
            // ordenCompraTB.getProveedorTB().getCelular(),
            // ordenCompraTB.getProveedorTB().getRazonSocial(),
            // "CODIGO PROCESO",
            // "IMPORTE EN LETRAS",
            // ordenCompraTB.getFechaRegistro(),
            // ordenCompraTB.getHoraVencimiento(),
            // ordenCompraTB.getFechaVencimiento(),
            // ordenCompraTB.getHoraVencimiento(),
            // "0",
            // "0",
            // "0",
            // "-",
            // ordenCompraTB.getEmpleadoTB().getApellidos() + " " +
            // ordenCompraTB.getEmpleadoTB().getNombres(),
            // "-",
            // "-",
            // "0",
            // "0",
            // "0",
            // ordenCompraTB.getObservacion(),
            // "0",
            // "0",
            // "0",
            // "0",
            // "0",
            // "0",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "",
            // "");
        }

        AnchorPane hbDetalle = new AnchorPane();
        for (int m = 0; m < ordenCompraTB.getOrdenCompraDetalleTBs().size(); m++) {
            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = new HBox();
                hBox.setId("dc_" + m + "" + i);
                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                billPrintable.hbDetalleOrdenCompra(hBox, box, ordenCompraTB.getOrdenCompraDetalleTBs(), m);
                hbDetalle.getChildren().add(hBox);
            }
        }

        double totalBruto = 0;
        double totalDescuento = 0;
        double totalSubTotal = 0;
        double totalImpuesto = 0;
        double totalNeto = 0;

        for (OrdenCompraDetalleTB ocdtb : ordenCompraTB.getOrdenCompraDetalleTBs()) {
            double importeBruto = ocdtb.getCosto() * ocdtb.getCantidad();
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
                    ordenCompraTB.getMonedaTB().getSimbolo(),
                    Tools.roundingValue(totalBruto, 2),
                    Tools.roundingValue(totalDescuento, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalImpuesto, 2),
                    Tools.roundingValue(totalNeto, 2),
                    "TARJETA",
                    "EFECTIVO",
                    "VUELTO",
                    ordenCompraTB.getProveedorTB().getNumeroDocumento(),
                    ordenCompraTB.getProveedorTB().getRazonSocial(),
                    "CODIGO VENTA",
                    ordenCompraTB.getProveedorTB().getCelular(),
                    "IMPORTE EN LETRAS",
                    "DOCUMENTO EMPLEADO",
                    ordenCompraTB.getEmpleadoTB().getApellidos() + " " + ordenCompraTB.getEmpleadoTB().getNombres(),
                    "CELULAR EMPLEADO",
                    "DIRECCION EMPLEADO",
                    ordenCompraTB.getObservacion());
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

    public void mostrarReporte(String idOrdenCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {

                File archivoc = new File(filePath);
                if (!archivoc.exists()) {
                    return "El documento no existe o la ruta no es accesible.";
                }
                try {
                    Object object = OrdenCompraADO.Obtener_Orden_Compra_ById(idOrdenCompra);
                    if (object instanceof OrdenCompraTB) {
                        OrdenCompraTB ordenCompraTB = (OrdenCompraTB) object;
                        double importeBrutoTotal = 0;
                        double descuentoTotal = 0;
                        double subImporteNetoTotal = 0;
                        double impuestoTotal = 0;
                        double importeNetoTotal = 0;
                        JSONArray array = new JSONArray();

                        for (OrdenCompraDetalleTB ocdtb : ordenCompraTB.getOrdenCompraDetalleTBs()) {
                            JSONObject jsono = new JSONObject();
                            jsono.put("id", ocdtb.getId());
                            jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
                            jsono.put("unidad", ocdtb.getSuministroTB().getUnidadCompraName());
                            jsono.put("producto", ocdtb.getSuministroTB().getClave() + "\n"
                                    + ocdtb.getSuministroTB().getNombreMarca());
                            jsono.put("costo", Tools.roundingValue(ocdtb.getCosto(), 2));
                            jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
                            jsono.put("importe", Tools
                                    .roundingValue(ocdtb.getCosto() * (ocdtb.getCantidad() - ocdtb.getDescuento()), 2));
                            array.add(jsono);

                            double importeBruto = ocdtb.getCosto() * ocdtb.getCantidad();
                            double descuento = ocdtb.getDescuento();
                            double subImporteBruto = importeBruto - descuento;
                            double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(),
                                    subImporteBruto);
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

                        InputStream dir = new FileInputStream(archivoc.getPath());

                        Map map = new HashMap();
                        map.put("LOGO", imgInputStream);
                        map.put("ICON", imgInputStreamIcon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO)
                                + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
                        map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);
                        map.put("PAGINAWEB", Session.COMPANY_PAGINAWEB);

                        map.put("DOCUMENTOEMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);
                        map.put("NOMBREDOCUMENTO", "ORDEN DE COMPRA");
                        map.put("NUMERODOCUMENTO", "N° - " + Tools.formatNumber(ordenCompraTB.getNumeracion()));
                        map.put("FECHAORDEN", ordenCompraTB.getFechaRegistro());
                        map.put("FECHAENTREGA", ordenCompraTB.getFechaVencimiento());

                        map.put("PROVEEDORDOCUMENTO", ordenCompraTB.getProveedorTB().getNumeroDocumento());
                        map.put("PROVEEDORINFORMACION", ordenCompraTB.getProveedorTB().getRazonSocial());
                        map.put("PROVEEDOREMAIL", ordenCompraTB.getProveedorTB().getEmail());
                        map.put("PROVEEDORCELULAR", ordenCompraTB.getProveedorTB().getCelular());
                        map.put("PROVEEDORDIRECCION", ordenCompraTB.getProveedorTB().getDireccion());

                        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                                ordenCompraTB.getMonedaTB().getNombre()));
                        map.put("OBSERVACION", ordenCompraTB.getObservacion());

                        map.put("IMPORTEBRUTO", ordenCompraTB.getMonedaTB().getSimbolo() + " "
                                + Tools.roundingValue(importeBrutoTotal, 2));
                        map.put("DESCUENTOTOTAL", ordenCompraTB.getMonedaTB().getSimbolo() + " "
                                + Tools.roundingValue(descuentoTotal, 2));
                        map.put("SUBIMPORTE", ordenCompraTB.getMonedaTB().getSimbolo() + " "
                                + Tools.roundingValue(subImporteNetoTotal, 2));
                        map.put("IMPUESTO",
                                ordenCompraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
                        map.put("IMPORTENETO", ordenCompraTB.getMonedaTB().getSimbolo() + " "
                                + Tools.roundingValue(importeNetoTotal, 2));

                        fileName = "ORDEN DE COMRA N° - " + Tools.formatNumber(ordenCompraTB.getNumeracion());
                        return JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
                    } else {
                        return (String) object;
                    }
                } catch (JRException | UnsupportedEncodingException | FileNotFoundException ex) {
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
            Tools.println(task.getMessage());
            Tools.println(task.getException().getLocalizedMessage());
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
                    // Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName(fileName);
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Orden de Compra");
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
