package controller.operaciones.venta;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.PrivilegioTB;

public class FxVentaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private VBox hbContenedorVentas;
    @FXML
    private TabPane tbContenedor;
    @FXML
    private Tab tbVentaUno;
    @FXML
    private Button btnAgregarVenta;

    private FxPrincipalController fxPrincipalController;

    private FxVentaEstructuraController ventaEstructuraController;

    private FxVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private ObservableList<PrivilegioTB> privilegioTBs;

    private short tipoVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void loadComponents() {
        if (tipoVenta == 1) { 
            ventaEstructuraController = (FxVentaEstructuraController) addEstructura(tbVentaUno);
        } else {
            ventaEstructuraNuevoController = (FxVentaEstructuraNuevoController) addEstructura(tbVentaUno);
        }
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        this.privilegioTBs = privilegioTBs;
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            btnAgregarVenta.setDisable(true);
        }
        if (tipoVenta == 1) {
            ventaEstructuraController.loadPrivilegios(privilegioTBs);
        } else {
            ventaEstructuraNuevoController.loadPrivilegios(privilegioTBs);
        }
    }

    private Object addEstructura(Tab tab) {
        Object object = null;
        try {
            String pathFile = tipoVenta == 1 ? FilesRouters.FX_VENTA_ESTRUCTURA : FilesRouters.FX_VENTA_ESTRUCTURA_NUEVO;
            FXMLLoader fXMLSeleccionado = new FXMLLoader(getClass().getResource(pathFile));
            VBox seleccionado = fXMLSeleccionado.load();
            object = fXMLSeleccionado.getController();
            tab.setContent(seleccionado);
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        return object;
    }

    public void loadElements() {
        if (tipoVenta == 1) {
            ventaEstructuraController.setContent(fxPrincipalController);
            ventaEstructuraController.getTxtSearch().requestFocus();
        } else {
            ventaEstructuraNuevoController.setContent(fxPrincipalController);
        }
    }

    private void addTabVentaEstructura() {
        Tab tab = new Tab("Venta " + (tbContenedor.getTabs().size() + 1));
        tbContenedor.getTabs().add(tab);
        if (tipoVenta == 1) {
            FxVentaEstructuraController controller = (FxVentaEstructuraController) addEstructura(tab);
            controller.setContent(fxPrincipalController);
            controller.getTxtSearch().requestFocus();
            controller.loadPrivilegios(privilegioTBs);
        } else {
            FxVentaEstructuraNuevoController controller = (FxVentaEstructuraNuevoController) addEstructura(tab);
            controller.setContent(fxPrincipalController);
            controller.getTxtSearch().requestFocus();
            controller.loadPrivilegios(privilegioTBs);
        }
    }

    @FXML
    private void onKeyPressedAgregarVenta(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addTabVentaEstructura();
        }
    }

    @FXML
    private void onActionAgregarVenta(ActionEvent event) {
        addTabVentaEstructura();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

    public void setTipoVenta(short tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

}
