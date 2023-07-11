package controller.consultas.cobrar;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.SuministroTB;
import model.VentaCreditoTB;
import model.VentaTB;
import service.VentaADO;

public class FxCuentasPorCobrarVisualizarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private Label lblComprobante;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblTelefonoCelular;
    @FXML
    private Label lblDireccion;
    @FXML
    private Label lblEmail;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblMontoPagado;
    @FXML
    private Label lblDiferencia;
    @FXML
    private Label lblCliente;
    @FXML
    private Label lblMontoTotal;
    @FXML
    private Label lblObservacion;
    @FXML
    private GridPane gpDetalle;
    @FXML
    private Button btnCobrar;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private FxCuentasPorCobrarController cuentasPorCobrarController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private VentaTB ventaTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCuentaPorCobrar(apWindow);
    }

    public void loadData(String idVenta) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<VentaTB> task = new Task<VentaTB>() {
            @Override
            public VentaTB call() throws Exception {
                Object result = VentaADO.obtenerVentaDetalleCreditoPorId(idVenta);
                if (result instanceof VentaTB) {
                    return (VentaTB) result;
                }
                throw new Exception((String) result);
            }
        };
        task.setOnScheduled(w -> {
            borrarContenido();
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                onEventCloseView();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    onEventCloseView();
                }
                event.consume();
            });
        });
        task.setOnSucceeded(w -> {
            ventaTB = task.getValue();
            lblCliente.setText(
                    ventaTB.getClienteTB().getNumeroDocumento() + " - " + ventaTB.getClienteTB().getInformacion());
            lblTelefonoCelular.setText(ventaTB.getClienteTB().getCelular());
            lblDireccion.setText(ventaTB.getClienteTB().getDireccion());
            lblEmail.setText(ventaTB.getClienteTB().getEmail());
            lblComprobante.setText(ventaTB.getSerie() + " - " + ventaTB.getNumeracion());
            lblEstado.setText(ventaTB.getEstadoName());
            lblMontoTotal.setText(Tools.roundingValue(ventaTB.getMontoTotal(), 2));
            lblMontoPagado.setText(Tools.roundingValue(ventaTB.getMontoCobrado(), 2));
            lblDiferencia.setText(Tools.roundingValue(ventaTB.getMontoRestante(), 2));
            lblObservacion.setText(ventaTB.getObservaciones());

            ventaTB.getVentaCreditoTBs().forEach(vc -> {
                vc.getBtnPagar().setOnAction(event -> {
                    onEventActualizarCobro(vc.getIdVentaCredito(), vc.getMonto());
                });
                vc.getBtnPagar().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        onEventActualizarCobro(vc.getIdVentaCredito(), vc.getMonto());
                        event.consume();
                    }
                });

                vc.getBtnQuitar().setOnAction(event -> {
                    onEventAnular(vc.getIdVentaCredito());
                });
                vc.getBtnQuitar().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        onEventAnular(vc.getIdVentaCredito());
                        event.consume();
                    }
                });
            });

            if (ventaTB.getTipoCredito() == 1) {
                btnCobrar.setDisable(true);
            }

            gpList.getChildren().clear();
            createHeadTableCredito();
            createBodyTableCredito();

            gpDetalle.getChildren().clear();
            createHeadTableDetalleVenta();
            createBodyTableDetalleVenta(ventaTB.getSuministroTBs());
            spBody.setDisable(false);
            hbLoad.setVisible(false);

        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void createHeadTableCredito() {
        gpList.add(addHeadGridPane("N°"), 0, 0);
        gpList.add(addHeadGridPane("Nrm. Transacción"), 1, 0);
        gpList.add(addHeadGridPane("Fecha de Cobro"), 2, 0);
        gpList.add(addHeadGridPane("Estado"), 3, 0);
        gpList.add(addHeadGridPane("Monto"), 4, 0);
        gpList.add(addHeadGridPane("Observación"), 5, 0);
        gpList.add(addHeadGridPane("Cobrar"), 6, 0);
        gpList.add(addHeadGridPane("Remover"), 7, 0);
    }

    private void createBodyTableCredito() {
        for (int i = 0; i < ventaTB.getVentaCreditoTBs().size(); i++) {
            VentaCreditoTB ventaCreditoTB = ventaTB.getVentaCreditoTBs().get(i);
            String id = ventaCreditoTB.getId() + "";
            String idVentaCredito = ventaCreditoTB.getIdVentaCredito();
            String fecha = ventaCreditoTB.getFechaPago();
            String estado = ventaCreditoTB.getEstado() == 1 ? "COBRADO" : "POR COBRAR";
            String color = ventaCreditoTB.getEstado() == 1 ? "#0771d3" : "#c62303";
            String monto = Tools.roundingValue(ventaCreditoTB.getMonto(), 2);
            String observacion = ventaCreditoTB.getObservacion();
            Node pagar = ventaCreditoTB.getEstado() == 1 ? new Label("-") : ventaCreditoTB.getBtnPagar();
            Node quitar = ventaCreditoTB.getEstado() == 0 ? new Label("-") : ventaCreditoTB.getBtnQuitar();
            String fill = "#020203";

            gpList.add(addElementGridPane("l1" + (i + 1), id, Pos.CENTER, null, fill), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), idVentaCredito, Pos.CENTER, null, fill), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), fecha, Pos.CENTER, null, fill), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), estado, Pos.CENTER, null, color), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), monto, Pos.CENTER, null, fill), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), observacion, Pos.CENTER, null, fill), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), "", Pos.CENTER, pagar, fill), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), "", Pos.CENTER, quitar, fill), 7, (i + 1));
        }
    }

    private void createHeadTableDetalleVenta() {
        gpDetalle.add(addHeadGridPane("N°"), 0, 0);
        gpDetalle.add(addHeadGridPane("Descripción"), 1, 0);
        gpDetalle.add(addHeadGridPane("Cantidad"), 2, 0);
        gpDetalle.add(addHeadGridPane("Bonificación"), 3, 0);
        gpDetalle.add(addHeadGridPane("Medida"), 4, 0);
        gpDetalle.add(addHeadGridPane("Impuesto"), 5, 0);
        gpDetalle.add(addHeadGridPane("Precio"), 6, 0);
        gpDetalle.add(addHeadGridPane("Descuento"), 7, 0);
        gpDetalle.add(addHeadGridPane("Importe"), 8, 0);
    }

    private void createBodyTableDetalleVenta(ArrayList<SuministroTB> arrList) {
        for (int i = 0; i < arrList.size(); i++) {
            SuministroTB suministroTB = arrList.get(i);
            String descripcion = suministroTB.getClave() + "\n" + suministroTB.getNombreMarca();
            String cantidad = Tools.roundingValue(suministroTB.getCantidad(), 2);
            String bonificacion = Tools.roundingValue(suministroTB.getBonificacion(), 2);
            String unidadCompra = suministroTB.getUnidadCompraName();
            String impuesto = suministroTB.getImpuestoTB().getNombreImpuesto();
            String precio = Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2);
            String descuento = Tools.roundingValue(suministroTB.getDescuento(), 2);
            String total = Tools.roundingValue(
                    suministroTB.getPrecioVentaGeneral() * (suministroTB.getCantidad() - suministroTB.getDescuento()),
                    2);

            gpDetalle.add(addElementGridPaneLabel("l1" + (i + 1), suministroTB.getId() + "", Pos.CENTER), 0, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l2" + (i + 1), descripcion, Pos.CENTER_LEFT), 1, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l3" + (i + 1), cantidad, Pos.CENTER_RIGHT), 2, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l4" + (i + 1), bonificacion, Pos.CENTER_RIGHT), 3, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l5" + (i + 1), unidadCompra, Pos.CENTER_LEFT), 4, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l6" + (i + 1), impuesto, Pos.CENTER_RIGHT), 5, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l7" + (i + 1), precio, Pos.CENTER_RIGHT), 6, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l8" + (i + 1), descuento, Pos.CENTER_RIGHT), 7, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l9" + (i + 1), total, Pos.CENTER_RIGHT), 8, (i + 1));
        }
    }

    private Label addHeadGridPane(String nombre) {
        Label label = new Label(nombre);
        label.setTextFill(Color.web("#FFFFFF"));
        label.getStyleClass().add("labelRoboto13");
        label.setStyle(
                "-fx-background-color:  #020203;-fx-padding:  0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight:100;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPane(String id, String nombre, Pos pos, Node node, String color) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setGraphic(node);
        label.setTextFill(Color.web(color));
        label.setStyle(
                "-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPaneLabel(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle(
                "-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void borrarContenido() {
        lblCliente.setText("-");
        lblTelefonoCelular.setText("-");
        lblDireccion.setText("-");
        lblEmail.setText("-");
        lblComprobante.setText("-");
        lblEstado.setText("-");
        lblObservacion.setText("-");

        lblMontoTotal.setText("0.00");
        lblMontoPagado.setText("0.00");
        lblDiferencia.setText("0.00");
    }

    /*
     * Modal para registrar un cobro o amortización
     */
    private void onEventGenerarCobro() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_GENERAR_COBRO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxGenerarCobroController controller = fXMLLoader.getController();
            // controller.setInitLoadVentaAbono(ventaTB);
            controller.setInitVentaAbonarController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Generar Cobro", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShowing(w -> controller.loadInitComponents(ventaTB));
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller Cuentas por cobrar" + ex.getLocalizedMessage());
        }
    }

    /*
     * Modal para editar un cobro o amortización
     */
    private void onEventActualizarCobro(String idVentaCredito, double monto) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_GENERAR_COBRO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxGenerarCobroController controller = fXMLLoader.getController();
            // controller.setInitLoadVentaAbono(ventaTB, idVentaCredito, monto);
            controller.setInitVentaAbonarController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Generar Cobro", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShowing(w -> controller.loadInitComponents(ventaTB));
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller Cuentas por Cobrar" + ex.getLocalizedMessage());
        }
    }

    public void openModalImpresion(String idVenta, String idVentaCredito) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketCuentaPorCobrar(controller.getApWindow());
            controller.setIdVenta(idVenta);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> {
                fxPrincipalController.closeFondoModal();
                loadData(ventaTB.getIdVenta());
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller Cuentas por Cobrar" + ex.getLocalizedMessage());
        }
    }

    private void onEventAnular(String idVentaCredito) {
        short value = Tools.AlertMessageConfirmation(apWindow, "Cuentas Por Cobrar", "¿Está seguro de continuar?");
        if (value == 1) {
            String result = VentaADO.removerIngreso(ventaTB.getIdVenta(), idVentaCredito);
            if (result.equalsIgnoreCase("removed")) {
                Tools.AlertMessageInformation(apWindow, "Cuentas Por Cobrar", "Se completo correctamento el proceso.");
                loadData(ventaTB.getIdVenta());
            } else {
                Tools.AlertMessageWarning(apWindow, "Cuentas Por Cobrar", result);
            }
        }
    }

    private void onEventCloseView() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(cuentasPorCobrarController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(cuentasPorCobrarController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(cuentasPorCobrarController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(cuentasPorCobrarController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(cuentasPorCobrarController.getVbWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        onEventCloseView();
    }

    @FXML
    private void onActionCobrar(ActionEvent event) {
        onEventGenerarCobro();
    }

    @FXML
    private void onKeyPressedCobrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGenerarCobro();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        fxOpcionesImprimirController.getTicketCuentasPorCobrar().mostrarReporte(ventaTB.getIdVenta());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketCuentasPorCobrar().mostrarReporte(ventaTB.getIdVenta());
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        fxOpcionesImprimirController.getTicketCuentasPorCobrar().imprimir(ventaTB.getIdVenta());
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketCuentasPorCobrar().imprimir(ventaTB.getIdVenta());
        }
    }

    public void setInitCuentasPorCobrar(FxPrincipalController fxPrincipalController,
            FxCuentasPorCobrarController cuentasPorCobrarController) {
        this.fxPrincipalController = fxPrincipalController;
        this.cuentasPorCobrarController = cuentasPorCobrarController;
    }

}
