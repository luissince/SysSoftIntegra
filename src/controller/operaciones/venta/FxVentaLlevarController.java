package controller.operaciones.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.VentaADO;

public class FxVentaLlevarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private RadioButton rbCompletado;
    @FXML
    private RadioButton rbCantidad;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtObservacion;

    private FxVentaDetalleController ventaDetalleController;

    private String idVenta;

    private String idSuministro;

    private String comprobante;

    private double costo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        ToggleGroup toggleGroup = new ToggleGroup();
        rbCompletado.setToggleGroup(toggleGroup);
        rbCantidad.setToggleGroup(toggleGroup);
    }

    public void setInitData(String idVenta, String idSuministro, String comprobante, double costo) {
        this.idVenta = idVenta;
        this.idSuministro = idSuministro;
        this.comprobante = comprobante;
        this.costo = costo;
    }

    private void llevarProducto() {
        if (rbCompletado.isSelected()) {
            executeQuery(0, txtObservacion.getText().trim());
        } else {
            if (!Tools.isNumeric(txtCantidad.getText().trim())) {
                Tools.AlertMessageWarning(apWindow, "Venta detalle", "Ingreso un valor númerico, por favor.");
                txtCantidad.requestFocus();
            } else {
                executeQuery(Double.parseDouble(txtCantidad.getText().trim()), txtObservacion.getText().trim());
            }
        }
    }

    private void executeQuery(double cantidad, String observacion) {
        short confirmation = Tools.AlertMessageConfirmation(apWindow, "Venta detalle", "¿Está seguro de continuar?");
        if (confirmation == 1) {
            String result = VentaADO.UpdateProductoParaLlevar(idVenta, idSuministro, comprobante, cantidad, costo, observacion, rbCompletado.isSelected());
            switch (result) {
                case "update":
                    Tools.AlertMessageInformation(apWindow, "Venta detalle", "Se actualizó correctamente el cambio.");
                    Tools.Dispose(apWindow);
                    ventaDetalleController.setInitComponents(idVenta);
                    break;
                case "noproduct":
                    Tools.AlertMessageWarning(apWindow, "Venta detalle", "No se pudo completar por problemas de id de venta y producto.");
                    break;
                case "historial":
                    Tools.AlertMessageWarning(apWindow, "Venta detalle", "No se puede hacer un movimiento total porque ya existe un historial de salida.");
                    break;
                default:
                    Tools.AlertMessageError(apWindow, "Venta detalle", result);
                    break;
            }
        }
    }

    @FXML
    private void onActionCompletado(ActionEvent event) {
        txtCantidad.setDisable(true);
    }

    @FXML
    private void onActionCantidad(ActionEvent event) {
        txtCantidad.setDisable(false);
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        llevarProducto();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            llevarProducto();
        }
    }

    @FXML
    private void onKeyTypedMonto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCantidad.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitVentaDetalleController(FxVentaDetalleController ventaDetalleController) {
        this.ventaDetalleController = ventaDetalleController;
    }

}
