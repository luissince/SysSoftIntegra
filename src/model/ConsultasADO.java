package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class ConsultasADO {

    public static Callable<Long[]> TotalObjectInit() {
        Callable<Long[]> callable = () -> {
            PreparedStatement preparedStatement = null;
            Long tipos[] = new Long[4];
            tipos[0] = (long) 0;
            tipos[1] = (long) 0;
            tipos[2] = (long) 0;
            tipos[3] = (long) 0;
            try {
                DBUtil.dbConnect();
                preparedStatement = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ArticuloTB");
                ResultSet setArticulo = preparedStatement.executeQuery();
                if (setArticulo.next()) {
                    tipos[0] = setArticulo.getLong("Total");
                }
                preparedStatement = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ClienteTB");
                ResultSet setClientes = preparedStatement.executeQuery();
                if (setClientes.next()) {
                    tipos[1] = setClientes.getLong("Total");
                }
                preparedStatement = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM ProveedorTB");
                ResultSet setProveedores = preparedStatement.executeQuery();
                if (setProveedores.next()) {
                    tipos[2] = setProveedores.getLong("Total");
                }
                preparedStatement = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) as Total FROM EmpleadoTB");
                ResultSet setEmpleados = preparedStatement.executeQuery();
                if (setEmpleados.next()) {
                    tipos[3] = setEmpleados.getLong("Total");
                }
            } catch (SQLException exception) {
                System.out.println(exception);
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException closeExeption) {
                    System.out.println(closeExeption);
                }
            }
            return tipos;
        };
        return callable;
    }
}
