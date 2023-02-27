package service;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.RolTB;

public class RolADO {

    public static ObservableList<RolTB> RolList() {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<RolTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("SELECT * FROM RolTB");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RolTB rolTB = new RolTB();
                rolTB.setIdRol(resultSet.getInt("IdRol"));
                rolTB.setNombre(resultSet.getString("Nombre"));
                rolTB.setSistema(resultSet.getBoolean("Sistema"));
                empList.add(rolTB);
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

    public static String AgregarRol(RolTB rolTB) {
        DBUtil dbf = new DBUtil();
        String result = "";
        CallableStatement codigo_rol = null;
        PreparedStatement statementRol = null;
        PreparedStatement statementMenus = null;
        PreparedStatement statementSubMenus = null;
        PreparedStatement statementPrivilegios = null;
        PreparedStatement statementPermisoMenus = null;
        PreparedStatement statementPermisoSubMenus = null;
        PreparedStatement statementPermisoPrivilegios = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigo_rol = dbf.getConnection().prepareCall("{? = call Fc_Rol_Generar_Codigo()}");
            codigo_rol.registerOutParameter(1, java.sql.Types.INTEGER);
            codigo_rol.execute();
            int id_rol = codigo_rol.getInt(1);

            statementRol = dbf.getConnection().prepareStatement("INSERT INTO RolTB (IdRol,Nombre,Sistema)VALUES(?,?,?)");
            statementRol.setInt(1, id_rol);
            statementRol.setString(2, rolTB.getNombre());
            statementRol.setBoolean(3, rolTB.isSistema());
            statementRol.addBatch();
            statementRol.executeBatch();

            statementMenus = dbf.getConnection().prepareStatement("SELECT IdMenu from MenuTB");
            ResultSet setMenus = statementMenus.executeQuery();

            statementPermisoMenus = dbf.getConnection().prepareStatement("INSERT INTO PermisoMenusTB(IdRol,IdMenus,Estado)VALUES(?,?,?)");
            while (setMenus.next()) {
                statementPermisoMenus.setInt(1, id_rol);
                statementPermisoMenus.setInt(2, setMenus.getInt("IdMenu"));
                statementPermisoMenus.setBoolean(3, false);
                statementPermisoMenus.addBatch();

                statementSubMenus = dbf.getConnection().prepareStatement("SELECT IdSubmenu FROM SubmenuTB WHERE IdMenu = ?");
                statementSubMenus.setInt(1, setMenus.getInt("IdMenu"));
                ResultSet setSubMenus = statementSubMenus.executeQuery();

                statementPermisoSubMenus = dbf.getConnection().prepareStatement("INSERT INTO PermisoSubMenusTB(IdRol,IdMenus,IdSubMenus,Estado)VALUES(?,?,?,?)");
                while (setSubMenus.next()) {
                    statementPermisoSubMenus.setInt(1, id_rol);
                    statementPermisoSubMenus.setInt(2, setMenus.getInt("IdMenu"));
                    statementPermisoSubMenus.setInt(3, setSubMenus.getInt("IdSubmenu"));
                    statementPermisoSubMenus.setBoolean(4, false);
                    statementPermisoSubMenus.addBatch();

                    statementPrivilegios = dbf.getConnection().prepareStatement("SELECT IdPrivilegio FROM PrivilegiosTB WHERE IdSubmenu = ?");
                    statementPrivilegios.setInt(1, setSubMenus.getInt("IdSubmenu"));
                    ResultSet setPrivilegios = statementPrivilegios.executeQuery();

                    statementPermisoPrivilegios = dbf.getConnection().prepareStatement("INSERT INTO PermisoPrivilegiosTB(IdRol,IdPrivilegio,Estado)VALUES(?,?,?)");
                    while (setPrivilegios.next()) {
                        statementPermisoPrivilegios.setInt(1, id_rol);
                        statementPermisoPrivilegios.setInt(2, setPrivilegios.getInt("IdPrivilegio"));
                        statementPermisoPrivilegios.setBoolean(3, false);
                        statementPermisoPrivilegios.addBatch();
                    }
                    statementPermisoPrivilegios.executeBatch();
                }
                statementPermisoSubMenus.executeBatch();
            }
            statementPermisoMenus.executeBatch();

            dbf.getConnection().commit();
            result = "registrado";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (codigo_rol != null) {
                    codigo_rol.close();
                }
                if (statementRol != null) {
                    statementRol.close();
                }
                if (statementMenus != null) {
                    statementMenus.close();
                }
                if (statementSubMenus != null) {
                    statementSubMenus.close();
                }
                if (statementPrivilegios != null) {
                    statementPrivilegios.close();
                }
                if (statementPermisoMenus != null) {
                    statementPermisoMenus.close();
                }
                if (statementPermisoSubMenus != null) {
                    statementPermisoSubMenus.close();
                }
                if (statementPermisoPrivilegios != null) {
                    statementPermisoPrivilegios.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static String editarRol(int idRol, String nombre) {
        DBUtil dbf = new DBUtil();
        String result = "";
        PreparedStatement statementRol = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementRol = dbf.getConnection().prepareStatement("UPDATE RolTB SET Nombre = ? WHERE IdRol = ?");
            statementRol.setString(1, nombre);
            statementRol.setInt(2, idRol);
            statementRol.addBatch();
            statementRol.executeBatch();
            dbf.getConnection().commit();
            result = "updated";

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementRol != null) {
                    statementRol.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static String RemoveRol(int idRol) {
        DBUtil dbf = new DBUtil();
        String result = "";
        PreparedStatement statementValidar = null;
        PreparedStatement statementRol = null;
        PreparedStatement statementPermisosMenu = null;
        PreparedStatement statementPermisosSubMenus = null;
        PreparedStatement statementPermisosPrivilegios = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidar = dbf.getConnection().prepareStatement("SELECT Sistema FROM RolTB WHERE IdRol = ? AND Sistema = 1");
            statementValidar.setInt(1, idRol);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                result = "sistema";
            } else {
                statementValidar.close();
                statementValidar = dbf.getConnection().prepareStatement("SELECT * FROM EmpleadoTB WHERE Rol = ?");
                statementValidar.setInt(1, idRol);
                if (statementValidar.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "exists";
                } else {
                    statementRol = dbf.getConnection().prepareStatement("DELETE FROM RolTB WHERE IdRol = ?");
                    statementRol.setInt(1, idRol);
                    statementRol.addBatch();
                    statementRol.executeBatch();

                    statementPermisosMenu = dbf.getConnection().prepareStatement("DELETE FROM PermisoMenusTB WHERE IdRol = ?");
                    statementPermisosMenu.setInt(1, idRol);
                    statementPermisosMenu.addBatch();
                    statementPermisosMenu.executeBatch();
                    
                    statementPermisosSubMenus = dbf.getConnection().prepareStatement("DELETE FROM PermisoSubMenusTB WHERE IdRol = ?");
                    statementPermisosSubMenus.setInt(1, idRol);
                    statementPermisosSubMenus.addBatch();
                    statementPermisosSubMenus.executeBatch();
                    
                    statementPermisosPrivilegios = dbf.getConnection().prepareStatement("DELETE FROM PermisoPrivilegiosTB WHERE IdRol = ?");
                    statementPermisosPrivilegios.setInt(1, idRol);
                    statementPermisosPrivilegios.addBatch();
                    statementPermisosPrivilegios.executeBatch();

                    dbf.getConnection().commit();
                    result = "removed";
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementRol != null) {
                    statementRol.close();
                }
                if (statementPermisosMenu != null) {
                    statementPermisosMenu.close();
                }
                if (statementPermisosSubMenus != null) {
                    statementPermisosSubMenus.close();
                }
                if (statementPermisosPrivilegios != null) {
                    statementPermisosPrivilegios.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

}
