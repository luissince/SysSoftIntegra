package model;

import javafx.beans.property.SimpleStringProperty;

public class ProveedorTB {

    private int id;
    private String idProveedor;
    private int tipoDocumento;
    private String tipoDocumentoName;
    private String numeroDocumento;
    private String razonSocial;
    private String nombreComercial;
    private int ambito;
    private int estado;
    private String telefono;
    private String celular;
    private String email;
    private String paginaWeb;
    private String direccion;
    private SimpleStringProperty estadoName;
    private String representante;

    public ProveedorTB() {

    }
    
    public ProveedorTB(String numeroDocumento, String razonSocial) {
        this.numeroDocumento = numeroDocumento;
        this.razonSocial = razonSocial;
    }
    
    public ProveedorTB(String idProveedor, String numeroDocumento, String razonSocial) {
        this.idProveedor = idProveedor;
        this.numeroDocumento = numeroDocumento;
        this.razonSocial = razonSocial;
    }
    
    public ProveedorTB(String numeroDocumento, String razonSocial, String telefono, String celular, String email, String direccion) {
        this.numeroDocumento = numeroDocumento;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.celular = celular;
        this.email = email;
        this.direccion = direccion;
    }

    public ProveedorTB(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(String idProveedor) {
        this.idProveedor = idProveedor;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento == null ? "" : numeroDocumento;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial != null ? nombreComercial : "";
    }

    public int getAmbito() {
        return ambito;
    }

    public void setAmbito(int ambito) {
        this.ambito = ambito;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
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

    public String getPaginaWeb() {
        return paginaWeb;
    }

    public void setPaginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion == null ? "" : direccion;
    }

    public String getTipoDocumentoName() {
        return tipoDocumentoName;
    }

    public void setTipoDocumentoName(String tipoDocumentoName) {
        this.tipoDocumentoName = tipoDocumentoName;
    }

    public SimpleStringProperty getEstadoName() {
        return estadoName;
    }

    public void setEstadoName(String estadoName) {
        this.estadoName = new SimpleStringProperty(estadoName);
    }

    public String getRepresentante() {
        return representante;
    }

    public void setRepresentante(String representante) {
        this.representante = representante == null ? "" : representante;
    }

    @Override
    public String toString() {
        return razonSocial;
    }

}
