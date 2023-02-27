package controller.posterminal.venta;

import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CajaTB;
import model.PrivilegioTB;
import service.CajaADO;

public class FxPostVentaController implements Initializable {

    @FXML
    private AnchorPane window;
    @FXML
    private VBox hbContenedorVentas;
    @FXML
    private TabPane tbContenedor;
    @FXML
    private Tab tbVentaUno;
    @FXML
    private Button btnAgregarVenta;

    private FxPrincipalController fxPrincipalController;

    private FxPostVentaEstructuraController ventaEstructuraController;

    private FxPostVentaEstructuraNuevoController ventaEstructuraNuevoController;

    private ObservableList<PrivilegioTB> privilegioTBs;

    private short tipoVenta;

    private boolean aperturaCaja;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aperturaCaja = false;
    }

    public void loadComponents() {
        if (tipoVenta == 1) {
            ventaEstructuraController = (FxPostVentaEstructuraController) addEstructura(tbVentaUno);
        } else {
            ventaEstructuraNuevoController = (FxPostVentaEstructuraNuevoController) addEstructura(tbVentaUno);
        }
    }

    public void loadPrivilegios(ObservableList<PrivilegioTB> privilegioTBs) {
        this.privilegioTBs = privilegioTBs;
        if (privilegioTBs.get(0).getIdPrivilegio() != 0 && !privilegioTBs.get(0).isEstado()) {
            btnAgregarVenta.setDisable(true);
        }
        if (tipoVenta == 1) {
            ventaEstructuraController.loadPrivilegios(privilegioTBs);
        } else {
            ventaEstructuraNuevoController.loadPrivilegios(privilegioTBs);
        }
    }

    private Object addEstructura(Tab tab) {
        Object object = null;
        try {
//            FXMLLoader fXMLSeleccionado = new FXMLLoader(getClass().getResource(FilesRouters.FX_VENTA_ESTRUCTURA_NUEVO));
            FXMLLoader fXMLSeleccionado = new FXMLLoader(getClass().getResource(tipoVenta == 1 ? FilesRouters.FX_POS_VENTA_ESTRUCTURA : FilesRouters.FX_POS_VENTA_ESTRUCTURA_NUEVO));
            VBox seleccionado = fXMLSeleccionado.load();
            object = fXMLSeleccionado.getController();
            tab.setContent(seleccionado);
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
        return object;
    }

    public void loadElements() {
        if (tipoVenta == 1) {
            ventaEstructuraController.setContent(fxPrincipalController);
            ventaEstructuraController.getTxtSearch().requestFocus();
        } else {
            ventaEstructuraNuevoController.setContent(fxPrincipalController);
        }
    }

    private void addTabVentaEstructura() {
        Tab tab = new Tab("Venta " + (tbContenedor.getTabs().size() + 1));
        tbContenedor.getTabs().add(tab);
        if (tipoVenta == 1) {
            FxPostVentaEstructuraController controller = (FxPostVentaEstructuraController) addEstructura(tab);
            controller.setContent(fxPrincipalController);
            controller.getTxtSearch().requestFocus();
            controller.loadPrivilegios(privilegioTBs);
        } else {
            FxPostVentaEstructuraNuevoController controller = (FxPostVentaEstructuraNuevoController) addEstructura(tab);
            controller.setContent(fxPrincipalController);
            controller.getTxtSearch().requestFocus();
            controller.loadPrivilegios(privilegioTBs);
        }
    }

    public void loadValidarCaja() {
        ExecutorService exec = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<Object> task = new Task<Object>() {
            @Override
            public Object call() {
                return CajaADO.ValidarCreacionCaja(Session.USER_ID);
            }
        };

        task.setOnSucceeded(e -> {
            Object object = task.getValue();
            if (object instanceof CajaTB) {
                CajaTB cajaTB = (CajaTB) object;
                switch (cajaTB.getId()) {
                    case 1:
                        openWindowFondoInicial();
                        break;
                    case 2:
                        aperturaCaja = true;
                        hbContenedorVentas.setDisable(false);
                        Session.CAJA_ID = cajaTB.getIdCaja();
                        break;
                    case 3:
                        openWindowValidarCaja(cajaTB.getFechaApertura() + " " + cajaTB.getHoraApertura());
                        break;
                    default:
                        break;
                }
            } else {
                openWindowCajaNoRegistrada("No se pudo verificar el estado de su caja por problemas de red, intente nuevamente.");
            }
        });

        task.setOnFailed(e -> {
//            vbPrincipal.getChildren().remove(ObjectGlobal.PANE);
        });

        task.setOnScheduled(e -> {
//            ObjectGlobal.InitializationTransparentBackground(vbPrincipal);
        });

        exec.execute(task);
        if (!exec.isShutdown()) {
            exec.shutdown();
        }
    }

    public void openWindowCajaNoRegistrada(String mensaje) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_CAJA_NO_REGISTRADA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());

            FxPostCajaNoRegistrada controller = fXMLLoader.getController();
            controller.initElements(mensaje);

            Stage stage = WindowStage.StageLoaderModal(parent, "Caja no registrada", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> {
           fxPrincipalController.closeFondoModal();
                if (aperturaCaja) {
                    aperturaCaja = false;
                    hbContenedorVentas.setDisable(true);
                }
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("venta controller openWindowFondoInicial():" + ex.getLocalizedMessage());
        }
    }

    public void openWindowFondoInicial() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_FONDO_INICIAL);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxPostVentaFondoInicialController controller = fXMLLoader.getController();
            controller.setInitVentaController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Fondo inicial", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> {
                fxPrincipalController.closeFondoModal();
                hbContenedorVentas.setDisable(!aperturaCaja);
            });
            stage.show();
        } catch (IOException ex) {
            System.out.println("venta controller openWindowFondoInicial():" + ex.getLocalizedMessage());
        }
    }

    private void openWindowValidarCaja(String dateTime) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_POS_VENTA_VALIDAR_CAJA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            //Controlller here
            FxPostVentaValidarCajaController controller = fXMLLoader.getController();
            controller.setInitVentaController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Validar apertura y cierre de caja", window.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> {
                fxPrincipalController.closeFondoModal();
                if (aperturaCaja) {
                    aperturaCaja = false;
                    hbContenedorVentas.setDisable(true);
                }
            });
            stage.show();
            controller.loadData(dateTime);
        } catch (IOException ex) {
            System.out.println("venta controller openWindowFondoInicial():" + ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPressedAgregarVenta(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            addTabVentaEstructura();
        }
    }

    @FXML
    private void onActionAgregarVenta(ActionEvent event) {
        addTabVentaEstructura();
    }

    public void setAperturaCaja(boolean aperturaCaja) {
        this.aperturaCaja = aperturaCaja;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

    public void setTipoVenta(short tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

}
