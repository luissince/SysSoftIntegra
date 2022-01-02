package controller.inventario.lote;

import controller.inventario.suministros.FxSuministrosListaController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.LoteADO;
import model.LoteTB;

public class FxLoteCambiarController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtArticulo;
    @FXML
    private TextField txtLote;
    @FXML
    private TextField txtCantidad;
    @FXML
    private DatePicker dtCaducidad;
    @FXML
    private Button btnProcess;
    @FXML
    private Button btnBuscar;

    private FxLoteProcesoController loteProcesoController;

    private FxLoteController loteController;

    private boolean statebatch;

    private int indexbatch;

    private long idLote;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        statebatch = false;
        indexbatch = 0;
        idLote = 0;
    }

    public void setAddLote() {
        statebatch = false;
        btnBuscar.setDisable(false);
    }

    public void setEditBatchRealizado(String[] value) {
        statebatch = true;
        txtLote.setText(value[0]);
        idLote = Long.valueOf(value[1]);
        txtArticulo.setText(value[2] + " " + value[3]);
        txtCantidad.setText(value[4]);
        Tools.actualDate(value[5], dtCaducidad);
        txtLote.setDisable(true);
        btnBuscar.setDisable(false);
        btnProcess.setText("Editar");
    }

    public void setEditBatch(String[] value) {
        statebatch = true;
        indexbatch = Integer.parseInt(value[0]);
        txtLote.setText(value[1]);
        txtCantidad.setText(value[2]);
        Tools.actualDate(value[3], dtCaducidad);
    }

    private void addLote() {
        if (!statebatch) {
            if(txtLote.getText().trim().length() == 0){
                Tools.AlertMessageWarning(window, "Lote", "Ingrese un lote.");
            }else if(!Tools.isNumeric(txtCantidad.getText())){
                Tools.AlertMessageWarning(window, "Lote", "Ingrese la cantidad.");
            }else if(dtCaducidad.getValue()!=null){
                Tools.AlertMessageWarning(window, "Lote", "Ingrese la fecha de vencimiento o caducidad.");
            }else{
                
            }
        } else {
            if (loteController != null) {
                if (!Tools.isNumeric(txtCantidad.getText())) {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Lotes", "Ingrese la cantidad de unidades del lote, por favor", false);
                    txtCantidad.requestFocus();
                } else {
                    if (dtCaducidad.getValue() == null) {
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Lotes", "Ingrese la fecha de caducidad, por favor", false);
                        dtCaducidad.requestFocus();
                    } else {
                        LoteTB loteTB = new LoteTB();
                        loteTB.setExistenciaActual(Double.parseDouble(txtCantidad.getText()));
                        loteTB.setFechaCaducidad(dtCaducidad.getValue());
                        loteTB.setIdLote(idLote);
                        String result = LoteADO.UpdateLote(loteTB);
                        if (result.equalsIgnoreCase("updated")) {
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Lotes", "Se actualizo correctamente el lote.", false);
                            Tools.Dispose(window);
                        } else {
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Lotes", result, false);

                        }

                    }
                }
            } else if (loteProcesoController != null) {
                if (!Tools.isNumeric(txtCantidad.getText())) {
                    Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Lotes", "Ingrese la cantidad de unidades del lote, por favor", false);
                    txtCantidad.requestFocus();
                } else {
                    if (Double.parseDouble(txtCantidad.getText()) < 1) {
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Lotes", "La cantidad debe ser mayor a 0, por favor", false);
                        txtCantidad.requestFocus();
                    } else {
                        if (dtCaducidad.getValue() == null) {
                            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Lotes", "Ingrese la fecha de caducidad, por favor", false);
                            dtCaducidad.requestFocus();
                        } else {
                            LoteTB loteTB = new LoteTB();
                            loteTB.setNumeroLote(txtLote.getText());
                            loteTB.setExistenciaActual(Double.parseDouble(txtCantidad.getText()));
                            loteTB.setExistenciaInicial(Double.parseDouble(txtCantidad.getText()));
                            loteTB.setFechaCaducidad(dtCaducidad.getValue());
                            loteTB.setIdArticulo(loteProcesoController.getIdArticulo());
                            if (!statebatch) {
                                loteProcesoController.getListLote().add(loteTB);
                                loteProcesoController.calculateBatch();
                            } else {
                                loteProcesoController.getListLote().set(indexbatch, loteTB);
                                loteProcesoController.calculateBatch();
                            }

                            Tools.Dispose(window);
                        }
                    }
                }
            }
        }
    }

    private void openWindowSuministro() {
        try {
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitLoteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
            controller.fillSuministrosTable((short) 0, "");
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPŕessedBuscar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
        }
    }

    @FXML
    private void onActionBuscar(ActionEvent event) {
        openWindowSuministro();
    }

    @FXML
    private void onActionAgregar(ActionEvent event) {
        addLote();

    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addLote();
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(window);
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onKeyTypedCantidad(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtCantidad.getText().contains(".") || c == '-' && txtCantidad.getText().contains("-")) {
            event.consume();
        }
    }

    public TextField getTxtArticulo() {
        return txtArticulo;
    }

    public TextField getTxtCantidad() {
        return txtCantidad;
    }

    public void setProcesoController(FxLoteProcesoController loteProcesoController) {
        this.loteProcesoController = loteProcesoController;
    }

    public void setLoteController(FxLoteController loteController) {
        this.loteController = loteController;
    }

}
