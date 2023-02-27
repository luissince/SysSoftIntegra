package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.ProduccionTB;
import model.SuministroTB;
import service.ProduccionADO;

public class FxProducirVisualizarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private Label lblTitulo;
    @FXML
    private Text lblCantidadProducir;
    @FXML
    private Text lblProducto;
    @FXML
    private Text lblEncargado;
    @FXML
    private Text lblFechaCreacion;
    @FXML
    private Text lblFechaProduccion;
    @FXML
    private Text lblEstado;
    @FXML
    private Text lblDuracion;
    @FXML
    private GridPane gpListInsumos;
    @FXML
    private Label lblCostoTotal;
    @FXML
    private Label lblCostoAdicional;
    @FXML
    private Label lblCosto;
    @FXML
    private Label lblTotal;
    @FXML
    private GridPane gpListMerma;
    @FXML
    private Label lblTotalMerma;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblObservacion;
    @FXML
    private Label lblCantidadTotal;
    @FXML
    private Label lblCostoProductoTotal;
    @FXML
    private Label lblCostoFinal;

    private FxProducirController fxProducirController;

    private FxPrincipalController fxPrincipalController;

    private ProduccionTB produccionTB;

    private double cantidadTotal = 0;

    private double costoProductoTotal = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setInitComponents(String idProducir) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return ProduccionADO.Obtener_Produccion_ById(idProducir);
            }
        };

        task.setOnScheduled(w -> {
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                closeWindow();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    closeWindow();
                }
                event.consume();
            });
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof ProduccionTB) {
                produccionTB = (ProduccionTB) object;
                lblTitulo.setText("ORDEN DE PRODUCCIÓN - " + idProducir);
                lblProducto.setText(produccionTB.getSuministroTB().getClave() + " - " + produccionTB.getSuministroTB().getNombreMarca());
                lblCantidadProducir.setText(produccionTB.isCantidadVariable() ? "VARIABLE" : Tools.roundingValue(produccionTB.getCantidad(), 2) + " " + produccionTB.getSuministroTB().getUnidadCompraName());
                lblEncargado.setText(produccionTB.getEmpleadoTB().getApellidos() + ", " + produccionTB.getEmpleadoTB().getNombres());
                lblFechaCreacion.setText(produccionTB.getFechaRegistro());
                lblFechaProduccion.setText(produccionTB.getFechaInicio());

                switch (produccionTB.getEstado()) {
                    case 1:
                        lblEstado.setFill(Color.web("#055bd3"));
                        lblEstado.setText("COMPLETADO");
                        break;
                    case 2:
                        lblEstado.setFill(Color.web("#cc6a00"));
                        lblEstado.setText("EN PRODUCIÓN");
                        break;
                    default:
                        lblEstado.setFill(Color.web("#e70606"));
                        lblEstado.setText("ANULADO");
                        break;
                }
                lblDuracion.setText(((produccionTB.getDias() == 1 ? produccionTB.getDias() + " día" : produccionTB.getDias() + " días ") + produccionTB.getHoras() + ":" + produccionTB.getMinutos() + ":00"));
                lblObservacion.setText(produccionTB.getDescripcion());
                fillTableDetalleInsumos(produccionTB.getSuministroInsumos());
                if (produccionTB.getMermaTB() != null) {
                    fillTableDetalleMerma(produccionTB.getMermaTB().getSuministroTBs());
                }
                lblCantidadTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cantidadTotal, 2));
                lblCostoProductoTotal.setText(produccionTB.isCantidadVariable() ? "0" : Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(costoProductoTotal / produccionTB.getCantidad(), 2));
                lblCostoFinal.setText(produccionTB.isCantidadVariable() ? "0" : Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cantidadTotal * (costoProductoTotal / produccionTB.getCantidad()), 2));
               
                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeWindow();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeWindow();
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

    private void fillTableDetalleInsumos(ArrayList<SuministroTB> suministroTBs) {
        double costoTotal = 0;
        for (int i = 0; i < suministroTBs.size(); i++) {
            gpListInsumos.add(addElementGridPane("l1" + (i + 1), suministroTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpListInsumos.add(addElementGridPane("l2" + (i + 1), suministroTBs.get(i).getClave() + "\n" + suministroTBs.get(i).getNombreMarca() + "", Pos.CENTER_LEFT), 1, (i + 1));
            gpListInsumos.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCantidad(), 2) + "", Pos.CENTER), 2, (i + 1));
            gpListInsumos.add(addElementGridPane("l4" + (i + 1), suministroTBs.get(i).getUnidadCompraName() + "", Pos.CENTER_LEFT), 3, (i + 1));
            gpListInsumos.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCostoCompra(), 2) + "", Pos.CENTER), 4, (i + 1));
            gpListInsumos.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCantidad() * suministroTBs.get(i).getCostoCompra(), 2) + "", Pos.CENTER), 5, (i + 1));
            costoTotal += suministroTBs.get(i).getCantidad() * suministroTBs.get(i).getCostoCompra();
        }

        cantidadTotal += produccionTB.getCantidad();
        costoProductoTotal = costoTotal + produccionTB.getCostoAdicional();
        double total = costoTotal + produccionTB.getCostoAdicional();
        lblCostoTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(costoTotal, 2));
        lblCostoAdicional.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(produccionTB.getCostoAdicional(), 2));
        lblCosto.setText(produccionTB.isCantidadVariable() ? "0" : Session.MONEDA_SIMBOLO + " " + (total / produccionTB.getCantidad()));
        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(total, 2));
    }

    private void fillTableDetalleMerma(ArrayList<SuministroTB> suministroTBs) {
        double costoTotal = 0;
        for (int i = 0; i < suministroTBs.size(); i++) {
            gpListMerma.add(addElementGridPane("l1" + (i + 1), suministroTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpListMerma.add(addElementGridPane("l2" + (i + 1), suministroTBs.get(i).getClave() + "\n" + suministroTBs.get(i).getNombreMarca() + "", Pos.CENTER_LEFT), 1, (i + 1));
            gpListMerma.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCantidad(), 2) + "", Pos.CENTER), 2, (i + 1));
            gpListMerma.add(addElementGridPane("l4" + (i + 1), suministroTBs.get(i).getUnidadCompraName() + "", Pos.CENTER_LEFT), 3, (i + 1));
            gpListMerma.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCostoCompra(), 2) + "", Pos.CENTER), 4, (i + 1));
            gpListMerma.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCantidad() * suministroTBs.get(i).getCostoCompra(), 2) + "", Pos.CENTER), 5, (i + 1));
            costoTotal += suministroTBs.get(i).getCantidad() * suministroTBs.get(i).getCostoCompra();
            cantidadTotal -= suministroTBs.get(i).getCantidad();
        }
        lblTotalMerma.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(costoTotal, 2));
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

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(fxProducirController.getWindow(), 0d);
        AnchorPane.setTopAnchor(fxProducirController.getWindow(), 0d);
        AnchorPane.setRightAnchor(fxProducirController.getWindow(), 0d);
        AnchorPane.setBottomAnchor(fxProducirController.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(fxProducirController.getWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeWindow();
    }

    public void setInitProducirController(FxProducirController fxProducirController, FxPrincipalController fxPrincipalController) {
        this.fxProducirController = fxProducirController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
