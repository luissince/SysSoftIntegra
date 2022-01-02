/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Ruberfc
 */

import java.sql.Timestamp;

public class ComprobanteTB {
  
    
    //private byte[] serie;
    private String serie_b;
    private String serie_f;
    private String serie_t;
    private String numeracion;
    private Timestamp facha;

    public ComprobanteTB() {
    }

    public ComprobanteTB(String serie_b, String serie_f, String serie_t, String numeracion, Timestamp facha) {
        this.serie_b = serie_b;
        this.serie_f = serie_f;
        this.serie_t = serie_t;
        this.numeracion = numeracion;
        this.facha = facha;
    }

    public String getSerie_b() {
        return serie_b;
    }

    public void setSerie_b(String serie_b) {
        this.serie_b = serie_b;
    }

    public String getSerie_f() {
        return serie_f;
    }

    public void setSerie_f(String serie_f) {
        this.serie_f = serie_f;
    }

    public String getSerie_t() {
        return serie_t;
    }

    public void setSerie_t(String serie_t) {
        this.serie_t = serie_t;
    }

    public String getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(String numeracion) {
        this.numeracion = numeracion;
    }

    public Timestamp getFacha() {
        return facha;
    }

    public void setFacha(Timestamp facha) {
        this.facha = facha;
    }

    
    
}
