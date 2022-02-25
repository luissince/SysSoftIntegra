package controller.consultas.compras;

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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.CompraADO;
import model.CompraTB;
import model.DetalleADO;
import model.DetalleTB;

public class FxComprasRealizadasController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<CompraTB> tvList;
    @FXML
    private TableColumn<CompraTB, String> tcId;
    @FXML
    private TableColumn<CompraTB, String> tcFechaCompra;
    @FXML
    private TableColumn<CompraTB, String> tcNumeracion;
    @FXML
    private TableColumn<CompraTB, String> tcProveedor;
    @FXML
    private TableColumn<CompraTB, String> tcTipo;
    @FXML
    private TableColumn<Label, String> tcEstado;
    @FXML
    private TableColumn<CompraTB, String> tcTotal;
    @FXML
    private ComboBox<DetalleTB> cbEstadoCompra;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private ComboBox<DetalleTB> cbComprobantes;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;

        tcId.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFechaCompra.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaCompra() + "\n"
                + cellData.getValue().getHoraCompra()
        ));
        tcNumeracion.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getComprobante() + "\n" + cellData.getValue().getSerie().toUpperCase() + "-" + cellData.getValue().getNumeracion()));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getProveedorTB().getNumeroDocumento() + "\n" + cellData.getValue().getProveedorTB().getRazonSocial().toUpperCase()
        ));
        tcTipo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoName()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getTotal(), 2)));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcFechaCompra.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcNumeracion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcProveedor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.23));
        tcTipo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);

        cbComprobantes.getItems().add(new DetalleTB(0, "TODOS"));
        cbComprobantes.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0015", ""));
        cbComprobantes.getSelectionModel().select(0);

        cbEstadoCompra.getItems().add(new DetalleTB(0, "TODOS"));
        cbEstadoCompra.getItems().add(new DetalleTB(1, "PAGADO"));
        cbEstadoCompra.getItems().add(new DetalleTB(2, "POR PAGAR"));
        cbEstadoCompra.getItems().add(new DetalleTB(3, "ANULADO"));
        cbEstadoCompra.getSelectionModel().select(0);

        loadInit();
    }

    public void loadInit() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillPurchasesTable(0, "", "", "", 0, 0);
            opcion = 0;

        }
    }

    private void fillPurchasesTable(int opcion, String value, String fechaInicial, String fechaFinal, int comprobante, int estadoCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CompraADO.Listar_Compras_Realizadas(opcion, value, fechaInicial, fechaFinal, comprobante, estadoCompra, (paginacion - 1) * 20, 20);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<CompraTB> empList = (ObservableList<CompraTB>) objects[0];
                if (!empList.isEmpty()) {
                    tvList.setItems(empList);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            }

            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
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

    private void openWindowDetalleCompra() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLPrincipal = new FXMLLoader(getClass().getResource(FilesRouters.FX_COMPRAS_DETALLE));
            AnchorPane node = fXMLPrincipal.load();

            FxComprasDetalleController controller = fXMLPrincipal.getController();
            controller.setInitComptrasController(this, fxPrincipalController);
            controller.setLoadDetalle(tvList.getSelectionModel().getSelectedItem().getIdCompra());

            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);

        } else {
            Tools.AlertMessage(vbWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Compra", "Debe seleccionar una compra de la lista", false);
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillPurchasesTable(0, "", "", "", 0, 0);
                break;
            case 1:
                fillPurchasesTable(1, txtSearch.getText().trim(), "", "", 0, 0);
                break;
            case 2:
                fillPurchasesTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobantes.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbEstadoCompra.getSelectionModel().getSelectedItem().getIdDetalle());
                break;
        }
    }

    @FXML
    private void onActionView(ActionEvent event) throws IOException {
        openWindowDetalleCompra();
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowDetalleCompra();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        loadInit();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInit();
        }
    }

    @FXML
    private void onActionSearch(ActionEvent event) {
        if (!tvList.getItems().isEmpty()) {
            tvList.requestFocus();
            tvList.getSelectionModel().select(0);
        }
    }

    @FXML
    private void onKeyReleasedSeach(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE
                && event.getCode() != KeyCode.F1
                && event.getCode() != KeyCode.F2
                && event.getCode() != KeyCode.F3
                && event.getCode() != KeyCode.F4
                && event.getCode() != KeyCode.F5
                && event.getCode() != KeyCode.F6
                && event.getCode() != KeyCode.F7
                && event.getCode() != KeyCode.F8
                && event.getCode() != KeyCode.F9
                && event.getCode() != KeyCode.F10
                && event.getCode() != KeyCode.F11
                && event.getCode() != KeyCode.F12
                && event.getCode() != KeyCode.ALT
                && event.getCode() != KeyCode.CONTROL
                && event.getCode() != KeyCode.UP
                && event.getCode() != KeyCode.DOWN
                && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.LEFT
                && event.getCode() != KeyCode.TAB
                && event.getCode() != KeyCode.CAPS
                && event.getCode() != KeyCode.SHIFT
                && event.getCode() != KeyCode.HOME
                && event.getCode() != KeyCode.WINDOWS
                && event.getCode() != KeyCode.ALT_GRAPH
                && event.getCode() != KeyCode.CONTEXT_MENU
                && event.getCode() != KeyCode.END
                && event.getCode() != KeyCode.INSERT
                && event.getCode() != KeyCode.PAGE_UP
                && event.getCode() != KeyCode.PAGE_DOWN
                && event.getCode() != KeyCode.NUM_LOCK
                && event.getCode() != KeyCode.PRINTSCREEN
                && event.getCode() != KeyCode.SCROLL_LOCK
                && event.getCode() != KeyCode.PAUSE
                && event.getCode() != KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(1, txtSearch.getText().trim(), "", "", 0, 0);
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent actionEvent) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobantes.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbEstadoCompra.getSelectionModel().getSelectedItem().getIdDetalle());
                opcion = 2;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent actionEvent) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobantes.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbEstadoCompra.getSelectionModel().getSelectedItem().getIdDetalle());
                opcion = 2;
            }
        }
    }

    @FXML
    private void OnActionComprobantes(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobantes.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbEstadoCompra.getSelectionModel().getSelectedItem().getIdDetalle());
                opcion = 2;
            }
        }
    }

    @FXML
    private void OnActionEstadoCompra(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobantes.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbEstadoCompra.getSelectionModel().getSelectedItem().getIdDetalle());
                opcion = 2;
            }
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

    public VBox getWindow() {
        return vbWindow;
    }

    public TableView<CompraTB> getTvList() {
        return tvList;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
