package controller.banco;

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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.BancoTB;
import service.BancoADO;

public class FxBancosController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<BancoTB> tvList;
    @FXML
    private TableColumn<BancoTB, String> tcNumero;
    @FXML
    private TableColumn<BancoTB, String> tcNombreCuenta;
    @FXML
    private TableColumn<BancoTB, String> tcNumeroCuenta;
    @FXML
    private TableColumn<BancoTB, String> tcDescripcion;
    @FXML
    private TableColumn<BancoTB, String> tcMostrar;
    @FXML
    private TableColumn<BancoTB, String> tcSaldo;
    @FXML
    private TableColumn<BancoTB, String> tcForma;
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
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcNombreCuenta.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombreCuenta().toUpperCase()));
        tcNumeroCuenta.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeroCuenta()));
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDescripcion().toUpperCase()));
        tcSaldo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSimboloMoneda() + " " + Tools.roundingValue(cellData.getValue().getSaldoInicial(), 2)));
        tcForma.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFormaPago() == (short) 1 ? "Efectivo".toUpperCase() : "Banco".toUpperCase()));
        tcMostrar.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().isMostrar() ? "EN REPORTES	" : "NO"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcNombreCuenta.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcNumeroCuenta.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcDescripcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcForma.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcMostrar.prefWidthProperty().bind(tvList.widthProperty().multiply(0.11));
        tcSaldo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        paginacion = 1;
        opcion = 0;
    }

    public void loadTableViewBanco(String buscar) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return BancoADO.Listar_Bancos(buscar, (paginacion - 1) * 20, 20);
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<BancoTB> bancoTBs = (ObservableList<BancoTB>) objects[0];
                if (!bancoTBs.isEmpty()) {
                    tvList.setItems(bancoTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
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
        
        task.setOnFailed((WorkerStateEvent event) -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnScheduled((WorkerStateEvent event) -> {
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

    private void eventViewBanco() {
        try {
            FXMLLoader fXMLPrincipal = new FXMLLoader(getClass().getResource(FilesRouters.FX_BANCO_HISTORIAL));
            HBox node = fXMLPrincipal.load();

            FxBancoHistorialController controller = fXMLPrincipal.getController();
            controller.setInitComptrasController(this, fxPrincipalController);
            controller.loadBanco(tvList.getSelectionModel().getSelectedItem().getIdBanco());

            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);

        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void eventNewBanco() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_BANCO_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxBancoProcesoController controller = fXMLLoader.getController();
            controller.setInitBancosController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Registre una nueva cuenta", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void eventEditBanco() {
        try {
            if (tvList.getSelectionModel().getSelectedIndex() < 0) {
                tvList.requestFocus();
                if (!tvList.getItems().isEmpty()) {
                    tvList.getSelectionModel().select(0);
                }
            } else {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_BANCO_PROCESO);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                //Controlller here
                FxBancoProcesoController controller = fXMLLoader.getController();
                controller.setInitBancosController(this);
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Editar cuenta", hbWindow.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                stage.show();
                controller.loadEditBanco(tvList.getSelectionModel().getSelectedItem().getIdBanco());
            }
        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void eventRemoveBanco() {
        if (tvList.getSelectionModel().getSelectedIndex() < 0) {
            tvList.requestFocus();
            if (!tvList.getItems().isEmpty()) {
                tvList.getSelectionModel().select(0);
            }
        } else {
            short option = Tools.AlertMessageConfirmation(hbWindow, "Banco", "¿Está seguro de continuar?");
            if (option == 1) {
                String result = BancoADO.Deleted_Banco(tvList.getSelectionModel().getSelectedItem().getIdBanco());
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageInformation(hbWindow, "Banco", "Se eliminó la cuenta correctamente.");
                    loadTableViewBanco("");
                } else if (result.equalsIgnoreCase("sistema")) {
                    Tools.AlertMessageWarning(hbWindow, "Banco", "La cuenta no se puede eliminar es propio del sistema.");
                } else if (result.equalsIgnoreCase("historial")) {
                    Tools.AlertMessageWarning(hbWindow, "Banco", "La cuenta no se puede eliminar porque tiene asociado un historial.");
                } else {
                    Tools.AlertMessageError(hbWindow, "Banco", result);
                }
            }
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                loadTableViewBanco("");
                break;
        }
    }

    private void onEventReloadBanco() {
        if (!lblLoad.isVisible()) {
            loadTableViewBanco("");
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                eventViewBanco();
            }
        }
    }

    @FXML
    private void onKeyPressedView(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() < 0) {
                tvList.requestFocus();
                if (!tvList.getItems().isEmpty()) {
                    tvList.getSelectionModel().select(0);
                }
            } else {
                eventViewBanco();
            }
        }
    }

    @FXML
    private void onActionView(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() < 0) {
            tvList.requestFocus();
            if (!tvList.getItems().isEmpty()) {
                tvList.getSelectionModel().select(0);
            }
        } else {
            eventViewBanco();
        }
    }

    @FXML
    private void onKeyPressedNew(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventNewBanco();
        }
    }

    @FXML
    private void onActionNew(ActionEvent event) {
        eventNewBanco();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventEditBanco();
        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) {
        eventEditBanco();
    }

    @FXML
    private void onActionRemove(ActionEvent event) {
        eventRemoveBanco();
    }

    @FXML
    private void onKeyPressedRemove(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventRemoveBanco();
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReloadBanco();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        onEventReloadBanco();
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

    public HBox getHbWindow() {
        return hbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }
}
