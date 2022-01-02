package model;

import java.io.Serializable;

public class DetalleTB implements Serializable {

    private int idDetalle;
    private String idMantenimiento;
    private String idAuxiliar;
    private String nombre;
    private String descripcion;
    private String estado;
    private String usuarioRegistro;

    public DetalleTB() {
    }

    public DetalleTB(String nombre) {
        this.nombre = nombre;
    }

    public DetalleTB(int idDetalle, String nombre) {
        this.idDetalle = idDetalle;
        this.nombre = nombre;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public String getIdAuxiliar() {
        return idAuxiliar;
    }

    public void setIdAuxiliar(String idAuxiliar) {
        this.idAuxiliar = idAuxiliar;
    }

    public String getIdMantenimiento() {
        return idMantenimiento;
    }

    public void setIdMantenimiento(String idMantenimiento) {
        this.idMantenimiento = idMantenimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    @Override
    public String toString() {
        return nombre;
    }

}
