package model;

import java.util.ArrayList;

public class NotaCreditoTB {

    private int id;
    private String idNotaCredito;
    private String idVendedor;
    private String idCliente;
    private int idComprobante;
    private int idMoneda;
    private String codigoAlterno;
    private String nombreComprobante;
    private String serie;
    private String numeracion;
    private int idMotivo;
    private String nombreMotivo;
    private String fechaRegistro;
    private String horaRegistro;
    private int estado;
    private String idVenta;
    private String xmlSunat;
    private String xmlDescripcion;
    private String codigoHash;
    private double importeNeto;

    private ClienteTB clienteTB;
    private VentaTB ventaTB;
    private MonedaTB monedaTB;
    private ArrayList<NotaCreditoDetalleTB> notaCreditoDetalleTBs;

    public NotaCreditoTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdNotaCredito() {
        return idNotaCredito;
    }

    public void setIdNotaCredito(String idNotaCredito) {
        this.idNotaCredito = idNotaCredito;
    }

    public String getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(String idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getCodigoAlterno() {
        return codigoAlterno;
    }

    public void setCodigoAlterno(String codigoAlterno) {
        this.codigoAlterno = codigoAlterno;
    }

    public String getNombreComprobante() {
        return nombreComprobante;
    }

    public void setNombreComprobante(String nombreComprobante) {
        this.nombreComprobante = nombreComprobante;
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

    public int getIdMotivo() {
        return idMotivo;
    }

    public void setIdMotivo(int idMotivo) {
        this.idMotivo = idMotivo;
    }

    public String getNombreMotivo() {
        return nombreMotivo;
    }

    public void setNombreMotivo(String nombreMotivo) {
        this.nombreMotivo = nombreMotivo;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
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

    public double getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(double importeNeto) {
        this.importeNeto = importeNeto;
    }

    public ClienteTB getClienteTB() {
        return clienteTB;
    }

    public void setClienteTB(ClienteTB clienteTB) {
        this.clienteTB = clienteTB;
    }

    public VentaTB getVentaTB() {
        return ventaTB;
    }

    public void setVentaTB(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    public MonedaTB getMonedaTB() {
        return monedaTB;
    }

    public void setMonedaTB(MonedaTB monedaTB) {
        this.monedaTB = monedaTB;
    }

    public ArrayList<NotaCreditoDetalleTB> getNotaCreditoDetalleTBs() {
        return notaCreditoDetalleTBs;
    }

    public void setNotaCreditoDetalleTBs(ArrayList<NotaCreditoDetalleTB> notaCreditoDetalleTBs) {
        this.notaCreditoDetalleTBs = notaCreditoDetalleTBs;
    }

}
