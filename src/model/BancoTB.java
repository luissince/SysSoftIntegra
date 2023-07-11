package model;

import java.util.ArrayList;

public class BancoTB {

    private int id;
    private String idBanco;
    private String nombreCuenta;
    private String numeroCuenta;
    private int idMoneda;
    private double saldoInicial;
    private String fecha;
    private String hora;
    private String descripcion;
    private boolean sistema;
    private boolean asignacion;
    private short formaPago;
    private boolean mostrar;
    private MonedaTB monedaTB;
    private ArrayList<BancoHistorialTB> bancoHistorialTBs;

    public BancoTB() {
    }

    public BancoTB(String idBanco, String nombreCuenta) {
        this.idBanco = idBanco;
        this.nombreCuenta = nombreCuenta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(String idBanco) {
        this.idBanco = idBanco;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public double getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(double saldoInicial) {
        this.saldoInicial = saldoInicial;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isSistema() {
        return sistema;
    }

    public void setSistema(boolean sistema) {
        this.sistema = sistema;
    }

    public boolean isAsignacion() {
        return asignacion;
    }

    public void setAsignacion(boolean asignacion) {
        this.asignacion = asignacion;
    }

    @Override
    public String toString() {
        return nombreCuenta;
    }

    public short getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(short formaPago) {
        this.formaPago = formaPago;
    }

    public boolean isMostrar() {
        return mostrar;
    }

    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }

    public MonedaTB getMonedaTB() {
        return monedaTB;
    }

    public void setMonedaTB(MonedaTB monedaTB) {
        this.monedaTB = monedaTB;
    }

    public ArrayList<BancoHistorialTB> getBancoHistorialTBs() {
        return bancoHistorialTBs;
    }

    public void setBancoHistorialTBs(ArrayList<BancoHistorialTB> bancoHistorialTBs) {
        this.bancoHistorialTBs = bancoHistorialTBs;
    }
    
    

}
