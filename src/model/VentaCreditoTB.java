
package model;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;


public class VentaCreditoTB {
        
    private int id;
    private String idVenta;
    private String idVentaCredito;
    private double monto;
    private String fechaPago;
    private String horaPago;
    private short estado;
    private String idUsuario;
    private String observacion;
    private EmpleadoTB empleadoTB;
    private VentaTB ventaTB;
    
    private TextField tfMonto;
    private DatePicker dpFecha;
    private CheckBox cbMontoInicial;
    private Button btnImprimir;
    private Button btnQuitar;
    
    public VentaCreditoTB(){
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public String getIdVentaCredito() {
        return idVentaCredito;
    }

    public void setIdVentaCredito(String idVentaCredito) {
        this.idVentaCredito = idVentaCredito;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getHoraPago() {
        return horaPago;
    }

    public void setHoraPago(String horaPago) {
        this.horaPago = horaPago;
    }

    public short getEstado() {
        return estado;
    }

    public void setEstado(short estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public VentaTB getVentaTB() {
        return ventaTB;
    }

    public void setVentaTB(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    public TextField getTfMonto() {
        return tfMonto;
    }

    public void setTfMonto(TextField tfMonto) {
        this.tfMonto = tfMonto;
    }

    public DatePicker getDpFecha() {
        return dpFecha;
    }

    public void setDpFecha(DatePicker dpFecha) {
        this.dpFecha = dpFecha;
    }

    public CheckBox getCbMontoInicial() {
        return cbMontoInicial;
    }

    public void setCbMontoInicial(CheckBox cbMontoInicial) {
        this.cbMontoInicial = cbMontoInicial;
    }

    public Button getBtnImprimir() {
        return btnImprimir;
    }

    public void setBtnImprimir(Button btnImprimir) {
        this.btnImprimir = btnImprimir;
    }

    public Button getBtnQuitar() {
        return btnQuitar;
    }

    public void setBtnQuitar(Button btnQuitar) {
        this.btnQuitar = btnQuitar;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

}
