package controller.inventario.traslados;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.inventario.movimientos.FxMovimientosController;
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
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.TrasladoADO;
import model.TrasladoTB;

public class FxTrasladoController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker dtFechaInicio;
    @FXML
    private DatePicker dtFechaFinal;
    @FXML
    private TableView<TrasladoTB> tvList;
    @FXML
    private TableColumn<TrasladoTB, String> tcNumero;
    @FXML
    private TableColumn<TrasladoTB, String> tcFecha;
    @FXML
    private TableColumn<TrasladoTB, String> tcGuia;
    @FXML
    private TableColumn<TrasladoTB, String> tcTipo;
    @FXML
    private TableColumn<TrasladoTB, String> tcEstado;
    @FXML
    private TableColumn<TrasladoTB, String> tcUsuario;
    @FXML
    private TableColumn<TrasladoTB, Button> tcDetalle;
    @FXML
    private TableColumn<TrasladoTB, Button> tcReporte;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;

    private FxPrincipalController fxPrincipalController;

    private FXMLLoader fXMLLoaderTrasladInventario;

    private AnchorPane nodeTrasladoInventario;

    private FxTrasladoInventarioController controllerTrasladoInventario;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paginacion = 1;
        opcion = 0;

        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketTraslado(vbWindow);

        Tools.actualDate(Tools.getDate(), dtFechaInicio);
        Tools.actualDate(Tools.getDate(), dtFechaFinal);

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcFecha.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getFecha() + "\n"
                + cellData.getValue().getHora()
        ));
        tcGuia.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getVentaTB() != null ? "" + cellData.getValue().getNumeracion() + "\n" + cellData.getValue().getVentaTB().getSerie() + "-" + cellData.getValue().getVentaTB().getNumeracion() : "Guía-" + cellData.getValue().getNumeracion()));
        tcTipo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getTipo() == 1 ? "INTERNO" : "EXTERNO"));
        tcEstado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEstado() == 1 ? "COMPLETADO" : "ANULADO"));
        tcUsuario.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEmpleadoTB().getApellidos() + ", " + cellData.getValue().getEmpleadoTB().getNombres()));
        tcDetalle.setCellValueFactory(new PropertyValueFactory<>("btnDetalle"));
        tcReporte.setCellValueFactory(new PropertyValueFactory<>("btnReporte"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcFecha.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcGuia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcTipo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcUsuario.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcDetalle.prefWidthProperty().bind(tvList.widthProperty().multiply(0.07));
        tcReporte.prefWidthProperty().bind(tvList.widthProperty().multiply(0.07));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        loadInit();

        try {
            fXMLLoaderTrasladInventario = WindowStage.LoaderWindow(getClass().getResource(FilesRouters.FX_TRASLADO_INVENTARIO));
            nodeTrasladoInventario = fXMLLoaderTrasladInventario.load();
            controllerTrasladoInventario = fXMLLoaderTrasladInventario.getController();;
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void loadInit() {
        if (!lblLoad.isVisible()) {
            paginacion = 1;
            fillVentasTable(0, "", "");
            opcion = 0;
        }
    }

    private void fillVentasTable(int opcion, String fechaInicial, String fechaFinal) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return TrasladoADO.ListarTraslados(opcion, fechaInicial, fechaFinal, (paginacion - 1) * 20, 20);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<TrasladoTB> trasladoTBs = (ObservableList<TrasladoTB>) objects[0];
                trasladoTBs.forEach(e -> {
                    e.getBtnDetalle().setOnAction(event -> {
                        openWindonDetalleTraslado(e.getIdTraslado());
                    });
                    e.getBtnDetalle().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            openWindonDetalleTraslado(e.getIdTraslado());
                            event.consume();
                        }
                    });

                    e.getBtnReporte().setOnAction(event -> {
                        fxOpcionesImprimirController.getTicketTraslado().mostrarReporte(e.getIdTraslado());
                    });
                    e.getBtnReporte().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            fxOpcionesImprimirController.getTicketTraslado().mostrarReporte(e.getIdTraslado());
                            event.consume();
                        }
                    });
                });

                if (!trasladoTBs.isEmpty()) {
                    tvList.setItems(trasladoTBs);
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
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
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

    private void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillVentasTable(0, "", "");
                break;
            case 1:
                fillVentasTable(1, Tools.getDatePicker(dtFechaInicio), Tools.getDatePicker(dtFechaFinal));
                break;
        }
    }

    private void openWindonDetalleTraslado(String idTraslado) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = FxMovimientosController.class.getClassLoader().getClass().getResource(FilesRouters.FX_TRASLADO_DETALLE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxTrasladoDetalleController controller = fXMLLoader.getController();
            controller.loadTraslado(idTraslado);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Detalle del movimiento", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

    }

//    private void openWindowTrasladoSimple() {
//        controllerTrasladoSimple.setContent(this, fxPrincipalController);
//        fxPrincipalController.getVbContent().getChildren().clear();
//        AnchorPane.setLeftAnchor(nodeTrasladoSimple, 0d);
//        AnchorPane.setTopAnchor(nodeTrasladoSimple, 0d);
//        AnchorPane.setRightAnchor(nodeTrasladoSimple, 0d);
//        AnchorPane.setBottomAnchor(nodeTrasladoSimple, 0d);
//        fxPrincipalController.getVbContent().getChildren().add(nodeTrasladoSimple);
//    }
    private void openWindowTrasladoInventario() {
        controllerTrasladoInventario.setContent(this, fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeTrasladoInventario, 0d);
        AnchorPane.setTopAnchor(nodeTrasladoInventario, 0d);
        AnchorPane.setRightAnchor(nodeTrasladoInventario, 0d);
        AnchorPane.setBottomAnchor(nodeTrasladoInventario, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeTrasladoInventario);
    }

    @FXML
    private void onKeyPressedTrasladoInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowTrasladoInventario();
        }
    }

    @FXML
    private void onActionTrasladoInventario(ActionEvent event) {
        openWindowTrasladoInventario();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadInit();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        loadInit();
    }

    @FXML
    private void onActionFecha(ActionEvent event) {
        if (dtFechaInicio.getValue() != null && dtFechaFinal.getValue() != null) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillVentasTable(1, Tools.getDatePicker(dtFechaInicio), Tools.getDatePicker(dtFechaFinal));
                opcion = 1;
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
