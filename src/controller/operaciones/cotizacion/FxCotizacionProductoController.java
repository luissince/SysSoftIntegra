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
import model.DetalleADO;
import model.DetalleTB;
import model.SuministroTB;

public class FxCotizacionProductoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblArticulo;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox<DetalleTB> cbUnidadMedida;

    private FxCotizacionController cotizacionController;

    private SuministroTB suministroTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        cbUnidadMedida.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0013", ""));
    }

    public void initComponents(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
        txtCantidad.setText(Tools.roundingValue(1, 2));
        txtPrecio.setText(Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2));
        lblArticulo.setText(suministroTB.getNombreMarca());
    }

    private void onEventAceptar() {
        if (!Tools.isNumeric(txtCantidad.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese la cantidad.");
            txtCantidad.requestFocus();
        } else if (!Tools.isNumeric(txtPrecio.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese el precio.");
            txtPrecio.requestFocus();
        } else {
            CotizacionDetalleTB cotizacionDetalleTB = new CotizacionDetalleTB();
            cotizacionDetalleTB.setIdSuministro(suministroTB.getIdSuministro());
            cotizacionDetalleTB.setCantidad(Double.parseDouble(txtCantidad.getText()));
            cotizacionDetalleTB.setPrecio(Double.parseDouble(txtPrecio.getText()));
            cotizacionDetalleTB.setDescuento(0);
            cotizacionDetalleTB.setIdImpuesto(suministroTB.getIdImpuesto());

            cotizacionDetalleTB.setImpuestoTB(suministroTB.getImpuestoTB());

            SuministroTB newSuministroTB = new SuministroTB();
            newSuministroTB.setClave(suministroTB.getClave());
            newSuministroTB.setNombreMarca(suministroTB.getNombreMarca());
            newSuministroTB.setUnidadCompraName(suministroTB.getUnidadCompraName());
            cotizacionDetalleTB.setSuministroTB(suministroTB);

            Button button = new Button("X");
            button.getStyleClass().add("buttonDark");
            cotizacionDetalleTB.setBtnRemove(button);

            Tools.Dispose(apWindow);
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
