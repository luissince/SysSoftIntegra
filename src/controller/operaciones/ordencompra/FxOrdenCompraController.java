package controller.operaciones.ordencompra;

import controller.contactos.proveedores.FxProveedorProcesoController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.ClienteTB;
import model.OrdenCompraDetalleTB;
import model.ProveedorADO;
import model.ProveedorTB;

public class FxOrdenCompraController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ComboBox<ProveedorTB> cbProveedor;
    @FXML
    private DatePicker dtFechaEmision;
    @FXML
    private DatePicker dtFechaVencimiento;
    @FXML
    private TableView<OrdenCompraDetalleTB> tvList;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, Button> tcOpcion;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcProducto;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcCantidad;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcCosto;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcImpuesto;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcImporte;
    @FXML
    private TableColumn<OrdenCompraDetalleTB, String> tcObservacion;
    @FXML
    private TextField txtObservacion;
    @FXML
    private Label lblSubImporte;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblValorVenta;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblImporteTotal;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblProceso;

    private FxPrincipalController principalController;

    double importeBrutoTotal;

    double descuentoTotal;

    double subImporteNetoTotal;

    double impuestoTotal;

    double importeNetoTotal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);

        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cellData.getValue().getCosto(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombre()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cellData.getValue().getCantidad() * cellData.getValue().getCosto(), 2)));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservacion()));

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.08));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.24));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcCosto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcObservacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));

        loadComboBoxProveedor();
    }

    private void loadComboBoxProveedor() {
        SearchComboBox<ClienteTB> searchComboBoxCliente = new SearchComboBox<>(cbProveedor, false);
        searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxCliente.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxCliente.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxCliente.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxCliente.getComboBox().hide();
            }
        });
        searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxCliente.getComboBox().getItems().clear();
            cbProveedor.getItems().addAll(ProveedorADO.getSearchComboBoxProveedores(searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().getText().trim()));
        });
        searchComboBoxCliente.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxCliente.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxCliente.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxCliente.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxCliente.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxCliente.getComboBox().hide();
                }
            }
        });
    }

    private void openWindowSuministro() {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitOrdenCompraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.setOnShown(w -> controller.getTxtSearch().requestFocus());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowProveedores() {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_PROVEEDORES_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxProveedorProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Proveedor", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.setOnShown(w -> controller.setValueAdd());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowAddProveedor():" + ex.getLocalizedMessage());
        }
    }

    public void getAddDetalle(OrdenCompraDetalleTB compraDetalleTB) {
        compraDetalleTB.getBtnRemove().setOnAction(e -> {
            tvList.getItems().remove(compraDetalleTB);
            calculateTotales();
        });
        compraDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(compraDetalleTB);
                calculateTotales();
            }
        });

        tvList.getItems().add(compraDetalleTB);
        int index = tvList.getItems().size() - 1;
        tvList.getSelectionModel().select(index);

        calculateTotales();
    }

    public void calculateTotales() {
        importeBrutoTotal = 0;
        descuentoTotal = 0;
        subImporteNetoTotal = 0;
        impuestoTotal = 0;
        importeNetoTotal = 0;

        for (OrdenCompraDetalleTB ocdtb : tvList.getItems()) {
            double importeBruto = ocdtb.getCosto();
            double descuento = ocdtb.getDescuento();
            double subImporteBruto = importeBruto - descuento;
            double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(), subImporteBruto);
            double impuesto = Tools.calculateTax(ocdtb.getImpuestoTB().getValor(), subImporteNeto);
            double importeNeto = subImporteNeto + impuesto;

            importeBrutoTotal += importeBruto;
            descuentoTotal += descuento;
            subImporteNetoTotal += subImporteNeto;
            impuestoTotal += impuesto;
            importeNetoTotal += importeNeto;
        }

        lblValorVenta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporte.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    public boolean validateDuplicate(String idSuministro) {
        boolean ret = false;
        for (int i = 0; i < tvList.getItems().size(); i++) {
            if (tvList.getItems().get(i).getIdSuministro().equals(idSuministro)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @FXML
    private void onKeyPressedProducto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
        }
    }

    @FXML
    private void onActionProducto(ActionEvent event) {
        openWindowSuministro();
    }

    @FXML
    private void onKeyPressedProveedor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProveedores();
        }
    }

    @FXML
    private void onActionProveedor(ActionEvent event) {
        openWindowProveedores();
    }

    public void setContent(FxPrincipalController principalController) {
        this.principalController = principalController;
    }

}
