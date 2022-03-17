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
import model.CompraADO;
import model.CompraTB;
import model.DetalleCompraTB;

public class FxCuentasPorPagarVisualizarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private Label lblProveedor;
    @FXML
    private Label lblComprobante;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblObservacion;
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
    private Label lblMontoTotal;
    @FXML
    private GridPane gpDetalle;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private FxCuentasPorPagarController cuentasPorPagarController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private CompraTB compraTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCuentaPorPagar(apWindow);
    }

    public void loadTableCompraCredito(String idCompra) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CompraADO.Obtener_Compra_ById_For_Credito(idCompra);
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
            if (object instanceof CompraTB) {
                compraTB = (CompraTB) object;
                lblProveedor.setText(compraTB.getProveedorTB().getNumeroDocumento() + " - " + compraTB.getProveedorTB().getRazonSocial());
                lblTelefonoCelular.setText(compraTB.getProveedorTB().getCelular());
                lblDireccion.setText(compraTB.getProveedorTB().getDireccion());
                lblEmail.setText(compraTB.getProveedorTB().getEmail());
                lblComprobante.setText(compraTB.getSerie() + " - " + compraTB.getNumeracion());
                lblEstado.setText(compraTB.getEstadoName());
                lblObservacion.setText(compraTB.getObservaciones());
                lblMontoTotal.setText(Tools.roundingValue(compraTB.getMontoTotal(), 2));
                lblMontoPagado.setText(Tools.roundingValue(compraTB.getMontoPagado(), 2));
                lblDiferencia.setText(Tools.roundingValue(compraTB.getMontoRestante(), 2));
                compraTB.getCompraCreditoTBs().forEach(vc -> {
                    vc.getBtnImprimir().setOnAction(event -> {
                        openModalImpresion(idCompra);
                    });
                    vc.getBtnImprimir().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            openModalImpresion(idCompra);
                        }
                    });
                });

                fillVentasDetalleTable();
                fillProductosTable(compraTB.getDetalleCompraTBs());
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
        for (int i = 0; i < compraTB.getCompraCreditoTBs().size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), compraTB.getCompraCreditoTBs().get(i).getId() + "", Pos.CENTER, null), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), compraTB.getCompraCreditoTBs().get(i).getIdCompraCredito(), Pos.CENTER, null), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), compraTB.getCompraCreditoTBs().get(i).getFechaPago(), Pos.CENTER, null), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), "Completado", Pos.CENTER, null), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(compraTB.getCompraCreditoTBs().get(i).getMonto(), 2), Pos.CENTER, null), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), compraTB.getCompraCreditoTBs().get(i).getObservacion(), Pos.CENTER, null), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), "", Pos.CENTER, compraTB.getCompraCreditoTBs().get(i).getBtnImprimir()), 6, (i + 1));
        }
    }

    private void fillProductosTable(ArrayList<DetalleCompraTB> arrList) {
        for (int i = 0; i < arrList.size(); i++) {
            gpDetalle.add(addElementGridPane("l1" + (i + 1), arrList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpDetalle.add(addElementGridPane("l2" + (i + 1), arrList.get(i).getSuministroTB().getClave() + "\n" + arrList.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpDetalle.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(arrList.get(i).getPrecioCompra(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpDetalle.add(addElementGridPane("l4" + (i + 1), Tools.roundingValue(arrList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 3, (i + 1));
            gpDetalle.add(addElementGridPane("l5" + (i + 1), arrList.get(i).getImpuestoTB().getNombre(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpDetalle.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(arrList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpDetalle.add(addElementGridPane("l7" + (i + 1), arrList.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_RIGHT), 6, (i + 1));
            gpDetalle.add(addElementGridPane("l8" + (i + 1), Tools.roundingValue(arrList.get(i).getCantidad() * arrList.get(i).getPrecioCompra() - arrList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
    }

    private Label addElementGridPane(String id, String nombre, Pos pos, Node node) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setGraphic(node);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
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
            URL url = getClass().getResource(FilesRouters.FX_AMARTIZAR_PAGOS);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxAmortizarPagosController controller = fXMLLoader.getController();
            controller.setInitValues(compraTB);
            controller.setInitAmortizarPagosController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Generar Pago", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    public void openModalImpresion(String idCompra) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketCuentaPorPagar(controller.getApWindow());
            controller.setIdCompra(idCompra);          
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
        AnchorPane.setLeftAnchor(cuentasPorPagarController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(cuentasPorPagarController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(cuentasPorPagarController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(cuentasPorPagarController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(cuentasPorPagarController.getVbWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeView();
    }

    @FXML
    private void onKeyPressedAmortizar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAmortizar();
        }
    }

    @FXML
    private void onActionAmortizar(ActionEvent event) {
        onEventAmortizar();
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (compraTB != null) {
                fxOpcionesImprimirController.getTicketCuentasPorPagar().mostrarReporte(compraTB.getIdCompra());
            }
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        if (compraTB != null) {
            fxOpcionesImprimirController.getTicketCuentasPorPagar().mostrarReporte(compraTB.getIdCompra());
        }
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (compraTB != null) {
                fxOpcionesImprimirController.getTicketCuentasPorPagar().imprimir(compraTB.getIdCompra());
            }
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        if (compraTB != null) {
            fxOpcionesImprimirController.getTicketCuentasPorPagar().imprimir(compraTB.getIdCompra());
        }
    }

    public void setInitCuentasPorPagar(FxPrincipalController fxPrincipalController, FxCuentasPorPagarController cuentasPorPagarController) {
        this.fxPrincipalController = fxPrincipalController;
        this.cuentasPorPagarController = cuentasPorPagarController;
    }

}
