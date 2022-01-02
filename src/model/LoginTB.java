
package model;

import java.io.Serializable;


public class LoginTB implements Serializable {


    private Long idLogin;
 
    private long idPersona;

    private  String usuario;
 
    private String clave;

    public LoginTB() {
    }

    public LoginTB(Long idLogin) {
        this.idLogin = idLogin;
    }

    public LoginTB(Long idLogin, long idPersona, String usuario, String clave) {
        this.idLogin = idLogin;
        this.idPersona = idPersona;
        this.usuario = usuario;
        this.clave = clave;
    }

    public Long getIdLogin() {
        return idLogin;
    }

    public void setIdLogin(Long idLogin) {
        this.idLogin = idLogin;
    }

    public long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(long idPersona) {
        this.idPersona = idPersona;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

}
