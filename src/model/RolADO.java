package model;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import static model.DBUtil.getConnection;

public class RolADO {

    public static ObservableList<RolTB> RolList() {
        String selectStmt = "SELECT * FROM RolTB";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<RolTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                RolTB rolTB = new RolTB();
                rolTB.setIdRol(resultSet.getInt("IdRol"));
                rolTB.setNombre(resultSet.getString("Nombre"));
                rolTB.setSistema(resultSet.getBoolean("Sistema"));
                empList.add(rolTB);
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();

            } catch (SQLException ex) {

            }

        }
        return empList;
    }

    public static String AgregarRol(RolTB rolTB) {
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
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            codigo_rol = getConnection().prepareCall("{? = call Fc_Rol_Generar_Codigo()}");
            codigo_rol.registerOutParameter(1, java.sql.Types.INTEGER);
            codigo_rol.execute();
            int id_rol = codigo_rol.getInt(1);

            statementRol = DBUtil.getConnection().prepareStatement("INSERT INTO RolTB (IdRol,Nombre,Sistema)VALUES(?,?,?)");
            statementRol.setInt(1, id_rol);
            statementRol.setString(2, rolTB.getNombre());
            statementRol.setBoolean(3, rolTB.isSistema());
            statementRol.addBatch();
            statementRol.executeBatch();

            statementMenus = DBUtil.getConnection().prepareStatement("SELECT IdMenu from MenuTB");
            ResultSet setMenus = statementMenus.executeQuery();

            statementPermisoMenus = DBUtil.getConnection().prepareStatement("INSERT INTO PermisoMenusTB(IdRol,IdMenus,Estado)VALUES(?,?,?)");
            while (setMenus.next()) {
                statementPermisoMenus.setInt(1, id_rol);
                statementPermisoMenus.setInt(2, setMenus.getInt("IdMenu"));
                statementPermisoMenus.setBoolean(3, false);
                statementPermisoMenus.addBatch();

                statementSubMenus = DBUtil.getConnection().prepareStatement("SELECT IdSubmenu FROM SubmenuTB WHERE IdMenu = ?");
                statementSubMenus.setInt(1, setMenus.getInt("IdMenu"));
                ResultSet setSubMenus = statementSubMenus.executeQuery();

                statementPermisoSubMenus = DBUtil.getConnection().prepareStatement("INSERT INTO PermisoSubMenusTB(IdRol,IdMenus,IdSubMenus,Estado)VALUES(?,?,?,?)");
                while (setSubMenus.next()) {
                    statementPermisoSubMenus.setInt(1, id_rol);
                    statementPermisoSubMenus.setInt(2, setMenus.getInt("IdMenu"));
                    statementPermisoSubMenus.setInt(3, setSubMenus.getInt("IdSubmenu"));
                    statementPermisoSubMenus.setBoolean(4, false);
                    statementPermisoSubMenus.addBatch();

                    statementPrivilegios = DBUtil.getConnection().prepareStatement("SELECT IdPrivilegio FROM PrivilegiosTB WHERE IdSubmenu = ?");
                    statementPrivilegios.setInt(1, setSubMenus.getInt("IdSubmenu"));
                    ResultSet setPrivilegios = statementPrivilegios.executeQuery();

                    statementPermisoPrivilegios = DBUtil.getConnection().prepareStatement("INSERT INTO PermisoPrivilegiosTB(IdRol,IdPrivilegio,Estado)VALUES(?,?,?)");
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

            DBUtil.getConnection().commit();
            result = "registrado";
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
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
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static String editarRol(int idRol, String nombre) {
        String result = "";
        PreparedStatement statementRol = null;
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            statementRol = DBUtil.getConnection().prepareStatement("UPDATE RolTB SET Nombre = ? WHERE IdRol = ?");
            statementRol.setString(1, nombre);
            statementRol.setInt(2, idRol);
            statementRol.addBatch();
            statementRol.executeBatch();
            DBUtil.getConnection().commit();
            result = "updated";

        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementRol != null) {
                    statementRol.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static String RemoveRol(int idRol) {
        String result = "";
        PreparedStatement statementValidar = null;
        PreparedStatement statementRol = null;
        PreparedStatement statementPermisosMenu = null;
        PreparedStatement statementPermisosSubMenus = null;
        PreparedStatement statementPermisosPrivilegios = null;
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            statementValidar = DBUtil.getConnection().prepareStatement("SELECT Sistema FROM RolTB WHERE IdRol = ? AND Sistema = 1");
            statementValidar.setInt(1, idRol);
            if (statementValidar.executeQuery().next()) {
                DBUtil.getConnection().rollback();
                result = "sistema";
            } else {
                statementValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM EmpleadoTB WHERE Rol = ?");
                statementValidar.setInt(1, idRol);
                if (statementValidar.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "exists";
                } else {
                    statementRol = DBUtil.getConnection().prepareStatement("DELETE FROM RolTB WHERE IdRol = ?");
                    statementRol.setInt(1, idRol);
                    statementRol.addBatch();
                    statementRol.executeBatch();

                    statementPermisosMenu = DBUtil.getConnection().prepareStatement("DELETE FROM PermisoMenusTB WHERE IdRol = ?");
                    statementPermisosMenu.setInt(1, idRol);
                    statementPermisosMenu.addBatch();
                    statementPermisosMenu.executeBatch();
                    
                    statementPermisosSubMenus = DBUtil.getConnection().prepareStatement("DELETE FROM PermisoSubMenusTB WHERE IdRol = ?");
                    statementPermisosSubMenus.setInt(1, idRol);
                    statementPermisosSubMenus.addBatch();
                    statementPermisosSubMenus.executeBatch();
                    
                    statementPermisosPrivilegios = DBUtil.getConnection().prepareStatement("DELETE FROM PermisoPrivilegiosTB WHERE IdRol = ?");
                    statementPermisosPrivilegios.setInt(1, idRol);
                    statementPermisosPrivilegios.addBatch();
                    statementPermisosPrivilegios.executeBatch();

                    DBUtil.getConnection().commit();
                    result = "removed";
                }
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
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
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

}
