package model;

import java.sql.Date;
import java.util.Collection;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleLongProperty;

public class PersonaTB {

    private SimpleLongProperty id;
    private SimpleStringProperty idPersona;
    private int tipoDocumento;
    private SimpleStringProperty tipoDocumentoName;
    private SimpleStringProperty numeroDocumento;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String primerNombre;
    private String segundoNombre;
    private int sexo;
    private Date fechaNacimiento;    
    private Collection<DirectorioTB> directorioTBCollection;
    private String datosCompletos;

    public PersonaTB() {
    }

    public PersonaTB(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public PersonaTB(String numeroDocumento, String datosCompletos) {
        this.numeroDocumento = new SimpleStringProperty(numeroDocumento);
        this.datosCompletos = datosCompletos;
    }

    public PersonaTB(String idPersona, String tipoDocumentoName, String numeroDocumento, String apellidoPaterno) {
        this.idPersona = new SimpleStringProperty(idPersona);
        this.tipoDocumentoName = new SimpleStringProperty(tipoDocumentoName);
        this.numeroDocumento = new SimpleStringProperty(numeroDocumento);
        this.apellidoPaterno = apellidoPaterno;
    }

    public SimpleLongProperty getId() {
        return id;
    }

    public void setId(long id) {
        this.id = new SimpleLongProperty(id);
    }

    public SimpleStringProperty getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = new SimpleStringProperty(idPersona);
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public SimpleStringProperty getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = new SimpleStringProperty(numeroDocumento);
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public int getSexo() {
        return sexo;
    }

    public void setSexo(int sexo) {
        this.sexo = sexo;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Collection<DirectorioTB> getDirectorioTBCollection() {
        return directorioTBCollection;
    }

    public void setDirectorioTBCollection(Collection<DirectorioTB> directorioTBCollection) {
        this.directorioTBCollection = directorioTBCollection;
    }

    public SimpleStringProperty getTipoDocumentoName() {
        return tipoDocumentoName;
    }

    public void setTipoDocumentoName(String tipoDocumentoName) {
        this.tipoDocumentoName = new SimpleStringProperty(tipoDocumentoName);
    }

    @Override
    public String toString() {
        return datosCompletos;
    }
}
