package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TipoMovimientoADO {

    public static ObservableList<TipoMovimientoTB> Get_list_Tipo_Movimiento(boolean ajuste, boolean all) {
        String data = "SELECT IdTipoMovimiento,Nombre,Predeterminado,Sistema,Ajuste FROM TipoMovimientoTB";
        String selectStmt = all ? data
                : data + " WHERE Ajuste = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<TipoMovimientoTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            if (!all) {
                preparedStatement.setBoolean(1, ajuste);
            }
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                TipoMovimientoTB movimientoTB = new TipoMovimientoTB();
                movimientoTB.setIdTipoMovimiento(resultSet.getInt("IdTipoMovimiento"));
                movimientoTB.setNombre(resultSet.getString("Nombre"));
                movimientoTB.setPredeterminado(resultSet.getBoolean("Predeterminado"));
                movimientoTB.setSistema(resultSet.getBoolean("Sistema"));
                movimientoTB.setAjuste(resultSet.getBoolean("Ajuste"));
                empList.add(movimientoTB);
            }

        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

}
