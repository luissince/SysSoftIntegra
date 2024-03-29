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

import model.CotizacionDetalleTB;
import model.CotizacionTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import service.BancoADO;
import service.CotizacionADO;
import service.EmpresaADO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketCotizacion {

    private final Node node;

    private final BillPrintable billPrintable;

    private final ConvertMonedaCadena monedaCadena;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private String fileName;

    public TicketCotizacion(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado,
            AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idCotizacion) {
        if (!Session.ESTADO_IMPRESORA_COTIZACION && Tools.isText(Session.NOMBRE_IMPRESORA_COTIZACION)
                && Tools.isText(Session.FORMATO_IMPRESORA_COTIZACION)) {
            Tools.AlertMessageWarning(node, "Cotización",
                    "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_COTIZACION.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_COTIZACION_ID == 0 && Session.TICKET_COTIZACION_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Cotización",
                        "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessCotizacionTicket(
                        idCotizacion,
                        Session.DESING_IMPRESORA_COTIZACION,
                        Session.TICKET_COTIZACION_ID,
                        Session.TICKET_COTIZACION_RUTA,
                        Session.NOMBRE_IMPRESORA_COTIZACION,
                        Session.CORTAPAPEL_IMPRESORA_COTIZACION);
            }
        } else {
            Tools.AlertMessageWarning(node, "Cotización",
                    "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessCotizacionTicket(String idCotizacion, String desing, int ticketId, String ticketRuta,
            String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = CotizacionADO.Obtener_Cotizacion_ById(idCotizacion, false);
                if (object instanceof CotizacionTB) {
                    try {
                        CotizacionTB cotizacionTB = (CotizacionTB) object;
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingCotizacion(cotizacionTB, ticketId, ticketRuta, nombreImpresora,
                                    cortaPapel);
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

    private String printTicketWithDesingCotizacion(CotizacionTB cotizacionTB, int ticketId, String ticketRuta,
            String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "", // tipoVenta
                    "COTIZACIÓN", // nombre_impresion_comprobante
                    "N° - " + Tools.formatNumber(cotizacionTB.getIdCotizacion()), // numeracion_serie_comprobante
                    cotizacionTB.getClienteTB().getNumeroDocumento(), // nummero_documento_cliente
                    cotizacionTB.getClienteTB().getInformacion(), // informacion_cliente
                    cotizacionTB.getClienteTB().getCelular(), // celular_cliente
                    cotizacionTB.getClienteTB().getDireccion(), // direccion_cliente
                    "CODIGO PROCESO", // codigoVenta
                    "IMPORTE EN LETRAS", // importe_total_letras
                    cotizacionTB.getFechaCotizacion(), // fechaInicioOperacion
                    cotizacionTB.getHoraCotizacion(), // horaInicioOperacion
                    cotizacionTB.getFechaCotizacion(), // fechaTerminoOperaciona
                    cotizacionTB.getHoraCotizacion(), // horaTerminoOperacion
                    "0", // calculado
                    "0", // contado
                    "0", // diferencia
                    "-", // empleadoNumeroDocumento
                    cotizacionTB.getEmpleadoTB().getApellidos() + "-" + cotizacionTB.getEmpleadoTB().getNombres(), // empleadoInformacion
                    "-", // empleadoCelular
                    "-", // empleadoDireccion
                    "0", // montoTotal
                    "0", // montoPagado
                    "0", // montoDiferencial
                    "-", // obsevacion_descripción
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
            // "COTIZACIÓN",
            // "N° - " + Tools.formatNumber(cotizacionTB.getIdCotizacion()),
            // cotizacionTB.getClienteTB().getNumeroDocumento(),
            // cotizacionTB.getClienteTB().getInformacion(),
            // cotizacionTB.getClienteTB().getCelular(),
            // cotizacionTB.getClienteTB().getDireccion(),
            // "CODIGO PROCESO",
            // "IMPORTE EN LETRAS",
            // cotizacionTB.getFechaCotizacion(),
            // cotizacionTB.getHoraCotizacion(),
            // cotizacionTB.getFechaCotizacion(),
            // cotizacionTB.getHoraCotizacion(),
            // "0",
            // "0",
            // "0",
            // "-",
            // cotizacionTB.getEmpleadoTB().getApellidos() + " " +
            // cotizacionTB.getEmpleadoTB().getNombres(),
            // "-",
            // "-",
            // "",
            // "",
            // "",
            // "",
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
        ObservableList<CotizacionDetalleTB> arrList = FXCollections
                .observableArrayList(cotizacionTB.getCotizacionDetalleTBs());
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

        for (CotizacionDetalleTB ocdtb : arrList) {
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
                    cotizacionTB.getMonedaTB().getSimbolo(),
                    Tools.roundingValue(totalBruto, 2),
                    Tools.roundingValue(totalDescuento, 2),
                    Tools.roundingValue(totalSubTotal, 2),
                    Tools.roundingValue(totalImpuesto, 2),
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

    public void mostrarReporte(String idCotizacion) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws FileNotFoundException {
                Object object = CotizacionADO.Obtener_Cotizacion_ById(idCotizacion, false);
                if (object instanceof CotizacionTB) {
                    try {
                        CotizacionTB cotizacionTB = (CotizacionTB) object;

                        double importeBrutoTotal = 0;
                        double descuentoTotal = 0;
                        double subImporteNetoTotal = 0;
                        double impuestoTotal = 0;
                        double importeNetoTotal = 0;
                        JSONArray array = new JSONArray();

                        for (CotizacionDetalleTB ocdtb : cotizacionTB.getCotizacionDetalleTBs()) {
                            JSONObject jsono = new JSONObject();
                            jsono.put("id", ocdtb.getId());
                            jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
                            jsono.put("unidad", ocdtb.getSuministroTB().getUnidadCompraName());
                            jsono.put("producto", ocdtb.getSuministroTB().getClave() + "\n"
                                    + ocdtb.getSuministroTB().getNombreMarca());
                            jsono.put("precio", Tools.roundingValue(ocdtb.getPrecio(), 2));
                            jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
                            jsono.put("importe", Tools.roundingValue(
                                    ocdtb.getPrecio() * (ocdtb.getCantidad() - ocdtb.getDescuento()), 2));
                            array.add(jsono);

                            double importeBruto = ocdtb.getPrecio() * ocdtb.getCantidad();
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

                        File archivoc = new File("./report/Cotizacion.jasper");
                        InputStream dir = new FileInputStream(archivoc.getPath());

                        // InputStream dir =
                        // getClass().getResourceAsStream("/report/Cotizacion.jasper");

                        Map<String, Object> map = new HashMap<>();
                        map.put("LOGO", imgInputStream);
                        map.put("ICON", imgInputStreamIcon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO)
                                + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
                        map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL));
                        map.put("PAGINAWEB", Session.COMPANY_PAGINAWEB);

                        map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
                        map.put("NOMBREDOCUMENTO", "COTIZACIÓN");
                        map.put("NUMERODOCUMENTO",
                                Tools.textShow("N° - ", Tools.formatNumber(cotizacionTB.getIdCotizacion())));

                        map.put("DATOSCLIENTE", cotizacionTB.getClienteTB().getInformacion());
                        map.put("DOCUMENTOCLIENTE", "");
                        map.put("NUMERODOCUMENTOCLIENTE", cotizacionTB.getClienteTB().getNumeroDocumento());
                        map.put("CELULARCLIENTE", cotizacionTB.getClienteTB().getCelular());
                        map.put("EMAILCLIENTE", cotizacionTB.getClienteTB().getEmail());
                        map.put("DIRECCIONCLIENTE", cotizacionTB.getClienteTB().getDireccion());

                        map.put("FECHAEMISION", cotizacionTB.getFechaCotizacion());
                        map.put("MONEDA", cotizacionTB.getMonedaTB().getNombre());

                        map.put("SIMBOLO", cotizacionTB.getMonedaTB().getSimbolo());
                        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                                cotizacionTB.getMonedaTB().getNombre()));

                        map.put("VALOR_VENTA", Tools.roundingValue(importeBrutoTotal, 2));
                        map.put("DESCUENTO", Tools.roundingValue(descuentoTotal, 2));
                        map.put("SUB_IMPORTE", Tools.roundingValue(subImporteNetoTotal, 2));
                        map.put("IMPUESTO_TOTAL", Tools.roundingValue(impuestoTotal, 2));
                        map.put("IMPORTE_TOTAL", Tools.roundingValue(importeNetoTotal, 2));
                        map.put("OBSERVACION", cotizacionTB.getObservaciones());
                        map.put("TERMINOS_CONDICIONES", EmpresaADO.Terminos_Condiciones());
                        map.put("NUMERO_CUENTA", BancoADO.Listar_Banco_Mostrar());

                        fileName = "COTIZACIÓN N° - " + Tools.formatNumber(cotizacionTB.getIdCotizacion());

                        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map,
                                new JsonDataSource(jsonDataStream));
                        return jasperPrint;
                    } catch (JRException | UnsupportedEncodingException | FileNotFoundException ex) {
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
                Object object = task.getValue();
                if (object instanceof JasperPrint) {
                    Tools.showAlertNotification("/view/image/succes_large.png",
                            "Generando reporte",
                            Tools.newLineString("Se genero correctamente el reporte."),
                            Duration.seconds(5),
                            Pos.BOTTOM_RIGHT);

                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    // Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName(fileName);
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, fileName);
                    stage.setResizable(true);
                    stage.show();
                    stage.requestFocus();

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
