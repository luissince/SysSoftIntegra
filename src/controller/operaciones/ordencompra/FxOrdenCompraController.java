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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.MonedaADO;
import model.MonedaTB;
import model.OrdenCompraADO;
import model.OrdenCompraDetalleTB;
import model.OrdenCompraTB;
import model.ProveedorADO;
import model.ProveedorTB;

public class FxOrdenCompraController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private HBox hbBody;
    @FXML
    private ComboBox<ProveedorTB> cbProveedor;
    @FXML
    private DatePicker dtFechaEmision;
    @FXML
    private DatePicker dtFechaVencimiento;
    @FXML
    private Label lblProceso;
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
    private ComboBox<MonedaTB> cbMoneda;
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
    private Button btnProducto;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblCambioMonedaMonto;
    @FXML
    private Label lblCambioMonedaTexto;

    private FxPrincipalController principalController;

    private String idOrdenCompra;

    private double importeBrutoTotal;

    private double descuentoTotal;

    private double subImporteNetoTotal;

    private double impuestoTotal;

    private double importeNetoTotal;

    private String monedaSimbolo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);

        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(monedaSimbolo + " " + Tools.roundingValue(cellData.getValue().getCosto(), 2)));
        tcImpuesto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getImpuestoTB().getNombre()));
        tcImporte.setCellValueFactory(cellData -> Bindings.concat(monedaSimbolo + " " + Tools.roundingValue(cellData.getValue().getCantidad() * cellData.getValue().getCosto(), 2)));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservacion()));

        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.08));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.24));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcCosto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImpuesto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcImporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcObservacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));

        loadComboBoxProveedor();
        cbMoneda.getItems().clear();
        cbMoneda.getItems().addAll(MonedaADO.GetMonedasComboBox());
        for (int i = 0; i < cbMoneda.getItems().size(); i++) {
            if (cbMoneda.getItems().get(i).isPredeterminado()) {
                cbMoneda.getSelectionModel().select(i);
                monedaSimbolo = cbMoneda.getItems().get(i).getSimbolo();
                break;
            }
        }
    }

    private void loadComboBoxProveedor() {
        SearchComboBox<ProveedorTB> scbProveedor = new SearchComboBox<>(cbProveedor, false);
        scbProveedor.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!scbProveedor.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    scbProveedor.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    scbProveedor.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                scbProveedor.getComboBox().hide();
            }
        });
        scbProveedor.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (scbProveedor.getCompletableFuture() != null) {
                return;
            }
            if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB) {
                return;
            }
            Platform.runLater(() -> {
                scbProveedor.getComboBox().getItems().clear();
                scbProveedor.getComboBox().setPromptText("Cargando información...");
            });
            scbProveedor.setCompletableFuture(CompletableFuture.supplyAsync(() -> {
                return ProveedorADO.getSearchComboBoxProveedores(scbProveedor.getSearchComboBoxSkin().getSearchBox().getText().trim());
            }).thenAcceptAsync(complete -> {
                if (complete instanceof List) {
                    Platform.runLater(() -> {
                        scbProveedor.getComboBox().getItems().addAll(complete);
                        scbProveedor.getComboBox().setPromptText("- Seleccione -");
                        scbProveedor.setCompletableFuture(null);
                    });
                }
            }));
            t.consume();
        });
        scbProveedor.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                scbProveedor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                scbProveedor.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        scbProveedor.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        scbProveedor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        scbProveedor.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        scbProveedor.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                scbProveedor.getComboBox().getSelectionModel().select(item);
                if (scbProveedor.getSearchComboBoxSkin().isClickSelection()) {
                    scbProveedor.getComboBox().hide();
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

    private void openWindowEditSuministro(OrdenCompraDetalleTB ordenCompraDetalleTB) {
        try {
            URL url = getClass().getResource(FilesRouters.FX_ORDEN_COMPRA_PRODUCTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOrdenCompraProductoController controller = fXMLLoader.getController();
            controller.setInitOrdenCompraController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Editar Producto", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> {
                if (!controller.getCompletableFuture().isDone()) {
                    controller.getCompletableFuture().cancel(true);
                }
            });
            stage.setOnShown(e -> controller.loadEditComponent(ordenCompraDetalleTB));
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en suministro orden de compra:" + ex.getLocalizedMessage());
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

        tvList.getItems().forEach(ocdtb -> {
            double importeBruto = ocdtb.getCosto() * ocdtb.getCantidad();
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
        lblDescuento.setText(monedaSimbolo + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporte.setText(monedaSimbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(monedaSimbolo + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteTotal.setText(monedaSimbolo + " " + Tools.roundingValue(importeNetoTotal, 2));

        if (cbMoneda.getSelectionModel().getSelectedIndex() >= 0) {
            double cambio = cbMoneda.getSelectionModel().getSelectedItem().getTipoCambio();
            if (cbMoneda.getSelectionModel().getSelectedItem().isPredeterminado()) {
                lblCambioMonedaTexto.setText("");
                lblCambioMonedaMonto.setText("");
            } else {
                for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                    if (cbMoneda.getItems().get(i).isPredeterminado()) {
                        lblCambioMonedaTexto.setText("Importe Neto " + cbMoneda.getItems().get(i).getAbreviado() + ":");
                        lblCambioMonedaMonto.setText(cbMoneda.getItems().get(i).getSimbolo() + " " + Tools.roundingValue(cambio * importeNetoTotal, 2));
                        break;
                    }
                }
            }
        }

        tvList.refresh();
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

    private void onEventGuardarOrdenCompra() {
        if (cbProveedor.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Seleccione su provedor.");
            cbProveedor.requestFocus();
            return;
        }

        if (dtFechaVencimiento.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Ingrese la fecha de entrega.");
            dtFechaVencimiento.requestFocus();
            return;
        }

        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "No hay productos en la lista.");
            btnProducto.requestFocus();
            return;
        }

        if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Orden de Compra", "Seleccione el tipo de moneda.");
            cbMoneda.requestFocus();
            return;
        }

        short value = Tools.AlertMessageConfirmation(apWindow, "Orden de Compra", "¿Está seguro de continuar?");
        if (value == 1) {
            ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<String> task = new Task<String>() {
                @Override
                protected String call() {
                    OrdenCompraTB compraTB = new OrdenCompraTB();
                    compraTB.setIdOrdenCompra(idOrdenCompra);
                    compraTB.setNumeracion(0);
                    compraTB.setIdEmpleado(Session.USER_ID);
                    compraTB.setIdProveedor(cbProveedor.getSelectionModel().getSelectedItem().getIdProveedor());
                    compraTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda());
                    compraTB.setTipoCambio(cbMoneda.getSelectionModel().getSelectedItem().getTipoCambio());
                    compraTB.setFechaRegistro(Tools.getDatePicker(dtFechaEmision));
                    compraTB.setHoraRegistro(Tools.getTime());
                    compraTB.setFechaVencimiento(Tools.getDatePicker(dtFechaVencimiento));
                    compraTB.setHoraVencimiento(Tools.getTime());
                    compraTB.setObservacion(txtObservacion.getText().trim());
                    compraTB.setOrdenCompraDetalleTBs(new ArrayList<>(tvList.getItems()));

                    return OrdenCompraADO.InsertarOrdenCompra(compraTB);
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
                    resetOrdenCompra();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetOrdenCompra();
                    }
                });
                lblMessageLoad.setText(task.getException().getLocalizedMessage());
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            });
            task.setOnSucceeded(w -> {
                String result = task.getValue();
                if (result.equalsIgnoreCase("inserted")) {
                    lblMessageLoad.setText("Se registró correctamente la orden de compra.");
                    lblMessageLoad.setTextFill(Color.web("#ffffff"));
                    btnAceptarLoad.setVisible(true);
                    btnAceptarLoad.setOnAction(event -> {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetOrdenCompra();
                    });
                    btnAceptarLoad.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            resetOrdenCompra();
                        }
                    });
                } else if (result.equalsIgnoreCase("updated")) {
                    lblMessageLoad.setText("se actualizó correctamente la orden de compra.");
                    lblMessageLoad.setTextFill(Color.web("#ffffff"));
                    btnAceptarLoad.setVisible(true);
                    btnAceptarLoad.setOnAction(event -> {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetOrdenCompra();
                    });
                    btnAceptarLoad.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            resetOrdenCompra();
                        }
                    });
                } else {
                    lblMessageLoad.setText(result);
                    lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                    btnAceptarLoad.setVisible(true);
                    btnAceptarLoad.setOnAction(event -> {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetOrdenCompra();
                    });
                    btnAceptarLoad.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            hbBody.setDisable(false);
                            hbLoad.setVisible(false);
                            resetOrdenCompra();
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

    public void resetOrdenCompra() {
        idOrdenCompra = "";
        cbProveedor.getItems().clear();
        Tools.actualDate(Tools.getDate(), dtFechaEmision);
        Tools.actualDate(Tools.getDate(), dtFechaVencimiento);
        tvList.getItems().clear();
        lblProceso.setText("Orden de compra en proceso de registrar");
        lblProceso.setTextFill(Color.web("#0060e8"));
        txtObservacion.clear();
        cbMoneda.getItems().clear();
        cbMoneda.getItems().addAll(MonedaADO.GetMonedasComboBox());
        for (int i = 0; i < cbMoneda.getItems().size(); i++) {
            if (cbMoneda.getItems().get(i).isPredeterminado()) {
                cbMoneda.getSelectionModel().select(i);
                monedaSimbolo = cbMoneda.getItems().get(i).getSimbolo();
                break;
            }
        }
        calculateTotales();
    }

    private void openWindowOrdenCompra() {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_ORDEN_COMPRA_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOrdenCompraListaController controller = fXMLLoader.getController();
            controller.setInitOrdenCompraListarController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar Ordenes de Compra", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            Tools.println("Error en la función openWindowOrdenCompra():" + ex.getLocalizedMessage());
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
                resetOrdenCompra();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetOrdenCompra();
                }
                event.consume();
            });
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof OrdenCompraTB) {
                OrdenCompraTB ordenCompraTB = (OrdenCompraTB) object;
                this.idOrdenCompra = idOrdenCompra;

                cbProveedor.getItems().clear();
                cbProveedor.getItems().add(new ProveedorTB(ordenCompraTB.getProveedorTB().getIdProveedor(), ordenCompraTB.getProveedorTB().getNumeroDocumento(), ordenCompraTB.getProveedorTB().getRazonSocial()));
                cbProveedor.getSelectionModel().select(0);

                lblProceso.setText("Orden de Compra en proceso de actualizar");
                lblProceso.setTextFill(Color.web("#c52700"));

                Tools.actualDate(Tools.getDate(), dtFechaEmision);
                Tools.actualDate(Tools.getDate(), dtFechaVencimiento);

                ObservableList<OrdenCompraDetalleTB> compraDetalleTBs = FXCollections.observableArrayList(ordenCompraTB.getOrdenCompraDetalleTBs());
                compraDetalleTBs.forEach(compraDetalleTB -> {
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
                });

                for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                    if (cbMoneda.getItems().get(i).getIdMoneda() == ordenCompraTB.getMonedaTB().getIdMoneda()) {
                        cbMoneda.getSelectionModel().select(i);
                        break;
                    }
                }

                tvList.setItems(compraDetalleTBs);
                calculateTotales();

                txtObservacion.setText(ordenCompraTB.getObservacion());

                hbBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    hbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetOrdenCompra();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        hbBody.setDisable(false);
                        hbLoad.setVisible(false);
                        resetOrdenCompra();
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

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardarOrdenCompra();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventGuardarOrdenCompra();
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
    private void onKeyPressedEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowEditSuministro(tvList.getSelectionModel().getSelectedItem());
            }
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowEditSuministro(tvList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void onKeyPressedLimpiar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            resetOrdenCompra();
        }
    }

    @FXML
    private void onActionLimpiar(ActionEvent event) {
        resetOrdenCompra();
    }

    @FXML
    private void onActionMoneda(ActionEvent event) {
        if (cbMoneda.getSelectionModel().getSelectedIndex() >= 0) {
            monedaSimbolo = cbMoneda.getSelectionModel().getSelectedItem().getSimbolo();
            calculateTotales();
        }
    }

    public void setContent(FxPrincipalController principalController) {
        this.principalController = principalController;
    }

}
