package controller.configuracion.tickets;

import controller.configuracion.impresoras.FxImprimirController;
import controller.menus.FxPrincipalController;
import controller.tools.FilesRouters;
import controller.tools.ImageViewTicket;
import controller.tools.Json;
import controller.tools.Session;
import controller.tools.TextFieldTicket;
import controller.tools.Tools;
import controller.tools.WindowStage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ImagenTB;
import model.TicketTB;
import service.ImageADO;
import service.TicketADO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class FxTicketController implements Initializable {

    @FXML
    private VBox vbWindow;
    @FXML
    private VBox vbContenedor;
    @FXML
    private AnchorPane apEncabezado;
    @FXML
    private AnchorPane apDetalleCabecera;
    @FXML
    private AnchorPane apPie;
    @FXML
    private TextField txtAnchoColumna;
    @FXML
    private ComboBox<Pos> cbAlignment;
    @FXML
    private CheckBox cbMultilinea;
    @FXML
    private CheckBox cbEditable;
    @FXML
    private TextField txtVariable;
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblColumnas;
    @FXML
    private Label formatoTicket;
    @FXML
    private CheckBox cbPredeterminado;
    @FXML
    private ComboBox<String> cbFuente;
    @FXML
    private ComboBox<Float> cbSize;
    @FXML
    private TextField txtAncho;
    @FXML
    private TextField txtAlto;

    private FxPrincipalController fxPrincipalController;

    private TextFieldTicket tfAnterior;

    private TextFieldTicket tfReference;

    private File selectFile;

    private HBox hboxAnterior;

    private HBox hboxReference;

    private short sheetWidth;

    private double pointWidth;

    private int idTicket;

    private int tipoTicket;

    private String ruta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pointWidth = 8.10;
        sheetWidth = 0;
        cbAlignment.getItems().add(Pos.CENTER_LEFT);
        cbAlignment.getItems().add(Pos.CENTER);
        cbAlignment.getItems().add(Pos.CENTER_RIGHT);
        cbFuente.getItems().addAll("Consola", "Roboto Regular", "Roboto Bold", "Qwitcher Grypen", "Dancing Scrip");
        cbSize.getItems().addAll(10.5f, 12.5f, 14.5f, 16.5f, 18.5f, 20.5f, 22.5f, 24.5f);
    }

    public void loadTicket(int idTicket, int tipoTicket, String nombre, String ruta, boolean predeterminado) {
        if (ruta != null) {
            this.idTicket = idTicket;
            this.tipoTicket = tipoTicket;
            this.ruta = ruta;
            hboxReference = null;
            JSONObject jSONObject = Json.obtenerObjetoJSON(ruta);
            apEncabezado.getChildren().clear();
            apDetalleCabecera.getChildren().clear();
            apPie.getChildren().clear();
            lblNombre.setText(nombre);
            formatoTicket.setText(tipoTicket + "");
            cbPredeterminado.setSelected(predeterminado);
            cbPredeterminado.setText(predeterminado ? "SI" : "NO");
            ArrayList<ImagenTB> imagenTBs = ImageADO.ListaImagePorIdRelacionado(idTicket);
            sheetWidth = jSONObject.get("column") != null ? Short.parseShort(jSONObject.get("column").toString())
                    : (short) 40;
            lblColumnas.setText("" + sheetWidth);
            vbContenedor.setPrefWidth(sheetWidth * pointWidth);
            if (jSONObject.get("cabecera") != null) {
                JSONObject cabeceraObjects = Json.obtenerObjetoJSON(jSONObject.get("cabecera").toString());
                for (int i = 0; i < cabeceraObjects.size(); i++) {
                    HBox box = generateElement(apEncabezado, "cb");
                    JSONObject objectObtener = Json.obtenerObjetoJSON(cabeceraObjects.get("cb_" + (i + 1)).toString());
                    if (objectObtener.get("text") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                Boolean.valueOf(object.get("multiline").toString()),
                                Short.parseShort(object.get("lines").toString()),
                                Short.parseShort(object.get("width").toString()),
                                getAlignment(object.get("align").toString()),
                                Boolean.parseBoolean(object.get("editable").toString()),
                                String.valueOf(object.get("variable").toString()),
                                String.valueOf(object.get("font").toString()),
                                Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    } else if (objectObtener.get("list") != null) {
                        JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                        Iterator it = array.iterator();
                        while (it.hasNext()) {
                            JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                            TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                    Boolean.valueOf(object.get("multiline").toString()),
                                    Short.parseShort(object.get("lines").toString()),
                                    Short.parseShort(object.get("width").toString()),
                                    getAlignment(object.get("align").toString()),
                                    Boolean.parseBoolean(object.get("editable").toString()),
                                    String.valueOf(object.get("variable").toString()),
                                    String.valueOf(object.get("font").toString()),
                                    Float.valueOf(object.get("size").toString()));
                            box.getChildren().add(field);
                        }
                    } else if (objectObtener.get("image") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                        ImageViewTicket imageView = addElementImageView("",
                                Short.parseShort(object.get("width").toString()),
                                Double.parseDouble(object.get("fitwidth").toString()),
                                Double.parseDouble(object.get("fitheight").toString()), false,
                                object.get("type").toString());
                        imageView.setId(String.valueOf(object.get("value").toString()));
                        box.setPrefWidth(imageView.getColumnWidth() * pointWidth);
                        box.setPrefHeight(imageView.getFitHeight());
                        box.setAlignment(getAlignment(object.get("align").toString()));
                        box.getChildren().add(imageView);
                    }
                }
            }
            if (jSONObject.get("detalle") != null) {
                JSONObject detalleObjects = Json.obtenerObjetoJSON(jSONObject.get("detalle").toString());
                for (int i = 0; i < detalleObjects.size(); i++) {
                    HBox box = generateElement(apDetalleCabecera, "dr");
                    JSONObject objectObtener = Json.obtenerObjetoJSON(detalleObjects.get("dr_" + (i + 1)).toString());
                    if (objectObtener.get("text") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                Boolean.valueOf(object.get("multiline").toString()),
                                Short.parseShort(object.get("lines").toString()),
                                Short.parseShort(object.get("width").toString()),
                                getAlignment(object.get("align").toString()),
                                Boolean.parseBoolean(object.get("editable").toString()),
                                String.valueOf(object.get("variable").toString()),
                                String.valueOf(object.get("font").toString()),
                                Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    } else if (objectObtener.get("list") != null) {
                        JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                        Iterator it = array.iterator();
                        while (it.hasNext()) {
                            JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                            TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                    Boolean.valueOf(object.get("multiline").toString()),
                                    Short.parseShort(object.get("lines").toString()),
                                    Short.parseShort(object.get("width").toString()),
                                    getAlignment(object.get("align").toString()),
                                    Boolean.parseBoolean(object.get("editable").toString()),
                                    String.valueOf(object.get("variable").toString()),
                                    String.valueOf(object.get("font").toString()),
                                    Float.valueOf(object.get("size").toString()));
                            box.getChildren().add(field);
                        }
                    } else if (objectObtener.get("image") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                        ImageViewTicket imageView = addElementImageView("",
                                Short.parseShort(object.get("width").toString()),
                                Double.parseDouble(object.get("fitwidth").toString()),
                                Double.parseDouble(object.get("fitheight").toString()), false,
                                object.get("type").toString());
                        imageView.setId(String.valueOf(object.get("value").toString()));
                        box.setPrefWidth(imageView.getColumnWidth() * pointWidth);
                        box.setPrefHeight(imageView.getFitHeight());
                        box.setAlignment(getAlignment(object.get("align").toString()));
                        box.getChildren().add(imageView);
                    }
                }
            }
            if (jSONObject.get("pie") != null) {
                JSONObject pieObjects = Json.obtenerObjetoJSON(jSONObject.get("pie").toString());
                for (int i = 0; i < pieObjects.size(); i++) {
                    HBox box = generateElement(apPie, "cp");
                    JSONObject objectObtener = Json.obtenerObjetoJSON(pieObjects.get("cp_" + (i + 1)).toString());
                    if (objectObtener.get("text") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("text").toString());
                        TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                Boolean.valueOf(object.get("multiline").toString()),
                                Short.parseShort(object.get("lines").toString()),
                                Short.parseShort(object.get("width").toString()),
                                getAlignment(object.get("align").toString()),
                                Boolean.parseBoolean(object.get("editable").toString()),
                                String.valueOf(object.get("variable").toString()),
                                String.valueOf(object.get("font").toString()),
                                Float.valueOf(object.get("size").toString()));
                        box.getChildren().add(field);
                    } else if (objectObtener.get("list") != null) {
                        JSONArray array = Json.obtenerArrayJSON(objectObtener.get("list").toString());
                        Iterator it = array.iterator();
                        while (it.hasNext()) {
                            JSONObject object = Json.obtenerObjetoJSON(it.next().toString());
                            TextFieldTicket field = addElementTextField("iu", object.get("value").toString(),
                                    Boolean.valueOf(object.get("multiline").toString()),
                                    Short.parseShort(object.get("lines").toString()),
                                    Short.parseShort(object.get("width").toString()),
                                    getAlignment(object.get("align").toString()),
                                    Boolean.parseBoolean(object.get("editable").toString()),
                                    String.valueOf(object.get("variable").toString()),
                                    String.valueOf(object.get("font").toString()),
                                    Float.valueOf(object.get("size").toString()));
                            box.getChildren().add(field);
                        }
                    } else if (objectObtener.get("image") != null) {
                        JSONObject object = Json.obtenerObjetoJSON(objectObtener.get("image").toString());
                        ImageViewTicket imageView = addElementImageView("",
                                Short.parseShort(object.get("width").toString()),
                                Double.parseDouble(object.get("fitwidth").toString()),
                                Double.parseDouble(object.get("fitheight").toString()), false,
                                object.get("type").toString());
                        imageView.setId(String.valueOf(object.get("value").toString()));
                        box.setPrefWidth(imageView.getColumnWidth() * pointWidth);
                        box.setPrefHeight(imageView.getFitHeight());
                        box.setAlignment(getAlignment(object.get("align").toString()));
                        box.getChildren().add(imageView);
                    }
                }
            }
            for (int i = 0; i < imagenTBs.size(); i++) {
                for (int m = 0; m < apEncabezado.getChildren().size(); m++) {
                    HBox hBox = (HBox) apEncabezado.getChildren().get(m);
                    if (hBox.getChildren().size() == 1) {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof ImageViewTicket) {
                            ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                                imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                                imageViewTicket
                                        .setImage(new Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < imagenTBs.size(); i++) {
                for (int m = 0; m < apDetalleCabecera.getChildren().size(); m++) {
                    HBox hBox = (HBox) apDetalleCabecera.getChildren().get(m);
                    if (hBox.getChildren().size() == 1) {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof ImageViewTicket) {
                            ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                                imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                                imageViewTicket
                                        .setImage(new Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < imagenTBs.size(); i++) {
                for (int m = 0; m < apPie.getChildren().size(); m++) {
                    HBox hBox = (HBox) apPie.getChildren().get(m);
                    if (hBox.getChildren().size() == 1) {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof ImageViewTicket) {
                            ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            if (imagenTBs.get(i).getIdSubRelacion().equalsIgnoreCase(imageViewTicket.getId())) {
                                imageViewTicket.setUrl(imagenTBs.get(i).getImagen());
                                imageViewTicket
                                        .setImage(new Image(new ByteArrayInputStream(imagenTBs.get(i).getImagen())));
                            }
                        }
                    }
                }
            }
        }
    }

    private void saveTicket() {
        try {
            int valideteUno = 0;
            int valideteDos = 0;
            int valideteTree = 0;
            for (int i = 0; i < apEncabezado.getChildren().size(); i++) {
                HBox hBox = (HBox) apEncabezado.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteUno++;
                }
            }

            for (int i = 0; i < apDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = (HBox) apDetalleCabecera.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteDos++;
                }
            }

            for (int i = 0; i < apPie.getChildren().size(); i++) {
                HBox hBox = (HBox) apPie.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteTree++;
                }
            }

            if (valideteUno > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en la cabecera sin contenido");
                return;
            }

            if (valideteDos > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en el detalle sin contenido");
                return;
            }

            if (valideteTree > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en el pie sin contenido");
                return;
            }

            if (!apEncabezado.getChildren().isEmpty() && !apDetalleCabecera.getChildren().isEmpty()
                    && !apPie.getChildren().isEmpty()) {
                JSONObject sampleObject = new JSONObject();
                sampleObject.put("column", sheetWidth);
                JSONObject cabecera = new JSONObject();
                for (int i = 0; i < apEncabezado.getChildren().size(); i++) {
                    HBox hBox = (HBox) apEncabezado.getChildren().get(i);
                    JSONObject cb = new JSONObject();
                    if (hBox.getChildren().size() > 1) {
                        JSONArray kids = new JSONArray();
                        for (int v = 0; v < hBox.getChildren().size(); v++) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(v);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            kids.add(cbkid);
                        }
                        cb.put("list", kids);
                    } else {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof TextFieldTicket) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            cb.put("text", cbkid);
                        } else if (object instanceof ImageViewTicket) {
                            ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", viewTicket.getId());
                            cbkid.put("width", viewTicket.getColumnWidth());
                            cbkid.put("align", hBox.getAlignment().toString());
                            cbkid.put("variable", "");
                            cbkid.put("fitwidth", viewTicket.getFitWidth());
                            cbkid.put("fitheight", viewTicket.getFitHeight());
                            cbkid.put("type", viewTicket.getType());
                            cb.put("image", cbkid);
                        }

                    }
                    cabecera.put("cb_" + (i + 1), cb);
                }

                JSONObject detalle = new JSONObject();
                for (int i = 0; i < apDetalleCabecera.getChildren().size(); i++) {
                    HBox hBox = (HBox) apDetalleCabecera.getChildren().get(i);
                    JSONObject cb = new JSONObject();
                    if (hBox.getChildren().size() > 1) {
                        JSONArray kids = new JSONArray();
                        for (int v = 0; v < hBox.getChildren().size(); v++) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(v);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            kids.add(cbkid);
                        }
                        cb.put("list", kids);
                    } else {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof TextFieldTicket) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            cb.put("text", cbkid);
                        } else if (object instanceof ImageViewTicket) {
                            ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", viewTicket.getId());
                            cbkid.put("width", viewTicket.getColumnWidth());
                            cbkid.put("align", hBox.getAlignment().toString());
                            cbkid.put("variable", "");
                            cbkid.put("fitwidth", viewTicket.getFitWidth());
                            cbkid.put("fitheight", viewTicket.getFitHeight());
                            cbkid.put("type", viewTicket.getType());
                            cb.put("image", cbkid);
                        }
                    }
                    detalle.put("dr_" + (i + 1), cb);
                }

                JSONObject pie = new JSONObject();
                for (int i = 0; i < apPie.getChildren().size(); i++) {
                    HBox hBox = (HBox) apPie.getChildren().get(i);
                    JSONObject cb = new JSONObject();
                    if (hBox.getChildren().size() > 1) {
                        JSONArray kids = new JSONArray();
                        for (int v = 0; v < hBox.getChildren().size(); v++) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(v);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            kids.add(cbkid);
                        }
                        cb.put("list", kids);
                    } else {
                        Object object = hBox.getChildren().get(0);
                        if (object instanceof TextFieldTicket) {
                            TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", field.getText());
                            cbkid.put("width", field.getColumnWidth());
                            cbkid.put("align", field.getAlignment().toString());
                            cbkid.put("multiline", field.isMultilineas());
                            cbkid.put("lines", field.getLines());
                            cbkid.put("editable", field.isEditable());
                            cbkid.put("variable", field.getVariable());
                            cbkid.put("font", field.getFontName());
                            cbkid.put("size", field.getFontSize());
                            cb.put("text", cbkid);
                        } else if (object instanceof ImageViewTicket) {
                            ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                            JSONObject cbkid = new JSONObject();
                            cbkid.put("value", viewTicket.getId());
                            cbkid.put("width", viewTicket.getColumnWidth());
                            cbkid.put("align", hBox.getAlignment().toString());
                            cbkid.put("variable", "");
                            cbkid.put("fitwidth", viewTicket.getFitWidth());
                            cbkid.put("fitheight", viewTicket.getFitHeight());
                            cbkid.put("type", viewTicket.getType());
                            cb.put("image", cbkid);
                        }
                    }
                    pie.put("cp_" + (i + 1), cb);
                }

                sampleObject.put("cabecera", cabecera);
                sampleObject.put("detalle", detalle);
                sampleObject.put("pie", pie);

                Files.write(Paths.get("./archivos/ticketventa.json"), sampleObject.toJSONString().getBytes());
                TicketTB ticketTB = new TicketTB();
                ticketTB.setId(idTicket);
                ticketTB.setNombreTicket(lblNombre.getText().trim().toUpperCase());
                ticketTB.setRuta(sampleObject.toJSONString());
                ticketTB.setTipo(tipoTicket);
                ticketTB.setPredeterminado(cbPredeterminado.isSelected());
                ticketTB.setApCabecera(apEncabezado);
                ticketTB.setApDetalle(apDetalleCabecera);
                ticketTB.setApPie(apPie);

                String result = TicketADO.CrudTicket(ticketTB);
                if (result.equalsIgnoreCase("duplicate")) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket",
                            "El nombre del formato ya existe, intente con otro.");
                } else if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(vbWindow, "Ticket", "Se actualizo correctamente el formato.");
                    if (cbPredeterminado.isSelected()) {
                        switch (tipoTicket) {
                            case 1:
                                Session.TICKET_VENTA_ID = idTicket;
                                Session.TICKET_VENTA_RUTA = sampleObject.toJSONString();
                                break;
                            case 2:
                                Session.TICKET_COMPRA_ID = idTicket;
                                Session.TICKET_COMPRA_RUTA = sampleObject.toJSONString();
                                break;
                            case 5:
                                Session.TICKET_CORTE_CAJA_ID = idTicket;
                                Session.TICKET_CORTE_CAJA_RUTA = sampleObject.toJSONString();
                                break;
                            case 7:
                                Session.TICKET_PRE_VENTA_ID = idTicket;
                                Session.TICKET_PRE_VENTA_RUTA = sampleObject.toJSONString();
                                break;
                            case 8:
                                Session.TICKET_COTIZACION_ID = idTicket;
                                Session.TICKET_COTIZACION_RUTA = sampleObject.toJSONString();
                                break;
                            case 9:
                                Session.TICKET_CUENTA_POR_COBRAR_ID = idTicket;
                                Session.TICKET_CUENTA_POR_COBRAR_RUTA = sampleObject.toJSONString();
                                break;
                            case 10:
                                Session.TICKET_CUENTA_POR_PAGAR_ID = idTicket;
                                Session.TICKET_CUENTA_POR_PAGAR_RUTA = sampleObject.toJSONString();
                                break;
                            case 11:
                                Session.TICKET_GUIA_REMISION_ID = idTicket;
                                Session.TICKET_GUIA_REMISION_RUTA = sampleObject.toJSONString();
                                break;
                            case 12:
                                Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID = idTicket;
                                Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA = sampleObject.toJSONString();
                                break;
                            case 13:
                                Session.TICKET_PEDIDO_ID = idTicket;
                                Session.TICKET_PEDIDO_RUTA = sampleObject.toJSONString();
                                break;
                            case 14:
                                Session.TICKET_ORDEN_COMPRA_ID = idTicket;
                                Session.TICKET_ORDEN_COMPRA_RUTA = sampleObject.toJSONString();
                                break;
                            case 15:
                                Session.TICKET_NOTA_CREDITO_ID = idTicket;
                                Session.TICKET_NOTA_CREDITO_RUTA = sampleObject.toJSONString();
                                break;
                            default:
                                break;
                        }
                    }
                    clearPane();
                } else if (result.equalsIgnoreCase("registered")) {
                    Tools.AlertMessageInformation(vbWindow, "Ticket",
                            "Se guardo correctamente el formato, recuerda siempre poner en predeterminado el ticket, si lo va utilizar frecuentemente.");
                    clearPane();
                } else {
                    Tools.AlertMessageError(vbWindow, "Ticket", result);
                }
            } else {
                if (apEncabezado.getChildren().isEmpty()) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket", "El encabezado está vacío.");
                } else if (apDetalleCabecera.getChildren().isEmpty()) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket", "El detalle cabecera está vacío.");
                } else if (apPie.getChildren().isEmpty()) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket", "El pie está vacío.");
                }
            }

        } catch (IOException ex) {
            Tools.AlertMessageError(vbWindow, "Ticket", "No se pudo guardar la hoja con problemas de formato.");
        }
    }

    private HBox generateElement(AnchorPane contenedor, String id) {
        if (contenedor.getChildren().isEmpty()) {
            return addElement(contenedor, id + "1", true);
        } else {
            HBox hBox = (HBox) contenedor.getChildren().get(contenedor.getChildren().size() - 1);
            String idGenerate = hBox.getId();
            String codigo = idGenerate.substring(2);
            int valor = Integer.parseInt(codigo) + 1;
            String newCodigo = id + valor;
            return addElement(contenedor, newCodigo, true);
        }
    }

    private HBox addElement(AnchorPane contenedor, String id, boolean useLayout) {
        double layoutY = 0;
        if (useLayout) {
            for (int i = 0; i < contenedor.getChildren().size(); i++) {
                layoutY += ((HBox) contenedor.getChildren().get(i)).getPrefHeight();
            }
        }

        ImageView imgRemove = new ImageView(new Image("/view/image/remove-item.png"));
        imgRemove.setFitWidth(20);
        imgRemove.setFitHeight(20);

        ImageView imgMoveDown = new ImageView(new Image("/view/image/movedown.png"));
        imgMoveDown.setFitWidth(20);
        imgMoveDown.setFitHeight(20);

        ImageView imgMoveUp = new ImageView(new Image("/view/image/moveup.png"));
        imgMoveUp.setFitWidth(20);
        imgMoveUp.setFitHeight(20);

        ImageView imgText = new ImageView(new Image("/view/image/text.png"));
        imgText.setFitWidth(20);
        imgText.setFitHeight(20);

        ImageView imgTextVariable = new ImageView(new Image("/view/image/text-variable.png"));
        imgTextVariable.setFitWidth(20);
        imgTextVariable.setFitHeight(20);

        ImageView imgTextLines = new ImageView(new Image("/view/image/text-lines.png"));
        imgTextLines.setFitWidth(20);
        imgTextLines.setFitHeight(20);

        ImageView imgUnaLinea = new ImageView(new Image("/view/image/linea.png"));
        imgUnaLinea.setFitWidth(20);
        imgUnaLinea.setFitHeight(20);

        ImageView imgDobleLinea = new ImageView(new Image("/view/image/doble-linea.png"));
        imgDobleLinea.setFitWidth(20);
        imgDobleLinea.setFitHeight(20);

        ImageView imgImage = new ImageView(new Image("/view/image/photo.png"));
        imgImage.setFitWidth(20);
        imgImage.setFitHeight(20);

        HBox hBox = new HBox();
        hBox.setId(id);
        hBox.setLayoutX(0);
        hBox.setLayoutY(layoutY);
        hBox.setPrefWidth(sheetWidth * pointWidth);
        // font size 12.5px
        hBox.setPrefHeight(30);
        hBox.setStyle(
                "-fx-padding:0 20 0 0;-fx-border-width: 1 1 1 1;-fx-border-color: #0066ff;;-fx-background-color: white;");
        hBox.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            hboxAnterior = hboxReference;
            if (hboxAnterior != null) {
                hboxAnterior.setStyle(
                        "-fx-padding:0 20 0 0;-fx-border-width: 1 1 1 1;-fx-border-color: #0066ff;-fx-background-color: white;");
            }
            hBox.setStyle(
                    "-fx-padding:0 20 0 0;-fx-border-width: 1 1 1 1;-fx-border-color: #0066ff;-fx-background-color: rgb(250, 198, 203);");

            hboxReference = hBox;
            if (hboxReference.getChildren().size() == 1) {
                Object object = hboxReference.getChildren().get(0);
                if (object instanceof ImageViewTicket) {
                    ImageViewTicket viewTicket = (ImageViewTicket) hboxReference.getChildren().get(0);
                    txtAncho.setText(Tools.roundingValue(viewTicket.getFitWidth(), 0));
                    txtAlto.setText(Tools.roundingValue(viewTicket.getFitHeight(), 0));
                }
            }
        });
        ContextMenu contextMenu = new ContextMenu();
        MenuItem remove = new MenuItem("Remover Renglón");
        remove.setGraphic(imgRemove);
        remove.setOnAction(e -> {
            for (int b = 0; b < contenedor.getChildren().size(); b++) {
                if (contenedor.getChildren().get(b).getId().equalsIgnoreCase(hBox.getId())) {
                    contenedor.getChildren().remove(b);
                    double yPosBefero = 0;
                    for (int p = 0; p < contenedor.getChildren().size(); p++) {
                        HBox hb = (HBox) contenedor.getChildren().get(p);
                        if (p == 0) {
                            double heightNow = hb.getPrefHeight();
                            hb.setLayoutY(p * heightNow);
                            yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                        } else {
                            hb.setLayoutY(yPosBefero);
                            yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                        }

                    }
                    contenedor.layout();
                    break;
                }
            }

            int valor = 0;
            for (int n = 0; n < contenedor.getChildren().size(); n++) {
                HBox boxn = (HBox) contenedor.getChildren().get(n);
                if (boxn.getChildren().size() == 1) {
                    Object object = boxn.getChildren().get(0);
                    if (object instanceof ImageViewTicket) {
                        valor++;
                        ImageViewTicket imageViewTicket = (ImageViewTicket) boxn.getChildren().get(0);
                        imageViewTicket.setId(boxn.getParent().equals(apEncabezado) ? "imc" + valor
                                : boxn.getParent().equals(apDetalleCabecera) ? "imd" + valor
                                        : boxn.getParent().equals(apPie) ? "imp" + valor : "");
                    }
                }
            }

        });

        MenuItem text = new MenuItem("Agregar Texto");
        text.setGraphic(imgText);
        text.setOnAction(e -> {
            TextFieldTicket field = addElementTextField("iu", "Escriba", false, (short) 0, (short) 7, Pos.CENTER_LEFT,
                    true, "", "Consola", 12.5f);
            hBox.getChildren().add(field);
        });

        MenuItem textVariable = new MenuItem("Agregar Variable");
        textVariable.setGraphic(imgTextVariable);
        textVariable.setOnAction(e -> {
            windowTextVar(hBox);
        });

        MenuItem textMultilinea = new MenuItem("Agregar Texto Multilínea");
        textMultilinea.setGraphic(imgTextLines);
        textMultilinea.setOnAction(e -> {
            windowTextMultilinea(hBox);
        });

        MenuItem textUnaLinea = new MenuItem("Agregar Línea");
        textUnaLinea.setGraphic(imgUnaLinea);
        textUnaLinea.setOnAction(e -> {
            String value = "";
            for (int i = 0; i < sheetWidth; i++) {
                value += "-";
            }
            TextFieldTicket field = addElementTextField("iu", value, false, (short) 0, sheetWidth, Pos.CENTER_LEFT,
                    false, "", "Consola", 12.5f);
            hBox.getChildren().add(field);
        });

        MenuItem textDosLineas = new MenuItem("Agregar Doble Línea");
        textDosLineas.setGraphic(imgDobleLinea);
        textDosLineas.setOnAction(e -> {
            String value = "";
            for (int i = 0; i < sheetWidth; i++) {
                value += "=";
            }
            TextFieldTicket field = addElementTextField("iu", value, false, (short) 0, sheetWidth, Pos.CENTER_LEFT,
                    false, "", "Consola", 12.5f);
            hBox.getChildren().add(field);
        });

        MenuItem moveUp = new MenuItem("Subir elemento");
        moveUp.setGraphic(imgMoveUp);
        moveUp.setOnAction(e -> {
            moveUpHBox(contenedor);
        });

        MenuItem moveDown = new MenuItem("Bajar elemento");
        moveDown.setGraphic(imgMoveDown);
        moveDown.setOnAction(e -> {
            moveDownHBox(contenedor);
        });

        MenuItem addImage = new MenuItem("Agregar imagen");
        addImage.setGraphic(imgImage);
        addImage.setOnAction(e -> {
            if (hBox.getChildren().isEmpty()) {

                int currentTotal = 0;

                for (int n = 0; n < contenedor.getChildren().size(); n++) {
                    HBox boxn = (HBox) contenedor.getChildren().get(n);
                    if (boxn.getChildren().size() == 1) {
                        if (boxn.getChildren().get(0) instanceof ImageViewTicket) {
                            currentTotal++;
                        }
                    }
                }

                String newCodigo = "";

                if (contenedor.equals(apEncabezado)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imc" + valor;
                } else if (contenedor.equals(apDetalleCabecera)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imd" + valor;
                } else if (contenedor.equals(apPie)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imp" + valor;
                }

                ImageViewTicket imageView = addElementImageView("/view/image/no-image.png", sheetWidth, 100, 86, true,
                        "image");
                imageView.setId(newCodigo);
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPrefHeight(imageView.getFitHeight());
                hBox.getChildren().add(imageView);
                double yPosBefero = 0;
                for (int p = 0; p < contenedor.getChildren().size(); p++) {
                    HBox hb = (HBox) contenedor.getChildren().get(p);
                    if (p == 0) {
                        double heightNow = hb.getPrefHeight();
                        hb.setLayoutY(p * heightNow);
                        yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                    } else {
                        hb.setLayoutY(yPosBefero);
                        yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                    }
                }
                contenedor.layout();
            }
        });

        MenuItem addQr = new MenuItem("Agregar QR");
        addQr.setGraphic(imgImage);
        addQr.setOnAction(e -> {
            if (hBox.getChildren().isEmpty()) {

                int currentTotal = 0;

                for (int n = 0; n < contenedor.getChildren().size(); n++) {
                    HBox boxn = (HBox) contenedor.getChildren().get(n);
                    if (boxn.getChildren().size() == 1) {
                        if (boxn.getChildren().get(0) instanceof ImageViewTicket) {
                            currentTotal++;
                        }
                    }
                }

                String newCodigo = "";

                if (contenedor.equals(apEncabezado)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imc" + valor;
                } else if (contenedor.equals(apDetalleCabecera)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imd" + valor;
                } else if (contenedor.equals(apPie)) {
                    int valor = currentTotal + 1;
                    newCodigo = "imp" + valor;
                }
                // try {
                // BufferedImage qrImage =
                // com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(new
                // com.google.zxing.qrcode.QRCodeWriter().encode("",com.google.zxing.BarcodeFormat.QR_CODE,
                // 300, 300));
                // } catch (WriterException ex) {
                // Logger.getLogger(FxTicketController.class.getName()).log(Level.SEVERE, null,
                // ex);
                // }
                ImageViewTicket imageView = addElementImageView("/view/image/qr.png", sheetWidth, 100, 86, true, "qr");
                imageView.setId(newCodigo);
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPrefHeight(imageView.getFitHeight());
                hBox.getChildren().add(imageView);
                double yPosBefero = 0;
                for (int p = 0; p < contenedor.getChildren().size(); p++) {
                    HBox hb = (HBox) contenedor.getChildren().get(p);
                    if (p == 0) {
                        double heightNow = hb.getPrefHeight();
                        hb.setLayoutY(p * heightNow);
                        yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                    } else {
                        hb.setLayoutY(yPosBefero);
                        yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                    }
                }
                contenedor.layout();
            }
        });

        contextMenu.getItems().addAll(text, textVariable, textMultilinea, textUnaLinea, textDosLineas, remove, moveUp,
                moveDown, addImage, addQr);

        hBox.setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(hBox, event.getSceneX(), event.getSceneY());
            short widthContent = 0;
            for (int i = 0; i < hBox.getChildren().size(); i++) {
                Object object = hBox.getChildren().get(i);
                if (object instanceof TextFieldTicket) {
                    widthContent += ((TextFieldTicket) hBox.getChildren().get(i)).getColumnWidth();
                } else if (object instanceof ImageViewTicket) {
                    widthContent += ((ImageViewTicket) hBox.getChildren().get(i)).getColumnWidth();
                }
            }
            text.setDisable((widthContent + 13) > sheetWidth);
            textUnaLinea.setDisable((widthContent + 13) > sheetWidth);
            textDosLineas.setDisable((widthContent + 13) > sheetWidth);
            addImage.setDisable(!hBox.getChildren().isEmpty());
            addQr.setDisable(!hBox.getChildren().isEmpty());
        });
        if (useLayout) {
            contenedor.getChildren().add(hBox);
        }
        return hBox;
    }

    public TextFieldTicket addElementTextField(String id, String titulo, boolean multilinea, short lines,
            short widthColumn, Pos align, boolean editable, String variable, String font, float size) {
        TextFieldTicket field = new TextFieldTicket(titulo, id);
        field.setMultilineas(multilinea);
        field.setLines(lines);
        field.setColumnWidth(widthColumn);
        field.setVariable(variable);
        field.setEditable(editable);
        field.setPreferredSize((double) widthColumn * pointWidth, 30);
        field.setAlignment(align);
        field.setFontColor(editable ? "black" : "#d62c0a");
        field.setFontName(font);
        field.setFontSize(size);
        field.setFontBackground("white");
        field.getStyleClass().add("text-field-ticket");
        field.setStyle("-fx-background-color:" + field.getFontBackground() + " ;-fx-text-fill:" + field.getFontColor()
                + ";-fx-font-family:"
                + (field.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                        : field.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                : field.getFontName().equalsIgnoreCase("Qwitcher Grypen") ? "Qwitcher Grypen"
                                        : field.getFontName().equalsIgnoreCase("Dancing Scrip") ? "Dancing Scrip"
                                                : "Roboto Bold")
                + ";-fx-font-size:" + field.getFontSize() + ";");
        field.addEventHandler(MouseEvent.MOUSE_PRESSED, m -> {
            tfAnterior = tfReference;
            if (tfAnterior != null) {
                tfAnterior
                        .setStyle(
                                "-fx-background-color: white;-fx-text-fill:" + tfAnterior.getFontColor()
                                        + ";-fx-font-family:"
                                        + (tfAnterior.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                                : tfAnterior.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                        : field.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                                ? "Qwitcher Grypen"
                                                                : field.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                                        ? "Dancing Scrip"
                                                                        : "Roboto Bold")
                                        + ";-fx-font-size:" + tfAnterior.getFontSize() + ";");
            }

            tfReference = field;

            tfReference.setFontBackground("#cecece");
            tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground() + ";-fx-text-fill:"
                    + tfReference.getFontColor() + ";-fx-font-family:"
                    + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                            : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                    : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen") ? "Qwitcher Grypen"
                                            : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                    ? "Dancing Scrip"
                                                    : "Roboto Bold")
                    + ";-fx-font-size:" + field.getFontSize() + ";");

            txtAnchoColumna.setText(tfReference.getColumnWidth() + "");
            cbAlignment.getItems().forEach(c -> {
                if (c.equals(tfReference.getAlignment())) {
                    cbAlignment.getSelectionModel().select(c);
                }
            });
            // HBox hBox = (HBox) tfReference.getParent();
            // cbFuente.setDisable(hBox.getChildren().size() > 1);
            cbFuente.getSelectionModel().select(tfReference.getFontName());
            // cbSize.setDisable(hBox.getChildren().size() > 1);
            cbSize.getSelectionModel().select(tfReference.getFontSize());
            cbMultilinea.setSelected(tfReference.isMultilineas());
            cbMultilinea.setText(tfReference.isMultilineas() ? "Si" : "No");
            cbEditable.setSelected(tfReference.isEditable());
            cbEditable.setText(tfReference.isEditable() ? "Si" : "No");
            txtVariable.setText(tfReference.getVariable());

        });

        field.lengthProperty()
                .addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!field.isMultilineas()) {
                        if (newValue.intValue() > oldValue.intValue()) {
                            if (field.getText().length() >= field.getColumnWidth()) {
                                field.setText(field.getText().substring(0, field.getColumnWidth()));
                            }
                        }
                    }
                });

        ImageView imgRemove = new ImageView(new Image("/view/image/remove-item.png"));
        imgRemove.setFitWidth(20);
        imgRemove.setFitHeight(20);

        ImageView imgAdaptParentWidth = new ImageView(new Image("/view/image/width-parent.png"));
        imgAdaptParentWidth.setFitWidth(20);
        imgAdaptParentWidth.setFitHeight(20);

        ImageView imgTextLeft = new ImageView(new Image("/view/image/text-left.png"));
        imgTextLeft.setFitWidth(20);
        imgTextLeft.setFitHeight(20);

        ImageView imgTextCenter = new ImageView(new Image("/view/image/text-center.png"));
        imgTextCenter.setFitWidth(20);
        imgTextCenter.setFitHeight(20);

        ImageView imgTextRight = new ImageView(new Image("/view/image/text-right.png"));
        imgTextRight.setFitWidth(20);
        imgTextRight.setFitHeight(20);

        ContextMenu contextMenu = new ContextMenu();

        MenuItem remove = new MenuItem("Remover Campo de Texto");
        remove.setGraphic(imgRemove);
        remove.setOnAction(e -> {
            ((HBox) field.getParent()).getChildren().remove(field);
        });
        MenuItem textAdaptWidth = new MenuItem("Adaptar el Ancho del Padre");
        textAdaptWidth.setGraphic(imgAdaptParentWidth);
        textAdaptWidth.setOnAction(e -> {
            field.setColumnWidth(sheetWidth);
            field.setPreferredSize(((double) field.getColumnWidth() * pointWidth), field.getPrefHeight());
        });

        MenuItem textLeft = new MenuItem("Alineación Izquierda");
        textLeft.setGraphic(imgTextLeft);
        textLeft.setOnAction(e -> {
            if (!field.getText().isEmpty()) {
                field.setAlignment(Pos.CENTER_LEFT);
            }
        });
        MenuItem textCenter = new MenuItem("Alineación Central");
        textCenter.setGraphic(imgTextCenter);
        textCenter.setOnAction(e -> {
            if (!field.getText().isEmpty()) {
                field.setAlignment(Pos.CENTER);
            }
        });
        MenuItem textRight = new MenuItem("Alineación Derecha");
        textRight.setGraphic(imgTextRight);
        textRight.setOnAction(e -> {
            if (!field.getText().isEmpty()) {
                field.setAlignment(Pos.CENTER_RIGHT);
            }
        });
        contextMenu.getItems().addAll(remove, textAdaptWidth, new SeparatorMenuItem(), textLeft, textCenter, textRight);
        field.setContextMenu(contextMenu);
        field.setOnContextMenuRequested((event) -> {
            if (((HBox) field.getParent()).getChildren().size() > 1) {
                textAdaptWidth.setDisable(true);
            }
            if (field.isMultilineas()) {
                textLeft.setDisable(true);
                textCenter.setDisable(true);
                textRight.setDisable(true);
            }
        });
        return field;
    }

    private ImageViewTicket addElementImageView(String path, short widthColumn, double width, double height,
            boolean newImage, String type) {
        ImageViewTicket imageView = new ImageViewTicket();
        imageView.setColumnWidth(widthColumn);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setSmooth(true);
        imageView.setType(type);
        if (newImage) {
            imageView.setImage(new Image(path));
            imageView.setUrl(Tools.getImageBytes(imageView.getImage(), Tools.getFileExtension(new File(path))));
        }
        return imageView;
    }

    private void actionAnchoColumnas() {
        if (tfReference != null) {
            if (Tools.isNumeric(txtAnchoColumna.getText())) {
                int widthContent = 0;
                for (int i = 0; i < ((HBox) tfReference.getParent()).getChildren().size(); i++) {
                    TextFieldTicket fieldTicket = ((TextFieldTicket) ((HBox) tfReference.getParent()).getChildren()
                            .get(i));
                    if (fieldTicket != tfReference) {
                        widthContent += fieldTicket.getColumnWidth();
                    }
                }
                if ((widthContent + Integer.parseInt(txtAnchoColumna.getText())) > sheetWidth) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket", "No puede sobrepasar al ancho de la hoja.");
                } else {
                    if (!tfReference.isMultilineas() && tfReference.isEditable()) {
                        if (tfReference.getText().length() < Integer.parseInt(txtAnchoColumna.getText())) {
                            tfReference.setColumnWidth(Short.parseShort(txtAnchoColumna.getText()));
                            tfReference.setPreferredSize(((double) tfReference.getColumnWidth() * pointWidth),
                                    tfReference.getPrefHeight());
                        } else {
                            tfReference.setColumnWidth(Short.parseShort(txtAnchoColumna.getText()));
                            tfReference.setPreferredSize(((double) tfReference.getColumnWidth() * pointWidth),
                                    tfReference.getPrefHeight());
                            tfReference.setText(tfReference.getText().substring(0, tfReference.getColumnWidth()));
                        }
                    } else {
                        if (tfReference.getText().length() < Integer.parseInt(txtAnchoColumna.getText())) {
                            tfReference.setColumnWidth(Short.parseShort(txtAnchoColumna.getText()));
                            tfReference.setPreferredSize(((double) tfReference.getColumnWidth() * pointWidth),
                                    tfReference.getPrefHeight());
                        } else {
                            tfReference.setColumnWidth(Short.parseShort(txtAnchoColumna.getText()));
                            tfReference.setPreferredSize(((double) tfReference.getColumnWidth() * pointWidth),
                                    tfReference.getPrefHeight());
                        }
                    }
                }
            }
        }
    }

    public void clearPane() {
        idTicket = 0;
        tipoTicket = 0;
        ruta = "";
        apEncabezado.getChildren().clear();
        apDetalleCabecera.getChildren().clear();
        apPie.getChildren().clear();
        txtAnchoColumna.setText("");
        cbAlignment.getSelectionModel().select(null);
        cbFuente.getSelectionModel().select(null);
        cbSize.getSelectionModel().select(null);
        cbMultilinea.setSelected(false);
        cbEditable.setSelected(false);
        txtVariable.setText("");
        lblNombre.setText("--");
        formatoTicket.setText("--");
        lblColumnas.setText("0");
        sheetWidth = 0;
        lblColumnas.setText(sheetWidth + "");
        cbPredeterminado.setSelected(false);
        cbPredeterminado.setText("NO");
        vbContenedor.setPrefWidth(Control.USE_COMPUTED_SIZE);
    }

    private Pos getAlignment(String align) {
        switch (align) {
            case "CENTER":
                return Pos.CENTER;
            case "CENTER_LEFT":
                return Pos.CENTER_LEFT;
            case "CENTER_RIGHT":
                return Pos.CENTER_RIGHT;
            default:
                return Pos.CENTER_LEFT;
        }
    }

    private void searchTicket() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_BUSQUEDA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketBusquedaController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            controller.loadComponents(0, true);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Seleccionar formato", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void nuevoTicket() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketProcesoController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Nuevo formato", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void editarTicket() {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_PROCESO);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketProcesoController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            controller.editarTicket(tipoTicket, lblNombre.getText(), sheetWidth);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Editar formato", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void windowTextMultilinea(HBox hBox) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_MULTILINEA);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketMultilineaController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            controller.setLoadComponent(hBox, sheetWidth);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar texto multilinea",
                    vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void windowTextVar(HBox hBox) {
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_VARIABLE);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketVariableController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            controller.setLoadComponent(hBox, sheetWidth);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Agregar variable", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    private void openWindowImpresora() {
        try {

            int valideteUno = 0;
            int valideteDos = 0;
            int valideteTree = 0;
            for (int i = 0; i < apEncabezado.getChildren().size(); i++) {
                HBox hBox = (HBox) apEncabezado.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteUno++;
                }
            }

            for (int i = 0; i < apDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = (HBox) apDetalleCabecera.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteDos++;
                }
            }

            for (int i = 0; i < apPie.getChildren().size(); i++) {
                HBox hBox = (HBox) apPie.getChildren().get(i);
                if (hBox.getChildren().isEmpty()) {
                    valideteTree++;
                }
            }

            if (valideteUno > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en la cabecera sin contenido");
            } else if (valideteDos > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en el detalle sin contenido");
            } else if (valideteTree > 0) {
                Tools.AlertMessageWarning(vbWindow, "Ticket", "Hay una fila en el pie sin contenido");
            } else {

                if (!apEncabezado.getChildren().isEmpty() && !apDetalleCabecera.getChildren().isEmpty()
                        && !apPie.getChildren().isEmpty()) {
                    fxPrincipalController.openFondoModal();
                    URL url = getClass().getResource(FilesRouters.FX_IMPRIMIR);
                    FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
                    Parent parent = fXMLLoader.load(url.openStream());
                    // Controlller here
                    FxImprimirController controller = fXMLLoader.getController();
                    controller.setInitTicketController(this);
                    //
                    Stage stage = WindowStage.StageLoaderModal(parent, "Imprimir Prueba",
                            vbWindow.getScene().getWindow());
                    stage.setResizable(false);
                    stage.sizeToScene();
                    stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
                    stage.show();
                } else {
                    if (apEncabezado.getChildren().isEmpty()) {
                        Tools.AlertMessageWarning(vbWindow, "Ticket", "El encabezado está vacío.");
                    } else if (apDetalleCabecera.getChildren().isEmpty()) {
                        Tools.AlertMessageWarning(vbWindow, "Ticket", "El detalle cabecera está vacío.");
                    } else if (apPie.getChildren().isEmpty()) {
                        Tools.AlertMessageWarning(vbWindow, "Ticket", "El pie está vacío.");
                    }
                }
            }
        } catch (IOException ex) {

        }
    }

    public void editarTicket(boolean editable, int tipoTicket, String nombre, short widthPage) {
        if (editable) {
            this.tipoTicket = tipoTicket;
            lblNombre.setText(nombre);
            sheetWidth = widthPage;
            lblColumnas.setText("" + sheetWidth);
            vbContenedor.setPrefWidth(sheetWidth * pointWidth);
            formatoTicket.setText(tipoTicket + "");

            for (short i = 0; i < apEncabezado.getChildren().size(); i++) {
                HBox hBox = (HBox) apEncabezado.getChildren().get(i);
                hBox.setPrefWidth(sheetWidth * pointWidth);

                short newwidth = (short) (sheetWidth / hBox.getChildren().size());

                for (short j = 0; j < hBox.getChildren().size(); j++) {
                    Object object = hBox.getChildren().get(j);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket fieldTicket = (TextFieldTicket) hBox.getChildren().get(j);
                        fieldTicket.setColumnWidth(newwidth);
                        fieldTicket.setPreferredSize(newwidth * pointWidth, fieldTicket.getPrefHeight());
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(j);
                        viewTicket.setColumnWidth(newwidth);
                    }
                }
            }

            for (short i = 0; i < apDetalleCabecera.getChildren().size(); i++) {
                HBox hBox = (HBox) apDetalleCabecera.getChildren().get(i);
                hBox.setPrefWidth(sheetWidth * pointWidth);

                short newwidth = (short) (sheetWidth / hBox.getChildren().size());
                for (short j = 0; j < hBox.getChildren().size(); j++) {
                    Object object = hBox.getChildren().get(j);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket fieldTicket = (TextFieldTicket) hBox.getChildren().get(j);
                        fieldTicket.setColumnWidth(newwidth);
                        fieldTicket.setPreferredSize(newwidth * pointWidth, fieldTicket.getPrefHeight());
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(j);
                        viewTicket.setColumnWidth(newwidth);
                    }
                }
            }

            for (short i = 0; i < apPie.getChildren().size(); i++) {
                HBox hBox = (HBox) apPie.getChildren().get(i);
                hBox.setPrefWidth(sheetWidth * pointWidth);

                short newwidth = (short) (sheetWidth / hBox.getChildren().size());
                for (short j = 0; j < hBox.getChildren().size(); j++) {
                    Object object = hBox.getChildren().get(j);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket fieldTicket = (TextFieldTicket) hBox.getChildren().get(j);
                        fieldTicket.setColumnWidth(newwidth);
                        fieldTicket.setPreferredSize(newwidth * pointWidth, fieldTicket.getPrefHeight());
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket viewTicket = (ImageViewTicket) hBox.getChildren().get(j);
                        viewTicket.setColumnWidth(newwidth);
                    }
                }
            }
        } else {
            clearPane();
            lblNombre.setText(nombre);
            sheetWidth = widthPage;
            lblColumnas.setText("" + sheetWidth);
            vbContenedor.setPrefWidth(sheetWidth * pointWidth);
            this.tipoTicket = tipoTicket;
            formatoTicket.setText(tipoTicket + "");

            cbPredeterminado.setSelected(false);
            cbPredeterminado.setText("NO");
        }
    }

    private void moveUpHBox(AnchorPane anchorPane) {
        for (int i = 0; i < anchorPane.getChildren().size(); i++) {

            if (((HBox) anchorPane.getChildren().get(i)).equals(hboxReference)) {
                if (i == 0) {
                    break;
                }

                String style = hboxReference.getStyle();

                HBox previous = (HBox) anchorPane.getChildren().get(i - 1);

                HBox oldHbox = addElement(anchorPane, previous.getId(), false);
                oldHbox.setLayoutY(previous.getLayoutY());
                oldHbox.setPrefHeight(hboxReference.getPrefHeight());

                for (int r = 0; r < hboxReference.getChildren().size(); r++) {
                    Object object = hboxReference.getChildren().get(r);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket tftAnterior = (TextFieldTicket) hboxReference.getChildren().get(r);
                        TextFieldTicket fieldTicket = addElementTextField(tftAnterior.getId(), tftAnterior.getText(),
                                tftAnterior.isMultilineas(), tftAnterior.getLines(), tftAnterior.getColumnWidth(),
                                tftAnterior.getAlignment(), tftAnterior.isEditable(), tftAnterior.getVariable(),
                                tftAnterior.getFontName(), tftAnterior.getFontSize());
                        oldHbox.getChildren().add(fieldTicket);
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket ivAnterior = (ImageViewTicket) hboxReference.getChildren().get(r);
                        ImageViewTicket imageTicket = addElementImageView("", ivAnterior.getColumnWidth(),
                                ivAnterior.getFitWidth(), ivAnterior.getFitHeight(), false, ivAnterior.getType());
                        imageTicket.setId(ivAnterior.getId());
                        imageTicket.setImage(new Image(new ByteArrayInputStream(ivAnterior.getUrl())));
                        imageTicket.setUrl(ivAnterior.getUrl());
                        oldHbox.setAlignment(hboxReference.getAlignment());
                        oldHbox.getChildren().add(imageTicket);
                    }
                }

                HBox newHbox = addElement(anchorPane, hboxReference.getId(), false);
                newHbox.setLayoutY(oldHbox.getLayoutY() + oldHbox.getPrefHeight());
                newHbox.setPrefHeight(previous.getPrefHeight());

                for (int a = 0; a < previous.getChildren().size(); a++) {
                    Object object = previous.getChildren().get(a);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket tftAnterior = (TextFieldTicket) previous.getChildren().get(a);
                        TextFieldTicket fieldTicket = addElementTextField(tftAnterior.getId(), tftAnterior.getText(),
                                tftAnterior.isMultilineas(), tftAnterior.getLines(), tftAnterior.getColumnWidth(),
                                tftAnterior.getAlignment(), tftAnterior.isEditable(), tftAnterior.getVariable(),
                                tftAnterior.getFontName(), tftAnterior.getFontSize());
                        newHbox.getChildren().add(fieldTicket);
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket ivAnterior = (ImageViewTicket) previous.getChildren().get(a);
                        ImageViewTicket imageTicket = addElementImageView("", ivAnterior.getColumnWidth(),
                                ivAnterior.getFitWidth(), ivAnterior.getFitHeight(), false, ivAnterior.getType());
                        imageTicket.setId(ivAnterior.getId());
                        imageTicket.setImage(new Image(new ByteArrayInputStream(ivAnterior.getUrl())));
                        imageTicket.setUrl(ivAnterior.getUrl());
                        newHbox.setAlignment(hboxReference.getAlignment());
                        newHbox.getChildren().add(imageTicket);
                    }
                }

                anchorPane.getChildren().set(i - 1, oldHbox);
                anchorPane.getChildren().set(i, newHbox);
                oldHbox.setStyle(style);
                hboxReference = oldHbox;
                break;

            }
        }

        anchorPane.layout();

    }

    private void moveDownHBox(AnchorPane anchorPane) {
        for (int i = 0; i < anchorPane.getChildren().size(); i++) {
            if (((HBox) anchorPane.getChildren().get(i)).equals(hboxReference)) {
                if (anchorPane.getChildren().size() == (i + 1)) {
                    break;
                }

                String style = hboxReference.getStyle();

                HBox later = (HBox) anchorPane.getChildren().get(i + 1);

                HBox newHbox = addElement(anchorPane, hboxReference.getId(), false);
                newHbox.setLayoutY(hboxReference.getLayoutY());
                newHbox.setPrefHeight(later.getPrefHeight());

                for (int a = 0; a < later.getChildren().size(); a++) {
                    Object object = later.getChildren().get(a);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket tftAnterior = (TextFieldTicket) later.getChildren().get(a);
                        TextFieldTicket fieldTicket = addElementTextField(tftAnterior.getId(), tftAnterior.getText(),
                                tftAnterior.isMultilineas(), tftAnterior.getLines(), tftAnterior.getColumnWidth(),
                                tftAnterior.getAlignment(), tftAnterior.isEditable(), tftAnterior.getVariable(),
                                tftAnterior.getFontName(), tftAnterior.getFontSize());
                        newHbox.getChildren().add(fieldTicket);
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket ivAnterior = (ImageViewTicket) later.getChildren().get(a);
                        ImageViewTicket imageTicket = addElementImageView("", ivAnterior.getColumnWidth(),
                                ivAnterior.getFitWidth(), ivAnterior.getFitHeight(), false, ivAnterior.getType());
                        imageTicket.setImage(new Image(new ByteArrayInputStream(ivAnterior.getUrl())));
                        imageTicket.setUrl(ivAnterior.getUrl());
                        newHbox.setAlignment(hboxReference.getAlignment());
                        newHbox.getChildren().add(imageTicket);
                    }
                }

                HBox oldHbox = addElement(anchorPane, later.getId(), false);
                oldHbox.setLayoutY(newHbox.getLayoutY() + newHbox.getPrefHeight());
                oldHbox.setPrefHeight(hboxReference.getPrefHeight());

                for (int r = 0; r < hboxReference.getChildren().size(); r++) {
                    Object object = hboxReference.getChildren().get(r);
                    if (object instanceof TextFieldTicket) {
                        TextFieldTicket tftAnterior = (TextFieldTicket) hboxReference.getChildren().get(r);
                        TextFieldTicket fieldTicket = addElementTextField(tftAnterior.getId(), tftAnterior.getText(),
                                tftAnterior.isMultilineas(), tftAnterior.getLines(), tftAnterior.getColumnWidth(),
                                tftAnterior.getAlignment(), tftAnterior.isEditable(), tftAnterior.getVariable(),
                                tftAnterior.getFontName(), tftAnterior.getFontSize());
                        oldHbox.getChildren().add(fieldTicket);
                    } else if (object instanceof ImageViewTicket) {
                        ImageViewTicket ivAnterior = (ImageViewTicket) hboxReference.getChildren().get(r);
                        ImageViewTicket imageTicket = addElementImageView("", ivAnterior.getColumnWidth(),
                                ivAnterior.getFitWidth(), ivAnterior.getFitHeight(), false, ivAnterior.getType());
                        imageTicket.setImage(new Image(new ByteArrayInputStream(ivAnterior.getUrl())));
                        imageTicket.setUrl(ivAnterior.getUrl());
                        oldHbox.setAlignment(hboxReference.getAlignment());
                        oldHbox.getChildren().add(imageTicket);
                    }
                }

                anchorPane.getChildren().set(i, newHbox);
                anchorPane.getChildren().set(i + 1, oldHbox);
                oldHbox.setStyle(style);
                hboxReference = oldHbox;
                break;

            }
        }
        anchorPane.layout();
    }

    public void leftElementHBox() {
        if (hboxReference != null) {
            hboxReference.setAlignment(Pos.CENTER_LEFT);
        }
    }

    private void centerElementHBox() {
        if (hboxReference != null) {
            hboxReference.setAlignment(Pos.CENTER);
        }
    }

    private void rightElementHBox() {
        if (hboxReference != null) {
            hboxReference.setAlignment(Pos.CENTER_RIGHT);
        }
    }

    private void loadSelectImage() {
        if (hboxReference != null) {
            if (hboxReference.getChildren().size() == 1) {
                Object object = hboxReference.getChildren().get(0);
                if (object instanceof ImageViewTicket) {
                    ImageViewTicket viewTicket = (ImageViewTicket) hboxReference.getChildren().get(0);
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Importar una imagen");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Elija una imagen", "*.png", "*.jpg", "*.jpeg", "*.gif"));
                    selectFile = fileChooser.showOpenDialog(vbWindow.getScene().getWindow());
                    if (selectFile != null) {
                        selectFile = new File(selectFile.getAbsolutePath());
                        if (selectFile.getName().endsWith("png") || selectFile.getName().endsWith("jpg")
                                || selectFile.getName().endsWith("jpeg") || selectFile.getName().endsWith("gif")) {
                            viewTicket.setImage(new Image(selectFile.toURI().toString()));
                            viewTicket.setSmooth(true);
                            viewTicket.setPreserveRatio(false);
                            viewTicket.setUrl(Tools.getImageBytes(selectFile));
                            // imageBytes = null;
                        } else {
                            Tools.AlertMessageWarning(vbWindow, "Mi Empresa",
                                    "No seleccionó un formato correcto de imagen.");
                        }
                    }
                }
            }
        }
    }

    private void onEventPredeterminado() {
        if (idTicket > 0) {
            short option = Tools.AlertMessageConfirmation(vbWindow, "Ticket",
                    "¿Está seguro de hacer prederteminado este modelo de ticket?");
            if (option == 1) {
                String result = TicketADO.ChangeDefaultState(idTicket, tipoTicket);
                if (result.equalsIgnoreCase("updated")) {
                    Tools.AlertMessageInformation(vbWindow, "Ticket", "Se realizó los cambios correctamente.");
                    cbPredeterminado.setSelected(true);
                    cbPredeterminado.setText("SI");
                    switch (tipoTicket) {
                        case 1:
                            Session.TICKET_VENTA_ID = idTicket;
                            Session.TICKET_VENTA_RUTA = ruta;
                            break;
                        case 2:
                            Session.TICKET_COMPRA_ID = idTicket;
                            Session.TICKET_COMPRA_RUTA = ruta;
                            break;
                        case 5:
                            Session.TICKET_CORTE_CAJA_ID = idTicket;
                            Session.TICKET_CORTE_CAJA_RUTA = ruta;
                            break;
                        case 7:
                            Session.TICKET_PRE_VENTA_ID = idTicket;
                            Session.TICKET_PRE_VENTA_RUTA = ruta;
                            break;
                        case 8:
                            Session.TICKET_COTIZACION_ID = idTicket;
                            Session.TICKET_COTIZACION_RUTA = ruta;
                            break;
                        case 9:
                            Session.TICKET_CUENTA_POR_COBRAR_ID = idTicket;
                            Session.TICKET_CUENTA_POR_COBRAR_RUTA = ruta;
                            break;
                        case 10:
                            Session.TICKET_CUENTA_POR_PAGAR_ID = idTicket;
                            Session.TICKET_CUENTA_POR_PAGAR_RUTA = ruta;
                            break;
                        case 11:
                            Session.TICKET_GUIA_REMISION_ID = idTicket;
                            Session.TICKET_GUIA_REMISION_RUTA = ruta;
                            break;
                        case 12:
                            Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID = idTicket;
                            Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA = ruta;
                            break;
                        case 13:
                            Session.TICKET_PEDIDO_ID = idTicket;
                            Session.TICKET_PEDIDO_RUTA = ruta;
                            break;
                        case 14:
                            Session.TICKET_ORDEN_COMPRA_ID = idTicket;
                            Session.TICKET_ORDEN_COMPRA_RUTA = ruta;
                            break;
                        case 15:
                            Session.TICKET_NOTA_CREDITO_ID = idTicket;
                            Session.TICKET_NOTA_CREDITO_RUTA = ruta;
                            break;
                        default:
                            break;
                    }
                } else {
                    Tools.AlertMessageError(vbWindow, "Ticket", result);
                }
            }
        } else {
            Tools.AlertMessageWarning(vbWindow, "Ticket",
                    "El modelo del ticket está en proceso de creación, busque en la lista para hacerlo prederteminado.");
        }

    }

    private void onEventEliminar() {
        if (idTicket != 0) {
            short value = Tools.AlertMessageConfirmation(vbWindow, "Ticket", "¿Está seguro de continuar?");
            if (value == 1) {
                String result = TicketADO.DeleteTicket(idTicket);
                if (result.equalsIgnoreCase("deleted")) {
                    Tools.AlertMessageConfirmation(vbWindow, "Ticket", "Se elemino correctamente el ticket.");
                    switch (tipoTicket) {
                        case 1:
                            Session.TICKET_VENTA_ID = 0;
                            Session.TICKET_VENTA_RUTA = "";
                            break;
                        case 2:
                            Session.TICKET_COMPRA_ID = 0;
                            Session.TICKET_COMPRA_RUTA = "";
                            break;
                        case 5:
                            Session.TICKET_CORTE_CAJA_ID = 0;
                            Session.TICKET_CORTE_CAJA_RUTA = "";
                            break;
                        case 7:
                            Session.TICKET_PRE_VENTA_ID = 0;
                            Session.TICKET_PRE_VENTA_RUTA = "";
                            break;
                        case 8:
                            Session.TICKET_COTIZACION_ID = 0;
                            Session.TICKET_COTIZACION_RUTA = "";
                            break;
                        case 9:
                            Session.TICKET_CUENTA_POR_COBRAR_ID = 0;
                            Session.TICKET_CUENTA_POR_COBRAR_RUTA = "";
                            break;
                        case 10:
                            Session.TICKET_CUENTA_POR_PAGAR_ID = 0;
                            Session.TICKET_CUENTA_POR_PAGAR_RUTA = "";
                            break;
                        case 11:
                            Session.TICKET_GUIA_REMISION_ID = 0;
                            Session.TICKET_GUIA_REMISION_RUTA = "";
                            break;
                        case 12:
                            Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_ID = 0;
                            Session.TICKET_HISTORIAL_SALIDA_PRODUCTOS_RUTA = "";
                            break;
                        case 13:
                            Session.TICKET_PEDIDO_ID = 0;
                            Session.TICKET_PEDIDO_RUTA = "";
                            break;
                        case 14:
                            Session.TICKET_ORDEN_COMPRA_ID = 0;
                            Session.TICKET_ORDEN_COMPRA_RUTA = "";
                            break;
                        case 15:
                            Session.TICKET_NOTA_CREDITO_ID = 0;
                            Session.TICKET_NOTA_CREDITO_RUTA = "";
                            break;
                        default:
                            break;
                    }
                    clearPane();
                } else if (result.equalsIgnoreCase("predeterminated")) {
                    Tools.AlertMessageWarning(vbWindow, "Ticket",
                            "El ticket es predeterminado, no puede eliminar por el momento.");
                } else {
                    Tools.AlertMessageError(vbWindow, "Ticket", result);
                }
            }
        }
    }

    private void onEventClonar() {
        if (idTicket == 0) {
            Tools.AlertMessageWarning(vbWindow, "Ticket", "No se puede clonar un ticket recien creado.");
            return;
        }
        try {
            fxPrincipalController.openFondoModal();
            URL url = getClass().getResource(FilesRouters.FX_TICKET_CLONAR);
            FXMLLoader fXMLLoader = WindowStage.LoaderWindow(url);
            Parent parent = fXMLLoader.load(url.openStream());
            // Controlller here
            FxTicketClonarController controller = fXMLLoader.getController();
            controller.setInitTicketController(this);
            //
            Stage stage = WindowStage.StageLoaderModal(parent, "Clonar formato", vbWindow.getScene().getWindow());
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setOnHiding(w -> fxPrincipalController.closeFondoModal());
            stage.setOnShowing(w -> controller.loadComponents(idTicket));
            stage.show();

        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    @FXML
    private void onKeyPressNuevo(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            nuevoTicket();
        }

    }

    @FXML
    private void onActionNuevo(ActionEvent event) {
        nuevoTicket();
    }

    @FXML
    private void onKeyPressEditar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            editarTicket();
        }
    }

    @FXML
    private void onActionEditar(ActionEvent event) {
        editarTicket();
    }

    @FXML
    private void onKeyPressedSave(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            saveTicket();
        }
    }

    @FXML
    private void onActionSave(ActionEvent event) {
        saveTicket();
    }

    @FXML
    private void onKeyPressedClonar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventClonar();
        }
    }

    @FXML
    private void onActionClonar(ActionEvent event) {
        onEventClonar();
    }

    @FXML
    private void onActionEliminar(ActionEvent event) {
        onEventEliminar();
    }

    @FXML
    private void onKeyPressedEliminar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventEliminar();
        }
    }

    @FXML
    private void onKeyPressedPrint(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowImpresora();
        }
    }

    @FXML
    private void onActionPrint(ActionEvent event) {
        openWindowImpresora();
    }

    @FXML
    private void onMouseClickedEncabezadoAdd(MouseEvent event) {
        generateElement(apEncabezado, "cb");
    }

    @FXML
    private void onMouseClickedDetalleCabeceraAdd(MouseEvent event) {
        generateElement(apDetalleCabecera, "dr");
    }

    @FXML
    private void onMouseClickedPieAdd(MouseEvent event) {
        generateElement(apPie, "cp");
    }

    @FXML
    private void onActionAnchoColumna(ActionEvent event) {
        actionAnchoColumnas();
    }

    @FXML
    private void onKeyTypedAnchoColumna(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onKeyPressedSearch(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            searchTicket();
        }
    }

    @FXML
    private void onActionSearch(ActionEvent event) {
        searchTicket();
    }

    @FXML
    private void onActionMultilinea(ActionEvent event) {
        if (tfReference != null) {
            if (!cbMultilinea.isSelected()) {
                tfReference.setMultilineas(false);
                tfReference.setLines((short) 0);
            } else {
                tfReference.setMultilineas(true);
                tfReference.setLines((short) 1);
            }
        }
    }

    @FXML
    private void onKeyPressedClear(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            clearPane();
        }
    }

    @FXML
    private void onActionClear(ActionEvent event) {
        clearPane();
    }

    @FXML
    private void onKeyPressedImage(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loadSelectImage();
        }
    }

    @FXML
    private void onActionImage(ActionEvent event) {
        loadSelectImage();
    }

    @FXML
    private void onKeyPressedMoveUp(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (hboxReference != null) {
                moveUpHBox((AnchorPane) hboxReference.getParent());
            }
        }
    }

    @FXML
    private void onActionMoveUp(ActionEvent event) {
        if (hboxReference != null) {
            moveUpHBox((AnchorPane) hboxReference.getParent());
        }
    }

    @FXML
    private void onKeyPressedMoveDown(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (hboxReference != null) {
                moveDownHBox((AnchorPane) hboxReference.getParent());
            }
        }
    }

    @FXML
    private void onActionMoveDown(ActionEvent event) {
        if (hboxReference != null) {
            moveDownHBox((AnchorPane) hboxReference.getParent());
        }
    }

    @FXML
    private void onKeyPressedLeft(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            leftElementHBox();
        }
    }

    @FXML
    private void onActionLeft(ActionEvent event) {
        leftElementHBox();
    }

    @FXML
    private void onKeyPressedCenter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            centerElementHBox();
        }
    }

    @FXML
    private void onActionCenter(ActionEvent event) {
        centerElementHBox();
    }

    @FXML
    private void onKeyPressedRight(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            rightElementHBox();
        }
    }

    @FXML
    private void onActionRight(ActionEvent event) {
        rightElementHBox();
    }

    @FXML
    private void onKeyPressedPredeterminado(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onEventPredeterminado();
        }
    }

    @FXML
    private void onActionPredeterminado(ActionEvent event) {
        onEventPredeterminado();
    }

    @FXML
    private void onActionFuente(ActionEvent event) {
        if (cbFuente.getSelectionModel().getSelectedIndex() >= 0) {
            if (tfReference != null) {
                switch (cbFuente.getSelectionModel().getSelectedIndex()) {
                    case 0:
                        tfReference.setFontName("Consola");
                        tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground()
                                + ";-fx-text-fill:" + tfReference.getFontColor() + ";-fx-font-family:"
                                + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                        : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                        ? "Qwitcher Grypen"
                                                        : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                                ? "Dancing Scrip"
                                                                : "Roboto Bold")
                                + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                        break;
                    case 1:
                        tfReference.setFontName("Roboto Regular");
                        tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground()
                                + ";-fx-text-fill:" + tfReference.getFontColor() + ";-fx-font-family:"
                                + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                        : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                        ? "Qwitcher Grypen"
                                                        : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                                ? "Dancing Scrip"
                                                                : "Roboto Bold")
                                + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                        break;
                    case 2:
                        tfReference.setFontName("Roboto Bold");
                        tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground()
                                + ";-fx-text-fill:" + tfReference.getFontColor() + ";-fx-font-family:"
                                + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                        : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                        ? "Qwitcher Grypen"
                                                        : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                                ? "Dancing Scrip"
                                                                : "Roboto Bold")
                                + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                        break;
                    case 3:
                        tfReference.setFontName("Qwitcher Grypen");
                        tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground()
                                + ";-fx-text-fill:" + tfReference.getFontColor() + ";-fx-font-family:"
                                + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                        : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                        ? "Qwitcher Grypen"
                                                        : "Roboto Bold")
                                + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                        break;

                    default:
                        tfReference.setFontName("Dancing Scrip");
                        tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground()
                                + ";-fx-text-fill:" + tfReference.getFontColor() + ";-fx-font-family:"
                                + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                        : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                                : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                        ? "Qwitcher Grypen"
                                                        : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                                ? "Dancing Scrip"
                                                                : "Roboto Bold")
                                + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                        break;
                }

                HBox hBox = (HBox) tfReference.getParent();
                for (int i = 0; i < hBox.getChildren().size(); i++) {
                    TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(i);
                    field.setFontName(tfReference.getFontName());
                    field.setStyle("-fx-background-color: " + field.getFontBackground() + ";-fx-text-fill:"
                            + field.getFontColor() + ";-fx-font-family:"
                            + (field.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                    : field.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                            : field.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                    ? "Qwitcher Grypen"
                                                    : field.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                            ? "Dancing Scrip"
                                                            : "Roboto Bold")
                            + ";-fx-font-size:" + field.getFontSize() + ";");
                }
            }
        }
    }

    @FXML
    private void onSize(ActionEvent event) {
        if (cbFuente.getSelectionModel().getSelectedIndex() >= 0) {
            if (tfReference != null) {
                tfReference.setFontSize(cbSize.getSelectionModel().getSelectedItem());
                tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground() + ";-fx-text-fill:"
                        + tfReference.getFontColor() + ";-fx-font-family:"
                        + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                        : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                ? "Qwitcher Grypen"
                                                : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                        ? "Dancing Scrip"
                                                        : "Roboto Bold")
                        + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                HBox hBox = (HBox) tfReference.getParent();
                for (int i = 0; i < hBox.getChildren().size(); i++) {
                    TextFieldTicket field = (TextFieldTicket) hBox.getChildren().get(i);
                    field.setFontSize(cbSize.getSelectionModel().getSelectedItem());
                    tfReference.setStyle("-fx-background-color: " + tfReference.getFontBackground() + ";-fx-text-fill:"
                            + tfReference.getFontColor() + ";-fx-font-family:"
                            + (tfReference.getFontName().equalsIgnoreCase("Consola") ? "Monospace"
                                    : tfReference.getFontName().equalsIgnoreCase("Roboto Regular") ? "Roboto"
                                            : tfReference.getFontName().equalsIgnoreCase("Qwitcher Grypen")
                                                    ? "Qwitcher Grypen"
                                                    : tfReference.getFontName().equalsIgnoreCase("Dancing Scrip")
                                                            ? "Dancing Scrip"
                                                            : "Roboto Bold")
                            + ";-fx-font-size:" + tfReference.getFontSize() + ";");
                }
            }
        }
    }

    @FXML
    private void onActionAncho(ActionEvent event) {
        if (hboxReference != null) {
            if (hboxReference.getChildren().size() == 1) {
                Object object = hboxReference.getChildren().get(0);
                if (object instanceof ImageViewTicket) {
                    ImageViewTicket viewTicket = (ImageViewTicket) hboxReference.getChildren().get(0);
                    double oldWidth = viewTicket.getFitWidth();
                    viewTicket.setFitWidth(Tools.isNumeric(txtAncho.getText().trim())
                            && Double.parseDouble(txtAncho.getText().trim()) > 0
                                    ? Double.parseDouble(txtAncho.getText())
                                    : oldWidth);
                }
            }
        }
    }

    @FXML
    private void onKeyTypedAncho(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    @FXML
    private void onActionAlto(ActionEvent event) {
        if (hboxReference != null) {
            if (hboxReference.getChildren().size() == 1) {
                Object object = hboxReference.getChildren().get(0);
                if (object instanceof ImageViewTicket) {
                    ImageViewTicket viewTicket = (ImageViewTicket) hboxReference.getChildren().get(0);
                    double oldHeight = viewTicket.getFitHeight();
                    viewTicket.setFitHeight(Tools.isNumeric(txtAlto.getText().trim())
                            && Double.parseDouble(txtAlto.getText().trim()) > 0 ? Double.parseDouble(txtAlto.getText())
                                    : oldHeight);
                    hboxReference.setPrefHeight(viewTicket.getFitHeight());
                    double yPosBefero = 0;
                    AnchorPane apDady = (AnchorPane) hboxReference.getParent();
                    for (int p = 0; p < apDady.getChildren().size(); p++) {
                        HBox hb = (HBox) apDady.getChildren().get(p);
                        if (p == 0) {
                            double heightNow = hb.getPrefHeight();
                            hb.setLayoutY(p * heightNow);
                            yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                        } else {
                            hb.setLayoutY(yPosBefero);
                            yPosBefero = hb.getLayoutY() + hb.getPrefHeight();
                        }
                    }
                    apDady.layout();
                }
            }
        }
    }

    @FXML
    private void onKeyTypedAlto(KeyEvent event) {
        char c = event.getCharacter().charAt(0);
        if ((c < '0' || c > '9') && (c != '\b')) {
            event.consume();
        }
    }

    public AnchorPane getHbEncabezado() {
        return apEncabezado;
    }

    public AnchorPane getHbDetalleCabecera() {
        return apDetalleCabecera;
    }

    public AnchorPane getHbPie() {
        return apPie;
    }

    public short getSheetWidth() {
        return sheetWidth;
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
