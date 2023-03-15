package controller.operaciones.cotizacion;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
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
import model.CotizacionDetalleTB;
import model.DetalleTB;
import model.SuministroTB;
import service.DetalleADO;

public class FxCotizacionProductoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblProducto;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox<DetalleTB> cbUnidadMedida;

    private FxCotizacionController cotizacionController;

    private SuministroTB suministroTB;

    private boolean edit;

    private int index;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        cbUnidadMedida.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0013", ""));
    }

    public void initComponents(SuministroTB suministroTB, boolean edit, int index) {
        this.suministroTB = suministroTB;
        this.edit = edit;
        this.index = index;
        txtCantidad.setText(Tools.roundingValue(suministroTB.getCantidad() <= 0 ? 1 : suministroTB.getCantidad(), 2));
        txtPrecio.setText(Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2));
        lblProducto.setText(suministroTB.getNombreMarca());
        for (DetalleTB dtb : cbUnidadMedida.getItems()) {
            if (dtb.getIdDetalle() == suministroTB.getUnidadCompra()) {
                cbUnidadMedida.getSelectionModel().select(dtb);
                break;
            }
        }
    }

    private void onEventAceptar() {
        if (!Tools.isNumeric(txtCantidad.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese la cantidad.");
            txtCantidad.requestFocus();
            return;
        }

        if (!Tools.isNumeric(txtPrecio.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese el precio.");
            txtPrecio.requestFocus();
            return;
        }

        if (cbUnidadMedida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Seleccione su unidad de medida.");
            cbUnidadMedida.requestFocus();
            return;
        }

        CotizacionDetalleTB cotizacionDetalleTB = new CotizacionDetalleTB();
        cotizacionDetalleTB.setIdSuministro(suministroTB.getIdSuministro());
        cotizacionDetalleTB.setCantidad(Double.parseDouble(txtCantidad.getText()));
        cotizacionDetalleTB.setPrecio(Double.parseDouble(txtPrecio.getText()));
        cotizacionDetalleTB.setDescuento(0);
        cotizacionDetalleTB.setIdImpuesto(suministroTB.getIdImpuesto());
        cotizacionDetalleTB.setUso(false);

        cotizacionDetalleTB.setImpuestoTB(suministroTB.getImpuestoTB());

        SuministroTB newSuministroTB = new SuministroTB();
        newSuministroTB.setClave(suministroTB.getClave());
        newSuministroTB.setNombreMarca(suministroTB.getNombreMarca());
        newSuministroTB.setUnidadCompra(cbUnidadMedida.getSelectionModel().getSelectedItem().getIdDetalle());
        newSuministroTB.setUnidadCompraName(cbUnidadMedida.getSelectionModel().getSelectedItem().getNombre());
        cotizacionDetalleTB.setSuministroTB(newSuministroTB);

        Button button = new Button("X");
        button.getStyleClass().add("buttonDark");
        cotizacionDetalleTB.setBtnRemove(button);

        Tools.Dispose(apWindow);
        if (edit) {
            cotizacionController.getEditDetalle(index, cotizacionDetalleTB);
        } else {
            cotizacionController.getAddDetalle(cotizacionDetalleTB);
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(apWindow);
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

    public TextField getTxtCantidad() {
        return txtCantidad;
    }

    public void setInitCotizacionController(FxCotizacionController cotizacionController) {
        this.cotizacionController = cotizacionController;
    }

}
