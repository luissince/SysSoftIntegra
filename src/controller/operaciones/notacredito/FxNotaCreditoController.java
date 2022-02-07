package controller.operaciones.notacredito;

import controller.menus.FxPrincipalController;
import controller.operaciones.venta.FxVentaListaController;
import controller.tools.FilesRouters;
import controller.tools.Session;
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
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.DetalleTB;
import model.DetalleVentaTB;
import model.MonedaTB;
import model.NotaCreditoADO;
import model.NotaCreditoDetalleTB;
import model.NotaCreditoTB;
import model.TipoDocumentoTB;
import model.VentaTB;

public class FxNotaCreditoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ScrollPane spBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private TextField txtSerieNumeracion;
    @FXML
    private ComboBox<TipoDocumentoTB> cbNotaCredito;
    @FXML
    private ComboBox<MonedaTB> cbMoneda;
    @FXML
    private DatePicker txtFechaRegistro;
    @FXML
    private ComboBox<TipoDocumentoTB> cbComprobante;
    @FXML
    private ComboBox<DetalleTB> cbMotivo;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtRazonsocial;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblImporteBruto;
    @FXML
    private Label lblDescuento;
    @FXML
    private Label lblSubImporte;
    @FXML
    private Label lblImporteNeto;
    @FXML
    private Label lblImpuesto;

    private FxPrincipalController principalController;

    private ArrayList<DetalleVentaTB> detalleVentaTBs;

    private String idVenta;

    private String idCliente;

    private int procedencia;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), txtFechaRegistro);
        detalleVentaTBs = new ArrayList<>();
        addElementPaneHead();
    }

    public void loadComponents(String comprobante) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return NotaCreditoADO.ListarComprobanteParaNotaCredito(comprobante);
            }
        };

        task.setOnScheduled(w -> {
            hbLoad.setVisible(true);
            spBody.setDisable(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            clearElements();
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ArrayList<TipoDocumentoTB> arrayNotaCredito = (ArrayList<TipoDocumentoTB>) objects[0];
                arrayNotaCredito.forEach(e -> cbNotaCredito.getItems().add(e));

                ArrayList<MonedaTB> arrayMoneda = (ArrayList<MonedaTB>) objects[1];
                arrayMoneda.forEach(e -> cbMoneda.getItems().add(e));

                ArrayList<TipoDocumentoTB> arrayTipoComprobante = (ArrayList<TipoDocumentoTB>) objects[2];
                arrayTipoComprobante.forEach(e -> cbComprobante.getItems().add(e));

                ArrayList<DetalleTB> arrayMotivoAnulacion = (ArrayList<DetalleTB>) objects[3];
                arrayMotivoAnulacion.forEach(e -> cbMotivo.getItems().add(e));

                ArrayList<DetalleTB> arrayTipoDocumento = (ArrayList<DetalleTB>) objects[4];
                arrayTipoDocumento.forEach(e -> cbTipoDocumento.getItems().add(e));

                VentaTB ventaTB = (VentaTB) objects[5];
                idVenta = ventaTB.getIdVenta();
                idCliente = ventaTB.getClienteTB().getIdCliente();
                txtSerieNumeracion.setText(ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                procedencia = ventaTB.getProcedencia();
                for (int i = 0; i < cbMoneda.getItems().size(); i++) {
                    if (cbMoneda.getItems().get(i).getIdMoneda() == ventaTB.getIdMoneda()) {
                        cbMoneda.getSelectionModel().select(i);
                        break;
                    }
                }

                for (int i = 0; i < cbComprobante.getItems().size(); i++) {
                    if (cbComprobante.getItems().get(i).getIdTipoDocumento() == ventaTB.getIdComprobante()) {
                        cbComprobante.getSelectionModel().select(i);
                        break;
                    }
                }

                for (int i = 0; i < cbTipoDocumento.getItems().size(); i++) {
                    if (cbTipoDocumento.getItems().get(i).getIdDetalle() == ventaTB.getClienteTB().getTipoDocumento()) {
                        cbTipoDocumento.getSelectionModel().select(i);
                        break;
                    }
                }

                if (cbNotaCredito.getItems().size() == 1) {
                    cbNotaCredito.getSelectionModel().select(0);
                }

                txtNumeroDocumento.setText(ventaTB.getClienteTB().getNumeroDocumento());
                txtRazonsocial.setText(ventaTB.getClienteTB().getInformacion());

                detalleVentaTBs.addAll((ArrayList<DetalleVentaTB>) objects[6]);
                detalleVentaTBs.forEach(e -> {
                    e.getBtnRemove().setOnAction(event -> {
                        detalleVentaTBs.remove(e);
                        addElementPaneHead();
                        addElementPaneBody();
                    });
                    e.getBtnRemove().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            detalleVentaTBs.remove(e);
                            addElementPaneHead();
                            addElementPaneBody();
                        }
                    });
                });

                addElementPaneHead();
                addElementPaneBody();

                hbLoad.setVisible(false);
                spBody.setDisable(false);
            } else if (object instanceof String) {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
            } else {
                lblMessageLoad.setText("Se produjo un error interno, comuníquese con su proveedor del sistema.");
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
            }
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void addElementPaneHead() {
        gpList.getChildren().clear();
        gpList.getColumnConstraints().get(0).setMinWidth(10);
        gpList.getColumnConstraints().get(0).setPrefWidth(80);
        gpList.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);
        gpList.getColumnConstraints().get(0).setHalignment(HPos.CENTER);

        gpList.getColumnConstraints().get(1).setMinWidth(10);
        gpList.getColumnConstraints().get(1).setPrefWidth(240);
        gpList.getColumnConstraints().get(1).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(2).setMinWidth(10);
        gpList.getColumnConstraints().get(2).setPrefWidth(160);
        gpList.getColumnConstraints().get(2).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(3).setMinWidth(10);
        gpList.getColumnConstraints().get(3).setPrefWidth(160);
        gpList.getColumnConstraints().get(3).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(4).setMinWidth(10);
        gpList.getColumnConstraints().get(4).setPrefWidth(160);
        gpList.getColumnConstraints().get(4).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(5).setMinWidth(10);
        gpList.getColumnConstraints().get(5).setPrefWidth(160);
        gpList.getColumnConstraints().get(5).setHgrow(Priority.SOMETIMES);

        gpList.add(addElementGridPaneLabel("l01", "Quitar"), 0, 0);
        gpList.add(addElementGridPaneLabel("l02", "Detalle"), 1, 0);
        gpList.add(addElementGridPaneLabel("l03", "Unidad"), 2, 0);
        gpList.add(addElementGridPaneLabel("l04", "Cantidad"), 3, 0);
        gpList.add(addElementGridPaneLabel("l05", "Precio Unit."), 4, 0);
        gpList.add(addElementGridPaneLabel("l06", "Importe"), 5, 0);
    }

    private void addElementPaneBody() {
        double totalImporteBruto = 0;
        double totalDescuento = 0;
        double totalSubImporte = 0;
        double totalImpuesto = 0;
        double totalImporteNeto = 0;

        for (int i = 0; i < detalleVentaTBs.size(); i++) {
            double cantidad = detalleVentaTBs.get(i).getSuministroTB().getCantidad();
            double impuesto = detalleVentaTBs.get(i).getSuministroTB().getImpuestoTB().getValor();
            double precioVenta = detalleVentaTBs.get(i).getSuministroTB().getPrecioVentaGeneral();
            double descuento = detalleVentaTBs.get(i).getSuministroTB().getDescuento();

            double precioBruto = precioVenta / ((impuesto / 100.00) + 1);
            double impuestoGenerado = precioBruto * (impuesto / 100.00);
            double impuestoTotal = impuestoGenerado * cantidad;
            double importeBrutoTotal = precioBruto * cantidad;
            double importeNeto = precioBruto + impuestoGenerado;
            double importeNetoTotal = importeBrutoTotal + impuestoTotal;

            gpList.add(detalleVentaTBs.get(i).getBtnRemove(), 0, (i + 1));
            gpList.add(addElementGridPaneLabel("l1" + (i + 1), detalleVentaTBs.get(i).getSuministroTB().getClave() + "\n" + detalleVentaTBs.get(i).getSuministroTB().getNombreMarca(), Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPaneLabel("l2" + (i + 1), detalleVentaTBs.get(i).getSuministroTB().getUnidadCompraName(), Pos.CENTER_LEFT), 2, (i + 1));
            gpList.add(addElementGridPaneLabel("l3" + (i + 1), Tools.roundingValue(cantidad, 2), Pos.CENTER_RIGHT), 3, (i + 1));
            gpList.add(addElementGridPaneLabel("l4" + (i + 1), Tools.roundingValue(importeNeto, 2), Pos.CENTER_RIGHT), 4, (i + 1));
            gpList.add(addElementGridPaneLabel("l5" + (i + 1), Tools.roundingValue(importeNetoTotal, 2), Pos.CENTER_RIGHT), 5, (i + 1));

            totalImporteBruto += importeBrutoTotal;
            totalDescuento += descuento;
            totalSubImporte += importeBrutoTotal;
            totalImpuesto += impuestoTotal;
            totalImporteNeto += importeNetoTotal;
        }

        lblImporteBruto.setText(Tools.roundingValue(totalImporteBruto, 2));
        lblDescuento.setText(Tools.roundingValue(totalDescuento, 2));
        lblSubImporte.setText(Tools.roundingValue(totalSubImporte, 2));
        lblImpuesto.setText(Tools.roundingValue(totalImpuesto, 2));
        lblImporteNeto.setText(Tools.roundingValue(totalImporteNeto, 2));
    }

    private Label addElementGridPaneLabel(String id, String nombre) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-background-color: #020203;-fx-text-fill:#ffffff;-fx-padding: 0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight: 100");
        label.getStyleClass().add("labelOpenSansRegular13");
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
        label.setStyle("-fx-text-fill:#020203;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    public void clearElements() {
        idVenta = "";
        idCliente = "";
        cbNotaCredito.getItems().clear();
        cbNotaCredito.getSelectionModel().select(null);
        cbMoneda.getItems().clear();
        cbMoneda.getSelectionModel().select(null);
        Tools.actualDate(Tools.getDate(), txtFechaRegistro);
        cbComprobante.getItems().clear();
        cbComprobante.getSelectionModel().select(null);
        txtSerieNumeracion.clear();
        cbMotivo.getItems().clear();
        cbMotivo.getSelectionModel().select(null);
        cbTipoDocumento.getItems().clear();
        cbTipoDocumento.getSelectionModel().select(null);
        txtNumeroDocumento.clear();
        txtRazonsocial.clear();
        detalleVentaTBs.clear();
        addElementPaneHead();
        addElementPaneBody();
        txtSerieNumeracion.requestFocus();
    }

    private void registrarNotaCredito() throws IOException {
        if (cbNotaCredito.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Debe seleccionar la nota de crédito.");
            cbNotaCredito.requestFocus();
        } else if (cbMoneda.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Debe seleccionar la moneda.");
            cbMoneda.requestFocus();
        } else if (txtFechaRegistro.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Ingrese la fecha de registro.");
            txtFechaRegistro.requestFocus();
        } else if (cbComprobante.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Debe seleccionar el tipo de comprobante.");
            cbComprobante.requestFocus();
        } else if (cbMotivo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Debe seleccionar el motivo de la nota de crédito.");
            cbMotivo.requestFocus();
        } else if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Debe seleccionar el tipo de documento de identidad");
            cbTipoDocumento.requestFocus();
        } else if (Tools.isText(txtNumeroDocumento.getText())) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Ingrese el  número de documento de identidad.");
            txtNumeroDocumento.requestFocus();
        } else if (Tools.isText(txtRazonsocial.getText())) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "Ingreso los datos del cliente.");
            txtRazonsocial.requestFocus();
        } else if (detalleVentaTBs.isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "No hay datos en el detalle para guardar el documento.");
        } else {
            NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
            notaCreditoTB.setIdVendedor(Session.USER_ID);
            notaCreditoTB.setIdCliente(idCliente);
            notaCreditoTB.setIdComprobante(cbNotaCredito.getSelectionModel().getSelectedItem().getIdTipoDocumento());
            notaCreditoTB.setIdMoneda(cbMoneda.getSelectionModel().getSelectedItem().getIdMoneda());
            notaCreditoTB.setIdMotivo(cbMotivo.getSelectionModel().getSelectedItem().getIdDetalle());
            notaCreditoTB.setFechaRegistro(Tools.getDatePicker(txtFechaRegistro));
            notaCreditoTB.setHoraRegistro(Tools.getTime());
            notaCreditoTB.setEstado(1);
            notaCreditoTB.setIdVenta(idVenta);
            ArrayList<NotaCreditoDetalleTB> creditoDetalleTBs = new ArrayList<>();
            detalleVentaTBs.forEach(f -> {
                NotaCreditoDetalleTB ncdtb = new NotaCreditoDetalleTB();
                ncdtb.setIdSuministro(f.getIdArticulo());
                ncdtb.setCantidad(f.getSuministroTB().getCantidad());
                ncdtb.setPrecio(f.getSuministroTB().getPrecioVentaGeneral());
                ncdtb.setDescuento(f.getSuministroTB().getDescuento());
                ncdtb.setIdImpuesto(f.getSuministroTB().getIdImpuesto());
                ncdtb.setImpuestoTB(f.getSuministroTB().getImpuestoTB());
                ncdtb.setSuministroTB(f.getSuministroTB());
                creditoDetalleTBs.add(ncdtb);
            });
            notaCreditoTB.setNotaCreditoDetalleTBs(creditoDetalleTBs);

            short value = Tools.AlertMessageConfirmation(apWindow, "Nota de Crédito", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = NotaCreditoADO.Registrar_NotaCredito(notaCreditoTB, procedencia);
                if (result.equalsIgnoreCase("registrado")) {
                    Tools.AlertMessageInformation(apWindow, "Nota de Crédito", "Se registró correctamente la nota de crédito.");
                    clearElements();
                } else if (result.equalsIgnoreCase("nota")) {
                    Tools.AlertMessageWarning(apWindow, "Nota de Crédito", "El comprobante ya tiene asociado una nota de crédito.");
                } else {
                    Tools.AlertMessageError(apWindow, "Nota de Crédito", result);
                }
            }
        }
    }

    private void openWindowVentas() {
        try {
            principalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxVentaListaController controller = fXMLLoader.getController();
            controller.setInitNotaCreditoController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione una venta", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> principalController.closeFondoModal());
            stage.setOnShown(w -> {
                controller.getTxtBuscar().requestFocus();
                controller.loadInit();
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onActionBuscarComprobante(ActionEvent event) {
        loadComponents(txtSerieNumeracion.getText().trim());
    }

    @FXML
    private void onKeyPressedBuscarComprobante(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadComponents(txtSerieNumeracion.getText().trim());
        }
    }

    @FXML
    private void onKeyPressedAceptarLoad(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            clearElements();
            hbLoad.setVisible(false);
            spBody.setDisable(false);

        }
    }

    @FXML
    private void onActionAceptarLoad(ActionEvent event) {
        clearElements();
        hbLoad.setVisible(false);
        spBody.setDisable(false);
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            registrarNotaCredito();
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) throws IOException {
        registrarNotaCredito();
    }

    @FXML
    private void onKeyPressedLimpiar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            clearElements();
        }
    }

    @FXML
    private void onActionLimpiar(ActionEvent event) {
        clearElements();
    }

    @FXML
    private void onKeyPressedVentas(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentas();
        }
    }

    @FXML
    private void onActionVentas(ActionEvent event) {
        openWindowVentas();
    }

    public ScrollPane getSpBody() {
        return spBody;
    }

    public HBox getHbLoad() {
        return hbLoad;
    }

    public void setContent(FxPrincipalController principalController) {
        this.principalController = principalController;
    }

}
