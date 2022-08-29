package model;

import controller.tools.Tools;
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
    private boolean tipoSelect;
    
    private int idTipoDocumentoConductor;
    private String numeroDocumentoConductor;
    private String nombreConductor;
    private String celularConductor;
    private String placaVehiculo;
    private String marcaVehiculo;
    private int idModalidadTraslado;
    private int idMotivoTraslado;
    private int IdUbigeo;

    private UbigeoTB ubigeoTB;
    private ConductorTB conductorTB;
   
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

    public boolean isTipoSelect() {
        return tipoSelect;
    }

    public void setTipoSelect(boolean tipoSelect) {
        this.tipoSelect = tipoSelect;
    }

    public int getIdTipoDocumentoConductor() {
        return idTipoDocumentoConductor;
    }

    public void setIdTipoDocumentoConductor(int idTipoDocumentoConductor) {
        this.idTipoDocumentoConductor = idTipoDocumentoConductor;
    }

    public String getNumeroDocumentoConductor() {
        return numeroDocumentoConductor;
    }

    public void setNumeroDocumentoConductor(String numeroDocumentoConductor) {
        this.numeroDocumentoConductor = numeroDocumentoConductor;
    }
    
    public String getNombreConductor() {
        return nombreConductor;
    }

    public void setNombreConductor(String nombreConductor) {
        this.nombreConductor = nombreConductor;
    }
    
    public String getCelularConductor() {
        return celularConductor;
    }

    public void setCelularConductor(String celularConductor) {
        this.celularConductor = celularConductor;
    }   
    
    
    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }   

    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }

    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }  
    
   
      public int getIdModalidadTraslado() {
        return idModalidadTraslado;
    }

    public void setIdModalidadTraslado(int idModalidadTraslado) {
        this.idModalidadTraslado = idModalidadTraslado;
    }  

    public int getIdMotivoTraslado() {
        return idMotivoTraslado;
    }

    public void setIdMotivoTraslado(int idMotivoTraslado) {
        this.idMotivoTraslado = idMotivoTraslado;
    }
     public int getIdUbigeo() {
        return IdUbigeo;
    }

    public void setIdUbigeo(int IdUbigeo) {
        this.IdUbigeo = IdUbigeo;
    }

    public UbigeoTB getUbigeoTB() {
        return ubigeoTB;
    }

    public void setUbigeoTB(UbigeoTB ubigeoTB) {
        this.ubigeoTB = ubigeoTB;
    }

    public ConductorTB getConductorTB() {
        return conductorTB;
    }

    public void setConductorTB(ConductorTB conductorTB) {
        this.conductorTB = conductorTB;
    }
    
    @Override
    public String toString() {
        if (tipoSelect) {
            return informacion;
        } else {
            return numeroDocumento + " - " + informacion;
        }
    }

}
