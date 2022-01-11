package controller.consultas.pagar;

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
import javafx.stage.Stage;
import model.SuministroTB;
import model.VentaADO;
import model.VentaTB;

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

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return VentaADO.Listar_Ventas_Detalle_Credito_ById(idVenta);
            }
        };
        task.setOnScheduled(w -> {
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                closeView();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    closeView();
                }
                event.consume();
            });
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof VentaTB) {
                ventaTB = (VentaTB) object;
                lblCliente.setText(ventaTB.getClienteTB().getNumeroDocumento() + " - " + ventaTB.getClienteTB().getInformacion());
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
                    vc.getBtnImprimir().setOnAction(event -> openModalImpresion(idVenta, vc.getIdVentaCredito()));
                    vc.getBtnImprimir().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            openModalImpresion(idVenta, vc.getIdVentaCredito());
                        }
                    });
                });

                fillVentasDetalleTable();
                fillVentasDetalleTable(ventaTB.getSuministroTBs());
                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeView();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeView();
                    }
                    event.consume();
                });
            }

        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void fillVentasDetalleTable() {
        for (int i = 0; i < ventaTB.getVentaCreditoTBs().size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), ventaTB.getVentaCreditoTBs().get(i).getId() + "", Pos.CENTER, null), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), ventaTB.getVentaCreditoTBs().get(i).getIdVentaCredito(), Pos.CENTER, null), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), ventaTB.getVentaCreditoTBs().get(i).getFechaPago(), Pos.CENTER, null), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), "Completado", Pos.CENTER, null), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(ventaTB.getVentaCreditoTBs().get(i).getMonto(), 2), Pos.CENTER, null), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), ventaTB.getVentaCreditoTBs().get(i).getObservacion(), Pos.CENTER, null), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), "", Pos.CENTER, ventaTB.getVentaCreditoTBs().get(i).getBtnImprimir()), 6, (i + 1));
        }
    }

    private void fillVentasDetalleTable(ArrayList<SuministroTB> arrList) {
        for (int i = 0; i < arrList.size(); i++) {
            gpDetalle.add(addElementGridPaneLabel("l1" + (i + 1), arrList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l2" + (i + 1), arrList.get(i).getClave() + "\n" + arrList.get(i).getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l3" + (i + 1), Tools.roundingValue(arrList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l4" + (i + 1), arrList.get(i).getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l5" + (i + 1), arrList.get(i).getImpuestoTB().getNombreImpuesto(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l6" + (i + 1), Tools.roundingValue(arrList.get(i).getPrecioVentaGeneral(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l7" + (i + 1), Tools.roundingValue(arrList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpDetalle.add(addElementGridPaneLabel("l8" + (i + 1), Tools.roundingValue(arrList.get(i).getPrecioVentaGeneral() * (arrList.get(i).getCantidad() - arrList.get(i).getDescuento()), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
    }

    private Label addElementGridPane(String id, String nombre, Pos pos, Node node) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setGraphic(node);
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

    private void onEventAmortizar() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_ABONO_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxVentaAbonoProcesoController controller = fXMLLoader.getController();
            controller.setInitLoadVentaAbono(ventaTB);
            controller.setInitVentaAbonarController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Generar Cobro", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    public void openModalImpresion(String idVenta, String idVentaCredito) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketCuentaPorCobrar(controller.getApWindow());
            controller.setIdVenta(idVenta);
            controller.setIdVentaCredito(idVentaCredito);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void closeView() {
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
        closeView();
    }

    @FXML
    private void onActionCobrar(ActionEvent event) {
        onEventAmortizar();
    }

    @FXML
    private void onKeyPressedCobrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAmortizar();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        if (ventaTB != null) {
            fxOpcionesImprimirController.getTicketCuentasPorCobrar().mostrarReporte(ventaTB.getIdVenta(), "");
        }
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (ventaTB != null) {
                fxOpcionesImprimirController.getTicketCuentasPorCobrar().mostrarReporte(ventaTB.getIdVenta(), "");
            }
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        if (ventaTB != null) {
            fxOpcionesImprimirController.getTicketCuentasPorCobrar().imprimir(ventaTB.getIdVenta(), "");
        }
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (ventaTB != null) {
                fxOpcionesImprimirController.getTicketCuentasPorCobrar().imprimir(ventaTB.getIdVenta(), "");
            }
        }
    }

    public void setInitCuentasPorCobrar(FxPrincipalController fxPrincipalController, FxCuentasPorCobrarController cuentasPorCobrarController) {
        this.fxPrincipalController = fxPrincipalController;
        this.cuentasPorCobrarController = cuentasPorCobrarController;
    }

}
