
package model;


public class HistorialSuministroSalidaTB {
    
    private int id;
    
    private int idHistorialSuministroLlevar;
    
    private String idVenta;
    
    private String idSuministro;
    
    private String fecha;
    
    private String hora;
    
    private double cantidad;
    
    private String observacion;

    public HistorialSuministroSalidaTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdHistorialSuministroLlevar() {
        return idHistorialSuministroLlevar;
    }

    public void setIdHistorialSuministroLlevar(int idHistorialSuministroLlevar) {
        this.idHistorialSuministroLlevar = idHistorialSuministroLlevar;
    }

    public String getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
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

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }        

}
