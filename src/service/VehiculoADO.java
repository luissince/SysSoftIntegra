package service;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.VehiculoTB;

public class VehiculoADO {

    public static String CrudVehiculo(VehiculoTB vehiculoTB) {
        DBUtil dbf = new DBUtil();

        CallableStatement codigoVehiculo = null;
        PreparedStatement preparedVehiculo = null;
        PreparedStatement preparedValidar = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidar = dbf.getConnection().prepareStatement("SELECT * FROM VehiculoTB WHERE NumeroPlaca = ?");
            preparedValidar.setString(1, vehiculoTB.getNumeroPlaca());
            if (preparedValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "duplicate";
            }

            codigoVehiculo = dbf.getConnection().prepareCall("{? = call Fc_Vehiculo_Codigo_Alfanumerico()}");
            codigoVehiculo.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoVehiculo.execute();
            String idVehiculo = codigoVehiculo.getString(1);

            preparedVehiculo = dbf.getConnection().prepareStatement("INSERT INTO VehiculoTB("
                    + "IdVehiculo,"
                    + "Marca,"
                    + "NumeroPlaca,"
                    + "IdEntidadEmisora,"
                    + "NumeroAutorizacion,"
                    + "FechaCreacion,"
                    + "HoraCreacion,"
                    + "FechaModificacion,"
                    + "HoraModificacion,"
                    + "IdEmpleado"
                    + ")VALUES(?,?,?,?,?,GETDATE(),GETDATE(),GETDATE(),GETDATE(),?)");
            preparedVehiculo.setString(1, idVehiculo);
            preparedVehiculo.setString(2, vehiculoTB.getMarca());
            preparedVehiculo.setString(3, vehiculoTB.getNumeroPlaca());
            preparedVehiculo.setInt(4, vehiculoTB.getIdEntidadEmisora());
            preparedVehiculo.setString(5, vehiculoTB.getNumeroAutorizacion());
            preparedVehiculo.setString(6, vehiculoTB.getIdEmpleado());
            preparedVehiculo.addBatch();

            preparedVehiculo.executeBatch();
            dbf.getConnection().commit();
            return "registered";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (codigoVehiculo != null) {
                    codigoVehiculo.close();
                }
                if (preparedVehiculo != null) {
                    preparedVehiculo.close();
                }
                if (preparedValidar != null) {
                    preparedValidar.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static List<VehiculoTB> GetSearchComboBoxVehiculo(String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<VehiculoTB> vehiculoTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Vehiculo_Buscar(?)}");
            preparedStatement.setString(1, search);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VehiculoTB vehiculoTB = new VehiculoTB();
                vehiculoTB.setIdVehiculo(rsEmps.getString("IdVehiculo"));
                vehiculoTB.setMarca(rsEmps.getString("Marca"));
                vehiculoTB.setNumeroPlaca(rsEmps.getString("NumeroPlaca")); 
                vehiculoTBs.add(vehiculoTB);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error en GetSearchComboBoxVehiculo(): " + e);
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
        return vehiculoTBs;
    }

}
