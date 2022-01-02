package controller.inventario.suministros;

import controller.tools.Tools;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.Initializable;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.output.OutputException;

public class FxCodigoBarrasController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private ImageView ivCodigo;
    @FXML
    private ComboBox<String> cbCodificacion;

    private String codigo;

    private FxSuministrosProcesoController suministrosProcesoController;

    private FxSuministrosProcesoModalController suministrosProcesoModalController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        cbCodificacion.getItems().addAll("Code bar", "Code 128");
        cbCodificacion.getSelectionModel().select(0);
        generarCodigo();
    }

    private void generarCodigo() {
        if (cbCodificacion.getSelectionModel().getSelectedIndex() == 0 || cbCodificacion.getSelectionModel().getSelectedIndex() == 1) {
            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;
            int dig7 = rd.nextInt(9000000) + 1000000;
            codigo = Integer.toString(dig5) + Integer.toString(dig7);

            try {

                Barcode barCode;

                if (cbCodificacion.getSelectionModel().getSelectedIndex() == 0) {
                    barCode = BarcodeFactory.createCodabar(codigo);
                } else {
                    barCode = BarcodeFactory.createCode128(codigo);
                }

//                File file = new File("C:/Users/Aleza/Desktop/temp.png");
                barCode.setBarHeight(50);
                barCode.setDrawingText(true);
                BufferedImage bufferedImage = new BufferedImage((int) ivCodigo.getFitWidth(), (int) ivCodigo.getFitHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = bufferedImage.createGraphics();
                barCode.draw((Graphics2D) graphics,
                        (bufferedImage.getWidth() - barCode.getWidth()) / 2,
                        (bufferedImage.getHeight() - barCode.getHeight()) / 2);

                WritableImage wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                PixelWriter pw = wr.getPixelWriter();
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        pw.setArgb(x, y, bufferedImage.getRGB(x, y));
                    }
                }
                ivCodigo.setImage(wr);
//                boolean result = ImageIO.write(bufferedImage, "png", file);
//                if (!result) {
//                   
//                }
            } catch (BarcodeException | OutputException ex) {
                Logger.getLogger(FxCodigoBarrasController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            Tools.AlertMessageWarning(window, "Articulo", "Seleccione un tipo de codigo de barras.");
        }
    }

    @FXML
    private void onActionGenerar(ActionEvent event) throws IOException {
        generarCodigo();
    }

    @FXML
    private void onKeyPressedGenerar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            generarCodigo();
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        cargarCodigo();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            cargarCodigo();
        }
    }

    private void cargarCodigo() {
        if (suministrosProcesoController != null) {
            if (codigo != null && (cbCodificacion.getSelectionModel().getSelectedIndex() == 0 || cbCodificacion.getSelectionModel().getSelectedIndex() == 1)) {
                suministrosProcesoController.getTxtClave().setText(codigo);
                Tools.Dispose(window);
            } else {
                Tools.AlertMessageWarning(window, "Suministro", "No genero el codigo de barras o no selecciono un tipo de codigo de barras.");
            }
        } else if (suministrosProcesoModalController != null) {
            if (codigo != null && (cbCodificacion.getSelectionModel().getSelectedIndex() == 0 || cbCodificacion.getSelectionModel().getSelectedIndex() == 1)) {
                suministrosProcesoModalController.getTxtClave().setText(codigo);
                Tools.Dispose(window);
            } else {
                Tools.AlertMessageWarning(window, "Suministro", "No genero el codigo de barras o no selecciono un tipo de codigo de barras.");
            }
        }

    }

    public void setControllerSuministro(FxSuministrosProcesoController suministrosProcesoController) {
        this.suministrosProcesoController = suministrosProcesoController;
    }

    public void setControllerSuministroModal(FxSuministrosProcesoModalController suministrosProcesoModalController) {
        this.suministrosProcesoModalController = suministrosProcesoModalController;
    }

}
