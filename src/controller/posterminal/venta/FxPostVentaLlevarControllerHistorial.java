package controller.posterminal.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.HistorialSuministroSalidaADO;
import model.HistorialSuministroSalidaTB;
import model.SuministroTB;
import model.VentaTB;

public class FxPostVentaLlevarControllerHistorial implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtProducto;
    @FXML
    private TableView<HistorialSuministroSalidaTB> tvList;
    @FXML
    private TableColumn<HistorialSuministroSalidaTB, String> tvNumero;
    @FXML
    private TableColumn<HistorialSuministroSalidaTB, String> tvFecha;
    @FXML
    private TableColumn<HistorialSuministroSalidaTB, String> tvCantidad;
    @FXML
    private TableColumn<HistorialSuministroSalidaTB, String> tvObservacion;

    private FxPostVentaDetalleController ventaDetalleController;

    private FxOpcionesImprimirController imprimirController;

    private VentaTB ventaTB;

    private SuministroTB suministroTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        tvNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tvFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        tvCantidad.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCantidad()));
        tvObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservacion()));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        imprimirController = new FxOpcionesImprimirController();
        imprimirController.loadComponents();
        imprimirController.loadTicketVentaLlevar(apWindow);
    }

    public void loadData(VentaTB ventaTB, SuministroTB suministroTB) {
        this.ventaTB = ventaTB;
        this.suministroTB = suministroTB;
        txtProducto.setText(suministroTB.getNombreMarca());
        fillTableHistorialMovimiento();
    }

    private void fillTableHistorialMovimiento() {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return HistorialSuministroSalidaADO.Listar_Historial_Suministro_Salida(ventaTB.getIdVenta(), suministroTB.getIdSuministro());
            }
        };

        task.setOnScheduled(w -> {
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
        });

        task.setOnFailed(w -> {
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof ObservableList) {
                ObservableList<HistorialSuministroSalidaTB> ventaTBs = (ObservableList<HistorialSuministroSalidaTB>) object;
                if (!ventaTBs.isEmpty()) {
                    tvList.setItems(ventaTBs);
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                }
            } else if (object instanceof String) {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView("Error en traer los datos, intente nuevamente.", "-fx-text-fill:#a70820;", false));
            }
        });

        executor.execute(task);
        if (executor.isShutdown()) {
            executor.shutdown();
        }

    }

    private void printTicket() {
        imprimirController.getTicketVentaLlevar().imprimir(ventaTB.getIdVenta(), suministroTB.getIdSuministro());
    }

    private void openWindowReporte() {
        imprimirController.getTicketVentaLlevar().mostrarReporte(ventaTB.getIdVenta(), suministroTB.getIdSuministro());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        openWindowReporte();
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            printTicket();
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        printTicket();
    }

    @FXML
    private void onKeyPressedClose(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionClose(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitVentaDetalleController(FxPostVentaDetalleController ventaDetalleController) {
        this.ventaDetalleController = ventaDetalleController;
    }

}
