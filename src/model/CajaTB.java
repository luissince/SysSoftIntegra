package model;

import javafx.scene.control.Label;


public class CajaTB {

    private int id;
    private String idCaja;
    private String fechaApertura;
    private String fechaCierre;
    private String horaApertura;
    private String horaCierre;
    private boolean estado;
    private double contado;
    private double calculado;
    private double diferencia;
    private String idUsuario;
    private EmpleadoTB empleadoTB;
    private MovimientoCajaTB movimientoCajaTB;
    private Label labelEstado;
    public CajaTB() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }    
    
    public String getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(String idCaja) {
        this.idCaja = idCaja;
    }

    public String getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(String fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(String fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(String horaApertura) {
        this.horaApertura = horaApertura;
    }

    public String getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(String horaCierre) {
        this.horaCierre = horaCierre;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public double getContado() {
        return contado;
    }

    public void setContado(double contado) {
        this.contado = contado;
    }

    public double getCalculado() {
        return calculado;
    }

    public void setCalculado(double calculado) {
        this.calculado = calculado;
    }

    public double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(double diferencia) {
        this.diferencia = diferencia;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public MovimientoCajaTB getMovimientoCajaTB() {
        return movimientoCajaTB;
    }

    public void setMovimientoCajaTB(MovimientoCajaTB movimientoCajaTB) {
        this.movimientoCajaTB = movimientoCajaTB;
    }

    public Label getLabelEstado() {
        return labelEstado;
    }

    public void setLabelEstado(Label labelEstado) {
        this.labelEstado = labelEstado;
    }

    
}
