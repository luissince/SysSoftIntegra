package controller.configuracion.tablasbasicas;

import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.MantenimientoADO;
import model.MantenimientoTB;

public class FxMantenimientoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtCode;
    @FXML
    private TextField txtName;
    @FXML
    private Button btnToAction;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void initWindow() {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        txtCode.setText(MantenimientoADO.GetIdMantenimiento());
    }

    public void aValidityProcess() {
        if (txtCode.getText().equalsIgnoreCase("")) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Mantenimiento", "Ingrese el código, por favor.", false);
            txtCode.requestFocus();
        } else if (txtName.getText().equalsIgnoreCase("")) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Mantenimiento", "Ingrese el nombre del mantenimiento, por favor.", false);
            txtName.requestFocus();
        } else {
                short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Mantenimiento", "¿Esta seguro de continuar?", true);
                if (confirmation == 1) {
                    MantenimientoTB mantenimientoTB = new MantenimientoTB();
                    mantenimientoTB.setIdMantenimiento(txtCode.getText());
                    mantenimientoTB.setNombre(txtName.getText().trim());
                    mantenimientoTB.setEstado('1');
                    mantenimientoTB.setUsuarioRegistro(Session.USER_ID);
                    String result = MantenimientoADO.CrudEntity(mantenimientoTB);
                    switch (result) {
                        case "registered":
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Mantenimiento", "Registrado correctamente.", false);
                            break;
                        case "updated":
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Mantenimiento", "Actualizado correctamente.", false);
                            break;
                        case "duplicate":
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Mantenimiento", "No se puede haber 2 items con el mismo nombre.", false);
                            txtName.requestFocus();
                            break;
                        case "error":
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Mantenimiento", "No se puedo completar la ejecución.", false);
                            break;
                        default:
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Mantenimiento", result, false);
                            break;
                    }
                
            } else {
                Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Mantenimiento", "No hay conexión al servidor.", false);
            }
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        aValidityProcess();
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            aValidityProcess();
        }
    }

    @FXML
    private void onActionToCancel(ActionEvent event) {
        Tools.Dispose(window);
    }

    @FXML
    private void onKeyPressedToCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    void setValues(String idMantenimiento, String nombre) {
        txtCode.setText(idMantenimiento);
        txtName.setText(nombre);
        btnToAction.setText("Actualizar");
        btnToAction.getStyleClass().add("buttonLightWarning");
    }

}
