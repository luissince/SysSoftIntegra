package model;

public class ImagenTB {

    private int idImage;
    private byte[] imagen;
    private String idRelacionado;
    private String idSubRelacion;

    public ImagenTB() {

    }

    public int getIdImage() {
        return idImage;
    }

    public void setIdImage(int idImage) {
        this.idImage = idImage;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getIdRelacionado() {
        return idRelacionado;
    }

    public void setIdRelacionado(String idRelacionado) {
        this.idRelacionado = idRelacionado;
    }

    public String getIdSubRelacion() {
        return idSubRelacion;
    }

    public void setIdSubRelacion(String idSubRelacion) {
        this.idSubRelacion = idSubRelacion;
    }

}
