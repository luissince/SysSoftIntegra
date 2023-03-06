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
    private ScrollPane spWindow;
    @FXML
    private Label lblLoad;
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

    private FxPrincipalController fxPrincipalController;

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

        loadCliente();
        loadVehiculo();
        loadConductor();
        loadEmpresa();
        loadUbigeoPartida();
        loadUbigeoLlegada();
        loadComponents();
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

    private void loadComponents() {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return new Object[]{
                    TipoDocumentoADO.GetTipoDocumentoGuiaRemision(),
                    DetalleADO.GetDetailId("0017"),
                    DetalleADO.GetDetailId("0018"),
                    DetalleADO.GetDetailId("0021"),
                    TipoDocumentoADO.GetDocumentoCombBoxVentas(),
                    EmpresaADO.GetEmpresa()
                };
            }
        };

        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });

        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
        });

        task.setOnSucceeded(e -> {
            Object[] result = (Object[]) task.getValue();

            cbDocumentoGuia.getItems().clear();
            cbDocumentoGuia.getItems().addAll((ArrayList<TipoDocumentoTB>) result[0]);

            cbMotivoTraslado.getItems().clear();
            cbMotivoTraslado.getItems().addAll((ObservableList<DetalleTB>) result[1]);

            Tools.actualDate(Tools.getDate(), dtFechaTraslado);
            cbTipoPesoCarga.getItems().clear();
            txtPesoCarga.setText("");

            txtDireccionPartida.setText("");
            txtDireccionLlegada.setText("");

            cbTipoPesoCarga.getItems().addAll((ObservableList<DetalleTB>) result[3]);

            cbTipoComprobante.getItems().clear();
            cbTipoComprobante.getItems().addAll((List<TipoDocumentoTB>) result[4]);
            txtSerieFactura.setText("");
            txtNumeracionFactura.setText("");

            EmpresaTB empresaTB = (EmpresaTB) result[5];

            txtDireccionPartida.setText(empresaTB.getDomicilio());
            cbUbigeoPartida.getItems().clear();
            if (empresaTB.getUbigeoTB().getIdUbigeo() > 0) {
                cbUbigeoPartida.getItems().add(empresaTB.getUbigeoTB());
                if (!cbUbigeoPartida.getItems().isEmpty()) {
                    cbUbigeoPartida.getSelectionModel().select(0);
                }
            }

            cbUbigeoLlegada.getItems().clear();
            tvList.getItems().clear();

            lblLoad.setVisible(false);
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

    private void onEventCliente() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxClienteProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Cliente", spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadAddCliente());
            stage.show();
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
            // Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto",
                    spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillSuministrosTable((short) 0, "");
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowVentas() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaListaController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione una venta", spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.loadInit();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowConductor() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CONDUCTOR_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxConductorProcesoController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Registrar un conductor",
                    spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> {
                controller.loadAddConductor();
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowVehiculo() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VEHICLO_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVehiculoProcesoController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Registrar un vehículo",
                    spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadAddConductor());
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    private void onEventGuardar() {
        /**
         * Tipo de guia de remision
         */
        if (cbDocumentoGuia.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el documento guía usar.");
            cbDocumentoGuia.requestFocus();
            return;
        }

        /**
         * Datos del cliente
         */
        if (cbCliente.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione un cliente.");
            cbCliente.requestFocus();
            return;
        }

        /**
         * Datos del traslado
         */
        if (cbMotivoTraslado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el motivo del traslado.");
            cbMotivoTraslado.requestFocus();
            return;
        }

        if (dtFechaTraslado.getValue() == null) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Ingrese la fecha de traslado.");
            dtFechaTraslado.requestFocus();
            return;
        }

        if (cbTipoPesoCarga.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el tipo de peso.");
            cbTipoPesoCarga.requestFocus();
            return;
        }

        if (Tools.isText(txtPesoCarga.getText())) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Ingrese el peso total.");
            txtPesoCarga.requestFocus();
            return;
        }

        /**
         * Datos del transporte privado
         */
        if (rbPrivado.isSelected() && cbVehiculo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el vehículo.");
            cbVehiculo.requestFocus();
            return;
        }

        /**
         * Datos del conductor
         */
        if (rbPrivado.isSelected() && cbConductor.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el conductor.");
            cbConductor.requestFocus();
            return;
        }

        /**
         * Datos del empresa - conductor
         */
        if (rbPublico.isSelected() && cbEmpresa.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el conductor.");
            cbEmpresa.requestFocus();
            return;
        }

        /**
         * Dirección de la partida y punto de llegada
         */
        if (Tools.isText(txtDireccionPartida.getText())) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Ingrese la dirección de partida.");
            txtDireccionPartida.requestFocus();
            return;
        }

        if (cbUbigeoPartida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el ubigeo de partida.");
            cbUbigeoPartida.requestFocus();
            return;
        }

        if (Tools.isText(txtDireccionLlegada.getText())) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Ingrese la dirección de llegada.");
            txtDireccionLlegada.requestFocus();
            return;
        }

        if (cbUbigeoLlegada.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Seleccione el ubigeo de llegada.");
            cbUbigeoLlegada.requestFocus();
            return;
        }

        /**
         * Detalle de la guía de remisión
         */
        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(spWindow, "Guía de Remisión", "Agregar productos a lista.");
            tvList.requestFocus();
            return;
        }

        short value = Tools.AlertMessageConfirmation(spWindow, "Guia Remisión", "¿Está seguro de continuar?");
        if (value == 1) {
            GuiaRemisionTB guiaRemisionTB = new GuiaRemisionTB();
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

            String[] result = GuiaRemisionADO.InsertarGuiaRemision(guiaRemisionTB).split("/");
            if (result[0].equalsIgnoreCase("register")) {
                openModalImpresion(result[1]);
                reset();
            } else {
                Tools.AlertMessageError(spWindow, "Guia Remisión", result[0]);
            }

        }
    }

    private void openModalImpresion(String idGuiaRemision) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketGuiaRemision(controller.getApWindow());
            controller.setIdGuiaRemision(idGuiaRemision);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", spWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller Modal Impresión: " + ex.getLocalizedMessage());
        }
    }

    private void reset() {
        cbDocumentoGuia.getSelectionModel().select(null);
        cbCliente.getItems().clear();
        cbMotivoTraslado.getSelectionModel().select(null);
        Tools.actualDate(Tools.getDate(), dtFechaTraslado);
        cbTipoPesoCarga.getSelectionModel().select(null);
        cbTipoPesoCarga.getSelectionModel().select(null);
        txtPesoCarga.clear();

        rbPublico.setSelected(true);
        cbVehiculo.setDisable(true);
        btnAgregarVehiculo.setDisable(true);
        cbConductor.setDisable(true);
        btnAgregarConductor.setDisable(true);

        cbEmpresa.setDisable(false);
        btnAgregarEmpresa.setDisable(true);

        cbVehiculo.getItems().clear();
        cbConductor.getItems().clear();
        cbEmpresa.getItems().clear();
        
        
        txtDireccionLlegada.clear();
        cbUbigeoLlegada.getItems().clear();
        
        cbTipoComprobante.getSelectionModel().select(null);
        txtSerieFactura.clear();
        txtNumeracionFactura.clear();
        tvList.getItems().clear();
        idVenta = "";
    }

    public void loadVentaById(String idVenta) {
        this.idVenta = idVenta;
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return VentaADO.Obtener_Venta_ById(idVenta);
            }
        };
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof VentaTB) {
                VentaTB ventaTB = (VentaTB) object;
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
                lblLoad.setVisible(false);
            } else {
                lblLoad.setVisible(false);
            }
        });
        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
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
    private void onKeyPressedToRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedToClient(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCliente();
        }
    }

    @FXML
    private void onActionToClient(ActionEvent event) {
        onEventCliente();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadComponents();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        loadComponents();
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministro();
        }
    }

    @FXML
    private void onActionAgregar(ActionEvent event) {
        openWindowSuministro();
    }

    @FXML
    private void onKeyPressedVentas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentas();
        }
    }

    @FXML
    private void onActionVentas(ActionEvent event) {
        openWindowVentas();
    }

    @FXML
    private void onKeyPressedVehiculo(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVehiculo();
        }
    }

    @FXML
    private void onActionVehiculo(ActionEvent event) {
        openWindowVehiculo();
    }

    @FXML
    private void onKeyPressedConductor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowConductor();
        }
    }

    @FXML
    private void onActionConductor(ActionEvent event) {
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
