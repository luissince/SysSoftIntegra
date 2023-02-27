package controller.reporte;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.DetalleTB;
import model.Utilidad;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import service.DetalleADO;
import service.UtilidadADO;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxVentaUtilidadesController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private DatePicker dpFechaInicial;
    @FXML
    private DatePicker dpFechaFinal;
    @FXML
    private CheckBox cbProductosSeleccionar;
    @FXML
    private TextField txtProducto;
    @FXML
    private Button btnProductos;
    @FXML
    private CheckBox cbMostrarProducto;
    @FXML
    private CheckBox cbCategoriaSeleccionar;
    @FXML
    private CheckBox cbMarcaSeleccionar;
    @FXML
    private CheckBox cbPresentacionSeleccionar;
    @FXML
    private ComboBox<DetalleTB> cbCategorias;
    @FXML
    private ComboBox<DetalleTB> cbMarcas;
    @FXML
    private ComboBox<DetalleTB> cbPresentacion;

    private FxPrincipalController fxPrincipalController;

    private String idSuministro;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFechaInicial);
        Tools.actualDate(Tools.getDate(), dpFechaFinal);
        cbCategorias.getItems().addAll(DetalleADO.GetDetailId("0006"));
        cbMarcas.getItems().addAll(DetalleADO.GetDetailId("0007"));
        cbPresentacion.getItems().addAll(DetalleADO.GetDetailId("0008"));
        //0013
        idSuministro = "";
    }

    private boolean validateDuplicateArticulo(ArrayList<Utilidad> view, Utilidad utilidad) {
        boolean ret = false;
        for (int i = 0; i < view.size(); i++) {
            if (view.get(i).getIdSuministro().equals(utilidad.getIdSuministro())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void openWindowSuministros() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_SUMINISTROS_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxSuministrosListaController controller = fXMLLoader.getController();
            controller.setInitVentaUtilidadesController(this);
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
        Object object = UtilidadADO.listUtilidadVenta(
                Tools.getDatePicker(dpFechaInicial),
                Tools.getDatePicker(dpFechaFinal),
                idSuministro,
                cbCategoriaSeleccionar.isSelected() ? 0 : cbCategorias.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbMarcaSeleccionar.isSelected() ? 0 : cbMarcas.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbPresentacionSeleccionar.isSelected() ? 0 : cbPresentacion.getSelectionModel().getSelectedItem().getIdDetalle());

        if (object instanceof ArrayList) {
            ArrayList<Utilidad> detalle_list = (ArrayList<Utilidad>) object;

            if (detalle_list.isEmpty()) {
                return "No hay registros para mostrar en el reporte.";
            }

            double costoTotal = 0;
            double precioTotal = 0;
            double utilidadGenerada = 0;
            for (int i = 0; i < detalle_list.size(); i++) {
                if (validateDuplicateArticulo(detalle_list, detalle_list.get(i))) {
                    costoTotal += detalle_list.get(i).getCostoVentaTotal();
                    precioTotal += detalle_list.get(i).getPrecioVentaTotal();
                    utilidadGenerada += detalle_list.get(i).getUtilidad();
                } else {
                    costoTotal = detalle_list.get(i).getCostoVentaTotal();
                    precioTotal = detalle_list.get(i).getPrecioVentaTotal();
                    utilidadGenerada = detalle_list.get(i).getUtilidad();
                }
            }

            ArrayList<Utilidad> list = new ArrayList<>();
            int count = 0;

            for (int i = 0; i < detalle_list.size(); i++) {
                if (validateDuplicateArticulo(list, detalle_list.get(i))) {
                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j).getIdSuministro().equalsIgnoreCase(detalle_list.get(i).getIdSuministro())) {
                            Utilidad newUtilidad = list.get(j);
                            newUtilidad.setCantidad(newUtilidad.getCantidad() + detalle_list.get(i).getCantidad());

                            newUtilidad.setCostoVenta(newUtilidad.getCostoVenta() + detalle_list.get(i).getCostoVenta());
                            newUtilidad.setCostoVentaTotal(newUtilidad.getCostoVentaTotal() + detalle_list.get(i).getCostoVentaTotal());

                            newUtilidad.setPrecioVenta(newUtilidad.getPrecioVenta() + detalle_list.get(i).getPrecioVenta());
                            newUtilidad.setPrecioVentaTotal(newUtilidad.getPrecioVentaTotal() + detalle_list.get(i).getPrecioVentaTotal());

                            newUtilidad.setUtilidad(newUtilidad.getUtilidad() + detalle_list.get(i).getUtilidad());
                        }
                    }
                } else {
                    count++;
                    Utilidad newUtilidad = new Utilidad();
                    newUtilidad.setId(count);
                    newUtilidad.setIdSuministro(detalle_list.get(i).getIdSuministro());
                    newUtilidad.setClave(detalle_list.get(i).getClave());
                    newUtilidad.setNombreMarca(detalle_list.get(i).getNombreMarca());
                    newUtilidad.setCantidad(detalle_list.get(i).getCantidad());

                    newUtilidad.setCostoVenta(detalle_list.get(i).getCostoVenta());
                    newUtilidad.setCostoVentaTotal(detalle_list.get(i).getCostoVentaTotal());

                    newUtilidad.setPrecioVenta(detalle_list.get(i).getPrecioVenta());
                    newUtilidad.setPrecioVentaTotal(detalle_list.get(i).getPrecioVentaTotal());

                    newUtilidad.setUtilidad(detalle_list.get(i).getUtilidad());

                    newUtilidad.setValorInventario(detalle_list.get(i).isValorInventario());
                    newUtilidad.setMedida(detalle_list.get(i).getMedida());
                    newUtilidad.setSimboloMoneda(detalle_list.get(i).getSimboloMoneda());
                    list.add(newUtilidad);
                }
            }

            List<Utilidad> newList = list.stream().map((Utilidad uti) -> {
                if (uti.getCantidad() > 0) {
                    uti.setCostoVenta(uti.getCostoVentaTotal() / uti.getCantidad());
                    uti.setPrecioVenta(uti.getPrecioVentaTotal() / uti.getCantidad());
                }
                return uti;
            }).collect(Collectors.toList());

            InputStream dir = getClass().getResourceAsStream("/report/Utilidad.jasper");

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(dir);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("RANGO_FECHA", dpFechaInicial.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")) + " - " + dpFechaFinal.getValue().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
            map.put("PRODUCTOS", cbProductosSeleccionar.isSelected() ? "TODOS" : txtProducto.getText());
            map.put("CATEGORIA", cbCategoriaSeleccionar.isSelected() ? "TODOS" : cbCategorias.getSelectionModel().getSelectedItem().getNombre());
            map.put("MARCA", cbMarcaSeleccionar.isSelected() ? "TODOS" : cbMarcas.getSelectionModel().getSelectedItem().getNombre());
            map.put("PRESENTACION", cbPresentacionSeleccionar.isSelected() ? "TODOS" : cbPresentacion.getSelectionModel().getSelectedItem().getNombre());
            map.put("ORDEN", "");
            map.put("COSTO_TOTAL", Tools.roundingValue(costoTotal, 2));
            map.put("PRECIO_TOTAL", Tools.roundingValue(precioTotal, 2));
            map.put("UTILIDAD_GENERADA", Tools.roundingValue(utilidadGenerada, 2));

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JRBeanCollectionDataSource(cbMostrarProducto.isSelected() ? detalle_list : newList));
            return jasperPrint;
        } else {
            return (String) object;
        }
    }

    private void openViewReporteGeneral() {
        if (!cbProductosSeleccionar.isSelected() && idSuministro.equalsIgnoreCase("") && txtProducto.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Ingrese un producto para generar el reporte.");
            btnProductos.requestFocus();
        } else if (!cbCategoriaSeleccionar.isSelected() && cbCategorias.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una categoría para generar el reporte.");
            cbCategorias.requestFocus();
        } else if (!cbMarcaSeleccionar.isSelected() && cbMarcas.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una marca para generar el reporte.");
            cbMarcas.requestFocus();
        } else if (!cbPresentacionSeleccionar.isSelected() && cbPresentacion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una presentación para generar el reporte.");
            cbPresentacion.requestFocus();
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
                                Tools.newLineString("Se completo la creación del reporte correctamente."),
                                Duration.seconds(10),
                                Pos.BOTTOM_RIGHT);

                        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
                        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                        Parent parent = fXMLLoader.load(url.openStream());
                        //Controlller here
                        FxReportViewController controller = fXMLLoader.getController();
                        controller.setFileName("Reporte de Utilidad al " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Utilidad");
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
                        Tools.newLineString("Se está generando el reporte de la utilidad."),
                        Duration.seconds(5),
                        Pos.BOTTOM_RIGHT);
            });
            exec.execute(task);
            if (!exec.isShutdown()) {
                exec.shutdown();
            }

        }
    }

    private void onEventExcel() {
        if (!cbProductosSeleccionar.isSelected() && idSuministro.equalsIgnoreCase("") && txtProducto.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Ingrese un producto para generar el reporte.");
            btnProductos.requestFocus();
        } else if (!cbCategoriaSeleccionar.isSelected() && cbCategorias.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una categoría para generar el reporte.");
            cbCategorias.requestFocus();
        } else if (!cbMarcaSeleccionar.isSelected() && cbMarcas.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una marca para generar el reporte.");
            cbMarcas.requestFocus();
        } else if (!cbPresentacionSeleccionar.isSelected() && cbPresentacion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Utilidades", "Seleccione una presentación para generar el reporte.");
            cbPresentacion.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Utilidad del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.setInitialFileName("Lista de Utilidad del " + Tools.getDatePicker(dpFechaInicial) + " al " + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xlsx")) {
                    onEvenExcel(file);
                } else {
                    Tools.AlertMessageWarning(vbWindow, "Exportar", "Elija un formato valido");
                }

            }
        }
    }

    private void onEvenExcel(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() {
                Object object = UtilidadADO.listUtilidadVenta(
                        Tools.getDatePicker(dpFechaInicial),
                        Tools.getDatePicker(dpFechaFinal),
                        idSuministro,
                        cbCategoriaSeleccionar.isSelected() ? 0 : cbCategorias.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbMarcaSeleccionar.isSelected() ? 0 : cbMarcas.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbPresentacionSeleccionar.isSelected() ? 0 : cbPresentacion.getSelectionModel().getSelectedItem().getIdDetalle());

                if (object instanceof ArrayList) {

                    ArrayList<Utilidad> detalle_list = (ArrayList<Utilidad>) object;

                    if (detalle_list.isEmpty()) {
                        return "No hay registros para mostrar en el reporte.";
                    }

                    double costoTotal = 0;
                    double precioTotal = 0;
                    double utilidadGenerada = 0;
                    for (int i = 0; i < detalle_list.size(); i++) {
                        if (validateDuplicateArticulo(detalle_list, detalle_list.get(i))) {
                            costoTotal += detalle_list.get(i).getCostoVentaTotal();
                            precioTotal += detalle_list.get(i).getPrecioVentaTotal();
                            utilidadGenerada += detalle_list.get(i).getUtilidad();
                        } else {
                            costoTotal = detalle_list.get(i).getCostoVentaTotal();
                            precioTotal = detalle_list.get(i).getPrecioVentaTotal();
                            utilidadGenerada = detalle_list.get(i).getUtilidad();
                        }
                    }

                    ArrayList<Utilidad> list = new ArrayList<>();
                    int count = 0;

                    for (int i = 0; i < detalle_list.size(); i++) {
                        if (validateDuplicateArticulo(list, detalle_list.get(i))) {
                            for (int j = 0; j < list.size(); j++) {
                                if (list.get(j).getIdSuministro().equalsIgnoreCase(detalle_list.get(i).getIdSuministro())) {
                                    Utilidad newUtilidad = list.get(j);
                                    newUtilidad.setCantidad(newUtilidad.getCantidad() + detalle_list.get(i).getCantidad());

                                    newUtilidad.setCostoVenta(newUtilidad.getCostoVenta() + detalle_list.get(i).getCostoVenta());
                                    newUtilidad.setCostoVentaTotal(newUtilidad.getCostoVentaTotal() + detalle_list.get(i).getCostoVentaTotal());

                                    newUtilidad.setPrecioVenta(newUtilidad.getPrecioVenta() + detalle_list.get(i).getPrecioVenta());
                                    newUtilidad.setPrecioVentaTotal(newUtilidad.getPrecioVentaTotal() + detalle_list.get(i).getPrecioVentaTotal());

                                    newUtilidad.setUtilidad(newUtilidad.getUtilidad() + detalle_list.get(i).getUtilidad());
                                }
                            }
                        } else {
                            count++;
                            Utilidad newUtilidad = new Utilidad();
                            newUtilidad.setId(count);
                            newUtilidad.setIdSuministro(detalle_list.get(i).getIdSuministro());
                            newUtilidad.setClave(detalle_list.get(i).getClave());
                            newUtilidad.setNombreMarca(detalle_list.get(i).getNombreMarca());
                            newUtilidad.setCantidad(detalle_list.get(i).getCantidad());

                            newUtilidad.setCostoVenta(detalle_list.get(i).getCostoVenta());
                            newUtilidad.setCostoVentaTotal(detalle_list.get(i).getCostoVentaTotal());

                            newUtilidad.setPrecioVenta(detalle_list.get(i).getPrecioVenta());
                            newUtilidad.setPrecioVentaTotal(detalle_list.get(i).getPrecioVentaTotal());

                            newUtilidad.setUtilidad(detalle_list.get(i).getUtilidad());

                            newUtilidad.setValorInventario(detalle_list.get(i).isValorInventario());
                            newUtilidad.setMedida(detalle_list.get(i).getMedida());
                            newUtilidad.setSimboloMoneda(detalle_list.get(i).getSimboloMoneda());
                            list.add(newUtilidad);
                        }
                    }

                    List<Utilidad> newList = list.stream().map((Utilidad uti) -> {
                        if (uti.getCantidad() > 0) {
                            uti.setCostoVenta(uti.getCostoVentaTotal() / uti.getCantidad());
                            uti.setPrecioVenta(uti.getPrecioVentaTotal() / uti.getCantidad());
                        }
                        return uti;
                    }).collect(Collectors.toList());

                    try {

                        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                            XSSFSheet sheet = workbook.createSheet("Utilidad");

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
                            cellTitle.setCellValue("Lista de Utilidad");
                            cellTitle.setCellStyle(cellStyleTitle);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
                            //-----------------------------------------------------------------------------------

                            XSSFCellStyle cellStyleDate = workbook.createCellStyle();
                            cellStyleDate.setFillForegroundColor(new XSSFColor(new java.awt.Color(128, 128, 128)));
                            cellStyleDate.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleDate.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            cellStyleDate.setFont(fontTitle);

                            XSSFRow dateRow = sheet.createRow(1);

                            cellTitle = dateRow.createCell(5);
                            cellTitle.setCellValue("FECHA");
                            cellTitle.setCellStyle(cellStyleDate);

                            cellTitle = dateRow.createCell(6);
                            cellTitle.setCellValue(Tools.getDatePicker(dpFechaInicial));
                            cellTitle.setCellStyle(cellStyleDate);

                            cellTitle = dateRow.createCell(7);
                            cellTitle.setCellValue(Tools.getDatePicker(dpFechaFinal));
                            cellTitle.setCellStyle(cellStyleDate);

                            sheet.autoSizeColumn(5);
                            sheet.autoSizeColumn(6);
                            sheet.autoSizeColumn(7);

                            //-------------------------------------------------------------------------
                            Font fontHeader = workbook.createFont();
                            fontHeader.setFontHeightInPoints((short) 12);
                            fontHeader.setBold(true);
                            fontHeader.setColor(HSSFColor.BLACK.index);

                            XSSFCellStyle cellStyleHeader = workbook.createCellStyle();
                            cellStyleHeader.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleHeader.setFont(fontHeader);

                            String header[] = {"#", "DESCRIPCIÓN", "CANTIDAD", "COSTO", "COSTO TOTAL", "PRECIO", "PRECIO TOTAL", "UTILIDAD"};
                            Row headerRow = sheet.createRow(2);
                            for (int i = 0; i < header.length; i++) {
                                Cell cell = headerRow.createCell(i);
                                cell.setCellStyle(cellStyleHeader);
                                cell.setCellValue(header[i].toUpperCase());
                            }

                            sheet.autoSizeColumn(0);
                            sheet.autoSizeColumn(1);
                            sheet.autoSizeColumn(2);
                            sheet.autoSizeColumn(3);
                            sheet.autoSizeColumn(4);
                            sheet.autoSizeColumn(5);
                            sheet.autoSizeColumn(6);
                            sheet.autoSizeColumn(7);

                            CellStyle cellStyle = workbook.createCellStyle();
                            int row = 2;
                            if (cbMostrarProducto.isSelected()) {
                                for (Utilidad uti : detalle_list) {
                                    row++;
                                    Row detailRow = sheet.createRow(row);

                                    Cell cell1 = detailRow.createCell(0);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
                                    cell1.setCellStyle(cellStyle);
                                    cell1.setCellType(Cell.CELL_TYPE_STRING);
                                    cell1.setCellValue(uti.getId());

                                    Cell cell2 = detailRow.createCell(1);
                                    cell2.setCellStyle(cellStyle);
                                    cell2.setCellType(Cell.CELL_TYPE_STRING);
                                    cell2.setCellValue(uti.getNombreMarca());

                                    Cell cell3 = detailRow.createCell(2);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell3.setCellValue(uti.getCantidad());

                                    Cell cell4 = detailRow.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell4.setCellValue(uti.getCostoVenta());

                                    Cell cell5 = detailRow.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell5.setCellValue(uti.getCostoVentaTotal());

                                    Cell cell6 = detailRow.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell6.setCellValue(uti.getPrecioVenta());

                                    Cell cell7 = detailRow.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell7.setCellValue(uti.getPrecioVentaTotal());

                                    Cell cell8 = detailRow.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell8.setCellValue(uti.getUtilidad());

                                }
                            } else {
                                for (Utilidad uti : newList) {
                                    row++;
                                    Row detailRow = sheet.createRow(row);

                                    Cell cell1 = detailRow.createCell(0);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));
                                    cell1.setCellStyle(cellStyle);
                                    cell1.setCellType(Cell.CELL_TYPE_STRING);
                                    cell1.setCellValue(uti.getId());

                                    Cell cell2 = detailRow.createCell(1);
                                    cell2.setCellStyle(cellStyle);
                                    cell2.setCellType(Cell.CELL_TYPE_STRING);
                                    cell2.setCellValue(uti.getNombreMarca());

                                    Cell cell3 = detailRow.createCell(2);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell3.setCellStyle(cellStyle);
                                    cell3.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell3.setCellValue(uti.getCantidad());

                                    Cell cell4 = detailRow.createCell(3);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell4.setCellStyle(cellStyle);
                                    cell4.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell4.setCellValue(uti.getCostoVenta());

                                    Cell cell5 = detailRow.createCell(4);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell5.setCellStyle(cellStyle);
                                    cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell5.setCellValue(uti.getCostoVentaTotal());

                                    Cell cell6 = detailRow.createCell(5);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell6.setCellStyle(cellStyle);
                                    cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell6.setCellValue(uti.getPrecioVenta());

                                    Cell cell7 = detailRow.createCell(6);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell7.setCellStyle(cellStyle);
                                    cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell7.setCellValue(uti.getPrecioVentaTotal());

                                    Cell cell8 = detailRow.createCell(7);
                                    cellStyle = workbook.createCellStyle();
                                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                                    cell8.setCellStyle(cellStyle);
                                    cell8.setCellType(Cell.CELL_TYPE_NUMERIC);
                                    cell8.setCellValue(uti.getUtilidad());

                                }
                            }

                            row++;
                            Row totalesRow = sheet.createRow(row + 1);
                            Cell cell2 = totalesRow.createCell(4);
                            cell2.setCellStyle(cellStyle);
                            cell2.setCellType(Cell.CELL_TYPE_STRING);
                            cell2.setCellValue("COSTO TOTAL");

                            Cell cell3 = totalesRow.createCell(6);
                            cell3.setCellStyle(cellStyle);
                            cell3.setCellType(Cell.CELL_TYPE_STRING);
                            cell3.setCellValue("PRECIO TOTAL");

                            Cell cell4 = totalesRow.createCell(7);
                            cell4.setCellStyle(cellStyle);
                            cell4.setCellType(Cell.CELL_TYPE_STRING);
                            cell4.setCellValue("UTILIDAD TOTAL");

                            sheet.autoSizeColumn(4);
                            sheet.autoSizeColumn(6);
                            sheet.autoSizeColumn(7);

                            totalesRow = sheet.createRow(row + 2);
                            Cell cell5 = totalesRow.createCell(4);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell5.setCellStyle(cellStyle);
                            cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell5.setCellValue(costoTotal);

                            Cell cell6 = totalesRow.createCell(6);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell6.setCellStyle(cellStyle);
                            cell6.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell6.setCellValue(precioTotal);

                            Cell cell7 = totalesRow.createCell(7);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell7.setCellStyle(cellStyle);
                            cell7.setCellType(Cell.CELL_TYPE_NUMERIC);
                            cell7.setCellValue(utilidadGenerada);

                            try (FileOutputStream out = new FileOutputStream(file)) {
                                workbook.write(out);
                            }
                            workbook.close();

                            return "successful";
                        }
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
                    Tools.newLineString("Se está generando el excel de utilidad."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    @FXML
    private void onKeyPressedReporteGeneral(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openViewReporteGeneral();
            event.consume();
        }
    }

    @FXML
    private void onActionReporteGeneral(ActionEvent event) {
        openViewReporteGeneral();
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
    private void onActionCbProductosSeleccionar(ActionEvent event) {
        if (cbProductosSeleccionar.isSelected()) {
            btnProductos.setDisable(true);
            idSuministro = "";
            txtProducto.setText("");
        } else {
            btnProductos.setDisable(false);
        }
    }

    @FXML
    private void onActionCbCategoriaSeleccionar(ActionEvent event) {
        if (cbCategoriaSeleccionar.isSelected()) {
            cbCategorias.setDisable(true);
            cbCategorias.getSelectionModel().select(null);
        } else {
            cbCategorias.setDisable(false);
        }
    }

    @FXML
    private void onActionCbMarcaSeleccionar(ActionEvent event) {
        if (cbMarcaSeleccionar.isSelected()) {
            cbMarcas.setDisable(true);
            cbMarcas.getSelectionModel().select(null);
        } else {
            cbMarcas.setDisable(false);
        }
    }

    @FXML
    private void onActionCbPresentacionSeleccionar(ActionEvent event) {
        if (cbPresentacionSeleccionar.isSelected()) {
            cbPresentacion.setDisable(true);
            cbPresentacion.getSelectionModel().select(null);
        } else {
            cbPresentacion.setDisable(false);
        }
    }

    @FXML
    private void onKeyPressedProductos(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowSuministros();
        }
    }

    @FXML
    private void onActionProductos(ActionEvent event) {
        openWindowSuministros();
    }

    public void setIdSuministro(String idSuministro) {
        this.idSuministro = idSuministro;
    }

    public TextField getTxtProducto() {
        return txtProducto;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
