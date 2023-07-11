package controller.menus;

import controller.inventario.valorinventario.FxListaInventarioController;
import controller.tools.DoughnutChart;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.GlobalADO;

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
    @FXML
    private GridPane gpListVentas;
    @FXML
    private GridPane gpListVecesVendidos;
    @FXML
    private GridPane gpListVentasCantidadVendidos;
    @FXML
    private Label lblPorEfectivo;
    @FXML
    private Label lblEfectivoSuma;
    @FXML
    private Label lblProCredito;
    @FXML
    private Label lblCreditoSuma;
    @FXML
    private Label lblPorTarjeta;
    @FXML
    private Label lblTarjetaSuma;
    @FXML
    private Label lblPorDeposito;
    @FXML
    private Label lblDepositoSuma;

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
                loadPieChartGraphics((int) arrayList.get(8), (int) arrayList.get(9), (int) arrayList.get(10), (int) arrayList.get(11));
                loadTableVentasTipos((JSONArray) arrayList.get(13));
                loadDoughnutChartGraphics((JSONArray) arrayList.get(14));
                loadTableVecesVendidos((JSONArray) arrayList.get(15));
                loadTableCantidadVendidos((JSONArray) arrayList.get(16));

//                    notificationState("Estado del producto","Tiene 15 días para su prueba, despues de ello\n se va bloquear el producto, gracias por elegirnos.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("Estado del inventario","Tiene un total de "+((int) arrayList.get(6))+" producto negativos,\n actualize su inventario por favor.","warning_large.png",Pos.TOP_RIGHT);
//                    notificationState("SysSoftIntegra","Usa la AppSysSoftIntegra para realizar consular\n en tiempo real de sus tiendas.","logo.png",Pos.TOP_LEFT);
            } else {
                Tools.println((String) object);
            }
            hbLoad.setVisible(false);
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void loadBarChartGraphics(JSONArray array) {
        ObservableList<String> list = FXCollections.observableArrayList(
                Arrays.asList(
                        "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SET", "OCT", "NOV", "DIC"
                ));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(list);
        xAxis.setLabel("Meses");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Monto");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendSide(Side.TOP);

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        LocalDate current_date = LocalDate.now();
        series1.setName("" + current_date.getYear());

        for (int i = 0; i < list.size(); i++) {
            String mes = list.get(i);
            double monto = 0;
            for (int j = 0; j < array.size(); j++) {
                JSONObject object = (JSONObject) array.get(j);
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

    private void loadPieChartGraphics(int ng, int in, int ne, int ex) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        data.add(new PieChart.Data("Negativos", ng));
        data.add(new PieChart.Data("Intermedios", in));
        data.add(new PieChart.Data("Necesarios", ne));
        data.add(new PieChart.Data("Excedentes", ex));

        PieChart pie = new PieChart(data);
        pie.setLegendSide(Side.TOP);
        pie.setTitleSide(Side.BOTTOM);
        pie.setLabelLineLength(60);
        pie.setLabelsVisible(true);

        vbInventario.getChildren().clear();
        vbInventario.getChildren().add(pie);
    }

    private void loadTableVentasTipos(JSONArray array) {

        gpListVentas.getChildren().clear();
        gpListVentas.add(addHeadGridPane("Mes"), 0, 0);
        gpListVentas.add(addHeadGridPane("Venta Sunat"), 1, 0);
        gpListVentas.add(addHeadGridPane("Venta Libre"), 2, 0);
        gpListVentas.add(addHeadGridPane("Venta Total"), 3, 0);

        List<String> meses = new ArrayList<>(Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Setiembre", "Octubre", "Noviembre", "Diciembre"));

        for (int i = 0; i < meses.size(); i++) {

            String mes = meses.get(i);
            double sunat = 0;
            double libre = 0;
            for (int j = 0; j < array.size(); j++) {
                JSONObject object = (JSONObject) array.get(j);
                if (Integer.parseInt(object.get("mes").toString()) == (i + 1)) {
                    sunat += Double.parseDouble(object.get("sunat").toString());
                    libre += Double.parseDouble(object.get("libre").toString());
                }
            }
            String background = (i % 2 != 0 ? "white" : "#dee2e6");
            gpListVentas.add(addElementGridPaneLabel("l1" + (i + 1), mes, Pos.CENTER, background), 0, (i + 1));
            gpListVentas.add(addElementGridPaneLabel("l2" + (i + 1), "S/ " + Tools.roundingValue(sunat, 2), Pos.CENTER_RIGHT, background), 1, (i + 1));
            gpListVentas.add(addElementGridPaneLabel("l3" + (i + 1), "S/ " + Tools.roundingValue(libre, 2), Pos.CENTER_RIGHT, background), 2, (i + 1));
            gpListVentas.add(addElementGridPaneLabel("l4" + (i + 1), "S/ " + Tools.roundingValue(sunat + libre, 2), Pos.CENTER_RIGHT, background), 3, (i + 1));
        }
    }

    private void loadDoughnutChartGraphics(JSONArray array) {
        double efectivo = 0;
        double credito = 0;
        double tarjeta = 0;
        double deposito = 0;
        double total;

        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            if (Integer.parseInt(object.get("IdNotaCredito").toString()) == 0 && Integer.parseInt(object.get("Estado").toString()) != 3) {
                if (Integer.parseInt(object.get("Estado").toString()) == 2 || Integer.parseInt(object.get("Tipo").toString()) == 2 && Integer.parseInt(object.get("Estado").toString()) == 1) {
                    credito += Double.parseDouble(object.get("Total").toString());
                } else if (Integer.parseInt(object.get("Estado").toString()) == 1 || Integer.parseInt(object.get("Estado").toString()) == 4) {
                    if ("EFECTIVO".equals(String.valueOf(object.get("FormaName").toString()))) {
                        efectivo += Double.parseDouble(object.get("Total").toString());
                    } else if ("TARJETA".equals(String.valueOf(object.get("FormaName").toString()))) {
                        tarjeta += Double.parseDouble(object.get("Total").toString());
                    } else if ("MIXTO".equals(String.valueOf(object.get("FormaName").toString()))) {
                        efectivo += Double.parseDouble(object.get("Efectivo").toString());
                        tarjeta += Double.parseDouble(object.get("Tarjeta").toString());
                    } else {
                        deposito += Double.parseDouble(object.get("Total").toString());
                    }
                }
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Efectivo", efectivo),
                new PieChart.Data("Crédito", credito),
                new PieChart.Data("Tarjeta", tarjeta),
                new PieChart.Data("Deposito", deposito));

        DoughnutChart chart = new DoughnutChart(pieChartData);
        chart.setLegendSide(Side.TOP);

        total = efectivo + credito + tarjeta + deposito;
        lblEfectivoSuma.setText("S/ " + Tools.roundingValue(efectivo, 2));
        lblCreditoSuma.setText("S/ " + Tools.roundingValue(credito, 2));
        lblTarjetaSuma.setText("S/ " + Tools.roundingValue(tarjeta, 2));
        lblDepositoSuma.setText("S/ " + Tools.roundingValue(deposito, 2));

        lblPorEfectivo.setText(Math.round(round(total, efectivo)) + " %");
        lblProCredito.setText(Math.round(round(total, credito)) + " %");
        lblPorTarjeta.setText(Math.round(round(total, tarjeta)) + " %");
        lblPorDeposito.setText(Math.round(round(total, deposito)) + " %");

        vbTipoVenta.getChildren().clear();
        vbTipoVenta.getChildren().add(chart);
    }

    private double round(double total, double valor) {
        return (valor * 100) / total;
    }

    private void loadTableVecesVendidos(JSONArray array) {
        gpListVecesVendidos.getChildren().clear();
        gpListVecesVendidos.add(addHeadGridPane("N°"), 0, 0);
        gpListVecesVendidos.add(addHeadGridPane("Producto"), 1, 0);
        gpListVecesVendidos.add(addHeadGridPane("Categoría/Marca"), 2, 0);
        gpListVecesVendidos.add(addHeadGridPane("Veces"), 3, 0);

        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            String background = (i % 2 != 0 ? "white" : "#dee2e6");
            gpListVecesVendidos.add(addElementGridPaneLabelTwo("l1" + (i + 1), "" + (i + 1), Pos.CENTER, background), 0, (i + 1));
            gpListVecesVendidos.add(addElementGridPaneLabelTwo("l2" + (i + 1), String.valueOf(object.get("Clave").toString()) + "\n" + String.valueOf(object.get("NombreMarca").toString()), Pos.CENTER_LEFT, background), 1, (i + 1));
            gpListVecesVendidos.add(addElementGridPaneLabelTwo("l3" + (i + 1), String.valueOf(object.get("Categoria").toString()) + "\n" + String.valueOf(object.get("Marca").toString()), Pos.CENTER_LEFT, background), 2, (i + 1));
            gpListVecesVendidos.add(addElementGridPaneLabelTwo("l4" + (i + 1), Tools.roundingValue(Double.parseDouble(object.get("Cantidad").toString()), 2), Pos.CENTER_LEFT, background), 3, (i + 1));
        }
    }

    private void loadTableCantidadVendidos(JSONArray array) {
        gpListVentasCantidadVendidos.getChildren().clear();
        gpListVentasCantidadVendidos.add(addHeadGridPane("N°"), 0, 0);
        gpListVentasCantidadVendidos.add(addHeadGridPane("Producto"), 1, 0);
        gpListVentasCantidadVendidos.add(addHeadGridPane("Categoría/Marca"), 2, 0);
        gpListVentasCantidadVendidos.add(addHeadGridPane("Cantidad"), 3, 0);

        for (int i = 0; i < array.size(); i++) {
            JSONObject object = (JSONObject) array.get(i);
            String background = (i % 2 != 0 ? "white" : "#dee2e6");
            gpListVentasCantidadVendidos.add(addElementGridPaneLabelTwo("l1" + (i + 1), "" + (i + 1), Pos.CENTER, background), 0, (i + 1));
            gpListVentasCantidadVendidos.add(addElementGridPaneLabelTwo("l2" + (i + 1), String.valueOf(object.get("Clave").toString()) + "\n" + String.valueOf(object.get("NombreMarca").toString()), Pos.CENTER_LEFT, background), 1, (i + 1));
            gpListVentasCantidadVendidos.add(addElementGridPaneLabelTwo("l3" + (i + 1), String.valueOf(object.get("Categoria").toString()) + "\n" + String.valueOf(object.get("Marca").toString()), Pos.CENTER_LEFT, background), 2, (i + 1));
            gpListVentasCantidadVendidos.add(addElementGridPaneLabelTwo("l4" + (i + 1), Tools.roundingValue(Double.parseDouble(object.get("Suma").toString()), 2) + "\n" + String.valueOf(object.get("Medida").toString()), Pos.CENTER_LEFT, background), 3, (i + 1));
        }
    }

    private Label addHeadGridPane(String nombre) {
        Label label = new Label(nombre);
        label.getStyleClass().add("labelRobotoBold14");
        label.setStyle("-fx-text-fill: #020203;;-fx-padding: 0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight: bold;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPaneLabel(String id, String nombre, Pos pos, String background) {
        Label label = new Label(nombre);
        label.setId(id);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle("-fx-text-fill:#6c757d;-fx-background-color: " + background + ";-fx-padding: 0.8333333333333334em 0.8333333333333334em;");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPaneLabelTwo(String id, String nombre, Pos pos, String background) {
        Label label = new Label(nombre);
        label.setId(id);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle("-fx-text-fill: #020203;-fx-background-color: " + background + ";-fx-padding: 0.8333333333333334em 0.8333333333333334em;");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
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
