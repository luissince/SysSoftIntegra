package controller.contactos.clientes;

import controller.menus.FxPrincipalController;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ClienteADO;
import model.ClienteTB;

public class FxClienteController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
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
    private TableColumn<ClienteTB, String> tcContacto;
    @FXML
    private TableColumn<ClienteTB, String> tcDirección;
    @FXML
    private TableColumn<ClienteTB, String> tcRepresentante;
    @FXML
    private TableColumn<ClienteTB, ImageView> tcPredeterminado;
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
        tcId.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
        tcDocumento.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getTipoDocumentoName() + "\n"
                + cellData.getValue().getNumeroDocumento())
        );
        tcPersona.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getInformacion()));
        tcContacto.setCellValueFactory(cellData -> Bindings.concat(
                (!Tools.isText(cellData.getValue().getTelefono()) ? "TEL: " + cellData.getValue().getTelefono() : "---") + "\n"
                + (!Tools.isText(cellData.getValue().getCelular()) ? "CEL: " + cellData.getValue().getCelular() : "---")
        )
        );
        tcDirección.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDireccion()));
        tcRepresentante.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getRepresentante()));
        tcPredeterminado.setCellValueFactory(new PropertyValueFactory<>("imagePredeterminado"));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcDocumento.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcPersona.prefWidthProperty().bind(tvList.widthProperty().multiply(0.26));
        tcContacto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcDirección.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcRepresentante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcPredeterminado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
        initTableLoad();
    }

    private void initTableLoad() {
        if (!lblLoad.isVisible()) {
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
                return ClienteADO.ListCliente(value, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<ClienteTB> clienteTBs = (ObservableList<ClienteTB>) objects[0];
                if (!clienteTBs.isEmpty()) {
                    tvList.setItems(clienteTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else if (object instanceof String) {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView("Error en traer los datos, intente nuevamente.", "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnScheduled(e -> {
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

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillCustomersTable("");
                break;
            case 1:
                fillCustomersTable(txtSearch.getText().trim());
                break;
        }
    }

    private void openWindowAddCliente() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxClienteProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Cliente", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w->controller.loadAddCliente());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Cliente controller en openWindowAddCliente()" + ex.getLocalizedMessage());
        }
    }

    private void openWindowEditCliente() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_PROCESO); 
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxClienteProcesoController controller = fXMLLoader.getController();
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Editar cliente", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w->controller.loadEditCliente(tvList.getSelectionModel().getSelectedItem().getIdCliente()));
            stage.show();
        } catch (IOException ex) {
            System.out.println("Cliente controller en openWindowEditCliente()" + ex.getLocalizedMessage());
        }
    }

    private void onEventProdeteminado() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            if (!lblLoad.isVisible()) {
                String result = ClienteADO.ChangeDefaultState(true, tvList.getSelectionModel().getSelectedItem().getIdCliente());
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(window, "Cliente", "Se cambió el cliente a predeterminado.");
                    initTableLoad();
                } else {
                    Tools.AlertMessageError(window, "Cliente", "Error: " + result);
                }
            }
        } else {
            Tools.AlertMessageWarning(window, "Cliente", "Seleccione un elemento de la lista.");
        }
    }

    private void openWindowRemoveCliente() {
        short value = Tools.AlertMessageConfirmation(window, "Eliminar cliente", "¿Está seguro de eliminar al cliente?");
        if (value == 1) {
            String result = ClienteADO.RemoveCliente(tvList.getSelectionModel().getSelectedItem().getIdCliente());
            if (result.equalsIgnoreCase("deleted")) {
                Tools.AlertMessageInformation(window, "Eliminar cliente", "Se elimino correctamente el cliente.");
                initTableLoad();
            } else if (result.equalsIgnoreCase("sistema")) {
                Tools.AlertMessageWarning(window, "Eliminar cliente", "No se puede eliminar el cliente porque es propio del sistema.");
            } else if (result.equalsIgnoreCase("venta")) {
                Tools.AlertMessageWarning(window, "Eliminar cliente", "No se puede eliminar al cliente porque tiene asociado ventas.");
            } else {
                Tools.AlertMessageError(window, "Eliminar cliente", result);
            }
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
                    fillCustomersTable(txtSearch.getText().trim());
                    opcion = 1;
                }
            }
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAddCliente();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        openWindowAddCliente();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowEditCliente();
            } else {
                Tools.AlertMessageWarning(window, "Clientes", "Seleccione un cliente para actualizar.");
                tvList.requestFocus();
            }
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowEditCliente();
        } else {
            Tools.AlertMessageWarning(window, "Clientes", "Seleccione un cliente para actualizar.");
            tvList.requestFocus();
        }
    }

    @FXML
    private void onKeyPressedRemove(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowRemoveCliente();
            } else {
                Tools.AlertMessageWarning(window, "Clientes", "Seleccione un cliente para actualizar.");
                tvList.requestFocus();
            }
        }
    }

    @FXML
    private void onActionRemove(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            openWindowRemoveCliente();
        } else {
            Tools.AlertMessageWarning(window, "Clientes", "Seleccione un cliente para actualizar.");
            tvList.requestFocus();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                openWindowEditCliente();
            }
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                initTableLoad();
            }
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            initTableLoad();
        }
    }

    @FXML
    private void onKeyPressedPredeterminado(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProdeteminado();
        }
    }

    @FXML
    private void onActionPredeterminado(ActionEvent event) {
        onEventProdeteminado();
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

    public TableView<ClienteTB> getTvList() {
        return tvList;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
