package controller.configuracion.roles;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.MenuADO;
import model.RolADO;

public class FxRolesEditarController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtNombre;
    @FXML
    private ListView<CheckBox> lvMenus;
    @FXML
    private ListView<CheckBox> lbSubmenus;
    @FXML
    private ListView<CheckBox> lvPrivilegios;

    private int idRol;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void loadEditRol(int idRol, String rol) {
        this.idRol = idRol;
        txtNombre.setText(rol);
        lvMenus.getItems().clear();
        MenuADO.GetMenus(idRol).forEach(e -> {
            CheckBox checkBox = new CheckBox();
            checkBox.setId("" + e.getIdMenu());
            checkBox.setText(e.getNombre());
            checkBox.setSelected(e.isEstado());
            checkBox.getStyleClass().add("check-box-contenido");
            checkBox.setOnAction(action -> {
                String result = MenuADO.CrudPermisosMenu(idRol, e.getIdMenu(), checkBox.isSelected());
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(window, "Roles", "Se actualizo correctamente.");
                } else {
                    Tools.AlertMessageError(window, "Roles", result);
                }
            });
            lvMenus.getItems().add(checkBox);
            lbSubmenus.getItems().clear();
        });
    }

    private void eventEditarNombre() {
        if (Tools.isText(txtNombre.getText().trim())) {
            Tools.AlertMessageWarning(window, "Roles", "El campo nombre del rol no puede estar vacío.");
            txtNombre.requestFocus();
        } else {
            short value = Tools.AlertMessageConfirmation(window, "Roles", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = RolADO.editarRol(idRol, txtNombre.getText().trim().toUpperCase());
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(window, "Roles", "Se actualizo correctamente el nombre");
                } else {
                    Tools.AlertMessageError(window, "Roles", result);
                }
            }
        }
    }

    @FXML
    private void onMouseClickedMenus(MouseEvent event) {
        if (lvMenus.getSelectionModel().getSelectedIndex() >= 0 && lvMenus.isFocused()) {
            lbSubmenus.getItems().clear();
            MenuADO.GetSubMenus(
                    idRol,
                    Integer.parseInt(lvMenus.getSelectionModel().getSelectedItem().getId())
            ).forEach(e -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setId("" + e.getIdSubMenu());
                checkBox.setText(e.getNombre());
                checkBox.setSelected(e.isEstado());
                checkBox.getStyleClass().add("check-box-contenido");
                checkBox.setOnAction(action -> {
                    String result = MenuADO.CrudPermisosSubMenu(idRol, e.getIdSubMenu(), checkBox.isSelected());
                    if (result.equalsIgnoreCase("updated")) {
                        Tools.AlertMessageInformation(window, "Roles", "Se actualizo correctamente.");
                    } else {
                        Tools.AlertMessageError(window, "Roles", result);
                    }
                });
                lbSubmenus.getItems().add(checkBox);
            });
        }
    }

    @FXML
    private void onMouseClickedSubMenus(MouseEvent event) {
        if (lbSubmenus.getSelectionModel().getSelectedIndex() >= 0 && lbSubmenus.isFocused()) {
            lvPrivilegios.getItems().clear();
            MenuADO.GetPrivilegios(
                    idRol,
                    Integer.parseInt(lbSubmenus.getSelectionModel().getSelectedItem().getId())
            ).forEach(e -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setText(e.getNombre());
                checkBox.setSelected(e.isEstado());
                checkBox.getStyleClass().add("check-box-contenido");
                checkBox.setOnAction(action -> {
                    String result = MenuADO.CrudPermisosPrivilegios(idRol, e.getIdPrivilegio(), checkBox.isSelected());
                    if (result.equalsIgnoreCase("updated")) {
                        Tools.AlertMessageInformation(window, "Roles", "Se actualizo correctamente.");
                    } else {
                        Tools.AlertMessageError(window, "Roles", result);
                    }
                });
                lvPrivilegios.getItems().add(checkBox);
            });
        }
    }

    @FXML
    private void onKeyPressedEditarNombre(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventEditarNombre();
        }
    }

    @FXML
    private void onActionEditarNombre(ActionEvent event) {
        eventEditarNombre();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

}
