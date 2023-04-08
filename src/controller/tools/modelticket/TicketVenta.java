package controller.tools.modelticket;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.ObjectGlobal;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.image.BufferedImage;
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
import java.util.ArrayList;
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
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;

import model.SuministroTB;
import model.TicketTB;
import model.VentaTB;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import service.BancoADO;
import service.EmpresaADO;
import service.TipoDocumentoADO;
import service.VentaADO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketVenta {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private final ConvertMonedaCadena monedaCadena;

    private String fileName;

    public TicketVenta(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera,
            AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void imprimir(String idVenta) {
        if (!Session.ESTADO_IMPRESORA_VENTA && Tools.isText(Session.NOMBRE_IMPRESORA_VENTA)
                && Tools.isText(Session.FORMATO_IMPRESORA_VENTA)) {
            Tools.AlertMessageWarning(node, "Venta",
                    "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }

        if (Session.FORMATO_IMPRESORA_VENTA.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_VENTA_ID == 0 && Tools.isText(Session.TICKET_VENTA_RUTA)) {
                Tools.AlertMessageWarning(node, "Venta",
                        "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterVenta(
                        idVenta,
                        Session.NOMBRE_IMPRESORA_VENTA,
                        Session.CORTAPAPEL_IMPRESORA_VENTA,
                        Session.FORMATO_IMPRESORA_VENTA);
            }
        } else if (Session.FORMATO_IMPRESORA_VENTA.equalsIgnoreCase("a4")) {
            executeProcessPrinterVenta(idVenta, Session.NOMBRE_IMPRESORA_VENTA, Session.CORTAPAPEL_IMPRESORA_VENTA,
                    Session.FORMATO_IMPRESORA_VENTA);
        } else {
            Tools.AlertMessageWarning(node, "Venta",
                    "Error al validar el formato de impresión, configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterVenta(String idVenta, String printerName, boolean printerCut, String format) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object result = VentaADO.Obtener_Venta_ById(idVenta);
                try {
                    if (result instanceof VentaTB) {
                        VentaTB ventaTB = (VentaTB) result;
                        if (format.equalsIgnoreCase("a4")) {
                            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
                            printRequestAttributeSet.add(new Copies(1));

                            PrinterName pn = new PrinterName(printerName, null);
                            PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
                            printServiceAttributeSet.add(pn);

                            JRPrintServiceExporter exporter = new JRPrintServiceExporter();

                            exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportA4(ventaTB));
                            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                                    printRequestAttributeSet);
                            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                                    printServiceAttributeSet);
                            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
                            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
                            exporter.exportReport();
                            return "completed";
                        } else {
                            double importeBrutoTotal = 0;
                            double descuentoTotal = 0;
                            double subImporteNetoTotal = 0;
                            double impuestoTotal = 0;
                            double importeNetoTotal = 0;
                            for (SuministroTB ocdtb : ventaTB.getSuministroTBs()) {
                                double importeBruto = ocdtb.getPrecioVentaGeneral() * ocdtb.getCantidad();
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

                            if (Session.DESING_IMPRESORA_VENTA.equalsIgnoreCase("withdesing")) {

                                Object object = TipoDocumentoADO.GetIdTicketForTipoDocumento(idVenta);
                                if (object instanceof TicketTB) {
                                    TicketTB ticketTB = (TicketTB) object;
                                    billPrintable.loadEstructuraTicket(ticketTB.getId(), ticketTB.getRuta(),
                                            hbEncabezado, hbDetalleCabecera, hbPie);
                                } else {
                                    billPrintable.loadEstructuraTicket(Session.TICKET_VENTA_ID,
                                            Session.TICKET_VENTA_RUTA, hbEncabezado, hbDetalleCabecera, hbPie);
                                }

                                ObjectGlobal.QR_PERU_DATA = String.format("|%s|%s|%s|%s|%s|%s|%s|%s|%s|",
                                        Session.COMPANY_NUMERO_DOCUMENTO,
                                        ventaTB.getTipoDocumentoTB().getCodigoAlterno(),
                                        ventaTB.getSerie(),
                                        ventaTB.getNumeracion(),
                                        Tools.roundingValue(impuestoTotal, 2),
                                        Tools.roundingValue(importeNetoTotal, 2),
                                        ventaTB.getFechaVenta(),
                                        ventaTB.getClienteTB().getIdAuxiliar(),
                                        ventaTB.getClienteTB().getNumeroDocumento());

                                for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
                                    HBox box = ((HBox) hbEncabezado.getChildren().get(i));
                                    billPrintable.hbEncebezado(box,
                                            ventaTB.getTipoName(), // tipoVenta
                                            ventaTB.getTipoDocumentoTB().getNombre(), // nombre_impresion_comprobante
                                            ventaTB.getSerie() + "-" + ventaTB.getNumeracion(), // numeracion_serie_comprobante
                                            ventaTB.getClienteTB().getNumeroDocumento(), // nummero_documento_cliente
                                            ventaTB.getClienteTB().getInformacion(), // informacion_cliente
                                            ventaTB.getClienteTB().getCelular(), // celular_cliente
                                            ventaTB.getClienteTB().getDireccion(), // direccion_cliente
                                            ventaTB.getCodigo(), // codigoVenta
                                            monedaCadena.Convertir(Tools.roundingValue(importeBrutoTotal, 2), true,
                                                    ventaTB.getMonedaTB().getNombre()), // importe_total_letras
                                            ventaTB.getFechaVenta(), // fechaInicioOperacion
                                            ventaTB.getHoraVenta(), // horaInicioOperacion
                                            "", // fechaTerminoOperaciona
                                            "", // horaTerminoOperacion
                                            "0", // calculado
                                            "0", // contado
                                            "0", // diferencia
                                            "-", // empleadoNumeroDocumento
                                            "-", // empleadoInformacion
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
                                }

                                AnchorPane hbDetalle = new AnchorPane();
                                ObservableList<SuministroTB> suministroTBs = FXCollections
                                        .observableArrayList(ventaTB.getSuministroTBs());
                                for (int m = 0; m < suministroTBs.size(); m++) {
                                    for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                                        HBox hBox = new HBox();
                                        hBox.setId("dc_" + m + "" + i);
                                        HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                                        billPrintable.hbDetalle(hBox, box, suministroTBs, m);
                                        hbDetalle.getChildren().add(hBox);
                                    }
                                }

                                for (int i = 0; i < hbPie.getChildren().size(); i++) {
                                    HBox box = ((HBox) hbPie.getChildren().get(i));
                                    billPrintable.hbPie(box, ventaTB.getMonedaTB().getSimbolo(),
                                            Tools.roundingValue(importeBrutoTotal, 2),
                                            Tools.roundingValue(descuentoTotal, 2),
                                            Tools.roundingValue(subImporteNetoTotal, 2),
                                            Tools.roundingValue(impuestoTotal, 2),
                                            Tools.roundingValue(importeNetoTotal, 2),
                                            Tools.roundingValue(ventaTB.getTarjeta(), 2),
                                            Tools.roundingValue(ventaTB.getEfectivo(), 2),
                                            Tools.roundingValue(ventaTB.getVuelto(), 2),
                                            ventaTB.getClienteTB().getNumeroDocumento(),
                                            ventaTB.getClienteTB().getInformacion(), ventaTB.getCodigo(),
                                            ventaTB.getClienteTB().getCelular(),
                                            monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                                                    ventaTB.getMonedaTB().getNombre()),
                                            "",
                                            "",
                                            "",
                                            "",
                                            "");
                                }

                                billPrintable.generateTicketPrint(hbEncabezado, hbDetalle, hbPie);
                                PrintService printService = billPrintable.findPrintService(printerName,
                                        PrinterJob.lookupPrintServices());
                                if (printService != null) {
                                    DocPrintJob job = printService.createPrintJob();
                                    PrinterJob pj = PrinterJob.getPrinterJob();
                                    pj.setPrintService(job.getPrintService());
                                    pj.setJobName(printerName);
                                    Book book = new Book();
                                    book.append(billPrintable, billPrintable.getPageFormat(pj));
                                    pj.setPageable(book);
                                    pj.print();
                                    if (printerCut) {
                                        billPrintable.printCortarPapel(printerName);
                                    }
                                    return "completed";
                                } else {
                                    return "error_name";
                                }
                            } else {
                                billPrintable.loadEstructuraTicket(Session.TICKET_VENTA_ID, Session.TICKET_VENTA_RUTA,
                                        hbEncabezado, hbDetalleCabecera, hbPie);

                                ArrayList<HBox> object = new ArrayList<>();
                                int rows = 0;
                                int lines = 0;
                                for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
                                    object.add((HBox) hbEncabezado.getChildren().get(i));
                                    HBox box = ((HBox) hbEncabezado.getChildren().get(i));
                                    rows++;
                                    lines += billPrintable.hbEncebezado(box,
                                            ventaTB.getTipoName(), // tipoVenta
                                            ventaTB.getTipoDocumentoTB().getNombre(), // nombre_impresion_comprobante
                                            ventaTB.getSerie() + "-" + ventaTB.getNumeracion(), // numeracion_serie_comprobante
                                            ventaTB.getClienteTB().getNumeroDocumento(), // nummero_documento_cliente
                                            ventaTB.getClienteTB().getInformacion(), // informacion_cliente
                                            ventaTB.getClienteTB().getCelular(), // celular_cliente
                                            ventaTB.getClienteTB().getDireccion(), // direccion_cliente
                                            ventaTB.getCodigo(), // codigoVenta
                                            monedaCadena.Convertir(Tools.roundingValue(importeBrutoTotal, 2), true,
                                                    ventaTB.getMonedaTB().getNombre()), // importe_total_letras
                                            ventaTB.getFechaVenta(), // fechaInicioOperacion
                                            ventaTB.getHoraVenta(), // horaInicioOperacion
                                            "", // fechaTerminoOperaciona
                                            "", // horaTerminoOperacion
                                            "0", // calculado
                                            "0", // contado
                                            "0", // diferencia
                                            "-", // empleadoNumeroDocumento
                                            "-", // empleadoInformacion
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
                                }

                                ObservableList<SuministroTB> suministroTBs = FXCollections
                                        .observableArrayList(ventaTB.getSuministroTBs());
                                for (int m = 0; m < suministroTBs.size(); m++) {
                                    for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                                        HBox hBox = new HBox();
                                        hBox.setId("dc_" + m + "" + i);
                                        HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                                        rows++;
                                        lines += billPrintable.hbDetalle(hBox, box, suministroTBs, m);
                                        object.add(hBox);
                                    }
                                }

                                for (int i = 0; i < hbPie.getChildren().size(); i++) {
                                    object.add((HBox) hbPie.getChildren().get(i));
                                    HBox box = ((HBox) hbPie.getChildren().get(i));
                                    rows++;
                                    lines += billPrintable.hbPie(box,
                                            ventaTB.getMonedaTB().getSimbolo(),
                                            Tools.roundingValue(importeBrutoTotal, 2),
                                            Tools.roundingValue(descuentoTotal, 2),
                                            Tools.roundingValue(subImporteNetoTotal, 2),
                                            Tools.roundingValue(impuestoTotal, 2),
                                            Tools.roundingValue(importeNetoTotal, 2),
                                            Tools.roundingValue(ventaTB.getTarjeta(), 2),
                                            Tools.roundingValue(ventaTB.getEfectivo(), 2),
                                            Tools.roundingValue(ventaTB.getVuelto(), 2),
                                            ventaTB.getClienteTB().getNumeroDocumento(),
                                            ventaTB.getClienteTB().getInformacion(), ventaTB.getCodigo(),
                                            ventaTB.getClienteTB().getCelular(),
                                            monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                                                    ventaTB.getMonedaTB().getNombre()),
                                            "",
                                            "",
                                            "",
                                            "",
                                            "");
                                }

                                return billPrintable.modelTicket(rows + lines + 1 + 10, lines, object,
                                        Session.NOMBRE_IMPRESORA_VENTA, Session.CORTAPAPEL_IMPRESORA_VENTA);
                            }
                        }
                    } else {
                        return (String) result;
                    }
                } catch (PrinterException | IOException | PrintException | JRException | WriterException ex) {
                    return "Error en imprimir: " + ex.getLocalizedMessage();
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

    private JasperPrint reportA4(VentaTB ventaTB)
            throws JRException, WriterException, UnsupportedEncodingException, FileNotFoundException {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;
        JSONArray array = new JSONArray();

        for (SuministroTB ocdtb : ventaTB.getSuministroTBs()) {
            JSONObject jsono = new JSONObject();
            jsono.put("id", ocdtb.getId());
            jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
            jsono.put("unidad", ocdtb.getUnidadCompraName());
            jsono.put("producto", ocdtb.getClave() + "\n" + ocdtb.getNombreMarca());
            jsono.put("precio", Tools.roundingValue(ocdtb.getPrecioVentaGeneral(), 2));
            jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
            jsono.put("importe", Tools
                    .roundingValue(ocdtb.getPrecioVentaGeneral() * (ocdtb.getCantidad() - ocdtb.getDescuento()), 2));
            array.add(jsono);

            double importeBruto = ocdtb.getPrecioVentaGeneral() * ocdtb.getCantidad();
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

        File archivoc = new File("./report/VentaRealizada.jasper");
        InputStream dir = new FileInputStream(archivoc.getPath());

        // InputStream dir =
        // getClass().getResourceAsStream("./report/VentaRealizada.jasper");

        StringBuilder sb = new StringBuilder();
        sb.append("|").append(Session.COMPANY_NUMERO_DOCUMENTO).append("|")
                .append(ventaTB.getTipoDocumentoTB().getCodigoAlterno()).append("|")
                .append(ventaTB.getSerie()).append("|")
                .append(ventaTB.getNumeracion()).append("|")
                .append(Tools.roundingValue(impuestoTotal, 2)).append("|")
                .append(Tools.roundingValue(importeNetoTotal, 2)).append("|")
                .append(ventaTB.getFechaVenta()).append("|")
                .append(ventaTB.getClienteTB().getIdAuxiliar()).append("|")
                .append(ventaTB.getClienteTB().getNumeroDocumento()).append("|");

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode(
                sb.toString(), BarcodeFormat.QR_CODE, 800, 800));

        Map<String, Object> map = new HashMap<>();
        map.put("LOGO", imgInputStream);
        map.put("ICON", imgInputStreamIcon);
        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
        map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL));
        map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO)
                + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
        map.put("PAGINAWEB", Session.COMPANY_PAGINAWEB);

        map.put("NOMBREDOCUMENTO", ventaTB.getTipoDocumentoTB().getNombre());
        map.put("NUMERODOCUMENTO", ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
        map.put("FECHA_VENTA", ventaTB.getFechaVenta());
        map.put("MONEDA", ventaTB.getMonedaTB().getNombre() + " - " + ventaTB.getMonedaTB().getAbreviado());

        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                ventaTB.getMonedaTB().getNombre()));
        map.put("DOUMENTO_CLIENTE", ventaTB.getClienteTB().getNumeroDocumento());
        map.put("INFORMACION_CLIENTE", ventaTB.getClienteTB().getInformacion());
        map.put("DIRECCION_CLIENTE", ventaTB.getClienteTB().getDireccion());
        map.put("CELULAR_CLIENTE", ventaTB.getClienteTB().getCelular());
        map.put("EMAIL_CLIENTE", ventaTB.getClienteTB().getEmail());
        map.put("FORMA_PAGO", ventaTB.getTipoName());

        map.put("TERMINOS_CONDICIONES", EmpresaADO.Terminos_Condiciones());
        map.put("NUMEROS_CUENTA", BancoADO.Listar_Banco_Mostrar());
        map.put("QR_DATA", qrImage);

        map.put("IMPORTE_BRUTO", ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
        map.put("DESCUENTO", ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(descuentoTotal, 2));
        map.put("SUB_IMPORTE", ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        map.put("IMPUESTO", ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
        map.put("IMPORTE_NETO", ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));

        return JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
    }

    public void mostrarReporte(String idVenta) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<JasperPrint> task = new Task<JasperPrint>() {
            @Override
            public JasperPrint call()
                    throws Exception {
                Object object = VentaADO.Obtener_Venta_ById(idVenta);
                if (object instanceof VentaTB) {

                    VentaTB ventaTB = (VentaTB) object;

                    double importeBrutoTotal = 0;
                    double descuentoTotal = 0;
                    double subImporteNetoTotal = 0;
                    double impuestoTotal = 0;
                    double importeNetoTotal = 0;

                    JSONArray array = new JSONArray();

                    for (SuministroTB ocdtb : ventaTB.getSuministroTBs()) {
                        JSONObject jsono = new JSONObject();
                        jsono.put("id", ocdtb.getId());
                        jsono.put("cantidad", Tools.roundingValue(ocdtb.getCantidad(), 2));
                        jsono.put("unidad", ocdtb.getUnidadCompraName());
                        jsono.put("producto", ocdtb.getClave() + "\n" + ocdtb.getNombreMarca());
                        jsono.put("precio", Tools.roundingValue(ocdtb.getPrecioVentaGeneral(), 2));
                        jsono.put("descuento", Tools.roundingValue(ocdtb.getDescuento(), 0));
                        jsono.put("importe", Tools.roundingValue(
                                ocdtb.getPrecioVentaGeneral() * (ocdtb.getCantidad() - ocdtb.getDescuento()),
                                2));
                        array.add(jsono);

                        double importeBruto = ocdtb.getPrecioVentaGeneral() * ocdtb.getCantidad();
                        double descuento = ocdtb.getDescuento();
                        double subImporteBruto = importeBruto - descuento;
                        double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(),
                                subImporteBruto);
                        double impuesto = Tools.calculateTax(ocdtb.getImpuestoTB().getValor(),
                                subImporteNeto);
                        double importeNeto = subImporteNeto + impuesto;

                        importeBrutoTotal += importeBruto;
                        descuentoTotal += descuento;
                        subImporteNetoTotal += subImporteNeto;
                        impuestoTotal += impuesto;
                        importeNetoTotal += importeNeto;
                    }
                    String json = new String(array.toJSONString().getBytes(), "UTF-8");
                    ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(json.getBytes());

                    InputStream icon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
                    InputStream logo = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
                    if (Session.COMPANY_IMAGE != null) {
                        logo = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                    }

                    File archivoc = new File("./report/VentaRealizada.jasper");
                    InputStream dir = new FileInputStream(archivoc.getPath());

                    // InputStream dir =
                    // getClass().getResourceAsStream("./report/VentaRealizada.jasper");

                    StringBuilder sb = new StringBuilder();
                    sb.append("|").append(Session.COMPANY_NUMERO_DOCUMENTO).append("|")
                            .append(ventaTB.getTipoDocumentoTB().getCodigoAlterno()).append("|")
                            .append(ventaTB.getSerie()).append("|")
                            .append(ventaTB.getNumeracion()).append("|")
                            .append(Tools.roundingValue(impuestoTotal, 2)).append("|")
                            .append(Tools.roundingValue(importeNetoTotal, 2)).append("|")
                            .append(ventaTB.getFechaVenta()).append("|")
                            .append(ventaTB.getClienteTB().getIdAuxiliar()).append("|")
                            .append(ventaTB.getClienteTB().getNumeroDocumento()).append("|");

                    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode(
                            sb.toString(), BarcodeFormat.QR_CODE, 800, 800));

                    /**
                     */
                    Map<String, Object> map = new HashMap<>();
                    map.put("LOGO", logo);
                    map.put("ICON", icon);
                    map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                    map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
                    map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                    map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL));
                    map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO)
                            + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
                    map.put("PAGINAWEB", Session.COMPANY_PAGINAWEB);

                    map.put("NOMBREDOCUMENTO", ventaTB.getTipoDocumentoTB().getNombre());
                    map.put("NUMERODOCUMENTO", ventaTB.getSerie() + "-" + Tools.formatNumber(ventaTB.getNumeracion()));
                    map.put("FECHA_VENTA", ventaTB.getFechaVenta());
                    map.put("MONEDA",
                            ventaTB.getMonedaTB().getNombre() + " - " + ventaTB.getMonedaTB().getAbreviado());

                    map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNetoTotal, 2), true,
                            ventaTB.getMonedaTB().getNombre()));
                    map.put("DOUMENTO_CLIENTE", ventaTB.getClienteTB().getNumeroDocumento());
                    map.put("INFORMACION_CLIENTE", ventaTB.getClienteTB().getInformacion());
                    map.put("DIRECCION_CLIENTE", ventaTB.getClienteTB().getDireccion());
                    map.put("CELULAR_CLIENTE", ventaTB.getClienteTB().getCelular());
                    map.put("EMAIL_CLIENTE", ventaTB.getClienteTB().getEmail());
                    map.put("FORMA_PAGO", ventaTB.getTipoName());

                    map.put("TERMINOS_CONDICIONES", EmpresaADO.Terminos_Condiciones());
                    map.put("NUMEROS_CUENTA", BancoADO.Listar_Banco_Mostrar());
                    map.put("QR_DATA", qrImage);

                    map.put("IMPORTE_BRUTO",
                            ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
                    map.put("DESCUENTO",
                            ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(descuentoTotal, 2));
                    map.put("SUB_IMPORTE",
                            ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
                    map.put("IMPUESTO",
                            ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
                    map.put("IMPORTE_NETO",
                            ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));

                    fileName = ventaTB.getTipoDocumentoTB().getNombre() + " " + ventaTB.getSerie() + "-"
                            + Tools.formatNumber(ventaTB.getNumeracion());

                    return JasperFillManager.fillReport(dir, map,
                            new JsonDataSource(jsonDataStream));
                }
                throw new Exception((String) object);
            }
        };

        task.setOnScheduled(w -> {
            Tools.showAlertNotification("/view/image/information_large.png",
                    "Reporte",
                    Tools.newLineString("Se envió los datos para generar el reporte."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Reporte",
                    Tools.newLineString("Se produjo un problema al generar el reporte."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnSucceeded(w -> {
            try {
                JasperPrint object = task.getValue();
                Tools.showAlertNotification("/view/image/succes_large.png",
                        "Reporte",
                        Tools.newLineString("Se genero correctamente el reporte de venta."),
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

            } catch (IOException ex) {
                Tools.showAlertNotification("/view/image/error_large.png",
                        "Reporte",
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
