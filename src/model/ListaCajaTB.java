
package model;


public class ListaCajaTB {
    
    private String idListaCaja;
    private String nombre;
    private boolean estado;

    public ListaCajaTB() {
    }

    public String getIdListaCaja() {
        return idListaCaja;
    }

    public void setIdListaCaja(String idListaCaja) {
        this.idListaCaja = idListaCaja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return  nombre ;
    }

    
    
}
