
package model;

public class TipoMovimientoTB {
    
    private int idTipoMovimiento;
    private String nombre;
    private boolean predeterminado ;
    private boolean sistema;
    private boolean ajuste;

    public TipoMovimientoTB() {
    }

    public TipoMovimientoTB(int idTipoMovimiento, String nombre) {
        this.idTipoMovimiento = idTipoMovimiento;
        this.nombre = nombre;
    }

    public TipoMovimientoTB(int idTipoMovimiento, String nombre, boolean ajuste) {
        this.idTipoMovimiento = idTipoMovimiento;
        this.nombre = nombre;
        this.ajuste = ajuste;
    }
    
    

    public int getIdTipoMovimiento() {
        return idTipoMovimiento;
    }

    public void setIdTipoMovimiento(int idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }

    public boolean isAjuste() {
        return ajuste;
    }

    public void setAjuste(boolean ajuste) {
        this.ajuste = ajuste;
    }

    @Override
    public String toString() {
        return nombre ;
    }
    
    
    
}
