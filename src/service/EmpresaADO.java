package service;

import controller.tools.Tools;
import model.EmpresaTB;
import model.UbigeoTB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpresaADO {

    public static String CrudEntity(EmpresaTB empresaTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementEmpresa = null;   

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM EmpresaTB WHERE IdEmpresa = ?");
            statementValidate.setInt(1, empresaTB.getIdEmpresa());
            if (statementValidate.executeQuery().next()) {
                statementEmpresa = dbf.getConnection().prepareStatement("UPDATE EmpresaTB\n"
                        + "SET GiroComercial=?,\n"
                        + "Nombre = ?,\n"
                        + "Telefono=?,\n"
                        + "Celular=?,\n"
                        + "PaginaWeb=?,\n"
                        + "Email=?,\n"
                        + "Domicilio=?,\n"
                        + "TipoDocumento=?,"
                        + "NumeroDocumento=?,\n"
                        + "RazonSocial=?,\n"
                        + "NombreComercial=?,\n"
                        + "Terminos=?,\n"
                        + "Condiciones=?,\n"
                        + "Image=?,\n"
                        + "Ubigeo=?\n"
                        + "WHERE IdEmpresa=?");
                statementEmpresa.setInt(1, empresaTB.getGiroComerial());
                statementEmpresa.setString(2, empresaTB.getNombre());
                statementEmpresa.setString(3, empresaTB.getTelefono());
                statementEmpresa.setString(4, empresaTB.getCelular());
                statementEmpresa.setString(5, empresaTB.getPaginaWeb());
                statementEmpresa.setString(6, empresaTB.getEmail());
                statementEmpresa.setString(7, empresaTB.getDomicilio());
                statementEmpresa.setInt(8, empresaTB.getTipoDocumento());
                statementEmpresa.setString(9, empresaTB.getNumeroDocumento());
                statementEmpresa.setString(10, empresaTB.getRazonSocial());
                statementEmpresa.setString(11, empresaTB.getNombreComercial());
                statementEmpresa.setString(12, empresaTB.getTerminos());
                statementEmpresa.setString(13, empresaTB.getCondiciones());
                statementEmpresa.setBytes(14, empresaTB.getImage());
                statementEmpresa.setInt(15, empresaTB.getIdUbigeo());
                statementEmpresa.setInt(16, empresaTB.getIdEmpresa());
                statementEmpresa.addBatch();

                statementEmpresa.executeBatch();
                dbf.getConnection().commit();
                return "updated";
            } else {
                statementEmpresa = dbf.getConnection().prepareStatement("INSERT INTO EmpresaTB"
                        + "(GiroComercial,\n"
                        + "Nombre,\n"
                        + "Telefono,\n"
                        + "Celular,\n"
                        + "PaginaWeb,\n"
                        + "Email,\n"
                        + "Domicilio,\n"
                        + "TipoDocumento,\n"
                        + "NumeroDocumento,\n"
                        + "RazonSocial,\n"
                        + "NombreComercial,\n"
                        + "Terminos,\n"
                        + "Condiciones,\n"
                        + "Image,\n"
                        + "Ubigeo)\n"
                        + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                statementEmpresa.setInt(1, empresaTB.getGiroComerial());
                statementEmpresa.setString(2, empresaTB.getNombre());
                statementEmpresa.setString(3, empresaTB.getTelefono());
                statementEmpresa.setString(4, empresaTB.getCelular());
                statementEmpresa.setString(5, empresaTB.getPaginaWeb());
                statementEmpresa.setString(6, empresaTB.getEmail());
                statementEmpresa.setString(7, empresaTB.getDomicilio());
                statementEmpresa.setInt(8, empresaTB.getTipoDocumento());
                statementEmpresa.setString(9, empresaTB.getNumeroDocumento());
                statementEmpresa.setString(10, empresaTB.getRazonSocial());
                statementEmpresa.setString(11, empresaTB.getNombreComercial());
                statementEmpresa.setString(12, empresaTB.getTerminos());
                statementEmpresa.setString(13, empresaTB.getCondiciones());
                statementEmpresa.setBytes(14, empresaTB.getImage());
                statementEmpresa.setInt(15, empresaTB.getIdUbigeo());
                statementEmpresa.addBatch();

                statementEmpresa.executeBatch();
                dbf.getConnection().commit();
                return "registered";
            }
        } catch (SQLException  | ClassNotFoundException e) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException ex) {

            }
            return e.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementEmpresa != null) {
                    statementEmpresa.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static boolean isConfiguration() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        try {
            dbf.dbConnect();
            statementValidate = dbf.getConnection().prepareStatement("select * from EmpresaTB");
            if (statementValidate.executeQuery().next()) {
                return true;
            }
        } catch (SQLException  | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {

            }
        }
        return false;
    }

    public static EmpresaTB GetEmpresa() {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        EmpresaTB empresaTB = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Empresa()}");
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                empresaTB = new EmpresaTB();
                empresaTB.setIdEmpresa(rsEmps.getInt("IdEmpresa"));
                empresaTB.setGiroComerial(rsEmps.getInt("GiroComercial"));
                empresaTB.setNombre(rsEmps.getString("Nombre"));
                empresaTB.setTelefono(rsEmps.getString("Telefono"));
                empresaTB.setCelular(rsEmps.getString("Celular"));
                empresaTB.setPaginaWeb(rsEmps.getString("PaginaWeb"));
                empresaTB.setEmail(rsEmps.getString("Email"));
                empresaTB.setDomicilio(rsEmps.getString("Domicilio"));

                empresaTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                empresaTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                empresaTB.setRazonSocial(rsEmps.getString("RazonSocial"));
                empresaTB.setNombreComercial(rsEmps.getString("NombreComercial"));
                empresaTB.setTerminos(rsEmps.getString("Terminos"));
                empresaTB.setCondiciones(rsEmps.getString("Condiciones"));
                empresaTB.setImage(rsEmps.getBytes("Image"));

                UbigeoTB ubigeoTB = new UbigeoTB();
                ubigeoTB.setIdUbigeo(rsEmps.getInt("IdUbigeo"));
                ubigeoTB.setUbigeo(rsEmps.getString("CodigoUbigeo"));
                ubigeoTB.setDepartamento(rsEmps.getString("Departamento"));
                ubigeoTB.setProvincia(rsEmps.getString("Provincia"));
                ubigeoTB.setDistrito(rsEmps.getString("Distrito"));
                empresaTB.setUbigeoTB(ubigeoTB);
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
        return empresaTB;
    }

    public static String Terminos_Condiciones() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementTerCond = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementTerCond = dbf.getConnection().prepareStatement("SELECT TOP 1 Terminos,Condiciones FROM EmpresaTB");
            resultSet = statementTerCond.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Terminos") + "\n" + resultSet.getString("Condiciones");
            } else {
                return "-";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Tools.println(ex.getLocalizedMessage());
            return "--";
        } finally {
            try {
                if (statementTerCond != null) {
                    statementTerCond.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

}
