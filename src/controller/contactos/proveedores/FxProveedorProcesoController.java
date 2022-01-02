package controller.contactos.proveedores;

import controller.tools.ApiPeru;
import controller.tools.Json;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.DetalleADO;
import model.DetalleTB;
import model.ProveedorADO;
import model.ProveedorTB;
import org.json.simple.JSONObject;

public class FxProveedorProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<DetalleTB> cbDocumentTypeFactura;
    @FXML
    private Button btnRegister;
    @FXML
    private TextField txtBusinessName;
    @FXML
    private TextField txtTradename;
    @FXML
    private TextField txtDocumentNumberFactura;
    @FXML
    private ComboBox<DetalleTB> cbAmbito;
    @FXML
    private ComboBox<DetalleTB> cbEstado;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPaginaWeb;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtRepresentante;
    @FXML
    private Tab tab1;
    @FXML
    private Tab tab2;
    @FXML
    private Button btnBuscarSunat;
    @FXML
    private Button btnBuscarReniec;

    private String idProveedor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        idProveedor = "";
        DetalleADO.GetDetailIdName("2", "0003", "").forEach(e -> {
            cbDocumentTypeFactura.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
        DetalleADO.GetDetailIdName("2", "0005", "").forEach(e -> {
            cbAmbito.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
        DetalleADO.GetDetailIdName("2", "0001", "").forEach(e -> {
            cbEstado.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
        cbEstado.getSelectionModel().select(0);

        tab2.setText("");

    }

    public void setValueAdd(String... value) {
        cbDocumentTypeFactura.requestFocus();
    }

    public void setValueUpdate(String idProveedor) {
        btnRegister.setText("Actualizar");
        btnRegister.getStyleClass().add("buttonLightWarning");
        cbDocumentTypeFactura.requestFocus();
        ProveedorTB proveedorTB = ProveedorADO.GetIdLisProveedor(idProveedor);
        if (proveedorTB != null) {
            idProveedor = proveedorTB.getIdProveedor();
            ObservableList<DetalleTB> lstypefa = cbDocumentTypeFactura.getItems();
            for (int i = 0; i < lstypefa.size(); i++) {
                if (proveedorTB.getTipoDocumento() == lstypefa.get(i).getIdDetalle()) {
                    cbDocumentTypeFactura.getSelectionModel().select(i);
                    break;
                }
            }
            txtBusinessName.setText(proveedorTB.getRazonSocial());
            txtTradename.setText(proveedorTB.getNombreComercial());
            txtDocumentNumberFactura.setText(proveedorTB.getNumeroDocumento());

            if (proveedorTB.getAmbito() != 0) {
                ObservableList<DetalleTB> lstamb = cbAmbito.getItems();
                for (int i = 0; i < lstamb.size(); i++) {
                    if (proveedorTB.getAmbito() == lstamb.get(i).getIdDetalle()) {
                        cbAmbito.getSelectionModel().select(i);
                        break;
                    }
                }
            }

            ObservableList<DetalleTB> lstest = cbEstado.getItems();
            for (int i = 0; i < lstest.size(); i++) {
                if (proveedorTB.getEstado() == lstest.get(i).getIdDetalle()) {
                    cbEstado.getSelectionModel().select(i);
                    break;
                }
            }

            txtTelefono.setText(proveedorTB.getTelefono());
            txtCelular.setText(proveedorTB.getCelular());
            txtEmail.setText(proveedorTB.getEmail());
            txtPaginaWeb.setText(proveedorTB.getPaginaWeb());
            txtDireccion.setText(proveedorTB.getDireccion());
            txtRepresentante.setText(proveedorTB.getRepresentante());

        }
    }

    private void toCrudProvider() {
        if (cbDocumentTypeFactura.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Proveedor", "Seleccione el tipo de documento, por favor.", false);

            cbDocumentTypeFactura.requestFocus();
        } else if (txtDocumentNumberFactura.getText().equalsIgnoreCase("")) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Proveedor", "Ingrese el número del documento, por favor.", false);

            txtDocumentNumberFactura.requestFocus();
        } else if (txtBusinessName.getText().equalsIgnoreCase("")) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Proveedor", "Ingrese la razón social o datos correspondientes, por favor.", false);

            txtBusinessName.requestFocus();
        } else if (cbEstado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Proveedor", "Seleccione el estado, por favor.", false);

            cbEstado.requestFocus();
        } else {
            short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Proveedor", "¿Esta seguro de continuar?", true);
            if (confirmation == 1) {
                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setIdProveedor(idProveedor);
                proveedorTB.setTipoDocumento(cbDocumentTypeFactura.getSelectionModel().getSelectedItem().getIdDetalle());
                proveedorTB.setNumeroDocumento(txtDocumentNumberFactura.getText().trim());
                proveedorTB.setRazonSocial(txtBusinessName.getText().trim());
                proveedorTB.setNombreComercial(txtTradename.getText().trim());
                proveedorTB.setAmbito(cbAmbito.getSelectionModel().getSelectedIndex() >= 0
                        ? cbAmbito.getSelectionModel().getSelectedItem().getIdDetalle()
                        : 0);
                proveedorTB.setEstado(cbEstado.getSelectionModel().getSelectedItem().getIdDetalle());
                proveedorTB.setTelefono(txtTelefono.getText().trim());
                proveedorTB.setCelular(txtCelular.getText().trim());
                proveedorTB.setEmail(txtEmail.getText().trim());
                proveedorTB.setPaginaWeb(txtPaginaWeb.getText().trim());
                proveedorTB.setDireccion(txtDireccion.getText().trim());
//                proveedorTB.setUsuarioRegistro(Session.USER_ID);
//                proveedorTB.setFechaRegistro(LocalDateTime.now());
                proveedorTB.setRepresentante(txtRepresentante.getText().trim());

                String result = ProveedorADO.CrudEntity(proveedorTB);
                switch (result) {
                    case "registered":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Proveedor", "Se registró correctamente el proveedor.", false);
                        Tools.Dispose(window);
                        break;
                    case "updated":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Proveedor", "Se actualizó correctamente el proveedor.", false);
                        Tools.Dispose(window);
                        break;
                    case "duplicate":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Proveedor", "No se puede haber 2 proveedores con el mismo número de documento.", false);
                        txtDocumentNumberFactura.requestFocus();
                        break;
                    default:
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Proveedor", result, false);
                        break;
                }

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
                return apiSunat.getUrlSunatApisPeru(txtDocumentNumberFactura.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtDocumentNumberFactura.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtDocumentNumberFactura.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL() == null) {
                    txtDocumentNumberFactura.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtDocumentNumberFactura.clear();

                    Tools.AlertMessageWarning(window, "Cliente", "Hubo un problema en cargar el JSON de la API.");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtDocumentNumberFactura.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtDocumentNumberFactura.clear();

                        Tools.AlertMessageWarning(window, "Cliente", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtDocumentNumberFactura.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("ruc") != null) {
                            txtDocumentNumberFactura.setText(sONObject.get("ruc").toString());
                        }
                        if (sONObject.get("razonSocial") != null) {
                            txtBusinessName.setText(sONObject.get("razonSocial").toString());
                        }
                        if (sONObject.get("direccion") != null) {
                            txtDireccion.setText(sONObject.get("direccion").toString());
                        }
                    }
                }
            } else {
                txtDocumentNumberFactura.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtDocumentNumberFactura.clear();

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
                return apiSunat.getUrlReniecApisPeru(txtDocumentNumberFactura.getText().trim());
            }
        };

        task.setOnScheduled(e -> {
            txtDocumentNumberFactura.setDisable(true);
            btnBuscarSunat.setDisable(true);
            btnBuscarReniec.setDisable(true);
        });

        task.setOnFailed(e -> {
            txtDocumentNumberFactura.setDisable(false);
            btnBuscarSunat.setDisable(false);
            btnBuscarReniec.setDisable(false);
        });

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("200")) {
                if (apiSunat.getJsonURL().equalsIgnoreCase("") || apiSunat.getJsonURL() == null) {
                    txtDocumentNumberFactura.setDisable(false);
                    btnBuscarSunat.setDisable(false);
                    btnBuscarReniec.setDisable(false);

                    txtDocumentNumberFactura.clear();

                    Tools.AlertMessageWarning(window, "Cliente", "Hubo un problema en cargar el JSON de la API.");
                } else {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        txtDocumentNumberFactura.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);

                        txtDocumentNumberFactura.clear();

                        Tools.AlertMessageWarning(window, "Cliente", "No se puedo obtener el formato del JSON.");
                    } else {
                        txtDocumentNumberFactura.setDisable(false);
                        btnBuscarSunat.setDisable(false);
                        btnBuscarReniec.setDisable(false);
                        if (sONObject.get("dni") != null) {
                            txtDocumentNumberFactura.setText(sONObject.get("dni").toString());
                        }
                        if (sONObject.get("apellidoPaterno") != null && sONObject.get("apellidoMaterno") != null && sONObject.get("nombres") != null) {
                            txtBusinessName.setText(sONObject.get("apellidoPaterno").toString() + " " + sONObject.get("apellidoMaterno").toString() + " " + sONObject.get("nombres").toString());
                        }
                    }
                }
            } else {
                txtDocumentNumberFactura.setDisable(false);
                btnBuscarSunat.setDisable(false);
                btnBuscarReniec.setDisable(false);

                txtDocumentNumberFactura.clear();

                Tools.AlertMessageError(window, "Cliente", result);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        toCrudProvider();
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            toCrudProvider();
        }
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
}
