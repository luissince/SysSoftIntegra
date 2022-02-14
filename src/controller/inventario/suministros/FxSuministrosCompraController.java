package controller.inventario.suministros;

import controller.operaciones.compras.FxComprasController;
import controller.inventario.lote.FxLoteProcesoController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DetalleCompraTB;
import model.ImpuestoADO;
import model.ImpuestoTB;
import model.LoteTB;
import model.PreciosADO;
import model.PreciosTB;
import model.SuministroADO;
import model.SuministroTB;

public class FxSuministrosCompraController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private Label lblDescripcion;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtCosto;
    @FXML
    private CheckBox cbPrecio;
    @FXML
    private VBox vbPrecio;
    @FXML
    private ComboBox<ImpuestoTB> cbImpuesto;
    @FXML
    private TextField txtPrecio1;
    @FXML
    private TextField txtPrecio2;
    @FXML
    private TextField txtPrecio3;
    @FXML
    private RadioButton rbPrecioNormal;
    @FXML
    private RadioButton rbPrecioPersonalizado;
    @FXML
    private VBox vbPrecioPersonalizado;
    @FXML
    private TextField txtPrecioVentaNetoPersonalizado;
    @FXML
    private TableView<PreciosTB> tvPrecios;
    @FXML
    private TableColumn<PreciosTB, TextField> tcNombre;
    @FXML
    private TableColumn<PreciosTB, TextField> tcMonto;
    @FXML
    private TableColumn<PreciosTB, TextField> tcFactor;
    @FXML
    private TableColumn<PreciosTB, Button> tcOpcion;
    @FXML
    private VBox vbContenedorPrecioNormal;
    @FXML
    private VBox vbContenedorPreciosPersonalizado;
    @FXML
    private HBox hbPrecioNormal;
    @FXML
    private ComboBox<ImpuestoTB> cbImpuestoCompra;

    private String clave;

    private String descripcion;

    private FxComprasController comprasController;

    private boolean editarSuministros;

    private String idSuminisitro;

    private int indexcompra;

    private ObservableList<LoteTB> loteTBs;

    private ArrayList<PreciosTB> tvPreciosNormal;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        editarSuministros = false;
        idSuminisitro = "";
        indexcompra = 0;
        tvPreciosNormal = new ArrayList();
        cargarComboBox();

        ToggleGroup groupPrecio = new ToggleGroup();
        rbPrecioNormal.setToggleGroup(groupPrecio);
        rbPrecioPersonalizado.setToggleGroup(groupPrecio);

        vbContenedorPreciosPersonalizado.getChildren().remove(vbPrecioPersonalizado);

        initTablePrecios();
    }

    private void initTablePrecios() {
        tcNombre.setCellValueFactory(new PropertyValueFactory<>("txtNombre"));
        tcMonto.setCellValueFactory(new PropertyValueFactory<>("txtValor"));
        tcFactor.setCellValueFactory(new PropertyValueFactory<>("txtFactor"));
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnOpcion"));
    }

    public void cargarComboBox() {
        cbImpuestoCompra.getItems().clear();
        cbImpuestoCompra.getItems().addAll(ImpuestoADO.GetTipoImpuestoCombBox());

        cbImpuesto.getItems().clear();
        cbImpuesto.getItems().addAll(ImpuestoADO.GetTipoImpuestoCombBox());
    }

    public void setLoadData(String idSuministro, boolean lote) {
        this.idSuminisitro = idSuministro;
        SuministroTB suministroTB = SuministroADO.GetSuministroById(idSuministro);
        this.clave = suministroTB.getClave();
        descripcion = suministroTB.getNombreMarca();
        lblDescripcion.setText(clave + " - " + descripcion);
        txtCosto.setText("" + suministroTB.getCostoCompra());

//        loteSuministro = lote;
        for (ImpuestoTB impuestoTB : cbImpuestoCompra.getItems()) {
            if (suministroTB.getIdImpuesto() == impuestoTB.getIdImpuesto()) {
                cbImpuestoCompra.getSelectionModel().select(impuestoTB);
                break;
            }
        }

        for (ImpuestoTB impuestoTB : cbImpuesto.getItems()) {
            if (suministroTB.getIdImpuesto() == impuestoTB.getIdImpuesto()) {
                cbImpuesto.getSelectionModel().select(impuestoTB);
                break;
            }
        }
        if (suministroTB.isTipoPrecio()) {
            rbPrecioNormal.setSelected(true);

            txtPrecio1.setText("" + suministroTB.getPrecioVentaGeneral());
            ObservableList<PreciosTB> preciosTBs = PreciosADO.Get_Lista_Precios_By_IdSuministro(idSuministro);
            if (!preciosTBs.isEmpty()) {
                if (((PreciosTB) preciosTBs.get(0)) != null) {
                    txtPrecio2.setText(Tools.roundingValue(((PreciosTB) preciosTBs.get(0)).getValor(), 4));
                }
                if (((PreciosTB) preciosTBs.get(1)) != null) {
                    txtPrecio3.setText(Tools.roundingValue(((PreciosTB) preciosTBs.get(1)).getValor(), 4));
                }
            }
        } else {
            vbContenedorPrecioNormal.getChildren().remove(hbPrecioNormal);
            rbPrecioPersonalizado.setSelected(true);
            vbContenedorPreciosPersonalizado.getChildren().add(vbPrecioPersonalizado);

            txtPrecioVentaNetoPersonalizado.setText("" + suministroTB.getPrecioVentaGeneral());
            ObservableList<PreciosTB> preciosTBs = PreciosADO.Get_Lista_Precios_By_IdSuministro(idSuministro);
            if (!preciosTBs.isEmpty()) {
                for (int i = 0; i < preciosTBs.size(); i++) {
                    PreciosTB ptb = preciosTBs.get(i);
                    ptb.getBtnOpcion().setOnAction(e -> {
                        executeEventRomeverPrice(ptb);
                    });
                    ptb.getBtnOpcion().setOnKeyPressed(e -> {
                        executeEventRomeverPrice(ptb);
                    });
                    tvPrecios.getItems().add(preciosTBs.get(i));
                }
            }
        }
    }

    public void setLoadEdit(DetalleCompraTB detalleCompraTB, int index, ObservableList<LoteTB> loteTBs) {
        idSuminisitro = detalleCompraTB.getIdSuministro();
        clave = detalleCompraTB.getSuministroTB().getClave();
        descripcion = detalleCompraTB.getSuministroTB().getNombreMarca();
        lblDescripcion.setText(detalleCompraTB.getSuministroTB().getClave() + " - " + detalleCompraTB.getSuministroTB().getNombreMarca());
        txtCantidad.setText("" + detalleCompraTB.getCantidad());

        txtCosto.setText("" + detalleCompraTB.getPrecioCompra());
        cbPrecio.setSelected(!detalleCompraTB.isCambiarPrecio());
        vbPrecio.setDisable(detalleCompraTB.isCambiarPrecio());

        editarSuministros = true;
        indexcompra = index;
        this.loteTBs = loteTBs;

        int impuesto = detalleCompraTB.getIdImpuesto();

        for (ImpuestoTB impuestoTB : cbImpuestoCompra.getItems()) {
            if (impuesto == impuestoTB.getIdImpuesto()) {
                cbImpuestoCompra.getSelectionModel().select(impuestoTB);
                break;
            }
        }

        for (ImpuestoTB impuestoTB : cbImpuesto.getItems()) {
            if (detalleCompraTB.getSuministroTB().getIdImpuesto() == impuestoTB.getIdImpuesto()) {
                cbImpuesto.getSelectionModel().select(impuestoTB);
                break;
            }
        }

        if (detalleCompraTB.getSuministroTB().isTipoPrecio()) {
            txtPrecio1.setText("" + detalleCompraTB.getSuministroTB().getPrecioVentaGeneral());

            ArrayList<PreciosTB> preciosTBs = detalleCompraTB.getSuministroTB().getPreciosTBs();
            if (!preciosTBs.isEmpty()) {
                if (((PreciosTB) preciosTBs.get(0)) != null) {
                    txtPrecio2.setText("" + ((PreciosTB) preciosTBs.get(0)).getValor());
                }
                if (((PreciosTB) preciosTBs.get(1)) != null) {
                    txtPrecio3.setText("" + ((PreciosTB) preciosTBs.get(1)).getValor());
                }
            }
        } else {
            vbContenedorPrecioNormal.getChildren().remove(hbPrecioNormal);
            rbPrecioPersonalizado.setSelected(true);
            vbContenedorPreciosPersonalizado.getChildren().add(vbPrecioPersonalizado);
            txtPrecioVentaNetoPersonalizado.setText("" + detalleCompraTB.getSuministroTB().getPrecioVentaGeneral());

            ArrayList<PreciosTB> preciosTBs = detalleCompraTB.getSuministroTB().getPreciosTBs();
            if (!preciosTBs.isEmpty()) {
                for (int i = 0; i < preciosTBs.size(); i++) {
                    PreciosTB ptb = preciosTBs.get(i);
                    ptb.getBtnOpcion().setOnAction(e -> {
                        executeEventRomeverPrice(ptb);
                    });
                    ptb.getBtnOpcion().setOnKeyPressed(e -> {
                        executeEventRomeverPrice(ptb);
                    });
                    tvPrecios.getItems().add(preciosTBs.get(i));
                }
            }
        }
        txtCantidad.requestFocus();
    }

    private boolean validateStock(TableView<DetalleCompraTB> view, String clave) {
        boolean ret = false;
        for (DetalleCompraTB d : view.getItems()) {
            if (d.getSuministroTB().getClave().equals(clave)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    public void addSuministrosList() throws IOException {
        if (!Tools.isNumeric(txtCantidad.getText())) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese un valor numérico en la cantidad");
            txtCantidad.requestFocus();
        } else if (Double.parseDouble(txtCantidad.getText()) <= 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "La cantidad no puede ser menor o igual a 0");
            txtCantidad.requestFocus();
        } else if (cbImpuestoCompra.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Seleccione un impuesto.");
        } else if (!Tools.isNumeric(txtCosto.getText())) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese un valor numerico en el costo");
            txtCosto.requestFocus();
        } else if (Double.parseDouble(txtCosto.getText()) <= 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "El costo no puede ser menor o igual a 0");
            txtCosto.requestFocus();
        } else if (cbImpuesto.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Compra", "Seleccione el impuesto para el precio de venta");
            cbImpuesto.requestFocus();
        } else {
            if (cbPrecio.isSelected()) {
                addSuministros(false, cbImpuestoCompra.getSelectionModel().getSelectedItem());
            } else {
                if (rbPrecioNormal.isSelected()) {
                    if (!Tools.isNumeric(txtPrecio1.getText().trim())) {
                        Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese un valor numérico en el precio principal");
                        txtPrecio1.requestFocus();
                    } else if (Double.parseDouble(txtPrecio1.getText()) <= 0) {
                        Tools.AlertMessageWarning(apWindow, "Compra", "El precio principal no puede ser menor o igual a 0");
                        txtPrecio1.requestFocus();
                    } else {
                        addSuministros(true, cbImpuestoCompra.getSelectionModel().getSelectedItem());
                    }
                } else {
                    if (!Tools.isNumeric(txtPrecioVentaNetoPersonalizado.getText().trim())) {
                        Tools.AlertMessageWarning(apWindow, "Compra", "Ingrese un valor numérico en el precio principal");
                        txtPrecioVentaNetoPersonalizado.requestFocus();
                    } else if (Double.parseDouble(txtPrecioVentaNetoPersonalizado.getText()) <= 0) {
                        Tools.AlertMessageWarning(apWindow, "Compra", "El precio principal no puede ser menor o igual a 0");
                        txtPrecioVentaNetoPersonalizado.requestFocus();
                    } else {
                        addSuministros(true, cbImpuestoCompra.getSelectionModel().getSelectedItem());
                    }
                }
            }
        }
    }

    private void addSuministros(boolean isPrecio, ImpuestoTB impuestoTB) {
        DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
        detalleCompraTB.setId(editarSuministros ? indexcompra + 1 : comprasController.getTvList().getItems().size() + 1);
        detalleCompraTB.setIdSuministro(idSuminisitro);
        detalleCompraTB.setCambiarPrecio(isPrecio);
        detalleCompraTB.setIdImpuesto(impuestoTB.getIdImpuesto());
        detalleCompraTB.setDescripcion("");
        detalleCompraTB.setCantidad(Double.parseDouble(txtCantidad.getText()));
        detalleCompraTB.setDescuento(0);
        detalleCompraTB.setPrecioCompra(Double.parseDouble(txtCosto.getText()));

        detalleCompraTB.setImpuestoTB(impuestoTB);

        //SUMINISTRO
        SuministroTB suministrosTB = new SuministroTB();
        suministrosTB.setClave(clave);
        suministrosTB.setNombreMarca(descripcion);

        //SECCIÓN DE LOS PRECIOS
        double precioValidado = rbPrecioNormal.isSelected()
                ? Tools.isNumeric(txtPrecio1.getText()) ? Double.parseDouble(txtPrecio1.getText()) : 0
                : Tools.isNumeric(txtPrecioVentaNetoPersonalizado.getText()) ? Double.parseDouble(txtPrecioVentaNetoPersonalizado.getText()) : 0;
        suministrosTB.setPrecioVentaGeneral(precioValidado);
        suministrosTB.setIdImpuesto(cbImpuesto.getSelectionModel().getSelectedItem().getIdImpuesto());
        suministrosTB.setTipoPrecio(rbPrecioNormal.isSelected());
        tvPreciosNormal.add(new PreciosTB(0, "Precio de Venta 1", !Tools.isNumeric(txtPrecio2.getText()) ? 0 : Double.parseDouble(txtPrecio2.getText()), 1));
        tvPreciosNormal.add(new PreciosTB(0, "Precio de Venta 2", !Tools.isNumeric(txtPrecio3.getText()) ? 0 : Double.parseDouble(txtPrecio3.getText()), 1));
        suministrosTB.setPreciosTBs(rbPrecioNormal.isSelected() ? tvPreciosNormal : new ArrayList<>(tvPrecios.getItems()));
        detalleCompraTB.setSuministroTB(suministrosTB);

//      detalleCompraTB.setLote(loteSuministro);
        Button btnRemove = new Button("X");
        btnRemove.getStyleClass().add("buttonDark");
        detalleCompraTB.setBtnRemove(btnRemove);

        if (!validateStock(comprasController.getTvList(), detalleCompraTB.getSuministroTB().getClave()) && !editarSuministros) {
            comprasController.addSuministroToTable(detalleCompraTB);
            comprasController.calculateTotals();
            Tools.Dispose(apWindow);

        } else if (editarSuministros) {
            comprasController.editSuministroToTable(indexcompra, detalleCompraTB);
            comprasController.calculateTotals();
            Tools.Dispose(apWindow);
        } else {
            Tools.AlertMessageWarning(apWindow, "Compra", "Ya hay un producto con las mismas características.");
        }
    }

    private void openWindowLote(SuministroTB suministroTB) {
        try {
            URL url = getClass().getResource(FilesRouters.FX_LOTE_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxLoteProcesoController controller = fXMLLoader.getController();
            controller.setSuministrosLoteController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar Lote", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
//            if (!loteedit) {
//                controller.setLoadData(suministroTB.getIdSuministro(), suministroTB.getClave(),
//                        suministroTB.getNombreMarca(),
//                        String.valueOf(suministroTB.getCantidad()));
//            } else {
//                controller.setEditData(new String[]{suministroTB.getIdSuministro(), suministroTB.getClave(),
//                    suministroTB.getNombreMarca(),
//                    String.valueOf(suministroTB.getCantidad())},
//                        loteTBs);
//            }
        } catch (IOException ex) {
            System.out.println("Suministros controller" + ex.getLocalizedMessage());
        }
    }

    private void addElementsTablePrecios() {
        PreciosTB precios = new PreciosTB();
        precios.setId(tvPrecios.getItems().isEmpty() ? 1 : tvPrecios.getItems().size() + 1);
        precios.setNombre("Precio " + precios.getId());
        precios.setValor(Double.parseDouble("0.00"));
        precios.setFactor(Double.parseDouble("1.00"));

        TextField tfNombre = new TextField("Precio " + precios.getId());
        tfNombre.getStyleClass().add("text-field-normal");
        tfNombre.setOnKeyReleased(event -> {
            precios.setNombre(tfNombre.getText());
        });
        precios.setTxtNombre(tfNombre);

        TextField tfValor = new TextField("0.00");
        tfValor.getStyleClass().add("text-field-normal");
        tfValor.setOnKeyReleased(event -> {
            precios.setValor(!Tools.isNumeric(tfValor.getText()) ? 0 : Double.parseDouble(tfValor.getText()));
        });
        tfValor.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && tfValor.getText().contains(".")) {
                event.consume();
            }
        });

        precios.setTxtValor(tfValor);

        TextField tfFactor = new TextField("1.00");
        tfFactor.getStyleClass().add("text-field-normal");
        tfFactor.setOnKeyReleased(event -> {
            precios.setFactor(!Tools.isNumeric(tfFactor.getText()) ? 1 : Double.parseDouble(tfFactor.getText()));
        });
        tfFactor.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && tfFactor.getText().contains(".")) {
                event.consume();
            }
        });
        precios.setTxtFactor(tfFactor);

        Button button = new Button();
        button.getStyleClass().add("buttonDark");
        ImageView view = new ImageView(new Image("/view/image/remove.png"));
        view.setFitWidth(22);
        view.setFitHeight(22);
        button.setGraphic(view);
        button.setOnAction(event -> {
            executeEventRomeverPrice(precios);
        });
        button.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                executeEventRomeverPrice(precios);
            }
        });
        precios.setBtnOpcion(button);
        precios.setEstado(true);
        tvPrecios.getItems().add(precios);
    }

    private void executeEventRomeverPrice(PreciosTB preciosTB) {
        short confirmation = Tools.AlertMessageConfirmation(apWindow, "Precios", "¿Esta seguro de quitar el precio?");
        if (confirmation == 1) {
            ObservableList<PreciosTB> observableList;
            observableList = tvPrecios.getItems();
            observableList.remove(preciosTB);
            tvPrecios.requestFocus();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) throws IOException {
        addSuministrosList();
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            addSuministrosList();
        }
    }

    @FXML
    private void onActionCancel(ActionEvent event) {
        Tools.Dispose(apWindow);
    }

    @FXML
    private void onKeyPressedCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(apWindow);
        }
    }

    @FXML
    private void onActionPrecio(ActionEvent event) {
        vbPrecio.setDisable(!cbPrecio.isSelected());
    }

    @FXML
    private void onKeyTypedCantidad(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtCantidad.getText().contains(".") || c == '-' && txtCantidad.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedCosto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtCosto.getText().contains(".") || c == '-' && txtCosto.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecio1(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecio1.getText().contains(".") || c == '-' && txtPrecio1.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecio2(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecio2.getText().contains(".") || c == '-' && txtPrecio2.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecio3(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecio3.getText().contains(".") || c == '-' && txtPrecio3.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecioPersonalizado(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecioVentaNetoPersonalizado.getText().contains(".") || c == '-' && txtPrecioVentaNetoPersonalizado.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onActionRbListaPrecios(ActionEvent event) {
        if (rbPrecioNormal.isSelected()) {
            vbContenedorPrecioNormal.getChildren().add(hbPrecioNormal);
            vbContenedorPreciosPersonalizado.getChildren().remove(vbPrecioPersonalizado);
        } else {
            vbContenedorPreciosPersonalizado.getChildren().add(vbPrecioPersonalizado);
            vbContenedorPrecioNormal.getChildren().remove(hbPrecioNormal);
        }
    }

    @FXML
    private void onKeyPressedNew(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addElementsTablePrecios();
        }
    }

    @FXML
    private void onActionNew(ActionEvent event) {
        addElementsTablePrecios();
    }

    public TextField getTxtCantidad() {
        return txtCantidad;
    }

    public void setValidarlote(boolean loteSuministro) {
//        this.loteSuministro = loteSuministro;
    }

    public void setInitComprasController(FxComprasController comprasController) {
        this.comprasController = comprasController;
    }

    public FxComprasController getComprasController() {
        return comprasController;
    }

}
