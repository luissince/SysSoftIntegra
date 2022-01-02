package model;

import java.util.ArrayList;

public class MermaTB {

    private int id;
    private String idMerma;
    private String idProduccion;
    private String idUsuario;
    private double costo;
    private double cantidad;
    private SuministroTB suministroTB;
    private ProduccionTB produccionTB;
    private String tipoMermaNombre;
    private DetalleTB dtTipoMerma;
    private ArrayList<SuministroTB> suministroTBs;

    private String productoReporte;
    private String tipoMermaReporte;
    private double costoReporte;
    private double cantidadReporte;
    private String unidadReporte;

    public MermaTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdMerma() {
        return idMerma;
    }

    public void setIdMerma(String idMerma) {
        this.idMerma = idMerma;
    }

    public String getIdProduccion() {
        return idProduccion;
    }

    public void setIdProduccion(String idProduccion) {
        this.idProduccion = idProduccion;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }

    public ProduccionTB getProduccionTB() {
        return produccionTB;
    }

    public void setProduccionTB(ProduccionTB produccionTB) {
        this.produccionTB = produccionTB;
    }

    public String getTipoMermaNombre() {
        return tipoMermaNombre;
    }

    public void setTipoMermaNombre(String tipoMermaNombre) {
        this.tipoMermaNombre = tipoMermaNombre;
    }

    public DetalleTB getDtTipoMerma() {
        return dtTipoMerma;
    }

    public void setDtTipoMerma(DetalleTB dtTipoMerma) {
        this.dtTipoMerma = dtTipoMerma;
    }

    public ArrayList<SuministroTB> getSuministroTBs() {
        return suministroTBs;
    }

    public void setSuministroTBs(ArrayList<SuministroTB> suministroMerma) {
        this.suministroTBs = suministroMerma;
    }

    public String getProductoReporte() {
        return productoReporte;
    }

    public void setProductoReporte(String productoReporte) {
        this.productoReporte = productoReporte;
    }

    public String getTipoMermaReporte() {
        return tipoMermaReporte;
    }

    public void setTipoMermaReporte(String tipoMermaReporte) {
        this.tipoMermaReporte = tipoMermaReporte;
    }

    public double getCostoReporte() {
        return costoReporte;
    }

    public void setCostoReporte(double costoReporte) {
        this.costoReporte = costoReporte;
    }

    public double getCantidadReporte() {
        return cantidadReporte;
    }

    public void setCantidadReporte(double cantidadReporte) {
        this.cantidadReporte = cantidadReporte;
    }

    public String getUnidadReporte() {
        return unidadReporte;
    }

    public void setUnidadReporte(String unidadReporte) {
        this.unidadReporte = unidadReporte;
    }


}
