package controller.consultas.pagar;

import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.CajaTB;
import model.CompraCreditoTB;
import model.CompraTB;
import model.EgresoTB;
import model.ModeloObject;
import model.MovimientoCajaTB;
import service.CajaADO;
import service.CompraADO;

public class FxGenerarPagoController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtMonto;
//    private ComboBox<BancoTB> cbCuenta;
    @FXML
    private Button btnGuardar;
    @FXML
    private RadioButton rbCaja;
//    private RadioButton rbBancos;
    @FXML
    private RadioButton rbEgreso;
    @FXML
    private TextField txtObservacion;
    @FXML
    private RadioButton rbEfectivo;
    @FXML
    private RadioButton rbTarjeta;
    @FXML
    private RadioButton rbDeposito;

    private FxCuentasPorPagarVisualizarController cuentasPorPagarVisualizarController;

    private CompraTB compraTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
//        BancoADO.GetBancoComboBox().forEach(e -> cbCuenta.getItems().add(new BancoTB(e.getIdBanco(), e.getNombreCuenta())));
        ToggleGroup toggleGroupTipo = new ToggleGroup();
//        rbBancos.setToggleGroup(toggleGroup);
        rbEgreso.setToggleGroup(toggleGroupTipo);
        rbCaja.setToggleGroup(toggleGroupTipo);

        ToggleGroup toggleGroupForma = new ToggleGroup();
        rbEfectivo.setToggleGroup(toggleGroupForma);
        rbTarjeta.setToggleGroup(toggleGroupForma);
        rbDeposito.setToggleGroup(toggleGroupForma);
    }

    public void setInitValues(CompraTB compraTB) {
        this.compraTB = compraTB;
    }

    private void eventGuardar() {
        if (!Tools.isNumeric(txtMonto.getText().trim())) {
            Tools.AlertMessageWarning(apWindow, "Amortizar", "Ingrese la amortización.");
            txtMonto.requestFocus();
        } else if (Double.parseDouble(txtMonto.getText().trim()) <= 0) {
            Tools.AlertMessageWarning(apWindow, "Amortizar", "La amortización no puede ser menor a 0.");
            txtMonto.requestFocus();
        } else if (Tools.isText(txtObservacion.getText())) {
            Tools.AlertMessageWarning(apWindow, "Amortizar", "Ingrese alguna observación para la amortización.");
            txtObservacion.requestFocus();
        } else {
            if (rbEgreso.isSelected()) {
                short value = Tools.AlertMessageConfirmation(apWindow, "Amortizar", "¿Está seguro de continuar?");
                if (value == 1) {
                    btnGuardar.setDisable(true);
                    CompraCreditoTB compraCreditoTB = new CompraCreditoTB();
                    compraCreditoTB.setIdCompra(compraTB.getIdCompra());
                    compraCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
                    compraCreditoTB.setFechaPago(Tools.getDate());
                    compraCreditoTB.setHoraPago(Tools.getTime());
                    compraCreditoTB.setEstado(true);
                    compraCreditoTB.setIdEmpleado(Session.USER_ID);
                    compraCreditoTB.setObservacion(txtObservacion.getText().trim());

                    /*
                    procedencia
                    1 = venta
                    2 = compra
                    
                    3 = ingreso libre
                    4 = salida libre
                    
                    5 = ingreso cuentas por cobrar
                    6 = salida cuentas por pagar
                    */

                    /*
                    forma
                    1 = efectivo
                    2 = tarjeta
                    3 = deposito
                     */
                    EgresoTB egresoTB = new EgresoTB();
                    egresoTB.setIdProcedencia("");
                    egresoTB.setIdUsuario(Session.USER_ID);
                    egresoTB.setDetalle("SALIDA DE DINERO POR CUENTAS POR PAGAR DEL COMPROBANTE " + compraTB.getSerie() + "-" + compraTB.getNumeracion());
                    egresoTB.setProcedencia(6);
                    egresoTB.setFecha(Tools.getDate());
                    egresoTB.setHora(Tools.getTime());
                    egresoTB.setForma(rbEfectivo.isSelected() ? 1 : rbTarjeta.isSelected() ? 2 : 3);
                    egresoTB.setMonto(Double.parseDouble(txtMonto.getText()));

                    ModeloObject result = CompraADO.Registrar_Amortizacion(compraCreditoTB, egresoTB, null);
                    if (result.getState().equalsIgnoreCase("inserted")) {
                        Tools.Dispose(apWindow);
                        cuentasPorPagarVisualizarController.openModalImpresion(compraTB.getIdCompra());
                        cuentasPorPagarVisualizarController.loadTableCompraCredito(compraTB.getIdCompra());
                    } else if (result.getState().equalsIgnoreCase("error")) {
                        Tools.AlertMessageWarning(apWindow, "Amortizar", result.getMessage());
                        btnGuardar.setDisable(false);
                    } else {
                        Tools.AlertMessageError(apWindow, "Amortizar", "No se completo el proceso por problemas de conexión.");
                        btnGuardar.setDisable(false);
                    }
                }
            } else {
                short value = Tools.AlertMessageConfirmation(apWindow, "Amortizar", "¿Está seguro de continuar?");
                if (value == 1) {
                    Object object = CajaADO.ValidarCreacionCaja(Session.USER_ID);
                    if (object instanceof CajaTB) {
                        CajaTB cajaTB = (CajaTB) object;
                        if (cajaTB.getId() == 2) {
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
                            btnGuardar.setDisable(true);
                            CompraCreditoTB compraCreditoTB = new CompraCreditoTB();
                            compraCreditoTB.setIdCompra(compraTB.getIdCompra());
                            compraCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
                            compraCreditoTB.setFechaPago(Tools.getDate());
                            compraCreditoTB.setHoraPago(Tools.getTime());
                            compraCreditoTB.setEstado(true);
                            compraCreditoTB.setIdEmpleado(Session.USER_ID);
                            compraCreditoTB.setObservacion(txtObservacion.getText().trim());

                            MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                            movimientoCajaTB.setIdCaja(cajaTB.getIdCaja());
                            movimientoCajaTB.setFechaMovimiento(Tools.getDate());
                            movimientoCajaTB.setHoraMovimiento(Tools.getTime());
                            movimientoCajaTB.setComentario("SALIDA DE DINERO POR CUENTAS POR PAGAR DEL COMPROBANTE " + compraTB.getSerie() + "-" + compraTB.getNumeracion());
                            movimientoCajaTB.setTipoMovimiento(rbEfectivo.isSelected() ? 5 : rbTarjeta.isSelected() ? 9 : 10);
                            movimientoCajaTB.setMonto(Double.parseDouble(txtMonto.getText()));

                            ModeloObject result = CompraADO.Registrar_Amortizacion(compraCreditoTB, null, movimientoCajaTB);
                            if (result.getState().equalsIgnoreCase("inserted")) {
                                Tools.Dispose(apWindow);
                                cuentasPorPagarVisualizarController.openModalImpresion(compraTB.getIdCompra());
                                cuentasPorPagarVisualizarController.loadTableCompraCredito(compraTB.getIdCompra());
                            } else if (result.getState().equalsIgnoreCase("error")) {
                                Tools.AlertMessageWarning(apWindow, "Amortizar", result.getMessage());
                                btnGuardar.setDisable(false);
                            } else {
                                Tools.AlertMessageError(apWindow, "Amortizar", "No se completo el proceso por problemas de conexión.");
                                btnGuardar.setDisable(false);
                            }
                        } else {
                            Tools.AlertMessageWarning(apWindow, "Amortizar", "No tiene ninguna caja aperturada para continuar con el proceso.");
                        }
                    } else {
                        Tools.AlertMessageError(apWindow, "Amortizar", "No se pudo obtener el estado de su caja por problemas de red, intente nuevamente.");
                    }
                }
            }
        }
    }

    @FXML
    private void onKeyTypedMonto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtMonto.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        eventGuardar();
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

    public void setInitAmortizarPagosController(FxCuentasPorPagarVisualizarController cuentasPorPagarVisualizarController) {
        this.cuentasPorPagarVisualizarController = cuentasPorPagarVisualizarController;
    }

}
