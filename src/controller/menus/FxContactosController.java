package controller.menus;

import controller.contactos.clientes.FxClienteController;
import controller.contactos.proveedores.FxProveedorController;
import controller.tools.FilesRouters;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.SubMenusTB;

public class FxContactosController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private HBox hbOperacionesUno;
    @FXML
    private VBox btnCliente;
    @FXML
    private VBox btnProveedor;
    /*
    Objectos de la ventana principal y venta que agrega al os hijos
     */
    private FxPrincipalController fxPrincipalController;
    /*
    Controller clientes     
     */
    private FXMLLoader fXMLCustomer;

    private VBox nodeCostumer;

    private FxClienteController controllerCostumer;
    /*
    Controller proveedores
     
     */
    private FXMLLoader fXMLProviders;

    private VBox nodeProviders;

    private FxProveedorController controllerProviders;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLCustomer = new FXMLLoader(getClass().getResource(FilesRouters.FX_CLIENTE));
            nodeCostumer = fXMLCustomer.load();
            controllerCostumer = fXMLCustomer.getController();

            fXMLProviders = new FXMLLoader(getClass().getResource(FilesRouters.FX_PROVEEDORES));
            nodeProviders = fXMLProviders.load();
            controllerProviders = fXMLProviders.getController();

        } catch (IOException ex) {
            System.out.println("Error en Contacto Controller:" + ex.getLocalizedMessage());
        }
    }

    public void loadSubMenus(ObservableList<SubMenusTB> subMenusTBs) {
        
        if (subMenusTBs.get(0).getIdSubMenu() != 0 && !subMenusTBs.get(0).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnCliente);
        } else {

        }

        if (subMenusTBs.get(1).getIdSubMenu() != 0 && !subMenusTBs.get(1).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnProveedor);
        } else {

        }
    }

    private void openWindowCostumer() {
        controllerCostumer.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeCostumer, 0d);
        AnchorPane.setTopAnchor(nodeCostumer, 0d);
        AnchorPane.setRightAnchor(nodeCostumer, 0d);
        AnchorPane.setBottomAnchor(nodeCostumer, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeCostumer);        
    }

    private void openWindowsProviders() {
        controllerProviders.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeProviders, 0d);
        AnchorPane.setTopAnchor(nodeProviders, 0d);
        AnchorPane.setRightAnchor(nodeProviders, 0d);
        AnchorPane.setBottomAnchor(nodeProviders, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeProviders);        
    }

    @FXML
    private void onKeyPressedCustomers(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCostumer();
        }
    }

    @FXML
    private void onActionCustomers(ActionEvent event) {
        openWindowCostumer();
    }

    @FXML
    private void onKeyPressedProviders(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowsProviders();
        }
    }

    @FXML
    private void onActionProviders(ActionEvent event) {
        openWindowsProviders();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
