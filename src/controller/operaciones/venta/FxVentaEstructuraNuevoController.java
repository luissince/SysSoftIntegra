package controller.operaciones.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.inventario.suministros.FxSuministrosProcesoModalController;
import controller.menus.FxPrincipalController;
import controller.tools.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.ClienteTB;
import model.MonedaTB;
import model.SuministroTB;
import model.TipoDocumentoTB;
import model.VentaTB;
import service.ClienteADO;
import service.ComprobanteADO;
import service.DetalleADO;
import service.MonedaADO;
import service.SuministroADO;
import service.TipoDocumentoADO;
import model.DetalleTB;
import model.PrivilegioTB;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.json.simple.JSONObject;

public class FxVentaEstructuraNuevoController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private FlowPane fpProductos;
    @FXML
    private ListView<BbItemProducto> lvProductoAgregados;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblTotal;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private ComboBox<TipoDocumentoTB> cbComprobante;
    @FXML
    private Text lblSerie;
    @FXML
    private Text lblNumeracion;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtDatosCliente;
    @FXML
    private TextField txtCelularCliente;
    @FXML
    private TextField txtCorreoElectronico;
    @FXML
    private TextField txtDireccionCliente;
    @FXML
    private VBox vbBodyCliente;
    @FXML
    private HBox hbLoadCliente;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private Button btnMovimientoCaja;
    @FXML
    private Button btnVentas;
    @FXML
    private Button btnPrintTiket;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private ObservableList<SuministroTB> listSuministros;

    private AutoCompletionBinding<String> autoCompletionBindingDocumento;

    private AutoCompletionBinding<ClienteTB> autoCompletionBindingInfo;

    private final Set<String> posiblesWordDocumento = new HashSet<>();

    private final Set<ClienteTB> posiblesWordInfo = new HashSet<>();

    private String monedaSimbolo;

    private String idCliente;

    private boolean vender_con_cantidades_negativas;

    private int totalPaginacion;

    private int paginacion;

    private short opcion;

    private boolean state;

    private double importeBrutoTotal;

    private double descuentoBrutoTotal;

    private double subImporteNetoTotal;

    private double impuestoNetoTotal;

    private double importeNetoTotal;

    private boolean mostrarUltimasVentas = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listSuministros = FXCollections.observableArrayList();
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();

        paginacion = 0;
        totalPaginacion = 0;
        opcion = 0;
        state = false;
        monedaSimbolo = "M";
        idCliente = "";
        loadDataComponent();
        autoCompletionBindingDocumento = TextFields.bindAutoCompletion(txtNumeroDocumento, posiblesWordDocumento);
        autoCompletionBindingDocumento.setOnAutoCompleted(e -> {
            if (!Tools.isText(txtNumeroDocumento.getText())) {
                onExecuteCliente((short) 2, txtNumeroDocumento.getText().trim());
                txtSearch.requestFocus();
            }
        });

        autoCompletionBindingInfo = TextFields.bindAutoCompletion(txtDatosCliente, posiblesWordInfo);
        autoCompletionBindingInfo.setOnAutoCompleted(e -> {
            if (!Tools.isText(e.getCompletion().getNumeroDocumento())) {
                onExecuteCliente((short) 2, e.getCompletion().getNumeroDocumento());
                txtSearch.requestFocus();
            }
        });
    }

    private void loadDataComponent() {
        cbComprobante.getItems().clear();
        TipoDocumentoADO.obtenerComprobantesParaVenta().forEach(e -> cbComprobante.getItems().add(e));
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

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        if (privilegioTBs.get(9).getIdPrivilegio() != 0 && !privilegioTBs.get(9).isEstado()) {
            btnMovimientoCaja.setDisable(true);
        }

        if (privilegioTBs.get(10).getIdPrivilegio() != 0 && !privilegioTBs.get(10).isEstado()) {
            btnPrintTiket.setDisable(true);
        }

        if (privilegioTBs.get(15).getIdPrivilegio() != 0 && !privilegioTBs.get(15).isEstado()) {
            btnVentas.setDisable(true);
        }

        vender_con_cantidades_negativas = privilegioTBs.get(34).getIdPrivilegio() != 0
                && !privilegioTBs.get(34).isEstado();

        if (privilegioTBs.get(36).getIdPrivilegio() != 0 && !privilegioTBs.get(36).isEstado()) {
            mostrarUltimasVentas = true;
        }
    }

    public void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillSuministrosTable((short) 0, "");
                break;
            case 1:
                fillSuministrosTable((short) 1, txtSearch.getText().trim());
                break;
        }
    }

    private void fillSuministrosTable(short tipo, String value) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return SuministroADO.Listar_Suministros_Lista_View(tipo, value, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                fpProductos.getChildren().clear();
                fpProductos.setAlignment(Pos.TOP_CENTER);
                listSuministros.addAll((ObservableList<SuministroTB>) objects[0]);
                if (!listSuministros.isEmpty()) {
                    listSuministros.forEach(tvList1 -> {
                        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                        PPI display = new PPI("" + screenBounds.getWidth(), "" + screenBounds.getHeight(), "" + 96);
                        double resultNumber = display.calc();
                        double dpi = resultNumber * 8;

                        VBox vBox = new VBox();
                        ImageView imageView = new ImageView(new Image("/view/image/no-image.png"));
                        if (tvList1.getNuevaImagen() != null) {
                            imageView = new ImageView(new Image(new ByteArrayInputStream(tvList1.getNuevaImagen())));
                        }
                        imageView.setFitWidth(dpi * 1.3);
                        imageView.setFitHeight(dpi * 1.2);
                        vBox.getChildren().add(imageView);

                        Label lblProducto = new Label(tvList1.getNombreMarca());
                        lblProducto.getStyleClass().add("labelRobotoBold15");
                        lblProducto.setTextFill(Color.web("#020203"));
                        lblProducto.setTextAlignment(TextAlignment.CENTER);
                        lblProducto.setAlignment(Pos.CENTER);
                        lblProducto.setMinWidth(Control.USE_PREF_SIZE);
                        lblProducto.setPrefWidth(dpi * 1.3);
                        vBox.getChildren().add(lblProducto);

                        Label lblMarca = new Label(
                                Tools.isText(tvList1.getMarcaName()) ? "No Marca" : tvList1.getMarcaName());
                        lblMarca.getStyleClass().add("labelOpenSansRegular13");
                        lblMarca.setTextFill(Color.web("#1a2226"));
                        vBox.getChildren().add(lblMarca);

                        Label lblCantidad = new Label(Tools.roundingValue(tvList1.getCantidad(), 2));
                        lblCantidad.getStyleClass().add("labelRobotoBold17");
                        lblCantidad
                                .setTextFill(tvList1.getCantidad() <= 0 ? Color.web("#e40000") : Color.web("#117cee"));
                        vBox.getChildren().add(lblCantidad);

                        Label lblTotalProducto = new Label(
                                Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(tvList1.getPrecioVentaGeneral(), 2));
                        lblTotalProducto.getStyleClass().add("labelRobotoBold19");
                        lblTotalProducto.setTextFill(Color.web("#009a1e"));
                        lblTotalProducto.maxWidth(Double.MAX_VALUE);
                        vBox.getChildren().add(lblTotalProducto);

                        vBox.setStyle(
                                "-fx-spacing: 0.4166666666666667em;-fx-padding: 0.4166666666666667em;-fx-border-color: #0478b2;-fx-border-width:2px;-fx-background-color:transparent;");
                        vBox.setAlignment(Pos.TOP_CENTER);
                        vBox.setMinWidth(Control.USE_COMPUTED_SIZE);
                        vBox.setPrefWidth(dpi * 1.5);
                        vBox.maxWidth(Control.USE_COMPUTED_SIZE);

                        Button button = new Button();
                        button.getStyleClass().add("buttonView");
                        button.setGraphic(vBox);
                        button.setOnAction(event -> {
                            addElementListView(tvList1);
                        });
                        button.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                addElementListView(tvList1);
                            }
                        });

                        fpProductos.getChildren().add(button);
                    });

                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    fpProductos.getChildren().clear();
                    fpProductos.setAlignment(Pos.CENTER);
                    Label lblLoad = new Label("No hay datos para mostrar.");
                    lblLoad.getStyleClass().add("labelRoboto15");
                    lblLoad.setTextFill(Color.web("#1a2226"));
                    lblLoad.setContentDisplay(ContentDisplay.TOP);
                    fpProductos.getChildren().add(lblLoad);
                }
            } else if (object instanceof String) {
                fpProductos.getChildren().clear();
                fpProductos.setAlignment(Pos.CENTER);
                Label lblLoad = new Label((String) object);
                lblLoad.getStyleClass().add("labelRoboto15");
                lblLoad.setTextFill(Color.web("#a70820"));
                lblLoad.setContentDisplay(ContentDisplay.TOP);
                fpProductos.getChildren().add(lblLoad);
            } else {
                fpProductos.getChildren().clear();
                fpProductos.setAlignment(Pos.CENTER);
                Label lblLoad = new Label("Error en traer los datos, intente nuevamente.");
                lblLoad.getStyleClass().add("labelRoboto15");
                lblLoad.setTextFill(Color.web("#a70820"));
                lblLoad.setContentDisplay(ContentDisplay.TOP);
                fpProductos.getChildren().add(lblLoad);
            }
            state = false;
        });
        task.setOnFailed(e -> {
            state = false;
            fpProductos.getChildren().clear();
            fpProductos.setAlignment(Pos.CENTER);
            Label lblLoad = new Label(task.getMessage());
            lblLoad.getStyleClass().add("labelRoboto15");
            lblLoad.setTextFill(Color.web("#a70820"));
            lblLoad.setContentDisplay(ContentDisplay.TOP);
            fpProductos.getChildren().add(lblLoad);
        });
        task.setOnScheduled(e -> {
            state = true;
            listSuministros.clear();
            fpProductos.getChildren().clear();
            fpProductos.setAlignment(Pos.CENTER);
            Label lblLoad = new Label("Cargando productos para su venta");
            lblLoad.getStyleClass().add("labelRoboto15");
            lblLoad.setTextFill(Color.web("#1a2226"));
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            lblLoad.setContentDisplay(ContentDisplay.TOP);
            lblLoad.setGraphic(progressIndicator);
            fpProductos.getChildren().add(lblLoad);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void addElementListView(SuministroTB a) {
        if (vender_con_cantidades_negativas && a.getCantidad() <= 0) {
            Tools.AlertMessageWarning(vbWindow, "Producto",
                    "No puede agregar el producto ya que tiene la cantidad menor que 0.");
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

        addProducto(suministroTB);
    }

    private void addProducto(SuministroTB suministroTB) {
        if (validateDuplicate(suministroTB)) {
            for (int i = 0; i < lvProductoAgregados.getItems().size(); i++) {
                if (lvProductoAgregados.getItems().get(i).getSuministroTB().getIdSuministro()
                        .equalsIgnoreCase(suministroTB.getIdSuministro())) {
                    BbItemProducto bbItemProducto = lvProductoAgregados.getItems().get(i);
                    bbItemProducto.getSuministroTB().setCantidad(bbItemProducto.getSuministroTB().getCantidad() + 1);
                    bbItemProducto.getChildren().clear();
                    bbItemProducto.addElementListView();

                    lvProductoAgregados.getItems().set(i, bbItemProducto);
                    calculateTotales();
                }
            }
        } else {
            BbItemProducto bbItemProducto = new BbItemProducto(suministroTB, lvProductoAgregados, this);
            bbItemProducto.addElementListView();
            lvProductoAgregados.getItems().add(bbItemProducto);
            calculateTotales();
        }
    }

    private boolean validateDuplicate(SuministroTB suministroTB) {
        boolean ret = false;
        for (int i = 0; i < lvProductoAgregados.getItems().size(); i++) {
            if (lvProductoAgregados.getItems().get(i).getSuministroTB().getIdSuministro()
                    .equals(suministroTB.getIdSuministro())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void searchTable(KeyEvent event, String value) {
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
            if (!state) {
                paginacion = 1;
                fillSuministrosTable((short) 1, value);
                opcion = 1;
            }
        }
    }

    public void calculateTotales() {
        importeBrutoTotal = 0;
        descuentoBrutoTotal = 0;
        subImporteNetoTotal = 0;
        impuestoNetoTotal = 0;
        importeNetoTotal = 0;

        lvProductoAgregados.getItems().forEach(suministroTB -> {
            double importeBruto = suministroTB.getSuministroTB().getPrecioVentaGeneral()
                    * suministroTB.getSuministroTB().getCantidad();
            double descuento = suministroTB.getSuministroTB().getDescuento();
            double subImporteBruto = importeBruto - descuento;
            double subImporteNeto = Tools.calculateTaxBruto(suministroTB.getSuministroTB().getImpuestoTB().getValor(),
                    subImporteBruto);
            double impuesto = Tools.calculateTax(suministroTB.getSuministroTB().getImpuestoTB().getValor(),
                    subImporteNeto);
            double importeNeto = subImporteNeto + impuesto;

            importeBrutoTotal += importeBruto;
            descuentoBrutoTotal += descuento;
            subImporteNetoTotal += subImporteNeto;
            impuestoNetoTotal += impuesto;
            importeNetoTotal += importeNeto;
        });

        lblTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    public void resetVenta() {
        paginacion = 0;
        totalPaginacion = 0;
        opcion = 0;
        state = false;
        listSuministros.clear();
        fpProductos.getChildren().clear();
        lvProductoAgregados.getItems().clear();

        loadDataComponent();
        lblPaginaActual.setText(paginacion + "");
        lblPaginaSiguiente.setText(totalPaginacion + "");
        lblTotal.setText(monedaSimbolo + " 0.00");
        txtSearch.clear();
        txtSearch.requestFocus();
        calculateTotales();
    }

    public void imprimirVenta(String idVenta) {
        fxOpcionesImprimirController.loadTicketVenta(vbWindow);
        fxOpcionesImprimirController.getTicketVenta().imprimir(idVenta);
    }

    private void imprimirPreVenta() {
        String numeroDocumento;
        String informacion;
        String celular;
        // String email;
        String direccion;

        numeroDocumento = txtNumeroDocumento.getText().trim().length() == 0 ? ""
                : txtNumeroDocumento.getText().trim().toUpperCase();
        informacion = txtDatosCliente.getText().trim().length() == 0 ? ""
                : txtDatosCliente.getText().trim().toUpperCase();
        celular = txtCelularCliente.getText().trim().length() == 0 ? ""
                : txtCelularCliente.getText().trim().toUpperCase();
        // email = txtCorreoElectronico.getText().trim().length() == 0 ? "" :
        // txtCorreoElectronico.getText().trim().toUpperCase();
        direccion = txtDireccionCliente.getText().trim().length() == 0 ? ""
                : txtDireccionCliente.getText().trim().toUpperCase();

        ObservableList<SuministroTB> observableList = FXCollections.observableArrayList();
        lvProductoAgregados.getItems().forEach(o -> observableList.add(o.getSuministroTB()));

        fxOpcionesImprimirController.loadTicketPreVenta(
                vbWindow,
                numeroDocumento,
                informacion,
                celular,
                direccion,
                monedaSimbolo,
                importeBrutoTotal,
                descuentoBrutoTotal,
                subImporteNetoTotal,
                impuestoNetoTotal,
                importeNetoTotal,
                observableList);
    }

    private void cancelarVenta() {
        short value = Tools.AlertMessageConfirmation(vbWindow, "Venta", "¿Está seguro de limpiar la venta?");
        if (value == 1) {
            resetVenta();
        }
    }

    private void openWindowDetalleProducto(int index, BbItemProducto bbItemProducto) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_DETALLE_PRODUCTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaDetalleProductoController controller = fXMLLoader.getController();
            controller.loadData(index, bbItemProducto);
            controller.setInitVentaEstructuraNuevoController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Detalle producto", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowImpresora():" + ex.getLocalizedMessage());
        }
    }

    public void onEventCobrar() {
        try {
            if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Venta", "Seleccione la moneda ha usar.");
                cbMoneda.requestFocus();
                return;
            }

            if (cbComprobante.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Venta", "Seleccione el tipo de comprobante.");
                cbComprobante.requestFocus();
                return;
            }

            if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Ventas", "Seleccione el tipo de documento del cliente.");
                cbTipoDocumento.requestFocus();
                return;
            }

            if (!Tools.isNumeric(txtNumeroDocumento.getText().trim())) {
                Tools.AlertMessageWarning(vbWindow, "Ventas", "Ingrese el número del documento del cliente.");
                txtNumeroDocumento.requestFocus();
                return;
            }

            if (cbComprobante.getSelectionModel().getSelectedItem().isCampo() && txtNumeroDocumento.getText()
                    .length() != cbComprobante.getSelectionModel().getSelectedItem().getNumeroCampo()) {
                Tools.AlertMessageWarning(vbWindow, "Ventas", "El número de documento tiene que tener "
                        + cbComprobante.getSelectionModel().getSelectedItem().getNumeroCampo() + " caracteres.");
                txtNumeroDocumento.requestFocus();
                return;
            }
            if (Tools.isText(txtDatosCliente.getText())) {
                Tools.AlertMessageWarning(vbWindow, "Ventas", "Ingrese los datos del cliente.");
                txtDatosCliente.requestFocus();
                return;
            }

            if (lvProductoAgregados.getItems().isEmpty()) {
                Tools.AlertMessageWarning(vbWindow, "Venta", "No hay productos en la lista para vender.");
                txtSearch.requestFocus();
                return;
            }

            if (importeNetoTotal <= 0) {
                Tools.AlertMessageWarning(vbWindow, "Venta", "El total de la venta no puede ser menor que 0.");
                return;
            }

            VentaTB ventaTB = new VentaTB();
            ventaTB.setVendedor(Session.USER_ID);
            ventaTB.setIdComprobante(cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento());

            TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
            tipoDocumentoTB.setNombre(cbComprobante.getSelectionModel().getSelectedItem().getNombre());
            ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

            ventaTB.setSerie(lblSerie.getText());
            ventaTB.setNumeracion(lblNumeracion.getText());
            ventaTB.setFechaVenta(Tools.getDate());
            ventaTB.setHoraVenta(Tools.getTime());
            ventaTB.setTotal(importeNetoTotal);
            ventaTB.setIdCotizacion("");

            ventaTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda());
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

            ArrayList<SuministroTB> suministroTBs = new ArrayList<>();
            lvProductoAgregados.getItems().forEach(e -> suministroTBs.add(e.getSuministroTB()));

            ventaTB.setSuministroTBs(suministroTBs);

            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaProcesoController controller = fXMLLoader.getController();
            controller.setInitVentaEstructuraNuevoController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Completar la venta",
                    vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShowing(w -> controller.loadInitComponents(ventaTB, vender_con_cantidades_negativas));
            stage.show();

        } catch (IOException ex) {
            System.out.println("openWindowVentaProceso():" + ex.getLocalizedMessage());
        }
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
                        return new Object[] { "client-exists", resultCliente };
                    } else {
                        return new Object[] { "client-no-exists", "" };
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
                    "Se produjo un error al buscar al cliente intenten\n nuevamente, si persiste el problema comuniquese con su \nproveedor del sistema.",
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
                        return new Object[] { "client-exists", resultCliente };
                    } else {
                        return new Object[] { "client-no-exists", "" };
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
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Buscando clíente",
                    task.getException().getLocalizedMessage(),
                    Duration.seconds(10),
                    Pos.TOP_RIGHT);
            clearDataClient();
        });

        task.setOnFailed(e -> {
            Tools.showAlertNotification("/view/image/error_large.png",
                    "Buscando clíente",
                    "Se produjo un error al buscar al cliente intenten\n nuevamente, si persiste el problema comuniquese con su \nproveedor del sistema.",
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

    private void onEventProducto() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_PROCESO_MODAL);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxSuministrosProcesoModalController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Producto", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.setInitArticulo();
        } catch (IOException ex) {
            System.out.println("Error en producto lista:" + ex.getLocalizedMessage());
        }
    }

    private void onEventMovimientoCaja() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_MOVIMIENTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            // FxVentaMovimientoController controller = fXMLLoader.getController();
            // controller.setInitVentaEstructuraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Movimiento de caja", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
        }
    }

    public void openWindowMostrarVentas() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_MOSTRAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaMostrarController controller = fXMLLoader.getController();
            controller.setMostrarUltimasVentas(mostrarUltimasVentas);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar ventas", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
        }
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) {
        switch (event.getCode()) {
            case F1:
                onEventCobrar();
                break;
            case F2:
                txtSearch.selectAll();
                txtSearch.requestFocus();
                break;
            case F3:
                cbMoneda.requestFocus();
                break;
            case F4:
                txtNumeroDocumento.requestFocus();
                break;
            case F5:

                break;
            case F6:
                cbComprobante.requestFocus();
                break;
            case F7:
                onEventMovimientoCaja();
                break;
            case F8:
                openWindowMostrarVentas();
                break;
            case F9:
                imprimirPreVenta();
                break;
            case F10:
                cancelarVenta();
                break;
            default:
                break;
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
    private void onKeyPressedToSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fpProductos.requestFocus();
            if (!fpProductos.getChildren().isEmpty()) {
                Button button = (Button) fpProductos.getChildren().get(0);
                button.requestFocus();
            }
        }
    }

    @FXML
    private void onKeyReleasedToSearch(KeyEvent event) {
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
            if (!Tools.isText(txtSearch.getText())) {
                if (!state) {
                    paginacion = 1;
                    searchTable(event, txtSearch.getText().trim());
                    opcion = 1;
                }
            }
        }
    }

    private void onKeyPressReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!state) {
                paginacion = 1;
                fillSuministrosTable((short) 0, "");
                opcion = 0;
            }
        }
    }

    private void onActionReload(ActionEvent event) {
        if (!state) {
            paginacion = 1;
            fillSuministrosTable((short) 0, "");
            opcion = 0;
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!state) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!state) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!state) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!state) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
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
    private void onMouseClickedProductosAgregados(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (lvProductoAgregados.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowDetalleProducto(lvProductoAgregados.getSelectionModel().getSelectedIndex(),
                        lvProductoAgregados.getSelectionModel().getSelectedItem());
                event.consume();
            }
        }
    }

    @FXML
    private void onKeyPressedProductosAgregados(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (lvProductoAgregados.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowDetalleProducto(lvProductoAgregados.getSelectionModel().getSelectedIndex(),
                        lvProductoAgregados.getSelectionModel().getSelectedItem());
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
    private void onKeyPressedProducto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProducto();
        }
    }

    @FXML
    private void onActionProducto(ActionEvent event) {
        onEventProducto();
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
    private void onKeyPressedCobrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCobrar();
        }
    }

    @FXML
    private void onActionCobrar(ActionEvent event) {
        onEventCobrar();
    }

    @FXML
    private void onKeyPressedMovimientoCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventMovimientoCaja();
        }
    }

    @FXML
    private void onActionMovimientoCaja(ActionEvent event) {
        onEventMovimientoCaja();
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
    private void onKeyPressedLimpiar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            cancelarVenta();
        }
    }

    @FXML
    private void onActionLimpiar(ActionEvent event) {
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
    private void onKeyTypedNumeroDocumento(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
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

    public TextField getTxtSearch() {
        return txtSearch;
    }

    public ComboBox<MonedaTB> getCbMoneda() {
        return cbMoneda;
    }

    public ListView<BbItemProducto> getLvProductoAgregados() {
        return lvProductoAgregados;
    }

    public int getIdTipoComprobante() {
        return cbComprobante.getSelectionModel().getSelectedItem().getIdTipoDocumento();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
