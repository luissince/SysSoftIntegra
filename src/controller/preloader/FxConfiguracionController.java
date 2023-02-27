package controller.preloader;

import controller.tools.Tools;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import org.json.simple.JSONObject;

public class FxConfiguracionController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtPuerto;
    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtClave;
    @FXML
    private TextField txtBaseDatos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    private void onEventAceptar() {
        if (Tools.isText(txtDireccion.getText())) {
            Tools.AlertMessageWarning(apWindow, "Configuración", "Ingrese la dirección del servidor.");
            txtDireccion.requestFocus();
        } else if (!Tools.isNumericInteger(txtPuerto.getText())) {
            Tools.AlertMessageWarning(apWindow, "Configuración", "Ingrese el puerto de conexión.");
            txtPuerto.requestFocus();
        } else if (Tools.isText(txtUsuario.getText())) {
            Tools.AlertMessageWarning(apWindow, "Configuración", "Ingrese el usuario del servidor.");
            txtUsuario.requestFocus();
        } else if (Tools.isText(txtClave.getText())) {
            Tools.AlertMessageWarning(apWindow, "Configuración", "Ingrese la clave del servidor.");
            txtClave.requestFocus();
        } else if (Tools.isText(txtBaseDatos.getText())) {
            Tools.AlertMessageWarning(apWindow, "Configuración", "Ingrese el nombre de la base de datos");
            txtBaseDatos.requestFocus();
        } else {
            JSONObject jsonoBody = new JSONObject();
            JSONObject jsonoContent = new JSONObject();
            jsonoContent.put("addres", txtDireccion.getText().trim());
            jsonoContent.put("port", txtPuerto.getText().trim());
            jsonoContent.put("dbname", txtBaseDatos.getText().trim());
            jsonoContent.put("user", txtUsuario.getText().trim());
            jsonoContent.put("password", txtClave.getText().trim());
            jsonoBody.put("body", jsonoContent);
            try {
                String fileName = "./archivos/connection.json";
                File directory = new File("./archivos");
                if (!directory.exists()) {
                    directory.mkdir();
                }
                File archivoc = new File(fileName);
                if (archivoc.exists()) {
                    archivoc.delete();
                }
                Files.write(Paths.get(fileName), jsonoBody.toJSONString().getBytes());
                Tools.AlertMessageInformation(apWindow, "Configuración",
                        "Se guardar correctamente el archivo de conexión.");
                onEventCerrar();
            } catch (IOException ex) {
                Tools.println(ex);
            }
        }
    }

    private void onEventProbarConexion() {
        if (Tools.isText(txtDireccion.getText())) {
            Tools.AlertMessageWarning(apWindow, "Conexión", "Ingrese la dirección del servidor.");
            txtDireccion.requestFocus();
        } else if (!Tools.isNumericInteger(txtPuerto.getText())) {
            Tools.AlertMessageWarning(apWindow, "Conexión", "Ingrese el puerto de conexión.");
            txtPuerto.requestFocus();
        } else if (Tools.isText(txtUsuario.getText())) {
            Tools.AlertMessageWarning(apWindow, "Conexión", "Ingrese el usuario del servidor.");
            txtUsuario.requestFocus();
        } else if (Tools.isText(txtClave.getText())) {
            Tools.AlertMessageWarning(apWindow, "Conexión", "Ingrese la clave del servidor.");
            txtClave.requestFocus();
        } else if (Tools.isText(txtBaseDatos.getText())) {
            Tools.AlertMessageWarning(apWindow, "Conexión", "Ingrese el nombre de la base de datos");
            txtBaseDatos.requestFocus();
        } else {
            // if (DBUtil.validateConnect(txtDireccion.getText().trim(),
            // txtPuerto.getText().trim(), txtBaseDatos.getText().trim(),
            // txtUsuario.getText().trim(), txtClave.getText().trim()).equals("ok")) {
            Tools.AlertMessageInformation(apWindow, "Conexión", "Se completo la conexión correctamente.");
        }
    }

    private void onEventCerrar() {
        Tools.Dispose(apWindow);
        Platform.exit();
    }

    @FXML
    private void onKeyPressedProbar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProbarConexion();
        }
    }

    @FXML
    private void onActionProbar(ActionEvent event) {
        onEventProbarConexion();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCerrar();
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        onEventCerrar();
    }

    @FXML
    private void onKeyTypedPuerto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

}
