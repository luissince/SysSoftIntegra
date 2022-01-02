package controller.configuracion.impuestos;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.ImpuestoADO;
import model.ImpuestoTB;

public class FxImpuestoController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<ImpuestoTB> tvList;
    @FXML
    private TableColumn<ImpuestoTB, String> tcNumeracion;
    @FXML
    private TableColumn<ImpuestoTB, String> tcOperacion;
    @FXML
    private TableColumn<ImpuestoTB, String> tcNombre;
    @FXML
    private TableColumn<ImpuestoTB, String> tcValor;
    @FXML
    private TableColumn<ImpuestoTB, ImageView> tcPredeterminado;
    @FXML
    private TableColumn<ImpuestoTB, String> tcCodigoAlterno;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private Text lblPredeterminado;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;
        tcNumeracion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcOperacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombreOperacion()));
        tcNombre.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        tcValor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getValor()));
        tcPredeterminado.setCellValueFactory(new PropertyValueFactory<>("imagePredeterminado"));
        tcCodigoAlterno.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCodigo()));

        tcNumeracion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcOperacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcNombre.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcValor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcCodigoAlterno.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcPredeterminado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        loadInitTable();
    }

    public void loadInitTable() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillTabletTax();
            opcion = 0;
        }
    }

    private void fillTabletTax() {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return ImpuestoADO.ListImpuestos((paginacion - 1) * 20, 20);
            }
        };
        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
            lblPredeterminado.setText("-");
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<ImpuestoTB> impuestoTBs = (ObservableList<ImpuestoTB>) objects[0];
                if (!impuestoTBs.isEmpty()) {
                    tvList.setItems(impuestoTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");

                    for (ImpuestoTB impuestoTB : impuestoTBs) {
                        if (impuestoTB.isPredeterminado()) {
                            lblPredeterminado.setText(impuestoTB.getNombre());
                            break;
                        }
                    }
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

    private void openWindowImpuestoRegister() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_IMPUESTO_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxImpuestoProcesoController controller = fXMLLoader.getController();
        controller.setInitImpuestoController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registre su impuesto", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();

    }

    private void openWindowImpuestoUpdate() {
        try {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_IMPUESTO_PROCESO);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                //Controlller here
                FxImpuestoProcesoController controller = fXMLLoader.getController();
                controller.setInitImpuestoController(this);
                controller.setUpdateImpuesto(tvList.getSelectionModel().getSelectedItem().getIdImpuesto());
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Actualizar su impuesto", window.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                stage.show();
            } else {
                Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Impuesto", "Seleccione un elemento de la lista.", false);
            }
        } catch (IOException ex) {
            System.out.println("openWindowImpuestoUpdate():" + ex.getLocalizedMessage());
        }

    }

    private void onEventProdeteminado() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            String result = ImpuestoADO.ChangeDefaultStateImpuesto(true, tvList.getSelectionModel().getSelectedItem().getIdImpuesto());
            if (result.equalsIgnoreCase("updated")) {
                System.out.println("Entre acaaaaa");
                Tools.AlertMessageInformation(window, "Impuesto", "Se cambio el estado correctamente.");
                loadInitTable();
            } else {
                Tools.AlertMessageError(window, "Impuesto", "Error: " + result);
            }
        } else {
            Tools.AlertMessageWarning(window, "Impuesto", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventDelete() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Impuesto", "¿Está seguro de eliminar el impuesto?", true);
            if (confirmation == 1) {
                String result = ImpuestoADO.DeleteImpuestoById(tvList.getSelectionModel().getSelectedItem().getIdImpuesto());
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(window, "Impuesto", "Se eliminó el impuesto correctamente.");
                    fillTabletTax();
                } else if (result.equalsIgnoreCase("sistema")) {
                    Tools.AlertMessageWarning(window, "Impuesto", "No se puede eliminar el impuesto, ya que es del sistema.");
                } else if (result.equalsIgnoreCase("suministro")) {
                    Tools.AlertMessageWarning(window, "Impuesto", "No se puede eliminar el impuesto, está ligado a un producto.");
                } else {
                    Tools.AlertMessageError(window, "Impuesto", result);
                }
            }
        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Impuesto", "Seleccione un elemento de la lista.", false);
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTabletTax();
                break;
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openWindowImpuestoUpdate();
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowImpuestoRegister();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowImpuestoRegister();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowImpuestoUpdate();
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) {
        openWindowImpuestoUpdate();
    }

    @FXML
    private void onKeyPressedPredetermined(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProdeteminado();
        }
    }

    @FXML
    private void onActionPredetermined(ActionEvent event) {
        onEventProdeteminado();
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
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInitTable();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        loadInitTable();
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {

    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {

    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
