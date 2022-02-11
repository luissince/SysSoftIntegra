package controller.configuracion.almacen;

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
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.AlmacenADO;
import model.AlmacenTB;

public class FxAlmacenController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<AlmacenTB> tvList;
    @FXML
    private TableColumn<AlmacenTB, String> tcId;
    @FXML
    private TableColumn<AlmacenTB, String> txtFecha;
    @FXML
    private TableColumn<AlmacenTB, String> txtNombre;
    @FXML
    private TableColumn<AlmacenTB, String> txtUbigeo;
    @FXML
    private TableColumn<AlmacenTB, String> txtDireccion;
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
        txtFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        txtNombre.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        txtUbigeo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getUbigeoTB()));
        txtDireccion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDireccion()));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        txtFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        txtNombre.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        txtUbigeo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        txtDireccion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.38));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        initLoad();
    }

    public void initLoad() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillTableAlmacen("");
            opcion = 0;
        }
    }

    private void fillTableAlmacen(String buscar) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return AlmacenADO.ListarAlmacen(buscar, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<AlmacenTB> empList = (ObservableList<AlmacenTB>) objects[0];
                if (!empList.isEmpty()) {
                    tvList.setItems(empList);
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
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowAdd() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_ALMACEN_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxAlmacenProcesoController controller = fXMLLoader.getController();
        controller.setInitAlmacenController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registre su almacen", vbWindow.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
    }

    private void openWindowEdit() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_ALMACEN_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxAlmacenProcesoController controller = fXMLLoader.getController();
            controller.setInitAlmacenController(this);
            controller.loadComponents(tvList.getSelectionModel().getSelectedItem());
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Registre su almacen", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        }
    }

    private void onEventDelete() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short value = Tools.AlertMessageConfirmation(vbWindow, "Almacen", "¿Está seguro de eliminar el almacen?");
            if (value == 1) {
                String result = AlmacenADO.DeletedAlmacenById(tvList.getSelectionModel().getSelectedItem().getIdAlmacen());
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(vbWindow, "Almacen", "Se eliminó correctamente el almacen.");
                    initLoad();
                } else if (result.equalsIgnoreCase("compra")) {
                    Tools.AlertMessageWarning(vbWindow, "Almacen", "El almacen esta ligado a un historial de compras.");
                } else if (result.equalsIgnoreCase("kardex")) {
                    Tools.AlertMessageWarning(vbWindow, "Almacen", "El almacen esta ligado a un kardex de productos.");
                } else if (result.equalsIgnoreCase("principal")) {
                    Tools.AlertMessageWarning(vbWindow, "Almacen", "No se puede eliminar el almacen generado por el sistema.");
                } else {
                    Tools.AlertMessageError(vbWindow, "Almacen", result);
                }
            }
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTableAlmacen(txtSearch.getText().trim());
                break;
        }
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillTableAlmacen(txtSearch.getText().trim());
            opcion = 0;
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAdd();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowAdd();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEdit();
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        openWindowEdit();
    }

    @FXML
    private void onKeyPressedDelete(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventDelete();
        }
    }

    @FXML
    private void onActionDelete(ActionEvent event) {
        onEventDelete();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initLoad();
        }

    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        initLoad();
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
