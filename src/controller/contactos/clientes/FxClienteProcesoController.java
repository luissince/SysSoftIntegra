package controller.contactos.clientes;

import controller.tools.ApiPeru;
import controller.tools.Json;
import controller.tools.SearchComboBox;
import controller.tools.Tools;
import java.net.URL;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.ClienteTB;
import model.DetalleTB;
import model.UbigeoTB;
import service.ClienteADO;
import service.DetalleADO;
import service.UbigeoADO;

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
    private ComboBox<UbigeoTB> cbUbigeo;
    @FXML
    private RadioButton rbActivo;
    @FXML
    private RadioButton rbInactivo;
    @FXML
    private Button btnBuscarSunat;
    @FXML
    private Button btnBuscarReniec;
    @FXML
    private ComboBox<DetalleTB> cbMotivoTraslado;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Button btnCancelarProceso;

    private String idCliente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        idCliente = "";
        ToggleGroup group = new ToggleGroup();
        rbActivo.setToggleGroup(group);
        rbInactivo.setToggleGroup(group);
        loadUbigeo();

    }

    public void loadAddCliente() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws Exception {
                Object listTipoDocumento = DetalleADO.obtenerDetallePorIdMantenimiento("0003");
                Object listMotivoTraslado = DetalleADO.obtenerDetallePorIdMantenimiento("0017");

                if (listTipoDocumento instanceof ObservableList
                        && listMotivoTraslado instanceof ObservableList) {
                    // && listUbigeo instanceof ObservableList) {
                    return new Object[] { listTipoDocumento, listMotivoTraslado };// return
                    // (ObservableList<DetalleTB>)
                    // listTicket;
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
            Object[] result = (Object[]) task.getValue();
            ObservableList<DetalleTB> ticketTBs = (ObservableList<DetalleTB>) result[0];
            cbDocumentType.getItems().addAll(ticketTBs);

            ObservableList<DetalleTB> motivoTraslado = (ObservableList<DetalleTB>) result[1];
            cbMotivoTraslado.getItems().addAll(motivoTraslado);

            hbLoadProcesando.setVisible(false);
            cbDocumentType.requestFocus();
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void loadUbigeo() {
        SearchComboBox<UbigeoTB> searchComboBoxUbigeoLlegada = new SearchComboBox<>(cbUbigeo, false);
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxUbigeoLlegada.getComboBox().hide();
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxUbigeoLlegada.getComboBox().getItems().clear();
            searchComboBoxUbigeoLlegada.getComboBox().getItems().addAll(UbigeoADO.GetSearchComboBoxUbigeo(
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().getText().trim()));
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxUbigeoLlegada.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxUbigeoLlegada.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxUbigeoLlegada.getComboBox().hide();
                        }
                    }
                });
    }

    public void loadEditCliente(String idCliente) {
        this.idCliente = idCliente;
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws Exception {
                Object listTipoDocumento = DetalleADO.obtenerDetallePorIdMantenimiento("0003");// se establece las diferente opciones
                Object listMotivoTraslado = DetalleADO.obtenerDetallePorIdMantenimiento("0017");
                Object cliente = ClienteADO.GetByIdCliente(idCliente);
                if (listTipoDocumento instanceof ObservableList
                        && listMotivoTraslado instanceof ObservableListBase
                        && cliente instanceof ClienteTB) {
                    return new Object[] { listTipoDocumento, listMotivoTraslado, cliente };
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
            Object[] result = (Object[]) task.getValue();

            ObservableList<DetalleTB> tipoDocumento = (ObservableList<DetalleTB>) result[0];
            cbDocumentType.getItems().addAll(tipoDocumento);

            ObservableList<DetalleTB> motivoTraslado = (ObservableList<DetalleTB>) result[1];
            cbMotivoTraslado.getItems().addAll(motivoTraslado);

            ClienteTB clienteTB = (ClienteTB) result[2];

            ObservableList<DetalleTB> lstype = cbDocumentType.getItems();
            for (int i = 0; i < lstype.size(); i++) {
                if (clienteTB.getTipoDocumento() == lstype.get(i).getIdDetalle()) {
                    cbDocumentType.getSelectionModel().select(i);
                    break;
                }
            }

            ObservableList<DetalleTB> listMotivoTraslado = cbMotivoTraslado.getItems();
            for (int i = 0; i < listMotivoTraslado.size(); i++) {
                if (clienteTB.getIdMotivoTraslado() == listMotivoTraslado.get(i).getIdDetalle()) {
                    cbMotivoTraslado.getSelectionModel().select(i);
                    break;
                }
            }

            cbUbigeo.getItems().clear();
            if (clienteTB.getUbigeoTB().getIdUbigeo() > 0) {
                cbUbigeo.getItems().add(clienteTB.getUbigeoTB());
                if (!cbUbigeo.getItems().isEmpty()) {
                    cbUbigeo.getSelectionModel().select(0);
                }
            }

            txtDocumentNumber.setText(clienteTB.getNumeroDocumento());
            txtInformacion.setText(clienteTB.getInformacion());
            txtTelefono.setText(clienteTB.getTelefono());
            txtEmail.setText(clienteTB.getEmail());
            txtDireccion.setText(clienteTB.getDireccion());
            txtRepresentante.setText(clienteTB.getRepresentante());
            btnRegister.setText("Actualizar");
            btnRegister.getStyleClass().add("buttonLightWarning");
            hbLoadProcesando.setVisible(false);
            cbDocumentType.requestFocus();
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void onEventRegistrar() {
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
            // clienteTB.setFechaNacimiento(new java.sql.Date(new
            // SimpleDateFormat("yyyy-MM-dd").parse(Tools.getDatePicker(dpBirthdate)).getTime()));
            clienteTB.setTelefono(txtTelefono.getText());
            clienteTB.setCelular(txtCelular.getText());
            clienteTB.setEmail(txtEmail.getText());
            clienteTB.setDireccion(txtDireccion.getText());
            clienteTB.setRepresentante(Tools.text(txtRepresentante.getText()));
            clienteTB.setEstado(rbActivo.isSelected() ? 1 : 0);
            clienteTB.setPredeterminado(false);
            clienteTB.setSistema(false);
            clienteTB.setIdMotivoTraslado(cbMotivoTraslado.getSelectionModel().getSelectedIndex() >= 0
                    ? cbMotivoTraslado.getSelectionModel().getSelectedItem().getIdDetalle()
                    : 0);

            clienteTB.setIdUbigeo(cbUbigeo.getSelectionModel().getSelectedIndex() >= 0
                    ? cbUbigeo.getSelectionModel().getSelectedItem().getIdUbigeo()
                    : 0);

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
                    Tools.AlertMessageWarning(window, "Cliente",
                            "No se puede haber 2 clientes con el mismo documento de identidad.");
                    txtDocumentNumber.requestFocus();
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
                        if (sONObject.get("apellidoPaterno") != null && sONObject.get("apellidoMaterno") != null
                                && sONObject.get("nombres") != null) {
                            txtInformacion.setText(sONObject.get("apellidoPaterno").toString() + " "
                                    + sONObject.get("apellidoMaterno").toString() + " "
                                    + sONObject.get("nombres").toString());
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
    private void onKeyPressedToRegister(KeyEvent event) throws ParseException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRegistrar();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) throws ParseException {
        onEventRegistrar();
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
