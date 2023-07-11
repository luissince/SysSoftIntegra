package controller.consultas.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.operaciones.venta.FxVentaLlevarController;
import controller.operaciones.venta.FxVentaLlevarControllerHistorial;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.EmpleadoTB;
import model.IngresoTB;
import model.SuministroTB;
import model.VentaTB;
import service.VentaADO;

public class FxVentaDetalleController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private Label lblLoad;
    @FXML
    private Text lblFechaVenta;
    @FXML
    private Text lblComprobante;
    @FXML
    private Label lblTotal;
    @FXML
    private Text lblCliente;
    @FXML
    private Text lbCorreoElectronico;
    @FXML
    private Text lbDireccion;
    @FXML
    private Text lblTipo;
    @FXML
    private Text lblObservaciones;
    @FXML
    private Text lblVendedor;
    @FXML
    private Label lblValorVenta;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubTotal;
    @FXML
    private Text lbClienteInformacion;
    @FXML
    private Label lblImpuesto;
    @FXML
    private GridPane gpListVetanDetalle;
    @FXML
    private GridPane gpListIngresos;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Label lblIngresos;

    private FxPrincipalController principalController;

    private FxVentaRealizadasController ventaRealizadasController;

    private FxOpcionesImprimirController opcionesImprimirController;

    private String idVenta;

    private VentaTB ventaTB;

    private double total;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        opcionesImprimirController = new FxOpcionesImprimirController();
        opcionesImprimirController.loadComponents();
        opcionesImprimirController.loadTicketVentaDetalle(apWindow);
    }

    public void loadInitComponents(String idVenta) {
        this.idVenta = idVenta;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<VentaTB> task = new Task<VentaTB>() {
            @Override
            protected VentaTB call() throws Exception {
                Object result = VentaADO.obtenerVentaPorIdVenta(idVenta);
                if (result instanceof VentaTB) {
                    return (VentaTB) result;
                }
                throw new Exception((String) result);
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
            ventaTB = task.getValue();
            EmpleadoTB empleadoTB = ventaTB.getEmpleadoTB();

            lblFechaVenta.setText(ventaTB.getFechaVenta() + " " + ventaTB.getHoraVenta());
            lblCliente.setText(
                    ventaTB.getClienteTB().getNumeroDocumento() + " " + ventaTB.getClienteTB().getInformacion());
            lbClienteInformacion
                    .setText(ventaTB.getClienteTB().getTelefono() + "-" + ventaTB.getClienteTB().getCelular());
            lbCorreoElectronico.setText(ventaTB.getClienteTB().getEmail());
            lbDireccion.setText(ventaTB.getClienteTB().getDireccion());
            lblComprobante.setText(
                    ventaTB.getTipoDocumentoTB().getNombre() + " " + ventaTB.getSerie() + "-"
                            + ventaTB.getNumeracion());
            lblObservaciones.setText(ventaTB.getObservaciones());
            lblTipo.setText(ventaTB.getTipoName() + " - " + ventaTB.getEstadoName());

            if (ventaTB.getEstado() == 3) {
                lblTipo.setStyle("-fx-fill: #990000;");
            } else if (ventaTB.getEstado() == 2) {
                lblTipo.setStyle(" -fx-fill: #6D5F02;");
            } else {
                lblTipo.setStyle("-fx-fill: #055bd3");
            }

            if (empleadoTB != null) {
                lblVendedor.setText(empleadoTB.getApellidos() + ", " + empleadoTB.getNombres());
            }

            gpListVetanDetalle.getChildren().clear();
            createHeadTableDetalleVenta();
            createBodyTableDetalleVenta(ventaTB.getSuministroTBs());

            gpListIngresos.getChildren().clear();
            createHeadTableIngresos();
            createBodyTableIngresos(ventaTB.getIngresoTBs());

            spBody.setDisable(false);
            hbLoad.setVisible(false);

            lblLoad.setVisible(false);
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    private void createHeadTableDetalleVenta() {
        gpListVetanDetalle.add(addHeadGridPane("N°"), 0, 0);
        gpListVetanDetalle.add(addHeadGridPane("Descripción"), 1, 0);
        gpListVetanDetalle.add(addHeadGridPane("Cantidad"), 2, 0);
        gpListVetanDetalle.add(addHeadGridPane("Bonificación"), 3, 0);
        gpListVetanDetalle.add(addHeadGridPane("Medida"), 4, 0);
        gpListVetanDetalle.add(addHeadGridPane("Impuesto"), 5, 0);
        gpListVetanDetalle.add(addHeadGridPane("Precio"), 6, 0);
        gpListVetanDetalle.add(addHeadGridPane("Descuento"), 7, 0);
        gpListVetanDetalle.add(addHeadGridPane("Importe"), 8, 0);
        gpListVetanDetalle.add(addHeadGridPane("Por Llevar"), 9, 0);
        gpListVetanDetalle.add(addHeadGridPane("Historial"), 10, 0);
    }

    private void createBodyTableDetalleVenta(ArrayList<SuministroTB> empList) {
        for (int i = 0; i < empList.size(); i++) {
            SuministroTB suministroTB = empList.get(i);
            int rowIndex = (i + 1);

            String id = suministroTB.getId() + "";
            String descripcion = suministroTB.getClave() + "\n" + suministroTB.getNombreMarca();
            String cantidad = Tools.roundingValue(suministroTB.getCantidad(), 2);
            String bonificacion = Tools.roundingValue(suministroTB.getBonificacion(), 2);
            String unidadCompra = suministroTB.getUnidadCompraName();
            String impuesto = suministroTB.getImpuestoTB().getNombreImpuesto();
            String precio = ventaTB.getMonedaTB().getSimbolo() + ""
                    + Tools.roundingValue(suministroTB.getPrecioVentaGeneral(), 2);
            String descuento = Tools.roundingValue(suministroTB.getDescuento(), 2);
            String total = ventaTB.getMonedaTB().getSimbolo() + ""
                    + Tools.roundingValue(suministroTB.getPrecioVentaGeneral() * suministroTB.getCantidad(), 2);
            Node estado = suministroTB.getEstadoName().equalsIgnoreCase("C")
                    ? addElementGridPaneLabel("l10" + rowIndex, "COMPLETADO", Pos.CENTER_LEFT)
                    : addElementGridPaneButtonLlevar("l11" + rowIndex,
                            "LLEVAR \n" + Tools.roundingValue(suministroTB.getPorLlevar(), 2),
                            suministroTB.getIdSuministro(), suministroTB.getCostoCompra(), Pos.CENTER_LEFT);

            gpListVetanDetalle.add(addElementGridPaneLabel("l1" + rowIndex, id, Pos.CENTER), 0, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l2" + rowIndex, descripcion, Pos.CENTER_LEFT), 1, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l3" + rowIndex, cantidad, Pos.CENTER_RIGHT), 2, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l4" + rowIndex, bonificacion, Pos.CENTER_RIGHT), 3,
                    rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l5" + rowIndex, unidadCompra, Pos.CENTER_LEFT), 4,
                    rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l6" + rowIndex, impuesto, Pos.CENTER_RIGHT), 5, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l7" + rowIndex, precio, Pos.CENTER_RIGHT), 6, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l8" + rowIndex, descuento, Pos.CENTER_RIGHT), 7, rowIndex);
            gpListVetanDetalle.add(addElementGridPaneLabel("l9" + rowIndex, total, Pos.CENTER_RIGHT), 8, rowIndex);
            gpListVetanDetalle.add(estado, 9, rowIndex);
            gpListVetanDetalle.add(
                    addElementGridPaneButtonHistorial("l12" + rowIndex, "HISTORIAL", suministroTB, Pos.CENTER_LEFT), 10,
                    rowIndex);
        }
        calcularTotales();
    }

    private void createHeadTableIngresos() {
        gpListIngresos.add(addHeadGridPane("N°"), 0, 0);
        gpListIngresos.add(addHeadGridPane("Fecha"), 1, 0);
        gpListIngresos.add(addHeadGridPane("Banco"), 2, 0);
        gpListIngresos.add(addHeadGridPane("Detalle"), 3, 0);
        gpListIngresos.add(addHeadGridPane("Monto"), 4, 0);
        gpListIngresos.add(addHeadGridPane("Vuelto"), 5, 0);
    }

    private void createBodyTableIngresos(ArrayList<IngresoTB> ingresoTBs) {
        if (ingresoTBs.isEmpty()) {
            Label titulo = addElementGridPaneLabel("l1", "No hay ingresos para mostrar.", Pos.CENTER);
            gpListIngresos.add(titulo, 0, 1);
            GridPane.setRowSpan(titulo, GridPane.REMAINING);
            GridPane.setColumnSpan(titulo, GridPane.REMAINING);
            lblIngresos.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(0, 2));
        } else {
            double suma = 0;
            for (int i = 0; i < ingresoTBs.size(); i++) {
                IngresoTB ingresoTB = ingresoTBs.get(i);

                String id = ingresoTB.getId() + "";
                String fecha = ingresoTB.getFecha() + "\n" + ingresoTB.getHora();
                String cuenta = ingresoTB.getBancoTB().getNombreCuenta();
                String detalle = ingresoTB.getDetalle();
                String monto = ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(ingresoTB.getMonto(), 2);
                String vuelto = ventaTB.getMonedaTB().getSimbolo() + " "
                        + Tools.roundingValue(ingresoTB.getVuelto(), 2);

                gpListIngresos.add(addElementGridPaneLabel("l1" + (i + 1), id, Pos.CENTER), 0, (i + 1));
                gpListIngresos.add(addElementGridPaneLabel("l2" + (i + 1), fecha, Pos.CENTER_LEFT), 1, (i + 1));
                gpListIngresos.add(addElementGridPaneLabel("l3" + (i + 1), cuenta, Pos.CENTER_LEFT), 2, (i + 1));
                gpListIngresos.add(addElementGridPaneLabel("l4" + (i + 1), detalle, Pos.CENTER_LEFT), 3, (i + 1));
                gpListIngresos.add(addElementGridPaneLabel("l5" + (i + 1), monto, Pos.CENTER_LEFT), 4, (i + 1));
                gpListIngresos.add(addElementGridPaneLabel("l6" + (i + 1), vuelto, Pos.CENTER_LEFT), 5, (i + 1));
                suma += ingresoTB.getMonto();
            }
            lblIngresos.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(suma, 2));
        }
    }

    private void calcularTotales() {
        double importeBrutoTotal = 0;
        double descuentoTotal = 0;
        double subImporteNetoTotal = 0;
        double impuestoTotal = 0;
        double importeNetoTotal = 0;
        total = 0;

        for (SuministroTB ocdtb : ventaTB.getSuministroTBs()) {
            double importeBruto = ocdtb.getPrecioVentaGeneral() * ocdtb.getCantidad();
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

        total = importeNetoTotal;
        String simbolo = ventaTB.getMonedaTB().getSimbolo();

        lblValorVenta.setText(simbolo + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(simbolo + " -" + Tools.roundingValue(descuentoTotal, 2));
        lblSubTotal.setText(simbolo + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(simbolo + " " + Tools.roundingValue(impuestoTotal, 2));
        lblTotal.setText(simbolo + " " + Tools.roundingValue(importeNetoTotal, 2));
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

    private Label addElementGridPaneLabel(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.getStyleClass().add("labelRoboto13");
        label.setStyle(
                "-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Button addElementGridPaneButtonLlevar(String id, String nombre, String idSuministro, double costo,
            Pos pos) {
        Button button = new Button(nombre);
        button.setId(id);
        button.getStyleClass().add("buttonLightCancel");
        button.setAlignment(pos);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        ImageView imageView = new ImageView(new Image("/view/image/plus.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setOnAction(event -> {
            openWindowLlevar(idSuministro, costo);
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openWindowLlevar(idSuministro, costo);
            }
        });
        return button;
    }

    private Button addElementGridPaneButtonHistorial(String id, String nombre, SuministroTB suministroTB, Pos pos) {
        Button button = new Button(nombre);
        button.setId(id);
        button.getStyleClass().add("buttonLightCancel");
        button.setAlignment(pos);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Double.MAX_VALUE);
        ImageView imageView = new ImageView(new Image("/view/image/asignacion.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setOnAction(event -> {
            openWindowHistorial(suministroTB);
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                openWindowHistorial(suministroTB);
            }
        });
        return button;
    }

    private void onEventCancelar() {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_DEVOLUCION);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaDevolucionController controller = fXMLLoader.getController();
            controller.setInitVentaDetalle(this);
            controller.setLoadVentaDevolucion(ventaTB, Tools.roundingValue(total, 2));
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Anular la venta", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Error en venta detalle: " + ex.getLocalizedMessage());
        }
    }

    private void openWindowLlevar(String idSuministro, double costo) {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LLEVAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaLlevarController controller = fXMLLoader.getController();
            controller.setInitVentaDetalleController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Producto a llevar", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnShowing(w -> controller.setInitData(idVenta, idSuministro, lblComprobante.getText(), costo));
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
        }
    }

    private void openWindowHistorial(SuministroTB suministroTB) {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LLEVAR_HISTORIAL);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxVentaLlevarControllerHistorial controller = fXMLLoader.getController();
            controller.setInitVentaDetalleController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Historial del Producto",
                    apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnShowing(w -> controller.loadData(ventaTB, suministroTB));
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
        }
    }

    private void openWindowReporte() {
        opcionesImprimirController.getTicketVenta().mostrarReporte(idVenta);
    }

    private void onEventImprimirVenta() {
        opcionesImprimirController.getTicketVenta().imprimir(idVenta);
    }

    private void onEventClose() {
        principalController.getVbContent().getChildren().remove(apWindow);
        principalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setTopAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setRightAnchor(ventaRealizadasController.getWindow(), 0d);
        AnchorPane.setBottomAnchor(ventaRealizadasController.getWindow(), 0d);
        principalController.getVbContent().getChildren().add(ventaRealizadasController.getWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) throws IOException {
        onEventClose();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCancelar();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) throws IOException {
        onEventCancelar();
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventImprimirVenta();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        onEventImprimirVenta();
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        openWindowReporte();
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

    public void setInitVentasController(FxVentaRealizadasController ventaRealizadasController,
            FxPrincipalController principalController) {
        this.ventaRealizadasController = ventaRealizadasController;
        this.principalController = principalController;
    }

    public AnchorPane getWindow() {
        return apWindow;
    }

}
