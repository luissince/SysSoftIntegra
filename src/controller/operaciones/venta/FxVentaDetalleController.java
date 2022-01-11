package controller.operaciones.venta;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
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
import model.SuministroTB;
import model.VentaADO;
import model.VentaTB;

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
    private GridPane gpList;
    @FXML
    private Label lblValorVenta;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubTotal;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblTarjeta;
    @FXML
    private Label lblVuelto;
    @FXML
    private Label lblValor;
    @FXML
    private Text lbClienteInformacion;
    @FXML
    private Label lblImpuesto;
    @FXML
    private Label lblDeposito;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

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

    public void setInitComponents(String idVenta) {
        this.idVenta = idVenta;

        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return VentaADO.Obtener_Venta_ById(idVenta);
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
            Object result = task.getValue();
            if (result instanceof VentaTB) {
                ventaTB = (VentaTB) result;
                EmpleadoTB empleadoTB = ventaTB.getEmpleadoTB();

                lblFechaVenta.setText(ventaTB.getFechaVenta() + " " + ventaTB.getHoraVenta());
                lblCliente.setText(ventaTB.getClienteTB().getNumeroDocumento() + " " + ventaTB.getClienteTB().getInformacion());
                lbClienteInformacion.setText(ventaTB.getClienteTB().getTelefono() + "-" + ventaTB.getClienteTB().getCelular());
                lbCorreoElectronico.setText(ventaTB.getClienteTB().getEmail());
                lbDireccion.setText(ventaTB.getClienteTB().getDireccion());
                lblComprobante.setText(ventaTB.getComprobanteName() + " " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                lblObservaciones.setText(ventaTB.getObservaciones());
                lblTipo.setText(ventaTB.getTipoName() + " - " + ventaTB.getEstadoName());

                lblEfectivo.setText(Tools.roundingValue(ventaTB.getEfectivo(), 2));
                lblTarjeta.setText(Tools.roundingValue(ventaTB.getTarjeta(), 2));
                lblDeposito.setText(Tools.roundingValue(ventaTB.getDeposito(), 2));
                lblValor.setText(Tools.roundingValue(ventaTB.getTotal(), 2));
                lblVuelto.setText(Tools.roundingValue(ventaTB.getVuelto(), 2));

                if (empleadoTB != null) {
                    lblVendedor.setText(empleadoTB.getApellidos() + ", " + empleadoTB.getNombres());
                }

                ObservableList<SuministroTB> empList = FXCollections.observableArrayList(ventaTB.getSuministroTBs());
                
                gpList.getChildren().clear();
                createHeadTable();
                createBodyTable(empList);

                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) result);
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
    
    private void createHeadTable() {
        gpList.add(addHeadGridPane("N#"), 0, 0);
        gpList.add(addHeadGridPane("Descripción"), 1, 0);
        gpList.add(addHeadGridPane("Cantidad"), 2, 0);
        gpList.add(addHeadGridPane("Medida"), 3, 0);
        gpList.add(addHeadGridPane("Impuesto"), 4, 0);
        gpList.add(addHeadGridPane("Precio"), 5, 0);
        gpList.add(addHeadGridPane("Descuento"), 6, 0);
        gpList.add(addHeadGridPane("Importe"), 7, 0);
        gpList.add(addHeadGridPane("Por Llevar"), 8, 0);
        gpList.add(addHeadGridPane("Historial"), 9, 0);
    }

    private void createBodyTable(ObservableList<SuministroTB> empList) {        
        for (int i = 0; i < empList.size(); i++) {
            gpList.add(addElementGridPaneLabel("l1" + (i + 1), empList.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPaneLabel("l2" + (i + 1), empList.get(i).getClave() + "\n" + empList.get(i).getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPaneLabel("l3" + (i + 1), Tools.roundingValue(empList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 2, (i + 1));
            gpList.add(addElementGridPaneLabel("l4" + (i + 1), empList.get(i).getUnidadCompraName(), Pos.CENTER_LEFT), 3, (i + 1));
            gpList.add(addElementGridPaneLabel("l5" + (i + 1), empList.get(i).getImpuestoTB().getNombreImpuesto(), Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPaneLabel("l6" + (i + 1), ventaTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(empList.get(i).getPrecioVentaGeneral(), 2), Pos.CENTER_RIGHT), 5, (i + 1));
            gpList.add(addElementGridPaneLabel("l7" + (i + 1), Tools.roundingValue(empList.get(i).getDescuento(), 2), Pos.CENTER_RIGHT), 6, (i + 1));
            gpList.add(addElementGridPaneLabel("l8" + (i + 1), ventaTB.getMonedaTB().getSimbolo() + "" + Tools.roundingValue(empList.get(i).getPrecioVentaGeneral() * empList.get(i).getCantidad(), 2), Pos.CENTER_RIGHT), 7, (i + 1));
            gpList.add(empList.get(i).getEstadoName().equalsIgnoreCase("C")
                    ? addElementGridPaneLabel("l9" + (i + 1), "COMPLETADO", Pos.CENTER_LEFT)
                    : addElementGridPaneButtonLlevar("l9" + (i + 1), "LLEVAR \n" + Tools.roundingValue(empList.get(i).getPorLlevar(), 2), empList.get(i).getIdSuministro(), empList.get(i).getCostoCompra(), Pos.CENTER_LEFT),
                    8, (i + 1));
            gpList.add(addElementGridPaneButtonHistorial("l10" + (i + 1), "HISTORIAL", empList.get(i), Pos.CENTER_LEFT), 9, (i + 1));
        }
        calcularTotales();
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

        lblValorVenta.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeBrutoTotal, 2));
        lblDescuento.setText(ventaTB.getMonedaTB().getSimbolo() + " -" + Tools.roundingValue(descuentoTotal, 2));
        lblSubTotal.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(subImporteNetoTotal, 2));
        lblImpuesto.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(impuestoTotal, 2));
        lblTotal.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(importeNetoTotal, 2));
    }

    private Label addHeadGridPane(String nombre) {
        Label label = new Label(nombre);
        label.setTextFill(Color.web("#FFFFFF"));
        label.setStyle("-fx-background-color:  #020203;-fx-padding:  0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight:100;");
        label.getStyleClass().add("labelRoboto13");
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

    private Button addElementGridPaneButtonLlevar(String id, String nombre, String idSuministro, double costo, Pos pos) {
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
            //Controlller here
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
            //Controlller here
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
            Tools.println("Venta estructura openWindowLlevar: " + ex.getLocalizedMessage());
        }
    }

    private void openWindowHistorial(SuministroTB suministroTB) {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LLEVAR_HISTORIAL);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxVentaLlevarControllerHistorial controller = fXMLLoader.getController();
            controller.setInitVentaDetalleController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Historial del Producto", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnShowing(w -> controller.loadData(ventaTB, suministroTB));
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            Tools.println("Venta estructura openWindowHistorial: " + ex.getLocalizedMessage());
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

    public void setInitVentasController(FxVentaRealizadasController ventaRealizadasController, FxPrincipalController principalController) {
        this.ventaRealizadasController = ventaRealizadasController;
        this.principalController = principalController;
    }

    public AnchorPane getWindow() {
        return apWindow;
    }

}
