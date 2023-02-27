package controller.posterminal.venta;

import controller.tools.Session;
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
import model.CajaTB;
import model.MovimientoCajaTB;
import service.CajaADO;

public class FxPostVentaFondoInicialController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtImporte;

    private FxPostVentaController ventaController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    private void onEventAceptar() {
        if (Tools.isNumeric(txtImporte.getText().trim())) {
            CajaTB cajaTB = new CajaTB();
            cajaTB.setFechaApertura(Tools.getDate());
            cajaTB.setHoraApertura(Tools.getTime("HH:mm:ss"));
            cajaTB.setIdUsuario(Session.USER_ID);
            cajaTB.setEstado(true);
            cajaTB.setContado(0);
            cajaTB.setCalculado(0);
            cajaTB.setDiferencia(0);
            
            MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
            movimientoCajaTB.setFechaMovimiento(Tools.getDate());
            movimientoCajaTB.setHoraMovimiento(Tools.getTime("HH:mm:ss"));
            movimientoCajaTB.setComentario("APERTURA DE CAJA");
            movimientoCajaTB.setTipoMovimiento((short)1);
            movimientoCajaTB.setMonto(Double.parseDouble(txtImporte.getText()));
            
            String result = CajaADO.AperturarCaja(cajaTB,movimientoCajaTB);
            if (result.equalsIgnoreCase("registrado")) {
                Tools.AlertMessageInformation(window, "Ventas", "Se aperturo correctamento la caja.");
                ventaController.setAperturaCaja(true);
                Tools.Dispose(window);
            } else {
                Tools.AlertMessageError(window, "Ventas", result);
                ventaController.setAperturaCaja(false);
            }
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onKeyTypedImporte(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtImporte.getText().contains(".") || c == '-' && txtImporte.getText().contains("-")) {
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

    public void setInitVentaController(FxPostVentaController ventaController) {
        this.ventaController = ventaController;
    }

}
