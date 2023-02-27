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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.GuiaRemisionTB;
import service.GuiaRemisionADO;

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
    private TableColumn<GuiaRemisionTB, String> tcFecha;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcComprobante;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcReferencia;
    @FXML
    private TableColumn<GuiaRemisionTB, String> tcCliente;
    @FXML
    private TableColumn<GuiaRemisionTB, Label> tcEstado;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings
                .concat(cellData.getValue().getFechaRegistro() + "\n" + cellData.getValue().getHoraRegistro()));
        tcComprobante
                .setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoDocumentoTB().getNombre()
                        + "\n" + cellData.getValue().getSerie() + "-"
                        + Tools.formatNumber(cellData.getValue().getNumeracion())));
        tcReferencia.setCellValueFactory(
                cellData -> Bindings.concat(cellData.getValue().getVentaTB().getTipoDocumentoTB().getNombre() + "\n"
                        + cellData.getValue().getVentaTB().getSerie() + "-"
                        + Tools.formatNumber(cellData.getValue().getVentaTB().getNumeracion())));
        tcCliente
                .setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento()
                        + "\n" + cellData.getValue().getClienteTB().getInformacion()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcReferencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.26));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);
    }

    public void loadInit() {
        paginacion = 1;
        fillGuiaRemisionTable(0, "", "", "");
        opcion = 0;
    }

    private void fillGuiaRemisionTable(int opcion, String buscar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return GuiaRemisionADO.CargarGuiaRemision(opcion, buscar, fechaInicio, fechaFinal,
                        (paginacion - 1) * 20, 20);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<GuiaRemisionTB> ventaTBs = (ObservableList<GuiaRemisionTB>) objects[0];
                if (!ventaTBs.isEmpty()) {
                    tvList.setItems(ventaTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(
                            Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(),
                    "-fx-text-fill:#a70820;", false));
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(
                    Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
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
            // Controlller here
            FxGuiaRemisionDetalleController controller = fXMLLoader.getController();
            controller.setInitGuiaRemisionRealizadasController(this, fxPrincipalController);
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

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillGuiaRemisionTable(0, "", "", "");
                break;
            case 1:
                fillGuiaRemisionTable(1, txtSearch.getText().trim(), "", "");
                break;
            case 2:
                fillGuiaRemisionTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                break;
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
                loadInit();
            }
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            loadInit();
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
            if (!Tools.isText(txtSearch.getText())) {
                if (!lblLoad.isVisible()) {
                    paginacion = 1;
                    fillGuiaRemisionTable(1, txtSearch.getText().trim(), "", "");
                    opcion = 1;
                }
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillGuiaRemisionTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                opcion = 2;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillGuiaRemisionTable(2, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                opcion = 2;
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
            if (!lblLoad.isVisible()) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
        }
    }

    public VBox getHbWindow() {
        return hbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
