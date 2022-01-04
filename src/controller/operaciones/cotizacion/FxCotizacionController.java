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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ClienteADO;
import model.ClienteTB;
import model.CotizacionADO;
import model.CotizacionTB;
import model.DetalleCotizacionTB;
import model.MonedaADO;
import model.MonedaTB;
import model.SuministroTB;

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
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, Button> tcOpcion;
    @FXML
    private TableColumn<SuministroTB, String> tcCantidad;
    @FXML
    private TableColumn<SuministroTB, String> tcProducto;
    @FXML
    private TableColumn<SuministroTB, String> tcImpuesto;
    @FXML
    private TableColumn<SuministroTB, String> tcPrecio;
    @FXML
    private TableColumn<SuministroTB, String> tcImporte;
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
    private HBox hbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private String idCotizacion;

    private double importeBruto;

    private double descuentoBruto;

    private double subImporteNeto;

    private double impuestoNeto;

    private double importeNeto;

    private String monedaSimbolo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        monedaSimbolo = "M";
        idCotizacion = "";
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);
        loadTableView();
        loadComboBoxCliente();
        cbMoneda.getItems().clear();
        cbMoneda.getItems().addAll(MonedaADO.GetMonedasComboBox());
        if (!cbMoneda.getItems().isEmpty()) {
            for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                if (cbMoneda.getItems().get(i).getPredeterminado()) {
                    cbMoneda.getSelectionModel().select(i);
                    monedaSimbolo = cbMoneda.getItems().get(i).getSimbolo();
                    break;
                }
            }
        }
    }

    private void loadTableView() {
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getClave() + "\n" + cellData.getValue().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcPrecio.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getPrecioVentaGeneral(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombreImpuesto()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getImporteNeto(), 2)));

        tcCantidad.setCellFactory(TextFieldTableCell.forTableColumn());
        tcCantidad.setOnEditCommit(data -> {
            final Double newCantidad = Tools.isNumeric(data.getNewValue())
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue()) : Double.parseDouble(data.getNewValue()))
                    : Double.parseDouble(data.getOldValue());

            SuministroTB suministroTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            suministroTB.setCantidad(newCantidad);
            double descuento = suministroTB.getDescuento();
            double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
            suministroTB.setImporteNeto(suministroTB.getCantidad() * precioDescuento);

            tvList.refresh();
            calculateTotales();
        });

        tcPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
        tcPrecio.setOnEditCommit(data -> {
            final Double newPrecio = Tools.isNumeric(data.getNewValue())
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue()) : Double.parseDouble(data.getNewValue()))
                    : Double.parseDouble(data.getOldValue());

            SuministroTB suministroTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            suministroTB.setPrecioVentaGeneral(newPrecio);
            double descuento = suministroTB.getDescuento();
            double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
            suministroTB.setImporteNeto(suministroTB.getCantidad() * precioDescuento);

            tvList.refresh();
            calculateTotales();
        });

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.08));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.34));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcPrecio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
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

    private void onEventCliente() {
        try {
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
            stage.show();
            controller.setValueAdd();
        } catch (IOException ex) {
            System.out.println("Cliente controller en openWindowAddCliente()" + ex.getLocalizedMessage());
        }
    }

    private void openWindowSuministro() {
        try {
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
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
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
            System.out.println("Controller Modal Impresión: " + ex.getLocalizedMessage());
        }
    }

    private void openWindowCotizaciones() {
        try {
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
        } catch (IOException ex) {
            Tools.println("Error en la funcioón openWindowCotizaciones():" + ex.getLocalizedMessage());
        }
    }

    public void getAddArticulo(SuministroTB suministro) {
        if (validateDuplicate(suministro)) {
            for (int i = 0; i < tvList.getItems().size(); i++) {
                if (tvList.getItems().get(i).getIdSuministro().equalsIgnoreCase(suministro.getIdSuministro())) {
                    SuministroTB suministroTB = tvList.getItems().get(i);
                    suministroTB.setPrecioVentaGeneral(suministro.getPrecioVentaGeneral());
                    suministroTB.setCantidad(suministro.getCantidad());
                    double descuento = suministroTB.getDescuento();
                    double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
                    suministroTB.setImporteNeto(suministroTB.getCantidad() * precioDescuento);
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
        }
    }

    public boolean validateDuplicate(SuministroTB suministroTB) {
        boolean ret = false;
        for (int i = 0; i < tvList.getItems().size(); i++) {
            if (tvList.getItems().get(i).getClave().equals(suministroTB.getClave())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void calculateTotales() {
        importeBruto = 0;
        tvList.getItems().forEach(e -> {
            double descuento = e.getDescuento();
            double precioDescuento = e.getPrecioVentaGeneral() - descuento;
            double subPrecio = Tools.calculateTaxBruto(e.getImpuestoTB().getValor(), precioDescuento);
            double precioBruto = subPrecio + descuento;
            importeBruto += precioBruto * e.getCantidad();
        });
        lblValorVenta.setText(monedaSimbolo + " " + Tools.roundingValue(importeBruto, 2));

        descuentoBruto = 0;
        tvList.getItems().forEach(e -> {
            double descuento = e.getDescuento();
            descuentoBruto += e.getCantidad() * descuento;
        });
        lblDescuento.setText(monedaSimbolo + " " + (Tools.roundingValue(descuentoBruto * (-1), 2)));

        subImporteNeto = 0;
        tvList.getItems().forEach(e -> {
            double descuento = e.getDescuento();
            double precioDescuento = e.getPrecioVentaGeneral() - descuento;
            double subPrecio = Tools.calculateTaxBruto(e.getImpuestoTB().getValor(), precioDescuento);
            subImporteNeto += e.getCantidad() * subPrecio;
        });
        lblSubImporte.setText(monedaSimbolo + " " + Tools.roundingValue(subImporteNeto, 2));

        impuestoNeto = 0;
        tvList.getItems().forEach(e -> {
            double descuento = e.getDescuento();
            double precioDescuento = e.getPrecioVentaGeneral() - descuento;
            double subPrecio = Tools.calculateTaxBruto(e.getImpuestoTB().getValor(), precioDescuento);
            double impuesto = Tools.calculateTax(e.getImpuestoTB().getValor(), subPrecio);
            impuestoNeto += e.getCantidad() * impuesto;
        });

        importeNeto = 0;
        importeNeto = subImporteNeto + impuestoNeto;
        lblImpuesto.setText(monedaSimbolo + " " + Tools.roundingValue(impuestoNeto, 2));
        lblImporteTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNeto, 2));
    }

    private void cancelarVenta() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Cotización", "¿Está seguro de limpiar la cotización?");
        if (value == 1) {
            resetVenta();
        }
    }

    public void resetVenta() {
        tvList.getItems().clear();
        cbCliente.getItems().clear();
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);
        txtObservacion.setText("");
        idCotizacion = "";
        lblProceso.setText("Cotización en proceso de registrar");
        lblProceso.setTextFill(Color.web("#ffffff"));
        cbMoneda.getItems().clear();
        cbMoneda.getItems().addAll(MonedaADO.GetMonedasComboBox());
        for (int i = 0; i < cbMoneda.getItems().size(); i++) {
            if (cbMoneda.getItems().get(i).getPredeterminado()) {
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
            public Object call() {
                return CotizacionADO.CargarCotizacionEditar(idCotizacion);
            }
        };
        task.setOnScheduled(w -> {
            hbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });
        task.setOnFailed(w -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
                resetVenta();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetVenta();
                }
                event.consume();
            });
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof CotizacionTB) {
                CotizacionTB cotizacionTB = (CotizacionTB) object;
                this.idCotizacion = idCotizacion;
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

//                dtFechaEmision.setValue(LocalDate.parse(cotizacionTB.getFechaCotizacion()));
//                dtFechaVencimiento.setValue(LocalDate.parse(cotizacionTB.getFechaVencimiento()));
                lblProceso.setText("Cotización en proceso de actualizar");
                lblProceso.setTextFill(Color.web("#c52700"));

                ObservableList<SuministroTB> cotizacionTBs = cotizacionTB.getDetalleSuministroTBs();
                for (int i = 0; i < cotizacionTBs.size(); i++) {
                    SuministroTB suministroTB = cotizacionTBs.get(i);
                    suministroTB.getBtnRemove().setOnAction(e -> {
                        tvList.getItems().remove(suministroTB);
                        calculateTotales();
                    });
                    suministroTB.getBtnRemove().setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            tvList.getItems().remove(suministroTB);
                            calculateTotales();
                        }
                    });
                }
                tvList.setItems(cotizacionTBs);

                calculateTotales();
                hbBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetVenta();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetVenta();
                    }
                    event.consume();
                });
            }
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventGuardar() {
        if (cbCliente.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Seleccione un cliente.");
            cbCliente.requestFocus();
        } else if (dtFechaVencimiento.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese un fecha valida.");
            dtFechaVencimiento.requestFocus();
        } else if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Ingrese productos a la lista.");
        } else if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Cotización", "Seleccione la moneda a usar.");
            cbMoneda.requestFocus();
        } else {
            short value = Tools.AlertMessageConfirmation(apWindow, "Cotización", "¿Está seguro de continuar?");
            if (value == 1) {
                ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                    Thread t = new Thread(runnable);
                    t.setDaemon(true);
                    return t;
                });

                Task<Object> task = new Task<Object>() {
                    @Override
                    protected Object call() {
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

                        ArrayList<DetalleCotizacionTB> detalleCotizacionTBs = new ArrayList();
                        tvList.getItems().stream().map((suministroTB) -> {
                            DetalleCotizacionTB detalleCotizacionTB = new DetalleCotizacionTB();
                            detalleCotizacionTB.setIdSuministros(suministroTB.getIdSuministro());
                            detalleCotizacionTB.setCantidad(suministroTB.getCantidad());
                            detalleCotizacionTB.setPrecio(suministroTB.getPrecioVentaGeneral());
                            detalleCotizacionTB.setDescuento(suministroTB.getDescuento());
                            detalleCotizacionTB.setIdImpuesto(suministroTB.getIdImpuesto());
                            return detalleCotizacionTB;
                        }).forEachOrdered((detalleCotizacionTB) -> {
                            detalleCotizacionTBs.add(detalleCotizacionTB);
                        });
                        cotizacionTB.setDetalleCotizacionTBs(detalleCotizacionTBs);

                        return CotizacionADO.CrudCotizacion(cotizacionTB);
                    }
                };

                task.setOnScheduled(w -> {
                    hbBody.setDisable(true);
                    hbLoad.setVisible(true);
                    btnAceptarLoad.setVisible(false);
                    lblMessageLoad.setText("Procesando información...");
                    lblMessageLoad.setTextFill(Color.web("#ffffff"));
                });
                task.setOnFailed(w -> {
                    btnAceptarLoad.setVisible(true);
                    btnAceptarLoad.setOnAction(event -> {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetVenta();
                    });
                    btnAceptarLoad.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            resetVenta();
                        }
                    });
                    lblMessageLoad.setText(task.getException().getLocalizedMessage());
                    lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                });
                task.setOnSucceeded(w -> {
                    Object object = task.getValue();
                    if (object instanceof String[]) {
                        String result[] = (String[]) object;
                        if (result[0].equalsIgnoreCase("1")) {
                            lblMessageLoad.setText("Se actualizó correctamente la cotización.");
                            lblMessageLoad.setTextFill(Color.web("#ffffff"));
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            openModalImpresion(result[1]);
                            resetVenta();
                        } else {
                            lblMessageLoad.setText("Se registro corectamente la cotización.");
                            lblMessageLoad.setTextFill(Color.web("#ffffff"));
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            openModalImpresion(result[1]);
                            resetVenta();
                        }
                    } else {
                        lblMessageLoad.setText((String) object);
                        lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                        btnAceptarLoad.setVisible(true);
                        btnAceptarLoad.setOnAction(event -> {
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            resetVenta();
                        });
                        btnAceptarLoad.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                hbBody.setDisable(false);
                                hbLoad.setVisible(false);
                                resetVenta();
                            }
                        });
                    }
                });

                exec.execute(task);

                if (!exec.isShutdown()) {
                    exec.shutdown();
                }
            }

        }
    }

    @FXML
    private void onKeyPressedCliente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCliente();
        }
    }

    @FXML
    private void onActionCliente(ActionEvent event) {
        onEventCliente();
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventGuardar();
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
    private void onActionMoneda(ActionEvent event) {

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
        if (null != event.getCode()) {
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
                    cancelarVenta();
                    break;
                default:
                    break;
            }
        }
    }

    public TableView<SuministroTB> getTvList() {
        return tvList;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
