package model;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class GuiaRemisionTB {

    private int id;
    private String idGuiaRemision;
    private int idComprobante;
    private String serie;
    private String numeracion;
    private String idCliente;
    private String idVendedor;
    private String fechaRegistro;
    private String horaRegistro;
    private int idMotivoTraslado;
    private int idModalidadTraslado;
    private String fechaTraslado;
    private String horaTraslado;
    private String idConductor;
    private int idPesoCarga;
    private double pesoCarga;
    private int idTipoDocumentoConducto;
    private String numeroDocumentoConductor;
    private String nombresConductor;
    private String telefonoConducto;
    private String numeroPlaca;
    private String numeroLicencia;
    private String direccionPartida;
    private int idUbigeoPartida;
    private String direccionLlegada;
    private int idUbigeoLlegada;
    private String idVenta;
    private int estado;
    private String xmlSunat;
    private String xmlDescripcion;
    private String codigoHash;
    private String xmlGenerado;
    private String numeroTicketSunat;
    private TipoDocumentoTB tipoDocumentoTB;
    private ObservableList<GuiaRemisionDetalleTB> listGuiaRemisionDetalle;
    private ClienteTB clienteTB;
    private EmpleadoTB empleadoTB;
    private UbigeoTB ubigeoPartidaTB;
    private UbigeoTB ubigeoLlegadaTB;
    private DetalleTB detalleMotivoTrasladoTB;
    private DetalleTB detalleModalidadTrasladoTB;
    private VentaTB ventaTB;
    private Label estadoLabel;

    public GuiaRemisionTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getHoraRegistro() {
        return horaRegistro;
    }

    public void setHoraRegistro(String horaRegistro) {
        this.horaRegistro = horaRegistro;
    }

    public int getIdMotivoTraslado() {
        return idMotivoTraslado;
    }

    public void setIdMotivoTraslado(int idMotivoTraslado) {
        this.idMotivoTraslado = idMotivoTraslado;
    }

    public int getIdModalidadTraslado() {
        return idModalidadTraslado;
    }

    public void setIdModalidadTraslado(int idModalidadTraslado) {
        this.idModalidadTraslado = idModalidadTraslado;
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

    public String getIdConductor() {
        return idConductor;
    }

    public void setIdConductor(String idConductor) {
        this.idConductor = idConductor;
    }

    public int getIdPesoCarga() {
        return idPesoCarga;
    }

    public void setIdPesoCarga(int idPesoCarga) {
        this.idPesoCarga = idPesoCarga;
    }

    public double getPesoCarga() {
        return pesoCarga;
    }

    public void setPesoCarga(double pesoCarga) {
        this.pesoCarga = pesoCarga;
    }

    public int getIdTipoDocumentoConducto() {
        return idTipoDocumentoConducto;
    }

    public void setIdTipoDocumentoConducto(int idTipoDocumentoConducto) {
        this.idTipoDocumentoConducto = idTipoDocumentoConducto;
    }

    public String getNumeroDocumentoConductor() {
        return numeroDocumentoConductor;
    }

    public void setNumeroDocumentoConductor(String numeroDocumentoConductor) {
        this.numeroDocumentoConductor = numeroDocumentoConductor;
    }

    public String getNombresConductor() {
        return nombresConductor;
    }

    public void setNombresConductor(String nombresConductor) {
        this.nombresConductor = nombresConductor;
    }

    public String getTelefonoConducto() {
        return telefonoConducto;
    }

    public void setTelefonoConducto(String telefonoConducto) {
        this.telefonoConducto = telefonoConducto;
    }

    public String getNumeroPlaca() {
        return numeroPlaca;
    }

    public void setNumeroPlaca(String numeroPlaca) {
        this.numeroPlaca = numeroPlaca;
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(String numeroLicencia) {
        this.numeroLicencia = numeroLicencia;
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

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getXmlSunat() {
        return xmlSunat;
    }

    public void setXmlSunat(String xmlSunat) {
        this.xmlSunat = xmlSunat;
    }

    public String getXmlDescripcion() {
        return xmlDescripcion;
    }

    public void setXmlDescripcion(String xmlDescripcion) {
        this.xmlDescripcion = xmlDescripcion;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public void setCodigoHash(String codigoHash) {
        this.codigoHash = codigoHash;
    }

    public String getXmlGenerado() {
        return xmlGenerado;
    }

    public void setXmlGenerado(String xmlGenerado) {
        this.xmlGenerado = xmlGenerado;
    }

    public String getNumeroTicketSunat() {
        return numeroTicketSunat;
    }

    public void setNumeroTicketSunat(String numeroTicketSunat) {
        this.numeroTicketSunat = numeroTicketSunat;
    }

    public TipoDocumentoTB getTipoDocumentoTB() {
        return tipoDocumentoTB;
    }

    public void setTipoDocumentoTB(TipoDocumentoTB tipoDocumentoTB) {
        this.tipoDocumentoTB = tipoDocumentoTB;
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

    public UbigeoTB getUbigeoPartidaTB() {
        return ubigeoPartidaTB;
    }

    public void setUbigeoPartidaTB(UbigeoTB ubigeoPartidaTB) {
        this.ubigeoPartidaTB = ubigeoPartidaTB;
    }

    public UbigeoTB getUbigeoLlegadaTB() {
        return ubigeoLlegadaTB;
    }

    public void setUbigeoLlegadaTB(UbigeoTB ubigeoLlegadaTB) {
        this.ubigeoLlegadaTB = ubigeoLlegadaTB;
    }

    public DetalleTB getDetalleMotivoTrasladoTB() {
        return detalleMotivoTrasladoTB;
    }

    public void setDetalleMotivoTrasladoTB(DetalleTB detalleMotivoTrasladoTB) {
        this.detalleMotivoTrasladoTB = detalleMotivoTrasladoTB;
    }

    public DetalleTB getDetalleModalidadTrasladoTB() {
        return detalleModalidadTrasladoTB;
    }

    public void setDetalleModalidadTrasladoTB(DetalleTB detalleModalidadTrasladoTB) {
        this.detalleModalidadTrasladoTB = detalleModalidadTrasladoTB;
    }

    public VentaTB getVentaTB() {
        return ventaTB;
    }

    public void setVentaTB(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    public Label getEstadoLabel() {
        return estadoLabel;
    }

    public void setEstadoLabel(Label estadoLabel) {
        this.estadoLabel = estadoLabel;
    }

}
