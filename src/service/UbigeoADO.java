

package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.UbigeoTB;


public class UbigeoADO {
    
    public static List<UbigeoTB> GetSearchComboBoxUbigeo(String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<UbigeoTB> ubigeoTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Obtener_Ubigeo_BySearch(?)}");
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
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error en GetSearchComboBoxUbigeo(): " + e);
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
        return ubigeoTBs;
    }

}
