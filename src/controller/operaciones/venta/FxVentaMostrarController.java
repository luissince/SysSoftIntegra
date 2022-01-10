package controller.operaciones.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.VentaADO;
import model.VentaTB;

public class FxVentaMostrarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<VentaTB> tvList;
    @FXML
    private TableColumn<VentaTB, String> tcNumero;
    @FXML
    private TableColumn<VentaTB, String> tcCliente;
    @FXML
    private TableColumn<VentaTB, String> tcDocumento;
    @FXML
    private TableColumn<VentaTB, String> tcFechaHora;
    @FXML
    private TableColumn<VentaTB, String> tcTotal;
    @FXML
    private TableColumn<VentaTB, Button> tcImprimir;
    @FXML
    private TableColumn<VentaTB, Button> tcAgregarVenta;
    @FXML
    private TableColumn<VentaTB, Button> tcSumarVenta;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxVentaEstructuraController ventaEstructuraController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcCliente.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClienteTB().getNumeroDocumento() + "\n" + cellData.getValue().getClienteTB().getInformacion()));
        tcFechaHora.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFechaVenta() + "\n"
                + cellData.getValue().getHoraVenta()
        ));
        tcDocumento.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getSerie() + "-" + cellData.getValue().getNumeracion() + "\n" + (cellData.getValue().getNotaCreditoTB() != null ? " Modificado(" + cellData.getValue().getNotaCreditoTB().getSerie() + "-" + cellData.getValue().getNotaCreditoTB().getNumeracion() + ")" : "")
        ));
        tcTotal.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMonedaTB().getSimbolo() + " " + Tools.roundingValue(cellData.getValue().getTotal(), 2)));

        tcImprimir.setCellValueFactory(new PropertyValueFactory<>("btnImprimir"));
        tcAgregarVenta.setCellValueFactory(new PropertyValueFactory<>("btnAgregar"));
        tcSumarVenta.setCellValueFactory(new PropertyValueFactory<>("btnSumar"));
        tvList.setPlaceholder(Tools.placeHolderTableView("Ingrese la información a buscar.", "-fx-text-fill:#020203;", false));

        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();

        paginacion = 1;
        opcion = 0;
    }

    private void fillVentasTable(int opcion, String value) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return VentaADO.ListVentasMostrarLibres(opcion, value, (paginacion - 1) * 10, 10);
            }
        };

        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });

        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });

        task.setOnSucceeded(e -> {
            Object result = task.getValue();
            if (result instanceof Object[]) {
                Object[] object = (Object[]) result;
                ObservableList<VentaTB> ventaTBs = (ObservableList<VentaTB>) object[0];
                if (!ventaTBs.isEmpty()) {
                    ventaTBs.forEach(f -> {
                        f.getBtnImprimir().setOnAction(event -> {
                            imprimirVenta(f.getIdVenta());
                        });
                        f.getBtnImprimir().setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                imprimirVenta(f.getIdVenta());
                            }
                            event.consume();
                        });
                        f.getBtnAgregar().setOnAction(event -> {
                            addVenta(f.getIdVenta());
                        });
                        f.getBtnAgregar().setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                addVenta(f.getIdVenta());
                            }
                            event.consume();
                        });

                        f.getBtnSumar().setOnAction(event -> {
                            plusVenta(f.getIdVenta());
                        });
                        f.getBtnSumar().setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                plusVenta(f.getIdVenta());
                            }
                        });
                    });
                    tvList.setItems(ventaTBs);
                    totalPaginacion = (int) (Math.ceil(((Integer) object[1]) / 10.00));
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) result, "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void addVenta(String idVenta) {
        if (ventaEstructuraController != null) {
            ventaEstructuraController.resetVenta();
            Tools.Dispose(apWindow);
            ventaEstructuraController.loadAddVenta(idVenta);
        }
    }

    public void plusVenta(String idVenta) {
        if (ventaEstructuraController != null) {
            Tools.Dispose(apWindow);
            ventaEstructuraController.loadPlusVenta(idVenta);
        }
    }

    private void imprimirVenta(String idVenta) {
        fxOpcionesImprimirController.loadTicketVentaDetalle(apWindow);
        fxOpcionesImprimirController.getTicketVenta().imprimir(idVenta);
    }

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillVentasTable(0, txtSearch.getText().trim());
                break;
            case 1:
                fillVentasTable(1, "");
                break;
        }
    }

    @FXML
    private void onKeyPressed10PrimerasVentas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable(1, "");
                opcion = 1;
            }
        }
    }

    @FXML
    private void onAction10PrimerasVentas(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillVentasTable(1, "");
            opcion = 1;
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
                    fillVentasTable(0, txtSearch.getText().trim());
                    opcion = 0;
                }
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

    public TextField getTxtSearch() {
        return txtSearch;
    }

    public void setInitControllerVentaEstructura(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

}
