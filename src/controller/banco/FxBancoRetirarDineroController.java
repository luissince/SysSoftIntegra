package controller.banco;

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
import model.BancoADO;
import model.BancoHistorialTB;

public class FxBancoRetirarDineroController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtDescripcion;

    private FxBancoHistorialController bancoHistorialController;

    private String idBanco;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }

    @FXML
    private void onKeyTypedMonto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtMonto.getText().contains(".")) {
            event.consume();
        }
    }
    
    private void eventGuardar(){
        if (!Tools.isNumeric(txtMonto.getText())) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Ingrese el monto");
            txtMonto.requestFocus();
        } else if (Double.parseDouble(txtMonto.getText()) <= 0) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Valor no puede ser menor a 0");
            txtMonto.requestFocus();
        } else if (txtDescripcion.getText().trim().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Ingrese una descripción");
            txtDescripcion.requestFocus();
        } else {
            BancoHistorialTB bancoHistorialTB = new BancoHistorialTB();
            bancoHistorialTB.setIdBanco(idBanco);
            bancoHistorialTB.setDescripcion(txtDescripcion.getText().trim());
            bancoHistorialTB.setFecha(Tools.getDate());
            bancoHistorialTB.setHora(Tools.getTime());
            bancoHistorialTB.setSalida(Double.parseDouble(txtMonto.getText()));            
            String result = BancoADO.Retirar_Dinero(bancoHistorialTB);
            if(result.equalsIgnoreCase("inserted")){
                Tools.AlertMessageInformation(apWindow, "Banco", "Se retiro correctamente");
                Tools.Dispose(apWindow);
                bancoHistorialController.loadBanco(idBanco);
            }else{
                Tools.AlertMessageError(apWindow, "Banco", result);
            }
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        eventGuardar();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventGuardar();
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

    public void setIdBanco(String idBanco) {
        this.idBanco = idBanco;
    }

    public void setInitBancosController(FxBancoHistorialController bancoHistorialController) {
        this.bancoHistorialController = bancoHistorialController;
    }

}
