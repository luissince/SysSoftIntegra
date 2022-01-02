package model;

public class TrasladoHistorialTB {

    private int id;
    private String idTraslado;
    private String idSuministro;
    private int idAlmacenOrigen;
    private int idAlmacenDestino;
    private double cantidad;
    private double peso;
    private String suministro;
    private String medida;
    private AlmacenTB almacenOrigenTB;
    private AlmacenTB almacenDestinoTB;
    private SuministroTB suministroTB;

    public TrasladoHistorialTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdTraslado() {
        return idTraslado;
    }

    public void setIdTraslado(String idTraslado) {
        this.idTraslado = idTraslado;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public int getIdAlmacenOrigen() {
        return idAlmacenOrigen;
    }

    public void setIdAlmacenOrigen(int idAlmacenOrigen) {
        this.idAlmacenOrigen = idAlmacenOrigen;
    }

    public int getIdAlmacenDestino() {
        return idAlmacenDestino;
    }

    public void setIdAlmacenDestino(int idAlmacenDestino) {
        this.idAlmacenDestino = idAlmacenDestino;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getSuministro() {
        return suministro;
    }

    public void setSuministro(String suministro) {
        this.suministro = suministro;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public AlmacenTB getAlmacenOrigenTB() {
        return almacenOrigenTB;
    }

    public void setAlmacenOrigenTB(AlmacenTB almacenOrigenTB) {
        this.almacenOrigenTB = almacenOrigenTB;
    }

    public AlmacenTB getAlmacenDestinoTB() {
        return almacenDestinoTB;
    }

    public void setAlmacenDestinoTB(AlmacenTB almacenDestinoTB) {
        this.almacenDestinoTB = almacenDestinoTB;
    }

    public SuministroTB getSuministroTB() {
        return suministroTB;
    }

    public void setSuministroTB(SuministroTB suministroTB) {
        this.suministroTB = suministroTB;
    }

}
