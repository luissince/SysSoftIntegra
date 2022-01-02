package controller.configuracion.etiquetas;

import controller.tools.Tools;
import java.awt.print.PageFormat;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.EtiquetaADO;
import model.EtiquetaTB;

public class FxEtiquetasNuevoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtAncho;
    @FXML
    private TextField txtAlto;
    @FXML
    private RadioButton rbVertical;
    @FXML
    private RadioButton rbHorizontal;
    @FXML
    private ComboBox<EtiquetaTB> cbTipoEtiqueta;

    private FxEtiquetasController etiquetasController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        ToggleGroup groupVende = new ToggleGroup();
        rbVertical.setToggleGroup(groupVende);
        rbHorizontal.setToggleGroup(groupVende);
        cbTipoEtiqueta.getItems().addAll(EtiquetaADO.ListTipoEtiquetas());
    }

    private void eventCrear() {
        if (txtNombre.getText().trim().isEmpty()) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "Ingrese el nombre del etiqueta.", false);
            txtNombre.requestFocus();
        } else if (!Tools.isNumeric(txtAncho.getText())) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "Ingrese el ancho en mm de la etiqueta.", false);
            txtAncho.requestFocus();
        } else if (Double.parseDouble(txtAncho.getText()) <= 0) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "El valor ingreso no es numérico o es negativo.", false);
            txtAncho.requestFocus();
        } else if (!Tools.isNumeric(txtAlto.getText())) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "Ingrese el alto en mm de la etiqueta.", false);
            txtAlto.requestFocus();
        } else if (Double.parseDouble(txtAlto.getText()) <= 0) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "El valor ingreso no es numérico o es negativo.", false);
            txtAlto.requestFocus();
        } else if (cbTipoEtiqueta.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Etiqueta", "Seleccione un tipo de etiqueta.", false);
            cbTipoEtiqueta.requestFocus();
        } else {
            etiquetasController.newEtiqueta(
                    txtNombre.getText(),
                    Double.parseDouble(txtAncho.getText()),
                    Double.parseDouble(txtAlto.getText()),
                    rbVertical.isSelected() ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE,
                    cbTipoEtiqueta.getSelectionModel().getSelectedItem().getIdEtiqueta(),
                    cbTipoEtiqueta.getSelectionModel().getSelectedItem().getNombre());
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onKeyPressedCrear(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventCrear();
        }
    }

    @FXML
    private void onActionCrear(ActionEvent event) {
        eventCrear();
    }

    @FXML
    private void onKeyTypedAncho(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtAncho.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedAlto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtAlto.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    public void setInitEtiquetasController(FxEtiquetasController etiquetasController) {
        this.etiquetasController = etiquetasController;
    }

}
