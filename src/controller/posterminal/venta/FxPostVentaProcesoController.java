package controller.posterminal.venta;

import controller.tools.ConvertMonedaCadena;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.ResultTransaction;
import model.SuministroTB;
import model.VentaADO;
import model.VentaTB;

public class FxPostVentaProcesoController implements Initializable {

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

    private FxPostVentaEstructuraController ventaEstructuraController;

    private ArrayList<SuministroTB> tvList;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private String moneda_simbolo;

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
        lblVueltoNombre.setText("Su cambio: ");
        lblVueltoNombreAdelantado.setText("Su cambio: ");
    }

    public void setInitComponents(VentaTB ventaTB, ArrayList<SuministroTB> tvList, boolean provilegios, String moneda) {
        this.ventaTB = ventaTB;
        this.tvList = tvList;
        moneda_simbolo = ventaTB.getMonedaTB().getSimbolo();
        total_venta = Double.parseDouble(Tools.roundingValue(ventaTB.getImporteNeto(), 2));

        lblTotal.setText("TOTAL A PAGAR: " + moneda_simbolo + " " + Tools.roundingValue(total_venta, 2));

        lblVuelto.setText(moneda_simbolo + " " + Tools.roundingValue(vueltoContado, 2));

        lblVueltoAdelantado.setText(moneda_simbolo + " " + Tools.roundingValue(vueltoAdelantado, 2));

        lblMonedaLetras.setText(monedaCadena.Convertir(Tools.roundingValue(total_venta, 2), true, moneda));
        txtDeposito.setText(Tools.roundingValue(total_venta, 2));
        txtDepositoAdelantado.setText(Tools.roundingValue(total_venta, 2));

        hbContenido.setDisable(false);
        txtEfectivo.requestFocus();
        this.privilegios = provilegios;
    }

    private void TotalAPagarContado() {
        if (txtEfectivo.getText().isEmpty() && txtTarjeta.getText().isEmpty()) {
            lblVuelto.setText(moneda_simbolo + " 0.00");
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

        lblVuelto.setText(moneda_simbolo + " " + Tools.roundingValue(vueltoContado, 2));
    }

    private void TotalAPagarAdelantado() {
        if (txtEfectivoAdelantado.getText().isEmpty() && txtTarjetaAdelantado.getText().isEmpty()) {
            lblVueltoAdelantado.setText(moneda_simbolo + " 0.00");
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

        lblVueltoAdelantado.setText(moneda_simbolo + " " + Tools.roundingValue(vueltoAdelantado, 2));
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
                        ResultTransaction result = VentaADO.registrarVentaContadoPosVenta(ventaTB, tvList, privilegios);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
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
                                Tools.AlertDialogMessage(window, Alert.AlertType.WARNING, "Venta", "No se puede completar la venta por que hay productos con stock inferior.", result.toStringArrayResult());
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
                break;

            case 1:
                if (txtFechaVencimiento.getValue() == null) {
                    Tools.AlertMessageWarning(window, "Venta", "Ingrese la fecha de vencimiento.");
                    txtFechaVencimiento.requestFocus();
                } else {
                    ventaTB.setTipo(2);
                    ventaTB.setEstado(2);
                    ventaTB.setVuelto(0);

                    ventaTB.setEfectivo(0);
                    ventaTB.setTarjeta(0);
                    ventaTB.setDeposito(0);
                    ventaTB.setNumeroOperacion("");
                    ventaTB.setObservaciones("");

                    ventaTB.setFechaVencimiento(Tools.getDatePicker(txtFechaVencimiento));
                    ventaTB.setHoraVencimiento(Tools.getTime());

                    short confirmation = Tools.AlertMessageConfirmation(window, "Venta", "¿Está seguro de continuar?");
                    if (confirmation == 1) {
                        ResultTransaction result = VentaADO.registrarVentaCreditoPosVenta(ventaTB, tvList, privilegios);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
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
                                Tools.AlertDialogMessage(window, Alert.AlertType.WARNING, "Venta", "No se puede completar la venta por que hay productos con stock inferior.", result.toStringArrayResult());
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
                        ResultTransaction result = VentaADO.registrarVentaAdelantadoPosVenta(ventaTB, tvList);
                        switch (result.getCode()) {
                            case "register":
                                short value = Tools.AlertMessage(window.getScene().getWindow(), "Venta", "Se realizó la venta con éxito, ¿Desea imprimir el comprobante?");
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
                                Tools.AlertDialogMessage(window, Alert.AlertType.WARNING, "Venta", "No se puede completar la venta por que hay productos con stock inferior.", result.toStringArrayResult());
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

    public void setInitVentaEstructuraController(FxPostVentaEstructuraController ventaEstructuraController) {
        this.ventaEstructuraController = ventaEstructuraController;
    }

}
