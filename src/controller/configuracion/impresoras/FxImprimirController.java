package controller.configuracion.impresoras;

import controller.configuracion.tickets.FxTicketController;
import controller.tools.BillPrintable;
import controller.tools.PrinterService;
import controller.tools.Tools;
import java.awt.print.Book;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javax.print.DocPrintJob;
import javax.print.PrintException;

public class FxImprimirController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ComboBox<String> cbImpresoras;
    @FXML
    private CheckBox cbCortarPapel;

    private FxTicketController ticketController;

    private PrinterService printerService;

    private BillPrintable billPrintable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        printerService = new PrinterService();
        printerService.getPrinters().forEach(e -> {
            cbImpresoras.getItems().add(e);
        });
        billPrintable = new BillPrintable();
    }

    private void executeImprimir() {
        if (cbImpresoras.getSelectionModel().getSelectedIndex() >= 0) {
            if (ticketController != null) {
                billPrintable.setSheetWidth(ticketController.getSheetWidth());
                billPrintable.generatePDFPrint(ticketController.getHbEncabezado(), ticketController.getHbDetalleCabecera(), ticketController.getHbPie());
                try {
                    DocPrintJob job = billPrintable.findPrintService(cbImpresoras.getSelectionModel().getSelectedItem(), PrinterJob.lookupPrintServices()).createPrintJob();
                    if (job != null) {
                        PrinterJob pj = PrinterJob.getPrinterJob();
                        pj.setPrintService(job.getPrintService());
                        pj.setJobName(cbImpresoras.getSelectionModel().getSelectedItem());
                        Book book = new Book();
                        book.append(billPrintable, billPrintable.getPageFormat(pj));
                        pj.setPageable(book);
                        pj.print();
                        if (cbCortarPapel.isSelected()) {
                            billPrintable.printCortarPapel(cbImpresoras.getSelectionModel().getSelectedItem());
                        }
                    } else {
                        Tools.AlertMessageWarning(apWindow, "Imprimir", "No puedo el nombre de la impresora, intente nuevamente.");
                    }
                } catch (PrinterException | PrintException | IOException ex) {
                    Tools.AlertMessageError(apWindow, "Imprimir", "Error en imprimit: " + ex.getLocalizedMessage());
                }
            }
        } else {
            Tools.AlertMessageWarning(apWindow, "Imprimir", "Seleccione un impresora para continuar.");
            cbImpresoras.requestFocus();
        }
    }

    @FXML
    private void onKeyPressedImprimir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            executeImprimir();
        }
    }

    @FXML
    private void onActionImprimir(ActionEvent event) {
        executeImprimir();
    }

    public void setInitTicketController(FxTicketController ticketController) {
        this.ticketController = ticketController;
    }

}
