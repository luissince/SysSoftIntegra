package controller.operaciones.cortecaja;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import service.CajaADO;

public class FxCajaAjusteController implements Initializable {

    @FXML
    private AnchorPane apWindow;

    private FxCajaConsultasController cajaConsultasController;

    private String idCaja;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }
    
    public void initComponents(String idCaja){
        this.idCaja = idCaja;
    }

    private void onEventAjustar() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Caja", "¿Está seguro de continuar?");
        if(value == 1){
            String result = CajaADO.AjustarCaja(idCaja);
            if(result.equalsIgnoreCase("upload")){
                Tools.AlertMessageInformation(apWindow, "Caja", "Se actualizo correctamente los cambios.");
                Tools.Dispose(apWindow);
            }else{
                Tools.AlertMessageWarning(apWindow, "Caja", result);
            }
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAjustar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAjustar();
        }
    }

    public void setInitCajaConsultasController(FxCajaConsultasController cajaConsultasController) {
        this.cajaConsultasController = cajaConsultasController;
    }

}
