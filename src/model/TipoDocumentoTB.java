package model;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class TipoDocumentoTB {

    private int id;
    private int idTipoDocumento;
    private String nombre;
    private String serie;
    private int numeracion;
    private boolean predeterminado;
    private boolean sistema;
    private boolean guia;
    private boolean factura;
    private boolean notaCredito;
    private boolean estado;
    private boolean campo;
    private int numeroCampo;
    private String codigoAlterno;
    private Label lblDestino;
    private ImageView ivPredeterminado;

    public TipoDocumentoTB() {

    }

    public TipoDocumentoTB(int idTipoDocumento, String nombre) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombre = nombre;
    }

    public TipoDocumentoTB(int idTipoDocumento, String nombre, boolean predeterminado) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombre = nombre;
        this.predeterminado = predeterminado;
    }

    public TipoDocumentoTB(int idTipoDocumento, String nombre, boolean predeterminado, ImageView ivPredeterminado) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombre = nombre;
        this.predeterminado = predeterminado;
        this.ivPredeterminado = ivPredeterminado;
    }

    public TipoDocumentoTB(int idTipoDocumento, String nombre, boolean predeterminado, Label lblDestino, ImageView ivPredeterminado) {
        this.idTipoDocumento = idTipoDocumento;
        this.nombre = nombre;
        this.predeterminado = predeterminado;
        this.lblDestino = lblDestino;
        this.ivPredeterminado = ivPredeterminado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(int numeracion) {
        this.numeracion = numeracion;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }

    public boolean isGuia() {
        return guia;
    }

    public void setGuia(boolean guia) {
        this.guia = guia;
    }

    public boolean isFactura() {
        return factura;
    }

    public void setFactura(boolean factura) {
        this.factura = factura;
    }

    public boolean isNotaCredito() {
        return notaCredito;
    }

    public void setNotaCredito(boolean notaCredito) {
        this.notaCredito = notaCredito;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public boolean isCampo() {
        return campo;
    }

    public void setCampo(boolean campo) {
        this.campo = campo;
    }

    public int getNumeroCampo() {
        return numeroCampo;
    }

    public void setNumeroCampo(int numeroCampo) {
        this.numeroCampo = numeroCampo;
    }

    public String getCodigoAlterno() {
        return codigoAlterno;
    }

    public void setCodigoAlterno(String codigoAlterno) {
        this.codigoAlterno = codigoAlterno == null ? "" : codigoAlterno;
    }

    public Label getLblDestino() {
        return lblDestino;
    }

    public void setLblDestino(Label lblDestino) {
        this.lblDestino = lblDestino;
    }

    public ImageView getIvPredeterminado() {
        return ivPredeterminado;
    }

    public void setIvPredeterminado(ImageView ivPredeterminado) {
        this.ivPredeterminado = ivPredeterminado;
    }

    @Override
    public String toString() {
        return nombre;
    }

}
