package controller.configuracion.empleados;

import controller.tools.Tools;
import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import model.DetalleTB;
import model.EmpleadoTB;
import model.RolTB;
import service.DetalleADO;
import service.EmpleadoADO;
import service.RolADO;

public class FxEmpleadosProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtApellidos;
    @FXML
    private TextField txtNombres;
    @FXML
    private ComboBox<DetalleTB> cbSexo;
    @FXML
    private DatePicker dpFechaNacimiento;
    @FXML
    private ComboBox<DetalleTB> cbEstado;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtClave;
    @FXML
    private ComboBox<RolTB> cbRol;
    @FXML
    private ImageView ivPerfil;
    @FXML
    private Button btnRegister;

    private String idEmpleado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        cbTipoDocumento.getItems().addAll(DetalleADO.Get_Detail_IdName("1", "0003", "RUC"));
        cbSexo.getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0004"));
        cbRol.getItems().addAll(RolADO.RolList());
        cbEstado.getItems().addAll(DetalleADO.Get_Detail_IdName("2", "0001", ""));
        cbEstado.getSelectionModel().select(0);
        idEmpleado = "";
    }

    public void setEditEmpleado(String value) {
        btnRegister.setText("Actualizar");
        btnRegister.getStyleClass().add("buttonLightWarning");
        EmpleadoTB empleadoTB = EmpleadoADO.GetByIdEmpleados(value);
        if (empleadoTB != null) {

            idEmpleado = value;

            ObservableList<DetalleTB> lstype = cbTipoDocumento.getItems();
            for (int i = 0; i < lstype.size(); i++) {
                if (empleadoTB.getTipoDocumento() == lstype.get(i).getIdDetalle()) {
                    cbTipoDocumento.getSelectionModel().select(i);
                    break;
                }
            }

            txtNumeroDocumento.setText(empleadoTB.getNumeroDocumento());
            txtApellidos.setText(empleadoTB.getApellidos());
            txtNombres.setText(empleadoTB.getNombres());

            if (empleadoTB.getSexo() != 0) {
                ObservableList<DetalleTB> lstsex = cbSexo.getItems();
                for (int i = 0; i < lstsex.size(); i++) {
                    if (empleadoTB.getSexo() == lstsex.get(i).getIdDetalle()) {
                        cbSexo.getSelectionModel().select(i);
                        break;
                    }
                }
            }

            if (empleadoTB.getFechaNacimiento() != null) {
                Tools.actualDate(empleadoTB.getFechaNacimiento().toString(), dpFechaNacimiento);
            }

            if (empleadoTB.getEstado() != 0) {
                ObservableList<DetalleTB> lstest = cbEstado.getItems();
                for (int i = 0; i < lstest.size(); i++) {
                    if (empleadoTB.getEstado() == lstest.get(i).getIdDetalle()) {
                        cbEstado.getSelectionModel().select(i);
                        break;
                    }
                }
            }

            txtTelefono.setText(empleadoTB.getTelefono());
            txtCelular.setText(empleadoTB.getCelular());
            txtEmail.setText(empleadoTB.getEmail());
            txtDireccion.setText(empleadoTB.getDireccion());

            txtUsuario.setText(empleadoTB.getUsuario());
            txtClave.setText(empleadoTB.getClave());

            if (empleadoTB.getRol() != 0) {
                ObservableList<RolTB> lsrol = cbRol.getItems();
                for (int i = 0; i < lsrol.size(); i++) {
                    if (empleadoTB.getRol() == lsrol.get(i).getIdRol()) {
                        cbRol.getSelectionModel().select(i);
                        break;
                    }
                }
            }

        }
    }

    private void onActionProcessCrud() throws ParseException {
        if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Empleado", "Seleccione el tipo de documento del empleado");
            cbTipoDocumento.requestFocus();
            return;
        }

        if (Tools.isText(txtNumeroDocumento.getText())) {
            Tools.AlertMessageWarning(window, "Empleado", "Ingrese el número del documento del empleado");
            txtNumeroDocumento.requestFocus();
            return;
        }

        if (Tools.isText(txtApellidos.getText())) {
            Tools.AlertMessageWarning(window, "Empleado", "Ingrese los apellidos del empleado");
            txtApellidos.requestFocus();
            return;
        }

        if (Tools.isText(txtNombres.getText())) {
            Tools.AlertMessageWarning(window, "Empleado", "Ingrese los nombres del empleado");
            txtNombres.requestFocus();
            return;
        }

        if (cbEstado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Empleado", "Seleccione el estado del empleado");
            cbEstado.requestFocus();
            return;
        }

        short confirmation = Tools.AlertMessageConfirmation(window, "Empleado", "¿Esta seguro de continuar?");
        if (confirmation == 1) {
            EmpleadoTB empleadoTB = new EmpleadoTB();
            empleadoTB.setIdEmpleado(idEmpleado);
            empleadoTB.setTipoDocumento(cbTipoDocumento.getSelectionModel().getSelectedIndex() >= 0
                    ? cbTipoDocumento.getSelectionModel().getSelectedItem().getIdDetalle()
                    : 0);
            empleadoTB.setNumeroDocumento(txtNumeroDocumento.getText().trim());
            empleadoTB.setApellidos(txtApellidos.getText().trim().toUpperCase());
            empleadoTB.setNombres(txtNombres.getText().trim().toUpperCase());
            empleadoTB.setSexo(cbSexo.getSelectionModel().getSelectedIndex() >= 0
                    ? cbSexo.getSelectionModel().getSelectedItem().getIdDetalle()
                    : 0);

            if (dpFechaNacimiento.getValue() != null) {
                empleadoTB.setFechaNacimiento(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd")
                        .parse(Tools.getDatePicker(dpFechaNacimiento)).getTime()));
            } else {
                empleadoTB.setFechaNacimiento(null);
            }

            empleadoTB.setPuesto(0);
            empleadoTB.setEstado(cbEstado.getSelectionModel().getSelectedIndex() >= 0
                    ? cbEstado.getSelectionModel().getSelectedItem().getIdDetalle()
                    : 0);
            empleadoTB.setTelefono(txtTelefono.getText().trim());
            empleadoTB.setCelular(txtCelular.getText().trim());
            empleadoTB.setEmail(txtEmail.getText().trim().toUpperCase());
            empleadoTB.setDireccion(txtDireccion.getText().trim().toUpperCase());
            empleadoTB.setUsuario(txtUsuario.getText().trim());
            empleadoTB.setClave(txtClave.getText().trim());
            empleadoTB.setRol(cbRol.getSelectionModel().getSelectedIndex() >= 0
                    ? cbRol.getSelectionModel().getSelectedItem().getIdRol()
                    : 0);
            empleadoTB.setSistema(false);
            empleadoTB.setHuella("");

            if (idEmpleado.equalsIgnoreCase("")) {
                String result = EmpleadoADO.InsertEmpleado(empleadoTB);
                switch (result) {
                    case "register":
                        Tools.AlertMessageInformation(window, "Empleado", "Registrado correctamente el empleado.");
                        Tools.Dispose(window);
                        break;
                    default:
                        Tools.AlertMessageError(window, "Empleado", result);
                        break;
                }
            } else {
                String result = EmpleadoADO.UpdateEmpleado(empleadoTB);
                switch (result) {
                    case "update":
                        Tools.AlertMessageInformation(window, "Empleado", "Actualizado correctamente el empleado.");
                        Tools.Dispose(window);
                        break;
                    default:
                        Tools.AlertMessageError(window, "Empleado", result);
                        break;
                }
            }
        }
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) throws ParseException {
        if (event.getCode() == KeyCode.ENTER) {
            onActionProcessCrud();

        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) throws ParseException {
        onActionProcessCrud();
    }

    @FXML
    private void onKeyPressedToCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onMouseClickedImage(MouseEvent event) {
        if (event.getClickCount() == 2) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Importar una imagen");
            fileChooser.getExtensionFilters()
                    .addAll(new FileChooser.ExtensionFilter("Elija una imagen", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(window.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("png") || file.getName().endsWith("jpg")
                        || file.getName().endsWith("jpeg")) {
                    ivPerfil.setImage(new Image(file.toURI().toString()));

                }
            }
        }
    }

    @FXML
    private void onActionToCancel(ActionEvent event) {
        Tools.Dispose(window);
    }

}
