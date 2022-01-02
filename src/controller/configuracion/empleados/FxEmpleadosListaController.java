package controller.configuracion.empleados;

import controller.operaciones.venta.FxVentaRealizadasController;
import controller.posterminal.venta.FxPostVentaRealizadasController;
import controller.reporte.FxPosReporteController;
import controller.reporte.FxProduccionReporteController;
import controller.reporte.FxVentaReporteController;
import controller.tools.Tools;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import model.EmpleadoADO;
import model.EmpleadoTB;

public class FxEmpleadosListaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<EmpleadoTB> tvList;
    @FXML
    private TableColumn<EmpleadoTB, Integer> tcId;
    @FXML
    private TableColumn<EmpleadoTB, String> tcDocumento;
    @FXML
    private TableColumn<EmpleadoTB, String> tcPersona;
    @FXML
    private TableColumn<EmpleadoTB, String> tcRol;

    private FxVentaReporteController ventaReporteController;

    private FxVentaReporteController ventaReporteDosController;

    private FxPosReporteController posReporteController;

    private FxVentaRealizadasController ventaRealizadasController;

    private FxPostVentaRealizadasController postVentaRealizadasController;

    private FxProduccionReporteController produccionReporteController;

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
                    case F3:
                        if (!status) {
                            fillEmpleadosTable("");
                        }
                        break;

                }
            }
        });
        tcId.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
        tcDocumento.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeroDocumento())
        );
        tcPersona.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getApellidos() + " ", cellData.getValue().getNombres())
        );
        tcRol.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getRolName()));
    }

    public void fillEmpleadosTable(String value) {

        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<List<EmpleadoTB>> task = new Task<List<EmpleadoTB>>() {
            @Override
            public ObservableList<EmpleadoTB> call() {
                return EmpleadoADO.ListEmpleados(value);
            }
        };

        task.setOnSucceeded(w -> {
            tvList.setItems((ObservableList<EmpleadoTB>) task.getValue());
            status = false;
        });
        task.setOnFailed(w -> {
            status = false;
        });

        task.setOnScheduled(w -> {
            status = true;
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void selectEmpleadoList() {
        if (ventaReporteController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                ventaReporteController.setIdEmpleado(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
                ventaReporteController.getTxtVendedores().setText(tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
            }
        } else if (posReporteController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                posReporteController.setIdEmpleado(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
                posReporteController.getTxtVendedores().setText(tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
            }
        } else if (ventaRealizadasController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                ventaRealizadasController.setIdEmpleado(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
                ventaRealizadasController.getTxtVendedor().setText(tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
            }
        } else if (postVentaRealizadasController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                postVentaRealizadasController.setIdEmpleado(tvList.getSelectionModel().getSelectedItem().getIdEmpleado());
                postVentaRealizadasController.getTxtVendedor().setText(tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
            }
        } else if (ventaReporteDosController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                ventaReporteDosController.setVendorReporte(tvList.getSelectionModel().getSelectedItem().getIdEmpleado(), tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
            }
        } else if (produccionReporteController != null) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                produccionReporteController.setLoadDataEmpleado(tvList.getSelectionModel().getSelectedItem().getIdEmpleado(), tvList.getSelectionModel().getSelectedItem().getApellidos() + " " + tvList.getSelectionModel().getSelectedItem().getNombres());
                Tools.Dispose(apWindow);
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
                fillEmpleadosTable(txtSearch.getText());
            }
        }
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!status) {
                fillEmpleadosTable("");
            }
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        if (!status) {
            fillEmpleadosTable("");
        }
    }

    @FXML
    private void onKeyPressedList(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            selectEmpleadoList();
        }
    }

    @FXML
    private void onMouseClickedList(MouseEvent event) {
        if (event.getClickCount() == 2) {
            selectEmpleadoList();
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            selectEmpleadoList();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        selectEmpleadoList();
    }

    public void setInitVentaReporteController(FxVentaReporteController ventaReporteController) {
        this.ventaReporteController = ventaReporteController;
    }

    public void setInitVentaReporteDosController(FxVentaReporteController ventaReporteDosController) {
        this.ventaReporteDosController = ventaReporteDosController;
    }

    public void setInitPostReporteController(FxPosReporteController posReporteController) {
        this.posReporteController = posReporteController;
    }

    public void setInitVentaRealizadasController(FxVentaRealizadasController ventaRealizadasController) {
        this.ventaRealizadasController = ventaRealizadasController;
    }

    public void setInitPostVentaRealizadasController(FxPostVentaRealizadasController postVentaRealizadasController) {
        this.postVentaRealizadasController = postVentaRealizadasController;
    }

    public void setInitProduccionReporteController(FxProduccionReporteController produccionReporteController) {
        this.produccionReporteController = produccionReporteController;
    }

}
