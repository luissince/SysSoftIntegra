package controller.operaciones.compras;

import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import model.CajaADO;
import model.CajaTB;
import model.CompraADO;
import model.CompraTB;
import model.DetalleCompraTB;
import model.IngresoTB;
import model.LoteTB;
import model.MovimientoCajaTB;

public class FxComprasProcesoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTotal;
    @FXML
    private HBox hbCaja;
    @FXML
    private HBox hbCredito;
    @FXML
    private HBox hbEgreso;
    @FXML
    private DatePicker txtFechaVencimiento;
    @FXML
    private Button btnCaja;
    @FXML
    private Button btnCredito;
    @FXML
    private Button btnEgreso;
    @FXML
    private RadioButton rbEfectivoCaja;
    @FXML
    private RadioButton rbTarjetaCaja;
    @FXML
    private RadioButton rbDepositoCaja;
    @FXML
    private RadioButton rbEfectivoEgreso;
    @FXML
    private RadioButton rbTarjetaEgreso;
    @FXML
    private RadioButton rbDepositoEgreso;

    private FxComprasController comprasController;

    private CompraTB compraTB = null;

    private TableView<DetalleCompraTB> tvList;

    private ObservableList<LoteTB> loteTBs;

    private double montoTotal = 0;

    private boolean caja;

    private boolean credito;

    private boolean egreso;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        ToggleGroup toggleGroupCaja = new ToggleGroup();
        rbEfectivoCaja.setToggleGroup(toggleGroupCaja);
        rbTarjetaCaja.setToggleGroup(toggleGroupCaja);
        rbDepositoCaja.setToggleGroup(toggleGroupCaja);

        ToggleGroup toggleGroupEgreso = new ToggleGroup();
        rbEfectivoEgreso.setToggleGroup(toggleGroupEgreso);
        rbTarjetaEgreso.setToggleGroup(toggleGroupEgreso);
        rbDepositoEgreso.setToggleGroup(toggleGroupEgreso);

        caja = true;
        credito = false;
        egreso = false;
    }

    public void setLoadProcess(CompraTB compraTB, TableView<DetalleCompraTB> tvList, ObservableList<LoteTB> loteTBs) {
        this.compraTB = compraTB;
        this.tvList = tvList;
        this.loteTBs = loteTBs;
        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(compraTB.getTotal(), 2));
        montoTotal = Double.parseDouble(Tools.roundingValue(compraTB.getTotal(), 2));
    }

    private void onEventProcess() {
        if (caja) {
            short value = Tools.AlertMessageConfirmation(apWindow, "Compra", "¿Está seguro de continuar?");
            if (value == 1) {
                Object object = CajaADO.ValidarCreacionCaja(Session.USER_ID);
                if (object instanceof CajaTB) {
                    CajaTB cajaTB = (CajaTB) object;
                    if (cajaTB.getId() == 2) {
                        compraTB.setTipo(1);
                        compraTB.setEfectivo(false);
                        compraTB.setTarjeta(false);
                        compraTB.setDeposito(false);
                        compraTB.setEstado(1);
                        compraTB.setFechaVencimiento(compraTB.getFechaCompra());
                        compraTB.setHoraVencimiento(compraTB.getHoraCompra());

                        if (rbEfectivoCaja.isSelected()) {
                            compraTB.setEfectivo(true);
                        } else if (rbTarjetaCaja.isSelected()) {
                            compraTB.setTarjeta(true);
                        } else {
                            compraTB.setDeposito(true);
                        }

                        /*
                            Tipo Movimiento 
                            TipoMovimiento = 1 apertura caja
                            TipoMovimiento = 2 venta efectivo
                            TipoMovimiento = 3 venta tarjeta
                            TipoMovimiento = 4 ingresos efectivo
                            TipoMovimiento = 5 salidas efectivo
                            TipoMovimiento = 6 venta deposito
                            TipoMovimiento = 7 ingresos tarjeta
                            TipoMovimiento = 8 ingresos deposito
                            TipoMovimiento = 9 salidas tarjeta
                            TipoMovimiento = 10 salidas deposito
                         */
                        MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                        movimientoCajaTB.setIdCaja(cajaTB.getIdCaja());
                        movimientoCajaTB.setFechaMovimiento(Tools.getDate());
                        movimientoCajaTB.setHoraMovimiento(Tools.getTime());
                        movimientoCajaTB.setComentario("SALIDA DE DINERO POR COBRO DE COMPRAS " + compraTB.getSerie() + "-" + compraTB.getNumeracion());
                        movimientoCajaTB.setTipoMovimiento(rbEfectivoCaja.isSelected() ? 5 : rbTarjetaCaja.isSelected() ? 9 : 10);
                        movimientoCajaTB.setMonto(montoTotal);

                        String result = CompraADO.Compra_Contado(null, movimientoCajaTB, compraTB, tvList, loteTBs);
                        if (result.equalsIgnoreCase("register")) {
                            Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente la compra.");
                            Tools.Dispose(apWindow);
                            comprasController.clearComponents();
                        } else {
                            Tools.AlertMessageError(apWindow, "Compra", result);
                        }
                    } else {
                        Tools.AlertMessageWarning(apWindow, "Compra", "No tiene ninguna caja aperturada para continuar con el proceso.");
                    }
                } else {
                    Tools.AlertMessageError(apWindow, "Compra", "No se pudo obtener el estado de su caja por problemas de red, intente nuevamente.");
                }
            }

        } else if (credito) {
            if (txtFechaVencimiento.getValue() == null) {
                Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese la fecha vencimiento.");
                txtFechaVencimiento.requestFocus();
            } else {
                compraTB.setTipo(2);
                compraTB.setEfectivo(false);
                compraTB.setTarjeta(false);
                compraTB.setDeposito(false);
                compraTB.setEstado(2);
                compraTB.setFechaVencimiento(Tools.getDatePicker(txtFechaVencimiento));
                compraTB.setHoraVencimiento(compraTB.getHoraCompra());

                short value = Tools.AlertMessageConfirmation(apWindow, "Compra", "¿Está seguro de continuar?");
                if (value == 1) {
                    String result = CompraADO.Compra_Credito(compraTB, tvList, loteTBs);
                    if (result.equalsIgnoreCase("register")) {
                        Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente la compra.");
                        Tools.Dispose(apWindow);
                        comprasController.clearComponents();
                    } else {
                        Tools.AlertMessageError(apWindow, "Compra", result);
                    }
                }
            }
        } else {
            compraTB.setTipo(1);
            compraTB.setEfectivo(false);
            compraTB.setTarjeta(false);
            compraTB.setDeposito(false);
            compraTB.setEstado(1);
            compraTB.setFechaVencimiento(compraTB.getFechaCompra());
            compraTB.setHoraVencimiento(compraTB.getHoraCompra());

            if (rbEfectivoEgreso.isSelected()) {
                compraTB.setEfectivo(true);
            } else if (rbTarjetaEgreso.isSelected()) {
                compraTB.setTarjeta(true);
            } else {
                compraTB.setDeposito(true);
            }

            /*procedencia
                    1 = venta
                    2 = compra
                    
                    3 = ingreso libre
                    4 = salida libre
                    
                    5 = ingreso cuentas por cobrar
                    6 = salida cuentas por pagar
             */
 /*orma
                    1 = efectivo
                    2 = tarjeta
                    3 = deposito
             */
            IngresoTB ingresoTB = new IngresoTB();
            ingresoTB.setIdProcedencia("");
            ingresoTB.setIdUsuario(Session.USER_ID);
            ingresoTB.setDetalle("SALIDA DE DINERO POR COMPRA");
            ingresoTB.setProcedencia(2);
            ingresoTB.setFecha(Tools.getDate());
            ingresoTB.setHora(Tools.getTime());
            ingresoTB.setForma(rbEfectivoEgreso.isSelected() ? 1 : rbTarjetaEgreso.isSelected() ? 2 : 3);
            ingresoTB.setMonto(montoTotal);

            short value = Tools.AlertMessageConfirmation(apWindow, "Compra", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = CompraADO.Compra_Contado(ingresoTB, null, compraTB, tvList, loteTBs);
                if (result.equalsIgnoreCase("register")) {
                    Tools.AlertMessageInformation(apWindow, "Compra", "Se registró correctamente la compra.");
                    Tools.Dispose(apWindow);
                    comprasController.clearComponents();
                } else {
                    Tools.AlertMessageError(apWindow, "Compra", result);
                }
            }
        }
    }

    private void onEventCaja() {
        credito = false;
        egreso = false;
        caja = true;
        hbCredito.setVisible(credito);
        hbEgreso.setVisible(egreso);
        hbCaja.setVisible(caja);
        btnCredito.getStyleClass().clear();
        btnCredito.setAlignment(Pos.CENTER);
        btnCredito.getStyleClass().add("buttonBorder");
        btnEgreso.getStyleClass().clear();
        btnEgreso.setAlignment(Pos.CENTER);
        btnEgreso.getStyleClass().add("buttonBorder");
        btnCaja.getStyleClass().clear();
        btnCaja.setAlignment(Pos.CENTER);
        btnCaja.getStyleClass().add("buttonLightDefault");
    }

    private void onEventCredito() {
        caja = false;
        egreso = false;
        credito = true;
        hbCredito.setVisible(credito);
        hbEgreso.setVisible(egreso);
        hbCaja.setVisible(caja);
        btnCaja.getStyleClass().clear();
        btnCaja.setAlignment(Pos.CENTER);
        btnCaja.getStyleClass().add("buttonBorder");
        btnEgreso.getStyleClass().clear();
        btnEgreso.setAlignment(Pos.CENTER);
        btnEgreso.getStyleClass().add("buttonBorder");
        btnCredito.getStyleClass().clear();
        btnCredito.setAlignment(Pos.CENTER);
        btnCredito.getStyleClass().add("buttonLightDefault");
    }

    private void onEventEgreso() {
        caja = false;
        credito = false;
        egreso = true;
        hbCredito.setVisible(credito);
        hbEgreso.setVisible(egreso);
        hbCaja.setVisible(caja);
        btnCaja.getStyleClass().clear();
        btnCaja.setAlignment(Pos.CENTER);
        btnCaja.getStyleClass().add("buttonBorder");
        btnCredito.getStyleClass().clear();
        btnCredito.setAlignment(Pos.CENTER);
        btnCredito.getStyleClass().add("buttonBorder");
        btnEgreso.getStyleClass().clear();
        btnEgreso.setAlignment(Pos.CENTER);
        btnEgreso.getStyleClass().add("buttonLightDefault");
    }

    @FXML
    private void onKeyPressedCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCaja();
            event.consume();
        }
    }

    @FXML
    private void onActionCaja(ActionEvent event) {
        onEventCaja();
    }

    @FXML
    private void onKeyPressedCredito(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCredito();
            event.consume();
        }
    }

    @FXML
    private void onActionCredito(ActionEvent event) {
        onEventCredito();
    }

    @FXML
    private void onKeyPressedEgreso(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEgreso();
            event.consume();
        }
    }

    @FXML
    private void onActionEgreso(ActionEvent event) {
        onEventEgreso();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProcess();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventProcess();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    public void setInitComprasController(FxComprasController comprasController) {
        this.comprasController = comprasController;
    }

}
