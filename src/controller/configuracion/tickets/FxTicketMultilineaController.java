package controller.configuracion.tickets;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import controller.tools.TextFieldTicket;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class FxTicketMultilineaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextArea textArea;

    private FxTicketController ticketController;

    private HBox hBox;

    private int sheetWidth;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
    }

    public void setLoadComponent(HBox hBox, int sheetWidth) {
        this.hBox = hBox;
        this.sheetWidth = sheetWidth;
    }

    private void addTextMultilinea() {
        if (!textArea.getText().trim().isEmpty()) {
            short widthContent = 0;
            for (short i = 0; i < hBox.getChildren().size(); i++) {
                widthContent += ((TextFieldTicket) hBox.getChildren().get(i)).getColumnWidth();
            }
            if (widthContent <= sheetWidth) {
                short widthNew = (short) (sheetWidth - widthContent);
                if (widthNew <= 0 || widthNew > sheetWidth) {
                    Tools.AlertMessageWarning(window, "Ticket", "No hay espacio suficiente en la fila.");
                } else {
                    TextFieldTicket field = ticketController.addElementTextField("iu", textArea.getText(), true, (short) 1, widthNew, Pos.CENTER_LEFT, true, "","Consola",12.5f);
                    hBox.getChildren().add(field);
                    Tools.Dispose(window);
                } 
            }
        } else {
            Tools.AlertMessage(window.getScene().getWindow(), Alert.AlertType.WARNING, "Ticket", "El área de texto no puede estar vacío.", false);
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addTextMultilinea();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        addTextMultilinea();
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
    private void onKeyPresseTextArea(KeyEvent event) {
        if (event.getCode() == KeyCode.TAB) {
            Node node = (Node) event.getSource();
            if (node instanceof TextArea) {
                TextAreaSkin skin = (TextAreaSkin) ((TextArea) node).getSkin();
                if (!event.isControlDown()) {
                    if (event.isShiftDown()) {
                        skin.getBehavior().traversePrevious();
                    } else {
                        skin.getBehavior().traverseNext();
                    }
                } else {
                    TextArea textA = (TextArea) node;
                    textA.replaceSelection("\t");
                }
                event.consume();
            }
        }
    }

    public void setInitTicketController(FxTicketController ticketController) {
        this.ticketController = ticketController;
    }

}
