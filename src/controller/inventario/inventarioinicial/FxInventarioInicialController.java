package controller.inventario.inventarioinicial;

import controller.menus.FxPrincipalController;
import controller.tools.Tools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.PreciosTB;
import model.SuministroTB;
import service.SuministroADO;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FxInventarioInicialController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, String> tcId;
    @FXML
    private TableColumn<SuministroTB, String> tcClave;
    @FXML
    private TableColumn<SuministroTB, String> tcProducto;
    @FXML
    private TableColumn<SuministroTB, String> tcStockMinimo;
    @FXML
    private TableColumn<SuministroTB, String> tcStockMaximo;
    @FXML
    private TableColumn<SuministroTB, String> tcCantidad;
    @FXML
    private TableColumn<SuministroTB, String> tcPrecioCompra;
    @FXML
    private TableColumn<SuministroTB, String> tcPrecioVenta;
    @FXML
    private TableColumn<SuministroTB, String> tcMensaje;
    @FXML
    private VBox vbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblTotalLista;
    @FXML
    private Label lblTotalIngresados;
    @FXML
    private Label lblTotalErrores;

    private FxPrincipalController fxPrincipalController;

    private int count = 0;

    private File file;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcId.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcClave.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClave()));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNombreMarca()));
        tcStockMinimo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getStockMinimo()));
        tcStockMaximo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getStockMaximo()));
        tcCantidad.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCantidad()));
        tcPrecioCompra.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCostoCompra()));
        tcPrecioVenta.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getPrecioVentaGeneral()));
        tcMensaje.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMensaje()));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcClave.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.18));
        tcStockMinimo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcStockMaximo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcPrecioCompra.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcPrecioVenta.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcMensaje.prefWidthProperty().bind(tvList.widthProperty().multiply(0.14));

        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
    }

    private void fillArticlesTable() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList> task = new Task<ArrayList>() {
            @Override
            public ArrayList call() {
                ArrayList<Object> objects = new ArrayList<>();
                tvList.getItems().forEach((suministroTB) -> {
                    Object result = SuministroADO.CrudMasivoSuministro(suministroTB);
                    if (result instanceof SuministroTB) {
                        objects.add((SuministroTB) result);
                    } else {
                        String value = (String) result;
                        if (value.equalsIgnoreCase("registered")) {
                            objects.add("ok");
                        }
                    }
                });
                return objects;
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            int ingresados = 0;
            int errores = 0;
            ObservableList<SuministroTB> suministroTBs = FXCollections.observableArrayList();
            ArrayList<Object> objects = task.getValue();
            for (Object value : objects) {
                if (value instanceof SuministroTB) {
                    errores++;
                    suministroTBs.add((SuministroTB) value);
                } else {
                    String result = (String) value;
                    if (result.equalsIgnoreCase("ok")) {
                        ingresados++;
                    }
                }
            }
            limpiarVista();

            lblTotalIngresados.setText(ingresados + "");
            lblTotalErrores.setText(errores + "");

            tvList.setItems(suministroTBs);
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
        });

        task.setOnFailed((WorkerStateEvent event) -> {
            limpiarVista();
            vbBody.setDisable(false);
            hbLoad.setVisible(false);

        });

        task.setOnScheduled((WorkerStateEvent event) -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void limpiarVista() {
        lblTotalLista.setText("0");
        lblTotalIngresados.setText("0");
        lblTotalErrores.setText("0");
        tvList.getItems().clear();
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        count = 0;
    }

    private void onEventSaveData() {
        if (!tvList.getItems().isEmpty()) {
            fillArticlesTable();
        } else {
            Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No hay productos en la lista para subir.");
        }
    }

    private void onEventGenerarExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Inventario Inicial");
        fileChooser.setInitialFileName("Inventario Inicial del " + Tools.getDate("dd-MM-yyyy"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
        );
        File fileExcel = fileChooser.showSaveDialog(apWindow.getScene().getWindow());
        if (fileExcel != null) {
            fileExcel = new File(fileExcel.getAbsolutePath());
            if (fileExcel.getName().endsWith("xls") || fileExcel.getName().endsWith("xlsx")) {
                generateExcel(fileExcel);
            } else {
                Tools.AlertMessageWarning(apWindow, "Exportar", "Elija un formato valido");
            }
        }
    }

    private void generateExcel(File file) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            public String call() throws InterruptedException {
                try {
                    Workbook workbook;
                    if (file.getName().endsWith("xls")) {
                        workbook = new HSSFWorkbook();
                    } else {
                        workbook = new XSSFWorkbook();
                    }

                    Sheet sheet = workbook.createSheet("Inventario Inicial");

                    Font font = workbook.createFont();
                    font.setFontHeightInPoints((short) 12);
                    font.setBold(true);
                    font.setColor(HSSFColor.BLACK.index);

                    CellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.setFont(font);
                    cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                    String header[] = {"Clave", "Clave Alterna", "Producto", "Stock Mínimo", "Stock Máximo", "Cantidad", "Costo", "Precio General", "Precio 2", "Precio 3"};

                    Row headerRow = sheet.createRow(0);
                    for (int i = 0; i < header.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellStyle(cellStyle);
                        cell.setCellValue(header[i].toUpperCase());
                    }
                    for (int i = 0; i < header.length; i++) {
                        sheet.autoSizeColumn(i);
                    }

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        workbook.write(out);
                    }

                    workbook.close();
                    return "successful";
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
                        Tools.newLineString("Se completó la creación del excel correctamente en la ruta " + file.getAbsolutePath()),
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

    private void onEventeUploadExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        fileChooser.setTitle("Inventario Inicial");
        fileChooser.setInitialFileName("Inventario Inicial");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
        );
        file = fileChooser.showOpenDialog(apWindow.getScene().getWindow());
        if (file != null) {

            file = new File(file.getAbsolutePath());
            if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {

                Service<Object> service = new Service<Object>() {
                    @Override
                    protected Task<Object> createTask() {
                        return new Task<Object>() {
                            @Override
                            protected Object call() {
                                ObservableList<SuministroTB> listImportada = FXCollections.observableArrayList();
                                try (Workbook workbook = WorkbookFactory.create(file)) {
                                    Sheet sheet = workbook.getSheetAt(0);
                                    Iterator<Row> iterator = sheet.rowIterator();
                                    int indexRow = -1;

                                    while (iterator.hasNext()) {
                                        indexRow++;

                                        Row row = iterator.next();
                                        Cell cell1 = row.getCell(0);
                                        Cell cell2 = row.getCell(1);
                                        Cell cell3 = row.getCell(2);
                                        Cell cell4 = row.getCell(3);
                                        Cell cell5 = row.getCell(4);
                                        Cell cell6 = row.getCell(5);
                                        Cell cell7 = row.getCell(6);
                                        Cell cell8 = row.getCell(7);
                                        Cell cell9 = row.getCell(8);
                                        Cell cell10 = row.getCell(9);

                                        if (indexRow > 0) {
                                            try {
                                                count++;
                                                updateMessage("" + count);

                                                String clv = !Tools.isText(cell1.getStringCellValue()) ? cell1.getStringCellValue() : "cl-" + count;
                                                String clva = !Tools.isText(cell2.getStringCellValue()) ? cell2.getStringCellValue() : "cl-" + count;
                                                String nom = !Tools.isText(cell3.getStringCellValue()) ? cell3.getStringCellValue() : "Producto " + count;
                                                double smi = Tools.isNumeric(cell4.toString()) ? Double.parseDouble(cell4.toString()) : 0;
                                                double sma = Tools.isNumeric(cell5.toString()) ? Double.parseDouble(cell5.toString()) : 0;
                                                double can = Tools.isNumeric(cell6.toString()) ? Double.parseDouble(cell6.toString()) : 0;
                                                double cos = Tools.isNumeric(cell7.toString()) ? Double.parseDouble(cell7.toString()) : 0;
                                                double pre = Tools.isNumeric(cell8.toString()) ? Double.parseDouble(cell8.toString()) : 0;
                                                double pre1 = Tools.isNumeric(cell9.toString()) ? Double.parseDouble(cell9.toString()) : 0;
                                                double pre2 = Tools.isNumeric(cell10.toString()) ? Double.parseDouble(cell10.toString()) : 0;

                                                SuministroTB suministroTB = new SuministroTB();
                                                suministroTB.setId(count);

                                                suministroTB.setOrigen(1);
                                                suministroTB.setClave(clv);
                                                suministroTB.setClaveAlterna(clva);
                                                suministroTB.setNombreMarca(nom);
                                                suministroTB.setNombreGenerico("");

                                                suministroTB.setCategoria(24);
                                                suministroTB.setMarcar(0);
                                                suministroTB.setPresentacion(0);
                                                suministroTB.setUnidadCompra(58);
                                                suministroTB.setUnidadVenta(1);

                                                suministroTB.setEstado(1);
                                                suministroTB.setStockMinimo(smi);
                                                suministroTB.setStockMaximo(sma);
                                                suministroTB.setCantidad(can);
                                                suministroTB.setIdImpuesto(1);
                                                suministroTB.setTipoPrecio(true);

                                                suministroTB.setCostoCompra(cos);
                                                suministroTB.setPrecioVentaGeneral(pre);
                                                suministroTB.setLote(false);
                                                suministroTB.setInventario(true);
                                                suministroTB.setValorInventario((short) 1);

                                                suministroTB.setClaveSat("");
                                                suministroTB.setNuevaImagen(null);

                                                ArrayList<PreciosTB> arrayList = new ArrayList<>();
                                                arrayList.add(new PreciosTB(0, "Precio de Venta 1", pre1, 1));
                                                arrayList.add(new PreciosTB(0, "Precio de Venta 2", pre2, 1));

                                                suministroTB.setPreciosTBs(arrayList);

                                                listImportada.add(suministroTB);
                                            } catch (NumberFormatException ex) {

                                                break;
                                            }
                                        }
                                    }
                                    return listImportada;
                                } catch (EncryptedDocumentException | IOException | InvalidFormatException ex) {
                                    return ex.getLocalizedMessage();
                                }

                            }
                        };
                    }
                };

                service.setOnSucceeded((WorkerStateEvent e) -> {
                    Object object = service.getValue();
                    if (object instanceof ObservableList) {
                        tvList.setItems((ObservableList<SuministroTB>) service.getValue());
                    } else {
                        tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#020203;", false));

                    }
                    lblLoad.setVisible(false);
                    lblTotalLista.textProperty().unbind();
                    count = 0;
                });
                service.setOnFailed((WorkerStateEvent e) -> {
                    lblLoad.setVisible(false);
                    tvList.setPlaceholder(Tools.placeHolderTableView(service.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
                    lblTotalLista.textProperty().unbind();
                    count = 0;
                });

                service.setOnScheduled((WorkerStateEvent e) -> {
                    lblLoad.setVisible(true);
                    tvList.getItems().clear();
                    tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
                    lblTotalLista.textProperty().bind(service.messageProperty());
                    count = 0;
                });
                service.restart();

            } else {
                Tools.AlertMessageWarning(apWindow, "Importar", "Elija un formato valido.");
            }
        }
    }

    private void onEventRevertirCambios() {
        short value = Tools.AlertMessageConfirmation(apWindow, "Inventario Inicial", "¿Esta seguro de revertir los cambios, se borraran todos los productos ingresados iniciando un nuevo inventario.");
        if (value == 1) {
            String result = SuministroADO.DeletedInventarioInicial();
            if (result.equalsIgnoreCase("ok")) {
                Tools.AlertMessageInformation(apWindow, "Inventario Inicial", "Se completo correctamente el proceso.");
            } else if (result.equalsIgnoreCase("venta")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe ventas asociadas a los productos.");
            } else if (result.equalsIgnoreCase("compra")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe compras asociadas a los productos.");
            } else if (result.equalsIgnoreCase("cotizacion")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe cotizaciones asociadas a los productos.");
            } else if (result.equalsIgnoreCase("produccion")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe producciones asociadas a los productos.");
            } else if (result.equalsIgnoreCase("formula")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe formulas asociadas a los productos.");
            } else if (result.equalsIgnoreCase("movimiento_inventario")) {
                Tools.AlertMessageWarning(apWindow, "Inventario Inicial", "No pudo revertir el inventario porque existe movimiento asociadas a los productos.");
            } else {
                Tools.AlertMessageError(apWindow, "Inventario Inicial", result);
            }
        }
    }

    @FXML
    private void onKeyPressedIniciar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSaveData();
            event.consume();
        }
    }

    @FXML
    private void onActionIniciar(ActionEvent event) {
        onEventSaveData();
    }

    @FXML
    private void onKeyPressedGenerar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGenerarExcel();
            event.consume();
        }
    }

    @FXML
    private void onActionGenerar(ActionEvent event) {
        onEventGenerarExcel();
    }

    @FXML
    private void onKeyPressedSubir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventeUploadExcel();
            event.consume();
        }
    }

    @FXML
    private void onActionSubir(ActionEvent event) {
        onEventeUploadExcel();
    }

    @FXML
    private void onKeyPressedLimpiar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            limpiarVista();
        }
    }

    @FXML
    private void onActionLimpiar(ActionEvent event) {
        limpiarVista();
    }

    @FXML
    private void onKeyPressedRevertir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventRevertirCambios();
        }
    }

    @FXML
    private void onActionRevertir(ActionEvent event) {
        onEventRevertirCambios();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
