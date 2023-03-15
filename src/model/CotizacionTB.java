package model;

import java.util.ArrayList;
import javafx.scene.control.Label;

public class CotizacionTB {

    private int id;

    private String idCotizacion;

    private String idCliente;

    private String idVendedor;

    private int idMoneda;

    private String fechaCotizacion;

    private String horaCotizacion;

    private String fechaVencimiento;

    private String horaVencimiento;

    private int estado;

    private double total;
    
    private String idVenta;

    private String observaciones; 
    
    private ClienteTB clienteTB;

    private EmpleadoTB empleadoTB;

    private MonedaTB monedaTB;

    private EmpresaTB empresaTB;
    
    private VentaTB ventaTB;
    
    private Label lblEstado;

    private ArrayList<CotizacionDetalleTB> cotizacionDetalleTBs;

    public CotizacionTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(String idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getFechaCotizacion() {
        return fechaCotizacion;
    }

    public void setFechaCotizacion(String fechaCotizacion) {
        this.fechaCotizacion = fechaCotizacion;
    }

    public String getHoraCotizacion() {
        return horaCotizacion;
    }

    public void setHoraCotizacion(String horaCotizacion) {
        this.horaCotizacion = horaCotizacion;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getHoraVencimiento() {
        return horaVencimiento;
    }

    public void setHoraVencimiento(String horaVencimiento) {
        this.horaVencimiento = horaVencimiento;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public ClienteTB getClienteTB() {
        return clienteTB;
    }

    public void setClienteTB(ClienteTB clienteTB) {
        this.clienteTB = clienteTB;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public EmpresaTB getEmpresaTB() {
        return empresaTB;
    }

    public void setEmpresaTB(EmpresaTB empresaTB) {
        this.empresaTB = empresaTB;
    }

    public VentaTB getVentaTB() {
        return ventaTB;
    }

    public void setVentaTB(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    public Label getLblEstado() {
        return lblEstado;
    }

    public void setLblEstado(Label lblEstado) {
        this.lblEstado = lblEstado;
    }

    public MonedaTB getMonedaTB() {
        return monedaTB;
    }

    public void setMonedaTB(MonedaTB monedaTB) {
        this.monedaTB = monedaTB;
    }

    public ArrayList<CotizacionDetalleTB> getCotizacionDetalleTBs() {
        return cotizacionDetalleTBs;
    }

    public void setCotizacionDetalleTBs(ArrayList<CotizacionDetalleTB> cotizacionDetalleTBs) {
        this.cotizacionDetalleTBs = cotizacionDetalleTBs;
    }

}
