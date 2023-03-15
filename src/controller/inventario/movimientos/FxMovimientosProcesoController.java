package controller.inventario.movimientos;

import controller.inventario.suministros.FxSuministrosListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.AjusteInventarioTB;
import model.AlmacenTB;
import model.SuministroTB;
import model.TipoMovimientoTB;
import service.AjusteInventarioADO;
import service.AlmacenADO;
import service.SuministroADO;
import service.TipoMovimientoADO;

public class FxMovimientosProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private HBox hbBody;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, Button> tcAccion;
    @FXML
    private TableColumn<SuministroTB, String> tcClave;
    @FXML
    private TableColumn<SuministroTB, TextField> tcNuevaExistencia;
    @FXML
    private TableColumn<SuministroTB, String> tcExistenciaActual;
    @FXML
    private TableColumn<SuministroTB, String> tcDiferencia;
    @FXML
    private TableColumn<SuministroTB, String> tcMedida;
    @FXML
    private RadioButton rbIncremento;
    @FXML
    private RadioButton rbDecremento;
    @FXML
    private ComboBox<TipoMovimientoTB> cbAjuste;
    @FXML
    private TextField txtObservacion;
    @FXML
    private ComboBox<AlmacenTB> cbAlmacen;
    @FXML
    private HBox hbBotones;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private FxMovimientosController movimientosController;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    private final ImageView loadImage = new ImageView(new Image("/view/image/load.gif"));

    private final ImageView successImage = new ImageView(new Image("/view/image/succes_large.png"));

    private final ImageView warningImage = new ImageView(new Image("/view/image/warning_large.png"));

    private final ImageView errorImage = new ImageView(new Image("/view/image/error_large.png"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcAccion.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));
        tcClave.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClave() + "\n" + cellData.getValue().getNombreMarca()));
        tcNuevaExistencia.setCellValueFactory(new PropertyValueFactory<>("txtMovimiento"));
        tcExistenciaActual.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcDiferencia.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDiferencia()));
        tcMedida.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getUnidadCompraName()));

        tcAccion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcClave.prefWidthProperty().bind(tvList.widthProperty().multiply(0.28));
        tcNuevaExistencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcExistenciaActual.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcDiferencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcMedida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));

        ToggleGroup groupAjuste = new ToggleGroup();
        rbIncremento.setToggleGroup(groupAjuste);
        rbDecremento.setToggleGroup(groupAjuste);

        TipoMovimientoADO.Get_list_Tipo_Movimiento(rbIncremento.isSelected(), false).forEach(e -> cbAjuste.getItems().add(e));

        loadImage.setFitWidth(120);
        loadImage.setFitHeight(120);

        successImage.setFitWidth(120);
        successImage.setFitHeight(120);

        warningImage.setFitWidth(120);
        warningImage.setFitHeight(120);

        errorImage.setFitWidth(120);
        errorImage.setFitHeight(120);

        filterCbAlmacen();
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

    private void registrarMovimiento() {
        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "La tabla no tiene campos para continuar con el proceso.");
            return;
        }

        int validete = 0;
        validete = tvList.getItems().stream().filter((SuministroTB e) -> e.getMovimiento() <= 0).map((_item) -> 1).reduce(validete, Integer::sum);
        if (validete > 0) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Su nueva existencia de un producto no puede ser menor que 0.");
            return;
        }

        if (cbAjuste.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Seleccione un tipo de ajuste, por favor.");
            cbAjuste.requestFocus();
            return;
        }

        if (Tools.isText(txtObservacion.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Ingrese una observación, por favor.");
            txtObservacion.requestFocus();
            return;
        }

        if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Seleccione el almacen.");
            cbAlmacen.requestFocus();
            return;
        }

        short validate = Tools.AlertMessageConfirmation(apWindow, "Movimiento", "¿Está seguro de continuar?");

        if (validate == 1) {
            ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });
            Task<String> task = new Task<String>() {
                @Override
                public String call() throws Exception {
                    AjusteInventarioTB inventarioTB = new AjusteInventarioTB();
                    inventarioTB.setFecha(Tools.getDate());
                    inventarioTB.setHora(Tools.getTime());
                    inventarioTB.setTipoAjuste(rbIncremento.isSelected());
                    inventarioTB.setTipoMovimiento(cbAjuste.getSelectionModel().getSelectedItem().getIdTipoMovimiento());
                    inventarioTB.setTipoMovimientoName(cbAjuste.getSelectionModel().getSelectedItem().getNombre());
                    inventarioTB.setObservacion(txtObservacion.getText().trim());
                    inventarioTB.setSuministro(true);
                    inventarioTB.setEstado(1);
                    inventarioTB.setIdAlmacen(cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                    inventarioTB.setSuministroTBs(new ArrayList<>(tvList.getItems()));

                    String result = AjusteInventarioADO.Crud_Movimiento_Inventario(inventarioTB);
                    if (result.equalsIgnoreCase("registered")) {
                        return result;
                    }
                    
                    throw new Exception(result);
                }
            };

            task.setOnScheduled(t -> {
                hbBody.setDisable(true);
                hbLoad.setVisible(true);
                btnAceptarLoad.setVisible(false);
                lblMessageLoad.setText("Procesando información...");
                lblMessageLoad.setTextFill(Color.web("#ffffff"));
                lblMessageLoad.setGraphic(loadImage);
            });

            task.setOnFailed(t -> {
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
                });
                lblMessageLoad.setText(task.getException().getLocalizedMessage());
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                lblMessageLoad.setGraphic(warningImage);
            });

            task.setOnSucceeded(t -> {
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
                });

                lblMessageLoad.setText("Se completó el registro correctamente.");
                lblMessageLoad.setTextFill(Color.web("#ffffff"));
                lblMessageLoad.setGraphic(successImage);
            });
            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }
    }

    public void clearComponents() {
        rbIncremento.setSelected(true);
        rbIncremento.setDisable(false);
        rbDecremento.setDisable(false);
        cbAjuste.setDisable(false);
        cbAjuste.getItems().clear();
        TipoMovimientoADO.Get_list_Tipo_Movimiento(rbIncremento.isSelected(), false).forEach(e -> cbAjuste.getItems().add(e));
        txtObservacion.setText("N/D");
        tvList.getItems().clear();

        searchComboBoxAlmacen.getComboBox().getItems().clear();
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }
    }

    private boolean validateDuplicate(String idSuministro) {
        boolean ret = false;
        for (int i = 0; i < tvList.getItems().size(); i++) {
            if (tvList.getItems().get(i).getIdSuministro().equals(idSuministro)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void addSuministroLista(String idSuministro) {
        if (validateDuplicate(idSuministro)) {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Ya hay un producto con las mismas características.");
            return;
        }
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return SuministroADO.List_Suministros_Movimiento(idSuministro);
            }
        };
        task.setOnScheduled(t -> {
            hbBotones.setDisable(true);
            lblLoad.setVisible(true);
        });
        task.setOnFailed(t -> {
            hbBotones.setDisable(false);
            lblLoad.setVisible(false);
        });
        task.setOnSucceeded(t -> {
            Object object = task.getValue();
            if (object instanceof SuministroTB) {
                SuministroTB suministroTB = (SuministroTB) object;
                suministroTB.setId(tvList.getItems().size() + 1);
                suministroTB.getBtnRemove().setOnAction(event -> {
                    tvList.getItems().remove(suministroTB);
                });
                suministroTB.getBtnRemove().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        tvList.getItems().remove(suministroTB);
                    }
                });
                suministroTB.getTxtMovimiento().setOnKeyReleased(event -> {
                    if (Tools.isNumeric(suministroTB.getTxtMovimiento().getText().trim())) {
                        if (rbIncremento.isSelected()) {
                            double newDiferencia = suministroTB.getCantidad() + Double.parseDouble(suministroTB.getTxtMovimiento().getText());
                            suministroTB.setMovimiento(Double.parseDouble(suministroTB.getTxtMovimiento().getText()));
                            suministroTB.setDiferencia(newDiferencia);
                            suministroTB.setCambios(true);
                        } else {
                            double newDiferencia = suministroTB.getCantidad() - Double.parseDouble(suministroTB.getTxtMovimiento().getText());
                            suministroTB.setMovimiento(Double.parseDouble(suministroTB.getTxtMovimiento().getText()));
                            suministroTB.setDiferencia(newDiferencia);
                            suministroTB.setCambios(true);
                        }
                    }
                    tvList.refresh();
                });

                suministroTB.getTxtMovimiento().focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        if (!suministroTB.isCambios()) {
                            suministroTB.getTxtMovimiento().setText(suministroTB.getMovimiento() + "");
                        }
                        tvList.refresh();
                    } else {
                        suministroTB.setCambios(false);
                    }
                });
                tvList.getItems().add(suministroTB);
            }
            hbBotones.setDisable(false);
            lblLoad.setVisible(false);
        });
        exec.execute(task);
    }

    private void openWindowSuministros() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitMovimientoProcesoController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.getTxtSearch().requestFocus());
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void executeEventRomever() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short confirmation = Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Compras", "¿Esta seguro de quitar el suministro?", true);
            if (confirmation == 1) {
                ObservableList<SuministroTB> articuloSelect, allArticulos;
                allArticulos = tvList.getItems();
                articuloSelect = tvList.getSelectionModel().getSelectedItems();
                articuloSelect.forEach(allArticulos::remove);
                if (!tvList.getItems().isEmpty()) {
                    int count = 0;
                    for (int i = 0; i < tvList.getItems().size(); i++) {
                        count++;
                        tvList.getItems().get(i).setId(count);
                    }
                    tvList.refresh();
                }
            }
        } else {
            Tools.AlertMessageWarning(apWindow, "Movimiento", "Seleccione un producto para removerlo");
        }
    }

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(movimientosController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(movimientosController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(movimientosController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(movimientosController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(movimientosController.getHbWindow());
    }

    @FXML
    private void onActionRelizarMovimiento(ActionEvent event) {
        registrarMovimiento();
    }

    @FXML
    private void onActionTipo(ActionEvent event) {
        cbAjuste.getItems().clear();
        TipoMovimientoADO.Get_list_Tipo_Movimiento(rbIncremento.isSelected(), false).forEach(e -> {
            cbAjuste.getItems().add(new TipoMovimientoTB(e.getIdTipoMovimiento(), e.getNombre(), e.isAjuste()));
        });
        if (!tvList.getItems().isEmpty()) {
            tvList.getItems().forEach((SuministroTB e) -> {
                if (rbIncremento.isSelected()) {
                    double newDiferencia = e.getCantidad() + e.getMovimiento();
                    e.setDiferencia(newDiferencia);
                } else {
                    double newDiferencia = e.getCantidad() - e.getMovimiento();
                    e.setDiferencia(newDiferencia);
                }
            });
            tvList.refresh();
        }
    }

    @FXML
    private void onKeyPressedTipo(KeyEvent event) {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case UP:
                    cbAjuste.getItems().clear();
                    TipoMovimientoADO.Get_list_Tipo_Movimiento(!rbIncremento.isSelected(), false).forEach(e -> {
                        cbAjuste.getItems().add(new TipoMovimientoTB(e.getIdTipoMovimiento(), e.getNombre(), e.isAjuste()));
                    });
                    break;
                case DOWN:
                    cbAjuste.getItems().clear();
                    TipoMovimientoADO.Get_list_Tipo_Movimiento(!rbIncremento.isSelected(), false).forEach(e -> {
                        cbAjuste.getItems().add(new TipoMovimientoTB(e.getIdTipoMovimiento(), e.getNombre(), e.isAjuste()));
                    });
                    break;
                case LEFT:
                    cbAjuste.getItems().clear();
                    TipoMovimientoADO.Get_list_Tipo_Movimiento(!rbIncremento.isSelected(), false).forEach(e -> {
                        cbAjuste.getItems().add(new TipoMovimientoTB(e.getIdTipoMovimiento(), e.getNombre(), e.isAjuste()));
                    });
                    break;
                case RIGHT:
                    cbAjuste.getItems().clear();
                    TipoMovimientoADO.Get_list_Tipo_Movimiento(!rbIncremento.isSelected(), false).forEach(e -> {
                        cbAjuste.getItems().add(new TipoMovimientoTB(e.getIdTipoMovimiento(), e.getNombre(), e.isAjuste()));
                    });
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeWindow();
    }

    @FXML
    private void onKeyPressedSuministros(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministros();
        }
    }

    @FXML
    private void onActionSuministro(ActionEvent event) {
        openWindowSuministros();
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case F1:
                    registrarMovimiento();
                    break;
                case F2:
                    openWindowSuministros();
                    break;
                case DELETE:
                    executeEventRomever();
                    break;
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    public TableView<SuministroTB> getTvList() {
        return tvList;
    }

    public void setContent(FxMovimientosController movimientosController, FxPrincipalController fxPrincipalController) {
        this.movimientosController = movimientosController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
