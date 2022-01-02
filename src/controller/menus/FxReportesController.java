package controller.menus;

import controller.reporte.FxCompraReporteController;
import controller.reporte.FxInventarioReporteController;
import controller.reporte.FxNotaCreditoReporteController;
import controller.reporte.FxPosReporteController;
import controller.reporte.FxProduccionReporteController;
import controller.reporte.FxVentaReporteController;
import controller.reporte.FxVentaUtilidadesController;
import controller.tools.FilesRouters;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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

public class FxReportesController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private TextField txtSearch;
    /*
    Objectos de la ventana principal y venta que agrega al os hijos
     */
    private FxPrincipalController fxPrincipalController;
    /*
    Controller ventas
     */
    private FXMLLoader fXMLVenta;

    private VBox nodeVenta;

    private FxVentaReporteController controllerReporteVenta;
    /*
    Controller compras
     */
    private FXMLLoader fXMLCompra;

    private VBox nodeCompra;

    private FxCompraReporteController controllerReporteCompra;
    
    /*
    Controller utilidades
     */
    private FXMLLoader fXMLUtilidades;

    private VBox nodeUtilidades;

    private FxVentaUtilidadesController controllerUtilidades;

    /*
    Controller nota credito
     */
    private FXMLLoader fXMLNotaCredito;

    private VBox nodeNotaCredito;

    private FxNotaCreditoReporteController controllerNotaCredito;

    /*
    Controller ventas pos
     */
    private FXMLLoader fXMLVentasPos;

    private VBox nodeVentasPos;

    private FxPosReporteController controllerVentasPos;

    /*
    Controller inventario
     */
    private FXMLLoader fXMLInvetario;

    private VBox nodeInventario;

    private FxInventarioReporteController controllerInventario;

    /*
    Controller produccion
     */
    private FXMLLoader fXMLProduccion;

    private VBox nodeProduccion;

    private FxProduccionReporteController controllerProduccion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLVenta = new FXMLLoader(getClass().getResource(FilesRouters.FX_VENTA_REPORTE));
            nodeVenta = fXMLVenta.load();
            controllerReporteVenta = fXMLVenta.getController();

            fXMLCompra = new FXMLLoader(getClass().getResource(FilesRouters.FX_COMPRA_REPORTE));
            nodeCompra = fXMLCompra.load();
            controllerReporteCompra = fXMLCompra.getController();

            fXMLUtilidades = new FXMLLoader(getClass().getResource(FilesRouters.FX_VENTA_UTILIDADES));
            nodeUtilidades = fXMLUtilidades.load();
            controllerUtilidades = fXMLUtilidades.getController();

            fXMLNotaCredito = new FXMLLoader(getClass().getResource(FilesRouters.FX_NOTA_CREDITO_REPORTE));
            nodeNotaCredito = fXMLNotaCredito.load();
            controllerNotaCredito = fXMLNotaCredito.getController();

            fXMLVentasPos = new FXMLLoader(getClass().getResource(FilesRouters.FX_REPORTE_POS));
            nodeVentasPos = fXMLVentasPos.load();
            controllerVentasPos = fXMLVentasPos.getController();

            fXMLInvetario = new FXMLLoader(getClass().getResource(FilesRouters.FX_INVENTARIO_REPORTE));
            nodeInventario = fXMLInvetario.load();
            controllerInventario = fXMLInvetario.getController();

            fXMLProduccion = new FXMLLoader(getClass().getResource(FilesRouters.FX_PRODUCCION_REPORTE));
            nodeProduccion = fXMLProduccion.load();
            controllerProduccion = fXMLProduccion.getController();
        } catch (IOException ex) {
            System.out.println("Error en Ingresos Controller:" + ex.getLocalizedMessage());
        }
    }

    private void openWindowVentas() {
        controllerReporteVenta.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeVenta, 0d);
        AnchorPane.setTopAnchor(nodeVenta, 0d);
        AnchorPane.setRightAnchor(nodeVenta, 0d);
        AnchorPane.setBottomAnchor(nodeVenta, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeVenta);
    }

    private void openWindowPos() {
        controllerVentasPos.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeVentasPos, 0d);
        AnchorPane.setTopAnchor(nodeVentasPos, 0d);
        AnchorPane.setRightAnchor(nodeVentasPos, 0d);
        AnchorPane.setBottomAnchor(nodeVentasPos, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeVentasPos);
    }

    private void openWindowNotaCredito() {
        controllerNotaCredito.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeNotaCredito, 0d);
        AnchorPane.setTopAnchor(nodeNotaCredito, 0d);
        AnchorPane.setRightAnchor(nodeNotaCredito, 0d);
        AnchorPane.setBottomAnchor(nodeNotaCredito, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeNotaCredito);
    }

    private void openWindowCompras() {
        controllerReporteCompra.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeCompra, 0d);
        AnchorPane.setTopAnchor(nodeCompra, 0d);
        AnchorPane.setRightAnchor(nodeCompra, 0d);
        AnchorPane.setBottomAnchor(nodeCompra, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeCompra);
    }

    private void openWindowUtilidades() {
        controllerUtilidades.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeUtilidades, 0d);
        AnchorPane.setTopAnchor(nodeUtilidades, 0d);
        AnchorPane.setRightAnchor(nodeUtilidades, 0d);
        AnchorPane.setBottomAnchor(nodeUtilidades, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeUtilidades);
    }

    private void openWindowInventario() {
        controllerInventario.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeInventario, 0d);
        AnchorPane.setTopAnchor(nodeInventario, 0d);
        AnchorPane.setRightAnchor(nodeInventario, 0d);
        AnchorPane.setBottomAnchor(nodeInventario, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeInventario);
    }

    private void openWindowProduccion() {
        controllerProduccion.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeProduccion, 0d);
        AnchorPane.setTopAnchor(nodeProduccion, 0d);
        AnchorPane.setRightAnchor(nodeProduccion, 0d);
        AnchorPane.setBottomAnchor(nodeProduccion, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeProduccion);
    }

    @FXML
    private void onKeyPressedVentas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentas();
        }
    }

    @FXML
    private void onActionVentas(ActionEvent event) {
        openWindowVentas();
    }

    @FXML
    private void onKeyPressedPos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowPos();
        }
    }

    @FXML
    private void onActionPos(ActionEvent event) {
        openWindowPos();
    }

    @FXML
    private void onKeyPressedNotaCredito(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowNotaCredito();
        }
    }

    @FXML
    private void onActionNotaCredito(ActionEvent event) {
        openWindowNotaCredito();
    }

    @FXML
    private void onKeyPressedCompras(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCompras();
        }
    }

    @FXML
    private void onActionCompras(ActionEvent event) {
        openWindowCompras();
    }

    @FXML
    private void onKeyPressedUtilidades(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowUtilidades();
        }
    }

    @FXML
    private void onActionUtilidades(ActionEvent event) {
        openWindowUtilidades();
    }

    @FXML
    private void onKeyPressedInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowInventario();
        }
    }

    @FXML
    private void onActionInventario(ActionEvent event) {
        openWindowInventario();
    }

    @FXML
    private void onKeyPressedProduccion(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProduccion();
            event.consume();
        }
    }

    @FXML
    private void onActionProduccion(ActionEvent event) {
        openWindowProduccion();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
