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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CompraTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import service.CompraADO;

public class FxCuentasPorPagarController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<CompraTB> tvList;
    @FXML
    private TableColumn<CompraTB, String> tcNumero;
    @FXML
    private TableColumn<CompraTB, String> tcFechaRegistro;
    @FXML
    private TableColumn<CompraTB, String> tcProveedor;
    @FXML
    private TableColumn<CompraTB, String> tcComprobante;
    @FXML
    private TableColumn<CompraTB, String> tcMontoTotal;
    @FXML
    private TableColumn<CompraTB, String> tcMontoPagado;
    @FXML
    private TableColumn<CompraTB, String> tcDiferencia;
    @FXML
    private TableColumn<CompraTB, Label> tcEstado;
    @FXML
    private TableColumn<CompraTB, HBox> tcOpciones;
    @FXML
    private TextField txtSearch;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
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
        tcFechaRegistro.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaCompra() + "\n" + cellData.getValue().getHoraCompra()));
        tcProveedor.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getProveedorTB().getNumeroDocumento() + "\n" + cellData.getValue().getProveedorTB().getRazonSocial()));
        tcComprobante.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion()));
        tcEstado.setCellValueFactory(new PropertyValueFactory<>("estadoLabel"));
        tcMontoTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getMontoTotal(), 2)));
        tcMontoPagado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getMontoPagado(), 2)));
        tcDiferencia.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getMontoRestante(), 2)));
        tcOpciones.setCellValueFactory(new PropertyValueFactory<>("hbOpciones"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFechaRegistro.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcProveedor.prefWidthProperty().bind(tvList.widthProperty().multiply(0.19));
        tcComprobante.prefWidthProperty().bind(tvList.widthProperty().multiply(0.12));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));
        tcMontoTotal.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcMontoPagado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcDiferencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcOpciones.prefWidthProperty().bind(tvList.widthProperty().multiply(0.07));

        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        loadTableCuentasPorPagar();
    }

    private void loadTableCuentasPorPagar() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillPurchasesTable(0, "", rbCuentas.isSelected(), "", "");
            opcion = 0;
        }
    }

    public void fillPurchasesTable(int opcion, String search, boolean mostrar, String fechaInicio, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CompraADO.ListComprasCredito(opcion, search, mostrar, fechaInicio, fechaFinal, (paginacion - 1) * 10, 10);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<CompraTB> empList = (ObservableList<CompraTB>) objects[0];
                if (!empList.isEmpty()) {
                    empList.forEach(f -> {
                        Button btnVisualizar = (Button) f.getHbOpciones().getChildren().get(0);
                        btnVisualizar.setOnAction(event -> {
                            onEventVisualizar(f.getIdCompra());

                        });
                        btnVisualizar.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                onEventVisualizar(f.getIdCompra());
                            }
                        });
                    });
                    tvList.setItems(empList);

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

    private void onEventVisualizar(String idCompra) {
        try {
            FXMLLoader fXMLLoader = new FXMLLoader(getClass().getResource(FilesRouters.FX_CUENTAS_POR_PAGAR_VISUALIZAR));
            AnchorPane node = fXMLLoader.load();
            //Controlller here
            FxCuentasPorPagarVisualizarController controller = fXMLLoader.getController();
            controller.loadTableCompraCredito(idCompra);
            controller.setInitCuentasPorPagar(fxPrincipalController, this);
            //
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(node, 0d);
            AnchorPane.setTopAnchor(node, 0d);
            AnchorPane.setRightAnchor(node, 0d);
            AnchorPane.setBottomAnchor(node, 0d);
            fxPrincipalController.getVbContent().getChildren().add(node);
        } catch (IOException ex) {
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
                Tools.AlertMessageWarning(vbWindow, "Reporte Cuentas por Pagar", "No hay páginas para mostrar en el reporte.");
                return;
            }

            InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

            if (Session.COMPANY_IMAGE != null) {
                imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
            }

            InputStream dir = getClass().getResourceAsStream("/report/CuentasPorPagar.jasper");

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
            Tools.AlertMessageError(vbWindow, "Reporte de Inventario", "Error al generar el reporte : " + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onActionSearch(ActionEvent event) {
        if (!tvList.getItems().isEmpty()) {
            tvList.getSelectionModel().select(0);
            tvList.requestFocus();
        }
    }

    @FXML
    private void onKeyReleasedSeach(KeyEvent event) {
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
    private void onActionFechaInicial(ActionEvent event) {
        if (dpFechaInicial.getValue() != null && dpFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", rbCuentas.isSelected(), Tools.getDatePicker(dpFechaInicial), Tools.getDatePicker(dpFechaFinal));
                opcion = 2;
            }
        }
    }

    @FXML
    private void onActionFechaFinal(ActionEvent event) {
        if (dpFechaInicial.getValue() != null && dpFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillPurchasesTable(2, "", rbCuentas.isSelected(), Tools.getDatePicker(dpFechaInicial), Tools.getDatePicker(dpFechaFinal));
                opcion = 2;
            }
        }
    }

    @FXML
    private void onKeyPressedVisualizar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                onEventVisualizar(tvList.getSelectionModel().getSelectedItem().getIdCompra());
            } else {
                Tools.AlertMessageWarning(vbWindow, "Compra", "Debe seleccionar una compra de la lista");
            }
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            onEventVisualizar(tvList.getSelectionModel().getSelectedItem().getIdCompra());
        } else {
            Tools.AlertMessageWarning(vbWindow, "Compra", "Debe seleccionar una compra de la lista");
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

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadTableCuentasPorPagar();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        loadTableCuentasPorPagar();
    }

    @FXML
    private void onMouseClickList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (!tvList.getItems().isEmpty()) {
                onEventVisualizar(tvList.getSelectionModel().getSelectedItem().getIdCompra());
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

    public VBox getVbWindow() {
        return vbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
