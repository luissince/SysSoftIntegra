package service;

import controller.tools.Session;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.MonedaTB;

public class MonedaADO {

    public static String CrudMoneda(MonedaTB monedaTB) {
        DBUtil dbf = new DBUtil();
        String result = null;

        PreparedStatement statementValidate = null;
        PreparedStatement statementMoneda = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT IdMoneda FROM MonedaTB WHERE IdMoneda = ?");
            statementValidate.setInt(1, monedaTB.getIdMoneda());
            if (statementValidate.executeQuery().next()) {
                statementValidate = dbf.getConnection()
                        .prepareStatement("SELECT Nombre FROM MonedaTB WHERE IdMoneda <> ? AND Nombre = ?");
                statementValidate.setInt(1, monedaTB.getIdMoneda());
                statementValidate.setString(2, monedaTB.getNombre());
                if (statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "duplicated";
                } else {
                    statementMoneda = dbf.getConnection().prepareStatement(
                            "UPDATE MonedaTB SET Nombre=?,Abreviado=?,Simbolo=?,TipoCambio=?,Predeterminado=? WHERE IdMoneda = ?");
                    statementMoneda.setString(1, monedaTB.getNombre());
                    statementMoneda.setString(2, monedaTB.getAbreviado());
                    statementMoneda.setString(3, monedaTB.getSimbolo());
                    statementMoneda.setDouble(4, monedaTB.getTipoCambio());
                    statementMoneda.setBoolean(5, monedaTB.isPredeterminado());
                    statementMoneda.setInt(6, monedaTB.getIdMoneda());
                    statementMoneda.addBatch();
                    statementMoneda.executeBatch();
                    dbf.getConnection().commit();
                    result = "updated";
                }
            } else {
                statementValidate = dbf.getConnection()
                        .prepareStatement("SELECT Nombre FROM MonedaTB WHERE Nombre = ?");
                statementValidate.setString(1, monedaTB.getNombre());
                if (statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "duplicated";
                } else {
                    statementMoneda = dbf.getConnection().prepareStatement(
                            "INSERT INTO MonedaTB(Nombre,Abreviado,Simbolo,TipoCambio,Predeterminado,Sistema) VALUES(?,?,?,?,?,?)");
                    statementMoneda.setString(1, monedaTB.getNombre());
                    statementMoneda.setString(2, monedaTB.getAbreviado());
                    statementMoneda.setString(3, monedaTB.getSimbolo());
                    statementMoneda.setDouble(4, monedaTB.getTipoCambio());
                    statementMoneda.setBoolean(5, monedaTB.isPredeterminado());
                    statementMoneda.setBoolean(6, monedaTB.getSistema());
                    statementMoneda.addBatch();
                    statementMoneda.executeBatch();
                    dbf.getConnection().commit();
                    result = "inserted";
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException e) {
                result = e.getLocalizedMessage();
            }
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementMoneda != null) {
                    statementMoneda.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static Object ListMonedas(int opcion, String buscar, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementList = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<MonedaTB> observableList = FXCollections.observableArrayList();

            statementList = dbf.getConnection().prepareStatement("{call Sp_Listar_Monedas(?,?,?,?)}");
            statementList.setInt(1, opcion);
            statementList.setString(2, buscar);
            statementList.setInt(3, posicionPagina);
            statementList.setInt(4, filasPorPagina);
            resultSet = statementList.executeQuery();
            while (resultSet.next()) {
                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setId(resultSet.getRow() + posicionPagina);
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("Nombre"));
                monedaTB.setAbreviado(resultSet.getString("Abreviado"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                monedaTB.setTipoCambio(resultSet.getDouble("TipoCambio"));
                monedaTB.setPredeterminado(resultSet.getBoolean("Predeterminado"));
                monedaTB.setImagePredeterminado(resultSet.getBoolean("Predeterminado")
                        ? new ImageView(new Image("/view/image/bandera.png", 22, 22, false, false))
                        : new ImageView(new Image("/view/image/unchecked.png", 22, 22, false, false)));
                observableList.add(monedaTB);
            }
            objects[0] = observableList;

            statementList = dbf.getConnection().prepareStatement("{call Sp_Listar_Monedas_Count(?,?)}");
            statementList.setInt(1, opcion);
            statementList.setString(2, buscar);
            resultSet = statementList.executeQuery();
            Integer total = 0;
            if (resultSet.next()) {
                total = resultSet.getInt("Total");
            }
            objects[1] = total;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementList != null) {
                    statementList.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String ChangeDefaultState(boolean state, int id) {
        DBUtil dbf = new DBUtil();
        String result = null;

        PreparedStatement statementSelect = null;
        PreparedStatement statementUpdate = null;
        PreparedStatement statementState = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementSelect = dbf.getConnection()
                    .prepareStatement("SELECT Predeterminado FROM MonedaTB WHERE Predeterminado = 1");
            if (statementSelect.executeQuery().next()) {
                statementUpdate = dbf.getConnection()
                        .prepareStatement("UPDATE MonedaTB SET Predeterminado = 0 WHERE Predeterminado = 1");
                statementUpdate.addBatch();

                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE MonedaTB SET Predeterminado = ? WHERE IdMoneda = ?");
                statementState.setBoolean(1, state);
                statementState.setInt(2, id);
                statementState.addBatch();

                statementUpdate.executeBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            } else {
                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE MonedaTB SET Predeterminado = ? WHERE IdMoneda = ?");
                statementState.setBoolean(1, state);
                statementState.setInt(2, id);
                statementState.addBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException e) {
                result = e.getLocalizedMessage();
            }

        } finally {
            try {
                if (statementSelect != null) {
                    statementSelect.close();
                }
                if (statementUpdate != null) {
                    statementUpdate.close();
                }
                if (statementState != null) {
                    statementState.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static String RemoveElement(int idMoneda) {
        DBUtil dbf = new DBUtil();
        String result = null;

        PreparedStatement statementValidate = null;
        PreparedStatement statementRemove = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM MonedaTB WHERE IdMoneda = ? AND Predeterminado = 1");
            statementValidate.setInt(1, idMoneda);
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                result = "predetermined";
            } else {
                statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE Moneda = ?");
                statementValidate.setInt(1, idMoneda);
                if (statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "venta";
                } else {
                    statementValidate = dbf.getConnection()
                            .prepareStatement("SELECT * FROM CompraTB WHERE TipoMoneda = ?");
                    statementValidate.setInt(1, idMoneda);
                    if (statementValidate.executeQuery().next()) {
                        dbf.getConnection().rollback();
                        result = "compra";
                    } else {
                        statementRemove = dbf.getConnection()
                                .prepareStatement("DELETE FROM MonedaTB WHERE IdMoneda = ?");
                        statementRemove.setInt(1, idMoneda);
                        statementRemove.addBatch();
                        statementRemove.executeBatch();
                        dbf.getConnection().commit();
                        result = "removed";
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException e) {
                result = e.getLocalizedMessage();
            }
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementRemove != null) {
                    statementRemove.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static void GetMonedaPredetermined() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statement = null;
        try {
            dbf.dbConnect();
            statement = dbf.getConnection()
                    .prepareStatement("SELECT IdMoneda,Nombre,Simbolo FROM MonedaTB WHERE Predeterminado = 1");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Session.MONEDA_ID = resultSet.getInt("IdMoneda");
                Session.MONEDA_NOMBRE = resultSet.getString("Nombre");
                Session.MONEDA_SIMBOLO = resultSet.getString("Simbolo");
            } else {
                Session.MONEDA_ID = 0;
                Session.MONEDA_NOMBRE = "MONEDA";
                Session.MONEDA_SIMBOLO = "M";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Error Moneda: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
                System.out.println("Error Moneda: " + e.getLocalizedMessage());
            }
        }
    }

    public static ArrayList<MonedaTB> GetMonedasComboBox() {
        DBUtil dbf = new DBUtil();
        ArrayList<MonedaTB> list = new ArrayList<>();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            statement = dbf.getConnection().prepareStatement(
                    "SELECT IdMoneda,Nombre,Simbolo,Abreviado,Predeterminado,TipoCambio FROM MonedaTB");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("Nombre"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                monedaTB.setAbreviado(resultSet.getString("Abreviado"));
                monedaTB.setPredeterminado(resultSet.getBoolean("Predeterminado"));
                monedaTB.setTipoCambio(resultSet.getDouble("TipoCambio"));
                list.add(monedaTB);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Error Moneda: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                System.out.println("Error Moneda: " + ex.getLocalizedMessage());
            }
        }
        return list;
    }

}
