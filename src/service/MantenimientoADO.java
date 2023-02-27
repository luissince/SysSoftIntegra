package service;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.MantenimientoTB;

public class MantenimientoADO {

    public static ObservableList<MantenimientoTB> ListMantenimiento(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<MantenimientoTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_List_Table_Matenimiento(?)}");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                MantenimientoTB emp = new MantenimientoTB();
                emp.setIdMantenimiento(rsEmps.getString("IdMantenimiento"));
                emp.setNombre(rsEmps.getString("Nombre"));
                emp.setValidar(rsEmps.getString("Validar"));
                empList.add(emp);
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
        return empList;
    }

    public static String CrudEntity(MantenimientoTB mantenimientoTB) {
        DBUtil dbf = new DBUtil();
        String result = null;
        PreparedStatement statementValidar = null;
        PreparedStatement statementMantenimiento = null;
        CallableStatement codigo_mantenimiento = null;

        

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigo_mantenimiento = dbf.getConnection().prepareCall("{? = call Fc_Mantenimiento_Generar_Codigo()}");
            codigo_mantenimiento.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_mantenimiento.execute();
            String id_mantenimiento = codigo_mantenimiento.getString(1);

            statementValidar = dbf.getConnection().prepareStatement("select IdMantenimiento from MantenimientoTB where IdMantenimiento = ?");
            statementValidar.setString(1, mantenimientoTB.getIdMantenimiento());

            if (statementValidar.executeQuery().next()) {
                //update
                statementMantenimiento = dbf.getConnection().prepareStatement("select Nombre,IdMantenimiento from MantenimientoTB where IdMantenimiento <> ? and Nombre = ?");
                statementMantenimiento.setString(1, mantenimientoTB.getIdMantenimiento());
                statementMantenimiento.setString(2, mantenimientoTB.getNombre());
                if (statementMantenimiento.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "duplicate";
                } else {
                    statementMantenimiento = dbf.getConnection().prepareStatement("update MantenimientoTB set Nombre = UPPER(?) where IdMantenimiento = ?");
                    statementMantenimiento.setString(1, mantenimientoTB.getNombre());
                    statementMantenimiento.setString(2, mantenimientoTB.getIdMantenimiento());
                    statementMantenimiento.addBatch();

                    statementMantenimiento.executeBatch();
                    dbf.getConnection().commit();
                    result = "updated";
                }
            } else {
                //insert
                statementMantenimiento = dbf.getConnection().prepareStatement("select Nombre,IdMantenimiento from MantenimientoTB where IdMantenimiento <> ? and Nombre = ?");
                statementMantenimiento.setString(1, mantenimientoTB.getIdMantenimiento());
                statementMantenimiento.setString(2, mantenimientoTB.getNombre());

                if (statementMantenimiento.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "duplicate";
                } else {
                    statementMantenimiento = dbf.getConnection().prepareStatement("insert into MantenimientoTB(IdMantenimiento,Nombre,Estado,UsuarioRegistro,FechaRegistro) values(?,UPPER(?),?,?,?)");
                    statementMantenimiento.setString(1, id_mantenimiento);
                    statementMantenimiento.setString(2, mantenimientoTB.getNombre());
                    statementMantenimiento.setString(3, mantenimientoTB.getEstado().toString());
                    statementMantenimiento.setString(4, mantenimientoTB.getUsuarioRegistro());
                    statementMantenimiento.setString(5, Tools.getDate());
                    statementMantenimiento.addBatch();

                    statementMantenimiento.executeBatch();
                    dbf.getConnection().commit();
                    result = "inserted";
                }

            }

        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMantenimiento != null) {
                    statementMantenimiento.close();
                }
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (codigo_mantenimiento != null) {
                    codigo_mantenimiento.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static String GetIdMantenimiento() {
        DBUtil dbf = new DBUtil();
        String selectStmt = "{? = call Fc_Mantenimiento_Generar_Codigo()}";
        CallableStatement callableStatement = null;
        try {
            dbf.dbConnect();
            callableStatement = dbf.getConnection().prepareCall(selectStmt);
            callableStatement.registerOutParameter(1, java.sql.Types.VARCHAR);
            callableStatement.execute();
            return callableStatement.getString(1);
        } catch (SQLException  | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e.getLocalizedMessage());
            return "Error al generar";
        } finally {
            try {
                if (callableStatement != null) {
                    callableStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                System.out.println("La consulta se cerro antes de finalizar: " + ex.getLocalizedMessage());
            }
        }
    }

    public static String DeleteMantenimiento(String idMantenimiento) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "delete from MantenimientoTB where IdMantenimiento = ?";
        PreparedStatement preparedStatement = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, idMantenimiento);
            return preparedStatement.executeUpdate() == 1 ? "eliminado" : "error";
        } catch (SQLException  | ClassNotFoundException e) {
            return "La operación de selección de SQL ha fallado: " + e.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return "La consulta se cerro antes de finalizar: " + ex.getLocalizedMessage();
            }
        }
    }

}
