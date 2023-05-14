package controller.operaciones.guiaremision;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.contactos.clientes.FxClienteProcesoController;
import controller.contactos.conductor.FxConductorProcesoController;
import controller.contactos.vehiculo.FxVehiculoProcesoController;
import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.operaciones.venta.FxVentaListaController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.ClienteTB;
import model.ConductorTB;
import model.DetalleTB;
import model.EmpresaTB;
import model.GuiaRemisionDetalleTB;
import model.GuiaRemisionTB;
import model.SuministroTB;
import model.TipoDocumentoTB;
import model.UbigeoTB;
import model.VehiculoTB;
import model.VentaTB;
import service.ClienteADO;
import service.ConductorADO;
import service.DetalleADO;
import service.EmpresaADO;
import service.GuiaRemisionADO;
import service.TipoDocumentoADO;
import service.UbigeoADO;
import service.VehiculoADO;
import service.VentaADO;

public class FxGuiaRemisionController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private VBox vbBody;
    @FXML
    private ScrollPane apScrollPane;
    @FXML
    private Button btnRegistrar;
    @FXML
    private ComboBox<TipoDocumentoTB> cbDocumentoGuia;
    @FXML
    private ComboBox<ClienteTB> cbCliente;
    @FXML
    private RadioButton rbPublico;
    @FXML
    private RadioButton rbPrivado;
    @FXML
    private ComboBox<ConductorTB> cbConductor;
    @FXML
    private ComboBox<DetalleTB> cbMotivoTraslado;
    @FXML
    private DatePicker dtFechaTraslado;
    @FXML
    private ComboBox<DetalleTB> cbTipoPesoCarga;
    @FXML
    private TextField txtPesoCarga;
    @FXML
    private ComboBox<VehiculoTB> cbVehiculo;
    @FXML
    private TextField txtDireccionPartida;
    @FXML
    private ComboBox<UbigeoTB> cbUbigeoPartida;
    @FXML
    private TextField txtDireccionLlegada;
    @FXML
    private ComboBox<UbigeoTB> cbUbigeoLlegada;
    @FXML
    private ComboBox<TipoDocumentoTB> cbTipoComprobante;
    @FXML
    private TextField txtSerieFactura;
    @FXML
    private TextField txtNumeracionFactura;
    @FXML
    private ComboBox<ConductorTB> cbEmpresa;
    @FXML
    private Button btnAgregarEmpresa;
    @FXML
    private TableView<GuiaRemisionDetalleTB> tvList;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, TextField> tcCodigo;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, TextField> tcDescripcion;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, TextField> tcMedida;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, TextField> tcCantidad;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, TextField> tcPeso;
    @FXML
    private TableColumn<GuiaRemisionDetalleTB, Button> tcOpcion;
    @FXML
    private Button btnAgregarVehiculo;
    @FXML
    private Button btnAgregarConductor;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private final ImageView loadImage = new ImageView(new Image("/view/image/load.gif"));

    private final ImageView warningImage = new ImageView(new Image("/view/image/warning_large.png"));

    private String idGuiaRemision;

    private String horaTraslado;

    private String idVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcCodigo.setCellValueFactory(new PropertyValueFactory<>("txtCodigo"));
        tcDescripcion.setCellValueFactory(new PropertyValueFactory<>("txtDescripcion"));
        tcMedida.setCellValueFactory(new PropertyValueFactory<>("txtUnidad"));
        tcCantidad.setCellValueFactory(new PropertyValueFactory<>("txtCantidad"));
        tcPeso.setCellValueFactory(new PropertyValueFactory<>("txtPeso"));
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnRemover"));

        tcCodigo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcDescripcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        tcMedida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcPeso.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcOpcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));

        Tools.actualDate(Tools.getDate(), dtFechaTraslado);
        txtDireccionPartida.setText(Session.COMPANY_DOMICILIO);

        ToggleGroup groupModalidad = new ToggleGroup();
        rbPublico.setToggleGroup(groupModalidad);
        rbPrivado.setToggleGroup(groupModalidad);

        loadImage.setFitWidth(120);
        loadImage.setFitHeight(120);

        warningImage.setFitWidth(120);
        warningImage.setFitHeight(120);

        apScrollPane.setOnKeyPressed(event -> {
            apWindow.requestFocus();
        });

        loadCliente();
        loadVehiculo();
        loadConductor();
        loadEmpresa();
        loadUbigeoPartida();
        loadUbigeoLlegada();
    }

    private void loadCliente() {
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
            List<ClienteTB> clienteTBs = ClienteADO.GetSearchComboBoxCliente(4,
                    searchComboBoxCliente.getSearchComboBoxSkin().getSearchBox().getText().trim());
            searchComboBoxCliente.getComboBox().getItems().addAll(clienteTBs);
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
        searchComboBoxCliente.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxCliente.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxCliente.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxCliente.getComboBox().hide();
                        }
                    }
                });
    }

    private void loadVehiculo() {
        SearchComboBox<VehiculoTB> searchComboBoxVehiculo = new SearchComboBox<>(cbVehiculo, false);
        searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxVehiculo.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxVehiculo.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxVehiculo.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxVehiculo.getComboBox().hide();
            }
        });
        searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxVehiculo.getComboBox().getItems().clear();
            List<VehiculoTB> vehiculoTBs = VehiculoADO.GetSearchComboBoxVehiculo(
                    searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().getText().trim());
            searchComboBoxVehiculo.getComboBox().getItems().addAll(vehiculoTBs);
        });
        searchComboBoxVehiculo.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxVehiculo.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxVehiculo.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxVehiculo.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxVehiculo.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxVehiculo.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxVehiculo.getComboBox().hide();
                        }
                    }
                });
    }

    private void loadConductor() {
        SearchComboBox<ConductorTB> searchComboBoxConductor = new SearchComboBox<>(cbConductor, false);
        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxConductor.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxConductor.getComboBox().hide();
            }
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxConductor.getComboBox().getItems().clear();
            List<ConductorTB> conductorTBs = ConductorADO.GetSearchComboBoxConductor(
                    searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().getText().trim(), 8);
            searchComboBoxConductor.getComboBox().getItems().addAll(conductorTBs);
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxConductor.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxConductor.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxConductor.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxConductor.getComboBox().hide();
                        }
                    }
                });
    }

    private void loadEmpresa() {
        SearchComboBox<ConductorTB> searchComboBoxConductor = new SearchComboBox<>(cbEmpresa, false);
        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxConductor.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxConductor.getComboBox().hide();
            }
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxConductor.getComboBox().getItems().clear();
            List<ConductorTB> conductorTBs = ConductorADO.GetSearchComboBoxConductor(
                    searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().getText().trim(), 11);
            searchComboBoxConductor.getComboBox().getItems().addAll(conductorTBs);
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxConductor.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxConductor.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxConductor.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxConductor.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxConductor.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxConductor.getComboBox().hide();
                        }
                    }
                });
    }

    private void loadUbigeoPartida() {
        SearchComboBox<UbigeoTB> searchComboBoxUbigeoPartida = new SearchComboBox<>(cbUbigeoPartida, false);
        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxUbigeoPartida.getComboBox().hide();
            }
        });
        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxUbigeoPartida.getComboBox().getItems().clear();
            List<UbigeoTB> ubigeoTBs = UbigeoADO.GetSearchComboBoxUbigeo(
                    searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().getText().trim());
            searchComboBoxUbigeoPartida.getComboBox().getItems().addAll(ubigeoTBs);
        });
        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxUbigeoPartida.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxUbigeoPartida.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxUbigeoPartida.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxUbigeoPartida.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxUbigeoPartida.getComboBox().hide();
                        }
                    }
                });
    }

    private void loadUbigeoLlegada() {
        SearchComboBox<UbigeoTB> searchComboBoxUbigeoLlegada = new SearchComboBox<>(cbUbigeoLlegada, false);
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxUbigeoLlegada.getComboBox().hide();
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxUbigeoLlegada.getComboBox().getItems().clear();
            List<UbigeoTB> ubigeoTBs = UbigeoADO.GetSearchComboBoxUbigeo(
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().getText().trim());
            ubigeoTBs.forEach(e -> {
                searchComboBoxUbigeoLlegada.getComboBox().getItems().add(e);
            });
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxUbigeoLlegada.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty()
                .addListener((p, o, item) -> {
                    if (item != null) {
                        searchComboBoxUbigeoLlegada.getComboBox().getSelectionModel().select(item);
                        if (searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().isClickSelection()) {
                            searchComboBoxUbigeoLlegada.getComboBox().hide();
                        }
                    }
                });
    }

    public void loadGuiaRemisionById(String idGuiaRemision) {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                Object tipoComprobanteGuia = TipoDocumentoADO.obtenerComprobantesParaGuiaRemision();
                ObservableList<DetalleTB> motivoTraslado = DetalleADO.obtenerDetallePorIdMantenimiento("0017");
                ObservableList<DetalleTB> pesoCarga = DetalleADO.obtenerDetallePorIdMantenimiento("0021");
                List<TipoDocumentoTB> tipoComprobanteVenta = TipoDocumentoADO.obtenerComprobantesParaVenta();
                Object guiaRemision = GuiaRemisionADO.obtenerGuiaRemisionParaEditar(idGuiaRemision);

                if (tipoComprobanteGuia instanceof String) {
                    throw new Exception((String) tipoComprobanteGuia);
                }

                if (guiaRemision instanceof String) {
                    throw new Exception((String) guiaRemision);
                }

                return new Object[] {
                        tipoComprobanteGuia,
                        motivoTraslado,
                        pesoCarga,
                        tipoComprobanteVenta,
                        guiaRemision
                };
            }
        };

        task.setOnScheduled(e -> {
            resetGuiaRemision();

            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            lblMessageLoad.setGraphic(loadImage);
        });

        task.setOnFailed(e -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                vbBody.setDisable(false);
                hbLoad.setVisible(false);
                resetGuiaRemision();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    vbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetGuiaRemision();
                }
                event.consume();
            });

            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            lblMessageLoad.setGraphic(warningImage);
        });

        task.setOnSucceeded(e -> {
            Object[] result = (Object[]) task.getValue();

            ArrayList<TipoDocumentoTB> tipoComprobanteGuia = (ArrayList<TipoDocumentoTB>) result[0];
            ObservableList<DetalleTB> motivoTraslado = (ObservableList<DetalleTB>) result[1];
            ObservableList<DetalleTB> pesoCarga = (ObservableList<DetalleTB>) result[2];
            List<TipoDocumentoTB> tipoComprobanteVenta = (List<TipoDocumentoTB>) result[3];
            GuiaRemisionTB guiaRemision = (GuiaRemisionTB) result[4];

            this.idGuiaRemision = guiaRemision.getIdGuiaRemision();

            // Cargar los comprobantes para la guía
            cbDocumentoGuia.getItems().addAll(tipoComprobanteGuia);

            // Carga los motivos de traslado para la guía
            cbMotivoTraslado.getItems().addAll(motivoTraslado);

            // Carga los tipos de peso para el traslado de la guía
            cbTipoPesoCarga.getItems().addAll(pesoCarga);

            // Carga los tipos de comprobantes
            cbTipoComprobante.getItems().addAll(tipoComprobanteVenta);

            for (TipoDocumentoTB dc : cbDocumentoGuia.getItems()) {
                if (dc.getIdTipoDocumento() == guiaRemision.getIdComprobante()) {
                    cbDocumentoGuia.getSelectionModel().select(dc);
                    break;
                }
            }

            cbCliente.getItems().add(guiaRemision.getClienteTB());
            if (!cbCliente.getItems().isEmpty()) {
                cbCliente.getSelectionModel().select(0);
            }

            if (guiaRemision.getIdModalidadTraslado().equals("MT0001")) {
                rbPublico.setSelected(true);

                cbVehiculo.setDisable(true);
                btnAgregarVehiculo.setDisable(true);
                cbConductor.setDisable(true);
                btnAgregarConductor.setDisable(true);

                cbEmpresa.setDisable(false);
                btnAgregarEmpresa.setDisable(false);
            } else {
                rbPrivado.setSelected(true);

                cbVehiculo.setDisable(false);
                btnAgregarVehiculo.setDisable(false);
                cbConductor.setDisable(false);
                btnAgregarConductor.setDisable(false);

                cbEmpresa.setDisable(true);
                btnAgregarEmpresa.setDisable(true);
            }

            for (DetalleTB mt : cbMotivoTraslado.getItems()) {
                if (mt.getIdDetalle() == guiaRemision.getIdMotivoTraslado()) {
                    cbMotivoTraslado.getSelectionModel().select(mt);
                    break;
                }
            }

            LocalDate localDate = LocalDate.parse(guiaRemision.getFechaTraslado());
            dtFechaTraslado.setValue(localDate);
            horaTraslado = guiaRemision.getHoraTraslado();

            for (DetalleTB pc : cbTipoPesoCarga.getItems()) {
                if (pc.getIdDetalle() == guiaRemision.getIdPesoCarga()) {
                    cbTipoPesoCarga.getSelectionModel().select(pc);
                    break;
                }
            }

            txtPesoCarga.setText(Tools.roundingValue(guiaRemision.getPesoCarga(), 2));

            if (guiaRemision.getVehiculoTB() != null) {
                cbVehiculo.getItems().add(guiaRemision.getVehiculoTB());
                if (!cbVehiculo.getItems().isEmpty()) {
                    cbVehiculo.getSelectionModel().select(0);
                }
            }

            if (rbPrivado.isSelected() && guiaRemision.getConductorTB() != null) {
                cbConductor.getItems().add(guiaRemision.getConductorTB());
                if (!cbConductor.getItems().isEmpty()) {
                    cbConductor.getSelectionModel().select(0);
                }
            }

            if (rbPublico.isSelected() && guiaRemision.getConductorTB() != null) {
                cbEmpresa.getItems().add(guiaRemision.getConductorTB());
                if (!cbEmpresa.getItems().isEmpty()) {
                    cbEmpresa.getSelectionModel().select(0);
                }
            }

            txtDireccionPartida.setText(guiaRemision.getDireccionPartida());
            cbUbigeoPartida.getItems().add(guiaRemision.getUbigeoPartidaTB());
            if (!cbUbigeoPartida.getItems().isEmpty()) {
                cbUbigeoPartida.getSelectionModel().select(0);
            }

            txtDireccionLlegada.setText(guiaRemision.getDireccionLlegada());
            cbUbigeoLlegada.getItems().add(guiaRemision.getUbigeoLlegadaTB());
            if (!cbUbigeoLlegada.getItems().isEmpty()) {
                cbUbigeoLlegada.getSelectionModel().select(0);
            }

            for (TipoDocumentoTB tdv : cbTipoComprobante.getItems()) {
                if (tdv.getIdTipoDocumento() == guiaRemision.getVentaTB().getIdComprobante()) {
                    cbTipoComprobante.getSelectionModel().select(tdv);
                    break;
                }
            }
            txtSerieFactura.setText(guiaRemision.getVentaTB().getSerie());
            txtNumeracionFactura.setText(guiaRemision.getVentaTB().getNumeracion());

            for (GuiaRemisionDetalleTB guiaRemisionDetalle : guiaRemision.getGuiaRemisionDetalle()) {
                addProducto(guiaRemisionDetalle);
            }

            btnRegistrar.setText("Editar (F1)");
            btnRegistrar.getStyleClass().clear();
            btnRegistrar.getStyleClass().add("buttonLightWarning");
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void loadVentaById(String idVenta) {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                Object tipoComprobanteGuia = TipoDocumentoADO.obtenerComprobantesParaGuiaRemision();
                ObservableList<DetalleTB> motivoTraslado = DetalleADO.obtenerDetallePorIdMantenimiento("0017");
                ObservableList<DetalleTB> pesoCarga = DetalleADO.obtenerDetallePorIdMantenimiento("0021");
                List<TipoDocumentoTB> tipoComprobanteVenta = TipoDocumentoADO.obtenerComprobantesParaVenta();
                EmpresaTB empresaTB = EmpresaADO.obtenerEmpresa();
                Object venta = VentaADO.obtenerVentaById(idVenta);

                if (tipoComprobanteGuia instanceof String) {
                    throw new Exception((String) tipoComprobanteGuia);
                }

                if (empresaTB == null) {
                    throw new Exception("No se puedo cargar los datos de la empresa.");
                }

                if (venta instanceof String) {
                    throw new Exception((String) venta);
                }

                return new Object[] {
                        tipoComprobanteGuia,
                        motivoTraslado,
                        pesoCarga,
                        tipoComprobanteVenta,
                        empresaTB,
                        venta
                };
            }
        };

        task.setOnScheduled(e -> {
            resetGuiaRemision();

            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            lblMessageLoad.setGraphic(loadImage);
        });

        task.setOnFailed(e -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                vbBody.setDisable(false);
                hbLoad.setVisible(false);
                resetGuiaRemision();
            });

            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    vbBody.setDisable(false);
                    hbLoad.setVisible(false);
                    resetGuiaRemision();
                }
                event.consume();
            });

            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            lblMessageLoad.setGraphic(warningImage);
        });

        task.setOnSucceeded(e -> {
            Object[] result = (Object[]) task.getValue();

            ArrayList<TipoDocumentoTB> tipoComprobanteGuia = (ArrayList<TipoDocumentoTB>) result[0];
            ObservableList<DetalleTB> motivoTraslado = (ObservableList<DetalleTB>) result[1];
            ObservableList<DetalleTB> pesoCarga = (ObservableList<DetalleTB>) result[2];
            List<TipoDocumentoTB> tipoComprobanteVenta = (List<TipoDocumentoTB>) result[3];
            EmpresaTB empresaTB = (EmpresaTB) result[4];
            VentaTB ventaTB = (VentaTB) result[5];

            // Cargar los comprobantes para la guía
            cbDocumentoGuia.getItems().addAll(tipoComprobanteGuia);

            if (cbDocumentoGuia.getItems().size() == 1) {
                cbDocumentoGuia.getSelectionModel().select(0);
            }

            // Carga los motivos de traslado para la guía
            cbMotivoTraslado.getItems().addAll(motivoTraslado);

            // Define al fecha de traslado por la actual
            Tools.actualDate(Tools.getDate(), dtFechaTraslado);

            // Carga los tipos de peso para el traslado de la guía
            cbTipoPesoCarga.getItems().addAll(pesoCarga);

            // Carga los tipos de comprobantes
            cbTipoComprobante.getItems().addAll(tipoComprobanteVenta);

            // Cargar información de la empresa
            txtDireccionPartida.setText(empresaTB.getDomicilio());
            if (empresaTB.getUbigeoTB().getIdUbigeo() > 0) {
                cbUbigeoPartida.getItems().add(empresaTB.getUbigeoTB());
                if (!cbUbigeoPartida.getItems().isEmpty()) {
                    cbUbigeoPartida.getSelectionModel().select(0);
                }
            }

            // Cargar información del la venta
            ArrayList<SuministroTB> empList = ventaTB.getSuministroTBs();
            ClienteTB clienteTB = ventaTB.getClienteTB();

            for (int i = 0; i < cbTipoComprobante.getItems().size(); i++) {
                if (cbTipoComprobante.getItems().get(i).getIdTipoDocumento() == ventaTB.getIdComprobante()) {
                    cbTipoComprobante.getSelectionModel().select(i);
                    break;
                }
            }
            txtSerieFactura.setText(ventaTB.getSerie());
            txtNumeracionFactura.setText(ventaTB.getNumeracion());
            cbCliente.getItems().clear();
            cbCliente.getItems().add(clienteTB);
            if (!cbCliente.getItems().isEmpty()) {
                cbCliente.getSelectionModel().select(0);
            }

            for (int i = 0; i < cbMotivoTraslado.getItems().size(); i++) {
                if (cbMotivoTraslado.getItems().get(i).getIdDetalle() == clienteTB.getIdMotivoTraslado()) {
                    cbMotivoTraslado.getSelectionModel().select(i);
                    break;
                }
            }

            txtDireccionLlegada.setText(clienteTB.getDireccion());
            cbUbigeoLlegada.getItems().clear();
            if (clienteTB.getUbigeoTB().getIdUbigeo() > 0) {
                cbUbigeoLlegada.getItems().add(clienteTB.getUbigeoTB());
                if (!cbUbigeoLlegada.getItems().isEmpty()) {
                    cbUbigeoLlegada.getSelectionModel().select(0);
                }
            }

            tvList.getItems().clear();
            empList.forEach((suministro) -> addProducto(suministro));

            this.idVenta = idVenta;

            vbBody.setDisable(false);
            hbLoad.setVisible(false);
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public boolean validateDuplicate(SuministroTB suministroTB) {
        boolean ret = false;
        for (int i = 0; i < tvList.getItems().size(); i++) {
            if (tvList.getItems().get(i).getIdSuministro().equals(suministroTB.getIdSuministro())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void addProducto(SuministroTB suministroTB) {
        GuiaRemisionDetalleTB guiaRemisionDetalleTB = new GuiaRemisionDetalleTB();
        guiaRemisionDetalleTB.setIdSuministro(suministroTB.getIdSuministro());
        guiaRemisionDetalleTB.setCodigo(suministroTB.getClave());
        guiaRemisionDetalleTB.setCantidad(suministroTB.getCantidad());
        guiaRemisionDetalleTB.setDescripcion(suministroTB.getNombreMarca());
        guiaRemisionDetalleTB.setUnidad(suministroTB.getUnidadCompraName());
        guiaRemisionDetalleTB.setPeso(0);

        TextField txtCodigo = new TextField(guiaRemisionDetalleTB.getCodigo());
        txtCodigo.getStyleClass().add("text-field-normal");

        TextField txtDescripcion = new TextField(guiaRemisionDetalleTB.getDescripcion());
        txtDescripcion.getStyleClass().add("text-field-normal");

        TextField txtUnidad = new TextField(guiaRemisionDetalleTB.getUnidad());
        txtUnidad.getStyleClass().add("text-field-normal");

        TextField txtCantidad = new TextField(Tools.roundingValue(guiaRemisionDetalleTB.getCantidad(), 0));
        txtCantidad.getStyleClass().add("text-field-normal");
        txtCantidad.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b')) {
                event.consume();
            }
        });

        TextField txtPeso = new TextField(Tools.roundingValue(guiaRemisionDetalleTB.getPeso(), 2));
        txtPeso.getStyleClass().add("text-field-normal");
        txtPeso.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && txtPeso.getText().contains(".")) {
                event.consume();
            }
        });

        Button btnRemover = new Button();
        btnRemover.getStyleClass().add("buttonDark");
        ImageView view = new ImageView(new Image("/view/image/remove.png"));
        view.setFitWidth(22);
        view.setFitHeight(22);
        btnRemover.setGraphic(view);
        btnRemover.setOnAction(event -> {
            tvList.getItems().remove(guiaRemisionDetalleTB);
        });
        btnRemover.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(guiaRemisionDetalleTB);
                event.consume();
            }
        });

        guiaRemisionDetalleTB.setTxtCodigo(txtCodigo);
        guiaRemisionDetalleTB.setTxtDescripcion(txtDescripcion);
        guiaRemisionDetalleTB.setTxtUnidad(txtUnidad);
        guiaRemisionDetalleTB.setTxtCantidad(txtCantidad);
        guiaRemisionDetalleTB.setTxtPeso(txtPeso);
        guiaRemisionDetalleTB.setBtnRemover(btnRemover);

        tvList.getItems().add(guiaRemisionDetalleTB);
    }

    public void addProducto(GuiaRemisionDetalleTB guiaRemisionDetalle) {
        GuiaRemisionDetalleTB guiaRemisionDetalleTB = new GuiaRemisionDetalleTB();
        guiaRemisionDetalleTB.setIdSuministro(guiaRemisionDetalle.getIdSuministro());
        guiaRemisionDetalleTB.setCodigo(guiaRemisionDetalle.getCodigo());
        guiaRemisionDetalleTB.setCantidad(guiaRemisionDetalle.getCantidad());
        guiaRemisionDetalleTB.setDescripcion(guiaRemisionDetalle.getDescripcion());
        guiaRemisionDetalleTB.setUnidad(guiaRemisionDetalle.getUnidad());
        guiaRemisionDetalleTB.setPeso(0);

        TextField txtCodigo = new TextField(guiaRemisionDetalleTB.getCodigo());
        txtCodigo.getStyleClass().add("text-field-normal");

        TextField txtDescripcion = new TextField(guiaRemisionDetalleTB.getDescripcion());
        txtDescripcion.getStyleClass().add("text-field-normal");

        TextField txtUnidad = new TextField(guiaRemisionDetalleTB.getUnidad());
        txtUnidad.getStyleClass().add("text-field-normal");

        TextField txtCantidad = new TextField(Tools.roundingValue(guiaRemisionDetalleTB.getCantidad(), 0));
        txtCantidad.getStyleClass().add("text-field-normal");
        txtCantidad.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b')) {
                event.consume();
            }
        });

        TextField txtPeso = new TextField(Tools.roundingValue(guiaRemisionDetalleTB.getPeso(), 2));
        txtPeso.getStyleClass().add("text-field-normal");
        txtPeso.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && txtPeso.getText().contains(".")) {
                event.consume();
            }
        });

        Button btnRemover = new Button();
        btnRemover.getStyleClass().add("buttonDark");
        ImageView view = new ImageView(new Image("/view/image/remove.png"));
        view.setFitWidth(22);
        view.setFitHeight(22);
        btnRemover.setGraphic(view);
        btnRemover.setOnAction(event -> {
            tvList.getItems().remove(guiaRemisionDetalleTB);
        });
        btnRemover.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                tvList.getItems().remove(guiaRemisionDetalleTB);
                event.consume();
            }
        });

        guiaRemisionDetalleTB.setTxtCodigo(txtCodigo);
        guiaRemisionDetalleTB.setTxtDescripcion(txtDescripcion);
        guiaRemisionDetalleTB.setTxtUnidad(txtUnidad);
        guiaRemisionDetalleTB.setTxtCantidad(txtCantidad);
        guiaRemisionDetalleTB.setTxtPeso(txtPeso);
        guiaRemisionDetalleTB.setBtnRemover(btnRemover);

        tvList.getItems().add(guiaRemisionDetalleTB);
    }

    private void onEventCliente() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxClienteProcesoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Cliente", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.loadAddCliente());
        stage.show();
    }

    private void onEventGuardar() throws IOException {
        /**
         * Tipo de guia de remision
         */
        if (cbDocumentoGuia.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el documento guía usar.");
            Tools.scrollTo(apScrollPane, cbDocumentoGuia);
            return;
        }

        /**
         * Datos del cliente
         */
        if (cbCliente.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione un cliente.");
            Tools.scrollTo(apScrollPane, cbCliente);
            return;
        }

        /**
         * Datos del traslado
         */
        if (cbMotivoTraslado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el motivo del traslado.");
            Tools.scrollTo(apScrollPane, cbMotivoTraslado);
            return;
        }

        if (dtFechaTraslado.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Ingrese la fecha de traslado.");
            Tools.scrollTo(apScrollPane, dtFechaTraslado);
            return;
        }

        if (cbTipoPesoCarga.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el tipo de peso.");
            Tools.scrollTo(apScrollPane, cbTipoPesoCarga);
            return;
        }

        if (Tools.isText(txtPesoCarga.getText())) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Ingrese el peso total.");
            Tools.scrollTo(apScrollPane, txtPesoCarga);
            return;
        }

        /**
         * Datos del transporte privado
         */
        if (rbPrivado.isSelected() && cbVehiculo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el vehículo.");
            Tools.scrollTo(apScrollPane, cbVehiculo);
            return;
        }

        /**
         * Datos del conductor
         */
        if (rbPrivado.isSelected() && cbConductor.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el conductor.");
            Tools.scrollTo(apScrollPane, cbConductor);
            return;
        }

        /**
         * Datos del empresa - conductor
         */
        if (rbPublico.isSelected() && cbEmpresa.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el conductor.");
            Tools.scrollTo(apScrollPane, cbEmpresa);
            return;
        }

        /**
         * Dirección de la partida y punto de llegada
         */
        if (Tools.isText(txtDireccionPartida.getText())) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Ingrese la dirección de partida.");
            Tools.scrollTo(apScrollPane, txtDireccionPartida);
            return;
        }

        if (cbUbigeoPartida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el ubigeo de partida.");
            Tools.scrollTo(apScrollPane, cbUbigeoPartida);
            return;
        }

        if (Tools.isText(txtDireccionLlegada.getText())) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Ingrese la dirección de llegada.");
            Tools.scrollTo(apScrollPane, txtDireccionLlegada);
            return;
        }

        if (cbUbigeoLlegada.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Seleccione el ubigeo de llegada.");
            Tools.scrollTo(apScrollPane, cbUbigeoLlegada);
            return;
        }

        /**
         * Detalle de la guía de remisión
         */
        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Guía de Remisión", "Agregar productos a lista.");
            Tools.scrollTo(apScrollPane, tvList);
            return;
        }

        short value = Tools.AlertMessageConfirmation(apWindow, "Guia Remisión", "¿Está seguro de continuar?");
        if (value == 1) {
            GuiaRemisionTB guiaRemisionTB = new GuiaRemisionTB();
            guiaRemisionTB.setIdGuiaRemision(idGuiaRemision);
            guiaRemisionTB
                    .setIdComprobante(cbDocumentoGuia.getSelectionModel().getSelectedItem().getIdTipoDocumento());

            guiaRemisionTB.setIdCliente(cbCliente.getSelectionModel().getSelectedItem().getIdCliente());
            guiaRemisionTB.setIdVendedor(Session.USER_ID);

            /**
             * Modalidad del traslado
             */
            guiaRemisionTB.setIdModalidadTraslado(rbPublico.isSelected() ? "MT0001" : "MT0002");

            /**
             * Datos del traslado
             */
            guiaRemisionTB
                    .setIdMotivoTraslado(cbMotivoTraslado.getSelectionModel().getSelectedItem().getIdDetalle());
            guiaRemisionTB.setFechaTraslado(Tools.getDatePicker(dtFechaTraslado));
            guiaRemisionTB.setHoraTraslado(horaTraslado);
            guiaRemisionTB.setIdPesoCarga(cbTipoPesoCarga.getSelectionModel().getSelectedItem().getIdDetalle());
            guiaRemisionTB.setPesoCarga(Double.parseDouble(txtPesoCarga.getText()));

            /**
             * Datos del transporte privado
             */
            guiaRemisionTB.setIdVehiculo(cbVehiculo.getSelectionModel().getSelectedIndex() >= 0
                    ? cbVehiculo.getSelectionModel().getSelectedItem().getIdVehiculo()
                    : "");

            /**
             * Datos del conductor
             */
            if (rbPublico.isSelected()) {
                guiaRemisionTB.setIdConductor(cbEmpresa.getSelectionModel().getSelectedItem().getIdConductor());
            } else {
                guiaRemisionTB.setIdConductor(cbConductor.getSelectionModel().getSelectedItem().getIdConductor());
            }

            /**
             * Dirección de partida y llegada
             */
            guiaRemisionTB.setDireccionPartida(txtDireccionPartida.getText().trim());
            guiaRemisionTB.setIdUbigeoPartida(cbUbigeoPartida.getSelectionModel().getSelectedItem().getIdUbigeo());
            guiaRemisionTB.setDireccionLlegada(txtDireccionLlegada.getText().trim());
            guiaRemisionTB.setIdUbigeoLlegada(cbUbigeoLlegada.getSelectionModel().getSelectedItem().getIdUbigeo());

            /**
             * Documento de referencia
             */
            guiaRemisionTB.setIdVenta(idVenta);
            guiaRemisionTB.setEstado(1);

            /**
             * Detalle de guía de remisión
             */
            guiaRemisionTB.setGuiaRemisionDetalle(tvList.getItems());

            if (guiaRemisionTB.getIdGuiaRemision() == "") {
                /*
                 * Proceso de registro de la guía de remisión
                 */
                String[] result = GuiaRemisionADO.registrarGuiaRemision(guiaRemisionTB).split("/");
                if (result[0].equalsIgnoreCase("register")) {
                    openModalImpresion(result[1]);
                    resetGuiaRemision();
                } else {
                    Tools.AlertMessageError(apWindow, "Guia Remisión", result[0]);
                }
            } else {
                /*
                 * Proceso de actualización de la guía de remisión
                 */
                String[] result = GuiaRemisionADO.actualizarGuiaRemision(guiaRemisionTB).split("/");
                if (result[0].equalsIgnoreCase("updated")) {
                    openModalImpresion(result[1]);
                    resetGuiaRemision();
                } else {
                    Tools.AlertMessageError(apWindow, "Guia Remisión", result[0]);
                }
            }
        }
    }

    /**
     * Funcion encargada de limpiar
     * todos los campos de la guía de remisión
     */
    private void resetGuiaRemision() {
        // Limpiar los tipos de documento
        cbDocumentoGuia.getItems().clear();

        // Limpiar los clientes
        cbCliente.getItems().clear();

        // Define a su estado inicial la modalidad de traslado
        rbPublico.setSelected(true);
        cbVehiculo.setDisable(true);

        // Limpiar los motivos de traslado
        cbMotivoTraslado.getItems().clear();

        // Define la fecha actual como traslado
        Tools.actualDate(Tools.getDate(), dtFechaTraslado);

        // Limpiar el tipo de paso
        cbTipoPesoCarga.getItems().clear();

        // Limpiar el campo peso carga
        txtPesoCarga.clear();

        // Define a su estado inicial el vehículo
        cbVehiculo.getItems().clear();
        cbVehiculo.setDisable(true);
        btnAgregarVehiculo.setDisable(true);

        // Define a su estado inicial el conductor
        cbConductor.getItems().clear();
        cbConductor.setDisable(true);
        btnAgregarConductor.setDisable(true);

        // Limpiar los datos de la empresa
        cbEmpresa.getItems().clear();
        cbEmpresa.setDisable(false);
        btnAgregarEmpresa.setDisable(false);

        // Limpiar los campos de dirección de partida
        txtDireccionPartida.clear();
        cbUbigeoPartida.getItems().clear();

        // Limpiar los campos de la dirección de llegada
        txtDireccionLlegada.clear();
        cbUbigeoLlegada.getItems().clear();

        // Limpiar los campos de la venta asociada
        cbTipoComprobante.getItems().clear();
        txtSerieFactura.clear();
        txtNumeracionFactura.clear();
        tvList.getItems().clear();

        idGuiaRemision = "";
        idVenta = "";

        btnRegistrar.setText("Registrar (F1)");
        btnRegistrar.getStyleClass().clear();
        btnRegistrar.getStyleClass().add("buttonLightDefault");

    }

    private void openWindowSuministro() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxSuministrosListaController controller = fXMLLoader.getController();
        controller.setInitGuiaRemisionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto",
                apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
        controller.fillSuministrosTable((short) 0, "");
    }

    private void openWindowVentas() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_VENTA_LISTA);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxVentaListaController controller = fXMLLoader.getController();
        controller.setInitGuiaRemisionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione una venta", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
        controller.loadInit();
    }

    private void openWindowConductor() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_CONDUCTOR_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxConductorProcesoController controller = fXMLLoader.getController();
        controller.setInitGuiaRemisionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registrar un conductor",
                apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> {
            controller.loadAddConductor();
        });
        stage.show();
    }

    private void openWindowVehiculo() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_VEHICLO_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxVehiculoProcesoController controller = fXMLLoader.getController();
        controller.setInitGuiaRemisionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registrar un vehículo",
                apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.loadAddConductor());
        stage.show();
    }

    private void openWindowGuiaRemision() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_GUIA_REMISION_LISTA);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxGuiaRemisionListaController controller = fXMLLoader.getController();
        controller.setInitGuiaRemisionController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Mostrar Guías de Remisión",
                apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.loadInit());
        stage.show();
    }

    private void openModalImpresion(String idGuiaRemision) throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxOpcionesImprimirController controller = fXMLLoader.getController();
        controller.loadTicketGuiaRemision(controller.getApWindow());
        controller.setIdGuiaRemision(idGuiaRemision);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", apWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
    }

    @FXML
    private void onKeyTypedPesoCarga(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtPesoCarga.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedNumeracion(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
            event.consume();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) throws IOException {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedToClient(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCliente();
            event.consume();
        }
    }

    @FXML
    private void onActionToClient(ActionEvent event) throws IOException {
        onEventCliente();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            resetGuiaRemision();
            event.consume();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        resetGuiaRemision();
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
        }
    }

    @FXML
    private void onActionAgregar(ActionEvent event) throws IOException {
        openWindowSuministro();
    }

    @FXML
    private void onKeyPressedVentas(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentas();
            event.consume();
        }
    }

    @FXML
    private void onActionVentas(ActionEvent event) throws IOException {
        openWindowVentas();
    }

    @FXML
    private void onKeyPressedGuias(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowGuiaRemision();
            event.consume();
        }
    }

    @FXML
    private void onActionGuias(ActionEvent event) throws IOException {
        openWindowGuiaRemision();
    }

    @FXML
    private void onKeyPressedVehiculo(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVehiculo();
        }
    }

    @FXML
    private void onActionVehiculo(ActionEvent event) throws IOException {
        openWindowVehiculo();
    }

    @FXML
    private void onKeyPressedConductor(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowConductor();
        }
    }

    @FXML
    private void onActionConductor(ActionEvent event) throws IOException {
        openWindowConductor();
    }

    @FXML
    private void onActionModalidadTraslado(ActionEvent event) {
        if (rbPublico.isSelected()) {
            cbVehiculo.setDisable(true);
            btnAgregarVehiculo.setDisable(true);
            cbConductor.setDisable(true);
            btnAgregarConductor.setDisable(true);

            cbEmpresa.setDisable(false);
            btnAgregarEmpresa.setDisable(false);
        } else {
            cbVehiculo.setDisable(false);
            btnAgregarVehiculo.setDisable(false);
            cbConductor.setDisable(false);
            btnAgregarConductor.setDisable(false);

            cbEmpresa.setDisable(true);
            btnAgregarEmpresa.setDisable(true);
        }
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) throws IOException {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case F1:
                    onEventGuardar();
                    event.consume();
                    break;
                case F2:
                    openWindowVentas();
                    event.consume();
                    break;
                case F3:
                    openWindowGuiaRemision();
                    event.consume();
                    break;
                case F5:
                    resetGuiaRemision();
                    event.consume();
                    break;
                default:
                    break;
            }
        }
    }

    public TableView<GuiaRemisionDetalleTB> getTvList() {
        return tvList;
    }

    public RadioButton getRbPublico() {
        return rbPublico;
    }

    public RadioButton getRbPrivado() {
        return rbPrivado;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
