package controller.menus;

import controller.inventario.valorinventario.FxListaInventarioController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
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

public class FxInicioController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Text lblFechaActual;
    @FXML
    private Text lblArticulo;
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
    private PieChart pcInventario;
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
    private VBox vbProductoMasVendidos;
    @FXML
    private HBox hbLoad;

    private ObservableList<PieChart.Data> datas = FXCollections.observableArrayList(
            new PieChart.Data("Productos Negativos", 0),
            new PieChart.Data("Productos Intermedios", 0),
            new PieChart.Data("Productos Necesarios", 0),
            new PieChart.Data("Productos Excedentes", 0)
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initClock();
        initGraphics();
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

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            protected ArrayList<Object> call() throws Exception {
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
            ArrayList<Object> arrayList = task.getValue();
            lblVentasTotales.setText(Session.MONEDA_SIMBOLO + " " + arrayList.get(0));
            lblComprasTotales.setText(Session.MONEDA_SIMBOLO + " " + arrayList.get(1));
            lblArticulo.setText((String) arrayList.get(2));
            lblCliente.setText((String) arrayList.get(3));
            lblProveedor.setText((String) arrayList.get(4));
            lblTrabajador.setText((String) arrayList.get(5));

            ArrayList<SuministroTB> listaProductos = (ArrayList<SuministroTB>) arrayList.get(10);

            loadGraphics((int) arrayList.get(6), (int) arrayList.get(7), (int) arrayList.get(8), (int) arrayList.get(9), listaProductos);

            lblVentasPagar.setText(Tools.roundingValue((int) arrayList.get(11), 0));
            lblComprasPagar.setText(Tools.roundingValue((int) arrayList.get(12), 0));
//                    notificationState("Estado del producto","Tiene 15 días para su prueba, despues de ello\n se va bloquear el producto, gracias por elegirnos.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("Estado del inventario","Tiene un total de "+((int) arrayList.get(6))+" producto negativos,\n actualize su inventario por favor.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("SysSoftIntegra","Usa la AppSysSoftIntegra para realizar consular\n en tiempo real de sus tiendas.","logo.png",Pos.TOP_LEFT);
            hbLoad.setVisible(false);
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }

    }

    public void initGraphics() {
        datas = FXCollections.observableArrayList(
                new PieChart.Data("Productos Negativos", 0),
                new PieChart.Data("Productos Intermedios", 0),
                new PieChart.Data("Productos Necesarios", 0),
                new PieChart.Data("Productos Excedentes", 0)
        );
        pcInventario.setData(datas);
    }

    private void loadGraphics(double negativas, double intermedias, double necesarias, double excedentes, ArrayList<SuministroTB> arrayList) {
        lblNegativos.setText("" + Tools.roundingValue(negativas, 0));
        lblIntermedios.setText("" + Tools.roundingValue(intermedias, 0));
        lblNecesarias.setText("" + Tools.roundingValue(necesarias, 0));
        lblExcentes.setText("" + Tools.roundingValue(excedentes, 0));

        datas = FXCollections.observableArrayList(
                new PieChart.Data("Productos Negativos", negativas),
                new PieChart.Data("Productos Intermedios", intermedias),
                new PieChart.Data("Productos Necesarios", necesarias),
                new PieChart.Data("Productos Excedentes", excedentes)
        );
        pcInventario.setData(datas);

        vbProductoMasVendidos.getChildren().clear();
        arrayList.forEach(e -> {
            productoModel(e.getNombreMarca(), Tools.roundingValue(e.getPrecioVentaGeneral(), 2), Tools.roundingValue(e.getCantidad(), 2));
        });
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
        vbProductoMasVendidos.getChildren().add(hBoxDetail);
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

    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {

        }
    }


}
