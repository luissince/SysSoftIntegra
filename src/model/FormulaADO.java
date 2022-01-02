package model;

import controller.tools.SearchComboBox;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

public class FormulaADO {

    public static Object ListarFormulas(int opcion, String fechaInicio, String fechaFinal, String buscar, int posicionPagina, int filasPorPagina) {
        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];

            ObservableList<FormulaTB> formulaTBs = FXCollections.observableArrayList();
            statementLista = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Formulas(?,?,?,?,?,?)}");
            statementLista.setInt(1, opcion);
            statementLista.setString(2, fechaInicio);
            statementLista.setString(3, fechaFinal);
            statementLista.setString(4, buscar);
            statementLista.setInt(5, posicionPagina);
            statementLista.setInt(6, filasPorPagina);
            resultSet = statementLista.executeQuery();
            while (resultSet.next()) {
                FormulaTB formulaTB = new FormulaTB();
                formulaTB.setId(resultSet.getRow() + posicionPagina);
                formulaTB.setIdFormula(resultSet.getString("IdFormula"));
                formulaTB.setTitulo(resultSet.getString("Titulo"));
                formulaTB.setCantidad(resultSet.getDouble("Cantidad"));
                formulaTB.setInstrucciones(resultSet.getString("Instrucciones"));
                formulaTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                formulaTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setClave(resultSet.getString("Clave"));
                suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                formulaTB.setSuministroTB(suministroTB);

                formulaTBs.add(formulaTB);
            }

            statementLista = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Formulas_Count(?,?,?,?)}");
            statementLista.setInt(1, opcion);
            statementLista.setString(2, fechaInicio);
            statementLista.setString(3, fechaFinal);
            statementLista.setString(4, buscar);
            resultSet = statementLista.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSet.next()) {
                cantidadTotal = resultSet.getInt("Total");
            }

            objects[0] = formulaTBs;
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String Crud_Formula(FormulaTB formulaTB) {
        CallableStatement statementCodigo = null;
        PreparedStatement statementValidate = null;
        PreparedStatement statementFormula = null;
        PreparedStatement statementDetalle = null;
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM FormulaTB WHERE IdFormula = ?");
            statementValidate.setString(1, formulaTB.getIdFormula());
            if (statementValidate.executeQuery().next()) {
                statementFormula = DBUtil.getConnection().prepareStatement("UPDATE FormulaTB SET Titulo = ?,Cantidad = ?,IdSuministro = ?,CostoAdicional = ?,Instrucciones = ?,IdUsuario = ? WHERE IdFormula = ?");
                statementFormula.setString(1, formulaTB.getTitulo());
                statementFormula.setDouble(2, formulaTB.getCantidad());
                statementFormula.setString(3, formulaTB.getIdSuministro());
                statementFormula.setDouble(4, formulaTB.getCostoAdicional());
                statementFormula.setString(5, formulaTB.getInstrucciones());
                statementFormula.setString(6, formulaTB.getIdUsuario());
                statementFormula.setString(7, formulaTB.getIdFormula());
                statementFormula.addBatch();
                statementFormula.executeBatch();

                statementDetalle = DBUtil.getConnection().prepareStatement("DELETE FROM FormulaDetalleTB WHERE IdFormula = ?");
                statementDetalle.setString(1, formulaTB.getIdFormula());
                statementDetalle.addBatch();
                statementDetalle.executeBatch();

                statementDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO FormulaDetalleTB(IdFormula,IdSuministro,Cantidad) VALUES(?,?,?)");
                for (SuministroTB suministroTB : formulaTB.getSuministroTBs()) {
                    statementDetalle.setString(1, formulaTB.getIdFormula());
                    statementDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                    statementDetalle.setDouble(3, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                    statementDetalle.addBatch();
                }
                statementDetalle.executeBatch();

                DBUtil.getConnection().commit();
                return "updated";
            } else {

                statementCodigo = DBUtil.getConnection().prepareCall("{? = call Fc_Formula_Alfanumerico()}");
                statementCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                statementCodigo.execute();
                String id_Formula = statementCodigo.getString(1);

                statementFormula = DBUtil.getConnection().prepareStatement("INSERT INTO FormulaTB(IdFormula,Titulo,Cantidad,IdSuministro,CostoAdicional,Instrucciones,Fecha,Hora,IdUsuario) VALUES(?,?,?,?,?,?,?,?,?)");
                statementFormula.setString(1, id_Formula);
                statementFormula.setString(2, formulaTB.getTitulo());
                statementFormula.setDouble(3, formulaTB.getCantidad());
                statementFormula.setString(4, formulaTB.getIdSuministro());
                statementFormula.setDouble(5, formulaTB.getCostoAdicional());
                statementFormula.setString(6, formulaTB.getInstrucciones());
                statementFormula.setString(7, formulaTB.getFecha());
                statementFormula.setString(8, formulaTB.getHora());
                statementFormula.setString(9, formulaTB.getIdUsuario());
                statementFormula.addBatch();

                statementDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO FormulaDetalleTB(IdFormula,IdSuministro,Cantidad) VALUES(?,?,?)");
                for (SuministroTB suministroTB : formulaTB.getSuministroTBs()) {
                    statementDetalle.setString(1, id_Formula);
                    statementDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                    statementDetalle.setDouble(3, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                    statementDetalle.addBatch();
                }

                statementFormula.executeBatch();
                statementDetalle.executeBatch();
                DBUtil.getConnection().commit();
                return "inserted";
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementFormula != null) {
                    statementFormula.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Formula_ById_Editar(String idFormula) {
        PreparedStatement statementFormula = null;
        PreparedStatement statementDetalle = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            statementFormula = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Formula_ById(?)}");
            statementFormula.setString(1, idFormula);
            resultSet = statementFormula.executeQuery();
            if (resultSet.next()) {
                FormulaTB formulaTB = new FormulaTB();
                formulaTB.setIdFormula(resultSet.getString("IdFormula"));
                formulaTB.setTitulo(resultSet.getString("Titulo"));
                formulaTB.setCantidad(resultSet.getDouble("Cantidad"));
                formulaTB.setIdSuministro(resultSet.getString("IdSuministro"));
                formulaTB.setSuministroTB(new SuministroTB(resultSet.getString("IdSuministro"), resultSet.getString("Clave"), resultSet.getString("NombreMarca")));
                formulaTB.setCostoAdicional(resultSet.getDouble("CostoAdicional"));
                formulaTB.setInstrucciones(resultSet.getString("Instrucciones"));
                formulaTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                formulaTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                statementDetalle = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Formula_ById(?)}");
                statementDetalle.setString(1, idFormula);
                resultSet = statementDetalle.executeQuery();
                ArrayList<SuministroTB> suministroTBs = new ArrayList();
                while (resultSet.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setCantidad(resultSet.getDouble("Cantidad"));

                    ComboBox<SuministroTB> comboBox = new ComboBox();
                    comboBox.setPromptText("-- Selecionar --");
                    comboBox.setPrefWidth(220);
                    comboBox.setPrefHeight(30);
                    comboBox.setMaxWidth(Double.MAX_VALUE);
                    suministroTB.setCbSuministro(comboBox);

                    SearchComboBox<SuministroTB> searchComboBox = new SearchComboBox<>(suministroTB.getCbSuministro(), false);
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
                        if (t.getCode() == KeyCode.ENTER) {
                            if (!searchComboBox.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                                searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                                searchComboBox.getSearchComboBoxSkin().getItemView().requestFocus();
                            }
                        } else if (t.getCode() == KeyCode.ESCAPE) {
                            searchComboBox.getComboBox().hide();
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
                        if (!Tools.isText(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText())) {
                            searchComboBox.getComboBox().getItems().clear();
                            List<SuministroTB> list = SuministroADO.getSearchComboBoxSuministros(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim(), false);
                            list.forEach(p -> searchComboBox.getComboBox().getItems().add(p));
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
                        switch (t.getCode()) {
                            case ENTER:
                            case SPACE:
                            case ESCAPE:
                                searchComboBox.getComboBox().hide();
                                break;
                            case UP:
                            case DOWN:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                searchComboBox.getSearchComboBoxSkin().getSearchBox().requestFocus();
                                searchComboBox.getSearchComboBoxSkin().getSearchBox().selectAll();
                                break;
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
                        if (item != null) {
                            searchComboBox.getComboBox().getSelectionModel().select(item);
                            if (searchComboBox.getSearchComboBoxSkin().isClickSelection()) {
                                searchComboBox.getComboBox().hide();
                            }
                        }
                    });
                    suministroTB.setSearchComboBoxSuministro(searchComboBox);
                    suministroTB.getCbSuministro().getItems().add(new SuministroTB(resultSet.getString("IdSuministro"), resultSet.getString("Clave"), resultSet.getString("NombreMarca")));
                    suministroTB.getCbSuministro().getSelectionModel().select(0);

                    TextField textField = new TextField(Tools.roundingValue(suministroTB.getCantidad(), 2));
                    textField.setPromptText("0.00");
                    textField.getStyleClass().add("text-field-normal");
                    textField.setPrefWidth(220);
                    textField.setPrefHeight(30);
                    textField.setOnKeyTyped(event -> {
                        char c = event.getCharacter().charAt(0);
                        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                            event.consume();
                        }
                        if (c == '.' && textField.getText().contains(".")) {
                            event.consume();
                        }
                    });
                    suministroTB.setTxtCantidad(textField);

                    Button button = new Button();
                    button.getStyleClass().add("buttonLightError");
                    button.setAlignment(Pos.CENTER);
                    button.setPrefWidth(Control.USE_COMPUTED_SIZE);
                    button.setPrefHeight(Control.USE_COMPUTED_SIZE);
//                    button.setMaxWidth(Double.MAX_VALUE);
//                    button.setMaxHeight(Double.MAX_VALUE);
                    ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
                    imageView.setFitWidth(20);
                    imageView.setFitHeight(20);
                    button.setGraphic(imageView);
                    suministroTB.setBtnRemove(button);
                    suministroTBs.add(suministroTB);
                }
                formulaTB.setSuministroTBs(suministroTBs);
                return formulaTB;
            } else {
                throw new Exception("No se puedo obtener los datos de la formula.");
            }
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementFormula != null) {
                    statementFormula.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Formula_ById(String idFormula) {
        PreparedStatement statementFormula = null;
        PreparedStatement statementDetalle = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            statementFormula = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Formula_ById(?)}");
            statementFormula.setString(1, idFormula);
            resultSet = statementFormula.executeQuery();
            if (resultSet.next()) {
                FormulaTB formulaTB = new FormulaTB();
                formulaTB.setIdFormula(resultSet.getString("IdFormula"));
                formulaTB.setTitulo(resultSet.getString("Titulo"));
                formulaTB.setCantidad(resultSet.getDouble("Cantidad"));
                formulaTB.setIdSuministro(resultSet.getString("IdSuministro"));
                SuministroTB stb = new SuministroTB();
                stb.setIdSuministro(resultSet.getString("IdSuministro"));
                stb.setClave(resultSet.getString("Clave"));
                stb.setNombreMarca(resultSet.getString("NombreMarca"));
                stb.setUnidadCompraName(resultSet.getString("Medida"));
                formulaTB.setSuministroTB(stb);
                formulaTB.setCostoAdicional(resultSet.getDouble("CostoAdicional"));
                formulaTB.setInstrucciones(resultSet.getString("Instrucciones"));
                formulaTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                formulaTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(resultSet.getString("Apellidos"));
                empleadoTB.setNombres(resultSet.getString("Nombres"));
                formulaTB.setEmpleadoTB(empleadoTB);

                statementDetalle = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Formula_ById(?)}");
                statementDetalle.setString(1, idFormula);
                resultSet = statementDetalle.executeQuery();
                ArrayList<SuministroTB> suministroTBs = new ArrayList();
                while (resultSet.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(resultSet.getRow());
                    suministroTB.setIdSuministro(resultSet.getString("IdSuministro"));
                    suministroTB.setClave(resultSet.getString("Clave"));
                    suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                    suministroTB.setCantidad(resultSet.getDouble("Cantidad"));
                    suministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                    suministroTBs.add(suministroTB);
                }
                formulaTB.setSuministroTBs(suministroTBs);
                return formulaTB;
            } else {
                throw new Exception("No se puedo obtener los datos de la formula.");
            }
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementFormula != null) {
                    statementFormula.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Formula_ByIdProducto(String idProducto) {
        PreparedStatement statementFormula = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            statementFormula = DBUtil.getConnection().prepareStatement("SELECT IdFormula,Titulo FROM FormulaTB WHERE IdSuministro = ?");
            statementFormula.setString(1, idProducto);
            resultSet = statementFormula.executeQuery();
            List<FormulaTB> formulaTBs = new ArrayList<>();
            while (resultSet.next()) {
                FormulaTB formulaTB = new FormulaTB();
                formulaTB.setIdFormula(resultSet.getString("IdFormula"));
                formulaTB.setTitulo(resultSet.getString("Titulo"));
                formulaTBs.add(formulaTB);
            }
            return formulaTBs;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementFormula != null) {
                    statementFormula.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String Eliminar_Formula_ById(String idFormula) {
        PreparedStatement statementValidar = null;
        PreparedStatement statementFormula = null;
        PreparedStatement statementDetalle = null;
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM FormulaTB WHERE IdFormula = ?");
            statementValidar.setString(1, idFormula);
            if (statementValidar.executeQuery().next()) {
                statementFormula = DBUtil.getConnection().prepareStatement("DELETE FROM FormulaTB WHERE IdFormula = ?");
                statementFormula.setString(1, idFormula);
                statementFormula.addBatch();
                statementFormula.executeBatch();

                statementDetalle = DBUtil.getConnection().prepareStatement("DELETE FROM FormulaDetalleTB WHERE IdFormula = ?");
                statementDetalle.setString(1, idFormula);
                statementDetalle.addBatch();
                statementDetalle.executeBatch();

                DBUtil.getConnection().commit();
                return "deleted";
            } else {
                DBUtil.getConnection().rollback();
                return "noid";
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementFormula != null) {
                    statementFormula.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static List<FormulaTB> getSearchComboBoxFormulas() {
        String selectStmt = "SELECT f.IdFormula,f.Titulo,s.IdSuministro,s.Clave,s.NombreMarca FROM FormulaTB AS f INNER JOIN SuministroTB AS s ON f.IdSuministro = s.IdSuministro";
        PreparedStatement preparedStatement = null;
        List<FormulaTB> formulaTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
//            preparedStatement.setString(1, idSuministro);
//            preparedStatement.setString(2, buscar);
            try (ResultSet rsEmps = preparedStatement.executeQuery()) {
                while (rsEmps.next()) {
                    FormulaTB formulaTB = new FormulaTB();
                    formulaTB.setIdFormula(rsEmps.getString("IdFormula"));
                    formulaTB.setTitulo(rsEmps.getString("Titulo"));

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                    suministroTB.setClave(rsEmps.getString("Clave"));
                    suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    formulaTB.setSuministroTB(suministroTB);

                    formulaTBs.add(formulaTB);
                }
            }
        } catch (SQLException e) {

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {

            }
        }
        return formulaTBs;
    }

}
