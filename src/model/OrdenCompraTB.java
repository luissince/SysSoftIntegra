
package model;

import java.util.ArrayList;

public class OrdenCompraTB {
    
    private int id;
    
    private String idOrdenCompra;
    
    private int numeracion;
    
    private String idProveedor;
    
    private String idEmpleado;
    
    private String fechaRegistro;
    
    private String horaRegistro;
    
    private String fechaVencimiento;
    
    private String horaVencimiento;
    
    private String observacion;
    
    private ProveedorTB proveedorTB;
    
    private EmpleadoTB empleadoTB;
    
    private ArrayList<OrdenCompraDetalleTB> ordenCompraDetalleTBs;
    
    private double total;

    public OrdenCompraTB() {
    
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdOrdenCompra() {
        return idOrdenCompra;
    }

    public void setIdOrdenCompra(String idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public int getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(int numeracion) {
        this.numeracion = numeracion;
    }

    public String getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(String idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(String horaRegistro) {
        this.horaRegistro = horaRegistro;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public ProveedorTB getProveedorTB() {
        return proveedorTB;
    }

    public void setProveedorTB(ProveedorTB proveedorTB) {
        this.proveedorTB = proveedorTB;
    }

    public ArrayList<OrdenCompraDetalleTB> getOrdenCompraDetalleTBs() {
        return ordenCompraDetalleTBs;
    }

    public void setOrdenCompraDetalleTBs(ArrayList<OrdenCompraDetalleTB> ordenCompraDetalleTBs) {
        this.ordenCompraDetalleTBs = ordenCompraDetalleTBs;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
    
}
