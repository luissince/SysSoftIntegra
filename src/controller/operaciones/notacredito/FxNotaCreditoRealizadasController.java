package controller.operaciones.notacredito;

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
import model.NotaCreditoTB;
import service.NotaCreditoADO;

public class FxNotaCreditoRealizadasController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<NotaCreditoTB> tvList;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcNumero;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcFechaRegistro;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcCliente;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcComprobante;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcDetalle;
    @FXML
    private TableColumn<NotaCreditoTB, String> tcTotal;
    @FXML
    private DatePicker txtFechaInicio;
    @FXML
    private DatePicker txtFechaFinal;
    @FXML
    private TextField txtSearch;
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
                cellData.getValue().getId()));
        tcFechaRegistro.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaRegistro() + "\n"
                        + cellData.getValue().getHoraRegistro()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getClienteTB().getNumeroDocumento() + "\n"
                        + cellData.getValue().getClienteTB().getInformacion()));
        tcComprobante.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getNombreComprobante() + "\n"
                        + cellData.getValue().getSerie() + "-"
                        + Tools.formatNumber(cellData.getValue().getNumeracion())));
        tcDetalle.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getVentaTB().getTipoDocumentoTB().getNombre() + "\n"
                        + "MODIFICADA: " + cellData.getValue().getVentaTB().getSerie() + "-"
                        + cellData.getValue().getVentaTB().getNumeracion()));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getMonedaTB().getSimbolo() + " "
                        + Tools.roundingValue(cellData.getValue().getImporteNeto(), 2)));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcFechaRegistro.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcDetalle.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        initLoad();
    }

    public void initLoad() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillNotaCreditoTable(0, "", "", "");
            opcion = 0;
        }
    }

    private void fillNotaCreditoTable(int opcion, String buscar, String fechaInico, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return NotaCreditoADO.ListarNotasCredito(opcion, buscar, fechaInico, fechaFinal, (paginacion - 1) * 10,
                        10);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<NotaCreditoTB> notaCreditoTBs = (ObservableList<NotaCreditoTB>) objects[0];
                if (!notaCreditoTBs.isEmpty()) {
                    tvList.setItems(notaCreditoTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 10.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(
                            Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else if (object instanceof String) {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView("Error en traer los datos, intente nuevamente.",
                        "-fx-text-fill:#a70820;", false));
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
            tvList.setPlaceholder(
                    Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillNotaCreditoTable(0, "", "", "");
                break;
            case 1:
                fillNotaCreditoTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                break;
            case 2:
                fillNotaCreditoTable(2, txtSearch.getText().trim(), "", "");
                break;
        }
    }

    private void openWindowNotaCreditoDetalle() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_NOTA_CREDITO_DETALLE));
            AnchorPane node = fXMLLoader.load();
            // Controlller here
            FxNotaCreditoDetalleController controller = fXMLLoader.getController();
            controller.loadInitData(tvList.getSelectionModel().getSelectedItem().getIdNotaCredito());
            controller.setInitNotaCreditoRealizadasController(this, fxPrincipalController);
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } else {
            Tools.AlertMessageWarning(vbWindow, "Nota de Crédito", "Debe seleccionar una compra de la lista");
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
                && event.getCode() != KeyCode.PAUSE) {
            if (!Tools.isText(txtSearch.getText())) {
                if (!lblLoad.isVisible()) {
                    paginacion = 1;
                    fillNotaCreditoTable(2, txtSearch.getText().trim(), "", "");
                    opcion = 2;
                }
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillNotaCreditoTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillNotaCreditoTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 1;
            }
        }
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.actualDate(Tools.getDate(), txtFechaInicio);
            Tools.actualDate(Tools.getDate(), txtFechaFinal);
            txtSearch.selectAll();
            txtSearch.requestFocus();
            initLoad();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        Tools.actualDate(Tools.getDate(), txtFechaInicio);
        Tools.actualDate(Tools.getDate(), txtFechaFinal);
        txtSearch.selectAll();
        txtSearch.requestFocus();
        initLoad();
    }

    @FXML
    private void onKeyPressedMostar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowNotaCreditoDetalle();
        }
    }

    @FXML
    private void onActionMostrar(ActionEvent event) throws IOException {
        openWindowNotaCreditoDetalle();
    }

    @FXML
    private void onKeyPressedTable(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowNotaCreditoDetalle();
        }
        event.consume();
    }

    @FXML
    private void onMouseClickedTable(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowNotaCreditoDetalle();
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
