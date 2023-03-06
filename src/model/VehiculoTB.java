package model;

public class VehiculoTB {

    private int id;

    private String idVehiculo;

    private String marca;

    private String numeroPlaca;

    private int idEntidadEmisora;

    private String numeroAutorizacion;

    private String fechaCreaccion;

    private String horaCreacion;

    private String fechaModificacion;

    private String horaModificacion;

    private String idEmpleado;

    public VehiculoTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(String idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getNumeroPlaca() {
        return numeroPlaca;
    }

    public void setNumeroPlaca(String numeroPlaca) {
        this.numeroPlaca = numeroPlaca;
    }

    public int getIdEntidadEmisora() {
        return idEntidadEmisora;
    }

    public void setIdEntidadEmisora(int idEntidadEmisora) {
        this.idEntidadEmisora = idEntidadEmisora;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public void setNumeroAutorizacion(String numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
    }

    public String getFechaCreaccion() {
        return fechaCreaccion;
    }

    public void setFechaCreaccion(String fechaCreaccion) {
        this.fechaCreaccion = fechaCreaccion;
    }

    public String getHoraCreacion() {
        return horaCreacion;
    }

    public void setHoraCreacion(String horaCreacion) {
        this.horaCreacion = horaCreacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getHoraModificacion() {
        return horaModificacion;
    }

    public void setHoraModificacion(String horaModificacion) {
        this.horaModificacion = horaModificacion;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    @Override
    public String toString() {
        return marca + ", " + numeroPlaca;
    }

}
