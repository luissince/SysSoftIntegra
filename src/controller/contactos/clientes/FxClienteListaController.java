package controller.contactos.clientes;

import controller.reporte.FxNotaCreditoReporteController;
import controller.reporte.FxPosReporteController;
import controller.reporte.FxVentaReporteController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.ClienteADO;
import model.ClienteTB;

public class FxClienteListaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<ClienteTB> tvList;
    @FXML
    private TableColumn<ClienteTB, Integer> tcId;
    @FXML
    private TableColumn<ClienteTB, String> tcDocumento;
    @FXML
    private TableColumn<ClienteTB, String> tcPersona;
    @FXML
    private TableColumn<ClienteTB, String> tcDireccion;

    private FxVentaReporteController ventaReporteController;

    private FxPosReporteController posReporteController;

    private FxNotaCreditoReporteController notaCreditoReporteController;

    private boolean status;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        apWindow.setOnKeyReleased(event -> {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case F1:
                        txtSearch.requestFocus();
                        txtSearch.selectAll();
                        break;
                    case F2:
                        openWindowAddCliente();
                        break;
                    case F3:
                        if (!status) {
                            fillCustomersTable("");
                        }
                        break;

                }
            }
        });
        tcId.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
        tcDocumento.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoDocumentoName() + ": " + cellData.getValue().getNumeroDocumento()));
        tcPersona.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getInformacion()));
        tcDireccion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDireccion()));
    }

    public void fillCustomersTable(String value) {

        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ObservableList<ClienteTB>> task = new Task<ObservableList<ClienteTB>>() {
            @Override
            public ObservableList<ClienteTB> call() {
                return ClienteADO.ListClienteVenta(value);
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            tvList.setItems(task.getValue());
            status = false;
        });
        task.setOnFailed((e) -> {
            status = false;
        });
        task.setOnScheduled((e) -> {
            status = true;
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void openWindowAddCliente() {
        try {
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxClienteProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Cliente", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
            controller.setValueAdd();
        } catch (IOException ex) {
            System.out.println("openWindowAddCliente()" + ex.getLocalizedMessage());
        }
    }

    private void selectClienteList() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (ventaReporteController != null) {
                ventaReporteController.setClienteVentaReporte(tvList.getSelectionModel().getSelectedItem().getIdCliente(),
                        tvList.getSelectionModel().getSelectedItem().getInformacion());
                Tools.Dispose(apWindow);
            } else if (notaCreditoReporteController != null) {
                notaCreditoReporteController.setClienteVentaReporte(tvList.getSelectionModel().getSelectedItem().getIdCliente(),
                        tvList.getSelectionModel().getSelectedItem().getInformacion());
                Tools.Dispose(apWindow);
            } else if (posReporteController != null) {
                posReporteController.setClienteVentaReporte(tvList.getSelectionModel().getSelectedItem().getIdCliente(),
                        tvList.getSelectionModel().getSelectedItem().getInformacion());
                Tools.Dispose(apWindow);
            }
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        if (!status) {
            fillCustomersTable("");
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!status) {
                fillCustomersTable("");
            }
        }
    }

    @FXML
    private void onKeyPressedToSearh(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!tvList.getItems().isEmpty()) {
                tvList.requestFocus();
                tvList.getSelectionModel().select(0);
            }
        }
    }

    @FXML
    private void onKeyReleasedToSearh(KeyEvent event) {
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
            if (!status) {
                fillCustomersTable(txtSearch.getText());
            }
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            selectClienteList();
        }

    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            selectClienteList();
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAddCliente();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            selectClienteList();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        selectClienteList();
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowAddCliente();
    }

    public void setInitVentaReporteController(FxVentaReporteController ventaReporteController) {
        this.ventaReporteController = ventaReporteController;
    }

    public void setInitPostReporteController(FxPosReporteController posReporteController) {
        this.posReporteController = posReporteController;
    }

    public void setInitNotaCreditoReporteController(FxNotaCreditoReporteController notaCreditoReporteController) {
        this.notaCreditoReporteController = notaCreditoReporteController;
    }

}
