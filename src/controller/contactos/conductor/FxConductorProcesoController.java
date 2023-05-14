package controller.contactos.conductor;

import controller.operaciones.guiaremision.FxGuiaRemisionController;
import controller.tools.ApiPeru;
import controller.tools.Json;
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
import model.ConductorTB;
import model.DetalleTB;
import service.DetalleADO;

import org.json.simple.JSONObject;
import service.ConductorADO;

public class FxConductorProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblTitulo;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtInformacion;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtLicenciaConducir;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtEmail;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnBuscarSunat;
    @FXML
    private Button btnBuscarReniec;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Button btnCancelarProceso;

    private FxGuiaRemisionController guiaRemisionController;

    private ApiPeru apiSunat;

    private String idConductor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        apiSunat = new ApiPeru();
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
                Object listTipoDocumento = DetalleADO.obtenerDetallePorIdMantenimiento("0003");// CARGA TIPO DE DOCUMENTO
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
            cbTipoDocumento.getItems().addAll((ObservableList<DetalleTB>) result);

            if (guiaRemisionController != null) {
                if (guiaRemisionController.getRbPublico().isSelected()) {
                    lblTitulo.setText("Datos del conductor - RUC");
                } else {
                    lblTitulo.setText("Datos del conductor - DNI");
                }
            }

            hbLoadProcesando.setVisible(false);
            cbTipoDocumento.requestFocus();
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void getApiSunat() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                return apiSunat.getUrlSunatApisPeru(txtNumeroDocumento.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtNumeroDocumento.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtNumeroDocumento.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL() == null) {
                    txtNumeroDocumento.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtNumeroDocumento.clear();

                    Tools.AlertMessageWarning(window, "Conductor", "Hubo un problema en cargar el JSON de la API.");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtNumeroDocumento.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtNumeroDocumento.clear();

                        Tools.AlertMessageWarning(window, "Conductor", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtNumeroDocumento.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("ruc") != null) {
                            txtNumeroDocumento.setText(sONObject.get("ruc").toString());
                        }
                        if (sONObject.get("razonSocial") != null) {
                            txtInformacion.setText(sONObject.get("razonSocial").toString());
                        }
                        if (sONObject.get("direccion") != null) {
                            txtDireccion.setText(sONObject.get("direccion").toString());
                        }
                    }
                }
            } else {
                txtNumeroDocumento.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtNumeroDocumento.clear();

                Tools.AlertMessageError(window, "Conductor", result);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void getApiReniec() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                return apiSunat.getUrlReniecApisPeru(txtNumeroDocumento.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtNumeroDocumento.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtNumeroDocumento.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL()
                        == null) {
                    txtNumeroDocumento.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtNumeroDocumento.clear();

                    Tools.AlertMessageWarning(window, "Conductor", "Hubo un problema en cargar el JSON de la API .");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtNumeroDocumento.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtNumeroDocumento.clear();

                        Tools.AlertMessageWarning(window, "Conductor", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtNumeroDocumento.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("dni") != null) {
                            txtNumeroDocumento.setText(sONObject.get("dni").toString());
                        }
                        if (sONObject.get("apellidoPaterno") != null
                                && sONObject.get("apellidoMaterno") != null
                                && sONObject.get("nombres") != null) {
                            txtInformacion.setText(sONObject.get("apellidoPaterno").toString() + " "
                                    + sONObject.get("apellidoMaterno").toString() + " "
                                    + sONObject.get("nombres").toString());
                        }
                    }
                }
            } else {
                txtNumeroDocumento.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtNumeroDocumento.clear();

                Tools.AlertMessageError(window, "Conductor", result);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    @FXML
    private void onEventRegistrar() {
        /**
         *
         */
        if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Conductor", "Seleccione el tipo de documento por favor.");
            cbTipoDocumento.requestFocus();
            return;
        }

        /**
         *
         */
       
        if (guiaRemisionController != null) {
            if (guiaRemisionController.getRbPublico().isSelected()) {
                if (!Tools.isNumericInteger(txtNumeroDocumento.getText())) {
                    Tools.AlertMessageWarning(window, "Conductor", "Ingrese el número de documento del conductor o empresa1.");
                    txtNumeroDocumento.requestFocus();
                    return;
                }

                if (txtNumeroDocumento.getText().trim().length() != 11) {
                    Tools.AlertMessageWarning(window, "Conductor", "El número de documento debe ser de 11 caracteres.");
                    txtNumeroDocumento.requestFocus();
                    return;
                }
            } else {
                if (!Tools.isNumericInteger(txtNumeroDocumento.getText())) {
                    Tools.AlertMessageWarning(window, "Conductor", "Ingrese el número de documento del conductor o empresa2.");
                    txtNumeroDocumento.requestFocus();
                    return;
                }

                if (txtNumeroDocumento.getText().trim().length() != 8) {
                    Tools.AlertMessageWarning(window, "Conductor", "El número de documento debe ser de 8 caracteres.");
                    txtNumeroDocumento.requestFocus();
                    return;
                }
            }
        } else {
            if (!Tools.isNumericInteger(txtNumeroDocumento.getText())) {
                Tools.AlertMessageWarning(window, "Conductor", "Ingrese el número de documento del conductor o empresa.");
                txtNumeroDocumento.requestFocus();
                return;
            }
        }

        /**
         *
         */
        if (Tools.isText(txtInformacion.getText())) {
            Tools.AlertMessageWarning(window, "Conductor", "Ingrese la inforamción del conductor o empresa.");
            txtInformacion.requestFocus();
            return;
        }

        /**
         *
         */
        ConductorTB conductorTB = new ConductorTB();
        conductorTB.setIdConductor(idConductor);
        conductorTB.setIdTipoDocumento(cbTipoDocumento.getSelectionModel().getSelectedItem().getIdDetalle());
        conductorTB.setNumeroDocumento(txtNumeroDocumento.getText().trim());
        conductorTB.setInformacion(txtInformacion.getText().toUpperCase());
        conductorTB.setCelular(txtCelular.getText().trim());
        conductorTB.setTelefono(txtTelefono.getText().trim());
        conductorTB.setEmail(txtEmail.getText().trim());
        conductorTB.setLicenciaConducir(txtLicenciaConducir.getText().trim());
        conductorTB.setDireccion(txtDireccion.getText().trim());
        conductorTB.setIdEmpleado(Session.USER_ID);

        /**
         *
         */
        short confirmation = Tools.AlertMessageConfirmation(window, "Conductor", "¿Esta seguro de continuar?");
        if (confirmation == 1) {
            String result = ConductorADO.CrudConductor(conductorTB);
            switch (result) {
                case "registered":
                    Tools.AlertMessageInformation(window, "Conductor", "Registrado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "updated":
                    Tools.AlertMessageInformation(window, "Conductor", "Actualizado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "duplicate":
                    Tools.AlertMessageWarning(window, "Conductor",
                            "No se puede haber 2 conductores con el mismo documento de identidad.");

                    break;
                default:
                    Tools.AlertMessageError(window, "Conductor", result);
                    break;
            }
        }
    }

    @FXML
    private void onKeyTypedNumeroDocumento(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedSunat(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            getApiSunat();
        }
    }

    @FXML
    private void onActionSunat(ActionEvent event) {
        getApiSunat();
    }

    @FXML
    private void onKeyPressedReniec(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            getApiReniec();
        }
    }

    @FXML
    private void onActionReniec(ActionEvent event) {
        getApiReniec();
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRegistrar();
        }
    }

    private void onEventRegistrar(ActionEvent event) {
        onEventRegistrar();
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

    public Label getLblTitulo() {
        return lblTitulo;
    }

    public void setInitGuiaRemisionController(FxGuiaRemisionController guiaRemisionController) {
        this.guiaRemisionController = guiaRemisionController;
    }

}
