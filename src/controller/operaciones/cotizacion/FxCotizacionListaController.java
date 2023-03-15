package controller.operaciones.cotizacion;

import controller.operaciones.venta.FxVentaEstructuraController;
import controller.posterminal.venta.FxPostVentaEstructuraController;
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
import model.CotizacionTB;
import service.CotizacionADO;

public class FxCotizacionListaController implements Initializable {

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
    private TableView<CotizacionTB> tvList;
    @FXML
    private TableColumn<CotizacionTB, String> tcNumero;
    @FXML
    private TableColumn<CotizacionTB, String> tcFechaRegistro;
    @FXML
    private TableColumn<CotizacionTB, String> tcCliente;
    @FXML
    private TableColumn<CotizacionTB, String> tcCotizacion;
    @FXML
    private TableColumn<CotizacionTB, Label> tcEstado;
    @FXML
    private TableColumn<CotizacionTB, String> tcTotal;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxVentaEstructuraController ventaEstructuraController;

    private FxPostVentaEstructuraController postVentaEstructuraController;

    private FxCotizacionController cotizacionController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcCotizacion.setCellValueFactory(cellData -> Bindings.concat("COTIZACIÓN\nN° - " + Tools.formatNumber(cellData.getValue().getIdCotizacion())));
        tcFechaRegistro.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaCotizacion() + "\n" + cellData.getValue().getHoraCotizacion()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento() + "\n" + cellData.getValue().getClienteTB().getInformacion()));
//        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservaciones()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("lblEstado"));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getTotal(), 2)));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcCotizacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));        
        tcFechaRegistro.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.30));        
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), txtFechaInicio);
        Tools.actualDate(Tools.getDate(), txtFechaFinal);
    }

    public void initLoad() {
        if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 0;
            }
        }
    }

    private void fillTableCotizacion(int opcion, String buscar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CotizacionADO.Listar_Cotizacion(opcion, buscar, fechaInicio, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<CotizacionTB> cotizacionTBs = (ObservableList<CotizacionTB>) objects[0];
                if (!cotizacionTBs.isEmpty()) {
                    tvList.setItems(cotizacionTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 10.00));
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

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTableCotizacion(0, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                break;
            case 1:
                fillTableCotizacion(1, txtBuscar.getText().trim(), "", "");
                break;
        }
    }

    private void onEventAceptar() {
        if (ventaEstructuraController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                ventaEstructuraController.resetVenta();
                Tools.Dispose(apWindow);
                ventaEstructuraController.loadCotizacion(tvList.getSelectionModel().getSelectedItem().getIdCotizacion());
            }
        } else if (postVentaEstructuraController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                postVentaEstructuraController.resetVenta();
                Tools.Dispose(apWindow);
                postVentaEstructuraController.loadCotizacion(tvList.getSelectionModel().getSelectedItem().getIdCotizacion());
            }
        } else if (cotizacionController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                cotizacionController.resetVenta();
                Tools.Dispose(apWindow);
                cotizacionController.loadCotizacion(tvList.getSelectionModel().getSelectedItem().getIdCotizacion());
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
        if (!lblLoad.isVisible()) {
            if (!txtBuscar.getText().isEmpty()) {
                paginacion = 1;
                fillTableCotizacion(1, txtBuscar.getText().trim(), "", "");
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaInicio(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (txtFechaInicio.getValue() != null && txtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(txtFechaInicio), Tools.getDatePicker(txtFechaFinal));
                opcion = 0;
            }
        }
    }

    @FXML
    private void onkeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                initLoad();
            }
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            initLoad();
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                onEventAceptar();
            }
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (!tvList.getItems().isEmpty()) {
            if (event.getClickCount() == 2) {
                onEventAceptar();
            }
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

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

    public void setInitPostVentaEstructuraController(FxPostVentaEstructuraController postVentaEstructuraController) {
        this.postVentaEstructuraController = postVentaEstructuraController;
    }

    public void setInitCotizacionListaController(FxCotizacionController cotizacionController) {
        this.cotizacionController = cotizacionController;
    }

}
