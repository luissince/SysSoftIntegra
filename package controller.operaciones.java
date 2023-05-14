package controller.operaciones.venta;

import controller.tools.ConvertMonedaCadena;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
    private TextField txtEfectivo;
    @FXML
    private TextField txtTarjeta;
    @FXML
    private Label lblVueltoNombre;
    @FXML
    private Label lblVuelto;
    @FXML
    private TextField txtEfectivoAdelantado;
    @FXML
    private TextField txtTarjetaAdelantado;
    @FXML
    private Label lblVueltoNombreAdelantado;
    @FXML
    private Label lblVueltoAdelantado;
    @FXML
    private VBox vbEfectivo;
    @FXML
    private VBox vbCredito;
    @FXML
    private VBox vbViewEfectivo;
    @FXML
    private VBox vbViewCredito;
    @FXML
    private VBox vbViewPagoAdelantado;
    @FXML
    private VBox vbViewOtrosMetodos;
    @FXML
    private Label lblEfectivo;
    @FXML
    private Label lblCredito;
    @FXML
    private VBox vbPagoAdelantado;
    @FXML
    private Label lblPagoAdelantado;
    @FXML
    private Label lblMonedaLetras;
    @FXML
    private VBox vbOtros;
    @FXML
    private Label lblOtros;
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
    private TableColumn<VentaCreditoTB, CheckBox> tcAdelanto;
    @FXML
    private TableColumn<VentaCreditoTB, ComboBox<String>> tcForma;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private ComboBox<BancoTB> cbMetodoTransaccion;
    @FXML
    private VBox vbContenedorMetodoCobro;
    @FXML
    private Label lblVueltoContadoNombre;
    @FXML
    private Label lblVueltoContado;

    private FxVentaEstructuraController ventaEstructuraController;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private double vueltoContado;

    private boolean estadoCobroContado;

    private double vueltoAdelantado;

    private boolean estadoCobroAdelantado;

    private double totalVenta;

    private short metodoVentaSelecciondo;

    private boolean privilegios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_PRESSED);
        metodoVentaSelecciondo = 0;
        estadoCobroContado = false;
        totalVenta = 0;
        vueltoContado = 0.00;
        monedaCadena = new ConvertMonedaCadena();
        lblVueltoNombre.setText("SU CAMBIO: ");
        lblVueltoNombreAdelantado.setText("SU CAMBIO: ");

        ToggleGroup toggleGroupTipo = new ToggleGroup();
        rbCreditoVariable.setToggleGroup(toggleGroupTipo);
        rbCreditoFijo.setToggleGroup(toggleGroupTipo);

        loadInitTablePlazos();
    }

    private void loadInitTablePlazos() {
        tcQuitar.setCellValueFactory(new PropertyValueFactory<>("btnQuitar"));
        tcFecha.setCellValueFactory(new PropertyValueFactory<>("dpFecha"));
        tcMonto.setCellValueFactory(new PropertyValueFactory<>("tfMonto"));
        tcAdelanto.setCellValueFactory(new PropertyValueFactory<>("cbMontoInicial"));
        tcForma.setCellValueFactory(new PropertyValueFactory<>("cbForma"));
        tvListPlazos.setPlaceholder(
                Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
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
        creditoTB.setDpFecha(dpFecha);

        creditoTB.setHoraPago(Tools.getTime());

        TextField txtMonto = new TextField("");
        txtMonto.getStyleClass().add("text-field-normal");
        txtMonto.setPrefWidth(200);
        txtMonto.setPrefHeight(30);
        creditoTB.setTfMonto(txtMonto);

        CheckBox cbMontoInicial = new CheckBox();
        cbMontoInicial.getStyleClass().add("check-box-contenido");
        cbMontoInicial.setPrefHeight(30);
        creditoTB.setCbMontoInicial(cbMontoInicial);

        ComboBox<String> cbForma = new ComboBox<>();
        cbForma.prefWidth(200);
        cbForma.setPrefHeight(30);
        cbForma.getItems().addAll("EFECTIVO", "TARJETA", "DEPOSITO");
        creditoTB.setCbForma(cbForma);

        tvListPlazos.getItems().add(creditoTB);
    }

    private void addMetodoCobro() {
        if (cbMetodoTransaccion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Venta", "Seleccione el método de cobro.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        List<Node> vBoxFiltrados = vbContenedorMetodoCobro.getChildren()
                .stream()
                .filter(vBoxFilter -> ((HBox) vBoxFilter).getId() == cbMetodoTransaccion.getSelectionModel()
                        .getSelectedItem().getIdBanco())
                .collect(Collectors.toList());

        if (vBoxFiltrados.size() != 0) {
            Tools.AlertMessageWarning(window, "Venta", "Ya existe en la lista el método de cobro.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        generarMetodoCobro(cbMetodoTransaccion.getSelectionModel().getSelectedItem());
    }

    private void generarMetodoCobro(BancoTB bancoTB) {
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

        HBox hbContenido = new HBox();
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
            vbContenedorMetodoCobro.getChildren().remove(hBox);
        });
        btnQuitar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                vbContenedorMetodoCobro.getChildren().remove(hBox);
            }
        });
        hbContenido.getChildren().addAll(txtOperacion, btnQuitar);
        vbOperacion.getChildren().addAll(lblOperacion, hbContenido);

        hBox.getChildren().addAll(vbMonto, vbOperacion);

        vbContenedorMetodoCobro.getChildren().add(hBox);
        txtMonto.requestFocus();
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
                Object object = BancoADO.ObtenerListarBancos();
                if (object instanceof ObservableList) {
                    return (ObservableList<BancoTB>) object;
                } else {
                    throw new Exception("Se produjo un error, intente nuevamente.");
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
            bancoConFormaPago1.ifPresent(this::generarMetodoCobro);

            this.ventaTB = ventaTB;
            totalVenta = Double.parseDouble(Tools.roundingValue(ventaTB.getTotal(), 2));

            lblTotal.setText(
                    "TOTAL A COBRAR: " + ventaTB.getMonedaTB().getSimbolo() + " "
                            + Tools.roundingValue(totalVenta, 2));

            lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " "
                    + Tools.roundingValue(vueltoContado, 2));

            lblVueltoAdelantado
                    .setText(ventaTB.getMonedaTB().getSimbolo() + " "
                            + Tools.roundingValue(vueltoAdelantado, 2));

            lblMonedaLetras.setText(
                    monedaCadena.Convertir(Tools.roundingValue(totalVenta, 2), true,
                            ventaTB.getMonedaTB().getNombre()));

            hbContenido.setDisable(false);
            txtEfectivo.requestFocus();
            this.privilegios = provilegios;

            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void generarVuelto() {
        double montoActual = 0;

        for (Node node : vbContenedorMetodoCobro.getChildren()) {
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

            if (!Tools.isNumeric(txtMonto.getText().trim()))
                return;

            montoActual += Double.parseDouble(txtMonto.getText().trim());
        }

        if (montoActual >= totalVenta) {
            vueltoContado = montoActual - totalVenta;
            lblVueltoContadoNombre.setText("SU CAMBIO ES");
            estadoCobroContado = true;
        } else {
            vueltoContado = totalVenta - montoActual;
            lblVueltoContadoNombre.setText("POR COBRAR");
            estadoCobroContado = false;
        }

        lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " " +
                Tools.roundingValue(vueltoContado, 2));
    }

    private void TotalAPagarContado() {
        if (txtEfectivo.getText().isEmpty() && txtTarjeta.getText().isEmpty()) {
            lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " 0.00");
            lblVueltoNombre.setText("POR COBRAR: ");
            estadoCobroContado = false;
        } else if (txtEfectivo.getText().isEmpty()) {
            if (Double.parseDouble(txtTarjeta.getText()) >= totalVenta) {
                vueltoContado = Double.parseDouble(txtTarjeta.getText()) - totalVenta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = totalVenta - Double.parseDouble(txtTarjeta.getText());
                lblVueltoNombre.setText("POR COBRAR: ");
                estadoCobroContado = false;
            }

        } else if (txtTarjeta.getText().isEmpty()) {
            if (Double.parseDouble(txtEfectivo.getText()) >= totalVenta) {
                vueltoContado = Double.parseDouble(txtEfectivo.getText()) - totalVenta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = totalVenta - Double.parseDouble(txtEfectivo.getText());
                lblVueltoNombre.setText("POR COBRAR: ");
                estadoCobroContado = false;
            }
        } else {
            double suma = (Double.parseDouble(txtEfectivo.getText())) +
                    (Double.parseDouble(txtTarjeta.getText()));
            if (suma >= totalVenta) {
                vueltoContado = suma - totalVenta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = totalVenta - suma;
                lblVueltoNombre.setText("POR COBRAR: ");
                estadoCobroContado = false;
            }
        }

        lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " " +
                Tools.roundingValue(vueltoContado, 2));
    }

    private void TotalAPagarAdelantado() {
        if (txtEfectivoAdelantado.getText().isEmpty() && txtTarjetaAdelantado.getText().isEmpty()) {
            lblVueltoAdelantado.setText(ventaTB.getMonedaTB().getSimbolo() + " 0.00");
            lblVueltoNombreAdelantado.setText("POR COBRAR: ");
            estadoCobroAdelantado = false;
        } else if (txtEfectivoAdelantado.getText().isEmpty()) {
            if (Double.parseDouble(txtTarjetaAdelantado.getText()) >= totalVenta) {
                vueltoAdelantado = Double.parseDouble(txtTarjetaAdelantado.getText()) - totalVenta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = totalVenta - Double.parseDouble(txtTarjetaAdelantado.getText());
                lblVueltoNombreAdelantado.setText("POR COBRAR: ");
                estadoCobroAdelantado = false;
            }

        } else if (txtTarjetaAdelantado.getText().isEmpty()) {
            if (Double.parseDouble(txtEfectivoAdelantado.getText()) >= totalVenta) {
                vueltoAdelantado = Double.parseDouble(txtEfectivoAdelantado.getText()) - totalVenta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = totalVenta - Double.parseDouble(txtEfectivoAdelantado.getText());
                lblVueltoNombreAdelantado.setText("POR COBRAR: ");
                estadoCobroAdelantado = false;
            }
        } else {
            double suma = (Double.parseDouble(txtEfectivoAdelantado.getText()))
                    + (Double.parseDouble(txtTarjetaAdelantado.getText()));
            if (suma >= totalVenta) {
                vueltoAdelantado = suma - totalVenta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = totalVenta - suma;
                lblVueltoNombreAdelantado.setText("POR COBRAR: ");
                estadoCobroAdelantado = false;
            }
        }

        lblVueltoAdelantado
                .setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoAdelantado, 2));
    }

    private void onEventAceptar() {
        switch (metodoVentaSelecciondo) {
            case 0:
                onEventEfectivo();
                break;
            case 1:
                onEventCredito();
                break;
            case 2:
                onEventAdelantado();
                break;
            case 3:
                onEventOtros();
                break;
        }
    }

    private void onEventEfectivo() {
        if (!estadoCobroContado) {
            Tools.AlertMessageWarning(window, "Venta", "El monto es menor que el total.");
            txtEfectivo.requestFocus();
        } else {
            ventaTB.setTipo(1);
            ventaTB.setEstado(1);
            ventaTB.setVuelto(vueltoContado);
            ventaTB.setObservaciones("");

            ventaTB.setEfectivo(0);
            ventaTB.setTarjeta(0);
            ventaTB.setDeposito(0);
            ventaTB.setNumeroOperacion("");

            ventaTB.setTipoCredito(0);

            if (Tools.isNumeric(txtEfectivo.getText()) && Double.parseDouble(txtEfectivo.getText()) > 0) {
                ventaTB.setEfectivo(Double.parseDouble(txtEfectivo.getText()));
            }

            if (Tools.isNumeric(txtTarjeta.getText()) && Double.parseDouble(txtTarjeta.getText()) > 0) {
                ventaTB.setTarjeta(Double.parseDouble(txtTarjeta.getText()));
            }

            if (Tools.isNumeric(txtEfectivo.getText()) && Tools.isNumeric(txtTarjeta.getText())) {
                if ((Double.parseDouble(txtEfectivo.getText())) >= totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados no son correctos!!");
                    return;
                }
                double efectivo = Double.parseDouble(txtEfectivo.getText());
                double tarjeta = Double.parseDouble(txtTarjeta.getText());
                if ((efectivo + tarjeta) != totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta",
                            " El monto a pagar no debe ser mayor al total!!");
                    return;
                }
            }

            if (!Tools.isNumeric(txtEfectivo.getText()) && Tools.isNumeric(txtTarjeta.getText())) {
                if ((Double.parseDouble(txtTarjeta.getText())) > totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta",
                            "El pago con tarjeta no debe ser mayor al total!!");
                    return;
                }
            }

            short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Esta seguro de continuar?");
            if (confirmation == 1) {
                ResultTransaction result = VentaADO.registrarVentaContado(ventaTB, privilegios);
                switch (result.getCode()) {
                    case "register":
                        short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta",
                                "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                        if (value == 1) {
                            ventaEstructuraController.resetVenta();
                            ventaEstructuraController.imprimirVenta(result.getResult());
                            Tools.Dispose(window);
                        } else {
                            ventaEstructuraController.resetVenta();
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
    }

    private void onEventCredito() {
        if (rbCreditoVariable.isSelected() && txtFechaVencimiento.getValue() == null) {
            Tools.AlertMessageWarning(window, "Venta", "Ingrese la fecha de vencimiento.");
            txtFechaVencimiento.requestFocus();
        } else {
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
                int pagoval = 0;

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

                    if (tvListPlazos.getItems().get(i).getCbMontoInicial().isSelected()) {
                        if (tvListPlazos.getItems().get(i).getCbForma().getSelectionModel()
                                .getSelectedIndex() < 0) {
                            pagoval++;
                        }
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

                if (pagoval > 0) {
                    Tools.AlertMessageWarning(window, "Venta",
                            "Hay plazos adelantados que necesitan tener la forma de pago.");
                    return;
                }

                ventaTB.setFechaVencimiento(Tools.getDatePicker(
                        tvListPlazos.getItems().get(tvListPlazos.getItems().size() - 1).getDpFecha()));
                ventaTB.setHoraVencimiento(Tools.getTime());
                ventaTB.setVentaCreditoTBs(new ArrayList<>(tvListPlazos.getItems()));
                ventaTB.setTipoCredito(1);
            }

            short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de continuar?");
            if (confirmation == 1) {
                ResultTransaction result = VentaADO.registrarVentaCredito(ventaTB, privilegios);
                switch (result.getCode()) {
                    case "register":
                        short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta",
                                "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                        if (value == 1) {
                            ventaEstructuraController.resetVenta();
                            ventaEstructuraController.imprimirVenta(result.getResult());
                            Tools.Dispose(window);
                        } else {
                            ventaEstructuraController.resetVenta();
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
    }

    private void onEventAdelantado() {
        if (!estadoCobroAdelantado) {
            Tools.AlertMessageWarning(window, "Venta", "El monto es menor que el total.");
            txtEfectivoAdelantado.requestFocus();
        } else {
            ventaTB.setTipo(1);
            ventaTB.setEstado(4);
            ventaTB.setVuelto(vueltoAdelantado);
            ventaTB.setObservaciones("");

            ventaTB.setEfectivo(0);
            ventaTB.setTarjeta(0);
            ventaTB.setDeposito(0);
            ventaTB.setNumeroOperacion("");
            ventaTB.setTipoCredito(0);

            if (Tools.isNumeric(txtEfectivoAdelantado.getText())
                    && Double.parseDouble(txtEfectivoAdelantado.getText()) > 0) {
                ventaTB.setEfectivo(Double.parseDouble(txtEfectivoAdelantado.getText()));
            }

            if (Tools.isNumeric(txtTarjetaAdelantado.getText())
                    && Double.parseDouble(txtTarjetaAdelantado.getText()) > 0) {
                ventaTB.setTarjeta(Double.parseDouble(txtTarjetaAdelantado.getText()));
            }

            if (Tools.isNumeric(txtEfectivoAdelantado.getText())
                    && Tools.isNumeric(txtTarjetaAdelantado.getText())) {
                if ((Double.parseDouble(txtEfectivoAdelantado.getText())) >= totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados no son correctos!!");
                    return;
                }
                double efectivo = Double.parseDouble(txtEfectivoAdelantado.getText());
                double tarjeta = Double.parseDouble(txtTarjetaAdelantado.getText());
                if ((efectivo + tarjeta) != totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta",
                            " El monto a pagar no debe ser mayor al total!!");
                    return;
                }
            }

            if (!Tools.isNumeric(txtEfectivoAdelantado.getText())
                    && Tools.isNumeric(txtTarjetaAdelantado.getText())) {
                if ((Double.parseDouble(txtTarjetaAdelantado.getText())) > totalVenta) {
                    Tools.AlertMessageWarning(window, "Venta",
                            "El pago con tarjeta no debe ser mayor al total!!");
                    return;
                }
            }

            short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Esta seguro de continuar?");
            if (confirmation == 1) {
                ResultTransaction result = VentaADO.registrarVentaAdelantado(ventaTB);
                switch (result.getCode()) {
                    case "register":
                        short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta",
                                "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                        if (value == 1) {
                            ventaEstructuraController.resetVenta();
                            ventaEstructuraController.imprimirVenta(result.getResult());
                            Tools.Dispose(window);
                        } else {
                            ventaEstructuraController.resetVenta();
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
    }

    private void onEventOtros() {
        // if (cbMetodoTransaccion.getSelectionModel().getSelectedIndex() < 0) {
        // Tools.AlertMessageWarning(window, "Venta", "Seleccione el metodo de pago.");
        // cbMetodoTransaccion.requestFocus();
        // return;
        // }

        // if (!Tools.isNumeric(txtMontoMetodoTransaccion.getText().trim())) {
        // Tools.AlertMessageWarning(window, "Venta", "Ingrese el monto de cobrar.");
        // txtMontoMetodoTransaccion.requestFocus();
        // return;
        // }

        // double monto= Double.parseDouble(txtMontoMetodoTransaccion.getText().trim());
        // if (monto < totalVenta) {
        // Tools.AlertMessageWarning(window, "Venta", "El monto ingresado es menor al
        // total de la venta.");
        // txtMontoMetodoTransaccion.requestFocus();
        // return;
        // }

        // ventaTB.setTipo(1);
        // ventaTB.setEstado(1);
        // ventaTB.setVuelto(0);
        // ventaTB.setObservaciones("");
        // ventaTB.setEfectivo(0);
        // ventaTB.setTarjeta(0);
        // ventaTB.setDeposito(0);
        // ventaTB.setNumeroOperacion("");
        // ventaTB.setTipoCredito(0);
        // IngresoTB ingresoTB = new IngresoTB();
        // ingresoTB.setIdBanco(cbMetodoTransaccion.getSelectionModel().getSelectedItem().getIdBanco());
        // ingresoTB.setMonto(totalVenta);
        // ingresoTB.setVuelto(totalVenta - monto);
        // ingresoTB.setDetalle(txtNumeroOperacion.getText().trim());

        double montoActual = 0;

        if (vbContenedorMetodoCobro.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(window, "Venta", "No hay metodos de cobro para continuar.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        for (Node node : vbContenedorMetodoCobro.getChildren()) {
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
                Tools.AlertMessageWarning(window, "Venta", "Ingrese el monto a cobrar o quítelo.");
                txtMonto.requestFocus();
                return;
            }

            double monto = Double.parseDouble(txtMonto.getText().trim());
            montoActual += monto;
        }

        if (montoActual < totalVenta) {
            Tools.AlertMessageWarning(window, "Venta",
                    "Los montos ingresados deben ser igual o mayor al total de la venta.");

            for (Node node : vbContenedorMetodoCobro.getChildren()) {
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

        List<IngresoTB> ingresoTBs = new ArrayList<>();

        for (Node node : vbContenedorMetodoCobro.getChildren()) {
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
                HBox hbContenido = (HBox) vbOperacion.getChildren().get(1);
                if (hbContenido != null) {
                    for (Node nodo : hbContenido.getChildren()) {
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
            ingresoTB.setMonto(monto);
            ingresoTB.setVuelto(vueltoContado);
            ingresoTB.setOperacion(txtOperacion.getText().trim());
            ingresoTBs.add(ingresoTB);
        }

        short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Estaseguro de continuar ?");
        if (confirmation == 1) {
            ResultTransaction result = VentaADO.registrarVenta(ventaTB, ingresoTBs,
                    privilegios);
            switch (result.getCode()) {
                case "register":
                    short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta",
                            "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                    if (value == 1) {
                        ventaEstructuraController.resetVenta();
                        ventaEstructuraController.imprimirVenta(result.getResult());
                        Tools.Dispose(window);
                    } else {
                        ventaEstructuraController.resetVenta();
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
    private void onKeyPressedAgregarMetodoCobro(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addMetodoCobro();
            event.consume();
        }
    }

    @FXML
    private void onActionAgregarMetodoCobro(ActionEvent event) {
        addMetodoCobro();
    }

    @FXML
    private void onKeyReleasedEfectivo(KeyEvent event) {
        if (txtEfectivo.getText().isEmpty()) {
            vueltoContado = totalVenta;
            TotalAPagarContado();
            return;
        }
        if (Tools.isNumeric(txtEfectivo.getText())) {
            TotalAPagarContado();
        }
    }

    @FXML
    private void onKeyTypedEfectivo(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtEfectivo.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void OnKeyReleasedTarjeta(KeyEvent event) {
        if (txtTarjeta.getText().isEmpty()) {
            vueltoContado = totalVenta;
            TotalAPagarContado();
            return;
        }
        if (Tools.isNumeric(txtTarjeta.getText())) {
            TotalAPagarContado();
        }
    }

    @FXML
    private void OnKeyTypedTarjeta(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtTarjeta.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyReleasedEfectivoAdelantado(KeyEvent event) {
        if (txtEfectivoAdelantado.getText().isEmpty()) {
            vueltoAdelantado = totalVenta;
            TotalAPagarAdelantado();
            return;
        }
        if (Tools.isNumeric(txtEfectivoAdelantado.getText())) {
            TotalAPagarAdelantado();
        }
    }

    @FXML
    private void onKeyTypedEfectivoAdelantado(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtEfectivoAdelantado.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void OnKeyReleasedTarjetaAdelantado(KeyEvent event) {
        if (txtTarjetaAdelantado.getText().isEmpty()) {
            vueltoAdelantado = totalVenta;
            TotalAPagarAdelantado();
            return;
        }
        if (Tools.isNumeric(txtTarjetaAdelantado.getText())) {
            TotalAPagarAdelantado();
        }
    }

    @FXML
    private void OnKeyTypedTarjetaAdelantado(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtTarjetaAdelantado.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onMouseClickedEfectivo(MouseEvent event) {
        if (metodoVentaSelecciondo != 1 || metodoVentaSelecciondo != 2 || metodoVentaSelecciondo != 3) {
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbEfectivo.setStyle("-fx-background-color:  #007bff;-fx-cursor:hand;-fx-padding:  0.5em");
            vbOtros.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");

            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblEfectivo.setStyle("-fx-text-fill:white;");
            lblOtros.setStyle("-fx-text-fill:#1a2226;");

            vbViewPagoAdelantado.setVisible(false);
            vbViewCredito.setVisible(false);
            vbViewEfectivo.setVisible(true);
            vbViewOtrosMetodos.setVisible(false);

            txtEfectivo.requestFocus();

            metodoVentaSelecciondo = 0;
        }
    }

    @FXML
    private void onMouseClickedCredito(MouseEvent event) {
        if (metodoVentaSelecciondo != 0 || metodoVentaSelecciondo != 2 || metodoVentaSelecciondo != 3) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbOtros.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:white;");
            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblOtros.setStyle("-fx-text-fill:#1a2226;");

            vbViewEfectivo.setVisible(false);
            vbViewCredito.setVisible(true);
            vbViewPagoAdelantado.setVisible(false);
            vbViewOtrosMetodos.setVisible(false);

            metodoVentaSelecciondo = 1;
        }
    }

    @FXML
    private void onMouseClickedPagoAdelantado(MouseEvent event) {
        if (metodoVentaSelecciondo != 0 || metodoVentaSelecciondo != 1 || metodoVentaSelecciondo != 3) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");
            vbOtros.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:white;");
            lblOtros.setStyle("-fx-text-fill:#1a2226;");

            vbViewEfectivo.setVisible(false);
            vbViewCredito.setVisible(false);
            vbViewPagoAdelantado.setVisible(true);
            vbViewOtrosMetodos.setVisible(false);

            txtEfectivoAdelantado.requestFocus();

            metodoVentaSelecciondo = 2;
        }
    }

    @FXML
    private void onMouseClickedOtros(MouseEvent event) {
        if (metodoVentaSelecciondo != 0 || metodoVentaSelecciondo != 1 || metodoVentaSelecciondo != 2) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em");
            vbOtros.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblOtros.setStyle("-fx-text-fill:white;");

            vbViewEfectivo.setVisible(false);
            vbViewCredito.setVisible(false);
            vbViewPagoAdelantado.setVisible(false);
            vbViewOtrosMetodos.setVisible(true);

            if (vbContenedorMetodoCobro.getChildren().size() > 0) {
                HBox hbox = (HBox) vbContenedorMetodoCobro.getChildren().get(0);
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

            metodoVentaSelecciondo = 3;
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

}
