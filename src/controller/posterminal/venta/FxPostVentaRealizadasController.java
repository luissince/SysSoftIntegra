package controller.posterminal.venta;

import controller.configuracion.empleados.FxEmpleadosListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.*;
import service.DetalleADO;
import service.TipoDocumentoADO;
import service.VentaADO;

public class FxPostVentaRealizadasController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<VentaTB> tvList;
    @FXML
    private TableColumn<VentaTB, Integer> tcId;
    @FXML
    private TableColumn<VentaTB, String> tcFechaVenta;
    @FXML
    private TableColumn<VentaTB, String> tcCliente;
    @FXML
    private TableColumn<VentaTB, String> tcTipo;
    @FXML
    private TableColumn<VentaTB, Label> tcEstado;
    @FXML
    private TableColumn<VentaTB, String> tcSerie;
    @FXML
    private TableColumn<VentaTB, String> tcTotal;
    @FXML
    private ComboBox<DetalleTB> cbEstado;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<TipoDocumentoTB> cbComprobante;
    @FXML
    private HBox hbVendedor;
    @FXML
    private TextField txtVendedor;
    @FXML
    private Button btnMostrar;
    @FXML
    private Button btnRecargar;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private Label lblMotonTotal;

    private FxPrincipalController fxPrincipalController;

    private String idEmpleado;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    private boolean buscarTodos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;
        loadTableView();
        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);

        cbEstado.getItems().add(new DetalleTB(0, "TODOS"));
        cbEstado.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0009", ""));
        cbEstado.getSelectionModel().select(0);

        cbComprobante.getItems().add(new TipoDocumentoTB(0, "TODOS"));
        cbComprobante.getItems().addAll(TipoDocumentoADO.GetDocumentoCombBoxVentas());
        cbComprobante.getSelectionModel().select(0);

        idEmpleado = Session.USER_ID;
        txtVendedor.setText(Session.USER_NAME.toUpperCase());
    }

    private void loadTableView() {
        tcId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tcFechaVenta.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaVenta() + "\n"
                        + cellData.getValue().getHoraVenta()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getClienteTB().getNumeroDocumento() + "\n"
                        + cellData.getValue().getClienteTB().getInformacion()));
        tcTipo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoName()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));
        tcSerie.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getTipoDocumentoTB().getNombre() + "\n"
                        + cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion()
                        + (cellData.getValue().getNotaCreditoTB() != null
                                ? " (NOTA CREDITO: " + cellData.getValue().getNotaCreditoTB().getSerie() + "-"
                                        + cellData.getValue().getNotaCreditoTB().getNumeracion() + ")"
                                : "")));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " "
                + Tools.roundingValue(cellData.getValue().getTotal(), 2)));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcFechaVenta.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcSerie.prefWidthProperty().bind(tvList.widthProperty().multiply(0.17));
        tcTipo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            btnMostrar.setDisable(true);
        }
        if (privilegioTBs.get(1).getIdPrivilegio() != 0 && !privilegioTBs.get(1).isEstado()) {
            btnRecargar.setDisable(true);
        }
        if (privilegioTBs.get(2).getIdPrivilegio() != 0 && !privilegioTBs.get(2).isEstado()) {
            dtFechaInicial.setDisable(true);
        }
        if (privilegioTBs.get(3).getIdPrivilegio() != 0 && !privilegioTBs.get(3).isEstado()) {
            dtFechaFinal.setDisable(true);
        }
        if (privilegioTBs.get(4).getIdPrivilegio() != 0 && !privilegioTBs.get(4).isEstado()) {
            cbComprobante.setDisable(true);
        }
        if (privilegioTBs.get(5).getIdPrivilegio() != 0 && !privilegioTBs.get(5).isEstado()) {
            cbEstado.setDisable(true);
        }
        if (privilegioTBs.get(6).getIdPrivilegio() != 0 && !privilegioTBs.get(6).isEstado()) {
            hbVendedor.setDisable(true);
            lblMotonTotal.setVisible(false);
        }
        if (privilegioTBs.get(7).getIdPrivilegio() != 0 && !privilegioTBs.get(7).isEstado()) {
            txtSearch.setDisable(true);
        }
        if (privilegioTBs.get(8).getIdPrivilegio() != 0 && privilegioTBs.get(8).isEstado()) {
            buscarTodos = true;
        }
    }

    public void loadInit() {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            paginacion = 1;
            fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                    cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                    cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
            opcion = 0;
        }
    }

    private void fillVentasTable(short opcion, String value, String fechaInicial, String fechaFinal, int comprobante,
            int estado, String usuario) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return VentaADO.Listar_Ventas_Pos(opcion, value, fechaInicial, fechaFinal, comprobante, estado, usuario,
                        (paginacion - 1) * 20, 20);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<VentaTB> ventaTBs = (ObservableList<VentaTB>) objects[0];
                if (!ventaTBs.isEmpty()) {
                    tvList.setItems(ventaTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                    lblMotonTotal.setText(Tools.roundingValue((double) objects[2], 2));
                } else {
                    tvList.setPlaceholder(
                            Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                    lblMotonTotal.setText(Tools.roundingValue(0, 2));
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
                lblMotonTotal.setText(Tools.roundingValue(0, 2));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(),
                    "-fx-text-fill:#a70820;", false));
            lblMotonTotal.setText(Tools.roundingValue(0, 2));
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(
                    Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
            lblMotonTotal.setText(Tools.roundingValue(0, 2));
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowDetalleVenta() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_POS_VENTA_DETALLE));
            AnchorPane node = fXMLLoader.load();
            // Controlller here
            FxPostVentaDetalleController controller = fXMLLoader.getController();
            controller.setInitVentasController(this, fxPrincipalController);
            controller.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdVenta());
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } else {
            Tools.AlertMessageWarning(window, "Ventas", "Debe seleccionar una compra de la lista");
        }
    }

    private void openWindowVendedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxEmpleadosListaController controller = fXMLLoader.getController();
            controller.setInitPostVentaRealizadasController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un vendedor", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Venta reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                break;
            case 1:
                fillVentasTable((short) (!buscarTodos ? 1 : 2), txtSearch.getText().trim(), "", "", 0, 0, idEmpleado);
                break;
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
    private void onKeyRelasedSearch(KeyEvent event) {
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
                    fillVentasTable((short) (!buscarTodos ? 1 : 2), txtSearch.getText().trim(), "", "", 0, 0,
                            idEmpleado);
                    opcion = 1;
                }
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                opcion = 0;
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            if (btnMostrar.isDisable()) {
                return;
            }
            openWindowDetalleVenta();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            if (btnMostrar.isDisable()) {
                return;
            }
            openWindowDetalleVenta();
        }
    }

    @FXML
    private void onKeyPressedMostar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleVenta();
        }
    }

    @FXML
    private void onActionMostrar(ActionEvent event) throws IOException {
        openWindowDetalleVenta();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                cbEstado.getSelectionModel().select(0);
                Tools.actualDate(Tools.getDate(), dtFechaInicial);
                Tools.actualDate(Tools.getDate(), dtFechaFinal);
                if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                    paginacion = 1;
                    fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial),
                            Tools.getDatePicker(dtFechaFinal),
                            cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                            cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                    opcion = 0;
                }
            }
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            cbEstado.getSelectionModel().select(0);
            Tools.actualDate(Tools.getDate(), dtFechaInicial);
            Tools.actualDate(Tools.getDate(), dtFechaFinal);
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionComprobante(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionEstado(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable((short) 0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal),
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        cbEstado.getSelectionModel().getSelectedItem().getIdDetalle(), idEmpleado);
                opcion = 0;
            }
        }
    }

    @FXML
    private void onKeyPressedVendedor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVendedores();
        }
    }

    @FXML
    private void onActionVendedor(ActionEvent event) {
        openWindowVendedores();
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
        return window;
    }

    public TableView<VentaTB> getTvList() {
        return tvList;
    }

    public TextField getTxtVendedor() {
        return txtVendedor;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }
}
