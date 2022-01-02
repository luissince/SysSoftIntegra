package controller.configuracion.tablasbasicas;

import controller.inventario.suministros.FxSuministrosProcesoController;
import controller.inventario.suministros.FxSuministrosProcesoModalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.DetalleADO;
import model.DetalleTB;

public class FxDetalleListaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtSearch;
    @FXML
    private ListView<DetalleTB> lvList;

    private String idMantenimiento;

    private FxSuministrosProcesoController suministrosProcesoController;

    private FxSuministrosProcesoModalController suministrosProcesoModalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        idMantenimiento = "";
    }

    public void initListDetalle(String idDetalle, String nombre) {
        this.idMantenimiento = idDetalle;
        lvList.getItems().clear();
        DetalleADO.GetDetailIdName("4", idMantenimiento, nombre).forEach(e -> {
            lvList.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
    }

    public void initListNameImpuesto(String idDetalle) {
        this.idMantenimiento = idDetalle;
        this.lvList.getItems().clear();
        DetalleADO.GetDetailNameImpuesto().forEach(e -> {
            this.lvList.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
    }

    private void openWindowProceso() throws IOException {

        URL url = getClass().getResource(FilesRouters.FX_DETALLE_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxDetalleProcesoController controller = fXMLLoader.getController();
        controller.setControllerDetalleLista(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Detalle Agregar", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.show();
        controller.setInitComponents(0, idMantenimiento);
    }

    private void openWindowEditar() throws IOException {
        if (lvList.getSelectionModel().getSelectedIndex() >= 0) {
            URL url = getClass().getResource(FilesRouters.FX_DETALLE_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxDetalleProcesoController controller = fXMLLoader.getController();
            controller.setControllerDetalleLista(this);
            controller.updateDetalle(lvList.getSelectionModel().getSelectedItem().getNombre());
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Detalle Editar", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
            controller.setInitComponents(lvList.getSelectionModel().getSelectedItem().getIdDetalle(),
                    idMantenimiento);
        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Detalle lista", "Seleccione un elemento de la lista para editarlo", false);
            lvList.requestFocus();
        }

    }

    private void selectDetail() {
        if (suministrosProcesoController != null) {
            if (idMantenimiento.equalsIgnoreCase("0006")) {
                suministrosProcesoController.setIdCategoria(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoController.getTxtCategoria().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0013")) {
                suministrosProcesoController.setIdMedida(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoController.getTxtMedida().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0007")) {
                suministrosProcesoController.setIdMarca(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoController.getTxtMarca().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0008")) {
                suministrosProcesoController.setIdPresentacion(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoController.getTxtPresentacion().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            }
        } else if (suministrosProcesoModalController != null) {
            if (idMantenimiento.equalsIgnoreCase("0006")) {
                suministrosProcesoModalController.setIdCategoria(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoModalController.getTxtCategoria().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0013")) {
                suministrosProcesoModalController.setIdMedida(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoModalController.getTxtMedida().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0007")) {
                suministrosProcesoModalController.setIdMarca(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoModalController.getTxtMarca().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            } else if (idMantenimiento.equalsIgnoreCase("0008")) {
                suministrosProcesoModalController.setIdPresentacion(lvList.getSelectionModel().getSelectedItem().getIdDetalle());
                suministrosProcesoModalController.getTxtPresentacion().setText(lvList.getSelectionModel().getSelectedItem().getNombre());
                Tools.Dispose(window);
            }
        }
    }

    @FXML
    private void onKeyPressedProceso(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProceso();
        }
    }

    @FXML
    private void onActionProceso(ActionEvent event) throws IOException {
        openWindowProceso();
    }

    @FXML
    private void onKeyPressedEditar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowEditar();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) throws IOException {
        openWindowEditar();
    }

    @FXML
    private void onKeyPressedToSearh(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            lvList.requestFocus();
            lvList.getSelectionModel().select(0);
        }
    }

    @FXML
    private void onKeyReleasedToSearch(KeyEvent event) {
        initListDetalle(idMantenimiento, txtSearch.getText().trim());
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (lvList.getSelectionModel().getSelectedIndex() >= 0 && lvList.isFocused()) {
            if (event.getClickCount() == 2) {
                selectDetail();

            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (lvList.getSelectionModel().getSelectedIndex() >= 0 && lvList.isFocused()) {
                selectDetail();
            }
        }
    }

    public void setControllerSuministro(FxSuministrosProcesoController suministrosProcesoController) {
        this.suministrosProcesoController = suministrosProcesoController;
    }

    public void setControllerSuministroModal(FxSuministrosProcesoModalController suministrosProcesoModalController) {
        this.suministrosProcesoModalController = suministrosProcesoModalController;
    }

}
