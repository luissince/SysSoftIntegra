package controller.operaciones.cotizacion;

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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.CotizacionADO;
import model.CotizacionTB;

public class FxCotizacionRealizadasController implements Initializable {

    @FXML
    private VBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtSearch;
    @FXML
    private DatePicker dtFechaInicial;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private TableView<CotizacionTB> tvList;
    @FXML
    private TableColumn<CotizacionTB, String> tcNumero;
    @FXML
    private TableColumn<CotizacionTB, String> tcFecha;
    @FXML
    private TableColumn<CotizacionTB, String> tcCliente;
    @FXML
    private TableColumn<CotizacionTB, String> tcCotizacion;
    @FXML
    private TableColumn<CotizacionTB, String> tcObservacion;
    @FXML
    private TableColumn<CotizacionTB, Label> tcEstado;
    @FXML
    private TableColumn<CotizacionTB, String> tcTotal;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController principalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaCotizacion() + "\n" + cellData.getValue().getHoraCotizacion()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento() + "\n" + cellData.getValue().getClienteTB().getInformacion()));
        tcCotizacion.setCellValueFactory(cellData -> Bindings.concat("COTIZACIÓN\n N° - " + Tools.formatNumber(cellData.getValue().getIdCotizacion())));
        tcObservacion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getObservaciones()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("lblEstado"));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getTotal(), 2)));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcCliente.prefWidthProperty().bind(tvList.widthProperty().multiply(0.17));
        tcCotizacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcObservacion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.11));
        tcTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), dtFechaInicial);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);

        loadInit();
    }

    public void loadInit() {
        if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                opcion = 0;
            }
        }
    }

    private void fillTableCotizacion(int opcion, String buscar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CotizacionADO.Listar_Cotizacion(opcion, buscar, fechaInicio, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<CotizacionTB> cotizacionTBs = (ObservableList<CotizacionTB>) objects[0];
                if (!cotizacionTBs.isEmpty()) {
                    tvList.setItems(cotizacionTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 10.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
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
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowDetalleCotizacion() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_COTIZACION_DETALLE));
            AnchorPane node = fXMLLoader.load();
            //Controlller here
            FxCotizacionDetalleController controller = fXMLLoader.getController();
            controller.setInitCotizacionesRealizadasController(this, principalController);
            //
            principalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            principalController.getVbContent().getChildren().add(node);
            controller.setInitComponents(tvList.getSelectionModel().getSelectedItem().getIdCotizacion());
        } else {
            Tools.AlertMessageWarning(hbWindow, "Cotizaciones", "Debe seleccionar una compra de la lista");
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTableCotizacion(0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                break;
            case 1:
                fillTableCotizacion(1, txtSearch.getText().trim(), "", "");
                break;
        }
    }

    private void removeCotizacion() {
        short value = Tools.AlertMessageConfirmation(hbWindow, "Cotización", "¿Está seguro de eliminar la cotización.");
        if (value == 1) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                String result = CotizacionADO.removeCotizacionById(tvList.getSelectionModel().getSelectedItem().getIdCotizacion());
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(hbWindow, "Cotización", "Se eliminó correctamente la cotización.");
                    loadInit();
                } else if (result.equalsIgnoreCase("noid")) {
                    Tools.AlertMessageWarning(hbWindow, "Cotización", "No puedo encontrar la cotización a eliminar, intente nuevamente.");
                } else {
                    Tools.AlertMessageError(hbWindow, "Cotización", result);
                }
            }
        }
    }

    @FXML
    private void onKeyPressedMostar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowDetalleCotizacion();
        }
    }

    @FXML
    private void onActionMostrar(ActionEvent event) throws IOException {
        openWindowDetalleCotizacion();
    }

    @FXML
    private void onKeyPressedEliminar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            removeCotizacion();
        }
    }

    @FXML
    private void onActionEliminar(ActionEvent event) {
        removeCotizacion();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInit();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        loadInit();
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
            if (!txtSearch.getText().isEmpty()) {
                paginacion = 1;
                fillTableCotizacion(1, txtSearch.getText().trim(), "", "");
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaInicial(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                opcion = 0;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dtFechaInicial.getValue() != null && dtFechaFinal.getValue() != null) {
                paginacion = 1;
                fillTableCotizacion(0, "", Tools.getDatePicker(dtFechaInicial), Tools.getDatePicker(dtFechaFinal));
                opcion = 0;
            }
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                openWindowDetalleCotizacion();
            }
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            if (!tvList.getItems().isEmpty()) {
                openWindowDetalleCotizacion();
            }
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

    public void setContent(FxPrincipalController principalController) {
        this.principalController = principalController;
    }

}
