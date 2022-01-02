package model;

import java.io.Serializable;
import javafx.beans.property.SimpleLongProperty;

public class DirectorioTB implements Serializable {

    private SimpleLongProperty id;
    private Long idDirectorio;
    private int atributo;
    private String nombre;
    private String valor;
    private String idPersona;
    private PersonaTB persona;

    public DirectorioTB() {
    }

    public DirectorioTB(long id, PersonaTB persona) {
        this.id = new SimpleLongProperty(id);
        this.persona = persona;
    }

    public DirectorioTB(Long idDirectorio) {
        this.idDirectorio = idDirectorio;
    }

    public DirectorioTB(Long idDirectorio, SimpleLongProperty id, int atributo, String valor) {
        this.idDirectorio = idDirectorio;
        this.id = id;
        this.atributo = atributo;
        this.valor = valor;
    }

    public SimpleLongProperty getId() {
        return id;
    }

    public void setId(long id) {
        this.id = new SimpleLongProperty(id);
    }

    public Long getIdDirectorio() {
        return idDirectorio;
    }

    public void setIdDirectorio(Long idDirectorio) {
        this.idDirectorio = idDirectorio;
    }

    public int getAtributo() {
        return atributo;
    }

    public void setAtributo(int atributo) {
        this.atributo = atributo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public PersonaTB getPersona() {
        return persona;
    }

    public void setPersona(PersonaTB persona) {
        this.persona = persona;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    

}
