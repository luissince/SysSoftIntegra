package controller.tools.modelticket;

import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.Session;
import controller.tools.Tools;
import java.awt.print.Book;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import model.SuministroTB;

public class TicketPreVenta {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private final ConvertMonedaCadena monedaCadena;

    private String numeroDocumento;

    private String informacionCliente;

    private String celularCliente;

    private String direccionCliente;

    private String monedaSimbolo;

    private double importeBruto;

    private double descuentoBruto;

    private double subImporteNeto;

    private double impuestoNeto;

    private double importeNeto;

    private ObservableList<SuministroTB> tvList;

    public TicketPreVenta(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void loadDataTicket(
            String numeroDocumento,
            String informacionCliente,
            String celularCliente,
            String direccionCliente,
            String monedaSimbolo,
            double importeBruto,
            double descuentoBruto,
            double subImporteNeto,
            double impuestoNeto,
            double importeNeto,
            ObservableList<SuministroTB> tvList) {
        this.numeroDocumento = numeroDocumento;
        this.informacionCliente = informacionCliente;
        this.celularCliente = celularCliente;
        this.direccionCliente = direccionCliente;
        this.monedaSimbolo = monedaSimbolo;
        this.importeBruto = importeBruto;
        this.descuentoBruto = descuentoBruto;
        this.subImporteNeto = subImporteNeto;
        this.impuestoNeto = impuestoNeto;
        this.importeNeto = importeNeto;
        this.tvList = tvList;
    }

    public void imprimir() {
        if (!Session.ESTADO_IMPRESORA_PRE_VENTA && Tools.isText(Session.NOMBRE_IMPRESORA_PRE_VENTA) && Tools.isText(Session.FORMATO_IMPRESORA_PRE_VENTA)) {
            Tools.AlertMessageWarning(node, "Venta", "No esta configurado la ruta de impresión, ve a la sección configuración/impresora.");
            return;
        }

        if (Session.FORMATO_IMPRESORA_PRE_VENTA.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_PRE_VENTA_ID == 0 && Tools.isText(Session.TICKET_PRE_VENTA_RUTA)) {
                Tools.AlertMessageWarning(node, "Venta", "No hay un diseño predeterminado para la impresión, configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterPreVenta(Session.NOMBRE_IMPRESORA_PRE_VENTA, Session.CORTAPAPEL_IMPRESORA_PRE_VENTA);
            }
        } else if (Session.FORMATO_IMPRESORA_PRE_VENTA.equalsIgnoreCase("a4")) {
//            executeProcessPrinterPreVenta(Session.NOMBRE_IMPRESORA_PRE_VENTA);
        } else {
            Tools.AlertMessageWarning(node, "Venta", "Error al validar el formato de impresión, configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterPreVenta(String printerName, boolean printerCut) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() throws PrintException {

                try {
                    if (Session.DESING_IMPRESORA_PRE_VENTA.equalsIgnoreCase("withdesing")) {
                        billPrintable.loadEstructuraTicket(Session.TICKET_PRE_VENTA_ID, Session.TICKET_PRE_VENTA_RUTA, hbEncabezado, hbDetalleCabecera, hbPie);

                        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
                            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
                            billPrintable.hbEncebezado(box,
                                    "",
                                    "NOTA DE VENTA",
                                    "---",
                                    numeroDocumento.toUpperCase(),
                                    informacionCliente.toUpperCase(),
                                    celularCliente.toUpperCase(),
                                    direccionCliente.toUpperCase(),
                                    "00000000",
                                    Tools.getDate(),
                                    Tools.getTime(),
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
                        for (int m = 0; m < tvList.size(); m++) {
                            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                                HBox hBox = new HBox();
                                hBox.setId("dc_" + m + "" + i);
                                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                                billPrintable.hbDetalle(hBox, box, tvList, m);
                                hbDetalle.getChildren().add(hBox);
                            }
                        }

                        for (int i = 0; i < hbPie.getChildren().size(); i++) {
                            HBox box = ((HBox) hbPie.getChildren().get(i));
                            billPrintable.hbPie(box, monedaSimbolo,
                                    Tools.roundingValue(importeBruto, 2),
                                    Tools.roundingValue(descuentoBruto, 2),
                                    Tools.roundingValue(subImporteNeto, 2),
                                    Tools.roundingValue(impuestoNeto, 2),
                                    Tools.roundingValue(importeNeto, 2),
                                    "TARJETA",
                                    "EFECTIVO",
                                    "VUELTO",
                                    numeroDocumento.toUpperCase(),
                                    informacionCliente.toUpperCase(),
                                    "CODIGO DE VENTA",
                                    celularCliente.toUpperCase(),
                                    monedaCadena.Convertir(Tools.roundingValue(importeNeto, 2), true, ""),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "");
                        }

                        billPrintable.generateTicketPrint(hbEncabezado, hbDetalle, hbPie);
                        PrintService printService = billPrintable.findPrintService(printerName, PrinterJob.lookupPrintServices());
                        if (printService == null) {
                            return "error_name";
                        } else {
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
                        }
                    } else {
                        billPrintable.loadEstructuraTicket(Session.TICKET_PRE_VENTA_ID, Session.TICKET_PRE_VENTA_RUTA, hbEncabezado, hbDetalleCabecera, hbPie);
                        ArrayList<HBox> object = new ArrayList<>();
                        int rows = 0;
                        int lines = 0;
                        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
                            object.add((HBox) hbEncabezado.getChildren().get(i));
                            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
                            rows++;
                            lines += billPrintable.hbEncebezado(box,
                                    "",
                                    "NOTA DE VENTA",
                                    "---",
                                    numeroDocumento.toUpperCase(),
                                    informacionCliente.toUpperCase(),
                                    celularCliente.toUpperCase(),
                                    direccionCliente.toUpperCase(),
                                    "00000000",
                                    Tools.getDate(),
                                    Tools.getTime(),
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

                        for (int m = 0; m < tvList.size(); m++) {
                            for (int i = 0; i < hbDetalleCabecera.getChildren().size(); i++) {
                                HBox hBox = new HBox();
                                hBox.setId("dc_" + m + "" + i);
                                HBox box = ((HBox) hbDetalleCabecera.getChildren().get(i));
                                rows++;
                                lines += billPrintable.hbDetalle(hBox, box, tvList, m);
                                object.add(hBox);
                            }
                        }

                        for (int i = 0; i < hbPie.getChildren().size(); i++) {
                            object.add((HBox) hbPie.getChildren().get(i));
                            HBox box = ((HBox) hbPie.getChildren().get(i));
                            rows++;
                            lines += billPrintable.hbPie(box, monedaSimbolo,
                                    Tools.roundingValue(importeBruto, 2),
                                    Tools.roundingValue(descuentoBruto, 2),
                                    Tools.roundingValue(subImporteNeto, 2),
                                    Tools.roundingValue(impuestoNeto, 2),
                                    Tools.roundingValue(importeNeto, 2),
                                    "TARJETA",
                                    "EFECTIVO",
                                    "VUELTO",
                                    numeroDocumento.toUpperCase(),
                                    informacionCliente.toUpperCase(),
                                    "CODIGO DE VENTA",
                                    celularCliente.toUpperCase(),
                                    monedaCadena.Convertir(Tools.roundingValue(importeNeto, 2), true, ""),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "");
                        }
                        return billPrintable.modelTicket(rows + lines + 1 + 5, lines, object, printerName, printerCut);
                    }
                } catch (PrinterException | IOException | PrintException ex) {
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
                        Tools.newLineString("Error en encontrar la impresora " + printerName + " por problemas de puerto, driver o configuración."),
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
                        Tools.newLineString("Se producto un problema por problemas de la impresora " + result),
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

}
