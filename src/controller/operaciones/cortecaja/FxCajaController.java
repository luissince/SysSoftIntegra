package controller.operaciones.cortecaja;

import controller.configuracion.impresoras.FxOpcionesImprimirController;
import controller.login.FxLoginController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.CajaADO;
import model.CajaTB;
import model.DBUtil;
import model.PrivilegioTB;

public class FxCajaController implements Initializable {

    @FXML
    private VBox window;
    @FXML
    private Label lblLoad;
    @FXML
    private Button btnRealizarCorte;
    @FXML
    private Button btnTerminarTurno;
    @FXML
    private Label lblTurno;
    @FXML
    private Label lblMontoBase;
    @FXML
    private Label lblTotalVentas;
    @FXML
    private Label lblBase;
    @FXML
    private Label lblVentaEfectivo;
    @FXML
    private Label lblVentaDeposito;
    @FXML
    private Label lblTotal;
    @FXML
    private Label lblVentaTarjeta;
    @FXML
    private Label lblIngresosEfectivo;
    @FXML
    private Label lblRetirosEfectivo;
    @FXML
    private Label lblIngresosTarjeta;
    @FXML
    private Label lblIngresosDeposito;
    @FXML
    private Label lblRetirosTarjeta;
    @FXML
    private Label lblRetirosDeposito;

    private FxPrincipalController fxPrincipalController;

    private double totalDineroCaja;

    private double totalTarjeta;

    private String idActual;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idActual = "";
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            btnRealizarCorte.setDisable(true);
        }
        if (privilegioTBs.get(1).getIdPrivilegio() != 0 && !privilegioTBs.get(1).isEstado()) {
            btnTerminarTurno.setDisable(true);
        }
        if (privilegioTBs.get(2).getIdPrivilegio() != 0 && !privilegioTBs.get(2).isEstado()) {
            lblTotalVentas.setVisible(false);
            lblVentaEfectivo.setVisible(false);
            lblVentaTarjeta.setVisible(false);
            lblTotal.setVisible(false);
        }
    }

    private void clearElements() {
        lblTurno.setText("00/00/0000 0:00");
        lblMontoBase.setText("M 00.00");
        lblTotalVentas.setText("M 00.00");
        lblBase.setText("M 0.00");
        lblVentaEfectivo.setText("M 0.00");
        lblVentaTarjeta.setText("M 0.00");
        lblVentaDeposito.setText("M 0.00");
        lblIngresosEfectivo.setText("M 0.00");
        lblIngresosTarjeta.setText("M 0.00");
        lblIngresosDeposito.setText("M 0.00");
        lblRetirosEfectivo.setText("M 0.00");
        lblRetirosTarjeta.setText("M 0.00");
        lblRetirosDeposito.setText("M 0.00");
        lblTotal.setText("M 0.00");
    }

    private void onEventCorteCaja() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CajaADO.ValidarAperturaCajaParaCerrar(Session.USER_ID);
            }
        };

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof ArrayList) {
                ArrayList<Object> objects = (ArrayList<Object>) object;

                CajaTB cajaTB = (CajaTB) objects.get(0);
                ArrayList<Double> arrayList = (ArrayList<Double>) objects.get(1);

                double montoBase = cajaTB.getContado();
                double ventaEfectivo = arrayList.get(0);
                double ventaTarjeta = arrayList.get(1);
                double ventaDeposito = arrayList.get(2);

                double ingresoEfectivo = arrayList.get(3);
                double ingresoTarjeta = arrayList.get(4);
                double ingresoDeposito = arrayList.get(5);

                double salidaEfectivo = arrayList.get(6);
                double salidaTarjeta = arrayList.get(7);
                double salidaDeposito = arrayList.get(8);

                switch (cajaTB.getId()) {
                    case 2:
                        lblTurno.setText(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                        lblMontoBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));
                        lblBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));

                        lblTotalVentas.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));

                        lblVentaEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                        lblVentaTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                        lblVentaDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                        lblIngresosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                        lblIngresosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                        lblIngresosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                        lblRetirosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                        lblRetirosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                        lblRetirosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                        totalDineroCaja = (montoBase + ventaEfectivo + ingresoEfectivo) - salidaEfectivo;
                        totalTarjeta = ventaTarjeta;
                        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalDineroCaja, 2));

                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(false);
                        idActual = cajaTB.getIdCaja();
                        break;
                    case 3:
                        lblTurno.setText(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                        lblMontoBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));
                        lblBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));

                        lblTotalVentas.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));

                        lblVentaEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                        lblVentaTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                        lblVentaDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                        lblIngresosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                        lblIngresosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                        lblIngresosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                        lblRetirosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                        lblRetirosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                        lblRetirosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                        totalDineroCaja = (montoBase + ventaEfectivo + ingresoEfectivo) - salidaEfectivo;
                        totalTarjeta = ventaTarjeta;
                        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalDineroCaja, 2));

                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(false);
                        idActual = cajaTB.getIdCaja();
                        break;
                    default:
                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(true);
                        clearElements();
                        break;
                }
            } else {
                Tools.AlertMessageError(window, "Corte de caja", (String) object);
                btnRealizarCorte.setDisable(false);
                btnTerminarTurno.setDisable(true);
                idActual = "";
                totalDineroCaja = 0;
                totalTarjeta = 0;
                clearElements();
            }
            lblLoad.setVisible(false);
        });

        task.setOnFailed(e -> {
            Tools.AlertMessageError(window, "Corte de caja", "No se pudo realizar la petición por problemas de conexión, intente nuevamente.");
            idActual = "";
            totalDineroCaja = 0;
            totalTarjeta = 0;
            btnRealizarCorte.setDisable(false);
            btnTerminarTurno.setDisable(true);
            lblLoad.setVisible(false);
        });

        task.setOnScheduled(e -> {
            idActual = "";
            totalDineroCaja = 0;
            totalTarjeta = 0;
            btnRealizarCorte.setDisable(true);
            btnTerminarTurno.setDisable(true);
            lblLoad.setVisible(true);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }

    }

    private void onEventTerminarTurno() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CajaADO.ValidarAperturaCajaParaCerrar(Session.USER_ID);
            }
        };

        task.setOnSucceeded(e -> {
            Object Object = task.getValue();
            if (Object instanceof ArrayList) {
                ArrayList<Object> objects = (ArrayList<Object>) Object;

                CajaTB cajaTB = (CajaTB) objects.get(0);
                ArrayList<Double> arrayList = (ArrayList<Double>) objects.get(1);

                double montoBase = cajaTB.getContado();
                double ventaEfectivo = arrayList.get(0);
                double ventaTarjeta = arrayList.get(1);
                double ventaDeposito = arrayList.get(2);

                double ingresoEfectivo = arrayList.get(3);
                double ingresoTarjeta = arrayList.get(4);
                double ingresoDeposito = arrayList.get(5);

                double salidaEfectivo = arrayList.get(6);
                double salidaTarjeta = arrayList.get(7);
                double salidaDeposito = arrayList.get(8);

                switch (cajaTB.getId()) {
                    case 2:
                        lblTurno.setText(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                        lblMontoBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));
                        lblBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));

                        lblTotalVentas.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));

                        lblVentaEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                        lblVentaTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                        lblVentaDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                        lblIngresosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                        lblIngresosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                        lblIngresosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                        lblRetirosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                        lblRetirosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                        lblRetirosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                        totalDineroCaja = (montoBase + ventaEfectivo + ingresoEfectivo) - salidaEfectivo;
                        totalTarjeta = ventaTarjeta;
                        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalDineroCaja, 2));

                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(false);
                        idActual = cajaTB.getIdCaja();
                        openWindowRealizarCorte();
                        break;
                    case 3:
                        lblTurno.setText(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                        lblMontoBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));
                        lblBase.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(montoBase, 2));

                        lblTotalVentas.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));

                        lblVentaEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaEfectivo, 2));
                        lblVentaTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaTarjeta, 2));
                        lblVentaDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ventaDeposito, 2));

                        lblIngresosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoEfectivo, 2));
                        lblIngresosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoTarjeta, 2));
                        lblIngresosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(ingresoDeposito, 2));

                        lblRetirosEfectivo.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaEfectivo, 2));
                        lblRetirosTarjeta.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaTarjeta, 2));
                        lblRetirosDeposito.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(salidaDeposito, 2));

                        totalDineroCaja = (cajaTB.getContado() + ventaEfectivo + ingresoEfectivo) - salidaEfectivo;
                        totalTarjeta = ventaTarjeta;
                        lblTotal.setText(Session.MONEDA_SIMBOLO + " " + Tools.roundingValue(totalDineroCaja, 2));

                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(false);
                        idActual = cajaTB.getIdCaja();
                        openWindowRealizarCorte();
                        break;
                    default:
                        btnRealizarCorte.setDisable(false);
                        btnTerminarTurno.setDisable(true);
                        clearElements();
                        break;
                }

            } else {
                Tools.AlertMessageError(window, "Corte de caja", (String) Object);
                btnRealizarCorte.setDisable(false);
                btnTerminarTurno.setDisable(true);
                idActual = "";
                totalDineroCaja = 0;
                totalTarjeta = 0;
                clearElements();
            }
            lblLoad.setVisible(false);
        });

        task.setOnFailed(e -> {
            Tools.AlertMessageError(window, "Corte de caja", "No se pudo realizar la petición por problemas de conexión, intente nuevamente.");
            idActual = "";
            totalDineroCaja = 0;
            totalTarjeta = 0;
            btnRealizarCorte.setDisable(false);
            btnTerminarTurno.setDisable(true);
            lblLoad.setVisible(false);
        });

        task.setOnScheduled(e -> {
            idActual = "";
            totalDineroCaja = 0;
            totalTarjeta = 0;
            btnRealizarCorte.setDisable(true);
            btnTerminarTurno.setDisable(true);
            lblLoad.setVisible(true);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void openModalImpresion(String idCaja) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_OPCIONES_IMPRIMIR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxOpcionesImprimirController controller = fXMLLoader.getController();
            controller.loadTicketCaja(controller.getApWindow());
            controller.setIdCaja(idCaja);
            controller.setInitOpcionesImprimirCaja(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnCloseRequest(w -> {
                Tools.Dispose(fxPrincipalController.getSpWindow());
                openWindowLogin();
            });
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();
        } catch (IOException ex) {
            System.out.println("Controller banco" + ex.getLocalizedMessage());
        }
    }

    private void openWindowRealizarCorte() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_CAJA_CERRAR_CAJA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());

            FxCajaCerrarCajaController controller = fXMLLoader.getController();
            controller.loadDataInit(idActual, totalDineroCaja, totalTarjeta);
            controller.setInitCerrarCajaController(this, fxPrincipalController);

            Stage stage = WindowStage.StageLoaderModal(parent, "Realizar corte de caja", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    public void openWindowLogin() {
        try {
            URL urllogin = getClass().getResource(FilesRouters.FX_LOGIN);
            FXMLLoader fXMLLoaderLogin = WindowStage.LoaderWindow(urllogin);
            Parent parent = fXMLLoaderLogin.load(urllogin.openStream());
            FxLoginController controller = fXMLLoaderLogin.getController();
            Scene scene = new Scene(parent);
            Stage primaryStage = new Stage();
            primaryStage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setTitle(FilesRouters.TITLE_APP);
            primaryStage.centerOnScreen();
            primaryStage.setMaximized(true);
            primaryStage.setOnCloseRequest(c -> {
                short optionI = Tools.AlertMessageConfirmation(parent, "SysSoft Integra", "¿Está seguro de cerrar la aplicación?");
                if (optionI == 1) {
                    try {
                        if (DBUtil.getConnection() != null && !DBUtil.getConnection().isClosed()) {
                            DBUtil.getConnection().close();
                        }
                        System.exit(0);
                        Platform.exit();
                    } catch (SQLException e) {
                        System.exit(0);
                        Platform.exit();
                    }
                } else {
                    c.consume();
                }
            });
            primaryStage.setOnShown(e -> controller.initComponents());
            primaryStage.show();
            primaryStage.requestFocus();
        } catch (IOException ex) {
            Tools.println("Login:" + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onActionCorte(ActionEvent event) {
        onEventCorteCaja();
    }

    @FXML
    private void onKeyPressedCorte(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventCorteCaja();
        }
    }

    @FXML
    private void onKeyPressedTerminarTurno(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventTerminarTurno();
        }
    }

    @FXML
    private void onActionTerminarTurno(ActionEvent event) {
        onEventTerminarTurno();
    }

    public FxPrincipalController getFxPrincipalController() {
        return fxPrincipalController;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
