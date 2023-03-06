package controller.contactos.vehiculo;

import controller.operaciones.guiaremision.FxGuiaRemisionController;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.DetalleTB;
import model.VehiculoTB;
import service.DetalleADO;
import service.VehiculoADO;

public class FxVehiculoProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtMarca;
    @FXML
    private TextField txtNumeroPlaca;
    @FXML
    private ComboBox<DetalleTB> cbEntidadEmisora;
    @FXML
    private TextField txtNumeroAutorizacion;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Button btnCancelarProceso;

    private FxGuiaRemisionController guiaRemisionController;

    private String idConductor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        idConductor = "";
    }

    public void loadAddConductor() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws Exception {
                Object listTipoDocumento = DetalleADO.GetDetailId("0018");// ENTIDAD EMISORA PARA VEHÍCULOS PERÚ
                if (listTipoDocumento instanceof ObservableList) {
                    return listTipoDocumento;
                } else {
                    throw new Exception("Se produjo un error, intente nuevamente.");
                }
            }
        };

        task.setOnScheduled(e -> {
            hbLoadProcesando.setVisible(true);
            lblTextoProceso.setText("PROCESANDO INFORMACIÓN...");
            btnCancelarProceso.setText("Cancelar Proceso");
            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }
            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                Tools.Dispose(window);
            });
        });

        task.setOnCancelled(e -> {

        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            Object result = (Object) task.getValue();
            cbEntidadEmisora.getItems().addAll((ObservableList<DetalleTB>) result);
            hbLoadProcesando.setVisible(false);
            txtMarca.requestFocus();
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventRegistrar() {
        if (Tools.isText(txtMarca.getText())) {
            Tools.AlertMessageWarning(window, "Vehículo", "Ingrese la marca o nombre del vehículo.");
            txtMarca.requestFocus();
            return;
        }

        if (Tools.isText(txtNumeroPlaca.getText())) {
            Tools.AlertMessageWarning(window, "Vehículo", "Ingrese el número de placa del vehículo.");
            txtNumeroPlaca.requestFocus();
            return;
        }

        VehiculoTB vehiculoTB = new VehiculoTB();
        vehiculoTB.setIdVehiculo(idConductor);
        vehiculoTB.setMarca(txtMarca.getText().trim().toUpperCase());
        vehiculoTB.setNumeroPlaca(txtNumeroPlaca.getText().trim().toUpperCase());
        vehiculoTB.setIdEntidadEmisora(cbEntidadEmisora.getSelectionModel().getSelectedIndex() >= 0
                ? cbEntidadEmisora.getSelectionModel().getSelectedItem().getIdDetalle() : 0
        );
        vehiculoTB.setNumeroAutorizacion(txtNumeroAutorizacion.getText().trim());
        vehiculoTB.setIdEmpleado(Session.USER_ID);

        short confirmation = Tools.AlertMessageConfirmation(window, "Vehículo", "¿Esta seguro de continuar?");
        if (confirmation == 1) {
            String result = VehiculoADO.CrudVehiculo(vehiculoTB);
            switch (result) {
                case "registered":
                    Tools.AlertMessageInformation(window, "Vehículo", "Registrado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "updated":
                    Tools.AlertMessageInformation(window, "Vehículo", "Actualizado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "duplicate":
                    Tools.AlertMessageWarning(window, "Vehículo",
                            "No se puede haber 2 vehículos con el mismo número de placa.");
                    break;
                default:
                    Tools.AlertMessageError(window, "Vehículo", result);
                    break;
            }
        }
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRegistrar();
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) {
        onEventRegistrar();
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    public void setInitGuiaRemisionController(FxGuiaRemisionController guiaRemisionController) {
        this.guiaRemisionController = guiaRemisionController;
    }

}
