package controller.reporte;

import controller.configuracion.empleados.FxEmpleadosListaController;
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
import model.CajaADO;
import model.IngresoADO;
import model.IngresoTB;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxIngresosEgresosController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private CheckBox cbVendedorSelect;
    @FXML
    private TextField txtVendedores;
    @FXML
    private Button btnVendedor;
    @FXML
    private CheckBox cbTransacciones;
    @FXML
    private CheckBox cbMovientoCaja;

    private FxPrincipalController fxPrincipalController;

    private String idVendedor;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
    }

    private void openWindowVendedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxEmpleadosListaController controller = fXMLLoader.getController();
            controller.setInitIngresosEgresosReporteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Elija un vendedor", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShown(w -> controller.loadInit());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Venta reporte controller:" + ex.getLocalizedMessage());
        }
    }

    private Object reportGenerateIngresos() throws JRException {
        Object objectMovimiento = CajaADO.ReporteGeneralMovimientoCaja(
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                cbVendedorSelect.isSelected() ? 0 : 1,
                idVendedor);

        Object objectTransaccion = IngresoADO.ReporteGeneralIngresosEgresos(
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                cbVendedorSelect.isSelected() ? 0 : 1,
                idVendedor);

        if (objectMovimiento instanceof ArrayList && objectTransaccion instanceof ArrayList) {

            ArrayList<IngresoTB> bsMovimiento = (ArrayList<IngresoTB>) objectMovimiento;
            ArrayList<IngresoTB> bsTransaccion = (ArrayList<IngresoTB>) objectTransaccion;

            if (cbTransacciones.isSelected()) {
                if (bsTransaccion.isEmpty()) {
                    return "No hay datos para mostrar.";
                }
            }

            if (cbMovientoCaja.isSelected()) {
                if (bsMovimiento.isEmpty()) {
                    return "No hay datos para mostrar.";
                }
            }

            if (!cbTransacciones.isSelected() && !cbMovientoCaja.isSelected()) {
                return "Debe seleccionar las transacciones y/o movimientos de caja.";
            }

            double totalEfectivoIngreso = 0;
            double totalEfectivoSalida = 0;

            double totalTarjetaIngreso = 0;
            double totalTarjetaSalida = 0;

            double totalDepositoIngreso = 0;
            double totalDepositoSalida = 0;

            ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();
            int count = 0;

            if (cbTransacciones.isSelected()) {
                for (IngresoTB bt : bsTransaccion) {
                    count++;
                    bt.setId(count);
                    ingresoTBs.add(bt);
                }

                for (IngresoTB ingresoTB : bsTransaccion) {
                    if (ingresoTB.getFormaIngreso().equalsIgnoreCase("EFECTIVO")) {
                        if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {
                            totalEfectivoIngreso += 0;
                            totalEfectivoSalida += ingresoTB.getEfectivo();
                        } else {
                            totalEfectivoIngreso += ingresoTB.getEfectivo();
                            totalEfectivoSalida += 0;
                        }
                    } else if (ingresoTB.getFormaIngreso().equalsIgnoreCase("TARJETA")) {
                        if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {
                            totalTarjetaIngreso += 0;
                            totalTarjetaSalida += ingresoTB.getTarjeta();
                        } else {
                            totalTarjetaIngreso += ingresoTB.getTarjeta();
                            totalTarjetaSalida += 0;
                        }
                    } else {
                        if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {
                            totalDepositoIngreso += 0;
                            totalDepositoSalida += ingresoTB.getDeposito();
                        } else {
                            totalDepositoIngreso += ingresoTB.getDeposito();
                            totalDepositoSalida += 0;
                        }
                    }
                }
            }

            if (cbMovientoCaja.isSelected()) {
                for (IngresoTB bt : bsMovimiento) {
                    count++;
                    bt.setId(count);
                    ingresoTBs.add(bt);
                }

                for (IngresoTB ingresoTB : bsMovimiento) {
                    if (ingresoTB.getFormaIngreso().equalsIgnoreCase("EFECTIVO")) {

                        if (ingresoTB.getTransaccion().equalsIgnoreCase("APERTURA DE CAJA")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("VENTAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("INGRESOS")) {
                            totalEfectivoSalida += 0;
                            totalEfectivoIngreso += ingresoTB.getEfectivo();
                        } else {
                            totalEfectivoIngreso += 0;
                            totalEfectivoSalida += ingresoTB.getEfectivo();
                        }

                    } else if (ingresoTB.getFormaIngreso().equalsIgnoreCase("TARJETA")) {

                        if (ingresoTB.getTransaccion().equalsIgnoreCase("APERTURA DE CAJA")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("VENTAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("INGRESOS")) {
                            totalTarjetaSalida += 0;
                            totalTarjetaIngreso += ingresoTB.getTarjeta();
                        } else {
                            totalTarjetaIngreso += 0;
                            totalTarjetaSalida += ingresoTB.getTarjeta();
                        }

                    } else {

                        if (ingresoTB.getTransaccion().equalsIgnoreCase("APERTURA DE CAJA")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("VENTAS")
                                || ingresoTB.getTransaccion().equalsIgnoreCase("INGRESOS")) {
                            totalDepositoSalida += 0;
                            totalDepositoIngreso += ingresoTB.getDeposito();
                        } else {
                            totalDepositoIngreso += 0;
                            totalDepositoSalida += ingresoTB.getDeposito();
                        }

                    }
                }
            }

            InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
            InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
            if (Session.COMPANY_IMAGE != null) {
                imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
            }
            InputStream dir = getClass().getResourceAsStream("/report/Ingresos.jasper");
            Map map = new HashMap();
            map.put("LOGO", imgInputStream);
            map.put("ICON", imgInputStreamIcon);
            map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
            map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C: ", Session.COMPANY_NUMERO_DOCUMENTO));
            map.put("DIRECCION", Session.COMPANY_DOMICILIO);
            map.put("TELEFONOCELULAR", "TELÉFONO: " + Session.COMPANY_TELEFONO + " CELULAR: " + Session.COMPANY_CELULAR);
            map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);
            map.put("FECHAS", "DEL " + Tools.formatDate(Tools.getDatePicker(dpFechaInicial)) + " AL " + Tools.formatDate(Tools.getDatePicker(dpFechaFinal)));
            map.put("INGRESO_EFECTIVO", Tools.roundingValue(totalEfectivoIngreso, 2));
            map.put("SALIDA_EFECTIVO", Tools.roundingValue(totalEfectivoSalida, 2));
            map.put("INGRESO_TARJETA", Tools.roundingValue(totalTarjetaIngreso, 2));
            map.put("SALIDA_TARJETA", Tools.roundingValue(totalTarjetaSalida, 2));
            map.put("INGRESO_DEPOSITO", Tools.roundingValue(totalDepositoIngreso, 2));
            map.put("SALIDA_DEPOSITO", Tools.roundingValue(totalDepositoSalida, 2));

            return JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(ingresoTBs));
        } else {
            return (String) objectTransaccion + " " + (String) objectMovimiento;
        }
    }

    private void openViewVisualizarIngresos() {
        if (!cbVendedorSelect.isSelected() && idVendedor.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos", "Ingrese un vendedor para generar el reporte.");
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
                    return reportGenerateIngresos();
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
                        controller.setFileName("REPORTE GENERAL DE MOVIMIENTO DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Movimientos");
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

    private void onEventPdfIngresos() {
        if (!cbVendedorSelect.isSelected() && idVendedor.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos", "Ingrese un vendedor para generar el reporte.");
            btnVendedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Movimientos");
            fileChooser.setInitialFileName("REPORTE DE MOVIMIENTO DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Documento", Arrays.asList("*.pdf", "*.PDF"))
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("pdf") || file.getName().endsWith("PDF")) {
                    onEventPdfIngresos(file);
                }
            }
        }
    }

    private void onEventPdfIngresos(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                try {
                    Object object = reportGenerateIngresos();
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

    private void onEventExcelIngresos() {
        if (!cbVendedorSelect.isSelected() && idVendedor.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos", "Ingrese un vendedor para generar el reporte.");
            btnVendedor.requestFocus();
        } else {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Ingresos");
            fileChooser.setInitialFileName("REPORTE DE MOVIMIENTO DEL " + Tools.getDatePicker(dpFechaInicial) + " AL " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                    onEventExcelIngresos(file);
                }
            }
        }
    }

    private void onEventExcelIngresos(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() throws InterruptedException {
                try {
                    Object objectMovimiento = CajaADO.ReporteGeneralMovimientoCaja(
                            Tools.getDatePicker(dpFechaInicial),
                            Tools.getDatePicker(dpFechaFinal),
                            cbVendedorSelect.isSelected() ? 0 : 1,
                            idVendedor);

                    Object objectTransaccion = IngresoADO.ReporteGeneralIngresosEgresos(
                            Tools.getDatePicker(dpFechaInicial),
                            Tools.getDatePicker(dpFechaFinal),
                            cbVendedorSelect.isSelected() ? 0 : 1,
                            idVendedor);

                    if (objectMovimiento instanceof ArrayList && objectTransaccion instanceof ArrayList) {

                        ArrayList<IngresoTB> bsMovimiento = (ArrayList<IngresoTB>) objectMovimiento;
                        ArrayList<IngresoTB> bsTransaccion = (ArrayList<IngresoTB>) objectTransaccion;

                        int count = 0;
                        ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();
                        if (cbTransacciones.isSelected()) {
                            for (IngresoTB bt : bsTransaccion) {
                                count++;
                                bt.setId(count);
                                ingresoTBs.add(bt);
                            }
                        }

                        if (cbMovientoCaja.isSelected()) {
                            for (IngresoTB bt : bsMovimiento) {
                                count++;
                                bt.setId(count);
                                ingresoTBs.add(bt);
                            }
                        }

                        Workbook workbook;
                        if (file.getName().endsWith("xls")) {
                            workbook = new HSSFWorkbook();
                        } else {
                            workbook = new XSSFWorkbook();
                        }

                        Sheet sheet = workbook.createSheet("Ingresos y Egresos");

                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        font.setColor(HSSFColor.BLACK.index);

                        CellStyle cellStyleHeader = workbook.createCellStyle();
                        cellStyleHeader.setFont(font);
                        cellStyleHeader.setFillForegroundColor(IndexedColors.AUTOMATIC.getIndex());
                        cellStyleHeader.setAlignment(CellStyle.ALIGN_CENTER);
                        cellStyleHeader.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

                        Row row = sheet.createRow(0);

                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
                        Cell cellId = row.createCell(0);
                        cellId.setCellStyle(cellStyleHeader);
                        cellId.setCellValue("Id");

                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
                        Cell cellDetalle = row.createCell(1);
                        cellDetalle.setCellStyle(cellStyleHeader);
                        cellDetalle.setCellValue("Transacción");

                        sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
                        Cell cellCantidad = row.createCell(2);
                        cellCantidad.setCellStyle(cellStyleHeader);
                        cellCantidad.setCellValue("Cantidad");

                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 3, 4));
                        Cell cellEfectivo = row.createCell(3);
                        cellEfectivo.setCellStyle(cellStyleHeader);
                        cellEfectivo.setCellValue("Efectivo");

                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 6));
                        Cell cellTarjeta = row.createCell(5);
                        cellTarjeta.setCellStyle(cellStyleHeader);
                        cellTarjeta.setCellValue("Tarjeta");

                        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 8));
                        Cell cellDeposito = row.createCell(7);
                        cellDeposito.setCellStyle(cellStyleHeader);
                        cellDeposito.setCellValue("Deposito");

                        sheet.autoSizeColumn(0);
                        sheet.autoSizeColumn(1);
                        sheet.autoSizeColumn(2);

                        row = sheet.createRow(1);

                        Cell cellIngreso1 = row.createCell(3);
                        cellIngreso1.setCellStyle(cellStyleHeader);
                        cellIngreso1.setCellValue("Ingreso");

                        Cell cellSalida1 = row.createCell(4);
                        cellSalida1.setCellStyle(cellStyleHeader);
                        cellSalida1.setCellValue("Salida");

                        Cell cellIngreso2 = row.createCell(5);
                        cellIngreso2.setCellStyle(cellStyleHeader);
                        cellIngreso2.setCellValue("Ingreso");

                        Cell cellSalida2 = row.createCell(6);
                        cellSalida2.setCellStyle(cellStyleHeader);
                        cellSalida2.setCellValue("Salida");

                        Cell cellIngreso3 = row.createCell(7);
                        cellIngreso3.setCellStyle(cellStyleHeader);
                        cellIngreso3.setCellValue("Ingreso");

                        Cell cellSalida3 = row.createCell(8);
                        cellSalida3.setCellStyle(cellStyleHeader);
                        cellSalida3.setCellValue("Salida");

                        int i = 0;
                        CellStyle cellStyle;
                        double totalEIngreso = 0;
                        double totalESalida = 0;

                        double totalTIngreso = 0;
                        double totalTSalida = 0;

                        double totalDIngreso = 0;
                        double totalDSalida = 0;

                        for (IngresoTB ingresoTB : ingresoTBs) {
                            row = sheet.createRow(i + 2);
                            cellStyle = workbook.createCellStyle();

                            Cell cell0 = row.createCell(0);
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
                            cell0.setCellStyle(cellStyle);
                            cell0.setCellValue(ingresoTB.getId());
                            cell0.setCellType(Cell.CELL_TYPE_NUMERIC);

                            Cell cell1 = row.createCell(1);
                            cell1.setCellStyle(cellStyle);
                            cell1.setCellValue(ingresoTB.getTransaccion());
                            cell1.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(1);

                            Cell cell2 = row.createCell(2);
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
                            cell2.setCellStyle(cellStyle);
                            cell2.setCellValue(ingresoTB.getCantidad());
                            cell2.setCellType(Cell.CELL_TYPE_NUMERIC);

                            if (ingresoTB.getFormaIngreso().equalsIgnoreCase("EFECTIVO")) {
                                if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                        || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {
                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(0);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(ingresoTB.getEfectivo());
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(0);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(0);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(0);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(0);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalESalida += ingresoTB.getEfectivo();
                                } else {
                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(ingresoTB.getEfectivo());
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(0);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(0);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(0);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(0);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(0);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalEIngreso += ingresoTB.getEfectivo();
                                }
                            } else if (ingresoTB.getFormaIngreso().equalsIgnoreCase("TARJETA")) {
                                if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                        || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {

                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(0);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(0);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(0);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(ingresoTB.getTarjeta());
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(0);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(0);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalTSalida += ingresoTB.getTarjeta();
                                } else {
                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(0);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(0);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(ingresoTB.getTarjeta());
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(0);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(0);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(0);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalTIngreso += ingresoTB.getTarjeta();
                                }
                            } else {
                                if (ingresoTB.getTransaccion().equalsIgnoreCase("COMPRAS")
                                        || ingresoTB.getTransaccion().equalsIgnoreCase("EGRESOS")) {

                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(0);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(0);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(0);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(0);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(0);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(ingresoTB.getDeposito());
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalDSalida += ingresoTB.getDeposito();
                                } else {
                                    Cell cell3 = row.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellValue(0);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell3.getColumnIndex());

                                    Cell cell4 = row.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellValue(0);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell4.getColumnIndex());

                                    Cell cell5 = row.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellValue(0);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell5.getColumnIndex());

                                    Cell cell6 = row.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellValue(0);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell6.getColumnIndex());

                                    Cell cell7 = row.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellValue(ingresoTB.getDeposito());
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell7.getColumnIndex());

                                    Cell cell8 = row.createCell(8);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellValue(0);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    sheet.autoSizeColumn(cell8.getColumnIndex());

                                    totalDIngreso += ingresoTB.getDeposito();
                                }
                            }
                            i++;
                        }

                        row = sheet.createRow(i + 2);

                        CellStyle cellStyleTotales = workbook.createCellStyle();
                        cellStyleTotales.setDataFormat(workbook.createDataFormat().getFormat("0.00"));

                        Cell cellTotalEIngreso = row.createCell(3);
                        cellTotalEIngreso.setCellStyle(cellStyleTotales);
                        cellTotalEIngreso.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalEIngreso.setCellValue(totalEIngreso);

                        Cell cellTotalESalida = row.createCell(4);
                        cellTotalESalida.setCellStyle(cellStyleTotales);
                        cellTotalESalida.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalESalida.setCellValue(totalESalida);

                        Cell cellTotalTIngreso = row.createCell(5);
                        cellTotalTIngreso.setCellStyle(cellStyleTotales);
                        cellTotalTIngreso.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalTIngreso.setCellValue(totalTIngreso);

                        Cell cellTotalTSalida = row.createCell(6);
                        cellTotalTSalida.setCellStyle(cellStyleTotales);
                        cellTotalTSalida.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalTSalida.setCellValue(totalTSalida);

                        Cell cellTotalDIngreso = row.createCell(7);
                        cellTotalDIngreso.setCellStyle(cellStyleTotales);
                        cellTotalDIngreso.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalDIngreso.setCellValue(totalDIngreso);

                        Cell cellTotalDSalida = row.createCell(8);
                        cellTotalDSalida.setCellStyle(cellStyleTotales);
                        cellTotalDSalida.setCellType(Cell.CELL_TYPE_NUMERIC);
                        cellTotalDSalida.setCellValue(totalDSalida);

                        try (FileOutputStream out = new FileOutputStream(file)) {
                            workbook.write(out);
                        }
                        workbook.close();
                        return "successful";
                    } else {
                        return "";
                    }
                } catch (IOException ex) {
                    return "Error al exportar el archivo, intente de nuevo.";
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
            openViewVisualizarIngresos();
        }
    }

    @FXML
    private void onActionVisualizar(ActionEvent event) {
        openViewVisualizarIngresos();
    }

    @FXML
    private void onKeyPressedPdf(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPdfIngresos();
        }
    }

    @FXML
    private void onActionPdf(ActionEvent event) {
        onEventPdfIngresos();
    }

    @FXML
    private void onKeyPressedExcel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExcelIngresos();
        }
    }

    @FXML
    private void onActionExcel(ActionEvent event) {
        onEventExcelIngresos();
    }

    @FXML
    private void onActionVendedor(ActionEvent event) {
        openWindowVendedores();
    }

    @FXML
    private void onKeyPressedVendedor(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVendedores();
        }
    }

    @FXML
    private void onActionVendedoresSelect(ActionEvent event) {
        if (cbVendedorSelect.isSelected()) {
            btnVendedor.setDisable(true);
            txtVendedores.setText("");
            idVendedor = "";
        } else {
            btnVendedor.setDisable(false);
        }
    }

    public void setLoadDataEmpleado(String idVendedor, String datos) {
        this.idVendedor = idVendedor;
        txtVendedores.setText(datos);
    }

    public VBox getVbWindow() {
        return vbWindow;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
