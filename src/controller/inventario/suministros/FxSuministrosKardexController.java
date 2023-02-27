package controller.inventario.suministros;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.AlmacenTB;
import model.KardexTB;
import model.SuministroTB;
import service.AlmacenADO;
import service.KardexADO;
import service.SuministroADO;

public class FxSuministrosKardexController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblProducto;
    @FXML
    private TableView<KardexTB> tvList;
    @FXML
    private TableColumn<KardexTB, String> tcNumero;
    @FXML
    private TableColumn<KardexTB, String> tcFecha;
    @FXML
    private TableColumn<KardexTB, String> tcDetalle;
    @FXML
    private TableColumn<KardexTB, Label> tcEntrada;
    @FXML
    private TableColumn<KardexTB, Label> tcSalida;
    @FXML
    private TableColumn<KardexTB, String> tcExistencia;
    @FXML
    private TableColumn<KardexTB, String> tcCostoPromedio;
    @FXML
    private TableColumn<KardexTB, String> tcDebe;
    @FXML
    private TableColumn<KardexTB, String> tcHaber;
    @FXML
    private TableColumn<KardexTB, String> tcSaldo;
    @FXML
    private ComboBox<AlmacenTB> cbAlmacen;
    @FXML
    private Label lblCantidadActual;
    @FXML
    private Label lblValorActual;

    private FxPrincipalController fxPrincipalController;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        tcDetalle.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMovimientoName() + "\n" + cellData.getValue().getDetalle().toUpperCase()));

        tcEntrada.setCellValueFactory(new PropertyValueFactory<>("lblEntreda"));
        tcSalida.setCellValueFactory(new PropertyValueFactory<>("lblSalida"));
        tcExistencia.setCellValueFactory(new PropertyValueFactory<>("lblExistencia"));

        tcCostoPromedio.setCellValueFactory(new PropertyValueFactory<>("lblCosto"));

        tcDebe.setCellValueFactory(new PropertyValueFactory<>("lblDebe"));
        tcHaber.setCellValueFactory(new PropertyValueFactory<>("lblHaber"));
        tcSaldo.setCellValueFactory(new PropertyValueFactory<>("lblSaldo"));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        //
        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcDetalle.prefWidthProperty().bind(tvList.widthProperty().multiply(0.19));

        tcEntrada.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcSalida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcExistencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));

        tcCostoPromedio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));

        tcDebe.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcHaber.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcSaldo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));

        filterCbAlmacen();
    }

    public void fillKardexTable(int opcion, String value, int idAlmacen) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return KardexADO.List_Kardex_By_Id_Suministro(opcion, value, idAlmacen);
            }
        };

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            lblCantidadActual.setText(Tools.roundingValue(0, 2));
            lblValorActual.setText(Tools.roundingValue(0, 2));
        });

        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<KardexTB> kardexTBs = (ObservableList<KardexTB>) objects[0];
                if (!kardexTBs.isEmpty()) {
                    tvList.setItems(kardexTBs);
                    double cantidad = (double) objects[1];
                    double saldo = (double) objects[2];
                    lblCantidadActual.setText(Tools.roundingValue(cantidad, 2));
                    lblValorActual.setText(Tools.roundingValue(saldo, 2));
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
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
        if (!cbAlmacen.getItems().isEmpty()) {
            cbAlmacen.getSelectionModel().select(0);
        }
    }

    private void openWindowSuministros() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitSuministrosKardexController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Suministro", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillSuministrosTable((short) 0, "");
        } catch (IOException ex) {
            System.out.println("Error en la vista suministros lista:" + ex.getLocalizedMessage());
        }
    }

    private void onEventBuscar() {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
            if (!lblLoad.isVisible()) {
                if (txtSearch.getText().trim().length() != 0) {
                    SuministroTB suministroTB = SuministroADO.GetSuministroById(txtSearch.getText().trim());
                    if (suministroTB != null) {
                        setLoadProducto(suministroTB.getClave() + " " + suministroTB.getNombreMarca());
                        fillKardexTable(0, suministroTB.getIdSuministro(), cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                    }
                }
            }
        }
    }

    private void onEventRecargar() {
        txtSearch.clear();
        txtSearch.requestFocus();
        searchComboBoxAlmacen.getComboBox().getItems().clear();
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }
        tvList.getItems().clear();
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRecargar();
            event.consume();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        onEventRecargar();
    }

    @FXML
    private void onKeyPressedProducto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministros();
            event.consume();
        }
    }

    @FXML
    private void onActionProducto(ActionEvent event) {
        openWindowSuministros();
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
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
                onEventBuscar();
            }
        }
    }

    @FXML
    private void onKeyPressedSearchSuministro(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventBuscar();
        }
    }

    @FXML
    private void onActionSearchSuministro(ActionEvent event) {
        onEventBuscar();
    }

    public void setLoadProducto(String value) {
//        this.idSuministro = idSuministro;
        lblProducto.setText(value);
    }

    public ComboBox<AlmacenTB> getCbAlmacen() {
        return cbAlmacen;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
