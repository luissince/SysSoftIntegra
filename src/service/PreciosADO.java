package service;

import controller.tools.Tools;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import model.PreciosTB;

public class PreciosADO {

    public static String CrudPrecio(PreciosTB preciosTB) {
        DBUtil dbf = new DBUtil();
        String result = "";
        
        PreparedStatement preparedValidation = null;
        PreparedStatement statementPrecio = null;
        PreparedStatement statementBusqueda = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidation = dbf.getConnection().prepareStatement("SELECT * FROM PreciosTB WHERE IdPrecios = ?");
            preparedValidation.setInt(1, preciosTB.getIdPrecios());
            if (preparedValidation.executeQuery().next()) {
                statementPrecio = dbf.getConnection().prepareStatement("UPDATE PreciosTB SET Nombre=?,Valor=? WHERE IdPrecios = ?");
                statementPrecio.setString(1, preciosTB.getTxtNombre().getText());
                statementPrecio.setDouble(2, Double.parseDouble(preciosTB.getTxtValor().getText()));
                statementPrecio.setInt(3, preciosTB.getIdPrecios());
                statementPrecio.addBatch();
                statementPrecio.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            } else {

                statementBusqueda = dbf.getConnection().prepareStatement("SELECT IdArticulo FROM ArticuloTB WHERE IdSuministro = ?");
                statementBusqueda.setString(1, preciosTB.getIdSuministro());
                if (statementBusqueda.executeQuery().next()) {
                    ResultSet resultSet = statementBusqueda.executeQuery();
                    if (resultSet.next()) {
                        preciosTB.setIdArticulo(resultSet.getString("IdArticulo"));
                    } else {
                        preciosTB.setIdArticulo("");
                    }
                }

                statementPrecio = dbf.getConnection().prepareStatement("INSERT INTO PreciosTB(IdArticulo,IdSuministro,Nombre,Valor,Estado)VALUES(?,?,?,?,?)");
                statementPrecio.setString(1, preciosTB.getIdArticulo());
                statementPrecio.setString(2, preciosTB.getIdSuministro());
                statementPrecio.setString(3, preciosTB.getTxtNombre().getText());
                statementPrecio.setDouble(4, Double.parseDouble(preciosTB.getTxtValor().getText()));
                statementPrecio.setBoolean(5, true);
                statementPrecio.addBatch();
                statementPrecio.executeBatch();
                dbf.getConnection().commit();
                result = "registered";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementBusqueda != null) {
                    statementBusqueda.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                if (statementPrecio != null) {
                    statementPrecio.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static String Removed_Precio(int idPrecio) {
        DBUtil dbf = new DBUtil();
        String result = "";
       
        PreparedStatement statementPrecio = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementPrecio = dbf.getConnection().prepareStatement("DELETE FROM PreciosTB WHERE IdPrecios = ?");
            statementPrecio.setInt(1, idPrecio);
            statementPrecio.addBatch();
            statementPrecio.executeBatch();
            dbf.getConnection().commit();
            result = "removed";

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementPrecio != null) {
                    statementPrecio.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static ObservableList<PreciosTB> Get_Lista_Precios_By_IdSuministro(String idSuministro) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<PreciosTB> list = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("SELECT * FROM PreciosTB WHERE IdSuministro = ?");
            preparedStatement.setString(1, idSuministro);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                PreciosTB preciosTB = new PreciosTB();
                preciosTB.setIdPrecios(rsEmps.getInt("IdPrecios"));
                preciosTB.setNombre(rsEmps.getString("Nombre"));
                preciosTB.setValor(rsEmps.getDouble("Valor"));
                preciosTB.setFactor(rsEmps.getDouble("Factor"));

                TextField tfNombre = new TextField(rsEmps.getString("Nombre"));
                tfNombre.getStyleClass().add("text-field-normal");
                tfNombre.setOnKeyReleased(event -> {
                    preciosTB.setNombre(tfNombre.getText());
                });

                TextField tfValor = new TextField(Tools.roundingValue(rsEmps.getDouble("Valor"), 2));
                tfValor.getStyleClass().add("text-field-normal");
                tfValor.setOnKeyReleased(event -> {
                    preciosTB.setValor(!Tools.isNumeric(tfValor.getText()) ? 0 : Double.parseDouble(tfValor.getText()));
                });
                tfValor.setOnKeyTyped((KeyEvent event) -> {
                    char c = event.getCharacter().charAt(0);
                    if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                        event.consume();
                    }
                    if (c == '.' && tfValor.getText().contains(".")) {
                        event.consume();
                    }
                });

                TextField tfFactor = new TextField(Tools.roundingValue(rsEmps.getDouble("Factor"), 2));
                tfFactor.getStyleClass().add("text-field-normal");
                tfFactor.setOnKeyReleased(event -> {
                    preciosTB.setFactor(!Tools.isNumeric(tfFactor.getText()) ? 1 : Double.parseDouble(tfFactor.getText()));
                });
                tfFactor.setOnKeyTyped((KeyEvent event) -> {
                    char c = event.getCharacter().charAt(0);
                    if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                        event.consume();
                    }
                    if (c == '.' && tfFactor.getText().contains(".")) {
                        event.consume();
                    }
                });

                Button button = new Button();
                button.getStyleClass().add("buttonDark");
                ImageView view = new ImageView(new Image("/view/image/remove.png"));
                view.setFitWidth(22);
                view.setFitHeight(22);
                button.setGraphic(view);

                preciosTB.setTxtNombre(tfNombre);
                preciosTB.setTxtValor(tfValor);
                preciosTB.setTxtFactor(tfFactor);
                preciosTB.setBtnOpcion(button);
                list.add(preciosTB);
            }
        } catch (SQLException | ClassNotFoundException ex) {

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return list;
    }

}
