package controller.operaciones.ordencompra;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.Session;
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
import model.OrdenCompraADO;
import model.OrdenCompraDetalleTB;
import model.OrdenCompraTB;

public class FxOrdenCompraDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblNumero;
    @FXML
    private Text lblFechaRegistro;
    @FXML
    private Text lblProveedor;
    @FXML
    private Text lblCelularTelefono;
    @FXML
    private Text lbCorreoElectronico;
    @FXML
    private Text lbDireccion;
    @FXML
    private Text lblObservaciones;
    @FXML
    private Text lblVendedor;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblImporteBruto;
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

    private FxOrdenCompraRealizadasController ordenCompraRealizadasController;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private String idOrdenCompra;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketOrdenCompra(apWindow);
    }

    public void setInitComponents(String idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return OrdenCompraADO.ObtenerOrdenCompraId(idOrdenCompra);
            }
        };
        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof OrdenCompraTB) {
                OrdenCompraTB ordenCompraTB = (OrdenCompraTB) object;
                lblNumero.setText("N° - " + Tools.formatNumber(ordenCompraTB.getNumeracion()));
                lblFechaRegistro.setText(ordenCompraTB.getFechaRegistro() + " " + ordenCompraTB.getHoraRegistro());
                lblProveedor.setText(ordenCompraTB.getProveedorTB().getNumeroDocumento() + " " + ordenCompraTB.getProveedorTB().getRazonSocial());
                lblCelularTelefono.setText(Tools.textShow("TEL.: ", ordenCompraTB.getProveedorTB().getTelefono()) + " " + Tools.textShow("CEL.: ", ordenCompraTB.getProveedorTB().getCelular()));
                lbCorreoElectronico.setText(ordenCompraTB.getProveedorTB().getEmail());
                lbDireccion.setText(ordenCompraTB.getProveedorTB().getDireccion());
                lblObservaciones.setText(ordenCompraTB.getObservacion());
                lblVendedor.setText(ordenCompraTB.getEmpleadoTB().getApellidos() + ", " + ordenCompraTB.getEmpleadoTB().getNombres());

                fillDetalleTable(ordenCompraTB.getOrdenCompraDetalleTBs());

                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
            }
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void fillDetalleTable(ArrayList<OrdenCompraDetalleTB> empList) {
        for (int i = 0; i < empList.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), empList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), empList.get(i).getSuministroTB().getClave() + "\n" + empList.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(empList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), empList.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(empList.get(i).getImpuestoTB().getValor(), 2) + "%", Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(empList.get(i).getCosto(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), Tools.roundingValue(empList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(empList.get(i).getCosto() * empList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calculateTotales(empList);
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

    public void calculateTotales(ArrayList<OrdenCompraDetalleTB> empList) {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;

        for (OrdenCompraDetalleTB ocdtb : empList) {
            double importeBruto = ocdtb.getCosto() * ocdtb.getCantidad();
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

        lblImporteBruto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    private void onEventClose() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(ordenCompraRealizadasController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(ordenCompraRealizadasController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(ordenCompraRealizadasController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(ordenCompraRealizadasController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(ordenCompraRealizadasController.getVbWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        onEventClose();
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketOrdenCompra().mostrarReporte(idOrdenCompra);
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        fxOpcionesImprimirController.getTicketOrdenCompra().mostrarReporte(idOrdenCompra);
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketOrdenCompra().imprimir(idOrdenCompra);
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        fxOpcionesImprimirController.getTicketOrdenCompra().imprimir(idOrdenCompra);
    }

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventClose();
        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        onEventClose();
    }

    public void setInitOrdenCompraRealizadasController(FxOrdenCompraRealizadasController ordenCompraRealizadasController, FxPrincipalController fxPrincipalController) {
        this.ordenCompraRealizadasController = ordenCompraRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
