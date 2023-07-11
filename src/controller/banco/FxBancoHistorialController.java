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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.BancoHistorialTB;
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
    private TableColumn<BancoHistorialTB, String> tcEstado;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcEntrada;
    @FXML
    private TableColumn<BancoHistorialTB, String> tcSalida;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxBancoController bancosController;

    private FxPrincipalController fxPrincipalController;

    private String idBanco;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> {
            String fecha = cellData.getValue().getFecha();
            String hora = cellData.getValue().getHora();
            return Bindings.concat(fecha + "\n" + hora);
        });
        tcUsuario.setCellValueFactory(cellData -> {
            String informacion = cellData.getValue().getEmpleadoTB().getNombres() + ", "
                    + cellData.getValue().getEmpleadoTB().getApellidos();
            String rol = cellData.getValue().getEmpleadoTB().getRolTB().getNombre();
            return Bindings.concat(informacion + "\n" + rol);
        });
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getDescripcion()));
        tcEstado.setCellValueFactory(cellData -> {
            boolean estado = cellData.getValue().isEstado();
            String formato = !estado ? "ANULADO" : "ACTIVO";
            return Bindings.concat(formato);
        });
        tcEntrada.setCellValueFactory(cellData -> {
            double entrada = cellData.getValue().getEntrada();
            String formato = entrada > 0 ? "+" + Tools.roundingValue(entrada, 2) : "";
            return Bindings.concat(formato);
        });
        tcSalida.setCellValueFactory(cellData -> {
            double salida = cellData.getValue().getSalida();
            String formato = salida > 0 ? "-" + Tools.roundingValue(salida, 2) : "";
            return Bindings.concat(formato);
        });

        tcEstado.setCellFactory(
                (TableColumn<BancoHistorialTB, String> param) -> new TableCell<BancoHistorialTB, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            BancoHistorialTB rowData = getTableView().getItems().get(getIndex());
                            if (rowData.isEstado()) {
                                setStyle("-fx-alignment:  CENTER; -fx-text-fill: #309c40;");
                            } else {
                                setStyle("-fx-alignment:  CENTER; -fx-text-fill: #990000;");
                            }
                            setText(item);
                        }
                    }
                });

        tcEntrada.setCellFactory(
                (TableColumn<BancoHistorialTB, String> param) -> new TableCell<BancoHistorialTB, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            BancoHistorialTB rowData = getTableView().getItems().get(getIndex());
                            if (rowData.isEstado()) {
                                setStyle("-fx-alignment:  CENTER-RIGHT; -fx-text-fill: #309c40;");
                            } else {
                                setStyle("-fx-alignment:  CENTER-RIGHT; -fx-text-fill: #990000;");
                            }
                            setText(item);
                        }
                    }
                });

        tcSalida.setCellFactory(
                (TableColumn<BancoHistorialTB, String> param) -> new TableCell<BancoHistorialTB, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            BancoHistorialTB rowData = getTableView().getItems().get(getIndex());
                            if (rowData.isEstado()) {
                                setStyle("-fx-alignment:  CENTER-RIGHT; -fx-text-fill: #309c40;");
                            } else {
                                setStyle("-fx-alignment:  CENTER-RIGHT; -fx-text-fill: #990000;");
                            }
                            setText(item);
                        }
                    }
                });

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcUsuario.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcDescripcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.21));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcEntrada.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcSalida.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tvList.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        paginacion = 1;
        opcion = 0;
    }

    public void loadBanco(String idBanco) {
        this.idBanco = idBanco;
        paginacion = 1;
        loadTableViewBancoDetalle(idBanco);
        opcion = 0;
    }

    private void loadTableViewBancoDetalle(String idBanco) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            public Object[] call() throws Exception {
                Object result = BancoADO.obtenerBancoHistorial(idBanco, (paginacion - 1) * 20, 20);
                if (result instanceof Object[]) {
                    return (Object[]) result;
                }
                throw new Exception((String) result);
            }
        };

        task.setOnScheduled((WorkerStateEvent event) -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(
                    Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });

        task.setOnFailed((WorkerStateEvent event) -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(),
                    "-fx-text-fill:#a70820;", false));
        });

        task.setOnSucceeded((WorkerStateEvent e) -> {
            Object[] objects = task.getValue();
            ObservableList<BancoHistorialTB> bancoHistorialTBs = (ObservableList<BancoHistorialTB>) objects[0];
            if (!bancoHistorialTBs.isEmpty()) {
                tvList.setItems(bancoHistorialTBs);
                totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                lblPaginaActual.setText(paginacion + "");
                lblPaginaSiguiente.setText(totalPaginacion + "");
                lblSaldoTotal.setText((String) objects[2]);
            } else {
                tvList.setPlaceholder(
                        Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                lblPaginaActual.setText("0");
                lblPaginaSiguiente.setText("0");
                lblSaldoTotal.setText((String) objects[2]);
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                loadTableViewBancoDetalle(idBanco);
                break;
        }
    }

    private void onEventCloseWindow() {
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
            // Controlller here
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
            // Controlller here
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

    private void onEventReload() {
        if (lblLoad.isVisible()) {
            return;
        }
        paginacion = 1;
        loadTableViewBancoDetalle(idBanco);
        opcion = 0;
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
            onEventReload();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        onEventReload();
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        onEventCloseWindow();
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

    public void setInitComptrasController(FxBancoController bancosController,
            FxPrincipalController fxPrincipalController) {
        this.bancosController = bancosController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
