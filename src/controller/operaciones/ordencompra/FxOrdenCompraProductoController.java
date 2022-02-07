package controller.operaciones.ordencompra;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.CompraADO;
import model.DetalleCompraTB;
import model.ImpuestoADO;
import model.ImpuestoTB;
import model.OrdenCompraDetalleTB;
import model.SuministroTB;

public class FxOrdenCompraProductoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblProducto;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtCosto;
    @FXML
    private ComboBox<ImpuestoTB> cbImpuestoCompra;
    @FXML
    private TableView<DetalleCompraTB> tvList;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcId;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcProveedor;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcCosto;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcCantidad;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcFechaCompra;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcObservacion;
    @FXML
    private TextField txObservacion;

    private FxOrdenCompraController ordenCompraController;

    private SuministroTB suministroTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        tcId.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDescripcion()));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getPrecioCompra(), 2)));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcFechaCompra.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaRegistro() + "\n" + cellData.getValue().getHoraRegistro()));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservacion()));

        cbImpuestoCompra.getItems().clear();
        cbImpuestoCompra.getItems().addAll(ImpuestoADO.GetTipoImpuestoCombBox());
    }

    public void loadComponents(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
        lblProducto.setText(suministroTB.getNombreMarca());
        txtCosto.setText(Tools.roundingValue(suministroTB.getCostoCompra(), 2));
        for (ImpuestoTB impuestoTB : cbImpuestoCompra.getItems()) {
            if (impuestoTB.getIdImpuesto() == suministroTB.getIdImpuesto()) {
                cbImpuestoCompra.getSelectionModel().select(impuestoTB);
                break;
            }
        }

        Object object = CompraADO.Listar_Detalle_Compra_ByIdSuministro(suministroTB.getIdSuministro());
        if (object instanceof ObservableList) {
            ObservableList<DetalleCompraTB> detalleCompraTBs = (ObservableList) object;
            tvList.setItems(detalleCompraTBs);
        }
    }

    private void addOrdenCompra() {
        if (!Tools.isNumeric(txtCantidad.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Ingrese la cantidad.");
            txtCantidad.requestFocus();
        } else if (!Tools.isNumeric(txtCosto.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Ingrese el costo.");
            txtCosto.requestFocus();
        } else if (cbImpuestoCompra.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Seleccione el impuesto.");
            cbImpuestoCompra.requestFocus();
        } else {
            OrdenCompraDetalleTB compraDetalleTB = new OrdenCompraDetalleTB();
            compraDetalleTB.setIdSuministro(suministroTB.getIdSuministro());
            compraDetalleTB.setCantidad(Double.parseDouble(txtCantidad.getText().trim()));
            compraDetalleTB.setCosto(Double.parseDouble(txtCosto.getText().trim()));
            compraDetalleTB.setDescuento(0);
            compraDetalleTB.setIdImpuesto(cbImpuestoCompra.getSelectionModel().getSelectedItem().getIdImpuesto());
            compraDetalleTB.setObservacion(txObservacion.getText().trim().toUpperCase());

            compraDetalleTB.setImpuestoTB(cbImpuestoCompra.getSelectionModel().getSelectedItem());

            SuministroTB newsSuministroTB = new SuministroTB();
            newsSuministroTB.setClave(suministroTB.getClave());
            newsSuministroTB.setNombreMarca(suministroTB.getNombreMarca());
            newsSuministroTB.setUnidadCompraName(suministroTB.getUnidadCompraName());
            compraDetalleTB.setSuministroTB(newsSuministroTB);

            Button btnRemove = new Button("X");
            btnRemove.getStyleClass().add("buttonDark");
            compraDetalleTB.setBtnRemove(btnRemove);

            ordenCompraController.getAddDetalle(compraDetalleTB);
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onKeyReleasedTvList(KeyEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (event.getCode() == KeyCode.UP) {
                txtCosto.setText(Tools.roundingValue(tvList.getSelectionModel().getSelectedItem().getPrecioCompra(), 2));
            } else if (event.getCode() == KeyCode.DOWN) {
                txtCosto.setText(Tools.roundingValue(tvList.getSelectionModel().getSelectedItem().getPrecioCompra(), 2));
            }
        }
    }

    @FXML
    private void onMouseClickedTvList(MouseEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            txtCosto.setText(Tools.roundingValue(tvList.getSelectionModel().getSelectedItem().getPrecioCompra(), 2));
        }
    }

    @FXML
    private void onKeyTypedCantidad(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCantidad.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedCosto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCosto.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addOrdenCompra();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        addOrdenCompra();
    }

    @FXML
    private void onKeyPressedCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancel(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitOrdenCompraController(FxOrdenCompraController ordenCompraController) {
        this.ordenCompraController = ordenCompraController;
    }

}
