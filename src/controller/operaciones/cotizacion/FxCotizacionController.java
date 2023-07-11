package controller.operaciones.cotizacion;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.contactos.clientes.FxClienteProcesoController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ClienteTB;
import model.CotizacionDetalleTB;
import model.CotizacionTB;
import model.MonedaTB;
import model.SuministroTB;
import service.ClienteADO;
import service.CotizacionADO;
import service.MonedaADO;

public class FxCotizacionController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ComboBox<ClienteTB> cbCliente;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private DatePicker dtFechaEmision;
    @FXML
    private DatePicker dtFechaVencimiento;
    @FXML
    private TableView<CotizacionDetalleTB> tvList;
    @FXML
    private TableColumn<CotizacionDetalleTB, Button> tcOpcion;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcUso;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcCantidad;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcMedida;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcProducto;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcImpuesto;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcPrecio;
    @FXML
    private TableColumn<CotizacionDetalleTB, String> tcImporte;
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
    private TextField txtObservacion;
    @FXML
    private Label lblProceso;
    @FXML
    private Label lblComprobante;
    @FXML
    private HBox hbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private String idCotizacion;

    private String idVenta;

    private double importeBrutoTotal;

    private double descuentoTotal;

    private double subImporteNetoTotal;

    private double impuestoTotal;

    private double importeNetoTotal;

    private String monedaSimbolo;

    private final ImageView loadImage = new ImageView(new Image("/view/image/load.gif"));

    private final ImageView successImage = new ImageView(new Image("/view/image/succes_large.png"));

    private final ImageView warningImage = new ImageView(new Image("/view/image/warning_large.png"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        monedaSimbolo = "M";
        idCotizacion = "";
        idVenta = "";
        
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);
        
        loadTableView();
        
        loadComboBoxCliente();
        
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

        loadImage.setFitWidth(120);
        loadImage.setFitHeight(120);

        successImage.setFitWidth(120);
        successImage.setFitHeight(120);

        warningImage.setFitWidth(120);
        warningImage.setFitHeight(120);
    }

    private void openAlertMessageWarning(String message) {
        fxPrincipalController.openFondoModal();
        Tools.AlertMessageWarning(apWindow, "Cotización", message);
        fxPrincipalController.closeFondoModal();
    }

    private void loadTableView() {
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcUso.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().isUso() ? "ASIGNADO" : "LIBRE"));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcMedida.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSuministroTB().getUnidadCompraName()));
        tcPrecio.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getPrecio(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombreImpuesto()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad() * (cellData.getValue().getPrecio() - cellData.getValue().getDescuento()), 2)));

        tcCantidad.setCellFactory(TextFieldTableCell.forTableColumn());
        tcCantidad.setOnEditCommit(data -> {
            final Double newCantidad = Tools.isNumeric(data.getNewValue())
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.valueOf(data.getOldValue()) : Double.valueOf(data.getNewValue()))
                    : Double.valueOf(data.getOldValue());

            CotizacionDetalleTB detalleTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            detalleTB.setCantidad(newCantidad);

            tvList.refresh();
            calculateTotales();
        });

        tcUso.setCellFactory((TableColumn<CotizacionDetalleTB, String> param) -> new TableCell<CotizacionDetalleTB, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item.equalsIgnoreCase("ASIGNADO")) {
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setStyle("-fx-text-fill: #020203;");
                    }
                    setText(item);
                }
            }
        });

        tcPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
        tcPrecio.setOnEditCommit(data -> {
            final Double newPrecio = Tools.isNumeric(data.getNewValue())
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.valueOf(data.getOldValue()) : Double.valueOf(data.getNewValue()))
                    : Double.valueOf(data.getOldValue());

            CotizacionDetalleTB detalleTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            detalleTB.setPrecio(newPrecio);

            tvList.refresh();
            calculateTotales();
        });

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcUso.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.22));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcMedida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcPrecio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

    }

    private void loadComboBoxCliente() {
        SearchComboBox<ClienteTB> searchComboBoxCliente = new SearchComboBox<>(cbCliente, false);
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
            cbCliente.getItems().addAll(ClienteADO.GetSearchComboBoxCliente(4, searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().getText().trim()));
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

    private void onEventCliente() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxClienteProcesoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Cliente", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.loadAddCliente());
        stage.show();
    }

    private void onEventEditar() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            CotizacionDetalleTB select = tvList.getSelectionModel().getSelectedItem();
            SuministroTB suministroSelect = select.getSuministroTB();

            if (select.isUso()) {
                openAlertMessageWarning("No se puede editar un producto ya usado, para ello es mejor clonar la cotización.");
                return;
            }

            SuministroTB suministroTB = new SuministroTB();
            suministroTB.setIdSuministro(suministroSelect.getIdSuministro());
            suministroTB.setClave(suministroSelect.getClave());
            suministroTB.setNombreMarca(suministroSelect.getNombreMarca());
            suministroTB.setUnidadCompra(suministroSelect.getUnidadCompra());
            suministroTB.setUnidadCompraName(suministroSelect.getUnidadCompraName());
            suministroTB.setIdImpuesto(select.getIdImpuesto());
            suministroTB.setImpuestoTB(select.getImpuestoTB());

            suministroTB.setCantidad(select.getCantidad());
            suministroTB.setPrecioVentaGeneral(select.getPrecio());

            fxPrincipalController.openFondoModal();
            URL url = WindowStage.class.getClassLoader().getClass().getResource(FilesRouters.FX_COTIZACION_PRODUCTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxCotizacionProductoController controller = fXMLLoader.getController();
            controller.setInitCotizacionController(this);
            controller.initComponents(suministroTB, true, tvList.getSelectionModel().getSelectedIndex());
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Producto", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.getTxtCantidad().requestFocus());
            stage.show();

        }
    }

    private void onEventGuardar() {
        if (cbCliente.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione un cliente.");
            cbCliente.requestFocus();
            return;
        }

        if (dtFechaVencimiento.getValue() == null) {
            openAlertMessageWarning("Ingrese un fecha valida.");
            dtFechaVencimiento.requestFocus();
            return;
        }

        if (tvList.getItems().isEmpty()) {
            openAlertMessageWarning("Ingrese productos a la lista.");
            return;
        }

        if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione la moneda a usar.");
            cbMoneda.requestFocus();
            return;
        }

        fxPrincipalController.openFondoModal();
        short value = Tools.AlertMessageConfirmation(apWindow, "Cotización", "¿Está seguro de continuar?");

        if (value == 1) {
            ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<Object> task = new Task<Object>() {
                @Override
                protected Object call() throws Exception {
                    CotizacionTB cotizacionTB = new CotizacionTB();
                    cotizacionTB.setIdCotizacion(idCotizacion);
                    cotizacionTB.setIdCliente(cbCliente.getSelectionModel().getSelectedItem().getIdCliente());
                    cotizacionTB.setIdVendedor(Session.USER_ID);
                    cotizacionTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda());
                    cotizacionTB.setFechaCotizacion(Tools.getDatePicker(dtFechaEmision));
                    cotizacionTB.setFechaVencimiento(Tools.getDatePicker(dtFechaVencimiento));
                    cotizacionTB.setHoraCotizacion(Tools.getTime());
                    cotizacionTB.setHoraVencimiento(Tools.getTime());
                    cotizacionTB.setEstado((short) 1);
                    cotizacionTB.setObservaciones(txtObservacion.getText().trim());
                    cotizacionTB.setIdVenta(idVenta);

                    cotizacionTB.setCotizacionDetalleTBs(new ArrayList<>(tvList.getItems()));
                    Object result = CotizacionADO.CrudCotizacion(cotizacionTB);
                    if (result instanceof String[]) {
                        return result;
                    }

                    throw new Exception((String) result);
                }
            };

            task.setOnScheduled(w -> {
                fxPrincipalController.closeFondoModal();
                hbBody.setDisable(true);
                hbLoad.setVisible(true);
                btnAceptarLoad.setVisible(false);
                lblMessageLoad.setText("Procesando información...");
                lblMessageLoad.setTextFill(Color.web("#ffffff"));
                lblMessageLoad.setGraphic(loadImage);
            });

            task.setOnFailed(w -> {
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetCotizacion();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetCotizacion();
                    }
                });

                lblMessageLoad.setText(task.getException().getLocalizedMessage());
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                lblMessageLoad.setGraphic(warningImage);
            });

            task.setOnSucceeded((WorkerStateEvent w) -> {
                String result[] = (String[]) task.getValue();
                if (result[0].equalsIgnoreCase("1")) {
                    lblMessageLoad.setText("Se actualizó correctamente la cotización.");
                    lblMessageLoad.setTextFill(Color.web("#ffffff"));
                    lblMessageLoad.setGraphic(successImage);

                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    openModalImpresion(result[1]);
                    resetCotizacion();
                } else {
                    lblMessageLoad.setText("Se registró corectamente la cotización.");
                    lblMessageLoad.setTextFill(Color.web("#ffffff"));
                    lblMessageLoad.setGraphic(successImage);

                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    openModalImpresion(result[1]);
                    resetCotizacion();
                }
            });

            exec.execute(task);

            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        } else {
            fxPrincipalController.closeFondoModal();
        }
    }

    private void onEventCancelar() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Cotización", "¿Está seguro de limpiar la cotización?");
        if (value == 1) {
            resetCotizacion();
        }
    }

    public void getAddDetalle(CotizacionDetalleTB cotizacionDetalleTB) {
        cotizacionDetalleTB.getBtnRemove().setOnAction(e -> {
            if (cotizacionDetalleTB.isUso()) {
                openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                return;
            }

            tvList.getItems().remove(cotizacionDetalleTB);
            calculateTotales();
        });

        cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (cotizacionDetalleTB.isUso()) {
                    openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                    return;
                }

                tvList.getItems().remove(cotizacionDetalleTB);
                calculateTotales();
            }
        });

        tvList.getItems().add(cotizacionDetalleTB);
        int index = tvList.getItems().size() - 1;
        tvList.getSelectionModel().select(index);

        calculateTotales();
    }

    public void getEditDetalle(int index, CotizacionDetalleTB cotizacionDetalleTB) {
        cotizacionDetalleTB.getBtnRemove().setOnAction(e -> {
            if (cotizacionDetalleTB.isUso()) {
                openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                return;
            }

            tvList.getItems().remove(cotizacionDetalleTB);
            calculateTotales();
        });

        cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (cotizacionDetalleTB.isUso()) {
                    openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                    return;
                }

                tvList.getItems().remove(cotizacionDetalleTB);
                calculateTotales();
            }
        });

        tvList.getItems().set(index, cotizacionDetalleTB);

        calculateTotales();
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

    public void calculateTotales() {
        importeBrutoTotal = 0;
        descuentoTotal = 0;
        subImporteNetoTotal = 0;
        impuestoTotal = 0;
        importeNetoTotal = 0;

        tvList.getItems().forEach(ocdtb -> {
            double importeBruto = ocdtb.getPrecio() * ocdtb.getCantidad();
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

        lblValorVenta.setText(monedaSimbolo + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(monedaSimbolo + " " + (Tools.roundingValue(descuentoTotal * (-1), 2)));
        lblSubImporte.setText(monedaSimbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(monedaSimbolo + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    public void resetCotizacion() {
        tvList.getItems().clear();
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        cbCliente.getItems().clear();
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);
        txtObservacion.setText("");
        idCotizacion = "";
        idVenta = "";
        lblProceso.setText("Proceso de Creación");
        lblProceso.setTextFill(Color.web("#0060e8"));
        lblComprobante.setText("Comprobante Asociado: -");
        cbMoneda.getItems().clear();
        Object monedaObject = MonedaADO.ObtenerListaMonedas();
        if (monedaObject instanceof ObservableList) {
            cbMoneda.setItems((ObservableList<MonedaTB>) monedaObject);
        }

        for (int i = 0; i < cbMoneda.getItems().size(); i++) {
            if (cbMoneda.getItems().get(i).isPredeterminado()) {
                cbMoneda.getSelectionModel().select(i);
                monedaSimbolo = cbMoneda.getItems().get(i).getSimbolo();
                break;
            }
        }
        calculateTotales();
    }

    public void loadCotizacion(String idCotizacion) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws Exception {
                Object result = CotizacionADO.Obtener_Cotizacion_ById(idCotizacion, false);

                if (result instanceof CotizacionTB) {
                    return result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(w -> {
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            lblMessageLoad.setGraphic(loadImage);
        });

        task.setOnFailed(w -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                resetCotizacion();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetCotizacion();
                }
                event.consume();
            });

            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            lblMessageLoad.setGraphic(warningImage);
        });

        task.setOnSucceeded(w -> {
            CotizacionTB cotizacionTB = (CotizacionTB) task.getValue();

            this.idCotizacion = idCotizacion;
            idVenta = cotizacionTB.getIdVenta();
            for (MonedaTB monedaTB : cbMoneda.getItems()) {
                if (monedaTB.getIdMoneda() == cotizacionTB.getIdMoneda()) {
                    cbMoneda.getSelectionModel().select(monedaTB);
                    monedaSimbolo = cbMoneda.getSelectionModel().getSelectedItem().getSimbolo();
                    break;
                }
            }

            cbCliente.getItems().clear();
            cbCliente.getItems().add(new ClienteTB(cotizacionTB.getClienteTB().getIdCliente(), cotizacionTB.getClienteTB().getNumeroDocumento(), cotizacionTB.getClienteTB().getInformacion(), cotizacionTB.getClienteTB().getCelular(), cotizacionTB.getClienteTB().getEmail(), cotizacionTB.getClienteTB().getDireccion()));
            cbCliente.getSelectionModel().select(0);

            Tools.actualDate(Tools.getDate(), dtFechaEmision);
            Tools.actualDate(Tools.getDate(), dtFechaVencimiento);

            lblProceso.setText("Proceso de Actualización");
            lblProceso.setTextFill(Color.web("#c52700"));
            if (cotizacionTB.getVentaTB() != null) {
                lblComprobante.setText("Comprobante Asociado: " + cotizacionTB.getVentaTB().getSerie() + "-" + Tools.formatNumber(cotizacionTB.getVentaTB().getNumeracion()));
            } else {
                lblComprobante.setText("Comprobante Asociado: -");
            }
            txtObservacion.setText(cotizacionTB.getObservaciones());

            ObservableList<CotizacionDetalleTB> cotizacionDetalleTBs = FXCollections.observableArrayList(cotizacionTB.getCotizacionDetalleTBs());
            cotizacionDetalleTBs.forEach(cotizacionDetalleTB -> {
                cotizacionDetalleTB.getBtnRemove().setOnAction(e -> {
                    if (cotizacionDetalleTB.isUso()) {
                        openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                        return;
                    }
                    tvList.getItems().remove(cotizacionDetalleTB);
                    calculateTotales();
                });
                cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        if (cotizacionDetalleTB.isUso()) {
                            openAlertMessageWarning("No se puede eliminar un producto ya usado, para ello es mejor clonar la cotización.");
                            return;
                        }
                        tvList.getItems().remove(cotizacionDetalleTB);
                        calculateTotales();
                    }
                });
            });
            tvList.setItems(cotizacionDetalleTBs);

            calculateTotales();

            hbBody.setDisable(false);
            hbLoad.setVisible(false);
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowSuministro() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxSuministrosListaController controller = fXMLLoader.getController();
        controller.setInitCotizacionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.getTxtSearch().requestFocus());
        stage.show();
    }

    private void openModalImpresion(String idCotizacion) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketCotizacion(controller.getApWindow());
            controller.setIdCotizacion(idCotizacion);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
        }
    }

    private void openWindowCotizaciones() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_COTIZACION_LISTA);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxCotizacionListaController controller = fXMLLoader.getController();
        controller.setInitCotizacionListaController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar Cotizaciones", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.initLoad());
        stage.show();
    }

    @FXML
    private void onKeyPressedCliente(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCliente();
        }
    }

    @FXML
    private void onActionCliente(ActionEvent event) throws IOException {
        onEventCliente();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) throws IOException {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedProducto(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
            event.consume();
        }
    }

    @FXML
    private void onActionProducto(ActionEvent event) throws IOException {
        openWindowSuministro();
    }

    @FXML
    private void onKeyPressedEditar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEditar();
            event.consume();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) throws IOException {
        onEventEditar();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCancelar();
            event.consume();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        onEventCancelar();
    }

    @FXML
    private void onActionMoneda(ActionEvent event) {

    }

    @FXML
    private void onKeyPressedCotizaciones(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCotizaciones();
        }
    }

    @FXML
    private void onActionCotizaciones(ActionEvent event) throws IOException {
        openWindowCotizaciones();
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) throws IOException {
        switch (event.getCode()) {
            case F1:
                onEventGuardar();
                break;
            case F2:
                openWindowSuministro();
                break;
            case F3:
                openWindowCotizaciones();
                break;
            case F4:

                break;
            case F5:
                onEventCancelar();
                break;
        }
    }

    public TableView<CotizacionDetalleTB> getTvList() {
        return tvList;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
