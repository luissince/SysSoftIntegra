package controller.operaciones.venta;

import controller.tools.BbItemProducto;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.PreciosTB;
import model.SuministroADO;

public class FxVentaDetalleProductoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtDescuento;
    @FXML
    private Label lblProducto;
    @FXML
    private ListView<PreciosTB> tvListPrecios;
    @FXML
    private TextField txtBonificacion;

    private FxVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private BbItemProducto bbItemProducto;

    private int index;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
    }

    public void loadData(int index, BbItemProducto bbItemProducto) {
        this.index = index;
        this.bbItemProducto = bbItemProducto;
        lblProducto.setText(bbItemProducto.getSuministroTB().getNombreMarca());
        txtPrecio.setText(Tools.roundingValue(bbItemProducto.getSuministroTB().getPrecioVentaGeneral(), 2));
        txtCantidad.setText(Tools.roundingValue(bbItemProducto.getSuministroTB().getCantidad(), 2));
        txtDescuento.setText(Tools.roundingValue(bbItemProducto.getSuministroTB().getDescuento(), 2));
        txtBonificacion.setText("" + bbItemProducto.getSuministroTB().getBonificacion());
        loadDataView(bbItemProducto.getSuministroTB().getIdSuministro());
    }

    public void loadDataView(String idSuministro) {
        ObservableList<PreciosTB> artPrices = SuministroADO.GetItemPriceList(idSuministro);
        tvListPrecios.setItems(artPrices);
        tvListPrecios.setOnMouseClicked(n -> {
            if (tvListPrecios.getSelectionModel().getSelectedIndex() >= 0) {
                if (Tools.isNumeric(txtCantidad.getText()) && Tools.isNumeric(txtDescuento.getText())) {
                    double valor = tvListPrecios.getSelectionModel().getSelectedItem().getValor();
                    double factor = tvListPrecios.getSelectionModel().getSelectedItem().getFactor();

                    double precio = factor <= 0 ? valor : valor / factor;
                    double descuento = Double.parseDouble(txtDescuento.getText());
                    double cantidad = factor <= 0 ? Double.parseDouble(txtCantidad.getText()) : factor == 1 ? Double.parseDouble(txtCantidad.getText()) : factor;

                    txtCantidad.setText(Tools.roundingValue(cantidad, 2));
                    bbItemProducto.getSuministroTB().setCantidad(cantidad);
                    bbItemProducto.getSuministroTB().setDescuento(descuento);
                    bbItemProducto.getSuministroTB().setPrecioVentaGeneral(precio);

                    txtPrecio.setText(Tools.roundingValue(bbItemProducto.getSuministroTB().getPrecioVentaGeneral(), 2));
                }
            }
        });
    }

    @FXML
    private void onKeyReleasedPrecio(KeyEvent event) {
        if (Tools.isNumeric(txtPrecio.getText()) && Tools.isNumeric(txtCantidad.getText()) && Tools.isNumeric(txtDescuento.getText())) {
            double precio = Double.parseDouble(txtPrecio.getText());
            bbItemProducto.getSuministroTB().setPrecioVentaGeneral(precio);
        }
    }

    @FXML
    private void onKeyReleasedCantidad(KeyEvent event) {
        if (Tools.isNumeric(txtPrecio.getText()) && Tools.isNumeric(txtCantidad.getText()) && Tools.isNumeric(txtDescuento.getText())) {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            bbItemProducto.getSuministroTB().setBonificacion(!Tools.isNumeric(txtBonificacion.getText().trim()) ? 0 : Double.parseDouble(txtBonificacion.getText().trim()));
            bbItemProducto.getSuministroTB().setCantidad(cantidad);
        }
    }

    @FXML
    private void onKeyReleasedDescuento(KeyEvent event) {
        if (Tools.isNumeric(txtPrecio.getText()) && Tools.isNumeric(txtCantidad.getText()) && Tools.isNumeric(txtDescuento.getText())) {
            double descuento = Double.parseDouble(txtDescuento.getText());
            bbItemProducto.getSuministroTB().setDescuento(descuento);
        }
    }

    @FXML
    private void onKeyReleasedBonificacion(KeyEvent event) {
        if (Tools.isNumeric(txtBonificacion.getText())) {
            double bonificacion = Double.parseDouble(txtBonificacion.getText().trim()) < 0 ? 0 : Double.parseDouble(txtBonificacion.getText().trim());
            bbItemProducto.getSuministroTB().setBonificacion(bonificacion);
        }
    }

    @FXML
    private void onKeyTypedPrecio(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecio.getText().contains(".") || c == '-' && txtPrecio.getText().contains("-")) {
            event.consume();
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
    private void onKeyTypedPorcentajeDescuento(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedBonificacion(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            bbItemProducto.getChildren().clear();
            bbItemProducto.addElementListView();
            ventaEstructuraNuevoController.getLvProductoAgregados().getItems().set(index, bbItemProducto);
            ventaEstructuraNuevoController.calculateTotales();
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        bbItemProducto.getChildren().clear();
        bbItemProducto.addElementListView();
        ventaEstructuraNuevoController.getLvProductoAgregados().getItems().set(index, bbItemProducto);
        ventaEstructuraNuevoController.calculateTotales();
        Tools.Dispose(apWindow);
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitVentaEstructuraNuevoController(FxVentaEstructuraNuevoController ventaEstructuraNuevoController) {
        this.ventaEstructuraNuevoController = ventaEstructuraNuevoController;
    }

}
