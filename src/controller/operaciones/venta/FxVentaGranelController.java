package controller.operaciones.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.SuministroTB;

public class FxVentaGranelController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtImporte;
    @FXML
    private Label lblArticulo;

    private FxVentaEstructuraController ventaEstructuraController;

    private SuministroTB suministroTB;

    private double oldPrecio;

    private boolean opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void initComponents(String value, SuministroTB suministroTB, boolean opcion) {
        lblTitle.setText(value);
        this.suministroTB = suministroTB;
        this.opcion = opcion;
        lblArticulo.setText(suministroTB.getNombreMarca());
        oldPrecio = suministroTB.getPrecioVentaGeneral();
    }

    private void executeEventAceptar() {
        double importe = opcion
                ? Tools.isNumeric(txtImporte.getText()) ? (Double.parseDouble(txtImporte.getText()) <= 0 ? oldPrecio 
                : oldPrecio + Double.parseDouble(txtImporte.getText())) : oldPrecio
                : Tools.isNumeric(txtImporte.getText())
                ? (Double.parseDouble(txtImporte.getText()) <= 0 
                ? oldPrecio : Double.parseDouble(txtImporte.getText()))
                : oldPrecio;

        double valor_sin_impuesto = importe / ((suministroTB.getImpuestoValor() / 100.00) + 1);
        double descuento = suministroTB.getDescuento();
        double porcentajeRestante = valor_sin_impuesto * (descuento / 100.00);
        double preciocalculado = valor_sin_impuesto - porcentajeRestante;

        suministroTB.setDescuentoCalculado(porcentajeRestante);
        suministroTB.setDescuentoSumado(porcentajeRestante * suministroTB.getCantidad());

        suministroTB.setPrecioVentaGeneralUnico(valor_sin_impuesto);
        suministroTB.setPrecioVentaGeneralReal(preciocalculado);

        double impuesto = Tools.calculateTax(suministroTB.getImpuestoValor(), suministroTB.getPrecioVentaGeneralReal());
        suministroTB.setImpuestoSumado(suministroTB.getCantidad() * impuesto);
        suministroTB.setPrecioVentaGeneral(suministroTB.getPrecioVentaGeneralReal() + impuesto);

        suministroTB.setImporteBruto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralUnico() );
        suministroTB.setSubImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());
        suministroTB.setImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneral());

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
