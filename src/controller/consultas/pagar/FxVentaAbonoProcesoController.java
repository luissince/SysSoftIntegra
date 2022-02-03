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
import model.CajaADO;
import model.CajaTB;
import model.IngresoTB;
import model.ModeloObject;
import model.MovimientoCajaTB;
import model.VentaADO;
import model.VentaCreditoTB;
import model.VentaTB;

public class FxVentaAbonoProcesoController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private TextField txtMonto;
    @FXML
    private RadioButton rbCaja;
//    private RadioButton rbBancos;
    @FXML
    private RadioButton rbIngreso;
//    private ComboBox<BancoTB> cbCuenta;
    @FXML
    private Button btnAceptar;
    @FXML
    private TextField txtObservacion;
    @FXML
    private RadioButton rbEfectivo;
    @FXML
    private RadioButton rbTarjeta;
    @FXML
    private RadioButton rbDeposito;

    private FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController;

    private VentaTB ventaTB;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
//        BancoADO.GetBancoComboBox().forEach(e -> cbCuenta.getItems().add(new BancoTB(e.getIdBanco(), e.getNombreCuenta())));
        ToggleGroup toggleGroupTipo = new ToggleGroup();
//        rbBancos.setToggleGroup(toggleGroup);
        rbIngreso.setToggleGroup(toggleGroupTipo);
        rbCaja.setToggleGroup(toggleGroupTipo);

        ToggleGroup toggleGroupForma = new ToggleGroup();
        rbEfectivo.setToggleGroup(toggleGroupForma);
        rbTarjeta.setToggleGroup(toggleGroupForma);
        rbDeposito.setToggleGroup(toggleGroupForma);
    }

    public void setInitLoadVentaAbono(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    private void saveAbono() {
        if (!Tools.isNumeric(txtMonto.getText().trim())) {
            Tools.AlertMessageWarning(window, "Abonar", "Ingrese el abono.");
            txtMonto.requestFocus();
        } else if (Double.parseDouble(txtMonto.getText().trim()) <= 0) {
            Tools.AlertMessageWarning(window, "Abonar", "El abono no puede ser menor a 0.");
            txtMonto.requestFocus();
        } else if (Tools.isText(txtObservacion.getText())) {
            Tools.AlertMessageWarning(window, "Abonar", "Ingrese alguna observación del abono.");
            txtObservacion.requestFocus();
        } else {
            if (rbIngreso.isSelected()) {
                short value = Tools.AlertMessageConfirmation(window, "Abonor", "¿Está seguro de continuar?");
                if (value == 1) {
                    btnAceptar.setDisable(true);
                    VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
                    ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
                    ventaCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
                    ventaCreditoTB.setFechaPago(Tools.getDate());
                    ventaCreditoTB.setHoraPago(Tools.getTime());
                    ventaCreditoTB.setEstado((short) 1);
                    ventaCreditoTB.setIdUsuario(Session.USER_ID);
                    ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

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
                    IngresoTB ingresoTB = new IngresoTB();
                    ingresoTB.setIdProcedencia("");
                    ingresoTB.setIdUsuario(Session.USER_ID);
                    ingresoTB.setDetalle("INGRESO DE DINERO POR CUENTAS POR COBRAR DEL COMPROBANTE " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                    ingresoTB.setProcedencia(5);
                    ingresoTB.setFecha(Tools.getDate());
                    ingresoTB.setHora(Tools.getTime());
                    ingresoTB.setForma(rbEfectivo.isSelected() ? 1 : rbTarjeta.isSelected() ? 2 : 3);
                    ingresoTB.setMonto(Double.parseDouble(txtMonto.getText()));

                    ModeloObject result = VentaADO.RegistrarAbono(ventaCreditoTB, ingresoTB, null);
                    if (result.getState().equalsIgnoreCase("inserted")) {
                        Tools.Dispose(window);
                        cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(), result.getIdResult());
                        cuentasPorCobrarVisualizarController.loadData(ventaTB.getIdVenta());
                    } else if (result.getState().equalsIgnoreCase("error")) {
                        Tools.AlertMessageWarning(window, "Abonar", result.getMessage());
                    } else {
                        Tools.AlertMessageError(window, "Abonar", "No se completo el proceso por problemas de conexión.");
                        btnAceptar.setDisable(false);
                    }
                }
            } else {
                short value = Tools.AlertMessageConfirmation(window, "Abonor", "¿Está seguro de continuar?");
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
                            btnAceptar.setDisable(true);
                            VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
                            ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
                            ventaCreditoTB.setMonto(Double.parseDouble(txtMonto.getText()));
                            ventaCreditoTB.setFechaPago(Tools.getDate());
                            ventaCreditoTB.setHoraPago(Tools.getTime());
                            ventaCreditoTB.setEstado((short) 1);
                            ventaCreditoTB.setIdUsuario(Session.USER_ID);
                            ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

                            MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                            movimientoCajaTB.setIdCaja(cajaTB.getIdCaja());
                            movimientoCajaTB.setFechaMovimiento(Tools.getDate());
                            movimientoCajaTB.setHoraMovimiento(Tools.getTime());
                            movimientoCajaTB.setComentario("INGRESO DE DINERO POR CUENTAS POR COBRAR DEL COMPROBANTE " + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                            movimientoCajaTB.setTipoMovimiento(rbEfectivo.isSelected() ? 4 : rbTarjeta.isSelected() ? 7 : 8);
                            movimientoCajaTB.setMonto(Double.parseDouble(txtMonto.getText()));

                            ModeloObject result = VentaADO.RegistrarAbono(ventaCreditoTB, null, movimientoCajaTB);
                            if (result.getState().equalsIgnoreCase("inserted")) {
                                Tools.Dispose(window);
                                cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(), result.getIdResult());
                                cuentasPorCobrarVisualizarController.loadData(ventaTB.getIdVenta());
                            } else if (result.getState().equalsIgnoreCase("error")) {
                                Tools.AlertMessageWarning(window, "Abonar", result.getMessage());
                            } else {
                                Tools.AlertMessageError(window, "Abonar", "No se completo el proceso por problemas de conexión.");
                                btnAceptar.setDisable(false);
                            }
                        } else {
                            Tools.AlertMessageWarning(window, "Abonar", "No tiene ninguna caja aperturada para continuar con el proceso.");
                        }
                    } else {
                        Tools.AlertMessageError(window, "Abonar", "No se pudo obtener el estado de su caja por problemas de red, intente nuevamente.");
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
    private void onActionAceptar(ActionEvent event) {
        saveAbono();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            saveAbono();
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

    public void setInitVentaAbonarController(FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController) {
        this.cuentasPorCobrarVisualizarController = cuentasPorCobrarVisualizarController;
    }

}
