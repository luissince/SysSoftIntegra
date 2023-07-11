package controller.operaciones.venta;

import controller.tools.ConvertMonedaCadena;
import static controller.tools.ObjectGlobal.ID_CONCEPTO_VENTA;
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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.BancoTB;
import model.IngresoTB;
import model.ResultTransaction;
import model.VentaCreditoTB;
import model.VentaTB;
import service.BancoADO;
import service.VentaADO;

public class FxVentaProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private HBox hbContenido;
    @FXML
    private Label lblTotal;
    @FXML
    private VBox vbEfectivo;
    @FXML
    private VBox vbCredito;
    @FXML
    private VBox vbViewCredito;
    @FXML
    private VBox vbViewEfectivo;
    @FXML
    private VBox vbPagoAdelantado;
    @FXML
    private Label lblPagoAdelantado;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblCredito;
    @FXML
    private Label lblMonedaLetras;
    @FXML
    private DatePicker txtFechaVencimiento;
    @FXML
    private RadioButton rbCreditoVariable;
    @FXML
    private HBox hbCVariable;
    @FXML
    private RadioButton rbCreditoFijo;
    @FXML
    private VBox vbCFijo;
    @FXML
    private TableView<VentaCreditoTB> tvListPlazos;
    @FXML
    private TableColumn<VentaCreditoTB, Button> tcQuitar;
    @FXML
    private TableColumn<VentaCreditoTB, DatePicker> tcFecha;
    @FXML
    private TableColumn<VentaCreditoTB, TextField> tcMonto;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private ComboBox<BancoTB> cbMetodoTransaccion;
    @FXML
    private VBox vbContenedorMetodoPago;
    @FXML
    private Label lblVueltoContadoNombre;
    @FXML
    private Label lblVueltoContado;

    private FxVentaEstructuraController ventaEstructuraController;

    private FxVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private double vueltoContado;

    private double totalVenta;

    private short metodoVentaSelecciondo;

    private boolean privilegios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_PRESSED);
        metodoVentaSelecciondo = 0;
        totalVenta = 0;
        vueltoContado = 0.00;
        monedaCadena = new ConvertMonedaCadena();
        lblVueltoContadoNombre.setText("SU CAMBIO");

        ToggleGroup toggleGroupTipo = new ToggleGroup();
        rbCreditoVariable.setToggleGroup(toggleGroupTipo);
        rbCreditoFijo.setToggleGroup(toggleGroupTipo);

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

        loadInitTablePlazos();
    }

    private void loadInitTablePlazos() {
        tcQuitar.setCellValueFactory(new PropertyValueFactory<>("btnQuitar"));
        tcFecha.setCellValueFactory(new PropertyValueFactory<>("dpFecha"));
        tcMonto.setCellValueFactory(new PropertyValueFactory<>("tfMonto"));
        tvListPlazos.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    public void loadInitComponents(VentaTB ventaTB, boolean provilegios) {
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
            ObservableList<BancoTB> bancos = task.getValue();
            cbMetodoTransaccion.getItems().addAll(bancos);

            Optional<BancoTB> bancoConFormaPago1 = bancos.stream().filter(b -> b.getFormaPago() == 1).findFirst();
            bancoConFormaPago1.ifPresent(this::generarMetodoPago);

            this.ventaTB = ventaTB;
            totalVenta = Double.parseDouble(Tools.roundingValue(ventaTB.getTotal(), 2));

            lblTotal.setText(
                    "TOTAL A PAGAR: " + ventaTB.getMonedaTB().getSimbolo() + " "
                            + Tools.roundingValue(totalVenta, 2));

            lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " "
                    + Tools.roundingValue(vueltoContado, 2));

            lblMonedaLetras.setText(
                    monedaCadena.Convertir(Tools.roundingValue(totalVenta, 2), true,
                            ventaTB.getMonedaTB().getNombre()));

            hbContenido.setDisable(false);
            this.privilegios = provilegios;

            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void addPlazos() {
        if (!rbCreditoFijo.isSelected()) {
            return;
        }
        VentaCreditoTB creditoTB = new VentaCreditoTB();

        Button btnQuitar = new Button("X");
        btnQuitar.getStyleClass().add("buttonDark");
        btnQuitar.setOnAction(b -> {
            tvListPlazos.getItems().remove(creditoTB);
        });
        btnQuitar.setOnKeyPressed(b -> {
            if (b.getCode() == KeyCode.ENTER) {
                tvListPlazos.getItems().remove(creditoTB);
            }
        });
        creditoTB.setBtnQuitar(btnQuitar);

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPrefWidth(200);
        dpFecha.setPrefHeight(30);
        dpFecha.setDayCellFactory(picket -> new DateCell() {
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
        creditoTB.setDpFecha(dpFecha);

        creditoTB.setHoraPago(Tools.getTime());

        TextField txtMonto = new TextField("");
        txtMonto.getStyleClass().add("text-field-normal");
        txtMonto.setPrefWidth(200);
        txtMonto.setPrefHeight(30);
        creditoTB.setTfMonto(txtMonto);

        tvListPlazos.getItems().add(creditoTB);
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
            if (event.getCharacter().isEmpty())
                return;

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

        lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " "
                + Tools.roundingValue(vueltoContado, 2));
    }

    private void onEventAceptar() {
        switch (metodoVentaSelecciondo) {
            case 0:
                onEventContado(1, 1);
                break;
            case 1:
                onEventCredito();
                break;
            case 2:
                onEventContado(1, 4);
                break;
        }
    }

    private void onEventContado(int tipo, int estado) {
        double montoActual = 0;

        if (vbContenedorMetodoPago.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(window, "Venta", "No hay metodos de pago para continuar.");
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
                Tools.AlertMessageWarning(window, "Venta", "Ingrese el monto a pagar o quítelo.");
                txtMonto.requestFocus();
                return;
            }

            double monto = Double.parseDouble(txtMonto.getText().trim());

            if (monto <= 0) {
                Tools.AlertMessageWarning(window, "Venta", "El monto no debe ser menor a cero.");
                txtMonto.requestFocus();
                return;
            }

            montoActual += monto;
        }

        if (montoActual < totalVenta) {
            Tools.AlertMessageWarning(window, "Venta",
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
                Tools.AlertMessageWarning(window, "Venta",
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
            ingresoTB.setIdConcepto(ID_CONCEPTO_VENTA); 
            ingresoTB.setOperacion(txtOperacion.getText().trim());
            ingresoTB.setEstado(true);
            ingresoTBs.add(ingresoTB);
        }

        ventaTB.setTipo(tipo);
        ventaTB.setEstado(estado);
        ventaTB.setVuelto(0);
        ventaTB.setObservaciones("");
        ventaTB.setEfectivo(0);
        ventaTB.setTarjeta(0);
        ventaTB.setDeposito(0);
        ventaTB.setNumeroOperacion("");
        ventaTB.setTipoCredito(0);

        registrarProceso(ingresoTBs);
    }

    private void onEventCredito() {
        if (rbCreditoVariable.isSelected() && txtFechaVencimiento.getValue() == null) {
            Tools.AlertMessageWarning(window, "Venta", "Ingrese la fecha de vencimiento.");
            txtFechaVencimiento.requestFocus();
            return;
        }

        LocalDate now = LocalDate.now();

        ventaTB.setTipo(2);
        ventaTB.setEstado(2);
        ventaTB.setVuelto(0);
        ventaTB.setObservaciones("");

        ventaTB.setEfectivo(0);
        ventaTB.setTarjeta(0);
        ventaTB.setDeposito(0);
        ventaTB.setNumeroOperacion("");

        if (rbCreditoVariable.isSelected()) {

            LocalDate localDate = txtFechaVencimiento.getValue();
            if (now.isAfter(localDate)) {
                Tools.AlertMessageWarning(window, "Venta",
                        "La fecha de pago debe ser posterior a la fecha de emisión");
                return;
            }

            if (now.equals(localDate)) {
                Tools.AlertMessageWarning(window, "Venta",
                        "La fecha de pago debe ser posterior a la fecha de emisión.");
                return;
            }

            ventaTB.setFechaVencimiento(Tools.getDatePicker(txtFechaVencimiento));
            ventaTB.setHoraVencimiento(Tools.getTime());
            ventaTB.setTipoCredito(0);
        } else {
            if (tvListPlazos.getItems().isEmpty()) {
                Tools.AlertMessageWarning(window, "Venta", "No hay plazos a pagar en la tabla.");
                return;
            }

            int fecha = 0;
            int monto = 0;
            int distint = 0;
            int fechavl = 0;
            int totalvl = 0;

            for (int i = 0; i < tvListPlazos.getItems().size(); i++) {
                if (tvListPlazos.getItems().get(i).getDpFecha().getValue() == null) {
                    fecha++;
                } else {
                    LocalDate localDate = tvListPlazos.getItems().get(i).getDpFecha().getValue();
                    if (now.isAfter(localDate)) {
                        fechavl++;
                    }

                    if (i > 0) {
                        LocalDate localDateBefore = tvListPlazos.getItems().get(i - 1).getDpFecha()
                                .getValue();
                        LocalDate localDateAfter = tvListPlazos.getItems().get(i).getDpFecha().getValue();

                        if (localDateBefore.isAfter(localDateAfter)) {
                            distint++;
                        }
                    }
                }

                if (!Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText())) {
                    monto++;
                }

                if (Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText())
                        && Double.parseDouble(tvListPlazos.getItems().get(i).getTfMonto().getText()) <= 0) {
                    monto++;
                }

                if (Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText())) {
                    totalvl += Double.parseDouble(tvListPlazos.getItems().get(i).getTfMonto().getText());
                }

            }

            if (fecha > 0) {
                Tools.AlertMessageWarning(window, "Venta",
                        "Los valores ingresados en la fila fecha de pago no son validos.");
                return;
            }

            if (monto > 0) {
                Tools.AlertMessageWarning(window, "Venta",
                        "Los valores ingresados en la fila monto no son númericos o son menores o igual a 0.");
                return;
            }

            if (distint > 0) {
                Tools.AlertMessageWarning(window, "Venta",
                        "Las fechas de pago ingresas son desconcordantes.");
                return;
            }

            if (totalvl < totalVenta) {
                Tools.AlertMessageWarning(window, "Venta",
                        "El monto total de plazos es menor al monto total de la venta.");
                return;
            }

            if (fechavl > 0) {
                Tools.AlertMessageWarning(window, "Venta",
                        "Una de las fechas o todas son menores qure la actual.");
                return;
            }

            ventaTB.setFechaVencimiento(Tools.getDatePicker(
                    tvListPlazos.getItems().get(tvListPlazos.getItems().size() - 1).getDpFecha()));
            ventaTB.setHoraVencimiento(Tools.getTime());
            ventaTB.setVentaCreditoTBs(new ArrayList<>(tvListPlazos.getItems()));
            ventaTB.setTipoCredito(1);
        }

        List<IngresoTB> ingresoTBs = new ArrayList<>();
        registrarProceso(ingresoTBs);
    }

    private void registrarProceso(List<IngresoTB> ingresoTBs) {
        short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de continuar?");
        if (confirmation == 1) {
            ResultTransaction result = VentaADO.registrarVenta(ventaTB, ingresoTBs,
                    privilegios);
            switch (result.getCode()) {
                case "register":
                    short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta",
                            "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                    if (value == 1) {
                        if (ventaEstructuraController != null) {
                            ventaEstructuraController.resetVenta();
                            ventaEstructuraController.imprimirVenta(result.getResult());
                        } else if (ventaEstructuraNuevoController != null) {
                            ventaEstructuraNuevoController.resetVenta();
                            ventaEstructuraNuevoController.imprimirVenta(result.getResult());
                        }
                        Tools.Dispose(window);
                    } else {
                        if (ventaEstructuraController != null) {
                            ventaEstructuraController.resetVenta();
                        } else if (ventaEstructuraNuevoController != null) {
                            ventaEstructuraNuevoController.resetVenta();
                        }
                        Tools.Dispose(window);
                    }
                    break;
                case "nocantidades":
                    Tools.AlertDialogMessage(window, Alert.AlertType.WARNING, "Venta",
                            "No se puede completar la venta por que hay productos con stock inferior.",
                            result.toStringArrayResult());
                    break;
                case "error":
                    Tools.AlertMessageError(window, "Venta", result.getResult());
                    break;
                default:
                    Tools.AlertMessageError(window, "Venta", result.getResult());
                    break;
            }
        }
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
    private void onMouseClickedEfectivo(MouseEvent event) {
        if (metodoVentaSelecciondo != 1 || metodoVentaSelecciondo != 2) {
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbEfectivo.setStyle("-fx-background-color:  #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblEfectivo.setStyle("-fx-text-fill:white;");

            vbViewCredito.setVisible(false);
            vbViewEfectivo.setVisible(true);

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
    }

    @FXML
    private void onMouseClickedCredito(MouseEvent event) {
        if (metodoVentaSelecciondo != 0 || metodoVentaSelecciondo != 2) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:white;");

            vbViewEfectivo.setVisible(false);
            vbViewCredito.setVisible(true);

            metodoVentaSelecciondo = 1;
        }
    }

    @FXML
    private void onMouseClickedPagoAdelantado(MouseEvent event) {
        if (metodoVentaSelecciondo != 0 || metodoVentaSelecciondo != 1) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:white;");

            vbViewCredito.setVisible(false);
            vbViewEfectivo.setVisible(true);

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

            metodoVentaSelecciondo = 2;
        }
    }

    @FXML
    private void onActionCredito(ActionEvent event) {
        if (rbCreditoVariable.isSelected()) {
            hbCVariable.setDisable(false);
            vbCFijo.setDisable(true);
        } else {
            hbCVariable.setDisable(true);
            vbCFijo.setDisable(false);
        }
    }

    @FXML
    private void onKeyPressedPlazos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addPlazos();
        }
    }

    @FXML
    private void onActionPlazos(ActionEvent event) {
        addPlazos();
    }

    public void setInitVentaEstructuraController(FxVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

    public void setInitVentaEstructuraNuevoController(FxVentaEstructuraNuevoController ventaEstructuraNuevoController) {
        this.ventaEstructuraNuevoController = ventaEstructuraNuevoController;
    }

}
