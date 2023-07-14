package model;

public class IngresoTB {

    private int id;
    private String idIngreso;
    private String idProcedencia;
    private String idVentaCredito;
    private String idUsuario;
    private String idConcepto;
    private String idBanco;
    private String detalle;
    private String fecha;
    private String hora;
    private double monto;
    private double vuelto;
    private boolean estado;
    private String operacion;
    private EmpleadoTB empleadoTB;
    private String transaccion;
    private String formaIngreso;
    private BancoTB bancoTB;

    public IngresoTB() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdIngreso() {
        return idIngreso;
    }

    public void setIdIngreso(String idIngreso) {
        this.idIngreso = idIngreso;
    }

    public String getIdProcedencia() {
        return idProcedencia;
    }

    public void setIdProcedencia(String idProcedencia) {
        this.idProcedencia = idProcedencia;
    }

    public String getIdVentaCredito() {
        return idVentaCredito;
    }

    public void setIdVentaCredito(String idVentaCredito) {
        this.idVentaCredito = idVentaCredito;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdConcepto() {
        return idConcepto;
    }

    public void setIdConcepto(String idConcepto) {
        this.idConcepto = idConcepto;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
    
    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public String getFormaIngreso() {
        return formaIngreso;
    }

    public void setFormaIngreso(String formaIngreso) {
        this.formaIngreso = formaIngreso;
    }

    public String getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(String idBanco) {
        this.idBanco = idBanco;
    }

    public double getVuelto() {
        return vuelto;
    }

    public void setVuelto(double vuelto) {
        this.vuelto = vuelto;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public BancoTB getBancoTB() {
        return bancoTB;
    }

    public void setBancoTB(BancoTB bancoTB) {
        this.bancoTB = bancoTB;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

}
