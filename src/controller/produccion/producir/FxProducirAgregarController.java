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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
import model.EmpleadoTB;
import model.FormulaTB;
import model.ProduccionTB;
import model.SuministroTB;
import service.DetalleADO;
import service.EmpleadoADO;
import service.FormulaADO;
import service.ProduccionADO;
import service.SuministroADO;

public class FxProducirAgregarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private ComboBox<SuministroTB> cbProducto;
    @FXML
    private TextField txtCantidad;
    @FXML
    private ComboBox<FormulaTB> cbFormula;
    @FXML
    private VBox vbPrimero;
    @FXML
    private VBox vbSegundo;
    @FXML
    private DatePicker dtFechaTermino;
    @FXML
    private TextField txtDias;
    @FXML
    private TextField txtHoras;
    @FXML
    private TextField txtMinutos;
    @FXML
    private RadioButton cbInterno;
    @FXML
    private RadioButton cbExterno;
    @FXML
    private ComboBox<EmpleadoTB> cbPersonaEncargada;
    @FXML
    private TextArea txtDescripcion;
    @FXML
    private GridPane gpListInsumo;
    @FXML
    private GridPane gpListMerma;
    @FXML
    private Button btnAgregar;
    @FXML
    private TextField txtCostoAdicional;
    @FXML
    private VBox vbBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private Text lblMedidaProducir;
    @FXML
    private Text lblProducto;
    @FXML
    private CheckBox cbCantidadVariable;
    @FXML
    private TextField txtCantidadProducir;

    private FxProducirController producirController;

    private FxPrincipalController fxPrincipalController;

    private ArrayList<SuministroTB> suministroInsumos;

    private ArrayList<SuministroTB> suministroMermas;

    private SearchComboBox<FormulaTB> searchComboBoxFormula;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suministroInsumos = new ArrayList();
        suministroMermas = new ArrayList();
        Tools.actualDate(Tools.getDate(), dtFechaTermino);
        ToggleGroup toggleGroup = new ToggleGroup();
        cbInterno.setToggleGroup(toggleGroup);
        cbExterno.setToggleGroup(toggleGroup);
        addElementPaneHeadInsumo();

        comboBoxFormulas();
        comboBoxProductos();
//        comboBoxMedidaProducir();
        comboBoxEmpleados();
        executeInitData();
    }

    private void executeInitData() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object[]> task = new Task<Object[]>() {
            @Override
            protected Object[] call() {
                return new Object[]{
                    FormulaADO.getSearchComboBoxFormulas()
                };
            }
        };

        task.setOnScheduled(w -> {
            hbLoad.setVisible(true);
            vbBody.setDisable(true);
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText("Error en cargar los datos, intente nuevamente por favor.");
        });

        task.setOnSucceeded(w -> {
            Object[] object = task.getValue();
            if (object[0] instanceof List) {
                searchComboBoxFormula.getComboBox().getItems().addAll((List<FormulaTB>) object[0]);
            }

            hbLoad.setVisible(false);
            vbBody.setDisable(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void comboBoxFormulas() {
        searchComboBoxFormula = new SearchComboBox<>(cbFormula, true);
        searchComboBoxFormula.setFilter((item, text) -> item.getTitulo().toLowerCase().contains(text.toLowerCase()));

        searchComboBoxFormula.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    cbProducto.getItems().clear();
                    cbProducto.getItems().add(cbFormula.getSelectionModel().getSelectedItem().getSuministroTB());
                    if (!cbProducto.getItems().isEmpty()) {
                        cbProducto.getSelectionModel().select(0);
                    }
                    searchComboBoxFormula.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    searchComboBoxFormula.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    searchComboBoxFormula.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });

        searchComboBoxFormula.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxFormula.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxFormula.getSearchComboBoxSkin().isClickSelection()) {
                    cbProducto.getItems().clear();
                    cbProducto.getItems().add(cbFormula.getSelectionModel().getSelectedItem().getSuministroTB());
                    if (!cbProducto.getItems().isEmpty()) {
                        cbProducto.getSelectionModel().select(0);
                    }
                    searchComboBoxFormula.getComboBox().hide();
                }
            }
        });
    }

    private void comboBoxProductos() {
        SearchComboBox<SuministroTB> sCbSuministro = new SearchComboBox<>(cbProducto, false);
        sCbSuministro.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!sCbSuministro.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    sCbSuministro.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    sCbSuministro.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                sCbSuministro.getComboBox().hide();
            }
        });

        sCbSuministro.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            String search = sCbSuministro.getSearchComboBoxSkin().getSearchBox().getText().trim();
            if (search.isEmpty()) {
                return;
            }
            if (sCbSuministro.getCompletableFuture() != null) {
                return;
            }
            if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB) {
                return;
            }
            Platform.runLater(() -> {
                sCbSuministro.getComboBox().getItems().clear();
                sCbSuministro.getComboBox().setPromptText("Cargando información...");
            });
            sCbSuministro.setCompletableFuture(CompletableFuture.supplyAsync(() -> {
                return SuministroADO.getSearchComboBoxSuministros(search, false);
            }).thenAcceptAsync(complete -> {
                if (complete instanceof List) {
                    Platform.runLater(() -> {
                        sCbSuministro.getComboBox().getItems().addAll(complete);
                        sCbSuministro.getComboBox().setPromptText("");
                        sCbSuministro.setCompletableFuture(null);
                    });
                }
            }));
            t.consume();
        });

        sCbSuministro.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    sCbSuministro.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    sCbSuministro.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    sCbSuministro.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        sCbSuministro.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                sCbSuministro.getComboBox().getSelectionModel().select(item);
                if (sCbSuministro.getSearchComboBoxSkin().isClickSelection()) {
                    sCbSuministro.getComboBox().hide();
                }
            }
        });
    }

    private void comboBoxEmpleados() {
        SearchComboBox<EmpleadoTB> scbEmpleado = new SearchComboBox<>(cbPersonaEncargada, false);
        scbEmpleado.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!scbEmpleado.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    scbEmpleado.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    scbEmpleado.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                scbEmpleado.getComboBox().hide();
            }
        });
        scbEmpleado.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            if (scbEmpleado.getCompletableFuture() != null) {
                return;
            }
            if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB) {
                return;
            }
            Platform.runLater(() -> {
                scbEmpleado.getComboBox().getItems().clear();
                scbEmpleado.getComboBox().setPromptText("Cargando información...");
            });
            scbEmpleado.setCompletableFuture(CompletableFuture.supplyAsync(() -> {
                return EmpleadoADO.getSearchComboBoxEmpleados(scbEmpleado.getSearchComboBoxSkin().getSearchBox().getText().trim());
            }).thenAcceptAsync(complete -> {
                if (complete instanceof List) {
                    Platform.runLater(() -> {
                        scbEmpleado.getComboBox().getItems().addAll(complete);
                        scbEmpleado.getComboBox().setPromptText("");
                        scbEmpleado.setCompletableFuture(null);
                    });
                }
            }));
            t.consume();
        });
        scbEmpleado.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            switch (t.getCode()) {
                case ENTER:
                case SPACE:
                case ESCAPE:
                    scbEmpleado.getComboBox().hide();
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    break;
                default:
                    scbEmpleado.getSearchComboBoxSkin().getSearchBox().requestFocus();
                    scbEmpleado.getSearchComboBoxSkin().getSearchBox().selectAll();
                    break;
            }
        });
        scbEmpleado.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                scbEmpleado.getComboBox().getSelectionModel().select(item);
                if (scbEmpleado.getSearchComboBoxSkin().isClickSelection()) {
                    scbEmpleado.getComboBox().hide();
                }
            }
        });
    }

    //------------------------------------------------------------------------------------ 
    private void addElementsTableInsumos() {
        SuministroTB suministroTB = new SuministroTB();
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
            String search = searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim();
            if (search.isEmpty()) {
                return;
            }
            if (searchComboBox.getCompletableFuture() != null) {
                return;
            }
            if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB) {
                return;
            }
            Platform.runLater(() -> {
                searchComboBox.getComboBox().getItems().clear();
                searchComboBox.getComboBox().setPromptText("Cargando información...");
            });
            searchComboBox.setCompletableFuture(CompletableFuture.supplyAsync(() -> {
                return SuministroADO.getSearchComboBoxSuministros(search, false);
            }).thenAcceptAsync(complete -> {
                if (complete instanceof List) {
                    Platform.runLater(() -> {
                        searchComboBox.getComboBox().getItems().addAll(complete);
                        searchComboBox.getComboBox().setPromptText("");
                        searchComboBox.setCompletableFuture(null);
                    });
                }
            }));
            
            t.consume();
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
            suministroInsumos.remove(suministroTB);
            addElementPaneHeadInsumo();
            addElementPaneBodyInsumo();
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                suministroInsumos.remove(suministroTB);
                addElementPaneHeadInsumo();
                addElementPaneBodyInsumo();
            }
        });
        suministroTB.setBtnRemove(button);
        suministroInsumos.add(suministroTB);
        addElementPaneHeadInsumo();
        addElementPaneBodyInsumo();
    }

    private void addElementPaneHeadInsumo() {
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
        gpListInsumo.getColumnConstraints().get(4).setHalignment(HPos.CENTER);

        gpListInsumo.add(addElementGridPaneLabel("l01", "N°"), 0, 0);
        gpListInsumo.add(addElementGridPaneLabel("l02", "Insumo"), 1, 0);
        gpListInsumo.add(addElementGridPaneLabel("l03", "Cantidad"), 2, 0);
        gpListInsumo.add(addElementGridPaneLabel("l04", "Medida/Peso"), 3, 0);
        gpListInsumo.add(addElementGridPaneLabel("l05", "Quitar"), 4, 0);
    }

    private void addElementPaneBodyInsumo() {
        for (int i = 0; i < suministroInsumos.size(); i++) {
            gpListInsumo.add(addElementGridPaneLabel("l1" + (i + 1), (i + 1) + "", Pos.CENTER), 0, (i + 1));
            gpListInsumo.add(suministroInsumos.get(i).getCbSuministro(), 1, (i + 1));
            gpListInsumo.add(suministroInsumos.get(i).getTxtCantidad(), 2, (i + 1));
            gpListInsumo.add(suministroInsumos.get(i).getTxtPeso(), 3, (i + 1));
            gpListInsumo.add(suministroInsumos.get(i).getBtnRemove(), 4, (i + 1));
        }
    }

    //------------------------------------------------------------------------------------  
    private void addElementsTableMerma() {
        SuministroTB suministroTB = new SuministroTB();
        ComboBox<SuministroTB> comboBox = new ComboBox();
        comboBox.setDisable(true);
        comboBox.setPromptText("-- Selecionar --");
        comboBox.setPrefWidth(220);
        comboBox.setPrefHeight(30);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        suministroTB.setCbSuministro(comboBox);

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

        List<SuministroTB> suministroTBs = SuministroADO.getSearchComboBoxSuministros(cbProducto.getSelectionModel().getSelectedItem().getIdSuministro(), true);
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
        gpListMerma.getColumnConstraints().get(4).setHalignment(HPos.CENTER);

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

    //------------------------------------------------------------------------------------ 
    private void onEventSiguiente() {
        if (cbProducto.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Producción", "Seleccione un producto a producir.");
            cbProducto.requestFocus();
        } else if (!cbCantidadVariable.isSelected() && !Tools.isNumeric(txtCantidad.getText())) {
            Tools.AlertMessageWarning(apWindow, "Producción", "Ingrese la cantidad a producir.");
            txtCantidad.requestFocus();
        } else if (!cbCantidadVariable.isSelected() && Double.parseDouble(txtCantidad.getText()) <= 0) {
            Tools.AlertMessageWarning(apWindow, "Producción", "Ingrese una cantidad mayor a 0");
            txtCantidad.requestFocus();
        } else if (cbPersonaEncargada.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Producción", "Seleccione al personal encargado.");
            cbPersonaEncargada.requestFocus();
        } else {
            vbPrimero.setVisible(false);
            vbSegundo.setVisible(true);
            lblProducto.setText(cbProducto.getSelectionModel().getSelectedItem().getNombreMarca());
            lblMedidaProducir.setText(cbProducto.getSelectionModel().getSelectedItem().getUnidadCompraName());
            txtCantidadProducir.setDisable(!cbCantidadVariable.isSelected());
            txtCantidadProducir.setText(cbCantidadVariable.isSelected() ? "0" : Tools.roundingValue(Double.parseDouble(txtCantidad.getText()), 2));

            if (cbFormula.getSelectionModel().getSelectedIndex() >= 0) {
                addFormulaProceso(cbFormula.getSelectionModel().getSelectedItem().getIdFormula());
            }
        }
    }

    public void addFormulaProceso(String idFormula) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() {
                return FormulaADO.Obtener_Formula_ById(idFormula);
            }
        };

        task.setOnScheduled(w -> {

        });
        task.setOnFailed(w -> {

        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof FormulaTB) {
                FormulaTB formulaTB = (FormulaTB) object;
                txtCostoAdicional.setText(Tools.roundingValue(formulaTB.getCostoAdicional(), 2));
                txtDescripcion.setText(formulaTB.getInstrucciones());
                for (SuministroTB suministroTB : formulaTB.getSuministroTBs()) {
                    ComboBox<SuministroTB> comboBox = new ComboBox<>();
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
                            suministroTBs.forEach(p -> cbProducto.getItems().add(p));
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
                    suministroTB.getCbSuministro().getItems().add(new SuministroTB(suministroTB.getIdSuministro(), suministroTB.getClave(), suministroTB.getNombreMarca()));
                    suministroTB.getCbSuministro().getSelectionModel().select(0);

                    TextField fieldCantidad = new TextField(cbCantidadVariable.isSelected() ? "0" : Tools.roundingValue(Double.parseDouble(txtCantidad.getText()), 2));
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
                        suministroInsumos.remove(suministroTB);
                        addElementPaneHeadInsumo();
                        addElementPaneBodyInsumo();
                    });
                    button.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            suministroInsumos.remove(suministroTB);
                            addElementPaneHeadInsumo();
                            addElementPaneBodyInsumo();
                        }
                    });
                    suministroTB.setBtnRemove(button);
                    suministroInsumos.add(suministroTB);
                    addElementPaneHeadInsumo();
                    addElementPaneBodyInsumo();
                }

            } else {
                Tools.println((String) object);
            }
        });
        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }
    //------------------------------------------------------------------------------------ 

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(producirController.getWindow(), 0d);
        AnchorPane.setTopAnchor(producirController.getWindow(), 0d);
        AnchorPane.setRightAnchor(producirController.getWindow(), 0d);
        AnchorPane.setBottomAnchor(producirController.getWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(producirController.getWindow());
    }

    private void onEventCancelar() {
        vbPrimero.setVisible(true);
        vbSegundo.setVisible(false);
        txtCantidad.clear();
        cbProducto.getItems().clear();
        cbProducto.getSelectionModel().select(null);
        cbFormula.getItems().clear();
        searchComboBoxFormula.getComboBox().getItems().addAll(FormulaADO.getSearchComboBoxFormulas());
        Tools.actualDate(Tools.getDate(), dtFechaTermino);

        txtDias.clear();
        txtHoras.clear();
        txtMinutos.clear();
        txtDescripcion.clear();
        cbInterno.setSelected(true);
        cbPersonaEncargada.getItems().clear();
        txtCostoAdicional.clear();
        lblProducto.setText("-");
        lblMedidaProducir.setText("-");
        txtCantidadProducir.setText("");
        suministroInsumos.clear();
        addElementPaneHeadInsumo();
        addElementPaneBodyInsumo();
        suministroMermas.clear();
        addElementPaneHeadMerma();
        addElementPaneBodyMerma();
    }

    private void openModalComplete(ProduccionTB produccionTB) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_MODAL_ESTADO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());

            FXModalEstadoController controller = fXMLLoader.getController();
            controller.setInitControllerProducirAgregar(this);
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

    private void onEventGuardar() {
        if (dtFechaTermino.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Producción", "Ingrese la fecha que termina la producción.");
            dtFechaTermino.requestFocus();
        } else if (suministroInsumos.isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Producción", "No hay matería prima para producir.");
            btnAgregar.requestFocus();
        } else {
            int cantidadInsumo = 0;
            int itemInsumo = 0;
            for (SuministroTB suministroTB : suministroInsumos) {
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
                for (SuministroTB suministroTB : suministroInsumos) {
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
                        for (SuministroTB suministroTB : suministroMermas) {
                            if (Tools.isNumeric(suministroTB.getTxtCantidad().getText()) && Double.parseDouble(suministroTB.getTxtCantidad().getText()) > 0) {
                                cantidadMerma += 0;
                            } else {
                                cantidadMerma += 1;
                            }
                            if (suministroTB.getCbSuministro().getSelectionModel().getSelectedIndex() >= 0) {
                                itemMerma += 0;
                            } else {
                                itemMerma += 1;
                            }
                        }

                        if (cantidadMerma > 0) {
                            Tools.AlertMessageWarning(apWindow, "Producción", "Hay cantidades en 0 en la lista de merma.");
                        } else if (itemMerma > 0) {
                            Tools.AlertMessageWarning(apWindow, "Producción", "No hay merma seleccionados en la lista.");
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
        ProduccionTB produccionTB = new ProduccionTB();
        produccionTB.setProyecto("");
        produccionTB.setFechaInicio(Tools.getDatePicker(dtFechaTermino));
        produccionTB.setHoraInicio(Tools.getTime());
        produccionTB.setDias(Tools.isNumericInteger(txtDias.getText()) ? Integer.parseInt(txtDias.getText()) : 0);
        produccionTB.setHoras(Tools.isNumericInteger(txtHoras.getText()) ? Integer.parseInt(txtHoras.getText()) : 0);
        produccionTB.setMinutos(Tools.isNumericInteger(txtMinutos.getText()) ? Integer.parseInt(txtMinutos.getText()) : 0);
        produccionTB.setIdProducto(cbProducto.getSelectionModel().getSelectedItem().getIdSuministro());
        produccionTB.setTipoOrden(true);
        produccionTB.setIdEncargado(cbPersonaEncargada.getSelectionModel().getSelectedItem().getIdEmpleado());
        produccionTB.setDescripcion(txtDescripcion.getText().trim());
        produccionTB.setFechaRegistro(Tools.getDate());
        produccionTB.setHoraRegistro(Tools.getTime());
        produccionTB.setCantidad(cbCantidadVariable.isSelected() ? 0 : Double.parseDouble(txtCantidad.getText()));
        produccionTB.setCantidadVariable(cbCantidadVariable.isSelected());
        produccionTB.setCostoAdicional(Tools.isNumeric(txtCostoAdicional.getText()) ? Double.parseDouble(txtCostoAdicional.getText()) : 0);
        produccionTB.setSuministroInsumos(newInsumos);
        produccionTB.setSuministroMermas(newMerma);
        openModalComplete(produccionTB);
    }

    public void executeRegistro(ProduccionTB produccionTB) {
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
            if (result.equalsIgnoreCase("inserted")) {
                lblMessageLoad.setText("Se registró correctamente la producción.");
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

    private boolean validateDuplicateInsumo(ArrayList<SuministroTB> view, SuministroTB suministroTB) {
        boolean ret = false;
        for (SuministroTB sm : view) {
            if (sm.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro().equals(suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro())) {
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
    private void onKeyPressedSiguiente(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSiguiente();
        }
    }

    @FXML
    private void onActionSiguiente(ActionEvent event) {
        onEventSiguiente();
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
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventGuardar();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventGuardar();
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCancelar();
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        onEventCancelar();
    }

    @FXML
    private void onKeyTypedCantidad(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCantidad.getText().contains(".")) {
            event.consume();
        }
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

    @FXML
    private void onActionCantidadVariable(ActionEvent event) {
        txtCantidad.setDisable(cbCantidadVariable.isSelected());
    }

    public TextField getTxtCantidadProducir() {
        return txtCantidadProducir;
    }

    public void setTxtCantidadProducir(TextField txtCantidadProducir) {
        this.txtCantidadProducir = txtCantidadProducir;
    }

    public void setInitControllerProducir(FxProducirController producirController, FxPrincipalController fxPrincipalController) {
        this.producirController = producirController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
