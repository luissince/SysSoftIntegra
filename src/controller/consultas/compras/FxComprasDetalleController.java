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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.CompraTB;
import model.DetalleCompraTB;
import model.EgresoTB;
import service.CompraADO;

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
    private GridPane gpListCompraDetalle;
    @FXML
    private GridPane gpListEgresos;
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
    private Label lblAlmacen;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblEgresos;

    private FxComprasRealizadasController comprascontroller;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private CompraTB compraTB;

    private String idCompra;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCompra(apWindow);
    }

    public void loadInitComponents(String idCompra) {
        this.idCompra = idCompra;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return CompraADO.obtenerCompraPorId(idCompra);
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
            if (object instanceof CompraTB) {
                compraTB = (CompraTB) object;

                lblProveedor.setText(compraTB.getProveedorTB().getNumeroDocumento() + " " + compraTB.getProveedorTB().getRazonSocial().toUpperCase());
                lblTelefono.setText(compraTB.getProveedorTB().getTelefono());
                lblCelular.setText(compraTB.getProveedorTB().getCelular());
                lblEmail.setText(compraTB.getProveedorTB().getEmail());
                lblDireccion.setText(compraTB.getProveedorTB().getDireccion());
                lblFechaCompra.setText(compraTB.getFechaCompra().toUpperCase() + " " + compraTB.getHoraCompra());
                lblComprobante.setText(compraTB.getComprobante() + " " + compraTB.getSerie() + "-" + compraTB.getNumeracion());
                lblObservacion.setText(compraTB.getObservaciones().equalsIgnoreCase("") ? "NO TIENE NINGUNA OBSERVACIÓN" : compraTB.getObservaciones());
                lblNotas.setText(compraTB.getNotas().equalsIgnoreCase("") ? "NO TIENE NINGUNA NOTA" : compraTB.getNotas());
                lblTipo.setText(compraTB.getTipoName());
                lblEstado.setText(compraTB.getEstadoName());
                lblTotalCompra.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(compraTB.getTotal(), 2));
                lblAlmacen.setText(compraTB.getAlmacenTB().getNombre());

                if (compraTB.getEstado() == 3) {
                    lblEstado.setTextFill(Color.web("#990000"));
                }

                gpListCompraDetalle.getChildren().clear();
                createHeadTableDetalleCompra();
                createBodyTableDetalleCompra(compraTB.getDetalleCompraTBs());

                gpListEgresos.getChildren().clear();
                createHeadTableEgreso();
                createBodyTableEgreso(compraTB.getEgresoTBs());

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

    private void createHeadTableDetalleCompra() {
        gpListCompraDetalle.add(addHeadGridPane("N°"), 0, 0);
        gpListCompraDetalle.add(addHeadGridPane("Descripción"), 1, 0);
        gpListCompraDetalle.add(addHeadGridPane("Precio"), 2, 0);
        gpListCompraDetalle.add(addHeadGridPane("Descuento"), 3, 0);
        gpListCompraDetalle.add(addHeadGridPane("Impuesto %"), 4, 0);
        gpListCompraDetalle.add(addHeadGridPane("Cantidad"), 5, 0);
        gpListCompraDetalle.add(addHeadGridPane("Unidad"), 6, 0);
        gpListCompraDetalle.add(addHeadGridPane("Importe"), 7, 0);
    }

    private void createBodyTableDetalleCompra(ArrayList<DetalleCompraTB> compraTBs) {
        for (int i = 0; i < compraTBs.size(); i++) {
            DetalleCompraTB detalleCompraTB = compraTBs.get(i);
            gpListCompraDetalle.add(addElementGridPane("l1" + (i + 1), detalleCompraTB.getId() + "", Pos.CENTER), 0, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l2" + (i + 1), detalleCompraTB.getSuministroTB().getClave() + "\n" + detalleCompraTB.getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l3" + (i + 1), compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(detalleCompraTB.getPrecioCompra(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l4" + (i + 1), Tools.roundingValue(detalleCompraTB.getDescuento(), 2), Pos.CENTER_RIGHT), 3, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l5" + (i + 1), detalleCompraTB.getImpuestoTB().getNombre(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(detalleCompraTB.getCantidad(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l7" + (i + 1), detalleCompraTB.getSuministroTB().getUnidadCompraName(), Pos.CENTER_RIGHT), 6, (i + 1));
            gpListCompraDetalle.add(addElementGridPane("l8" + (i + 1), compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(detalleCompraTB.getCantidad() * (detalleCompraTB.getPrecioCompra() - detalleCompraTB.getDescuento()), 2), Pos.CENTER_RIGHT), 7, (i + 1));
        }
        calcularTotales(compraTBs);
    }

    private void createHeadTableEgreso() {
        gpListEgresos.add(addHeadGridPane("N°"), 0, 0);
        gpListEgresos.add(addHeadGridPane("Fecha"), 1, 0);
        gpListEgresos.add(addHeadGridPane("Banco"), 2, 0);
        gpListEgresos.add(addHeadGridPane("Detalle"), 3, 0);
        gpListEgresos.add(addHeadGridPane("Monto"), 4, 0);
        gpListEgresos.add(addHeadGridPane("Vuelto"), 5, 0);
    }

    private void createBodyTableEgreso(ArrayList<EgresoTB> egresoTBs) {
        if (egresoTBs.isEmpty()) {
            Label titulo = addElementGridPane("l1", "No hay salidas para mostrar.", Pos.CENTER);
            gpListEgresos.add(titulo, 0, 1);
            GridPane.setRowSpan(titulo, GridPane.REMAINING);
            GridPane.setColumnSpan(titulo, GridPane.REMAINING);
            lblEgresos.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(0, 2));
        } else {
            double suma = 0;
            for (int i = 0; i < egresoTBs.size(); i++) {
                EgresoTB egresoTB = egresoTBs.get(i);
                gpListEgresos.add(addElementGridPane("l1" + (i + 1), egresoTB.getId() + "", Pos.CENTER), 0, (i + 1));
                gpListEgresos.add(addElementGridPane("l2" + (i + 1), egresoTB.getFecha() + "\n" + egresoTB.getHora(), Pos.CENTER_LEFT), 1, (i + 1));
                gpListEgresos.add(addElementGridPane("l3" + (i + 1), egresoTB.getBancoTB().getNombreCuenta(), Pos.CENTER_LEFT), 2, (i + 1));
                gpListEgresos.add(addElementGridPane("l4" + (i + 1), egresoTB.getDetalle(), Pos.CENTER_LEFT), 3, (i + 1));
                gpListEgresos.add(addElementGridPane("l5" + (i + 1), Tools.roundingValue(egresoTB.getMonto(), 2), Pos.CENTER_RIGHT), 4, (i + 1));
                gpListEgresos.add(addElementGridPane("l6" + (i + 1), Tools.roundingValue(egresoTB.getVuelto(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
                suma += egresoTB.getMonto();
            }
            lblEgresos.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(suma, 2));
        }
    }

    private Label addHeadGridPane(String nombre) {
        Label label = new Label(nombre);
        label.setTextFill(Color.web("#FFFFFF"));
        label.getStyleClass().add("labelRoboto13");
        label.setStyle(
                "-fx-background-color:  #020203;-fx-padding:  0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight:100;");
        label.setAlignment(Pos.CENTER);
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

    private void calcularTotales(ArrayList<DetalleCompraTB> compraTBs) {
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

        lblImporteBruto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(descuentoTotal, 2));
        lblSubImporteNeto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
        lblImporteNeto.setText(compraTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    private void onEventReporte() {
        fxOpcionesImprimirController.getTiketCompra().mostrarReporte(idCompra);
    }

    private void onEventImprimir() {
        fxOpcionesImprimirController.getTiketCompra().imprimir(idCompra);
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
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        onEventReporte();
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventImprimir();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        onEventImprimir();
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

    public void setInitComptrasController(FxComprasRealizadasController comprascontroller, FxPrincipalController fxPrincipalController) {
        this.comprascontroller = comprascontroller;
        this.fxPrincipalController = fxPrincipalController;
    }

}
