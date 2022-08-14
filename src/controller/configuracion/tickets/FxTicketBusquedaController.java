package controller.configuracion.tickets;

import controller.inventario.valorinventario.FxValorInventarioController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.TicketADO;
import model.TicketTB;

public class FxTicketBusquedaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ListView<TicketTB> lvLista;

    private FxTicketController ticketController;

    private FxValorInventarioController valorInventarioController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void loadComponents(int tipo, boolean todos) {
        lvLista.getItems().addAll(TicketADO.ListTicketOpcion(tipo, todos));
    }

    private void selectTicket() {
        if (ticketController != null) {
            if (lvLista.getSelectionModel().getSelectedIndex() >= 0) {
                ticketController.loadTicket(
                        lvLista.getSelectionModel().getSelectedItem().getId(),
                        lvLista.getSelectionModel().getSelectedItem().getTipo(),
                        lvLista.getSelectionModel().getSelectedItem().getNombreTicket(),
                        lvLista.getSelectionModel().getSelectedItem().getRuta(),
                        lvLista.getSelectionModel().getSelectedItem().isPredeterminado()
                );
                Tools.Dispose(window);
            }
        } else if (valorInventarioController != null) {
            if (lvLista.getSelectionModel().getSelectedIndex() >= 0) {
//                valorInventarioController.setGenerarTicket(lvLista.getSelectionModel().getSelectedItem().getId(),
//                        lvLista.getSelectionModel().getSelectedItem().getRuta());
                Tools.Dispose(window);
            }
        }

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

    @FXML
    private void onMouseClickedLista(MouseEvent event) {
        if (event.getClickCount() == 2) {
            selectTicket();
        }
    }

    @FXML
    private void onKeyPressedSelect(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            selectTicket();
        }
    }

    @FXML
    private void onActionSelect(ActionEvent event) {
        selectTicket();
    }

    public void setInitTicketController(FxTicketController ticketController) {
        this.ticketController = ticketController;
    }

    public void setInitValorInventarioController(FxValorInventarioController valorInventarioController) {
        this.valorInventarioController = valorInventarioController;
    }

}
