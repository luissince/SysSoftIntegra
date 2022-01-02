

package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UbigeoADO {
    
    public static List<UbigeoTB> GetSearchComboBoxUbigeo(String search) {
        String selectStmt = "{call Sp_Obtener_Ubigeo_BySearch(?)}";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<UbigeoTB> ubigeoTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, search);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                UbigeoTB ubigeoTB = new UbigeoTB();
                ubigeoTB.setIdUbigeo(rsEmps.getInt("IdUbigeo"));
                ubigeoTB.setUbigeo(rsEmps.getString("Ubigeo"));
                ubigeoTB.setDepartamento(rsEmps.getString("Departamento"));
                ubigeoTB.setProvincia(rsEmps.getString("Provincia"));
                ubigeoTB.setDistrito(rsEmps.getString("Distrito"));
                ubigeoTBs.add(ubigeoTB);
            }
        } catch (SQLException e) {
            System.out.println("Error en GetSearchComboBoxUbigeo(): " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return ubigeoTBs;
    }

}
