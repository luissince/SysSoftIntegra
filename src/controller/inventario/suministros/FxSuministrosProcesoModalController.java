package controller.inventario.suministros;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import controller.configuracion.tablasbasicas.FxDetalleListaController;
import controller.tools.FilesRouters;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DetalleADO;
import model.DetalleTB;
import model.ImpuestoADO;
import model.ImpuestoTB;
import model.PreciosTB;
import model.SuministroADO;
import model.SuministroTB;

public class FxSuministrosProcesoModalController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private TextField txtClave;
    @FXML
    private TextField txtClaveAlterna;
    @FXML
    private TextField txtNombreMarca;
    @FXML
    private TextField txtNombreGenerico;
    @FXML
    private TextField txtCategoria;
    @FXML
    private TextField txtMarca;
    @FXML
    private TextField txtPresentacion;
    @FXML
    private ComboBox<DetalleTB> cbEstado;
    @FXML
    private ImageView lnPrincipal;
    @FXML
    private Button btnRegister;
    @FXML
    private TextField txtStockMinimo;
    @FXML
    private TextField txtStockMaximo;
    @FXML
    private TextField txtCosto;
    @FXML
    private CheckBox cbLote;
    @FXML
    private CheckBox cbInventario;
    @FXML
    private RadioButton rbUnidad;
    @FXML
    private RadioButton rbMedida;
    @FXML
    private RadioButton rbGranel;
    @FXML
    private TextField txtMedida;
    @FXML
    private TextField txtPrecioVentaNeto1;
    @FXML
    private TextField txtPrecioVentaNeto2;
    @FXML
    private TextField txtPrecioVentaNeto3;
    @FXML
    private ComboBox<ImpuestoTB> cbImpuesto;
    @FXML
    private VBox vbInventario;
    @FXML
    private RadioButton rbValorUnidad;
    @FXML
    private RadioButton rbValorCosto;
    @FXML
    private RadioButton rbValorMedida;
    @FXML
    private TextField txtClaveSat;
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
    private RadioButton rbPrecioNormal;
    @FXML
    private RadioButton rbPrecioPersonalizado;
    @FXML
    private VBox vbContenedorPrecioNormal;
    @FXML
    private VBox vbContenedorPreciosPersonalizado;
    @FXML
    private HBox hbPrecioNormal;
    @FXML
    private VBox vbPrecioPersonalizado;
    @FXML
    private TextField txtPrecioVentaNetoPersonalizado;
    @FXML
    private TabPane tpContenedor;
    @FXML
    private RadioButton rbTodoModulos;
    @FXML
    private RadioButton rbModuloVentas;
    @FXML
    private RadioButton rbModuloProduccion;
    @FXML
    private TextArea txtDescripcion;

    private String idSuministro;

    private File selectFile;

    private byte[] imageBytes;
    //private String selectImage;

    private int idPresentacion;

    private int idCategoria;

    private int idMarca;

    private int idMedida;

//    private boolean articulo;
    private boolean estadoOrigen;

    private ObservableList<PreciosTB> tvPreciosNormal;

    private SingleSelectionModel<Tab> selectionModel = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(apWindow, KeyEvent.KEY_RELEASED);
        selectionModel = tpContenedor.getSelectionModel();
        ToggleGroup groupVende = new ToggleGroup();
        ToggleGroup groupValor = new ToggleGroup();
        ToggleGroup groupPrecio = new ToggleGroup();
        ToggleGroup groupUso = new ToggleGroup();

        rbUnidad.setToggleGroup(groupVende);
        rbMedida.setToggleGroup(groupVende);
        rbGranel.setToggleGroup(groupVende);

        rbValorUnidad.setToggleGroup(groupValor);
        rbValorCosto.setToggleGroup(groupValor);
        rbValorMedida.setToggleGroup(groupValor);

        rbPrecioNormal.setToggleGroup(groupPrecio);
        rbPrecioPersonalizado.setToggleGroup(groupPrecio);

        rbTodoModulos.setToggleGroup(groupUso);
        rbModuloVentas.setToggleGroup(groupUso);
        rbModuloProduccion.setToggleGroup(groupUso);

        tvPreciosNormal = FXCollections.observableArrayList();
        initTablePrecios();
    }

    public void setInitArticulo() {
        clearElements();
        rbPrecioNormal.setSelected(true);
        txtPrecioVentaNeto2.setId("" + 0);
        txtPrecioVentaNeto3.setId("" + 0);
        vbContenedorPrecioNormal.getChildren().add(hbPrecioNormal);
        setIniciarCarga();
        txtClave.requestFocus();
    }

    public void clearElements() {
        idSuministro = "";
        tvPreciosNormal.clear();
        idMedida = 0;
        idPresentacion = 0;
        idCategoria = 0;
        idMarca = 0;
        selectFile = null;
        imageBytes = null;
        lnPrincipal.setImage(new Image("/view/image/no-image.png"));
        lnPrincipal.setFitWidth(160);
        lnPrincipal.setFitHeight(160);
        btnRegister.setText("Registrar");
        btnRegister.getStyleClass().clear();
        btnRegister.getStyleClass().add("buttonLightDefault");

//        cbAceptar.setSelected(true);
//        cbAceptar.setVisible(true);
        txtClave.clear();
        txtClaveAlterna.clear();
        txtNombreMarca.clear();
        txtMedida.clear();
        txtCategoria.clear();
        rbUnidad.setSelected(true);
        txtStockMinimo.clear();
        txtStockMaximo.clear();
        cbImpuesto.getItems().clear();
        cbInventario.setSelected(false);
        vbInventario.setDisable(true);
        txtCosto.clear();
        rbValorUnidad.setSelected(true);
        txtNombreGenerico.clear();
        cbEstado.getItems().clear();
        txtMarca.clear();
        txtPresentacion.clear();
        cbLote.setSelected(false);
        txtClaveSat.clear();
        txtPrecioVentaNetoPersonalizado.clear();
        tvPrecios.getItems().clear();
        vbContenedorPrecioNormal.getChildren().remove(hbPrecioNormal);
        vbContenedorPreciosPersonalizado.getChildren().remove(vbPrecioPersonalizado);
    }

    private void initTablePrecios() {
        tcNombre.setCellValueFactory(new PropertyValueFactory<>("txtNombre"));
        tcMonto.setCellValueFactory(new PropertyValueFactory<>("txtValor"));
        tcFactor.setCellValueFactory(new PropertyValueFactory<>("txtFactor"));
        tcOpcion.setCellValueFactory(new PropertyValueFactory<>("btnOpcion"));
    }

    private void openAlertMessageWarning(String message) {
        Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Producto", message, false);
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
        button.getStyleClass().add("buttonLightWarning");
        ImageView view = new ImageView(new Image("/view/image/remove-black.png"));
        view.setFitWidth(24);
        view.setFitHeight(24);
        button.setGraphic(view);
        button.setOnAction(event -> {
            executeEventRomeverPrice(precios);
        });
        button.setOnKeyPressed(event -> {
            executeEventRomeverPrice(precios);
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

    private void loadEventos(ArrayList<Object> objects) {
        cbImpuesto.getItems().clear();
        List<ImpuestoTB> list1 = (List<ImpuestoTB>) objects.get(1);
        list1.forEach(e -> {
            cbImpuesto.getItems().add(new ImpuestoTB(e.getIdImpuesto(), e.getNombre(), e.getValor(), e.isPredeterminado()));
        });

        cbEstado.getItems().clear();
        ObservableList<DetalleTB> list2 = (ObservableList<DetalleTB>) objects.get(2);
        list2.forEach(e -> {
            cbEstado.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
        });
        cbEstado.getSelectionModel().select(0);
    }

    public void setIniciarCarga() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            public ArrayList<Object> call() {
                ArrayList<Object> arrayList = new ArrayList<>();
                arrayList.add(DetalleADO.Get_Detail_IdName("1", "0016", "SUB PRODUCTO(S)"));
                arrayList.add(ImpuestoADO.GetTipoImpuestoCombBox());
                arrayList.add(DetalleADO.Get_Detail_IdName("2", "0001", ""));
                return arrayList;
            }
        };

        task.setOnSucceeded(w -> {
            ArrayList<Object> objects = task.getValue();
            loadEventos(objects);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private void aValidityProcess() {
        //primera validacion
        if (txtClave.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese la clave del producto, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtClave.requestFocus();
        } else if (txtNombreMarca.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese el nombre del producto, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtNombreMarca.requestFocus();
        } else if (txtMedida.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese el nombre de la unidad de medida, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtMedida.requestFocus();
        } else if (idMedida <= 0) {
            openAlertMessageWarning("Ingrese el nombre de la unidad de medida, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtMedida.requestFocus();
        } else if (txtCategoria.getText().isEmpty()) {
            openAlertMessageWarning("Ingrese el nombre de la categoría, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtCategoria.requestFocus();
        } else if (idCategoria <= 0) {
            openAlertMessageWarning("Ingrese el nombre de la categoría, por favor.");
            if (!selectionModel.isSelected(0)) {
                selectionModel.select(0);
            }
            txtCategoria.requestFocus();
        } else if (!estadoOrigen && cbImpuesto.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Seleccione el impuesto, por favor.");
            if (!selectionModel.isSelected(1)) {
                selectionModel.select(1);
            }
            cbImpuesto.requestFocus();
        }//segunda validacion de lista de precio normal 
        else if (!estadoOrigen && rbPrecioNormal.isSelected() && !Tools.isNumeric(txtPrecioVentaNeto1.getText())) {
            openAlertMessageWarning("Ingrese el primer precio de venta, por favor.");
            if (!selectionModel.isSelected(1)) {
                selectionModel.select(1);
            }
            txtPrecioVentaNeto1.requestFocus();
        } else if (!estadoOrigen && rbPrecioNormal.isSelected() && Double.parseDouble(txtPrecioVentaNeto1.getText()) <= 0) {
            openAlertMessageWarning("El precio de venta no puede ser menor o igual a 0, por favor.");
            if (!selectionModel.isSelected(1)) {
                selectionModel.select(1);
            }
            txtPrecioVentaNeto1.requestFocus();
        }//segunda validación de lista de precios personalizado 
        else if (!estadoOrigen && rbPrecioPersonalizado.isSelected() && !Tools.isNumeric(txtPrecioVentaNetoPersonalizado.getText())) {
            openAlertMessageWarning("Ingrese el precio de venta, por favor.");
            if (!selectionModel.isSelected(1)) {
                selectionModel.select(1);
            }
            txtPrecioVentaNetoPersonalizado.requestFocus();
        } else if (!estadoOrigen && rbPrecioPersonalizado.isSelected() && Double.parseDouble(txtPrecioVentaNetoPersonalizado.getText()) <= 0) {
            openAlertMessageWarning("El precio de venta no puede ser menor o igual a 0, por favor.");
            if (!selectionModel.isSelected(1)) {
                selectionModel.select(1);
            }
            txtPrecioVentaNetoPersonalizado.requestFocus();
        } else if (cbInventario.isSelected()) {
            if (!estadoOrigen && !Tools.isNumeric(txtCosto.getText())) {
                openAlertMessageWarning("Ingrese el costo del producto, por favor.");
                if (!selectionModel.isSelected(1)) {
                    selectionModel.select(1);
                }
                txtCosto.requestFocus();
            } else if (!estadoOrigen && Double.parseDouble(txtCosto.getText()) < 0) {
                openAlertMessageWarning("El costo del producto no puede ser menor que 0, por favor.");
                if (!selectionModel.isSelected(1)) {
                    selectionModel.select(1);
                }
                txtCosto.requestFocus();
            } else if (cbEstado.getSelectionModel().getSelectedIndex() < 0) {
                openAlertMessageWarning("Selecciona el estado del producto, por favor.");
                if (!selectionModel.isSelected(2)) {
                    selectionModel.select(2);
                }
                cbEstado.requestFocus();
            } else {
                crudProducto();
            }
        } else if (cbEstado.getSelectionModel().getSelectedIndex() < 0) {
            openAlertMessageWarning("Selecciona el estado del producto, por favor.");
            if (!selectionModel.isSelected(2)) {
                selectionModel.select(2);
            }
            cbEstado.requestFocus();
        } else {
            crudProducto();
        }
    }

    private void crudProducto() {
        short confirmation = Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.CONFIRMATION, "Movimiento", "¿Está seguro de continuar?", true);
        if (confirmation == 1) {
            SuministroTB suministroTB = new SuministroTB();
            suministroTB.setIdSuministro(idSuministro);
            suministroTB.setOrigen(rbTodoModulos.isSelected() ? 1 : rbModuloVentas.isSelected() ? 2 : 3);
            suministroTB.setClave(txtClave.getText().trim());
            suministroTB.setClaveAlterna(txtClaveAlterna.getText().trim());
            suministroTB.setNombreMarca(txtNombreMarca.getText().trim());
            suministroTB.setNombreGenerico(txtNombreGenerico.getText().trim());
            suministroTB.setImagenFile(selectFile);
            suministroTB.setNuevaImagen(imageBytes != null ? imageBytes
                    : selectFile == null
                            ? null
                            : Tools.getImageBytes(selectFile));
            //suministroTB.setImagenTB(selectImage);
            suministroTB.setCategoria(idCategoria != 0
                    ? idCategoria
                    : 0);
            suministroTB.setMarcar(idMarca != 0
                    ? idMarca
                    : 0);
            suministroTB.setUnidadCompra(idMedida != 0
                    ? idMedida
                    : 0);
            suministroTB.setPresentacion(idPresentacion != 0
                    ? idPresentacion
                    : 0);

            suministroTB.setStockMinimo(Tools.isNumeric(txtStockMinimo.getText())
                    ? Double.parseDouble(txtStockMinimo.getText().trim())
                    : 0);

            suministroTB.setStockMaximo(Tools.isNumeric(txtStockMaximo.getText())
                    ? Double.parseDouble(txtStockMaximo.getText().trim())
                    : 0);

            //agregar lista de precios
            suministroTB.setCostoCompra(Tools.isNumeric(txtCosto.getText())
                    ? Double.parseDouble(txtCosto.getText())
                    : 0);

            double precioValidado = rbPrecioNormal.isSelected()
                    ? Tools.isNumeric(txtPrecioVentaNeto1.getText()) ? Double.parseDouble(txtPrecioVentaNeto1.getText()) : 0
                    : Tools.isNumeric(txtPrecioVentaNetoPersonalizado.getText()) ? Double.parseDouble(txtPrecioVentaNetoPersonalizado.getText()) : 0;
            suministroTB.setPrecioVentaGeneral(precioValidado);

            suministroTB.setEstado(cbEstado.getSelectionModel().getSelectedIndex() >= 0
                    ? cbEstado.getSelectionModel().getSelectedItem().getIdDetalle()
                    : 0);

            int se_vende;

            if (rbUnidad.isSelected()) {
                se_vende = 1;
            } else if (rbGranel.isSelected()) {
                se_vende = 2;
            } else {
                se_vende = 3;
            }

            suministroTB.setUnidadVenta(se_vende);
            suministroTB.setLote(cbLote.isSelected());
            suministroTB.setInventario(cbInventario.isSelected());
            suministroTB.setValorInventario(rbValorUnidad.isSelected() ? (short) 1 : rbValorCosto.isSelected() ? (short) 2 : (short) 3);
            suministroTB.setIdImpuesto(cbImpuesto.getSelectionModel().getSelectedIndex() >= 0 ? cbImpuesto.getSelectionModel().getSelectedItem().getIdImpuesto() : 0);
            suministroTB.setClaveSat(txtClaveSat.getText().trim());
            suministroTB.setTipoPrecio(rbPrecioNormal.isSelected());
            suministroTB.setDescripcion(txtDescripcion.getText().trim());

            tvPreciosNormal.add(new PreciosTB(Integer.parseInt(txtPrecioVentaNeto2.getId()), "Precio de Venta 1", !Tools.isNumeric(txtPrecioVentaNeto2.getText()) ? 0 : Double.parseDouble(txtPrecioVentaNeto2.getText()), 1));
            tvPreciosNormal.add(new PreciosTB(Integer.parseInt(txtPrecioVentaNeto3.getId()), "Pricio de Venta 2", !Tools.isNumeric(txtPrecioVentaNeto3.getText()) ? 0 : Double.parseDouble(txtPrecioVentaNeto3.getText()), 1));

            String result = SuministroADO.CrudSuministro(suministroTB,
                    rbPrecioNormal.isSelected() ? tvPreciosNormal : tvPrecios.getItems());
            switch (result) {
                case "registered":
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.INFORMATION, "Producto", "Registrado correctamente el producto.", false);
                    closeWindow();
                    break;
                case "updated":
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.INFORMATION, "Producto", "Actualizado correctamente el producto.", false);
                    closeWindow();
                    break;
                case "duplicate":
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Producto", "No se puede haber 2 producto con la misma clave.", false);
                    txtClave.requestFocus();
                    break;
                case "duplicatename":
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Producto", "No se puede haber 2 producto con el mismo nombre.", false);
                    txtNombreMarca.requestFocus();
                    break;
                case "mayor":
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.WARNING, "Producto", "No se puede desleccionar el lote, ya que el producto cuenta con unidades.", false);
                    cbLote.requestFocus();
                    break;
                default:
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.ERROR, "Producto", result, false);
                    break;
            }
        }
    }

    private void openWindowDetalle(String title, String idDetalle, boolean valor) {
        try {
            URL url = getClass().getResource(FilesRouters.FX_DETALLE_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxDetalleListaController controller = fXMLLoader.getController();
            controller.setControllerSuministroModal(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, title, apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();

            stage.show();
            if (valor == true) {
                controller.initListNameImpuesto(idDetalle);
            } else {
                controller.initListDetalle(idDetalle, "");
            }
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

    }

    private void openWindowGerarCodigoBarras() {
        try {
            URL url = getClass().getResource(FilesRouters.FX_CODIGO_BARRAS);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxCodigoBarrasController controller = fXMLLoader.getController();
            controller.setControllerSuministroModal(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Generar codigo de barras", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();

            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void selectFileImage() {
        if (selectFile.getName().endsWith("png") || selectFile.getName().endsWith("jpg") || selectFile.getName().endsWith("jpeg") || selectFile.getName().endsWith(".gif")) {
            lnPrincipal.setImage(new Image(selectFile.toURI().toString()));
            FileInputStream inputStream = null;
            byte[] buffer = new byte[1024];
            try (FileOutputStream outputStream = new FileOutputStream("." + File.separator + "img" + File.separator + selectFile.getName())) {
                inputStream = new FileInputStream(selectFile.getAbsolutePath());
                int byteRead;
                while ((byteRead = inputStream.read(buffer)) != 1) {
                    outputStream.write(buffer, 0, byteRead);
                }
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Tools.AlertMessage(apWindow.getScene().getWindow(), Alert.AlertType.ERROR, "Producto", e.getLocalizedMessage() + ": Consulte con el proveedor del sistema del problema :D", false);
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        System.out.println(ex.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private void openWindowFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar una imagen");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Elija una imagen", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        selectFile = fileChooser.showOpenDialog(apWindow.getScene().getWindow());
        if (selectFile != null) {
            selectFile = new File(selectFile.getAbsolutePath());
            if (selectFile.getName().endsWith("png") || selectFile.getName().endsWith("jpg") || selectFile.getName().endsWith("jpeg") || selectFile.getName().endsWith("gif")) {
                Image image = new Image(selectFile.toURI().toString());
                lnPrincipal.setSmooth(true);
                lnPrincipal.setPreserveRatio(false);
                lnPrincipal.setImage(image);
                lnPrincipal.setFitWidth(160);
                lnPrincipal.setFitHeight(160);
                imageBytes = null;
            } else {
                Tools.AlertMessageWarning(apWindow, "Producto", "No seleccionó un formato correcto de imagen.");
            }
        }
    }

    private void closeWindow() {
        Tools.Dispose(apWindow);
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        aValidityProcess();
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            aValidityProcess();
        }
    }

    @FXML
    private void onKeyPressedToCancel(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            closeWindow();
        }
    }

    @FXML
    private void onActionToCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void onActionPhoto(ActionEvent event) {
        openWindowFile();
    }

    @FXML
    private void onActionRemovePhoto(ActionEvent event) {
        lnPrincipal.setImage(new Image("/view/image/no-image.png"));
        lnPrincipal.setFitWidth(160);
        lnPrincipal.setFitHeight(160);
        selectFile = null;
        imageBytes = null;
    }

    @FXML
    private void onKeyTypedClave(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedClaveAlterna(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedMinimo(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtStockMinimo.getText().contains(".") || c == '-' && txtStockMinimo.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedMaxino(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtStockMaximo.getText().contains(".") || c == '-' && txtStockMaximo.getText().contains("-")) {
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
    private void onKeyTypedPrecioNeto1(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecioVentaNeto1.getText().contains(".") || c == '-' && txtPrecioVentaNeto1.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecioNeto2(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecioVentaNeto2.getText().contains(".") || c == '-' && txtPrecioVentaNeto2.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedPrecioNeto3(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecioVentaNeto3.getText().contains(".") || c == '-' && txtPrecioVentaNeto3.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyTypedDetalle(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if (c != '\b') {
            event.consume();
        }
    }

    @FXML
    private void onActionGenerar(ActionEvent event) {
        openWindowGerarCodigoBarras();
    }

    @FXML
    private void onKeyReleasedCategoria(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            openWindowDetalle("Agregar Categoría", "0006", false);
        }
    }

    @FXML
    private void onKeyReleasedMarca(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            openWindowDetalle("Agregar Marca", "0007", false);
        }
    }

    @FXML
    private void onKeyReleasedPresentacion(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            openWindowDetalle("Agregar Presentación", "0008", false);
        }
    }

    @FXML
    private void onMouseClickedCategoria(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openWindowDetalle("Agregar Categoría", "0006", false);
        }
    }

    @FXML
    private void onMouseClickedMarca(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openWindowDetalle("Agregar Marca", "0007", false);
        }
    }

    @FXML
    private void onMouseClickedPresentacion(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openWindowDetalle("Agregar Presentación", "0008", false);
        }
    }

    @FXML
    private void onMouseClickedMedida(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openWindowDetalle("Agregar Departamento", "0013", false);
        }
    }

    @FXML
    private void onKeyReleasedMedida(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE) {
            openWindowDetalle("Agregar Departamento", "0013", false);
        }
    }

    @FXML
    private void onKeyTypedMedida(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if (c != '\b') {
            event.consume();
        }
    }

    @FXML
    private void onKeyReleasedCosto(KeyEvent event) {

    }

    @FXML
    private void onActionInventario(ActionEvent event) {
        vbInventario.setDisable(!cbInventario.isSelected());
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
    private void onKeyTypedPrecioNetoPerzonalizado(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b') && (c != '.') && (c != '-')) {
            event.consume();
        }
        if (c == '.' && txtPrecioVentaNetoPersonalizado.getText().contains(".") || c == '-' && txtPrecioVentaNetoPersonalizado.getText().contains("-")) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedAdd(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addElementsTablePrecios();
        }
    }

    @FXML
    private void onActionAdd(ActionEvent event) {
        addElementsTablePrecios();
    }

    @FXML
    private void OnKeyPressedDescripcion(KeyEvent event) {
        if (event.getCode().equals(KeyCode.TAB)) {
            Node node = (Node) event.getSource();
            if (node instanceof TextArea) {
                TextAreaSkin skin = (TextAreaSkin) ((TextArea) node).getSkin();
                if (!event.isControlDown()) {
                    if (event.isShiftDown()) {
                        skin.getBehavior().traversePrevious();
                    } else {
                        skin.getBehavior().traverseNext();
                    }
                } else {
                    TextArea textArea = (TextArea) node;
                    textArea.replaceSelection("\t");
                }
                event.consume();
            }
        }
    }

    public void setIdPresentacion(int idPresentacion) {
        this.idPresentacion = idPresentacion;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public void setIdMedida(int idMedida) {
        this.idMedida = idMedida;
    }

    public TextField getTxtPresentacion() {
        return txtPresentacion;
    }

    public TextField getTxtCategoria() {
        return txtCategoria;
    }

    public TextField getTxtMarca() {
        return txtMarca;
    }

    public TextField getTxtClave() {
        return txtClave;
    }

    public TextField getTxtMedida() {
        return txtMedida;
    }

}
