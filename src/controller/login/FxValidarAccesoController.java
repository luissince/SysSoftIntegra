
package controller.login;

import controller.operaciones.venta.FxVentaRealizadasController;
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
import javafx.scene.text.Text;

public class FxValidarAccesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Text lblComprobante;
    @FXML
    private TextField txtClave;
    
    private FxVentaRealizadasController ventaRealizadasController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    
    
    private void validarAcceso(){
        if(Tools.isText(txtClave.getText())){
            Tools.AlertMessageWarning(apWindow, "Validar Acceso", "Ingrese la clave para continuar.");
        }else{
                
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        validarAcceso();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER){
            validarAcceso();
        }
    }
    
    
}
