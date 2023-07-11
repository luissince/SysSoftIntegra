package controller.menus;

import controller.operaciones.cortecaja.FxCajaController;
import controller.posterminal.venta.FxPostVentaController;
import controller.posterminal.venta.FxPostVentaRealizadasController;
import controller.tools.FilesRouters;
import controller.tools.Session;
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
import model.PrivilegioTB;
import model.SubMenusTB;
import service.MenuADO;

public class FxPosTerminalController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private VBox vbContentOne;
    @FXML
    private HBox hbOperacionesUno;
    @FXML
    private VBox btnCorteCaja;
    @FXML
    private VBox btnTerminalUno;
    @FXML
    private VBox btnTerminalDos;
    @FXML
    private VBox btnVentasEchas;

    /*
     * Objectos de la ventana principal y venta que agrega a los hijos
     */
    private FxPrincipalController fxPrincipalController;

    /*
     * Controller ventas
     */
    private FXMLLoader fXMLVentaOld;

    private AnchorPane nodeVentaOld;

    private FxPostVentaController controllerVentaOld;

    /*
     * Controller ventas nueva
     */
    private FXMLLoader fXMLVentaNew;

    private AnchorPane nodeVentaNew;

    private FxPostVentaController controllerVentaNew;

    /*
     * Controller corte de caja
     */
    private FXMLLoader fXMLCorteCaja;

    private VBox nodeCorteCaja;

    private FxCajaController controllerCorteCaja;

    /*
     * Controller ventas realizadas
     */
    private FXMLLoader fXMLVentaRealizadas;

    private VBox nodeVentaRealizadas;

    private FxPostVentaRealizadasController ventaRealizadasController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLVentaOld = new FXMLLoader(getClass().getResource(FilesRouters.FX_POS_VENTA));
            nodeVentaOld = fXMLVentaOld.load();
            controllerVentaOld = fXMLVentaOld.getController();
            controllerVentaOld.setTipoVenta((short) 1);
            controllerVentaOld.loadComponents();

            fXMLVentaNew = new FXMLLoader(getClass().getResource(FilesRouters.FX_POS_VENTA));
            nodeVentaNew = fXMLVentaNew.load();
            controllerVentaNew = fXMLVentaNew.getController();
            controllerVentaNew.setTipoVenta((short) 2);
            controllerVentaNew.loadComponents();

            fXMLCorteCaja = new FXMLLoader(getClass().getResource(FilesRouters.FX_CAJA));
            nodeCorteCaja = fXMLCorteCaja.load();
            controllerCorteCaja = fXMLCorteCaja.getController();

            fXMLVentaRealizadas = new FXMLLoader(getClass().getResource(FilesRouters.FX_POS_VENTA_REALIZADAS));
            nodeVentaRealizadas = fXMLVentaRealizadas.load();
            ventaRealizadasController = fXMLVentaRealizadas.getController();
        } catch (IOException ex) {
            System.out.println("Error en Pos Terminal Controller:" + ex.getLocalizedMessage());
        }
    }

    public void loadSubMenus(ObservableList<SubMenusTB> subMenusTBs) {
        if (subMenusTBs.get(0).getIdSubMenu() != 0 && !subMenusTBs.get(0).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnTerminalUno);
            hbOperacionesUno.getChildren().remove(btnTerminalDos);
        } else {
            ObservableList<PrivilegioTB> privilegioTBs = MenuADO.GetPrivilegios(Session.USER_ROL,
                    subMenusTBs.get(0).getIdSubMenu());
            controllerVentaOld.loadPrivilegios(privilegioTBs);
            controllerVentaNew.loadPrivilegios(privilegioTBs);
        }

        if (subMenusTBs.get(1).getIdSubMenu() != 0 && !subMenusTBs.get(1).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnCorteCaja);
        } else {
            ObservableList<PrivilegioTB> privilegioTBs = MenuADO.GetPrivilegios(Session.USER_ROL,
                    subMenusTBs.get(1).getIdSubMenu());
            controllerCorteCaja.loadPrivilegios(privilegioTBs);
        }

        if (subMenusTBs.get(2).getIdSubMenu() != 0 && !subMenusTBs.get(2).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnVentasEchas);
        } else {
            ObservableList<PrivilegioTB> privilegioTBs = MenuADO.GetPrivilegios(Session.USER_ROL,
                    subMenusTBs.get(0).getIdSubMenu());
            ventaRealizadasController.loadPrivilegios(privilegioTBs);
        }
    }

    private void openWindowVenta() {
        controllerVentaOld.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeVentaOld, 0d);
        AnchorPane.setTopAnchor(nodeVentaOld, 0d);
        AnchorPane.setRightAnchor(nodeVentaOld, 0d);
        AnchorPane.setBottomAnchor(nodeVentaOld, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeVentaOld);
        controllerVentaOld.loadValidarCaja();
        controllerVentaOld.loadElements();
    }

    private void openWindowVentaNueva() {
        controllerVentaNew.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeVentaNew, 0d);
        AnchorPane.setTopAnchor(nodeVentaNew, 0d);
        AnchorPane.setRightAnchor(nodeVentaNew, 0d);
        AnchorPane.setBottomAnchor(nodeVentaNew, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeVentaNew);
        controllerVentaNew.loadValidarCaja();
        controllerVentaNew.loadElements();
    }

    private void openWindowCorteCaja() {
        controllerCorteCaja.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeCorteCaja, 0d);
        AnchorPane.setTopAnchor(nodeCorteCaja, 0d);
        AnchorPane.setRightAnchor(nodeCorteCaja, 0d);
        AnchorPane.setBottomAnchor(nodeCorteCaja, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeCorteCaja);

    }

    private void openWindowVentaRealizadas() {
        ventaRealizadasController.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeVentaRealizadas, 0d);
        AnchorPane.setTopAnchor(nodeVentaRealizadas, 0d);
        AnchorPane.setRightAnchor(nodeVentaRealizadas, 0d);
        AnchorPane.setBottomAnchor(nodeVentaRealizadas, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeVentaRealizadas);
        ventaRealizadasController.loadInit();
    }

    @FXML
    private void onKeyPressedVentas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVenta();
        }
    }

    @FXML
    private void onActionVentas(ActionEvent event) {
        openWindowVenta();
    }

    @FXML
    private void onKeyPressedVentasNueva(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentaNueva();
        }
    }

    @FXML
    private void onActionVentasNueva(ActionEvent event) {
        openWindowVentaNueva();
    }

    @FXML
    private void onKeyPressedCorteCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCorteCaja();
        }
    }

    @FXML
    private void onActionCorteCaja(ActionEvent event) {
        openWindowCorteCaja();
    }

    @FXML
    private void onKeyPressedVentasRealizadas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentaRealizadas();
        }
    }

    @FXML
    private void onActionVentasRealizadas(ActionEvent event) {
        openWindowVentaRealizadas();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
