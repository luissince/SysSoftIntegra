package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

public class LoteADO {

    public static String UpdateLote(LoteTB loteTB) {
        String selectStmt = "UPDATE LoteTB set ExistenciaActual = ?,FechaCaducidad = ? WHERE IdLote = ?";
        PreparedStatement preparedStatement = null;
        String result = "";
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setDouble(1, loteTB.getExistenciaActual());
            preparedStatement.setDate(2, Date.valueOf(loteTB.getFechaCaducidad()));
            preparedStatement.setLong(3, loteTB.getIdLote());
            preparedStatement.addBatch();

            preparedStatement.executeBatch();
            DBUtil.getConnection().commit();
            result = "updated";

        } catch (SQLException e) {
            try {
                result = "Error en LoteADO:" + e.getLocalizedMessage();
                DBUtil.getConnection().rollback();
            } catch (SQLException ex) {
                result = "Error en LoteADO:" + ex.getLocalizedMessage();
            }
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = "Error en LoteADO:" + ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static ObservableList<LoteTB> ListLote(short opcion, String value) {
        String selectStmt = "{call Sp_Listar_Lote(?,?)}";
        CallableStatement callableStatement = null;
        ResultSet rsEmps = null;
        ObservableList<LoteTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            callableStatement = DBUtil.getConnection().prepareCall(selectStmt);
            callableStatement.setShort(1, opcion);
            callableStatement.setString(2, value);

            rsEmps = callableStatement.executeQuery();

            while (rsEmps.next()) {
                LoteTB loteTB = new LoteTB();
                loteTB.setId(rsEmps.getInt("Filas"));
                loteTB.setIdLote(rsEmps.getLong("IdLote"));
                loteTB.setNumeroLote(rsEmps.getString("NumeroLote"));
//                loteTB.setArticuloTB(new ArticuloTB(rsEmps.getString("Clave"), rsEmps.getString("NombreMarca")));
                loteTB.setFechaCaducidad(rsEmps.getDate("FechaCaducidad").toLocalDate());
                loteTB.setExistenciaInicial(rsEmps.getDouble("ExistenciaInicial"));
                loteTB.setExistenciaActual(rsEmps.getDouble("ExistenciaActual"));
                Label label = new Label("");
                if (loteTB.getFechaCaducidad().isBefore(LocalDate.parse(Tools.getDate())) || loteTB.getFechaCaducidad().equals(LocalDate.parse(Tools.getDate()))) {
                    label.setText("CADUCADO");
                    label.getStyleClass().add("label-proceso");
                } else if (LocalDate.now().isBefore(loteTB.getFechaCaducidad()) && ChronoUnit.DAYS.between(LocalDate.now(), loteTB.getFechaCaducidad()) <= 15) {
                    label.setText("POR CADUCAR");
                    label.getStyleClass().add("label-medio");
                } else {
                    label.setText("VIGENTE");
                    label.getStyleClass().add("label-asignacion");
                }
                loteTB.setLblEstado(label);

                empList.add(loteTB);
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);

        } finally {
            try {
                if (callableStatement != null) {
                    callableStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

    public static String GetTotalCaducados() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String result = "";
        try {
            DBUtil.dbConnect();
            statement = DBUtil.getConnection().prepareStatement("SELECT COUNT(FechaCaducidad) AS Caducados FROM LoteTB WHERE FechaCaducidad <= CAST(GETDATE() AS DATE)");
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("Caducados");
            }
        } catch (SQLException ex) {
            result = "Error en LoteADO:" + ex.getLocalizedMessage();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = "Error en LoteADO:" + ex.getLocalizedMessage();
            }

        }
        return result;
    }

    public static String GetTotalPorCaducar() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String result = "";
        try {
            DBUtil.dbConnect();
            statement = DBUtil.getConnection().prepareStatement("SELECT COUNT(FechaCaducidad) AS PorCaducar FROM LoteTB WHERE GETDATE() <= FechaCaducidad and DATEDIFF(day, GETDATE(), FechaCaducidad)<=15 ");
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("PorCaducar");
            }
        } catch (SQLException ex) {
            result = "Error en LoteADO:" + ex.getLocalizedMessage();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = "Error en LoteADO:" + ex.getLocalizedMessage();
            }

        }
        return result;
    }

}
