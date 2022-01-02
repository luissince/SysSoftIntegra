
package model;

import javafx.collections.ObservableList;


public class GuiaRemisionTB {
    
    private int id;
    private String idVenta;
    private String idGuiaRemision;
    private int idComprobante;
    private String serie;
    private String numeracion;
    private String idCliente;
    private String idVendedor;
    private String email;
    private int idMotivoTraslado;
    private String motivoTrasladoDescripcion;
    private int idModalidadTraslado;
    private String modalidadTrasladDescripcion;
    private String fechaTraslado;
    private String horaTraslado;
    private double pesoBruto;
    private int numeroBultos;
    private int tipoDocumentoConducto;
    private String numeroConductor;
    private String nombreConductor;
    private String telefonoCelularConducto;
    private String numeroPlaca;
    private String marcaVehiculo; 
    private String direccionPartida;
    private int idUbigeoPartida;
    private String ubigeoPartidaDescripcion;
    private String direccionLlegada;
    private int idUbigeoLlegada;
    private String ubigeoLlegadaDescripcion;
    private int idTipoComprobanteFactura;
    private String comprobanteAsociado;
    private String serieFactura;
    private String numeracionFactura;
    private ObservableList<GuiaRemisionDetalleTB> listGuiaRemisionDetalle;
    private ClienteTB clienteTB;
    private EmpleadoTB empleadoTB;
    
    public GuiaRemisionTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public String getIdGuiaRemision() {
        return idGuiaRemision;
    }

    public void setIdGuiaRemision(String idGuiaRemision) {
        this.idGuiaRemision = idGuiaRemision;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(String numeracion) {
        this.numeracion = numeracion;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIdMotivoTraslado() {
        return idMotivoTraslado;
    }

    public void setIdMotivoTraslado(int idMotivoTraslado) {
        this.idMotivoTraslado = idMotivoTraslado;
    }

    public String getModalidadTrasladDescripcion() {
        return modalidadTrasladDescripcion;
    }

    public void setModalidadTrasladDescripcion(String modalidadTrasladDescripcion) {
        this.modalidadTrasladDescripcion = modalidadTrasladDescripcion;
    }

    public int getIdModalidadTraslado() {
        return idModalidadTraslado;
    }

    public void setIdModalidadTraslado(int idModalidadTraslado) {
        this.idModalidadTraslado = idModalidadTraslado;
    }

    public String getMotivoTrasladoDescripcion() {
        return motivoTrasladoDescripcion;
    }

    public void setMotivoTrasladoDescripcion(String motivoTrasladoDescripcion) {
        this.motivoTrasladoDescripcion = motivoTrasladoDescripcion;
    }

    public String getFechaTraslado() {
        return fechaTraslado;
    }

    public void setFechaTraslado(String fechaTraslado) {
        this.fechaTraslado = fechaTraslado;
    }

    public String getHoraTraslado() {
        return horaTraslado;
    }

    public void setHoraTraslado(String horaTraslado) {
        this.horaTraslado = horaTraslado;
    }

    public double getPesoBruto() {
        return pesoBruto;
    }

    public void setPesoBruto(double pesoBruto) {
        this.pesoBruto = pesoBruto;
    }

    public int getNumeroBultos() {
        return numeroBultos;
    }

    public void setNumeroBultos(int numeroBultos) {
        this.numeroBultos = numeroBultos;
    }

    public int getTipoDocumentoConducto() {
        return tipoDocumentoConducto;
    }

    public void setTipoDocumentoConducto(int tipoDocumentoConducto) {
        this.tipoDocumentoConducto = tipoDocumentoConducto;
    }

    public String getNumeroConductor() {
        return numeroConductor;
    }

    public void setNumeroConductor(String numeroConductor) {
        this.numeroConductor = numeroConductor;
    }

    public String getNombreConductor() {
        return nombreConductor;
    }

    public void setNombreConductor(String nombreConductor) {
        this.nombreConductor = nombreConductor;
    }

    public String getTelefonoCelularConducto() {
        return telefonoCelularConducto;
    }

    public void setTelefonoCelularConducto(String telefonoCelularConducto) {
        this.telefonoCelularConducto = telefonoCelularConducto;
    }

    public String getNumeroPlaca() {
        return numeroPlaca;
    }

    public void setNumeroPlaca(String numeroPlaca) {
        this.numeroPlaca = numeroPlaca;
    }

    public String getMarcaVehiculo() {
        return marcaVehiculo;
    }

    public void setMarcaVehiculo(String marcaVehiculo) {
        this.marcaVehiculo = marcaVehiculo;
    }

    public String getDireccionPartida() {
        return direccionPartida;
    }

    public void setDireccionPartida(String direccionPartida) {
        this.direccionPartida = direccionPartida;
    }

    public int getIdUbigeoPartida() {
        return idUbigeoPartida;
    }

    public void setIdUbigeoPartida(int idUbigeoPartida) {
        this.idUbigeoPartida = idUbigeoPartida;
    }

    public String getUbigeoPartidaDescripcion() {
        return ubigeoPartidaDescripcion;
    }

    public void setUbigeoPartidaDescripcion(String ubigeoPartidaDescripcion) {
        this.ubigeoPartidaDescripcion = ubigeoPartidaDescripcion;
    }

    public String getDireccionLlegada() {
        return direccionLlegada;
    }

    public void setDireccionLlegada(String direccionLlegada) {
        this.direccionLlegada = direccionLlegada;
    }

    public int getIdUbigeoLlegada() {
        return idUbigeoLlegada;
    }

    public void setIdUbigeoLlegada(int idUbigeoLlegada) {
        this.idUbigeoLlegada = idUbigeoLlegada;
    }

    public String getUbigeoLlegadaDescripcion() {
        return ubigeoLlegadaDescripcion;
    }

    public void setUbigeoLlegadaDescripcion(String ubigeoLlegadaDescripcion) {
        this.ubigeoLlegadaDescripcion = ubigeoLlegadaDescripcion;
    }

    public String getComprobanteAsociado() {
        return comprobanteAsociado;
    }

    public void setComprobanteAsociado(String comprobanteAsociado) {
        this.comprobanteAsociado = comprobanteAsociado;
    }

    public int getIdTipoComprobanteFactura() {
        return idTipoComprobanteFactura;
    }

    public void setIdTipoComprobanteFactura(int idTipoComprobanteFactura) {
        this.idTipoComprobanteFactura = idTipoComprobanteFactura;
    }

    public String getSerieFactura() {
        return serieFactura;
    }

    public void setSerieFactura(String serieFactura) {
        this.serieFactura = serieFactura;
    }

    public String getNumeracionFactura() {
        return numeracionFactura;
    }

    public void setNumeracionFactura(String numeracionFactura) {
        this.numeracionFactura = numeracionFactura;
    }

    public ObservableList<GuiaRemisionDetalleTB> getListGuiaRemisionDetalle() {
        return listGuiaRemisionDetalle;
    }

    public void setListGuiaRemisionDetalle(ObservableList<GuiaRemisionDetalleTB> listGuiaRemisionDetalle) {
        this.listGuiaRemisionDetalle = listGuiaRemisionDetalle;
    }

    public ClienteTB getClienteTB() {
        return clienteTB;
    }

    public void setClienteTB(ClienteTB clienteTB) {
        this.clienteTB = clienteTB;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }   

}
