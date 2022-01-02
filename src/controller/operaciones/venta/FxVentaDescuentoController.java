package controller.operaciones.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.SuministroTB;

public class FxVentaDescuentoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtPrecioVenta;
    @FXML
    private TextField txtPorcentajeDescuento;

    private FxVentaEstructuraController ventaEstructuraController;

    private SuministroTB suministroTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void initComponents(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
        txtPrecioVenta.setText(Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2));
    }

    private void executeDescuento() {
        if (Tools.isNumeric(txtPorcentajeDescuento.getText())) {

            double precio = suministroTB.getPrecioVentaGeneralUnico();

            double descuento = Double.parseDouble(txtPorcentajeDescuento.getText());
            double porcentajeRestante = precio * (descuento / 100.00);
            double preciocalculado = precio - porcentajeRestante;

            suministroTB.setDescuento(descuento);
            suministroTB.setDescuentoCalculado(porcentajeRestante);
            suministroTB.setDescuentoSumado(porcentajeRestante * suministroTB.getCantidad());

            suministroTB.setPrecioVentaGeneralUnico(precio);
            suministroTB.setPrecioVentaGeneralReal(preciocalculado);

            double impuesto = Tools.calculateTax(suministroTB.getImpuestoValor(), suministroTB.getPrecioVentaGeneralReal());

            suministroTB.setImpuestoSumado(suministroTB.getCantidad() * impuesto);
            suministroTB.setPrecioVentaGeneral(suministroTB.getPrecioVentaGeneralReal() + impuesto);

            suministroTB.setImporteBruto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralUnico());
            suministroTB.setSubImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());
            suministroTB.setImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneral());

            ventaEstructuraController.getTvList().refresh();
            ventaEstructuraController.calculateTotales();
            Tools.Dispose(window);
            ventaEstructuraController.getTxtSearch().requestFocus();
            ventaEstructuraController.getTxtSearch().clear();
        }
    }

    @FXML
    private void onKeyTypedPorcentajeDescuento(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeDescuento();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        executeDescuento();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

}
