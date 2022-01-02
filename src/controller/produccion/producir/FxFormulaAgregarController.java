package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
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
import model.FormulaADO;
import model.FormulaTB;
import model.SuministroADO;
import model.SuministroTB;

public class FxFormulaAgregarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtCantidad;
    @FXML
    private ComboBox<SuministroTB> cbProducto;
    @FXML
    private TextField txtCostoAdicional;
    @FXML
    private TextField txtInstrucciones;
    @FXML
    private GridPane gpList;
    @FXML
    private TabPane tpContenedor;
    @FXML
    private HBox hbLoad;
    @FXML
    private VBox vbBody;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxPrincipalController fxPrincipalController;

    private FxFormulaController formulaController;

    private ArrayList<SuministroTB> suministroTBs;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        suministroTBs = new ArrayList<>();
        addElementPaneHead();
        comboBoxProductos();
    }

    private void addElementsTableInsumo() {
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
            if (!Tools.isText(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText())) {
                searchComboBox.getComboBox().getItems().clear();
                List<SuministroTB> insumoTBss = SuministroADO.getSearchComboBoxSuministros(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim(), false);
                insumoTBss.forEach(p -> searchComboBox.getComboBox().getItems().add(p));
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

        TextField textField = new TextField();
        textField.setPromptText("0.00");
        textField.getStyleClass().add("text-field-normal");
        textField.setPrefWidth(220);
        textField.setPrefHeight(30);
        textField.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && textField.getText().contains(".")) {
                event.consume();
            }
        });
        suministroTB.setTxtCantidad(textField);

        Button button = new Button();
        button.getStyleClass().add("buttonLightError");
        button.setAlignment(Pos.CENTER);
        button.setPrefWidth(Control.USE_COMPUTED_SIZE);
        button.setPrefHeight(Control.USE_COMPUTED_SIZE);
//        button.setMaxWidth(Double.MAX_VALUE);
//        button.setMaxHeight(Double.MAX_VALUE);
        ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        button.setGraphic(imageView);
        button.setOnAction(event -> {
            suministroTBs.remove(suministroTB);
            addElementPaneHead();
            addElementPaneBody();
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                suministroTBs.remove(suministroTB);
                addElementPaneHead();
                addElementPaneBody();
            }
        });
        suministroTB.setBtnRemove(button);
        suministroTBs.add(suministroTB);
        addElementPaneHead();
        addElementPaneBody();
    }

    private void addElementPaneHead() {
        gpList.getChildren().clear();
        gpList.getColumnConstraints().get(0).setMinWidth(10);
        gpList.getColumnConstraints().get(0).setPrefWidth(40);
        gpList.getColumnConstraints().get(0).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(1).setMinWidth(10);
        gpList.getColumnConstraints().get(1).setPrefWidth(340);
        gpList.getColumnConstraints().get(1).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(2).setMinWidth(10);
        gpList.getColumnConstraints().get(2).setPrefWidth(260);
        gpList.getColumnConstraints().get(2).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(3).setMinWidth(10);
        gpList.getColumnConstraints().get(3).setPrefWidth(180);
        gpList.getColumnConstraints().get(3).setHgrow(Priority.SOMETIMES);

        gpList.getColumnConstraints().get(4).setMinWidth(10);
        gpList.getColumnConstraints().get(4).setPrefWidth(60);
        gpList.getColumnConstraints().get(4).setHgrow(Priority.SOMETIMES);
        gpList.getColumnConstraints().get(4).setHalignment(HPos.CENTER);

        gpList.add(addElementGridPaneLabel("l01", "N°"), 0, 0);
        gpList.add(addElementGridPaneLabel("l02", "Insumo"), 1, 0);
        gpList.add(addElementGridPaneLabel("l03", "Cantidad"), 2, 0);
        gpList.add(addElementGridPaneLabel("l04", "Medida"), 3, 0);
        gpList.add(addElementGridPaneLabel("l05", "Quitar"), 4, 0);
    }

    private void addElementPaneBody() {
        for (int i = 0; i < suministroTBs.size(); i++) {
            gpList.add(addElementGridPaneLabel("l1" + (i + 1), (i + 1) + "", Pos.CENTER), 0, (i + 1));
            gpList.add(suministroTBs.get(i).getCbSuministro(), 1, (i + 1));
            gpList.add(suministroTBs.get(i).getTxtCantidad(), 2, (i + 1));
            gpList.add(addElementGridPaneLabel("l4" + (i + 1), "MEDIDA", Pos.CENTER), 3, (i + 1));
            gpList.add(suministroTBs.get(i).getBtnRemove(), 4, (i + 1));
        }
    }

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

    private void comboBoxProductos() {
        SearchComboBox<SuministroTB> searchComboBox = new SearchComboBox<>(cbProducto, false);
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
    }

    private void onEventExecute() {
        if (Tools.isText(txtTitulo.getText())) {
            Tools.AlertMessageWarning(apWindow, "Formula", "Ingrese un título.");
            tpContenedor.getSelectionModel().select(0);
            txtTitulo.requestFocus();
        } else if (!Tools.isNumeric(txtCantidad.getText())) {
            Tools.AlertMessageWarning(apWindow, "Formula", "Ingrese la cantidad por formula.");
            tpContenedor.getSelectionModel().select(0);
            txtCantidad.requestFocus();
        } else if (cbProducto.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Formula", "Seleccione el producto a producir.");
            tpContenedor.getSelectionModel().select(0);
            cbProducto.requestFocus();
        } else if (suministroTBs.isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Formula", "No hay insumos para continuar con el proceso.");
            tpContenedor.getSelectionModel().select(1);
        } else {
            int cantidad = 0;
            int insumo = 0;
            for (SuministroTB suministroTB : suministroTBs) {
                if (Tools.isNumeric(suministroTB.getTxtCantidad().getText()) && Double.parseDouble(suministroTB.getTxtCantidad().getText()) > 0) {
                    cantidad += 0;
                } else {
                    cantidad += 1;
                }

                if (suministroTB.getSearchComboBoxSuministro().getComboBox().getSelectionModel().getSelectedIndex() >= 0) {
                    insumo += 0;
                } else {
                    insumo += 1;
                }

            }

            if (cantidad > 0) {
                Tools.AlertMessageWarning(apWindow, "Formula", "Hay cantidades en 0 en la lista de materia prima.");
                tpContenedor.getSelectionModel().select(1);
            } else if (insumo > 0) {
                Tools.AlertMessageWarning(apWindow, "Formula", "No hay insumos seleccionados en la lista.");
                tpContenedor.getSelectionModel().select(1);
            } else {
                short value = Tools.AlertMessageConfirmation(vbBody, "Formula", "¿Estás seguro de continuar?");
                if (value == 1) {
                    ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                        Thread t = new Thread(runnable);
                        t.setDaemon(true);
                        return t;
                    });

                    Task<String> task = new Task<String>() {
                        @Override
                        protected String call() {
                            FormulaTB formulaTB = new FormulaTB();
                            formulaTB.setTitulo(txtTitulo.getText().trim().toUpperCase());
                            formulaTB.setCantidad(Double.parseDouble(txtCantidad.getText()));
                            formulaTB.setIdSuministro(cbProducto.getSelectionModel().getSelectedItem().getIdSuministro());
                            formulaTB.setCostoAdicional(!Tools.isNumeric(txtCostoAdicional.getText()) ? 0 : Double.parseDouble(txtCostoAdicional.getText()));
                            formulaTB.setInstrucciones(txtInstrucciones.getText().trim().toUpperCase());
                            formulaTB.setFecha(Tools.getDate());
                            formulaTB.setHora(Tools.getTime());
                            formulaTB.setIdUsuario(Session.USER_ID);
                            formulaTB.setSuministroTBs(suministroTBs);
                            return FormulaADO.Crud_Formula(formulaTB);
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
                            closeView();
                            onEventLimpiar();
                        });
                        btnAceptarLoad.setOnKeyPressed(event -> {
                            if (event.getCode() == KeyCode.ENTER) {
                                closeView();
                                onEventLimpiar();
                            }
                        });
                        lblMessageLoad.setText(task.getException().getLocalizedMessage());
                        lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                    });
                    task.setOnSucceeded(w -> {
                        String result = task.getValue();
                        if (result.equalsIgnoreCase("inserted")) {
                            lblMessageLoad.setText("Se registró correctamente la formula.");
                            lblMessageLoad.setTextFill(Color.web("#ffffff"));
                            btnAceptarLoad.setVisible(true);
                            btnAceptarLoad.setOnAction(event -> {
                                closeView();
                                onEventLimpiar();
                            });
                            btnAceptarLoad.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    closeView();
                                    onEventLimpiar();
                                }
                            });
                        } else {
                            lblMessageLoad.setText(result);
                            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                            btnAceptarLoad.setVisible(true);
                            btnAceptarLoad.setOnAction(event -> {
                                closeView();
                                onEventLimpiar();
                            });
                            btnAceptarLoad.setOnKeyPressed(event -> {
                                if (event.getCode() == KeyCode.ENTER) {
                                    closeView();
                                    onEventLimpiar();
                                }
                            });
                        }
                    });
                    exec.execute(task);

                    if (!exec.isShutdown()) {
                        exec.shutdown();
                    }
                }

            }
        }
    }

    private boolean validateDuplicateInsumo(TableView<SuministroTB> view, SuministroTB suministroTB) {
        boolean ret = false;
        for (SuministroTB sm : view.getItems()) {
            if (sm.getClave().equals(suministroTB.getClave())) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private void closeView() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(formulaController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(formulaController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(formulaController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(formulaController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(formulaController.getHbWindow());
    }

    private void onEventLimpiar() {
        tpContenedor.getSelectionModel().select(0);
        txtTitulo.clear();
        txtCantidad.clear();
        cbProducto.getItems().clear();
        txtCostoAdicional.clear();
        txtInstrucciones.clear();
        suministroTBs.clear();
        gpList.getChildren().clear();
        vbBody.setDisable(false);
        hbLoad.setVisible(false);
        lblMessageLoad.setText("Cargando datos..."); 
        btnAceptarLoad.setVisible(false);
    }

    @FXML
    private void onKeyPressedRegistrar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventExecute();
        }
    }

    @FXML
    private void onActionRegistrar(ActionEvent event) {
        onEventExecute();
    }

    @FXML
    private void onKeyPressedLimpiar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventLimpiar();
        }
    }

    @FXML
    private void onActionLimpiar(ActionEvent event) {
        onEventLimpiar();
    }

    @FXML
    private void onKeyPressedAddInsumo(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addElementsTableInsumo();
        }
    }

    @FXML
    private void onActionAddInsumo(ActionEvent event) {
        addElementsTableInsumo();
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
    private void onKeyTypedCostoAdicinal(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
            event.consume();
        }
        if (c == '.' && txtCostoAdicional.getText().contains(".")) {
            event.consume();
        }
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeView();
    }

    public void setInitFormulaController(FxFormulaController formulaController) {
        this.formulaController = formulaController;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
