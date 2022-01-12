package controller.inventario.traslados;

import controller.menus.FxPrincipalController;
import controller.operaciones.venta.FxVentaListaController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.DetalleADO;
import model.DetalleTB;
import model.SuministroADO;
import model.SuministroTB;
import model.TrasladoADO;
import model.TrasladoTB;

public class FxTrasladoGuiaController implements Initializable {

    @FXML
    private AnchorPane apWindow;
    @FXML
    private VBox vbBody;
    @FXML
    private Label lblLoad;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private TableView<SuministroTB> tvList;
    @FXML
    private TableColumn<SuministroTB, String> tcNumero;
    @FXML
    private TableColumn<SuministroTB, String> tcProducto;
    @FXML
    private TableColumn<SuministroTB, TextField> tcCantidad;
    @FXML
    private TableColumn<SuministroTB, TextField> tcPeso;
    @FXML
    private TableColumn<SuministroTB, String> tcUnidad;
    @FXML
    private TableColumn<SuministroTB, Button> tcQuitar;
    @FXML
    private TextField txtObservacion;
    @FXML
    private HBox hbLoad;
    @FXML
    private Label lblMessageLoad;
    @FXML
    private Button btnAceptarLoad;
    @FXML
    private ComboBox<DetalleTB> cbMotivoTraslado;
    @FXML
    private TextField txtPartida;
    @FXML
    private TextField txtLlegada;
    @FXML
    private CheckBox cbNumeracion;
    @FXML
    private TextField txtNumeracion;
    @FXML
    private TextField txtDocumentoAsociado;

    private FxTrasladoController fxTrasladoController;

    private FxPrincipalController fxPrincipalController;

    private String idVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.actualDate(Tools.getDate(), dpFecha);

        tcNumero.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getId()));
        tcProducto.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getClave() + "\n" + cellData.getValue().getNombreMarca()));
        tcCantidad.setCellValueFactory(new PropertyValueFactory<>("txtMovimiento"));
        tcPeso.setCellValueFactory(new PropertyValueFactory<>("txtPeso"));
        tcUnidad.setCellValueFactory(cellData -> Bindings.concat(cellData.getValue().getUnidadCompraName()));
        tcQuitar.setCellValueFactory(new PropertyValueFactory<>("btnRemove"));

        tcNumero.prefWidthProperty().bind(tvList.widthProperty().multiply(0.05));
        tcProducto.prefWidthProperty().bind(tvList.widthProperty().multiply(0.20));
        tcCantidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcPeso.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tcUnidad.prefWidthProperty().bind(tvList.widthProperty().multiply(0.15));
        tcQuitar.prefWidthProperty().bind(tvList.widthProperty().multiply(0.10));
        tvList.setPlaceholder(Tools.placeHolderTableView("No hay datos para mostrar.", "-fx-text-fill:#020203;", false));

        txtPartida.setText(Session.COMPANY_DOMICILIO);
        cbMotivoTraslado.getItems().addAll(DetalleADO.GetDetailId("0017"));
        idVenta = "";
    }

    public void addSuministroLista(String idSuministro) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });
        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return SuministroADO.List_Suministros_Movimiento(idSuministro);
            }
        };
        task.setOnScheduled(t -> {

            lblLoad.setVisible(true);
        });
        task.setOnFailed(t -> {

            lblLoad.setVisible(false);
        });
        task.setOnSucceeded(t -> {
            Object object = task.getValue();
            if (object instanceof SuministroTB) {
                SuministroTB suministroTB = (SuministroTB) object;
                suministroTB.setId(tvList.getItems().size() + 1);
                suministroTB.getBtnRemove().setOnAction(event -> {
                    tvList.getItems().remove(suministroTB);
                });
                suministroTB.getBtnRemove().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        tvList.getItems().remove(suministroTB);
                    }
                });
                tvList.getItems().add(suministroTB);
            }
            lblLoad.setVisible(false);
        });
        exec.execute(task);
    }

    private void closeWindow() {
        fxPrincipalController.getVbContent().getChildren().remove(apWindow);
        fxPrincipalController.getVbContent().getChildren().clear();
        AnchorPane.setLeftAnchor(fxTrasladoController.getVbWindow(), 0d);
        AnchorPane.setTopAnchor(fxTrasladoController.getVbWindow(), 0d);
        AnchorPane.setRightAnchor(fxTrasladoController.getVbWindow(), 0d);
        AnchorPane.setBottomAnchor(fxTrasladoController.getVbWindow(), 0d);
        fxPrincipalController.getVbContent().getChildren().add(fxTrasladoController.getVbWindow());
    }

    private void onEventSave() {
        if (dpFecha.getValue() == null) {
            Tools.AlertMessageWarning(apWindow, "Traslado", "Ingrese la fecha de traslado.");
            dpFecha.requestFocus();
        } else if (txtPartida.getText() == null) {
            Tools.AlertMessageWarning(apWindow, "Traslado", "Ingrese el punto de partida.");
            txtPartida.requestFocus();
        } else if (cbMotivoTraslado.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(apWindow, "Traslado", "Seleccione el motivo de traslado.");
            cbMotivoTraslado.requestFocus();
        } else if (tvList.getItems().isEmpty()) {
            Tools.AlertMessageWarning(apWindow, "Traslado", "No hay productos en la lista para continuar.");
            tvList.requestFocus();
        } else {

            TrasladoTB trasladoTB = new TrasladoTB();
            trasladoTB.setFecha(Tools.getDate());
            trasladoTB.setHora(Tools.getTime());
            trasladoTB.setFechaTraslado(Tools.getDatePicker(dpFecha));
            trasladoTB.setHoraTraslado(Tools.getTime());
            trasladoTB.setEstado(1);
            trasladoTB.setTipo(2);
            trasladoTB.setIdEmpleado(Session.USER_ID);
            trasladoTB.setPuntoPartida(txtPartida.getText().trim());
            trasladoTB.setPuntoLlegada(txtLlegada.getText().trim());
            trasladoTB.setSalidaInventario(false);
            trasladoTB.setAsociarDocumento(true);
            trasladoTB.setIdMotivo(cbMotivoTraslado.getSelectionModel().getSelectedItem().getIdDetalle());
            trasladoTB.setIdVenta(idVenta);
            trasladoTB.setObservacion(txtObservacion.getText().trim());
            trasladoTB.setUsarNumeracion(cbNumeracion.isSelected());
            trasladoTB.setNumeracion(!Tools.isNumericInteger(txtNumeracion.getText().trim()) ? 1 : Integer.parseInt(txtNumeracion.getText().trim()));

            int countCantidad = 0;
            int countPeso = 0;

            for (SuministroTB sm : tvList.getItems()) {
                if (!Tools.isNumeric(sm.getTxtMovimiento().getText().trim())) {
                    countCantidad++;
                } else if (Double.parseDouble(sm.getTxtMovimiento().getText().trim()) <= 0) {
                    countCantidad++;
                }

                if (!Tools.isNumeric(sm.getTxtPeso().getText().trim())) {
                    countPeso++;
                } else if (Double.parseDouble(sm.getTxtPeso().getText().trim()) <= 0) {
                    countPeso++;
                }
            }

            if (countCantidad > 0) {
                Tools.AlertMessageWarning(apWindow, "Traslado", "Hay campos en la columna cantidad que no son numericos o menores que 0.");
                tvList.requestFocus();
            } else if (countPeso > 0) {
                Tools.AlertMessageWarning(apWindow, "Traslado", "Hay campos en la columna peso que no son numericos o menores que 0.");
                tvList.requestFocus();
            } else {
                short value = Tools.AlertMessageConfirmation(apWindow, "Traslado", "¿Está seguro de continuar?");
                if (value == 1) {

                    ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
                        Thread t = new Thread(runnable);
                        t.setDaemon(true);
                        return t;
                    });
                    Task<String> task = new Task<String>() {
                        @Override
                        public String call() {
                            return TrasladoADO.CrudTrasladoGuia(trasladoTB, tvList.getItems());
                        }
                    };
                    task.setOnScheduled(t -> {
                        vbBody.setDisable(true);
                        hbLoad.setVisible(true);
                        btnAceptarLoad.setVisible(false);
                        lblMessageLoad.setText("Procesando información...");
                        lblMessageLoad.setTextFill(Color.web("#ffffff"));
                    });
                    task.setOnFailed(t -> {
                        lblMessageLoad.setText(task.getException().getLocalizedMessage());
                        lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                        onEventAceptLoad();
                    });
                    task.setOnSucceeded(t -> {
                        String result = task.getValue();
                        if (result.equalsIgnoreCase("inserted")) {
                            lblMessageLoad.setText("Se registró correctamente el traslado.");
                            lblMessageLoad.setTextFill(Color.web("#ffffff"));

                        } else {
                            lblMessageLoad.setText(result);
                            lblMessageLoad.setTextFill(Color.web("#ff6d6d"));
                        }
                        onEventAceptLoad();
                    });
                    exec.execute(task);
                }
            }
        }
    }

    private void onEventAceptLoad() {
        btnAceptarLoad.setVisible(true);
        btnAceptarLoad.setOnAction(event -> {
            vbBody.setDisable(false);
            hbLoad.setVisible(false);
            btnAceptarLoad.setVisible(false);
            onEventReload();
        });
        btnAceptarLoad.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                vbBody.setDisable(false);
                hbLoad.setVisible(false);
                btnAceptarLoad.setVisible(false);
                onEventReload();
                event.consume();
            }
        });
    }

    private void onEventReload() {
        Tools.actualDate(Tools.getDate(), dpFecha);
        txtPartida.setText(Session.COMPANY_DOMICILIO);
        txtLlegada.clear();
        txtDocumentoAsociado.clear();
        cbMotivoTraslado.getItems().clear();
        cbMotivoTraslado.getItems().addAll(DetalleADO.GetDetailId("0017"));
        idVenta = "";
        txtObservacion.clear();
        txtNumeracion.setText("");
        cbNumeracion.setSelected(false);
        tvList.getItems().clear();
    }

    private void openWindowVentas() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_VENTA_LISTA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxVentaListaController controller = fXMLLoader.getController();
            controller.setInitTrasladoGuia(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccione una venta", apWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
            controller.loadInit();
        } catch (IOException ex) {
            System.out.println("openWindowArticulos():" + ex.getLocalizedMessage());
        }
    }

    public void loadDataVenta(String idVenta, String comprobante) {
        this.idVenta = idVenta;
        txtDocumentoAsociado.setText(comprobante);
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<SuministroTB>> task = new Task<ArrayList<SuministroTB>>() {
            @Override
            protected ArrayList<SuministroTB> call() {
                return TrasladoADO.ObtenerDetalleVenta(idVenta);
            }
        };

        task.setOnScheduled(w -> {
            lblLoad.setVisible(true);
        });

        task.setOnFailed(w -> {
            lblLoad.setVisible(false);
        });
        task.setOnSucceeded(w -> {
            ArrayList<SuministroTB> suministroTBs = task.getValue();
            suministroTBs.forEach(suministroTB -> {
                suministroTB.setId(tvList.getItems().size() + 1);
                suministroTB.getBtnRemove().setOnAction(event -> {
                    tvList.getItems().remove(suministroTB);
                });
                suministroTB.getBtnRemove().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        tvList.getItems().remove(suministroTB);
                    }
                });
                if (!validateDuplicate(tvList, suministroTB)) {
                    tvList.getItems().add(suministroTB);
                }
            });
            lblLoad.setVisible(false);
        });

        exec.execute(task);

        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    private boolean validateDuplicate(TableView<SuministroTB> view, SuministroTB suministroTB) {
        boolean ret = false;
        for (int i = 0; i < view.getItems().size(); i++) {
            if (view.getItems().get(i).getIdSuministro().equals(suministroTB.getIdSuministro())) {
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
    private void onKeyPressedGuardar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventSave();
        }
    }

    @FXML
    private void onActionGuardar(ActionEvent event) {
        onEventSave();
    }

    @FXML
    private void onKeyPressedRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventReload();
        }
    }

    @FXML
    private void onActionRecargar(ActionEvent event) {
        onEventReload();
    }

    @FXML
    private void onKeyPressedAddVenta(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowVentas();
        }
    }

    @FXML
    private void onActionAddVenta(ActionEvent event) {
        openWindowVentas();
    }

    @FXML
    private void onActionNumeracion(ActionEvent event) {
        txtNumeracion.setDisable(cbNumeracion.isSelected());
    }

    @FXML
    private void onKeyTypedNumeracion(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyReleasedWindow(KeyEvent event) {
        switch (event.getCode()) {
            case F1:
                onEventSave();
                break;

            case F2:
                openWindowVentas();
                break;

            case F5:
                onEventReload();
                break;
        }
    }

    public TableView<SuministroTB> getTvList() {
        return tvList;
    }

    public void setContent(FxTrasladoController fxTrasladoController, FxPrincipalController fxPrincipalController) {
        this.fxTrasladoController = fxTrasladoController;
        this.fxPrincipalController = fxPrincipalController;
    }

}
