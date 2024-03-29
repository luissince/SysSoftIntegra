package controller.configuracion.tickets;

import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.TicketTB;
import service.TicketADO;

public class FxTicketProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtColumnas;
    @FXML
    private ComboBox<TicketTB> cbTpo;
    @FXML
    private Button btnSave;

    private FxTicketController ticketController;

    private boolean editar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        Object object = TicketADO.ListTipoTicket();
        if (object instanceof ArrayList) {
            cbTpo.getItems().addAll((ArrayList) object);
        }
        editar = false;
    }

    public void editarTicket(int tipoTicket, String name, short column) {
        editar = true;
        btnSave.setText("Editar");
        btnSave.getStyleClass().add("buttonLightWarning");
        txtNombre.setText(name);
        txtColumnas.setText(column + "");
        for (int i = 0; i < cbTpo.getItems().size(); i++) {
            if (cbTpo.getItems().get(i).getId() == tipoTicket) {
                cbTpo.getSelectionModel().select(i);
                break;
            }
        }
        cbTpo.setDisable(true);
    }

    private void addTicket() {
        if (editar) {
            if (!Tools.isNumericInteger(txtColumnas.getText().trim())) {
                Tools.AlertMessageWarning(apWindow, "Ticket", "El valor en el campo columna no es un número");
                txtColumnas.requestFocus();
            } else if (Short.parseShort(txtColumnas.getText()) <= 0) {
                Tools.AlertMessageWarning(apWindow, "Ticket", "El valor en el campo columna es menor que 0");
                txtColumnas.requestFocus();
            } else {
                ticketController.editarTicket(editar, cbTpo.getSelectionModel().getSelectedItem().getId(), txtNombre.getText().trim(), Short.parseShort(txtColumnas.getText()));
                Tools.Dispose(apWindow);
            }
        } else {
            if (!Tools.isNumericInteger(txtColumnas.getText().trim())) {
                Tools.AlertMessageWarning(apWindow, "Ticket", "El valor en el campo columna no es un número");
                txtColumnas.requestFocus();
            } else if (Short.parseShort(txtColumnas.getText()) <= 0) {
                Tools.AlertMessageWarning(apWindow, "Ticket", "El valor en el campo columna es menor que 0");
                txtColumnas.requestFocus();
            } else if (cbTpo.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(apWindow, "Ticket", "Seleccione el tipo de ticket");
                cbTpo.requestFocus();
            } else {
                ticketController.editarTicket(editar, cbTpo.getSelectionModel().getSelectedItem().getId(), txtNombre.getText().trim(), Short.parseShort(txtColumnas.getText()));
                Tools.Dispose(apWindow);
            }
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        addTicket();
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addTicket();
        }
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
    private void onKeyTypedColumnas(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    public void setInitTicketController(FxTicketController ticketController) {
        this.ticketController = ticketController;
    }

}
