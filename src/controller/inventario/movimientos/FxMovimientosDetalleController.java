package controller.inventario.movimientos;

import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.AjusteInventarioADO;
import model.MovimientoInventarioDetalleTB;
import model.AjusteInventarioTB;

public class FxMovimientosDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Label lblTIpoMovimiento;
    @FXML
    private Label lblHoraFecha;
    @FXML
    private Label lblObservacion;
    @FXML
    private Label lblEstado;
    @FXML
    private TableView<MovimientoInventarioDetalleTB> tvList;
    @FXML
    private TableColumn<MovimientoInventarioDetalleTB, Integer> tcNumero;
    @FXML
    private TableColumn<MovimientoInventarioDetalleTB, String> tcDescripcion;
    @FXML
    private TableColumn<MovimientoInventarioDetalleTB, String> tcCantidad;
    @FXML
    private TableColumn<MovimientoInventarioDetalleTB, CheckBox> tcVerificar;
    @FXML
    private TableColumn<MovimientoInventarioDetalleTB, CheckBox> tcPrecio;
    @FXML
    private Button btnRegistrar;
    @FXML
    private TextField txtCofigoVerificacion;

    private String idMovimientoInventario;

    private AjusteInventarioTB inventarioTB;

    private ObservableList<MovimientoInventarioDetalleTB> detalleTBs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);

        tcNumero.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()
        ));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcVerificar.setCellValueFactory(new PropertyValueFactory<>("verificar"));
        tcPrecio.setCellValueFactory(new PropertyValueFactory<>("actualizarPrecio"));
    }

    public void setIniciarCarga(String value) {
        idMovimientoInventario = value;
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            public ArrayList<Object> call() {
                return AjusteInventarioADO.Obtener_Movimiento_Inventario_By_Id(value);
            }
        };

        task.setOnScheduled((e) -> {
            lblLoad.setVisible(true);
        });

        task.setOnFailed((e) -> {
            lblLoad.setVisible(false);
        });

        task.setOnSucceeded((e) -> {
            ArrayList<Object> objects = task.getValue();
            if (!objects.isEmpty()) {

                inventarioTB = (AjusteInventarioTB) objects.get(0);
                detalleTBs = (ObservableList<MovimientoInventarioDetalleTB>) objects.get(1);

                if (inventarioTB != null) {
                    lblTIpoMovimiento.setText(inventarioTB.getTipoMovimientoName());
                    lblHoraFecha.setText(inventarioTB.getFecha() + " " + inventarioTB.getHora());
                    lblObservacion.setText(inventarioTB.getObservacion());             
                    lblEstado.setText(inventarioTB.getEstadoName());
                    if (inventarioTB.getEstadoName().equalsIgnoreCase("COMPLETADO")) {
                        btnRegistrar.setDisable(true);
                        lblEstado.getStyleClass().add("label-asignacion");
                    } else if (inventarioTB.getEstadoName().equalsIgnoreCase("EN PROCESO")) {
                        lblEstado.getStyleClass().add("label-medio");
                    } else if (inventarioTB.getEstadoName().equalsIgnoreCase("CANCELADO")) {
                        btnRegistrar.setDisable(true);
                        lblEstado.getStyleClass().add("label-proceso");
                    } else {
                        lblEstado.getStyleClass().add("label-asignacion");
                    }
                }
                tvList.setItems(detalleTBs);
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void executeRegistrar() {
        if (inventarioTB != null && detalleTBs != null && !detalleTBs.isEmpty()) {
            inventarioTB.setIdMovimientoInventario(idMovimientoInventario);
            inventarioTB.setCodigoVerificacion(txtCofigoVerificacion.getText().trim());
            short option = Tools.AlertMessageConfirmation(apWindow, "Movimiento", "¿Esta seguro de continuar?");
            if (option == 1) {
                btnRegistrar.setDisable(true);
                String result = AjusteInventarioADO.RegistrarMovimientoSuministro(inventarioTB, detalleTBs);
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(apWindow, "Movimiento", "Se registraron los cambios correctamente.");
                    Tools.Dispose(apWindow);
                } else if (result.equalsIgnoreCase("inserted")) {
                    Tools.AlertMessageWarning(apWindow, "Movimiento", "El movimiento ya está con estado 1 no se puede relizar el proceso.");
                    btnRegistrar.setDisable(false);
                } else if (result.equalsIgnoreCase("nocde")) {
                    Tools.AlertMessageWarning(apWindow, "Movimiento", "No se pudo completar la operación por error de código de verificación.");
                    btnRegistrar.setDisable(false);
                } else {
                    Tools.AlertMessageError(apWindow, "Movimiento", result);
                    btnRegistrar.setDisable(false);
                }
            }
        } else {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "No se puede continuar por problemas al cargar la información intente nuevamente.");
        }
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeRegistrar();
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) {
        executeRegistrar();
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {

    }

}
