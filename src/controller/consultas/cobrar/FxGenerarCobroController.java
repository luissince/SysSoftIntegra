package controller.consultas.cobrar;

import controller.tools.ConvertMonedaCadena;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.BancoTB;
import model.CajaTB;
import model.IngresoTB;
import model.ModeloObject;
import model.MovimientoCajaTB;
import model.VentaCreditoTB;
import model.VentaTB;
import service.BancoADO;
import service.CajaADO;
import service.VentaADO;

public class FxGenerarCobroController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblVueltoContadoNombre;
    @FXML
    private Label lblVueltoContado;
    @FXML
    private Button btnAceptar;
    @FXML
    private TextField txtObservacion;
    @FXML
    private VBox vbViewEfectivo;
    @FXML
    private ComboBox<BancoTB> cbMetodoTransaccion;
    @FXML
    private VBox vbContenedorMetodoPago;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private VBox vbContenido;
    @FXML
    private Label lblMonedaLetras;

    private FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private double vueltoContado;

    private double totalVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        totalVenta = 0;
        vueltoContado = 0.00;
        monedaCadena = new ConvertMonedaCadena();
        lblVueltoContadoNombre.setText("SU CAMBIO");
    }

    // private void setInitLoadVentaAbono(VentaTB ventaTB) {
    // this.ventaTB = ventaTB;
    // }

    // private void setInitLoadVentaAbono(VentaTB ventaTB, String idVentaCredito,
    // double monto) {
    // this.ventaTB = ventaTB;
    // this.idVentaCredito = idVentaCredito;
    // txtMonto.setText(Tools.roundingValue(monto, 2));
    // txtMonto.setDisable(true);
    // }

    public void loadInitComponents(VentaTB ventaTB) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ObservableList<BancoTB>> task = new Task<ObservableList<BancoTB>>() {
            @Override
            public ObservableList<BancoTB> call() throws Exception {
                Object result = BancoADO.ObtenerListarBancos();
                if (result instanceof ObservableList<?>) {
                    return (ObservableList<BancoTB>) result;
                } else {
                    throw new Exception("El objeto devuelto por el método no es una lista observable de bancos.");
                }
            }
        };

        task.setOnScheduled(e -> {
            hbLoadProcesando.setVisible(true);
            lblTextoProceso.setText("PROCESANDO INFORMACIÓN...");
            btnCancelarProceso.setText("Cancelar Proceso");
            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }
            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                Tools.Dispose(window);
            });
        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            this.ventaTB = ventaTB;
            ObservableList<BancoTB> bancos = task.getValue();
            cbMetodoTransaccion.getItems().addAll(bancos);

            Optional<BancoTB> bancoConFormaPago = bancos.stream().filter(b -> b.getFormaPago() == 1).findFirst();
            bancoConFormaPago.ifPresent(this::generarMetodoPago);

            totalVenta = Double.parseDouble(Tools.roundingValue(ventaTB.getMontoRestante(), 2));

            lblTotal.setText(
                    "TOTAL A PAGAR: " + ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(totalVenta, 2));
            lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));
            lblMonedaLetras.setText(monedaCadena.Convertir(Tools.roundingValue(totalVenta, 2), true,
                    ventaTB.getMonedaTB().getNombre()));
            vbContenido.setDisable(false);
            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventAceptar() {
        double montoActual = 0;

        if (vbContenedorMetodoPago.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(window, "Cobro", "No hay metodos de pago para continuar.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        for (Node node : vbContenedorMetodoPago.getChildren()) {
            HBox hbox = (HBox) node;

            TextField txtMonto = null;
            VBox vbMonto = (VBox) hbox.getChildren().get(0);
            if (vbMonto != null) {
                for (Node nodo : vbMonto.getChildren()) {
                    if (nodo instanceof TextField) {
                        txtMonto = (TextField) nodo;
                        break;
                    }
                }
            }

            if (!Tools.isNumeric(txtMonto.getText().trim())) {
                Tools.AlertMessageWarning(window, "Cobro", "Ingrese el monto a pagar o quítelo.");
                txtMonto.requestFocus();
                return;
            }

            double monto = Double.parseDouble(txtMonto.getText().trim());

            if (monto <= 0) {
                Tools.AlertMessageWarning(window, "Cobro", "El monto no debe ser menor a cero.");
                txtMonto.requestFocus();
                return;
            }

            montoActual += monto;
        }

        if (montoActual < totalVenta) {
            Tools.AlertMessageWarning(window, "Cobro",
                    "Los montos ingresados deben ser igual o mayor al total de la venta.");

            for (Node node : vbContenedorMetodoPago.getChildren()) {
                HBox hbox = (HBox) node;
                VBox vbMonto = (VBox) hbox.getChildren().get(0);
                if (vbMonto != null) {
                    if (!vbMonto.getChildren().isEmpty()) {
                        ((TextField) vbMonto.getChildren().get(vbMonto.getChildren().size() - 1)).requestFocus();
                    }
                }
            }

            return;
        }

        if (vbContenedorMetodoPago.getChildren().size() > 1) {
            if (montoActual != totalVenta) {
                Tools.AlertMessageWarning(window, "Cobro",
                        "Si se eligen más de dos métodos de pago, no se podrá recibir vuelto o cambio.");
                return;
            }
        }

        List<IngresoTB> ingresoTBs = new ArrayList<>();

        for (Node node : vbContenedorMetodoPago.getChildren()) {
            HBox hbox = (HBox) node;

            TextField txtMonto = null;
            VBox vbMonto = (VBox) hbox.getChildren().get(0);
            if (vbMonto != null) {
                for (Node nodo : vbMonto.getChildren()) {
                    if (nodo instanceof TextField) {
                        txtMonto = (TextField) nodo;
                        break;
                    }
                }
            }

            TextField txtOperacion = null;
            VBox vbOperacion = (VBox) hbox.getChildren().get(1);
            if (vbOperacion != null) {
                HBox hbContenidoOperacion = (HBox) vbOperacion.getChildren().get(1);
                if (hbContenidoOperacion != null) {
                    for (Node nodo : hbContenidoOperacion.getChildren()) {
                        if (nodo instanceof TextField) {
                            txtOperacion = (TextField) nodo;
                            break;
                        }
                    }
                }
            }

            double monto = Double.parseDouble(txtMonto.getText().trim());

            IngresoTB ingresoTB = new IngresoTB();
            ingresoTB.setIdBanco(hbox.getId());
            ingresoTB.setMonto(monto - vueltoContado);
            ingresoTB.setVuelto(vueltoContado);
            ingresoTB.setOperacion(txtOperacion.getText().trim());
            ingresoTBs.add(ingresoTB);
        }

        VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
        ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
        ventaCreditoTB.setMonto(montoActual);
        ventaCreditoTB.setEstado((short) 1);
        ventaCreditoTB.setIdUsuario(Session.USER_ID);
        ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

        short value = Tools.AlertMessageConfirmation(window, "Cobro", "¿Está seguro de continuar?");
        if (value == 1) {
            String[] result = VentaADO.RegistrarAbono(ventaCreditoTB, ingresoTBs);
            if (result[0] == "1") {
                Tools.Dispose(window);
                cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(), result[2]);
            } else {
                Tools.AlertMessageWarning(window, "Cobro", result[1]);
            }
        }

        // if (!Tools.isNumeric(txtMonto.getText().trim())) {
        // Tools.AlertMessageWarning(window, "Abonar", "Ingrese el abono.");
        // txtMonto.requestFocus();
        // } else if (Double.parseDouble(txtMonto.getText().trim()) <= 0) {
        // Tools.AlertMessageWarning(window, "Abonar", "El abono no puede ser menor a
        // 0.");
        // txtMonto.requestFocus();
        // } else if (Tools.isText(txtObservacion.getText())) {
        // Tools.AlertMessageWarning(window, "Abonar", "Ingrese alguna observación del
        // abono.");
        // txtObservacion.requestFocus();
        // } else {
        // if (rbIngreso.isSelected()) {
        // short value = Tools.AlertMessageConfirmation(window, "Abonor", "¿Está seguro
        // de continuar?");
        // if (value == 1) {
        // btnAceptar.setDisable(true);
        // VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
        // ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
        // ventaCreditoTB.setIdVentaCredito(idVentaCredito);
        // ventaCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
        // ventaCreditoTB.setFechaPago(Tools.getDate());
        // ventaCreditoTB.setHoraPago(Tools.getTime());
        // ventaCreditoTB.setEstado((short) 1);
        // ventaCreditoTB.setIdUsuario(Session.USER_ID);
        // ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

        // /*
        // * procedencia
        // * 1 = venta
        // * 2 = compra
        // *
        // * 3 = ingreso libre
        // * 4 = salida libre
        // *
        // * 5 = ingreso cuentas por cobrar
        // * 6 = salida cuentas por pagar
        // */

        // /*
        // * forma
        // * 1 = efectivo
        // * 2 = tarjeta
        // * 3 = deposito
        // */
        // IngresoTB ingresoTB = new IngresoTB();
        // ingresoTB.setIdProcedencia("");
        // ingresoTB.setIdUsuario(Session.USER_ID);
        // ingresoTB.setDetalle("INGRESO DE DINERO POR CUENTAS POR COBRAR DEL
        // COMPROBANTE "
        // + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
        // ingresoTB.setProcedencia(5);
        // ingresoTB.setFecha(Tools.getDate());
        // ingresoTB.setHora(Tools.getTime());
        // ingresoTB.setForma(rbEfectivo.isSelected() ? 1 : rbTarjeta.isSelected() ? 2 :
        // 3);
        // ingresoTB.setMonto(Double.parseDouble(txtMonto.getText()));

        // ModeloObject result = idVentaCredito.equals("")
        // ? VentaADO.RegistrarAbono(ventaCreditoTB, ingresoTB, null)
        // : VentaADO.RegistrarAbonoUpdateById(ventaCreditoTB, ingresoTB, null);
        // if (result.getState().equalsIgnoreCase("inserted")) {
        // Tools.Dispose(window);
        // cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(),
        // result.getIdResult());
        // cuentasPorCobrarVisualizarController.loadData(ventaTB.getIdVenta());
        // } else if (result.getState().equalsIgnoreCase("error")) {
        // Tools.AlertMessageWarning(window, "Abonar", result.getMessage());
        // } else {
        // Tools.AlertMessageError(window, "Abonar",
        // "No se completo el proceso por problemas de conexión.");
        // btnAceptar.setDisable(false);
        // }
        // }
        // } else {
        // short value = Tools.AlertMessageConfirmation(window, "Abonor", "¿Está seguro
        // de continuar?");
        // if (value == 1) {
        // Object object = CajaADO.ValidarCreacionCaja(Session.USER_ID);
        // if (object instanceof CajaTB) {
        // CajaTB cajaTB = (CajaTB) object;
        // if (cajaTB.getId() == 2) {
        // /*
        // * Tipo Movimiento
        // * TipoMovimiento = 1 apertura caja
        // * TipoMovimiento = 2 venta efectivo
        // * TipoMovimiento = 3 venta tarjeta
        // * TipoMovimiento = 4 ingresos efectivo
        // * TipoMovimiento = 5 salidas efectivo
        // * TipoMovimiento = 6 venta deposito
        // * TipoMovimiento = 7 ingresos tarjeta
        // * TipoMovimiento = 8 ingresos deposito
        // * TipoMovimiento = 9 salidas tarjeta
        // * TipoMovimiento = 10 salidas deposito
        // */
        // btnAceptar.setDisable(true);
        // VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
        // ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
        // ventaCreditoTB.setIdVentaCredito(idVentaCredito);
        // ventaCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
        // ventaCreditoTB.setFechaPago(Tools.getDate());
        // ventaCreditoTB.setHoraPago(Tools.getTime());
        // ventaCreditoTB.setEstado((short) 1);
        // ventaCreditoTB.setIdUsuario(Session.USER_ID);
        // ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

        // MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
        // movimientoCajaTB.setIdCaja(cajaTB.getIdCaja());
        // movimientoCajaTB.setFechaMovimiento(Tools.getDate());
        // movimientoCajaTB.setHoraMovimiento(Tools.getTime());
        // movimientoCajaTB.setComentario("INGRESO DE DINERO POR CUENTAS POR COBRAR DEL
        // COMPROBANTE "
        // + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
        // movimientoCajaTB
        // .setTipoMovimiento(rbEfectivo.isSelected() ? 4 : rbTarjeta.isSelected() ? 7 :
        // 8);
        // movimientoCajaTB.setMonto(Double.parseDouble(txtMonto.getText()));

        // ModeloObject result = idVentaCredito.equals("")
        // ? VentaADO.RegistrarAbono(ventaCreditoTB, null, movimientoCajaTB)
        // : VentaADO.RegistrarAbonoUpdateById(ventaCreditoTB, null, movimientoCajaTB);
        // if (result.getState().equalsIgnoreCase("inserted")) {
        // Tools.Dispose(window);
        // cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(),
        // result.getIdResult());
        // cuentasPorCobrarVisualizarController.loadData(ventaTB.getIdVenta());
        // } else if (result.getState().equalsIgnoreCase("error")) {
        // Tools.AlertMessageWarning(window, "Abonar", result.getMessage());
        // } else {
        // Tools.AlertMessageError(window, "Abonar",
        // "No se completo el proceso por problemas de conexión.");
        // btnAceptar.setDisable(false);
        // }
        // } else {
        // Tools.AlertMessageWarning(window, "Abonar",
        // "No tiene ninguna caja aperturada para continuar con el proceso.");
        // }
        // } else {
        // Tools.AlertMessageError(window, "Abonar",
        // "No se pudo obtener el estado de su aja por problemas de red, intente
        // nuevamente.");
        // }
        // }
        // }
        // }

    }

    private void addMetodoPago() {
        if (cbMetodoTransaccion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Venta", "Seleccione el método de pago.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        List<Node> vBoxFiltrados = vbContenedorMetodoPago.getChildren()
                .stream()
                .filter(vBoxFilter -> ((HBox) vBoxFilter).getId() == cbMetodoTransaccion.getSelectionModel()
                        .getSelectedItem().getIdBanco())
                .collect(Collectors.toList());

        if (vBoxFiltrados.size() != 0) {
            Tools.AlertMessageWarning(window, "Venta", "Ya existe en la lista el método de pago.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        generarMetodoPago(cbMetodoTransaccion.getSelectionModel().getSelectedItem());
    }

    private void generarMetodoPago(BancoTB bancoTB) {
        HBox hBox = new HBox();
        hBox.setId(bancoTB.getIdBanco());
        hBox.setStyle("-fx-spacing: 0.6666666666666666em;");

        VBox vbMonto = new VBox();
        HBox.setHgrow(vbMonto, Priority.ALWAYS);
        vbMonto.setStyle("-fx-spacing: 0.5333333333333333em;");
        Label lblMonto = new Label(
                "Monto - " + bancoTB.getNombreCuenta());
        lblMonto.getStyleClass().add("labelOpenSansRegular13");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("0.00");
        txtMonto.getStyleClass().add("text-field-normal");
        txtMonto.setPrefWidth(260);
        txtMonto.setPrefHeight(30);
        txtMonto.setOnAction(event -> onEventAceptar());
        txtMonto.setOnKeyReleased(event -> {
            if (txtMonto.getText().isEmpty()) {
                vueltoContado = totalVenta;
                generarVuelto();
                return;
            }
            if (Tools.isNumeric(txtMonto.getText())) {
                generarVuelto();
            }
        });
        txtMonto.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && txtMonto.getText().contains(".")) {
                event.consume();
            }
        });
        vbMonto.getChildren().addAll(lblMonto, txtMonto);

        VBox vbOperacion = new VBox();
        HBox.setHgrow(vbOperacion, Priority.ALWAYS);
        vbOperacion.setStyle("-fx-spacing: 0.5333333333333333em;");
        Label lblOperacion = new Label(
                "N° de Operación - " + bancoTB.getNombreCuenta());
        lblOperacion.getStyleClass().add("labelOpenSansRegular13");

        HBox hbContendorOperacion = new HBox();
        TextField txtOperacion = new TextField();
        txtOperacion.setPromptText(
                "N° Operación");
        txtOperacion.getStyleClass().add("text-field-normal");
        txtOperacion.setPrefWidth(260);
        txtOperacion.setPrefHeight(30);
        txtOperacion.setOnAction(event -> onEventAceptar());
        Button btnQuitar = new Button();
        btnQuitar.setPrefHeight(30);
        btnQuitar.setMaxHeight(Double.MAX_VALUE);
        btnQuitar.getStyleClass().add("buttonLightError");
        ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btnQuitar.setGraphic(imageView);
        btnQuitar.setOnAction(event -> {
            vbContenedorMetodoPago.getChildren().remove(hBox);
            generarVuelto();
        });
        btnQuitar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                vbContenedorMetodoPago.getChildren().remove(hBox);
                generarVuelto();
            }
        });
        hbContendorOperacion.getChildren().addAll(txtOperacion, btnQuitar);
        vbOperacion.getChildren().addAll(lblOperacion, hbContendorOperacion);

        hBox.getChildren().addAll(vbMonto, vbOperacion);

        vbContenedorMetodoPago.getChildren().add(hBox);
        txtMonto.requestFocus();
    }

    private void generarVuelto() {
        double montoActual = 0;

        for (Node contenedorMetodoPagoNode : vbContenedorMetodoPago.getChildren()) {
            HBox hbox = (HBox) contenedorMetodoPagoNode;

            VBox contenedorMonto = (VBox) hbox.getChildren().get(0);
            if (contenedorMonto != null) {
                for (Node nodo : contenedorMonto.getChildren()) {
                    if (nodo instanceof TextField) {
                        TextField txtMonto = (TextField) nodo;
                        if (Tools.isNumeric(txtMonto.getText().trim())) {
                            montoActual += Double.parseDouble(txtMonto.getText().trim());
                        }
                    }
                }
            }

        }

        if (montoActual >= totalVenta) {
            vueltoContado = montoActual - totalVenta;
            lblVueltoContadoNombre.setText("SU CAMBIO ES");
        } else {
            vueltoContado = totalVenta - montoActual;
            lblVueltoContadoNombre.setText("POR PAGAR");
        }

        lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));
    }

    @FXML
    private void onKeyPressedAgregarMetodoPago(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addMetodoPago();
            event.consume();
        }
    }

    @FXML
    private void onActionAgregarMetodoPago(ActionEvent event) {
        addMetodoPago();
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    public AnchorPane getWindow() {
        return window;
    }

    public void setInitVentaAbonarController(
            FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController) {
        this.cuentasPorCobrarVisualizarController = cuentasPorCobrarVisualizarController;
    }

}
