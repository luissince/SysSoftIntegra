package controller.posterminal.venta;

import controller.tools.ConvertMonedaCadena;
import controller.tools.Tools;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.ResultTransaction;
import model.VentaCreditoTB;
import model.VentaTB;
import service.VentaADO;

public class FxPostVentaProcesoNuevoController implements Initializable {

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
    private DatePicker txtFechaVencimiento;
    @FXML
    private CheckBox cbDeposito;
    @FXML
    private VBox vbEfectivoView;
    @FXML
    private VBox vbDepositoView;
    @FXML
    private TextField txtDeposito;
    @FXML
    private TextField txtNumOperacion;
    @FXML
    private CheckBox cbDepositoAdelantado;
    @FXML
    private VBox vbEfectivoViewAdelantado;
    @FXML
    private VBox vbDepositoViewAdelantado;
    @FXML
    private TextField txtDepositoAdelantado;
    @FXML
    private TextField txtNumOperacionAdelantado;
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

    private FxPostVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private double vueltoContado;

    private boolean estadoCobroContado;

    private double vueltoAdelantado;

    private boolean estadoCobroAdelantado;

    private double total_venta;

    private short state_view_pago;

    private boolean privilegios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_PRESSED);
        state_view_pago = 0;
        estadoCobroContado = false;
        total_venta = 0;
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
        tvListPlazos.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
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

    public void setInitComponents(VentaTB ventaTB, boolean provilegios) {
        this.ventaTB = ventaTB;
        total_venta = Double.parseDouble(Tools.roundingValue(ventaTB.getTotal(), 2));

        lblTotal.setText("TOTAL A PAGAR: " + ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(total_venta, 2));

        lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));

        lblVueltoAdelantado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoAdelantado, 2));

        lblMonedaLetras.setText(monedaCadena.Convertir(Tools.roundingValue(total_venta, 2), true, ventaTB.getMonedaTB().getSimbolo()));
        txtDeposito.setText(Tools.roundingValue(total_venta, 2));
        txtDepositoAdelantado.setText(Tools.roundingValue(total_venta, 2));

        hbContenido.setDisable(false);
        txtEfectivo.requestFocus();
        this.privilegios = provilegios;
    }

    private void TotalAPagarContado() {
        if (txtEfectivo.getText().isEmpty() && txtTarjeta.getText().isEmpty()) {
            lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " 0.00");
            lblVueltoNombre.setText("POR PAGAR: ");
            estadoCobroContado = false;
        } else if (txtEfectivo.getText().isEmpty()) {
            if (Double.parseDouble(txtTarjeta.getText()) >= total_venta) {
                vueltoContado = Double.parseDouble(txtTarjeta.getText()) - total_venta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = total_venta - Double.parseDouble(txtTarjeta.getText());
                lblVueltoNombre.setText("POR PAGAR: ");
                estadoCobroContado = false;
            }

        } else if (txtTarjeta.getText().isEmpty()) {
            if (Double.parseDouble(txtEfectivo.getText()) >= total_venta) {
                vueltoContado = Double.parseDouble(txtEfectivo.getText()) - total_venta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = total_venta - Double.parseDouble(txtEfectivo.getText());
                lblVueltoNombre.setText("POR PAGAR: ");
                estadoCobroContado = false;
            }
        } else {
            double suma = (Double.parseDouble(txtEfectivo.getText())) + (Double.parseDouble(txtTarjeta.getText()));
            if (suma >= total_venta) {
                vueltoContado = suma - total_venta;
                lblVueltoNombre.setText("SU CAMBIO ES: ");
                estadoCobroContado = true;
            } else {
                vueltoContado = total_venta - suma;
                lblVueltoNombre.setText("POR PAGAR: ");
                estadoCobroContado = false;
            }
        }

        lblVuelto.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));
    }

    private void TotalAPagarAdelantado() {
        if (txtEfectivoAdelantado.getText().isEmpty() && txtTarjetaAdelantado.getText().isEmpty()) {
            lblVueltoAdelantado.setText(ventaTB.getMonedaTB().getSimbolo() + " 0.00");
            lblVueltoNombreAdelantado.setText("POR PAGAR: ");
            estadoCobroAdelantado = false;
        } else if (txtEfectivoAdelantado.getText().isEmpty()) {
            if (Double.parseDouble(txtTarjetaAdelantado.getText()) >= total_venta) {
                vueltoAdelantado = Double.parseDouble(txtTarjetaAdelantado.getText()) - total_venta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = total_venta - Double.parseDouble(txtTarjetaAdelantado.getText());
                lblVueltoNombreAdelantado.setText("POR PAGAR: ");
                estadoCobroAdelantado = false;
            }

        } else if (txtTarjetaAdelantado.getText().isEmpty()) {
            if (Double.parseDouble(txtEfectivoAdelantado.getText()) >= total_venta) {
                vueltoAdelantado = Double.parseDouble(txtEfectivoAdelantado.getText()) - total_venta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = total_venta - Double.parseDouble(txtEfectivoAdelantado.getText());
                lblVueltoNombreAdelantado.setText("POR PAGAR: ");
                estadoCobroAdelantado = false;
            }
        } else {
            double suma = (Double.parseDouble(txtEfectivoAdelantado.getText())) + (Double.parseDouble(txtTarjetaAdelantado.getText()));
            if (suma >= total_venta) {
                vueltoAdelantado = suma - total_venta;
                lblVueltoNombreAdelantado.setText("SU CAMBIO ES: ");
                estadoCobroAdelantado = true;
            } else {
                vueltoAdelantado = total_venta - suma;
                lblVueltoNombreAdelantado.setText("POR PAGAR: ");
                estadoCobroAdelantado = false;
            }
        }

        lblVueltoAdelantado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoAdelantado, 2));
    }

    private void onEventAceptar() {
        switch (state_view_pago) {
            case 0:
                if (!estadoCobroContado && !cbDeposito.isSelected()) {
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

                    if (cbDeposito.isSelected()) {
                        if (!Tools.isNumeric(txtDeposito.getText())) {
                            Tools.AlertMessageWarning(window, "Venta", "El monto del deposito tiene que ser numérico.");
                            txtDeposito.requestFocus();
                            return;
                        } else if (Tools.isText(txtNumOperacion.getText().trim())) {
                            Tools.AlertMessageWarning(window, "Venta", "Ingrese el número de Operación");
                            txtNumOperacion.requestFocus();
                            return;
                        } else {
                            ventaTB.setDeposito(Double.parseDouble(txtDeposito.getText().trim()));
                            ventaTB.setNumeroOperacion(txtNumOperacion.getText().trim());
                        }
                    } else {

                        if (Tools.isNumeric(txtEfectivo.getText()) && Double.parseDouble(txtEfectivo.getText()) > 0) {
                            ventaTB.setEfectivo(Double.parseDouble(txtEfectivo.getText()));
                        }

                        if (Tools.isNumeric(txtTarjeta.getText()) && Double.parseDouble(txtTarjeta.getText()) > 0) {
                            ventaTB.setTarjeta(Double.parseDouble(txtTarjeta.getText()));
                        }

                        if (Tools.isNumeric(txtEfectivo.getText()) && Tools.isNumeric(txtTarjeta.getText())) {
                            if ((Double.parseDouble(txtEfectivo.getText())) >= total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados no son correctos!!");
                                return;
                            }
                            double efectivo = Double.parseDouble(txtEfectivo.getText());
                            double tarjeta = Double.parseDouble(txtTarjeta.getText());
                            if ((efectivo + tarjeta) != total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", " El monto a pagar no debe ser mayor al total!!");
                                return;
                            }
                        }

                        if (!Tools.isNumeric(txtEfectivo.getText()) && Tools.isNumeric(txtTarjeta.getText())) {
                            if ((Double.parseDouble(txtTarjeta.getText())) > total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", "El pago con tarjeta no debe ser mayor al total!!");
                                return;
                            }
                        }
                    }

                    short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Esta seguro de continuar?");
                    if (confirmation == 1) {
                        ResultTransaction result = VentaADO.registrarVentaContadoPosVenta(ventaTB, privilegios);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                                if (value == 1) {
                                    ventaEstructuraNuevoController.resetVenta();
                                    ventaEstructuraNuevoController.imprimirVenta(result.getResult());
                                    Tools.Dispose(window);
                                } else {
                                    ventaEstructuraNuevoController.resetVenta();
                                    Tools.Dispose(window);
                                }
                                break;
                            case "nocantidades":
                                Tools.AlertMessageWarning(window,"Venta", "No se puede completar la venta por que hay productos con stock inferior.");
                                break;
                            case "error":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            case "caja":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            default:
                                Tools.AlertMessageError(window, "Venta", result.getResult());
                                break;
                        }
                    }

                }
                break;

            case 1:
                if (rbCreditoVariable.isSelected() && txtFechaVencimiento.getValue() == null) {
                    Tools.AlertMessageWarning(window, "Venta", "Ingrese la fecha de vencimiento.");
                    txtFechaVencimiento.requestFocus();
                } else {
                    ventaTB.setTipo(2);
                    ventaTB.setEstado(2);
                    ventaTB.setVuelto(0);
                    ventaTB.setObservaciones("");

                    ventaTB.setEfectivo(0);
                    ventaTB.setTarjeta(0);
                    ventaTB.setDeposito(0);
                    ventaTB.setNumeroOperacion("");

                    if (rbCreditoVariable.isSelected()) {
                        LocalDate now = LocalDate.now();
                        LocalDate localDate = txtFechaVencimiento.getValue();
                        if (now.isAfter(localDate)) {
                            Tools.AlertMessageWarning(window, "Venta", "La fecha de pago no puede ser manor ala fecha de emisión.");
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
                                LocalDate now = LocalDate.now();
                                LocalDate localDate = tvListPlazos.getItems().get(i).getDpFecha().getValue();
                                if (now.isAfter(localDate)) {
                                    fechavl++;
                                }

                                if (i > 0) {
                                    LocalDate localDateBefore = tvListPlazos.getItems().get(i - 1).getDpFecha().getValue();
                                    LocalDate localDateAfter = tvListPlazos.getItems().get(i).getDpFecha().getValue();

                                    if (localDateBefore.isAfter(localDateAfter)) {
                                        distint++;
                                    }
                                }
                            }

                            if (!Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText())) {
                                monto++;
                            }

                            if (Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText()) && Double.parseDouble(tvListPlazos.getItems().get(i).getTfMonto().getText()) <= 0) {
                                monto++;
                            }

                            if (Tools.isNumeric(tvListPlazos.getItems().get(i).getTfMonto().getText())) {
                                totalvl += Double.parseDouble(tvListPlazos.getItems().get(i).getTfMonto().getText());
                            }

                            if (tvListPlazos.getItems().get(i).getCbMontoInicial().isSelected()) {
                                if (tvListPlazos.getItems().get(i).getCbForma().getSelectionModel().getSelectedIndex() < 0) {
                                    pagoval++;
                                }
                            }

                        }

                        if (fecha > 0) {
                            Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados en la fila fecha de pago no son validos.");
                            return;
                        }

                        if (monto > 0) {
                            Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados en la fila monto no son númericos o son menores o igual a 0.");
                            return;
                        }

                        if (distint > 0) {
                            Tools.AlertMessageWarning(window, "Venta", "Las fechas de pago ingresas son desconcordantes.");
                            return;
                        }

                        if (totalvl < total_venta) {
                            Tools.AlertMessageWarning(window, "Venta", "El monto total de plazos es menor al monto total de la venta.");
                            return;
                        }

                        if (fechavl > 0) {
                            Tools.AlertMessageWarning(window, "Venta", "Una de las fechas o todas son menores qure la actual.");
                            return;
                        }

                        if (pagoval > 0) {
                            Tools.AlertMessageWarning(window, "Venta", "Hay plazos adelantados que necesitan tener la forma de pago.");
                            return;
                        }

                        ventaTB.setFechaVencimiento(Tools.getDatePicker(tvListPlazos.getItems().get(tvListPlazos.getItems().size() - 1).getDpFecha()));
                        ventaTB.setHoraVencimiento(Tools.getTime());
                        ventaTB.setVentaCreditoTBs(new ArrayList<>(tvListPlazos.getItems()));
                        ventaTB.setTipoCredito(1);
                    }

                    short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de continuar?");
                    if (confirmation == 1) {
                        ResultTransaction result = VentaADO.registrarVentaCreditoPosVenta(ventaTB, privilegios);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                                if (value == 1) {
                                    ventaEstructuraNuevoController.resetVenta();
                                    ventaEstructuraNuevoController.imprimirVenta(result.getResult());
                                    Tools.Dispose(window);
                                } else {
                                    ventaEstructuraNuevoController.resetVenta();
                                    Tools.Dispose(window);
                                }
                                break;
                            case "nocantidades":
                                Tools.AlertMessageWarning(window, "Venta", "No se puede completar la venta por que hay productos con stock inferior.");
                                break;
                            case "error":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            case "caja":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            default:
                                Tools.AlertMessageError(window, "Venta", result.getResult());
                                break;
                        }
                    }

                }
                break;

            case 2:
                if (!estadoCobroAdelantado && !cbDepositoAdelantado.isSelected()) {
                    Tools.AlertMessageWarning(window, "Venta", "El monto es menor que el total.");
                    txtEfectivoAdelantado.requestFocus();
                } else {
                    ventaTB.setTipo(1);
                    ventaTB.setEstado(4);
                    ventaTB.setVuelto(0);
                    ventaTB.setObservaciones("");

                    ventaTB.setEfectivo(0);
                    ventaTB.setTarjeta(0);
                    ventaTB.setDeposito(0);
                    ventaTB.setNumeroOperacion("");

                    if (cbDepositoAdelantado.isSelected()) {
                        if (!Tools.isNumeric(txtDepositoAdelantado.getText())) {
                            Tools.AlertMessageWarning(window, "Venta", "El monto del deposito tiene que ser numérico.");
                            txtDepositoAdelantado.requestFocus();
                            return;
                        } else if (Tools.isText(txtNumOperacionAdelantado.getText().trim())) {
                            Tools.AlertMessageWarning(window, "Venta", "Ingrese el número de Operación");
                            txtNumOperacionAdelantado.requestFocus();
                            return;
                        } else {
                            ventaTB.setDeposito(Double.parseDouble(txtDepositoAdelantado.getText().trim()));
                            ventaTB.setNumeroOperacion(txtNumOperacionAdelantado.getText().trim());
                        }
                    } else {
                        if (Tools.isNumeric(txtEfectivoAdelantado.getText()) && Double.parseDouble(txtEfectivoAdelantado.getText()) > 0) {
                            ventaTB.setEfectivo(Double.parseDouble(txtEfectivoAdelantado.getText()));
                        }

                        if (Tools.isNumeric(txtTarjetaAdelantado.getText()) && Double.parseDouble(txtTarjetaAdelantado.getText()) > 0) {
                            ventaTB.setTarjeta(Double.parseDouble(txtTarjetaAdelantado.getText()));
                        }

                        if (Tools.isNumeric(txtEfectivoAdelantado.getText()) && Tools.isNumeric(txtTarjetaAdelantado.getText())) {
                            if ((Double.parseDouble(txtEfectivoAdelantado.getText())) >= total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", "Los valores ingresados no son correctos!!");
                                return;
                            }
                            double efectivo = Double.parseDouble(txtEfectivoAdelantado.getText());
                            double tarjeta = Double.parseDouble(txtTarjetaAdelantado.getText());
                            if ((efectivo + tarjeta) != total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", " El monto a pagar no debe ser mayor al total!!");
                                return;
                            }
                        }

                        if (!Tools.isNumeric(txtEfectivoAdelantado.getText()) && Tools.isNumeric(txtTarjetaAdelantado.getText())) {
                            if ((Double.parseDouble(txtTarjetaAdelantado.getText())) > total_venta) {
                                Tools.AlertMessageWarning(window, "Venta", "El pago con tarjeta no debe ser mayor al total!!");
                                return;
                            }
                        }
                    }

                    short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Esta seguro de continuar?");
                    if (confirmation == 1) {
                        ResultTransaction result = VentaADO.registrarVentaAdelantadoPosVenta(ventaTB);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
                                if (value == 1) {
                                    ventaEstructuraNuevoController.resetVenta();
                                    ventaEstructuraNuevoController.imprimirVenta(result.getResult());
                                    Tools.Dispose(window);
                                } else {
                                    ventaEstructuraNuevoController.resetVenta();
                                    Tools.Dispose(window);
                                }
                                break;
                            case "nocantidades":
                                Tools.AlertMessageWarning(window, "Venta", "No se puede completar la venta por que hay productos con stock inferior.");
                                break;
                            case "error":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            case "caja":
                                Tools.AlertMessageWarning(window, "Venta", result.getResult());
                                break;
                            default:
                                Tools.AlertMessageError(window, "Venta", result.getResult());
                                break;
                        }
                    }

                }
                break;
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
    private void onKeyReleasedEfectivo(KeyEvent event) {
        if (txtEfectivo.getText().isEmpty()) {
            vueltoContado = total_venta;
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
            vueltoContado = total_venta;
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
            vueltoAdelantado = total_venta;
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
            vueltoAdelantado = total_venta;
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
        if (state_view_pago != 1 || state_view_pago != 2) {
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbEfectivo.setStyle("-fx-background-color:  #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblEfectivo.setStyle("-fx-text-fill:white;");

            vbViewPagoAdelantado.setVisible(false);
            vbViewCredito.setVisible(false);
            vbViewEfectivo.setVisible(true);

            txtEfectivo.requestFocus();

            state_view_pago = 0;
        }
    }

    @FXML
    private void onMouseClickedCredito(MouseEvent event) {
        if (state_view_pago == 0 || state_view_pago == 2) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:white;");

            vbViewEfectivo.setVisible(false);
            vbViewPagoAdelantado.setVisible(false);
            vbViewCredito.setVisible(true);

            state_view_pago = 1;
        }
    }

    @FXML
    private void onMouseClickedPagoAdelantado(MouseEvent event) {
        if (state_view_pago == 0 || state_view_pago == 1) {
            vbEfectivo.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbCredito.setStyle("-fx-background-color: white;-fx-cursor:hand;-fx-padding:  0.5em;");
            vbPagoAdelantado.setStyle("-fx-background-color: #007bff;-fx-cursor:hand;-fx-padding:  0.5em");

            lblEfectivo.setStyle("-fx-text-fill:#1a2226;");
            lblCredito.setStyle("-fx-text-fill:#1a2226;");
            lblPagoAdelantado.setStyle("-fx-text-fill:white;");

            vbViewEfectivo.setVisible(false);
            vbViewCredito.setVisible(false);
            vbViewPagoAdelantado.setVisible(true);

            txtEfectivoAdelantado.requestFocus();

            state_view_pago = 2;
        }
    }

    @FXML
    private void onActionDepostio(ActionEvent event) {
        if (!cbDeposito.isSelected()) {
            vbDepositoView.setVisible(false);
            vbEfectivoView.setVisible(true);
            txtEfectivo.requestFocus();
        } else {
            vbEfectivoView.setVisible(false);
            vbDepositoView.setVisible(true);
            txtNumOperacion.requestFocus();
        }
    }

    @FXML
    private void onActionDepositoAdelantado(ActionEvent event) {
        if (!cbDepositoAdelantado.isSelected()) {
            vbDepositoViewAdelantado.setVisible(false);
            vbEfectivoViewAdelantado.setVisible(true);
            txtEfectivoAdelantado.requestFocus();
        } else {
            vbEfectivoViewAdelantado.setVisible(false);
            vbDepositoViewAdelantado.setVisible(true);
            txtNumOperacionAdelantado.requestFocus();
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

    public void setInitVentaEstructuraNuevoController(FxPostVentaEstructuraNuevoController ventaEstructuraNuevoController) {
        this.ventaEstructuraNuevoController = ventaEstructuraNuevoController;
    }

}
