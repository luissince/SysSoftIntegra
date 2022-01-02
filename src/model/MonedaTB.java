package model;

import javafx.scene.image.ImageView;

public class MonedaTB {
    
    private int id;
    private int idMoneda;
    private String nombre;
    private String abreviado;
    private String simbolo;
    private Double tipoCambio;
    private Boolean predeterminado;
    private Boolean sistema;
    private ImageView imagePredeterminado;

    public MonedaTB() {
    }

    public MonedaTB(String simbolo) {
        this.simbolo = simbolo;
    }

    public MonedaTB(String nombre, String simbolo) {
        this.nombre = nombre;
        this.simbolo = simbolo;
    }

    public MonedaTB(int idMoneda, String nombre) {
        this.idMoneda = idMoneda;
        this.nombre = nombre;
    }

    public MonedaTB(int idMoneda, String nombre, Boolean predeterminado) {
        this.idMoneda = idMoneda;
        this.nombre = nombre;
        this.predeterminado = predeterminado;
    }

    public MonedaTB(int idMoneda, String nombre, String simbolo, Boolean predeterminado) {
        this.idMoneda = idMoneda;
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.predeterminado = predeterminado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAbreviado() {
        return abreviado;
    }

    public void setAbreviado(String abreviado) {
        this.abreviado = abreviado;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public Double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(Double tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public Boolean getSistema() {
        return sistema;
    }

    public void setSistema(Boolean sistema) {
        this.sistema = sistema;
    }

    public Boolean getPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(Boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    public ImageView getImagePredeterminado() {
        return imagePredeterminado;
    }

    public void setImagePredeterminado(ImageView imagePredeterminado) {
        this.imagePredeterminado = imagePredeterminado;
    }

    @Override
    public String toString() {
        return  nombre ;
    }
    
    
    
    
}
