package controller.configuracion.concepto;

import controller.menus.FxPrincipalController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import model.ConceptoTB;

public class FxConceptoController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtBuscar;
    @FXML
    private TableView<ConceptoTB> tvList;
    @FXML
    private TableColumn<ConceptoTB, String> tcId;
    @FXML
    private TableColumn<ConceptoTB, String> tcNombre;
    @FXML
    private TableColumn<ConceptoTB, String> tcTipo;
    @FXML
    private TableColumn<ConceptoTB, String> tcCodigo;
    @FXML
    private TableColumn<ConceptoTB, String> tcFecha;
    @FXML
    private TableColumn<ConceptoTB, String> tcSistema;
    @FXML
    private TableColumn<ConceptoTB, String> tcEstado;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    
    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionAgregar(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedEliminar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionEliminar(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedBuscar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
            }
            event.consume();
        }
    }

    @FXML
    private void onKeyReleasedBuscar(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE
                && event.getCode() != KeyCode.F1
                && event.getCode() != KeyCode.F2
                && event.getCode() != KeyCode.F3
                && event.getCode() != KeyCode.F4
                && event.getCode() != KeyCode.F5
                && event.getCode() != KeyCode.F6
                && event.getCode() != KeyCode.F7
                && event.getCode() != KeyCode.F8
                && event.getCode() != KeyCode.F9
                && event.getCode() != KeyCode.F10
                && event.getCode() != KeyCode.F11
                && event.getCode() != KeyCode.F12
                && event.getCode() != KeyCode.ALT
                && event.getCode() != KeyCode.CONTROL
                && event.getCode() != KeyCode.UP
                && event.getCode() != KeyCode.DOWN
                && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.LEFT
                && event.getCode() != KeyCode.TAB
                && event.getCode() != KeyCode.CAPS
                && event.getCode() != KeyCode.SHIFT
                && event.getCode() != KeyCode.HOME
                && event.getCode() != KeyCode.WINDOWS
                && event.getCode() != KeyCode.ALT_GRAPH
                && event.getCode() != KeyCode.CONTEXT_MENU
                && event.getCode() != KeyCode.END
                && event.getCode() != KeyCode.INSERT
                && event.getCode() != KeyCode.PAGE_UP
                && event.getCode() != KeyCode.PAGE_DOWN
                && event.getCode() != KeyCode.NUM_LOCK
                && event.getCode() != KeyCode.PRINTSCREEN
                && event.getCode() != KeyCode.SCROLL_LOCK
                && event.getCode() != KeyCode.PAUSE) {
            if (!Tools.isText(txtBuscar.getText())) {
                if (!lblLoad.isVisible()) {

                }
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {

            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

            event.consume();
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }
}
