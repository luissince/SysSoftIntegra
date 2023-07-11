package controller.consultas.cobrar;

import controller.tools.ConvertMonedaCadena;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.BancoTB;
import model.IngresoTB;
import model.VentaCreditoTB;
import model.VentaTB;
import service.BancoADO;
import service.VentaADO;

public class FxGenerarCobroController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblVueltoContadoNombre;
    @FXML
    private Label lblVueltoContado;
    @FXML
    private Button btnAceptar;
    @FXML
    private TextField txtObservacion;
    @FXML
    private VBox vbViewEfectivo;
    @FXML
    private ComboBox<BancoTB> cbMetodoTransaccion;
    @FXML
    private VBox vbContenedorMetodoPago;
    @FXML
    private HBox hbLoadProcesando;
    @FXML
    private Label lblTextoProceso;
    @FXML
    private Button btnCancelarProceso;
    @FXML
    private VBox vbContenido;
    @FXML
    private Label lblMonedaLetras;

    private FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController;

    private ConvertMonedaCadena monedaCadena;

    private VentaTB ventaTB;

    private double vueltoContado;

    private double totalVenta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Tools.DisposeWindow(window, KeyEvent.KEY_RELEASED);
        totalVenta = 0;
        vueltoContado = 0.00;
        monedaCadena = new ConvertMonedaCadena();
        lblVueltoContadoNombre.setText("SU CAMBIO: ");
    }

    // private void setInitLoadVentaAbono(VentaTB ventaTB) {
    // this.ventaTB = ventaTB;
    // }

    // private void setInitLoadVentaAbono(VentaTB ventaTB, String idVentaCredito,
    // double monto) {
    // this.ventaTB = ventaTB;
    // this.idVentaCredito = idVentaCredito;
    // txtMonto.setText(Tools.roundingValue(monto, 2));
    // txtMonto.setDisable(true);
    // }

    /*
     * Función encargada de obtener los métodos de cobro
     */
    public void loadInitComponents(VentaTB ventaTB) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ObservableList<BancoTB>> task = new Task<ObservableList<BancoTB>>() {
            @Override
            public ObservableList<BancoTB> call() throws Exception {
                Object result = BancoADO.ObtenerListarBancos();
                if (result instanceof ObservableList<?>) {
                    return (ObservableList<BancoTB>) result;
                } else {
                    throw new Exception("El objeto devuelto por el método no es una lista observable de bancos.");
                }
            }
        };

        task.setOnScheduled(e -> {
            hbLoadProcesando.setVisible(true);
            lblTextoProceso.setText("PROCESANDO INFORMACIÓN...");
            btnCancelarProceso.setText("Cancelar Proceso");
            if (btnCancelarProceso.getOnAction() != null) {
                btnCancelarProceso.removeEventHandler(ActionEvent.ACTION, btnCancelarProceso.getOnAction());
            }
            btnCancelarProceso.setOnAction(event -> {
                if (task.isRunning()) {
                    task.cancel();
                }
                Tools.Dispose(window);
            });
        });

        task.setOnFailed(e -> {
            lblTextoProceso.setText(task.getException().getMessage());
            btnCancelarProceso.setText("Cerrar Vista");
        });

        task.setOnSucceeded(e -> {
            this.ventaTB = ventaTB;
            ObservableList<BancoTB> bancos = task.getValue();
            cbMetodoTransaccion.getItems().addAll(bancos);

            totalVenta = Double.parseDouble(Tools.roundingValue(ventaTB.getMontoRestante(), 2));

            lblTotal.setText(
                    "TOTAL A COBRAR: " + ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(totalVenta, 2));
            lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));
            lblMonedaLetras.setText(monedaCadena.Convertir(Tools.roundingValue(totalVenta, 2), true,
                    ventaTB.getMonedaTB().getNombre()));
            vbContenido.setDisable(false);
            hbLoadProcesando.setVisible(false);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    /*
     * Agrega un método de cobro
     * que no se encuentre en la lista
     */
    private void addMetodoCobro() {
        // Se debe seleccionar un método de cobro.
        if (cbMetodoTransaccion.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Cobro", "Seleccione el método de cobro.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        // Solo se puede agregar un método de cobro.
        if (!vbContenedorMetodoPago.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(window, "Cobro", "Solo se puede usar un solo método de cobro.");
            return;
        }

        // Se genera el método de cobro si se cumple las 2 condiciones.
        generarMetodoCobro(cbMetodoTransaccion.getSelectionModel().getSelectedItem());
    }

    /*
     * Función encargada de diseñar el método de cobro
     * con una sección para ingresar el monto y la operación
     */
    private void generarMetodoCobro(BancoTB bancoTB) {
        HBox hBox = new HBox();
        hBox.setId(bancoTB.getIdBanco());
        hBox.setStyle("-fx-spacing: 0.6666666666666666em;");

        VBox vbMonto = new VBox();
        HBox.setHgrow(vbMonto, Priority.ALWAYS);
        vbMonto.setStyle("-fx-spacing: 0.5333333333333333em;");
        Label lblMonto = new Label(
                "Monto - " + bancoTB.getNombreCuenta());
        lblMonto.getStyleClass().add("labelOpenSansRegular13");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("0.00");
        txtMonto.getStyleClass().add("text-field-normal");
        txtMonto.setPrefWidth(260);
        txtMonto.setPrefHeight(30);
        txtMonto.setOnAction(event -> onEventAceptar());
        txtMonto.setOnKeyReleased(event -> {
            if (txtMonto.getText().isEmpty()) {
                vueltoContado = totalVenta;
                generarVuelto();
                return;
            }
            if (Tools.isNumeric(txtMonto.getText())) {
                generarVuelto();
            }
        });
        txtMonto.setOnKeyTyped(event -> {
            if (event.getCharacter().isEmpty())
                return;

            char c = event.getCharacter().charAt(0);
            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                event.consume();
            }
            if (c == '.' && txtMonto.getText().contains(".")) {
                event.consume();
            }
        });
        vbMonto.getChildren().addAll(lblMonto, txtMonto);

        VBox vbOperacion = new VBox();
        HBox.setHgrow(vbOperacion, Priority.ALWAYS);
        vbOperacion.setStyle("-fx-spacing: 0.5333333333333333em;");
        Label lblOperacion = new Label(
                "N° de Operación - " + bancoTB.getNombreCuenta());
        lblOperacion.getStyleClass().add("labelOpenSansRegular13");

        HBox hbContendorOperacion = new HBox();
        TextField txtOperacion = new TextField();
        txtOperacion.setPromptText(
                "N° Operación");
        txtOperacion.getStyleClass().add("text-field-normal");
        txtOperacion.setPrefWidth(260);
        txtOperacion.setPrefHeight(30);
        txtOperacion.setOnAction(event -> onEventAceptar());
        Button btnQuitar = new Button();
        btnQuitar.setPrefHeight(30);
        btnQuitar.setMaxHeight(Double.MAX_VALUE);
        btnQuitar.getStyleClass().add("buttonLightError");
        ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        btnQuitar.setGraphic(imageView);
        btnQuitar.setOnAction(event -> {
            vbContenedorMetodoPago.getChildren().remove(hBox);
            generarVuelto();
        });
        btnQuitar.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                vbContenedorMetodoPago.getChildren().remove(hBox);
                generarVuelto();
            }
        });
        hbContendorOperacion.getChildren().addAll(txtOperacion, btnQuitar);
        vbOperacion.getChildren().addAll(lblOperacion, hbContendorOperacion);

        hBox.getChildren().addAll(vbMonto, vbOperacion);

        vbContenedorMetodoPago.getChildren().add(hBox);
        txtMonto.requestFocus();
    }

    /*
     * Función encargada de calcular
     * el restante de la deuda
     */
    private void generarVuelto() {
        double montoActual = 0;

        for (Node contenedorMetodoPagoNode : vbContenedorMetodoPago.getChildren()) {
            HBox hbox = (HBox) contenedorMetodoPagoNode;

            VBox contenedorMonto = (VBox) hbox.getChildren().get(0);
            if (contenedorMonto != null) {
                for (Node nodo : contenedorMonto.getChildren()) {
                    if (nodo instanceof TextField) {
                        TextField txtMonto = (TextField) nodo;
                        if (Tools.isNumeric(txtMonto.getText().trim())) {
                            montoActual += Double.parseDouble(txtMonto.getText().trim());
                        }
                    }
                }
            }

        }

        if (montoActual >= totalVenta) {
            vueltoContado = montoActual - totalVenta;
            lblVueltoContadoNombre.setText("SU CAMBIO ES: ");
        } else {
            vueltoContado = totalVenta - montoActual;
            lblVueltoContadoNombre.setText("POR COBRAR: ");
        }

        lblVueltoContado.setText(ventaTB.getMonedaTB().getSimbolo() + " " + Tools.roundingValue(vueltoContado, 2));
    }

    /*
     * Función encargada de procesar el cobro
     */
    private void onEventAceptar() {
        // Valida si existe algún método de cobro para continuar.
        if (vbContenedorMetodoPago.getChildren().isEmpty()) {
            Tools.AlertMessageWarning(window, "Cobro", "No hay metodos de cobro para continuar.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        // Valida que no exista mas de 2 método de cobro.
        if (vbContenedorMetodoPago.getChildren().size() > 1) {
            Tools.AlertMessageWarning(window, "Cobro", "No debe haber mas de 2 método de cobro.");
            cbMetodoTransaccion.requestFocus();
            return;
        }

        // Obtenemos el nodo padre
        HBox hbox = (HBox) vbContenedorMetodoPago.getChildren().get(0);

        // Validar que va tomar el valor de monto
        TextField txtMonto = null;
        // Obtenemos el hijo donde se encuentra los textfields
        VBox vbMonto = (VBox) hbox.getChildren().get(0);
        // validamos que el resultado no sea nulo
        if (vbMonto != null) {
            // Recorremos el hijo vbox para encontrar el textfield de monto
            for (Node nodo : vbMonto.getChildren()) {
                if (nodo instanceof TextField) {
                    txtMonto = (TextField) nodo;
                    break;
                }
            }
        }

        // El monto ingresado debe ser númerico
        if (!Tools.isNumeric(txtMonto.getText().trim())) {
            Tools.AlertMessageWarning(window, "Cobro", "Ingrese el monto a cobrar o quítelo.");
            txtMonto.requestFocus();
            return;
        }

        // Asignamos el monto
        double monto = Double.parseDouble(txtMonto.getText().trim());

        // El monto debe ser mayor a 0
        if (monto <= 0) {
            Tools.AlertMessageWarning(window, "Cobro", "El monto no debe ser menor a cero.");
            txtMonto.requestFocus();
            return;
        }

        // TextField encargado de obtener el número de operación
        TextField txtOperacion = null;
        // Hijo que contiene el valor de la operación
        VBox vbOperacion = (VBox) hbox.getChildren().get(1);
        // Validamos que el resultado no sea nulo
        if (vbOperacion != null) {
            HBox hbContenidoOperacion = (HBox) vbOperacion.getChildren().get(1);
            if (hbContenidoOperacion != null) {
                for (Node nodo : hbContenidoOperacion.getChildren()) {
                    if (nodo instanceof TextField) {
                        txtOperacion = (TextField) nodo;
                        break;
                    }
                }
            }
        }

        if (monto >= totalVenta) {
            monto = monto - vueltoContado;
        }

        VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
        ventaCreditoTB.setVentaTB(ventaTB);
        ventaCreditoTB.setIdVenta(ventaTB.getIdVenta());
        ventaCreditoTB.setMonto(monto);
        ventaCreditoTB.setEstado((short) 1);
        ventaCreditoTB.setIdUsuario(Session.USER_ID);
        ventaCreditoTB.setObservacion(txtObservacion.getText().trim());

        IngresoTB ingresoTB = new IngresoTB();
        ingresoTB.setIdProcedencia(ventaTB.getIdVenta()); 
        ingresoTB.setIdBanco(hbox.getId());
        ingresoTB.setIdUsuario(Session.USER_ID);
        ingresoTB.setMonto(monto);
        ingresoTB.setVuelto(0);
        ingresoTB.setOperacion(txtOperacion.getText().trim());
        ingresoTB.setEstado(true);

        short value = Tools.AlertMessageConfirmation(window, "Cobro", "¿Está seguro de continuar?");
        if (value == 1) {
            String[] result = VentaADO.registrarVentaCreditoIngreso(ventaCreditoTB, ingresoTB);
            if (result[0] == "1") {
                Tools.Dispose(window);
                cuentasPorCobrarVisualizarController.openModalImpresion(ventaTB.getIdVenta(),
                        result[2]);
            } else {
                Tools.AlertMessageWarning(window, "Cobro", result[1]);
            }
        }
    }

    @FXML
    private void onKeyPressedAgregarMetodoPago(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addMetodoCobro();
            event.consume();
        }
    }

    @FXML
    private void onActionAgregarMetodoPago(ActionEvent event) {
        addMetodoCobro();
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        onEventAceptar();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventAceptar();
        }
    }

    @FXML
    private void onKeyPressedCancelar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            Tools.Dispose(window);
        }
    }

    @FXML
    private void onActionCancelar(ActionEvent event) {
        Tools.Dispose(window);
    }

    public AnchorPane getWindow() {
        return window;
    }

    public void setInitVentaAbonarController(
            FxCuentasPorCobrarVisualizarController cuentasPorCobrarVisualizarController) {
        this.cuentasPorCobrarVisualizarController = cuentasPorCobrarVisualizarController;
    }

}
