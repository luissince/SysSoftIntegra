package controller.consultas.guiaremision;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import model.GuiaRemisionDetalleTB;
import model.GuiaRemisionTB;
import service.GuiaRemisionADO;

public class FxGuiaRemisionDetalleController implements Initializable {

    @FXML
    private ScrollPane spWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Button btnReporte;
    @FXML
    private Button btnTicket;
    @FXML
    private Text lblNumero;
    @FXML
    private Text lblFechaRegistro;
    @FXML
    private Text lblHoraRegistro;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lblClienteTelefonoCelular;
    @FXML
    private Text lblCorreoElectronico;
    @FXML
    private Text lblDireccion;
    @FXML
    private Text lblMotivoTraslado;
    @FXML
    private Text lblModalidadTraslado;
    @FXML
    private Text lblPesoBultos;
    @FXML
    private Text lblConducto;
    @FXML
    private Text lblNumeroPlaca;
    @FXML
    private Text lblNumeroLicencia;
    @FXML
    private Text lblDireccionPartida;
    @FXML
    private Text lblDireccionLlegada;
    @FXML
    private Text lblTipoComprobante;
    @FXML
    private Text lblSerieNumeracion;
    @FXML
    private GridPane gpList;

    private FxGuiaRemisionRealizadasController guiaRemisionRealizadasController;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController opcionesImprimirController;

    private GuiaRemisionTB guiaRemisionTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        opcionesImprimirController = new FxOpcionesImprimirController();
        opcionesImprimirController.loadComponents();
        opcionesImprimirController.loadTicketGuiaRemision(spWindow);
    }

    public void setInitComponents(String idGuiaRemision) {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return GuiaRemisionADO.obtenerGuiaRemisionById(idGuiaRemision);
            }
        };
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof GuiaRemisionTB) {
                guiaRemisionTB = (GuiaRemisionTB) object;
                lblNumero.setText(guiaRemisionTB.getSerie() + "-" +
                        Tools.formatNumber(guiaRemisionTB.getNumeracion()));
                lblFechaRegistro.setText(guiaRemisionTB.getFechaRegistro());
                lblHoraRegistro.setText(guiaRemisionTB.getHoraRegistro());

                lblCliente.setText(guiaRemisionTB.getClienteTB().getNumeroDocumento() +
                        guiaRemisionTB.getClienteTB().getInformacion());
                lblClienteTelefonoCelular.setText(guiaRemisionTB.getClienteTB().getCelular());
                lblCorreoElectronico.setText(guiaRemisionTB.getClienteTB().getEmail());
                lblDireccion.setText(guiaRemisionTB.getClienteTB().getDireccion());

                lblMotivoTraslado.setText(guiaRemisionTB.getDetalleMotivoTrasladoTB().getNombre());
                lblModalidadTraslado.setText(guiaRemisionTB.getModalidadTrasladoTB().getNombre());

                lblPesoBultos.setText(
                        guiaRemisionTB.getPesoCarga() + " " + guiaRemisionTB.getDetallePesoCargaTB().getIdAuxiliar());

                if (guiaRemisionTB.getConductorTB() != null) {
                    lblConducto.setText(guiaRemisionTB.getConductorTB().getInformacion());
                    lblNumeroLicencia.setText(guiaRemisionTB.getConductorTB().getLicenciaConducir());
                }

                if (guiaRemisionTB.getVehiculoTB() != null) {
                    lblNumeroPlaca.setText(guiaRemisionTB.getVehiculoTB().getNumeroPlaca());
                }

                lblDireccionPartida.setText(guiaRemisionTB.getDireccionPartida());
                lblDireccionLlegada.setText(guiaRemisionTB.getDireccionLlegada());

                lblTipoComprobante.setText(guiaRemisionTB.getVentaTB().getTipoDocumentoTB().getNombre());
                lblSerieNumeracion.setText(
                        guiaRemisionTB.getVentaTB().getSerie() + "-"
                                + Tools.formatNumber(guiaRemisionTB.getVentaTB().getNumeracion()));

                fillGuiaDetalleTable(guiaRemisionTB.getGuiaRemisionDetalle());
                lblLoad.setVisible(false);
            } else {
                btnReporte.setDisable(true);
                btnTicket.setDisable(false);
                lblLoad.setVisible(false);
            }
        });
        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void fillGuiaDetalleTable(ObservableList<GuiaRemisionDetalleTB> empList) {
        for (int i = 0; i < empList.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), "" + (i + 1), Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), empList.get(i).getDescripcion(), Pos.CENTER_LEFT), 1,
                    (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), empList.get(i).getCodigo(), Pos.CENTER_LEFT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), empList.get(i).getUnidad(), Pos.CENTER_RIGHT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(empList.get(i).getCantidad(), 2),
                    Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(empList.get(i).getPeso(), 2),
                    Pos.CENTER_RIGHT), 5, (i + 1));
        }
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
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

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        fxPrincipalController.getVbContent().getChildren().remove(spWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(guiaRemisionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(guiaRemisionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(guiaRemisionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(guiaRemisionRealizadasController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(guiaRemisionRealizadasController.getHbWindow());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (guiaRemisionTB != null) {
                opcionesImprimirController.getTicketGuiaRemision().mostrarReporte(guiaRemisionTB.getIdGuiaRemision());
            }
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        if (guiaRemisionTB != null) {
            opcionesImprimirController.getTicketGuiaRemision().mostrarReporte(guiaRemisionTB.getIdGuiaRemision());
        }
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (guiaRemisionTB != null) {
                opcionesImprimirController.getTicketGuiaRemision().imprimir(guiaRemisionTB.getIdGuiaRemision());
            }
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        if (guiaRemisionTB != null) {
            opcionesImprimirController.getTicketGuiaRemision().imprimir(guiaRemisionTB.getIdGuiaRemision());
        }
    }

    public void setInitGuiaRemisionRealizadasController(
            FxGuiaRemisionRealizadasController guiaRemisionRealizadasController,
            FxPrincipalController fxPrincipalController) {
        this.guiaRemisionRealizadasController = guiaRemisionRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
