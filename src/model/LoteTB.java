package model;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

public class LoteTB {
    
    /*
    Atributos para las vistas en general
    */
    private int id;
    private long idLote;
    private String numeroLote;
    private ObjectProperty<LocalDate> fechaCaducidad;
    private double existenciaInicial;
    private double existenciaActual;
    private String idArticulo;
    private String idCompra;
    private SuministroTB suministroTB;
    private Label lblEstado;
    /*
    Atributos para el reporte
     */
    private String descripcion;
    private String fechaListado;
    private String cantidad;
    
    public LoteTB() {

    }

    public LoteTB(int id, String numeroLote) {
        this.id = id;
        this.numeroLote = numeroLote;
    }

    public LoteTB(int id, String numeroLote, String descripcion, String fechaListado, String cantidad) {
        this.id = id;
        this.numeroLote = numeroLote;
        this.descripcion = descripcion;
        this.fechaListado = fechaListado;
        this.cantidad = cantidad;
    }
    
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getIdLote() {
        return idLote;
    }

    public void setIdLote(long idLote) {
        this.idLote = idLote;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad.get();
    }

    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = new SimpleObjectProperty(fechaCaducidad);
    }

    public double getExistenciaInicial() {
        return existenciaInicial;
    }

    public void setExistenciaInicial(double existenciaInicial) {
        this.existenciaInicial = existenciaInicial;
    }

    public double getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(double existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public String getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(String idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    } 

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaListado() {
        return fechaListado;
    }

    public void setFechaListado(String fechaListado) {
        this.fechaListado = fechaListado;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public Label getLblEstado() {
        return lblEstado;
    }

    public void setLblEstado(Label lblEstado) {
        this.lblEstado = lblEstado;
    }

}
