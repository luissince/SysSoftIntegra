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
import model.CotizacionDetalleTB;
import model.CotizacionTB;
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
    private TableView<CotizacionDetalleTB> tvList;
    @FXML
    private TableColumn<CotizacionDetalleTB, Button> tcOpcion;
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
    private HBox hbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private String idCotizacion;

    private double importeBrutoTotal;

    private double descuentoTotal;

    private double subImporteNetoTotal;

    private double impuestoTotal;

    private double importeNetoTotal;

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
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue()) : Double.parseDouble(data.getNewValue()))
                    : Double.parseDouble(data.getOldValue());

            CotizacionDetalleTB detalleTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            detalleTB.setCantidad(newCantidad);

            tvList.refresh();
            calculateTotales();
        });

        tcPrecio.setCellFactory(TextFieldTableCell.forTableColumn());
        tcPrecio.setOnEditCommit(data -> {
            final Double newPrecio = Tools.isNumeric(data.getNewValue())
                    ? (Double.parseDouble(data.getNewValue()) <= 0 ? Double.parseDouble(data.getOldValue()) : Double.parseDouble(data.getNewValue()))
                    : Double.parseDouble(data.getOldValue());

            CotizacionDetalleTB detalleTB = data.getTableView().getItems().get(data.getTablePosition().getRow());
            detalleTB.setPrecio(newPrecio);

            tvList.refresh();
            calculateTotales();
        });

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.32));
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

    private void onEventEditar() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            try {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(tvList.getSelectionModel().getSelectedItem().getSuministroTB().getIdSuministro());
                suministroTB.setClave(tvList.getSelectionModel().getSelectedItem().getSuministroTB().getClave());
                suministroTB.setNombreMarca(tvList.getSelectionModel().getSelectedItem().getSuministroTB().getNombreMarca());
                suministroTB.setUnidadCompra(tvList.getSelectionModel().getSelectedItem().getSuministroTB().getUnidadCompra());
                suministroTB.setUnidadCompraName(tvList.getSelectionModel().getSelectedItem().getSuministroTB().getUnidadCompraName());
                suministroTB.setIdImpuesto(tvList.getSelectionModel().getSelectedItem().getIdImpuesto());
                suministroTB.setImpuestoTB(tvList.getSelectionModel().getSelectedItem().getImpuestoTB());

                suministroTB.setCantidad(tvList.getSelectionModel().getSelectedItem().getCantidad());
                suministroTB.setPrecioVentaGeneral(tvList.getSelectionModel().getSelectedItem().getPrecio());

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
            } catch (IOException ix) {
                System.out.println("Error Producto Lista Controller:" + ix.getLocalizedMessage());
            }
        }
    }

    public void getAddDetalle(CotizacionDetalleTB cotizacionDetalleTB) {
        cotizacionDetalleTB.getBtnRemove().setOnAction(e -> {
            tvList.getItems().remove(cotizacionDetalleTB);
            calculateTotales();
        });

        cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
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
            tvList.getItems().remove(cotizacionDetalleTB);
            calculateTotales();
        });

        cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
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

    private void cancelarVenta() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Cotización", "¿Está seguro de limpiar la cotización?");
        if (value == 1) {
            resetVenta();
        }
    }

    public void resetVenta() {
        tvList.getItems().clear();
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
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
                return CotizacionADO.Obtener_Cotizacion_ById(idCotizacion);
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

                Tools.actualDate(Tools.getDate(), dtFechaEmision);
                Tools.actualDate(Tools.getDate(), dtFechaVencimiento);

                lblProceso.setText("Cotización en proceso de actualizar");
                lblProceso.setTextFill(Color.web("#c52700"));
                txtObservacion.setText(cotizacionTB.getObservaciones());

                ObservableList<CotizacionDetalleTB> cotizacionDetalleTBs = FXCollections.observableArrayList(cotizacionTB.getCotizacionDetalleTBs());
                cotizacionDetalleTBs.forEach(cotizacionDetalleTB -> {
                    cotizacionDetalleTB.getBtnRemove().setOnAction(e -> {
                        tvList.getItems().remove(cotizacionDetalleTB);
                        calculateTotales();
                    });
                    cotizacionDetalleTB.getBtnRemove().setOnKeyPressed(e -> {
                        if (e.getCode() == KeyCode.ENTER) {
                            tvList.getItems().remove(cotizacionDetalleTB);
                            calculateTotales();
                        }
                    });
                });
                tvList.setItems(cotizacionDetalleTBs);

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
                        cotizacionTB.setIdVenta("");

                        cotizacionTB.setCotizacionDetalleTBs(new ArrayList<>(tvList.getItems()));
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
    private void onKeyPressedEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEditar();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
        onEventEditar();
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
                cancelarVenta();
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
