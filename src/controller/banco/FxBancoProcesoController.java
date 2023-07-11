package controller.banco;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.BancoTB;
import model.MonedaTB;
import service.BancoADO;
import service.MonedaADO;

public class FxBancoProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtNombreCuenta;
    @FXML
    private TextField txtNumeroCuenta;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private Button btnProceso;
    @FXML
    private RadioButton CuentaEfectivo;
    @FXML
    private RadioButton CuentaBancaria;
    @FXML
    private CheckBox cbMostrar;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private VBox vbContent;

    private FxBancoController bancosController;

    private String idBanco;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        ToggleGroup group = new ToggleGroup();
        CuentaEfectivo.setToggleGroup(group);
        CuentaBancaria.setToggleGroup(group);

        idBanco = "";
    }

    public void loadAddBanco() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                Object listMonedas = MonedaADO.ObtenerListaMonedas();

                if (listMonedas instanceof ObservableList) {
                    return new Object[] { listMonedas };
                }
                throw new Exception("Se produjo un error, intente nuevamente.");
            }
        };

        task.setOnScheduled(e -> {
            vbContent.setDisable(true);
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
                Tools.Dispose(apWindow);
            });
        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            Object[] result = task.getValue();
            ObservableList<MonedaTB> monedaTBs = (ObservableList<MonedaTB>) result[0];
            cbMoneda.setItems(monedaTBs);

            hbLoadProcesando.setVisible(false);
            vbContent.setDisable(false);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void loadEditBanco(String idBanco) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                BancoTB bancoTB = BancoADO.obtenerBancoPorId(idBanco);
                Object listMonedas = MonedaADO.ObtenerListaMonedas();

                if (listMonedas instanceof ObservableList) {
                    return new Object[] { bancoTB, listMonedas };
                }
                throw new Exception("Se produjo un error, intente nuevamente.");
            }
        };

        task.setOnScheduled(e -> {
            this.idBanco = idBanco;
            lblTitle.setText("Editar cuenta");
            btnProceso.setText("Editar");
            btnProceso.getStyleClass().add("buttonLightWarning");

            vbContent.setDisable(true);
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
                Tools.Dispose(apWindow);
            });
        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            Object[] result = task.getValue();
            BancoTB bancoTB = (BancoTB) result[0];
            ObservableList<MonedaTB> monedaTBs = (ObservableList<MonedaTB>) result[1];

            cbMoneda.setItems(monedaTBs);

            txtNombreCuenta.setText(bancoTB.getNombreCuenta());
            txtNumeroCuenta.setText(bancoTB.getNumeroCuenta());
            for (MonedaTB moneda : cbMoneda.getItems()) {
                if (moneda.getIdMoneda() == bancoTB.getIdMoneda()) {
                    cbMoneda.getSelectionModel().select(moneda);
                    break;
                }
            }
            if (bancoTB.getFormaPago() == 1) {
                CuentaEfectivo.setSelected(true);
            } else {
                CuentaBancaria.setSelected(true);
            }
            txtDescripcion.setText(bancoTB.getDescripcion());
            cbMostrar.setSelected(bancoTB.isMostrar());

            hbLoadProcesando.setVisible(false);
            vbContent.setDisable(false);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void eventGuardar() {
        if (Tools.isText(txtNombreCuenta.getText())) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Ingrese el nombre de la cuenta.");
            txtNombreCuenta.requestFocus();
            return;
        }
        if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Seleccione un moneda.");
            cbMoneda.requestFocus();
            return;
        }

        BancoTB bancoTB = new BancoTB();
        bancoTB.setIdBanco(idBanco);
        bancoTB.setNombreCuenta(txtNombreCuenta.getText());
        bancoTB.setNumeroCuenta(txtNumeroCuenta.getText());
        bancoTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda());
        bancoTB.setSaldoInicial(0);
        bancoTB.setFecha(Tools.getDate());
        bancoTB.setHora(Tools.getTime());
        bancoTB.setDescripcion(txtDescripcion.getText().trim());
        bancoTB.setFormaPago(CuentaEfectivo.isSelected() ? (short) 1 : (short) 2);
        bancoTB.setMostrar(cbMostrar.isSelected());
        short option = Tools.AlertMessageConfirmation(apWindow, "Banco", "¿Está seguro de continuar?");
        if (option == 1) {
            if (idBanco == "") {
                onEventAgregar(bancoTB);
            } else {
                onEventEditar(bancoTB);
            }
        }
    }

    private void onEventAgregar(BancoTB bancoTB) {
        String result = BancoADO.registrarBanco(bancoTB);
        if (result.equalsIgnoreCase("duplicate")) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Existe una cuenta con el mismo nombre.");
            txtNombreCuenta.requestFocus();
        } else if (result.equalsIgnoreCase("inserted")) {
            Tools.AlertMessageInformation(apWindow, "Banco", "Se registro correctamente la cuenta.");
            bancosController.loadTableViewBanco("");
            Tools.Dispose(apWindow);
        } else {
            Tools.AlertMessageError(apWindow, "Banco", result);
        }
    }

    private void onEventEditar(BancoTB bancoTB) {
        String result = BancoADO.actualizarBanco(bancoTB);
        if (result.equalsIgnoreCase("duplicate")) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Existe una cuenta con el mismo nombre.");
            txtNombreCuenta.requestFocus();
        }else if(result.equalsIgnoreCase("noid")){
            Tools.AlertMessageWarning(apWindow, "Banco", "El id envíado no existe.");
            txtNombreCuenta.requestFocus();
        } 
        else if (result.equalsIgnoreCase("updated")) {
            Tools.AlertMessageInformation(apWindow, "Banco", "Se actualizo correctamente la cuenta.");
            bancosController.loadTableViewBanco("");
            Tools.Dispose(apWindow);
        } else {
            Tools.AlertMessageError(apWindow, "Banco", result);
        }
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        eventGuardar();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitBancosController(FxBancoController bancosController) {
        this.bancosController = bancosController;
    }

}
