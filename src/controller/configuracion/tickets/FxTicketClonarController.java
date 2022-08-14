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
import model.TicketADO;
import model.TicketTB;

public class FxTicketClonarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<TicketTB> cbTpo;
    @FXML
    private Button btnSave;

    private FxTicketController ticketController;

    private int idTicket;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        Object object = TicketADO.ListTipoTicket();
        if (object instanceof ArrayList) {
            cbTpo.getItems().addAll((ArrayList) object);
        }
    }

    public void loadComponents(int idTicket) {
        this.idTicket = idTicket;
    }

    private void onEventAdd() {
        if (Tools.isText(txtNombre.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Ticket", "Ingrese el nombre del ticket.");
            txtNombre.requestFocus();
        } else if (cbTpo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Ticket", "Seleccione el tipo de ticket.");
            cbTpo.requestFocus();
        } else {
            TicketTB ticketTB = new TicketTB();
            ticketTB.setIdTicket(idTicket);
            ticketTB.setNombreTicket(txtNombre.getText().trim().toUpperCase());
            ticketTB.setTipo(cbTpo.getSelectionModel().getSelectedItem().getId());
            String result = TicketADO.ClonarTicket(ticketTB);
            if (result.equalsIgnoreCase("inserted")) {
                Tools.AlertMessageInformation(apWindow, "Ticket", "Se clonó correctamente");
                Tools.Dispose(apWindow);
                ticketController.clearPane();
            } else {
                Tools.AlertMessageWarning(apWindow, "Ticket", result);
            }
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        onEventAdd();
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAdd();
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

    public void setInitTicketController(FxTicketController ticketController) {
        this.ticketController = ticketController;
    }

}
