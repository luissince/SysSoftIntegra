package controller.configuracion.impuestos;

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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.DetalleADO;
import model.DetalleTB;
import model.ImpuestoADO;
import model.ImpuestoTB;

public class FxImpuestoProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<DetalleTB> cbOperaciones;
    @FXML
    private TextField txtNombreImpuesto;
    @FXML
    private TextField txtValor;
    @FXML
    private TextField txtCodigoAlterno;
    @FXML
    private TextField txtNumero;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtLetra;
    @FXML
    private TextField txtCategoria;
    @FXML
    private Button btnGuardar;

    private FxImpuestoController impuestoController;

    private int idImpuesto;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idImpuesto = 0;
        DetalleADO.GetDetailIdName("2", "0010", "").forEach(e -> {
            cbOperaciones.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
    }

    public void setUpdateImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
        btnGuardar.setText("Actualizar");
        btnGuardar.getStyleClass().add("buttonLightWarning");
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ImpuestoTB> task = new Task<ImpuestoTB>() {
            @Override
            public ImpuestoTB call() {
                return ImpuestoADO.GetImpuestoById(idImpuesto);
            }
        };

        task.setOnSucceeded(w -> {
            ImpuestoTB impuestoTB = task.getValue();
            if (impuestoTB != null) {

                ObservableList<DetalleTB> lsori = cbOperaciones.getItems();
                if (impuestoTB.getOperacion() != 0) {
                    for (int i = 0; i < lsori.size(); i++) {
                        if (impuestoTB.getOperacion() == lsori.get(i).getIdDetalle()) {
                            cbOperaciones.getSelectionModel().select(i);
                            break;
                        }
                    }
                }

                txtNombreImpuesto.setText(impuestoTB.getNombre());
                txtValor.setText(Tools.roundingValue(impuestoTB.getValor(), 2));
                txtCodigoAlterno.setText(impuestoTB.getCodigo());
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onProccesImpuesto() {
        if (cbOperaciones.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Impuesto", "Ingrese el nombre del impuesto de la operación.");
            cbOperaciones.requestFocus();
        } else if (txtNombreImpuesto.getText().trim().isEmpty()) {
            Tools.AlertMessageWarning(window, "Impuesto", "Ingrese el nombre del impuesto.");
            txtNombreImpuesto.requestFocus();
        } else if (!Tools.isNumeric(txtValor.getText())) {
            Tools.AlertMessageWarning(window, "Impuesto", "Ingrese el valor del impuesto.");
            txtValor.requestFocus();
        } else {
            ImpuestoTB impuestoTB = new ImpuestoTB();
            impuestoTB.setIdImpuesto(idImpuesto);
            impuestoTB.setOperacion(cbOperaciones.getSelectionModel().getSelectedItem().getIdDetalle());
            impuestoTB.setNombre(txtNombreImpuesto.getText().trim());
            impuestoTB.setValor(Double.parseDouble(txtValor.getText()));
            impuestoTB.setCodigo(txtCodigoAlterno.getText().trim());
            impuestoTB.setNumeracion(txtNumero.getText().trim());
            impuestoTB.setNombreImpuesto(txtNombre.getText().trim());
            impuestoTB.setLetra(txtLetra.getText().trim());
            impuestoTB.setCategoria(txtCategoria.getText().trim());
            impuestoTB.setPredeterminado(false);
            impuestoTB.setCodigo(txtCodigoAlterno.getText().isEmpty() ? "0" : txtCodigoAlterno.getText().trim());
            impuestoTB.setSistema(false);
            String result = ImpuestoADO.CrudImpuesto(impuestoTB);
            if (result.equalsIgnoreCase("duplicated")) {
                Tools.AlertMessageWarning(window, "Impuesto", "Hay un impuesto con el mismo nombre.");
                txtNombreImpuesto.requestFocus();
            } else if (result.equalsIgnoreCase("updated")) {
                Tools.AlertMessageInformation(window, "Impuesto", "Se actualizó correctamente.");
                impuestoController.loadInitTable();
                Tools.Dispose(window);
            } else if (result.equalsIgnoreCase("inserted")) {
                Tools.AlertMessageInformation(window, "Impuesto", "Se ingreso correctamente.");
                impuestoController.loadInitTable();
                Tools.Dispose(window);
            } else {
                Tools.AlertMessageError(window, "Impuesto", result);
            }
        }
    }

    @FXML
    private void onKeyTypedValor(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtValor.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onProccesImpuesto();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onProccesImpuesto();
        }
    }

    @FXML
    private void onKeyCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    public void setInitImpuestoController(FxImpuestoController impuestoController) {
        this.impuestoController = impuestoController;
    }

}
