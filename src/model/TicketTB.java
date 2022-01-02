package model;

import javafx.scene.layout.AnchorPane;

public class TicketTB {

    private int id;
    private String nombreTicket;
    private Object variable;
    private String idVariable;
    private int tipo;
    private String ruta;
    private boolean predeterminado;
    private AnchorPane apCabecera;
    private AnchorPane apDetalle;
    private AnchorPane apPie;
    
    public TicketTB() {
        
    }

    public TicketTB(String nombreTicket, Object variable) {
        this.nombreTicket = nombreTicket;
        this.variable = variable;
    }

    public TicketTB(String nombreTicket, Object variable, String idVariable) {
        this.nombreTicket = nombreTicket;
        this.variable = variable;
        this.idVariable = idVariable;
    }    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreTicket() {
        return nombreTicket;
    }

    public void setNombreTicket(String nombreTicket) {
        this.nombreTicket = nombreTicket;
    }

    public Object getVariable() {
        return variable;
    }

    public void setVariable(Object variable) {
        this.variable = variable;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getIdVariable() {
        return idVariable;
    }

    public void setIdVariable(String idVariable) {
        this.idVariable = idVariable;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }
    
    public AnchorPane getApCabecera() {
        return apCabecera;
    }

    public void setApCabecera(AnchorPane apCabecera) {
        this.apCabecera = apCabecera;
    }

    public AnchorPane getApDetalle() {
        return apDetalle;
    }

    public void setApDetalle(AnchorPane apDetalle) {
        this.apDetalle = apDetalle;
    }

    public AnchorPane getApPie() {
        return apPie;
    }

    public void setApPie(AnchorPane apPie) {
        this.apPie = apPie;
    }

    @Override
    public String toString() {
        return nombreTicket;
    }

}
