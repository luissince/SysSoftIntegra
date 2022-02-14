package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class IngresoADO {

    public static Object ReporteGeneralIngresosEgresos(String fechaInicio, String fechaFinal, int usuario, String idUusuario) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            DBUtil.dbConnect();
            ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();

            preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Reporte_General_Ingresos_Egresos(?,?,?,?)}");
            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFinal);
            preparedStatement.setInt(3, usuario);
            preparedStatement.setString(4, idUusuario);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                IngresoTB ingresoTB = new IngresoTB();
                ingresoTB.setId(resultSet.getRow());
                ingresoTB.setTransaccion(resultSet.getString("Transaccion"));
                ingresoTB.setCantidad(resultSet.getInt("Cantidad"));
                ingresoTB.setFormaIngreso(resultSet.getString("FormaIngreso"));
                ingresoTB.setEfectivo(resultSet.getDouble("Efectivo"));
                ingresoTB.setTarjeta(resultSet.getDouble("Tarjeta"));
                ingresoTB.setDeposito(resultSet.getDouble("Deposito"));
                ingresoTBs.add(ingresoTB);
            }

            return ingresoTBs;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
