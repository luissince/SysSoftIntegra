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
import javafx.scene.layout.HBox;
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
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
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
    private Label lblMetodoPago;
    @FXML
    private Label lblAlmacen;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxComprasRealizadasController comprascontroller;

    private FxPrincipalController fxPrincipalController;

    private ArrayList<CompraCreditoTB> listComprasCredito;

    private FxOpcionesImprimirController fxOpcionesImprimirController;
    
    private CompraTB compraTB;

    private String idCompra;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCompra(apWindow);
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
                return CompraADO.ObtenerCompraId(idCompra);
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
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                compraTB = (CompraTB) objects[0];
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
                lblTotalCompra.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(compraTB.getTotal(), 2));
                lblAlmacen.setText(compraTB.getAlmacenTB().getNombre());

                if (compraTB.getEstado() == 3) {
                    lblEstado.setTextFill(Color.web("#990000"));
                }

                vbCondicion.getChildren().clear();
                if (!listComprasCredito.isEmpty()) {
                    lblMetodoPago.setText("Método de pago al crédito");
                    for (int i = 0; i < listComprasCredito.size(); i++) {
                        vbCondicion.getChildren().add(adddElementCondicion("Nro." + ((i + 1) < 10 ? "00" + (i + 1) : ((i + 1) >= 10 && (i + 1) <= 99 ? "0" + (i + 1) : (i + 1))) + " Pago el " + listComprasCredito.get(i).getFechaPago() + " por " + compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(listComprasCredito.get(i).getMonto(), 2)));
                    }
                } else {
                    lblMetodoPago.setText("Método de pago al contado");
                    vbCondicion.getChildren().add(adddElementCondicion("Pago al contado por el valor de: " + lblTotalCompra.getText()));
                }

                fillArticlesTable((ArrayList<DetalleCompraTB>) objects[1], compraTB.getMonedaTB().getSimbolo());

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

    private void fillArticlesTable(ArrayList<DetalleCompraTB> compraTBs, String simbolo) {
        for (int i = 0; i < compraTBs.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), compraTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), compraTBs.get(i).getSuministroTB().getClave() + "\n" + compraTBs.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), simbolo + " " + Tools.roundingValue(compraTBs.get(i).getPrecioCompra(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), "-" + Tools.roundingValue(compraTBs.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 3, (i + 1));
            gpList.add(addElementGridPane("l5" + (i + 1), compraTBs.get(i).getImpuestoTB().getNombre(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(compraTBs.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPane("l7" + (i + 1), compraTBs.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPane("l8" + (i + 1), simbolo + " " + Tools.roundingValue(compraTBs.get(i).getCantidad() * (compraTBs.get(i).getPrecioCompra() - compraTBs.get(i).getDescuento()), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calcularTotales(compraTBs, simbolo);
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

    private void calcularTotales(ArrayList<DetalleCompraTB> compraTBs, String simbolo) {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;

        for (DetalleCompraTB ocdtb : compraTBs) {
            double importeBruto = ocdtb.getPrecioCompra() * ocdtb.getCantidad();
            double descuento = ocdtb.getDescuento();
            double subImporteBruto = importeBruto - descuento;
            double subImporteNeto = Tools.calculateTaxBruto(ocdtb.getImpuestoTB().getValor(), subImporteBruto);
            double impuesto = Tools.calculateTax(ocdtb.getImpuestoTB().getValor(), subImporteNeto);
            double importeNeto = subImporteNeto + impuesto;

            importeBrutoTotal += importeBruto;
            descuentoTotal += descuento;
            subImporteNetoTotal += subImporteNeto;
            impuestoTotal += impuesto;
            importeNetoTotal += importeNeto;
        }

        lblImporteBruto.setText(simbolo + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(simbolo + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporteNeto.setText(simbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(simbolo + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteNeto.setText(simbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    private Text adddElementCondicion(String value) {
        Text txtTitulo = new Text(value);
        txtTitulo.setStyle("-fx-fill:#020203");
        txtTitulo.getStyleClass().add("labelOpenSansRegular13");
        return txtTitulo;
    }

    private void onEventReporte() {
        fxOpcionesImprimirController.getTiketCompra().mostrarReporte(idCompra);
    }

    private void eventAnularCompra() {
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
            Stage stage = WindowStage.StageLoaderModal(parent, "Anular su compra", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller compras" + ex.getLocalizedMessage());
        }
    }

    private void onEventClose() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setTopAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setRightAnchor(comprascontroller.getWindow(), 0d);
        AnchorPane.setBottomAnchor(comprascontroller.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(comprascontroller.getWindow());
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
        onEventClose();
    }

    @FXML
    private void onKeyPressedAnular(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventAnularCompra();
        }
    }

    @FXML
    private void onActionAnular(ActionEvent event) {
        eventAnularCompra();
    }

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventClose();
        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        onEventClose();
    }

    public AnchorPane getApWindow() {
        return apWindow;
    }

    public CompraTB getCompraTB() {
        return compraTB;
    }

    public ArrayList<CompraCreditoTB> getListComprasCredito() {
        return listComprasCredito;
    }

    public void setInitComptrasController(FxComprasRealizadasController comprascontroller, FxPrincipalController fxPrincipalController) {
        this.comprascontroller = comprascontroller;
        this.fxPrincipalController = fxPrincipalController;
    }

}
