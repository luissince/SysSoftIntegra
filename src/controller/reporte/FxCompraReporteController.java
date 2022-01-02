package controller.reporte;

import controller.contactos.proveedores.FxProveedorListaController;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import model.CompraADO;
import model.CompraTB;
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

public class FxCompraReporteController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private CheckBox cbProveedoresSeleccionar;
    @FXML
    private TextField txtProveedor;
    @FXML
    private Button btnProveedor;
    @FXML
    private RadioButton rbContado;
    @FXML
    private RadioButton rbCredito;
    @FXML
    private CheckBox cbTipoCompra;
    @FXML
    private HBox hbFormasPago;
    @FXML
    private CheckBox cbMetodoPagoSeleccionar;
    @FXML
    private HBox hbFormaPago;
    @FXML
    private RadioButton rbEfectivo;
    @FXML
    private RadioButton rbTarjeta;
    @FXML
    private RadioButton rbDeposito;

    private FxPrincipalController fxPrincipalController;

    private String idProveedor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);

        ToggleGroup groupTipoPago = new ToggleGroup();
        rbContado.setToggleGroup(groupTipoPago);
        rbCredito.setToggleGroup(groupTipoPago);

        ToggleGroup groupFormaPago = new ToggleGroup();
        rbEfectivo.setToggleGroup(groupFormaPago);
        rbTarjeta.setToggleGroup(groupFormaPago);
        rbDeposito.setToggleGroup(groupFormaPago);
        idProveedor = "";
    }

    private void openWindowProveedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_PROVEEDORES_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxProveedorListaController controller = fXMLLoader.getController();
            controller.setInitComprasReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Proveedor", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.initTable());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller reporte" + ex.getLocalizedMessage());
        }
    }

    private Object reportGenerate() throws JRException {
        Object object = CompraADO.GetReporteGenetalCompras(Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                idProveedor,
                cbTipoCompra.isSelected() ? 0 : rbContado.isSelected() ? 1 : 2,
                !cbMetodoPagoSeleccionar.isSelected(),
                rbEfectivo.isSelected() ? 1 : rbTarjeta.isSelected() ? 2 : 3);

        if (object instanceof ArrayList) {
            ArrayList<CompraTB> list = (ArrayList<CompraTB>) object;
            if (!list.isEmpty()) {
                double totalContado = 0;
                double totalCredito = 0;
                double totalAnulados = 0;

                double efectivo = 0;
                double tarjeta = 0;
                double deposito = 0;

                for (CompraTB c : list) {
                    switch (c.getEstado()) {
                        case 1:
                            totalContado += c.getTotal();
                            break;
                        case 2:
                            totalCredito += c.getTotal();
                            break;
                        default:
                            totalAnulados += c.getTotal();
                            break;
                    }

                    if (c.getEstado() != 3) {
                        if (c.getEfectivo()) {
                            efectivo += c.getTotal();
                        } else if (c.getTarjeta()) {
                            tarjeta += c.getTotal();
                        } else if (c.getDeposito()) {
                            deposito += c.getTotal();
                        }
                    }

                }

                Map map = new HashMap();
                map.put("PERIODO", dpFechaInicial.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")) + " - " + dpFechaFinal.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                map.put("TIPO", cbTipoCompra.isSelected() ? "TODOS" : rbContado.isSelected() ? "AL CONTADO" : "AL CRÉDITO");
                map.put("PROVEEDOR", cbProveedoresSeleccionar.isSelected() ? "TODOS" : txtProveedor.getText().toUpperCase());
                map.put("METODO", cbMetodoPagoSeleccionar.isSelected() ? "TODOS" : rbEfectivo.isSelected() ? "EFECTIVO" : rbTarjeta.isSelected() ? "TARJETA" : "DEPOSITO");
                map.put("COMPRACONTADO", Tools.roundingValue(totalContado, 2));
                map.put("COMPRACREDITO", Tools.roundingValue(totalCredito, 2));
                map.put("COMPRANULADAS", Tools.roundingValue(totalAnulados, 2));
                map.put("EFECTIVO", Tools.roundingValue(efectivo, 2));
                map.put("TARJETA", Tools.roundingValue(tarjeta, 2));
                map.put("DEPOSITO", Tools.roundingValue(deposito, 2));
                
                InputStream dir = getClass().getResourceAsStream("/report/CompraGeneral.jasper");
                JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(list));
                return jasperPrint;
            } else {
                return "No hay datos para mostrar";
            }
        } else {
            return (String) object;
        }
    }

    private void openViewVisualizar() {
        if (!cbProveedoresSeleccionar.isSelected() && idProveedor.equalsIgnoreCase("") && txtProveedor.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Compras", "Ingrese un proveedor para generar el reporte.");
            btnProveedor.requestFocus();
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
                        controller.setFileName("Lista de compras del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Compras");
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
        if (!cbProveedoresSeleccionar.isSelected() && idProveedor.equalsIgnoreCase("") && txtProveedor.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Compras", "Ingrese un proveedor para generar el reporte.");
            btnProveedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Compra");
            fileChooser.setInitialFileName("Lista de compras del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
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
        if (!cbProveedoresSeleccionar.isSelected() && idProveedor.equalsIgnoreCase("") && txtProveedor.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Compras", "Ingrese un proveedor para generar el reporte.");
            btnProveedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Compras");
            fileChooser.setInitialFileName("Lista de compras del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                    onEventExcel(file);
                } else {
                    Tools.AlertMessageWarning(vbWindow, "Exportar", "Elija un formato valido");
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
                Object object = CompraADO.GetReporteGenetalCompras(
                        Tools.getDatePicker(dpFechaInicial),
                        Tools.getDatePicker(dpFechaFinal),
                        idProveedor,
                        cbTipoCompra.isSelected() ? 0 : rbContado.isSelected() ? 1 : 2,
                        !cbMetodoPagoSeleccionar.isSelected(),
                        rbEfectivo.isSelected() ? 1 : rbTarjeta.isSelected() ? 2 : 3);
                if (object instanceof ArrayList) {
                    try {
                        ArrayList<CompraTB> list = (ArrayList<CompraTB>) object;

                        Workbook workbook;
                        if (file.getName().endsWith("xls")) {
                            workbook = new HSSFWorkbook();
                        } else {
                            workbook = new XSSFWorkbook();
                        }

                        Sheet sheet = workbook.createSheet("Compras");

                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        font.setColor(HSSFColor.BLACK.index);

                        CellStyle cellStyleHeader = workbook.createCellStyle();
                        cellStyleHeader.setFont(font);
                        cellStyleHeader.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        String header[] = {"Id", "Fecha", "Documento", "Proveedor", "Serie", "Numeración", "Tipo de Compra", "Estado", "Importe"};

                        Row headerRow = sheet.createRow(0);
                        for (int i = 0; i < header.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellStyle(cellStyleHeader);
                            cell.setCellValue(header[i].toUpperCase());

                        }

                        CellStyle cellStyle = workbook.createCellStyle();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).getEstadoName().equalsIgnoreCase("ANULADO")) {
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
                            cell2.setCellValue(String.valueOf(list.get(i).getFechaCompra()));
                            cell2.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell2.getColumnIndex());

                            Cell cell3 = row.createCell(2);
                            cell3.setCellStyle(cellStyle);
                            cell3.setCellValue(String.valueOf(list.get(i).getProveedorTB().getNumeroDocumento()));
                            cell3.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell3.getColumnIndex());

                            Cell cell4 = row.createCell(3);
                            cell4.setCellStyle(cellStyle);
                            cell4.setCellValue(String.valueOf(list.get(i).getProveedorTB().getRazonSocial()));
                            cell4.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell4.getColumnIndex());

                            Cell cell5 = row.createCell(4);
                            cell5.setCellStyle(cellStyle);
                            cell5.setCellValue(list.get(i).getSerie());
                            cell5.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell5.getColumnIndex());

                            Cell cell6 = row.createCell(5);
                            cell6.setCellStyle(cellStyle);
                            cell6.setCellValue(list.get(i).getNumeracion());
                            cell6.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell6.getColumnIndex());

                            Cell cell7 = row.createCell(6);
                            cell7.setCellStyle(cellStyle);
                            cell7.setCellValue(list.get(i).getTipoName());
                            cell7.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell7.getColumnIndex());

                            Cell cell8 = row.createCell(7);
                            cell8.setCellStyle(cellStyle);
                            cell8.setCellValue(list.get(i).getEstadoName());
                            cell8.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell8.getColumnIndex());

                            Cell cell9 = row.createCell(8);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell9.setCellStyle(cellStyle);
                            cell9.setCellValue(Double.parseDouble(Tools.roundingValue(list.get(i).getTotal(), 2)));
                            cell9.setCellType(Cell.CELL_TYPE_NUMERIC);
                            sheet.autoSizeColumn(cell9.getColumnIndex());
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
    private void onActionCbProveedoresSeleccionar(ActionEvent event) {
        if (cbProveedoresSeleccionar.isSelected()) {
            btnProveedor.setDisable(true);
            txtProveedor.setText("");
            idProveedor = "";
        } else {
            btnProveedor.setDisable(false);
        }
    }

    @FXML
    private void onActionCbTipoCompro(ActionEvent event) {
        if (cbTipoCompra.isSelected()) {
            hbFormasPago.setDisable(true);
        } else {
            hbFormasPago.setDisable(false);
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
    private void onKeyPressedProveedor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProveedores();
        }
    }

    @FXML
    private void onActionProveedor(ActionEvent event) {
        openWindowProveedores();
    }

    @FXML
    private void onActionCbMetodoPago(ActionEvent event) {
        hbFormaPago.setDisable(cbMetodoPagoSeleccionar.isSelected());
    }

    public void setInitCompraReporteValue(String idProveedor, String datosProveedor) {
        this.idProveedor = idProveedor;
        txtProveedor.setText(datosProveedor);
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
