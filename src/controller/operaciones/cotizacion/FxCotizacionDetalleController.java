package controller.operaciones.cotizacion;

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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.CotizacionADO;
import model.CotizacionTB;
import model.SuministroTB;

public class FxCotizacionDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblNumero;
    @FXML
    private Text lblFechaVenta;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lblCelularTelefono;
    @FXML
    private Text lbCorreoElectronico;
    @FXML
    private Text lbDireccion;
    @FXML
    private Text lblObservaciones;
    @FXML
    private Text lblVendedor;
    @FXML
    private GridPane gpList;
    @FXML
    private ScrollPane spBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblImporteBruto;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubImporteNeto;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblImporteNeto;

    private FxCotizacionRealizadasController cotizacionRealizadasController;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private CotizacionTB cotizacionTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCotizacion(apWindow);
    }

    public void setInitComponents(String idCotizacion) {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return CotizacionADO.CargarCotizacionById(idCotizacion);
            }
        };
        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof CotizacionTB) {
                cotizacionTB = (CotizacionTB) object;
                lblNumero.setText("N° " + cotizacionTB.getIdCotizacion());
                lblFechaVenta.setText(cotizacionTB.getFechaCotizacion());
                lblCliente.setText(cotizacionTB.getClienteTB().getInformacion());
                lblCelularTelefono.setText("TEL.: " + cotizacionTB.getClienteTB().getTelefono() + " CEL.: " + cotizacionTB.getClienteTB().getCelular());
                lbCorreoElectronico.setText(cotizacionTB.getClienteTB().getEmail());
                lbDireccion.setText(cotizacionTB.getClienteTB().getDireccion());
                lblObservaciones.setText(cotizacionTB.getObservaciones());
                lblVendedor.setText(cotizacionTB.getEmpleadoTB().getApellidos() + " " + cotizacionTB.getEmpleadoTB().getNombres());

                ObservableList<SuministroTB> cotizacionTBs = cotizacionTB.getDetalleSuministroTBs();
                fillVentasDetalleTable(cotizacionTBs, cotizacionTB.getMonedaTB().getSimbolo());

                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
            }
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void fillVentasDetalleTable(ObservableList<SuministroTB> empList, String simbolo) {       
        for (int i = 0; i < empList.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), empList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), empList.get(i).getClave() + "\n" + empList.get(i).getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(empList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), empList.get(i).getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(empList.get(i).getImpuestoTB().getValor(), 2) + "%", Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), simbolo + "" + Tools.roundingValue(empList.get(i).getPrecioVentaGeneral(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), "-" + Tools.roundingValue(empList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), simbolo + "" + Tools.roundingValue(empList.get(i).getImporteNeto(), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calculateTotales(simbolo);
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
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

    public void calculateTotales(String simbolo) {
        lblImporteBruto.setText(Tools.roundingValue(cotizacionTB.getImporteBruto(), 2));
        lblDescuento.setText(Tools.roundingValue(cotizacionTB.getDescuento(), 2));
        lblSubImporteNeto.setText(Tools.roundingValue(cotizacionTB.getSubImporteNeto(), 2));
        lblImpuesto.setText(Tools.roundingValue(cotizacionTB.getImpuesto(), 2));
        lblImporteNeto.setText(Tools.roundingValue(cotizacionTB.getImporteNeto(), 2));
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(cotizacionRealizadasController.getHbWindow());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketCotizacion().mostrarReporte(cotizacionTB.getIdCotizacion());
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        fxOpcionesImprimirController.getTicketCotizacion().mostrarReporte(cotizacionTB.getIdCotizacion());
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxOpcionesImprimirController.getTicketCotizacion().imprimir(cotizacionTB.getIdCotizacion());
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        fxOpcionesImprimirController.getTicketCotizacion().imprimir(cotizacionTB.getIdCotizacion());
    }

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            fxPrincipalController.getVbContent().getChildren().remove(apWindow);
            fxPrincipalController.getVbContent().getChildren().clear();
            AnchorPane.setLeftAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
            AnchorPane.setTopAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
            AnchorPane.setRightAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
            AnchorPane.setBottomAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
            fxPrincipalController.getVbContent().getChildren().add(cotizacionRealizadasController.getHbWindow());
        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(cotizacionRealizadasController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(cotizacionRealizadasController.getHbWindow());
    }

    public void setInitCotizacionesRealizadasController(FxCotizacionRealizadasController cotizacionRealizadasController, FxPrincipalController fxPrincipalController) {
        this.cotizacionRealizadasController = cotizacionRealizadasController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
