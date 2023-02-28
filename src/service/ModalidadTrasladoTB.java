package service;

public class ModalidadTrasladoTB {

    private int id;

    private String idModalidadTraslado;

    private String codigo;

    private String nombre;

    private boolean estado;

    private String fecha;

    private String hora;

    public ModalidadTrasladoTB() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdModalidadTraslado() {
        return idModalidadTraslado;
    }

    public void setIdModalidadTraslado(String idModalidadTraslado) {
        this.idModalidadTraslado = idModalidadTraslado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
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

}
