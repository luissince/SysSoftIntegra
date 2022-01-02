
package model;

import javafx.scene.control.Label;


public class KardexTB {
    
    private int id;
    private int idKardex;
    private String idArticulo;
    private String fecha;
    private String hora;
    private short tipo;
    private int movimiento;
    private String movimientoName;
    private String detalle;
    private double cantidad;    
    private double costo;
    private double total;
    
    private Label lblEntreda;    
    private Label lblSalida;
    private Label lblExistencia;
    
    private Label lblCosto;
    
    private Label lblDebe;
    private Label lblHaber;
    private Label lblSaldo;
    
    public KardexTB(){
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdKardex() {
        return idKardex;
    }

    public void setIdKardex(int idKardex) {
        this.idKardex = idKardex;
    }

    public String getIdArticulo() {
        return idArticulo;
    }

    public void setIdArticulo(String idArticulo) {
        this.idArticulo = idArticulo;
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

    public short getTipo() {
        return tipo;
    }

    public void setTipo(short tipo) {
        this.tipo = tipo;
    }

    public int getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(int movimiento) {
        this.movimiento = movimiento;
    }

    public String getMovimientoName() {
        return movimientoName;
    }

    public void setMovimientoName(String movimientoName) {
        this.movimientoName = movimientoName;
    }
 
    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Label getLblEntreda() {
        return lblEntreda;
    }

    public void setLblEntreda(Label lblEntreda) {
        this.lblEntreda = lblEntreda;
    }

    public Label getLblSalida() {
        return lblSalida;
    }

    public void setLblSalida(Label lblSalida) {
        this.lblSalida = lblSalida;
    }

    public Label getLblExistencia() {
        return lblExistencia;
    }

    public void setLblExistencia(Label lblExistencia) {
        this.lblExistencia = lblExistencia;
    }

    public Label getLblCosto() {
        return lblCosto;
    }

    public void setLblCosto(Label lblCosto) {
        this.lblCosto = lblCosto;
    }

    public Label getLblDebe() {
        return lblDebe;
    }

    public void setLblDebe(Label lblDebe) {
        this.lblDebe = lblDebe;
    }

    public Label getLblHaber() {
        return lblHaber;
    }

    public void setLblHaber(Label lblHaber) {
        this.lblHaber = lblHaber;
    }

    public Label getLblSaldo() {
        return lblSaldo;
    }

    public void setLblSaldo(Label lblSaldo) {
        this.lblSaldo = lblSaldo;
    }
  
}
