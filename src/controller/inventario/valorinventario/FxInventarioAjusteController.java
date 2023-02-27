package controller.inventario.valorinventario;

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
import model.SuministroTB;
import service.SuministroADO;

public class FxInventarioAjusteController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblClave;
    @FXML
    private Label lblProducto;
    @FXML
    private TextField txtStockMinimo;
    @FXML
    private TextField txtstockMaximo;

    private FxValorInventarioController valorInventarioController;

    private String idSuministro;

    private int idAlmacen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }

    public void setLoadComponents(SuministroTB suministroTB) {
        this.idSuministro = suministroTB.getIdSuministro();
        this.idAlmacen = suministroTB.getAlmacenTB().getIdAlmacen();
        lblClave.setText(suministroTB.getClave());
        lblProducto.setText(suministroTB.getNombreMarca());
        txtStockMinimo.setText(Tools.roundingValue(suministroTB.getStockMinimo(), 2));
        txtstockMaximo.setText(Tools.roundingValue(suministroTB.getStockMaximo(), 2));
    }

    private void onExecuteGuardar() {
        if (!Tools.isNumeric(txtStockMinimo.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Ajuste de inventario", "El valor ingresado debe ser numerico.");
            txtStockMinimo.requestFocus();
        } else if (!Tools.isNumeric(txtstockMaximo.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Ajuste de inventario", "El valor ingresado debe ser numerico.");
            txtstockMaximo.requestFocus();
        } else {
            short option = Tools.AlertMessageConfirmation(apWindow, "Ajuste de inventario", "¿Esta seguro de continuar?");
            if (option == 1) {
                String value = SuministroADO.UpdatedInventarioStockMM(idAlmacen, idSuministro, txtStockMinimo.getText().trim(), txtstockMaximo.getText().trim());
                if (value.equalsIgnoreCase("updated")) {
                    valorInventarioController.onEventPaginacion();
                    Tools.Dispose(apWindow);
                    Tools.AlertMessageInformation(apWindow, "Ajuste de inventario", "Se realizo los cambios correctamenten.");
                } else {
                    Tools.AlertMessageError(apWindow, "Ajuste de inventario", value);
                }
            }
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onExecuteGuardar();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onExecuteGuardar();
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

    @FXML
    private void onKeyTypedMinimo(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtStockMinimo.getText().contains(".") || c == '-' && txtStockMinimo.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedMaxino(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtstockMaximo.getText().contains(".") || c == '-' && txtstockMaximo.getText().contains("-")) {
            event.consume();
        }
    }

    public void setInitValorInventarioController(FxValorInventarioController valorInventarioController) {
        this.valorInventarioController = valorInventarioController;
    }

}
