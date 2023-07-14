
package controller.consultas.ingreso;

import java.net.URL;
import java.util.ResourceBundle;

import controller.menus.FxPrincipalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import model.IngresoTB;

public class FxIngresoRealizadoController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Button btnMostrar;
    @FXML
    private Button btnRecargar;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private ComboBox<?> cbEstado;
    @FXML
    private TextField txtBuscar;
    @FXML
    private TableView<IngresoTB> tvList;
    @FXML
    private TableColumn<IngresoTB, ?> tcId;
    @FXML
    private TableColumn<IngresoTB, ?> tcCliente;
    @FXML
    private TableColumn<IngresoTB, ?> tcCorrelativo;
    @FXML
    private TableColumn<IngresoTB, ?> tcFecha;
    @FXML
    private TableColumn<IngresoTB, ?> tcCuenta;
    @FXML
    private TableColumn<IngresoTB, ?> tcObservacion;
    @FXML
    private TableColumn<IngresoTB, ?> tcEstado;
    @FXML
    private TableColumn<IngresoTB, ?> tcImporte;
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
    private void onKeyPressedMostar(KeyEvent event) {
    }

    @FXML
    private void onActionMostrar(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
    }

    @FXML
    private void onActionEstado(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedBuscar(KeyEvent event) {
    }

    @FXML
    private void onKeyRelasedBuscar(KeyEvent event) {
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
