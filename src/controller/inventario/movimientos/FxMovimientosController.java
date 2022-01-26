package controller.inventario.movimientos;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.AjusteInventarioADO;
import model.AjusteInventarioTB;
import model.PrivilegioTB;
import model.TipoMovimientoADO;
import model.TipoMovimientoTB;

public class FxMovimientosController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private ComboBox<TipoMovimientoTB> cbMovimiento;
    @FXML
    private TableView<AjusteInventarioTB> tvList;
    @FXML
    private TableColumn<AjusteInventarioTB, String> tcNumero;
    @FXML
    private TableColumn<AjusteInventarioTB, String> tcTipoMovimiento;
    @FXML
    private TableColumn<AjusteInventarioTB, String> tcFecha;
    @FXML
    private TableColumn<AjusteInventarioTB, String> tcObservacion;
    @FXML
    private TableColumn<AjusteInventarioTB, String> tcInformacion;
    @FXML
    private TableColumn<AjusteInventarioTB, Label> tcEstado;
    @FXML
    private TableColumn<AjusteInventarioTB, Button> tcOpcion;
    @FXML
    private DatePicker dtFechaInicio;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private Button btnRealizarMovimiento;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FXMLLoader fXMLLoaderMovimientoDetalle;

    private AnchorPane nodeMovimientoDetalle;

    private FxMovimientosProcesoController controllerMovimientoDetalle;

    private ObservableList<PrivilegioTB> privilegioTBs;

    private FxPrincipalController fxPrincipalController;

    private short opcion;

    private int paginacion;

    private int totalPaginacion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcTipoMovimiento.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoMovimientoName()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservacion()));
        tcInformacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getInformacion()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("lblEstado"));
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("validar"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcTipoMovimiento.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcObservacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.22));
        tcInformacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        cbMovimiento.getItems().add(new TipoMovimientoTB(0, "--TODOS--", false));
        cbMovimiento.getItems().addAll(TipoMovimientoADO.Get_list_Tipo_Movimiento(true, true));
        cbMovimiento.getSelectionModel().select(0);

        Tools.actualDate(Tools.getDate(), dtFechaInicio);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);

        try {
            fXMLLoaderMovimientoDetalle = WindowStage.LoaderWindow(getClass().getResource(FilesRouters.FX_MOVIMIENTOS_PROCESO));
            nodeMovimientoDetalle = fXMLLoaderMovimientoDetalle.load();
            controllerMovimientoDetalle = fXMLLoaderMovimientoDetalle.getController();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        this.privilegioTBs = privilegioTBs;
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            btnRealizarMovimiento.setDisable(!privilegioTBs.get(0).isEstado());
        }
    }

    public void loadInitTable() {
        paginacion = 1;
        fillTableMovimiento(0, 0, "", "");
        opcion = 0;
    }

    private void fillTableMovimiento(int opcion, int movimiento, String fechaInicial, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return AjusteInventarioADO.Listar_Movimiento_Inventario(opcion, movimiento, fechaInicial, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded((WorkerStateEvent e) -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<AjusteInventarioTB> inventarioTBs = (ObservableList<AjusteInventarioTB>) objects[0];
                if (!inventarioTBs.isEmpty()) {
                    for (AjusteInventarioTB mi : inventarioTBs) {
                        mi.getValidar().setOnAction(event -> {
                            openWindowMovimientoDetalle(mi.getIdMovimientoInventario());
                        });
                        mi.getValidar().setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                openWindowMovimientoDetalle(mi.getIdMovimientoInventario());
                            }
                        });
                    }
                    tvList.setItems(inventarioTBs);
                    
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
        task.setOnFailed((WorkerStateEvent e) -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnScheduled((WorkerStateEvent e) -> {
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

    private void openWindowMovimientoDetalle(String idMovimiento) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = FxMovimientosController.class.getClassLoader().getClass().getResource(FilesRouters.FX_MOVIMIENTOS_DETALLE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxMovimientosDetalleController controller = fXMLLoader.getController();
            controller.setIniciarCarga(idMovimiento);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Detalle del Ajuste", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void openWindowRealizarMovimientoProducto() {
        controllerMovimientoDetalle.setContent(this, fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeMovimientoDetalle, 0d);
        AnchorPane.setTopAnchor(nodeMovimientoDetalle, 0d);
        AnchorPane.setRightAnchor(nodeMovimientoDetalle, 0d);
        AnchorPane.setBottomAnchor(nodeMovimientoDetalle, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeMovimientoDetalle);
    }

    public void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTableMovimiento(0, 0, "", "");
                break;
            case 1:
                fillTableMovimiento(1, cbMovimiento.getSelectionModel().getSelectedItem().getIdTipoMovimiento(), Tools.getDatePicker(dtFechaInicio), Tools.getDatePicker(dtFechaFinal));
                break;
        }
    }

    @FXML
    private void onActionRelizarMovimiento(ActionEvent event) {
        openWindowRealizarMovimientoProducto();

    }

    @FXML
    private void onKeyPressedRealizarMovimiento(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowRealizarMovimientoProducto();
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                loadInitTable();
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            loadInitTable();
            opcion = 0;
        }
    }

    @FXML
    private void onActionTipoMovimiento(ActionEvent event) {
        if (cbMovimiento.getSelectionModel().getSelectedIndex() >= 0) {
            if (dtFechaInicio.getValue() != null && dtFechaFinal.getValue() != null) {
                if (!lblLoad.isVisible()) {
                    paginacion = 1;
                    fillTableMovimiento(1, cbMovimiento.getSelectionModel().getSelectedItem().getIdTipoMovimiento(), Tools.getDatePicker(dtFechaInicio), Tools.getDatePicker(dtFechaFinal));
                    opcion = 1;
                }
            }
        }
    }

    @FXML
    private void onActionFecha(ActionEvent event) {
        if (dtFechaInicio.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillTableMovimiento(1, cbMovimiento.getSelectionModel().getSelectedItem().getIdTipoMovimiento(), Tools.getDatePicker(dtFechaInicio), Tools.getDatePicker(dtFechaFinal));
                opcion = 1;
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

    public void setOpcion(short opcion) {
        this.opcion = opcion;
    }

    public HBox getHbWindow() {
        return hbWindow;
    }

    public TableView<AjusteInventarioTB> getTvList() {
        return tvList;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
