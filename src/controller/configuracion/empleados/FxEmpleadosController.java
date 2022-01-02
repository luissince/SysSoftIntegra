package controller.configuracion.empleados;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
    private TableColumn<EmpleadoTB, Integer> tcId;
    @FXML
    private TableColumn<EmpleadoTB, String> tcDocument;
    @FXML
    private TableColumn<EmpleadoTB, String> tcCompleteData;
    @FXML
    private TableColumn<EmpleadoTB, String> tcContact;
    @FXML
    private TableColumn<EmpleadoTB, String> tcMarketStall;
    @FXML
    private TableColumn<EmpleadoTB, String> tcState;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcId.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
        tcDocument.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeroDocumento()));
        tcCompleteData.setCellValueFactory(cellData
                -> Bindings.concat(
                        cellData.getValue().getApellidos() + " ",
                        cellData.getValue().getNombres()
                )
        );
        tcContact.setCellValueFactory(cellData
                -> Bindings.concat(
                        !Tools.isText(cellData.getValue().getTelefono())
                        ? "TEL: " + cellData.getValue().getTelefono() + "\n" + "CEL: " + cellData.getValue().getCelular()
                        : "CEL: " + cellData.getValue().getCelular()
                )
        );
        tcMarketStall.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getPuestoName()));
        tcState.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEstadoName()));
           
         tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcDocument.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcCompleteData.prefWidthProperty().bind(tvList.widthProperty().multiply(0.23));
        tcContact.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        tcMarketStall.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcState.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
    }

    public void fillEmpleadosTable(String value) {

        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<List<EmpleadoTB>> task = new Task<List<EmpleadoTB>>() {
            @Override
            public ObservableList<EmpleadoTB> call() {
                return EmpleadoADO.ListEmpleados(value);
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            tvList.setItems((ObservableList<EmpleadoTB>) task.getValue());

            lblLoad.setVisible(false);
        });
        task.setOnFailed((WorkerStateEvent event) -> {

            lblLoad.setVisible(false);
        });

        task.setOnScheduled((WorkerStateEvent event) -> {
            lblLoad.setVisible(true);
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
                } else if (resultado.equalsIgnoreCase("caja")) {
                    Tools.AlertMessageWarning(window, "Empleado", "No se puede eliminar el empleado, porque tiene historial de cajas..");
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

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowEmpleadosAdd();
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowEmpleadosEdit(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
        }

    }

    @FXML
    private void onActionReload(ActionEvent event) {
        fillEmpleadosTable("");
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            txtSearch.requestFocus();
        }
    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
        fillEmpleadosTable(txtSearch.getText().trim());
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

     public void setContent( FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
