package controller.reporte;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
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
import java.util.List;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.AlmacenADO;
import model.AlmacenTB;
import model.DetalleADO;
import model.DetalleTB;
import model.SuministroADO;
import model.SuministroTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
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

public class FxInventarioReporteController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private ComboBox<AlmacenTB> cbInventario;
    @FXML
    private CheckBox cbSelectUnidadMedida;
    @FXML
    private CheckBox cbSelectCategoria;
    @FXML
    private CheckBox cbSelectMarca;
    @FXML
    private CheckBox cbSelectPresentacion;
    @FXML
    private CheckBox cbSelectExistencia;
    @FXML
    private ComboBox<DetalleTB> cbUnidadMedida;
    @FXML
    private ComboBox<DetalleTB> cbCategorias;
    @FXML
    private ComboBox<DetalleTB> cbMarcas;
    @FXML
    private ComboBox<DetalleTB> cbPresentaciones;
    @FXML
    private ComboBox<String> cbExistencia;

    private FxPrincipalController fxPrincipalController;

    private SearchComboBox<AlmacenTB> searchComboBoxAlmacen;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbExistencia.getItems().addAll("NEGATIVOS", "INTERMEDIAS", "NECESARIAS", "EXCEDENTES");
        filterCbAlmacen();
        filterCbUnidadMedida();
        filterCbCategoria();
        filterCbMarcas();
        filterCbPresentaciones();
    }

    private void filterCbAlmacen() {
        searchComboBoxAlmacen = new SearchComboBox<>(cbInventario, true);
        searchComboBoxAlmacen.setFilter((item, text) -> item.getNombre().toLowerCase().contains(text.toLowerCase()));
        searchComboBoxAlmacen.getComboBox().getItems().addAll(AlmacenADO.GetSearchComboBoxAlmacen());

        searchComboBoxAlmacen.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
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
    }

    private void filterCbUnidadMedida() {
        SearchComboBox<DetalleTB> sCbUnidadMedida = new SearchComboBox<>(cbUnidadMedida, false);

        sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!sCbUnidadMedida.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    sCbUnidadMedida.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    sCbUnidadMedida.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                sCbUnidadMedida.getComboBox().hide();
            }
        });

        sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().getText())) {
                sCbUnidadMedida.getComboBox().getItems().clear();
                sCbUnidadMedida.getComboBox().getItems().addAll(DetalleADO.Get_Detail_IdName("4", "0013", sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().getText().trim()));
            }
        });

        sCbUnidadMedida.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    sCbUnidadMedida.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    sCbUnidadMedida.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });

        sCbUnidadMedida.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                sCbUnidadMedida.getComboBox().getSelectionModel().select(item);
                if (sCbUnidadMedida.getSearchComboBoxSkin().isClickSelection()) {
                    sCbUnidadMedida.getComboBox().hide();
                }
            }
        });
    }

    private void filterCbCategoria() {
        SearchComboBox<DetalleTB> sCbCategoria = new SearchComboBox<>(cbCategorias, false);

        sCbCategoria.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!sCbCategoria.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    sCbCategoria.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    sCbCategoria.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                sCbCategoria.getComboBox().hide();
            }
        });

        sCbCategoria.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(sCbCategoria.getSearchComboBoxSkin().getSearchBox().getText())) {
                sCbCategoria.getComboBox().getItems().clear();
                sCbCategoria.getComboBox().getItems().addAll(DetalleADO.Get_Detail_IdName("4", "0006", sCbCategoria.getSearchComboBoxSkin().getSearchBox().getText().trim()));
            }
        });

        sCbCategoria.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    sCbCategoria.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    sCbCategoria.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    sCbCategoria.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });

        sCbCategoria.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                sCbCategoria.getComboBox().getSelectionModel().select(item);
                if (sCbCategoria.getSearchComboBoxSkin().isClickSelection()) {
                    sCbCategoria.getComboBox().hide();
                }
            }
        });
    }

    private void filterCbMarcas() {
        SearchComboBox<DetalleTB> sCbMarca = new SearchComboBox<>(cbMarcas, false);

        sCbMarca.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!sCbMarca.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    sCbMarca.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    sCbMarca.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                sCbMarca.getComboBox().hide();
            }
        });

        sCbMarca.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(sCbMarca.getSearchComboBoxSkin().getSearchBox().getText())) {
                sCbMarca.getComboBox().getItems().clear();
                sCbMarca.getComboBox().getItems().addAll(DetalleADO.Get_Detail_IdName("4", "0007", sCbMarca.getSearchComboBoxSkin().getSearchBox().getText().trim()));
            }
        });

        sCbMarca.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    sCbMarca.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    sCbMarca.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    sCbMarca.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });

        sCbMarca.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                sCbMarca.getComboBox().getSelectionModel().select(item);
                if (sCbMarca.getSearchComboBoxSkin().isClickSelection()) {
                    sCbMarca.getComboBox().hide();
                }
            }
        });
    }

    private void filterCbPresentaciones() {
        SearchComboBox<DetalleTB> sCbPresentacion = new SearchComboBox<>(cbPresentaciones, false);

        sCbPresentacion.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!sCbPresentacion.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    sCbPresentacion.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    sCbPresentacion.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                sCbPresentacion.getComboBox().hide();
            }
        });

        sCbPresentacion.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(sCbPresentacion.getSearchComboBoxSkin().getSearchBox().getText())) {
                sCbPresentacion.getComboBox().getItems().clear();
                sCbPresentacion.getComboBox().getItems().addAll(DetalleADO.Get_Detail_IdName("4", "0008", sCbPresentacion.getSearchComboBoxSkin().getSearchBox().getText().trim()));
            }
        });

        sCbPresentacion.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    sCbPresentacion.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    sCbPresentacion.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    sCbPresentacion.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });

        sCbPresentacion.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                sCbPresentacion.getComboBox().getSelectionModel().select(item);
                if (sCbPresentacion.getSearchComboBoxSkin().isClickSelection()) {
                    sCbPresentacion.getComboBox().hide();
                }
            }
        });
    }

    private Object reportGenerate() throws JRException {
        Object object = SuministroADO.GetReporteGeneralInventario(
                cbInventario.getSelectionModel().getSelectedItem().getIdAlmacen(),
                cbSelectUnidadMedida.isSelected() ? 0 : cbUnidadMedida.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbSelectCategoria.isSelected() ? 0 : cbCategorias.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbSelectMarca.isSelected() ? 0 : cbMarcas.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbSelectPresentacion.isSelected() ? 0 : cbPresentaciones.getSelectionModel().getSelectedItem().getIdDetalle(),
                cbSelectExistencia.isSelected() ? 0
                : cbExistencia.getSelectionModel().getSelectedIndex() == 0 ? 1
                : cbExistencia.getSelectionModel().getSelectedIndex() == 1 ? 2
                : cbExistencia.getSelectionModel().getSelectedIndex() == 2 ? 3
                : 4
        );
        if (object instanceof List) {

            ArrayList<SuministroTB> empList = (ArrayList<SuministroTB>) object;
            if (empList.isEmpty()) {
                return "No hay datos para mostrar.";
            }

            Map map = new HashMap();
            map.put("ALMACEN", cbInventario.getSelectionModel().getSelectedItem().getNombre());
            map.put("MEDIDA", cbSelectUnidadMedida.isSelected() ? "TODOS" : cbUnidadMedida.getSelectionModel().getSelectedItem().getNombre());
            map.put("CATEGORIA", cbSelectCategoria.isSelected() ? "TODOS" : cbCategorias.getSelectionModel().getSelectedItem().getNombre());
            map.put("MARCA", cbSelectMarca.isSelected() ? "TODOS" : cbMarcas.getSelectionModel().getSelectedItem().getNombre());
            map.put("PRESENTACION", cbSelectPresentacion.isSelected() ? "TODOS" : cbPresentaciones.getSelectionModel().getSelectedItem().getNombre());
            map.put("ESTADO", cbSelectExistencia.isSelected() ? "TODOS" : cbExistencia.getSelectionModel().getSelectedItem());
            map.put("TOTAL_REGISTRO", empList.size() + "");

            InputStream dir = getClass().getResourceAsStream("/report/Inventario.jasper");
            JasperPrint jasperPrint = JasperFillManager.fillReport(dir, map, new JRBeanCollectionDataSource(empList));
            return jasperPrint;
        } else {
            return (String) object;
        }
    }

    private void onEventVisualizar() {
        if (cbInventario.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un inventario.");
            cbInventario.requestFocus();
        } else if (!cbSelectUnidadMedida.isSelected() && cbUnidadMedida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una unidad de medida.");
            cbUnidadMedida.requestFocus();
        } else if (!cbSelectCategoria.isSelected() && cbCategorias.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una categoría.");
            cbCategorias.requestFocus();
        } else if (!cbSelectMarca.isSelected() && cbMarcas.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una marca.");
            cbMarcas.requestFocus();
        } else if (!cbSelectPresentacion.isSelected() && cbPresentaciones.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una presentación.");
            cbPresentaciones.requestFocus();
        } else if (!cbSelectExistencia.isSelected() && cbExistencia.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un tipo de existencia.");
            cbExistencia.requestFocus();
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
                        controller.setFileName("Lista del inventarios");
                        controller.setJasperPrint((JasperPrint) object);
                        controller.show();
                        Stage stage = WindowStage.StageLoader(parent, "Reporte General de Inventario");
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
                        Tools.newLineString("Se está generando el modal de inventario."),
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
        if (cbInventario.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un inventario.");
            cbInventario.requestFocus();
        } else if (!cbSelectUnidadMedida.isSelected() && cbUnidadMedida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una unidad de medida.");
            cbUnidadMedida.requestFocus();
        } else if (!cbSelectCategoria.isSelected() && cbCategorias.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una categoría.");
            cbCategorias.requestFocus();
        } else if (!cbSelectMarca.isSelected() && cbMarcas.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una marca.");
            cbMarcas.requestFocus();
        } else if (!cbSelectPresentacion.isSelected() && cbPresentaciones.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una presentación.");
            cbPresentaciones.requestFocus();
        } else if (!cbSelectExistencia.isSelected() && cbExistencia.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un tipo de existencia.");
            cbExistencia.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Inventario");
            fileChooser.setInitialFileName("Lista de Inventario");
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
                    Tools.newLineString("Se está generando el pdf de inventario."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void onEventExcel() {
        if (cbInventario.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un inventario.");
            cbInventario.requestFocus();
        } else if (!cbSelectUnidadMedida.isSelected() && cbUnidadMedida.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una unidad de medida.");
            cbUnidadMedida.requestFocus();
        } else if (!cbSelectCategoria.isSelected() && cbCategorias.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una categoría.");
            cbCategorias.requestFocus();
        } else if (!cbSelectMarca.isSelected() && cbMarcas.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una marca.");
            cbMarcas.requestFocus();
        } else if (!cbSelectPresentacion.isSelected() && cbPresentaciones.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione una presentación.");
            cbPresentaciones.requestFocus();
        } else if (!cbSelectExistencia.isSelected() && cbExistencia.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(vbWindow, "Inventario", "Seleccione un tipo de existencia.");
            cbExistencia.requestFocus();
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
            fileChooser.setTitle("Reporte de Inventario");
            fileChooser.setInitialFileName("Lista de Inventario");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"),
                    new FileChooser.ExtensionFilter("Libro de Excel(1997-2003) (*.xls)", "*.xls")
            );
            File file = fileChooser.showSaveDialog(vbWindow.getScene().getWindow());
            if (file != null) {
                file = new File(file.getAbsolutePath());
                if (file.getName().endsWith("xlsx")) {
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
                Object object = SuministroADO.GetReporteGeneralInventario(
                        cbInventario.getSelectionModel().getSelectedItem().getIdAlmacen(),
                        cbSelectUnidadMedida.isSelected() ? 0 : cbUnidadMedida.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbSelectCategoria.isSelected() ? 0 : cbCategorias.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbSelectMarca.isSelected() ? 0 : cbMarcas.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbSelectPresentacion.isSelected() ? 0 : cbPresentaciones.getSelectionModel().getSelectedItem().getIdDetalle(),
                        cbSelectExistencia.isSelected() ? 0
                        : cbExistencia.getSelectionModel().getSelectedIndex() == 0 ? 1
                        : cbExistencia.getSelectionModel().getSelectedIndex() == 1 ? 2
                        : cbExistencia.getSelectionModel().getSelectedIndex() == 2 ? 3
                        : 4
                );

                if (object instanceof ArrayList) {
                    ArrayList<SuministroTB> list = (ArrayList<SuministroTB>) object;
                    if (list.isEmpty()) {
                        return "No hay datos para mostrar.";
                    }
                    try {

                        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                            XSSFSheet sheet = workbook.createSheet("Inventario");

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
                            cellTitle.setCellValue("Lista de Inventario");
                            cellTitle.setCellStyle(cellStyleTitle);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

                            //-----------------------------------------------------------------------------------
                            Font fontHeader = workbook.createFont();
                            fontHeader.setFontHeightInPoints((short) 12);
                            fontHeader.setBold(true);
                            fontHeader.setColor(HSSFColor.BLACK.index);

                            XSSFCellStyle cellStyleHeader = workbook.createCellStyle();
                            cellStyleHeader.setAlignment(CellStyle.ALIGN_CENTER);
                            cellStyleHeader.setFont(fontHeader);

                            String header[] = {"#", "PRODUCTO", "CANTIDAD", "MEDIDA", "CATEGORÍA", "MARCA", "PRESENTACIÓN"};
                            Row headerRow = sheet.createRow(1);
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

                            cellStyleHeader = workbook.createCellStyle();
                            int row = 1;
                            for (SuministroTB sm : list) {
                                row++;
                                Row rowBody = sheet.createRow(row);

                                Cell cell1 = rowBody.createCell(0);
                                cell1.setCellStyle(cellStyleHeader);
                                cell1.setCellValue(sm.getId());

                                Cell cell2 = rowBody.createCell(1);
                                cell2.setCellStyle(cellStyleHeader);
                                cell2.setCellValue(sm.getClave() + "\n" + sm.getNombreMarca());
                            }

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
                    Tools.newLineString("Se está generando el excel del inventario."),
                    Duration.seconds(5),
                    Pos.BOTTOM_RIGHT);
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    @FXML
    private void onKeyPressedVisualizarInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventVisualizar();
        }
    }

    @FXML
    private void onActionVisualizarInventario(ActionEvent event) {
        onEventVisualizar();
    }

    @FXML
    private void onKeyPressedPdfInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPdf();
            event.consume();
        }
    }

    @FXML
    private void onActionPdfInventario(ActionEvent event) {
        onEventPdf();
    }

    @FXML
    private void onKeyPressedExcelInventario(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExcel();
            event.consume();
        }
    }

    @FXML
    private void onActionExcelInventario(ActionEvent event) {
        onEventExcel();
    }

    @FXML
    private void onActionCbUnidadMedidaInventario(ActionEvent event) {
        cbUnidadMedida.setDisable(cbSelectUnidadMedida.isSelected());
    }

    @FXML
    private void onActionCbCategoriaInventario(ActionEvent event) {
        cbCategorias.setDisable(cbSelectCategoria.isSelected());
    }

    @FXML
    private void onActionCbMarcaInventario(ActionEvent event) {
        cbMarcas.setDisable(cbSelectMarca.isSelected());
    }

    @FXML
    private void onActionCbPresentacionInventario(ActionEvent event) {
        cbPresentaciones.setDisable(cbSelectPresentacion.isSelected());
    }

    @FXML
    private void onActionCbExistenciaInventario(ActionEvent event) {
        cbExistencia.setDisable(cbSelectExistencia.isSelected());
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
