package controller.configuracion.tablasbasicas;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import model.DetalleADO;
import model.DetalleTB;
import static model.MantenimientoADO.*;
import static model.DetalleADO.*;
import model.MantenimientoTB;

public class FxDetalleMantenimientoController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private TextField txtSearchMaintenance;
    @FXML
    private ListView<MantenimientoTB> lvMaintenance;
    @FXML
    private TextField txtSearchDetail;
    @FXML
    private Text lblItems;
    @FXML
    private TableView<DetalleTB> tvDetail;
    @FXML
    private TableColumn<DetalleTB, String> tcNumero;
    @FXML
    private TableColumn<DetalleTB, String> tcCodAuxiliar;
    @FXML
    private TableColumn<DetalleTB, String> tcNombre;
    @FXML
    private TableColumn<DetalleTB, String> tcDescripcion;
    @FXML
    private TableColumn<DetalleTB, String> tcEstado;
    @FXML
    private Label lblWarnings;
    @FXML
    private ImageView imWarning;
    @FXML
    private Text lblDetail;

    private boolean onAnimationStart, onAnimationFinished;

    private  FxPrincipalController fxPrincipalController;

    private String validarProceso;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        onAnimationFinished = false;
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getIdDetalle()));
        tcCodAuxiliar.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getIdAuxiliar()));
        tcNombre.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombre()));
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDescripcion()));
        tcEstado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEstado()));

        tcNumero.prefWidthProperty().bind(tvDetail.widthProperty().multiply(0.06));
        tcCodAuxiliar.prefWidthProperty().bind(tvDetail.widthProperty().multiply(0.15));
        tcNombre.prefWidthProperty().bind(tvDetail.widthProperty().multiply(0.25));
        tcDescripcion.prefWidthProperty().bind(tvDetail.widthProperty().multiply(0.40));
        tcEstado.prefWidthProperty().bind(tvDetail.widthProperty().multiply(0.12));

        validarProceso = "0";
    }

    private void initMaintenance(String... value) {
        lvMaintenance.getItems().clear();
        ObservableList<MantenimientoTB> listMaintenance = ListMantenimiento(value[0]);
        listMaintenance.forEach(e
                -> lvMaintenance.getItems().add(e)
        );
        lblItems.setText(listMaintenance.isEmpty() == true ? "Items (0)" : "Items (" + listMaintenance.size() + ")");
        if (!lvMaintenance.getItems().isEmpty()) {
            lvMaintenance.getSelectionModel().select(0);
            MantenimientoTB mtb = lvMaintenance.getSelectionModel().getSelectedItem();
            initDetail(mtb.getIdMantenimiento(), "");
        }
    }

    public void initDetail(String value, String search) {
        ObservableList<DetalleTB> listDetail = ListDetail(value, search);
        validarProceso = value.equalsIgnoreCase("0010") ? "1" : "0";
        tvDetail.setItems(listDetail);
        lblDetail.setText(listDetail.isEmpty() == true ? "Ingrese el nombre del detalle (0)" : "Ingrese el nombre del detalle (" + listDetail.size() + ")");
    }

    @FXML
    private void onMouseClickedAgregar(MouseEvent event) throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_MENTANIMIENTO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxMantenimientoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Agregar item de mantenimiento", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
        stage.show();
        controller.initWindow();
    }

    @FXML
    private void onMouseClickedEditar(MouseEvent event) throws IOException {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0 && lvMaintenance.isFocused()) {
          fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_MENTANIMIENTO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxMantenimientoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Editar item de mantenimiento", window.getScene().getWindow());
            stage.setResizable(false);
            stage.setOnHiding((WindowEvent WindowEvent) -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.setValues(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(), lvMaintenance.getSelectionModel().getSelectedItem().getNombre());
        }
    }

    @FXML
    private void onMouseClickedRemover(MouseEvent event) {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0 && lvMaintenance.isFocused()) {
            short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Mantenimiento", "¿Esta seguro de continuar?", true);
            if (confirmation == 1) {
                String result = DeleteMantenimiento(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento());
                switch (result) {
                    case "eliminado":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Detalle mantenimiento", "Eliminado correctamente.", false);
                        reloadListView();
                        break;
                    case "error":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Detalle mantenimiento", "No se puedo completar la ejecución.", false);
                        break;
                    default:
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Detalle mantenimiento", result, false);
                        break;
                }
            } else {
                Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Detalle mantenimiento", "Se cancelo la petición.", false);

            }
        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Detalle mantenimiento", "No hay conexión al servidor.", false);
        }

    }

    public void reloadListView() {
        initMaintenance("");
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws ClassNotFoundException, SQLException {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0) {
            initDetail(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(), "");
        }
    }

    @FXML
    private void onKeyReleasedList(KeyEvent event) throws ClassNotFoundException, SQLException {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0) {
            initDetail(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(), "");
        }
    }

    private void onActionEditDetail() throws IOException {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0 && tvDetail.getSelectionModel().getSelectedIndex() >= 0) {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_DETALLE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxDetalleController controller = fXMLLoader.getController();
            controller.initConfiguracion(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar detalle del item", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.setValueUpdate(lvMaintenance.getSelectionModel().getSelectedItem().getNombre(),
                    lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(),
                    tvDetail.getSelectionModel().getSelectedItem().getIdDetalle() + "",
                    tvDetail.getSelectionModel().getSelectedItem().getIdAuxiliar(),
                    tvDetail.getSelectionModel().getSelectedItem().getNombre(),
                    tvDetail.getSelectionModel().getSelectedItem().getDescripcion(),
                    tvDetail.getSelectionModel().getSelectedItem().getEstado());

        } else {
            onAnimationStart = true;
            if (onAnimationStart && !onAnimationFinished) {
                onAnimationFinished = true;
                lblWarnings.setText("Seleccione un item del detalle para actualizar.");
                lblWarnings.setStyle("-fx-text-fill:#da0505");
                imWarning.setImage(new Image("/view/image/warning.png"));
                tvDetail.requestFocus();
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), imWarning);
                scaleTransition.setByX(0.4f);
                scaleTransition.setByY(0.4f);
                scaleTransition.setCycleCount(4);
                scaleTransition.setAutoReverse(true);
                scaleTransition.setOnFinished((ActionEvent action) -> {
                    lblWarnings.setText("Las opciones del detalle están en el panel de los botonen.");
                    lblWarnings.setStyle("-fx-text-fill:#23283a");
                    imWarning.setImage(null);
                    onAnimationFinished = false;
                    onAnimationStart = false;
                });
                scaleTransition.play();

            }
        }
    }

    @FXML
    private void onMouseClickedDetail(MouseEvent event) throws IOException {
        if (tvDetail.getSelectionModel().getSelectedIndex() >= 0) {
            if (event.getClickCount() == 2) {
                onActionEditDetail();
            }
        }
    }

    @FXML
    private void onActionSearchItems(ActionEvent event) {
        lvMaintenance.requestFocus();
    }

    @FXML
    private void onKeyReleasedSearchItems(KeyEvent event) {
        initMaintenance(txtSearchMaintenance.getText());
    }

    @FXML
    private void onActionSearchDetail(ActionEvent event) {
        tvDetail.requestFocus();
    }

    @FXML
    private void onKeyReleasedSearchDetail(KeyEvent event) throws ClassNotFoundException, SQLException {
        initDetail(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(), txtSearchDetail.getText().trim());
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0 && validarProceso.equals("0")) {
           fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_DETALLE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxDetalleController controller = fXMLLoader.getController();
            controller.setValueAdd(lvMaintenance.getSelectionModel().getSelectedItem().getNombre(),
                    lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento(),
                    tvDetail.getSelectionModel().getSelectedIndex() >= 0 ? tvDetail.getSelectionModel().getSelectedItem().getIdDetalle() + "" : "0");
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar detalle del item", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
            stage.show();

        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Detalle mantenimiento", "No hay seleccionado un item del matenimiento o no esta permitido su acción.", false);
            lvMaintenance.requestFocus();
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        onActionEditDetail();
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        reloadListView();
    }

    @FXML
    private void onActionRemover(ActionEvent event) {
        if (lvMaintenance.getSelectionModel().getSelectedIndex() >= 0 && tvDetail.getSelectionModel().getSelectedIndex() >= 0 && validarProceso.equalsIgnoreCase("0")) {
            short confirmation = Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Mantenimiento", "¿Esta seguro de continuar?", true);
            if (confirmation == 1) {
                DetalleTB detalleTB = new DetalleTB();
                detalleTB.setIdDetalle(tvDetail.getSelectionModel().getSelectedItem().getIdDetalle());
                detalleTB.setIdMantenimiento(lvMaintenance.getSelectionModel().getSelectedItem().getIdMantenimiento());
                String result = DetalleADO.DeleteDetail(detalleTB);
                switch (result) {
                    case "eliminado":
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.INFORMATION, "Detalle mantenimiento", "Eliminado correctamente.", false);
                        reloadListView();
                        break;
                    default:
                        Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.ERROR, "Detalle mantenimiento", result, false);
                        break;
                }
            }
        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Detalle mantenimiento", "Seleccione el item para removerlo o no esta permitido su acción.", false);
            tvDetail.requestFocus();
        }

    }

    public void setContent( FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
