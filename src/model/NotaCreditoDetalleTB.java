

package model;


public class NotaCreditoDetalleTB {
    
    private int id;
    private String idNotaCredito;
    private String idSuministro;
    private double cantidad;
    private double precio;
    private double descuento;
    private int idImpuesto;
    private SuministroTB suministroTB;
    private ImpuestoTB impuestoTB;
    
    public NotaCreditoDetalleTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }  

    public String getIdNotaCredito() {
        return idNotaCredito;
    }

    public void setIdNotaCredito(String idNotaCredito) {
        this.idNotaCredito = idNotaCredito;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }   

    public ImpuestoTB getImpuestoTB() {
        return impuestoTB;
    }

    public void setImpuestoTB(ImpuestoTB impuestoTB) {
        this.impuestoTB = impuestoTB;
    }

    
}
