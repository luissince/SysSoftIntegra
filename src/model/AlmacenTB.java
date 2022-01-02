package model;

public class AlmacenTB {

    private int id;

    private int idAlmacen;

    private String nombre;

    private int idUbigeo;

    private UbigeoTB ubigeoTB;

    private String direccion;

    private String fecha;

    private String hora;

    private String idUsuario;

    public AlmacenTB() {
    }

    public AlmacenTB(int idAlmacen, String nombre) {
        this.idAlmacen = idAlmacen;
        this.nombre = nombre;
    }
 
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdUbigeo() {
        return idUbigeo;
    }

    public void setIdUbigeo(int idUbigeo) {
        this.idUbigeo = idUbigeo;
    }

    public UbigeoTB getUbigeoTB() {
        return ubigeoTB;
    }

    public void setUbigeoTB(UbigeoTB ubigeoTB) {
        this.ubigeoTB = ubigeoTB;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    @Override
    public String toString() {
        return nombre;
    }

}
