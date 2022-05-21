package controller.tools.modelticket;

import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import model.TrasladoADO;
import model.TrasladoTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketTraslado {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketTraslado(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
    }

    public void imprimir(String idTraslado) {

    }

    public void mostrarReporte(String idTraslado) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws JRException {
                Object object = TrasladoADO.ObtenerTrasladoById(idTraslado);
                if (object instanceof TrasladoTB) {
                    TrasladoTB trasladoTB = (TrasladoTB) object;
                    
                    InputStream dir = getClass().getResourceAsStream("/report/TrasladoInventario.jasper");

                    InputStream logo = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
                    if (Session.COMPANY_IMAGE != null) {
                        logo = new ByteArrayInputStream(Session.COMPANY_IMAGE);
                    }

                    Map map = new HashMap();
                    map.put("LOGO", logo);
                    map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                    map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
                    map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                    map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL));
                    map.put("TELEFONOCELULAR", Tools.textShow("TELÉFONO: ", Session.COMPANY_TELEFONO) + Tools.textShow(" CELULAR: ", Session.COMPANY_CELULAR));
                    map.put("PAGINAWEB", Session.COMPANY_PAGINAWEB);

                    map.put("FECHA_ENVIO", trasladoTB.getFecha());
                    map.put("FECHA_SALIDA", trasladoTB.getFechaTraslado());
                    map.put("LUGAR_PARTIDA", trasladoTB.getPuntoPartida());
                    map.put("LUGAR_LLEGADA", trasladoTB.getPuntoLlegada());
                    map.put("USUARIO", trasladoTB.getEmpleadoTB().getApellidos() + ", " + trasladoTB.getEmpleadoTB().getNombres());
                    map.put("CANTIDAD_ITEMS", trasladoTB.getHistorialTBs().size() + "");
                    map.put("TIPO", trasladoTB.getTipo() == 1 ? "INTERNO" : "EXTERNO");

                    JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(trasladoTB.getHistorialTBs()));
                    return jasperPrint;
                } else {
                    return (String) object;
                }
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            try {
                if (object instanceof JasperPrint) {
                    Tools.showAlertNotification(
                            "/view/image/information_large.png",
                            "Reporte",
                            Tools.newLineString("Se completo la creación del reporte de traslado."),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);

                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName("Reporte de Traslado");
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Reporte de Traslado");
                    stage.setResizable(true);
                    stage.show();
                    stage.requestFocus();
                } else {
                    Tools.showAlertNotification(
                            "/view/image/warning_large.png",
                            "Reporte",
                            Tools.newLineString((String) object),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);
                }
            } catch (HeadlessException | IOException ex) {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Reporte",
                        Tools.newLineString("Error al generar el reporte : " + ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Reporte",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/pdf.png",
                    "Reporte",
                    Tools.newLineString("Se está generando el reporte de traslado."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

}
