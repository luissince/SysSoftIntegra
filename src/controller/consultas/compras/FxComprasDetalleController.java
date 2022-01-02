package controller.consultas.compras;

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
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.CompraADO;
import model.CompraCreditoTB;
import model.CompraTB;
import model.DetalleCompraTB;

public class FxComprasDetalleController implements Initializable {

    @FXML
    private ScrollPane cpWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblFechaCompra;
    @FXML
    private Label lblProveedor;
    @FXML
    private Label lblTelefono;
    @FXML
    private Label lblCelular;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblDireccion;
    @FXML
    private Label lblTipo;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblComprobante;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblImporteBruto;
    @FXML
    private Label lblSubImporteNeto;
    @FXML
    private Label lblImporteNeto;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblObservacion;
    @FXML
    private Label lblNotas;
    @FXML
    private Label lblTotalCompra;
    @FXML
    private VBox vbCondicion;
    @FXML
    private Button btnReporte;
    @FXML
    private Button btnAnular;
    @FXML
    private Label lblMetodoPago;
    @FXML
    private Label lblAlmacen;

    private FxComprasRealizadasController comprascontroller;

    private FxPrincipalController fxPrincipalController;

    private String idCompra;

    private ObservableList<DetalleCompraTB> arrList = null;

    private ArrayList<CompraCreditoTB> listComprasCredito;

    private CompraTB compraTB = null;

    private double subImporteNeto;

    private double impuesto;

    private double importeNeto;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCompra(cpWindow);
    }

    public void setLoadDetalle(String idCompra) {
        this.idCompra = idCompra;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return CompraADO.ListCompletaDetalleCompra(idCompra);
            }
        };

        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });
        task.setOnRunning(e -> {
            lblLoad.setVisible(true);
        });
        task.setOnFailed(e -> {
            lblLoad.setVisible(false);
        });
        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                compraTB = (CompraTB) objects[0];
                ObservableList<DetalleCompraTB> empList = (ObservableList<DetalleCompraTB>) objects[1];
                listComprasCredito = (ArrayList<CompraCreditoTB>) objects[2];

                lblProveedor.setText(compraTB.getProveedorTB().getNumeroDocumento() + " " + compraTB.getProveedorTB().getRazonSocial().toUpperCase());
                lblTelefono.setText(compraTB.getProveedorTB().getTelefono());
                lblCelular.setText(compraTB.getProveedorTB().getCelular());
                lblEmail.setText(compraTB.getProveedorTB().getEmail());
                lblDireccion.setText(compraTB.getProveedorTB().getDireccion());
                lblFechaCompra.setText(compraTB.getFechaCompra().toUpperCase() + " " + compraTB.getHoraCompra());
                lblComprobante.setText(compraTB.getSerie() + " - N °" + compraTB.getNumeracion());
                lblObservacion.setText(compraTB.getObservaciones().equalsIgnoreCase("") ? "NO TIENE NINGUNA OBSERVACIÓN" : compraTB.getObservaciones());
                lblNotas.setText(compraTB.getNotas().equalsIgnoreCase("") ? "NO TIENE NINGUNA NOTA" : compraTB.getNotas());
                lblTipo.setText(compraTB.getTipoName());
                lblEstado.setText(compraTB.getEstadoName());
                lblTotalCompra.setText(compraTB.getMonedaNombre() + " " + Tools.roundingValue(compraTB.getTotal(), 2));
                lblAlmacen.setText(compraTB.getAlmacenTB().getNombre());

                switch (compraTB.getEstado()) {
                    case 2:
                        btnReporte.setDisable(false);
                        btnAnular.setDisable(false);
                        break;
                    case 3:
                        btnReporte.setDisable(false);
                        btnAnular.setDisable(true);
                        lblEstado.setTextFill(Color.web("#990000"));
                        break;
                    case 4:
                        btnReporte.setDisable(false);
                        btnAnular.setDisable(true);
                        vbCondicion.getChildren().add(adddElementCondicion("La compra se encuentra en un estado de guardado"));
                        lblMetodoPago.setText("");
                        break;
                    default:
                        btnReporte.setDisable(false);
                        btnAnular.setDisable(false);
                        break;
                }

                if (!listComprasCredito.isEmpty()) {
                    lblMetodoPago.setText("Método de pago al crédito");
                    for (int i = 0; i < listComprasCredito.size(); i++) {
                        vbCondicion.getChildren().add(adddElementCondicion("Nro." + ((i + 1) < 10 ? "00" + (i + 1) : ((i + 1) >= 10 && (i + 1) <= 99 ? "0" + (i + 1) : (i + 1))) + " Pago el " + listComprasCredito.get(i).getFechaPago() + " por " + compraTB.getMonedaNombre() + " " + Tools.roundingValue(listComprasCredito.get(i).getMonto(), 2)));
                    }
                } else {
                    lblMetodoPago.setText("Método de pago al contado");
                    vbCondicion.getChildren().add(adddElementCondicion("Pago al contado por el valor de: " + lblTotalCompra.getText()));
                }

                fillArticlesTable(empList);
            } else {
                gpList.add(addElementGridPane("l1", (String) object, Pos.CENTER), 0, 1);
            }
            lblLoad.setVisible(false);
        });
        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void fillArticlesTable(ObservableList<DetalleCompraTB> arr) {
        arrList = arr;
        for (int i = 0; i < arrList.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), arrList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), arrList.get(i).getSuministroTB().getClave() + "\n" + arrList.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), compraTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(arrList.get(i).getPrecioCompra(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), "-" + Tools.roundingValue(arrList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(arrList.get(i).getValorImpuesto(), 2) + "%", Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(arrList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), arrList.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), compraTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(arrList.get(i).getCantidad() * (arrList.get(i).getPrecioCompra() - arrList.get(i).getDescuento()), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calcularTotales();
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

    private void calcularTotales() {
        subImporteNeto = 0;
        arrList.forEach(e -> subImporteNeto += (e.getSubImporteNeto() * e.getCantidad()));
        lblSubImporteNeto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNeto, 2));

        impuesto = 0;
        arrList.forEach(e -> impuesto += (e.getImpuestoGenerado() * e.getCantidad()));
        lblImpuesto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuesto, 2));

        importeNeto = 0;
        arrList.forEach(e -> importeNeto += (e.getImporteNeto() * e.getCantidad()));
        lblImporteNeto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNeto, 2));
    }

    private Text adddElementCondicion(String value) {
        Text txtTitulo = new Text(value);
        txtTitulo.setStyle("-fx-fill:#020203");
        txtTitulo.getStyleClass().add("labelOpenSansRegular13");
        return txtTitulo;
    }

    private Label addLabelTitle(String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setStyle("-fx-text-fill:#020203;-fx-padding: 0.4166666666666667em 0em  0.4166666666666667em 0em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Control.USE_COMPUTED_SIZE);
        label.setMaxHeight(Control.USE_COMPUTED_SIZE);
        return label;
    }

    private Label addLabelTotal(String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setStyle("-fx-text-fill:#0771d3;");
        label.getStyleClass().add("labelRobotoMedium15");
        label.setAlignment(pos);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void onEventReporte() {
        fxOpcionesImprimirController.getTiketCompra().mostrarReporte(idCompra);
    }

    private void eventCancelarVenta() {
        if (!idCompra.equalsIgnoreCase("") || idCompra != null) {
            try {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_COMPRAS_CANCELAR);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                //Controlller here
                FxComprasCancelarController controller = fXMLLoader.getController();
                controller.setComprasDetalleController(this);
                controller.loadComponents();
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Anular su compra", cpWindow.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                stage.show();
            } catch (IOException ex) {
                System.out.println("Controller compras" + ex.getLocalizedMessage());
            }

        }
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReporte();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        onEventReporte();
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) throws IOException {
        fxPrincipalController.getVbContent().getChildren().remove(cpWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setTopAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setRightAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setBottomAnchor(comprascontroller.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(comprascontroller.getWindow());
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventCancelarVenta();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        eventCancelarVenta();
    }

    public ScrollPane getCpWindow() {
        return cpWindow;
    }

    public CompraTB getCompraTB() {
        return compraTB;
    }

    public ObservableList<DetalleCompraTB> getArrList() {
        return arrList;
    }

    public ArrayList<CompraCreditoTB> getListComprasCredito() {
        return listComprasCredito;
    }

    public void setInitComptrasController(FxComprasRealizadasController comprascontroller, FxPrincipalController fxPrincipalController) {
        this.comprascontroller = comprascontroller;
        this.fxPrincipalController = fxPrincipalController;
    }

}
