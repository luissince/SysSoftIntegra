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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import model.IngresoTB;
import model.SuministroTB;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.data.JsonDataSource;
import service.CajaADO;
import service.IngresoADO;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    private Object reportGenerateIngresos() {
        try {
            File archivoc = new File("./report/Ingresos.jasper");
            InputStream dir = new FileInputStream(archivoc.getPath());

            JSONArray array = new JSONArray();
            for (int i = 0; i < 10; i++) {
                JSONObject jsono = new JSONObject();
                jsono.put("id", i);

                array.add(jsono);
            }
            String json = new String(array.toJSONString().getBytes(), "UTF-8");
            ByteArrayInputStream jsonDataStream = new ByteArrayInputStream(json.getBytes());

            InputStream imgInputStreamIcon = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
            InputStream imgInputStream = getClass().getResourceAsStream(FilesRouters.IMAGE_LOGO);
            if (Session.COMPANY_IMAGE != null) {
                imgInputStream = new ByteArrayInputStream(Session.COMPANY_IMAGE);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("LOGO", imgInputStream);
            map.put("ICON", imgInputStreamIcon);
            map.put("EMPRESA", Session.COMPANY_RAZON_SOCIAL);
            map.put("DOCUMENTOEMPRESA", Tools.textShow("R.U.C: ", Session.COMPANY_NUMERO_DOCUMENTO));
            map.put("DIRECCION", Session.COMPANY_DOMICILIO);
            map.put("TELEFONOCELULAR",
                    "TELÉFONO: " + Session.COMPANY_TELEFONO + " CELULAR: " + Session.COMPANY_CELULAR);
            map.put("EMAIL", "EMAIL: " + Session.COMPANY_EMAIL);

            return JasperFillManager.fillReport(dir, map, new JsonDataSource(jsonDataStream));
        } catch (JRException ex) {
            return "Error en generar el pdf: " + ex.getLocalizedMessage();
        } catch (FileNotFoundException ex) {
            return "Error obtener la ruta del pdf: " + ex.getLocalizedMessage();
        } catch (UnsupportedEncodingException ex) {
            return "Error convertir los datos del array a json: " + ex.getLocalizedMessage();
        }
    }

    private void openViewVisualizarIngresos() {
        if (!cbVendedorSelect.isSelected() && idVendedor.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos",
                    "Ingrese un vendedor para generar el reporte.");
            btnVendedor.requestFocus();
            return;
        }

        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<JasperPrint> task = new Task<JasperPrint>() {
            @Override
            public JasperPrint call() throws Exception {
                Object result = reportGenerateIngresos();
                if (result instanceof JasperPrint) {
                    return (JasperPrint) result;
                }

                throw new Exception((String) result);
            }
        };

        task.setOnScheduled(w -> {
            Tools.println("setOnScheduled");
            Tools.showAlertNotification(
                    "/view/image/pdf.png",
                    "Generar Vista",
                    Tools.newLineString("Se está generando el reporte."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnFailed(w -> {
            Tools.println("setOnFailed");
            Tools.showAlertNotification(
                    "/view/image/error_large.png",
                    "Generar Vista",
                    Tools.newLineString(task.getException().getLocalizedMessage()),
                    Duration.seconds(10),
                    Pos.BOTTOM_RIGHT);
        });

        task.setOnSucceeded(w -> {
            Tools.println("setOnSucceeded");
            try {
                Tools.showAlertNotification(
                        "/view/image/information_large.png",
                        "Generar Vista",
                        Tools.newLineString("Se completo la creación del modal correctamente."),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);

                String fechaInicial = Tools.getDatePicker(dpFechaInicial);
                String fechaFinal = Tools.getDatePicker(dpFechaFinal);
                String titulo = "REPORTE FINANCIERO DEL " + fechaInicial + " AL " + fechaFinal;

                onEventMostrarPdf(task.getValue(), titulo);
            } catch (HeadlessException | IOException ex) {
                Tools.println("ingreso aca");
                Tools.showAlertNotification(
                        "/view/image/warning_large.png",
                        "Generar Vista",
                        Tools.newLineString(ex.getLocalizedMessage()),
                        Duration.seconds(10),
                        Pos.BOTTOM_RIGHT);
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventMostrarPdf(JasperPrint jasperPrint, String titulo) throws IOException {
        URL url = getClass().getResource(FilesRouters.FX_REPORTE_VIEW);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        // Controlller here
        FxReportViewController controller = fXMLLoader.getController();
        controller.setFileName(titulo);
        controller.setJasperPrint(jasperPrint);
        controller.show();
        Stage stage = WindowStage.StageLoader(parent, "Reporte Financiero");
        stage.setResizable(true);
        stage.show();
        stage.requestFocus();
    }

    private void onEventPdfIngresos() {
        if (!cbVendedorSelect.isSelected() && idVendedor.equalsIgnoreCase("") && txtVendedores.getText().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos",
                    "Ingrese un vendedor para generar el reporte.");
            btnVendedor.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Movimientos");
            fileChooser.setInitialFileName("REPORTE DE MOVIMIENTO DEL " + Tools.getDatePicker(dpFechaInicial) + " AL "
                    + Tools.getDatePicker(dpFechaFinal));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PDF Documento", Arrays.asList("*.pdf", "*.PDF")));
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
                        Tools.newLineString(
                                "Se completó la creación del pdf correctamente en la ruta " + file.getAbsolutePath()),
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
                    Tools.newLineString(
                            "Se produjo un problema en el momento de generar, intente nuevamente o comuníquese con su proveedor del sistema."),
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
            Tools.AlertMessageWarning(vbWindow, "Reporte General de Movimientos",
                    "Ingrese un vendedor para generar el reporte.");
            btnVendedor.requestFocus();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Reporte de Ingresos");
        fileChooser.setInitialFileName("REPORTE DE MOVIMIENTO DEL " + Tools.getDatePicker(dpFechaInicial) + " AL "
                + Tools.getDatePicker(dpFechaFinal));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls"));
        File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
        if (file != null) {
            file = new File(file.getAbsolutePath());
            if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                onEventExcelIngresos(file);
            }
        }
    }

    private void onEventExcelIngresos(File file) {

    }

    private void openWindowVendedores() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_EMPLEADO_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
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
