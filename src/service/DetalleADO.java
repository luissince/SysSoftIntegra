package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DetalleTB;

public class DetalleADO {

    public static ObservableList<DetalleTB> ListDetail(String... value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<DetalleTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_List_Table_Detalle(?,?)}");
            preparedStatement.setString(1, value[0]);
            preparedStatement.setString(2, value[1]);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DetalleTB emp = new DetalleTB();
                emp.setIdDetalle(resultSet.getInt("IdDetalle"));
                emp.setIdAuxiliar(resultSet.getString("IdAuxiliar"));
                emp.setNombre(resultSet.getString("Nombre"));
                emp.setDescripcion(resultSet.getString("Descripcion"));
                emp.setEstado(resultSet.getString("Estado"));
                empList.add(emp);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                System.out.println("La operación de selección de SQL ha fallado: " + ex);
            }

        }
        return empList;
    }

    public static String CrudEntity(DetalleTB detalleTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementDetalle = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection().prepareStatement("select IdDetalle,IdMantenimiento from DetalleTB where IdDetalle=? and IdMantenimiento=?");
            statementValidate.setInt(1, detalleTB.getIdDetalle());
            statementValidate.setString(2, detalleTB.getIdMantenimiento());
            if (statementValidate.executeQuery().next()) {
                statementDetalle = dbf.getConnection().prepareStatement("select IdDetalle,IdMantenimiento from DetalleTB where IdDetalle<>? and IdMantenimiento=? and Nombre = ?");
                statementDetalle.setInt(1, detalleTB.getIdDetalle());
                statementDetalle.setString(2, detalleTB.getIdMantenimiento());
                statementDetalle.setString(3, detalleTB.getNombre());
                if (statementDetalle.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {
                    statementDetalle = dbf.getConnection().prepareStatement("update DetalleTB set IdAuxiliar=UPPER(?),Nombre=UPPER(?),Descripcion=UPPER(?),Estado=? where IdDetalle =? and IdMantenimiento = ?");
                    statementDetalle.setString(1, detalleTB.getIdAuxiliar());
                    statementDetalle.setString(2, detalleTB.getNombre());
                    statementDetalle.setString(3, detalleTB.getDescripcion());
                    statementDetalle.setString(4, detalleTB.getEstado());
                    statementDetalle.setInt(5, detalleTB.getIdDetalle());
                    statementDetalle.setString(6, detalleTB.getIdMantenimiento());
                    statementDetalle.addBatch();

                    statementDetalle.executeBatch();
                    dbf.getConnection().commit();
                    return "updated";
                }
            } else {
                statementDetalle = dbf.getConnection().prepareStatement("select Nombre from DetalleTB where IdMantenimiento = ? and Nombre = ?");
                statementDetalle.setString(1, detalleTB.getIdMantenimiento());
                statementDetalle.setString(2, detalleTB.getNombre());
                if (statementDetalle.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {
                    statementDetalle = dbf.getConnection().prepareStatement("insert into DetalleTB(IdMantenimiento,IdAuxiliar,Nombre,Descripcion,Estado,UsuarioRegistro) values(?,?,?,?,?,?)");
                    statementDetalle.setString(1, detalleTB.getIdMantenimiento());
                    statementDetalle.setString(2, detalleTB.getIdAuxiliar());
                    statementDetalle.setString(3, detalleTB.getNombre().trim().toUpperCase());
                    statementDetalle.setString(4, detalleTB.getDescripcion().trim().toUpperCase());
                    statementDetalle.setString(5, detalleTB.getEstado());
                    statementDetalle.setString(6, detalleTB.getUsuarioRegistro());
                    statementDetalle.addBatch();

                    statementDetalle.executeBatch();
                    dbf.getConnection().commit();
                    return "inserted";
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }

        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String DeleteDetail(DetalleTB detalleTB) {
        DBUtil dbf = new DBUtil();
        String result = null;
       
        PreparedStatement preparedStatement = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            preparedStatement = dbf.getConnection().prepareStatement("DELETE FROM DetalleTB WHERE IdDetalle = ? AND IdMantenimiento = ?");
            preparedStatement.setInt(1, detalleTB.getIdDetalle());
            preparedStatement.setString(2, detalleTB.getIdMantenimiento());
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
            dbf.getConnection().commit();
            result = "eliminado";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException e) {
                result = e.getLocalizedMessage();
            }
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static ObservableList<DetalleTB> GetDetailId(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<DetalleTB> detalleTBs = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Get_Detalle_Id(?)}");
            preparedStatement.setString(1, value);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DetalleTB detalleTB = new DetalleTB();
                detalleTB.setIdDetalle(resultSet.getInt("IdDetalle"));
                detalleTB.setNombre(resultSet.getString("Nombre"));
                detalleTB.setIdAuxiliar(resultSet.getString("IdAuxiliar"));
                detalleTBs.add(detalleTB);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return detalleTBs;
    }

    public static ObservableList<DetalleTB> Get_Detail_IdName(String... value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<DetalleTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Get_Detalle_IdNombre(?,?,?)}");
            preparedStatement.setString(1, value[0]);
            preparedStatement.setString(2, value[1]);
            preparedStatement.setString(3, value[2]);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                DetalleTB detalleTB = new DetalleTB();
                detalleTB.setIdDetalle(resultSet.getInt("IdDetalle"));
                detalleTB.setNombre(resultSet.getString("Nombre"));
                if (value[0].equalsIgnoreCase("3")) {
                    detalleTB.setDescripcion(resultSet.getString("Descripcion"));
                }
                empList.add(detalleTB);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

    public static ObservableList<DetalleTB> GetDetailNameImpuesto() {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<DetalleTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("select IdImpuesto,Nombre from ImpuestoTB");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                DetalleTB detalleTB = new DetalleTB();
                detalleTB.setIdDetalle(resultSet.getInt("IdImpuesto"));
                detalleTB.setNombre(resultSet.getString("Nombre"));
                empList.add(detalleTB);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

}
