package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonaADO {

    public static String GetPersonasId(String value) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "SELECT IdPersona FROM PersonaTB WHERE NumeroDocumento = ?";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        String IdPersona = "";
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                IdPersona = rsEmps.getString("IdPersona");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);

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
        return IdPersona;
    }

}
