package controller.banco;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import model.BancoHistorialTB;
import model.BancoTB;
import service.BancoADO;

public class FxBancoHistorialController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Label lblSaldoTotal;
    @FXML
    private TableView<BancoHistorialTB> tvList;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcNumero;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcUsuario;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcFecha;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcDescripcion;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcEntrada;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcSalida;

    private FxBancosController bancosController;

    private  FxPrincipalController fxPrincipalController;

    private String idBanco;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFecha() + "\n" + cellData.getValue().getHora()));
        tcUsuario.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEmpleadoTB().getInformacion()));
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDescripcion()));
        tcEntrada.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEntrada() == 0 ? "" : Tools.roundingValue(cellData.getValue().getEntrada(), 2)));
        tcSalida.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSalida() == 0 ? "" : Tools.roundingValue(cellData.getValue().getSalida(), 2)));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        tcUsuario.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcDescripcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.25));
        tcEntrada.prefWidthProperty().bind(tvList.widthProperty().multiply(0.17));
        tcSalida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.17));
    }

    public void loadBanco(String idBanco) {
        this.idBanco = idBanco;
        loadTableViewBanco(idBanco);
    }

    private void loadTableViewBanco(String idBanco) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            public ArrayList<Object> call() {
                return BancoADO.Listar_Bancos_Historial(idBanco);
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            ArrayList<Object> arrayList = task.getValue();
            if (!arrayList.isEmpty()) {
                BancoTB bancoTB = (BancoTB) arrayList.get(0);
                if (bancoTB != null) {
                    lblSaldoTotal.setText(bancoTB.getSimboloMoneda() + " " + Tools.roundingValue(bancoTB.getSaldoInicial(), 2));
                }
                tvList.setItems((ObservableList<BancoHistorialTB>) arrayList.get(1));
                lblLoad.setVisible(false);
            }
        });
        task.setOnFailed((WorkerStateEvent event) -> {
            lblLoad.setVisible(false);
        });

        task.setOnScheduled((WorkerStateEvent event) -> {
            lblLoad.setVisible(true);
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void getOutWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(hbWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(bancosController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(bancosController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(bancosController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(bancosController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(bancosController.getHbWindow());
    }

    private void eventAdd() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_BANCO_AGREGAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxBancoAgregarDineroController controller = fXMLLoader.getController();
            controller.setInitBancosController(this);
            controller.setIdBanco(idBanco);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Ingreso de dinero", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void eventRetirar() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_BANCO_RETIRAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxBancoRetirarDineroController controller = fXMLLoader.getController();
            controller.setInitBancosController(this);
            controller.setIdBanco(idBanco);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Salida de dinero", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void eventTransferencia() {

    }

    private void eventReload() {

    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventAdd();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        eventAdd();
    }

    @FXML
    private void onKeyPressedRetirar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventRetirar();
        }
    }

    @FXML
    private void onActionRetirar(ActionEvent event) {
        eventRetirar();
    }

    @FXML
    private void onKeyPressedTransferir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventTransferencia();
        }
    }

    @FXML
    private void onActionTransferir(ActionEvent event) {
        eventTransferencia();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventReload();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        eventReload();
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        getOutWindow();
    }

    public void setInitComptrasController(FxBancosController bancosController, FxPrincipalController fxPrincipalController) {
        this.bancosController = bancosController;
        this.fxPrincipalController = fxPrincipalController;
    }


}
