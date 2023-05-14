package controller.consultas.compras;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import service.CompraADO;

public class FxComprasCancelarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTotal;
    @FXML
    private TextField txtEfectivo;
    @FXML
    private TextField txtObservacion;

    private FxComprasDetalleController comprasDetalleController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }

    public void loadComponents() {
        lblTotal.setText(comprasDetalleController.getCompraTB().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(comprasDetalleController.getCompraTB().getTotal(), 2));
        txtEfectivo.setText(Tools.roundingValue(comprasDetalleController.getCompraTB().getTotal(), 2));
    }

    private void executeAceptar() {
        if (txtObservacion.getText().trim().length() == 0) {
            Tools.AlertMessageWarning(apWindow, "Detalle de la compra", "Ingrese el motivo de la anulación");
        } else {
            short value = Tools.AlertMessageConfirmation(apWindow, "Detalle de la compra", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = CompraADO.anularCompra(comprasDetalleController.getCompraTB(), txtObservacion.getText().toUpperCase());
                if (result.equalsIgnoreCase("cancel")) {
                    Tools.AlertMessageWarning(apWindow, "Detalle de la compra", "La compra ya se encuentra anulada.");
                    Tools.Dispose(apWindow);
                } else if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(apWindow, "Detalle de la compra", "Se completo correctamente los cambios.");
                    Tools.Dispose(apWindow);
                    comprasDetalleController.loadInitComponents(comprasDetalleController.getCompraTB().getIdCompra());
                } else if (result.equalsIgnoreCase("credito")) {
                    Tools.AlertMessageWarning(apWindow, "Detalle de la compra", "No se puede eliminar la compra porque tiene ligado un historial de crédito.");
                    Tools.Dispose(apWindow);
                } else {
                    Tools.AlertMessageError(apWindow, "Detalle de la compra", result);
                }
            }
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        executeAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeAceptar();
        }
    }

    public void setComprasDetalleController(FxComprasDetalleController comprasDetalleController) {
        this.comprasDetalleController = comprasDetalleController;
    }

}
