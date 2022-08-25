package controller.contactos.clientes;

import controller.tools.ApiPeru;
import controller.tools.Json;
import controller.tools.Tools;
import java.net.URL;
import java.text.ParseException;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.ClienteADO;
import model.ClienteTB;
import model.DetalleADO;
import model.DetalleTB;
import org.json.simple.JSONObject;

public class FxClienteProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<DetalleTB> cbDocumentType;
    @FXML
    private TextField txtDocumentNumber;
    @FXML
    private TextField txtInformacion;
    @FXML
    private Button btnRegister;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtRepresentante;
    @FXML
    private RadioButton rbActivo;
    @FXML
    private RadioButton rbInactivo;
    @FXML
    private Button btnBuscarSunat;
    @FXML
    private Button btnBuscarReniec;
    @FXML
    private ComboBox<DetalleTB> cmbxTipoDcoumentDriver;
    @FXML
    private TextField txtFieldNdocumentoDriver;
    @FXML
    private TextField txtFieldNombreDriver;
    @FXML
    private TextField TxtFeldCelularDriver;
    @FXML
    private TextField TxtFieldNPlacaCar;
    @FXML
    private TextField TxtFieldMarcaCar;

    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Button btnCancelarProceso;

    private String idCliente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        idCliente = "";
        cbDocumentType.getItems().addAll(DetalleADO.GetDetailId("0003"));
        ToggleGroup group = new ToggleGroup();
        rbActivo.setToggleGroup(group);
        rbInactivo.setToggleGroup(group);

    }

    public void setValueAdd() {
        cbDocumentType.requestFocus();
    }
    public void loadAddCliente(){
         ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

         
//          exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }
    
    public void loadEditCliente(){
         ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

         
//          exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void setValueUpdate(String idCliente) {
//        btnRegister.setText("Actualizar");
//        btnRegister.getStyleClass().add("buttonLightWarning");
//        cbDocumentType.requestFocus();
//        ClienteTB clienteTB = ClienteADO.GetByIdCliente(idCliente);
//        if (clienteTB != null) {
//            this.idCliente = clienteTB.getIdCliente();
//            ObservableList<DetalleTB> lstype = cbDocumentType.getItems();
//            for (int i = 0; i < lstype.size(); i++) {
//                if (clienteTB.getTipoDocumento() == lstype.get(i).getIdDetalle()) {
//                    cbDocumentType.getSelectionModel().select(i);
//                    break;
//                }
//            }
//
//            txtDocumentNumber.setText(clienteTB.getNumeroDocumento());
//            txtInformacion.setText(clienteTB.getInformacion());
//
//            if (clienteTB.getEstado() == 1) {
//                rbActivo.setSelected(true);
//            } else {
//                rbInactivo.setSelected(true);
//            }
//
//            txtTelefono.setText(clienteTB.getTelefono());
//            txtCelular.setText(clienteTB.getCelular());
//            txtEmail.setText(clienteTB.getEmail());
//            txtDireccion.setText(clienteTB.getDireccion());
//        }

    }

    public void aValidityProcess() throws ParseException {
        if (cbDocumentType.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Cliente", "Seleccione el tipo de documento por favor.");
            cbDocumentType.requestFocus();
            return;
        }

        if (txtDocumentNumber.getText().trim().equalsIgnoreCase("")) {
            Tools.AlertMessageWarning(window, "Cliente", "Ingrese el documento de identificación por favor.");
            txtDocumentNumber.requestFocus();
            return;
        }

        if (txtInformacion.getText().trim().equalsIgnoreCase("")) {
            Tools.AlertMessageWarning(window, "Cliente", "Ingrese la información del cliente por favor.");
            txtInformacion.requestFocus();
            return;
        }

        short confirmation = Tools.AlertMessageConfirmation(window, "Cliente", "¿Esta seguro de continuar?");
        if (confirmation == 1) {

            ClienteTB clienteTB = new ClienteTB();
            clienteTB.setIdCliente(idCliente);
            clienteTB.setTipoDocumento(cbDocumentType.getSelectionModel().getSelectedItem().getIdDetalle());
            clienteTB.setInformacion(txtInformacion.getText().trim().toUpperCase());
            clienteTB.setNumeroDocumento(txtDocumentNumber.getText().trim().toUpperCase());
//              clienteTB.setFechaNacimiento(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse(Tools.getDatePicker(dpBirthdate)).getTime()));
            clienteTB.setTelefono(txtTelefono.getText().trim());
            clienteTB.setCelular(txtCelular.getText().trim());
            clienteTB.setEmail(txtEmail.getText().trim());
            clienteTB.setDireccion(txtDireccion.getText().trim().toUpperCase());
            clienteTB.setRepresentante(txtRepresentante.getText().trim().toUpperCase());
            clienteTB.setEstado(rbActivo.isSelected() ? 1 : 0);
            clienteTB.setPredeterminado(false);
            clienteTB.setSistema(false);
            //    clienteTB.setIdTipoDocumentoConducto(cmbxTipoDcoumentDriver.getSelectionModel().getSelectedItem().getIdDetalle());
            clienteTB.setNumeroDocumentoConductor(txtFieldNdocumentoDriver.getText().trim().toUpperCase());
            clienteTB.setNombreConductor(txtFieldNombreDriver.getText().trim());
            clienteTB.setCelularConductor(TxtFeldCelularDriver.getText().trim());
            clienteTB.setPlacaVehiculo(TxtFieldNPlacaCar.getText().trim().toUpperCase());
            clienteTB.setMarcaVehiculo(TxtFieldMarcaCar.getText().trim());

            String result = ClienteADO.CrudCliente(clienteTB);
            switch (result) {
                case "registered":
                    Tools.AlertMessageInformation(window, "Cliente", "Registrado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "updated":
                    Tools.AlertMessageInformation(window, "Cliente", "Actualizado correctamente.");
                    Tools.Dispose(window);
                    break;
                case "duplicate":
                    Tools.AlertMessageWarning(window, "Cliente", "No se puede haber 2 personas con el mismo documento de identidad.");
                    txtDocumentNumber.requestFocus();
                    break;
                case "duplicatename":
                    Tools.AlertMessageWarning(window, "Cliente", "No se puede haber 2 personas con la misma información.");
                    txtInformacion.requestFocus();
                    break;
                default:
                    Tools.AlertMessageError(window, "Cliente", result);
                    break;
            }
        }
    }

    private void getApiSunat() {
        ApiPeru apiSunat = new ApiPeru();

        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                return apiSunat.getUrlSunatApisPeru(txtDocumentNumber.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtDocumentNumber.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtDocumentNumber.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL() == null) {
                    txtDocumentNumber.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtDocumentNumber.clear();

                    Tools.AlertMessageWarning(window, "Cliente", "Hubo un problema en cargar el JSON de la API.");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtDocumentNumber.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtDocumentNumber.clear();

                        Tools.AlertMessageWarning(window, "Cliente", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtDocumentNumber.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("ruc") != null) {
                            txtDocumentNumber.setText(sONObject.get("ruc").toString());
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
                txtDocumentNumber.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtDocumentNumber.clear();

                Tools.AlertMessageError(window, "Cliente", result);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void getApiReniec() {
        ApiPeru apiSunat = new ApiPeru();

        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                return apiSunat.getUrlReniecApisPeru(txtDocumentNumber.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtDocumentNumber.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtDocumentNumber.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL() == null) {
                    txtDocumentNumber.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtDocumentNumber.clear();

                    Tools.AlertMessageWarning(window, "Cliente", "Hubo un problema en cargar el JSON de la API.");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtDocumentNumber.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtDocumentNumber.clear();

                        Tools.AlertMessageWarning(window, "Cliente", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtDocumentNumber.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("dni") != null) {
                            txtDocumentNumber.setText(sONObject.get("dni").toString());
                        }
                        if (sONObject.get("apellidoPaterno") != null && sONObject.get("apellidoMaterno") != null && sONObject.get("nombres") != null) {
                            txtInformacion.setText(sONObject.get("apellidoPaterno").toString() + " " + sONObject.get("apellidoMaterno").toString() + " " + sONObject.get("nombres").toString());
                        }
                    }
                }
            } else {
                txtDocumentNumber.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtDocumentNumber.clear();

                Tools.AlertMessageError(window, "Cliente", result);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    @FXML
    private void onKeyTypedNumeroDocumentoConductor(KeyEvent event) {
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
    private void onKeyPressedToRegister(KeyEvent event) throws ParseException {
        if (event.getCode() == KeyCode.ENTER) {
            aValidityProcess();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) throws ParseException {
        aValidityProcess();
    }

    @FXML
    private void onKeyPressedToCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionToCancel(ActionEvent event) {
        Tools.Dispose(window);
    }

}
