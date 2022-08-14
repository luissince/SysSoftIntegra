package controller.configuracion.comprobante;

import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.TicketADO;
import model.TicketTB;
import model.TipoDocumentoADO;
import model.TipoDocumentoTB;

public class FxTipoDocumentoProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtSerie;
    @FXML
    private TextField txtNumeracion;
    @FXML
    private TextField txtCodigoAlterno;
    @FXML
    private Button btnGuardar;
    @FXML
    private CheckBox cbGuia;
    @FXML
    private CheckBox cbFacturado;
    @FXML
    private CheckBox cbNotaCredito;
    @FXML
    private TextField txtCaracteres;
    @FXML
    private CheckBox cbUsaCaracteres;
    @FXML
    private CheckBox cbEstado;
    @FXML
    private ComboBox<TicketTB> cbImprimir;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Button btnCancelarProceso;

    private FxTipoDocumentoController tipoDocumentoController;

    private int idTipoDocumento;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void initUpdate(int codigo) {
        idTipoDocumento = codigo;
//        txtNombre.setText(nombre);
//        txtSerie.setText(serie);
//        txtNumeracion.setText("" + numeracion);
//        txtCodigoAlterno.setText(codigoAlterno);
//        btnGuardar.setText("Actualizar");
//        btnGuardar.getStyleClass().add("buttonLightWarning");
//        cbGuia.setSelected(guia);
//        cbGuia.setText(guia ? "Si" : "No");
//        cbFacturado.setSelected(factura);
//        cbFacturado.setText(factura ? "Si" : "No");
//        cbNotaCredito.setSelected(notaCredito);
//        cbNotaCredito.setText(notaCredito ? "Si" : "No");
//        cbEstado.setSelected(estado);
//        cbEstado.setText(estado ? "Activo" : "Inactivo");
//        cbUsaCaracteres.setSelected(usaCaracteres);
//        cbUsaCaracteres.setText(usaCaracteres ? "Si" : "No");
//        txtCaracteres.setDisable(!usaCaracteres);
//        txtCaracteres.setText(numCaracteres + "");

        loadComponents();
    }

    private void loadComponents() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                
                
                
                return TicketADO.ListTicket();
            }
        };

        task.setOnScheduled(e -> {
            hbLoadProcesando.setVisible(true);

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
        
        task.setOnFailed(e->{
            Tools.println(task.getMessage());
            Tools.println(task.getException().getMessage());
        });

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof ArrayList) {
                cbImprimir.getItems().addAll((ArrayList) object);
            }

            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void saveTipoImpuesto() {
        if (txtNombre.getText().trim().isEmpty()) {
            Tools.AlertMessageWarning(window, "Tipo de documento", "Ingrese el nombre del comprobante.");
            txtNombre.requestFocus();
        } else if (txtSerie.getText().trim().isEmpty()) {
            Tools.AlertMessageWarning(window, "Tipo de documento", "Ingrese la serie del comprobante.");
            txtSerie.requestFocus();
        } else if (!Tools.isNumericInteger(txtNumeracion.getText())) {
            Tools.AlertMessageWarning(window, "Tipo de documento", "Ingrese la numeración del comprobante.");
            txtNumeracion.requestFocus();
        } else if (Integer.parseInt(txtNumeracion.getText().trim()) <= 0) {
            Tools.AlertMessageWarning(window, "Tipo de documento", "La numeración no puede ser menor que 1.");
            txtNumeracion.requestFocus();
        } else if (cbUsaCaracteres.isSelected() && !Tools.isNumericInteger(txtCaracteres.getText().trim())) {
            Tools.AlertMessageWarning(window, "Tipo de documento", "Ingrese el número de caracteres a validar.");
            txtCaracteres.requestFocus();
        } else {
            TipoDocumentoTB documentoTB = new TipoDocumentoTB();
            documentoTB.setIdTipoDocumento(idTipoDocumento);
            documentoTB.setNombre(txtNombre.getText().toUpperCase().trim());
            documentoTB.setSerie(txtSerie.getText().trim().toUpperCase());
            documentoTB.setNumeracion(Integer.parseInt(txtNumeracion.getText().trim()));
            documentoTB.setCodigoAlterno(txtCodigoAlterno.getText().trim());
            documentoTB.setPredeterminado(false);
            documentoTB.setGuia(cbGuia.isSelected());
            documentoTB.setFactura(cbFacturado.isSelected());
            documentoTB.setNotaCredito(cbNotaCredito.isSelected());
            documentoTB.setCampo(cbUsaCaracteres.isSelected());
            documentoTB.setNumeroCampo(!Tools.isNumericInteger(txtCaracteres.getText()) ? 0 : Integer.parseInt(txtCaracteres.getText()));
            documentoTB.setEstado(cbEstado.isSelected());

            short value = Tools.AlertMessageConfirmation(window, "Comprobante", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = TipoDocumentoADO.CrudTipoDocumento(documentoTB);
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Tipo de documento", "Se actualizado correctamente", false);
                    Tools.Dispose(window);
                    tipoDocumentoController.initLoad();
                } else if (result.equalsIgnoreCase("duplicate")) {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Tipo de documento", "Ya existe comprobante con el mismo nombre", false);
                    txtNombre.requestFocus();
                } else if (result.equalsIgnoreCase("inserted")) {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Tipo de documento", "Se ha insertado correctamente", false);
                    Tools.Dispose(window);
                    tipoDocumentoController.initLoad();
                } else {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Tipo de documento", result, false);
                }
            }
        }
    }

    @FXML
    private void onKeyTypedNumeracion(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        saveTipoImpuesto();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            saveTipoImpuesto();
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

    @FXML
    private void onActionGuia(ActionEvent event) {
        cbGuia.setText(cbGuia.isSelected() ? "Si" : "No");
    }

    @FXML
    private void onActionFactura(ActionEvent event) {
        cbFacturado.setText(cbFacturado.isSelected() ? "Si" : "No");
    }

    @FXML
    private void onActionNotaCredito(ActionEvent event) {
        cbNotaCredito.setText(cbNotaCredito.isSelected() ? "Si" : "No");
    }

    @FXML
    private void onKeyTypedCaracteres(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onActionUsaCaracteres(ActionEvent event) {
        txtCaracteres.setDisable(!cbUsaCaracteres.isSelected());
        cbUsaCaracteres.setText(cbUsaCaracteres.isSelected() ? "Si" : "No");
        txtCaracteres.requestFocus();
    }

    @FXML
    private void onActionEstado(ActionEvent event) {
        cbEstado.setText(cbEstado.isSelected() ? "Activo" : "Inactivo");
    }

    public void setTipoDocumentoController(FxTipoDocumentoController tipoDocumentoController) {
        this.tipoDocumentoController = tipoDocumentoController;
    }

}
