package controller.inventario.lote;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.awt.HeadlessException;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import model.LoteADO;
import model.LoteTB;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

public class FxLoteController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private TableView<LoteTB> tvList;
    @FXML
    private TableColumn<LoteTB, Integer> tcId;
    @FXML
    private TableColumn<LoteTB, String> tcNumeroLote;
    @FXML
    private TableColumn<LoteTB, String> tcArticulo;
    @FXML
    private TableColumn<LoteTB, String> tcCaducidad;
    @FXML
    private TableColumn<LoteTB, String> tcActual;
    @FXML
    private TableColumn<LoteTB, Label> tcPorCaducar;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbEstado;
    @FXML
    private Label lblCaducados;
    @FXML
    private Label lblPorCaducar;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tcId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        tcNumeroLote.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getNumeroLote()));
        tcArticulo.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getSuministroTB().getClave() + "\n" + cellData.getValue().getSuministroTB().getNombreMarca()));
        tcCaducidad.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getFechaCaducidad().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))));
        tcActual.setCellValueFactory(cellData -> Bindings.concat(Tools.roundingValue(cellData.getValue().getExistenciaActual(), 2)));
        tcPorCaducar.setCellValueFactory(new PropertyValueFactory<>("lblEstado"));

        tcId.prefWidthProperty().bind(tvList.widthProperty().multiply(0.06));
        tcNumeroLote.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcArticulo.prefWidthProperty().bind(tvList.widthProperty().multiply(0.3));
        tcCaducidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.17));
        tcActual.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcPorCaducar.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));

        cbEstado.getItems().addAll("Todos", "Proximos a caducar", "Caducados");
        cbEstado.getSelectionModel().select(0);
    }

    public void fillLoteTable(short opcion, String value) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            public ArrayList<Object> call() {
                ArrayList<Object> list = new ArrayList<>();
                list.add(LoteADO.GetTotalCaducados());
                list.add(LoteADO.GetTotalPorCaducar());
                list.add(LoteADO.ListLote(opcion, value));
                return list;
            }
        };

        task.setOnSucceeded((WorkerStateEvent e) -> {
            ArrayList<Object> list = task.getValue();
            if (!list.isEmpty()) {
                lblCaducados.setText("Lotes caducados -> " + list.get(0) == null ? "No hubo resultados" : (String) list.get(0));
                lblPorCaducar.setText("Lotes proximos a caducarse a 2 semanas -> " + list.get(1) == null ? "No hubo resultados" : (String) list.get(1));
                tvList.setItems((ObservableList<LoteTB>) list.get(2));
                lblLoad.setVisible(false);
            }
        });
        task.setOnFailed((WorkerStateEvent event) -> {
            lblLoad.setVisible(false);
        });

        task.setOnScheduled((WorkerStateEvent event) -> {
            lblLoad.setVisible(true);
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void addLote() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_LOTE_PROCESO);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxLoteProcesoController controller = fXMLLoader.getController();
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Registro de lotes", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();
    }

    private void editarLote() throws IOException {
        fxPrincipalController.openFondoModal();
        URL url = getClass().getResource(FilesRouters.FX_LOTE_CAMBIAR);
        FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
        Parent parent = fXMLLoader.load(url.openStream());
        //Controlller here
        FxLoteCambiarController controller = fXMLLoader.getController();
        controller.setLoteController(this);
        controller.setEditBatchRealizado(new String[]{
            tvList.getSelectionModel().getSelectedItem().getNumeroLote(),
            tvList.getSelectionModel().getSelectedItem().getIdLote() + "",
            tvList.getSelectionModel().getSelectedItem().getSuministroTB().getClave(),
            tvList.getSelectionModel().getSelectedItem().getSuministroTB().getNombreMarca(),
            tvList.getSelectionModel().getSelectedItem().getExistenciaActual() + "",
            tvList.getSelectionModel().getSelectedItem().getFechaCaducidad() + ""
        });
        //
        Stage stage = WindowStage.StageLoaderModal(parent, "Editar lote", window.getScene().getWindow());
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
        stage.show();

    }

    private void onViewReporte() {
        try {
            ArrayList<LoteTB> list = new ArrayList();
            for (int i = 0; i < tvList.getItems().size(); i++) {
                list.add(new LoteTB(
                        tvList.getItems().get(i).getId(),
                        tvList.getItems().get(i).getNumeroLote(),
                        tvList.getItems().get(i).getSuministroTB().getClave() + "\n" + tvList.getItems().get(i).getSuministroTB().getNombreMarca(),
                        tvList.getItems().get(i).getFechaCaducidad().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Tools.roundingValue(tvList.getItems().get(i).getExistenciaActual(), 2)));
            }

            Map map = new HashMap();
            map.put("TIPOFILTRO", String.valueOf(cbEstado.getSelectionModel().getSelectedItem()));

            JasperPrint jasperPrint = JasperFillManager.fillReport(FxLoteController.class.getResourceAsStream("/report/ListarLotes.jasper"), map, new JRBeanCollectionDataSource(list));

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            JasperViewer jasperViewer = new JasperViewer(jasperPrint, false);
            jasperViewer.setIconImage(new ImageIcon(getClass().getResource(FilesRouters.IMAGE_ICON)).getImage());
            jasperViewer.setTitle("Lista de Lote(s)");
            jasperViewer.setSize(840, 650);
            jasperViewer.setLocationRelativeTo(null);
            jasperViewer.setVisible(true);

        } catch (HeadlessException | ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException | JRException ex) {
            System.out.println("Error al generar el reporte : " + ex);
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            addLote();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        addLote();
    }

    @FXML
    private void onActionEdit(ActionEvent event) throws IOException {
        if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
            editarLote();
        }
    }

    @FXML
    private void onKeyPressedEdit(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            if (tvList.getSelectionModel().getSelectedIndex() >= 0) {
                editarLote();
            }
        }
    }

    @FXML
    private void onActionSearch(ActionEvent event) {
        fillLoteTable((short) 0, txtSearch.getText().trim());
    }

    @FXML
    private void onActionReload(ActionEvent event) {
        fillLoteTable((short) 0, "");
    }

    @FXML
    private void onActionReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onViewReporte();
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        onViewReporte();
    }

    @FXML
    private void onActionEstado(ActionEvent event) {
        switch (cbEstado.getSelectionModel().getSelectedIndex()) {
            case 0:
                fillLoteTable((short) 0, "");
                break;
            case 1:
                fillLoteTable((short) 1, "");
                break;

            case 2:
                fillLoteTable((short) 2, "");
                break;
        }
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
