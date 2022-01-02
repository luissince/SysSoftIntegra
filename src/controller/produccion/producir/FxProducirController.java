package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.ProduccionADO;
import model.ProduccionTB;

public class FxProducirController implements Initializable {

    @FXML
    private HBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<ProduccionTB> tvList;
    @FXML
    private TableColumn<ProduccionTB, String> tcNumero;
    @FXML
    private TableColumn<ProduccionTB, String> tcEncargado;
    @FXML
    private TableColumn<ProduccionTB, String> tcFechaInicio;
    @FXML
    private TableColumn<ProduccionTB, String> tcProductoFabricar;
    @FXML
    private TableColumn<ProduccionTB, String> tcCantidad;
    @FXML
    private TableColumn<ProduccionTB, Label> tcEstado;
    @FXML
    private TableColumn<ProduccionTB, String> tcCosto;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbEstado;

    private FxProducirEditarController producirEditarController;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;    
   
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;
        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);
        cbEstado.getItems().addAll("COMPLETADO", "EN PRODUCCIÓN", "ANULADO");
        loadTableComponents();
        initLoadTable();
    }

    private void loadTableComponents() {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));

        tcEncargado.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getEmpleadoTB().getNumeroDocumento() + "\n"
                + cellData.getValue().getEmpleadoTB().getApellidos() + " " + cellData.getValue().getEmpleadoTB().getNombres()
        ));
        tcFechaInicio.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaInicio() + "\n"
                + cellData.getValue().getFechaRegistro()
        ));
        tcProductoFabricar.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSuministroTB().getClave() + "\n"
                + cellData.getValue().getSuministroTB().getNombreMarca()
        ));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().isCantidadVariable() ? "VARIABLE"
                : Tools.roundingValue(cellData.getValue().getCantidad(), 2) + " " + cellData.getValue().getSuministroTB().getUnidadCompraName()
        ));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("lblEstado"));
        tcCosto.setCellValueFactory(cellData -> Bindings.concat(
                Tools.roundingValue(cellData.getValue().getCosto(), 2)
        ));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcEncargado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcFechaInicio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
//        tcPedido.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcProductoFabricar.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcCosto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    private void initLoadTable() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillProduccionTable(0, "", "", "", 0);
            opcion = 0;
        }
    }

    private void fillProduccionTable(int tipo, String fechaInicio, String fechaFinal, String buscar, int estado) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return ProduccionADO.ListarProduccion(tipo, fechaInicio, fechaFinal, buscar, estado, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });

        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));

        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<ProduccionTB> produccionTBs = (ObservableList<ProduccionTB>) objects[0];
                if (!produccionTBs.isEmpty()) {
                    tvList.setItems(produccionTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }

            } else if (object instanceof String) {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView("Error en traer los datos, intente nuevamente.", "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillProduccionTable(0, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "", 0);
                break;
            case 1:
                fillProduccionTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), txtSearch.getText().trim(), 0);
                break;
            case 2:
                fillProduccionTable(2, "", "", txtSearch.getText().trim(), 0);
                break;
            case 3:
                fillProduccionTable(3, "", "", "", getSelectEstadoId(cbEstado.getSelectionModel().getSelectedItem()));
                break;
        }
    }

    private void onViewProducirProceso() {
        try {
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(getClass().getResource(FilesRouters.FX_PRODUCIR_AGREGAR));
            AnchorPane node = fXMLLoader.load();
            FxProducirAgregarController controller = fXMLLoader.getController();
            controller.setInitControllerProducir(this, fxPrincipalController);
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void editarProduccion(String idProduccion) {
        try {
            String estado;
            int numberEstado;

            switch (tvList.getSelectionModel().getSelectedItem().getEstado()) {
                case 3:
                    estado = "Anulado";
                    numberEstado = 3;
                    break;
                case 2:
                    estado = "En Produccion";
                    numberEstado = 2;
                    break;
                default:
                    estado = "Completado";
                    numberEstado = 1;
                    break;
            }

            if (numberEstado == 2) {
                FXMLLoader fXMLProduccionEditarProceso = new FXMLLoader(getClass().getResource(FilesRouters.FX_PRODUCIR_EDITAR));
                AnchorPane nodeProduccionProceso = fXMLProduccionEditarProceso.load();
                FxProducirEditarController produccionEditarController = fXMLProduccionEditarProceso.getController();
                produccionEditarController.setInitControllerProducir(this, fxPrincipalController);
                produccionEditarController.editarProduccion(idProduccion);
                fxPrincipalController.getVbContent().getChildren().clear();
                AnchorPane.setLeftAnchor(nodeProduccionProceso, 0d);
                AnchorPane.setTopAnchor(nodeProduccionProceso, 0d);
                AnchorPane.setRightAnchor(nodeProduccionProceso, 0d);
                AnchorPane.setBottomAnchor(nodeProduccionProceso, 0d);
                fxPrincipalController.getVbContent().getChildren().add(nodeProduccionProceso);
            } else {
                Tools.AlertMessageWarning(window, "Producción", "No se puede editar dicho producto ya que se encuentra en estado " + estado);
            }
        } catch (IOException ex) {
            System.out.println("Controller formula proceso" + ex.getLocalizedMessage());
        }
    }

    private void visualizarProduccion() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_PEDIDO_VISUALIZAR));
            AnchorPane node = fXMLLoader.load();

            FxProducirVisualizarController visualizarController = fXMLLoader.getController();
            visualizarController.setInitProducirController(this, fxPrincipalController);
            visualizarController.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdProduccion());

            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } else {
            Tools.AlertMessageWarning(window, "Produccion", "Debe seleccionar una producción de la lista");
        }

    }

    private void anularProduccion() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            switch (tvList.getSelectionModel().getSelectedItem().getEstado()) {
                case 1:
                    Tools.AlertMessageWarning(window, "Produccion", "No se puede anular una producción que ya se encuentra en estado COMPLETADO");
                    break;
                case 2:
                    short value = Tools.AlertMessageConfirmation(window, "Insumo", "¿Está seguro de anular dicha producción?");
                    if (value == 1) {
//                        int estado = tvList.getSelectionModel().getSelectedItem().getEstado();
                        String idProduccion = tvList.getSelectionModel().getSelectedItem().getIdProduccion();
                        String result = ProduccionADO.AnularProduccion(idProduccion);
                        if (result.equalsIgnoreCase("anulado")) {
                            Tools.AlertMessageInformation(window, "Produccion", "Se anuló correctamente la produccion.");
                            initLoadTable();
                        } else {
                            Tools.AlertMessageError(window, "Produccion", result);
                        }
                    }
                    break;
                default:
                    Tools.AlertMessageWarning(window, "Produccion", "Dicha producción ya se encuentra anulada");
                    break;
            }
        } else {
            Tools.AlertMessageError(window, "Produccion", "Debe seleccionar una producción de la lista");
        }
    }

    private int getSelectEstadoId(String value) {
        if (value.equalsIgnoreCase("ANULADO")) {
            return 3;
        } else if (value.equalsIgnoreCase("EN PRODUCCIÓN")) {
            return 2;
        } else {
            return 1;
        }
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewProducirProceso();
        }
    }

    @FXML
    private void onActionAgregar(ActionEvent event) {
        onViewProducirProceso();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            txtSearch.requestFocus();
            txtSearch.selectAll();
            initLoadTable();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        txtSearch.requestFocus();
        txtSearch.selectAll();
        initLoadTable();
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
            if (!Tools.isText(txtSearch.getText())) {
                if (!lblLoad.isVisible()) {
                    paginacion = 1;
                    fillProduccionTable(2, "", "", txtSearch.getText().trim(), 0);
                    opcion = 2;
                }
            }
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillProduccionTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "", 0);
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillProduccionTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "", 0);
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionEstado(ActionEvent event) {
        if (cbEstado.getSelectionModel().getSelectedIndex() >= 0) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillProduccionTable(3, "", "", "", getSelectEstadoId(cbEstado.getSelectionModel().getSelectedItem()));
                opcion = 3;
            }
        }
    }

    @FXML
    private void onKeyPressedEditarProduccion(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                editarProduccion(tvList.getSelectionModel().getSelectedItem().getIdProduccion());
            }
        }
    }

    @FXML
    private void onActionEditarProduccion(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            editarProduccion(tvList.getSelectionModel().getSelectedItem().getIdProduccion());
        }
    }

    @FXML
    private void onKeyPressedVisualizar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            visualizarProduccion();
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) throws IOException {
        visualizarProduccion();
    }

    @FXML
    private void onKeyPressedAnularProduccion(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            anularProduccion();
        }
    }

    @FXML
    private void onActionAnularProduccion(ActionEvent event) {
        anularProduccion();
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            visualizarProduccion();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            visualizarProduccion();
        }
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) throws IOException {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case F1:

                    break;
                case F2:

                    break;
                case F3:
                    anularProduccion();
                    break;
                case F4:
                    visualizarProduccion();
                    break;
                case F5:

                    break;
                default:
                    break;
            }
        }

    }

    public HBox getWindow() {
        return window;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
