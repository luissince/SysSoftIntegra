package controller.operaciones.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.DetalleADO;
import model.DetalleTB;
import model.SuministroTB;

public class FxVentaGranelController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtImporte;
    @FXML
    private ComboBox<DetalleTB> cbUnidadMedida;

    private FxVentaEstructuraController ventaEstructuraController;

    private SuministroTB suministroTB;

    private double oldPrecio;

    private boolean opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        cbUnidadMedida.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0013", ""));
    }

    public void initComponents(String value, SuministroTB suministroTB, boolean opcion) {
        lblTitle.setText(value);
        this.suministroTB = suministroTB;
        this.opcion = opcion;
        oldPrecio = suministroTB.getPrecioVentaGeneral();
        for (DetalleTB dtb : cbUnidadMedida.getItems()) {
            if (dtb.getIdDetalle() == suministroTB.getUnidadCompra()) {
                cbUnidadMedida.getSelectionModel().select(dtb);
                break;
            }
        }
    }

    private void executeEventAceptar() {
        double importe = opcion
                ? Tools.isNumeric(txtImporte.getText()) ? (Double.parseDouble(txtImporte.getText()) <= 0 ? oldPrecio
                : oldPrecio + Double.parseDouble(txtImporte.getText())) : oldPrecio
                : Tools.isNumeric(txtImporte.getText())
                ? (Double.parseDouble(txtImporte.getText()) <= 0
                ? oldPrecio : Double.parseDouble(txtImporte.getText()))
                : oldPrecio;
        
        suministroTB.setUnidadCompra(cbUnidadMedida.getSelectionModel().getSelectedItem().getIdDetalle());
        suministroTB.setUnidadCompraName(cbUnidadMedida.getSelectionModel().getSelectedItem().getNombre());
        suministroTB.setPrecioVentaGeneral(importe);

        ventaEstructuraController.getTvList().refresh();
        ventaEstructuraController.calculateTotales();
        Tools.Dispose(window);
        ventaEstructuraController.getTxtSearch().requestFocus();
        ventaEstructuraController.getTxtSearch().clear();
    }

    @FXML
    private void onKeyTypedImporte(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtImporte.getText().contains(".") || c == '-' && txtImporte.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        executeEventAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeEventAceptar();
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
            ventaEstructuraController.getTxtSearch().requestFocus();
            ventaEstructuraController.getTxtSearch().clear();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
        ventaEstructuraController.getTxtSearch().requestFocus();
        ventaEstructuraController.getTxtSearch().clear();
    }

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

}
