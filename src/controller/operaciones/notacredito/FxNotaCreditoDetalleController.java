package controller.operaciones.notacredito;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.NotaCreditoADO;
import model.NotaCreditoDetalleTB;
import model.NotaCreditoTB;

public class FxNotaCreditoDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
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
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxNotaCreditoRealizadasController creditoRealizadasController;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private String idNotaCredito;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketNotaCredito(apWindow);
    }

    public void loadInitData(String idNotaCredito) {
        this.idNotaCredito = idNotaCredito;
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

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            btnAceptarLoad.setVisible(false);
        });

        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof NotaCreditoTB) {
                NotaCreditoTB notaCreditoTB = (NotaCreditoTB) object;

                lblFechaRegistro.setText(notaCreditoTB.getFechaRegistro() + " " + notaCreditoTB.getHoraRegistro());
                lblCliente.setText(notaCreditoTB.getClienteTB().getNumeroDocumento() + " - " + notaCreditoTB.getClienteTB().getInformacion());
                lblClienteCelulares.setText(notaCreditoTB.getClienteTB().getTelefono() + " - " + notaCreditoTB.getClienteTB().getTelefono());
                lbCorreoElectronico.setText(notaCreditoTB.getClienteTB().getEmail());
                lbDireccion.setText(notaCreditoTB.getClienteTB().getDireccion());

                lblNotaCredito.setText(notaCreditoTB.getNombreComprobante());
                lblNotaCreditoSerieNumeracion.setText(notaCreditoTB.getSerie() + "-" + Tools.formatNumber(notaCreditoTB.getNumeracion()));

                lblComprobanteReferencia.setText(notaCreditoTB.getVentaTB().getComprobanteName());
                lblSerieNumeracionReferencia.setText(notaCreditoTB.getVentaTB().getSerie() + "-" + notaCreditoTB.getVentaTB().getNumeracion());
                lblMotivoAnulacion.setText(notaCreditoTB.getNombreMotivo());

                fillNotaCreditoDetalle(notaCreditoTB.getNotaCreditoDetalleTBs(), notaCreditoTB.getMonedaTB().getSimbolo());

                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void fillNotaCreditoDetalle(ArrayList<NotaCreditoDetalleTB> creditoDetalleTBs, String simbolo) {
        for (int i = 0; i < creditoDetalleTBs.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), creditoDetalleTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), creditoDetalleTBs.get(i).getSuministroTB().getClave() + "\n" + creditoDetalleTBs.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), creditoDetalleTBs.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getImpuestoTB().getValor(), 2) + "%", Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), simbolo + " " + Tools.roundingValue(creditoDetalleTBs.get(i).getPrecio(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), Tools.roundingValue(creditoDetalleTBs.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), simbolo + " " + Tools.roundingValue(creditoDetalleTBs.get(i).getCantidad() * (creditoDetalleTBs.get(i).getPrecio() - creditoDetalleTBs.get(i).getDescuento()), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calculateTotales(creditoDetalleTBs, simbolo);
    }

    public void calculateTotales(ArrayList<NotaCreditoDetalleTB> empList, String simbolo) {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;

        for (NotaCreditoDetalleTB ocdtb : empList) {
            double importeBruto = ocdtb.getPrecio() * ocdtb.getCantidad();
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

        lblValorVenta.setText(simbolo + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(simbolo + " " + "-" + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporteNeto.setText(simbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(simbolo + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteNeto.setText(simbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(creditoRealizadasController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(creditoRealizadasController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(creditoRealizadasController.getVbWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeWindow();
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketNotaCredito().mostrarReporte(idNotaCredito);
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        fxOpcionesImprimirController.getTicketNotaCredito().mostrarReporte(idNotaCredito);
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

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            closeWindow();
        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        closeWindow();
    }

    public void setInitNotaCreditoRealizadasController(FxNotaCreditoRealizadasController creditoRealizadasController, FxPrincipalController fxPrincipalController) {
        this.creditoRealizadasController = creditoRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
