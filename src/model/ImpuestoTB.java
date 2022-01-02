package model;

import javafx.scene.image.ImageView;

public class ImpuestoTB {
    
    private int id;
    private int idImpuesto;
    private int operacion;
    private String nombreOperacion;
    private String nombre;
    private double valor;
    private String codigo;
    private String numeracion;
    private String nombreImpuesto;
    private String letra;
    private String categoria;
    private boolean predeterminado;
    private boolean sistema;
    private ImageView imagePredeterminado;

    public ImpuestoTB() {
    }

    public ImpuestoTB(String nombre, double valor) {
        this.nombre = nombre;
        this.valor = valor;
    }    

    public ImpuestoTB(int idImpuesto, String nombre, Boolean predeterminado) {
        this.idImpuesto = idImpuesto;
        this.nombre = nombre;
        this.predeterminado = predeterminado;
    }

    public ImpuestoTB(int idImpuesto, String nombreImpuesto, double valor) {
        this.idImpuesto = idImpuesto;
        this.nombre = nombreImpuesto;
        this.valor = valor;
    }

    public ImpuestoTB(int idImpuesto, String nombre, double valor, boolean predeterminado) {
        this.idImpuesto = idImpuesto;
        this.nombre = nombre;
        this.valor = valor;
        this.predeterminado = predeterminado;
    }

    public ImpuestoTB(int idImpuesto, int operacion, String nombre, double valor, boolean predeterminado) {
        this.idImpuesto = idImpuesto;
        this.operacion = operacion;
        this.nombre = nombre;
        this.valor = valor;
        this.predeterminado = predeterminado;
    }

    public ImpuestoTB(int idImpuesto, String nombreOperacion, String nombre, double valor, boolean predeterminado) {
        this.idImpuesto = idImpuesto;
        this.nombreOperacion = nombreOperacion;
        this.nombre = nombre;
        this.valor = valor;
        this.predeterminado = predeterminado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public int getOperacion() {
        return operacion;
    }

    public void setOperacion(int operacion) {
        this.operacion = operacion;
    }

    public String getNombreOperacion() {
        return nombreOperacion;
    }

    public void setNombreOperacion(String nombreOperacion) {
        this.nombreOperacion = nombreOperacion == null ? "" : nombreOperacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(String numeracion) {
        this.numeracion = numeracion;
    }

    public String getNombreImpuesto() {
        return nombreImpuesto;
    }

    public void setNombreImpuesto(String nombreImpuesto) {
        this.nombreImpuesto = nombreImpuesto;
    }

    public String getLetra() {
        return letra;
    }

    public void setLetra(String letra) {
        this.letra = letra;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public ImageView getImagePredeterminado() {
        return imagePredeterminado;
    }

    public void setImagePredeterminado(ImageView imagePredeterminado) {
        this.imagePredeterminado = imagePredeterminado;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }

    @Override
    public String toString() {
        return nombre;
    }

}
