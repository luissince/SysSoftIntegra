package model;

import javafx.scene.control.Button;

public class CotizacionDetalleTB {

    private int id;

    private String idCotizacion;

    private String idSuministro;

    private double cantidad;

    private double precio;

    private double descuento;

    private int idImpuesto;

    private int idMedida;

    private boolean uso;

    private SuministroTB suministroTB;

    private ImpuestoTB impuestoTB;

    private Button btnRemove;

    public CotizacionDetalleTB() {
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

    public int getIdMedida() {
        return idMedida;
    }

    public void setIdMedida(int idMedida) {
        this.idMedida = idMedida;
    }

    public boolean isUso() {
        return uso;
    }

    public void setUso(boolean uso) {
        this.uso = uso;
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

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button btnRemove) {
        this.btnRemove = btnRemove;
    }

}
