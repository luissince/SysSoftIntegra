package controller.operaciones.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.PreciosTB;
import model.SuministroADO;
import model.SuministroTB;

public class FxListaPreciosController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblNombreArticulo;
    @FXML
    private TableView<PreciosTB> tvList;
    @FXML
    private TableColumn<PreciosTB, Integer> tcNumero;
    @FXML
    private TableColumn<PreciosTB, String> tcNombre;
    @FXML
    private TableColumn<PreciosTB, String> tcValor;
    @FXML
    private TableColumn<PreciosTB, String> tcFactor;

    private FxVentaEstructuraController ventaEstructuraController;

    private SuministroTB suministroTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        tcNumero.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tcNombre.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        tcValor.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getValor(), 2)));
        tcFactor.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getFactor(), 2)));
    }

    public void loadDataView(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
        ObservableList<PreciosTB> artPrices = SuministroADO.GetItemPriceList(suministroTB.getIdSuministro());
        tvList.setItems(artPrices);
        if (!tvList.getItems().isEmpty()) {
            tvList.requestFocus();
            tvList.getSelectionModel().select(0);
        }
        lblNombreArticulo.setText(suministroTB.getNombreMarca());
    }

    private void onSelectPrice() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            double valor = tvList.getSelectionModel().getSelectedItem().getValor();
            double factor = tvList.getSelectionModel().getSelectedItem().getFactor();

            double precio = factor <= 0 ? valor : valor / factor;
            double cantidad = factor <= 0 ? suministroTB.getCantidad() : factor;

            suministroTB.setCantidad(cantidad);
            suministroTB.setPrecioVentaGeneral(precio);

            ventaEstructuraController.getTvList().refresh();
            ventaEstructuraController.calculateTotales();
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onSelectPrice();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            onSelectPrice();
        }
    }

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

}
