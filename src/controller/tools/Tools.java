package controller.tools;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Formatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import org.controlsfx.control.Notifications;

public class Tools {

    private static final Pattern p = Pattern.compile("[0-9]+");

    public static short AlertMessageConfirmation(Node node, String title, String value) {
        return AlertMessage(node.getScene().getWindow(), Alert.AlertType.CONFIRMATION, title, value, true);
    }

    public static short AlertMessageInformation(Node node, String title, String value) {
        return AlertMessage(node.getScene().getWindow(), Alert.AlertType.INFORMATION, title, value, false);
    }

    public static short AlertMessageWarning(Node node, String title, String value) {
        return AlertMessage(node.getScene().getWindow(), Alert.AlertType.WARNING, title, value, false);
    }

    public static short AlertMessageError(Node node, String title, String value) {
        return AlertMessage(node.getScene().getWindow(), Alert.AlertType.ERROR, title, value, false);
    }

    public static short AlertMessage(Window window, Alert.AlertType type, String title, String message,
            boolean validation) {
        final URL url = Tools.class.getClass().getResource(FilesRouters.STYLE_ALERT);
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(window);
        alert.getDialogPane().getStylesheets().add(url.toExternalForm());
        alert.getButtonTypes().clear();

        ButtonType buttonTypeOne = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeTwo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType buttonTypeClose = new ButtonType("Aceptar", ButtonBar.ButtonData.CANCEL_CLOSE);
        if (validation) {
            alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo);
            Button buttonOne = (Button) alert.getDialogPane().lookupButton(buttonTypeOne);
            buttonOne.setDefaultButton(false);
            Button buttonTwo = (Button) alert.getDialogPane().lookupButton(buttonTypeTwo);
            buttonOne.setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.ENTER) {
                    alert.setResult(buttonTypeOne);
                }
            });
            buttonTwo.setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.ENTER) {
                    alert.setResult(buttonTypeTwo);
                }
            });
            Optional<ButtonType> optional = alert.showAndWait();
            return (short) (optional.get() == buttonTypeOne ? 1 : 0);
        } else {
            alert.getButtonTypes().setAll(buttonTypeClose);
            Button buttonOne = (Button) alert.getDialogPane().lookupButton(buttonTypeClose);
            buttonOne.setOnKeyPressed((KeyEvent event) -> {
                if (event.getCode() == KeyCode.ENTER) {
                    alert.setResult(buttonTypeClose);
                }
            });
            Optional<ButtonType> optional = alert.showAndWait();
            return (short) (optional.get() == buttonTypeClose ? 1 : 0);
        }
    }

    public static short AlertMessage(Window window, String title, String value) {
        final URL url = Tools.class.getClass().getResource(FilesRouters.STYLE_ALERT);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        alert.setTitle(title);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(window);
        alert.setHeaderText(null);
        alert.setContentText(value);
        alert.getDialogPane().getStylesheets().add(url.toExternalForm());
        alert.getButtonTypes().clear();

        ButtonType buttonTypeTwo = new ButtonType("Aceptar y no Imprimir", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeOne = new ButtonType("Aceptar e Imprimir", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeTwo, buttonTypeOne);
        Button buttonTwo = (Button) alert.getDialogPane().lookupButton(buttonTypeTwo);
        buttonTwo.setDefaultButton(false);
        Button buttonOne = (Button) alert.getDialogPane().lookupButton(buttonTypeOne);
        buttonTwo.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                alert.setResult(buttonTypeTwo);
            }
        });
        buttonOne.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                alert.setResult(buttonTypeOne);
            }
        });

        Optional<ButtonType> optional = alert.showAndWait();
        return (short) (optional.get() == buttonTypeTwo ? 0 : 1);

    }

    public static Alert AlertMessage(Window window, Alert.AlertType type, String value) {
        final URL url = Tools.class.getClass().getResource(FilesRouters.STYLE_ALERT);
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        alert.setTitle("Movimiento");
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(window);
        alert.setHeaderText(null);
        alert.setContentText(value);
        alert.getDialogPane().getStylesheets().add(url.toExternalForm());
        alert.show();
        return alert;
    }

    public static Alert AlertDialogMessage(Node node, Alert.AlertType type, String title, String value, String error) {
        final URL url = Tools.class.getClass().getResource(FilesRouters.STYLE_ALERT);
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(FilesRouters.IMAGE_ICON));
        alert.setTitle(title);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(node.getScene().getWindow());
        alert.setHeaderText(null);
        alert.setContentText(value);
        alert.getDialogPane().getStylesheets().add(url.toExternalForm());

        TextArea textArea = new TextArea(error);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
        return alert;
    }

    public static void DisposeWindow(AnchorPane window, EventType<KeyEvent> eventType) {
        window.addEventHandler(eventType, (KeyEvent event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Dispose(window);
                event.consume();
            }
        });
    }

    public static void Dispose(Node window) {
        Stage stage = (Stage) window.getScene().getWindow();
        stage.close();
    }

    public static void Dispose(AnchorPane window) {
        Stage stage = (Stage) window.getScene().getWindow();
        stage.close();
    }

    public static String roundingValue(double valor, int decimals) {
        BigDecimal decimal = BigDecimal.valueOf(valor);
        decimal = decimal.setScale(decimals, RoundingMode.HALF_UP);
        return decimal.toPlainString();
    }

    public static String formatNumber(String numero) {
        if (numero.length() >= 6) {
            return numero;
        }

        Formatter formatter = new Formatter();
        return String.valueOf(formatter.format("%06d", Integer.valueOf(numero)));
    }

    public static String formatNumber(int numero) {
        if (String.valueOf(numero).length() >= 6) {
            return String.valueOf(numero);
        }

        Formatter formatter = new Formatter();
        return String.valueOf(formatter.format("%06d", numero));
    }

    public static boolean isNumeric(String cadena) {
        if (cadena.trim() == null || cadena.trim().isEmpty()) {
            return false;
        }
        boolean resultado;
        try {
            Double.valueOf(cadena);
            resultado = true;
        } catch (NumberFormatException ex) {
            resultado = false;
        }
        return resultado;
    }

    public static boolean isNumericInteger(String cadena) {
        return cadena != null && p.matcher(cadena).find();
    }

    public static String[] getDataPeople(String data) {
        if (data != null) {
            return data.trim().split(" ");
        } else {
            return null;
        }
    }

    public static double calculateTax(double porcentaje, double valor) {
        double igv = (double) (porcentaje / 100.00);
        return (double) (valor * igv);
    }

    public static double calculateAumento(double margen, double costo) {
        double totalimporte = costo + (costo * (margen / 100.00));
        return Double.parseDouble(Tools.roundingValue(totalimporte, 1));
    }

    public static double calculateTaxBruto(double impuesto, double monto) {
        return monto / ((impuesto + 100) * 0.01);
    }

    public static void actualDate(String date, DatePicker datePicker) {
        if (date.contains("-")) {
            datePicker.setValue(LocalDate.of(Integer.parseInt(date.split("-")[0]), Integer.parseInt(date.split("-")[1]),
                    Integer.parseInt(date.split("-")[2])));
        } else if (date.contains("/")) {
            datePicker.setValue(LocalDate.of(Integer.parseInt(date.split("/")[0]), Integer.parseInt(date.split("/")[1]),
                    Integer.parseInt(date.split("/")[2])));
        }
    }

    public static String getDatePicker(DatePicker datePicker) {
        LocalDate localDate = datePicker.getValue();
        return localDate.toString();
    }

    public static String formatDate(String date) {
        String arr[] = date.split("-");
        if (arr != null && arr.length != 0) {
            return arr[2] + "/" + arr[1] + "/" + arr[0];
        } else {
            return date;
        }
    }

    public static void openFile(String ruta) {
        try {
            File path = new File(ruta);
            Desktop.getDesktop().open(path);
        } catch (IOException ex) {
            Logger.getLogger(Tools.class.getName()).log(Level.INFO, ex.getLocalizedMessage());
        }
    }

    public static boolean isText(String cadena) {
        return cadena == null || cadena.trim().isEmpty();
    }

    public static String getDate() {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    public static java.sql.Date getCurrentDate() throws ParseException {
        Date dateCurrent = new SimpleDateFormat("yyyy-MM-dd").parse(getDate());
        return new java.sql.Date(dateCurrent.getTime());
    }

    public static String getDate(String format) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    public static String getTime() {
        Date date = new Date();
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm:ss.SSSSSS");
        return hour.format(date);
    }

    public static String getTime(String format) {
        Date date = new Date();
        SimpleDateFormat hour = new SimpleDateFormat(format);
        return hour.format(date);
    }

    public static Timestamp getDateHour() {
        return new Timestamp(new Date().getTime());
    }

    public static String getFileExtension(File file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        int i = name.lastIndexOf('.');
        String ext = i > 0 ? name.substring(i + 1) : "";
        return ext;
    }

    public static void println(Object object) {
        System.out.println(object);
    }

    public static void print(Object object) {
        System.out.print(object);
    }

    public static byte[] getImageBytes(File file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] bs = new byte[1024];
            for (int readNum; (readNum = inputStream.read(bs)) != -1;) {
                outputStream.write(bs, 0, readNum);
            }
            return outputStream.toByteArray();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public static byte[] getImageBytes(Image image, String extension) {
        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension, byteOutput);
            return byteOutput.toByteArray();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        return null;
    }

    public static void showAlertNotification(String url, String title, String message, Duration duration, Pos pos) {
        Image image = new Image(url);
        ImageView imageView = new ImageView(image);
        double newWidth = Tools.newSizeImagePorcent(image.getWidth(), 40);
        double nweHeight = Tools.newSizeImagePorcent(image.getHeight(), 40);
        imageView.setFitWidth(newWidth);
        imageView.setFitHeight(nweHeight);
        Notifications notifications = Notifications.create()
                .title(title)
                .text(message)
                .graphic(imageView)
                .hideAfter(duration)
                .position(pos)
                .onAction(n -> {

                });
        notifications.darkStyle();
        notifications.show();
    }

    public static String AddText2Guines(String value) {
        return value.trim().isEmpty() ? "--" : value;
    }

    public static Label placeHolderTableView(String message, String styleCss, boolean viewImage) {
        Label label = new Label(message);
        label.setStyle(styleCss);
        label.getStyleClass().add("labelRoboto13");
        if (viewImage) {
            ImageView imageView = new ImageView(new Image("/view/image/load.gif"));
            imageView.setFitWidth(48);
            imageView.setFitHeight(48);
            label.setGraphic(imageView);
        }
        return label;
    }

    public static String colorFxDefault() {
        return "-fx-text-fill:#020203;";
    }

    public static String colorFxError() {
        return "-fx-text-fill:#a70820;";
    }

    public static double newSizeImagePorcent(double width, double porcent) {
        return width - (width * (porcent / 100));
    }

    public static int convertEmToPx(double em) {
        return (int) ((12 * em) / 1);
    }

    public static String textShow(String title, String text) {
        return text.isEmpty() ? "" : (title.toUpperCase() + "" + text.toUpperCase());
    }

    public static String text(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    public static String newLineString(String value) {
        String format = "";
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            count++;
            if (count == 40) {
                format = format + "\n";
                format = format + String.valueOf(c);
                count = 0;
            } else {
                format = format + String.valueOf(c);
            }
        }
        return format;
    }

    public static void scrollTo(ScrollPane scrollPane, Node node) {
        // Obtener las coordenadas del nodo hijo en relación a la vista del ScrollPane
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        Bounds scrollBounds = scrollPane.sceneToLocal(bounds);

        // Desplazar el contenido del ScrollPane para asegurarse de que el nodo hijo sea visible
        double hValue = Math.max(0, Math.min(1, (scrollBounds.getMinX() - 20) / (scrollPane.getContent().getBoundsInLocal().getWidth() - scrollBounds.getWidth())));
        double vValue = Math.max(0, Math.min(1, (scrollBounds.getMinY() - 20) / (scrollPane.getContent().getBoundsInLocal().getHeight() - scrollBounds.getHeight())));
        scrollPane.setHvalue(hValue);
        scrollPane.setVvalue(vValue);
        
        node.requestFocus();
    }
}
