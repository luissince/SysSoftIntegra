package controller.configuracion.etiquetas;

import controller.reporte.FxReportViewController;
import controller.tools.PrinterService;
import controller.tools.Tools;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;

public class FxImpresoraEtiquetaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ComboBox<String> cbImpresoras;
    @FXML
    private TextField txtCopias;

    private FxReportViewController reportViewController;

    private PrinterService printerService;

    private Printable printable;

    private double widthEtiquetaMM;

    private double heightEtiquetaMM;

    private int orientacionEtiqueta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        printerService = new PrinterService();
        printerService.getPrinters().forEach(e -> {
            cbImpresoras.getItems().add(e);
        });
        if (printerService.getPrintNameDefault() != null || !"".equals(printerService.getPrintNameDefault())) {
            for (int i = 0; i < cbImpresoras.getItems().size(); i++) {
                if (cbImpresoras.getItems().get(i).equalsIgnoreCase(printerService.getPrintNameDefault())) {
                    cbImpresoras.getSelectionModel().select(i);
                    break;

                }
            }
        }
    }

    public void loadImpresoraEtiqueta(Printable printable, double widthEtiquetaMM, double heightEtiquetaMM,
            int orientacionEtiqueta) {
        this.printable = printable;
        this.widthEtiquetaMM = widthEtiquetaMM;
        this.heightEtiquetaMM = heightEtiquetaMM;
        this.orientacionEtiqueta = orientacionEtiqueta;
    }

    private void eventImprimir() {
        if (cbImpresoras.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Ventana de impresión", "Seleccione una impresa.");
            cbImpresoras.requestFocus();
            return;
        }

        if (!Tools.isNumericInteger(txtCopias.getText())) {
            Tools.AlertMessageWarning(window, "Ventana de impresión", "Ingrese la cantidad de copias a imprimir.");
            txtCopias.requestFocus();
            txtCopias.setText("1");
            txtCopias.selectAll();
            return;
        }

        if (Integer.parseInt(txtCopias.getText()) <= 0) {
            Tools.AlertMessageWarning(window, "Ventana de impresión", "El número de copias debe ser mayor a 0.");
            txtCopias.requestFocus();
            txtCopias.setText("1");
            txtCopias.selectAll();
            return;
        }

        if (reportViewController != null) {
            printReporte();
        } else {
            printEtiquetas();
        }

    }

    private void printReporte() {
        try {
            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            printRequestAttributeSet.add(new Copies(Integer.parseInt(txtCopias.getText())));

            PrinterName pn = new PrinterName(cbImpresoras.getSelectionModel().getSelectedItem(), null);

            PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
            printServiceAttributeSet.add(pn);

            JRPrintServiceExporter exporter = new JRPrintServiceExporter();

            exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportViewController.getJasperPrint());
            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET,
                    printRequestAttributeSet);
            exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                    printServiceAttributeSet);
            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
            exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
            exporter.exportReport();
            Tools.Dispose(window);
        } catch (NumberFormatException | JRException ex) {
            Tools.AlertMessageError(window, "Ventana de impresión", "Se produjo un error intente nuevamente.");
        }
    }

    private void printEtiquetas() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setCopies(Integer.parseInt(txtCopias.getText()));
        Book book = new Book();
        book.append(printable, getPageFormat(pj));
        pj.setPageable(book);
        for (PrintService printService : PrinterJob.lookupPrintServices()) {
            if (cbImpresoras.getSelectionModel().getSelectedItem().equals(printService.getName())) {
                try {
                    pj.setPrintService(printService);
                    pj.print();
                } catch (PrinterException ex) {
                }
            }
        }
    }

    public PageFormat getPageFormat(PrinterJob pj) {
        PageFormat pf = pj.defaultPage();
        Paper paper = pf.getPaper();
        paper.setSize(converMmToPoint(widthEtiquetaMM), converMmToPoint(heightEtiquetaMM));
        paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
        pf.setOrientation(orientacionEtiqueta);
        pf.setPaper(paper);
        return pf;
    }

    public double converMmToPoint(double mm) {
        return mm * 2.83465; // 1 mm = 2.83465 puntos
    }

    @FXML
    private void onKeyPressedCopias(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventImprimir();
        }
    }

    @FXML
    private void onKeyTypedCopias(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            eventImprimir();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        eventImprimir();
    }

    public void setReportViewController(FxReportViewController reportViewController) {
        this.reportViewController = reportViewController;
    }

}
