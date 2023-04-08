package controller.operaciones.venta;

import controller.inventario.traslados.FxTrasladoGuiaController;
import controller.operaciones.guiaremision.FxGuiaRemisionController;
import controller.operaciones.notacredito.FxNotaCreditoController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import model.VentaTB;
import service.VentaADO;

public class FxVentaListaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtBuscar;
    @FXML
    private DatePicker txtFechaInicio;
    @FXML
    private DatePicker txtFechaFinal;
    @FXML
    private TableView<VentaTB> tvList;
    @FXML
    private TableColumn<VentaTB, String> tcNumero;
    @FXML
    private TableColumn<VentaTB, String> tcFecha;
    @FXML
    private TableColumn<VentaTB, String> tcComprobante;
    @FXML
    private TableColumn<VentaTB, String> tcCliente;
    @FXML
    private TableColumn<VentaTB, Label> tcEstado;
    @FXML
    private TableColumn<VentaTB, String> tcTotal;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxGuiaRemisionController guiaRemisionController;

    private FxNotaCreditoController notaCreditoController;

    private FxTrasladoGuiaController trasladoGuiaController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings
                .concat(cellData.getValue().getFechaVenta() + "\n" + cellData.getValue().getHoraVenta()));
        tcComprobante
                .setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoDocumentoTB().getNombre()
                        + "\n" + cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion()));
        tcCliente
                .setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento()
                        + "\n" + cellData.getValue().getClienteTB().getInformacion()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " "
                + Tools.roundingValue(cellData.getValue().getTotal(), 2)));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), txtFechaInicio);
        Tools.actualDate(Tools.getDate(), txtFechaFinal);

        paginacion = 1;
        opcion = 0;
    }

    public void loadInit() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillVentasTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal), 0);
            opcion = 1;
        }
    }

    private void fillVentasTable(int opcion, String value, String fechaInicial, String fechaFinal, int estado) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return VentaADO.Listar_Ventas_All(opcion, value, fechaInicial, fechaFinal, estado,
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
            case 1:
                fillVentasTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal), 0);
                break;
            case 2:
                fillVentasTable(2, txtBuscar.getText().trim(), "", "", 0);
                break;
        }
    }

    private void onEventAceptar() {
        if (guiaRemisionController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                guiaRemisionController.loadVentaById(tvList.getSelectionModel().getSelectedItem().getIdVenta());
                Tools.Dispose(apWindow);
            }
        } else if (notaCreditoController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                notaCreditoController.loadComponents(tvList.getSelectionModel().getSelectedItem().getSerie() + "-"
                        + tvList.getSelectionModel().getSelectedItem().getNumeracion());
                Tools.Dispose(apWindow);
            }
        } else if (trasladoGuiaController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                trasladoGuiaController.loadDataVenta(
                        tvList.getSelectionModel().getSelectedItem().getIdVenta(),
                        tvList.getSelectionModel().getSelectedItem().getSerie() + "-"
                                + tvList.getSelectionModel().getSelectedItem().getNumeracion());
                Tools.Dispose(apWindow);
            }
        }

    }

    @FXML
    private void onKeyPressedBuscar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void onKeyReleasedBuscar(KeyEvent event) {
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
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable(2, txtBuscar.getText().trim(), "", "", 0);
                opcion = 2;
            }
        }
    }

    @FXML
    private void onActionFechaInicio(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal), 0);
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable(1, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal), 0);
                opcion = 1;
            }
        }
    }

    @FXML
    private void onkeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInit();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        loadInit();
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            onEventAceptar();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(apWindow);
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

    public TextField getTxtBuscar() {
        return txtBuscar;
    }

    public void setInitGuiaRemisionController(FxGuiaRemisionController guiaRemisionController) {
        this.guiaRemisionController = guiaRemisionController;
    }

    public void setInitNotaCreditoController(FxNotaCreditoController notaCreditoController) {
        this.notaCreditoController = notaCreditoController;
    }

    public void setInitTrasladoGuia(FxTrasladoGuiaController trasladoGuiaController) {
        this.trasladoGuiaController = trasladoGuiaController;
    }

}
