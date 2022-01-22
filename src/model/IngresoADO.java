package model;

import controller.tools.Tools;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class IngresoADO {

    public static Object GetResumenIngresos(String fechaInicio, String fechaFinal, int usuario, String idUusuario) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            DBUtil.dbConnect();
            ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();

            preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Reporte_Ingresos(?,?,?,?)}");
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

    public static ArrayList<IngresoTB> GetListaIngresos(String fechaInicio, String fechaFinal, int usuario, String idUusuario) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Reporte_Ingresos_Lista(?,?,?,?)}");
            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFinal);
            preparedStatement.setInt(3, usuario);
            preparedStatement.setString(4, idUusuario);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                IngresoTB ingresoTB = new IngresoTB();
                ingresoTB.setId(resultSet.getRow());
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                empleadoTB.setApellidos(resultSet.getString("Apellidos"));
                empleadoTB.setNombres(resultSet.getString("Nombres"));
                ingresoTB.setEmpleadoTB(empleadoTB);
                ingresoTB.setTransaccion(resultSet.getString("Transaccion"));
                ingresoTB.setDetalle(resultSet.getString("Detalle"));
                ingresoTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                ingresoTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                ingresoTB.setFormaIngreso(resultSet.getString("FormaIngreso"));
                ingresoTB.setMonto(resultSet.getDouble("Monto"));
                ingresoTBs.add(ingresoTB);
            }
        } catch (SQLException ex) {
            Tools.println("Error Ingresos ADO: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {
            }
        }
        return ingresoTBs;
    }

}
