package model;

import java.util.ArrayList;
import javafx.scene.control.Button;

public class DetalleCompraTB {

    private int id;
    private String idCompra;
    private String idSuministro;
    private String descripcion;
    private String medida;
    private double cantidad;
    private double precioCompra;
    private double descuento;
    private double importeBruto;
    private double descuentoBruto;
    private double subImporteNeto;
    private double impuestoGenerado;
    private double importeNeto;
    private int idImpuesto;
    private String nombreImpuesto;
    private double valorImpuesto;
    private boolean lote;
    private boolean cambiarPrecio;
    private ArrayList<LoteTB> listLote;
    private SuministroTB suministroTB;
    private Button remove;

    public DetalleCompraTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
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

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getImporteBruto() {
        return importeBruto;
    }

    public void setImporteBruto(double importeBruto) {
        this.importeBruto = importeBruto;
    }

    public double getDescuentoBruto() {
        return descuentoBruto;
    }

    public void setDescuentoBruto(double descuentoBruto) {
        this.descuentoBruto = descuentoBruto;
    }

    public double getSubImporteNeto() {
        return subImporteNeto;
    }

    public void setSubImporteNeto(double subImporteNeto) {
        this.subImporteNeto = subImporteNeto;
    }

    public double getImpuestoGenerado() {
        return impuestoGenerado;
    }

    public void setImpuestoGenerado(double impuestoGenerado) {
        this.impuestoGenerado = impuestoGenerado;
    }

    public double getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(double importeNeto) {
        this.importeNeto = importeNeto;
    }

    public int getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(int idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public String getNombreImpuesto() {
        return nombreImpuesto;
    }

    public void setNombreImpuesto(String nombreImpuesto) {
        this.nombreImpuesto = nombreImpuesto;
    }

    public double getValorImpuesto() {
        return valorImpuesto;
    }

    public void setValorImpuesto(double valorImpuesto) {
        this.valorImpuesto = valorImpuesto;
    }

    public boolean isLote() {
        return lote;
    }

    public void setLote(boolean lote) {
        this.lote = lote;
    }

    public boolean isCambiarPrecio() {
        return cambiarPrecio;
    }

    public void setCambiarPrecio(boolean cambiarPrecio) {
        this.cambiarPrecio = cambiarPrecio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public ArrayList<LoteTB> getListLote() {
        return listLote;
    }

    public void setListLote(ArrayList<LoteTB> listLote) {
        this.listLote = listLote;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }

    public Button getRemove() {
        return remove;
    }

    public void setRemove(Button remove) {
        this.remove = remove;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

}
