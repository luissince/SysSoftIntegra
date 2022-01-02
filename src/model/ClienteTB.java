package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.ImageView;

public class ClienteTB {

    private SimpleIntegerProperty id;
    private String idCliente;
    private int tipoDocumento;
    private String tipoDocumentoName;
    private String numeroDocumento;
    private String informacion;
    private String telefono;
    private String celular;
    private String email;
    private String direccion;
    private int estado;
    private String estadoName;
    private String representante;
    private boolean predeterminado;
    private boolean sistema;
    private ImageView imagePredeterminado;
    private String idAuxiliar;

    public ClienteTB() {

    }

    public ClienteTB(String informacion) {
        this.informacion = informacion;
    }

    public ClienteTB(String numeroDocumento, String informacion) {
        this.numeroDocumento = numeroDocumento;
        this.informacion = informacion;
    }

    public ClienteTB(String numeroDocumento, String informacion, String celular, String email, String direccion) {
        this.numeroDocumento = numeroDocumento;
        this.informacion = informacion;
        this.celular = celular;
        this.email = email;
        this.direccion = direccion;
    }

    public ClienteTB(String idCliente, String numeroDocumento, String informacion, String celular, String email, String direccion) {
        this.idCliente = idCliente;
        this.numeroDocumento = numeroDocumento;
        this.informacion = informacion;
        this.celular = celular;
        this.email = email;
        this.direccion = direccion;
    }

    public ClienteTB(String idCliente, int tipoDocumento, String numeroDocumento, String informacion, String celular, String email, String direccion) {
        this.idCliente = idCliente;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.informacion = informacion;
        this.celular = celular;
        this.email = email;
        this.direccion = direccion;
    }

    public ClienteTB(String idCliente, int tipoDocumento, String numeroDocumento, String informacion, String telefono, String celular, String email, String direccion) {
        this.idCliente = idCliente;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.informacion = informacion;
        this.celular = celular;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public SimpleIntegerProperty getId() {
        return id;
    }

    public void setId(int id) {
        this.id = new SimpleIntegerProperty(id);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getTipoDocumentoName() {
        return tipoDocumentoName;
    }

    public void setTipoDocumentoName(String tipoDocumentoName) {
        this.tipoDocumentoName = tipoDocumentoName;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getInformacion() {
        return informacion;
    }

    public void setInformacion(String informacion) {
        this.informacion = informacion == null ? "" : informacion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono == null ? "" : telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular == null ? "" : celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? "" : email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion == null ? "" : direccion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getEstadoName() {
        return estadoName;
    }

    public void setEstadoName(String estadoName) {
        this.estadoName = estadoName == null ? "" : estadoName;
    }

    public String getRepresentante() {
        return representante;
    }

    public void setRepresentante(String representante) {
        this.representante = representante == null ? "" : representante;
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

    public ImageView getImagePredeterminado() {
        return imagePredeterminado;
    }

    public void setImagePredeterminado(ImageView imagePredeterminado) {
        this.imagePredeterminado = imagePredeterminado;
    }

    public String getIdAuxiliar() {
        return idAuxiliar;
    }

    public void setIdAuxiliar(String idAuxiliar) {
        this.idAuxiliar = idAuxiliar;
    }

    @Override
    public String toString() {
        return informacion;
    }

}
