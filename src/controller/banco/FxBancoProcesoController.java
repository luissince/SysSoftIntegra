package controller.banco;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.BancoTB;
import model.MonedaTB;
import service.BancoADO;
import service.MonedaADO;

public class FxBancoProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTitle;
    @FXML
    private TextField txtNombreCuenta;
    @FXML
    private TextField txtNumeroCuenta;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private TextField txtSaldoInicial;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private Button btnProceso;
    @FXML
    private HBox hbCuenta;
    @FXML
    private RadioButton CuentaEfectivo;
    @FXML
    private RadioButton CuentaBancaria;
    @FXML
    private CheckBox cbMostrar;

    private FxBancosController bancosController;

    private String idBanco;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        cbMoneda.getItems().add(new MonedaTB(0, "Seleccione..."));
        cbMoneda.getItems().addAll(MonedaADO.ObtenerListaMonedas());
        cbMoneda.getSelectionModel().select(0);
        idBanco = "";
        ToggleGroup group = new ToggleGroup();
        CuentaEfectivo.setToggleGroup(group);
        CuentaBancaria.setToggleGroup(group);
    }

    public void loadEditBanco(String idBanco) {
        this.idBanco = idBanco;
        lblTitle.setText("Editar cuenta");
        btnProceso.setText("Editar");
        btnProceso.getStyleClass().add("buttonLightWarning");
        BancoTB bancoTB = BancoADO.Obtener_Banco_Por_Id(idBanco);
        if (bancoTB != null) {
            txtNombreCuenta.setText(bancoTB.getNombreCuenta());
            txtNumeroCuenta.setText(bancoTB.getNumeroCuenta());
            for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                if (cbMoneda.getItems().get(i).getIdMoneda() == bancoTB.getIdMoneda()) {
                    cbMoneda.getSelectionModel().select(i);
                    break;
                }
            }
            cbMoneda.setDisable(bancoTB.getSaldoInicial() > 0);
            txtSaldoInicial.setDisable(bancoTB.getSaldoInicial() > 0);
            txtSaldoInicial.setText(Tools.roundingValue(bancoTB.getSaldoInicial(), 4));
            hbCuenta.setDisable(bancoTB.getSaldoInicial() > 0);
            if (bancoTB.getFormaPago() == 1) {
                CuentaEfectivo.setSelected(true);
            } else {
                CuentaBancaria.setSelected(true);
            }
            txtDescripcion.setText(bancoTB.getDescripcion());
            cbMostrar.setSelected(bancoTB.isMostrar());
        }
    }

    private void eventGuardar() {
        if (Tools.isText(txtNombreCuenta.getText())) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Ingrese el nombre de la cuenta.");
            txtNombreCuenta.requestFocus();
        } else if (cbMoneda.getSelectionModel().getSelectedIndex() <= 0) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Seleccione un moneda.");
            cbMoneda.requestFocus();
        } else if (!Tools.isNumeric(txtSaldoInicial.getText())) {
            Tools.AlertMessageWarning(apWindow, "Banco", "Ingrese el saldo inicial.");
            txtSaldoInicial.requestFocus();
        } else {
            BancoTB bancoTB = new BancoTB();
            bancoTB.setIdBanco(idBanco);
            bancoTB.setNombreCuenta(txtNombreCuenta.getText());
            bancoTB.setNumeroCuenta(txtNumeroCuenta.getText());
            bancoTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedIndex() > 0 ? cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda() : 0);
            bancoTB.setFecha(Tools.getDate());
            bancoTB.setHora(Tools.getTime());
            bancoTB.setSaldoInicial(Double.parseDouble(txtSaldoInicial.getText()));
            bancoTB.setDescripcion(txtDescripcion.getText().trim());
            bancoTB.setFormaPago(CuentaEfectivo.isSelected() ? (short) 1 : (short) 2);
            bancoTB.setMostrar(cbMostrar.isSelected());
            short option = Tools.AlertMessageConfirmation(apWindow, "Banco", "¿Está seguro de continuar?");
            if (option == 1) {
                String result = BancoADO.Proceso_Banco(bancoTB);
                if (result.equalsIgnoreCase("duplicate")) {
                    Tools.AlertMessageWarning(apWindow, "Banco", "Existe una cuenta con el mismo nombre.");
                    txtNombreCuenta.requestFocus();
                } else if (result.equalsIgnoreCase("inserted")) {
                    Tools.AlertMessageInformation(apWindow, "Banco", "Se registro correctamente la cuenta.");
                    bancosController.loadTableViewBanco("");
                    Tools.Dispose(apWindow);
                } else if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(apWindow, "Banco", "Se actualizo correctamente la cuenta.");
                    bancosController.loadTableViewBanco("");
                    Tools.Dispose(apWindow);
                } else {
                    Tools.AlertMessageError(apWindow, "Banco", result);
                }
            }
        }
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        eventGuardar();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    @FXML
    private void onKeyTypedSaldoInicial(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtSaldoInicial.getText().contains(".")) {
            event.consume();
        }
    }

    public void setInitBancosController(FxBancosController bancosController) {
        this.bancosController = bancosController;
    }

}
