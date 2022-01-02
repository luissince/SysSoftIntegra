package controller.reporte;

import controller.contactos.clientes.FxClienteListaController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.NotaCreditoADO;
import model.NotaCreditoTB;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxNotaCreditoReporteController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private CheckBox cbClientes;
    @FXML
    private TextField txtClientes;
    @FXML
    private Button btnClientes;

    private FxPrincipalController fxPrincipalController;

    private String idCliente;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        idCliente = "";
    }

    private void openWindowClientes() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CLIENTE_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxClienteListaController controller = fXMLLoader.getController();
            controller.setInitNotaCreditoReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un cliente", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillCustomersTable("");
        } catch (IOException ex) {
            System.out.println("Nota Credito reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private Object reportGenerate() throws JRException {
        Object object = NotaCreditoADO.GetReporteNotaCredito(
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                cbClientes.isSelected() ? 0 : 1,
                idCliente);
        if (object instanceof ArrayList) {
            ArrayList<NotaCreditoTB> ingresoTBs = (ArrayList<NotaCreditoTB>) object;
            if (!ingresoTBs.isEmpty()) {
                InputStream dir = getClass().getResourceAsStream("/report/NotaCreditoGeneral.jasper");
                Map map = new HashMap();
                map.put("CLIENTE", cbClientes.isSelected() ? "TODOS" : txtClientes.getText());
                map.put("PERIODO", "DEL " + Tools.formatDate(Tools.getDatePicker(dpFechaInicial)) + " al " + Tools.formatDate(Tools.getDatePicker(dpFechaFinal)));

                JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(ingresoTBs));
                return jasperPrint;
            } else {
                return "No hay datos para mostrar";
            }
        } else {
            return object;
        }

    }

    private void openViewVisualizar() {

        if (!cbClientes.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Notas de Credito", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else {

            ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<Object> task = new Task<Object>() {
                @Override
                public Object call() throws JRException {
                    return reportGenerate();
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
                        controller.setFileName("Lista de Notas de Crédito del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Notas de Credito");
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
        if (!cbClientes.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Notas de Credito", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Notas de Crédito");
            fileChooser.setInitialFileName("Lista de Notas de Crédito del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Documento", Arrays.asList("*.pdf", "*.PDF"))
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("pdf") || file.getName().endsWith("PDF")) {
                    onEventPdf(file);
                }
            }
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
                    Object object = reportGenerate();
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

    private void onEventExcel() {
        if (!cbClientes.isSelected() && idCliente.equalsIgnoreCase("") && txtClientes.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Notas de Credito", "Ingrese un cliente para generar el reporte.");
            btnClientes.requestFocus();
        } else {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Nota de Credito");
            fileChooser.setInitialFileName("Lista de Notas de Crédito del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                    onEventExcel(file);
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
            public String call() {
                Object object = NotaCreditoADO.GetReporteNotaCredito(
                        Tools.getDatePicker(dpFechaInicial),
                        Tools.getDatePicker(dpFechaFinal),
                        cbClientes.isSelected() ? 0 : 1,
                        idCliente);
                if (object instanceof ArrayList) {
                    try {
                        ArrayList<NotaCreditoTB> notaCreditoTBs = (ArrayList<NotaCreditoTB>) object;

                        Workbook workbook;
                        if (file.getName().endsWith("xls")) {
                            workbook = new HSSFWorkbook();
                        } else {
                            workbook = new XSSFWorkbook();
                        }

                        Sheet sheet = workbook.createSheet("Ingresos");

                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        font.setColor(HSSFColor.BLACK.index);

                        CellStyle cellStyleHeader = workbook.createCellStyle();
                        cellStyleHeader.setFont(font);
                        cellStyleHeader.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        String header[] = {"Id", "N° Documento", "Cliente", "Serie", "Numeracion", "Total"};

                        Row headerRow = sheet.createRow(0);
                        for (int i = 0; i < header.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellStyle(cellStyleHeader);
                            cell.setCellValue(header[i].toUpperCase());
                        }

                        CellStyle cellStyle = workbook.createCellStyle();
                        for (int i = 0; i < notaCreditoTBs.size(); i++) {
                            Row row = sheet.createRow(i + 1);
                            Cell cell1 = row.createCell(0);
                            cell1.setCellStyle(cellStyle);
                            cell1.setCellValue(String.valueOf(i + 1));
                            cell1.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell1.getColumnIndex());

                            Cell cell2 = row.createCell(1);
                            cell2.setCellStyle(cellStyle);
                            cell2.setCellValue(notaCreditoTBs.get(i).getClienteTB().getNumeroDocumento());
                            cell2.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell2.getColumnIndex());

                            Cell cell3 = row.createCell(2);
                            cell3.setCellStyle(cellStyle);
                            cell3.setCellValue(notaCreditoTBs.get(i).getClienteTB().getInformacion());
                            cell3.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell3.getColumnIndex());

                            Cell cell4 = row.createCell(3);
                            cell4.setCellStyle(cellStyle);
                            cell4.setCellValue(notaCreditoTBs.get(i).getSerie());
                            cell4.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell4.getColumnIndex());

                            Cell cell5 = row.createCell(4);
                            cell5.setCellStyle(cellStyle);
                            cell5.setCellValue(notaCreditoTBs.get(i).getNumeracion());
                            cell5.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell5.getColumnIndex());

                            Cell cell6 = row.createCell(5);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell6.setCellStyle(cellStyle);
                            cell6.setCellValue(Double.parseDouble(Tools.roundingValue(notaCreditoTBs.get(i).getImporteNeto(), 2)));
                            cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                            sheet.autoSizeColumn(cell6.getColumnIndex());
                        }
                        try (FileOutputStream out = new FileOutputStream(file)) {
                            workbook.write(out);
                        }
                        workbook.close();
                        return "successful";
                    } catch (IOException ex) {
                        return ex.getLocalizedMessage();
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
    private void onActionCbClientes(ActionEvent event) {
        if (cbClientes.isSelected()) {
            btnClientes.setDisable(true);
            txtClientes.setText("");
            idCliente = "";
        } else {
            btnClientes.setDisable(false);
        }
    }

    @FXML
    private void onKeyPressedClientes(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowClientes();
        }
    }

    public void setClienteVentaReporte(String idCliente, String datos) {
        this.idCliente = idCliente;
        txtClientes.setText(datos);
    }
    
    @FXML
    private void onActionClientes(ActionEvent event) {
        
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
