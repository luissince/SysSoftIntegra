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

public class FxVentaCantidadesController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtCantidad;
    @FXML
    private Label lblArticulo;

    private FxVentaEstructuraController ventaEstructuraController;

    private SuministroTB suministroTB;

    private double oldCantidad;

    private boolean tipoVenta;

    private boolean primerLlamado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        primerLlamado = false;
    }

    public void initComponents(SuministroTB suministroTB, boolean tipoVenta, boolean primerLlamado) {
        lblArticulo.setText(suministroTB.getNombreMarca());
        this.suministroTB = suministroTB;
        this.oldCantidad = suministroTB.getCantidad();
        this.tipoVenta = tipoVenta;
        this.primerLlamado = primerLlamado;
        txtCantidad.setText(Tools.roundingValue(suministroTB.getCantidad(), 2));
    }

    private void eventMas() {
        if (Tools.isNumeric(txtCantidad.getText().trim())) {
            if (Double.parseDouble(txtCantidad.getText().trim()) > 0) {
                double cantidad = Double.parseDouble(txtCantidad.getText()) + 1;
                txtCantidad.setText(Tools.roundingValue(cantidad, 2));
            } else {
                txtCantidad.setText(Tools.roundingValue(1, 2));
            }
        } else {
            txtCantidad.setText(Tools.roundingValue(1, 2));
        }
    }

    private void eventMenos() {
        if (Tools.isNumeric(txtCantidad.getText().trim())) {
            if (Double.parseDouble(txtCantidad.getText().trim()) > 1) {
                double cantidad = Double.parseDouble(txtCantidad.getText()) - 1;
                txtCantidad.setText(Tools.roundingValue(cantidad, 2));
            } else {
                txtCantidad.setText(Tools.roundingValue(1, 2));
            }
        } else {
            txtCantidad.setText(Tools.roundingValue(1, 2));
        }
    }

    private void eventAceptar() {
        double cantidad = Tools.isNumeric(txtCantidad.getText().trim())
                ? (Double.parseDouble(txtCantidad.getText()) <= 0 ? oldCantidad : Double.parseDouble(txtCantidad.getText()))
                : oldCantidad;
        suministroTB.setCantidad(primerLlamado ? cantidad : suministroTB.getCantidad() + cantidad);
        double porcentajeRestante = suministroTB.getPrecioVentaGeneralUnico() * (suministroTB.getDescuento() / 100.00);

        suministroTB.setDescuentoCalculado(porcentajeRestante);
        suministroTB.setDescuentoSumado(porcentajeRestante * suministroTB.getCantidad());

        double impuesto = Tools.calculateTax(suministroTB.getImpuestoValor(), suministroTB.getPrecioVentaGeneralReal());
        suministroTB.setImpuestoSumado(suministroTB.getCantidad() * impuesto);

        suministroTB.setImporteBruto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralUnico());
        suministroTB.setSubImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());
        suministroTB.setImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneral());

        if (tipoVenta) {
            ventaEstructuraController.getAddArticulo(suministroTB, ventaEstructuraController.getWindow().getScene().getWindow());
            Tools.Dispose(apWindow);
        } else {
            ventaEstructuraController.getTvList().refresh();
            ventaEstructuraController.calculateTotales();
            Tools.Dispose(apWindow);
            ventaEstructuraController.getTxtSearch().requestFocus();
            ventaEstructuraController.getTxtSearch().clear();
        }
    }

    @FXML
    private void onKeyPressedMas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventMas();
        }
    }

    @FXML
    private void onActionMas(ActionEvent event) {
        eventMas();
    }

    @FXML
    private void onKeyPressedMenos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventMenos();
        }
    }

    @FXML
    private void onActionMenos(ActionEvent event) {
        eventMenos();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventAceptar();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        eventAceptar();
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

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }
}
