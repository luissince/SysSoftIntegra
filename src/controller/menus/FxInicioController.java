package controller.menus;

import controller.inventario.valorinventario.FxListaInventarioController;
import controller.tools.DoughnutChart;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.GlobalADO;
import model.SuministroTB;
import org.controlsfx.control.Notifications;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FxInicioController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Text lblFechaActual;
    @FXML
    private Text lblProducto;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lblProveedor;
    @FXML
    private Text lblTrabajador;
    @FXML
    private Text lblVentasTotales;
    @FXML
    private Text lblComprasTotales;
    @FXML
    private Text lblVentasPagar;
    @FXML
    private Text lblComprasPagar;
    @FXML
    private Text lblNegativos;
    @FXML
    private Text lblIntermedios;
    @FXML
    private Text lblNecesarias;
    @FXML
    private Text lblExcentes;
    @FXML
    private HBox hbLoad;
    @FXML
    private VBox vbVentas;
    @FXML
    private VBox vbInventario;
    @FXML
    private VBox vbTipoVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initClock();
    }

    private void initClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, (ActionEvent e) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d ', de' MMMM 'de' yyyy HH:mm:ss");
            lblFechaActual.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void initDashboard() {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                return GlobalADO.DashboardLoad(Tools.getDate());
            }
        };
        task.setOnScheduled(e -> {
            hbLoad.setVisible(true);
        });

        task.setOnFailed(e -> {
            hbLoad.setVisible(false);
        });

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof ArrayList) {
                ArrayList<Object> arrayList = (ArrayList<Object>) object;

                lblVentasTotales.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue((double) arrayList.get(0), 2));
                lblComprasTotales.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue((double) arrayList.get(1), 2));
                lblVentasPagar.setText(Tools.roundingValue((int) arrayList.get(2), 0));
                lblComprasPagar.setText(Tools.roundingValue((int) arrayList.get(3), 0));

                lblProducto.setText(Tools.roundingValue((double) arrayList.get(4), 0));
                lblCliente.setText(Tools.roundingValue((double) arrayList.get(5), 0));
                lblProveedor.setText(Tools.roundingValue((double) arrayList.get(6), 0));
                lblTrabajador.setText(Tools.roundingValue((double) arrayList.get(7), 0));

                lblNegativos.setText(Tools.roundingValue((int) arrayList.get(8), 0));
                lblIntermedios.setText(Tools.roundingValue((int) arrayList.get(9), 0));
                lblNecesarias.setText(Tools.roundingValue((int) arrayList.get(10), 0));
                lblExcentes.setText(Tools.roundingValue((int) arrayList.get(11), 0));

                loadBarChartGraphics((JSONArray) arrayList.get(12));
                loadPieChartGraphics();
                loadDoughnutChartGraphics();
//  
//            ArrayList<SuministroTB> listaProductos = (ArrayList<SuministroTB>) arrayList.get(10);
//
//            loadGraphics((int) arrayList.get(6), (int) arrayList.get(7), (int) arrayList.get(8), (int) arrayList.get(9), listaProductos);
//
//                    notificationState("Estado del producto","Tiene 15 días para su prueba, despues de ello\n se va bloquear el producto, gracias por elegirnos.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("Estado del inventario","Tiene un total de "+((int) arrayList.get(6))+" producto negativos,\n actualize su inventario por favor.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("SysSoftIntegra","Usa la AppSysSoftIntegra para realizar consular\n en tiempo real de sus tiendas.","logo.png",Pos.TOP_LEFT);
            }
            hbLoad.setVisible(false);

        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }

    }

    private void loadBarChartGraphics(JSONArray array) {
        JSONArray arrayVentas = array;

        ObservableList<String> list = FXCollections.observableArrayList(
                Arrays.asList(
                        "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SET", "OCT", "NOV", "DIC"
                ));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(list);
        xAxis.setLabel("category");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("score");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendSide(Side.TOP);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Fiat");

        for (int i = 0; i < list.size(); i++) {
            String mes = list.get(i);
            double monto = 0;
            for (int j = 0; j < arrayVentas.size(); j++) {
                JSONObject object = (JSONObject) arrayVentas.get(j);
                if (Integer.parseInt(object.get("mes").toString()) == (i + 1)) {
                    monto = Double.parseDouble(object.get("monto").toString());
                    break;
                }
            }
            series1.getData().add(new XYChart.Data<>(mes, monto));
        }
        barChart.getData().addAll(series1);

        vbVentas.getChildren().clear();
        vbVentas.getChildren().add(barChart);
    }

    private void loadPieChartGraphics() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        data.add(new PieChart.Data("Negativos", 20));
        data.add(new PieChart.Data("Intermedios", 40));
        data.add(new PieChart.Data("Necesarios", 10));
        data.add(new PieChart.Data("Excedentes", 15));

        PieChart pie = new PieChart(data);
        pie.setLegendSide(Side.TOP);
        pie.setTitleSide(Side.BOTTOM);
        pie.setLabelLineLength(60);
        pie.setLabelsVisible(true);

        vbInventario.getChildren().clear();
        vbInventario.getChildren().add(pie);
    }

    private void loadDoughnutChartGraphics() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Grapefruit", 13),
                new PieChart.Data("Oranges", 25),
                new PieChart.Data("Plums", 10),
                new PieChart.Data("Pears", 22),
                new PieChart.Data("Apples", 30));

        DoughnutChart chart = new DoughnutChart(pieChartData);
        chart.setLegendSide(Side.TOP);

        vbTipoVenta.getChildren().clear();
        vbTipoVenta.getChildren().add(chart);
    }

    private void productoModel(String product, String price, String quantity) {
        HBox hBoxDetail = new HBox();
        hBoxDetail.setStyle("-fx-border-color: #cccccc;-fx-border-width: 0px 0px 1px 0px;");

        HBox hBoxImage = new HBox();
        HBox.setHgrow(hBoxImage, Priority.SOMETIMES);
        hBoxImage.setStyle("-fx-padding: 0.6666666666666666em;");
        ImageView imageView = new ImageView(new Image("/view/image/noimage.jpg"));
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(false);
        hBoxImage.getChildren().add(imageView);

        HBox hBoxContent = new HBox();
        HBox.setHgrow(hBoxContent, Priority.ALWAYS);

        VBox vBoxProduct = new VBox();
        HBox.setHgrow(vBoxProduct, Priority.ALWAYS);
        vBoxProduct.setAlignment(Pos.CENTER_LEFT);
        vBoxProduct.setStyle("-fx-padding: 0.6666666666666666em;");
        Label lblProducto = new Label(product);
        lblProducto.setStyle("-fx-text-fill:#020203;");
        lblProducto.getStyleClass().add("labelRobotoBold15");
        Label lblPrice = new Label("Precio: " + price);
        lblPrice.setStyle("-fx-text-fill:#545050;");
        lblPrice.getStyleClass().add("labelRoboto13");
        lblPrice.setMinWidth(Control.USE_COMPUTED_SIZE);
        lblPrice.setMinHeight(Control.USE_COMPUTED_SIZE);
        lblPrice.setPrefWidth(Control.USE_COMPUTED_SIZE);
        lblPrice.setPrefHeight(Control.USE_COMPUTED_SIZE);
        lblPrice.setMaxWidth(Control.USE_COMPUTED_SIZE);
        lblPrice.setMaxHeight(Control.USE_COMPUTED_SIZE);
        vBoxProduct.getChildren().addAll(lblProducto, lblPrice);

        VBox vBoxQuantity = new VBox();
        HBox.setHgrow(vBoxQuantity, Priority.SOMETIMES);
        vBoxQuantity.setAlignment(Pos.CENTER_RIGHT);
        vBoxQuantity.setStyle("-fx-padding:0.6666666666666666em;");
        Label lblQuantity = new Label("Cantidad: " + quantity);
        lblQuantity.setStyle("-fx-background-color: #0766cc;-fx-text-fill: #ffffff;-fx-padding: 10px 15px;-fx-border-radius: 0.25em;-fx-border-color: #0766cc;-fx-background-radius: 0.25em;");
        lblQuantity.getStyleClass().add("labelRoboto13");
        lblQuantity.setMinWidth(Control.USE_COMPUTED_SIZE);
        lblQuantity.setMinHeight(Control.USE_COMPUTED_SIZE);
        lblQuantity.setPrefWidth(Control.USE_COMPUTED_SIZE);
        lblQuantity.setPrefHeight(Control.USE_COMPUTED_SIZE);
        lblQuantity.setMaxWidth(Control.USE_COMPUTED_SIZE);
        lblQuantity.setMaxHeight(Control.USE_COMPUTED_SIZE);
        vBoxQuantity.getChildren().add(lblQuantity);
        hBoxContent.getChildren().addAll(vBoxProduct, vBoxQuantity);

        hBoxDetail.getChildren().addAll(hBoxImage, hBoxContent);
//        vbProductoMasVendidos.getChildren().add(hBoxDetail);
    }

    private void onEventInventario(short existencia) {
        try {
            // ObjectGlobal.InitializationTransparentBackground(vbPrincipal);
            URL url = getClass().getResource(FilesRouters.FX_LISTAR_INVENTARIO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxListaInventarioController controller = fXMLLoader.getController();
            controller.loadData(existencia);
            //
            Stage stage = WindowStage.StageLoader(parent, "Inventario general");
            stage.setResizable(true);
            stage.sizeToScene();
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void notificationState(String title, String message, String url, Pos pos) {
        Image image = new Image("/view/image/" + url);
        Notifications notifications = Notifications.create()
                .title(title)
                .text(message)
                .graphic(new ImageView(image))
                .hideAfter(Duration.seconds(5))
                .position(pos)
                .onAction(n -> {
                    Tools.println(n);
                });
        notifications.darkStyle();
        notifications.show();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            initDashboard();
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        initDashboard();
    }

    @FXML
    private void onKeyPressedNegativos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventInventario((short) 1);
        }
    }

    @FXML
    private void onActionNegativos(ActionEvent event) {
        onEventInventario((short) 1);
    }

    @FXML
    private void onKeyPressedIntermedios(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventInventario((short) 2);
        }
    }

    @FXML
    private void onActionIntermedios(ActionEvent event) {
        onEventInventario((short) 2);
    }

    @FXML
    private void onKeyPressedNecesarios(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventInventario((short) 3);
        }
    }

    @FXML
    private void onActionNecesarios(ActionEvent event) {
        onEventInventario((short) 3);
    }

    @FXML
    private void onKeyPressedExcedentes(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventInventario((short) 4);
        }
    }

    @FXML
    private void onActionExcedentes(ActionEvent event) {
        onEventInventario((short) 4);
    }

}
