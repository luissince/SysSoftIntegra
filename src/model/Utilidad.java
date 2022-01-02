package model;

public class Utilidad {

    private int id;
    private String idSuministro;
    private String clave;
    private String nombreMarca;
    private double cantidad;
    private String medida;
    private double costoVenta;
    private double costoVentaTotal;
    private double precioVenta;
    private double precioVentaTotal;
    private boolean valorInventario;
    private String unidadCompra;
    private String simboloMoneda;
    private double utilidad;

    public Utilidad() {
    }

    public Utilidad(int id, String nombreMarca) {
        this.id = id;
        this.nombreMarca = nombreMarca;
    }

    public Utilidad(int id, String nombreMarca, double cantidad) {
        this.id = id;
        this.nombreMarca = nombreMarca;
        this.cantidad = cantidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public double getCostoVenta() {
        return costoVenta;
    }

    public void setCostoVenta(double costoVenta) {
        this.costoVenta = costoVenta;
    }

    public double getCostoVentaTotal() {
        return costoVentaTotal;
    }

    public void setCostoVentaTotal(double costoVentaTotal) {
        this.costoVentaTotal = costoVentaTotal;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getPrecioVentaTotal() {
        return precioVentaTotal;
    }

    public void setPrecioVentaTotal(double precioVentaTotal) {
        this.precioVentaTotal = precioVentaTotal;
    }

    public boolean isValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(boolean valorInventario) {
        this.valorInventario = valorInventario;
    }

    public String getUnidadCompra() {
        return unidadCompra;
    }

    public void setUnidadCompra(String unidadCompra) {
        this.unidadCompra = unidadCompra;
    }

    public String getSimboloMoneda() {
        return simboloMoneda;
    }

    public void setSimboloMoneda(String simboloMoneda) {
        this.simboloMoneda = simboloMoneda;
    }

    public double getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(double utilidad) {
        this.utilidad = utilidad;
    }

}
