package model;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class VentaTB {

    private int id;

    private String idVenta;

    private String idCliente;

    private String vendedor;

    private int idComprobante;

    private int idMoneda;

    private String serie;

    private String numeracion;

    private String fechaVenta;

    private String horaVenta;

    private String fechaVencimiento;

    private String horaVencimiento;

    private double total;

    private int tipo;

    private String tipoName;

    private int estado;

    private String estadoName;

    private Label estadoLabel;

    private String formaName;

    private String observaciones;

    private double efectivo;

    private double vuelto;

    private double tarjeta;

    private double deposito;

    private int tipoCredito;

    private String codigo;

    private String numeroOperacion;

    private boolean tipopago;

    private ClienteTB clienteTB;

    private MonedaTB monedaTB;

    private EmpleadoTB empleadoTB;

    private NotaCreditoTB notaCreditoTB;

    private ArrayList<SuministroTB> suministroTBs;

    private HBox hbOpciones;

    private Button btnImprimir;

    private Button btnAgregar;

    private Button btnSumar;

    private ArrayList<VentaCreditoTB> ventaCreditoTBs;

    private double montoTotal;

    private double montoCobrado;

    private double montoRestante;

    private int procedencia;

    private EmpresaTB empresaTB;

    private String idCotizacion;

    private TipoDocumentoTB tipoDocumentoTB;

    private ArrayList<IngresoTB> ingresoTBs;

    public VentaTB() {

    }

    public VentaTB(int id, String fechaVenta) {
        this.id = id;
        this.fechaVenta = fechaVenta;
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

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
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

    public String getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(String fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public String getHoraVenta() {
        return horaVenta;
    }

    public void setHoraVenta(String horaVenta) {
        this.horaVenta = horaVenta;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public double getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(double efectivo) {
        this.efectivo = efectivo;
    }

    public double getVuelto() {
        return vuelto;
    }

    public void setVuelto(double vuelto) {
        this.vuelto = vuelto;
    }

    public double getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(double tarjeta) {
        this.tarjeta = tarjeta;
    }

    public double getDeposito() {
        return deposito;
    }

    public void setDeposito(double deposito) {
        this.deposito = deposito;
    }

    public int getTipoCredito() {
        return tipoCredito;
    }

    public void setTipoCredito(int tipoCredito) {
        this.tipoCredito = tipoCredito;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isTipopago() {
        return tipopago;
    }

    public void setTipopago(boolean tipopago) {
        this.tipopago = tipopago;
    }

    public String getNumeroOperacion() {
        return numeroOperacion;
    }

    public void setNumeroOperacion(String numeroOperacion) {
        this.numeroOperacion = numeroOperacion;
    }

    public ClienteTB getClienteTB() {
        return clienteTB;
    }

    public void setClienteTB(ClienteTB clienteTB) {
        this.clienteTB = clienteTB;
    }

    public MonedaTB getMonedaTB() {
        return monedaTB;
    }

    public void setMonedaTB(MonedaTB monedaTB) {
        this.monedaTB = monedaTB;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public NotaCreditoTB getNotaCreditoTB() {
        return notaCreditoTB;
    }

    public void setNotaCreditoTB(NotaCreditoTB notaCreditoTB) {
        this.notaCreditoTB = notaCreditoTB;
    }

    public ArrayList<SuministroTB> getSuministroTBs() {
        return suministroTBs;
    }

    public void setSuministroTBs(ArrayList<SuministroTB> suministroTBs) {
        this.suministroTBs = suministroTBs;
    }

    public HBox getHbOpciones() {
        return hbOpciones;
    }

    public void setHbOpciones(HBox hbOpciones) {
        this.hbOpciones = hbOpciones;
    }

    public Button getBtnImprimir() {
        return btnImprimir;
    }

    public void setBtnImprimir(Button btnImprimir) {
        this.btnImprimir = btnImprimir;
    }

    public Button getBtnAgregar() {
        return btnAgregar;
    }

    public void setBtnAgregar(Button btnAgregar) {
        this.btnAgregar = btnAgregar;
    }

    public Button getBtnSumar() {
        return btnSumar;
    }

    public void setBtnSumar(Button btnSumar) {
        this.btnSumar = btnSumar;
    }

    public ArrayList<VentaCreditoTB> getVentaCreditoTBs() {
        return ventaCreditoTBs;
    }

    public void setVentaCreditoTBs(ArrayList<VentaCreditoTB> ventaCreditoTBs) {
        this.ventaCreditoTBs = ventaCreditoTBs;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public double getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(double montoCobrado) {
        this.montoCobrado = montoCobrado;
    }

    public double getMontoRestante() {
        return montoRestante;
    }

    public void setMontoRestante(double montoRestante) {
        this.montoRestante = montoRestante;
    }

    public int getProcedencia() {
        return procedencia;
    }

    public void setProcedencia(int procedencia) {
        this.procedencia = procedencia;
    }

    public EmpresaTB getEmpresaTB() {
        return empresaTB;
    }

    public void setEmpresaTB(EmpresaTB empresaTB) {
        this.empresaTB = empresaTB;
    }

    public String getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(String idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public TipoDocumentoTB getTipoDocumentoTB() {
        return tipoDocumentoTB;
    }

    public void setTipoDocumentoTB(TipoDocumentoTB tipoDocumentoTB) {
        this.tipoDocumentoTB = tipoDocumentoTB;
    }

    public ArrayList<IngresoTB> getIngresoTBs() {
        return ingresoTBs;
    }

    public void setIngresoTBs(ArrayList<IngresoTB> ingresoTBs) {
        this.ingresoTBs = ingresoTBs;
    }

    @Override
    public String toString() {
        return "VentaTB [id=" + id + ", idVenta=" + idVenta + ", idCliente=" + idCliente + ", vendedor=" + vendedor
                + ", idComprobante=" + idComprobante + ", idMoneda=" + idMoneda + ", serie=" + serie + ", numeracion="
                + numeracion + ", fechaVenta=" + fechaVenta + ", horaVenta=" + horaVenta + ", fechaVencimiento="
                + fechaVencimiento + ", horaVencimiento=" + horaVencimiento + ", total=" + total + ", tipo=" + tipo
                + ", tipoName=" + tipoName + ", estado=" + estado + ", estadoName=" + estadoName + ", estadoLabel="
                + estadoLabel + ", formaName=" + formaName + ", observaciones=" + observaciones + ", efectivo="
                + efectivo + ", vuelto=" + vuelto + ", tarjeta=" + tarjeta + ", deposito=" + deposito + ", tipoCredito="
                + tipoCredito + ", codigo=" + codigo + ", numeroOperacion=" + numeroOperacion + ", tipopago=" + tipopago
                + ", clienteTB=" + clienteTB + ", monedaTB=" + monedaTB + ", empleadoTB=" + empleadoTB
                + ", notaCreditoTB=" + notaCreditoTB + ", suministroTBs=" + suministroTBs + ", hbOpciones=" + hbOpciones
                + ", btnImprimir=" + btnImprimir + ", btnAgregar=" + btnAgregar + ", btnSumar=" + btnSumar
                + ", ventaCreditoTBs=" + ventaCreditoTBs + ", montoTotal=" + montoTotal + ", montoCobrado="
                + montoCobrado + ", montoRestante=" + montoRestante + ", procedencia=" + procedencia + ", empresaTB="
                + empresaTB + ", idCotizacion=" + idCotizacion + ", tipoDocumentoTB=" + tipoDocumentoTB
                + ", ingresoTBs=" + ingresoTBs + "]";
    }

}
