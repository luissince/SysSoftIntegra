package controller.posterminal.venta;

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

public class FxPostVentaValidarCajaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtFecha;

    private FxPostVentaController ventaController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }

    public void loadData(String dateTime) {
        txtFecha.setText(dateTime);
    }

    @FXML
    private void onKeyPressedOk(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionOk(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitVentaController(FxPostVentaController ventaController) {
        this.ventaController = ventaController;
    }

}
