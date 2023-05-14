package controller.operaciones.compras;

import controller.tools.ConvertMonedaCadena;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.time.LocalDate;
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.BancoTB;
import model.CompraTB;
import model.EgresoTB;
import model.LoteTB;
import service.BancoADO;
import service.CompraADO;

public class FxComprasProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTotal;
    @FXML
    private VBox vbContado;
    @FXML
    private HBox hbCredito;
    @FXML
    private DatePicker txtFechaVencimiento;
    @FXML
    private Button btnContado;
    @FXML
    private Button btnCredito;
    @FXML
    private ComboBox<BancoTB> cbMetodoTransaccion;
    @FXML
    private VBox vbContenedorMetodoPago;
    @FXML
    private VBox hbContenido;
    @FXML
    private Label lblMonedaLetras;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private Label lblVueltoContadoNombre;
    @FXML
    private Label lblVueltoContado;

    private ConvertMonedaCadena monedaCadena;

    private FxComprasController comprasController;

    private CompraTB compraTB;

    private ObservableList<LoteTB> loteTBs;

    private double vueltoContado;

    private double totalCompra = 0;

    private short metodoVentaSelecciondo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        metodoVentaSelecciondo = 0;
        vueltoContado = 0.00;

        monedaCadena = new ConvertMonedaCadena();
        lblVueltoContadoNombre.setText("SU CAMBIO");

        txtFechaVencimiento.setDayCellFactory(picket -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
                if (date.isEqual(LocalDate.now())) {
                    setDisable(true);
                }
            };
        });
    }

    public void loadInitComponents(CompraTB compraTB, ObservableList<LoteTB> loteTBs) {
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
                Tools.Dispose(apWindow);
            });
        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            ObservableList<BancoTB> bancos = task.getValue();
            cbMetodoTransaccion.getItems().addAll(bancos);

            Optional<BancoTB> bancoConFormaPago1 = bancos.stream().filter(b -> b.getFormaPago() == 1).findFirst();
            bancoConFormaPago1.ifPresent(this::generarMetodoPago);

            this.compraTB = compraTB;
            this.loteTBs = loteTBs;
            totalCompra = Double.parseDouble(Tools.roundingValue(compraTB.getTotal(), 2));

            lblTotal.setText(
                    "TOTAL A PAGAR: " + compraTB.getMonedaTB().getSimbolo() + " "
                            + Tools.roundingValue(totalCompra, 2));

            lblMonedaLetras.setText(
                    monedaCadena.Convertir(Tools.roundingValue(totalCompra, 2), true,
                            compraTB.getMonedaTB().getNombre()));

            hbContenido.setDisable(false);

            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void addMetodoPago() {
        if (cbMetodoTransaccion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Seleccione el método de pago.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        List<Node> vBoxFiltrados = vbContenedorMetodoPago.getChildren()
                .stream()
                .filter(vBoxFilter -> ((HBox) vBoxFilter).getId() == cbMetodoTransaccion.getSelectionModel()
                        .getSelectedItem().getIdBanco())
                .collect(Collectors.toList());

        if (vBoxFiltrados.size() != 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Ya existe en la lista el método de pago.");
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
                vueltoContado = totalCompra;
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

        if (montoActual >= totalCompra) {
            vueltoContado = montoActual - totalCompra;
            lblVueltoContadoNombre.setText("SU CAMBIO ES");
        } else {
            vueltoContado = totalCompra - montoActual;
            lblVueltoContadoNombre.setText("POR PAGAR");
        }

        lblVueltoContado.setText(compraTB.getMonedaTB().getSimbolo() + " "
                + Tools.roundingValue(vueltoContado, 2));
    }

    private void onEventAceptar() {
        switch (metodoVentaSelecciondo) {
            case 0:
                onEventContado();
                break;
            case 1:
                onEventCredito();
                break;
        }
    }

    private void onEventContado() {
        double montoActual = 0;

        if (vbContenedorMetodoPago.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Venta", "No hay metodos de pago para continuar.");
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
                Tools.AlertMessageWarning(apWindow, "Venta", "Ingrese el monto a pagar o quítelo.");
                txtMonto.requestFocus();
                return;
            }

            double monto = Double.parseDouble(txtMonto.getText().trim());

            if (monto <= 0) {
                Tools.AlertMessageWarning(apWindow, "Venta", "El monto no debe ser menor a cero.");
                txtMonto.requestFocus();
                return;
            }

            montoActual += monto;
        }

        if (montoActual < totalCompra) {
            Tools.AlertMessageWarning(apWindow, "Venta",
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
            if (montoActual != totalCompra) {
                Tools.AlertMessageWarning(apWindow, "Venta",
                        "Si se eligen más de dos métodos de pago, no se podrá recibir vuelto o cambio.");
                return;
            }
        }

        List<EgresoTB> egresoTBs = new ArrayList<>();

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

            EgresoTB ingresoTB = new EgresoTB();
            ingresoTB.setIdBanco(hbox.getId());
            ingresoTB.setIdUsuario(Session.USER_ID);
            ingresoTB.setIdCliente(compraTB.getIdProveedor());
            ingresoTB.setDetalle("");
            ingresoTB.setMonto(monto - vueltoContado);
            ingresoTB.setVuelto(vueltoContado);
            ingresoTB.setOperacion(txtOperacion.getText().trim());
            ingresoTB.setFecha(Tools.getDate());
            ingresoTB.setHora(Tools.getTime());
            egresoTBs.add(ingresoTB);
        }

        short value = Tools.AlertMessageConfirmation(apWindow, "Compra", "¿Está seguro de continuar?");
        if (value == 1) {
            compraTB.setTipo(1);
            compraTB.setEstado(1);

            compraTB.setEfectivo(false);
            compraTB.setTarjeta(false);
            compraTB.setDeposito(false);
            compraTB.setFechaVencimiento(compraTB.getFechaCompra());
            compraTB.setHoraVencimiento(compraTB.getHoraCompra());

            String result = CompraADO.registrarCompra(compraTB, egresoTBs);

            if (result.equalsIgnoreCase("register")) {
                Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente la compra.");
                Tools.Dispose(apWindow);
                comprasController.clearComponents();
            } else {
                Tools.AlertMessageError(apWindow, "Compra", result);
            }

            /*
             * Tipo Movimiento
             * TipoMovimiento = 1 apertura caja
             * TipoMovimiento = 2 venta efectivo
             * TipoMovimiento = 3 venta tarjeta
             * TipoMovimiento = 4 ingresos efectivo
             * TipoMovimiento = 5 salidas efectivo
             * TipoMovimiento = 6 venta deposito
             * TipoMovimiento = 7 ingresos tarjeta
             * TipoMovimiento = 8 ingresos deposito
             * TipoMovimiento = 9 salidas tarjeta
             * TipoMovimiento = 10 salidas deposito
             */
            // MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
            // movimientoCajaTB.setIdCaja(cajaTB.getIdCaja());
            // movimientoCajaTB.setFechaMovimiento(Tools.getDate());
            // movimientoCajaTB.setHoraMovimiento(Tools.getTime());
            // movimientoCajaTB.setComentario("SALIDA DE DINERO POR COBRO DE COMPRAS " +
            // compraTB.getSerie()
            // + "-" + compraTB.getNumeracion());
            // movimientoCajaTB.setTipoMovimiento(
            // rbEfectivoCaja.isSelected() ? 5 : rbTarjetaCaja.isSelected() ? 9 : 10);
            // movimientoCajaTB.setMonto(montoTotal);
            // String result = CompraADO.Compra_Contado(null, movimientoCajaTB, compraTB,
            // tvList, loteTBs);
            // if (result.equalsIgnoreCase("register")) {
            // Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente
            // la compra.");
            // Tools.Dispose(apWindow);
            // comprasController.clearComponents();
            // } else {
            // Tools.AlertMessageError(apWindow, "Compra", result);
            // }

        }
    }

    private void onEventCredito() {
        if (txtFechaVencimiento.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese la fecha vencimiento.");
            txtFechaVencimiento.requestFocus();
            return;
        }

        compraTB.setTipo(2);
        compraTB.setEstado(2);

        compraTB.setEfectivo(false);
        compraTB.setTarjeta(false);
        compraTB.setDeposito(false);

        compraTB.setFechaVencimiento(Tools.getDatePicker(txtFechaVencimiento));
        compraTB.setHoraVencimiento(compraTB.getHoraCompra());

        List<EgresoTB> egresoTBs = new ArrayList<>();

        short value = Tools.AlertMessageConfirmation(apWindow, "Compra", "¿Está seguro de continuar?");
        if (value == 1) {
            String result = CompraADO.registrarCompra(compraTB, egresoTBs);
            if (result.equalsIgnoreCase("register")) {
                Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente la compra.");
                Tools.Dispose(apWindow);
                comprasController.clearComponents();
            } else {
                Tools.AlertMessageError(apWindow, "Compra", result);
            }
        }

    }

    private void onEventSeleccionarContado() {
        hbCredito.setVisible(false);
        btnCredito.getStyleClass().clear();
        btnCredito.setAlignment(Pos.CENTER);
        btnCredito.getStyleClass().add("buttonBorder");

        vbContado.setVisible(true);
        btnContado.getStyleClass().clear();
        btnContado.setAlignment(Pos.CENTER);
        btnContado.getStyleClass().add("buttonLightDefault");
        cbMetodoTransaccion.requestFocus();

        if (!vbContenedorMetodoPago.getChildren().isEmpty()) {
            HBox hbox = (HBox) vbContenedorMetodoPago.getChildren().get(0);
            TextField primerTextField = null;

            VBox vbMonto = (VBox) hbox.getChildren().get(0);
            if (vbMonto != null) {
                for (Node nodo : vbMonto.getChildren()) {
                    if (nodo instanceof TextField) {
                        primerTextField = (TextField) nodo;
                        break;
                    }
                }

            }

            if (primerTextField != null) {
                primerTextField.requestFocus();
            }
        }

        metodoVentaSelecciondo = 0;
    }

    private void onEventSelecionarCredito() {
        vbContado.setVisible(false);
        btnContado.getStyleClass().clear();
        btnContado.setAlignment(Pos.CENTER);
        btnContado.getStyleClass().add("buttonBorder");

        hbCredito.setVisible(true);
        btnCredito.getStyleClass().clear();
        btnCredito.setAlignment(Pos.CENTER);
        btnCredito.getStyleClass().add("buttonLightDefault");
        txtFechaVencimiento.requestFocus();

        metodoVentaSelecciondo = 1;
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
    private void onKeyPressedContado(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSeleccionarContado();
            event.consume();
        }
    }

    @FXML
    private void onActionContado(ActionEvent event) {
        onEventSeleccionarContado();
    }

    @FXML
    private void onKeyPressedCredito(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSelecionarCredito();
            event.consume();
        }
    }

    @FXML
    private void onActionCredito(ActionEvent event) {
        onEventSelecionarCredito();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
            event.consume();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    public void setInitComprasController(FxComprasController comprasController) {
        this.comprasController = comprasController;
    }

}
