package controller.inventario.valorinventario;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AlmacenTB;
import model.DetalleTB;
import model.SuministroTB;
import service.AlmacenADO;
import service.DetalleADO;
import service.SuministroADO;

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

public class FxValorInventarioController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private Label lblLoad;
    @FXML
    private TextField txtProducto;
    @FXML
    private ComboBox<String> cbExistencia;
    @FXML
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, Integer> tcNumero;
    @FXML
    private TableColumn<SuministroTB, String> tcDescripcion;
    @FXML
    private TableColumn<SuministroTB, Label> tcExistencia;
    @FXML
    private TableColumn<SuministroTB, String> tcStock;
    @FXML
    private TableColumn<SuministroTB, String> tcCategoria;
    @FXML
    private TableColumn<SuministroTB, String> tcMarca;
    @FXML
    private TableColumn<SuministroTB, String> cbInventario;
    @FXML
    private TableColumn<SuministroTB, String> cbEstado;
    @FXML
    private Label lblValoTotal;
    @FXML
    private TextField txtNameProduct;
    @FXML
    private ComboBox<DetalleTB> cbCategoria;
    @FXML
    private ComboBox<DetalleTB> cbMarca;
    @FXML
    private Label lblPaginaActual;
    @FXML
    private Label lblPaginaSiguiente;
    @FXML
    private Label lblCantidadTotal;
    @FXML
    private ComboBox<AlmacenTB> cbAlmacen;

    private FxPrincipalController fxPrincipalController;

    private AnchorPane apEncabezado;

    private AnchorPane apDetalleCabecera;

    private AnchorPane apPie;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    private SearchComboBox<String> searchComboBoxExistencias;

    private SearchComboBox<DetalleTB> searchComboBoxCategoria;

    private SearchComboBox<DetalleTB> searchComboBoxMarca;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    private int paginacion;

    private int totalPaginacion;

    private short opcion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketInventario(vbWindow);

        tcNumero.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tcDescripcion.setCellValueFactory(cellData -> Bindings.concat(
                cellData.getValue().getClave() + (cellData.getValue().getClaveAlterna().isEmpty() ? "" : " - ") + cellData.getValue().getClaveAlterna()
                + "\n" + cellData.getValue().getNombreMarca()));
        tcCategoria.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getCategoriaName()));
        tcMarca.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getMarcaName()));
        tcExistencia.setCellValueFactory(new PropertyValueFactory<>("lblCantidad"));
        tcStock.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getStockMinimo(), 2) + " - " + Tools.roundingValue(cellData.getValue().getStockMaximo(), 2)));
        cbInventario.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().isInventario() ? "SI" : "NO"));
        cbEstado.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.04));
        tcDescripcion.prefWidthProperty().bind(tvList.widthProperty().multiply(0.24));
        tcCategoria.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcMarca.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcExistencia.prefWidthProperty().bind(tvList.widthProperty().multiply(0.16));
        tcStock.prefWidthProperty().bind(tvList.widthProperty().multiply(0.13));
        cbInventario.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        cbEstado.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        paginacion = 1;
        filtercbCategoria();
        loadInit();
    }

    private void loadInit() {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
            if (!lblLoad.isVisible()) {
                paginacion = 1;
                fillInventarioTable("", 0, "", 0, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                opcion = 0;
            }
        }
    }

    private void fillInventarioTable(String producto, int tipoExistencia, String nameProduct, int opcion, int categoria, int marca, int idAlmacen) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return SuministroADO.ListInventario(producto, tipoExistencia, nameProduct, opcion, categoria, marca, idAlmacen, (paginacion - 1) * 20, 20);
            }
        };
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof Object[]) {
                Object[] objects = (Object[]) object;
                ObservableList<SuministroTB> stbs = (ObservableList<SuministroTB>) objects[0];
                if (!stbs.isEmpty()) {
                    tvList.setItems(stbs);
                    double total = 0;
//                    total = stbs.stream().map(v -> v.getImporteNeto()).reduce(total, (accumulator, _item) -> accumulator + _item);
                    lblValoTotal.setText(Session.MONEDA_SIMBOLO + Tools.roundingValue(total, 2));

                    int integer = (int) (Math.ceil((double) (((Integer) objects[1]) / 20.00)));
                    totalPaginacion = integer;
                    lblPaginaActual.setText(paginacion + "");
                    lblPaginaSiguiente.setText(totalPaginacion + "");

                    lblCantidadTotal.setText(tvList.getItems().size() + "");
                } else {
                    tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));
                    lblPaginaActual.setText("0");
                    lblPaginaSiguiente.setText("0");
                    lblCantidadTotal.setText("0");
                    lblValoTotal.setText(Session.MONEDA_SIMBOLO + Tools.roundingValue(0, 2));
                }
            } else {
                tvList.setPlaceholder(Tools.placeHolderTableView((String) object, "-fx-text-fill:#a70820;", false));
            }
            lblLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
            tvList.setPlaceholder(Tools.placeHolderTableView(task.getException().getLocalizedMessage(), "-fx-text-fill:#a70820;", false));
        });
        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
            tvList.getItems().clear();
            tvList.setPlaceholder(Tools.placeHolderTableView("Cargando información...", "-fx-text-fill:#020203;", true));
            totalPaginacion = 0;
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void filtercbCategoria() {
        searchComboBoxAlmacen = new SearchComboBox<>(cbAlmacen, true);
        searchComboBoxAlmacen.setFilter((item, text) -> item.getNombre().toLowerCase().contains(text.toLowerCase()));
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxAlmacen.getComboBox().hide();
            }
        });
        searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxAlmacen.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxAlmacen.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxAlmacen.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxAlmacen.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxAlmacen.getComboBox().hide();
                }
            }
        });
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }

//        
        searchComboBoxExistencias = new SearchComboBox<>(cbExistencia, true);
        searchComboBoxExistencias.setFilter((item, text) -> item.toLowerCase().contains(text.toLowerCase()));
        searchComboBoxExistencias.getComboBox().getItems().addAll("Todas las Cantidades", "Cantidades negativas", "Cantidades intermedias", "Cantidades necesaria", "Cantidades excedentes");
        searchComboBoxExistencias.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxExistencias.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxExistencias.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                            if (!lblLoad.isVisible()) {
                                paginacion = 1;
                                fillInventarioTable("", searchComboBoxExistencias.getComboBox().getSelectionModel().getSelectedIndex(), "", 3, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                                opcion = 3;
                            }
                        }
                        searchComboBoxExistencias.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxExistencias.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxExistencias.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxExistencias.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxExistencias.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxExistencias.getSearchComboBoxSkin().isClickSelection()) {
                    if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                        if (!lblLoad.isVisible()) {
                            paginacion = 1;
                            fillInventarioTable("", searchComboBoxExistencias.getComboBox().getSelectionModel().getSelectedIndex(), "", (short) 3, 0, 0, cbAlmacen.getSelectionModel().getSelectedIndex() == -1 ? 0 : cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                            opcion = 3;
                        }
                    }
                    searchComboBoxExistencias.getComboBox().hide();
                }
            }
        });

//        
        searchComboBoxCategoria = new SearchComboBox<>(cbCategoria, true);
        searchComboBoxCategoria.setFilter((item, text) -> item.getNombre().toLowerCase().contains(text.toLowerCase()));
        searchComboBoxCategoria.getComboBox().getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0006"));
        searchComboBoxCategoria.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxCategoria.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxCategoria.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                            if (!lblLoad.isVisible()) {
                                paginacion = 1;
                                fillInventarioTable("", 0, "", 4, ((DetalleTB) searchComboBoxCategoria.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), 0, cbAlmacen.getSelectionModel().getSelectedIndex() == -1 ? 0 : cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                                opcion = 4;
                            }
                        }
                        searchComboBoxCategoria.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxCategoria.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxCategoria.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });

        searchComboBoxCategoria.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxCategoria.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxCategoria.getSearchComboBoxSkin().isClickSelection()) {
                    if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                        if (!lblLoad.isVisible()) {
                            paginacion = 1;
                            fillInventarioTable("", 0, "", 4, ((DetalleTB) searchComboBoxCategoria.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), 0, cbAlmacen.getSelectionModel().getSelectedIndex() == -1 ? 0 : cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                            opcion = 4;
                        }
                    }
                    searchComboBoxCategoria.getComboBox().hide();
                }
            }
        });

//        
        searchComboBoxMarca = new SearchComboBox<>(cbMarca, true);
        searchComboBoxMarca.setFilter((item, text) -> item.getNombre().toLowerCase().contains(text.toLowerCase()));
        searchComboBoxMarca.getComboBox().getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0007"));
        searchComboBoxMarca.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxMarca.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxMarca.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                            if (!lblLoad.isVisible()) {
                                paginacion = 1;
                                fillInventarioTable("", 0, "", 5, 0, ((DetalleTB) searchComboBoxMarca.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), cbAlmacen.getSelectionModel().getSelectedIndex() == -1 ? 0 : cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                                opcion = 5;
                            }
                        }
                        searchComboBoxMarca.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxMarca.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxMarca.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });

        searchComboBoxMarca.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxMarca.getComboBox().getSelectionModel().select(item);
                // ocultar popup cuando el item fue seleccionado mediante un click
                if (searchComboBoxMarca.getSearchComboBoxSkin().isClickSelection()) {
                    if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                        if (!lblLoad.isVisible()) {
                            paginacion = 1;
                            fillInventarioTable("", 0, "", 5, 0, ((DetalleTB) searchComboBoxMarca.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), cbAlmacen.getSelectionModel().getSelectedIndex() == -1 ? 0 : cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                            opcion = 5;
                        }
                    }
                    searchComboBoxMarca.getComboBox().hide();
                }
            }
        });
    }

    private void openWindowAjuste() {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            try {
                fxPrincipalController.openFondoModal();
                URL url = getClass().getResource(FilesRouters.FX_INVENTARIO_AJUSTE);
                FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                Parent parent = fXMLLoader.load(url.openStream());
                //Controlller here
                FxInventarioAjusteController controller = fXMLLoader.getController();
                controller.setInitValorInventarioController(this);
                controller.setLoadComponents(tvList.getSelectionModel().getSelectedItem());
                //
                Stage stage = WindowStage.StageLoaderModal(parent, "Inventario general", vbWindow.getScene().getWindow());
                stage.setResizable(false);
                stage.sizeToScene();
                stage.setOnHiding((w) -> fxPrincipalController.closeFondoModal());
                stage.show();
            } catch (IOException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        }
    }

    public void onEventPaginacion() {
        switch (opcion) {
            case 0:
                fillInventarioTable("", 0, "", 0, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
            case 1:
                fillInventarioTable(txtProducto.getText().trim(), 0, "", 1, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
            case 2:
                fillInventarioTable("", 0, txtNameProduct.getText().trim(), 2, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
            case 3:
                fillInventarioTable("", searchComboBoxExistencias.getComboBox().getSelectionModel().getSelectedIndex(), "", 3, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
            case 4:
                fillInventarioTable("", 0, "", 4, ((DetalleTB) searchComboBoxCategoria.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
            default:
                fillInventarioTable("", 0, "", 5, 0, ((DetalleTB) searchComboBoxMarca.getComboBox().getSelectionModel().getSelectedItem()).getIdDetalle(), cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                break;
        }
    }

    private void onEventReload() {
        searchComboBoxAlmacen.getComboBox().getItems().clear();
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());
        if (!searchComboBoxAlmacen.getComboBox().getItems().isEmpty()) {
            searchComboBoxAlmacen.getComboBox().getSelectionModel().select(0);
        }

        searchComboBoxCategoria.getComboBox().getItems().clear();
        searchComboBoxCategoria.getComboBox().getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0006"));

        searchComboBoxMarca.getComboBox().getItems().clear();
        searchComboBoxMarca.getComboBox().getItems().addAll(DetalleADO.obtenerDetallePorIdMantenimiento("0007"));
        loadInit();
    }

    private void onEventExcel(File file, int idAlmacen) {
        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<String> task = new Task<String>() {
            @Override
            public String call() throws InterruptedException {
                Object object = SuministroADO.GetReporteGeneralInventario(idAlmacen, 0, 0, 0, 0, 0);
                if (object instanceof ArrayList) {
                    try {
                        ArrayList<SuministroTB> empList = (ArrayList<SuministroTB>) object;
                        Workbook workbook;
                        if (file.getName().endsWith("xls")) {
                            workbook = new HSSFWorkbook();
                        } else {
                            workbook = new XSSFWorkbook();
                        }
                        Sheet sheet = workbook.createSheet("Productos");

                        Font font = workbook.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        font.setColor(HSSFColor.BLACK.index);

                        CellStyle cellStyleHeader = workbook.createCellStyle();
                        cellStyleHeader.setFont(font);
                        cellStyleHeader.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                        String header[] = {"ID", "CLAVE", "CLAVE ALTERNA", "DESCRIPCIÓN", "COSTO", "PRECIO", "CANTIDAD", "MEDIDA", "STOCK MINIMO", "STOCK MAXIMO", "CATEGORIA", "MARCA"};

                        Row headerRow = sheet.createRow(0);
                        for (int i = 0; i < header.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellStyle(cellStyleHeader);
                            cell.setCellValue(header[i].toUpperCase());
                        }

                        CellStyle cellStyle = workbook.createCellStyle();
                        for (int i = 0; i < empList.size(); i++) {

                            Font fontBlack = workbook.createFont();
                            fontBlack.setColor(HSSFColor.BLACK.index);
                            fontBlack.setBold(false);
                            cellStyle.setFont(fontBlack);

                            Row row = sheet.createRow(i + 1);

                            Cell cell1 = row.createCell(0);
                            cell1.setCellStyle(cellStyle);
                            cell1.setCellType(Cell.CELL_TYPE_STRING);
                            cell1.setCellValue("" + String.valueOf(i + 1));
                            sheet.autoSizeColumn(cell1.getColumnIndex());

                            Cell cell2 = row.createCell(1);
                            cell2.setCellStyle(cellStyle);
                            cell2.setCellValue(String.valueOf(empList.get(i).getClave()));
                            cell2.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell2.getColumnIndex());

                            Cell cell3 = row.createCell(2);
                            cell3.setCellStyle(cellStyle);
                            cell3.setCellValue(String.valueOf(empList.get(i).getClaveAlterna()));
                            cell3.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell3.getColumnIndex());

                            Cell cell4 = row.createCell(3);
                            cell4.setCellStyle(cellStyle);
                            cell4.setCellValue(String.valueOf(empList.get(i).getNombreMarca()));
                            cell4.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell4.getColumnIndex());

                            Cell cell5 = row.createCell(4);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell5.setCellStyle(cellStyle);
                            cell5.setCellValue(Double.parseDouble(Tools.roundingValue(empList.get(i).getCostoCompra(), 2)));
                            cell5.setCellType(Cell.CELL_TYPE_NUMERIC);
                            sheet.autoSizeColumn(cell5.getColumnIndex());

                            Cell cell6 = row.createCell(5);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell6.setCellStyle(cellStyle);
                            cell6.setCellValue(String.valueOf(empList.get(i).getPrecioVentaGeneral()));
                            cell6.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell6.getColumnIndex());

                            Cell cell7 = row.createCell(6);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell7.setCellStyle(cellStyle);
                            cell7.setCellValue(String.valueOf(empList.get(i).getCantidad()));
                            cell7.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell7.getColumnIndex());

                            Cell cell8 = row.createCell(7);
                            cellStyle = workbook.createCellStyle();
                            cell8.setCellStyle(cellStyle);
                            cell8.setCellValue(String.valueOf(empList.get(i).getUnidadCompra()));
                            cell8.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell8.getColumnIndex());

                            Cell cell9 = row.createCell(8);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell9.setCellStyle(cellStyle);
                            cell9.setCellValue(String.valueOf(empList.get(i).getStockMinimo()));
                            cell5.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell5.getColumnIndex());

                            Cell cell10 = row.createCell(9);
                            cellStyle = workbook.createCellStyle();
                            cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                            cell10.setCellStyle(cellStyle);
                            cell10.setCellValue(String.valueOf(empList.get(i).getStockMaximo()));
                            cell10.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell10.getColumnIndex());

                            Cell cell11 = row.createCell(10);
                            cellStyle = workbook.createCellStyle();
                            cell11.setCellStyle(cellStyle);
                            cell11.setCellValue(String.valueOf(empList.get(i).getCategoriaName()));
                            cell11.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell11.getColumnIndex());

                            Cell cell12 = row.createCell(11);
                            cellStyle = workbook.createCellStyle();
                            cell12.setCellStyle(cellStyle);
                            cell12.setCellValue(String.valueOf(empList.get(i).getMarcaName()));
                            cell12.setCellType(Cell.CELL_TYPE_STRING);
                            sheet.autoSizeColumn(cell12.getColumnIndex());
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
                        Tools.newLineString("Se completo la creación del excel correctamente."),
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
                    Tools.newLineString("Se está generando el excel de productos."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventGenerateExcel() {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Lista de Productos");
            fileChooser.setInitialFileName("Inventario General - " + cbAlmacen.getSelectionModel().getSelectedItem().getNombre());
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
                    onEventExcel(file, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                }
            }
        }
    }

    @FXML
    private void onKeyReleasedProducto(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE
                && event.getCode() != KeyCode.F1
                && event.getCode() != KeyCode.F2
                && event.getCode() != KeyCode.F3
                && event.getCode() != KeyCode.F4
                && event.getCode() != KeyCode.F5
                && event.getCode() != KeyCode.F6
                && event.getCode() != KeyCode.F7
                && event.getCode() != KeyCode.F8
                && event.getCode() != KeyCode.F9
                && event.getCode() != KeyCode.F10
                && event.getCode() != KeyCode.F11
                && event.getCode() != KeyCode.F12
                && event.getCode() != KeyCode.ALT
                && event.getCode() != KeyCode.CONTROL
                && event.getCode() != KeyCode.UP
                && event.getCode() != KeyCode.DOWN
                && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.LEFT
                && event.getCode() != KeyCode.TAB
                && event.getCode() != KeyCode.CAPS
                && event.getCode() != KeyCode.SHIFT
                && event.getCode() != KeyCode.HOME
                && event.getCode() != KeyCode.WINDOWS
                && event.getCode() != KeyCode.ALT_GRAPH
                && event.getCode() != KeyCode.CONTEXT_MENU
                && event.getCode() != KeyCode.END
                && event.getCode() != KeyCode.INSERT
                && event.getCode() != KeyCode.PAGE_UP
                && event.getCode() != KeyCode.PAGE_DOWN
                && event.getCode() != KeyCode.NUM_LOCK
                && event.getCode() != KeyCode.PRINTSCREEN
                && event.getCode() != KeyCode.SCROLL_LOCK
                && event.getCode() != KeyCode.PAUSE) {
            if (event.getCode() == KeyCode.ENTER) {
                if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                    if (!lblLoad.isVisible()) {
                        paginacion = 1;
                        fillInventarioTable(txtProducto.getText().trim(), 0, "", 1, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                        opcion = 1;
                    }
                }
            }
        }
    }

    @FXML
    private void onKeyPressedAjuste(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAjuste();
        }
    }

    @FXML
    private void onActionAjuste(ActionEvent event) {
        openWindowAjuste();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReload();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        onEventReload();
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un almacen para continuar.");
            return;
        }

        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "No hay productos en la lista para continuar.");
            return;
        }

        fxOpcionesImprimirController.getTicketInventario().reporteInventarioActual(cbAlmacen.getSelectionModel().getSelectedItem().getNombre(), tvList.getItems());
    }

    @FXML
    private void onKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un almacen para continuar.");
                return;
            }

            if (tvList.getItems().isEmpty()) {
                Tools.AlertMessageWarning(vbWindow, "Inventario", "No hay productos en la lista para continuar.");
                return;
            }

            fxOpcionesImprimirController.getTicketInventario().reporteInventarioActual(cbAlmacen.getSelectionModel().getSelectedItem().getNombre(), tvList.getItems());
            event.consume();
        }
    }

    @FXML
    private void onActionReptAjuste(ActionEvent event) {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un almacen para continuar.");
            return;
        }

        if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "No hay productos en la lista para continuar.");
            return;
        }

        fxOpcionesImprimirController.getTicketInventario().reporteInventarioAjuste(cbAlmacen.getSelectionModel().getSelectedItem().getNombre(), tvList.getItems());
    }

    @FXML
    private void onKeyPressedReptAjuste(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (cbAlmacen.getSelectionModel().getSelectedIndex() < 0) {
                Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un almacen para continuar.");
                return;
            }

            if (tvList.getItems().isEmpty()) {
                Tools.AlertMessageWarning(vbWindow, "Inventario", "No hay productos en la lista para continuar.");
                return;
            }

            fxOpcionesImprimirController.getTicketInventario().reporteInventarioAjuste(cbAlmacen.getSelectionModel().getSelectedItem().getNombre(), tvList.getItems());
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedExcel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGenerateExcel();
        }
    }

    @FXML
    private void onActionExcel(ActionEvent event) {
        onEventGenerateExcel();
    }

    @FXML
    private void onKeyReleasedNameProduct(KeyEvent event) {
        if (event.getCode() != KeyCode.ESCAPE
                && event.getCode() != KeyCode.F1
                && event.getCode() != KeyCode.F2
                && event.getCode() != KeyCode.F3
                && event.getCode() != KeyCode.F4
                && event.getCode() != KeyCode.F5
                && event.getCode() != KeyCode.F6
                && event.getCode() != KeyCode.F7
                && event.getCode() != KeyCode.F8
                && event.getCode() != KeyCode.F9
                && event.getCode() != KeyCode.F10
                && event.getCode() != KeyCode.F11
                && event.getCode() != KeyCode.F12
                && event.getCode() != KeyCode.ALT
                && event.getCode() != KeyCode.CONTROL
                && event.getCode() != KeyCode.UP
                && event.getCode() != KeyCode.DOWN
                && event.getCode() != KeyCode.RIGHT
                && event.getCode() != KeyCode.LEFT
                && event.getCode() != KeyCode.TAB
                && event.getCode() != KeyCode.CAPS
                && event.getCode() != KeyCode.SHIFT
                && event.getCode() != KeyCode.HOME
                && event.getCode() != KeyCode.WINDOWS
                && event.getCode() != KeyCode.ALT_GRAPH
                && event.getCode() != KeyCode.CONTEXT_MENU
                && event.getCode() != KeyCode.END
                && event.getCode() != KeyCode.INSERT
                && event.getCode() != KeyCode.PAGE_UP
                && event.getCode() != KeyCode.PAGE_DOWN
                && event.getCode() != KeyCode.NUM_LOCK
                && event.getCode() != KeyCode.PRINTSCREEN
                && event.getCode() != KeyCode.SCROLL_LOCK
                && event.getCode() != KeyCode.PAUSE) {
            if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
                if (!lblLoad.isVisible()) {
                    paginacion = 1;
                    fillInventarioTable("", 0, txtNameProduct.getText().trim(), 2, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
                    opcion = 2;
                }
            }
        }
    }

    @FXML
    private void onKeyPressedAnterior(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion > 1) {
                    paginacion--;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionAnterior(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion > 1) {
                paginacion--;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                if (paginacion < totalPaginacion) {
                    paginacion++;
                    onEventPaginacion();
                }
            }
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            if (paginacion < totalPaginacion) {
                paginacion++;
                onEventPaginacion();
            }
        }
    }

    @FXML
    private void onActionAlmacen(ActionEvent event) {
        if (cbAlmacen.getSelectionModel().getSelectedIndex() >= 0) {
            fillInventarioTable("", 0, "", 0, 0, 0, cbAlmacen.getSelectionModel().getSelectedItem().getIdAlmacen());
        }
    }

    public TableView<SuministroTB> getTvList() {
        return tvList;
    }

    public TableColumn<SuministroTB, Label> getTcExistencia() {
        return tcExistencia;
    }

    public SearchComboBox<String> getSearchComboBoxExistencias() {
        return searchComboBoxExistencias;
    }

    public AnchorPane getApEncabezado() {
        return apEncabezado;
    }

    public AnchorPane getApDetalleCabecera() {
        return apDetalleCabecera;
    }

    public AnchorPane getApPie() {
        return apPie;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
