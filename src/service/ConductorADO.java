
package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.ConductorTB;

public class ConductorADO {

    public static List<ConductorTB> GetSearchComboBoxConductor(String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<ConductorTB> conductorTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Conductor_Buscar(?)}");
            preparedStatement.setString(1, search);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                ConductorTB conductorTB = new ConductorTB();
                conductorTB.setIdConductor(rsEmps.getString("IdConductor"));
                conductorTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                conductorTB.setInformacion(rsEmps.getString("Informacion"));
                conductorTBs.add(conductorTB);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error en GetSearchComboBoxConductor(): " + e);
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
        return conductorTBs;
    }

}
