package controller.preloader;

import com.sun.javafx.application.LauncherImpl;
import controller.login.FxLoginController;
import controller.tools.FilesRouters;
import controller.tools.WindowStage;
import controller.tools.Session;
import controller.tools.Tools;
import java.net.URL;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.DBUtil;

public class SysSoftIntegra extends Application {

    private FxLoginController loginController;

    private Scene scene;

    @Override
    public void init() throws Exception {
       
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        if (Session.CONFIGURATION_STATE) {

            URL urllogin = getClass().getResource(FilesRouters.FX_BIENVENIDA);
            FXMLLoader fXMLLoaderLogin = WindowStage.LoaderWindow(urllogin);
            Parent parent = fXMLLoaderLogin.load(urllogin.openStream());
            scene = new Scene(parent);

            primaryStage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setTitle(FilesRouters.TITLE_APP);
            primaryStage.centerOnScreen();
//            primaryStage.setMaximized(true);
            primaryStage.show();
            primaryStage.requestFocus();

        } else {
            URL urllogin = getClass().getResource(FilesRouters.FX_LOGIN);
            FXMLLoader fXMLLoaderLogin = WindowStage.LoaderWindow(urllogin);
            Parent parent = fXMLLoaderLogin.load(urllogin.openStream());
            loginController = fXMLLoaderLogin.getController();
            scene = new Scene(parent);

            primaryStage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
            primaryStage.setScene(scene);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setTitle(FilesRouters.TITLE_APP);
            primaryStage.centerOnScreen();
            primaryStage.setMaximized(true);
            primaryStage.setOnCloseRequest(c -> {
                short option = Tools.AlertMessageConfirmation(parent, "SysSoft Integra", "¿Está seguro de cerrar la aplicación?");
                if (option == 1) {
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
            primaryStage.show();
            primaryStage.requestFocus();
            loginController.initComponents();
        }
    }

    public static void main(String[] args) {
        LauncherImpl.launchApplication(SysSoftIntegra.class, SplashScreen.class, args);
    }

}
