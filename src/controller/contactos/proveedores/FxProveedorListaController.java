package controller.contactos.proveedores;

import controller.operaciones.compras.FxComprasController;
import controller.reporte.FxCompraReporteController;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.ProveedorADO;
import model.ProveedorTB;

public class FxProveedorListaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<ProveedorTB> tvList;
    @FXML
    private TableColumn<ProveedorTB, String> tcId;
    @FXML
    private TableColumn<ProveedorTB, String> tcDocument;
    @FXML
    private TableColumn<ProveedorTB, String> tcRepresentative;
    @FXML
    private TableColumn<ProveedorTB, String> tcMovil;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxCompraReporteController compraReporteController;

    private boolean status;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

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
                        openWindowAddProveedor();
                        break;
                    case F3:
                        if (!status) {
                            fillCustomersTable("");
                        }
                        break;

                }
            }
        });

        tcId.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcDocument.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getTipoDocumentoName() + "\n" + cellData.getValue().getNumeroDocumento())
        );
        tcRepresentative.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getRazonSocial()));
        tcMovil.setCellValueFactory(cellData -> Bindings.concat(
                (cellData.getValue().getTelefono().equals("") ? "Sin N° de Teléfono" : cellData.getValue().getTelefono()).toUpperCase()
                + "\n"
                + (cellData.getValue().getCelular().equals("") ? "Sin N° de Celular" : cellData.getValue().getCelular()).toUpperCase()
        ));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        paginacion = 1;
        opcion = 0;
    }

    public void initTable() {
        if (!status) {
            paginacion = 1;
            fillCustomersTable("");
            opcion = 0;
        }
    }

    private void fillCustomersTable(String value) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return ProveedorADO.ListProveedor(value, (paginacion - 1) * 20, 20);
            }
        };
        task.setOnScheduled(e -> {
            status = true;
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        task.setOnFailed(e -> {
            status = false;
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnSucceeded(e -> {
            Object result = task.getValue();
            if (result instanceof Object[]) {
                Object[] objects = (Object[]) result;
                ObservableList<ProveedorTB> empList = (ObservableList<ProveedorTB>) objects[0];
                if (!empList.isEmpty()) {
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                    tvList.setItems(empList);
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) result, "-fx-text-fill:#a70820;", false));
            }
            status = false;
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void openWindowAddProveedor() {
        try {
            URL url = getClass().getResource(FilesRouters.FX_PROVEEDORES_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxProveedorProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Proveedor", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
            controller.setValueAdd();
        } catch (IOException ex) {
            System.out.println("openWindowAddProveedor():" + ex.getLocalizedMessage());
        }
    }

    private void executeEvent() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
             if (compraReporteController != null) {
                compraReporteController.setInitCompraReporteValue(tvList.getSelectionModel().getSelectedItem().getIdProveedor(),
                        tvList.getSelectionModel().getSelectedItem().getRazonSocial());
                Tools.Dispose(apWindow);
            }
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillCustomersTable("");
                break;
            case 1:
                fillCustomersTable(txtSearch.getText());
                break;
        }
    }

    @FXML
    private void onKeyReleasedToSearch(KeyEvent event) {
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
                paginacion = 1;
                fillCustomersTable(txtSearch.getText().trim());
                opcion = 0;
            }
        }
    }

    @FXML
    private void onKeyPressedToSearh(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            tvList.requestFocus();
        }
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAddProveedor();
        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) throws IOException {
        openWindowAddProveedor();
    }

    @FXML
    private void onKeyPressedToReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initTable();
        }
    }

    @FXML
    private void onActionToReload(ActionEvent event) {
        initTable();
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeEvent();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            executeEvent();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeEvent();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        executeEvent();
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!status) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!status) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!status) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!status) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
        }
    }

    public void setInitComprasReporteController(FxCompraReporteController compraReporteController) {
        this.compraReporteController = compraReporteController;
    }

}
