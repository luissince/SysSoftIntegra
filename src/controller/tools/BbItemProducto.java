package controller.tools;

import controller.operaciones.venta.FxVentaEstructuraNuevoController;
import controller.posterminal.venta.FxPostVentaEstructuraNuevoController;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.SuministroTB;

public class BbItemProducto extends HBox {

    private final ListView<BbItemProducto> lvProductoAgregados;

    private FxVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private FxPostVentaEstructuraNuevoController postVentaEstructuraNuevoController;

    private SuministroTB suministroTB;

    public BbItemProducto(SuministroTB suministroTB, ListView<BbItemProducto> lvProductoAgregados, FxVentaEstructuraNuevoController ventaEstructuraNuevoController) {
        this.suministroTB = suministroTB;
        this.lvProductoAgregados = lvProductoAgregados;
        this.ventaEstructuraNuevoController = ventaEstructuraNuevoController;
    }

    public BbItemProducto(SuministroTB suministroTB, ListView<BbItemProducto> lvProductoAgregados, FxPostVentaEstructuraNuevoController postVentaEstructuraNuevoController) {
        this.suministroTB = suministroTB;
        this.lvProductoAgregados = lvProductoAgregados;
        this.postVentaEstructuraNuevoController = postVentaEstructuraNuevoController;
    }

    public void addElementListView() {
        setMinWidth(Control.USE_PREF_SIZE);
        setPrefWidth(Control.USE_COMPUTED_SIZE);
        maxWidth(Control.USE_PREF_SIZE);

        VBox vbLeft = new VBox();
        HBox.setHgrow(vbLeft, Priority.ALWAYS);
        vbLeft.setAlignment(Pos.CENTER_LEFT);
        vbLeft.setMaxWidth(Control.USE_PREF_SIZE);
        Label lblProducto = new Label(suministroTB.getNombreMarca() + (suministroTB.getBonificacion() <= 0 ? "" : "(BONIFICACIÓN: " + suministroTB.getBonificacion() + ")"));
        lblProducto.getStyleClass().add("labelRoboto14");
        lblProducto.setTextFill(Color.web("#020203"));
        lblProducto.setWrapText(true);
        lblProducto.setPrefWidth(200);
        vbLeft.getChildren().add(lblProducto);
        Label lblPrecio = new Label(Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2));
        lblPrecio.getStyleClass().add("labelRobotoBold14");
        lblPrecio.setTextFill(Color.web("#020203"));
        vbLeft.getChildren().add(lblPrecio);

        HBox hbCenter = new HBox();
        hbCenter.setStyle("-fx-spacing: 0.4166666666666667em;");
        hbCenter.setAlignment(Pos.CENTER);
        HBox.setHgrow(hbCenter, Priority.ALWAYS);
//        Button btnMenos = new Button();
//        ImageView ivMenos = new ImageView(new Image("/view/image/remove-item.png"));
//        ivMenos.setFitWidth(24);
//        ivMenos.setFitHeight(24);
//        btnMenos.setGraphic(ivMenos);
//        btnMenos.getStyleClass().add("buttonBorder");
//        btnMenos.setOnAction(a -> {
//
//        });
//        btnMenos.setOnKeyPressed(a -> {
//            if (a.getCode() == KeyCode.ENTER) {
//
//            }
//        });
        Label lblCantidad = new Label(Tools.roundingValue(suministroTB.getCantidad(), 2));
        lblCantidad.getStyleClass().add("labelRobotoMedium14");
        lblCantidad.setTextFill(Color.web("#020203"));
//        Button btnMas = new Button();
//        ImageView ivMas = new ImageView(new Image("/view/image/plus.png"));
//        ivMas.setFitWidth(24);
//        ivMas.setFitHeight(24);
//        btnMas.setGraphic(ivMas);
//        btnMas.getStyleClass().add("buttonBorder");
//        btnMas.setOnAction(a -> {
//            suministroTB.setCantidad(suministroTB.getCantidad() + 1);
//            double porcentajeRestante = suministroTB.getPrecioVentaGeneralUnico() * (suministroTB.getDescuento() / 100.00);
//
//            suministroTB.setDescuentoCalculado(porcentajeRestante);
//            suministroTB.setDescuentoSumado(porcentajeRestante * suministroTB.getCantidad());
//
//            double impuesto = Tools.calculateTax(suministroTB.getImpuestoValor(), suministroTB.getPrecioVentaGeneralReal());
//            suministroTB.setImpuestoSumado(suministroTB.getCantidad() * impuesto);
//
//            suministroTB.setSubImporte(suministroTB.getPrecioVentaGeneralUnico() * suministroTB.getCantidad());
//            suministroTB.setSubImporteDescuento(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());
//            suministroTB.setTotalImporte(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());           
//
//            ventaEstructuraNuevoController.calculateTotales();
//        });
//        btnMas.setOnKeyPressed(a -> {
//            if (a.getCode() == KeyCode.ENTER) {
//
//            }
//        });
        hbCenter.getChildren().addAll(lblCantidad);

        VBox vbRight = new VBox();
//            vbRight.setPrefWidth(165);
        HBox.setHgrow(vbRight, Priority.ALWAYS);
        vbRight.setStyle("-fx-spacing: 0.4166666666666667em;");
        vbRight.setAlignment(Pos.CENTER_RIGHT);
        Label lblImporte = new Label(Tools.roundingValue(suministroTB.getPrecioVentaGeneral() * suministroTB.getCantidad(), 2));
        lblImporte.getStyleClass().add("labelRoboto14");
        lblImporte.setTextFill(Color.web("#020203"));
        Button btnRemoved = new Button();
        ImageView ivRemoved = new ImageView(new Image("/view/image/remove.png"));
        ivRemoved.setFitWidth(24);
        ivRemoved.setFitHeight(24);
        btnRemoved.setGraphic(ivRemoved);
        btnRemoved.getStyleClass().add("buttonDark");
        btnRemoved.setOnAction((ActionEvent a) -> {
            lvProductoAgregados.getItems().remove(this);
            if (ventaEstructuraNuevoController != null) {
                ventaEstructuraNuevoController.calculateTotales();
            } else {
                postVentaEstructuraNuevoController.calculateTotales();
            }

        });
        btnRemoved.setOnKeyPressed(a -> {
            if (a.getCode() == KeyCode.ENTER) {
                lvProductoAgregados.getItems().remove(this);
                if (ventaEstructuraNuevoController != null) {
                    ventaEstructuraNuevoController.calculateTotales();
                } else {
                    postVentaEstructuraNuevoController.calculateTotales();
                }

            }
        });
        vbRight.getChildren().addAll(lblImporte, btnRemoved);

        getChildren().add(vbLeft);
        getChildren().add(hbCenter);
        getChildren().add(vbRight);
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }
}
