package controller.tools;

import br.com.adilson.util.PrinterMatrix;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import model.CompraCreditoTB;
import model.GuiaRemisionDetalleTB;
import model.HistorialSuministroSalidaTB;
import model.ImageADO;
import model.ImagenTB;
import model.MovimientoCajaTB;
import model.OrdenCompraDetalleTB;
import model.SuministroTB;
import model.VentaCreditoTB;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BillPrintable implements Printable {

    private int sheetWidth;

    private final double pointWidthSizeView;

    private final double pointWidthSizePaper;

    private AnchorPane apEncabezado;

    private AnchorPane apDetalle;

    private AnchorPane apPie;

    public BillPrintable() {
        sheetWidth = 0;
        pointWidthSizeView = 8.10;
        pointWidthSizePaper = 5.10;
    }

    /**
     *
     * @param box
     * @param tipoVenta
     * @param nombre_impresion_comprobante
     * @param numeracion_serie_comprobante
     * @param nummero_documento_cliente
     * @param informacion_cliente
     * @param celular_cliente
     * @param direccion_cliente
     * @param codigoVenta
     * @param importe_total_letras
     * @param fechaInicioOperacion
     * @param horaInicioOperacion
     * @param fechaTerminoOperaciona
     * @param horaTerminoOperacion
     * @param calculado
     * @param contado
     * @param diferencia
     * @param empleadoNumeroDocumento
     * @param empleadoInformacion
     * @param empleadoCelular
     * @param empleadoDireccion
     * @param montoTotal
     * @param montoPagado
     * @param montoDiferencial
     * @param obsevacion_descripción
     * @param monto_inicial_caja
     * @param monto_efectivo_caja
     * @param monto_tarjeta_caja
     * @param monto_deposito_caja
     * @param monto_ingreso_caja
     * @param monto_egreso_caja
     * @param nombre_impresion_comprobante_guia
     * @param numeracion_serie_comprobante_guia
     * @param direccion_partida_guia
     * @param ubigeo_partida_guia
     * @param direccion_llegada_guia
     * @param ubigeo_llegada_guia
     * @param numero_documento_trasportista_guia
     * @param informacion_trasportista_guia
     * @param marca_vehiculo_guia
     * @param numero_placa_vehiculo_guia
     * @param movito_traslado_guia
     * @return
     */
    public int hbEncebezado(HBox box,
            String tipoVenta,
            String nombre_impresion_comprobante,
            String numeracion_serie_comprobante,
            String nummero_documento_cliente,
            String informacion_cliente,
            String celular_cliente,
            String direccion_cliente,
            String codigoVenta,
            String importe_total_letras,
            String fechaInicioOperacion,
            String horaInicioOperacion,
            String fechaTerminoOperaciona,
            String horaTerminoOperacion,
            String calculado,
            String contado,
            String diferencia,
            String empleadoNumeroDocumento,
            String empleadoInformacion,
            String empleadoCelular,
            String empleadoDireccion,
            String montoTotal,
            String montoPagado,
            String montoDiferencial,
            String obsevacion_descripción,
            String monto_inicial_caja,
            String monto_efectivo_caja,
            String monto_tarjeta_caja,
            String monto_deposito_caja,
            String monto_ingreso_caja,
            String monto_egreso_caja,
            String nombre_impresion_comprobante_guia,
            String numeracion_serie_comprobante_guia,
            String direccion_partida_guia,
            String ubigeo_partida_guia,
            String direccion_llegada_guia,
            String ubigeo_llegada_guia,
            String numero_documento_trasportista_guia,
            String informacion_trasportista_guia,
            String marca_vehiculo_guia,
            String numero_placa_vehiculo_guia,
            String movito_traslado_guia) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("repeempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_REPRESENTANTE));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("telempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_TELEFONO));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("celempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_CELULAR));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("pagwempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_PAGINAWEB));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("emailempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_EMAIL));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direcempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_DOMICILIO));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("rucempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_NUMERO_DOCUMENTO));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("razoempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_RAZON_SOCIAL));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nomcomempresa")) {
                    fieldTicket.setText(Tools.AddText2Guines(Session.COMPANY_NOMBRE_COMERCIAL));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fchactual")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.getDate("dd/MM/yyyy")));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horactual")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.getTime("hh:mm:ss aa")));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("docventa")) {
                    fieldTicket.setText(Tools.AddText2Guines(nombre_impresion_comprobante));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numventa")) {
                    fieldTicket.setText(Tools.AddText2Guines(numeracion_serie_comprobante));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("tipofomaventa")) {
                    fieldTicket.setText(Tools.AddText2Guines(tipoVenta));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codigo")) {
                    fieldTicket.setText(Tools.AddText2Guines(codigoVenta));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numcliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(nummero_documento_cliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("infocliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(informacion_cliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("celcliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(celular_cliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direcliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(direccion_cliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("importetotalletras")) {
                    fieldTicket.setText(Tools.AddText2Guines(importe_total_letras));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("finiciooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(fechaInicioOperacion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("hiniciooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(horaInicioOperacion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fterminooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(fechaTerminoOperaciona));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("hterminooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(horaTerminoOperacion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("contado")) {
                    fieldTicket.setText(Tools.AddText2Guines(calculado));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("calculado")) {
                    fieldTicket.setText(Tools.AddText2Guines(contado));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("diferencia")) {
                    fieldTicket.setText(Tools.AddText2Guines(diferencia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoNumeroDocumento));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("infoempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoInformacion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("celempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoCelular));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direcempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoDireccion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montotal")) {
                    fieldTicket.setText(Tools.AddText2Guines(montoTotal));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montopagacobra")) {
                    fieldTicket.setText(Tools.AddText2Guines(montoPagado));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montorestanten")) {
                    fieldTicket.setText(Tools.AddText2Guines(montoDiferencial));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(obsevacion_descripción));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("aperturacaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_inicial_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ventasefectivocaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_efectivo_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ventastarjetacaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_tarjeta_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ventasdepositocaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_deposito_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ingresosefectivocaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_ingreso_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("egresosefectivocaja")) {
                    fieldTicket.setText(Tools.AddText2Guines(monto_egreso_caja));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("docuasociaguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(nombre_impresion_comprobante_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numeroasocuguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(numeracion_serie_comprobante_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direciparguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(direccion_partida_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ubigeparguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(ubigeo_partida_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direcilleguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(direccion_llegada_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("ubigelleguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(ubigeo_llegada_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("documetrasguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(numero_documento_trasportista_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("infotrasguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(informacion_trasportista_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("marcavehiguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(marca_vehiculo_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numeroplacaguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(numero_placa_vehiculo_guia));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("motivotrasguia")) {
                    fieldTicket.setText(Tools.AddText2Guines(movito_traslado_guia));
                }
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalle(HBox hBox, HBox box, ObservableList<SuministroTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClaveAlterna()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClave()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    String nombreMarcaReplace = arrList.get(m).getNombreMarca().replaceAll("\"", "");
                    fieldTicket.setText(Tools.AddText2Guines(nombreMarcaReplace));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getPrecioVentaGeneral(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines("-" + Tools.roundingValue(arrList.get(m).getDescuento(), 0)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getPrecioVentaGeneral() * arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("unimearticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getUnidadVentaName()));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleCotizacion(HBox hBox, HBox box, ObservableList<SuministroTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClaveAlterna()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClave()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    String nombreMarcaReplace = arrList.get(m).getNombreMarca().replaceAll("\"", "");
                    fieldTicket.setText(Tools.AddText2Guines(nombreMarcaReplace));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getPrecioVentaGeneral(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines("-" + Tools.roundingValue(arrList.get(m).getDescuento(), 0)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getImporteNeto(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("unimearticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getUnidadVentaName()));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetallePedido(HBox hBox, HBox box, ObservableList<SuministroTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClaveAlterna()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getClave()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    String nombreMarcaReplace = arrList.get(m).getNombreMarca().replaceAll("\"", "");
                    fieldTicket.setText(Tools.AddText2Guines(nombreMarcaReplace));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("costarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines("-" + Tools.roundingValue(0, 0)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("unimearticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getUnidadVentaName()));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleCuentaCobrar(HBox hBox, HBox box, ObservableList<VentaCreditoTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 0) + "%"));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getObservacion()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fechadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getFechaPago()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getHoraPago()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getMonto(), 2)));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleCuentaPagar(HBox hBox, HBox box, ObservableList<CompraCreditoTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 0) + "%"));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getObservacion()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fechadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getFechaPago()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getHoraPago()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getMonto(), 2)));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleHistorialSumistroSalida(HBox hBox, HBox box, ArrayList<HistorialSuministroSalidaTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 0) + "%"));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getObservacion()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fechadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getFecha()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getHora()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleGuiaRemision(HBox hBox, HBox box, ObservableList<GuiaRemisionDetalleTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getCodigo()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getDescripcion()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 0) + "%"));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(0, 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("unimearticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getUnidad()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("fechadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horadetalle")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("montooperacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbDetalleOrdenCompra(HBox hBox, HBox box, ArrayList<OrdenCompraDetalleTB> arrList, int m) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("numfilas")) {
                    fieldTicket.setText(Tools.AddText2Guines("" + (m + 1)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codalternoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(""));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codbarrasarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getSuministroTB().getClave()));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("nombretarticulo")) {
                    String nombreMarcaReplace = arrList.get(m).getSuministroTB().getNombreMarca().replaceAll("\"", "");
                    fieldTicket.setText(Tools.AddText2Guines(nombreMarcaReplace));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("cantarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("precarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCosto(), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("descarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getDescuento(), 0)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("impoarticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.roundingValue(arrList.get(m).getCantidad() * (arrList.get(m).getCosto() - arrList.get(m).getDescuento()), 2)));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("unimearticulo")) {
                    fieldTicket.setText(Tools.AddText2Guines(arrList.get(m).getSuministroTB().getUnidadCompraName()));
                }
                hBox.getChildren().add(addElementTextField("iu", fieldTicket.getText(), fieldTicket.isMultilineas(), fieldTicket.getLines(), fieldTicket.getColumnWidth(), fieldTicket.getAlignment(), fieldTicket.isEditable(), fieldTicket.getVariable(), fieldTicket.getFontName(), fieldTicket.getFontSize()));
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public int hbPie(HBox box,
            String moneda,
            String importeBruto,
            String descuentoBruto,
            String subImporteNeto,
            String impuestoNeto,
            String importeNeto,
            String tarjeta,
            String efectivo,
            String vuelto,
            String numCliente,
            String infoCliente,
            String codigoVenta,
            String celular_cliente,
            String importe_total_letras,
            String empleadoNumeroDocumento,
            String empleadoInformacion,
            String empleadoCelular,
            String direccionEmpleado,
            String observacion) {
        int lines = 0;
        for (int j = 0; j < box.getChildren().size(); j++) {
            if (box.getChildren().get(j) instanceof TextFieldTicket) {
                TextFieldTicket fieldTicket = ((TextFieldTicket) box.getChildren().get(j));
                if (fieldTicket.getVariable().equalsIgnoreCase("fchactual")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.getDate("dd/MM/yyyy")));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("horactual")) {
                    fieldTicket.setText(Tools.AddText2Guines(Tools.getTime("hh:mm:ss aa")));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("imptotal")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + importeBruto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("dscttotal")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + descuentoBruto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("subtotal")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + subImporteNeto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("valorimpustos")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + impuestoNeto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("totalpagar")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + importeNeto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("tarjeta")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + tarjeta));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("efectivo")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + efectivo));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("vuelto")) {
                    fieldTicket.setText(Tools.AddText2Guines(moneda + " " + vuelto));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("codigo")) {
                    fieldTicket.setText(Tools.AddText2Guines(codigoVenta));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numcliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(numCliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("infocliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(infoCliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("celcliente")) {
                    fieldTicket.setText(Tools.AddText2Guines(celular_cliente));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("importetotalletras")) {
                    fieldTicket.setText(Tools.AddText2Guines(importe_total_letras));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("numempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoNumeroDocumento));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("infoempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoInformacion));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("celempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(empleadoCelular));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("direcempleado")) {
                    fieldTicket.setText(Tools.AddText2Guines(direccionEmpleado));
                } else if (fieldTicket.getVariable().equalsIgnoreCase("observacion")) {
                    fieldTicket.setText(Tools.AddText2Guines(observacion));
                }
                lines = fieldTicket.getLines();
            }
        }
        return lines;
    }

    public String modelTicket(int rows, int lines, ArrayList<HBox> object, String nombreimpresora, boolean cortar) {
        int column = sheetWidth;
        try {
            PrinterMatrix p = new PrinterMatrix();

            p.setOutSize(rows, column);
            short linesbefore;
            short linescurrent;
            short linesafter = 0;
            int linescount;
            int rowscount = 0;
            for (int i = 0; i < object.size(); i++) {
                HBox hBox = object.get(i);
                rowscount += 1;
                linescount = rowscount + linesafter;
                linesafter = 0;
                linescurrent = 0;
                if (hBox.getChildren().size() > 1) {
                    int columnI;
                    int columnF;
                    int columnA = 0;
                    int countColumns = 0;
                    rowscount = linescount;
                    for (int v = 0; v < hBox.getChildren().size(); v++) {
                        TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(v);
                        columnI = columnA;
                        columnF = columnI + field.getColumnWidth();
                        columnA = columnF;
                        if (null != field.getAlignment()) {
                            switch (field.getAlignment()) {
                                case CENTER_LEFT:
                                    p.printTextWrap(linescount + linesafter, field.getLines(), columnI, columnF, field.getText());
                                    linesbefore = (short) field.getLines();
                                    linescurrent = (short) (linesbefore == 1 ? linesbefore : linescurrent);
                                    countColumns++;
                                    linesafter = hBox.getChildren().size() == countColumns ? linescurrent : 0;
                                    break;
                                case CENTER:
                                    p.printTextWrap(linescount + linesafter, field.getLines(), ((columnI + columnF) - field.getText().length()) / 2, columnF, field.getText());
                                    linesbefore = (short) field.getLines();
                                    linescurrent = (short) (linesbefore == 1 ? linesbefore : linescurrent);
                                    countColumns++;
                                    linesafter = hBox.getChildren().size() == countColumns ? linescurrent : 0;
                                    break;
                                case CENTER_RIGHT:
                                    p.printTextWrap(linescount + linesafter, field.getLines(), columnF - field.getText().length(), columnF, field.getText());
                                    linesbefore = (short) field.getLines();
                                    linescurrent = (short) (linesbefore == 1 ? linesbefore : linescurrent);
                                    countColumns++;
                                    linesafter = hBox.getChildren().size() == countColumns ? linescurrent : 0;
                                    break;
                                default:
                                    p.printTextWrap(linescount + linesafter, field.getLines(), columnI, columnF, field.getText());
                                    linesbefore = (short) field.getLines();
                                    linescurrent = (short) (linesbefore == 1 ? linesbefore : linescurrent);
                                    countColumns++;
                                    linesafter = hBox.getChildren().size() == countColumns ? linescurrent : 0;
                                    break;
                            }
                        }
                    }
                } else {
                    rowscount = linescount;
                    TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(0);
                    if (null != field.getAlignment()) {
                        switch (field.getAlignment()) {
                            case CENTER_LEFT:
                                p.printTextWrap(linescount + linesafter, field.getLines(), 0, field.getColumnWidth(), field.getText());
                                linesafter = (short) field.getLines();
                                break;
                            case CENTER:
                                p.printTextWrap(linescount + linesafter, field.getLines(), (field.getColumnWidth() - field.getText().length()) / 2, field.getColumnWidth(), field.getText());
                                linesafter = (short) field.getLines();
                                break;
                            case CENTER_RIGHT:
                                p.printTextWrap(linescount + linesafter, field.getLines(), field.getColumnWidth() - field.getText().length(), field.getColumnWidth(), field.getText());
                                linesafter = (short) field.getLines();
                                break;
                            default:
                                p.printTextWrap(linescount + linesafter, field.getLines(), 0, field.getColumnWidth(), field.getText());
                                linesafter = (short) field.getLines();
                                break;
                        }
                    }
                }
            }
            int salida = object.size() + lines;
            salida++;
            p.printTextWrap(salida, 0, 0, column, "\n");
            salida++;
            p.printTextWrap(salida, 0, 0, column, "\n");
            salida++;
            p.printTextWrap(salida, 0, 0, column, "\n");
            salida++;
            p.printTextWrap(salida, 0, 0, column, "\n");
            salida++;
            p.printTextWrap(salida, 0, 0, column, "\n");
            File archivo = new File("./archivos/modeloimpresoraticket.txt");
            if (archivo.exists()) {
                archivo.delete();
                p.toFile("./archivos/modeloimpresoraticket.txt");
                printDoc("./archivos/modeloimpresoraticket.txt", nombreimpresora, cortar);
                return "completed";
            } else {
                p.toFile("./archivos/modeloimpresoraticket.txt");
                printDoc("./archivos/modeloimpresoraticket.txt", nombreimpresora, cortar);
                return "completed";
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    public void generatePDFPrint(AnchorPane apEncabezado, AnchorPane apDetalle, AnchorPane apPie) {
        this.apEncabezado = apEncabezado;
        this.apDetalle = apDetalle;
        this.apPie = apPie;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex == 0) {

            int y = 0;
            //Consola
            //RobotoRegular"
            //"RobotoBold

            BufferedImage image = new BufferedImage((int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D gimage = image.createGraphics();
            Graphics2D g2d = (Graphics2D) graphics;
            double width = pageFormat.getImageableWidth();

            g2d.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
            gimage.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());

            gimage.setColor(Color.white);
            gimage.fillRect(0, 0, (int) pageFormat.getImageableWidth(), (int) pageFormat.getHeight());
            gimage.setPaint(Color.black);
            g2d.setPaint(Color.black);

            y = createRow(apEncabezado, g2d, gimage, width, y);
            y = createRow(apDetalle, g2d, gimage, width, y);
            createRow(apPie, g2d, gimage, width, y);

            graphics.dispose();
            gimage.dispose();
            try {
                ImageIO.write(image, "png", new File("yourImageName.png"));
            } catch (IOException ex) {
                System.out.println("Error en imprimir: " + ex.getLocalizedMessage());
            }
            return PAGE_EXISTS;
        } else {
            return NO_SUCH_PAGE;
        }
    }

    private int createRow(AnchorPane anchorPane, Graphics2D g2d, Graphics2D gimage, double width, int y) {
        if (anchorPane == null) {
            return y;
        }
        for (int i = 0; i < anchorPane.getChildren().size(); i++) {
            HBox box = ((HBox) anchorPane.getChildren().get(i));

            if (box.getChildren().size() > 1) {
                int yMax = 0;
                int yAux = 0;
                int xPos = 0;
                int yPos;
                yPos = y;
                for (int j = 0; j < box.getChildren().size(); j++) {
                    TextFieldTicket field = (TextFieldTicket) box.getChildren().get(j);

                    Font font = new Font(field.getFontName().equalsIgnoreCase("Consola")
                            ? FontsPersonalize.FONT_CONSOLAS
                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
                            ? FontsPersonalize.FONT_ROBOTO
                            : FontsPersonalize.FONT_ROBOTO,
                            field.getFontName().equalsIgnoreCase("Consola")
                            ? Font.PLAIN
                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
                            ? Font.PLAIN
                            : Font.BOLD,
                            (int) (field.getFontSize() - 3.5f));

                    AttributedString attributedString = new AttributedString(field.getText());
                    attributedString.addAttribute(TextAttribute.FONT, font);

                    AttributedCharacterIterator charIterator = attributedString.getIterator();
                    LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(charIterator, g2d.getFontRenderContext());
                    lineBreakMeasurer.setPosition(charIterator.getBeginIndex());
                    float xmove = (float) (field.getColumnWidth() * pointWidthSizePaper);
                    while (lineBreakMeasurer.getPosition() < charIterator.getEndIndex()) {
                        TextLayout layout = lineBreakMeasurer.nextLayout(xmove);
                        yAux += layout.getAscent() + 3;
                        int x = field.getAlignment() == Pos.CENTER_LEFT
                                ? 0
                                : field.getAlignment() == Pos.CENTER
                                ? (int) ((field.getColumnWidth() * pointWidthSizePaper) - layout.getAdvance()) / 2
                                : (int) ((field.getColumnWidth() * pointWidthSizePaper) - layout.getAdvance());

                        layout.draw(g2d, xPos + x, yPos + yAux);
                        layout.draw(gimage, xPos + x, yPos + yAux);
                        yAux += layout.getDescent() + layout.getLeading();
                    }
                    if (yMax < yAux) {
                        yMax = yAux;
                    }
                    yAux = 0;
                    xPos += xmove;
                }
                yPos = yMax;
                y += yPos;
            } else {
                if (box.getChildren().get(0) instanceof TextFieldTicket) {
                    TextFieldTicket field = (TextFieldTicket) box.getChildren().get(0);
                    Font font = new Font(field.getFontName().equalsIgnoreCase("Consola")
                            ? FontsPersonalize.FONT_CONSOLAS
                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
                            ? FontsPersonalize.FONT_ROBOTO
                            : FontsPersonalize.FONT_ROBOTO,
                            field.getFontName().equalsIgnoreCase("Consola")
                            ? Font.PLAIN
                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
                            ? Font.PLAIN
                            : Font.BOLD,
                            (int) (field.getFontSize() - 3.5f));
                    AttributedString attributedString = new AttributedString(field.getText());
                    attributedString.addAttribute(TextAttribute.FONT, font);

                    AttributedCharacterIterator charIterator = attributedString.getIterator();
                    LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(charIterator, g2d.getFontRenderContext());
                    lineBreakMeasurer.setPosition(charIterator.getBeginIndex());
                    while (lineBreakMeasurer.getPosition() < charIterator.getEndIndex()) {
                        TextLayout layout = lineBreakMeasurer.nextLayout((float) width);
                        y += layout.getAscent() + 3;
                        int x = field.getAlignment() == Pos.CENTER_LEFT
                                ? 0
                                : field.getAlignment() == Pos.CENTER
                                ? (int) (width - layout.getAdvance()) / 2
                                : (int) (width - layout.getAdvance());
                        layout.draw(g2d, x, y);
                        layout.draw(gimage, x, y);
                        y += layout.getDescent() + layout.getLeading();
                    }
                } else if (box.getChildren().get(0) instanceof ImageViewTicket) {
                    ImageViewTicket imageView = (ImageViewTicket) box.getChildren().get(0);
                    if (imageView.getType().equalsIgnoreCase("qr")) {
                        try {
                            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(new QRCodeWriter().encode(ObjectGlobal.QR_PERU_DATA, BarcodeFormat.QR_CODE, 300, 300));
                            g2d.drawImage(qrImage, box.getAlignment() == Pos.CENTER_LEFT ? 0
                                    : box.getAlignment() == Pos.CENTER
                                    ? (int) (width - imageView.getFitWidth()) / 2
                                    : box.getAlignment() == Pos.CENTER_RIGHT ? (int) (width - imageView.getFitWidth()) : 0, y, (int) imageView.getFitWidth(), (int) imageView.getFitHeight(), null);

                            gimage.drawImage(qrImage, box.getAlignment() == Pos.CENTER_LEFT ? 0
                                    : box.getAlignment() == Pos.CENTER
                                    ? (int) (width - imageView.getFitWidth()) / 2
                                    : box.getAlignment() == Pos.CENTER_RIGHT ? (int) (width - imageView.getFitWidth()) : 0, y, (int) imageView.getFitWidth(), (int) imageView.getFitHeight(), null);

                            y += imageView.getFitHeight() + 3;
                        } catch (WriterException ex) {
                            System.out.println(ex.getLocalizedMessage());
                        }
                    } else {
                        try {
                            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("/view/image/no-image.png"));
                            if (imageView.getUrl() != null) {
                                ByteArrayInputStream bais = new ByteArrayInputStream(imageView.getUrl());
                                image = ImageIO.read(bais);
                            }
                            g2d.drawImage(image, box.getAlignment() == Pos.CENTER_LEFT ? 0
                                    : box.getAlignment() == Pos.CENTER
                                    ? (int) (width - imageView.getFitWidth()) / 2
                                    : box.getAlignment() == Pos.CENTER_RIGHT ? (int) (width - imageView.getFitWidth()) : 0, y, (int) imageView.getFitWidth(), (int) imageView.getFitHeight(), null);

                            gimage.drawImage(image, box.getAlignment() == Pos.CENTER_LEFT ? 0
                                    : box.getAlignment() == Pos.CENTER
                                    ? (int) (width - imageView.getFitWidth()) / 2
                                    : box.getAlignment() == Pos.CENTER_RIGHT ? (int) (width - imageView.getFitWidth()) : 0, y, (int) imageView.getFitWidth(), (int) imageView.getFitHeight(), null);

                            y += imageView.getFitHeight() + 3;
                        } catch (IOException ex) {
                            System.out.println(ex.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        return y;
    }

//    private JasperDesign getJasperDesign(int width, int height, AnchorPane apEncabezado, AnchorPane apDetalle, AnchorPane pPie) throws JRException {
//        /*
//        width=595
//        height=842
//        top=right=bottom=left=20
//        columnwidth=555
//        columnspace=0
//        columns=1
//        */
//        JasperDesign jasperDesign = new JasperDesign();
//        jasperDesign.setName("Formato de ticket");
//        jasperDesign.setPageWidth(width);
//        jasperDesign.setPageHeight(height);
//        jasperDesign.setColumnWidth(width);
//        jasperDesign.setColumnSpacing(0);
//        jasperDesign.setLeftMargin(0);
//        jasperDesign.setRightMargin(0);
//        jasperDesign.setTopMargin(0);
//        jasperDesign.setBottomMargin(0);
//
//        JRDesignBand band = new JRDesignBand();
//        band.setSplitType(SplitTypeEnum.STRETCH);
//        band.setHeight(height);
//
//        //font size 9f x 15 height
//        int rows = 0;
//        rows += createRow(apEncabezado, jasperDesign, band, width, rows);
//        rows = createRow(apDetalle, jasperDesign, band, width, rows);
//        createRow(pPie, jasperDesign, band, width, rows);
//
////        jasperDesign.setTitle(band);
//        ((JRDesignSection) jasperDesign.getDetailSection()).addBand(band);
//        return jasperDesign;
//    }
//
//    @Deprecated
//    private int createRow(AnchorPane anchorPane, JasperDesign jasperDesign, JRDesignBand band, int width, int rows) {
//        for (int i = 0; i < anchorPane.getChildren().size(); i++) {
//
//            HBox box = ((HBox) anchorPane.getChildren().get(i));
//            StringBuilder result = new StringBuilder();
//
//            if (box.getChildren().size() > 1) {
//
//                int columnI;
//                int columnF;
//                int columnA = 0;
//                float fontSize = 0;
//                for (int j = 0; j < box.getChildren().size(); j++) {
//                    TextFieldTicket field = (TextFieldTicket) box.getChildren().get(j);
//                    fontSize = field.getFontSize();
//                    columnI = columnA;
//                    columnF = columnI + field.getColumnWidth();
//                    columnA = columnF;
//
//                    int totalWidth;
//                    int length;
//
//                    if (null != field.getAlignment()) {
//                        switch (field.getAlignment()) {
//                            case CENTER_LEFT:
//                                totalWidth = columnF - columnI;
//                                length = field.getText().length();
//                                int posl = 0;
//                                for (int ca = 0; ca < totalWidth; ca++) {
//                                    if (ca >= 0 && ca <= (length - 1)) {
//                                        char c = field.getText().charAt(posl);
//                                        result.append(c);
//                                        posl++;
//
//                                    } else {
//                                        result.append(" ");
//                                    }
//                                }
//                                break;
//                            case CENTER:
//                                totalWidth = columnF - columnI;
//                                length = field.getText().length();
//                                int centro = (totalWidth - length) / 2;
//                                int posc = 0;
//                                for (int ca = 0; ca < totalWidth; ca++) {
//                                    if (ca >= centro && ca <= (centro + (length - 1))) {
//                                        char c = field.getText().charAt(posc);
//                                        result.append(c);
//                                        posc++;
//
//                                    } else {
//                                        result.append(" ");
//                                    }
//                                }
//
//                                break;
//                            case CENTER_RIGHT:
//                                totalWidth = columnF - columnI;
//                                length = field.getText().length();
//                                int right = totalWidth - length;
//                                int posr = 0;
//                                for (int ca = 0; ca < totalWidth; ca++) {
//                                    if (ca >= right && ca <= (right + (length - 1))) {
//                                        char c = field.getText().charAt(posr);
//                                        result.append(c);
//                                        posr++;
//
//                                    } else {
//                                        result.append(" ");
//                                    }
//                                }
//                                break;
//                            default:
//                                totalWidth = columnF - columnI;
//                                length = field.getText().length();
//                                int posd = 0;
//                                for (int ca = 0; ca < totalWidth; ca++) {
//                                    if (ca >= 0 && ca <= (length - 1)) {
//                                        char c = field.getText().charAt(posd);
//                                        result.append(c);
//                                        posd++;
//
//                                    } else {
//                                        result.append(" ");
//                                    }
//                                }
//                                break;
//                        }
//                    }
//                }
//
//                JRDesignTextField staticText = new JRDesignTextField();
//                staticText.setStretchWithOverflow(true);
//                staticText.setBlankWhenNull(true);
//
//                staticText.setX(0);
//                staticText.setY(rows);
//                staticText.setWidth(width);
//                staticText.setHeight((int) (fontSize + 2.5f));//15-9 17-11 19-13 21-15 23-17 25-19 27-21
//                staticText.setFontSize(fontSize - 3.5f);
//                staticText.setFontName("Consola");
//
//                staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
//                staticText.setStretchType(StretchTypeEnum.NO_STRETCH);
//                staticText.setPositionType(PositionTypeEnum.FLOAT);
//                staticText.setMode(ModeEnum.OPAQUE);
//
//                //TextField expression
//                JRDesignExpression expression = new JRDesignExpression();
//                expression.setValueClass(String.class);
//                expression.setText("\"" + result.toString() + "\"");
//                staticText.setExpression(expression);
//                band.addElement(staticText);
//                rows += staticText.getHeight();
//
////                JRDesignStaticText staticText = new JRDesignStaticText();
////                staticText.setX(0);
////                staticText.setY(rows);
////                staticText.setWidth(width);
////                staticText.setHeight(15);
////                staticText.setFontSize(9f);
////                //staticText.setFontName(field.getFontName());
////                staticText.setFontName("Consola");
////                staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
////                staticText.setText(result.toString());
////                band.addElement(staticText);
////                rows += 15;
//            } else {
//
//                if (box.getChildren().get(0) instanceof TextFieldTicket) {
//
////                    TextFieldTicket field = (TextFieldTicket) box.getChildren().get(0);
////                    int columnI;
////                    int columnF;
////
////                    columnI = 0;
////                    columnF = columnI + field.getColumnWidth();
////
////                    int totalWidth;
////                    int length;
////
////                    if (null != field.getAlignment()) {
////                        switch (field.getAlignment()) {
////                            case CENTER_LEFT:
////                                totalWidth = columnF - columnI;
////                                length = field.getText().length();
////                                int posl = 0;
////                                for (int ca = 0; ca < totalWidth; ca++) {
////                                    if (ca >= 0 && ca <= (length - 1)) {
////                                        char c = field.getText().charAt(posl);
////                                        result.append(c);
////                                        posl++;
////                                    } else {
////                                        result.append(" ");
////                                    }
////                                }
////                                break;
////                            case CENTER:
////                                totalWidth = columnF - columnI;
////                                length = field.getText().length();
////                                int centro = (totalWidth - length) / 2;
////                                int posc = 0;
////                                for (int ca = 0; ca < totalWidth; ca++) {
////                                    if (ca >= centro && ca <= (centro + (length - 1))) {
////                                        char c = field.getText().charAt(posc);
////                                        result.append(c);
////                                        posc++;
////                                    } else {
////                                        result.append(" ");
////                                    }
////                                }
////
////                                break;
////                            case CENTER_RIGHT:
////                                totalWidth = columnF - columnI;
////                                length = field.getText().length();
////                                int right = totalWidth - length;
////                                int posr = 0;
////                                for (int ca = 0; ca < totalWidth; ca++) {
////                                    if (ca >= right && ca <= (right + (length - 1))) {
////                                        char c = field.getText().charAt(posr);
////                                        result.append(c);
////                                        posr++;
////                                    } else {
////                                        result.append(" ");
////                                    }
////                                }
////                                break;
////                            default:
////                                totalWidth = columnF - columnI;
////                                length = field.getText().length();
////                                int posd = 0;
////                                for (int ca = 0; ca < totalWidth; ca++) {
////                                    if (ca >= 0 && ca <= (length - 1)) {
////                                        char c = field.getText().charAt(posd);
////                                        result.append(c);
////                                        posd++;
////                                    } else {
////                                        result.append(" ");
////                                    }
////                                }
////                                break;
////                        }
////                    }
//                    TextFieldTicket field = (TextFieldTicket) box.getChildren().get(0);
//                    JRDesignTextField staticText = new JRDesignTextField();
//                    staticText.setStretchWithOverflow(true);
//                    staticText.setBlankWhenNull(true);
//
//                    staticText.setX(0);
//                    staticText.setY(rows);
//                    staticText.setWidth(width);
//                    staticText.setHeight((int) (field.getFontSize() + 2.5f));//15-9 17-11 19-13 21-15 23-17 25-19 27-21
//                    staticText.setFontSize(field.getFontSize() - 3.5f);
//                    staticText.setFontName(
//                            field.getFontName().equalsIgnoreCase("Consola")
//                            ? "Consola"
//                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
//                            ? "RobotoRegular"
//                            : "RobotoBold");
//                    staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
//                    staticText.setHorizontalTextAlign(
//                            field.getAlignment() == Pos.CENTER_LEFT
//                            ? HorizontalTextAlignEnum.LEFT
//                            : field.getAlignment() == Pos.CENTER
//                            ? HorizontalTextAlignEnum.CENTER
//                            : HorizontalTextAlignEnum.RIGHT);
//
//                    staticText.setStretchType(StretchTypeEnum.NO_STRETCH);
//                    staticText.setPositionType(PositionTypeEnum.FLOAT);
//                    staticText.setMode(ModeEnum.OPAQUE);
//
//                    JRDesignExpression expression = new JRDesignExpression();
//                    expression.setValueClass(String.class);
//                    expression.setText("\"" + field.getText() + "\"");
//                    staticText.setExpression(expression);
//                    band.addElement(staticText);
//                    rows += staticText.getHeight();
//
////                    JRDesignStaticText staticText = new JRDesignStaticText();
////                    staticText.setX(0);
////                    staticText.setY(rows);
////                    staticText.setWidth(width);
////                    staticText.setHeight((int) (field.getFontSize()+2.5f));//15-9 17-11 19-13 21-15 23-17 25-19 27-21
////                    staticText.setFontSize(field.getFontSize()-3.5f);
////                    staticText.setFontName(
////                            field.getFontName().equalsIgnoreCase("Consola")
////                            ? "Consola"
////                            : field.getFontName().equalsIgnoreCase("Roboto Regular")
////                            ? "RobotoRegular"
////                            : "RobotoBold");
////                    staticText.setHorizontalTextAlign(
////                            field.getAlignment() == Pos.CENTER_LEFT
////                            ? HorizontalTextAlignEnum.LEFT
////                            : field.getAlignment() == Pos.CENTER
////                            ? HorizontalTextAlignEnum.CENTER
////                            : HorizontalTextAlignEnum.RIGHT);
////                    staticText.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
////                    staticText.setText(field.getText());
//////                    staticText.setText(result.toString());
////                    band.addElement(staticText);
////                    rows += staticText.getHeight();
//                } else if (box.getChildren().get(0) instanceof ImageViewTicket) {
//                    ImageViewTicket imageView = (ImageViewTicket) box.getChildren().get(0);
//                    String idImage = "./archivos/no-image.png";
//                    try {
//                        ByteArrayInputStream bais = new ByteArrayInputStream(imageView.getUrl());
//                        BufferedImage bufferedImage = ImageIO.read(bais);
//                        String idGenerated = getIdGenerateImage();
//                        boolean validateImage = true;
//                        while (validateImage) {
//                            File file = new File("./archivos/" + idGenerated + ".png");
//                            if (file.exists()) {
//                                idGenerated = getIdGenerateImage();
//                            } else {
//                                validateImage = false;
//                                idGenerated = "./archivos/" + idGenerated + ".png";
//                                idImage = idGenerated;
//                            }
//                        }
//                        ImageIO.write(bufferedImage, "png", new File(idGenerated));
//                    } catch (IOException ex1) {
//                    }
//                    JRDesignImage image = new JRDesignImage(jasperDesign);
//                    image.setX(
//                            box.getAlignment() == Pos.CENTER_LEFT ? 0
//                            : box.getAlignment() == Pos.CENTER
//                            ? (int) (width - imageView.getFitWidth()) / 2
//                            : box.getAlignment() == Pos.CENTER_RIGHT ? (int) (width - imageView.getFitWidth()) : 0
//                    );
//                    image.setY(rows);
//                    image.setWidth((int) imageView.getFitWidth());
//                    image.setHeight((int) imageView.getFitHeight());
//                    image.setScaleImage(ScaleImageEnum.FILL_FRAME);
//                    JRDesignExpression expr = new JRDesignExpression();
//                    expr.setText("\"" + idImage + "\"");
//                    image.setExpression(expr);
//                    band.addElement(image);
//                    rows += imageView.getFitHeight();
//                    fileImages.add(idImage);
//                }
//            }
//        }
//        return rows;
//    }
    public void printCortarPapel(String printerName) throws PrintException, IOException {
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
        PrintService service = findPrintService(printerName, printService);
        DocPrintJob job = service.createPrintJob();
        byte[] bytes = "\n\n\n".getBytes("CP437");
        byte[] cutP = new byte[]{0x1d, 'V', 1};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(bytes);
        outputStream.write(cutP);
        byte c[] = outputStream.toByteArray();
        Doc doc = new SimpleDoc(c, flavor, null);
        job.print(doc, null);
    }

    private void printDoc(String ruta, String nombreimpresora, boolean cortar) {
        File file = new File(ruta);
        FileInputStream inputStream = null;
        try {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
            }
            if (inputStream == null) {
                return;
            }
            DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
            PrintService service = findPrintService(nombreimpresora, printService);
            DocPrintJob job = service.createPrintJob();

            byte[] bytes = readFileToByteArray(file);
            byte[] cutP = new byte[]{0x1d, 'V', 1};
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(bytes);
            if (cortar) {
                outputStream.write(cutP);
            }
            byte c[] = outputStream.toByteArray();
            Doc doc = new SimpleDoc(c, flavor, null);
            job.print(doc, null);
        } catch (IOException | PrintException e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public PrintService findPrintService(String printerName, PrintService[] services) {
        PrintService printService = null;
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                printService = service;
                break;
            }
        }
        return printService;
    }

    public PageFormat getPageFormat(PrinterJob pj) {
        PageFormat pf = pj.defaultPage();
        Paper paper = pf.getPaper();
        paper.setSize((int) Math.ceil(sheetWidth * pointWidthSizePaper), 20000);
        paper.setImageableArea(0, 0, (int) Math.ceil(sheetWidth * pointWidthSizePaper), 20000);
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }

    private static byte[] readFileToByteArray(File file) {
        byte[] bArray = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bArray);
            fis.close();

        } catch (IOException ioExp) {
        }
        return bArray;
    }

    public void loadEstructuraTicket(int idTicket, String ruta, AnchorPane apEncabezado, AnchorPane apDetalleCabecera, AnchorPane apPie) {
        apEncabezado.getChildren().clear();
        apDetalleCabecera.getChildren().clear();
        apPie.getChildren().clear();
        JSONObject jSONObject = Json.obtenerObjetoJSON(ruta);

        ArrayList<ImagenTB> imagenTBs = ImageADO.ListaImagePorIdRelacionado(idTicket);

        sheetWidth = jSONObject.get("column") != null ? Short.parseShort(jSONObject.get("column").toString()) : (short) 40;

        if (jSONObject.get("cabecera") != null) {
            JSONObject cabeceraObjects = Json.obtenerObjetoJSON(jSONObject.get("cabecera").toString());
            for (int i = 0; i < cabeceraObjects.size(); i++) {
                HBox box = generateElement(apEncabezado, "cb");
                JSONObject objectObtener = Json.obtenerObjetoJSON(cabeceraObjects.get("cb_" + (i + 1)).toString());
                if (objectObtener.get("text") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                    TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                    box.getChildren().add(field);
                } else if (objectObtener.get("list") != null) {
                    JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                    Iterator it = array.iterator();
                    while (it.hasNext()) {
                        JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    }
                } else if (objectObtener.get("image") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                    ImageViewTicket imageView = addElementImageView("", Short.parseShort(object.get("width").toString()), Double.parseDouble(object.get("fitwidth").toString()), Double.parseDouble(object.get("fitheight").toString()), false, object.get("type").toString());
                    imageView.setId(String.valueOf(object.get("value").toString()));
                    box.setPrefWidth(imageView.getColumnWidth() * pointWidthSizeView);
                    box.setPrefHeight(imageView.getFitHeight());
                    box.setAlignment(getAlignment(object.get("align").toString()));
                    box.getChildren().add(imageView);
                }
            }
        }
        if (jSONObject.get("detalle") != null) {
            JSONObject detalleObjects = Json.obtenerObjetoJSON(jSONObject.get("detalle").toString());
            for (int i = 0; i < detalleObjects.size(); i++) {
                HBox box = generateElement(apDetalleCabecera, "dr");
                JSONObject objectObtener = Json.obtenerObjetoJSON(detalleObjects.get("dr_" + (i + 1)).toString());
                if (objectObtener.get("text") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                    TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                    box.getChildren().add(field);
                } else if (objectObtener.get("list") != null) {
                    JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                    Iterator it = array.iterator();
                    while (it.hasNext()) {
                        JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    }
                } else if (objectObtener.get("image") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                    ImageViewTicket imageView = addElementImageView("", Short.parseShort(object.get("width").toString()), Double.parseDouble(object.get("fitwidth").toString()), Double.parseDouble(object.get("fitheight").toString()), false, object.get("type").toString());
                    imageView.setId(String.valueOf(object.get("value").toString()));
                    box.setPrefWidth(imageView.getColumnWidth() * pointWidthSizeView);
                    box.setPrefHeight(imageView.getFitHeight());
                    box.setAlignment(getAlignment(object.get("align").toString()));
                    box.getChildren().add(imageView);
                }
            }
        }

        if (jSONObject.get("pie") != null) {
            JSONObject pieObjects = Json.obtenerObjetoJSON(jSONObject.get("pie").toString());
            for (int i = 0; i < pieObjects.size(); i++) {
                HBox box = generateElement(apPie, "cp");
                JSONObject objectObtener = Json.obtenerObjetoJSON(pieObjects.get("cp_" + (i + 1)).toString());
                if (objectObtener.get("text") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                    TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                    box.getChildren().add(field);
                } else if (objectObtener.get("list") != null) {
                    JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                    Iterator it = array.iterator();
                    while (it.hasNext()) {
                        JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(), Boolean.valueOf(object.get("multiline").toString()), Short.parseShort(object.get("lines").toString()), Short.parseShort(object.get("width").toString()), getAlignment(object.get("align").toString()), Boolean.parseBoolean(object.get("editable").toString()), String.valueOf(object.get("variable").toString()), String.valueOf(object.get("font").toString()), Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    }
                } else if (objectObtener.get("image") != null) {
                    JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                    ImageViewTicket imageView = addElementImageView("", Short.parseShort(object.get("width").toString()), Double.parseDouble(object.get("fitwidth").toString()), Double.parseDouble(object.get("fitheight").toString()), false, object.get("type").toString());
                    imageView.setId(String.valueOf(object.get("value").toString()));
                    box.setPrefWidth(imageView.getColumnWidth() * pointWidthSizeView);
                    box.setPrefHeight(imageView.getFitHeight());
                    box.setAlignment(getAlignment(object.get("align").toString()));
                    box.getChildren().add(imageView);
                }
            }
        }

        for (int i = 0; i < imagenTBs.size(); i++) {
            for (int m = 0; m < apEncabezado.getChildren().size(); m++) {
                HBox hBox = (HBox) apEncabezado.getChildren().get(m);
                if (hBox.getChildren().size() == 1) {
                    if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                        ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                        if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                            imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                            imageViewTicket.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                        }
                    }
                }
            }
        }

        for (int i = 0; i < imagenTBs.size(); i++) {
            for (int m = 0; m < apDetalleCabecera.getChildren().size(); m++) {
                HBox hBox = (HBox) apDetalleCabecera.getChildren().get(m);
                if (hBox.getChildren().size() == 1) {
                    if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                        ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                        if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                            imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                            imageViewTicket.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                        }
                    }
                }
            }
        }

        for (int i = 0; i < imagenTBs.size(); i++) {
            for (int m = 0; m < apPie.getChildren().size(); m++) {
                HBox hBox = (HBox) apPie.getChildren().get(m);
                if (hBox.getChildren().size() == 1) {
                    if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                        ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                        if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                            imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                            imageViewTicket.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                        }
                    }
                }
            }
        }

    }

    private HBox generateElement(AnchorPane contenedor, String id) {
        if (contenedor.getChildren().isEmpty()) {
            return addElement(contenedor, id + "1", true);
        } else {
            HBox hBox = (HBox) contenedor.getChildren().get(contenedor.getChildren().size() - 1);
            String idGenerate = hBox.getId();
            String codigo = idGenerate.substring(2);
            int valor = Integer.parseInt(codigo) + 1;
            String newCodigo = id + valor;
            return addElement(contenedor, newCodigo, true);
        }
    }

    private HBox addElement(AnchorPane contenedor, String id, boolean useLayout) {
        double layoutY = 0;
        if (useLayout) {
            for (int i = 0; i < contenedor.getChildren().size(); i++) {
                layoutY += ((HBox) contenedor.getChildren().get(i)).getPrefHeight();
            }
        }

        HBox hBox = new HBox();
        hBox.setId(id);
        hBox.setLayoutX(0);
        hBox.setLayoutY(layoutY);
        hBox.setPrefWidth(sheetWidth * pointWidthSizeView);
        hBox.setPrefHeight(30);
        if (useLayout) {
            contenedor.getChildren().add(hBox);
        }
        return hBox;
    }

    private TextFieldTicket addElementTextField(String id, String titulo, boolean multilinea, short lines, short widthColumn, Pos align, boolean editable, String variable, String font, float size) {
        TextFieldTicket field = new TextFieldTicket(titulo, id);
        field.setMultilineas(multilinea);
        field.setLines(lines);
        field.setColumnWidth(widthColumn);
        field.setVariable(variable);
        field.setEditable(editable);
        field.getStyleClass().add("text-field-ticket");
        field.setAlignment(align);
        field.setFontName(font);
        field.setFontSize(size);
        return field;
    }

    private ImageViewTicket addElementImageView(String path, short widthColumn, double width, double height, boolean newImage, String type) {
        ImageViewTicket imageView = new ImageViewTicket();
        imageView.setColumnWidth(widthColumn);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setSmooth(true);
        imageView.setType(type);
        if (newImage) {
            imageView.setImage(new javafx.scene.image.Image(path));
            imageView.setUrl(Tools.getImageBytes(imageView.getImage(), Tools.getFileExtension(new File(path))));
        }
        return imageView;
    }

    private Pos getAlignment(String align) {
        switch (align) {
            case "CENTER":
                return Pos.CENTER;
            case "CENTER_LEFT":
                return Pos.CENTER_LEFT;
            case "CENTER_RIGHT":
                return Pos.CENTER_RIGHT;
            default:
                return Pos.CENTER_LEFT;
        }
    }

    public void setSheetWidth(int sheetWidth) {
        this.sheetWidth = sheetWidth;
    }

}
