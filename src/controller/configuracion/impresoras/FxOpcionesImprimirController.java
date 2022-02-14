package controller.configuracion.impresoras;

import controller.operaciones.cortecaja.FxCajaController;
import controller.tools.BillPrintable;
import controller.tools.ConvertMonedaCadena;
import controller.tools.Tools;
import controller.tools.modelticket.TicketCaja;
import controller.tools.modelticket.TicketCompra;
import controller.tools.modelticket.TicketCotizacion;
import controller.tools.modelticket.TicketCuentasPorCobrar;
import controller.tools.modelticket.TicketCuentasPorPagar;
import controller.tools.modelticket.TicketGuiaRemision;
import controller.tools.modelticket.TicketNotaCredito;
import controller.tools.modelticket.TicketOrdenCompra;
import controller.tools.modelticket.TicketPreVenta;
import controller.tools.modelticket.TicketTraslado;
import controller.tools.modelticket.TicketVenta;
import controller.tools.modelticket.TicketVentaLlevar;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import model.SuministroTB;

public class FxOpcionesImprimirController implements Initializable {

    @FXML
    private AnchorPane apWindow;

    private FxCajaController cajaController;

    private ConvertMonedaCadena monedaCadena;

    private BillPrintable billPrintable;

    private AnchorPane hbEncabezado;

    private AnchorPane hbDetalleCabecera;

    private AnchorPane hbPie;

    private TicketVenta ticketVenta;

    private TicketPreVenta ticketPreVenta;

    private TicketCotizacion ticketCotizacion;

    private TicketCaja ticketCaja;

    private TicketCuentasPorCobrar ticketCuentasPorCobrar;

    private TicketCuentasPorPagar ticketCuentasPorPagar;

    private TicketGuiaRemision ticketGuiaRemision;

    private TicketVentaLlevar ticketVentaLlevar;

    private TicketTraslado ticketTraslado;

    private TicketCompra ticketCompra;

    private TicketOrdenCompra ticketOrdenCompra;

    private TicketNotaCredito ticketNotaCredito;

    private String idVenta;

    private String idVentaCredito;

    private String idCompra;

    private String idCompraCredito;

    private String idCaja;

    private String idCotizacion;

    private String idPedido;

    private String idGuiaRemision;

    private String idSuministro;

    private String idTraslado;

    private String idOrdenCompra;

    private String idNotaCredito;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadComponents();
    }

    public void loadComponents() {
        billPrintable = new BillPrintable();
        hbEncabezado = new AnchorPane();
        hbDetalleCabecera = new AnchorPane();
        hbPie = new AnchorPane();
        monedaCadena = new ConvertMonedaCadena();
    }

    public void loadTicketVenta(Node node) {
        ticketVenta = new TicketVenta(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    public void loadTicketPreVenta(
            Node node,
            String numeroDocumento,
            String informacionCliente,
            String celularCliente,
            String direccionCliente,
            String monedaSimbolo,
            double importeBruto,
            double descuentoBruto,
            double subImporteNeto,
            double impuestoNeto,
            double importeNeto,
            ObservableList<SuministroTB> tvList) {
        ticketPreVenta = new TicketPreVenta(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
        ticketPreVenta.loadDataTicket(
                numeroDocumento,
                informacionCliente,
                celularCliente,
                direccionCliente,
                monedaSimbolo,
                importeBruto,
                descuentoBruto,
                subImporteNeto,
                impuestoNeto,
                importeNeto,
                tvList);
        ticketPreVenta.imprimir();
    }

    public void loadTicketVentaDetalle(Node node) {
        ticketVenta = new TicketVenta(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    public void loadTicketCotizacion(Node node) {
        ticketCotizacion = new TicketCotizacion(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    public void loadTicketCaja(Node node) {
        ticketCaja = new TicketCaja(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketCuentaPorCobrar(Node node) {
        ticketCuentasPorCobrar = new TicketCuentasPorCobrar(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketCuentaPorPagar(Node node) {
        ticketCuentasPorPagar = new TicketCuentasPorPagar(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketGuiaRemision(Node node) {
        ticketGuiaRemision = new TicketGuiaRemision(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketVentaLlevar(Node node) {
        ticketVentaLlevar = new TicketVentaLlevar(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketTraslado(Node node) {
        ticketTraslado = new TicketTraslado(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie);
    }

    public void loadTicketCompra(Node node) {
        ticketCompra = new TicketCompra(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    public void loadTicketOrdenCompra(Node node) {
        ticketOrdenCompra = new TicketOrdenCompra(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    public void loadTicketNotaCredito(Node node) {
        ticketNotaCredito = new TicketNotaCredito(node, billPrintable, hbEncabezado, hbDetalleCabecera, hbPie, monedaCadena);
    }

    private void onEventAceptar() {
        if (ticketCotizacion != null) {
            Tools.Dispose(apWindow);
        } else if (ticketCaja != null) {
            Tools.Dispose(apWindow);
            Tools.Dispose(cajaController.getFxPrincipalController().getSpWindow());
            cajaController.openWindowLogin();
        } else if (ticketCuentasPorCobrar != null) {
            Tools.Dispose(apWindow);
        } else if (ticketCuentasPorPagar != null) {
            Tools.Dispose(apWindow);
        } else if (ticketGuiaRemision != null) {
            Tools.Dispose(apWindow);
        } else if (ticketVentaLlevar != null) {
            Tools.Dispose(apWindow);
        } else if (ticketTraslado != null) {
            Tools.Dispose(apWindow);
        } else if (ticketCompra != null) {
            Tools.Dispose(apWindow);
        } else if (ticketOrdenCompra != null) {
            Tools.Dispose(apWindow);
        }
    }

    private void onEventTicket() {
        if (ticketCotizacion != null) {
            ticketCotizacion.imprimir(idCotizacion);
            Tools.Dispose(apWindow);
        } else if (ticketCaja != null) {
            ticketCaja.imprimir(idCaja);
            Tools.Dispose(cajaController.getFxPrincipalController().getSpWindow());
            cajaController.openWindowLogin();
        } else if (ticketCuentasPorCobrar != null) {
            Tools.Dispose(apWindow);
            ticketCuentasPorCobrar.imprimir(idVenta, idVentaCredito);
        } else if (ticketCuentasPorPagar != null) {
            Tools.Dispose(apWindow);
            ticketCuentasPorPagar.imprimir(idCompra, idCompraCredito);
        } else if (ticketGuiaRemision != null) {
            Tools.Dispose(apWindow);
            ticketGuiaRemision.imprimir(idGuiaRemision);
        } else if (ticketVentaLlevar != null) {
            Tools.Dispose(apWindow);
            ticketVentaLlevar.imprimir(idVenta, idSuministro);
        } else if (ticketTraslado != null) {
            Tools.Dispose(apWindow);
            ticketTraslado.imprimir(idTraslado);
        } else if (ticketCompra != null) {
            ticketCompra.imprimir(idCompra);
            Tools.Dispose(apWindow);
        } else if (ticketOrdenCompra != null) {
            Tools.Dispose(apWindow);
        }
    }

    private void onEvent4a() {
        if (ticketCotizacion != null) {
            ticketCotizacion.mostrarReporte(idCotizacion);
            Tools.Dispose(apWindow);
        } else if (ticketCaja != null) {
            ticketCaja.mostrarReporte(idCaja);
            Tools.Dispose(cajaController.getFxPrincipalController().getSpWindow());
            cajaController.openWindowLogin();
        } else if (ticketCuentasPorCobrar != null) {
            Tools.Dispose(apWindow);
            ticketCuentasPorCobrar.mostrarReporte(idVenta, idVentaCredito);
        } else if (ticketCuentasPorPagar != null) {
            Tools.Dispose(apWindow);
            ticketCuentasPorPagar.mostrarReporte(idCompra, idCompraCredito);
        } else if (ticketGuiaRemision != null) {
            Tools.Dispose(apWindow);
            ticketGuiaRemision.mostrarReporte(idGuiaRemision);
        } else if (ticketVentaLlevar != null) {
            Tools.Dispose(apWindow);
        } else if (ticketTraslado != null) {
            Tools.Dispose(apWindow);
            ticketTraslado.mostrarReporte(idTraslado);
        } else if (ticketCompra != null) {
            Tools.Dispose(apWindow);
            ticketCompra.mostrarReporte(idCompra);
        } else if (ticketOrdenCompra != null) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) throws IOException {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedImprimirTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventTicket();
        }
    }

    @FXML
    private void onActionImprimirTicket(ActionEvent event) {
        onEventTicket();
    }

    @FXML
    private void onKeyPressedImprimirA4(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEvent4a();
        }
    }

    @FXML
    private void onActionImprimirA4(ActionEvent event) {
        onEvent4a();
    }

    public TicketVenta getTicketVenta() {
        return ticketVenta;
    }

    public TicketCotizacion getTicketCotizacion() {
        return ticketCotizacion;
    }

    public TicketCaja getTicketCaja() {
        return ticketCaja;
    }

    public TicketCuentasPorCobrar getTicketCuentasPorCobrar() {
        return ticketCuentasPorCobrar;
    }

    public TicketCuentasPorPagar getTicketCuentasPorPagar() {
        return ticketCuentasPorPagar;
    }

    public TicketGuiaRemision getTicketGuiaRemision() {
        return ticketGuiaRemision;
    }

    public TicketVentaLlevar getTicketVentaLlevar() {
        return ticketVentaLlevar;
    }

    public TicketTraslado getTicketTraslado() {
        return ticketTraslado;
    }

    public TicketCompra getTiketCompra() {
        return ticketCompra;
    }

    public TicketOrdenCompra getTicketOrdenCompra() {
        return ticketOrdenCompra;
    }

    public TicketNotaCredito getTicketNotaCredito() {
        return ticketNotaCredito;
    }

    public AnchorPane getApWindow() {
        return apWindow;
    }

    public void setIdCotizacion(String idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public void setIdCaja(String idCaja) {
        this.idCaja = idCaja;
    }

    public void setIdVenta(String idVenta) {
        this.idVenta = idVenta;
    }

    public void setIdVentaCredito(String idVentaCredito) {
        this.idVentaCredito = idVentaCredito;
    }

    public void setIdCompra(String idCompra) {
        this.idCompra = idCompra;
    }

    public void setIdCompraCredito(String idCompraCredito) {
        this.idCompraCredito = idCompraCredito;
    }

    public void setIdGuiaRemision(String idGuiaRemision) {
        this.idGuiaRemision = idGuiaRemision;
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public void setIdTraslado(String idTraslado) {
        this.idTraslado = idTraslado;
    }

    public void setIdOrdenCompra(String idOrdenCompra) {
        this.idOrdenCompra = idOrdenCompra;
    }

    public void setIdNotaCredito(String idNotaCredito) {
        this.idNotaCredito = idNotaCredito;
    }

    public void setInitOpcionesImprimirCaja(FxCajaController cajaController) {
        this.cajaController = cajaController;
    }

}
