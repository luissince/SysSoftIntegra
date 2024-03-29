package service;

import controller.tools.Session;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ProveedorTB;

public class ProveedorADO {

    public static String CrudEntity(ProveedorTB proveedorTB) {
        DBUtil dbf = new DBUtil();
  

        PreparedStatement preparedValidation = null;
        CallableStatement codigoProveedor = null;
        PreparedStatement preparedProveedor = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidation = dbf.getConnection().prepareStatement("SELECT IdProveedor FROM ProveedorTB WHERE IdProveedor = ?");
            preparedValidation.setString(1, proveedorTB.getIdProveedor());
            if (preparedValidation.executeQuery().next()) {
                preparedValidation = dbf.getConnection().prepareStatement("SELECT NumeroDocumento FROM ProveedorTB WHERE IdProveedor <> ? AND NumeroDocumento = ?");
                preparedValidation.setString(1, proveedorTB.getIdProveedor());
                preparedValidation.setString(2, proveedorTB.getNumeroDocumento());
                if (preparedValidation.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {
                    preparedProveedor = dbf.getConnection().prepareCall("UPDATE "
                            + "ProveedorTB "
                            + "SET "
                            + "TipoDocumento=?,"
                            + "NumeroDocumento=?,"
                            + "RazonSocial=UPPER(?),"
                            + "NombreComercial=UPPER(?),"
                            + "Ambito=?,"
                            + "Estado=?,"
                            + "Telefono=?,"
                            + "Celular=?,"
                            + "Email=?,"
                            + "PaginaWeb=?,"
                            + "Direccion=?,"
                            + "Representante=? "
                            + "WHERE IdProveedor=?");

                    preparedProveedor.setInt(1, proveedorTB.getTipoDocumento());
                    preparedProveedor.setString(2, proveedorTB.getNumeroDocumento());
                    preparedProveedor.setString(3, proveedorTB.getRazonSocial());
                    preparedProveedor.setString(4, proveedorTB.getNombreComercial());
                    preparedProveedor.setInt(5, proveedorTB.getAmbito());
                    preparedProveedor.setInt(6, proveedorTB.getEstado());
                    preparedProveedor.setString(7, proveedorTB.getTelefono());
                    preparedProveedor.setString(8, proveedorTB.getCelular());
                    preparedProveedor.setString(9, proveedorTB.getEmail());
                    preparedProveedor.setString(10, proveedorTB.getPaginaWeb());
                    preparedProveedor.setString(11, proveedorTB.getDireccion());
                    preparedProveedor.setString(12, proveedorTB.getRepresentante());
                    preparedProveedor.setString(13, proveedorTB.getIdProveedor());
                    preparedProveedor.addBatch();

                    preparedProveedor.executeBatch();
                    dbf.getConnection().commit();
                    return "updated";
                }
            } else {
                preparedValidation = dbf.getConnection().prepareStatement("select NumeroDocumento from ProveedorTB where NumeroDocumento = ?");
                preparedValidation.setString(1, proveedorTB.getNumeroDocumento());
                if (preparedValidation.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {
                    codigoProveedor = dbf.getConnection().prepareCall("{? = call Fc_Proveedor_Codigo_Alfanumerico()}");
                    codigoProveedor.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigoProveedor.execute();
                    String idProveedor = codigoProveedor.getString(1);

                    preparedProveedor = dbf.getConnection().prepareCall("INSERT INTO "
                            + "ProveedorTB("
                            + "IdProveedor,"
                            + "TipoDocumento,"
                            + "NumeroDocumento,"
                            + "RazonSocial,"
                            + "NombreComercial,"
                            + "Ambito,"
                            + "Estado,"
                            + "Telefono,"
                            + "Celular,"
                            + "Email,"
                            + "PaginaWeb,"
                            + "Direccion,"
                            + "UsuarioRegistro,"
                            + "FechaRegistro,"
                            + "Representante)"
                            + "values(?,?,?,UPPER(?),UPPER(?),?,?,?,?,?,?,?,?,?,?)");
                    preparedProveedor.setString(1, idProveedor);
                    preparedProveedor.setInt(2, proveedorTB.getTipoDocumento());
                    preparedProveedor.setString(3, proveedorTB.getNumeroDocumento());
                    preparedProveedor.setString(4, proveedorTB.getRazonSocial());
                    preparedProveedor.setString(5, proveedorTB.getNombreComercial());
                    preparedProveedor.setInt(6, proveedorTB.getAmbito());
                    preparedProveedor.setInt(7, proveedorTB.getEstado());
                    preparedProveedor.setString(8, proveedorTB.getTelefono());
                    preparedProveedor.setString(9, proveedorTB.getCelular());
                    preparedProveedor.setString(10, proveedorTB.getEmail());
                    preparedProveedor.setString(11, proveedorTB.getPaginaWeb());
                    preparedProveedor.setString(12, proveedorTB.getDireccion());
                    preparedProveedor.setString(13, Session.USER_ID);
                    preparedProveedor.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
                    preparedProveedor.setString(15, proveedorTB.getRepresentante());
                    preparedProveedor.addBatch();

                    preparedProveedor.executeBatch();
                    dbf.getConnection().commit();
                    return "registered";
                }
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
                return exr.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedProveedor != null) {
                    preparedProveedor.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                if (codigoProveedor != null) {
                    codigoProveedor.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String RemoveProveedor(String idProveedor) {
        DBUtil dbf = new DBUtil();
       
        PreparedStatement preparedValidation = null;
        PreparedStatement preparedProveedor = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidation = dbf.getConnection().prepareStatement("SELECT * FROM CompraTB WHERE Proveedor = ?");
            preparedValidation.setString(1, idProveedor);
            if (preparedValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "exists";
            } else {
                preparedProveedor = dbf.getConnection().prepareStatement("DELETE FROM ProveedorTB WHERE IdProveedor = ?");
                preparedProveedor.setString(1, idProveedor);
                preparedProveedor.addBatch();
                preparedProveedor.executeBatch();
                dbf.getConnection().commit();
                return "removed";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
                return exr.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                if (preparedProveedor != null) {
                    preparedProveedor.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ListProveedor(String search, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] object = new Object[2];
            ObservableList<ProveedorTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Proveedor(?,?,?)}");
            preparedStatement.setString(1, search);
            preparedStatement.setInt(2, posicionPagina);
            preparedStatement.setInt(3, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setId(rsEmps.getRow());
                proveedorTB.setIdProveedor(rsEmps.getString("IdProveedor"));
                proveedorTB.setTipoDocumentoName(rsEmps.getString("Documento"));
                proveedorTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(rsEmps.getString("RazonSocial"));
                proveedorTB.setNombreComercial(rsEmps.getString("NombreComercial"));
                proveedorTB.setEstadoName(rsEmps.getString("Estado"));
                proveedorTB.setTelefono(rsEmps.getString("Telefono"));
                proveedorTB.setCelular(rsEmps.getString("Celular"));
//                proveedorTB.setFechaRegistro(rsEmps.getTimestamp("FRegistro").toLocalDateTime());
                proveedorTB.setRepresentante(rsEmps.getString("Representante"));
                empList.add(proveedorTB);
            }

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Proveedor_Count(?)}");
            preparedStatement.setString(1, search);
            rsEmps = preparedStatement.executeQuery();
            Integer total = 0;
            if (rsEmps.next()) {
                total = rsEmps.getInt("Total");
            }
            object[0] = empList;
            object[1] = total;

            return object;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
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
                return ex.getLocalizedMessage();
            }
        }

    }

    public static ProveedorTB GetIdLisProveedor(String idProveedor) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ProveedorTB proveedorTB = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement( "{call Sp_Get_Proveedor_By_Id(?)}");
            preparedStatement.setString(1, idProveedor);
            try (ResultSet rsEmps = preparedStatement.executeQuery()) {
                while (rsEmps.next()) {
                    proveedorTB = new ProveedorTB();
                    proveedorTB.setIdProveedor(rsEmps.getString("IdProveedor"));
                    proveedorTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                    proveedorTB.setRazonSocial(rsEmps.getString("RazonSocial"));
                    proveedorTB.setNombreComercial(rsEmps.getString("NombreComercial"));
                    proveedorTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                    proveedorTB.setAmbito(rsEmps.getInt("Ambito"));
                    proveedorTB.setEstado(rsEmps.getInt("Estado"));
                    proveedorTB.setTelefono(rsEmps.getString("Telefono"));
                    proveedorTB.setCelular(rsEmps.getString("Celular"));
                    proveedorTB.setEmail(rsEmps.getString("Email"));
                    proveedorTB.setPaginaWeb(rsEmps.getString("PaginaWeb"));
                    proveedorTB.setDireccion(rsEmps.getString("Direccion"));
                    proveedorTB.setRepresentante(rsEmps.getString("Representante"));
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return proveedorTB;
    }

    public static String GetProveedorId(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        String IdProveedor = "";
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement( "SELECT IdProveedor FROM ProveedorTB WHERE NumeroDocumento = ?");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                IdProveedor = rsEmps.getString("IdProveedor");
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
        return IdProveedor;
    }

    public static List<ProveedorTB> getSearchComboBoxProveedores(String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        List<ProveedorTB> proveedorTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Obtener_Proveedor_For_ComboBox(?)}");
            preparedStatement.setString(1, search);
            try (ResultSet rsEmps = preparedStatement.executeQuery()) {
                while (rsEmps.next()) {
                    ProveedorTB proveedorTB = new ProveedorTB();
                    proveedorTB.setIdProveedor(rsEmps.getString("IdProveedor"));
                    proveedorTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                    proveedorTB.setRazonSocial(rsEmps.getString("RazonSocial"));
                    proveedorTBs.add(proveedorTB);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error en getSearchComboBoxProveedores(): " + e.getLocalizedMessage());
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {

            }
        }
        return proveedorTBs;
    }

}
