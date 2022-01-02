package controller.operaciones.compras;

import controller.contactos.proveedores.FxProveedorProcesoController;
import controller.inventario.suministros.FxSuministrosCompraController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.AlmacenADO;
import model.AlmacenTB;
import model.CompraTB;
import model.DetalleCompraTB;
import model.LoteTB;
import model.PrivilegioTB;
import model.ProveedorADO;
import model.ProveedorTB;
import model.SuministroTB;

public class FxComprasController implements Initializable {

    @FXML
    private ScrollPane spWindow;
    @FXML
    private ComboBox<ProveedorTB> cbProveedor;
    @FXML
    private TextField cbNumeracion;
    @FXML
    private DatePicker tpFechaCompra;
    @FXML
    private TableView<DetalleCompraTB> tvList;
    @FXML
    private TableColumn<DetalleCompraTB, Button> tcAccion;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcArticulo;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcCantidad;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcCosto;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcImpuesto;
    @FXML
    private TableColumn<DetalleCompraTB, String> tcImporte;
    @FXML
    private Label lblSubImporteNeto;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblImporteNeto;
    @FXML
    private Button btnArticulo;
    @FXML
    private TextArea txtObservaciones;
    @FXML
    private TextArea txtNotas;
    @FXML
    private HBox hbBotones;
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnQuitar;
    @FXML
    private Button btnProveedor;
    @FXML
    private TextField cbSerie;
    @FXML
    private ComboBox<AlmacenTB> cbAlmacen;

    private FxPrincipalController fxPrincipalController;

    private ObservableList<LoteTB> loteTBs;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    private double subImporteNeto;

    private double impuesto;

    private double importeNeto;

    private boolean loadData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData = false;
        loteTBs = FXCollections.observableArrayList();
        Tools.actualDate(Tools.getDate(), tpFechaCompra);
        initTable();
        filterCbProveedor();
        filterCbAlmacen();
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            hbBotones.getChildren().remove(btnRegistrar);
        }
        if (privilegioTBs.get(1).getIdPrivilegio() != 0 && !privilegioTBs.get(1).isEstado()) {
            hbBotones.getChildren().remove(btnArticulo);
        }
        if (privilegioTBs.get(2).getIdPrivilegio() != 0 && !privilegioTBs.get(2).isEstado()) {
            hbBotones.getChildren().remove(btnEditar);
        }
        if (privilegioTBs.get(3).getIdPrivilegio() != 0 && !privilegioTBs.get(3).isEstado()) {
            hbBotones.getChildren().remove(btnQuitar);
        }
        if (privilegioTBs.get(4).getIdPrivilegio() != 0 && !privilegioTBs.get(4).isEstado()) {
            hbBotones.getChildren().remove(btnProveedor);
        }
        if (privilegioTBs.get(5).getIdPrivilegio() != 0 && !privilegioTBs.get(5).isEstado()) {
            tpFechaCompra.setDisable(true);
        }
        if (privilegioTBs.get(6).getIdPrivilegio() != 0 && !privilegioTBs.get(6).isEstado()) {
            cbSerie.setDisable(true);
        }
        if (privilegioTBs.get(7).getIdPrivilegio() != 0 && !privilegioTBs.get(7).isEstado()) {
            cbNumeracion.setDisable(true);
        }
        if (privilegioTBs.get(8).getIdPrivilegio() != 0 && !privilegioTBs.get(8).isEstado()) {
            // txtProducto.setDisable(true);
        }
        if (privilegioTBs.get(9).getIdPrivilegio() != 0 && !privilegioTBs.get(9).isEstado()) {
            txtObservaciones.setDisable(true);
        }
        if (privilegioTBs.get(10).getIdPrivilegio() != 0 && !privilegioTBs.get(10).isEstado()) {
            txtNotas.setDisable(true);
        }
    }

    private void initTable() {
        tcAccion.setCellValueFactory(new PropertyValueFactory<>("remove"));
        tcArticulo.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()
        ));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getPrecioCompra(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombreImpuesto()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue((cellData.getValue().getPrecioCompra() - cellData.getValue().getDescuento()) * cellData.getValue().getCantidad(), 2)));

        tcAccion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcArticulo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.39));
        tcCosto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
    }

    private void filterCbProveedor() {
        SearchComboBox<ProveedorTB> searchComboBox = new SearchComboBox<>(cbProveedor, false);
        searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBox.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBox.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBox.getComboBox().hide();
            }
        });
        searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBox.getComboBox().getItems().clear();
            List<ProveedorTB> proveedorTBs = ProveedorADO.getSearchComboBoxProveedores(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim());
            proveedorTBs.forEach(p -> cbProveedor.getItems().add(p));
        });
        searchComboBox.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    searchComboBox.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBox.getComboBox().getSelectionModel().select(item);
                if (searchComboBox.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBox.getComboBox().hide();
                }
            }
        });
    }

    private void filterCbAlmacen() {
        searchComboBoxAlmacen = new SearchComboBox<>(cbAlmacen, true);
        searchComboBoxAlmacen.setFilter((item, text) -> item.getNombre().toLowerCase().contains(text.toLowerCase()));
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxAlmacen.getComboBox().hide();
            }
        });
        searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxAlmacen.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxAlmacen.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxAlmacen.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxAlmacen.getComboBox().hide();
                }
            }
        });
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }
    }

    public void setLoadProveedor(String idProveedor) {
        for (ProveedorTB p : cbProveedor.getItems()) {
            if (p.getIdProveedor().equalsIgnoreCase(idProveedor)) {
                cbProveedor.getSelectionModel().select(p);
                break;
            }
        }
    }

    public void clearComponents() {
        cbSerie.clear();
        cbNumeracion.clear();
        Tools.actualDate(Tools.getDate(), tpFechaCompra);
        tvList.getItems().clear();
        loteTBs.clear();
        initTable();
        searchComboBoxAlmacen.getComboBox().getItems().clear();
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }

        lblSubImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + "0.00");
        lblImpuesto.setText(Session.MONEDA_SIMBOLO + " " + "0.00");
        lblImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + "0.00");

        txtObservaciones.clear();
        txtNotas.clear();
        cbProveedor.getItems().clear();
    }

    private void openAlertMessageWarning(String message) {
        fxPrincipalController.openFondoModal();
        Tools.AlertMessageWarning(spWindow, "Compras", message);
        fxPrincipalController.closeFondoModal();
    }

    private void openWindowRegister() {
        if (cbProveedor.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione un proveedor, por favor.");
            cbProveedor.requestFocus();
        } else if (cbSerie.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese la serie del comprobante, por favor.");
            cbSerie.requestFocus();
        } else if (cbNumeracion.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese la numeración del comprobante, por favor.");
            cbNumeracion.requestFocus();
        } else if (tpFechaCompra.getValue() == null) {
            openAlertMessageWarning("Ingrese la fecha de compra, por favor.");
            tpFechaCompra.requestFocus();
        } else if (tvList.getItems().isEmpty()) {
            openAlertMessageWarning("Ingrese algún producto para realizar la compra, por favor.");
            btnArticulo.requestFocus();
        } else if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione un almacen.");
            cbAlmacen.requestFocus();
        } else {
            try {
                CompraTB compraTB = new CompraTB();
                compraTB.setIdProveedor(cbProveedor.getSelectionModel().getSelectedItem().getIdProveedor());
                compraTB.setSerie(cbSerie.getText().trim());
                compraTB.setNumeracion(cbNumeracion.getText().trim());
                compraTB.setIdMoneda(Session.MONEDA_ID);
                compraTB.setFechaCompra(Tools.getDatePicker(tpFechaCompra));
                compraTB.setHoraCompra(Tools.getTime());
                compraTB.setTotal(importeNeto);
                compraTB.setObservaciones(txtObservaciones.getText().trim().isEmpty() ? "" : txtObservaciones.getText().trim());
                compraTB.setNotas(txtNotas.getText().trim().isEmpty() ? "" : txtNotas.getText().trim());
                compraTB.setUsuario(Session.USER_ID);
                compraTB.setIdAlmacen(cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());

                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_COMPRAS_PROCESO);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
//              Controlller here
                FxComprasProcesoController controller = fXMLLoader.getController();
                controller.setInitComprasController(this);
                controller.setLoadProcess(compraTB, tvList, loteTBs);
//
                Stage stage = WindowStage.StageLoaderModal(parent, "Pago de la compra", spWindow.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
                stage.show();
            } catch (IOException ex) {
                System.out.println("Controller compras" + ex.getLocalizedMessage());
            }
        }
    }

    private void openWindowSuministrasAdd() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = WindowStage.class.getClassLoader().getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitComprasController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.getTxtSearch().requestFocus());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller compras" + ex.getLocalizedMessage());
        }
    }

    private void openWindowSuministroEdit() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            ObservableList<DetalleCompraTB> detalleCompraTBs;
            detalleCompraTBs = tvList.getSelectionModel().getSelectedItems();
            detalleCompraTBs.forEach(e -> {
                try {
                    fxPrincipalController.openFondoModal();
                    URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_COMPRA);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxSuministrosCompraController controller = fXMLLoader.getController();
                    controller.setInitComprasController(this);
                    //
                    Stage stage = WindowStage.StageLoaderModal(parent, "Editar suministro", spWindow.getScene().getWindow());
                    stage.setResizable(false);
                    stage.sizeToScene();
                    stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                    stage.show();

                    DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                    detalleCompraTB.setIdSuministro(e.getIdSuministro());
                    //
                    SuministroTB suministrosTB = new SuministroTB();
                    suministrosTB.setClave(e.getSuministroTB().getClave());
                    suministrosTB.setNombreMarca(e.getSuministroTB().getNombreMarca());
                    suministrosTB.setPrecioVentaGeneral(e.getSuministroTB().getPrecioVentaGeneral());
                    suministrosTB.setPrecioMargenGeneral(e.getSuministroTB().getPrecioMargenGeneral());
                    suministrosTB.setPrecioUtilidadGeneral(e.getSuministroTB().getPrecioUtilidadGeneral());
                    suministrosTB.setImpuestoId(e.getSuministroTB().getImpuestoId());
                    suministrosTB.setTipoPrecio(e.getSuministroTB().isTipoPrecio());
                    suministrosTB.setPreciosTBs(e.getSuministroTB().getPreciosTBs());
                    detalleCompraTB.setSuministroTB(suministrosTB);
                    //
                    detalleCompraTB.setCambiarPrecio(e.isCambiarPrecio());
                    detalleCompraTB.setCantidad(e.getCantidad());
                    detalleCompraTB.setPrecioCompra(e.getPrecioCompra());
                    detalleCompraTB.setDescuento(e.getDescuento());
                    detalleCompraTB.setIdImpuesto(e.getIdImpuesto());
                    detalleCompraTB.setDescripcion(e.getDescripcion());
                    controller.setLoadEdit(detalleCompraTB, tvList.getSelectionModel().getSelectedIndex(), loteTBs);
                } catch (IOException ex) {
                    System.out.println(ex.getLocalizedMessage());
                }
            });
        } else {
            openAlertMessageWarning("Seleccione un producto para editarlo.");
        }
    }

    private void onViewRemove() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            ObservableList<DetalleCompraTB> observableList, detalleCompraTBs;
            observableList = tvList.getItems();
            detalleCompraTBs = tvList.getSelectionModel().getSelectedItems();
            detalleCompraTBs.forEach(e -> {
                for (int i = 0; i < loteTBs.size(); i++) {
                    if (loteTBs.get(i).getIdArticulo().equals(e.getSuministroTB().getIdSuministro())) {
                        loteTBs.remove(i);
                        i--;
                    }
                }
                observableList.remove(e);
            });
            for (int i = 0; i < tvList.getItems().size(); i++) {
                tvList.getItems().get(i).setId(i + 1);
            }
            tvList.refresh();
            calculateTotals();
        } else {
            openAlertMessageWarning("Seleccione un producto para removerlo.");
        }
    }

    public void addSuministroToTable(DetalleCompraTB detalleCompraTB) {
        detalleCompraTB.getRemove().setOnAction(e -> {
            tvList.getItems().remove(detalleCompraTB);
            tvList.refresh();
            calculateTotals();
        });
        detalleCompraTB.getRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(detalleCompraTB);
                tvList.refresh();
                calculateTotals();
            }
        });
        tvList.getItems().add(detalleCompraTB);
    }

    public void editSuministroToTable(int index, DetalleCompraTB detalleCompraTB) {
        detalleCompraTB.getRemove().setOnAction(e -> {
            tvList.getItems().remove(detalleCompraTB);
            tvList.refresh();
            calculateTotals();
        });
        detalleCompraTB.getRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(detalleCompraTB);
                tvList.refresh();
                calculateTotals();
            }
        });
        tvList.getItems().set(index, detalleCompraTB);
    }

    private void openWindowProvedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_PROVEEDORES_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxProveedorProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Proveedor", spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.setValueAdd());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowAddProveedor():" + ex.getLocalizedMessage());
        }
    }

    public void calculateTotals() {
        subImporteNeto = 0;
        tvList.getItems().forEach(e -> subImporteNeto += e.getSubImporteNeto() * e.getCantidad());
        lblSubImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(subImporteNeto, 2));

        impuesto = 0;
        tvList.getItems().forEach(e -> impuesto += e.getImpuestoGenerado() * e.getCantidad());
        lblImpuesto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(impuesto, 2));

        importeNeto = 0;
        tvList.getItems().forEach(e -> importeNeto += e.getImporteNeto() * e.getCantidad());
        lblImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeNeto, 2));

    }

    @FXML
    private void onKeyPressedRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowRegister();
        }
    }

    @FXML
    private void onActionRegister(ActionEvent event) {
        openWindowRegister();
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministrasAdd();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        openWindowSuministrasAdd();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministroEdit();
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) {
        openWindowSuministroEdit();
    }

    @FXML
    private void onKeyPressedRemover(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewRemove();
        }
    }

    @FXML
    private void onActionRemover(ActionEvent event) {
        onViewRemove();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            short value = Tools.AlertMessageConfirmation(spWindow, "Compras", "¿Está seguro de limpiar la compra?");
            if (value == 1) {
                clearComponents();
            }
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        short value = Tools.AlertMessageConfirmation(spWindow, "Compras", "¿Está seguro de limpiar la compra?");
        if (value == 1) {
            clearComponents();
        }
    }

    @FXML
    private void onKeyPressedProviders(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProvedores();
        }
    }

    @FXML
    private void onActionProviders(ActionEvent event) {
        openWindowProvedores();
    }

    @FXML
    private void onKeyTypedNumeracion(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
            event.consume();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (!tvList.getItems().isEmpty()) {
                openWindowSuministroEdit();
            }
        }
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case F1:
                    openWindowRegister();
                    break;
                case F2:
                    openWindowSuministrasAdd();
                    break;
                case F3:
                    openWindowSuministroEdit();
                    break;
                case F4:
                    onViewRemove();
                    break;
                case F5:
                    short value = Tools.AlertMessageConfirmation(spWindow, "Compras", "¿Está seguro de cancelar el venta?");
                    if (value == 1) {
                        clearComponents();
                    }
                    break;
                case F6:
                    openWindowProvedores();
                    break;
                case F7:
                    cbProveedor.requestFocus();
                    break;
                case F8:
                    cbAlmacen.requestFocus();
                    break;
            }
        }
    }

    public boolean isLoadData() {
        return loadData;
    }

    public void setLoadData(boolean loadData) {
        this.loadData = loadData;
    }

    public TableView<DetalleCompraTB> getTvList() {
        return tvList;
    }

    public ObservableList<LoteTB> getLoteTBs() {
        return loteTBs;
    }

    public void setContent(FxPrincipalController principalController) {
        this.fxPrincipalController = principalController;
    }

}
