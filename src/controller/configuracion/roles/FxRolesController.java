package controller.configuracion.roles;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.ObjectGlobal;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.MenuADO;
import model.RolADO;
import model.RolTB;

public class FxRolesController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private ListView<RolTB> lvRol;
    @FXML
    private ListView<CheckBox> lvMenus;
    @FXML
    private ListView<CheckBox> lbSubmenus;
    @FXML
    private ListView<CheckBox> lvPrivilegios;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        eventReload();
    }

    private void onViewRolesAgregar() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_ROLES_AGREGAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxRolesAgregarController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Rol", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {

        }

    }

    private void onViewRolesEditar() {
        if (lvRol.getSelectionModel().getSelectedIndex() >= 0) {
            try {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_ROLES_EDITAR);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                //Controlller here
                FxRolesEditarController controller = fXMLLoader.getController();
                controller.loadEditRol(lvRol.getSelectionModel().getSelectedItem().getIdRol(), lvRol.getSelectionModel().getSelectedItem().getNombre());
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Editar Rol", window.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                stage.show();
            } catch (IOException ex) {
            }
        }

    }

    private void eventEliminar() {
        if (lvRol.getSelectionModel().getSelectedIndex() >= 0) {
            String result = RolADO.RemoveRol(lvRol.getSelectionModel().getSelectedItem().getIdRol());
            if (result.equalsIgnoreCase("sistema")) {
                Tools.AlertMessageWarning(window, "Roles", "El rol no se puede eliminar, ya que es propio del sistema.");
            } else if (result.equalsIgnoreCase("exists")) {
                Tools.AlertMessageWarning(window, "Roles", "El rol no se puede eliminar, ya que esta asignado a un empleado.");
            } else if (result.equalsIgnoreCase("removed")) {
                Tools.AlertMessageInformation(window, "Roles", "Se elimino el rol correctamente.");
                eventReload();
            } else {
                Tools.AlertMessageError(window, "Roles", result);
            }
        }
    }

    private void eventReload() {
        lvRol.getItems().clear();
        RolADO.RolList().forEach(e -> {
            lvRol.getItems().add(new RolTB(e.getIdRol(), e.getNombre(), e.isSistema()));
        });
        lvMenus.getItems().clear();
        lbSubmenus.getItems().clear();
        lvPrivilegios.getItems().clear();
    }

    @FXML
    private void onMouseClickedRoles(MouseEvent event) {
        if (lvRol.getSelectionModel().getSelectedIndex() >= 0 && lvRol.isFocused()) {
            lvMenus.getItems().clear();
            MenuADO.GetMenus(lvRol.getSelectionModel().getSelectedItem().getIdRol()).forEach(e -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setDisable(true);
                checkBox.setId("" + e.getIdMenu());
                checkBox.setText(e.getNombre());
                checkBox.setSelected(e.isEstado());
                checkBox.getStyleClass().add("check-box-contenido");
                lvMenus.getItems().add(checkBox);
                lbSubmenus.getItems().clear();
            });
        }
    }

    @FXML
    private void onMouseClickedMenus(MouseEvent event) {
        if (lvMenus.getSelectionModel().getSelectedIndex() >= 0 && lvMenus.isFocused()) {
            lbSubmenus.getItems().clear();
            MenuADO.GetSubMenus(
                    lvRol.getSelectionModel().getSelectedItem().getIdRol(),
                    Integer.parseInt(lvMenus.getSelectionModel().getSelectedItem().getId())
            ).forEach(e -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setDisable(true);
                checkBox.setId("" + e.getIdSubMenu());
                checkBox.setText(e.getNombre());
                checkBox.setSelected(e.isEstado());
                checkBox.getStyleClass().add("check-box-contenido");
                lbSubmenus.getItems().add(checkBox);
            });
        }
    }

    @FXML
    private void onMouseClickedSubMenus(MouseEvent event) {
        if (lbSubmenus.getSelectionModel().getSelectedIndex() >= 0 && lbSubmenus.isFocused()) {
            lvPrivilegios.getItems().clear();
            MenuADO.GetPrivilegios(
                    lvRol.getSelectionModel().getSelectedItem().getIdRol(),
                    Integer.parseInt(lbSubmenus.getSelectionModel().getSelectedItem().getId())
            ).forEach(e -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setDisable(true);
                checkBox.setText(e.getNombre());
                checkBox.setSelected(e.isEstado());
                checkBox.getStyleClass().add("check-box-contenido");
                lvPrivilegios.getItems().add(checkBox);
            });
        }
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewRolesAgregar();
        }
    }

    @FXML
    private void onActionAgregar(ActionEvent event) {
        onViewRolesAgregar();
    }

    @FXML
    private void onKeyPressedEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewRolesEditar();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
        onViewRolesEditar();
    }

    @FXML
    private void onKeyPressedRemover(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventEliminar();
        }
    }

    @FXML
    private void onActionRemover(ActionEvent event) {
        eventEliminar();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventReload();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        eventReload();
    }

    public void setContent( FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
