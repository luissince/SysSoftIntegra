package controller.tools.modelticket;

import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.Session;
import controller.tools.Tools;
import java.awt.print.Book;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javax.print.DocPrintJob;
import org.controlsfx.control.Notifications;

public class TicketInventario {

    private final Node node;

    private final BillPrintable billPrintable;

    private final ConvertMonedaCadena monedaCadena;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketInventario(Node node, BillPrintable billPrintable, ConvertMonedaCadena monedaCadena, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.monedaCadena = monedaCadena;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir() {
//        if (!Session.ESTADO_IMPRESORA_VENTA && Session.NOMBRE_IMPRESORA_VENTA == null) {
//            Tools.AlertMessageWarning(vbWindow, "Inventario general", "No hay ruta de impresión, no se ha configurado la ruta de impresión");
//            return;
//        }
//
//        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
//            Thread t = new Thread(runnable);
//            t.setDaemon(true);
//            return t;
//        });
//        try {
//            Task<String> task = new Task<String>() {
//                @Override
//                public String call() {
//                    try {
//                        billPrintable.loadEstructuraTicket(idTicket, rutaTicket, apEncabezado, apDetalleCabecera, apPie);
//
//                        for (int i = 0; i < apEncabezado.getChildren().size(); i++) {
//                            HBox box = ((HBox) apEncabezado.getChildren().get(i));
//                            billPrintable.hbEncebezado(box,
//                                    "TICKET",
//                                    "00000000",
//                                    "SIN DOCUMENTO",
//                                    "PUBLICO GENERAL",
//                                    "SIN CELULAR",
//                                    "SIN DIRECCION",
//                                    "SIN CODIGO",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "0",
//                                    "0",
//                                    "0",
//                                    "0",
//                                    "0");
//                        }
//
//                        AnchorPane hbDetalle = new AnchorPane();
//                        for (int m = 0; m < tvList.getItems().size(); m++) {
//                            for (int i = 0; i < apDetalleCabecera.getChildren().size(); i++) {
//                                HBox hBox = new HBox();
//                                hBox.setId("dc_" + m + "" + i);
//                                HBox box = ((HBox) apDetalleCabecera.getChildren().get(i));
//                                billPrintable.hbDetalle(hBox, box, tvList.getItems(), m);
//                                hbDetalle.getChildren().add(hBox);
//                            }
//                        }
//
//                        for (int i = 0; i < apPie.getChildren().size(); i++) {
//                            HBox box = ((HBox) apPie.getChildren().get(i));
//                            billPrintable.hbPie(box, "",
//                                    Tools.roundingValue(0, 2),
//                                    "-" + Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    Tools.roundingValue(0, 2),
//                                    "SIN DOCUMENTO",
//                                    "PUBLICO GENERAL", "SIN CODIGO", "SIN CELULAR", "",
//                                    "",
//                                    "",
//                                    "",
//                                    "",
//                                    "");
//                        }
//
//                        billPrintable.generatePDFPrint(apEncabezado, hbDetalle, apPie);
//
//                        DocPrintJob job = billPrintable.findPrintService(Session.NOMBRE_IMPRESORA_VENTA, PrinterJob.lookupPrintServices()).createPrintJob();
//
//                        if (job != null) {
//                            PrinterJob pj = PrinterJob.getPrinterJob();
//                            pj.setPrintService(job.getPrintService());
//                            pj.setJobName(Session.NOMBRE_IMPRESORA_VENTA);
//                            Book book = new Book();
//                            book.append(billPrintable, billPrintable.getPageFormat(pj));
//                            pj.setPageable(book);
//                            pj.print();
//                            if (Session.CORTAPAPEL_IMPRESORA_VENTA) {
////                                billPrintable.printCortarPapel(Session.NOMBRE_IMPRESORA);
//                            }
//                            return "completed";
//                        } else {
//                            return "error_name";
//                        }
//                    } catch (PrinterException/* | IOException | PrintException*/ ex) {
//                        return "Error en imprimir: " + ex.getLocalizedMessage();
//                    }
//                }
//            };
//
//            task.setOnSucceeded(w -> {
//                String result = task.getValue();
//                if (result.equalsIgnoreCase("completed")) {
//                    Image image = new Image("/view/image/information_large.png");
//                    Notifications notifications = Notifications.create()
//                            .title("Envío de impresión")
//                            .text("Se completo el proceso de impresión correctamente.")
//                            .graphic(new ImageView(image))
//                            .hideAfter(Duration.seconds(5))
//                            .position(Pos.BOTTOM_RIGHT)
//                            .onAction(n -> {
//                                Tools.println(n);
//                            });
//                    notifications.darkStyle();
//                    notifications.show();
//                } else if (result.equalsIgnoreCase("error_name")) {
//                    Image image = new Image("/view/image/warning_large.png");
//                    Notifications notifications = Notifications.create()
//                            .title("Envío de impresión")
//                            .text("Error en encontrar el nombre de la impresión por problemas de puerto o driver.")
//                            .graphic(new ImageView(image))
//                            .hideAfter(Duration.seconds(10))
//                            .position(Pos.CENTER)
//                            .onAction(n -> {
//                                Tools.println(n);
//                            });
//                    notifications.darkStyle();
//                    notifications.show();
//                } else {
//                    Image image = new Image("/view/image/error_large.png");
//                    Notifications notifications = Notifications.create()
//                            .title("Envío de impresión")
//                            .text("Error en la configuración de su impresora: " + result)
//                            .graphic(new ImageView(image))
//                            .hideAfter(Duration.seconds(10))
//                            .position(Pos.CENTER)
//                            .onAction(n -> {
//                                Tools.println(n);
//                            });
//                    notifications.darkStyle();
//                    notifications.show();
//                }
//            });
//
//            task.setOnFailed(w -> {
//                Image image = new Image("/view/image/warning_large.png");
//                Notifications notifications = Notifications.create()
//                        .title("Envío de impresión")
//                        .text("Se produjo un problema en el proceso de envío, \n intente nuevamente o comuníquese \ncon su proveedor del sistema.")
//                        .graphic(new ImageView(image))
//                        .hideAfter(Duration.seconds(10))
//                        .position(Pos.BOTTOM_RIGHT)
//                        .onAction(n -> {
//                            Tools.println(n);
//                        });
//                notifications.darkStyle();
//                notifications.show();
//            });
//
//            task.setOnScheduled(w -> {
//                Image image = new Image("/view/image/print.png");
//                Notifications notifications = Notifications.create()
//                        .title("Envío de impresión")
//                        .text("Se envió la impresión a la cola, este\n proceso puede tomar unos segundos.")
//                        .graphic(new ImageView(image))
//                        .hideAfter(Duration.seconds(5))
//                        .position(Pos.BOTTOM_RIGHT)
//                        .onAction(n -> {
//                            Tools.println(n);
//                        });
//                notifications.darkStyle();
//                notifications.show();
//            });
//            exec.execute(task);
//
//        } finally {
//            exec.shutdown();
//        }
    }

}
