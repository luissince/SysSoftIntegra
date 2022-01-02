package model;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class CompraCreditoTB {

    private int id;
    private String idCompra;
    private String idCompraCredito;
    private double monto;
    private String cuota;
    private String fechaPago;
    private String horaPago;
    private boolean estado;
    private TextField txtCredito;
    private DatePicker dpFecha;
    private Button btnRemove;
    private Text txtEstado;
    private CheckBox cbSeleccion;
    private Button btnImprimir;
    private ProveedorTB proveedorTB;
    private String observacion;
    private String idEmpleado;
    private CompraTB compraTB;

    public CompraCreditoTB() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    public String getIdCompraCredito() {
        return idCompraCredito;
    }

    public void setIdCompraCredito(String idCompraCredito) {
        this.idCompraCredito = idCompraCredito;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getCuota() {
        return cuota;
    }

    public void setCuota(String cuota) {
        this.cuota = cuota;
    }

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getHoraPago() {
        return horaPago;
    }

    public void setHoraPago(String horaPago) {
        this.horaPago = horaPago;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public TextField getTxtCredito() {
        return txtCredito;
    }

    public void setTxtCredito(TextField txtCredito) {
        this.txtCredito = txtCredito;
    }

    public DatePicker getDpFecha() {
        return dpFecha;
    }

    public void setDpFecha(DatePicker dpFecha) {
        this.dpFecha = dpFecha;
    }

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button btnRemove) {
        this.btnRemove = btnRemove;
    }

    public Text getTxtEstado() {
        return txtEstado;
    }

    public void setTxtEstado(Text txtEstado) {
        this.txtEstado = txtEstado;
    }

    public CheckBox getCbSeleccion() {
        return cbSeleccion;
    }

    public void setCbSeleccion(CheckBox cbSeleccion) {
        this.cbSeleccion = cbSeleccion;
    }

    public Button getBtnImprimir() {
        return btnImprimir;
    }

    public void setBtnImprimir(Button btnImprimir) {
        this.btnImprimir = btnImprimir;
    }

    public ProveedorTB getProveedorTB() {
        return proveedorTB;
    }

    public void setProveedorTB(ProveedorTB proveedorTB) {
        this.proveedorTB = proveedorTB;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public CompraTB getCompraTB() {
        return compraTB;
    }

    public void setCompraTB(CompraTB compraTB) {
        this.compraTB = compraTB;
    }

}
