package controller.posterminal.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.operaciones.cotizacion.FxCotizacionListaController;
import controller.tools.ApiPeru;
import controller.tools.FilesRouters;
import controller.tools.Json;
import controller.tools.ObjectGlobal;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import model.ClienteTB;
import model.CotizacionDetalleTB;
import model.CotizacionTB;
import model.DetalleTB;
import model.MonedaTB;
import model.PrivilegioTB;
import model.SuministroTB;
import model.TipoDocumentoTB;
import model.VentaTB;
import service.ClienteADO;
import service.ComprobanteADO;
import service.CotizacionADO;
import service.DetalleADO;
import service.MonedaADO;
import service.SuministroADO;
import service.TipoDocumentoADO;
import service.VentaADO;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.json.simple.JSONObject;

public class FxPostVentaEstructuraController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private HBox hbBotonesSuperior;
    @FXML
    private Button btnCobrar;
    @FXML
    private Button btnArticulo;
    @FXML
    private Button btnListaPrecio;
    @FXML
    private Button btnCantidad;
    @FXML
    private Button btnCambiarPrecio;
    @FXML
    private Button btnDescuento;
    @FXML
    private Button btnSumarPrecio;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, Button> tcOpcion;
    @FXML
    private TableColumn<SuministroTB, String> tcCantidad;
    @FXML
    private TableColumn<SuministroTB, String> tcArticulo;
    @FXML
    private TableColumn<SuministroTB, String> tcImpuesto;
    @FXML
    private TableColumn<SuministroTB, String> tcPrecio;
    @FXML
    private TableColumn<SuministroTB, String> tcDescuento;
    @FXML
    private TableColumn<SuministroTB, String> tcImporte;
    @FXML
    private Text lblTotal;
    @FXML
    private Text lblSerie;
    @FXML
    private Text lblNumeracion;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private ComboBox<TipoDocumentoTB> cbComprobante;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtDatosCliente;
    @FXML
    private TextField txtDireccionCliente;
    @FXML
    private TextField txtCelularCliente;
    @FXML
    private Label lblImporteBruto;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubImporte;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblImporteTotal;
    @FXML
    private Button btnMovimiento;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnVentasPorDia;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtCorreoElectronico;
    @FXML
    private HBox hbLoadCliente;
    @FXML
    private VBox vbBodyCliente;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private VBox vbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Button btnPrintTiket;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private AutoCompletionBinding<String> autoCompletionBindingDocumento;

    private AutoCompletionBinding<ClienteTB> autoCompletionBindingInfo;

    private String monedaSimbolo;

    private String idCliente;

    private String idCotizacion;

    private final Set<String> posiblesWordDocumento = new HashSet<>();

    private final Set<ClienteTB> posiblesWordInfo = new HashSet<>();

    private boolean unidades_cambio_cantidades;

    private boolean unidades_cambio_precio;

    private boolean unidades_cambio_descuento;

    private boolean valormonetario_cambio_cantidades;

    private boolean valormonetario_cambio_precio;

    private boolean valormonetario_cambio_decuento;

    private boolean medida_cambio_cantidades;

    private boolean medida_cambio_precio;

    private boolean medida_cambio_decuento;

    private boolean vender_con_cantidades_negativas;

    private boolean cerar_modal_agregar_item_lista;

    private boolean stateSearch;

    private double importeBrutoTotal;

    private double descuentoBrutoTotal;

    private double subImporteNetoTotal;

    private double impuestoNetoTotal;

    private double importeNetoTotal;

    private boolean mostrarUltimasVentas = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        monedaSimbolo = "M";
        idCliente = "";
        stateSearch = false;
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();

        initTable();
        loadDataComponent();
        autoCompletionBindingDocumento = TextFields.bindAutoCompletion(txtNumeroDocumento, posiblesWordDocumento);
        autoCompletionBindingDocumento.setOnAutoCompleted(e -> {
            if (!Tools.isText(txtNumeroDocumento.getText())) {
                onExecuteCliente((short) 2, txtNumeroDocumento.getText().trim());
                btnArticulo.requestFocus();
            }
        });

        autoCompletionBindingInfo = TextFields.bindAutoCompletion(txtDatosCliente, posiblesWordInfo);
        autoCompletionBindingInfo.setOnAutoCompleted(e -> {
            if (!Tools.isText(e.getCompletion().getNumeroDocumento())) {
                onExecuteCliente((short) 2, e.getCompletion().getNumeroDocumento());
                btnArticulo.requestFocus();
            }
        });
    }

    private void loadDataComponent() {
        cbComprobante.getItems().clear();
        cbComprobante.getItems().addAll(TipoDocumentoADO.obtenerComprobantesParaVenta());

        if (!cbComprobante.getItems().isEmpty()) {
            for (int i = 0; i < cbComprobante.getItems().size(); i++) {
                if (cbComprobante.getItems().get(i).isPredeterminado()) {
                    cbComprobante.getSelectionModel().select(i);
                    break;
                }
            }

            if (cbComprobante.getSelectionModel().getSelectedIndex() >= 0) {
                String[] array = ComprobanteADO.GetSerieNumeracionEspecifico(
                        cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento()).split("-");
                lblSerie.setText(array[0]);
                lblNumeracion.setText(array[1]);
            }
        }

        cbMoneda.getItems().clear();
        Object monedaObject = MonedaADO.ObtenerListaMonedas();
        if (monedaObject instanceof ObservableList) {
            cbMoneda.setItems((ObservableList<MonedaTB>) monedaObject);
        }

        if (!cbMoneda.getItems().isEmpty()) {
            for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                if (cbMoneda.getItems().get(i).isPredeterminado()) {
                    cbMoneda.getSelectionModel().select(i);
                    monedaSimbolo = cbMoneda.getItems().get(i).getSimbolo();
                    break;
                }
            }
        }

        cbTipoDocumento.getItems().clear();
        cbTipoDocumento.getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0003"));

        idCotizacion = "";

        idCliente = Session.CLIENTE_ID;
        txtNumeroDocumento.setText(Session.CLIENTE_NUMERO_DOCUMENTO);
        txtDatosCliente.setText(Session.CLIENTE_DATOS);
        txtCelularCliente.setText(Session.CLIENTE_CELULAR);
        txtCorreoElectronico.setText(Session.CLIENTE_EMAIL);
        txtDireccionCliente.setText(Session.CLIENTE_DIRECCION);

        ObjectGlobal.DATA_CLIENTS.forEach(c -> posiblesWordDocumento.add(c));

        ObjectGlobal.DATA_INFO_CLIENTS.forEach(c -> posiblesWordInfo.add(c));

        if (!cbTipoDocumento.getItems().isEmpty()) {
            for (DetalleTB detalleTB : cbTipoDocumento.getItems()) {
                if (detalleTB.getIdDetalle() == Session.CLIENTE_TIPO_DOCUMENTO) {
                    cbTipoDocumento.getSelectionModel().select(detalleTB);
                    break;
                }
            }
        }
    }

    private void initTable() {
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcArticulo.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getClave() + " - " + cellData.getValue().getUnidadCompraName() + "\n"
                + cellData.getValue().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcPrecio.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getPrecioVentaGeneral(), 2)));
        tcDescuento.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getDescuento(), 2)));
        tcImpuesto.setCellValueFactory(
                cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombreImpuesto()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(
                        cellData.getValue().getCantidad()
                        * (cellData.getValue().getPrecioVentaGeneral() - cellData.getValue().getDescuento()),
                        2)));
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        if (privilegioTBs.get(1).getIdPrivilegio() != 0 && !privilegioTBs.get(1).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnCobrar);
        }
        if (privilegioTBs.get(2).getIdPrivilegio() != 0 && !privilegioTBs.get(2).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnArticulo);
        }
        if (privilegioTBs.get(3).getIdPrivilegio() != 0 && !privilegioTBs.get(3).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnListaPrecio);
        }
        if (privilegioTBs.get(4).getIdPrivilegio() != 0 && !privilegioTBs.get(4).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnCantidad);
        }
        if (privilegioTBs.get(5).getIdPrivilegio() != 0 && !privilegioTBs.get(5).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnCambiarPrecio);
        }
        if (privilegioTBs.get(6).getIdPrivilegio() != 0 && !privilegioTBs.get(6).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnDescuento);
        }
        if (privilegioTBs.get(7).getIdPrivilegio() != 0 && !privilegioTBs.get(7).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnSumarPrecio);
        }
        if (privilegioTBs.get(8).getIdPrivilegio() != 0 && !privilegioTBs.get(8).isEstado()) {
            // hbBotonesInferior.getChildren().remove(btnQuitar);
        }
        if (privilegioTBs.get(9).getIdPrivilegio() != 0 && !privilegioTBs.get(9).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnMovimiento);
        }
        if (privilegioTBs.get(10).getIdPrivilegio() != 0 && !privilegioTBs.get(10).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnPrintTiket);
        }
        if (privilegioTBs.get(11).getIdPrivilegio() != 0 && !privilegioTBs.get(11).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnCancelar);
        }
        if (privilegioTBs.get(12).getIdPrivilegio() != 0 && !privilegioTBs.get(12).isEstado()) {
            // btnCliente.setDisable(true);
        }
        if (privilegioTBs.get(13).getIdPrivilegio() != 0 && !privilegioTBs.get(13).isEstado()) {
            cbComprobante.setDisable(true);
        }
        if (privilegioTBs.get(14).getIdPrivilegio() != 0 && !privilegioTBs.get(14).isEstado()) {
            cbMoneda.setDisable(true);
        }
        if (privilegioTBs.get(15).getIdPrivilegio() != 0 && !privilegioTBs.get(15).isEstado()) {
            hbBotonesSuperior.getChildren().remove(btnVentasPorDia);
        }
        if (privilegioTBs.get(16).getIdPrivilegio() != 0 && !privilegioTBs.get(16).isEstado()) {

        } else {
            tcCantidad.setCellFactory(TextFieldTableCell.forTableColumn());

            tcCantidad.setOnEditCommit(data -> {
                final Double value = Tools.isNumeric(data.getNewValue())
                        ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue())
                        : Double.parseDouble(data.getNewValue()))
                        : Double.parseDouble(data.getOldValue());
                SuministroTB suministroTB = data.getTableView().getItems().get(data.getTablePosition().getRow());

                if (unidades_cambio_cantidades && suministroTB.getUnidadVenta() == 1
                        || valormonetario_cambio_cantidades && suministroTB.getUnidadVenta() == 2
                        || medida_cambio_cantidades && suministroTB.getUnidadVenta() == 3) {
                    suministroTB.setCantidad(value);
                    tvList.refresh();
                    calculateTotales();
                } else {
                    suministroTB.setCantidad(Double.parseDouble(data.getOldValue()));
                    tvList.refresh();
                    calculateTotales();
                }
            });
        }
        if (privilegioTBs.get(17).getIdPrivilegio() != 0 && !privilegioTBs.get(17).isEstado()) {

        } else {
            tcPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
            tcPrecio.setOnEditCommit(data -> {
                final Double value = Tools.isNumeric(data.getNewValue())
                        ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue())
                        : Double.parseDouble(data.getNewValue()))
                        : Double.parseDouble(data.getOldValue());
                SuministroTB suministroTB = data.getTableView().getItems().get(data.getTablePosition().getRow());

                if (!unidades_cambio_precio && suministroTB.getUnidadVenta() == 1
                        || !valormonetario_cambio_precio && suministroTB.getUnidadVenta() == 2
                        || !medida_cambio_precio && suministroTB.getUnidadVenta() == 3) {
                    tvList.refresh();
                    calculateTotales();
                } else {
                    suministroTB.setPrecioVentaGeneral(value);
                    tvList.refresh();
                    calculateTotales();
                }
            });
        }
        unidades_cambio_cantidades = !(privilegioTBs.get(18).getIdPrivilegio() != 0
                && !privilegioTBs.get(18).isEstado());
        unidades_cambio_precio = !(privilegioTBs.get(19).getIdPrivilegio() != 0 && !privilegioTBs.get(19).isEstado());
        unidades_cambio_descuento = !(privilegioTBs.get(20).getIdPrivilegio() != 0
                && !privilegioTBs.get(20).isEstado());

        valormonetario_cambio_cantidades = !(privilegioTBs.get(21).getIdPrivilegio() != 0
                && !privilegioTBs.get(21).isEstado());
        valormonetario_cambio_precio = !(privilegioTBs.get(22).getIdPrivilegio() != 0
                && !privilegioTBs.get(22).isEstado());
        valormonetario_cambio_decuento = !(privilegioTBs.get(23).getIdPrivilegio() != 0
                && !privilegioTBs.get(23).isEstado());

        medida_cambio_cantidades = !(privilegioTBs.get(24).getIdPrivilegio() != 0 && !privilegioTBs.get(24).isEstado());
        medida_cambio_precio = !(privilegioTBs.get(25).getIdPrivilegio() != 0 && !privilegioTBs.get(25).isEstado());
        medida_cambio_decuento = !(privilegioTBs.get(26).getIdPrivilegio() != 0 && !privilegioTBs.get(26).isEstado());

        tcOpcion.setVisible(!(privilegioTBs.get(27).getIdPrivilegio() != 0 && !privilegioTBs.get(27).isEstado()));
        tcCantidad.setVisible(!(privilegioTBs.get(28).getIdPrivilegio() != 0 && !privilegioTBs.get(28).isEstado()));
        tcArticulo.setVisible(!(privilegioTBs.get(29).getIdPrivilegio() != 0 && !privilegioTBs.get(29).isEstado()));
        tcImpuesto.setVisible(!(privilegioTBs.get(30).getIdPrivilegio() != 0 && !privilegioTBs.get(30).isEstado()));
        tcPrecio.setVisible(!(privilegioTBs.get(31).getIdPrivilegio() != 0 && !privilegioTBs.get(31).isEstado()));
        tcDescuento.setVisible(!(privilegioTBs.get(32).getIdPrivilegio() != 0 && !privilegioTBs.get(32).isEstado()));
        tcImporte.setVisible(!(privilegioTBs.get(33).getIdPrivilegio() != 0 && !privilegioTBs.get(33).isEstado()));

        vender_con_cantidades_negativas = privilegioTBs.get(34).getIdPrivilegio() != 0
                && !privilegioTBs.get(34).isEstado();

        cerar_modal_agregar_item_lista = privilegioTBs.get(35).getIdPrivilegio() != 0
                && !privilegioTBs.get(35).isEstado();

        if (privilegioTBs.get(36).getIdPrivilegio() != 0 && !privilegioTBs.get(36).isEstado()) {
            mostrarUltimasVentas = true;
        }

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcArticulo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.30));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcPrecio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcDescuento.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    private void filterSuministro(String search) {
        SuministroTB a = SuministroADO.Get_Suministro_By_Search(search);
        if (a != null) {
            if (vender_con_cantidades_negativas && a.getCantidad() <= 0) {
                Tools.AlertMessageWarning(window, "Venta",
                        "No puede agregar el producto ya que tiene la cantidad <= 0.");
                txtSearch.clear();
                txtSearch.selectAll();
                txtSearch.requestFocus();
                return;
            }
            SuministroTB suministroTB = new SuministroTB();
            suministroTB.setIdSuministro(a.getIdSuministro());
            suministroTB.setClave(a.getClave());
            suministroTB.setNombreMarca(a.getNombreMarca());
            suministroTB.setCantidad(1);
            suministroTB.setCostoCompra(a.getCostoCompra());
            suministroTB.setBonificacion(0);
            suministroTB.setUnidadCompra(a.getUnidadCompra());
            suministroTB.setUnidadCompraName(a.getUnidadCompraName());

            suministroTB.setDescuento(0);
            suministroTB.setDescuentoCalculado(0);
            suministroTB.setDescuentoSumado(0);

            suministroTB.setPrecioVentaGeneral(a.getPrecioVentaGeneral());
            suministroTB.setPrecioVentaGeneralUnico(a.getPrecioVentaGeneral());
            suministroTB.setPrecioVentaGeneralReal(a.getPrecioVentaGeneral());
            suministroTB.setPrecioVentaGeneralAuxiliar(suministroTB.getPrecioVentaGeneral());

            suministroTB.setIdImpuesto(a.getIdImpuesto());
            suministroTB.setImpuestoTB(a.getImpuestoTB());

            suministroTB.setInventario(a.isInventario());
            suministroTB.setUnidadVenta(a.getUnidadVenta());
            suministroTB.setValorInventario(a.getValorInventario());

            Button button = new Button("X");
            button.getStyleClass().add("buttonDark");
            button.setOnAction(b -> {
                tvList.getItems().remove(suministroTB);
                calculateTotales();
            });
            button.setOnKeyPressed(b -> {
                if (b.getCode() == KeyCode.ENTER) {
                    tvList.getItems().remove(suministroTB);
                    calculateTotales();
                }
            });
            suministroTB.setBtnRemove(button);

            getSuministroCodigoBarras(suministroTB);
            txtSearch.clear();
            txtSearch.selectAll();
            txtSearch.requestFocus();
        } else {
            txtSearch.clear();
            txtSearch.selectAll();
            txtSearch.requestFocus();
        }
    }

    private void openWindowSuministro() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitPostVentaEstructuraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            fxPrincipalController.closeFondoModal();
        }
    }

    private void openWindowListaPrecios() {
        try {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_POS_LISTA_PRECIOS);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                // Controlller here
                FxPostListaPreciosController controller = fXMLLoader.getController();
                controller.setInitVentaEstructuraController(this);
                controller.loadDataView(tvList.getSelectionModel().getSelectedItem());
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Lista de precios", window.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> {
                    fxPrincipalController.closeFondoModal();
                    calculateTotales();
                });
                stage.show();

            } else {
                tvList.requestFocus();
            }
        } catch (IOException ex) {
            System.out.println("openWindowListaPrecios():" + ex.getLocalizedMessage());
        }
    }

    public void openWindowCantidad(boolean isClose, Window window, boolean primerLlamado) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (!unidades_cambio_cantidades && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 1
                    || !valormonetario_cambio_cantidades
                    && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 2
                    || !medida_cambio_cantidades
                    && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 3) {
                Tools.AlertMessageWarning(window.getScene().getRoot(), "Ventas",
                        "No se puede cambiar la cantidad a este producto.");
            } else {
                try {
                    if (isClose) {
                        fxPrincipalController.openFondoModal();
                    }
                    URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_CANTIDADES);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    // Controlller here
                    FxPostVentaCantidadesController controller = fXMLLoader.getController();
                    controller.setInitVentaEstructuraController(this);
                    controller.initComponents(tvList.getSelectionModel().getSelectedItem(), false, primerLlamado);

                    //
                    Stage stage = WindowStage.StageLoaderModal(parent, "Modificar cantidades", window);
                    stage.setResizable(false);
                    stage.sizeToScene();
                    stage.setOnShown(w -> {
                        controller.getTxtCantidad().requestFocus();
                    });
                    stage.setOnHiding(w -> {
                        if (isClose) {
                            fxPrincipalController.closeFondoModal();
                        }
                    });
                    stage.show();
                } catch (IOException ex) {
                }
            }
        } else {
            tvList.requestFocus();
            if (!tvList.getItems().isEmpty()) {
                tvList.getSelectionModel().select(0);
            }
        }

    }

    private void openWindowCambiarPrecio(String title, boolean opcion, boolean isClose, Window window) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (!unidades_cambio_precio && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 1
                    || !valormonetario_cambio_precio
                    && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 2
                    || !medida_cambio_precio
                    && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 3) {
                Tools.AlertMessageWarning(window.getScene().getRoot(), "Ventas",
                        "No se puede cambiar precio a este producto.");
            } else {
                try {
                    if (isClose) {
                        fxPrincipalController.openFondoModal();
                    }
                    URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_GRANEL);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    // Controlller here
                    FxPostVentaGranelController controller = fXMLLoader.getController();
                    controller.setInitVentaEstructuraController(this);
                    //
                    Stage stage = WindowStage.StageLoaderModal(parent, title, window.getScene().getWindow());
                    stage.setResizable(false);
                    stage.sizeToScene();
                    stage.setOnHiding(w -> {
                        if (isClose) {
                            fxPrincipalController.closeFondoModal();
                        }
                        calculateTotales();
                    });
                    stage.show();
                    controller.initComponents(title, tvList.getSelectionModel().getSelectedItem(), opcion);
                } catch (IOException ie) {
                    System.out.println("openWindowGranel():" + ie.getLocalizedMessage());
                }
            }
        } else {
            tvList.requestFocus();
        }

    }

    private void openWindowDescuento() {
        try {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                if (!unidades_cambio_descuento && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 1
                        || !valormonetario_cambio_decuento
                        && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 2
                        || !medida_cambio_decuento
                        && tvList.getSelectionModel().getSelectedItem().getValorInventario() == 3) {
                    Tools.AlertMessageWarning(window, "Ventas", "No se puede aplicar descuento a este producto.");
                } else {
                    fxPrincipalController.openFondoModal();
                    URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_DESCUENTO);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    // Controlller here
                    FxPostVentaDescuentoController controller = fXMLLoader.getController();
                    controller.setInitVentaEstructuraController(this);
                    //
                    Stage stage = WindowStage.StageLoaderModal(parent, "Descuento del Producto",
                            window.getScene().getWindow());
                    stage.setResizable(false);
                    stage.sizeToScene();
                    stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                    stage.show();
                    controller.initComponents(tvList.getSelectionModel().getSelectedItem());
                }
            } else {
                tvList.requestFocus();
            }
        } catch (IOException ex) {
            System.out.println("openWindowDescuento():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowCashMovement() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_MOVIMIENTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxPostVentaMovimientoController controller = fXMLLoader.getController();
            // controller.setInitVentaEstructuraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Movimiento de caja", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowCashMovement():" + ex.getLocalizedMessage());
        }

    }

    public void openWindowMostrarVentas() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_MOSTRAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxPostVentaMostrarController controller = fXMLLoader.getController();
            controller.setInitControllerVentaEstructura(this);
            controller.setMostrarUltimasVentas(mostrarUltimasVentas);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar ventas", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> {
                controller.getTxtSearch().requestFocus();
            });
            stage.show();

        } catch (IOException ex) {
        }
    }

    public void openWindowCotizaciones() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_COTIZACION_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxCotizacionListaController controller = fXMLLoader.getController();
            controller.setInitPostVentaEstructuraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar Cotizaciones", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.initLoad());
            stage.show();
        } catch (IOException ex) {
        }
    }

    private void openWindowVentaProceso() {
        try {
            if (tvList.getItems().isEmpty()) {
                Tools.AlertMessageWarning(window, "Ventas", "Debes agregar artículos a la venta");
            } else if (cbComprobante.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(window, "Ventas", "Seleccione el tipo de comprobante");
                cbComprobante.requestFocus();
            } else if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(window, "Ventas", "Seleccione el tipo de documento del cliente.");
                cbTipoDocumento.requestFocus();
            } else if (!Tools.isNumeric(txtNumeroDocumento.getText().trim())) {
                Tools.AlertMessageWarning(window, "Ventas", "Ingrese el número del documento del cliente.");
                txtNumeroDocumento.requestFocus();
            } else if (cbComprobante.getSelectionModel().getSelectedItem().isCampo() && txtNumeroDocumento.getText()
                    .length() != cbComprobante.getSelectionModel().getSelectedItem().getNumeroCampo()) {
                Tools.AlertMessageWarning(window, "Ventas", "El número de documento tiene que tener "
                        + cbComprobante.getSelectionModel().getSelectedItem().getNumeroCampo() + " caracteres.");
                txtNumeroDocumento.requestFocus();
            } else if (txtDatosCliente.getText().trim().equalsIgnoreCase("")) {
                Tools.AlertMessageWarning(window, "Ventas", "Ingrese los datos del cliente.");
                txtDatosCliente.requestFocus();
            } else if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(window, "Ventas", "Seleccione un moneda.");
                cbMoneda.requestFocus();
            } else if (importeNetoTotal <= 0) {
                Tools.AlertMessageWarning(window, "Ventas", "El total de la venta no puede ser menor que 0.");
            } else {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setVendedor(Session.USER_ID);
                ventaTB.setIdComprobante(cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento());

                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setNombre(cbComprobante.getSelectionModel().getSelectedItem().getNombre());
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

                ventaTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedIndex() >= 0
                        ? cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda()
                        : 0);
                ventaTB.setSerie(lblSerie.getText());
                ventaTB.setNumeracion(lblNumeracion.getText());
                ventaTB.setFechaVenta(Tools.getDate());
                ventaTB.setHoraVenta(Tools.getTime());
                ventaTB.setTotal(importeNetoTotal);
                ventaTB.setIdCotizacion(idCotizacion);

                ventaTB.setMonedaTB(cbMoneda.getSelectionModel().getSelectedItem());

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(idCliente);
                clienteTB.setTipoDocumento(cbTipoDocumento.getSelectionModel().getSelectedItem().getIdDetalle());
                clienteTB.setNumeroDocumento(txtNumeroDocumento.getText().trim().toUpperCase());
                clienteTB.setInformacion(txtDatosCliente.getText().trim().toUpperCase());
                clienteTB.setCelular(txtCelularCliente.getText().trim().toUpperCase());
                clienteTB.setEmail(txtCorreoElectronico.getText().trim().toUpperCase());
                clienteTB.setDireccion(txtDireccionCliente.getText().trim().toUpperCase());
                ventaTB.setClienteTB(clienteTB);

                ventaTB.setSuministroTBs(new ArrayList<>(tvList.getItems()));

                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_PROCESO);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                // Controlller here
                FxPostVentaProcesoController controller = fXMLLoader.getController();
                controller.setInitVentaEstructuraController(this);
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Completar la venta", window.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                stage.setOnShowing(w -> controller.setInitComponents(ventaTB, vender_con_cantidades_negativas));
                stage.show();
            }
        } catch (IOException ex) {
            System.out.println("openWindowVentaProceso():" + ex.getLocalizedMessage());
        }
    }

    public void getAddSuministro(SuministroTB suministro, Window window) {
        if (validateDuplicate(suministro)) {
            for (int i = 0; i < tvList.getItems().size(); i++) {
                if (tvList.getItems().get(i).getIdSuministro().equalsIgnoreCase(suministro.getIdSuministro())) {
                    switch (suministro.getUnidadVenta()) {
                        case 3:
                            tvList.requestFocus();
                            tvList.getSelectionModel().select(i);
                            openWindowCantidad(cerar_modal_agregar_item_lista, window, false);
                            break;
                        case 2:
                            tvList.requestFocus();
                            tvList.getSelectionModel().select(i);
                            openWindowCambiarPrecio("Cambiar precio del Producto", false,
                                    cerar_modal_agregar_item_lista, window);
                            break;
                        default:
                            SuministroTB suministroTB = tvList.getItems().get(i);
                            suministroTB.setCantidad(suministroTB.getCantidad() + 1);

                            tvList.refresh();
                            tvList.getSelectionModel().select(i);
                            calculateTotales();
                            break;
                    }
                    break;
                }
            }
        } else {
            switch (suministro.getUnidadVenta()) {
                case 3: {
                    tvList.getItems().add(suministro);
                    int index = tvList.getItems().size() - 1;
                    tvList.requestFocus();
                    tvList.getSelectionModel().select(index);
                    openWindowCantidad(cerar_modal_agregar_item_lista, window, true);
                    calculateTotales();
                    break;
                }
                case 2: {
                    tvList.getItems().add(suministro);
                    int index = tvList.getItems().size() - 1;
                    tvList.requestFocus();
                    tvList.getSelectionModel().select(index);
                    openWindowCambiarPrecio("Cambiar precio al Artículo", false, cerar_modal_agregar_item_lista,
                            window);
                    calculateTotales();
                    break;
                }
                default: {
                    tvList.getItems().add(suministro);
                    int index = tvList.getItems().size() - 1;
                    tvList.getSelectionModel().select(index);
                    calculateTotales();
                    txtSearch.requestFocus();
                    break;
                }
            }
        }

    }

    public void getSuministroCodigoBarras(SuministroTB suministro) {
        if (validateDuplicate(suministro)) {
            for (int i = 0; i < tvList.getItems().size(); i++) {
                if (tvList.getItems().get(i).getIdSuministro().equalsIgnoreCase(suministro.getIdSuministro())) {

                    SuministroTB suministroTB = tvList.getItems().get(i);
                    suministroTB.setCantidad(suministroTB.getCantidad() + 1);
                    tvList.refresh();
                    tvList.getSelectionModel().select(i);
                    calculateTotales();
                    break;

                }
            }
        } else {
            tvList.getItems().add(suministro);
            int index = tvList.getItems().size() - 1;
            tvList.getSelectionModel().select(index);
            calculateTotales();
            txtSearch.requestFocus();
        }

    }

    private boolean validateDuplicate(SuministroTB suministroTB) {
        boolean ret = false;
        for (SuministroTB sm : tvList.getItems()) {
            if (sm.getClave().equals(suministroTB.getClave())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void cancelarVenta() {
        short value = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de limpiar la venta?");
        if (value == 1) {
            resetVenta();
        }
    }

    public void calculateTotales() {
        importeBrutoTotal = 0;
        descuentoBrutoTotal = 0;
        subImporteNetoTotal = 0;
        impuestoNetoTotal = 0;
        importeNetoTotal = 0;

        tvList.getItems().forEach(suministroTB -> {
            double importeBruto = suministroTB.getPrecioVentaGeneral() * suministroTB.getCantidad();
            double descuento = suministroTB.getDescuento();
            double subImporteBruto = importeBruto - descuento;
            double subImporteNeto = Tools.calculateTaxBruto(suministroTB.getImpuestoTB().getValor(), subImporteBruto);
            double impuesto = Tools.calculateTax(suministroTB.getImpuestoTB().getValor(), subImporteNeto);
            double importeNeto = subImporteNeto + impuesto;

            importeBrutoTotal += importeBruto;
            descuentoBrutoTotal += descuento;
            subImporteNetoTotal += subImporteNeto;
            impuestoNetoTotal += impuesto;
            importeNetoTotal += importeNeto;
        });

        lblImporteBruto.setText(monedaSimbolo + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(monedaSimbolo + " " + (Tools.roundingValue(descuentoBrutoTotal, 2)));
        lblSubImporte.setText(monedaSimbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(monedaSimbolo + " " + Tools.roundingValue(impuestoNetoTotal, 2));

        lblImporteTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
        lblTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    public void resetVenta() {
        tvList.getItems().clear();
        loadDataComponent();
        calculateTotales();
        txtSearch.requestFocus();
    }

    public void loadCotizacion(String idCotizacion) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<CotizacionTB> task = new Task<CotizacionTB>() {
            @Override
            public CotizacionTB call() throws Exception {
                Object result = CotizacionADO.Obtener_Cotizacion_ById(idCotizacion, true);
                if (result instanceof CotizacionTB) {
                    return (CotizacionTB) result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(w -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });

        task.setOnFailed(w -> {
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });

        task.setOnSucceeded(w -> {
            CotizacionTB cotizacionTB = task.getValue();
            for (DetalleTB detalleTB : cbTipoDocumento.getItems()) {
                if (detalleTB.getIdDetalle() == cotizacionTB.getClienteTB().getTipoDocumento()) {
                    cbTipoDocumento.getSelectionModel().select(detalleTB);
                    break;
                }
            }

            for (MonedaTB monedaTB : cbMoneda.getItems()) {
                if (monedaTB.getIdMoneda() == cotizacionTB.getIdMoneda()) {
                    cbMoneda.getSelectionModel().select(monedaTB);
                    monedaSimbolo = cbMoneda.getSelectionModel().getSelectedItem().getSimbolo();
                    break;
                }
            }

            this.idCotizacion = idCotizacion;

            txtNumeroDocumento.setText(cotizacionTB.getClienteTB().getNumeroDocumento());
            txtDatosCliente.setText(cotizacionTB.getClienteTB().getInformacion());
            txtCelularCliente.setText(cotizacionTB.getClienteTB().getCelular());
            txtCorreoElectronico.setText(cotizacionTB.getClienteTB().getEmail());
            txtDireccionCliente.setText(cotizacionTB.getClienteTB().getDireccion());

            ObservableList<SuministroTB> suministroTBs = FXCollections.observableArrayList();
            cotizacionTB.getCotizacionDetalleTBs().forEach(detalleTB -> {
                CotizacionDetalleTB cdtb = detalleTB;

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(cdtb.getSuministroTB().getIdSuministro());
                suministroTB.setClave(cdtb.getSuministroTB().getClave());
                suministroTB.setNombreMarca(cdtb.getSuministroTB().getNombreMarca());
                suministroTB.setCantidad(cdtb.getCantidad());
                suministroTB.setCostoCompra(cdtb.getSuministroTB().getCostoCompra());
                suministroTB.setBonificacion(0);
                suministroTB.setUnidadCompra(cdtb.getSuministroTB().getUnidadCompra());
                suministroTB.setUnidadCompraName(cdtb.getSuministroTB().getUnidadCompraName());

                suministroTB.setDescuento(cdtb.getDescuento());
                suministroTB.setDescuentoCalculado(cdtb.getDescuento());
                suministroTB.setDescuentoSumado(cdtb.getDescuento());

                suministroTB.setPrecioVentaGeneral(cdtb.getPrecio());
                suministroTB.setPrecioVentaGeneralUnico(cdtb.getPrecio());
                suministroTB.setPrecioVentaGeneralReal(cdtb.getPrecio());

                suministroTB.setIdImpuesto(cdtb.getIdImpuesto());
                suministroTB.setImpuestoTB(cdtb.getImpuestoTB());

                suministroTB.setInventario(cdtb.getSuministroTB().isInventario());
                suministroTB.setUnidadVenta(cdtb.getSuministroTB().getUnidadVenta());
                suministroTB.setValorInventario(cdtb.getSuministroTB().getValorInventario());
                suministroTB.setUso(cdtb.isUso());

                Button button = new Button("X");
                button.getStyleClass().add("buttonDark");
                button.setOnAction(e -> {
                    tvList.getItems().remove(suministroTB);
                    calculateTotales();
                });
                button.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        tvList.getItems().remove(suministroTB);
                        calculateTotales();
                    }
                });
                suministroTB.setBtnRemove(button);

                suministroTBs.add(suministroTB);
            });

            tvList.setItems(suministroTBs);
            calculateTotales();

            vbBody.setDisable(false);
            hbLoad.setVisible(false);

        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void loadAddVenta(String idVenta) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<VentaTB> task = new Task<VentaTB>() {
            @Override
            public VentaTB call() throws Exception {
                Object result = VentaADO.obtenerVentaPorIdVenta(idVenta);
                if (result instanceof VentaTB) {
                    return (VentaTB) result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(w -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });

        task.setOnFailed(w -> {
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });

        task.setOnSucceeded(w -> {
            VentaTB ventaTB = task.getValue();

            for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                if (cbTipoDocumento.getItems().get(i).getIdDetalle() == ventaTB.getClienteTB().getTipoDocumento()) {
                    cbTipoDocumento.getSelectionModel().select(i);
                    break;
                }
            }

            for (MonedaTB monedaTB : cbMoneda.getItems()) {
                if (monedaTB.getIdMoneda() == ventaTB.getIdMoneda()) {
                    cbMoneda.getSelectionModel().select(monedaTB);
                    monedaSimbolo = cbMoneda.getSelectionModel().getSelectedItem().getSimbolo();
                    break;
                }
            }

            txtNumeroDocumento.setText(ventaTB.getClienteTB().getNumeroDocumento());
            txtDatosCliente.setText(ventaTB.getClienteTB().getInformacion());
            txtCelularCliente.setText(ventaTB.getClienteTB().getCelular());
            txtCorreoElectronico.setText(ventaTB.getClienteTB().getEmail());
            txtDireccionCliente.setText(ventaTB.getClienteTB().getDireccion());

            ObservableList<SuministroTB> cotizacionTBs = FXCollections.observableArrayList();
            for (int i = 0; i < ventaTB.getSuministroTBs().size(); i++) {
                SuministroTB oldSuministroTB = ventaTB.getSuministroTBs().get(i);

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(oldSuministroTB.getIdSuministro());
                suministroTB.setClave(oldSuministroTB.getClave());
                suministroTB.setNombreMarca(oldSuministroTB.getNombreMarca());
                suministroTB.setCantidad(oldSuministroTB.getCantidad());
                suministroTB.setCostoCompra(oldSuministroTB.getCostoCompra());
                suministroTB.setBonificacion(0);
                suministroTB.setUnidadCompra(oldSuministroTB.getUnidadCompra());
                suministroTB.setUnidadCompraName(oldSuministroTB.getUnidadCompraName());

                suministroTB.setDescuento(oldSuministroTB.getDescuento());
                suministroTB.setDescuentoCalculado(oldSuministroTB.getDescuento());
                suministroTB.setDescuentoSumado(oldSuministroTB.getDescuento());

                suministroTB.setPrecioVentaGeneral(oldSuministroTB.getPrecioVentaGeneral());
                suministroTB.setPrecioVentaGeneralUnico(oldSuministroTB.getPrecioVentaGeneral());
                suministroTB.setPrecioVentaGeneralReal(oldSuministroTB.getPrecioVentaGeneral());

                suministroTB.setIdImpuesto(oldSuministroTB.getImpuestoTB().getIdImpuesto());
                suministroTB.setImpuestoTB(oldSuministroTB.getImpuestoTB());

                suministroTB.setInventario(oldSuministroTB.isInventario());
                suministroTB.setUnidadVenta(oldSuministroTB.getUnidadVenta());
                suministroTB.setValorInventario(oldSuministroTB.getValorInventario());

                Button button = new Button("X");
                button.getStyleClass().add("buttonDark");
                button.setOnAction(e -> {
                    tvList.getItems().remove(suministroTB);
                    calculateTotales();
                });
                button.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        tvList.getItems().remove(suministroTB);
                        calculateTotales();
                    }
                });
                suministroTB.setBtnRemove(button);

                cotizacionTBs.add(suministroTB);
            }

            tvList.setItems(cotizacionTBs);
            calculateTotales();

            vbBody.setDisable(false);
            hbLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void loadPlusVenta(String idVenta) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<VentaTB> task = new Task<VentaTB>() {
            @Override
            public VentaTB call() throws Exception {
                Object result = VentaADO.obtenerVentaPorIdVenta(idVenta);

                if (result instanceof VentaTB) {
                    return (VentaTB) result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(w -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });

        task.setOnFailed(w -> {
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });

        task.setOnSucceeded(w -> {
            VentaTB ventaTB = task.getValue();

            ventaTB.getSuministroTBs().forEach(s -> {
                if (validateDuplicate(s)) {
                    for (int i = 0; i < tvList.getItems().size(); i++) {
                        if (tvList.getItems().get(i).getIdSuministro().equalsIgnoreCase(s.getIdSuministro())) {
                            SuministroTB suministroTB = tvList.getItems().get(i);
                            suministroTB.setCantidad(suministroTB.getCantidad() + s.getCantidad());
                            tvList.refresh();
                            tvList.getSelectionModel().select(i);
                            calculateTotales();
                            break;
                        }
                    }
                } else {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setIdSuministro(s.getIdSuministro());
                    suministroTB.setClave(s.getClave());
                    suministroTB.setNombreMarca(s.getNombreMarca());
                    suministroTB.setCantidad(s.getCantidad());
                    suministroTB.setCostoCompra(s.getCostoCompra());
                    suministroTB.setBonificacion(0);
                    suministroTB.setUnidadCompra(s.getUnidadCompra());
                    suministroTB.setUnidadCompraName(s.getUnidadCompraName());

                    suministroTB.setDescuento(s.getDescuento());
                    suministroTB.setDescuentoCalculado(s.getDescuento());
                    suministroTB.setDescuentoSumado(s.getDescuento());

                    suministroTB.setPrecioVentaGeneral(s.getPrecioVentaGeneral());
                    suministroTB.setPrecioVentaGeneralUnico(s.getPrecioVentaGeneral());
                    suministroTB.setPrecioVentaGeneralReal(s.getPrecioVentaGeneral());

                    suministroTB.setIdImpuesto(s.getImpuestoTB().getIdImpuesto());
                    suministroTB.setImpuestoTB(s.getImpuestoTB());

                    suministroTB.setInventario(s.isInventario());
                    suministroTB.setUnidadVenta(s.getUnidadVenta());
                    suministroTB.setValorInventario(s.getValorInventario());

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");
                    button.setOnAction(e -> {
                        tvList.getItems().remove(suministroTB);
                        calculateTotales();
                    });
                    button.setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            tvList.getItems().remove(suministroTB);
                            calculateTotales();
                        }
                    });
                    suministroTB.setBtnRemove(button);

                    tvList.getItems().add(suministroTB);
                    int index = tvList.getItems().size() - 1;
                    tvList.getSelectionModel().select(index);
                    calculateTotales();
                }
            });

            vbBody.setDisable(false);
            hbLoad.setVisible(false);
            txtSearch.requestFocus();

        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void removeProducto() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Esta seguro de quitar el artículo?");
            if (confirmation == 1) {
                ObservableList<SuministroTB> articuloSelect, allArticulos;
                allArticulos = tvList.getItems();
                articuloSelect = tvList.getSelectionModel().getSelectedItems();
                articuloSelect.forEach(allArticulos::remove);
                calculateTotales();
            }
        } else {
            Tools.AlertMessageWarning(window, "Venta", "Seleccione un artículo para quitarlo");
        }
    }

    public void imprimirVenta(String idVenta) {
        fxOpcionesImprimirController.loadTicketVenta(window);
        fxOpcionesImprimirController.getTicketVenta().imprimir(idVenta);
    }

    private void imprimirPreVenta() {
        fxOpcionesImprimirController.loadTicketPreVenta(
                window,
                txtNumeroDocumento.getText().trim().toUpperCase(),
                txtDatosCliente.getText().trim().toUpperCase(),
                txtCelularCliente.getText().trim().toUpperCase(),
                txtDireccionCliente.getText().trim().toUpperCase(),
                monedaSimbolo,
                importeBrutoTotal,
                descuentoBrutoTotal,
                subImporteNetoTotal,
                impuestoNetoTotal,
                importeNetoTotal,
                tvList.getItems());
    }

    public void onExecuteCliente(short opcion, String search) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ClienteTB> task = new Task<ClienteTB>() {
            @Override
            public ClienteTB call() throws Exception {
                Object result = ClienteADO.GetSearchClienteNumeroDocumento(opcion, search);

                if (result instanceof ClienteTB) {
                    return (ClienteTB) result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(e -> {
            txtNumeroDocumento.setText("");
            txtDatosCliente.setText("");
            txtCelularCliente.setText("");
            txtCorreoElectronico.setText("");
            txtDireccionCliente.setText("");

            vbBodyCliente.setDisable(true);
            hbLoadCliente.setVisible(true);

            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }
            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                vbBodyCliente.setDisable(false);
                hbLoadCliente.setVisible(false);
            });
        });

        task.setOnCancelled(e -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Buscando clíente",
                    "Se canceló la busqueda, \nreintente por favor.",
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();
        });

        task.setOnFailed(e -> {
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Buscando clíente",
                    task.getException().getLocalizedMessage(),
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();

            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        task.setOnSucceeded(e -> {
            ClienteTB clienteTB = task.getValue();

            Tools.showAlertNotification("/view/image/succes_large.png",
                    "Buscando clíente",
                    "Se completo la busqueda con exito.",
                    Duration.seconds(5),
                    Pos.TOP_RIGHT);

            idCliente = clienteTB.getIdCliente();
            txtNumeroDocumento.setText(clienteTB.getNumeroDocumento());
            txtDatosCliente.setText(clienteTB.getInformacion());
            txtCelularCliente.setText(clienteTB.getCelular());
            txtCorreoElectronico.setText(clienteTB.getEmail());
            txtDireccionCliente.setText(clienteTB.getDireccion());
            for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                if (cbTipoDocumento.getItems().get(i).getIdDetalle() == clienteTB.getTipoDocumento()) {
                    cbTipoDocumento.getSelectionModel().select(i);
                    break;
                }
            }

            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void onExecuteApiSunat() {
        ApiPeru apiSunat = new ApiPeru();
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                Object resultCliente = ClienteADO.GetSearchClienteNumeroDocumento((short) 2,
                        txtNumeroDocumento.getText().trim());
                String resultApi = apiSunat.getUrlSunatApisPeru(txtNumeroDocumento.getText().trim());

                if (resultApi.equalsIgnoreCase("200") && !Tools.isText(apiSunat.getJsonURL())) {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        throw new Exception(
                                "No se pudo parcear le formato de resultado, intente en un par de minutos.");
                    }

                    if (resultCliente instanceof ClienteTB) {
                        return new Object[]{"client-exists", resultCliente};
                    } else {
                        return new Object[]{"client-no-exists", ""};
                    }
                }

                throw new Exception(
                        "Se produjo un error al buscar al cliente intente\n nuevamente, si persiste el problema comuniquese con su \nproveedor del sistema.");
            }
        };

        task.setOnScheduled(e -> {
            txtDatosCliente.setText("");
            txtCelularCliente.setText("");
            txtCorreoElectronico.setText("");
            txtDireccionCliente.setText("");

            vbBodyCliente.setDisable(true);
            hbLoadCliente.setVisible(true);

            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }

            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                vbBodyCliente.setDisable(false);
                hbLoadCliente.setVisible(false);
            });
        });

        task.setOnCancelled(e -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Buscando clíente",
                    "Se canceló la busqueda, \nreintente por favor.",
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();
        });

        task.setOnFailed(e -> {
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Buscando clíente",
                    task.getException().getLocalizedMessage(),
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();

            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        task.setOnSucceeded(e -> {
            Object[] result = task.getValue();

            String stateClient = (String) result[0];

            JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());

            Tools.showAlertNotification("/view/image/succes_large.png",
                    "Buscando clíente",
                    "Se completo la busqueda con exito.",
                    Duration.seconds(5),
                    Pos.TOP_RIGHT);

            if (sONObject.get("ruc") != null) {
                txtNumeroDocumento.setText(sONObject.get("ruc").toString());
            }
            if (sONObject.get("razonSocial") != null) {
                txtDatosCliente.setText(sONObject.get("razonSocial").toString());
            }
            if (sONObject.get("direccion") != null) {
                txtDireccionCliente.setText(sONObject.get("direccion").toString());
            }

            if (stateClient.equals("client-exists")) {
                ClienteTB clienteTB = (ClienteTB) result[1];
                txtCelularCliente.setText(clienteTB.getCelular());
                txtCorreoElectronico.setText(clienteTB.getEmail());

                for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                    if (cbTipoDocumento.getItems().get(i).getIdDetalle() == clienteTB.getTipoDocumento()) {
                        cbTipoDocumento.getSelectionModel().select(i);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                    if (cbTipoDocumento.getItems().get(i).getIdAuxiliar().equals("6")) {
                        cbTipoDocumento.getSelectionModel().select(i);
                        break;
                    }
                }
            }

            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void onExecuteApiReniec() {
        ApiPeru apiSunat = new ApiPeru();
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                Object resultCliente = ClienteADO.GetSearchClienteNumeroDocumento((short) 2,
                        txtNumeroDocumento.getText().trim());
                String resultApi = apiSunat.getUrlReniecApisPeru(txtNumeroDocumento.getText().trim());

                if (resultApi.equalsIgnoreCase("200") && !Tools.isText(apiSunat.getJsonURL())) {
                    JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());
                    if (sONObject == null) {
                        throw new Exception(
                                "No se pudo parcear le formato de resultado, intente en un par de minutos.");
                    }

                    if (resultCliente instanceof ClienteTB) {
                        return new Object[]{"client-exists", resultCliente};
                    } else {
                        return new Object[]{"client-no-exists", ""};
                    }
                }

                throw new Exception(
                        "Se produjo un error al buscar al cliente intente\n nuevamente, si persiste el problema comuniquese con su \nproveedor del sistema.");
            }
        };

        task.setOnScheduled(e -> {
            txtDatosCliente.setText("");
            txtCelularCliente.setText("");
            txtDireccionCliente.setText("");
            txtDireccionCliente.setText("");

            vbBodyCliente.setDisable(true);
            hbLoadCliente.setVisible(true);

            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }
            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                vbBodyCliente.setDisable(false);
                hbLoadCliente.setVisible(false);
            });
        });

        task.setOnCancelled(e -> {
            Tools.showAlertNotification("/view/image/warning_large.png",
                    "Buscando clíente",
                    "Se canceló la busqueda, \nreintente por favor.",
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();
        });

        task.setOnFailed(e -> {
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Buscando clíente",
                    task.getException().getLocalizedMessage(),
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();
            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        task.setOnSucceeded(e -> {
            Object[] result = task.getValue();

            String stateClient = (String) result[0];

            Tools.showAlertNotification("/view/image/succes_large.png",
                    "Buscando clíente",
                    "Se completo la busqueda con exito.",
                    Duration.seconds(5),
                    Pos.TOP_RIGHT);

            JSONObject sONObject = Json.obtenerObjetoJSON(apiSunat.getJsonURL());

            if (sONObject.get("dni") != null) {
                txtNumeroDocumento.setText(sONObject.get("dni").toString());
            }

            if (sONObject.get("apellidoPaterno") != null && sONObject.get("apellidoMaterno") != null
                    && sONObject.get("nombres") != null) {
                txtDatosCliente.setText(sONObject.get("apellidoPaterno").toString() + " "
                        + sONObject.get("apellidoMaterno").toString() + " " + sONObject.get("nombres").toString());
            }

            if (stateClient.equals("client-exists")) {
                ClienteTB clienteTB = (ClienteTB) result[1];

                txtCelularCliente.setText(clienteTB.getCelular());
                txtCorreoElectronico.setText(clienteTB.getEmail());
                txtDireccionCliente.setText(clienteTB.getDireccion());

                for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                    if (cbTipoDocumento.getItems().get(i).getIdDetalle() == clienteTB.getTipoDocumento()) {
                        cbTipoDocumento.getSelectionModel().select(i);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                    if (cbTipoDocumento.getItems().get(i).getIdAuxiliar().equals("1")) {
                        cbTipoDocumento.getSelectionModel().select(i);
                        break;
                    }
                }
            }

            vbBodyCliente.setDisable(false);
            hbLoadCliente.setVisible(false);
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void clearDataClient() {
        txtDatosCliente.setText("");
        txtCelularCliente.setText("");
        txtCorreoElectronico.setText("");
        txtDireccionCliente.setText("");

        for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
            if (cbTipoDocumento.getItems().get(i).getIdAuxiliar().equals("0")) {
                cbTipoDocumento.getSelectionModel().select(i);
                break;
            }
        }

    }

    private void learnWordDocumento(String text) {
        posiblesWordDocumento.add(text);
        if (autoCompletionBindingDocumento != null) {
            autoCompletionBindingDocumento.dispose();
        }
        autoCompletionBindingDocumento = TextFields.bindAutoCompletion(txtNumeroDocumento, posiblesWordDocumento);
    }

    @FXML
    private void onKeyPressedRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentaProceso();
        }
    }

    @FXML
    private void onActionRegister(ActionEvent event) {
        openWindowVentaProceso();
    }

    @FXML
    private void onKeyPressedArticulo(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
        }
    }

    @FXML
    private void onActionArticulo(ActionEvent event) {
        openWindowSuministro();
    }

    @FXML
    private void onKeyPressedListaPrecios(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowListaPrecios();
        }
    }

    @FXML
    private void onActionListaPrecios(ActionEvent event) {
        openWindowListaPrecios();
    }

    @FXML
    private void onKeyPressedCantidad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCantidad(true, window.getScene().getWindow(), false);
        }
    }

    @FXML
    private void onActionCantidad(ActionEvent event) {
        openWindowCantidad(true, window.getScene().getWindow(), false);
    }

    @FXML
    private void onKeyPressedPrecio(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCambiarPrecio("Cambiar precio al Producto", false, true, window.getScene().getWindow());
        }
    }

    @FXML
    private void onActionPrecio(ActionEvent event) {
        openWindowCambiarPrecio("Cambiar precio al Producto", false, true, window.getScene().getWindow());
    }

    @FXML
    private void onKeyPressedDescuento(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDescuento();
        }
    }

    @FXML
    private void onActionDescuento(ActionEvent event) {
        openWindowDescuento();
    }

    @FXML
    private void onKeyPressedPrecioSumar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCambiarPrecio("Sumar precio al Producto", true, true, window.getScene().getWindow());
        }
    }

    @FXML
    private void onActionPrecioSumar(ActionEvent event) {
        openWindowCambiarPrecio("Sumar precio al Producto", true, true, window.getScene().getWindow());
    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE
                && event.getCode() != KeyCode.F1
                && event.getCode() != KeyCode.F2
                && event.getCode() != KeyCode.F3
                && event.getCode() != KeyCode.F4
                && event.getCode() != KeyCode.F5
                && event.getCode() != KeyCode.F6
                && event.getCode() != KeyCode.F7
                && event.getCode() != KeyCode.F8
                && event.getCode() != KeyCode.F9
                && event.getCode() != KeyCode.F10
                && event.getCode() != KeyCode.F11
                && event.getCode() != KeyCode.F12
                && event.getCode() != KeyCode.ALT
                && event.getCode() != KeyCode.CONTROL
                && event.getCode() != KeyCode.UP
                && event.getCode() != KeyCode.DOWN
                && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.LEFT
                && event.getCode() != KeyCode.TAB
                && event.getCode() != KeyCode.CAPS
                && event.getCode() != KeyCode.SHIFT
                && event.getCode() != KeyCode.HOME
                && event.getCode() != KeyCode.WINDOWS
                && event.getCode() != KeyCode.ALT_GRAPH
                && event.getCode() != KeyCode.CONTEXT_MENU
                && event.getCode() != KeyCode.END
                && event.getCode() != KeyCode.INSERT
                && event.getCode() != KeyCode.PAGE_UP
                && event.getCode() != KeyCode.PAGE_DOWN
                && event.getCode() != KeyCode.NUM_LOCK
                && event.getCode() != KeyCode.PRINTSCREEN
                && event.getCode() != KeyCode.SCROLL_LOCK
                && event.getCode() != KeyCode.PAUSE) {
            if (event.getCode() == KeyCode.ENTER) {
                if (!stateSearch) {
                    if (!txtSearch.getText().trim().isEmpty()) {
                        filterSuministro(txtSearch.getText().trim());
                    }
                }
            }
        }
    }

    @FXML
    private void onKeyReleasedList(KeyEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.ADD) {
                int index = tvList.getSelectionModel().getSelectedIndex();
                SuministroTB suministroTB = tvList.getSelectionModel().getSelectedItem();
                if (!valormonetario_cambio_cantidades && suministroTB.getUnidadVenta() == 2) {
                    return;
                }
                suministroTB.setCantidad(suministroTB.getCantidad() + 1);
                tvList.refresh();
                tvList.getSelectionModel().select(index);
                calculateTotales();

            } else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                int index = tvList.getSelectionModel().getSelectedIndex();
                SuministroTB suministroTB = tvList.getSelectionModel().getSelectedItem();
                if (!valormonetario_cambio_cantidades && suministroTB.getUnidadVenta() == 2) {
                    return;
                }
                if (suministroTB.getCantidad() <= 1) {
                    return;
                }
                suministroTB.setCantidad(suministroTB.getCantidad() - 1);
                tvList.refresh();
                tvList.getSelectionModel().select(index);
                calculateTotales();
            }
        }
    }

    @FXML
    private void onActionComprobante(ActionEvent event) {
        if (cbComprobante.getSelectionModel().getSelectedIndex() >= 0) {
            String[] array = ComprobanteADO.GetSerieNumeracionEspecifico(
                    cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento()).split("-");
            lblSerie.setText(array[0]);
            lblNumeracion.setText(array[1]);
        }
    }

    @FXML
    private void onActionMoneda(ActionEvent event) {
        if (cbMoneda.getSelectionModel().getSelectedIndex() >= 0) {
            monedaSimbolo = cbMoneda.getSelectionModel().getSelectedItem().getSimbolo();
            calculateTotales();
        }
    }

    @FXML
    private void onKeyPressedCashMovement(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCashMovement();
        }
    }

    @FXML
    private void onActionCashMovement(ActionEvent event) {
        openWindowCashMovement();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            cancelarVenta();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        cancelarVenta();
    }

    @FXML
    private void onKeyPressedVentasPorDia(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMostrarVentas();
        }
    }

    @FXML
    private void onActionVentasPorDia(ActionEvent event) {
        openWindowMostrarVentas();
    }

    @FXML
    private void onKeyPressedCliente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onExecuteCliente((short) 2, txtNumeroDocumento.getText().trim());
            learnWordDocumento(txtNumeroDocumento.getText().trim());
        }
    }

    @FXML
    private void onActionCliente(ActionEvent event) {
        onExecuteCliente((short) 2, txtNumeroDocumento.getText().trim());
        learnWordDocumento(txtNumeroDocumento.getText().trim());
    }

    @FXML
    private void onKeyPressedSunat(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onExecuteApiSunat();
            learnWordDocumento(txtNumeroDocumento.getText().trim());
        }
    }

    @FXML
    private void onActionSunat(ActionEvent event) {
        onExecuteApiSunat();
        learnWordDocumento(txtNumeroDocumento.getText().trim());
    }

    @FXML
    private void onKeyPressedReniec(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onExecuteApiReniec();
            learnWordDocumento(txtNumeroDocumento.getText().trim());
        }
    }

    @FXML
    private void onActionReniec(ActionEvent event) {
        onExecuteApiReniec();
        learnWordDocumento(txtNumeroDocumento.getText().trim());
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            imprimirPreVenta();
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        imprimirPreVenta();
    }

    @FXML
    private void onKeyTypedNumeroDocumento(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedCotizaciones(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCotizaciones();
        }
    }

    @FXML
    private void onActionCotizaciones(ActionEvent event) {
        openWindowCotizaciones();
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) {
        if (event.getCode() == KeyCode.F1) {
            openWindowVentaProceso();
            event.consume();
        } else if (event.getCode() == KeyCode.F2) {
            openWindowSuministro();
            event.consume();
        } else if (event.getCode() == KeyCode.F3) {
            openWindowListaPrecios();
            event.consume();
        } else if (event.getCode() == KeyCode.F4) {
            openWindowCantidad(true, window.getScene().getWindow(), false);
            event.consume();
        } else if (event.getCode() == KeyCode.F5) {
            openWindowCambiarPrecio("Cambiar precio al Artículo", false, true, window.getScene().getWindow());
            event.consume();
        } else if (event.getCode() == KeyCode.F6) {
            openWindowDescuento();
            event.consume();
        } else if (event.getCode() == KeyCode.F7) {
            openWindowCambiarPrecio("Sumar precio al Artículo", true, true, window.getScene().getWindow());
            event.consume();
        } else if (event.getCode() == KeyCode.F8) {
            openWindowCashMovement();
            event.consume();
        } else if (event.getCode() == KeyCode.F9) {
            imprimirPreVenta();
            event.consume();
        } else if (event.getCode() == KeyCode.F10) {
            short value = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de limpiar la venta?");
            if (value == 1) {
                resetVenta();
                event.consume();
            } else {
                event.consume();
            }
        } else if (event.getCode() == KeyCode.F11) {
            openWindowMostrarVentas();
            event.consume();
        } else if (event.getCode() == KeyCode.F12) {
            openWindowCotizaciones();
            event.consume();
        } else if (event.getCode() == KeyCode.DELETE) {
            removeProducto();
            event.consume();
        } else if (event.isControlDown() && event.getCode() == KeyCode.D) {
            txtSearch.requestFocus();
            txtSearch.selectAll();
            event.consume();
        } else if (event.isControlDown() && event.getCode() == KeyCode.W) {
            tvList.requestFocus();
            if (!tvList.getItems().isEmpty()) {
                tvList.getSelectionModel().select(0);
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            fxPrincipalController.closeFondoModal();
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        vbBody.setDisable(false);
        hbLoad.setVisible(false);
    }

    public TextField getTxtSearch() {
        return txtSearch;
    }

    public TableView<SuministroTB> getTvList() {
        return tvList;
    }

    public boolean isVender_con_cantidades_negativas() {
        return vender_con_cantidades_negativas;
    }

    public boolean isCerar_modal_agregar_item_lista() {
        return cerar_modal_agregar_item_lista;
    }

    public VBox getWindow() {
        return window;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
