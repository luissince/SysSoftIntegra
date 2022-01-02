package controller.produccion.producir;

import controller.menus.FxPrincipalController;
import controller.tools.Tools;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.FormulaADO;
import model.FormulaTB;
import model.SuministroTB;

public class FxFormulaVisualizarController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblTitulo;
    @FXML
    private Text lblProduccion;
    @FXML
    private Text lblCreado;
    @FXML
    private Text lblFechaCreacion;
    @FXML
    private Text lblCostoAdicional;
    @FXML
    private GridPane gpList;
    @FXML
    private ScrollPane spBody;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;

    private FxFormulaController fxFormulaController;

    private FxPrincipalController fxPrincipalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void setInitComponents(String idFormula) {
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
            spBody.setDisable(true);
            hbLoad.setVisible(true);
            lblMessageLoad.setText("Cargando datos...");
            lblMessageLoad.setTextFill(Color.web("#ffffff"));
            btnAceptarLoad.setVisible(false);
        });
        task.setOnFailed(w -> {
            lblMessageLoad.setText(task.getException().getLocalizedMessage());
            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
            btnAceptarLoad.setVisible(true);
            btnAceptarLoad.setOnAction(event -> {
                closeView();
            });
            btnAceptarLoad.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    closeView();
                }
                event.consume();
            });
        });
        task.setOnSucceeded(w -> {
            Object object = task.getValue();
            if (object instanceof FormulaTB) {
                FormulaTB formulaTB = (FormulaTB) object;
                lblTitulo.setText("FORMULA DE PRODUCCIÓN - " + formulaTB.getTitulo());
                lblProduccion.setText(Tools.roundingValue(formulaTB.getCantidad(), 2) + " " + formulaTB.getSuministroTB().getUnidadCompraName() + " - " + formulaTB.getSuministroTB().getNombreMarca());
                lblCreado.setText(formulaTB.getEmpleadoTB().getApellidos() + " " + formulaTB.getEmpleadoTB().getNombres());
                lblFechaCreacion.setText(formulaTB.getFecha() + " - " + formulaTB.getHora());
                lblCostoAdicional.setText(Tools.roundingValue(formulaTB.getCostoAdicional(), 2));
                fillTableDetalleFormula(formulaTB.getSuministroTBs());
                spBody.setDisable(false);
                hbLoad.setVisible(false);
            } else {
                lblMessageLoad.setText((String) object);
                lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                btnAceptarLoad.setVisible(true);
                btnAceptarLoad.setOnAction(event -> {
                    closeView();
                });
                btnAceptarLoad.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        closeView();
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

    private void fillTableDetalleFormula(ArrayList<SuministroTB> suministroTBs) {
        for (int i = 0; i < suministroTBs.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), suministroTBs.get(i).getId() + "", Pos.CENTER), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), suministroTBs.get(i).getNombreMarca() + "", Pos.CENTER_LEFT), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), Tools.roundingValue(suministroTBs.get(i).getCantidad(), 2) + "", Pos.CENTER), 2, (i + 1));
            gpList.add(addElementGridPane("l4" + (i + 1), suministroTBs.get(i).getUnidadCompraName() + "", Pos.CENTER_LEFT), 3, (i + 1));
        }
    }

    private Label addElementGridPane(String id, String nombre, Pos pos) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setStyle("-fx-text-fill:#020203;-fx-background-color: #dddddd;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void closeView() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(fxFormulaController.getHbWindow(), 0d);
        AnchorPane.setTopAnchor(fxFormulaController.getHbWindow(), 0d);
        AnchorPane.setRightAnchor(fxFormulaController.getHbWindow(), 0d);
        AnchorPane.setBottomAnchor(fxFormulaController.getHbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(fxFormulaController.getHbWindow());
    }

    @FXML
    private void onMouseClickedBehind(MouseEvent event) {
        closeView();
    }

    public void setInitFormulaController(FxFormulaController fxFormulaController, FxPrincipalController fxPrincipalController) {
        this.fxFormulaController = fxFormulaController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
