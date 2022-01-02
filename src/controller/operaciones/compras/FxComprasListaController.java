package controller.operaciones.compras;

import controller.inventario.movimientos.FxMovimientosProcesoController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.CompraADO;
import model.CompraTB;

public class FxComprasListaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private DatePicker dtFechaCompra;
    @FXML
    private TableView<CompraTB> tvList;
    @FXML
    private TableColumn<CompraTB, String> tcNumero;
    @FXML
    private TableColumn<CompraTB, String> tcFecha;
    @FXML
    private TableColumn<CompraTB, String> tcSerie;
    @FXML
    private TableColumn<CompraTB, String> tcProveedor;
    @FXML
    private TableColumn<CompraTB, String> tcTotal;

    private FxMovimientosProcesoController movimientosProcesoController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaCompra()+"\n"+cellData.getValue().getHoraCompra()));
        tcSerie.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeracion()));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getIdProveedor()));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaNombre()+" "+Tools.roundingValue(cellData.getValue().getTotal(), 2)));
    }

    public void loadListCompras(String search, String fecha, short option) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        try {
            Task<ObservableList<CompraTB>> task = new Task<ObservableList<CompraTB>>() {
                @Override
                public ObservableList<CompraTB> call() {
                    return CompraADO.List_Compras_Realizadas(search, fecha, option);
                }
            };

            task.setOnScheduled(t -> {
                lblLoad.setVisible(true);
            });
            task.setOnFailed(t -> {
                lblLoad.setVisible(false);
            });
            task.setOnSucceeded(t -> {
                tvList.setItems(task.getValue());
                lblLoad.setVisible(false);
            });

            exec.execute(task);

        } catch (Exception ex) {

        } finally {
            exec.shutdown();
        }
    }

    private void executeList() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            //movimientosProcesoController.loadComprasRealizadas(tvList.getSelectionModel().getSelectedItem().getIdCompra());
            Tools.Dispose(apWindow);
        } else {
            tvList.requestFocus();
        }
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void onKeyReleasedSearch(KeyEvent event) {
        if (!lblLoad.isVisible()) {
            loadListCompras(txtSearch.getText().trim(), "", (short) 0);
        }

    }

    @FXML
    private void onActionFechaCompra(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            loadListCompras("", Tools.getDatePicker(dtFechaCompra), (short) 1);
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeList();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            executeList();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeList();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        executeList();
    }

    @FXML
    private void onKeyPressedCerrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCerrar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitMovimientoProcesoController(FxMovimientosProcesoController movimientosProcesoController) {
        this.movimientosProcesoController = movimientosProcesoController;
    }

}
