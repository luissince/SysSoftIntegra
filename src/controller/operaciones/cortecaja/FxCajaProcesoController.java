package controller.operaciones.cortecaja;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.CajaADO;

public class FxCajaProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtNombre;
    @FXML
    private RadioButton rbEstadoUno;
    @FXML
    private RadioButton rbEstadoDos;
    @FXML
    private Button btnRegister;

    private String idListaCaja;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        ToggleGroup groupVende = new ToggleGroup();
        groupVende.selectToggle(rbEstadoUno);
        groupVende.selectToggle(rbEstadoDos);
    }

    public void initRegistroCaja(String title) {
        lblTitle.setText(title);
    }

    public void initActualizarCaja(String title, String idCaja, String nombre, boolean estado) {
        idListaCaja = idCaja;
        lblTitle.setText(title);
        txtNombre.setText(nombre);
        rbEstadoUno.setSelected(estado);
        btnRegister.setText("Actualizar");
        btnRegister.getStyleClass().add("buttonLightWarning");
    }

    private void eventRegistrarCaja() {
        if (Tools.isText(txtNombre.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Proceso caja", "Ingrese el nombre de la caja.");
            txtNombre.requestFocus();
        } else {
            short option = Tools.AlertMessageConfirmation(apWindow, "Proceso caja", "¿Está seguro de continuar?");
            if (option == 1) {
                String result = CajaADO.CrudListaCaja(idListaCaja, txtNombre.getText(), rbEstadoUno.isSelected());
                if (result.equalsIgnoreCase("insert")) {
                    Tools.AlertMessageInformation(apWindow, "Proceso caja", "Se registro correctamente.");
                    Tools.Dispose(apWindow);
                } else if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(apWindow, "Proceso caja", "Se actualizo correctamente.");
                    Tools.Dispose(apWindow);
                } else {
                    Tools.AlertMessageError(apWindow, "Proceso caja", result);
                }
            }
        }
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventRegistrarCaja();
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) {
        eventRegistrarCaja();
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

}
