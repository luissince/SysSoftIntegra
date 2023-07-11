package controller.menus;

import controller.banco.FxBancoController;
import controller.configuracion.almacen.FxAlmacenController;
import controller.configuracion.comprobante.FxTipoDocumentoController;
import controller.configuracion.empleados.FxEmpleadosController;
import controller.configuracion.etiquetas.FxEtiquetasController;
import controller.configuracion.impresoras.FxImpresoraController;
import controller.configuracion.impuestos.FxImpuestoController;
import controller.configuracion.miempresa.FxMiEmpresaController;
import controller.configuracion.moneda.FxMonedaController;
import controller.configuracion.roles.FxRolesController;
import controller.configuracion.tablasbasicas.FxDetalleMantenimientoController;
import controller.configuracion.tickets.FxTicketController;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.SubMenusTB;

public class FxConfiguracionController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private HBox hbOperacionesUno;
    @FXML
    private VBox btnEmpresa;
    @FXML
    private VBox btnTablasBasicas;
    @FXML
    private VBox btnRoles;
    @FXML
    private VBox btnEmpleados;
    @FXML
    private VBox btnMoneda;
    @FXML
    private VBox btnComprobante;
    @FXML
    private HBox hbOperacionesDos;
    @FXML
    private VBox btnImpuesto;
    @FXML
    private VBox btnTickets;
    @FXML
    private VBox btnEtiqueta;
    @FXML
    private VBox btnImpresora;
    @FXML
    private VBox btnAlmacen;
    @FXML
    private VBox btnBancos;
    /*
     * Objectos de la ventana principal y venta que agrega al os hijos
     */
    private FxPrincipalController fxPrincipalController;
    /*
     * Controller empresa
     */
    private FXMLLoader fXMLEmpresa;

    private ScrollPane nodeMiEmpresa;

    private FxMiEmpresaController controllerMiEmpresa;
    /*
     * Controller tablas básicas
     */
    private FXMLLoader fXMLTablasBasicas;

    private VBox nodeTablasBasicas;

    private FxDetalleMantenimientoController controllerTablasBasicas;
    /*
     * Controller privilegios
     */
    private FXMLLoader fXMLTablasRoles;

    private VBox nodeRoles;

    private FxRolesController controllerRoles;

    /*
     * Controller empleado
     */
    private FXMLLoader fXMLEmpleado;

    private VBox nodeEmpleado;

    private FxEmpleadosController controllerEmpleado;

    /*
     * Controller moneda
     */
    private FXMLLoader fXMLMoneda;

    private VBox nodeMoneda;

    private FxMonedaController controllerMoneda;

    /*
     * Controller moneda
     */
    private FXMLLoader fXMLImpuesto;

    private VBox nodeImpuesto;

    private FxImpuestoController controllerImpuesto;

    /*
     * Controller ticket
     */
    private FXMLLoader fXMLTicket;

    private VBox nodeTicketa;

    private FxTicketController controllerTicket;
    /*
     * Controller etiquetas
     */
    private FXMLLoader fXMLEtiquetas;

    private VBox nodeEtiqueta;

    private FxEtiquetasController controllerEtiqueta;
    /*
     * Controller impresora
     */
    private FXMLLoader fXMLImpresora;

    private VBox nodeImpresora;

    private FxImpresoraController controllerImpresora;
    /*
     * Controller impresora
     */
    private FXMLLoader fXMLTipoDocumento;

    private VBox nodeTipoDocumento;

    private FxTipoDocumentoController controllerTipoDocumento;
    /*
     * Controller almacen
     */
    private FXMLLoader fXMLAlmacen;

    private VBox nodeAlmacen;

    private FxAlmacenController controllerAlmacen;

    /*
     * Controller banco consultas
     */
    private FXMLLoader fXMLBancos;

    private HBox nodeBancos;

    private FxBancoController bancosController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLEmpresa = new FXMLLoader(getClass().getResource(FilesRouters.FX_MI_EMPRESA));
            nodeMiEmpresa = fXMLEmpresa.load();
            controllerMiEmpresa = fXMLEmpresa.getController();

            fXMLTablasBasicas = new FXMLLoader(getClass().getResource(FilesRouters.FX_DETALLE_MANTENIMIENTO));
            nodeTablasBasicas = fXMLTablasBasicas.load();
            controllerTablasBasicas = fXMLTablasBasicas.getController();

            fXMLTablasRoles = new FXMLLoader(getClass().getResource(FilesRouters.FX_ROLES));
            nodeRoles = fXMLTablasRoles.load();
            controllerRoles = fXMLTablasRoles.getController();

            fXMLTicket = new FXMLLoader(getClass().getResource(FilesRouters.FX_TICKET));
            nodeTicketa = fXMLTicket.load();
            controllerTicket = fXMLTicket.getController();

            fXMLImpuesto = new FXMLLoader(getClass().getResource(FilesRouters.FX_IMPUESTO));
            nodeImpuesto = fXMLImpuesto.load();
            controllerImpuesto = fXMLImpuesto.getController();

            fXMLMoneda = new FXMLLoader(getClass().getResource(FilesRouters.FX_MONEDA));
            nodeMoneda = fXMLMoneda.load();
            controllerMoneda = fXMLMoneda.getController();

            fXMLEmpleado = new FXMLLoader(getClass().getResource(FilesRouters.FX_EMPLEADO));
            nodeEmpleado = fXMLEmpleado.load();
            controllerEmpleado = fXMLEmpleado.getController();

            fXMLEtiquetas = new FXMLLoader(getClass().getResource(FilesRouters.FX_ETIQUETA));
            nodeEtiqueta = fXMLEtiquetas.load();
            controllerEtiqueta = fXMLEtiquetas.getController();

            fXMLImpresora = new FXMLLoader(getClass().getResource(FilesRouters.FX_IMPRESORA));
            nodeImpresora = fXMLImpresora.load();
            controllerImpresora = fXMLImpresora.getController();

            fXMLTipoDocumento = new FXMLLoader(getClass().getResource(FilesRouters.FX_TIPO_DOCUMENTO));
            nodeTipoDocumento = fXMLTipoDocumento.load();
            controllerTipoDocumento = fXMLTipoDocumento.getController();

            fXMLAlmacen = new FXMLLoader(getClass().getResource(FilesRouters.FX_ALMACEN_CONFIGURACION));
            nodeAlmacen = fXMLAlmacen.load();
            controllerAlmacen = fXMLAlmacen.getController();

            fXMLBancos = new FXMLLoader(getClass().getResource(FilesRouters.FX_BANCO));
            nodeBancos = fXMLBancos.load();
            bancosController = fXMLBancos.getController();
        } catch (IOException ex) {
            System.out.println("Error en Configuración Controller:" + ex.getLocalizedMessage());
        }
    }

    public void loadSubMenus(ObservableList<SubMenusTB> subMenusTBs) {
        if (subMenusTBs.get(0).getIdSubMenu() != 0 && !subMenusTBs.get(0).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnEmpresa);
        } else {

        }
        if (subMenusTBs.get(1).getIdSubMenu() != 0 && !subMenusTBs.get(1).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnTablasBasicas);
        } else {

        }
        if (subMenusTBs.get(2).getIdSubMenu() != 0 && !subMenusTBs.get(2).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnRoles);
        } else {

        }
        if (subMenusTBs.get(3).getIdSubMenu() != 0 && !subMenusTBs.get(3).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnEmpleados);
        } else {

        }
        if (subMenusTBs.get(4).getIdSubMenu() != 0 && !subMenusTBs.get(4).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnMoneda);
        } else {

        }
        if (subMenusTBs.get(5).getIdSubMenu() != 0 && !subMenusTBs.get(5).isEstado()) {
            hbOperacionesUno.getChildren().remove(btnComprobante);
        } else {

        }
        if (subMenusTBs.get(6).getIdSubMenu() != 0 && !subMenusTBs.get(6).isEstado()) {
            hbOperacionesDos.getChildren().remove(btnImpuesto);
        } else {

        }
        if (subMenusTBs.get(7).getIdSubMenu() != 0 && !subMenusTBs.get(7).isEstado()) {
            hbOperacionesDos.getChildren().remove(btnTickets);
        } else {

        }
        if (subMenusTBs.get(8).getIdSubMenu() != 0 && !subMenusTBs.get(8).isEstado()) {
            hbOperacionesDos.getChildren().remove(btnEtiqueta);
        } else {

        }
        if (subMenusTBs.get(9).getIdSubMenu() != 0 && !subMenusTBs.get(9).isEstado()) {
            hbOperacionesDos.getChildren().remove(btnImpresora);
        } else {

        }

        if (subMenusTBs.get(10).getIdSubMenu() != 0 && !subMenusTBs.get(10).isEstado()) {
            hbOperacionesDos.getChildren().remove(btnAlmacen);
        }
    }

    private void openWindowTablasBasicas() {
        controllerTablasBasicas.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeTablasBasicas, 0d);
        AnchorPane.setTopAnchor(nodeTablasBasicas, 0d);
        AnchorPane.setRightAnchor(nodeTablasBasicas, 0d);
        AnchorPane.setBottomAnchor(nodeTablasBasicas, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeTablasBasicas);
        controllerTablasBasicas.reloadListView();
    }

    private void openWindowCompany() {
        controllerMiEmpresa.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeMiEmpresa, 0d);
        AnchorPane.setTopAnchor(nodeMiEmpresa, 0d);
        AnchorPane.setRightAnchor(nodeMiEmpresa, 0d);
        AnchorPane.setBottomAnchor(nodeMiEmpresa, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeMiEmpresa);
    }

    private void openWindowPrivileges() {
        controllerRoles.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeRoles, 0d);
        AnchorPane.setTopAnchor(nodeRoles, 0d);
        AnchorPane.setRightAnchor(nodeRoles, 0d);
        AnchorPane.setBottomAnchor(nodeRoles, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeRoles);
    }

    private void openWindowEmployes() {
        controllerEmpleado.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeEmpleado, 0d);
        AnchorPane.setTopAnchor(nodeEmpleado, 0d);
        AnchorPane.setRightAnchor(nodeEmpleado, 0d);
        AnchorPane.setBottomAnchor(nodeEmpleado, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeEmpleado);
    }

    private void openWindowMoney() {
        controllerMoneda.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeMoneda, 0d);
        AnchorPane.setTopAnchor(nodeMoneda, 0d);
        AnchorPane.setRightAnchor(nodeMoneda, 0d);
        AnchorPane.setBottomAnchor(nodeMoneda, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeMoneda);
    }

    private void openWindowVoucher() {
        controllerTipoDocumento.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeTipoDocumento, 0d);
        AnchorPane.setTopAnchor(nodeTipoDocumento, 0d);
        AnchorPane.setRightAnchor(nodeTipoDocumento, 0d);
        AnchorPane.setBottomAnchor(nodeTipoDocumento, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeTipoDocumento);
    }

    private void openWindowTex() {
        controllerImpuesto.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeImpuesto, 0d);
        AnchorPane.setTopAnchor(nodeImpuesto, 0d);
        AnchorPane.setRightAnchor(nodeImpuesto, 0d);
        AnchorPane.setBottomAnchor(nodeImpuesto, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeImpuesto);
    }

    private void openWindowTickets() {
        controllerTicket.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeTicketa, 0d);
        AnchorPane.setTopAnchor(nodeTicketa, 0d);
        AnchorPane.setRightAnchor(nodeTicketa, 0d);
        AnchorPane.setBottomAnchor(nodeTicketa, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeTicketa);
    }

    private void openWindowEtiquetas() {
        controllerEtiqueta.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeEtiqueta, 0d);
        AnchorPane.setTopAnchor(nodeEtiqueta, 0d);
        AnchorPane.setRightAnchor(nodeEtiqueta, 0d);
        AnchorPane.setBottomAnchor(nodeEtiqueta, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeEtiqueta);
        // controllerEtiqueta.loadEtiqueta(0, new
        // File("./archivos/etiqueta.json").getAbsolutePath());
    }

    private void openWindowImpresora() {
        controllerImpresora.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeImpresora, 0d);
        AnchorPane.setTopAnchor(nodeImpresora, 0d);
        AnchorPane.setRightAnchor(nodeImpresora, 0d);
        AnchorPane.setBottomAnchor(nodeImpresora, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeImpresora);
    }

    private void openWindowAlmacen() {
        controllerAlmacen.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeAlmacen, 0d);
        AnchorPane.setTopAnchor(nodeAlmacen, 0d);
        AnchorPane.setRightAnchor(nodeAlmacen, 0d);
        AnchorPane.setBottomAnchor(nodeAlmacen, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeAlmacen);
    }

    private void openWindowBanco() {
        bancosController.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeBancos, 0d);
        AnchorPane.setTopAnchor(nodeBancos, 0d);
        AnchorPane.setRightAnchor(nodeBancos, 0d);
        AnchorPane.setBottomAnchor(nodeBancos, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeBancos);
        bancosController.loadTableViewBanco("");
    }

    @FXML
    private void onKeyPressedCompany(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCompany();
        }
    }

    @FXML
    private void onActionCompany(ActionEvent event) {
        openWindowCompany();
    }

    @FXML
    private void onKeyPressedTablasBasicas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowTablasBasicas();
        }
    }

    @FXML
    private void onActionTablasBasicas(ActionEvent event) {
        openWindowTablasBasicas();
    }

    @FXML
    private void onKeyPressedPrivileges(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowPrivileges();
        }
    }

    @FXML
    private void onActionPrivileges(ActionEvent event) {
        openWindowPrivileges();
    }

    @FXML
    private void onKeyPressedEmployes(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEmployes();
        }
    }

    @FXML
    private void onActionEmployes(ActionEvent event) {
        openWindowEmployes();
    }

    @FXML
    private void onKeyPressedMoney(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMoney();
        }
    }

    @FXML
    private void onActionMoney(ActionEvent event) {
        openWindowMoney();
    }

    @FXML
    private void onKeyPressedVoucher(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVoucher();
        }
    }

    @FXML
    private void onActionVoucher(ActionEvent event) {
        openWindowVoucher();
    }

    @FXML
    private void onKeyPressedTax(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowTex();
        }
    }

    @FXML
    private void onActionTax(ActionEvent event) {
        openWindowTex();
    }

    @FXML
    private void onKeyPressedTickets(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowTickets();
        }
    }

    @FXML
    private void onActionTickets(ActionEvent event) {
        openWindowTickets();
    }

    @FXML
    private void onKeyPressedEtiquetas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEtiquetas();
        }
    }

    @FXML
    private void onActionEtiquetas(ActionEvent event) {
        openWindowEtiquetas();
    }

    @FXML
    private void onKeyPressedImpresora(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowImpresora();
        }
    }

    @FXML
    private void onActionImpresora(ActionEvent event) {
        openWindowImpresora();
    }

    @FXML
    private void onKeyPressedAlmacen(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAlmacen();
        }
    }

    @FXML
    private void onActionAlmacen(ActionEvent event) {
        openWindowAlmacen();
    }

    @FXML
    private void onKeyPressedBanco(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowBanco();
        }
    }

    @FXML
    private void onActionBanco(ActionEvent event) {
        openWindowBanco();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
