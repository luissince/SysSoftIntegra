package model;

public class EmpresaTB {

    private int idEmpresa;
    private int giroComerial;
    private String nombre;
    private String telefono;
    private String celular;
    private String paginaWeb;
    private String email;
    private String terminos;
    private String condiciones;
    private String domicilio;
    private int tipoDocumento;
    private String numeroDocumento;
    private String razonSocial;
    private String nombreComercial;
    private byte[] image;
    private int idUbigeo;
    private UbigeoTB ubigeoTB;
    private String usuarioSol;
    private String claveSol;
    private String certificadoRuta;
    private String certificadoClave;

    public EmpresaTB() {
    }

    public EmpresaTB(int idEmpresa, String nombre, String telefono, String celular, String paginaWeb, String email, String domicilio, int tipoDocumento, String numeroDocumento, String razonSocial, String nombreComercial) {
        this.idEmpresa = idEmpresa;
        this.nombre = nombre;
        this.telefono = telefono;
        this.celular = celular;
        this.paginaWeb = paginaWeb;
        this.email = email;
        this.domicilio = domicilio;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getGiroComerial() {
        return giroComerial;
    }

    public void setGiroComerial(int giroComerial) {
        this.giroComerial = giroComerial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getPaginaWeb() {
        return paginaWeb;
    }

    public void setPaginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTerminos() {
        return terminos;
    }

    public void setTerminos(String terminos) {
        this.terminos = terminos;
    }

    public String getCondiciones() {
        return condiciones;
    }

    public void setCondiciones(String condiciones) {
        this.condiciones = condiciones;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial == null ? "" : razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial == null ? "" : nombreComercial;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
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

    public String getUsuarioSol() {
        return usuarioSol;
    }

    public void setUsuarioSol(String usuarioSol) {
        this.usuarioSol = usuarioSol;
    }

    public String getClaveSol() {
        return claveSol;
    }

    public void setClaveSol(String claveSol) {
        this.claveSol = claveSol;
    }

    public String getCertificadoRuta() {
        return certificadoRuta;
    }

    public void setCertificadoRuta(String certificadoRuta) {
        this.certificadoRuta = certificadoRuta;
    }

    public String getCertificadoClave() {
        return certificadoClave;
    }

    public void setCertificadoClave(String certificadoClave) {
        this.certificadoClave = certificadoClave;
    }
    
    

}
