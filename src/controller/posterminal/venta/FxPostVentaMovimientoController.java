package controller.posterminal.venta;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.MovimientoCajaTB;
import service.VentaADO;

public class FxPostVentaMovimientoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<String> cbMovimiento;
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtComentario;
    @FXML
    private Button btnEjecutar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        cbMovimiento.getItems().addAll("Entrada", "Salida");
    }

    private void saveMovimiento() {
        if (cbMovimiento.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Ventas", "Seleccione el movimiento a realizar.");
            cbMovimiento.requestFocus();
        } else if (!Tools.isNumeric(txtMonto.getText().trim())) {
            Tools.AlertMessageWarning(window, "Ventas", "El monto ingreso no tiene el formato correcto.");
            txtMonto.requestFocus();
        } else if (txtComentario.getText().isEmpty()) {
            Tools.AlertMessageWarning(window, "Ventas", "Ingrese un comentario.");
            txtComentario.requestFocus();
        } else {            
            short opcion = Tools.AlertMessageConfirmation(window, "Ventas", "¿Está seguro de continuar?");
            if (opcion == 1) {
                ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                    Thread t = new Thread(runnable);
                    t.setDaemon(true);
                    return t;
                });

                Task<String> task = new Task<String>() {
                    @Override
                    public String call() {
                        MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                        movimientoCajaTB.setFechaMovimiento(Tools.getDate());
                        movimientoCajaTB.setHoraMovimiento(Tools.getTime());
                        movimientoCajaTB.setComentario(txtComentario.getText().toUpperCase().trim());
                        movimientoCajaTB.setTipoMovimiento(cbMovimiento.getSelectionModel().getSelectedIndex() == 0 ? (short) 4 : (short) 5);
                        movimientoCajaTB.setMonto(Double.parseDouble(txtMonto.getText()));
                        return VentaADO.movimientoCaja(movimientoCajaTB);
                    }
                };

                task.setOnSucceeded(e -> {
                    String result = task.getValue();
                    if (result.equalsIgnoreCase("updated")) {
                        Tools.AlertMessageInformation(window, "Ventas", "Se proceso correctamente su movimiento de caja.");
                        Tools.Dispose(window);
                    } else {
                        Tools.AlertMessageError(window, "Ventas", result);
                    }
                    btnEjecutar.setDisable(false);
                });

                task.setOnFailed(e -> {
                    btnEjecutar.setDisable(false);
                });

                task.setOnScheduled(e -> {
                    btnEjecutar.setDisable(true);
                });

                exec.execute(task);
                if (!exec.isShutdown()) {
                    exec.shutdown();
                }
            }
        }
    }

//    private void onKeyPressedComentario(KeyEvent event) {
//        if (event.getCode() == KeyCode.TAB) {
//            Node node = (Node) event.getSource();
//            if (node instanceof TextArea) {
//                TextAreaSkin skin = (TextAreaSkin) ((TextArea) node).getSkin();
//                if (!event.isControlDown()) {
//                    if (event.isShiftDown()) {
//                        skin.getBehavior().traversePrevious();
//                    } else {
//                        skin.getBehavior().traverseNext();
//                    }
//                } else {
//                    TextArea textA = (TextArea) node;
//                    textA.replaceSelection("\t");
//                }
//                event.consume();
//            }
//        }
//    }
    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            saveMovimiento();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        saveMovimiento();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onKeyTypedMonto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtMonto.getText().contains(".") || c == '-' && txtMonto.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

}
