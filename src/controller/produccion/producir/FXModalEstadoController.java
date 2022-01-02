package controller.produccion.producir;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.ProduccionTB;

public class FXModalEstadoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private RadioButton rbSaveOden;
    @FXML
    private RadioButton rbSaveAndEnd;

    private FxProducirAgregarController fxProducirAgregarController;

    private FxProducirEditarController fxProducirEditarController;

    private ProduccionTB produccionTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        ToggleGroup guardar = new ToggleGroup();
        rbSaveOden.setToggleGroup(guardar);
        rbSaveAndEnd.setToggleGroup(guardar);
        rbSaveOden.setSelected(true);
    }

    private void GuardarProduccion() {

        if (fxProducirAgregarController != null) {
            if (rbSaveOden.isSelected()) {
                produccionTB.setEstado(2);
            } else {
                produccionTB.setEstado(1);
            }
            if (produccionTB.getEstado() == 1 && !Tools.isNumeric(fxProducirAgregarController.getTxtCantidadProducir().getText())) {
                Tools.AlertMessageWarning(window, "Producción", "El valor ingresado no es numérico en el campo cantidad a producir.");
            } else if (produccionTB.getEstado() == 1 && Double.parseDouble(fxProducirAgregarController.getTxtCantidadProducir().getText()) <= 0) {
                Tools.AlertMessageWarning(window, "Producción", "La cantidad producida no debe ser manor a 0.");
            } else {
                if (produccionTB.getEstado() == 1) {
                    produccionTB.setCantidad(Double.parseDouble(fxProducirAgregarController.getTxtCantidadProducir().getText()));
                    produccionTB.setCantidadVariable(false);
                }
                short value = Tools.AlertMessageConfirmation(window, "Producción", "¿Está seguro de continuar?");
                if (value == 1) {
                    Tools.Dispose(window);
                    fxProducirAgregarController.executeRegistro(produccionTB);
                }
            }
        } else if (fxProducirEditarController != null) {
            if (rbSaveOden.isSelected()) {
                produccionTB.setEstado(2);
            } else {
                produccionTB.setEstado(1);
            }
            if (produccionTB.getEstado() == 1 && !Tools.isNumeric(fxProducirEditarController.getTxtCantidadProducir().getText())) {
                Tools.AlertMessageWarning(window, "Producción", "El valor ingresado no es numérico en el campo cantidad a producir.");
            } else if (produccionTB.getEstado() == 1 && Double.parseDouble(fxProducirEditarController.getTxtCantidadProducir().getText()) <= 0) {
                Tools.AlertMessageWarning(window, "Producción", "La cantidad producida no debe ser manor a 0.");
            } else {
                if (produccionTB.getEstado() == 1) {
                    produccionTB.setCantidad(Double.parseDouble(fxProducirEditarController.getTxtCantidadProducir().getText()));
                    produccionTB.setCantidadVariable(false);
                }
                short value = Tools.AlertMessageConfirmation(window, "Producción", "¿Está seguro de continuar?");
                if (value == 1) {
                    Tools.Dispose(window);
                    fxProducirEditarController.executeEdicion(produccionTB);
                }
            }
        }
    }

    @FXML
    private void onKeyPressedRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            GuardarProduccion();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        GuardarProduccion();
    }

    @FXML
    private void onKeyPressedCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionToCancel(ActionEvent event) {
        Tools.Dispose(window);
    }

    public void setInitControllerProducirAgregar(FxProducirAgregarController fxProducirAgregarController) {
        this.fxProducirAgregarController = fxProducirAgregarController;
    }

    public void setInitControllerProducirEditar(FxProducirEditarController fxProducirEditarController) {
        this.fxProducirEditarController = fxProducirEditarController;
    }

    public void setProduccionTB(ProduccionTB produccionTB) {
        this.produccionTB = produccionTB;
    }
}
