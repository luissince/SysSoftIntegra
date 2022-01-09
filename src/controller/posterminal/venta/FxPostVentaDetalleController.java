package controller.posterminal.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.reporte.FxReportViewController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.EmpleadoTB;
import model.ImpuestoADO;
import model.ImpuestoTB;
import model.SuministroTB;
import model.VentaADO;
import model.VentaTB;
import net.sf.jasperreports.engine.JRException;

public class FxPostVentaDetalleController implements Initializable {

    @FXML
    private ScrollPane window;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblFechaVenta;
    @FXML
    private Text lblComprobante;
    @FXML
    private Label lblTotal;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lbCorreoElectronico;
    @FXML
    private Text lbDireccion;
    @FXML
    private Text lblTipo;
    @FXML
    private Text lblObservaciones;
    @FXML
    private Text lblVendedor;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblValorVenta;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblSubTotal;
    @FXML
    private Button btnCancelarVenta;
    @FXML
    private Button btnReporte;
    @FXML
    private Button btnImprimir;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblTarjeta;
    @FXML
    private Label lblVuelto;
    @FXML
    private Label lblValor;
    @FXML
    private Text lbClienteInformacion;
    @FXML
    private Label lblDeposito;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private FxPostVentaRealizadasController ventaRealizadasController;

    private ObservableList<SuministroTB> arrList = null;

    private ArrayList<ImpuestoTB> arrayImpuestos;

    private VentaTB ventaTB;

    private String idVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        arrayImpuestos = new ArrayList<>();
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketVentaDetalle(window);
    }

    public void setInitComponents(String idVenta) {
        this.idVenta = idVenta;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                arrayImpuestos.clear();
                ImpuestoADO.GetTipoImpuestoCombBox().forEach(e -> {
                    arrayImpuestos.add(new ImpuestoTB(e.getIdImpuesto(), e.getNombreOperacion(), e.getNombre(), e.getValor(), e.isPredeterminado()));
                });
                return VentaADO.Obtener_Venta_ById(idVenta);
            }
        };

        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });

        task.setOnSucceeded(e -> {
            Object result = task.getValue();
            if (result instanceof VentaTB) {
                ventaTB = (VentaTB) result;
                EmpleadoTB empleadoTB = ventaTB.getEmpleadoTB();
                ObservableList<SuministroTB> empList = FXCollections.observableArrayList(ventaTB.getSuministroTBs());
                lblFechaVenta.setText(ventaTB.getFechaVenta() + " " + ventaTB.getHoraVenta());
                lblCliente.setText(ventaTB.getClienteTB().getNumeroDocumento() + "-" + ventaTB.getClienteTB().getInformacion());
                lbClienteInformacion.setText(ventaTB.getClienteTB().getTelefono() + "-" + ventaTB.getClienteTB().getCelular());
                lbCorreoElectronico.setText(ventaTB.getClienteTB().getEmail());
                lbDireccion.setText(ventaTB.getClienteTB().getDireccion());
                lblComprobante.setText(ventaTB.getComprobanteName() + " " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                lblObservaciones.setText(ventaTB.getObservaciones());
                lblTipo.setText(ventaTB.getTipoName() + " - " + ventaTB.getEstadoName());
                btnCancelarVenta.setDisable(ventaTB.getEstado() == 3);

                lblEfectivo.setText(Tools.roundingValue(ventaTB.getEfectivo(), 2));
                lblTarjeta.setText(Tools.roundingValue(ventaTB.getTarjeta(), 2));
                lblDeposito.setText(Tools.roundingValue(ventaTB.getDeposito(), 2));
//                lblValor.setText(Tools.roundingValue(ventaTB.getImporteNeto(), 2));
                lblVuelto.setText(Tools.roundingValue(ventaTB.getVuelto(), 2));

                if (empleadoTB != null) {
                    lblVendedor.setText(empleadoTB.getApellidos() + " " + empleadoTB.getNombres());
                }
                fillVentasDetalleTable(empList);
                lblLoad.setVisible(false);
            } else {
                btnReporte.setDisable(true);
                btnCancelarVenta.setDisable(true);
                btnImprimir.setDisable(true);
                lblLoad.setVisible(false);
            }
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void fillVentasDetalleTable(ObservableList<SuministroTB> empList) {
        arrList = empList;
        for (int i = 0; i < arrList.size(); i++) {
            gpList.add(addElementGridPaneLabel("l1" + (i + 1), arrList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPaneLabel("l2" + (i + 1), arrList.get(i).getClave() + "\n" + arrList.get(i).getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPaneLabel("l3" + (i + 1), Tools.roundingValue(arrList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPaneLabel("l4" + (i + 1), arrList.get(i).getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPaneLabel("l5" + (i + 1), arrList.get(i).getImpuestoTB().getNombreImpuesto(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPaneLabel("l6" + (i + 1), ventaTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(arrList.get(i).getPrecioVentaGeneral(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPaneLabel("l7" + (i + 1), Tools.roundingValue(arrList.get(i).getDescuento(), 2) + "%", Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPaneLabel("l8" + (i + 1), ventaTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(arrList.get(i).getPrecioVentaGeneral() * arrList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 7, (i + 1));
            gpList.add(arrList.get(i).getEstadoName().equalsIgnoreCase("C")
                    ? addElementGridPaneLabel("l9" + (i + 1), "COMPLETADO", Pos.CENTER_LEFT)
                    : addElementGridPaneButtonLlevar("l9" + (i + 1), "LLEVAR \n" + Tools.roundingValue(arrList.get(i).getPorLlevar(), 2), arrList.get(i).getIdSuministro(), arrList.get(i).getCostoCompra(), Pos.CENTER_LEFT),
                    8, (i + 1));
            gpList.add(addElementGridPaneButtonHistorial("l10" + (i + 1), "HISTORIAL", arrList.get(i), Pos.CENTER_LEFT), 9, (i + 1));
        }
        calcularTotales();
    }

    private void onEventCancelar() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_DEVOLUCION);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxPostVentaDevolucionController controller = fXMLLoader.getController();
            controller.setInitVentaDetalle(this);
//            controller.setLoadVentaDevolucion(idVenta, arrList, ventaTB.getSerie() + "-" + ventaTB.getNumeracion(), Tools.roundingValue(ventaTB.getImporteNeto(), 2));
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Cancelar la venta", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en venta detalle: " + ex.getLocalizedMessage());
        }
    }

    private void calcularTotales() {
        if (ventaTB != null) {
//            lblValorVenta.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(ventaTB.getImporteBruto(), 2));
//            lblDescuento.setText(ventaTB.getMonedaTB().getSimbolo() + " -" + Tools.roundingValue(ventaTB.getDescuento(), 2));
//            lblSubTotal.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(ventaTB.getSubImporteNeto(), 2));
//            lblImpuesto.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(ventaTB.getImpuesto(), 2));
//            lblTotal.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(ventaTB.getImporteNeto(), 2));
        }
    }

    private Label addElementGridPaneLabel(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Button addElementGridPaneButtonLlevar(String id, String nombre, String idSuministro, double costo, Pos pos) {
        Button button = new Button(nombre);
        button.setId(id);
        button.getStyleClass().add("buttonLightCancel");
        button.setAlignment(pos);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        ImageView imageView = new ImageView(new Image("/view/image/plus.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setOnAction(event -> {
            openWindowLlevar(idSuministro, costo);
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openWindowLlevar(idSuministro, costo);
            }
        });
        return button;
    }

    private Button addElementGridPaneButtonHistorial(String id, String nombre, SuministroTB suministroTB, Pos pos) {
        Button button = new Button(nombre);
        button.setId(id);
        button.getStyleClass().add("buttonLightCancel");
        button.setAlignment(pos);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        ImageView imageView = new ImageView(new Image("/view/image/asignacion.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setOnAction(event -> {
            openWindowHistorial(suministroTB);
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openWindowHistorial(suministroTB);
            }
        });
        return button;
    }

    private Label addLabelTitle(String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setStyle("-fx-text-fill: #020203;-fx-padding:  0.4166666666666667em 0em  0.4166666666666667em 0em");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setMinWidth(Control.USE_COMPUTED_SIZE);
        label.setMinHeight(Control.USE_COMPUTED_SIZE);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Control.USE_COMPUTED_SIZE);
        return label;
    }

    private Label addLabelTotal(String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setStyle("-fx-text-fill:#0771d3;");
        label.getStyleClass().add("labelRoboto15");
        label.setAlignment(pos);
        label.setMinWidth(Control.USE_COMPUTED_SIZE);
        label.setMinHeight(Control.USE_COMPUTED_SIZE);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Control.USE_COMPUTED_SIZE);
        return label;
    }

    private void openWindowLlevar(String idSuministro, double costo) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_LLEVAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxPostVentaLlevarController controller = fXMLLoader.getController();
            controller.setInitData(idVenta, idSuministro, lblComprobante.getText(), costo);
            controller.setInitVentaDetalleController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Producto a llevar", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            Tools.println("Venta estructura openWindowLlevar: " + ex.getLocalizedMessage());
        }
    }

    private void openWindowHistorial(SuministroTB suministroTB) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_LLEVAR_HISTORIAL);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxPostVentaLlevarControllerHistorial controller = fXMLLoader.getController();
            controller.setInitVentaDetalleController(this);
            controller.loadData(ventaTB, suministroTB);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Historial de Salida", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            Tools.println("Venta estructura openWindowHistorial: " + ex.getLocalizedMessage());
        }
    }

    private void openWindowReporte() {
//        if (ventaTB != null) {
//            try {
//                URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
//                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
//                Parent parent = fXMLLoader.load(url.openStream());
//                //Controlller here
//                FxReportViewController controller = fXMLLoader.getController();
//                controller.setFileName(ventaTB.getComprobanteName().toUpperCase() + " " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
//                controller.setJasperPrint(fxOpcionesImprimirController.getTicketVenta().reportA4());
//                controller.show();
//                Stage stage = WindowStage.StageLoader(parent, "Venta realizada");
//                stage.setResizable(true);
//                stage.show();
//                stage.requestFocus();
//            } catch (IOException | JRException ex) {
//                Tools.AlertMessageError(window, "Reporte de Ventas", "Error al generar el reporte: " + ex.getLocalizedMessage());
//            }
//        } else {
//            Tools.AlertMessageWarning(window, "Detalle Venta", "No se puede generar el reporte por problemas en al carga de información.");
//        }
    }

    private void onEventImprimirVenta() {
        if (idVenta != null || !idVenta.equalsIgnoreCase("")) {
            fxOpcionesImprimirController.getTicketVenta().imprimir(idVenta);
        } else {
            Tools.AlertMessageWarning(window, "Detalle Venta", "No se puede generar la impresión por poblemas de carga de datos.");
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCancelar();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) throws IOException {
        onEventCancelar();
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventImprimirVenta();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        onEventImprimirVenta();
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) throws IOException {
        fxPrincipalController.getVbContent().getChildren().remove(window);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setTopAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setRightAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setBottomAnchor(ventaRealizadasController.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(ventaRealizadasController.getWindow());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        openWindowReporte();
    }

    public void setInitVentasController(FxPostVentaRealizadasController ventaRealizadasController, FxPrincipalController fxPrincipalController) {
        this.ventaRealizadasController = ventaRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

    public ScrollPane getWindow() {
        return window;
    }

}
