package model;

public class PrivilegioTB {

    private int idPrivilegio;
    private int idSubmenu;
    private String nombre;
    private boolean estado;
    
    public PrivilegioTB() {
    }

    public int getIdPrivilegio() {
        return idPrivilegio;
    }

    public void setIdPrivilegio(int idPrivilegio) {
        this.idPrivilegio = idPrivilegio;
    }

    public int getIdSubmenu() {
        return idSubmenu;
    }

    public void setIdSubmenu(int idSubmenu) {
        this.idSubmenu = idSubmenu;
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

}
