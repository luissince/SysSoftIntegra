package controller.menus;

import controller.inventario.inventarioinicial.FxInventarioInicialController;
import controller.inventario.lote.FxLoteController;
import controller.inventario.valorinventario.FxValorInventarioController;
import controller.inventario.movimientos.FxMovimientosController;
import controller.inventario.suministros.FxSuministrosController;
import controller.inventario.suministros.FxSuministrosKardexController;
import controller.inventario.traslados.FxTrasladoController;
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
import model.MenuADO;
import model.PrivilegioTB;
import model.SubMenusTB;

public class FxInventarioController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private HBox hbOperacionesUno;
    @FXML
    private HBox hbOperacionesDos;
    @FXML
    private VBox btnProducto;
    @FXML
    private VBox btnKardex;
    @FXML
    private VBox btnValorInventario;
    @FXML
    private VBox btnInventarioInicial;
    @FXML
    private VBox btnMovimiento;
    @FXML
    private VBox btnLote;
    @FXML
    private VBox btnTraslado;
    /*
    Objectos de la ventana principal y venta que agrega al os hijos
     */
    private FxPrincipalController fxPrincipalController;
    /*
    Controller suministros     
     */
    private FXMLLoader fXMLSuministros;

    private HBox nodeSuministros;

    private FxSuministrosController controllerSuministros;

    /*
    Controller movimiento     
     */
    private FXMLLoader fXMLMovimiento;

    private HBox nodeMovimiento;

    private FxMovimientosController controllerMovimiento;

    /*
    Controller kardex
     */
    private FXMLLoader fXMLKardex;

    private HBox nodeKardex;

    private FxSuministrosKardexController controllerKardex;


    /*
    Controller asignacion
     */
    private FXMLLoader fXMLInventarioInicial;

    private AnchorPane nodeInventarioInicial;

    private FxInventarioInicialController controllerInventarioInicial;

    /*
    Controller valor de inventario
     */
    private FXMLLoader fXMLValorInventario;

    private VBox nodeValorInventario;

    private FxValorInventarioController controllerValorInventario;

    /*
    Controller lote
     */
    private FXMLLoader fXMLLote;

    private VBox nodeLote;

    private FxLoteController controllerLote;

    /*
    Controller traslado
     */
    private FXMLLoader fXMLTraslado;

    private VBox nodeTraslado;

    private FxTrasladoController controllerTraslado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLSuministros = new FXMLLoader(getClass().getResource(FilesRouters.FX_SUMINISTROS));
            nodeSuministros = fXMLSuministros.load();
            controllerSuministros = fXMLSuministros.getController();

            fXMLMovimiento = new FXMLLoader(getClass().getResource(FilesRouters.FX_MOVIMIENTOS));
            nodeMovimiento = fXMLMovimiento.load();
            controllerMovimiento = fXMLMovimiento.getController();

            fXMLKardex = new FXMLLoader(getClass().getResource(FilesRouters.FX_SUMINISTROS_KARDEX));
            nodeKardex = fXMLKardex.load();
            controllerKardex = fXMLKardex.getController();

            fXMLInventarioInicial = new FXMLLoader(getClass().getResource(FilesRouters.FX_INVENTARIO_INICIAL));
            nodeInventarioInicial = fXMLInventarioInicial.load();
            controllerInventarioInicial = fXMLInventarioInicial.getController();

            fXMLValorInventario = new FXMLLoader(getClass().getResource(FilesRouters.FX_VALOR_INVENTARIO));
            nodeValorInventario = fXMLValorInventario.load();
            controllerValorInventario = fXMLValorInventario.getController();

            fXMLLote = new FXMLLoader(getClass().getResource(FilesRouters.FX_LOTE));
            nodeLote = fXMLLote.load();
            controllerLote = fXMLLote.getController();

            fXMLTraslado = new FXMLLoader(getClass().getResource(FilesRouters.FX_TRASLADO));
            nodeTraslado = fXMLTraslado.load();
            controllerTraslado = fXMLTraslado.getController();
        } catch (IOException ex) {
            System.out.println("Error en Inventario Controller:" + ex.getLocalizedMessage());
        }
    }

    public void loadSubMenus(ObservableList<SubMenusTB> subMenusTBs) {
        if (subMenusTBs.get(0).getIdSubMenu() != 0 && !subMenusTBs.get(0).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnProducto);
        } else {
            ObservableList<PrivilegioTB> privilegioTBs = MenuADO.GetPrivilegios(Session.USER_ROL, subMenusTBs.get(0).getIdSubMenu());
            controllerSuministros.loadPrivilegios(privilegioTBs);
        }

        if (subMenusTBs.get(1).getIdSubMenu() != 0 && !subMenusTBs.get(1).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnKardex);
        } else {

        }

        if (subMenusTBs.get(2).getIdSubMenu() != 0 && !subMenusTBs.get(2).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnValorInventario);
        } else {

        }

        if (subMenusTBs.get(3).getIdSubMenu() != 0 && !subMenusTBs.get(3).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnInventarioInicial);
        } else {

        }

        if (subMenusTBs.get(4).getIdSubMenu() != 0 && !subMenusTBs.get(4).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnMovimiento);
        } else {
            ObservableList<PrivilegioTB> privilegioTBs = MenuADO.GetPrivilegios(Session.USER_ROL, subMenusTBs.get(4).getIdSubMenu());
            controllerMovimiento.loadPrivilegios(privilegioTBs);
        }

        if (subMenusTBs.get(5).getIdSubMenu() != 0 && !subMenusTBs.get(5).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnLote);
        } else {

        }
        
        if(subMenusTBs.get(6).getIdSubMenu() != 0 && !subMenusTBs.get(6).isEstado()){
            hbOperacionesDos.getChildren().remove(btnTraslado);
        }
    }

    private void openWindowKardex() {
        controllerKardex.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeKardex, 0d);
        AnchorPane.setTopAnchor(nodeKardex, 0d);
        AnchorPane.setRightAnchor(nodeKardex, 0d);
        AnchorPane.setBottomAnchor(nodeKardex, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeKardex);
    }

    private void openWindowValorInventario() {
        controllerValorInventario.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeValorInventario, 0d);
        AnchorPane.setTopAnchor(nodeValorInventario, 0d);
        AnchorPane.setRightAnchor(nodeValorInventario, 0d);
        AnchorPane.setBottomAnchor(nodeValorInventario, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeValorInventario);
    }

    private void openWindowSuministros() {
        controllerSuministros.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeSuministros, 0d);
        AnchorPane.setTopAnchor(nodeSuministros, 0d);
        AnchorPane.setRightAnchor(nodeSuministros, 0d);
        AnchorPane.setBottomAnchor(nodeSuministros, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeSuministros);
        if (controllerSuministros.getTvList().getItems().isEmpty()) {
            controllerSuministros.fillTableSuministros((short) 0, "", "", 0, 0);
        }
    }

    private void openWindowMovimientos() {
        controllerMovimiento.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeMovimiento, 0d);
        AnchorPane.setTopAnchor(nodeMovimiento, 0d);
        AnchorPane.setRightAnchor(nodeMovimiento, 0d);
        AnchorPane.setBottomAnchor(nodeMovimiento, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeMovimiento);
        if (controllerMovimiento.getTvList().getItems().isEmpty()) {
            controllerMovimiento.setOpcion((short) 1);
            controllerMovimiento.loadInitTable();
        }
    }

    private void openWindowLote() {
        controllerLote.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeLote, 0d);
        AnchorPane.setTopAnchor(nodeLote, 0d);
        AnchorPane.setRightAnchor(nodeLote, 0d);
        AnchorPane.setBottomAnchor(nodeLote, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeLote);
        controllerLote.fillLoteTable((short) 0, "");

    }

    private void openWindowInventarioInicial() {
        controllerInventarioInicial.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeInventarioInicial, 0d);
        AnchorPane.setTopAnchor(nodeInventarioInicial, 0d);
        AnchorPane.setRightAnchor(nodeInventarioInicial, 0d);
        AnchorPane.setBottomAnchor(nodeInventarioInicial, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeInventarioInicial);
    }

    private void openWindowTraslado() {
        controllerTraslado.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeTraslado, 0d);
        AnchorPane.setTopAnchor(nodeTraslado, 0d);
        AnchorPane.setRightAnchor(nodeTraslado, 0d);
        AnchorPane.setBottomAnchor(nodeTraslado, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeTraslado);
    }

    @FXML
    private void onKeyPressedInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowValorInventario();
        }
    }

    @FXML
    private void onActionInventario(ActionEvent event) {
        openWindowValorInventario();
    }

    @FXML
    private void onKeyPressedInventarioInicial(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowInventarioInicial();
        }
    }

    @FXML
    private void onActionInventarioInicial(ActionEvent event) {
        openWindowInventarioInicial();
    }

    @FXML
    private void onKeyPressedSuministros(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministros();
        }
    }

    @FXML
    private void onActionSuministros(ActionEvent event) {
        openWindowSuministros();
    }

    @FXML
    private void onKeyPressedMovimientos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMovimientos();
        }
    }

    @FXML
    private void onActionMovimientos(ActionEvent event) {
        openWindowMovimientos();
    }

    @FXML
    private void onKeyPressedKardex(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowKardex();
        }
    }

    @FXML
    private void onActionKardex(ActionEvent event) {
        openWindowKardex();
    }

    @FXML
    private void onKeyPressedLotes(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowLote();
        }
    }

    @FXML
    private void onActionLotes(ActionEvent event) {
        openWindowLote();
    }

    @FXML
    private void onActionMovimiento(ActionEvent event) {
        openWindowTraslado();
    }

    @FXML
    private void onKeyPressedMovimiento(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowTraslado();
        }
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
