package model;

import java.util.ArrayList;
import javafx.scene.control.Button;

public class TrasladoTB {

    private int id;
    private String idTraslado;
    private String fecha;
    private String hora;
    private String fechaTraslado;
    private String horaTraslado;
    private String observacion;
    private int estado;
    private int tipo;
    private int idAlmacen;
    private String puntoPartida;
    private String puntoLlegada;
    private boolean salidaInventario;
    private boolean asociarDocumento;
    private String idEmpleado;
    private int idMotivo;
    private String idVenta;
    private int correlativo;
    private boolean usarNumeracion;
    private int numeracion;
    private VentaTB ventaTB;
    private EmpleadoTB empleadoTB;
    private Button btnDetalle;
    private ArrayList<TrasladoHistorialTB> historialTBs;

    public TrasladoTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdTraslado() {
        return idTraslado;
    }

    public void setIdTraslado(String idTraslado) {
        this.idTraslado = idTraslado;
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getPuntoPartida() {
        return puntoPartida;
    }

    public void setPuntoPartida(String puntoPartida) {
        this.puntoPartida = puntoPartida;
    }

    public String getPuntoLlegada() {
        return puntoLlegada;
    }

    public void setPuntoLlegada(String puntoLlegada) {
        this.puntoLlegada = puntoLlegada;
    }

    public boolean isSalidaInventario() {
        return salidaInventario;
    }

    public void setSalidaInventario(boolean salidaInventario) {
        this.salidaInventario = salidaInventario;
    }

    public boolean isAsociarDocumento() {
        return asociarDocumento;
    }

    public void setAsociarDocumento(boolean asociarDocumento) {
        this.asociarDocumento = asociarDocumento;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public int getIdMotivo() {
        return idMotivo;
    }

    public void setIdMotivo(int idMotivo) {
        this.idMotivo = idMotivo;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public boolean isUsarNumeracion() {
        return usarNumeracion;
    }

    public void setUsarNumeracion(boolean usarNumeracion) {
        this.usarNumeracion = usarNumeracion;
    }

    public int getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(int numeracion) {
        this.numeracion = numeracion;
    }

    public int getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(int correlativo) {
        this.correlativo = correlativo;
    }

    public EmpleadoTB getEmpleadoTB() {
        return empleadoTB;
    }

    public void setEmpleadoTB(EmpleadoTB empleadoTB) {
        this.empleadoTB = empleadoTB;
    }

    public VentaTB getVentaTB() {
        return ventaTB;
    }

    public void setVentaTB(VentaTB ventaTB) {
        this.ventaTB = ventaTB;
    }

    public Button getBtnDetalle() {
        return btnDetalle;
    }

    public void setBtnDetalle(Button btnDetalle) {
        this.btnDetalle = btnDetalle;
    }

    public ArrayList<TrasladoHistorialTB> getHistorialTBs() {
        return historialTBs;
    }

    public void setHistorialTBs(ArrayList<TrasladoHistorialTB> historialTBs) {
        this.historialTBs = historialTBs;
    }

}
