package controller.configuracion.roles;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.RolADO;
import model.RolTB;

public class FxRolesAgregarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtNombre;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);

    }

    private void executeRegistrar() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Roles", "¿Está seguro de continuar?");
        if (value == 1) {
            RolTB rolTB = new RolTB();
            rolTB.setNombre(txtNombre.getText().trim().toUpperCase());
            rolTB.setSistema(false);
            String result = RolADO.AgregarRol(rolTB);
            if (result.equalsIgnoreCase("registrado")) {
                Tools.AlertMessageInformation(apWindow, "Roles", "Se creo el rol correctamente.");
                Tools.Dispose(apWindow);
            } else {
                Tools.AlertMessageError(apWindow, "apWindow", result);
            }
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) {
        executeRegistrar();
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeRegistrar();
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

}
