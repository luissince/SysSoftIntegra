package controller.configuracion.miempresa;

import controller.menus.FxPrincipalController;
import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import model.DetalleTB;
import model.EmpresaTB;
import model.UbigeoTB;
import service.DetalleADO;
import service.EmpresaADO;
import service.UbigeoADO;

public class FxMiEmpresaController implements Initializable {

    @FXML
    private ScrollPane window;
    @FXML
    private Label lblLoad;
    @FXML
    private ComboBox<DetalleTB> cbGiroComercial;
    @FXML
    private TextField txtRepresentante;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtCelular;
    @FXML
    private TextField txtPaginasWeb;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtDomicilio;
    @FXML
    private ComboBox<DetalleTB> cbTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtRazonSocial;
    @FXML
    private TextField txtNombreComercial;
    @FXML
    private ImageView lnPrincipal;
    @FXML
    private ComboBox<UbigeoTB> cbUbigeo;
    @FXML
    private TextField txtTerminos;
    @FXML
    private TextField txtCondiciones;

    private FxPrincipalController fxPrincipalController;

    private boolean validate;

    private byte[] imageBytes;

    private int idEmpresa;

    private File selectFile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadMiEmpresa();
        loadUbigeo();
    }

    private void loadMiEmpresa() {

        ExecutorService exec = Executors.newCachedThreadPool((Runnable runnable) -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        });

        Task<ArrayList<Object>> task = new Task<ArrayList<Object>>() {
            @Override
            public ArrayList<Object> call() {
                ArrayList<Object> objects = new ArrayList<>();
                objects.add(DetalleADO.Get_Detail_IdName("3", "0011", ""));
                objects.add(DetalleADO.Get_Detail_IdName("3", "0003", ""));
                objects.add(EmpresaADO.GetEmpresa());
                return objects;
            }
        };
        task.setOnSucceeded(t -> {
            ArrayList<Object> objects = task.getValue();
            if (!objects.isEmpty()) {
                if (objects.get(0) != null) {
                    ObservableList<DetalleTB> gc = (ObservableList<DetalleTB>) objects.get(0);
                    gc.forEach(e -> {
                        cbGiroComercial.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
                    });
                }
                if (objects.get(1) != null) {
                    ObservableList<DetalleTB> tp = (ObservableList<DetalleTB>) objects.get(1);
                    tp.forEach(e -> {
                        cbTipoDocumento.getItems().add(new DetalleTB(e.getIdDetalle(), e.getNombre()));
                    });
                }
                if (objects.get(2) != null) {
                    EmpresaTB empresaTb = (EmpresaTB) objects.get(2);
                    if (empresaTb != null) {
                        validate = true;
                        idEmpresa = empresaTb.getIdEmpresa();

                        ObservableList<DetalleTB> lsgiro = cbGiroComercial.getItems();
                        if (empresaTb.getGiroComerial() != 0) {
                            for (int i = 0; i < lsgiro.size(); i++) {
                                if (empresaTb.getGiroComerial() == lsgiro.get(i).getIdDetalle()) {
                                    cbGiroComercial.getSelectionModel().select(i);
                                    break;
                                }
                            }
                        }

                        txtRepresentante.setText(empresaTb.getNombre());
                        txtTelefono.setText(empresaTb.getTelefono());
                        txtCelular.setText(empresaTb.getCelular());
                        txtPaginasWeb.setText(empresaTb.getPaginaWeb());
                        txtEmail.setText(empresaTb.getEmail());
                        txtDomicilio.setText(empresaTb.getDomicilio());
                        txtTerminos.setText(empresaTb.getTerminos());
                        txtCondiciones.setText(empresaTb.getCondiciones());

                        ObservableList<DetalleTB> lsdoc = cbTipoDocumento.getItems();
                        if (empresaTb.getTipoDocumento() != 0) {
                            for (int i = 0; i < lsdoc.size(); i++) {
                                if (empresaTb.getTipoDocumento() == lsdoc.get(i).getIdDetalle()) {
                                    cbTipoDocumento.getSelectionModel().select(i);
                                    break;
                                }
                            }
                        }

                        txtNumeroDocumento.setText(empresaTb.getNumeroDocumento());
                        txtRazonSocial.setText(empresaTb.getRazonSocial());
                        txtNombreComercial.setText(empresaTb.getNombreComercial());

                        if (empresaTb.getImage() != null) {
                            imageBytes = empresaTb.getImage();
                            lnPrincipal.setImage(new Image(new ByteArrayInputStream(empresaTb.getImage())));
                        } else {
                            imageBytes = null;
                            lnPrincipal.setImage(new Image("/view/image/no-image.png"));
                        }

                        cbUbigeo.getItems().clear();
                        if (empresaTb.getUbigeoTB().getIdUbigeo() > 0) {
                            cbUbigeo.getItems().add(empresaTb.getUbigeoTB());
                            if (!cbUbigeo.getItems().isEmpty()) {
                                cbUbigeo.getSelectionModel().select(0);
                            }
                        }

                    } else {
                        validate = false;
                    }
                }
                lblLoad.setVisible(false);
            } else {
                lblLoad.setVisible(false);
            }
        });
        task.setOnFailed(e -> {
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

    private void loadUbigeo() {
        SearchComboBox<UbigeoTB> searchComboBoxUbigeoLlegada = new SearchComboBox<>(cbUbigeo, false);
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                if (!searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                    searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().requestFocus();
                }
            } else if (t.getCode() == KeyCode.ESCAPE) {
                searchComboBoxUbigeoLlegada.getComboBox().hide();
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
            searchComboBoxUbigeoLlegada.getComboBox().getItems().clear();
            searchComboBoxUbigeoLlegada.getComboBox().getItems().addAll(UbigeoADO.GetSearchComboBoxUbigeo(searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().getText().trim()));
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
            if (null == t.getCode()) {
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
            } else {
                switch (t.getCode()) {
                    case ENTER:
                    case SPACE:
                    case ESCAPE:
                        searchComboBoxUbigeoLlegada.getComboBox().hide();
                        break;
                    case UP:
                    case DOWN:
                    case LEFT:
                    case RIGHT:
                        break;
                    default:
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().requestFocus();
                        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getSearchBox().selectAll();
                        break;
                }
            }
        });
        searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
            if (item != null) {
                searchComboBoxUbigeoLlegada.getComboBox().getSelectionModel().select(item);
                if (searchComboBoxUbigeoLlegada.getSearchComboBoxSkin().isClickSelection()) {
                    searchComboBoxUbigeoLlegada.getComboBox().hide();
                }
            }
        });
    }

    private void openWindowFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar una imagen");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Elija una imagen", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        selectFile = fileChooser.showOpenDialog(window.getScene().getWindow());
        if (selectFile != null) {
            selectFile = new File(selectFile.getAbsolutePath());
            if (selectFile.getName().endsWith("png") || selectFile.getName().endsWith("jpg") || selectFile.getName().endsWith("jpeg") || selectFile.getName().endsWith("gif")) {
                Image image = new Image(selectFile.toURI().toString(), 200, 200, false, true);
                lnPrincipal.setSmooth(true);
                lnPrincipal.setPreserveRatio(false);
                lnPrincipal.setImage(image);
                imageBytes = null;
            } else {
                Tools.AlertMessageWarning(window, "Mi Empresa", "No seleccionó un formato correcto de imagen.");
            }
        }
    }

    private void clearImage() {
        lnPrincipal.setImage(new Image("/view/image/no-image.png"));
        selectFile = null;
        imageBytes = null;
    }

    private void aValidityProcess() {
        if (cbGiroComercial.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Seleccione el giro comercial, por favor.");
            cbGiroComercial.requestFocus();
        } else if (txtRepresentante.getText().equalsIgnoreCase("")) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Ingrese el nombre del representante, por favor.");
            txtRepresentante.requestFocus();
        } else if (txtDomicilio.getText().isEmpty()) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Ingrese la dirección fiscal de la empresa, por favor.");
            txtDomicilio.requestFocus();
        } else if (cbTipoDocumento.getSelectionModel().getSelectedIndex() < 0) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Seleccione el tipo de documento, por favor.");
            cbTipoDocumento.requestFocus();
        } else if (txtNumeroDocumento.getText().isEmpty()) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Ingrese el número del documento, por favor.");
            txtNumeroDocumento.requestFocus();
        } else if (txtRazonSocial.getText().isEmpty()) {
            Tools.AlertMessageWarning(window, "Mi Empresa", "Ingrese la razón social, por favor.");
            txtRazonSocial.requestFocus();
        } else {
            short confirmation = Tools.AlertMessageConfirmation(window, "Mi Empresa", "¿Esta seguro de continuar?");
            if (confirmation == 1) {
                EmpresaTB empresaTB = new EmpresaTB();
                empresaTB.setIdEmpresa(validate == true ? idEmpresa : 0);
                empresaTB.setGiroComerial(cbGiroComercial.getSelectionModel().getSelectedItem().getIdDetalle());
                empresaTB.setNombre(txtRepresentante.getText().trim());
                empresaTB.setTelefono(txtTelefono.getText().trim().isEmpty() ? "" : txtTelefono.getText().trim());
                empresaTB.setCelular(txtCelular.getText().trim().isEmpty() ? "" : txtCelular.getText().trim());
                empresaTB.setPaginaWeb(txtPaginasWeb.getText().trim());
                empresaTB.setEmail(txtEmail.getText().trim());
                empresaTB.setDomicilio(txtDomicilio.getText().trim());
                empresaTB.setTipoDocumento(cbTipoDocumento.getSelectionModel().getSelectedIndex() >= 0
                        ? cbTipoDocumento.getSelectionModel().getSelectedItem().getIdDetalle()
                        : 0);
                empresaTB.setTerminos(txtTerminos.getText().trim());
                empresaTB.setCondiciones(txtCondiciones.getText().trim());
                empresaTB.setNumeroDocumento(txtNumeroDocumento.getText().trim().isEmpty() ? "" : txtNumeroDocumento.getText().trim());
                empresaTB.setRazonSocial(txtRazonSocial.getText().trim().isEmpty() ? txtRepresentante.getText().trim() : txtRazonSocial.getText().trim());
                empresaTB.setNombreComercial(txtNombreComercial.getText().trim());

                empresaTB.setImage(
                        imageBytes != null ? imageBytes
                                : selectFile == null
                                        ? null
                                        : Tools.getImageBytes(selectFile)
                );
                empresaTB.setIdUbigeo(cbUbigeo.getSelectionModel().getSelectedIndex() >= 0
                        ? cbUbigeo.getSelectionModel().getSelectedItem().getIdUbigeo() : 0);

                String result = EmpresaADO.CrudEntity(empresaTB);
                switch (result) {
                    case "registered":
                        Tools.AlertMessageInformation(window, "Mi Empresa", "Registrado correctamente.");
                        Session.COMPANY_REPRESENTANTE = txtRepresentante.getText();
                        Session.COMPANY_RAZON_SOCIAL = txtRazonSocial.getText();
                        Session.COMPANY_NOMBRE_COMERCIAL = txtNombreComercial.getText();
                        Session.COMPANY_NUMERO_DOCUMENTO = txtNumeroDocumento.getText();
                        Session.COMPANY_TELEFONO = txtTelefono.getText();
                        Session.COMPANY_CELULAR = txtCelular.getText();
                        Session.COMPANY_PAGINAWEB = txtPaginasWeb.getText();
                        Session.COMPANY_EMAIL = txtEmail.getText();
                        Session.COMPANY_DOMICILIO = txtDomicilio.getText();
                        Session.COMPANY_IMAGE = imageBytes != null ? imageBytes : selectFile == null
                                ? null
                                : Tools.getImageBytes(selectFile);
                        break;
                    case "updated":
                        Tools.AlertMessageInformation(window, "Mi Empresa", "Actualizado correctamente.");
                        Session.COMPANY_REPRESENTANTE = txtRepresentante.getText();
                        Session.COMPANY_RAZON_SOCIAL = txtRazonSocial.getText();
                        Session.COMPANY_NOMBRE_COMERCIAL = txtNombreComercial.getText();
                        Session.COMPANY_NUMERO_DOCUMENTO = txtNumeroDocumento.getText();
                        Session.COMPANY_TELEFONO = txtTelefono.getText();
                        Session.COMPANY_CELULAR = txtCelular.getText();
                        Session.COMPANY_PAGINAWEB = txtPaginasWeb.getText();
                        Session.COMPANY_EMAIL = txtEmail.getText();
                        Session.COMPANY_DOMICILIO = txtDomicilio.getText();
                        Session.COMPANY_IMAGE = imageBytes != null ? imageBytes : selectFile == null
                                ? null
                                : Tools.getImageBytes(selectFile);
                        break;
                    default:
                        Tools.AlertMessageError(window, "Mi Empresa", result);
                        break;
                }
            }

        }
    }

    @FXML
    private void onActionToRegister(ActionEvent event) {
        aValidityProcess();
    }

    @FXML
    private void onKeyPressedToRegister(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            aValidityProcess();
        }
    }

    @FXML
    private void onKeyPressedPhoto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            openWindowFile();
        }
    }

    @FXML
    private void onActionPhoto(ActionEvent event) {
        openWindowFile();

    }

    @FXML
    private void onKeyPressedRemovePhoto(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            clearImage();
        }
    }

    @FXML
    private void onKeyPressedToRecargar(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (!lblLoad.isVisible()) {
                loadMiEmpresa();
            }
        }
    }

    @FXML
    private void onActionToRecargar(ActionEvent event) {
        if (!lblLoad.isVisible()) {
            loadMiEmpresa();
        }

    }

    @FXML
    private void onActionRemovePhoto(ActionEvent event) {
        clearImage();
    }

    public void setContent(FxPrincipalController fxPrincipalController) {
        this.fxPrincipalController = fxPrincipalController;
    }

}
