
package controller.inventario.valorinventario;

import controller.reporte.FxReportViewController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;


public class FxReporteOpcionesInventarioController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private RadioButton rbOpcionUno;
    @FXML
    private RadioButton rbOpcionDos;
    
    private FxValorInventarioController valorInventarioController;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        ToggleGroup group = new ToggleGroup();
        rbOpcionUno.setToggleGroup(group);
        rbOpcionDos.setToggleGroup(group);
    }    
    
    private void onEventGenerarReporte(){
        if (valorInventarioController.getTvList().getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Reporte Inventario", "No hay datos en la lista para mostrar en el reporte");
            return;
        }
        try {
            InputStream dir = getClass().getResourceAsStream("/report/"+(rbOpcionUno.isSelected()?"InventarioActual.jasper":"InventarioActualAjuste.jasper"));

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(dir);
            Map map = new HashMap();
            map.put("EMPRESA_RAZON_SOCIAL", Session.COMPANY_RAZON_SOCIAL);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JRBeanCollectionDataSource(valorInventarioController.getTvList().getItems()));

            URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxReportViewController controller = fXMLLoader.getController();
            controller.setJasperPrint(jasperPrint);
            controller.show();
            Stage stage = WindowStage.StageLoader(parent, "Inventario General");
            stage.setResizable(true);
            stage.show();
            stage.requestFocus();

        } catch (HeadlessException | JRException | IOException ex) {
            Tools.AlertMessageError(apWindow, "Reporte de Inventario", "Error al generar el reporte : " + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPressedGenerar(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER){
            onEventGenerarReporte();
        }
    }

    @FXML
    private void onActionGenerar(ActionEvent event) {
    onEventGenerarReporte();
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
         if(event.getCode() == KeyCode.ENTER){
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    void setInitValorInventarioController(FxValorInventarioController valorInventarioController) {
        this.valorInventarioController=valorInventarioController;
    }
    
}
