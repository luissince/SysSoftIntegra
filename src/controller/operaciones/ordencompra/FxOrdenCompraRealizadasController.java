package controller.operaciones.ordencompra;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.OrdenCompraADO;
import model.OrdenCompraTB;

public class FxOrdenCompraRealizadasController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker txtFechaInicio;
    @FXML
    private DatePicker txtFechaFinal;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<OrdenCompraTB> tvList;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcNumero;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcFechaRegistro;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcProveedor;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcOrdenCompra;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcObservacion;
    @FXML
    private TableColumn<OrdenCompraTB, String> tcTotal;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;
        Tools.actualDate(Tools.getDate(), txtFechaInicio);
        Tools.actualDate(Tools.getDate(), txtFechaFinal);

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getId()
        ));
        tcFechaRegistro.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaRegistro() + "\n"
                + cellData.getValue().getHoraRegistro()
        ));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getProveedorTB().getNumeroDocumento() + "\n"
                + cellData.getValue().getProveedorTB().getRazonSocial()
        ));
        tcOrdenCompra.setCellValueFactory(cellData -> Bindings.concat(
                "N° - " + Tools.formatNumber(cellData.getValue().getNumeracion())
        ));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getObservacion().toUpperCase()
        ));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getTotal(), 2)
        ));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcFechaRegistro.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcProveedor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcOrdenCompra.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcObservacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        initLoad();
    }

    public void initLoad() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillOrdenCompraTable(0, "", "", "");
            opcion = 0;
        }
    }

    private void fillOrdenCompraTable(int opcion, String buscar, String fechaInico, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return OrdenCompraADO.ListarOrdenCompra(opcion, buscar, fechaInico, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<OrdenCompraTB> ordenCompraTBs = (ObservableList<OrdenCompraTB>) objects[0];
                if (!ordenCompraTBs.isEmpty()) {
                    tvList.setItems(ordenCompraTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 10.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else if (object instanceof String) {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView("Error en traer los datos, intente nuevamente.", "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowDetalleOrdenCompra() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_ORDEN_COMPRA_DETALLE));
            AnchorPane node = fXMLLoader.load();
            //Controlller here
            FxOrdenCompraDetalleController controller = fXMLLoader.getController();
            controller.setInitOrdenCompraRealizadasController(this, fxPrincipalController);
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
            controller.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdOrdenCompra());
        } else {
            Tools.AlertMessageWarning(vbWindow, "Orden de Compra", "Debe seleccionar una orden de compra de la lista");
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillOrdenCompraTable(0, "", "", "");
                break;
            case 1:
                fillOrdenCompraTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                break;
            case 2:
                fillOrdenCompraTable(2, txtSearch.getText().trim(), "", "");
                break;
        }
    }

    @FXML
    private void onKeyPressedMostrar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleOrdenCompra();
        }
    }

    @FXML
    private void onActionMostrar(ActionEvent event) throws IOException {
        openWindowDetalleOrdenCompra();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initLoad();
        }
    }

    @FXML
    private void onActionRecarga(ActionEvent event) {
        initLoad();
    }

    @FXML
    private void onActionFechaInicio(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillOrdenCompraTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillOrdenCompraTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 1;
            }
        }
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
            }
        }

    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillOrdenCompraTable(2, txtSearch.getText().trim(), "", "");
            opcion = 2;
        }
    }

    @FXML
    private void onKeyPressedTvList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleOrdenCompra();
        }
    }

    @FXML
    private void onMouseClickedTvList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowDetalleOrdenCompra();
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
        }
    }

    public VBox getVbWindow() {
        return vbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
