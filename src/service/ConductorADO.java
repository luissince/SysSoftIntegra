package service;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.ConductorTB;

public class ConductorADO {

    public static String CrudConductor(ConductorTB conductorTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement codigoConductor = null;
        PreparedStatement preparedConductor = null;
        PreparedStatement preparedValidar = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidar = dbf.getConnection().prepareStatement("SELECT * FROM ConductorTB WHERE NumeroDocumento = ?");
            preparedValidar.setString(1, conductorTB.getNumeroDocumento());
            if (preparedValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "duplicate";
            }

            codigoConductor = dbf.getConnection().prepareCall("{? = call Fc_Conductor_Codigo_Alfanumerico()}");
            codigoConductor.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoConductor.execute();
            String idConductor = codigoConductor.getString(1);

            preparedConductor = dbf.getConnection().prepareStatement("INSERT INTO ConductorTB("
                    + "IdConductor,"
                    + "IdTipoDocumento,"
                    + "NumeroDocumento,"
                    + "Informacion,"
                    + "Celular,"
                    + "Telefono,"
                    + "Email,"
                    + "LicenciaConducir,"
                    + "Direccion,"
                    + "FechaCreacion,"
                    + "HoraCreacion,"
                    + "FechaModificacion,"
                    + "HoraModificacion,"
                    + "IdEmpleado)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),GETDATE(),GETDATE(),?)");
            preparedConductor.setString(1, idConductor);
            preparedConductor.setInt(2, conductorTB.getIdTipoDocumento());
            preparedConductor.setString(3, conductorTB.getNumeroDocumento());
            preparedConductor.setString(4, conductorTB.getInformacion());
            preparedConductor.setString(5, conductorTB.getCelular());
            preparedConductor.setString(6, conductorTB.getTelefono());
            preparedConductor.setString(7, conductorTB.getEmail());
            preparedConductor.setString(8, conductorTB.getLicenciaConducir());
            preparedConductor.setString(9, conductorTB.getDireccion());
            preparedConductor.setString(10, conductorTB.getIdEmpleado());
            preparedConductor.addBatch();

            preparedConductor.executeBatch();
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
                if (preparedConductor != null) {
                    preparedConductor.close();
                }
                if (codigoConductor != null) {
                    codigoConductor.close();
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

    public static List<ConductorTB> GetSearchComboBoxConductor(String search, int length) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<ConductorTB> conductorTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Conductor_Buscar(?,?)}");
            preparedStatement.setString(1, search);
            preparedStatement.setInt(2, length);
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
