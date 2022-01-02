package model;

import controller.tools.Tools;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PreciosTB {

    private int id;
    private int idPrecios;
    private String idArticulo;
    private String idSuministro;
    private String nombre;
    private double valor;
    private double factor;
    private boolean estado;

    private TextField txtNombre;
    private TextField txtValor;
    private TextField txtFactor;
    private Button btnOpcion;

    public PreciosTB() {
    }

    public PreciosTB(int idPrecios, String nombre, double valor) {
        this.idPrecios = idPrecios;
        this.nombre = nombre;
        this.valor = valor;
    }

    public PreciosTB(int idPrecios, String nombre, double valor, double factor) {
        this.idPrecios = idPrecios;
        this.nombre = nombre;
        this.valor = valor;
        this.factor = factor;
    }

    public PreciosTB(String nombre, double valor, double factor) {
        this.nombre = nombre;
        this.valor = valor;
        this.factor = factor;
    }

    public PreciosTB(String nombre, double valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPrecios() {
        return idPrecios;
    }

    public void setIdPrecios(int idPrecios) {
        this.idPrecios = idPrecios;
    }

    public String getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(String idArticulo) {
        this.idArticulo = idArticulo;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
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

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public TextField getTxtNombre() {
        return txtNombre;
    }

    public void setTxtNombre(TextField txtNombre) {
        this.txtNombre = txtNombre;
    }

    public TextField getTxtValor() {
        return txtValor;
    }

    public void setTxtValor(TextField txtValor) {
        this.txtValor = txtValor;
    }

    public TextField getTxtFactor() {
        return txtFactor;
    }

    public void setTxtFactor(TextField txtFactor) {
        this.txtFactor = txtFactor;
    }

    public Button getBtnOpcion() {
        return btnOpcion;
    }

    public void setBtnOpcion(Button btnOpcion) {
        this.btnOpcion = btnOpcion;
    }

    @Override
    public String toString() {
        return nombre + ": " + Tools.roundingValue(valor, 2) + (factor <= 1 ? "" : " Factor(" + Tools.roundingValue(factor, 2)+")");
    }
}
