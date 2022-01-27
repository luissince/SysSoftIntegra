package controller.reporte;

import controller.configuracion.empleados.FxEmpleadosListaController;
import controller.contactos.clientes.FxClienteListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.CajaADO;
import model.TipoDocumentoADO;
import model.TipoDocumentoTB;
import model.VentaADO;
import model.VentaTB;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxPosReporteController implements Initializable {

    @FXML
    private VBox hbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private ComboBox<TipoDocumentoTB> cbDocumentos;
    @FXML
    private CheckBox cbDocumentosSeleccionar;
    @FXML
    private TextField txtClientes;
    @FXML
    private Button btnClientes;
    @FXML
    private CheckBox cbClientesSeleccionar;
    @FXML
    private TextField txtVendedores;
    @FXML
    private Button btnVendedor;
    @FXML
    private CheckBox cbVendedoresSeleccionar;
    @FXML
    private HBox hbTipoPago;
    @FXML
    private CheckBox cbTipoPagoSeleccionar;
    @FXML
    private RadioButton rbContado;
    @FXML
    private RadioButton rbCredito;
    @FXML
    private DatePicker dpFechaInicialCaja;
    @FXML
    private DatePicker dpFechaFinalCaja;
    @FXML
    private CheckBox cbMetodoPagoSeleccionar;
    @FXML
    private HBox hbFormaPago;
    @FXML
    private RadioButton rbEfectivo;
    @FXML
    private RadioButton rbTarjeta;
    @FXML
    private RadioButton rbMixto;
    @FXML
    private RadioButton rbDeposito;

    private FxPrincipalController fxPrincipalController;

    private String idCliente;

    private String idEmpleado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        Tools.actualDate(Tools.getDate(), dpFechaInicialCaja);
        Tools.actualDate(Tools.getDate(), dpFechaFinalCaja);

        ToggleGroup groupFormaPago = new ToggleGroup();
        rbContado.setToggleGroup(groupFormaPago);
        rbCredito.setToggleGroup(groupFormaPago);

        ToggleGroup groupMetodoPago = new ToggleGroup();
        rbEfectivo.setToggleGroup(groupMetodoPago);
        rbTarjeta.setToggleGroup(groupMetodoPago);
        rbMixto.setToggleGroup(groupMetodoPago);
        rbDeposito.setToggleGroup(groupMetodoPago);

        cbDocumentos.getItems().clear();
        TipoDocumentoADO.GetDocumentoCombBoxVentas().forEach(e -> cbDocumentos.getItems().add(e));
        idCliente = idEmpleado = "";
    }

    private void openWindowClientes() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxClienteListaController controller = fXMLLoader.getController();
            controller.setInitPostReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un cliente", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillCustomersTable("");
        } catch (IOException ex) {
            System.out.println("Venta reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private void openWindowVendedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxEmpleadosListaController controller = fXMLLoader.getController();
            controller.setInitPostReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un vendedor", hbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillEmpleadosTable("");
        } catch (IOException ex) {
            System.out.println("Venta reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private void openViewVisualizar() {
        if (!cbDocumentosSeleccionar.isSelected() && cbDocumentos.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Seleccione un documento para generar el reporte.");
            cbDocumentos.requestFocus();
        } else if (!cbClientesSeleccionar.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else if (!cbVendedoresSeleccionar.isSelected() && idEmpleado.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un empleado para generar el reporte.");
            btnVendedor.requestFocus();
        } else {
            ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<Object> task = new Task<Object>() {
                @Override
                public Object call() throws JRException {
                    return reportGenerateVentas();
                }
            };

            task.setOnSucceeded(w -> {
                Object object = task.getValue();
                try {
                    if (object instanceof JasperPrint) {
                        Tools.showAlertNotification(
                                "/view/image/information_large.png",
                                "Generar Vista",
                                Tools.newLineString("Se completo la creación del modal correctamente."),
                                Duration.seconds(10),
                                Pos.BOTTOM_RIGHT);

                        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                        Parent parent = fXMLLoader.load(url.openStream());
                        //Controlller here
                        FxReportViewController controller = fXMLLoader.getController();
                        controller.setFileName("Lista de Ventas Pos del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Ventas Pos");
                        stage.setResizable(true);
                        stage.show();
                        stage.requestFocus();
                    } else {
                        Tools.showAlertNotification(
                                "/view/image/warning_large.png",
                                "Generar Vista",
                                Tools.newLineString((String) object),
                                Duration.seconds(10),
                                Pos.BOTTOM_RIGHT);
                    }
                } catch (HeadlessException | IOException ex) {
                    Tools.showAlertNotification(
                            "/view/image/warning_large.png",
                            "Generar Vista",
                            Tools.newLineString("Error al generar el reporte : " + ex.getLocalizedMessage()),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);
                }
            });

            task.setOnFailed(w -> {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar Vista",
                        Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            });

            task.setOnScheduled(w -> {
                Tools.showAlertNotification(
                        "/view/image/pdf.png",
                        "Generar Vista",
                        Tools.newLineString("Se está generando el modal de ventas."),
                        Duration.seconds(5),
                        Pos.BOTTOM_RIGHT);
            });
            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }
    }

    private void onEventPdf() {
        if (!cbDocumentosSeleccionar.isSelected() && cbDocumentos.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Seleccione un documento para generar el reporte.");
            cbDocumentos.requestFocus();
        } else if (!cbClientesSeleccionar.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else if (!cbVendedoresSeleccionar.isSelected() && idEmpleado.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un empleado para generar el reporte.");
            btnVendedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Ventas");
            fileChooser.setInitialFileName("Lista de Ventas Pos del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Documento", Arrays.asList("*.pdf", "*.PDF"))
            );
            File file = fileChooser.showSaveDialog(hbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("pdf") || file.getName().endsWith("PDF")) {
                    onEventPdf(file);

                }
            }
        }
    }

    private void onEventExcel() {
        if (!cbDocumentosSeleccionar.isSelected() && cbDocumentos.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Seleccione un documento para generar el reporte.");
            cbDocumentos.requestFocus();
        } else if (!cbClientesSeleccionar.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else if (!cbVendedoresSeleccionar.isSelected() && idEmpleado.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(hbWindow, "Reporte General de Ventas", "Ingrese un empleado para generar el reporte.");
            btnVendedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Ventas");
            fileChooser.setInitialFileName("Lista de Ventas Pos del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(hbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                    onEventExcel(file);
                } else {
                    Tools.AlertMessage(hbWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Exportar", "Elija un formato valido", false);
                }
            }
        }
    }

    private void onEventExcel(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() throws InterruptedException {
                Object object = VentaADO.Reporte_Genetal_Ventas(
                        2,
                        Tools.getDatePicker(dpFechaInicial),
                        Tools.getDatePicker(dpFechaFinal),
                        cbDocumentosSeleccionar.isSelected() ? 0 : cbDocumentos.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                        idCliente,
                        idEmpleado,
                        cbTipoPagoSeleccionar.isSelected() ? 0 : rbContado.isSelected() ? 1 : 2,
                        cbMetodoPagoSeleccionar.isSelected(),
                        rbEfectivo.isSelected() ? 0 : rbTarjeta.isSelected() ? 1 : rbMixto.isSelected() ? 2 : 3);
                if (object instanceof ArrayList) {
                    try {
                        ArrayList<VentaTB> list = (ArrayList<VentaTB>) object;

                        Workbook workbook;
                        if (file.getName().endsWith("xls")) {
                            workbook = new HSSFWorkbook();
                        } else {
                            workbook = new XSSFWorkbook();
                        }

                        Sheet sheet = workbook.createSheet("Ventas");

                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        font.setColor(HSSFColor.BLACK.index);

                        CellStyle cellStyleHeader = workbook.createCellStyle();
                        cellStyleHeader.setFont(font);
                        cellStyleHeader.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        String header[] = {"Id", "Fecha", "Documento", "Cliente", "Comprobante", "Serie", "Numeración", "Tipo de Venta", "Forma de Cobro", "Estado", "Importe"};

                        Row headerRow = sheet.createRow(0);
                        for (int i = 0; i < header.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellStyle(cellStyleHeader);
                            cell.setCellValue(header[i].toUpperCase());
                        }

                        CellStyle cellStyle = workbook.createCellStyle();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getEstadoName().equalsIgnoreCase("ANULADO") || list.get(i).getNotaCreditoTB() != null) {
                                Font fontRed = workbook.createFont();
                                fontRed.setColor(HSSFColor.RED.index);
                                cellStyle = workbook.createCellStyle();
                                cellStyle.setFont(fontRed);
                                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                            } else {
                                Font fontBlack = workbook.createFont();
                                fontBlack.setColor(HSSFColor.BLACK.index);
                                fontBlack.setBold(false);
                                cellStyle.setFont(fontBlack);
                            }

                            Row row = sheet.createRow(i + 1);
                            Cell cell1 = row.createCell(0);
                            cell1.setCellStyle(cellStyle);
                            cell1.setCellValue(String.valueOf(i + 1));
                            cell1.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell1.getColumnIndex());

                            Cell cell2 = row.createCell(1);
                            cell2.setCellStyle(cellStyle);
                            cell2.setCellValue(String.valueOf(list.get(i).getFechaVenta()));
                            cell2.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell2.getColumnIndex());

                            Cell cell3 = row.createCell(2);
                            cell3.setCellStyle(cellStyle);
                            cell3.setCellValue(String.valueOf(list.get(i).getClienteTB().getNumeroDocumento()));
                            cell3.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell3.getColumnIndex());

                            Cell cell4 = row.createCell(3);
                            cell4.setCellStyle(cellStyle);
                            cell4.setCellValue(String.valueOf(list.get(i).getClienteTB().getInformacion()));
                            cell4.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell4.getColumnIndex());

                            Cell cell5 = row.createCell(4);
                            cell5.setCellStyle(cellStyle);
                            cell5.setCellValue(list.get(i).getComprobanteName());
                            cell5.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell5.getColumnIndex());

                            Cell cell6 = row.createCell(5);
                            cell6.setCellStyle(cellStyle);
                            cell6.setCellValue(list.get(i).getSerie());
                            cell6.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell6.getColumnIndex());

                            Cell cell7 = row.createCell(6);
                            cell7.setCellStyle(cellStyle);
                            cell7.setCellValue(list.get(i).getNumeracion());
                            cell7.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell7.getColumnIndex());

                            Cell cell8 = row.createCell(7);
                            cell8.setCellStyle(cellStyle);
                            cell8.setCellValue(list.get(i).getTipoName());
                            cell8.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell8.getColumnIndex());

                            Cell cell9 = row.createCell(8);
                            cell9.setCellStyle(cellStyle);
                            cell9.setCellValue(list.get(i).getFormaName());
                            cell9.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell9.getColumnIndex());

                            Cell cell10 = row.createCell(9);
                            cell10.setCellStyle(cellStyle);
                            cell10.setCellValue(list.get(i).getEstadoName());
                            cell10.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell10.getColumnIndex());

                            Cell cell11 = row.createCell(10);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell11.setCellStyle(cellStyle);
                            cell11.setCellValue(Double.parseDouble(Tools.roundingValue(list.get(i).getTotal(), 2)));
                            cell11.setCellType(Cell.CELL_TYPE_NUMERIC);
                            sheet.autoSizeColumn(cell11.getColumnIndex());
                        }
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            workbook.write(out);
                        }
                        workbook.close();
                        return "successful";
                    } catch (IOException ex) {
                        return "Error al exportar el archivo, intente de nuevo.";
                    }
                } else {
                    return (String) object;
                }
            }
        };

        task.setOnSucceeded(w -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("successful")) {
                Tools.showAlertNotification(
                        "/view/image/information_large.png",
                        "Generar Excel",
                        Tools.newLineString("Se completo la creación del excel correctamente en la ruta " + file.getAbsolutePath()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar Excel",
                        Tools.newLineString(result),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Generar Excel",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/excel.png",
                    "Generar Excel",
                    Tools.newLineString("Se está generando el excel de ventas."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventPdf(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {

                try {
                    Object object = reportGenerateVentas();
                    if (object instanceof JasperPrint) {
                        JasperExportManager.exportReportToPdfFile((JasperPrint) object, file.getAbsolutePath());
                        return "successful";
                    } else {
                        return (String) object;
                    }
                } catch (JRException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        };

        task.setOnSucceeded(w -> {
            String object = task.getValue();
            if (object.equalsIgnoreCase("successful")) {
                Tools.showAlertNotification(
                        "/view/image/information_large.png",
                        "Generar PDF",
                        Tools.newLineString("Se completó la creación del pdf correctamente en la ruta " + file.getAbsolutePath()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar PDF",
                        Tools.newLineString((String) object),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Generar PDF",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/pdf.png",
                    "Generar PDF",
                    Tools.newLineString("Se está generando el pdf de ventas."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private Object reportGenerateVentas() throws JRException {
        Object object = VentaADO.Reporte_Genetal_Ventas(
                2,
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                cbDocumentosSeleccionar.isSelected() ? 0 : cbDocumentos.getSelectionModel().getSelectedItem().getIdTipoDocumento(),
                idCliente,
                idEmpleado,
                cbTipoPagoSeleccionar.isSelected() ? 0 : rbContado.isSelected() ? 1 : 2,
                cbMetodoPagoSeleccionar.isSelected(),
                rbEfectivo.isSelected() ? 0 : rbTarjeta.isSelected() ? 1 : rbMixto.isSelected() ? 2 : 3);

        if (object instanceof ArrayList) {
            ArrayList<VentaTB> list = (ArrayList<VentaTB>) object;
            if (list.isEmpty()) {
                return "No hay datos para mostrar";
            }
            double totalcontado = 0;
            double totalcredito = 0;
            double totalcreditopagado = 0;
            double totalanulado = 0;

            double efectivo = 0;
            double tarjeta = 0;
            double deposito = 0;

            for (VentaTB vt : list) {
                if (vt.getNotaCreditoTB() != null) {
                    totalanulado += vt.getTotal();
                } else {
                    if (vt.getEstado() == 3) {
                        totalanulado += vt.getTotal();
                    } else {
                        if (vt.getTipo() == 1 && vt.getEstado() == 1 || vt.getTipo() == 1 && vt.getEstado() == 4) {
                            totalcontado += vt.getTotal();
                        } else if (vt.getTipo() == 2 && vt.getEstado() == 1) {
                            totalcreditopagado += vt.getTotal();
                        } else {
                            totalcredito += vt.getTotal();
                        }
                    }
                }

                if (vt.getNotaCreditoTB() == null && vt.getEstado() != 3) {
                    if (vt.getTipo() == 2 || vt.getTipo() == 2 && vt.getEstado() == 1) {
//                        efectivo += vt.getTotal();
                    } else if (vt.getEstado() == 1 || vt.getEstado() == 4) {
                        if (vt.getFormaName().equalsIgnoreCase("EFECTIVO")) {
                            efectivo += vt.getTotal();
                        } else if (vt.getFormaName().equalsIgnoreCase("TARJETA")) {
                            tarjeta += vt.getTotal();
                        } else if (vt.getFormaName().equalsIgnoreCase("MIXTO")) {
                            efectivo += vt.getEfectivo();
                            tarjeta += vt.getTarjeta();
                        } else {
                            deposito += vt.getTotal();
                        }
                    }
                }

            }

            Map map = new HashMap();
            map.put("PERIODO", dpFechaInicial.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")) + " - " + dpFechaFinal.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
            map.put("DOCUMENTO", cbDocumentosSeleccionar.isSelected() ? "TODOS" : cbDocumentos.getSelectionModel().getSelectedItem().getNombre());
            map.put("ORDEN", "TODOS");
            map.put("CLIENTE", cbClientesSeleccionar.isSelected() ? "TODOS" : txtClientes.getText().toUpperCase());
            map.put("TIPO", cbTipoPagoSeleccionar.isSelected() ? "TODOS" : rbContado.isSelected() ? "CONTADO" : "CRÉDITO");
            map.put("METODO", cbMetodoPagoSeleccionar.isSelected() ? "TODOS" : rbEfectivo.isSelected() ? "EFECTIVO" : rbTarjeta.isSelected() ? "TARJETA" : rbMixto.isSelected() ? "MIXTO" : "DEPÓSITO");

            map.put("VENDEDOR", cbVendedoresSeleccionar.isSelected() ? "TODOS" : txtVendedores.getText().toUpperCase());
            map.put("TOTAANULADO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalanulado, 2));
            map.put("TOTALCREDITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalcredito, 2));
            map.put("TOTALCREDITOCOBRADO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalcreditopagado, 2));
            map.put("TOTALCONTADO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalcontado, 2));

            map.put("EFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(efectivo, 2));
            map.put("TARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(tarjeta, 2));
            map.put("DEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(deposito, 2));

            JasperPrint jasperPrint = JasperFillManager.fillReport(FxVentaReporteController.class.getResourceAsStream("/report/VentaGeneral.jasper"), map, new JRBeanCollectionDataSource(list));
            return jasperPrint;
        } else {
            return (String) object;
        }
    }

    private void openViewVisualizarCaja() {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() throws JRException {
                return reportGeneralCorteCaja();
            }
        };

        task.setOnSucceeded(w -> {
            try {
                Object object = task.getValue();
                if (object instanceof JasperPrint) {
                    Tools.showAlertNotification(
                            "/view/image/information_large.png",
                            "Generar Vista",
                            Tools.newLineString("Se completo la creación del modal correctamente."),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);

                    URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    //Controlller here
                    FxReportViewController controller = fXMLLoader.getController();
                    controller.setFileName("Reporte de Corte de Caja del " + Tools.getDatePicker(dpFechaInicialCaja) + " al " + Tools.getDatePicker(dpFechaFinalCaja));
                    controller.setJasperPrint((JasperPrint) object);
                    controller.show();
                    Stage stage = WindowStage.StageLoader(parent, "Reporte General de Corte de Caja");
                    stage.setResizable(true);
                    stage.show();
                    stage.requestFocus();
                } else {
                    Tools.showAlertNotification(
                            "/view/image/warning_large.png",
                            "Generar Vista",
                            Tools.newLineString((String) object),
                            Duration.seconds(10),
                            Pos.BOTTOM_RIGHT);
                }

            } catch (HeadlessException | IOException ex) {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar Vista",
                        Tools.newLineString("Error al generar el reporte : " + ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Generar Vista",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/pdf.png",
                    "Generar Vista",
                    Tools.newLineString("Se está generando el modal de ventas."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void onEventPdfCaja() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Reporte de Corte de Caja");
        fileChooser.setInitialFileName("Reporte de Corte de Caja del " + Tools.getDatePicker(dpFechaInicialCaja) + " al " + Tools.getDatePicker(dpFechaFinalCaja));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Documento", Arrays.asList("*.pdf", "*.PDF"))
        );
        File file = fileChooser.showSaveDialog(hbWindow.getScene().getWindow());
        if (file != null) {
            file = new File(file.getAbsolutePath());
            if (file.getName().endsWith("pdf") || file.getName().endsWith("PDF")) {
                onEventPdfCaja(file);

            }
        }
    }

    private void onEventExcelCaja() {
        if (dpFechaInicialCaja.getValue() == null) {
            Tools.AlertMessageWarning(hbWindow, "Reporte Corte de Caja", "Ingrese la fecha de inicio.");
            dpFechaInicialCaja.requestFocus();
        } else if (dpFechaFinalCaja.getValue() == null) {
            Tools.AlertMessageWarning(hbWindow, "Reporte Corte de Caja", "Ingrese la fecha de fin.");
            dpFechaFinalCaja.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Corte de Caja");
            fileChooser.setInitialFileName("Reporte de Corte de Caja del " + Tools.getDatePicker(dpFechaInicialCaja) + " al " + Tools.getDatePicker(dpFechaFinalCaja));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx")
            );
            File file = fileChooser.showSaveDialog(hbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xlsx")) {
                    onEventExcelCaja(file);
                } else {
                    Tools.AlertMessageWarning(hbWindow, "Exportar", "Elija un formato valido");
                }
            }
        }
    }

    private void onEventPdfCaja(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                try {
                    Object object = reportGeneralCorteCaja();
                    if (object instanceof JasperPrint) {
                        JasperExportManager.exportReportToPdfFile((JasperPrint) object, file.getAbsolutePath());
                        return "successful";
                    } else {
                        return (String) object;
                    }
                } catch (JRException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        };

        task.setOnSucceeded(w -> {
            String object = task.getValue();
            if (object.equalsIgnoreCase("successful")) {
                Tools.showAlertNotification(
                        "/view/image/information_large.png",
                        "Generar PDF",
                        Tools.newLineString("Se completó la creación del pdf correctamente en la ruta " + file.getAbsolutePath()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar PDF",
                        Tools.newLineString((String) object),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Generar PDF",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/pdf.png",
                    "Generar PDF",
                    Tools.newLineString("Se está generando el pdf de ventas."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventExcelCaja(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = CajaADO.ListarMovimientoPorFecha(Tools.getDatePicker(dpFechaInicialCaja), Tools.getDatePicker(dpFechaFinalCaja));

                if (object instanceof ArrayList) {
                    ArrayList<Double> arrayList = (ArrayList<Double>) object;
                    double montoBase = arrayList.get(0);

                    double ventaEfectivo = arrayList.get(1);
                    double ventaTarjeta = arrayList.get(2);
                    double ventaDeposito = arrayList.get(3);

                    double ingresoEfectivo = arrayList.get(4);
                    double ingresoTarjeta = arrayList.get(5);
                    double ingresoDeposito = arrayList.get(6);

                    double salidaEfectivo = arrayList.get(7);
                    double salidaTarjeta = arrayList.get(8);
                    double salidaDeposito = arrayList.get(9);

                    double sumaEfectivo = (montoBase + ventaEfectivo + ingresoEfectivo) - salidaEfectivo;

                    try {
                        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                            XSSFSheet sheet = workbook.createSheet("Resumen");

                            Font fontTitle = workbook.createFont();
                            fontTitle.setFontHeightInPoints((short) 12);
                            fontTitle.setBold(true);
                            fontTitle.setColor(HSSFColor.WHITE.index);

                            XSSFCellStyle cellStyleTitle = workbook.createCellStyle();
                            cellStyleTitle.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 106, 193)));
                            cellStyleTitle.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleTitle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            cellStyleTitle.setFont(fontTitle);

                            XSSFRow titleRow = sheet.createRow(0);
                            XSSFCell cellTitle = titleRow.createCell(0);
                            cellTitle.setCellValue("Resumen de Movimiento de Corte de Caja");
                            cellTitle.setCellStyle(cellStyleTitle);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                            //-------------------------------------------------------------------------

                            XSSFCellStyle cellStyleHeader = workbook.createCellStyle();
                            cellStyleHeader.setFillForegroundColor(new XSSFColor(new java.awt.Color(128, 128, 128)));
                            cellStyleHeader.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            cellStyleHeader.setFont(fontTitle);

                            XSSFRow headerRow = sheet.createRow(1);

                            cellTitle = headerRow.createCell(3);
                            cellTitle.setCellValue("FECHA");
                            cellTitle.setCellStyle(cellStyleHeader);

                            cellTitle = headerRow.createCell(4);
                            cellTitle.setCellValue(Tools.getDatePicker(dpFechaInicialCaja));
                            cellTitle.setCellStyle(cellStyleHeader);

                            cellTitle = headerRow.createCell(5);
                            cellTitle.setCellValue(Tools.getDatePicker(dpFechaFinalCaja));
                            cellTitle.setCellStyle(cellStyleHeader);

                            sheet.autoSizeColumn(3);
                            sheet.autoSizeColumn(4);
                            sheet.autoSizeColumn(5);

                            //-------------------------------------------------------------------------
                            XSSFCellStyle cellStyleNumber = workbook.createCellStyle();
                            cellStyleNumber.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

                            XSSFRow header = sheet.createRow(2);
                            XSSFCell cell = header.createCell(0);
                            cell.setCellValue("MONTO BASE");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(montoBase, 2)));
                            //-------------------------------------------------------------------------

                            //-------------------------------------------------------------------------                         
                            header = sheet.createRow(3);
                            cell = header.createCell(0);
                            cell.setCellValue("VENTA EN EFECTIVO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ventaEfectivo, 2)));

                            header = sheet.createRow(4);
                            cell = header.createCell(0);
                            cell.setCellValue("VENTA CON TARJETA");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ventaTarjeta, 2)));

                            header = sheet.createRow(5);
                            cell = header.createCell(0);
                            cell.setCellValue("VENTA CON DEPOSITO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ventaDeposito, 2)));
                            //-------------------------------------------------------------------------

                            //-------------------------------------------------------------------------
                            header = sheet.createRow(6);
                            cell = header.createCell(0);
                            cell.setCellValue("INGRESO CON EFECTIVO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ingresoEfectivo, 2)));

                            header = sheet.createRow(7);
                            cell = header.createCell(0);
                            cell.setCellValue("INGRESO CON TARJETA");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ingresoTarjeta, 2)));

                            header = sheet.createRow(8);
                            cell = header.createCell(0);
                            cell.setCellValue("INGRESO CON DEPOSITO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(ingresoDeposito, 2)));
                            //-------------------------------------------------------------------------

                            //-------------------------------------------------------------------------
                            header = sheet.createRow(9);
                            cell = header.createCell(0);
                            cell.setCellValue("SALIDAS EN EFECTIVO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(salidaEfectivo, 2)));

                            header = sheet.createRow(10);
                            cell = header.createCell(0);
                            cell.setCellValue("SALIDA CON TARJETA");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(salidaTarjeta, 2)));

                            header = sheet.createRow(11);
                            cell = header.createCell(0);
                            cell.setCellValue("SALIDA CON DEPOSITO");

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(salidaDeposito, 2)));
                            //-------------------------------------------------------------------------

                            Font fontSuma = workbook.createFont();
                            fontSuma.setFontHeightInPoints((short) 12);
                            fontSuma.setBold(true);
                            fontSuma.setColor(HSSFColor.WHITE.index);

                            XSSFCellStyle cellStyleSuma = workbook.createCellStyle();
                            cellStyleSuma.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 0, 0)));
                            cellStyleSuma.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleSuma.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            cellStyleSuma.setFont(fontSuma);

                            header = sheet.createRow(13);
                            cell = header.createCell(0);
                            cell.setCellValue("TOTAL EFECTIVO");
                            cell.setCellStyle(cellStyleSuma);

                            cell = header.createCell(5);
                            cell.setCellStyle(cellStyleNumber);
                            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell.setCellValue(Double.parseDouble(Tools.roundingValue(sumaEfectivo, 2)));
                            sheet.autoSizeColumn(0);

                            try (FileOutputStream out = new FileOutputStream(file)) {
                                workbook.write(out);
                            }
                        }
                        return "successful";
                    } catch (IOException ex) {
                        return "Error al exportar el archivo, intente de nuevo.";
                    }

                } else {
                    return (String) object;
                }
            }
        };

        task.setOnSucceeded(w -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("successful")) {
                Tools.showAlertNotification(
                        "/view/image/information_large.png",
                        "Generar Excel",
                        Tools.newLineString("Se completo la creación del excel correctamente en la ruta " + file.getAbsolutePath()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            } else {
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar Excel",
                        Tools.newLineString(result),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        task.setOnFailed(w -> {
            Tools.showAlertNotification(
                    "/view/image/warning_large.png",
                    "Generar Excel",
                    Tools.newLineString("Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnScheduled(w -> {
            Tools.showAlertNotification(
                    "/view/image/excel.png",
                    "Generar Excel",
                    Tools.newLineString("Se está generando el excel de corte de caja."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private Object reportGeneralCorteCaja() throws JRException {
        Object object = CajaADO.ListarMovimientoPorFecha(Tools.getDatePicker(dpFechaInicialCaja), Tools.getDatePicker(dpFechaFinalCaja));

        if (object instanceof ArrayList) {
            ArrayList<Double> arrayList = (ArrayList<Double>) object;
            if (arrayList.isEmpty()) {
                return "No hay datos para mostrar.";
            }
            InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_ICON);
            InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);

            if (Session.COMPANY_IMAGE != null) {
                imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
            }
            InputStream dir = getClass().getResourceAsStream("/report/CortedeCajaResumen.jasper");

            double montoInicial = arrayList.get(0);

            double ventaEfectivo = arrayList.get(1);
            double ventaTarjeta = arrayList.get(2);
            double ventaDeposito = arrayList.get(3);

            double ingresoEfectivo = arrayList.get(4);
            double ingresoTarjeta = arrayList.get(5);
            double ingresoDeposito = arrayList.get(6);

            double salidaEfectivo = arrayList.get(7);
            double salidaTarjeta = arrayList.get(8);
            double salidaDeposito = arrayList.get(9);

            Map map = new HashMap();
            map.put("LOGO", imgInputStream);
            map.put("ICON", imgInputStreamIcon);
            map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
            map.put("DOCUMENTOEMPRESA", Session.COMPANY_NUMERO_DOCUMENTO);
            map.put("DIRECCION", Session.COMPANY_DOMICILIO);
            map.put("EMAIL", Session.COMPANY_EMAIL);
            map.put("TELEFONOCELULAR", Session.COMPANY_TELEFONO + " - " + Session.COMPANY_CELULAR);
            map.put("INICIODETURNO", dpFechaInicialCaja.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
            map.put("FINDETURNO", dpFechaFinalCaja.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));

            map.put("BASE", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoInicial, 2));
            map.put("VENTASENEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
            map.put("VENTASCONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
            map.put("VENTASCONDEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

            map.put("INGRESODEEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
            map.put("INGRESOCONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
            map.put("INGRESOCONDEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

            map.put("SALIDADEEFECTIVO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
            map.put("SALIDACONTARJETA", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
            map.put("SALIDADEPOSITO", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

            map.put("TOTAL", Session.MONEDA_SIMBOLO + " " + Tools.roundingValue((montoInicial + ventaEfectivo + ingresoEfectivo) - salidaEfectivo, 2));

            JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JREmptyDataSource());

            return jasperPrint;
        } else {
            return (String) object;
        }
    }

    @FXML
    private void onKeyPressedVisualizar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openViewVisualizar();
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) {
        openViewVisualizar();
    }

    @FXML
    private void onKeyPressedPdf(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPdf();
        }
    }

    @FXML
    private void onActionPdf(ActionEvent event) {
        onEventPdf();
    }

    @FXML
    private void onKeyPressedExcel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExcel();
        }
    }

    @FXML
    private void onActionExcel(ActionEvent event) {
        onEventExcel();
    }

    @FXML
    private void onActionCbDocumentosSeleccionar(ActionEvent event) {
        if (cbDocumentosSeleccionar.isSelected()) {
            cbDocumentos.setDisable(true);
            cbDocumentos.getSelectionModel().select(null);
        } else {
            cbDocumentos.setDisable(false);
        }
    }

    @FXML
    private void onActionCbClientesSeleccionar(ActionEvent event) {
        if (cbClientesSeleccionar.isSelected()) {
            btnClientes.setDisable(true);
            txtClientes.setText("");
            idCliente = "";
        } else {
            btnClientes.setDisable(false);
        }
    }

    @FXML
    private void onActionCbVendedoresSeleccionar(ActionEvent event) {
        if (cbVendedoresSeleccionar.isSelected()) {
            btnVendedor.setDisable(true);
            txtVendedores.setText("");
            idEmpleado = "";
        } else {
            btnVendedor.setDisable(false);
        }
    }

    @FXML
    private void onActionCbTipoPago(ActionEvent event) {
        hbTipoPago.setDisable(cbTipoPagoSeleccionar.isSelected());
    }

    @FXML
    private void onActionCbMetodoPago(ActionEvent event) {
        hbFormaPago.setDisable(cbMetodoPagoSeleccionar.isSelected());
    }

    @FXML
    private void onKeyPressedClientes(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowClientes();
        }
    }

    @FXML
    private void onActionClientes(ActionEvent event) {
        openWindowClientes();
    }

    @FXML
    private void onKeyPressedVendedor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVendedores();
        }
    }

    @FXML
    private void onActionVendedor(ActionEvent event) {
        openWindowVendedores();
    }

    @FXML
    private void onKeyPressedVisualizarCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openViewVisualizarCaja();
        }
    }

    @FXML
    private void onActionVisualizarCaja(ActionEvent event) {
        openViewVisualizarCaja();
    }

    @FXML
    private void onKeyPressedPdfCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPdfCaja();
        }
    }

    @FXML
    private void onActionPdfCaja(ActionEvent event) {
        onEventPdfCaja();
    }

    @FXML
    private void onKeyPressedExcelCaja(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExcelCaja();
        }
    }

    @FXML
    private void onActionExcelCaja(ActionEvent event) {
        onEventExcelCaja();
    }

    public void setClienteVentaReporte(String idCliente, String datos) {
        this.idCliente = idCliente;
        txtClientes.setText(datos);
    }

    public void setIdEmpleado(String idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public TextField getTxtVendedores() {
        return txtVendedores;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
