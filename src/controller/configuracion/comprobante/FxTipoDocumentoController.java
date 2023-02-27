package controller.configuracion.comprobante;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.TipoDocumentoTB;
import service.TipoDocumentoADO;

public class FxTipoDocumentoController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<TipoDocumentoTB> tvList;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcNumero;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcTipoComprobante;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcSerie;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcNumeracion;
    @FXML
    private TableColumn<TipoDocumentoTB, Label> tcDestino;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcCodigoAlterno;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcEstado;
    @FXML
    private TableColumn<TipoDocumentoTB, String> tcCaracteres;
    @FXML
    private TableColumn<TipoDocumentoTB, ImageView> tcPredeterminado;
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
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcTipoComprobante.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        tcSerie.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSerie()));
        tcNumeracion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeracion()));
        tcDestino.setCellValueFactory(new PropertyValueFactory<>("lblDestino"));
        tcEstado.setCellValueFactory(
                cellData -> Bindings.concat(cellData.getValue().isEstado() ? "Activo" : "Inactivo"));
        tcCodigoAlterno.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCodigoAlterno()));
        tcCaracteres.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().isCampo() ? "Si" : "No"));
        tcPredeterminado.setCellValueFactory(new PropertyValueFactory<>("ivPredeterminado"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcTipoComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcSerie.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcNumeracion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcDestino.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcCodigoAlterno.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcCaracteres.prefWidthProperty().bind(tvList.widthProperty().multiply(0.09));
        tcPredeterminado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        initLoad();
    }

    public void initLoad() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillTabletTipoDocumento(0, "");
            opcion = 0;
        }
    }

    private void fillTabletTipoDocumento(int opcion, String buscar) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return TipoDocumentoADO.ListTipoDocumento(opcion, buscar, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<TipoDocumentoTB> notaCreditoTBs = (ObservableList<TipoDocumentoTB>) objects[0];
                if (!notaCreditoTBs.isEmpty()) {
                    tvList.setItems(notaCreditoTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 10.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");

                    for (int i = 0; i < notaCreditoTBs.size(); i++) {
                        if (notaCreditoTBs.get(i).isPredeterminado()) {
                            lblPredeterminado.setText(notaCreditoTBs.get(i).getNombre());
                            break;
                        } else {
                            lblPredeterminado.setText("Ninguno");
                        }
                    }

                } else {
                    tvList.setPlaceholder(
                            Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                    lblPredeterminado.setText("Ninguno");
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
                lblPredeterminado.setText("Ninguno");
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));
            lblPredeterminado.setText("Ninguno");
        });

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(
                    Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
            lblPredeterminado.setText("Ninguno");
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTabletTipoDocumento(0, "");
                break;
        }
    }

    private void openWindowAdd() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_TIPO_DOCUMENTO_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxTipoDocumentoProcesoController controller = fXMLLoader.getController();
        controller.setTipoDocumentoController(this);

        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Nuevo comprobante", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.setOnShown(w -> controller.initCreate());
        stage.show();
    }

    private void openWindowEdit() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TIPO_DOCUMENTO_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTipoDocumentoProcesoController controller = fXMLLoader.getController();
            controller.setTipoDocumentoController(this);

            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Actualizar el comprobante",
                    window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(
                    w -> controller.initUpdate(tvList.getSelectionModel().getSelectedItem().getIdTipoDocumento()));
            stage.show();
        } else {
            Tools.AlertMessageWarning(window, "Tipo de comprobante", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventPredeterminado() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            String result = TipoDocumentoADO.ChangeDefaultState(true,
                    tvList.getSelectionModel().getSelectedItem().getIdTipoDocumento());
            if (result.equalsIgnoreCase("updated")) {
                Tools.AlertMessageInformation(window, "Tipo de comprobante", "Se cambio el estado correctamente.");
                initLoad();
            } else {
                Tools.AlertMessageError(window, "Tipo de comprobante", "Error: " + result);
            }
        } else {
            Tools.AlertMessageWarning(window, "Tipo de comprobante", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventRemove() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short value = Tools.AlertMessageConfirmation(window, "Tipo de comprobante",
                    "¿Esta seguro de eliminar el tipo de comprobante?");
            if (value == 1) {
                String result = TipoDocumentoADO
                        .EliminarTipoDocumento(tvList.getSelectionModel().getSelectedItem().getIdTipoDocumento());
                if (result.equalsIgnoreCase("removed")) {
                    Tools.AlertMessageInformation(window, "Tipo de comprobante",
                            "Se elimino correctamente el tipo de documento.");
                    initLoad();
                } else if (result.equalsIgnoreCase("venta")) {
                    Tools.AlertMessageWarning(window, "Tipo de comprobante",
                            "El tipo de documento esta ligado a una venta.");
                } else if (result.equalsIgnoreCase("notacredito")) {
                    Tools.AlertMessageWarning(window, "Tipo de comprobante",
                            "El tipo de documento esta ligado a una nota de crédito.");
                } else if (result.equalsIgnoreCase("sistema")) {
                    Tools.AlertMessageWarning(window, "Tipo de comprobante",
                            "El tipo de documento no se puede eliminar porque es del sistema.");
                } else {
                    Tools.AlertMessageError(window, "Tipo de comprobante", result);
                }
            }
        } else {
            Tools.AlertMessageWarning(window, "Tipo de comprobante", "Seleccione un elemento de la lista.");
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
    private void onKeyPressedRemove(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRemove();
        }
    }

    @FXML
    private void onActionRemove(ActionEvent event) {
        onEventRemove();
    }

    @FXML
    private void onKeyPressedPredetermined(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPredeterminado();
        }
    }

    @FXML
    private void onActionPredetermined(ActionEvent event) {
        onEventPredeterminado();
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowEdit();
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initLoad();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
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
