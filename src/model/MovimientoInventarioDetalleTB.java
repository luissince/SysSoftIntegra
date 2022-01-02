

package model;

import javafx.scene.control.CheckBox;

public class MovimientoInventarioDetalleTB {
    
    private int id;
    
    private String idMovimientoInventario;
    
    private String idSuministro;
    
    private double cantidad;
    
    private double costo;
    
    private double precio;
    
    private SuministroTB suministroTB;
    
    private CheckBox verificar;
    
    private CheckBox actualizarPrecio;

    public MovimientoInventarioDetalleTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdMovimientoInventario() {
        return idMovimientoInventario;
    }

    public void setIdMovimientoInventario(String idMovimientoInventario) {
        this.idMovimientoInventario = idMovimientoInventario;
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

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }

    public CheckBox getVerificar() {
        return verificar;
    }

    public void setVerificar(CheckBox verificar) {
        this.verificar = verificar;
    }

    public CheckBox getActualizarPrecio() {
        return actualizarPrecio;
    }

    public void setActualizarPrecio(CheckBox actualizarPrecio) {
        this.actualizarPrecio = actualizarPrecio;
    }

}
