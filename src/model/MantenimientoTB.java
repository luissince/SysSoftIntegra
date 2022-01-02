
package model;

import java.io.Serializable;
import java.util.Date;

public class MantenimientoTB implements Serializable {

    private String idMantenimiento;
    private String nombre;
    private Character estado;
    private String validar;
    private String usuarioRegistro;
    private Date fechaRegistro;

    public MantenimientoTB() {
    }

    public MantenimientoTB(String idMantenimiento, String nombre) {
        this.idMantenimiento = idMantenimiento;
        this.nombre = nombre;
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

    public Character getEstado() {
        return estado;
    }

    public void setEstado(Character estado) {
        this.estado = estado;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getValidar() {
        return validar;
    }

    public void setValidar(String validar) {
        this.validar = validar;
    }

    


    @Override
    public String toString() {
        return nombre ;
    }

}
