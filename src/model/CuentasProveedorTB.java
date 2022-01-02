
package model;


public class CuentasProveedorTB {
   
    private String idCompra;
    private int idCuentasProveedor;
    private double montoTotal;
    private int plazos;
    private String plazosName;
    private String fechaPago;
    private String fechaRegistro;
    
    public CuentasProveedorTB() {
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdCuentasProveedor() {
        return idCuentasProveedor;
    }

    public void setIdCuentasProveedor(int idCuentasProveedor) {
        this.idCuentasProveedor = idCuentasProveedor;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public int getPlazos() {
        return plazos;
    }

    public void setPlazos(int plazos) {
        this.plazos = plazos;
    }

    public String getPlazosName() {
        return plazosName;
    }

    public void setPlazosName(String plazosName) {
        this.plazosName = plazosName;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
   
}
