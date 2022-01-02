package controller.tools;

import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class WindowStage {

    public static FXMLLoader LoaderWindow(URL ruta) {
        FXMLLoader fXMLLoader = new FXMLLoader();
        URL url = ruta;
        fXMLLoader.setLocation(url);
        fXMLLoader.setBuilderFactory(new JavaFXBuilderFactory());
        return fXMLLoader;
    }

    public static Stage StageLoaderModal(Parent parent, String title, Window window) {
        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        stage.setScene(scene);
        stage.setTitle(title);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);
        return stage;
    }

    public static Stage StageLoader(Parent parent, String title) {
        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        stage.setScene(scene);
        stage.setTitle(title);
        return stage;
    }

}
