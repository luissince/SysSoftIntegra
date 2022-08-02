package controller.operaciones.cortecaja;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.menus.FxPrincipalController;
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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.CajaADO;
import model.CajaTB;
import model.MovimientoCajaTB;

public class FxCajaConsultasController implements Initializable {

    @FXML
    private ScrollPane window;
    @FXML
    private Label lblLoad;
    @FXML
    private GridPane gpList;
    @FXML
    private Label lblInicoTurno;
    @FXML
    private Label lblFinTurno;
    @FXML
    private Label lblMontoBase;
    @FXML
    private Label lblBase;
    @FXML
    private Label lblVentaEfectivo;
    @FXML
    private Label lblVentaTarjeta;
    @FXML
    private Label lblVentaDeposito;
    @FXML
    private Label lblIngresosEfectivo;
    @FXML
    private Label lblRetirosEfectivo;
    @FXML
    private Label lblContado;
    @FXML
    private Label lblCanculado;
    @FXML
    private Label lblDiferencia;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblIngresosTarjeta;
    @FXML
    private Label lblIngresosDeposito;
    @FXML
    private Label lblRetirosTarjeta;
    @FXML
    private Label lblRetirosDeposito;

    private CajaTB cajaTB;

    private FxPrincipalController fxPrincipalController;

    private FxOpcionesImprimirController fxOpcionesImprimirController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fxOpcionesImprimirController = new FxOpcionesImprimirController();
        fxOpcionesImprimirController.loadComponents();
        fxOpcionesImprimirController.loadTicketCaja(window);
    }

    public void loadDataCorteCaja(String idCaja) {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CajaADO.ListarMovimientoPorById(idCaja);
            }
        };

        task.setOnSucceeded(e -> {
            Object objects = task.getValue();
            if (objects instanceof Object[]) {
                Object[] objectData = (Object[]) objects;

                cajaTB = (CajaTB) objectData[0];
                ArrayList<Double> arrayList = (ArrayList<Double>) objectData[1];

                double montoInicial = arrayList.get(0);

                double ventaEfectivo = arrayList.get(1);
                double ventaTarjeta = arrayList.get(2);
                double ventaDeposito = arrayList.get(3);

                double ingresoEfectivo = arrayList.get(4);
                double ingresoTarjeta = arrayList.get(5);
                double ingresoDeposito = arrayList.get(6);

                double salidaEfectivo = arrayList.get(7);
                double salidaTarjeta = arrayList.get(8);
                double salidaDeposito = arrayList.get(9);

                lblInicoTurno.setText(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                lblFinTurno.setText(cajaTB.getFechaCierre() + " " + cajaTB.getHoraCierre());

                lblMontoBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoInicial, 2));
                lblBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoInicial, 2));

                lblContado.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getContado(), 2));
                lblCanculado.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getCalculado(), 2));
                lblDiferencia.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(cajaTB.getDiferencia(), 2));

                lblVentaEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                lblVentaTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                lblVentaDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                lblIngresosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                lblIngresosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                lblIngresosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                lblRetirosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                lblRetirosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                lblRetirosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue((montoInicial + ventaEfectivo + ingresoEfectivo) - salidaEfectivo, 2));

                gpList.getChildren().clear();
                createHeadTable();
                fillVentasDetalleTable((ArrayList<MovimientoCajaTB>) objectData[2]);
            } else {
                Tools.AlertMessageError(window, "Corte de caja", (String) objects);
            }
            lblLoad.setVisible(false);
        });

        task.setOnFailed(e -> {
            Tools.AlertMessageError(window, "Corte de caja", "Se genero un problema en ejecutar la tarea.");
            lblLoad.setVisible(false);
        });

        task.setOnScheduled(e -> {
            lblLoad.setVisible(true);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void createHeadTable() {
        gpList.add(addHeadGridPane("N#"), 0, 0);
        gpList.add(addHeadGridPane("Fecha Registro"), 1, 0);
        gpList.add(addHeadGridPane("Comentario"), 2, 0);
        gpList.add(addHeadGridPane("Concepto"), 3, 0);
        gpList.add(addHeadGridPane("Ingresos"), 4, 0);
        gpList.add(addHeadGridPane("Salidas"), 5, 0);
    }

    private void fillVentasDetalleTable(ArrayList<MovimientoCajaTB> arrList) {
        for (int i = 0; i < arrList.size(); i++) {
            gpList.add(addElementGridPane("l1" + (i + 1), "" + arrList.get(i).getId(), Pos.CENTER_LEFT, "#020203"), 0, (i + 1));
            gpList.add(addElementGridPane("l2" + (i + 1), arrList.get(i).getFechaMovimiento() + "\n" + arrList.get(i).getHoraMovimiento(), Pos.CENTER_LEFT, "#020203"), 1, (i + 1));
            gpList.add(addElementGridPane("l3" + (i + 1), arrList.get(i).getComentario(), Pos.CENTER_LEFT, "#020203"), 2, (i + 1));

            gpList.add(addElementGridPane("l4" + (i + 1),
                    arrList.get(i).getTipoMovimiento() == 1 ? "MONTO INICIAL"
                            
                    : arrList.get(i).getTipoMovimiento() == 2 ? "VENTA EN EFECTIVO"
                    : arrList.get(i).getTipoMovimiento() == 3 ? "VENTA CON TARJETA"
                    : arrList.get(i).getTipoMovimiento() == 6 ? "VENTA CON DEPOSITO"
                            
                    : arrList.get(i).getTipoMovimiento() == 4 ? "INGRESO EN EFECTIVO"
                    : arrList.get(i).getTipoMovimiento() == 7 ? "INGRESO CON TARJETA"
                    : arrList.get(i).getTipoMovimiento() == 8 ? "INGRESO CON DEPOSITO"
                            
                    : arrList.get(i).getTipoMovimiento() == 5 ? "SALIDA EN EFECTIVO"
                    : arrList.get(i).getTipoMovimiento() == 9 ? "SALIDA CON TARJETA"
                    : "SALIDA CON DEPOSITO",
                    Pos.CENTER_LEFT, "#020203"), 3, (i + 1));

            gpList.add(addElementGridPane("l5" + (i + 1),
                    arrList.get(i).getTipoMovimiento() == 1//MONTO INICIAL
                    || arrList.get(i).getTipoMovimiento() == 2//VENTA EFECTIVO
                    || arrList.get(i).getTipoMovimiento() == 3//VENTA TARJETA
                    || arrList.get(i).getTipoMovimiento() == 4//INGRESO EFECTIVO
                    || arrList.get(i).getTipoMovimiento() == 6//VENTA DEPOSITO
                    || arrList.get(i).getTipoMovimiento() == 7//INGRESO TARJETA
                    || arrList.get(i).getTipoMovimiento() == 8//INGRESO DEPOSITO
                    ? Tools.roundingValue(arrList.get(i).getMonto(), 2)
                    : "",
                    Pos.CENTER_RIGHT, "#0d4e9e"), 4, (i + 1));
            gpList.add(addElementGridPane("l6" + (i + 1),
                    arrList.get(i).getTipoMovimiento() == 5//SALIDA EFECTIVO
                    || arrList.get(i).getTipoMovimiento() == 9//SALIDA TARJETA
                    || arrList.get(i).getTipoMovimiento() == 10//SALIDA DEPOSITO
                    ? Tools.roundingValue(arrList.get(i).getMonto(), 2)
                    : "",
                    Pos.CENTER_RIGHT, "#ff0000"), 5, (i + 1));
        }
    }

    private Label addHeadGridPane(String nombre) {
        Label label = new Label(nombre);
        label.setTextFill(Color.web("#FFFFFF"));
        label.setStyle("-fx-background-color:  #020203;-fx-padding:  0.6666666666666666em 0.16666666666666666em 0.6666666666666666em 0.16666666666666666em;-fx-font-weight:100;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private Label addElementGridPane(String id, String nombre, Pos pos, String textFill) {
        Label label = new Label(nombre);
        label.setId(id);
        label.setTextFill(Color.web(textFill));
        label.setStyle("-fx-background-color: #eaeaea;-fx-padding: 0.4166666666666667em 0.8333333333333334em 0.4166666666666667em 0.8333333333333334em;");
        label.getStyleClass().add("labelRoboto13");
        label.setAlignment(pos);
        label.setWrapText(true);
        label.setPrefWidth(Control.USE_COMPUTED_SIZE);
        label.setPrefHeight(Control.USE_COMPUTED_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        return label;
    }

    private void openWindowCaja() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CAJA_BUSQUEDA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxCajaBusquedaController controller = fXMLLoader.getController();
            controller.setInitCajaConsultasController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccionar corte de caja", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void openWindowAjuste() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CAJA_AJUSTE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxCajaAjusteController controller = fXMLLoader.getController();
            controller.initComponents(cajaTB.getIdCaja());
            controller.setInitCajaConsultasController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccionar corte de caja", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void onEventReporte(String idCaja) {
        fxOpcionesImprimirController.getTicketCaja().mostrarReporte(idCaja);
    }

    public void onEventImprimir(String idCaja) {
        fxOpcionesImprimirController.getTicketCaja().imprimir(idCaja);
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowCaja();
        }
    }

    @FXML
    private void onActionSearch(ActionEvent event) {
        openWindowCaja();
    }

    @FXML
    private void OnKeyPressedReporte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (cajaTB != null) {
                onEventReporte(cajaTB.getIdCaja());
            }
        }
    }

    @FXML
    private void onActionReporte(ActionEvent event) {
        if (cajaTB != null) {
            onEventReporte(cajaTB.getIdCaja());
        }
    }

    @FXML
    private void onKeyPressedTicket(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (cajaTB != null) {
                onEventImprimir(cajaTB.getIdCaja());
            }
        }
    }

    @FXML
    private void onActionTicket(ActionEvent event) {
        if (cajaTB != null) {
            onEventImprimir(cajaTB.getIdCaja());
        }
    }

    @FXML
    private void onKeyPressedAjuste(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowAjuste();
        }
    }

    @FXML
    private void onActionAjuste(ActionEvent event) {
        openWindowAjuste();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }
}
