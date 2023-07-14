package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import controller.tools.Tools;

public class ConsultasADO {

    public static Callable<Long[]> TotalObjectInit() {
        DBUtil dbf = new DBUtil();
        Callable<Long[]> callable = () -> {
            PreparedStatement preparedStatement = null;
            Long tipos[] = new Long[4];
            tipos[0] = (long) 0;
            tipos[1] = (long) 0;
            tipos[2] = (long) 0;
            tipos[3] = (long) 0;
            try {
                dbf.dbConnect();
                preparedStatement = dbf.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ArticuloTB");
                ResultSet setArticulo = preparedStatement.executeQuery();
                if (setArticulo.next()) {
                    tipos[0] = setArticulo.getLong("Total");
                }
                preparedStatement = dbf.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ClienteTB");
                ResultSet setClientes = preparedStatement.executeQuery();
                if (setClientes.next()) {
                    tipos[1] = setClientes.getLong("Total");
                }
                preparedStatement = dbf.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ProveedorTB");
                ResultSet setProveedores = preparedStatement.executeQuery();
                if (setProveedores.next()) {
                    tipos[2] = setProveedores.getLong("Total");
                }
                preparedStatement = dbf.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM EmpleadoTB");
                ResultSet setEmpleados = preparedStatement.executeQuery();
                if (setEmpleados.next()) {
                    tipos[3] = setEmpleados.getLong("Total");
                }
            } catch (SQLException | ClassNotFoundException exception) {
                System.out.println(exception);
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    dbf.dbDisconnect();
                } catch (SQLException closeExeption) {
                    System.out.println(closeExeption);
                }
            }
            return tipos;
        };
        return callable;
    }

    public static Object reporteIngresosEgresos(int opcion, String fechaInicio, String fechaFinal, String idEmpleado) {
        DBUtil dbf = new DBUtil();

        PreparedStatement preparedStatement = null;
        try {
            dbf.dbConnect();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Reporte_General_Ingresos_Egresos(?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, fechaInicio);
            preparedStatement.setString(3, fechaFinal);
            preparedStatement.setString(4, idEmpleado);
            ResultSet resultSet = preparedStatement.executeQuery();

            JSONArray array = new JSONArray();
            double total = 0;
            double total_ingreso = 0;
            double total_egreso = 0;
            while (resultSet.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("Id", resultSet.getRow());
                jsono.put("Codigo", resultSet.getString("Codigo"));
                jsono.put("Nombre", resultSet.getString("Nombre"));
                jsono.put("Cantidad", Tools.roundingValue(resultSet.getInt("Cantidad"), 0));
                jsono.put("Ingreso", Tools.roundingValue(resultSet.getDouble("Ingreso"), 2));
                jsono.put("Egreso", Tools.roundingValue(resultSet.getDouble("Egreso"), 2));
                array.add(jsono);

                total += resultSet.getDouble("Ingreso") - resultSet.getDouble("Egreso");
                total_ingreso += resultSet.getDouble("Ingreso");
                total_egreso += resultSet.getDouble("Egreso");
            }
            return new Object[]{array, total_ingreso, total_egreso, total};
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
}
