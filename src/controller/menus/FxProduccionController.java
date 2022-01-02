package controller.menus;

import controller.produccion.producir.FxFormulaController;
import controller.produccion.producir.FxMermaController;
import controller.produccion.producir.FxProducirController;
import controller.tools.FilesRouters;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class FxProduccionController implements Initializable {

    @FXML
    private HBox hbWindow;
    @FXML
    private TextField txtSearch;

    /*
    Objectos de la ventana principal y venta que agrega a los hijos
     */
    private FxPrincipalController fxPrincipalController;

    /*
    Controller producir     
     */
    private FXMLLoader fXMLProducir;

    private HBox nodeProducir;

    private FxProducirController controllerProducir;

    /*
    Controller formula     
     */
    private FXMLLoader fXMLFormula;

    private HBox nodeFormula;

    private FxFormulaController controllerFormula;

    /*
    Controller merma     
     */
    private FXMLLoader fXMLMerma;

    private HBox nodeMerma;

    private FxMermaController controllerMerma;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            fXMLProducir = new FXMLLoader(getClass().getResource(FilesRouters.FX_PRODUCIR));
            nodeProducir = fXMLProducir.load();
            controllerProducir = fXMLProducir.getController();

            fXMLFormula = new FXMLLoader(getClass().getResource(FilesRouters.FX_FORMULA));
            nodeFormula = fXMLFormula.load();
            controllerFormula = fXMLFormula.getController();

            fXMLMerma = new FXMLLoader(getClass().getResource(FilesRouters.FX_MERMA));
            nodeMerma = fXMLMerma.load();
            controllerMerma = fXMLMerma.getController();
        } catch (IOException ex) {
            System.out.println("Error IOException:" + ex.getLocalizedMessage());
        } catch (Exception ex) {
            System.out.println("Error Exception:" + ex.getLocalizedMessage());
        }
    }

    private void openWindowProducir() {
        controllerProducir.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeProducir, 0d);
        AnchorPane.setTopAnchor(nodeProducir, 0d);
        AnchorPane.setRightAnchor(nodeProducir, 0d);
        AnchorPane.setBottomAnchor(nodeProducir, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeProducir);
    }

    private void openWindowFormula() {
        controllerFormula.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeFormula, 0d);
        AnchorPane.setTopAnchor(nodeFormula, 0d);
        AnchorPane.setRightAnchor(nodeFormula, 0d);
        AnchorPane.setBottomAnchor(nodeFormula, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeFormula);
    }

    private void openWindowMerma() {
        controllerMerma.setContent(fxPrincipalController);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(nodeMerma, 0d);
        AnchorPane.setTopAnchor(nodeMerma, 0d);
        AnchorPane.setRightAnchor(nodeMerma, 0d);
        AnchorPane.setBottomAnchor(nodeMerma, 0d);
        fxPrincipalController.getVbContent().getChildren().add(nodeMerma);
    }

    @FXML
    private void onKeyPressedProducir(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowProducir();
        }
    }

    @FXML
    private void onActionProducir(ActionEvent event) {
        openWindowProducir();
    }

    @FXML
    private void onKeyPressedFormula(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowFormula();
        }
    }

    @FXML
    private void onActionFormula(ActionEvent event) {
        openWindowFormula();
    }

    @FXML
    private void onKeyPressedMerma(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowMerma();
        }
    }

    @FXML
    private void onActionMerma(ActionEvent event) {
        openWindowMerma();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
