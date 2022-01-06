
package model;

import javafx.scene.control.Button;


public class OrdenCompraDetalleTB {
    
    private int id;
   
    private String idOrdenCompra;
    
    private String idSuministro;
    
    private double cantidad;
    
    private double descuento;
    
    private double costo;
    
    private int idImpuesto;
    
    private String observacion;
    
    private Button btnRemove;
    
    private ImpuestoTB impuestoTB;
    
    private SuministroTB suministroTB;

    public OrdenCompraDetalleTB() {
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

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button btnRemove) {
        this.btnRemove = btnRemove;
    }

    public ImpuestoTB getImpuestoTB() {
        return impuestoTB;
    }

    public void setImpuestoTB(ImpuestoTB impuestoTB) {
        this.impuestoTB = impuestoTB;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }
    
}
