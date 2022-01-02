package controller.consultas.pagar;

import controller.menus.FxPrincipalController;
import controller.reporte.FxReportViewController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.VentaADO;
import model.VentaTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

public class FxCuentasPorCobrarController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<VentaTB> tvList;
    @FXML
    private TableColumn<VentaTB, String> tcNumero;
    @FXML
    private TableColumn<VentaTB, String> tcFechaRegistro;
    @FXML
    private TableColumn<VentaTB, String> tcProveedor;
    @FXML
    private TableColumn<VentaTB, String> tcComprobante;
    @FXML
    private TableColumn<VentaTB, Label> tcEstado;
    @FXML
    private TableColumn<VentaTB, String> tcMontoTotal;
    @FXML
    private TableColumn<VentaTB, String> tcMontoCobrado;
    @FXML
    private TableColumn<VentaTB, String> tcDiferencia;
    @FXML
    private TableColumn<VentaTB, HBox> tcOpciones;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private CheckBox rbCuentas;

    private FxPrincipalController fxPrincipalController;

    private short opcion;

    private int paginacion;

    private int totalPaginacion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFechaRegistro.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaVenta() + "\n" + cellData.getValue().getHoraVenta()));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento() + "\n" + cellData.getValue().getClienteTB().getInformacion()));
        tcComprobante.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));
        tcMontoTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaName() + " " + Tools.roundingValue(cellData.getValue().getMontoTotal(), 2)));
        tcMontoCobrado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaName() + " " + Tools.roundingValue(cellData.getValue().getMontoCobrado(), 2)));
        tcDiferencia.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaName() + " " + Tools.roundingValue(cellData.getValue().getMontoRestante(), 2)));
        tcOpciones.setCellValueFactory(new PropertyValueFactory<>("hbOpciones"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFechaRegistro.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcProveedor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcMontoTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcMontoCobrado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcDiferencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcOpciones.prefWidthProperty().bind(tvList.widthProperty().multiply(0.08));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        loadTableCuentasPorCobrar();
    }

    private void loadTableCuentasPorCobrar() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillPurchasesTable(0, "", rbCuentas.isSelected(), "", "");
            opcion = 0;
        }
    }

    public void fillPurchasesTable(int opcion, String buscar, boolean mostrar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task< Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return VentaADO.ListarVentasCredito(opcion, buscar, mostrar, fechaInicio, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<VentaTB> ventaTBs = (ObservableList<VentaTB>) objects[0];
                if (!ventaTBs.isEmpty()) {
                    ventaTBs.forEach(e -> {
                        Button btnVisualizar = (Button) e.getHbOpciones().getChildren().get(0);
                        btnVisualizar.setOnAction(event -> {
                            onEventVisualizar(e.getIdVenta());
                        });
                        btnVisualizar.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                onEventVisualizar(e.getIdVenta());
                            }
                        });
                    });
                    tvList.setItems(ventaTBs);
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
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getMessage(), "-fx-text-fill:#a70820;", false));
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

    private void onEventVisualizar(String idVenta) {
        try {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_CUENTAS_POR_COBRAR_VISUALIZAR));
            AnchorPane node = fXMLLoader.load();
            //Controlller here
            FxCuentasPorCobrarVisualizarController controller = fXMLLoader.getController();
            controller.loadData(idVenta);
            controller.setInitCuentasPorCobrar(fxPrincipalController, this);
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } catch (IOException ex) {
            Tools.println("Error en la vista Generar el cobro:" + ex.getLocalizedMessage());
        }
    }

    public void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillPurchasesTable(0, "", rbCuentas.isSelected(), "", "");
                break;
            case 1:
                fillPurchasesTable(1, txtSearch.getText().trim(), rbCuentas.isSelected(), "", "");
                break;
            case 2:
                fillPurchasesTable(2, "", rbCuentas.isSelected(), Tools.getDatePicker(dpFechaInicial), Tools.getDatePicker(dpFechaFinal));
                break;
        }
    }

    private void onEventReporte() {
        try {

            if (tvList.getItems().isEmpty()) {
                Tools.AlertMessageWarning(vbWindow, "Reporte Cuentas por Cobrar", "No hay páginas para mostrar en el reporte.");
                return;
            }

            InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

            if (Session.COMPANY_IMAGE != null) {
                imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
            }

            InputStream dir = getClass().getResourceAsStream("/report/CuentasPorCobrar.jasper");

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(dir);
            Map map = new HashMap();
            map.put("LOGO", imgInputStream);
            map.put("PERIODO", Tools.getDatePicker(dpFechaInicial) + " - " + Tools.getDatePicker(dpFechaFinal));
            map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
            map.put("DIRECCION", Session.COMPANY_DOMICILIO);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JRBeanCollectionDataSource(tvList.getItems()));

            URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxReportViewController controller = fXMLLoader.getController();
            controller.setJasperPrint(jasperPrint);
            controller.show();
            Stage stage = WindowStage.StageLoader(parent, "Cuentas por pagar");
            stage.setResizable(true);
            stage.show();
            stage.requestFocus();

        } catch (HeadlessException | JRException | IOException ex) {
            Tools.AlertMessageError(vbWindow, "Cuentas por Cobrar", "Error al generar el reporte : " + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPressedVisualizar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                onEventVisualizar(tvList.getSelectionModel().getSelectedItem().getIdVenta());
            } else {
                Tools.AlertMessage(vbWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Venta", "Debe seleccionar una venta de la lista", false);
            }
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            onEventVisualizar(tvList.getSelectionModel().getSelectedItem().getIdVenta());
        } else {
            Tools.AlertMessage(vbWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Venta", "Debe seleccionar una venta de la lista", false);
        }
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.actualDate(Tools.getDate(), dpFechaInicial);
            Tools.actualDate(Tools.getDate(), dpFechaFinal);
            loadTableCuentasPorCobrar();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        loadTableCuentasPorCobrar();
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
                && event.getCode() != KeyCode.PAUSE
                && event.getCode() != KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(1, txtSearch.getText().trim(), rbCuentas.isSelected(), "", "");
                opcion = 1;
            }
        }
    }

    @FXML
    private void onActionFechaInicio(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dpFechaInicial.getValue() != null & dpFechaFinal.getValue() != null) {
                paginacion = 1;
                fillPurchasesTable(2, "", rbCuentas.isSelected(), Tools.getDatePicker(dpFechaInicial), Tools.getDatePicker(dpFechaFinal));
                opcion = 2;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (dpFechaInicial.getValue() != null & dpFechaFinal.getValue() != null) {
                paginacion = 1;
                fillPurchasesTable(2, "", rbCuentas.isSelected(), Tools.getDatePicker(dpFechaInicial), Tools.getDatePicker(dpFechaFinal));
                opcion = 2;
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

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        onEventReporte();
    }

    public VBox getVbWindow() {
        return vbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
