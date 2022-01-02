package controller.configuracion.almacen;

import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.AlmacenADO;
import model.AlmacenTB;
import model.UbigeoADO;
import model.UbigeoTB;

public class FxAlmacenProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<UbigeoTB> cbUbigeo;
    @FXML
    private TextField txtDireccion;
    @FXML
    private Button btnGuardar;

    private FxAlmacenController fxAlmacenController;

    private int idAlmacen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        idAlmacen = -1;
        loadUbigeo();
    }

    public void loadComponents(AlmacenTB almacenTB) {
        idAlmacen = almacenTB.getIdAlmacen();
        txtNombre.setText(almacenTB.getNombre());

        cbUbigeo.getItems().clear();
        cbUbigeo.getItems().add(almacenTB.getUbigeoTB());
        if (!cbUbigeo.getItems().isEmpty()) {
            cbUbigeo.getSelectionModel().select(0);
        }

        txtDireccion.setText(almacenTB.getDireccion());
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
            List<UbigeoTB> ubigeoTBs = UbigeoADO.GetSearchComboBoxUbigeo(searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().getText().trim());
            ubigeoTBs.forEach(e -> {
                searchComboBoxUbigeoLlegada.getComboBox().getItems().add(e);
            });
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
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxUbigeoLlegada.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxUbigeoLlegada.getComboBox().hide();
                }
            }
        });
    }

    private void onEventGuardar() {
        if (txtNombre.getText().trim().length() == 0) {
            Tools.AlertMessageWarning(apWindow, "Almacen", "Ingrese el nombre del almacen.");
            txtNombre.requestFocus();
        } else if (cbUbigeo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Almacen", "Seleccione el ubigeo del almacen.");
            cbUbigeo.requestFocus();
        } else {

            AlmacenTB almacenTB = new AlmacenTB();
            almacenTB.setIdAlmacen(idAlmacen);
            almacenTB.setNombre(txtNombre.getText().trim().toUpperCase());
            almacenTB.setIdUbigeo(cbUbigeo.getSelectionModel().getSelectedItem().getIdUbigeo());
            almacenTB.setDireccion(txtDireccion.getText().trim().toUpperCase());
            almacenTB.setFecha(Tools.getDate());
            almacenTB.setHora(Tools.getTime());
            almacenTB.setIdUsuario(Session.USER_ID);

            short value = Tools.AlertMessageConfirmation(apWindow, "Almacen", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = AlmacenADO.CrudAlmacen(almacenTB);
                if (result.equalsIgnoreCase("inserted")) {
                    Tools.AlertMessageInformation(apWindow, "Almacen", "Se registró correctamente el almacen.");
                    Tools.Dispose(apWindow);
                    fxAlmacenController.initLoad();
                } else if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(apWindow, "Almacen", "Se actualizó correctamente el almacen.");
                    Tools.Dispose(apWindow);
                    fxAlmacenController.initLoad();
                } else if (result.equalsIgnoreCase("name")) {
                    Tools.AlertMessageWarning(apWindow, "Almacen", "No se puede registrar o actualizar el mismo nombre.");
                } else {
                    Tools.AlertMessageError(apWindow, "Almacen", result);
                }
            }
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onKeyCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitAlmacenController(FxAlmacenController fxAlmacenController) {
        this.fxAlmacenController = fxAlmacenController;
    }

}
