package controller.inventario.traslados;

import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.TrasladoHistorialTB;
import model.TrasladoTB;
import service.TrasladoADO;

public class FxTrasladoDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Label lblFechaRegistro;
    @FXML
    private Label lblPuntoPartida;
    @FXML
    private Label lblPuntoLlegada;
    @FXML
    private Label lblTipoTraslado;
    @FXML
    private Label lblObservacion;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblTrasladoGuia;
    @FXML
    private TableView<TrasladoHistorialTB> tvList;
    @FXML
    private TableColumn<TrasladoHistorialTB, String> tcNumero;
    @FXML
    private TableColumn<TrasladoHistorialTB, String> tcProducto;
    @FXML
    private TableColumn<TrasladoHistorialTB, String> tcCantidad;
    @FXML
    private TableColumn<TrasladoHistorialTB, String> tcPeso;
    @FXML
    private Label lblEstadoTraslado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);       

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getCantidad(), 2)));
        tcPeso.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getPeso(), 2)));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    public void loadTraslado(String idTraslado) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return TrasladoADO.ObtenerTrasladoById(idTraslado);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof TrasladoTB) {
                TrasladoTB trasladoTB = (TrasladoTB) object;
                lblFechaRegistro.setText(trasladoTB.getFecha() + " " + trasladoTB.getHora());
                lblPuntoPartida.setText(trasladoTB.getPuntoPartida());
                lblPuntoLlegada.setText(trasladoTB.getPuntoLlegada());
                lblTipoTraslado.setText(trasladoTB.getTipo() == 1 ? "INTERNO" : "EXTERNO");
                lblEstadoTraslado.setText(trasladoTB.getEstado() == 1 ? "COMPLETADO" : "ANULADO");
                lblObservacion.setText(trasladoTB.getObservacion());
                lblUsuario.setText(trasladoTB.getEmpleadoTB().getApellidos() + " " + trasladoTB.getEmpleadoTB().getNombres());
                lblTrasladoGuia.setText(trasladoTB.getIdVenta().equalsIgnoreCase("") ? "-" : "SI");

                ObservableList<TrasladoHistorialTB> observableList = FXCollections.observableArrayList(trasladoTB.getHistorialTBs());
                tvList.setItems(observableList);
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
            lblLoad.setVisible(false);
        });
        task.setOnScheduled(w -> {
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            lblLoad.setVisible(true);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }
}
