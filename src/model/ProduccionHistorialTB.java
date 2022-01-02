
package model;


public class ProduccionHistorialTB {
    
    private String idProduccio;
    private String idEmpleado;
    private String fecha;
    private String hora;
    private double entreda;
    private double salida;
    private int dias;
    private int horas;
    private int minutos;
    private String descripcion;

    public ProduccionHistorialTB() {
    }

    public String getIdProduccio() {
        return idProduccio;
    }

    public void setIdProduccio(String idProduccio) {
        this.idProduccio = idProduccio;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
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

    public double getEntreda() {
        return entreda;
    }

    public void setEntreda(double entreda) {
        this.entreda = entreda;
    }

    public double getSalida() {
        return salida;
    }

    public void setSalida(double salida) {
        this.salida = salida;
    }

    public int getDias() {
        return dias;
    }

    public void setDias(int dias) {
        this.dias = dias;
    }

    public int getHoras() {
        return horas;
    }

    public void setHoras(int horas) {
        this.horas = horas;
    }

    public int getMinutos() {
        return minutos;
    }

    public void setMinutos(int minutos) {
        this.minutos = minutos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    

}
