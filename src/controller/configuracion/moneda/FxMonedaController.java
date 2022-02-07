package controller.configuracion.moneda;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.MonedaADO;
import model.MonedaTB;

public class FxMonedaController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<MonedaTB> tvList;
    @FXML
    private TableColumn<MonedaTB, String> tcNumero;
    @FXML
    private TableColumn<MonedaTB, String> tcMoneda;
    @FXML
    private TableColumn<MonedaTB, String> tcTipoCambio;
    @FXML
    private TableColumn<MonedaTB, String> tcAbreviatura;
    @FXML
    private TableColumn<MonedaTB, ImageView> tcPredeterminado;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private Text lblPredeterminado;

    private FxPrincipalController fxPrincipalController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcMoneda.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSimbolo() + " - " + cellData.getValue().getNombre()
        ));
        tcTipoCambio.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipoCambio()));
        tcAbreviatura.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getAbreviado()));
        tcPredeterminado.setCellValueFactory(new PropertyValueFactory<>("imagePredeterminado"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcMoneda.prefWidthProperty().bind(tvList.widthProperty().multiply(0.24));
        tcTipoCambio.prefWidthProperty().bind(tvList.widthProperty().multiply(0.24));
        tcAbreviatura.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcPredeterminado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    public void fillTableMonedas() {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return MonedaADO.ListMonedas(0, "", (paginacion - 1) * 20, 20);
            }
        };

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<MonedaTB> monedaTBs = (ObservableList<MonedaTB>) objects[0];
                if (!monedaTBs.isEmpty()) {
                    tvList.setItems(monedaTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) objects[1]) / 20.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                    for (int i = 0; i < monedaTBs.size(); i++) {
                        if (monedaTBs.get(i).isPredeterminado()) {
                            lblPredeterminado.setText(monedaTBs.get(i).getNombre());
                        }
                    }
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
            lblPredeterminado.setText("Ninguno");
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void openWindowMoneyRegister() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_MONEDA_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxMonedaProcesoController controller = fXMLLoader.getController();
        controller.setInitMoneyController(this);
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registre su moneda", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();

    }

    private void openWindowMoneyUpdate() throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_MONEDA_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxMonedaProcesoController controller = fXMLLoader.getController();
            controller.setInitMoneyController(this);
            controller.setUpdateMoney(tvList.getSelectionModel().getSelectedItem().getIdMoneda(),
                    tvList.getSelectionModel().getSelectedItem().getNombre(),
                    tvList.getSelectionModel().getSelectedItem().getAbreviado(),
                    tvList.getSelectionModel().getSelectedItem().getSimbolo(),
                    tvList.getSelectionModel().getSelectedItem().getTipoCambio()
            );
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Actualizar su moneda", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } else {
            Tools.AlertMessageWarning(window, "Moneda", "Seleccione un elemento de la lista.");
        }

    }

    private void onEventProdeteminado() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            String result = MonedaADO.ChangeDefaultState(true, tvList.getSelectionModel().getSelectedItem().getIdMoneda());
            if (result.equalsIgnoreCase("updated")) {
                Tools.AlertMessageInformation(window, "Moneda", "Se cambio la moneda nacional.");
                fillTableMonedas();
            } else {
                Tools.AlertMessageError(window, "Moneda", "Error: " + result);
            }
        } else {
            Tools.AlertMessageWarning(window, "Moneda", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventRemover() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            short value = Tools.AlertMessageConfirmation(window, "Moneda", "¿Está seguro de eliminar la moneda?");
            if (value == 1) {
                String result = MonedaADO.RemoveElement(tvList.getSelectionModel().getSelectedItem().getIdMoneda());
                if (result.equalsIgnoreCase("predetermined")) {
                    Tools.AlertMessageWarning(window, "Moneda", "No se puedo eliminar la moneda ya que está predeterminado.");
                } else if (result.equalsIgnoreCase("venta")) {
                    Tools.AlertMessageWarning(window, "Moneda", "No se puedo eliminar la moneda ya que está ligado a una venta.");
                } else if (result.equalsIgnoreCase("compra")) {
                    Tools.AlertMessageWarning(window, "Moneda", "No se puedo eliminar la moneda ya que está ligado a un compra.");
                } else if (result.equalsIgnoreCase("removed")) {
                    Tools.AlertMessageWarning(window, "Moneda", "Se eliminó correctamente la moneda.");
                    fillTableMonedas();
                } else {
                    Tools.AlertMessageError(window, "Moneda", "Error: " + result);
                }
            }
        } else {
            Tools.AlertMessageWarning(window, "Moneda", "Seleccione un elemento de la lista.");
        }
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillTableMonedas();
                break;

        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMoneyRegister();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        openWindowMoneyRegister();
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMoneyUpdate();

        }
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        openWindowMoneyUpdate();

    }

    @FXML
    private void onKeyPressedRemove(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRemover();
        }
    }

    @FXML
    private void onActionRemove(ActionEvent event) {
        onEventRemover();
    }

    @FXML
    private void onKeyPressedPredetermined(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProdeteminado();
        }
    }

    @FXML
    private void onActionPredetermined(ActionEvent event) {
        onEventProdeteminado();
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) throws IOException {
        if (event.getClickCount() == 2) {
            openWindowMoneyUpdate();
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

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
