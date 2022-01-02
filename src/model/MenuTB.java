
package model;


public class MenuTB {
    private int idMenu;
    private String nombre;
    private boolean estado;
    private SubMenusTB subMenusTB;
    
    public MenuTB() {
    }

    public int getIdMenu() {
        return idMenu;
    }

    public void setIdMenu(int idMenu) {
        this.idMenu = idMenu;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public SubMenusTB getSubMenusTB() {
        return subMenusTB;
    }

    public void setSubMenusTB(SubMenusTB subMenusTB) {
        this.subMenusTB = subMenusTB;
    }
    
    
    
}
