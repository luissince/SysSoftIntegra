package controller.operaciones.cortecaja;

import controller.tools.Tools;
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
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.CajaADO;
import model.CajaTB;

public class FxCajaBusquedaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dcFechaFinal;
    @FXML
    private TableView<CajaTB> tvLista;
    @FXML
    private TableColumn<CajaTB, String> tcFechaApertura;
    @FXML
    private TableColumn<CajaTB, String> tcFechaCierre;
    @FXML
    private TableColumn<CajaTB, Label> tcEstado;
    @FXML
    private TableColumn<CajaTB, String> tcContado;
    @FXML
    private TableColumn<CajaTB, String> tcCalculado;
    @FXML
    private TableColumn<CajaTB, String> tcDiferencia;
    @FXML
    private TableColumn<CajaTB, String> tcUsuario;

    private FxCajaConsultasController cajaConsultasController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dcFechaFinal);
        tcFechaApertura.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaApertura() + "\n"
                + cellData.getValue().getHoraApertura()
        ));
        tcFechaCierre.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaCierre() + "\n"
                + cellData.getValue().getHoraCierre()
        ));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("labelEstado"));
        tcContado.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getContado(), 2)));
        tcCalculado.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCalculado(), 2)));
        tcDiferencia.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getDiferencia(), 2)));
        tcUsuario.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEmpleadoTB().getApellidos() + "\n" + cellData.getValue().getEmpleadoTB().getNombres()));
        if (dtFechaInicial.getValue() != null && dcFechaFinal.getValue() != null) {
            fillTableCajas(Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dcFechaFinal));
        }
    }

    private void acceptBusqueda() {
        if (tvLista.getSelectionModel().getSelectedIndex() >= 0) {
            cajaConsultasController.loadDataCorteCaja(tvLista.getSelectionModel().getSelectedItem().getIdCaja());
            Tools.Dispose(window);
        }
    }

    public void fillTableCajas(String fechaInicial, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CajaADO.ListarCajasAperturadas(fechaInicial, fechaFinal);
            }
        };
        task.setOnSucceeded((WorkerStateEvent e) -> {
            Object object = task.getValue();
            if(object instanceof ObservableList){               
                 tvLista.setItems(( ObservableList<CajaTB>)object);
            }           
            lblLoad.setVisible(false);
        });
        task.setOnFailed((WorkerStateEvent e) -> {
            lblLoad.setVisible(false);
        });
        task.setOnScheduled((WorkerStateEvent e) -> {
            lblLoad.setVisible(true);
            tvLista.getItems().clear();
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void reloadBusqueda() {
        if (!lblLoad.isVisible()) {
            Tools.actualDate(Tools.getDate(), dtFechaInicial);
            Tools.actualDate(Tools.getDate(), dcFechaFinal);
            if (dtFechaInicial.getValue() != null && dcFechaFinal.getValue() != null) {
                fillTableCajas(Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dcFechaFinal));
            }
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            acceptBusqueda();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        acceptBusqueda();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    @FXML
    private void onKeyPresedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            reloadBusqueda();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        reloadBusqueda();
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dcFechaFinal.getValue() != null) {
                fillTableCajas(Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dcFechaFinal));
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dcFechaFinal.getValue() != null) {
                fillTableCajas(Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dcFechaFinal));
            }
        }
    }

    @FXML
    private void onMouseClickedLista(MouseEvent event) {
        if (event.getClickCount() == 2) {
            acceptBusqueda();
        }
    }

    public void setInitCajaConsultasController(FxCajaConsultasController cajaConsultasController) {
        this.cajaConsultasController = cajaConsultasController;
    }

}
