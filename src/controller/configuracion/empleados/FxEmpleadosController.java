package controller.configuracion.empleados;

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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.EmpleadoADO;
import model.EmpleadoTB;

public class FxEmpleadosController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<EmpleadoTB> tvList;
    @FXML
    private TableColumn<EmpleadoTB, String> tcId;
    @FXML
    private TableColumn<EmpleadoTB, String> tcCompleteData;
    @FXML
    private TableColumn<EmpleadoTB, String> tcContact;
    @FXML
    private TableColumn<EmpleadoTB, String> tcRol;
    @FXML
    private TableColumn<EmpleadoTB, String> tcAddres;
    @FXML
    private TableColumn<EmpleadoTB, String> tcState;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;

        tcId.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcCompleteData.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getNumeroDocumento() + "\n"
                + cellData.getValue().getApellidos() + ", ",
                cellData.getValue().getNombres()
        ));
        tcContact.setCellValueFactory(cellData -> Bindings.concat(
                "TEL: " + cellData.getValue().getTelefono() + "\n" + "CEL: " + cellData.getValue().getCelular()
        ));
        tcRol.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getRolName()));
        tcAddres.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDireccion()));
        tcState.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEstadoName()));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
//        tcDocument.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcCompleteData.prefWidthProperty().bind(tvList.widthProperty().multiply(0.23));
        tcContact.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        tcRol.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcAddres.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcState.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        loadInit();
    }

    public void loadInit() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillEmpleadosTable(0, "");
            opcion = 0;
        }
    }

    private void fillEmpleadosTable(int opcion, String buscar) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return EmpleadoADO.ListEmpleados(opcion, buscar, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnScheduled((WorkerStateEvent event) -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });

        task.setOnFailed((WorkerStateEvent event) -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnSucceeded((WorkerStateEvent e) -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<EmpleadoTB> empleadoTBs = (ObservableList<EmpleadoTB>) objects[0];
                if (!empleadoTBs.isEmpty()) {
                    tvList.setItems(empleadoTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
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

    private void openWindowEmpleadosAdd() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxEmpleadosProcesoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Empleado", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
    }

    private void openWindowEmpleadosEdit(String value) throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxEmpleadosProcesoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Editar Empleado", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
        controller.setEditEmpleado(value);
    }

    private void onEventExecuteDelete() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Empleado", "¿Está seguro de eliminar al empleado?", true);
            if (confirmation == 1) {
                String resultado = EmpleadoADO.DeleteEmpleadoById(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
                if (resultado.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(window, "Empleado", "Se eliminó el empleado correctamente.");
                    loadInit();
                } else if (resultado.equalsIgnoreCase("caja")) {
                    Tools.AlertMessageWarning(window, "Empleado", "No se puede eliminar el empleado, porque tiene historial de cajas.");
                } else if (resultado.equalsIgnoreCase("compra")) {
                    Tools.AlertMessageWarning(window, "Empleado", "No se puede eliminar el empleado, porque tiene un historial de compras.");
                } else if (resultado.equalsIgnoreCase("sistema")) {
                    Tools.AlertMessageWarning(window, "Empleado", "El empleado no puede ser eliminado porque es parte del sistema.");
                } else {
                    Tools.AlertMessageError(window, "Empleado", resultado);
                }
            }
        } else {
            Tools.AlertMessageWarning(window, "Empleado", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillEmpleadosTable(0, "");
                break;
            case 1:
                fillEmpleadosTable(1, txtSearch.getText().trim());
                break;
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEmpleadosAdd();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowEmpleadosAdd();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEmpleadosEdit(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowEmpleadosEdit(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInit();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        loadInit();
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
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
                    fillEmpleadosTable(1, txtSearch.getText().trim());
                    opcion = 1;
                }
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowEmpleadosEdit(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
            }
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (event.getClickCount() == 2) {
                openWindowEmpleadosEdit(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
            }
        }
    }

    @FXML
    private void onActionDelete(ActionEvent event) {
        onEventExecuteDelete();
    }

    @FXML
    private void onKeyPressedDelete(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExecuteDelete();
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

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
