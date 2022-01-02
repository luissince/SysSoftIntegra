package model;

public class RolTB {

    private int idRol;
    private String nombre;
    private boolean sistema;
    
    public RolTB() {
        
    }

    public RolTB(int idRol, String nombre, boolean sistema) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.sistema = sistema;
    }   

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return  nombre ;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }    

}
