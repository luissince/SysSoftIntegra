package controller.operaciones.notacredito;

import controller.menus.FxPrincipalController;
import controller.reporte.FxReportViewController;
import controller.tools.ConvertMonedaCadena;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.NotaCreditoADO;
import model.NotaCreditoDetalleTB;
import model.NotaCreditoTB;
import model.SuministroTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class FxNotaCreditoDetalleController implements Initializable {

    @FXML
    private ScrollPane spWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblFechaRegistro;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lblClienteCelulares;
    @FXML
    private Text lbCorreoElectronico;
    @FXML
    private Text lbDireccion;
    @FXML
    private Text lblNotaCredito;
    @FXML
    private Text lblComprobanteReferencia;
    @FXML
    private Text lblMotivoAnulacion;
    @FXML
    private Text lblNotaCreditoSerieNumeracion;
    @FXML
    private Text lblSerieNumeracionReferencia;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblValorVenta;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubImporteNeto;
    @FXML
    private Label lblImporteNeto;
    @FXML
    private Label lblImpuesto;

    private FxNotaCreditoRealizadasController creditoRealizadasController;

    private FxPrincipalController fxPrincipalController;

    private NotaCreditoTB notaCreditoTB;

    private ConvertMonedaCadena monedaCadena;

    private double importeBruto = 0;

    private double descuentoBruto = 0;

    private double sumImporteNeto = 0;

    private double impuestoNeto = 0;

    private double importeNeto = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        monedaCadena = new ConvertMonedaCadena();
        importeBruto = descuentoBruto = sumImporteNeto = impuestoNeto = importeNeto = 0;
    }

    public void loadInitData(String idNotaCredito) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return NotaCreditoADO.DetalleNotaCreditoById(idNotaCredito);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof NotaCreditoTB) {
                notaCreditoTB = (NotaCreditoTB) object;

                lblFechaRegistro.setText(notaCreditoTB.getFechaRegistro() + " " + notaCreditoTB.getHoraRegistro());
                lblCliente.setText(notaCreditoTB.getClienteTB().getNumeroDocumento() + " - " + notaCreditoTB.getClienteTB().getInformacion());
                lblClienteCelulares.setText(notaCreditoTB.getClienteTB().getTelefono() + " - " + notaCreditoTB.getClienteTB().getTelefono());
                lbCorreoElectronico.setText(notaCreditoTB.getClienteTB().getEmail());
                lbDireccion.setText(notaCreditoTB.getClienteTB().getDireccion());

                lblNotaCredito.setText(notaCreditoTB.getNombreComprobante());
                lblNotaCreditoSerieNumeracion.setText(notaCreditoTB.getSerie() + "-" + notaCreditoTB.getNumeracion());

                lblComprobanteReferencia.setText(notaCreditoTB.getVentaTB().getComprobanteName());
                lblSerieNumeracionReferencia.setText(notaCreditoTB.getVentaTB().getSerie() + "-" + notaCreditoTB.getVentaTB().getNumeracion());
                lblMotivoAnulacion.setText(notaCreditoTB.getNombreMotivo());

                fillNotaCreditoDetalle(notaCreditoTB.getNotaCreditoDetalleTBs());
            } else if (object instanceof String) {
                Tools.println(object);
            } else {
                Tools.println("Verga");
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void fillNotaCreditoDetalle(ArrayList<NotaCreditoDetalleTB> creditoDetalleTBs) {
        importeBruto = descuentoBruto = sumImporteNeto = impuestoNeto = importeNeto = 0;
        for (int i = 0; i < creditoDetalleTBs.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), creditoDetalleTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), creditoDetalleTBs.get(i).getSuministroTB().getClave() + "\n" + creditoDetalleTBs.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), creditoDetalleTBs.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), creditoDetalleTBs.get(i).getNombreImpuesto(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getPrecio(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), "-" + Tools.roundingValue(creditoDetalleTBs.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getImporte(), 2), Pos.CENTER_RIGHT), 7, (i + 1));

            double valorCantidad = creditoDetalleTBs.get(i).getCantidad();
            double valorBruto = Tools.calculateTaxBruto(creditoDetalleTBs.get(i).getValorImpuesto(), creditoDetalleTBs.get(i).getPrecio());
            double valorDescuento = creditoDetalleTBs.get(i).getDescuento();
            double valorSubNeto = valorBruto - valorDescuento;
            double valorImpuesto = Tools.calculateTax(creditoDetalleTBs.get(i).getValorImpuesto(), valorSubNeto);
            double valorNeto = valorSubNeto + valorImpuesto;

            importeBruto += (valorCantidad * valorBruto);
            descuentoBruto += (valorCantidad * valorDescuento);
            sumImporteNeto += (valorCantidad * valorSubNeto);
            impuestoNeto += (valorCantidad * valorImpuesto);
            importeNeto += (valorCantidad * valorNeto);
        }

        lblValorVenta.setText(notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBruto, 2));
        lblDescuento.setText(notaCreditoTB.getMonedaTB().getSimbolo() + " " + "-" + Tools.roundingValue(descuentoBruto, 2));
        lblSubImporteNeto.setText(notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(sumImporteNeto, 2));
        lblImpuesto.setText(notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoNeto, 2));
        lblImporteNeto.setText(notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNeto, 2));
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private JasperPrint reportA4() throws JRException {

        InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

        if (Session.COMPANY_IMAGE != null) {
            imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
        }

        InputStream dir = getClass().getResourceAsStream("/report/NotaCredito.jasper");

        ArrayList<SuministroTB> arrayList = new ArrayList();
        notaCreditoTB.getNotaCreditoDetalleTBs().forEach(e -> {
            SuministroTB suministroTB = new SuministroTB();
            suministroTB.setId(e.getId());
            suministroTB.setCantidad(e.getCantidad());
            suministroTB.setUnidadCompraName(e.getSuministroTB().getUnidadCompraName());
            suministroTB.setNombreMarca(e.getSuministroTB().getClave() + "\n" + e.getSuministroTB().getNombreMarca());
            suministroTB.setPrecioVentaGeneral(e.getPrecio());
            suministroTB.setDescuento(e.getDescuento());
            suministroTB.setImporteNeto(e.getImporte());
            arrayList.add(suministroTB);
        });

        Map map = new HashMap();
        map.put("LOGO", imgInputStream);
        map.put("ICON", imgInputStreamIcon);
        map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
        map.put("DIRECCION", Session.COMPANY_DOMICILIO);
        map.put("TELEFONOCELULAR", "TELÉFONO: " + Session.COMPANY_TELEFONO + " CELULAR: " + Session.COMPANY_CELULAR);
        map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);

        map.put("DOCUMENTOEMPRESA", "R.U.C " + Session.COMPANY_NUMERO_DOCUMENTO);
        map.put("NOMBREDOCUMENTO", notaCreditoTB.getNombreComprobante());
        map.put("NUMERODOCUMENTO", notaCreditoTB.getSerie() + "-" + notaCreditoTB.getNumeracion());

        map.put("DOCUMENTOCLIENTE", notaCreditoTB.getClienteTB().getTipoDocumentoName() + ":");
        map.put("NUMERODOCUMENTOCLIENTE", notaCreditoTB.getClienteTB().getNumeroDocumento());
        map.put("DATOSCLIENTE", notaCreditoTB.getClienteTB().getInformacion());
        map.put("DIRECCIONCLIENTE", notaCreditoTB.getClienteTB().getDireccion());
        map.put("CELULARCLIENTE", notaCreditoTB.getClienteTB().getCelular());
        map.put("EMAILCLIENTE", notaCreditoTB.getClienteTB().getEmail());

        map.put("FECHAEMISION", notaCreditoTB.getFechaRegistro());
        map.put("MONEDA", notaCreditoTB.getMonedaTB().getNombre() + "-" + notaCreditoTB.getMonedaTB().getAbreviado());

        map.put("DOCUMENTO_REFERENCIA", notaCreditoTB.getVentaTB().getSerie() + "-" + notaCreditoTB.getVentaTB().getNumeracion());
        map.put("MOTIVO_ANULACION", notaCreditoTB.getNombreMotivo());

        map.put("VALORSOLES", monedaCadena.Convertir(Tools.roundingValue(importeNeto, 2), true, notaCreditoTB.getMonedaTB().getNombre()));
        map.put("QRDATA", Session.COMPANY_NUMERO_DOCUMENTO + "|" + notaCreditoTB.getCodigoAlterno() + "|" + notaCreditoTB.getSerie() + "|" + notaCreditoTB.getNumeracion() + "|" + Tools.roundingValue(impuestoNeto, 2) + "|" + Tools.roundingValue(importeNeto, 2) + "|" + notaCreditoTB.getFechaRegistro() + "|" + notaCreditoTB.getClienteTB().getIdAuxiliar() + "|" + notaCreditoTB.getClienteTB().getNumeroDocumento() + "|");

        map.put("VALOR_VENTA", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBruto, 2));
        map.put("DESCUENTO", notaCreditoTB.getMonedaTB().getSimbolo() + " " + "-" + Tools.roundingValue(descuentoBruto, 2));
        map.put("SUB_IMPORTE", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(sumImporteNeto, 2));
        map.put("IMPUESTO_TOTAL", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoNeto, 2));
        map.put("IMPORTE_TOTAL", notaCreditoTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNeto, 2));

        JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(arrayList));
        return jasperPrint;
    }

    private void openWindowReporte() {
        try {

            URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxReportViewController controller = fXMLLoader.getController();
            controller.setFileName(notaCreditoTB.getNombreComprobante().toUpperCase() + " " + notaCreditoTB.getSerie() + "-" + notaCreditoTB.getNumeracion());
            controller.setJasperPrint(reportA4());
            controller.show();
            Stage stage = WindowStage.StageLoader(parent, "Nota de Crédito");
            stage.setResizable(true);
            stage.show();
            stage.requestFocus();

        } catch (IOException | JRException ex) {
            Tools.AlertMessageError(spWindow, "Reporte de Nota de Crédito", "Error al generar el reporte : " + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        fxPrincipalController.getVbContent().getChildren().remove(spWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(creditoRealizadasController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(creditoRealizadasController.getVbWindow());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        openWindowReporte();
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.println("i");
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        Tools.println("i");
    }

    public void setInitNotaCreditoRealizadasController(FxNotaCreditoRealizadasController creditoRealizadasController, FxPrincipalController fxPrincipalController) {
        this.creditoRealizadasController = creditoRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
