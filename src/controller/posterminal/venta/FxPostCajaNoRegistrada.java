package controller.posterminal.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class FxPostCajaNoRegistrada implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblMensaje;

    private FxPostVentaController ventaController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }
    
    public void initElements(String mensaje){
        lblMensaje.setText(mensaje);
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        Tools.Dispose(window);
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    public void setInitVentaController(FxPostVentaController ventaController) {
        this.ventaController = ventaController;
    }

}
