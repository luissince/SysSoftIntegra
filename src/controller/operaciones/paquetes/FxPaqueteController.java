
package controller.operaciones.paquetes;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class FxPaqueteController implements Initializable {

    @FXML
    private Label lblLoad;
    @FXML
    private HBox hbContenedorBotonos;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnClonar;
    @FXML
    private Button btnRecargar;
    @FXML
    private Button btnEtiqueta;
    @FXML
    private Button btnSuprimir;
    @FXML
    private TextField txtClave;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<?> cbCategoria;
    @FXML
    private ComboBox<?> cbMarca;
    @FXML
    private TableView<?> tvList;
    @FXML
    private TableColumn<?, ?> tcNumeracion;
    @FXML
    private TableColumn<?, ?> tcClave;
    @FXML
    private TableColumn<?, ?> tcDescripcion;
    @FXML
    private TableColumn<?, ?> tcCategoria;
    @FXML
    private TableColumn<?, ?> tcMarca;
    @FXML
    private TableColumn<?, ?> tcEstado;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private ImageView ivPrincipal;
    @FXML
    private Text lblName;
    @FXML
    private Text lblPrice;
    @FXML
    private Text lblPriceBruto;
    @FXML
    private Text lblImpuesto;
    @FXML
    private Label lblQuantity;

   
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }    

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) {
    }

    @FXML
    private void onActionEdit(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedClone(KeyEvent event) {
    }

    @FXML
    private void onActionClone(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
    }

    @FXML
    private void onActionReload(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedEtiquetas(KeyEvent event) {
    }

    @FXML
    private void onActionEtiquetas(ActionEvent event) {
    }

    @FXML
    private void onKeyPressedRemove(KeyEvent event) {
    }

    @FXML
    private void onActionRemove(ActionEvent event) {
    }

    @FXML
    private void onActionClave(ActionEvent event) {
    }

    @FXML
    private void onKeyReleasedSearchCode(KeyEvent event) {
    }

    @FXML
    private void onActionNombre(ActionEvent event) {
    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
    }

    @FXML
    private void onActionCategoria(ActionEvent event) {
    }

    @FXML
    private void onActionMarca(ActionEvent event) {
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
    }

    @FXML
    private void onKeyReleasedList(KeyEvent event) {
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
    
}
