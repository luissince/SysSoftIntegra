package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.MenuTB;
import model.PrivilegioTB;
import model.SubMenusTB;

public class MenuADO {

    public static ObservableList<MenuTB> GetMenus(int idRol) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "select\n"
                + "m.IdMenu,\n"
                + "m.Nombre,\n"
                + "pm.Estado\n"
                + "from\n"
                + "PermisoMenusTB as pm inner join RolTB as r \n"
                + "on pm.IdRol = r.IdRol\n"
                + "inner join MenuTB as m \n"
                + "on pm.IdMenus = m.IdMenu\n"
                + "where pm.IdRol = ? ";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<MenuTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setInt(1, idRol);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                MenuTB menuTB = new MenuTB();
                menuTB.setIdMenu(resultSet.getInt("IdMenu"));
                menuTB.setNombre(resultSet.getString("Nombre"));
                menuTB.setEstado(resultSet.getBoolean("Estado"));
                empList.add(menuTB);
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

    public static ObservableList<SubMenusTB> GetSubMenus(int rol, int menu) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "select\n"
                + "sm.IdSubmenu,\n"
                + "sm.Nombre,\n"
                + "psm.Estado\n"
                + "from PermisoSubMenusTB as psm\n"
                + "inner join RolTB as r \n"
                + "on psm.IdRol = r.IdRol\n"
                + "inner join SubmenuTB as sm\n"
                + "on psm.IdSubMenus = sm.IdSubmenu\n"
                + "where psm.IdRol = ? and psm.IdMenus = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<SubMenusTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setInt(1, rol);
            preparedStatement.setInt(2, menu);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                SubMenusTB menuTB = new SubMenusTB();
                menuTB.setIdSubMenu(resultSet.getInt("IdSubmenu"));
                menuTB.setNombre(resultSet.getString("Nombre"));
                menuTB.setEstado(resultSet.getBoolean("Estado"));
                empList.add(menuTB);
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

    public static ObservableList<PrivilegioTB> GetPrivilegios(int rol, int submenu) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "SELECT\n" 
                + "pp.IdPrivilegio,\n"
                + "p.Nombre,\n"
                + "pp.Estado\n"
                + "FROM PermisoPrivilegiosTB AS pp INNER JOIN PrivilegiosTB AS p ON pp.IdPrivilegio = p.IdPrivilegio\n"
                + "WHERE pp.IdRol = ? AND p.IdSubmenu = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<PrivilegioTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setInt(1, rol);
            preparedStatement.setInt(2, submenu);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                PrivilegioTB privilegioTB = new PrivilegioTB();
                privilegioTB.setIdPrivilegio(resultSet.getInt("IdPrivilegio"));
                privilegioTB.setNombre(resultSet.getString("Nombre"));
                privilegioTB.setEstado(resultSet.getBoolean("Estado"));
                empList.add(privilegioTB);
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

    public static ObservableList<MenuTB> ListMenus() {
        DBUtil dbf = new DBUtil();
        String selectStmt = "select IdMenu,Nombre from MenuTB";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<MenuTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                MenuTB menuTB = new MenuTB();
                menuTB.setIdMenu(resultSet.getInt("IdMenu"));
                menuTB.setNombre(resultSet.getString("Nombre"));
                empList.add(menuTB);
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

    public static ObservableList<SubMenusTB> ListSubMenus(int menu) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "select IdSubmenu,Nombre from SubmenuTB where IdMenu = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<SubMenusTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setInt(1, menu);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                SubMenusTB subMenusTB = new SubMenusTB();
                subMenusTB.setIdSubMenu(resultSet.getInt("IdSubmenu"));
                subMenusTB.setNombre(resultSet.getString("Nombre"));
                empList.add(subMenusTB);
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

    public static ObservableList<PrivilegioTB> ListPrivilegios(int subMenu) {
        DBUtil dbf = new DBUtil();
        String selectStmt = "select IdPrivilegio,Nombre from PrivilegiosTB where IdSubmenu = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ObservableList<PrivilegioTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(selectStmt);
            preparedStatement.setInt(1, subMenu);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PrivilegioTB privilegioTB = new PrivilegioTB();
                privilegioTB.setIdPrivilegio(resultSet.getInt("IdPrivilegio"));
                privilegioTB.setNombre(resultSet.getString("Nombre"));
                empList.add(privilegioTB);
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

    public static String CrudPermisosMenu(int rol, int idmenu, boolean estado) {
        DBUtil dbf = new DBUtil();
      

        PreparedStatement statementPermisosMenu = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementPermisosMenu = dbf.getConnection().prepareStatement("UPDATE PermisoMenusTB SET Estado = ? WHERE IdRol = ? AND IdMenus = ?");
            statementPermisosMenu.setBoolean(1, estado);
            statementPermisosMenu.setInt(2, rol);
            statementPermisosMenu.setInt(3, idmenu);
            statementPermisosMenu.addBatch();
            statementPermisosMenu.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementPermisosMenu != null) {
                    statementPermisosMenu.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String CrudPermisosSubMenu(int rol, int idsubmenus, boolean estado) {
        DBUtil dbf = new DBUtil();
    

        PreparedStatement statementPermisosMenu = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementPermisosMenu = dbf.getConnection().prepareStatement("UPDATE PermisoSubMenusTB SET Estado = ? WHERE IdRol = ? AND IdSubMenus = ?");
            statementPermisosMenu.setBoolean(1, estado);
            statementPermisosMenu.setInt(2, rol);
            statementPermisosMenu.setInt(3, idsubmenus);
            statementPermisosMenu.addBatch();
            statementPermisosMenu.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementPermisosMenu != null) {
                    statementPermisosMenu.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String CrudPermisosPrivilegios(int rol, int idprivilegios, boolean estado) {
        DBUtil dbf = new DBUtil();
       

        PreparedStatement statementPermisosMenu = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementPermisosMenu = dbf.getConnection().prepareStatement("UPDATE PermisoPrivilegiosTB SET Estado = ? WHERE IdRol = ? AND IdPrivilegio = ?");
            statementPermisosMenu.setBoolean(1, estado);
            statementPermisosMenu.setInt(2, rol);
            statementPermisosMenu.setInt(3, idprivilegios);
            statementPermisosMenu.addBatch();
            statementPermisosMenu.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementPermisosMenu != null) {
                    statementPermisosMenu.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
