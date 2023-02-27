package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.VehiculoTB;

public class VehiculoADO {

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
