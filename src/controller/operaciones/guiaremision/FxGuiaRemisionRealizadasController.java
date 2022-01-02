package controller.operaciones.guiaremision;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.GuiaRemisionADO;
import model.GuiaRemisionTB;

public class FxGuiaRemisionRealizadasController implements Initializable {

    @FXML
    private VBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Button btnMostrar;
    @FXML
    private Button btnRecargar;
    @FXML
    private TextField txtSearch;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private TableView<GuiaRemisionTB> tvList;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcNumero;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcVendedor;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcComprobante;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcFecha;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcCliente;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcVendedor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEmpleadoTB().getApellidos() + "\n" + cellData.getValue().getEmpleadoTB().getNombres()));
        tcComprobante.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaTraslado() + "\n" + cellData.getValue().getHoraTraslado()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getInformacion()));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcVendedor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        tcComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.30));//

        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);
    }

    public void loadInit() {
        loadTable((short) 1, "", "", "");
    }

    private void loadTable(short opcion, String buscar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<ObservableList<GuiaRemisionTB>> task = new Task<ObservableList<GuiaRemisionTB>>() {
            @Override
            public ObservableList<GuiaRemisionTB> call() {
                return GuiaRemisionADO.CargarGuiaRemision(opcion, buscar, fechaInicio, fechaFinal);
            }
        };
        task.setOnSucceeded(w -> {
            ObservableList<GuiaRemisionTB> cotizacionTBs = task.getValue();
            if (!cotizacionTBs.isEmpty()) {
                tvList.setItems(cotizacionTBs);
                lblLoad.setVisible(false);
            } else {
                tvList.getItems().clear();
                lblLoad.setVisible(false);
            }
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowDetalleGuiaRemision() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_GUIA_REMISION_DETALLE));
            ScrollPane node = fXMLLoader.load();
            //Controlller here
            FxGuiaRemisionDetalleController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionRealizadasController(this,fxPrincipalController);
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
            controller.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdGuiaRemision());
        } else {
            Tools.AlertMessageWarning(hbWindow, "Cotizaciones", "Debe seleccionar una compra de la lista");
        }
    }

    @FXML
    private void onKeyPressedMostar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleGuiaRemision();
        }
    }

    @FXML
    private void onActionMostrar(ActionEvent event) throws IOException {
        openWindowDetalleGuiaRemision();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                txtSearch.clear();
                Tools.actualDate(Tools.getDate(), dtFechaInicial);
                Tools.actualDate(Tools.getDate(), dtFechaFinal);
                loadTable((short) 1, "", "", "");
            }
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            txtSearch.clear();
            Tools.actualDate(Tools.getDate(), dtFechaInicial);
            Tools.actualDate(Tools.getDate(), dtFechaFinal);
            loadTable((short) 1, "", "", "");
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
    private void onKeyRelasedSearch(KeyEvent event) {
        if (!lblLoad.isVisible()) {
            loadTable((short) 2, txtSearch.getText().trim(), "", "");
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                loadTable((short) 3, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                loadTable((short) 3, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleGuiaRemision();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowDetalleGuiaRemision();
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {

    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {

    }

    public VBox getHbWindow() {
        return hbWindow;
    }

  
    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController=fxPrincipalController;
    }

}
