package controller.menus;

import controller.login.FxLoginController;
import controller.tools.FilesRouters;
import controller.tools.Session;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.MenuTB;
import model.SubMenusTB;
import service.MenuADO;
import service.VentaADO;

import org.json.simple.JSONArray;

public class FxPrincipalController implements Initializable {

    @FXML
    private ScrollPane spWindow;
    @FXML
    private HBox hbFondoModal;
    @FXML
    private HBox hbLoadModulos;
    @FXML
    private AnchorPane vbContent;
    @FXML
    private Label lblPuesto;
    @FXML
    private VBox hbMenus;
    @FXML
    private HBox btnInicio;
    @FXML
    private HBox btnOperaciones;
    @FXML
    private HBox btnConsultas;
    @FXML
    private HBox btnInventario;
    @FXML
    private HBox btnProduccion;
    @FXML
    private HBox btnContactos;
    @FXML
    private HBox btnReportes;
    @FXML
    private HBox btnPosTerminal;
    @FXML
    private HBox btnConfiguracion;
    @FXML
    private Label lblDatos;
    @FXML
    private VBox vbSiderBar;
    @FXML
    private HBox hbLoadMessage;

    private HBox hbReferent;

    /* DASHBOARD CONTROLLER */
    private VBox fxBienvenida;

    /* DASHBOARD CONTROLLER */
    private AnchorPane fxInicio;

    /* DASHBOARD OPERACIONES */
    private HBox fxOperaciones;

    private FxOperacionesController operacionesController;

    /* CONSULTAS CONTROLLER */
    private HBox fxConsultas;

    private FxConsultasController consultasController;

    /* DASHBOARD INVENTARIO */
    private HBox fxInventario;

    private FxInventarioController inventarioController;

    /* DASHBOARD POS TERMINAR */
    private HBox fxPosTerminal;

    private FxPosTerminalController posTerminalController;

    /* DASHBOARD PRODUCCION */
    private HBox fxProduccion;

    /* DASHBOARD CONTACTOS */
    private HBox fxContactos;

    private FxContactosController contactosController;

    /* DASHBOARD REPORTES */
    private HBox fxReportes;

    /* DASHBOARD CONFIGURACION */
    private HBox fxConfiguracion;

    private FxConfiguracionController configuracionController;

    private boolean isExpand = true;

    private double width_siderbar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            FXMLLoader fXMLBienvenida = new FXMLLoader(getClass().getResource(FilesRouters.FX_INDEX));
            fxBienvenida = fXMLBienvenida.load();

            FXMLLoader fXMLInicio = new FXMLLoader(getClass().getResource(FilesRouters.FX_INICIO));
            fxInicio = fXMLInicio.load();

            FXMLLoader fXMLOperaciones = new FXMLLoader(getClass().getResource(FilesRouters.FX_OPERACIONES));
            fxOperaciones = fXMLOperaciones.load();
            operacionesController = fXMLOperaciones.getController();
            operacionesController.setContent(this);

            FXMLLoader fXMLConsultas = new FXMLLoader(getClass().getResource(FilesRouters.FX_CONSULTAS));
            fxConsultas = fXMLConsultas.load();
            consultasController = fXMLConsultas.getController();
            consultasController.setContent(this);

            FXMLLoader fXMLInventario = new FXMLLoader(getClass().getResource(FilesRouters.FX_INVENTARIO));
            fxInventario = fXMLInventario.load();
            inventarioController = fXMLInventario.getController();
            inventarioController.setContent(this);

            FXMLLoader fXMLProduccion = new FXMLLoader(getClass().getResource(FilesRouters.FX_PRODUCCION));
            fxProduccion = fXMLProduccion.load();
            FxProduccionController produccionController = fXMLProduccion.getController();
            produccionController.setContent(this);

            FXMLLoader fXMLPosTerminal = new FXMLLoader(getClass().getResource(FilesRouters.FX_POSTERMINAL));
            fxPosTerminal = fXMLPosTerminal.load();
            posTerminalController = fXMLPosTerminal.getController();
            posTerminalController.setContent(this);

            FXMLLoader fXMLContactos = new FXMLLoader(getClass().getResource(FilesRouters.FX_CONTACTOS));
            fxContactos = fXMLContactos.load();
            contactosController = fXMLContactos.getController();
            contactosController.setContent(this);

            FXMLLoader fXMLReportes = new FXMLLoader(getClass().getResource(FilesRouters.FX_REPORTES));
            fxReportes = fXMLReportes.load();
            FxReportesController controllerReportes = fXMLReportes.getController();
            controllerReportes.setContent(this);

            FXMLLoader fXMLConfiguracion = new FXMLLoader(getClass().getResource(FilesRouters.FX_CONFIGURACION));
            fxConfiguracion = fXMLConfiguracion.load();
            configuracionController = fXMLConfiguracion.getController();
            configuracionController.setContent(this);
        } catch (IOException ex) {
            System.out.println("Error en controller principal:" + ex.getLocalizedMessage());
        }
    }

    public void initLoadMenus() {
        ExecutorService executor = Executors.newCachedThreadPool((runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ObservableList<MenuTB>> task = new Task<ObservableList<MenuTB>>() {
            @Override
            protected ObservableList<MenuTB> call() {
                btnInicio.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxInicio, btnInicio));
                btnOperaciones.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxOperaciones, btnOperaciones));
                btnConsultas.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxConsultas, btnConsultas));
                btnInventario.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxInventario, btnInventario));
                btnProduccion.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxProduccion, btnProduccion));
                btnPosTerminal.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxPosTerminal, btnPosTerminal));
                btnContactos.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxContactos, btnContactos));
                btnReportes.setOnMouseClicked((event) -> onMouseClickedMenus(event, fxReportes, btnReportes));
                btnConfiguracion
                        .setOnMouseClicked((event) -> onMouseClickedMenus(event, fxConfiguracion, btnConfiguracion));
                return MenuADO.GetMenus(Session.USER_ROL);
            }
        };

        task.setOnSucceeded(e -> {
            ObservableList<MenuTB> menuTBs = task.getValue();
            // CONTROLADOR INICIO
            if (menuTBs.get(0).getIdMenu() != 0 && !menuTBs.get(0).isEstado()) {
                hbMenus.getChildren().remove(btnInicio);
            }
            // OPERACIONES OPERACIONES
            if (menuTBs.get(1).getIdMenu() != 0 && !menuTBs.get(1).isEstado()) {
                hbMenus.getChildren().remove(btnOperaciones);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(1).getIdMenu());
                operacionesController.loadSubMenus(subMenusTBs);
            }
            // OPERACIONES CONSULTAS
            if (menuTBs.get(2).getIdMenu() != 0 && !menuTBs.get(2).isEstado()) {
                hbMenus.getChildren().remove(btnConsultas);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(2).getIdMenu());
                consultasController.loadSubMenus(subMenusTBs);
            }
            // OPERACIONES INVENTARIO
            if (menuTBs.get(3).getIdMenu() != 0 && !menuTBs.get(3).isEstado()) {
                hbMenus.getChildren().remove(btnInventario);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(3).getIdMenu());
                inventarioController.loadSubMenus(subMenusTBs);
            }
            // OPERACIONES PRODUCCION
            if (menuTBs.get(4).getIdMenu() != 0 && !menuTBs.get(4).isEstado()) {
                hbMenus.getChildren().remove(btnProduccion);
            }

            // OPERACIONES POS TERMINAL
            if (menuTBs.get(5).getIdMenu() != 0 && !menuTBs.get(5).isEstado()) {
                hbMenus.getChildren().remove(btnPosTerminal);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(5).getIdMenu());
                posTerminalController.loadSubMenus(subMenusTBs);
            }

            // OPERACIONES CONTACTOS
            if (menuTBs.get(6).getIdMenu() != 0 && !menuTBs.get(6).isEstado()) {
                hbMenus.getChildren().remove(btnContactos);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(6).getIdMenu());
                contactosController.loadSubMenus(subMenusTBs);
            }

            // OPERACIONES REPORTES
            if (menuTBs.get(7).getIdMenu() != 0 && !menuTBs.get(7).isEstado()) {
                hbMenus.getChildren().remove(btnReportes);
            }

            // OPERACIONES CONFIGURACIÓN
            if (menuTBs.get(8).getIdMenu() != 0 && !menuTBs.get(8).isEstado()) {
                hbMenus.getChildren().remove(btnConfiguracion);
            } else {
                ObservableList<SubMenusTB> subMenusTBs = MenuADO.GetSubMenus(Session.USER_ROL,
                        menuTBs.get(8).getIdMenu());
                configuracionController.loadSubMenus(subMenusTBs);
            }

            // NOTIFICACIONES
            Object object = VentaADO.ListarNotificaciones();
            if (object instanceof JSONArray) {
                JSONArray jSONArray = (JSONArray) object;
                if (!jSONArray.isEmpty()) {
                    Platform.runLater(() -> {
                        hbLoadMessage.setVisible(true);
                    });
                }
            }

            Platform.runLater(() -> {
                hbLoadModulos.setVisible(false);
            });
        });

        executor.execute(task);
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public void initInicioController() {
        Stage stage = (Stage) spWindow.getScene().getWindow();
        stage.setOnCloseRequest(c -> {
            try {
                openFondoModal();
                short option = Tools.AlertMessageConfirmation(spWindow, "SysSoft Integra",
                        "¿Está seguro de cerrar la aplicación?");
                if (option == 1) {
                    System.exit(0);
                    Platform.exit();
                } else {
                    closeFondoModal();
                    c.consume();
                }
            } catch (Exception ex) {
                System.out.println("Close window:" + ex.getLocalizedMessage());
            }
        });

        width_siderbar = vbSiderBar.getPrefWidth();

        setNode(fxBienvenida);

    }

    public void initUserSession(String value) {
        lblPuesto.setText(value);
        lblDatos.setText(Session.USER_NAME);
    }

    private void setNode(Node node) {
        vbContent.getChildren().clear();
        AnchorPane.setLeftAnchor(node, 0d);
        AnchorPane.setTopAnchor(node, 0d);
        AnchorPane.setRightAnchor(node, 0d);
        AnchorPane.setBottomAnchor(node, 0d);
        vbContent.getChildren().add(node);
    }

    private void selectFocust(HBox hbButton) {
        if (hbReferent != null) {
            hbReferent.getStyleClass().remove("buttonMenuActivate");
            ((Label) hbReferent.getChildren().get(0)).getStyleClass().remove("buttonMenuActivateText");
        }

        hbReferent = hbButton;
        hbReferent.getStyleClass().add("buttonMenuActivate");
        ((Label) hbReferent.getChildren().get(0)).getStyleClass().add("buttonMenuActivateText");
    }

    private void onMouseClickedMenus(MouseEvent event, Node vista, HBox menu) {
        if (event.getSource() == menu) {
            setNode(vista);
            selectFocust(menu);
            event.consume();
        }
    }

    @FXML
    private void onMouseClickedSiderBar(MouseEvent event) {
        if (isExpand) {
            vbSiderBar.setMinWidth(0);
            vbSiderBar.setPrefWidth(0);
            isExpand = false;
        } else {
            vbSiderBar.setMinWidth(200);
            vbSiderBar.setPrefWidth(width_siderbar);
            isExpand = true;
        }
        event.consume();
    }

    @FXML
    private void onMouseClickedCerrar(MouseEvent event) throws IOException {
        Tools.Dispose(spWindow);
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
            short option = Tools.AlertMessageConfirmation(parent, "SysSoft Integra",
                    "¿Está seguro de cerrar la aplicación?");
            if (option == 1) {
                System.exit(0);
                Platform.exit();
            } else {
                c.consume();
            }
        });
        primaryStage.setOnShown(e -> controller.initComponents());
        primaryStage.show();
        primaryStage.requestFocus();
    }

    @FXML
    private void onKeyPressedAceptar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            hbLoadMessage.setVisible(false);
        }
    }

    @FXML
    private void onActionAceptar(ActionEvent event) {
        hbLoadMessage.setVisible(false);
    }

    public ScrollPane getSpWindow() {
        return spWindow;
    }

    public HBox getHbFondoModal() {
        return hbFondoModal;
    }

    public AnchorPane getVbContent() {
        return vbContent;
    }

    public void openFondoModal() {
        hbFondoModal.setVisible(true);
    }

    public void closeFondoModal() {
        hbFondoModal.setVisible(false);
    }

}
