package controller.operaciones.compras;

import controller.contactos.proveedores.FxProveedorProcesoController;
import controller.inventario.suministros.FxSuministrosCompraController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.operaciones.ordencompra.FxOrdenCompraListaController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.AlmacenADO;
import model.AlmacenTB;
import model.CompraADO;
import model.CompraTB;
import model.DetalleADO;
import model.DetalleCompraTB;
import model.DetalleTB;
import model.LoteTB;
import model.OrdenCompraADO;
import model.OrdenCompraTB;
import model.PreciosTB;
import model.PrivilegioTB;
import model.ProveedorADO;
import model.ProveedorTB;
import model.SuministroTB;

public class FxComprasController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private HBox hbBody;
    @FXML
    private ComboBox<ProveedorTB> cbProveedor;
    @FXML
    private TextField cbNumeracion;
    @FXML
    private DatePicker tpFechaCompra;
    @FXML
    private TableView<DetalleCompraTB> tvList;
    @FXML
    private TableColumn<DetalleCompraTB, Button> tcQuitar;
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
    private Label lblImporteBruto;
    @FXML
    private Label lblDescuento;
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
    private Button btnProveedor;
    @FXML
    private TextField cbSerie;
    @FXML
    private ComboBox<AlmacenTB> cbAlmacen;
    @FXML
    private ComboBox<DetalleTB> cbComprobante;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private CheckBox cbActualizarInventario;

    private FxPrincipalController fxPrincipalController;

    private ObservableList<LoteTB> loteTBs;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    private double importeBrutoTotal = 0;

    private double descuentoTotal = 0;

    private double subImporteNetoTotal = 0;

    private double impuestoTotal = 0;

    private double importeNetoTotal = 0;

    private boolean loadData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData = false;
        loteTBs = FXCollections.observableArrayList();

        cbComprobante.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0015", ""));

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
//            hbBotones.getChildren().remove(btnQuitar);
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
        tcQuitar.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcArticulo.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()
        ));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getPrecioCompra(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombre()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue((cellData.getValue().getPrecioCompra() - cellData.getValue().getDescuento()) * cellData.getValue().getCantidad(), 2)));

        tcQuitar.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcArticulo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.39));
        tcCosto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
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
            searchComboBox.getComboBox().getItems().addAll(ProveedorADO.getSearchComboBoxProveedores(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim()));
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
        cbActualizarInventario.setSelected(true);

        searchComboBoxAlmacen.getComboBox().getItems().clear();
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }

        cbComprobante.getItems().clear();
        cbComprobante.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0015", ""));

        calculateTotals();

        txtObservaciones.clear();
        txtNotas.clear();
        cbProveedor.getItems().clear();
    }

    private void openAlertMessageWarning(String message) {
        fxPrincipalController.openFondoModal();
        Tools.AlertMessageWarning(apWindow, "Compras", message);
        fxPrincipalController.closeFondoModal();
    }

    private void openWindowRegister() throws IOException {
        if (cbProveedor.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione un proveedor, por favor.");
            cbProveedor.requestFocus();
        } else if (cbComprobante.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione el comprobante de la compra.");
            cbComprobante.requestFocus();
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
            CompraTB compraTB = new CompraTB();
            compraTB.setIdProveedor(cbProveedor.getSelectionModel().getSelectedItem().getIdProveedor());
            compraTB.setIdComprobante(cbComprobante.getSelectionModel().getSelectedItem().getIdDetalle());
            compraTB.setSerie(cbSerie.getText().trim());
            compraTB.setNumeracion(cbNumeracion.getText().trim());
            compraTB.setIdMoneda(Session.MONEDA_ID);
            compraTB.setFechaCompra(Tools.getDatePicker(tpFechaCompra));
            compraTB.setHoraCompra(Tools.getTime());
            compraTB.setTotal(importeNetoTotal);
            compraTB.setObservaciones(txtObservaciones.getText().trim().isEmpty() ? "" : txtObservaciones.getText().trim());
            compraTB.setNotas(txtNotas.getText().trim().isEmpty() ? "" : txtNotas.getText().trim());
            compraTB.setUsuario(Session.USER_ID);
            compraTB.setIdAlmacen(cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
            compraTB.setActualizarAlmacen(cbActualizarInventario.isSelected());

            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_COMPRAS_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxComprasProcesoController controller = fXMLLoader.getController();
            controller.setInitComprasController(this);
            controller.setLoadProcess(compraTB, tvList, loteTBs);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Completar compra", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
            stage.show();
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
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", apWindow.getScene().getWindow());
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
                    Stage stage = WindowStage.StageLoaderModal(parent, "Editar suministro", apWindow.getScene().getWindow());
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
                    suministrosTB.setIdImpuesto(e.getSuministroTB().getIdImpuesto());
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
            ObservableList<DetalleCompraTB> articuloSelect, allArticulos;
            allArticulos = tvList.getItems();
            articuloSelect = tvList.getSelectionModel().getSelectedItems();
            articuloSelect.forEach(allArticulos::remove);

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
        detalleCompraTB.getBtnRemove().setOnAction(e -> {
            tvList.getItems().remove(detalleCompraTB);
            tvList.refresh();
            calculateTotals();
        });
        detalleCompraTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(detalleCompraTB);
                tvList.refresh();
                calculateTotals();
            }
        });
        tvList.getItems().add(detalleCompraTB);
    }

    public void editSuministroToTable(int index, DetalleCompraTB detalleCompraTB) {
        detalleCompraTB.getBtnRemove().setOnAction(e -> {
            tvList.getItems().remove(detalleCompraTB);
            tvList.refresh();
            calculateTotals();
        });
        detalleCompraTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(detalleCompraTB);
                tvList.refresh();
                calculateTotals();
            }
        });
        tvList.getItems().set(index, detalleCompraTB);
    }

    public void loadCompra(String idCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CompraADO.Obtener_Compra_ById(idCompra);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof CompraTB) {

                CompraTB compraTB = (CompraTB) object;

                cbProveedor.getItems().clear();
                cbProveedor.getItems().add(new ProveedorTB(compraTB.getProveedorTB().getIdProveedor(), compraTB.getProveedorTB().getNumeroDocumento(), compraTB.getProveedorTB().getRazonSocial()));
                cbProveedor.getSelectionModel().select(0);

                for (int i = 0; i < cbComprobante.getItems().size(); i++) {
                    if (cbComprobante.getItems().get(i).getIdDetalle() == compraTB.getIdComprobante()) {
                        cbComprobante.getSelectionModel().select(i);
                    }
                }

                cbSerie.setText(compraTB.getSerie());
                cbNumeracion.setText(compraTB.getNumeracion());

                compraTB.getDetalleCompraTBs().forEach(dctb -> {
                    DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                    detalleCompraTB.setId(tvList.getItems().size() + 1);
                    detalleCompraTB.setIdSuministro(dctb.getIdSuministro());
                    detalleCompraTB.setCambiarPrecio(false);
                    detalleCompraTB.setIdImpuesto(dctb.getIdImpuesto());
                    detalleCompraTB.setDescripcion("");
                    detalleCompraTB.setCantidad(dctb.getCantidad());
                    detalleCompraTB.setDescuento(dctb.getDescuento());
                    detalleCompraTB.setPrecioCompra(dctb.getPrecioCompra());
                    detalleCompraTB.setCambiarPrecio(true);
                    detalleCompraTB.setImpuestoTB(dctb.getImpuestoTB());

                    //SUMINISTRO
                    SuministroTB suministrosTB = new SuministroTB();
                    suministrosTB.setClave(dctb.getSuministroTB().getClave());
                    suministrosTB.setNombreMarca(dctb.getSuministroTB().getNombreMarca());
                    suministrosTB.setPrecioVentaGeneral(dctb.getSuministroTB().getPrecioVentaGeneral());
                    suministrosTB.setIdImpuesto(dctb.getSuministroTB().getIdImpuesto());
                    suministrosTB.setTipoPrecio(dctb.getSuministroTB().isTipoPrecio());

                    if (suministrosTB.isTipoPrecio()) {
                        ArrayList<PreciosTB> tvPreciosNormal = new ArrayList<>();
                        tvPreciosNormal.add(new PreciosTB(0, "PRECIO DE VENTA 1", ((PreciosTB) dctb.getSuministroTB().getPreciosTBs().get(0)).getValor(), 1));
                        tvPreciosNormal.add(new PreciosTB(0, "PRECIO DE VENTA 2", ((PreciosTB) dctb.getSuministroTB().getPreciosTBs().get(1)).getValor(), 1));
                        suministrosTB.setPreciosTBs(tvPreciosNormal);
                    } else {
                        suministrosTB.setPreciosTBs(new ArrayList<>(dctb.getSuministroTB().getPreciosTBs()));
                    }
                    detalleCompraTB.setSuministroTB(suministrosTB);

                    Button btnRemove = new Button("X");
                    btnRemove.getStyleClass().add("buttonDark");
                    btnRemove.setOnAction(e -> {
                        tvList.getItems().remove(detalleCompraTB);
                        tvList.refresh();
                        calculateTotals();
                    });
                    btnRemove.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            tvList.getItems().remove(detalleCompraTB);
                            tvList.refresh();
                            calculateTotals();
                        }
                    });

                    detalleCompraTB.setBtnRemove(btnRemove);

                    tvList.getItems().add(detalleCompraTB);
                });
                calculateTotals();

                txtNotas.setText(compraTB.getNotas());
                txtObservaciones.setText(compraTB.getObservaciones());

                hbBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    clearComponents();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        clearComponents();
                    }
                    event.consume();
                });
            }
        });

        task.setOnFailed(w -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                clearComponents();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    clearComponents();
                }
                event.consume();
            });
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
        });

        task.setOnScheduled(w -> {
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void loadOrdenCompra(String idOrdenCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return OrdenCompraADO.Obtener_Orden_Compra_ById(idOrdenCompra);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof OrdenCompraTB) {

                OrdenCompraTB ordenCompraTB = (OrdenCompraTB) object;

                cbProveedor.getItems().clear();
                cbProveedor.getItems().add(new ProveedorTB(ordenCompraTB.getProveedorTB().getIdProveedor(), ordenCompraTB.getProveedorTB().getNumeroDocumento(), ordenCompraTB.getProveedorTB().getRazonSocial()));
                cbProveedor.getSelectionModel().select(0);

                ordenCompraTB.getOrdenCompraDetalleTBs().forEach(dctb -> {
                    DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                    detalleCompraTB.setId(tvList.getItems().size() + 1);
                    detalleCompraTB.setIdSuministro(dctb.getIdSuministro());
                    detalleCompraTB.setCambiarPrecio(false);
                    detalleCompraTB.setIdImpuesto(dctb.getIdImpuesto());
                    detalleCompraTB.setDescripcion("");
                    detalleCompraTB.setCantidad(dctb.getCantidad());
                    detalleCompraTB.setDescuento(dctb.getDescuento());
                    detalleCompraTB.setPrecioCompra(dctb.getCosto());
                    detalleCompraTB.setCambiarPrecio(true);
                    detalleCompraTB.setImpuestoTB(dctb.getImpuestoTB());

                    //SUMINISTRO
                    SuministroTB suministrosTB = new SuministroTB();
                    suministrosTB.setIdSuministro(dctb.getIdSuministro());
                    suministrosTB.setClave(dctb.getSuministroTB().getClave());
                    suministrosTB.setNombreMarca(dctb.getSuministroTB().getNombreMarca());
                    suministrosTB.setUnidadCompraName(dctb.getSuministroTB().getUnidadCompraName());
                    suministrosTB.setPrecioVentaGeneral(dctb.getSuministroTB().getPrecioVentaGeneral());
                    suministrosTB.setIdImpuesto(dctb.getSuministroTB().getIdImpuesto());
                    suministrosTB.setTipoPrecio(dctb.getSuministroTB().isTipoPrecio());

                    if (suministrosTB.isTipoPrecio()) {
                        ArrayList<PreciosTB> tvPreciosNormal = new ArrayList<>();
                        tvPreciosNormal.add(new PreciosTB(0, "PRECIO DE VENTA 1", ((PreciosTB) dctb.getSuministroTB().getPreciosTBs().get(0)).getValor(), 1));
                        tvPreciosNormal.add(new PreciosTB(0, "PRECIO DE VENTA 2", ((PreciosTB) dctb.getSuministroTB().getPreciosTBs().get(1)).getValor(), 1));
                        suministrosTB.setPreciosTBs(tvPreciosNormal);
                    } else {
                        suministrosTB.setPreciosTBs(new ArrayList<>(dctb.getSuministroTB().getPreciosTBs()));
                    }
                    detalleCompraTB.setSuministroTB(suministrosTB);

                    Button btnRemove = new Button("X");
                    btnRemove.getStyleClass().add("buttonDark");
                    btnRemove.setOnAction(e -> {
                        tvList.getItems().remove(detalleCompraTB);
                        tvList.refresh();
                        calculateTotals();
                    });
                    btnRemove.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            tvList.getItems().remove(detalleCompraTB);
                            tvList.refresh();
                            calculateTotals();
                        }
                    });

                    detalleCompraTB.setBtnRemove(btnRemove);

                    tvList.getItems().add(detalleCompraTB);
                });

                calculateTotals();
                txtObservaciones.setText(ordenCompraTB.getObservacion());

                hbBody.setDisable(false);
                hbLoad.setVisible(false);

            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    clearComponents();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        clearComponents();
                    }
                    event.consume();
                });
            }
        });

        task.setOnFailed(w -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                clearComponents();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    clearComponents();
                }
                event.consume();
            });
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
        });

        task.setOnScheduled(w -> {
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
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
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Proveedor", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.setValueAdd());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowAddProveedor():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowCompras() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_COMPRAS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxComprasListaController controller = fXMLLoader.getController();
            controller.setInitCompras(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Lista de Compras", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowCompras():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowOrdenCompra() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_ORDEN_COMPRA_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOrdenCompraListaController controller = fXMLLoader.getController();
            controller.setInitCompras(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Lista de Compras", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowCompras():" + ex.getLocalizedMessage());
        }
    }

    public void calculateTotals() {
        importeBrutoTotal = 0;
        descuentoTotal = 0;
        subImporteNetoTotal = 0;
        impuestoTotal = 0;
        importeNetoTotal = 0;

        tvList.getItems().forEach(ocdtb -> {
            double importeBruto = ocdtb.getPrecioCompra() * ocdtb.getCantidad();
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
        });

        lblImporteBruto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteNeto.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    public void onEventRecargar() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Compras", "¿Está seguro de cancelar el venta?");
        if (value == 1) {
            clearComponents();
        }
    }

    @FXML
    private void onKeyPressedRegister(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowRegister();
        }
    }

    @FXML
    private void onActionRegister(ActionEvent event) throws IOException {
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

    private void onKeyPressedRemover(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewRemove();
        }
    }

    private void onActionRemover(ActionEvent event) {
        onViewRemove();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRecargar();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        onEventRecargar();
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
    private void onKeyPressedCompras(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCompras();
        }
    }

    @FXML
    private void onActionCompras(ActionEvent event) {
        openWindowCompras();
    }

    @FXML
    private void onKeyPressedOrdenCompra(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowOrdenCompra();
        }
    }

    @FXML
    private void onActionOrdenCompra(ActionEvent event) {
        openWindowOrdenCompra();
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

    private void onKeyReleasedWindow(KeyEvent event) throws IOException {
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
                    openWindowProvedores();
                    break;
                case F5:
                    onEventRecargar();
                    break;
                case F6:

                    break;
                case F7:

                    break;
                case F8:
                    cbProveedor.requestFocus();
                    break;
                case F9:
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
