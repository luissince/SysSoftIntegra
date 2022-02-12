package controller.configuracion.impresoras;

import controller.menus.FxPrincipalController;
import controller.tools.FontsPersonalize;
import controller.tools.PrinterService;
import controller.tools.Session;
import controller.tools.Tools;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;
import model.TicketADO;
import model.TicketTB;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.type.HorizontalAlignEnum;

public class FxImpresoraController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private ComboBox<String> cbImpresoras;
    @FXML
    private RadioButton rbTicket;
    @FXML
    private RadioButton rbDocumentos;
    @FXML
    private CheckBox cbPaperCut;
    @FXML
    private ComboBox<TicketTB> cbTipo;
    @FXML
    private Label lblEstado;
    @FXML
    private RadioButton rbTicketWithDesing;
    @FXML
    private RadioButton rbTicketNoDesing;

    private PrinterService printerService;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ToggleGroup group = new ToggleGroup();
        rbTicket.setToggleGroup(group);
        rbDocumentos.setToggleGroup(group);

        ToggleGroup group1 = new ToggleGroup();
        rbTicketWithDesing.setToggleGroup(group1);
        rbTicketNoDesing.setToggleGroup(group1);
        printerService = new PrinterService();
        loadComponents();
    }

    private void loadComponents() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<List<Object>> task = new Task<List<Object>>() {
            @Override
            public List<Object> call() throws InterruptedException {
                ArrayList<TicketTB> ticketTBs = TicketADO.ListTipoTicket();
                List<String> printerList = printerService.getPrinters();
                List<Object> objects = new ArrayList<>();
                objects.add(ticketTBs);
                objects.add(printerList);
                return objects;
            }
        };

        task.setOnSucceeded(w -> {
            List<Object> objects = task.getValue();
            if (!objects.isEmpty()) {
                if (objects.get(0) != null && objects.get(1) != null) {
                    cbTipo.getItems().clear();
                    cbImpresoras.getItems().clear();
                    cbTipo.getItems().addAll((ArrayList<TicketTB>) objects.get(0));
                    cbImpresoras.getItems().addAll((ArrayList<String>) objects.get(1));
                }
                lblEstado.setText("Información cargada correctamente.");
                lblLoad.setVisible(false);
            } else {
                lblEstado.setText("Información cargada sin datos.");
                lblLoad.setVisible(false);
            }
        });

        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            lblEstado.setText("Error en cargar la información, intente nuevamente.");
        });

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            lblEstado.setText("Procesando carga...");
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private PrintService findPrintService(String printerName, PrintService[] services) {
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        return null;
    }

    public PageFormat getPageFormat(PrinterJob pj) {
        PageFormat pf = pj.defaultPage();
        Paper paper = pf.getPaper();
        paper.setImageableArea(0, 0, 100, paper.getHeight());
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }

    private void printTicket(String printName) {
        try {
            DocPrintJob job = findPrintService(printName, PrinterJob.lookupPrintServices()).createPrintJob();
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setPrintService(job.getPrintService());
            pj.setJobName(printName);
            Book book = new Book();
            book.append(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                    if (pageIndex == 0) {
                        Graphics2D g2d = (Graphics2D) graphics;
                        g2d.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
                        g2d.setPaint(Color.black);
                        g2d.setFont(new Font(FontsPersonalize.FONT_ROBOTO, Font.PLAIN, 12));
                        g2d.drawString("Prueba de impresión de ticket.", 10, 10);
                        graphics.dispose();
                        return PAGE_EXISTS;
                    } else {
                        return NO_SUCH_PAGE;
                    }
                }
            }, getPageFormat(pj));
            pj.setPageable(book);
            pj.print();
        } catch (PrinterException ex) {
            Tools.AlertMessageWarning(vbWindow, "Impresora", "Error al imprimir");
        }
    }

    private void printDocument4A(String printName) {
        try {

            int width = 595;
            int height = 842;
            int margin = 20;
            int columnwidth = 555;
            int columnspace = 0;

            Map param = new HashMap();
            JasperDesign jasperDesign = getJasperDesign(width, height, margin, columnwidth, columnspace);
            JasperReport report = JasperCompileManager.compileReport(jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, param, new JREmptyDataSource());
            PrintReportToPrinter(jasperPrint, printName);

        } catch (JRException | PrintException | IOException er) {
            Tools.AlertMessageWarning(vbWindow, "Impresora", "Error en generar e imprimir el documento de prueba A4.");
        }
    }

    private JasperDesign getJasperDesign(int width, int height, int margin, int columnWidth, int columnSpace) throws JRException {

        JasperDesign jasperDesign = new JasperDesign();
        jasperDesign.setName("Impresión");
        jasperDesign.setPageWidth(width);
        jasperDesign.setPageHeight(height);

        jasperDesign.setColumnWidth(columnWidth);
        jasperDesign.setColumnSpacing(columnSpace);

        jasperDesign.setLeftMargin(margin);
        jasperDesign.setRightMargin(margin);
        jasperDesign.setTopMargin(margin);
        jasperDesign.setBottomMargin(margin);

        JRDesignBand band = new JRDesignBand();
        band.setHeight(100);
        JRDesignStaticText staticText = new JRDesignStaticText();
        staticText.setX(0);
        staticText.setY(0);
        staticText.setWidth(width);
        staticText.setHeight(50);
        staticText.setHorizontalAlignment(HorizontalAlignEnum.LEFT);
        staticText.setText("Diseño de impresión A4");
        band.addElement(staticText);

        jasperDesign.setTitle(band);
        return jasperDesign;
    }

    private void PrintReportToPrinter(JasperPrint jp, String printName) throws JRException, PrintException, IOException {
        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
        printRequestAttributeSet.add(new Copies(1));

        PrinterName printerName = new PrinterName(printName, null);

        PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
        printServiceAttributeSet.add(printerName);

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
        exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, printServiceAttributeSet);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
        exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
        exporter.exportReport();
    }

    private void onEventProbarImpresion() {
        if (rbTicket.isSelected()) {
            if (cbImpresoras.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Impresora", "Seleccione una impresora para continuar.");
                cbImpresoras.requestFocus();
            } else {
                printTicket(cbImpresoras.getSelectionModel().getSelectedItem());
            }
        } else {
            if (cbImpresoras.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Impresora", "Seleccione una impresora para continuar.");
                cbImpresoras.requestFocus();
            } else {
                printDocument4A(cbImpresoras.getSelectionModel().getSelectedItem());
            }
        }
    }

    private void onEventSaveConfiguration() {
        if (cbImpresoras.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Impresora", "Seleccione una impresora para continuar.");
            cbImpresoras.requestFocus();
        } else if (cbTipo.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Impresora", "Seleccione el destino de la impresión.");
            cbTipo.requestFocus();
        } else {
            String ruta = "./archivos/" + cbTipo.getSelectionModel().getSelectedItem().getNombreTicket() + ".properties";
            File file = new File(ruta);
            Properties prop = new Properties();
            if (file.exists()) {
                try (InputStream input = new FileInputStream(ruta)) {
                    prop.load(input);
                    saveProperties(prop, ruta);
                } catch (IOException ex) {
                    Tools.AlertMessageError(vbWindow, "Impresora", "Error en cargar el archivo: " + ex.getLocalizedMessage());
                }
            } else {
                saveProperties(prop, ruta);
            }
        }
    }

    private void saveProperties(Properties prop, String ruta) {
        try (OutputStream output = new FileOutputStream(ruta)) {
            if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("VENTA")) {
                prop.setProperty("printerNameVenta", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperVenta", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatVenta", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingVenta", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de venta");

                Session.ESTADO_IMPRESORA_VENTA = true;
                Session.NOMBRE_IMPRESORA_VENTA = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_VENTA = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_VENTA = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_VENTA = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("COMPRA")) {
                prop.setProperty("printerNameCompra", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperCompra", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatCompra", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingCompra", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de compra");

                Session.ESTADO_IMPRESORA_COMPRA = true;
                Session.NOMBRE_IMPRESORA_COMPRA = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_COMPRA = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_COMPRA = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_COMPRA = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("PRE VENTA")) {
                prop.setProperty("printerNamePreVenta", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperPreVenta", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatPreVenta", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingVenta", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de pre venta");

                Session.ESTADO_IMPRESORA_PRE_VENTA = true;
                Session.NOMBRE_IMPRESORA_PRE_VENTA = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_PRE_VENTA = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_PRE_VENTA = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_PRE_VENTA = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("COTIZACION")) {
                prop.setProperty("printerNameCotizacion", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperCotizacion", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatCotizacion", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingCotizacion", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de cotización");

                Session.ESTADO_IMPRESORA_COTIZACION = true;
                Session.NOMBRE_IMPRESORA_COTIZACION = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_COTIZACION = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_COTIZACION = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_COTIZACION = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("CORTE DE CAJA")) {
                prop.setProperty("printerNameCorteCaja", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperCorteCaja", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatCorteCaja", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingCorteCaja", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de corte de caja");

                Session.ESTADO_IMPRESORA_CORTE_CAJA = true;
                Session.NOMBRE_IMPRESORA_CORTE_CAJA = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_CORTE_CAJA = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_CORTE_CAJA = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_CORTE_CAJA = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("CUENTAS POR COBRAR")) {
                prop.setProperty("printerNameCuentasPorCobrar", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperCuentasPorCobrar", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatCuentasPorCobrar", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingCuentasPorCobrar", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de cuentas por cobrar");

                Session.ESTADO_IMPRESORA_CUENTA_POR_COBRAR = true;
                Session.NOMBRE_IMPRESORA_CUENTA_POR_COBRAR = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_CUENTA_POR_COBRAR = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_CUENTA_POR_COBRAR = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_CUENTA_POR_COBRAR = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("CUENTAS POR PAGAR")) {
                prop.setProperty("printerNameCuentasPorPagar", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperCuentasPorPagar", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatCuentasPorPagar", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingCuentasPorPagar", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de cuentas por pagar");

                Session.ESTADO_IMPRESORA_CUENTA_POR_PAGAR = true;
                Session.NOMBRE_IMPRESORA_CUENTA_POR_PAGAR = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_CUENTA_POR_PAGAR = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_CUENTA_POR_PAGAR = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_CUENTA_POR_PAGAR = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("HISTORIAL DE SALIDA DEL PRODUCTO")) {
                prop.setProperty("printerNameHistorialSalidaProducto", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperHistorialSalidaProducto", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatHistorialSalidaProducto", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingHistorialSalidaProducto", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de historial de producto");

                Session.ESTADO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = true;
                Session.NOMBRE_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_HISTORIA_SALIDA_PRODUCTOS = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("PEDIDO")) {
                prop.setProperty("printerNamePedido", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperPedido", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatPedido", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingPedido", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de pedido");

                Session.ESTADO_IMPRESORA_PEDIDO = true;
                Session.NOMBRE_IMPRESORA_PEDIDO = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_PEDIDO = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_PEDIDO = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_PEDIDO = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("GUIA DE REMISION")) {
                prop.setProperty("printerNameGuiaRemision", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperGuiaRemision", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatGuiaRemision", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingGuiaRemision", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de pedido");

                Session.ESTADO_IMPRESORA_GUIA_REMISION = true;
                Session.NOMBRE_IMPRESORA_GUIA_REMISION = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_GUIA_REMISION = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_GUIA_REMISION = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_GUIA_REMISION = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("ORDEN DE COMPRA")) {
                prop.setProperty("printerNameOrdenCompra", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperOrdenCompra", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatOrdenCompra", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingOrdenCompra", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de pedido");

                Session.ESTADO_IMPRESORA_ORDEN_COMPRA = true;
                Session.NOMBRE_IMPRESORA_ORDEN_COMPRA = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_ORDEN_COMPRA = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_ORDEN_COMPRA = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_ORDEN_COMPRA = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else if (cbTipo.getSelectionModel().getSelectedItem().getNombreTicket().equals("NOTA DE CREDITO")) {
                prop.setProperty("printerNameNotaCredito", cbImpresoras.getSelectionModel().getSelectedItem());
                prop.setProperty("printerCutPaperNotaCredito", cbPaperCut.isSelected() + "");
                prop.setProperty("printerTypeFormatNotaCredito", rbTicket.isSelected() ? "ticket" : "a4");
                prop.setProperty("printerTypeDesingNotaCredito", rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing");
                prop.store(output, "Ruta de configuración de la impresora de pedido");

                Session.ESTADO_IMPRESORA_NOTA_CREDITO = true;
                Session.NOMBRE_IMPRESORA_NOTA_CREDITO = cbImpresoras.getSelectionModel().getSelectedItem();
                Session.CORTAPAPEL_IMPRESORA_NOTA_CREDITO = cbPaperCut.isSelected();
                Session.FORMATO_IMPRESORA_NOTA_CREDITO = rbTicket.isSelected() ? "ticket" : "a4";
                Session.DESING_IMPRESORA_NOTA_CREDITO = rbTicketWithDesing.isSelected() ? "withdesing" : "nodesing";
                Tools.AlertMessageInformation(vbWindow, "Impresora", "Se guardo la configuración correctamente.");
            } else {
                Tools.AlertMessageWarning(vbWindow, "Impresora", "No se puede guardar la configuración, comuníquese con su proveedor para habilitar el módulo.");
            }
        } catch (IOException io) {
            Tools.AlertMessageError(vbWindow, "Impresora", "Error al crear el archivo: " + io.getLocalizedMessage());

        }
    }

    @FXML
    private void onActionOpcionRadioButton(ActionEvent event) {
        cbPaperCut.setDisable(!rbTicket.isSelected());
    }

    @FXML
    private void onKeyPressedProbarImpresion(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventProbarImpresion();
        }
    }

    @FXML
    private void onActionProbarImpresion(ActionEvent event) {
        onEventProbarImpresion();
    }

    @FXML
    private void onKeyPressedGuardarConfiguracion(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSaveConfiguration();
        }
    }

    @FXML
    private void onActionGuardarConfiguracion(ActionEvent event) {
        onEventSaveConfiguration();
    }

    @FXML
    private void onKeyPressedReload(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                loadComponents();
            }
        }
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            loadComponents();
        }
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
