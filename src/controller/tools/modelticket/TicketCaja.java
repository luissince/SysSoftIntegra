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
import javax.print.PrintService;
import model.CajaADO;
import model.CajaTB;
import model.MovimientoCajaTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketCaja {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketCaja(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir(String idCaja) {
        if (!Session.ESTADO_IMPRESORA_CORTE_CAJA && Tools.isText(Session.NOMBRE_IMPRESORA_CORTE_CAJA) && Tools.isText(Session.FORMATO_IMPRESORA_CORTE_CAJA)) {
            Tools.AlertMessageWarning(node, "Corte de Caja", "No esta configurado la ruta de impresión ve a la sección configuración/impresora.");
            return;
        }
        if (Session.FORMATO_IMPRESORA_CORTE_CAJA.equalsIgnoreCase("ticket")) {
            if (Session.TICKET_CORTE_CAJA_ID == 0 && Session.TICKET_CORTE_CAJA_RUTA.equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(node, "Corte de Caja", "No hay un diseño predeterminado para la impresión configure su ticket en la sección configuración/tickets.");
            } else {
                executeProcessPrinterCorteCaja(
                        idCaja,
                        Session.DESING_IMPRESORA_CORTE_CAJA,
                        Session.TICKET_CORTE_CAJA_ID,
                        Session.TICKET_CORTE_CAJA_RUTA,
                        Session.NOMBRE_IMPRESORA_CORTE_CAJA,
                        Session.CORTAPAPEL_IMPRESORA_CORTE_CAJA
                );
            }
        } else if (Session.FORMATO_IMPRESORA_CORTE_CAJA.equalsIgnoreCase("a4")) {

        } else {
            Tools.AlertMessageWarning(node, "Corte de Caja", "Error al validar el formato de impresión configure en la sección configuración/impresora.");
        }
    }

    private void executeProcessPrinterCorteCaja(String idCaja, String desing, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object arrayList = CajaADO.ListarMovimientoPorById(idCaja);
                if (arrayList instanceof Object[]) {
                    try {
                        Object object[] = (Object[]) arrayList;
                        CajaTB cajaTB = (CajaTB) object[0];
                        if (desing.equalsIgnoreCase("withdesing")) {
                            return printTicketWithDesingCorteCaja(cajaTB, (ArrayList<Double>) object[1], ticketId, ticketRuta, nombreImpresora, cortaPapel);
                        } else {
                            return "empty";
//                                billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);
//                                return printTicketNoDesingCorteCaja(cajaTB, (ArrayList<Double>) object[1], nombreImpresora, cortaPapel);
                        }
                    } catch (PrinterException | IOException | PrintException ex) {
                        return "Error en imprimir: " + ex.getLocalizedMessage();
                    }
                } else {
                    return (String) arrayList;
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

    private String printTicketWithDesingCorteCaja(CajaTB cajaTB, ArrayList<Double> arrayList, int ticketId, String ticketRuta, String nombreImpresora, boolean cortaPapel) throws PrinterException, PrintException, IOException {
        billPrintable.loadEstructuraTicket(ticketId, ticketRuta, hbEncabezado, hbDetalleCabecera, hbPie);

        double montoInicial = arrayList.get(0);

        double ventaEfectivo = arrayList.get(1);
        double ventaTarjeta = arrayList.get(2);
        double ventaDeposito = arrayList.get(3);

        double ingresoEfectivo = arrayList.get(4);
        double ingresoTarjeta = arrayList.get(5);
        double ingresoDeposito = arrayList.get(6);

        double salidaEfectivo = arrayList.get(7);
        double salidaTarjeta = arrayList.get(8);
        double salidaDeposito = arrayList.get(9);

        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
            billPrintable.hbEncebezado(box,
                    "",
                    "CORTE DE CAJA",
                    cajaTB.getIdCaja(),
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    cajaTB.getFechaApertura(),
                    cajaTB.getHoraApertura(),
                    cajaTB.getFechaCierre(),
                    cajaTB.getHoraCierre(),
                    Tools.roundingValue(cajaTB.getCalculado(), 2),
                    Tools.roundingValue(cajaTB.getContado(), 2),
                    Tools.roundingValue(cajaTB.getDiferencia(), 2),
                    cajaTB.getEmpleadoTB().getNumeroDocumento(),
                    cajaTB.getEmpleadoTB().getApellidos(),
                    cajaTB.getEmpleadoTB().getCelular(),
                    cajaTB.getEmpleadoTB().getDireccion(),
                    "",
                    "",
                    "",
                    "",
                    Tools.roundingValue(montoInicial, 2),
                    Tools.roundingValue(ventaEfectivo, 2),
                    Tools.roundingValue(ventaTarjeta, 2),
                    Tools.roundingValue(ventaDeposito, 2),
                    Tools.roundingValue(ingresoEfectivo, 2),
                    Tools.roundingValue(salidaEfectivo, 2),
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
                    "0.00",
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

        billPrintable.generatePDFPrint(hbEncabezado, hbDetalle, hbPie);

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

    public void mostrarReporte(String idCaja) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                try {
                    Object objects = CajaADO.ListarMovimientoPorById(idCaja);
                    if (objects instanceof Object[]) {
                        Object[] objectData = (Object[]) objects;
                        CajaTB cajaTB = (CajaTB) objectData[0];
                        ArrayList<Double> arrayList = (ArrayList<Double>) objectData[1];
                        ArrayList<MovimientoCajaTB> arrList = (ArrayList<MovimientoCajaTB>) objectData[2];

                        ArrayList newList = new ArrayList();
                        arrList.stream().map(mc -> {
                            MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                            movimientoCajaTB.setId(mc.getId());
                            movimientoCajaTB.setFechaMovimiento(mc.getFechaMovimiento());
                            movimientoCajaTB.setHoraMovimiento(mc.getHoraMovimiento());
                            movimientoCajaTB.setComentario(mc.getComentario());
                            movimientoCajaTB.setConcepto(
                                    mc.getTipoMovimiento() == 1 ? "MONTO INICIAL"
                                    : mc.getTipoMovimiento() == 2 ? "VENTA EN EFECTIVO"
                                    : mc.getTipoMovimiento() == 3 ? "VENTA CON TARJETA"
                                    : mc.getTipoMovimiento() == 4 ? "INGRESO EN EFECTIVO"
                                    : mc.getTipoMovimiento() == 5 ? "SALIDA EN EFECTIVO"
                                    : mc.getTipoMovimiento() == 6 ? "VENTA CON DEPOSITO"
                                    : mc.getTipoMovimiento() == 7 ? "INGRESO CON TARJETA"
                                    : mc.getTipoMovimiento() == 8 ? "INGRESO CON DEPOSITO"
                                    : mc.getTipoMovimiento() == 9 ? "SALIDA CON TARJETA"
                                    : "SALIDA CON DEPOSITO");
                            movimientoCajaTB.setEntrada(
                                    mc.getTipoMovimiento() == 1
                                    || mc.getTipoMovimiento() == 2
                                    || mc.getTipoMovimiento() == 3
                                    || mc.getTipoMovimiento() == 4
                                    || mc.getTipoMovimiento() == 6
                                    || mc.getTipoMovimiento() == 7
                                    || mc.getTipoMovimiento() == 8
                                    ? mc.getMonto() : 0);
                            movimientoCajaTB.setSalida(
                                    mc.getTipoMovimiento() == 5
                                    || mc.getTipoMovimiento() == 9
                                    || mc.getTipoMovimiento() == 10
                                    ? mc.getMonto() : 0);
                            return movimientoCajaTB;
                        }).forEachOrdered(movimientoCajaTB -> {
                            newList.add(movimientoCajaTB);
                        });

                        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_ICON);
                        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

                        if (Session.COMPANY_IMAGE != null) {
                            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                        }

                        double montoInicial = arrayList.get(0);

                        double ventaEfectivo = arrayList.get(1);
                        double ventaTarjeta = arrayList.get(2);
                        double ventaDeposito = arrayList.get(3);

                        double ingresoEfectivo = arrayList.get(4);
                        double ingresoTarjeta = arrayList.get(5);
                        double ingresoDeposito = arrayList.get(6);

                        double salidaEfectivo = arrayList.get(7);
                        double salidaTarjeta = arrayList.get(8);
                        double salidaDeposito = arrayList.get(9);

                        InputStream dir = getClass().getResourceAsStream("/report/CortedeCaja.jasper");

                        Map map = new HashMap();
                        map.put("LOGO", imgInputStream);
                        map.put("ICON", imgInputStreamIcon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("EMAIL", Session.COMPANY_EMAIL);
                        map.put("TELEFONOCELULAR", Session.COMPANY_TELEFONO + " - " + Session.COMPANY_CELULAR);

                        map.put("INICIODETURNO", cajaTB.getFechaApertura());
                        map.put("HORAINICIO", cajaTB.getHoraApertura());
                        map.put("FINDETURNO", cajaTB.getFechaCierre());
                        map.put("HORAFIN", cajaTB.getHoraCierre());
                        map.put("CONTADO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getContado(), 2));
                        map.put("CALCULADO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getCalculado(), 2));
                        map.put("DIFERENCIA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getDiferencia(), 2));
                        map.put("CAJEROASISTENTE", cajaTB.getEmpleadoTB().getNombres() + " " + cajaTB.getEmpleadoTB().getApellidos());

                        map.put("BASE", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoInicial, 2));

                        map.put("VENTASENEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                        map.put("VENTASCONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                        map.put("VENTASCONDEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                        map.put("INGRESODEEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                        map.put("INGRESOCONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                        map.put("INGRESOCONDEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                        map.put("SALIDADEEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                        map.put("SALIDACONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                        map.put("SALIDADEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                        map.put("TOTAL", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue((montoInicial + ventaEfectivo + ingresoEfectivo) - salidaEfectivo, 2));

                        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(newList));

                        return jasperPrint;
                    } else {
                        return (String) objects;
                    }
                } catch (JRException ex) {
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
                    controller.setJasperPrint((JasperPrint) result);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Corte de Caja");
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

//    private String printTicketNoDesingCorteCaja(CajaTB cajaTB, ArrayList<Double> movimientoCajaTBs, String nombreImpresora, boolean cortaPapel) {
//        ArrayList<HBox> object = new ArrayList<>();
//        int rows = 0;
//        int lines = 0;
//        for (int i = 0; i < hbEncabezado.getChildren().size(); i++) {
//            object.add((HBox) hbEncabezado.getChildren().get(i));
//            HBox box = ((HBox) hbEncabezado.getChildren().get(i));
//            rows++;
//            lines += billPrintable.hbEncebezado(box,
//                    "CORTE DE CAJA",
//                    cajaTB.getIdCaja(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    "",
//                    cajaTB.getFechaApertura(),
//                    cajaTB.getHoraApertura(),
//                    cajaTB.getFechaCierre(),
//                    cajaTB.getHoraCierre(),
//                    Tools.roundingValue(cajaTB.getCalculado(), 2),
//                    Tools.roundingValue(cajaTB.getContado(), 2),
//                    Tools.roundingValue(cajaTB.getDiferencia(), 2),
//                    cajaTB.getEmpleadoTB().getNumeroDocumento(),
//                    cajaTB.getEmpleadoTB().getApellidos(),
//                    cajaTB.getEmpleadoTB().getCelular(),
//                    cajaTB.getEmpleadoTB().getDireccion(),
//                    "",
//                    "",
//                    "",
//                    "",
//                    Tools.roundingValue(movimientoCajaTBs.get(0), 2),
//                    Tools.roundingValue(movimientoCajaTBs.get(1), 2),
//                    Tools.roundingValue(movimientoCajaTBs.get(2), 2),
//                    Tools.roundingValue(movimientoCajaTBs.get(3), 2),
//                    Tools.roundingValue(movimientoCajaTBs.get(4), 2)
//            );
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
//                    "",
//                    "",
//                    "",
//                    "",
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
