package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.SearchComboBox;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.DetalleTB;
import model.ProduccionTB;
import model.SuministroTB;
import service.DetalleADO;
import service.ProduccionADO;
import service.SuministroADO;

public class FxProducirEditarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtCantidadProducir;
    @FXML
    private Text lblProducto;
    @FXML
    private Text lblEncargado;
    @FXML
    private Text lblFechaCreacion;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private Button btnAgregar;
    @FXML
    private GridPane gpListInsumo;
    @FXML
    private GridPane gpListMerma;
    @FXML
    private VBox vbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private TextField txtDias;
    @FXML
    private TextField txtHoras;
    @FXML
    private TextField txtMinutos;
    @FXML
    private TextField txtCostoAdicional;

    private FxPrincipalController fxPrincipalController;

    private FxProducirController producirController;

    private String idProduccion;

    private ProduccionTB produccionTB;

    private ArrayList<SuministroTB> suministroTBs;

    private ArrayList<SuministroTB> suministroMermas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suministroTBs = new ArrayList();
        suministroMermas = new ArrayList();
        addElementPaneHeadInsumos();
    }

    public void editarProduccion(String idProduccion) {
        this.idProduccion = idProduccion;
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return ProduccionADO.Obtener_Produccion_Editor_ById(idProduccion);
            }
        };

        task.setOnScheduled(w -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getMessage());
            lblMessageLoad.setTextFill(Color.web("ea4242"));
            btnAceptarLoad.setOnAction(event -> {
                closeWindow();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    closeWindow();
                }
                event.consume();
            });
        });

        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof ProduccionTB) {
                produccionTB = (ProduccionTB) object;
                lblProducto.setText(produccionTB.getSuministroTB().getClave() + " - " + produccionTB.getSuministroTB().getNombreMarca());
                if (produccionTB.isCantidadVariable()) {
                    txtCantidadProducir.setDisable(false);
                    txtCantidadProducir.setText(Tools.roundingValue(produccionTB.getCantidad(), 2));
                } else {
                    txtCantidadProducir.setDisable(true);
                    txtCantidadProducir.setText(Tools.roundingValue(produccionTB.getCantidad(), 2));
//                    + " " + produccionTB.getSuministroTB().getUnidadCompraName()
                }
                lblEncargado.setText(produccionTB.getEmpleadoTB().getApellidos() + " " + produccionTB.getEmpleadoTB().getNombres());
                lblFechaCreacion.setText(produccionTB.getFechaRegistro() + " - " + produccionTB.getHoraRegistro());
                txtCostoAdicional.setText(Tools.roundingValue(produccionTB.getCostoAdicional(), 2));
                txtDescripcion.setText(produccionTB.getDescripcion());
                for (SuministroTB suministroTB : produccionTB.getSuministroInsumos()) {
                    suministroTB.getBtnRemove().setOnAction(event -> {
                        suministroTBs.remove(suministroTB);
                        addElementPaneHeadInsumos();
                        addElementPaneBodyInsumos();
                    });
                    suministroTB.getBtnRemove().setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            suministroTBs.remove(suministroTB);
                            addElementPaneHeadInsumos();
                            addElementPaneBodyInsumos();
                        }
                    });
                    suministroTBs.add(suministroTB);
                }
                addElementPaneHeadInsumos();
                addElementPaneBodyInsumos();
                vbBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("ea4242"));
                btnAceptarLoad.setOnAction(event -> {
                    closeWindow();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeWindow();
                    }
                    event.consume();
                });
            }
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void addElementsTableInsumos() {
        SuministroTB suministroTB = new SuministroTB();
        suministroTB.setCantidad(0);
        ComboBox<SuministroTB> comboBox = new ComboBox();
        comboBox.setPromptText("-- Selecionar --");
        comboBox.setPrefWidth(220);
        comboBox.setPrefHeight(30);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        suministroTB.setCbSuministro(comboBox);

        SearchComboBox<SuministroTB> searchComboBox = new SearchComboBox<>(suministroTB.getCbSuministro(), false);
        searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBox.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBox.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBox.getComboBox().hide();
            }
        });
        searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText())) {
                searchComboBox.getComboBox().getItems().clear();
                List<SuministroTB> suministroTBs = SuministroADO.getSearchComboBoxSuministros(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim(), false);
                suministroTBs.forEach(p -> suministroTB.getCbSuministro().getItems().add(p));
            }
        });
        searchComboBox.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    searchComboBox.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBox.getComboBox().getSelectionModel().select(item);
                if (searchComboBox.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBox.getComboBox().hide();
                }
            }
        });
        suministroTB.setSearchComboBoxSuministro(searchComboBox);

        TextField fieldCantidad = new TextField(Tools.roundingValue(1, 2));
        fieldCantidad.setPromptText("0.00");
        fieldCantidad.getStyleClass().add("text-field-normal");
        fieldCantidad.setPrefWidth(220);
        fieldCantidad.setPrefHeight(30);
        fieldCantidad.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && fieldCantidad.getText().contains(".")) {
                event.consume();
            }
        });
        suministroTB.setTxtCantidad(fieldCantidad);

        TextField fieldPeso = new TextField(Tools.roundingValue(0, 2));
        fieldPeso.setPromptText("0.00");
        fieldPeso.getStyleClass().add("text-field-normal");
        fieldPeso.setPrefWidth(220);
        fieldPeso.setPrefHeight(30);
        fieldPeso.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && fieldPeso.getText().contains(".")) {
                event.consume();
            }
        });
        suministroTB.setTxtPeso(fieldPeso);

        Button button = new Button();
        button.getStyleClass().add("buttonLightError");
        button.setAlignment(Pos.CENTER);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);

        button.setOnAction(event -> {
            suministroTBs.remove(suministroTB);
            addElementPaneHeadInsumos();
            addElementPaneBodyInsumos();
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                suministroTBs.remove(suministroTB);
                addElementPaneHeadInsumos();
                addElementPaneBodyInsumos();
            }
        });
        suministroTB.setBtnRemove(button);
        suministroTBs.add(suministroTB);
        addElementPaneHeadInsumos();
        addElementPaneBodyInsumos();
    }

    private void addElementPaneHeadInsumos() {
        gpListInsumo.getChildren().clear();
        gpListInsumo.getColumnConstraints().get(0).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(0).setPrefWidth(40);
        gpListInsumo.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);

        gpListInsumo.getColumnConstraints().get(1).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(1).setPrefWidth(320);
        gpListInsumo.getColumnConstraints().get(1).setHgrow(Priority.SOMETIMES);

        gpListInsumo.getColumnConstraints().get(2).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(2).setPrefWidth(60);
        gpListInsumo.getColumnConstraints().get(2).setHgrow(Priority.SOMETIMES);

        gpListInsumo.getColumnConstraints().get(3).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(3).setPrefWidth(60);
        gpListInsumo.getColumnConstraints().get(3).setHgrow(Priority.SOMETIMES);

        gpListInsumo.getColumnConstraints().get(4).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(4).setPrefWidth(60);
        gpListInsumo.getColumnConstraints().get(4).setHgrow(Priority.SOMETIMES);

        gpListInsumo.getColumnConstraints().get(5).setMinWidth(10);
        gpListInsumo.getColumnConstraints().get(5).setPrefWidth(60);
        gpListInsumo.getColumnConstraints().get(5).setHgrow(Priority.SOMETIMES);
        gpListInsumo.getColumnConstraints().get(5).setHalignment(HPos.CENTER);

        gpListInsumo.add(addElementGridPaneLabel("l01", "N°"), 0, 0);
        gpListInsumo.add(addElementGridPaneLabel("l02", "Insumo"), 1, 0);
        gpListInsumo.add(addElementGridPaneLabel("l03", "Cantidad Utilizada"), 2, 0);
        gpListInsumo.add(addElementGridPaneLabel("l04", "Cantidad a Utilizar"), 3, 0);
        gpListInsumo.add(addElementGridPaneLabel("l05", "Medida/Peso"), 4, 0);
        gpListInsumo.add(addElementGridPaneLabel("l06", "Quitar"), 5, 0);
    }

    private void addElementPaneBodyInsumos() {
        for (int i = 0; i < suministroTBs.size(); i++) {
            gpListInsumo.add(addElementGridPaneLabel("l1" + (i + 1), (i + 1) + "", Pos.CENTER), 0, (i + 1));
            gpListInsumo.add(suministroTBs.get(i).getCbSuministro(), 1, (i + 1));
            gpListInsumo.add(addElementGridPaneLabel("l3", Tools.roundingValue(suministroTBs.get(i).getCantidad(), 2), Pos.CENTER), 2, (i + 1));
            gpListInsumo.add(suministroTBs.get(i).getTxtCantidad(), 3, (i + 1));
            gpListInsumo.add(suministroTBs.get(i).getTxtPeso(), 4, (i + 1));
            gpListInsumo.add(suministroTBs.get(i).getBtnRemove(), 5, (i + 1));
        }
    }

    //------------------------------------------------------------------------------------   
    private void addElementsTableMerma() {
        SuministroTB suministroTB = new SuministroTB();

        ComboBox<SuministroTB> comboBoxSuministro = new ComboBox();
        comboBoxSuministro.setDisable(true);
        comboBoxSuministro.setPromptText("-- Selecionar --");
        comboBoxSuministro.setPrefWidth(220);
        comboBoxSuministro.setPrefHeight(30);
        comboBoxSuministro.setMaxWidth(Double.MAX_VALUE);
        suministroTB.setCbSuministro(comboBoxSuministro);

        SearchComboBox<SuministroTB> scSuministro = new SearchComboBox<>(suministroTB.getCbSuministro(), false);
        scSuministro.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!scSuministro.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    scSuministro.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    scSuministro.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                scSuministro.getComboBox().hide();
            }
        });
        scSuministro.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(scSuministro.getSearchComboBoxSkin().getSearchBox().getText())) {
                scSuministro.getComboBox().getItems().clear();
            }
        });
        scSuministro.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    scSuministro.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    scSuministro.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    scSuministro.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        scSuministro.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                scSuministro.getComboBox().getSelectionModel().select(item);
                if (scSuministro.getSearchComboBoxSkin().isClickSelection()) {
                    scSuministro.getComboBox().hide();
                }
            }
        });
        suministroTB.setSearchComboBoxSuministro(scSuministro);
        List<SuministroTB> suministroTBs = SuministroADO.getSearchComboBoxSuministros(produccionTB.getIdProducto(), true);
        suministroTBs.forEach(p -> suministroTB.getCbSuministro().getItems().add(p));
        if (!suministroTB.getCbSuministro().getItems().isEmpty()) {
            suministroTB.getCbSuministro().getSelectionModel().select(0);
        }

        TextField fieldCantidad = new TextField(Tools.roundingValue(1, 2));
        fieldCantidad.setPromptText("0.00");
        fieldCantidad.getStyleClass().add("text-field-normal");
        fieldCantidad.setPrefWidth(220);
        fieldCantidad.setPrefHeight(30);
        fieldCantidad.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && fieldCantidad.getText().contains(".")) {
                event.consume();
            }
        });
        suministroTB.setTxtCantidad(fieldCantidad);

        TextField fieldPeso = new TextField(Tools.roundingValue(0, 2));
        fieldPeso.setPromptText("0.00");
        fieldPeso.getStyleClass().add("text-field-normal");
        fieldPeso.setPrefWidth(220);
        fieldPeso.setPrefHeight(30);
        fieldPeso.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && fieldPeso.getText().contains(".")) {
                event.consume();
            }
        });
        suministroTB.setTxtPeso(fieldPeso);

        ComboBox<DetalleTB> comboBoxDetalle = new ComboBox();
        comboBoxDetalle.setDisable(false);
        comboBoxDetalle.setPromptText("-- Selecionar --");
        comboBoxDetalle.setPrefWidth(220);
        comboBoxDetalle.setPrefHeight(30);
        comboBoxDetalle.setMaxWidth(Double.MAX_VALUE);
        suministroTB.setCbTipoMerma(comboBoxDetalle);

        SearchComboBox<DetalleTB> searchComboBoxTipoMerma = new SearchComboBox<>(suministroTB.getCbTipoMerma(), false);
        searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxTipoMerma.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxTipoMerma.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxTipoMerma.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxTipoMerma.getComboBox().hide();
            }
        });
        searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (!Tools.isText(searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().getText())) {
                searchComboBoxTipoMerma.getComboBox().getItems().clear();
                suministroTB.getCbTipoMerma().getItems().addAll(DetalleADO.Get_Detail_IdName("4", "0020", searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().getText().trim()));
            }
        });
        searchComboBoxTipoMerma.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    searchComboBoxTipoMerma.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    searchComboBoxTipoMerma.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        searchComboBoxTipoMerma.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxTipoMerma.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxTipoMerma.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxTipoMerma.getComboBox().hide();
                }
            }
        });

        Button button = new Button();
        button.getStyleClass().add("buttonLightError");
        button.setAlignment(Pos.CENTER);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
        ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);

        button.setOnAction(event -> {
            suministroMermas.remove(suministroTB);
            addElementPaneHeadMerma();
            addElementPaneBodyMerma();
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                suministroMermas.remove(suministroTB);
                addElementPaneHeadMerma();
                addElementPaneBodyMerma();
            }
        });
        suministroTB.setBtnRemove(button);
        suministroMermas.add(suministroTB);
        addElementPaneHeadMerma();
        addElementPaneBodyMerma();
    }

    private void addElementPaneHeadMerma() {
        gpListMerma.getChildren().clear();
        gpListMerma.getColumnConstraints().get(0).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(0).setPrefWidth(40);
        gpListMerma.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);

        gpListMerma.getColumnConstraints().get(1).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(1).setPrefWidth(320);
        gpListMerma.getColumnConstraints().get(1).setHgrow(Priority.SOMETIMES);

        gpListMerma.getColumnConstraints().get(2).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(2).setPrefWidth(60);
        gpListMerma.getColumnConstraints().get(2).setHgrow(Priority.SOMETIMES);

        gpListMerma.getColumnConstraints().get(3).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(3).setPrefWidth(60);
        gpListMerma.getColumnConstraints().get(3).setHgrow(Priority.SOMETIMES);

        gpListMerma.getColumnConstraints().get(4).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(4).setPrefWidth(60);
        gpListMerma.getColumnConstraints().get(4).setHgrow(Priority.SOMETIMES);

        gpListMerma.getColumnConstraints().get(5).setMinWidth(10);
        gpListMerma.getColumnConstraints().get(5).setPrefWidth(60);
        gpListMerma.getColumnConstraints().get(5).setHgrow(Priority.SOMETIMES);
        gpListMerma.getColumnConstraints().get(5).setHalignment(HPos.CENTER);

        gpListMerma.add(addElementGridPaneLabel("l01", "N°"), 0, 0);
        gpListMerma.add(addElementGridPaneLabel("l02", "Insumo"), 1, 0);
        gpListMerma.add(addElementGridPaneLabel("l03", "Cantidad"), 2, 0);
        gpListMerma.add(addElementGridPaneLabel("l04", "Medida/Peso"), 3, 0);
        gpListMerma.add(addElementGridPaneLabel("l05", "Tipo Merma"), 4, 0);
        gpListMerma.add(addElementGridPaneLabel("l06", "Quitar"), 5, 0);
    }

    private void addElementPaneBodyMerma() {
        for (int i = 0; i < suministroMermas.size(); i++) {
            gpListMerma.add(addElementGridPaneLabel("l1" + (i + 1), (i + 1) + "", Pos.CENTER), 0, (i + 1));
            gpListMerma.add(suministroMermas.get(i).getCbSuministro(), 1, (i + 1));
            gpListMerma.add(suministroMermas.get(i).getTxtCantidad(), 2, (i + 1));
            gpListMerma.add(suministroMermas.get(i).getTxtPeso(), 3, (i + 1));
            gpListMerma.add(suministroMermas.get(i).getCbTipoMerma(), 4, (i + 1));
            gpListMerma.add(suministroMermas.get(i).getBtnRemove(), 5, (i + 1));
        }
    }

    //------------------------------------------------------------------------------------ 
    private Label addElementGridPaneLabel(String id, String nombre) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-background-color: #020203;-fx-text-fill:#ffffff;-fx-padding: 0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight: 100");
        label.getStyleClass().add("labelOpenSansRegular13");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPaneLabel(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-text-fill:#020203;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void modalEstado(ProduccionTB produccionTB) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_MODAL_ESTADO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());

            FXModalEstadoController controller = fXMLLoader.getController();
            controller.setInitControllerProducirEditar(this);
            controller.setProduccionTB(produccionTB);

            Stage stage = WindowStage.StageLoaderModal(parent, "Confirmacion de Producción", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println("Controller modal estado" + ex.getLocalizedMessage());
        }
    }

    private void onEventEditar() {
        if (suministroTBs.isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Producción", "No hay matería prima para producir.");
            btnAgregar.requestFocus();
        } else {
            int cantidadInsumo = 0;
            int itemInsumo = 0;
            for (SuministroTB suministroTB : suministroTBs) {
                if (Tools.isNumeric(suministroTB.getTxtCantidad().getText()) && Double.parseDouble(suministroTB.getTxtCantidad().getText()) > 0) {
                    cantidadInsumo += 0;
                } else {
                    cantidadInsumo += 1;
                }
                if (suministroTB.getCbSuministro().getSelectionModel().getSelectedIndex() >= 0) {
                    itemInsumo += 0;
                } else {
                    itemInsumo += 1;
                }
            }

            if (cantidadInsumo > 0) {
                Tools.AlertMessageWarning(apWindow, "Producción", "Hay cantidades en 0 en la lista de insumos.");
            } else if (itemInsumo > 0) {
                Tools.AlertMessageWarning(apWindow, "Producción", "No hay insumos seleccionados en la lista.");
            } else {
                int duplicateInsumo = 0;
                ArrayList<SuministroTB> newSuministroInsumos = new ArrayList();
                for (SuministroTB suministroTB : suministroTBs) {
                    if (validateDuplicateInsumo(newSuministroInsumos, suministroTB)) {
                        duplicateInsumo += 1;
                    } else {
                        newSuministroInsumos.add(suministroTB);
                        duplicateInsumo += 0;
                    }
                }
                if (duplicateInsumo > 0) {
                    Tools.AlertMessageWarning(apWindow, "Producción", "Hay insumos duplicados en la lista.");
                } else {
                    if (!suministroMermas.isEmpty()) {
                        int cantidadMerma = 0;
                        int itemMerma = 0;
                        int tipoMerma = 0;
                        for (SuministroTB suministroTB : suministroMermas) {
                            if (Tools.isNumeric(suministroTB.getTxtCantidad().getText()) && Double.parseDouble(suministroTB.getTxtCantidad().getText()) <= 0) {
                                cantidadMerma += 1;
                            }
                            if (suministroTB.getCbSuministro().getSelectionModel().getSelectedIndex() < 0) {
                                itemMerma += 1;
                            }
                            if (suministroTB.getCbTipoMerma().getSelectionModel().getSelectedIndex() < 0) {
                                tipoMerma += 1;
                            }
                        }

                        if (cantidadMerma > 0) {
                            Tools.AlertMessageWarning(apWindow, "Producción", "Hay cantidades en 0 en la lista de merma.");
                        } else if (itemMerma > 0) {
                            Tools.AlertMessageWarning(apWindow, "Producción", "No hay merma seleccionados en la lista.");
                        } else if (tipoMerma > 0) {
                            Tools.AlertMessageWarning(apWindow, "Producción", "Seleccion el tipo de merma de la lista.");
                        } else {
                            int duplicateMerma = 0;
                            ArrayList<SuministroTB> newSuministroMermas = new ArrayList();
                            for (SuministroTB suministroTB : suministroMermas) {
                                if (validateDuplicateInsumo(newSuministroMermas, suministroTB)) {
                                    duplicateMerma += 1;
                                } else {
                                    newSuministroMermas.add(suministroTB);
                                    duplicateMerma += 0;
                                }
                            }

                            if (duplicateMerma > 0) {
                                Tools.AlertMessageWarning(apWindow, "Producción", "Hay mermas duplicados en la lista.");
                            } else {
                                produccionObject(newSuministroInsumos, newSuministroMermas);
                            }
                        }
                    } else {
                        produccionObject(newSuministroInsumos, suministroMermas);
                    }
                }
            }

        }
    }

    private void produccionObject(ArrayList<SuministroTB> newInsumos, ArrayList<SuministroTB> newMerma) {
        produccionTB.setIdProduccion(idProduccion);
        produccionTB.setFechaRegistro(Tools.getDate());
        produccionTB.setHoraRegistro(Tools.getTime());
        produccionTB.setDias(Tools.isNumericInteger(txtDias.getText()) ? Integer.parseInt(txtDias.getText()) : 0);
        produccionTB.setHoras(Tools.isNumericInteger(txtHoras.getText()) ? Integer.parseInt(txtHoras.getText()) : 0);
        produccionTB.setMinutos(Tools.isNumericInteger(txtMinutos.getText()) ? Integer.parseInt(txtMinutos.getText()) : 0);
        produccionTB.setCostoAdicional(Tools.isNumeric(txtCostoAdicional.getText()) ? Double.parseDouble(txtCostoAdicional.getText()) : 0);
        produccionTB.setDescripcion(txtDescripcion.getText().trim());
        produccionTB.setSuministroInsumos(newInsumos);
        produccionTB.setSuministroMermas(newMerma);
        modalEstado(produccionTB);
    }

    public void executeEdicion(ProduccionTB produccionTB) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return ProduccionADO.Crud_Produccion(produccionTB);
            }
        };
        task.setOnScheduled(w -> {
            vbBody.setDisable(true);
            hbLoad.setVisible(true);
            btnAceptarLoad.setVisible(false);
            lblMessageLoad.setText("Procesando información...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
        });
        task.setOnFailed(w -> {
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                closeWindow();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    closeWindow();
                }
            });
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
        });
        task.setOnSucceeded(w -> {
            String result = task.getValue();
            if (result.equalsIgnoreCase("updated")) {
                lblMessageLoad.setText("Se actualizó correctamente la producción.");
                lblMessageLoad.setTextFill(Color.web("#ffffff"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeWindow();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeWindow();
                    }
                });
            } else if (result.equalsIgnoreCase("state")) {
                lblMessageLoad.setText("La producción se encuentra anulada o terminada.");
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeWindow();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeWindow();
                    }
                });
            } else {
                lblMessageLoad.setText(result);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeWindow();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeWindow();
                    }
                });
            }
        });
        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(producirController.getWindow(), 0d);
        AnchorPane.setTopAnchor(producirController.getWindow(), 0d);
        AnchorPane.setRightAnchor(producirController.getWindow(), 0d);
        AnchorPane.setBottomAnchor(producirController.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(producirController.getWindow());
    }

    private boolean validateDuplicateInsumo(ArrayList<SuministroTB> view, SuministroTB suministroTB) {
        boolean ret = false;
        for (SuministroTB sm : view) {
            if (sm.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro()
                    .equals(suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeWindow();
    }

    @FXML
    private void onKeyPressedEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEditar();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
        onEventEditar();
    }

    @FXML
    private void onKeyPressedAgregar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addElementsTableInsumos();
        }
    }

    @FXML
    private void onActonAgregar(ActionEvent event) {
        addElementsTableInsumos();
    }

    @FXML
    private void onKeyPressedMerma(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addElementsTableMerma();
        }
    }

    @FXML
    private void onActonMerma(ActionEvent event) {
        addElementsTableMerma();
    }

    @FXML
    private void onKeyTypedDias(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedHoras(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedMinutos(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedCantidadProducir(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCantidadProducir.getText().contains(".")) {
            event.consume();
        }
    }

    public TextField getTxtCantidadProducir() {
        return txtCantidadProducir;
    }

    public void setInitControllerProducir(FxProducirController producirController, FxPrincipalController fxPrincipalController) {
        this.producirController = producirController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
