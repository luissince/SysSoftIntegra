
package model;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class GuiaRemisionDetalleTB {
    
    private int id;
    private int idGuiaRemision;
    private String idSuministro;
    private String codigo;
    private String descripcion;
    private String unidad;
    private double cantidad;
    private double peso;
    private TextField txtCodigo;
    private TextField txtDescripcion;
    private TextField txtUnidad;
    private TextField txtCantidad;
    private TextField txtPeso;
    private Button btnRemover;

    public GuiaRemisionDetalleTB() {
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdGuiaRemision() {
        return idGuiaRemision;
    }

    public void setIdGuiaRemision(int idGuiaRemision) {
        this.idGuiaRemision = idGuiaRemision;
    }

    public String getIdSuministro() {
        return idSuministro;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
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

    public TextField getTxtCodigo() {
        return txtCodigo;
    }

    public void setTxtCodigo(TextField txtCodigo) {
        this.txtCodigo = txtCodigo;
    }

    public TextField getTxtDescripcion() {
        return txtDescripcion;
    }

    public void setTxtDescripcion(TextField txtDescripcion) {
        this.txtDescripcion = txtDescripcion;
    }

    public TextField getTxtUnidad() {
        return txtUnidad;
    }

    public void setTxtUnidad(TextField txtUnidad) {
        this.txtUnidad = txtUnidad;
    }

    public TextField getTxtCantidad() {
        return txtCantidad;
    }

    public void setTxtCantidad(TextField txtCantidad) {
        this.txtCantidad = txtCantidad;
    }

    public TextField getTxtPeso() {
        return txtPeso;
    }

    public void setTxtPeso(TextField txtPeso) {
        this.txtPeso = txtPeso;
    }

    public Button getBtnRemover() {
        return btnRemover;
    }

    public void setBtnRemover(Button btnRemover) {
        this.btnRemover = btnRemover;
    }

    
    
}
