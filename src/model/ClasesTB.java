
package model;

import javafx.beans.property.SimpleStringProperty;

public class ClasesTB {
    
    private String idClase;
    private SimpleStringProperty nombreClase;
    private String codigoAuxiliar;
    private String descripcion;
    private String estado;

    public ClasesTB(){
    }
    
    public ClasesTB(String idClase, SimpleStringProperty nombreClase, String auxiliar, String descripcion, String estado){
        this.idClase = idClase;
        this.nombreClase = nombreClase;
        this.codigoAuxiliar = auxiliar;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public String getIdClase() {
        return idClase;
    }

    public void setIdClase(String idClase) {
        this.idClase = idClase;
    }

    public SimpleStringProperty getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = new SimpleStringProperty(nombreClase);
    }

    public String getCodigoAuxiliar() {
        return codigoAuxiliar == null ? "": codigoAuxiliar;
    }

    public void setCodigoAuxiliar(String codigoAuxiliar) {
        this.codigoAuxiliar = codigoAuxiliar;
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
    
    @Override
    public String toString() {
        return nombreClase.get();
    }
}
