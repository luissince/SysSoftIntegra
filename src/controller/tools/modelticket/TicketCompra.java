package controller.tools.modelticket;

import controller.reporte.FxReportViewController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
import model.CompraADO;
import model.CompraTB;
import model.DetalleCompraTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class TicketCompra {

    private final Node node;

    private final BillPrintable billPrintable;

    private final AnchorPane hbEncabezado;

    private final AnchorPane hbDetalleCabecera;

    private final AnchorPane hbPie;

    private final ConvertMonedaCadena monedaCadena;

    public TicketCompra(Node node, BillPrintable billPrintable, AnchorPane hbEncabezado, AnchorPane hbDetalleCabecera, AnchorPane hbPie, ConvertMonedaCadena monedaCadena) {
        this.node = node;
        this.billPrintable = billPrintable;
        this.hbEncabezado = hbEncabezado;
        this.hbDetalleCabecera = hbDetalleCabecera;
        this.hbPie = hbPie;
        this.monedaCadena = monedaCadena;
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
                try {
                    Object object = CompraADO.ListCompletaDetalleCompra(idCompra);
                    if (object instanceof Object[]) {
                        Object[] objects = (Object[]) object;
                        CompraTB compraTB = (CompraTB) objects[0];
                        ObservableList<DetalleCompraTB> empList = (ObservableList<DetalleCompraTB>) objects[1];

                        double subImporteNeto = 0;
                        double impuesto = 0;
                        double importeNeto = 0;

                        ArrayList<DetalleCompraTB> list = new ArrayList();
                        for (DetalleCompraTB e : empList) {
                            subImporteNeto += (e.getSubImporteNeto() * e.getCantidad());
                            impuesto += (e.getImpuestoGenerado() * e.getCantidad());
                            importeNeto += (e.getImporteNeto() * e.getCantidad());

                            DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                            detalleCompraTB.setId(e.getId());
                            detalleCompraTB.setCantidad(e.getCantidad());
                            detalleCompraTB.setMedida(e.getSuministroTB().getUnidadCompraName());
                            detalleCompraTB.setDescripcion(e.getSuministroTB().getClave() + "\n" + e.getSuministroTB().getNombreMarca());
                            detalleCompraTB.setDescuento(e.getDescuento());
                            detalleCompraTB.setPrecioCompra(e.getPrecioCompra());
                            list.add(detalleCompraTB);
                        }

                        if (list.isEmpty()) {
                            return "No hay registros para mostrar en el reporte.";
                        }

                        InputStream dir = getClass().getResourceAsStream("/report/CompraRealizada.jasper");

                        InputStream logo = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

                        InputStream icon = getClass().getResourceAsStream(FilesRouters.IMAGE_ICON);

                        Map map = new HashMap();
                        map.put("LOGO", logo);
                        map.put("ICON", icon);
                        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
                        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
                        map.put("TELEFONOCELULAR", Tools.textShow("TEL.: ", Session.COMPANY_TELEFONO) + " " + Tools.textShow("CEL.: ", Session.COMPANY_CELULAR));
                        map.put("EMAIL", Tools.textShow("EMAIL: ", Session.COMPANY_EMAIL.toUpperCase()));
                        map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C ", Session.COMPANY_NUMERO_DOCUMENTO));
                        map.put("PAGINAWEB", Tools.textShow("", Session.COMPANY_PAGINAWEB));
                        map.put("NUMEROCOMPRA", idCompra);

                        map.put("FECHAELABORACION", compraTB.getFechaCompra().toUpperCase());
                        map.put("MONEDA", compraTB.getMonedaTB().getNombre());
                        map.put("DOCUMENTOPROVEEDOR", compraTB.getProveedorTB().getNumeroDocumento());
                        map.put("DATOSPROVEEDOR", compraTB.getProveedorTB().getRazonSocial());
                        map.put("DIRECCIONPROVEEDOR", Tools.textShow("", compraTB.getProveedorTB().getDireccion()));
                        map.put("PROVEEDORTELEFONOS", Tools.textShow("TEL.: ", compraTB.getProveedorTB().getTelefono()) + " " + Tools.textShow("CEL.: ", compraTB.getProveedorTB().getCelular()));
                        map.put("PROVEEDOREMAIL", Tools.textShow("", compraTB.getProveedorTB().getEmail()));
                        map.put("COMPROBANTE", compraTB.getSerie().toUpperCase() + "-" + compraTB.getNumeracion());

                        map.put("NOTAS", compraTB.getNotas());
                        map.put("SIMBOLO", compraTB.getMonedaNombre());
                        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(compraTB.getTotal(), 2), true, compraTB.getMonedaTB().getNombre()));

                        map.put("VALOR_VENTA", Tools.roundingValue(0, 2));
                        map.put("DESCUENTO", Tools.roundingValue(0, 2));
                        map.put("SUB_IMPORTE", Tools.roundingValue(subImporteNeto, 2));
                        map.put("IMPUESTO_TOTAL", Tools.roundingValue(impuesto, 2));
                        map.put("IMPORTE_TOTAL", Tools.roundingValue(importeNeto, 2));

                        return JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(list));
                    } else {
                        return (String) object;
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

}
