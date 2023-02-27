package controller.reporte;

import controller.configuracion.empleados.FxEmpleadosListaController;
import controller.inventario.suministros.FxSuministrosListaController;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DetalleTB;
import model.MermaTB;
import model.ProduccionTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import service.DetalleADO;
import service.MermaADO;
import service.ProduccionADO;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxProduccionReporteController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private CheckBox cbEstadoSeleccionar;
    @FXML
    private ComboBox<String> cbEstado;
    @FXML
    private CheckBox cbEncagadoSeleccionar;
    @FXML
    private HBox hbEncargado;
    @FXML
    private TextField txtEmpleado;
    @FXML
    private CheckBox cbTipoMermaSeleccionar;
    @FXML
    private ComboBox<DetalleTB> cbTipoMerma;
    @FXML
    private CheckBox cbProductoMermaSeleccionar;
    @FXML
    private TextField txtProductoMerma;
    @FXML
    private Button btnProductos;
    @FXML
    private CheckBox cbAgruparMerma;

    private FxPrincipalController fxPrincipalController;

    private String idEmpleado;

    private String idProductoMerma;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        cbEstado.getItems().addAll("COMPLETADO", "EN PRODUCCIÓN", "ANULADO");
        cbTipoMerma.getItems().addAll(DetalleADO.GetDetailId("0020"));
        idEmpleado = idProductoMerma = "";
    }

    private void onEventEmpleado() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxEmpleadosListaController controller = fXMLLoader.getController();
            controller.setInitProduccionReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un Empleado", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Venta reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private void openWindowSuministros() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitProduccionReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione un Producto", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.fillSuministrosTable((short) 0, "");
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private Object reportGenerate() throws JRException {
        Object object = ProduccionADO.Reporte_Produccion(
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                cbEstadoSeleccionar.isSelected() ? 0 : cbEstado.getSelectionModel().getSelectedIndex() == 0 ? 1 : cbEstado.getSelectionModel().getSelectedIndex() == 1 ? 2 : 3,
                cbEncagadoSeleccionar.isSelected() ? "" : idEmpleado
        );
        if (object instanceof ArrayList) {
            ArrayList<ProduccionTB> arrayList = (ArrayList<ProduccionTB>) object;
            if (arrayList.isEmpty()) {
                return "No hay datos para mostrar";
            }
            InputStream dir = getClass().getResourceAsStream("/report/Produccion.jasper");
            Map map = new HashMap();
            map.put("PERIODO", dpFechaInicial.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")) + " - " + dpFechaFinal.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
            map.put("ENCARGADO", cbEncagadoSeleccionar.isSelected() ? "TODOS" : txtEmpleado.getText().trim());
            map.put("ESTADO", cbEstadoSeleccionar.isSelected() ? "TODOS" : cbEstado.getSelectionModel().getSelectedItem());

            return JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(arrayList));
        } else {
            return (String) object;
        }
    }

    private void onEventVisualizar() {
        if (!cbEncagadoSeleccionar.isSelected() && idEmpleado.equalsIgnoreCase("")) {
            Tools.AlertMessageWarning(vbWindow, "Producción", "Ingrese el encargado para continuar.");
            cbEstado.requestFocus();
        } else if (!cbEstadoSeleccionar.isSelected() && cbEstado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Producción", "Seleccione el estado de la producción.");
            cbEstado.requestFocus();
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
                        controller.setFileName("LISTA DE PRODUCCIÓN DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Produccion");
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
                        Tools.newLineString("Se está generando el modal de producción."),
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Reporte de Producción");
        fileChooser.setInitialFileName("LISTA DE PRODUCCIÓN DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Reporte de Producción");
        fileChooser.setInitialFileName("LISTA DE PRODUCCIÓN DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
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

    private void onEventExcel(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                try {

                    Workbook workbook;
                    if (file.getName().endsWith("xls")) {
                        workbook = new HSSFWorkbook();
                    } else {
                        workbook = new XSSFWorkbook();
                    }

                    Sheet sheet = workbook.createSheet("Producción");

//                        Font font = workbook.createFont();
//                        font.setFontHeightInPoints((short) 12);
//                        font.setBold(true);
//                        font.setColor(HSSFColor.BLACK.index);
//
//                        CellStyle cellStyleHeader = workbook.createCellStyle();
//                        cellStyleHeader.setFont(font);
//                        cellStyleHeader.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//                        String header[] = {"Id", "Fecha", "Documento", "Proveedor", "Serie", "Numeración", "Tipo de Compra", "Estado", "Importe"};
//
//                        Row headerRow = sheet.createRow(0);
//                        for (int i = 0; i < header.length; i++) {
//                            Cell cell = headerRow.createCell(i);
//                            cell.setCellStyle(cellStyleHeader);
//                            cell.setCellValue(header[i].toUpperCase());
//
//                        }
//
//                        CellStyle cellStyle = workbook.createCellStyle();
//                        for (int i = 0; i < list.size(); i++) {
//                            if (list.get(i).getEstadoName().equalsIgnoreCase("ANULADO")) {
//                                Font fontRed = workbook.createFont();
//                                fontRed.setColor(HSSFColor.RED.index);
//                                cellStyle = workbook.createCellStyle();
//                                cellStyle.setFont(fontRed);
//                                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//                            } else {
//                                Font fontBlack = workbook.createFont();
//                                fontBlack.setColor(HSSFColor.BLACK.index);
//                                fontBlack.setBold(false);
//                                cellStyle.setFont(fontBlack);
//                            }
//
//                            Row row = sheet.createRow(i + 1);
//                            Cell cell1 = row.createCell(0);
//                            cell1.setCellStyle(cellStyle);
//                            cell1.setCellValue(String.valueOf(i + 1));
//                            cell1.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell1.getColumnIndex());
//
//                            Cell cell2 = row.createCell(1);
//                            cell2.setCellStyle(cellStyle);
//                            cell2.setCellValue(String.valueOf(list.get(i).getFechaCompra()));
//                            cell2.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell2.getColumnIndex());
//
//                            Cell cell3 = row.createCell(2);
//                            cell3.setCellStyle(cellStyle);
//                            cell3.setCellValue(String.valueOf(list.get(i).getProveedorTB().getNumeroDocumento()));
//                            cell3.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell3.getColumnIndex());
//
//                            Cell cell4 = row.createCell(3);
//                            cell4.setCellStyle(cellStyle);
//                            cell4.setCellValue(String.valueOf(list.get(i).getProveedorTB().getRazonSocial()));
//                            cell4.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell4.getColumnIndex());
//
//                            Cell cell5 = row.createCell(4);
//                            cell5.setCellStyle(cellStyle);
//                            cell5.setCellValue(list.get(i).getSerie());
//                            cell5.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell5.getColumnIndex());
//
//                            Cell cell6 = row.createCell(5);
//                            cell6.setCellStyle(cellStyle);
//                            cell6.setCellValue(list.get(i).getNumeracion());
//                            cell6.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell6.getColumnIndex());
//
//                            Cell cell7 = row.createCell(6);
//                            cell7.setCellStyle(cellStyle);
//                            cell7.setCellValue(list.get(i).getTipoName());
//                            cell7.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell7.getColumnIndex());
//
//                            Cell cell8 = row.createCell(7);
//                            cell8.setCellStyle(cellStyle);
//                            cell8.setCellValue(list.get(i).getEstadoName());
//                            cell8.setCellType(Cell.CELL_TYPE_STRING);
//                            sheet.autoSizeColumn(cell8.getColumnIndex());
//
//                            Cell cell9 = row.createCell(8);
//                            cellStyle = workbook.createCellStyle();
//                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
//                            cell9.setCellStyle(cellStyle);
//                            cell9.setCellValue(Double.parseDouble(Tools.roundingValue(list.get(i).getTotal(), 2)));
//                            cell9.setCellType(Cell.CELL_TYPE_NUMERIC);
//                            sheet.autoSizeColumn(cell9.getColumnIndex());
//                        }
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        workbook.write(out);
                    }
                    workbook.close();

                    return "successful";
                } catch (IOException ex) {
                    return ex.getLocalizedMessage();
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

    private Object reportGenerateMerma() throws JRException {
        Object object = MermaADO.ReporteMerma(
                cbTipoMermaSeleccionar.isSelected() ? 0 : cbTipoMerma.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbProductoMermaSeleccionar.isSelected() ? "" : idProductoMerma,
                cbAgruparMerma.isSelected()
        );
        if (object instanceof ArrayList) {
            if (((ArrayList<MermaTB>) object).isEmpty()) {
                return "No hay datos para mostrar.";
            }

            double costoPromedio = 0;
            double cantidadMerma = 0;
            for (MermaTB m : ((ArrayList<MermaTB>) object)) {
                costoPromedio += m.getCostoReporte();
                cantidadMerma += m.getCantidadReporte();
            }

            InputStream dir = getClass().getResourceAsStream("/report/Merma.jasper");
            Map map = new HashMap();
            map.put("PRODUCTO", cbProductoMermaSeleccionar.isSelected() ? "TODOS" : txtProductoMerma.getText().trim().toUpperCase());
            map.put("TIPO_MERMA", cbTipoMermaSeleccionar.isSelected() ? "TODOS" : cbTipoMerma.getSelectionModel().getSelectedItem().getNombre());
            map.put("COSTO_PROMEDIO", Tools.roundingValue(costoPromedio, 2));
            map.put("CANTIDAD_MERMA", Tools.roundingValue(cantidadMerma, 2));

            return JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource((ArrayList<MermaTB>) object));
        } else {
            return (String) object;
        }

    }

    private void onEventVisualizarMerma() {
        if (!cbTipoMermaSeleccionar.isSelected() && cbTipoMerma.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Merma", "Seleccione el tipo de merma.");
        } else if (!cbProductoMermaSeleccionar.isSelected() && idProductoMerma.equalsIgnoreCase("")) {
            Tools.AlertMessageWarning(vbWindow, "Merma", "Ingrese el producto a buscar");
        } else {
            ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                return t;
            });

            Task<Object> task = new Task<Object>() {
                @Override
                public Object call() throws JRException {
                    return reportGenerateMerma();
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
                        controller.setFileName("Reporte de Merma");
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Mermas");
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
                        Tools.newLineString("Se está generando el reporte de merma."),
                        Duration.seconds(5),
                        Pos.BOTTOM_RIGHT);
            });
            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }
        }
    }

    @FXML
    private void onKeyPressedVisualizar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventVisualizar();
            event.consume();
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) {
        onEventVisualizar();
    }

    @FXML
    private void onKeyPressedPdf(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPdf();
            event.consume();
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
            event.consume();
        }
    }

    @FXML
    private void onActionExcel(ActionEvent event) {
        onEventExcel();
    }

    @FXML
    private void onActionEstadoSeleccionar(ActionEvent event) {
        cbEstado.setDisable(cbEstadoSeleccionar.isSelected());
    }

    @FXML
    private void onActionEncargadoSeleccionar(ActionEvent event) {
        hbEncargado.setDisable(cbEncagadoSeleccionar.isSelected());
    }

    @FXML
    private void onKeyPressedEmpleado(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEmpleado();
            event.consume();
        }
    }

    @FXML
    private void onActionEmpleado(ActionEvent event) {
        onEventEmpleado();
    }

    @FXML
    private void onKeyPressedVisualizarMerma(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventVisualizarMerma();
            event.consume();
        }
    }

    @FXML
    private void onActionVisualizarMerma(ActionEvent event) {
        onEventVisualizarMerma();
    }

    @FXML
    private void onActionTipoMermaSeleccionar(ActionEvent event) {
        cbTipoMerma.setDisable(cbTipoMermaSeleccionar.isSelected());
    }

    @FXML
    private void onActionCbProductoMermaSeleccionar(ActionEvent event) {
        btnProductos.setDisable(cbProductoMermaSeleccionar.isSelected());
    }

    @FXML
    private void onKeyPressedProductos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministros();
            event.consume();
        }
    }

    @FXML
    private void onActionProductos(ActionEvent event) {
        openWindowSuministros();
    }

    public void setLoadDataEmpleado(String idEmpleado, String datos) {
        this.idEmpleado = idEmpleado;
        txtEmpleado.setText(datos);
    }

    public void setLoadDataProductoMerma(String idProductoMerma, String datos) {
        this.idProductoMerma = idProductoMerma;
        txtProductoMerma.setText(datos);
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }
}
