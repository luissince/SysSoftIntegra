package controller.tools.modelticket;

import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AjusteInventarioADO;
import model.AjusteInventarioTB;
import model.MovimientoInventarioDetalleTB;
import model.SuministroTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JsonDataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TicketMovimiento {

    private final Node node;

    private final BillPrintable billPrintable;

    private final ConvertMonedaCadena monedaCadena;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    public TicketMovimiento(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
    }

    public void reporteMovimiento(String idMovimientoInventario) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                try {
                    Object object = AjusteInventarioADO.Obtener_Movimiento_Inventario_By_Id(idMovimientoInventario);
                    if (object instanceof AjusteInventarioTB) {

                        AjusteInventarioTB ajusteInventarioTB = (AjusteInventarioTB) object;

                        JSONArray array = new JSONArray();
                        int count = 0;
                        for (MovimientoInventarioDetalleTB detalleTB : ajusteInventarioTB.getMovimientoInventarioDetalleTBs()) {
                            count++;
                            JSONObject jsono = new JSONObject();
                            jsono.put("id", count);
                            jsono.put("descripcion", detalleTB.getSuministroTB().getClave() + "\n" + detalleTB.getSuministroTB().getNombreMarca());
                            jsono.put("categoria", detalleTB.getSuministroTB().getCategoriaName());
                            jsono.put("marca", detalleTB.getSuministroTB().getMarcaName());
                            jsono.put("cantidad", detalleTB.getCantidad() + "\n" + detalleTB.getSuministroTB().getUnidadCompraName());
                            array.add(jsono);
                        }

                        String json = new String(array.toJSONString().getBytes(), "UTF-8");
                        ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(json.getBytes());

                        InputStream dir = getClass().getResourceAsStream("/report/AjusteInventario.jasper");

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

                        map.put("TIPO", ajusteInventarioTB.isTipoAjuste() ? "INCREMENTO" : "DECREMENTO");
                        map.put("METODO", ajusteInventarioTB.getTipoMovimientoName());
                        map.put("FECHA", ajusteInventarioTB.getFecha() + " " + ajusteInventarioTB.getHora());
                        map.put("OBSERVACION", ajusteInventarioTB.getObservacion());
                        map.put("ALMACEN", ajusteInventarioTB.getAlmacenTB().getNombre());
                        map.put("ITEMS", String.valueOf(ajusteInventarioTB.getMovimientoInventarioDetalleTBs().size()));

                        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
                        return jasperPrint;
                    } else {
                        return (String) object;
                    }
                } catch (JRException | UnsupportedEncodingException ex) {
                    return ex.getLocalizedMessage();
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
                            Tools.newLineString("Se completo la creación del reporte de ajuste de inventario."),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);

                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName("Reporte de Ajuste de Inventario");
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Reporte de Ajuste de Inventario");
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
                    Tools.newLineString("Se está generando el reporte de ajuste de inventario."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

}
