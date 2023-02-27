package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.FormulaTB;
import service.FormulaADO;

public class FxFormulaController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private TableView<FormulaTB> tvList;
    @FXML
    private TableColumn<FormulaTB, String> tcNumero;
    @FXML
    private TableColumn<FormulaTB, String> tcFecha;
    @FXML
    private TableColumn<FormulaTB, String> tcProducto;
    @FXML
    private TableColumn<FormulaTB, String> tcFormula;
    @FXML
    private TableColumn<FormulaTB, String> tcCantidad;
    @FXML
    private TableColumn<FormulaTB, String> tcInstrucciones;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FXMLLoader fXMLFormulaProceso;

    private FxPrincipalController fxPrincipalController;

    private AnchorPane nodeFormulaProceso;

    private FxFormulaAgregarController formulaProcesoController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;
        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcFormula.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTitulo()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcInstrucciones.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getInstrucciones()));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcFormula.prefWidthProperty().bind(tvList.widthProperty().multiply(0.22));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcInstrucciones.prefWidthProperty().bind(tvList.widthProperty().multiply(0.28));

        try {
            fXMLFormulaProceso = new FXMLLoader(getClass().getResource(FilesRouters.FX_FORMULA_AGREGAR));
            nodeFormulaProceso = fXMLFormulaProceso.load();
            formulaProcesoController = fXMLFormulaProceso.getController();
            formulaProcesoController.setInitFormulaController(this);

        } catch (IOException ex) {
            System.out.println("Controller formula proceso" + ex.getLocalizedMessage());
        }

        initLoadTable();
    }

    private void initLoadTable() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillFormulaTable(0, "", "", "");
            opcion = 0;
        }
    }

    private void fillFormulaTable(int opcion, String fechaInicio, String fechaFinal, String buscar) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return FormulaADO.ListarFormulas(opcion, fechaInicio, fechaFinal, buscar, (paginacion - 1) * 20, 20);
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
                ObservableList<FormulaTB> ventaTBs = (ObservableList<FormulaTB>) objects[0];
                if (!ventaTBs.isEmpty()) {
                    tvList.setItems(ventaTBs);
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

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillFormulaTable(0, "", "", "");
                break;
            case 1:
                fillFormulaTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "");
                break;
            case 2:
                fillFormulaTable(2, "", "", txtSearch.getText().trim());
                break;
        }
    }

    private void openWindowFormulaProcesoAdd() {
        formulaProcesoController.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeFormulaProceso, 0d);
        AnchorPane.setTopAnchor(nodeFormulaProceso, 0d);
        AnchorPane.setRightAnchor(nodeFormulaProceso, 0d);
        AnchorPane.setBottomAnchor(nodeFormulaProceso, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeFormulaProceso);
    }

    private void openWindowFormulaProcesoEdit(String idFormula) {
        try {
            FXMLLoader fXMLFormulaProceso = new FXMLLoader(getClass().getResource(FilesRouters.FX_FORMULA_EDITAR));
            AnchorPane nodeFormulaProceso = fXMLFormulaProceso.load();
            FxFormulaEditarController formulaProcesoController = fXMLFormulaProceso.getController();
            formulaProcesoController.setInitFormulaController(this);
            formulaProcesoController.editFormulaProceso(idFormula);
            formulaProcesoController.setContent(fxPrincipalController);
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(nodeFormulaProceso, 0d);
            AnchorPane.setTopAnchor(nodeFormulaProceso, 0d);
            AnchorPane.setRightAnchor(nodeFormulaProceso, 0d);
            AnchorPane.setBottomAnchor(nodeFormulaProceso, 0d);
            fxPrincipalController.getVbContent().getChildren().add(nodeFormulaProceso);
        } catch (IOException ex) {
            System.out.println("Controller formula proceso" + ex.getLocalizedMessage());
        }
    }

    private void openViewVisualizarFormula() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_FORMULA_VISUALIZAR));
            AnchorPane node = fXMLLoader.load();
            //Controlller here
            FxFormulaVisualizarController controller = fXMLLoader.getController();
            controller.setInitFormulaController(this, fxPrincipalController);
            controller.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdFormula());
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } else {
            Tools.AlertMessageWarning(hbWindow, "Formula", "Debe seleccionar una formula de la lista.");
        }
    }

    private void removeFormula() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short value = Tools.AlertMessageConfirmation(hbWindow, "Formula", "¿Está seguro de eliminar la formula?");
            if (value == 1) {
                String result = FormulaADO.Eliminar_Formula_ById(tvList.getSelectionModel().getSelectedItem().getIdFormula());
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(hbWindow, "Formula", "Se eliminó correctamente la formula");
                    initLoadTable();
                } else if (result.equalsIgnoreCase("noid")) {
                    Tools.AlertMessageWarning(lblLoad, "Formula", "No existe el id de la formula, intente nuevamente por favor.");
                } else {
                    Tools.AlertMessageError(lblLoad, "Formula", result);
                }
            }
        }
    }

    @FXML
    private void onKeyPressedNuevaFormula(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowFormulaProcesoAdd();
        }
    }

    @FXML
    private void onActionNuevaFormula(ActionEvent event) {
        openWindowFormulaProcesoAdd();
    }

    @FXML
    private void onKeyPressedEditarFormula(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowFormulaProcesoEdit(tvList.getSelectionModel().getSelectedItem().getIdFormula());
            }
        }
    }

    @FXML
    private void onActionEditarFormula(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowFormulaProcesoEdit(tvList.getSelectionModel().getSelectedItem().getIdFormula());
        }
    }

    @FXML
    private void onKeyPressedEliminarFormula(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            removeFormula();
        }
    }

    @FXML
    private void onActionEliminarFormula(ActionEvent event) {
        removeFormula();
    }

    @FXML
    private void onKeyPressedVisualizarFormula(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openViewVisualizarFormula();
        }
    }

    @FXML
    private void onActionVisualizarFormula(ActionEvent event) throws IOException {
        openViewVisualizarFormula();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initLoadTable();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        initLoadTable();
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillFormulaTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "");
                opcion = 1;

            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillFormulaTable(1, Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal), "");
                opcion = 1;

            }
        }
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
                    fillFormulaTable(2, "", "", txtSearch.getText().trim());
                    opcion = 2;
                }
            }
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openViewVisualizarFormula();
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
    private void onKeyReleasedWindow(KeyEvent event) throws IOException {
        if (null != event.getCode()) {
            switch (event.getCode()) {
                case F1:
                    openWindowFormulaProcesoAdd();
                    break;
                case F2:
                    if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                        openWindowFormulaProcesoEdit(tvList.getSelectionModel().getSelectedItem().getIdFormula());
                    }
                    break;
                case F3:
                    removeFormula();
                    break;
                case F4:
                    openViewVisualizarFormula();
                    break;
                case F5:
                    initLoadTable();
                    break;
                default:
                    break;
            }
        }
    }

    public HBox getHbWindow() {
        return hbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
