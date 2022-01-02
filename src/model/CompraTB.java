package model;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class CompraTB {

    private int id;
    private String idCompra;
    private String idProveedor;
    private int tipoDocumento;
    private String serie;
    private String numeracion;
    private int idMoneda;
    private String monedaNombre;
    private String fechaCompra;
    private String horaCompra;
    private String fechaVencimiento;
    private String horaVencimiento;
    private double subTotal;
    private double descuento;
    private double total;
    private String observaciones;
    private String notas;
    private String destino;
    private int idAlmacen;

    private int tipo;
    private String tipoName;
    private int estado;
    private String estadoName;
    private Label estadoLabel;
    private String formaName;
    private boolean efectivo;
    private boolean tarjeta;
    private boolean deposito;

    private String usuario;

    private ProveedorTB proveedorTB;
    private SuministroTB suministroTB;
    private MonedaTB monedaTB;
    private AlmacenTB almacenTB;
    
    private ArrayList<CompraCreditoTB> compraCreditoTBs;
    private ObservableList<DetalleCompraTB> detalleCompraTBs;
    private HBox hbOpciones;

    private double montoTotal;
    private double montoPagado;
    private double montoRestante;

    public CompraTB() {

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

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie == null ? "" : serie;
    }

    public String getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(String numeracion) {
        this.numeracion = numeracion;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public String getMonedaNombre() {
        return monedaNombre;
    }

    public void setMonedaNombre(String monedaNombre) {
        this.monedaNombre = monedaNombre;
    }

    public String getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(String fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getHoraCompra() {
        return horaCompra;
    }

    public void setHoraCompra(String horaCompra) {
        this.horaCompra = horaCompra;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getHoraVencimiento() {
        return horaVencimiento;
    }

    public void setHoraVencimiento(String horaVencimiento) {
        this.horaVencimiento = horaVencimiento;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones == null ? "" : observaciones;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas == null ? "" : notas;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public ProveedorTB getProveedorTB() {
        return proveedorTB;
    }

    public void setProveedorTB(ProveedorTB proveedorTB) {
        this.proveedorTB = proveedorTB;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }

    public MonedaTB getMonedaTB() {
        return monedaTB;
    }

    public void setMonedaTB(MonedaTB monedaTB) {
        this.monedaTB = monedaTB;
    }

    public AlmacenTB getAlmacenTB() {
        return almacenTB;
    }

    public void setAlmacenTB(AlmacenTB almacenTB) {
        this.almacenTB = almacenTB;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getTipoName() {
        return tipoName;
    }

    public void setTipoName(String tipoName) {
        this.tipoName = tipoName;
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
        this.estadoName = estadoName;
    }

    public Label getEstadoLabel() {
        return estadoLabel;
    }

    public void setEstadoLabel(Label estadoLabel) {
        this.estadoLabel = estadoLabel;
    }

    public String getFormaName() {
        return formaName;
    }

    public void setFormaName(String formaName) {
        this.formaName = formaName;
    }    

    public boolean getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(boolean efectivo) {
        this.efectivo = efectivo;
    }

    public boolean getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(boolean tarjeta) {
        this.tarjeta = tarjeta;
    }

    public boolean getDeposito() {
        return deposito;
    }

    public void setDeposito(boolean deposito) {
        this.deposito = deposito;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public ArrayList<CompraCreditoTB> getCompraCreditoTBs() {
        return compraCreditoTBs;
    }

    public void setCompraCreditoTBs(ArrayList<CompraCreditoTB> compraCreditoTBs) {
        this.compraCreditoTBs = compraCreditoTBs;
    }

    public ObservableList<DetalleCompraTB> getDetalleCompraTBs() {
        return detalleCompraTBs;
    }

    public void setDetalleCompraTBs(ObservableList<DetalleCompraTB> detalleCompraTBs) {
        this.detalleCompraTBs = detalleCompraTBs;
    }

    public HBox getHbOpciones() {
        return hbOpciones;
    }

    public void setHbOpciones(HBox hbOpciones) {
        this.hbOpciones = hbOpciones;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public double getMontoRestante() {
        return montoRestante;
    }

    public void setMontoRestante(double montoRestante) {
        this.montoRestante = montoRestante;
    }

}
