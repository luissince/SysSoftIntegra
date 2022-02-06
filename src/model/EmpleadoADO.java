package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;

public class EmpleadoADO {

    public static String InsertEmpleado(EmpleadoTB empleadoTB) {
        PreparedStatement empleado = null;
        CallableStatement codigo_empleado = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            codigo_empleado = DBUtil.getConnection().prepareCall("{? = call Fc_Empleado_Codigo_Alfanumerico()}");
            codigo_empleado.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_empleado.execute();
            String id_empleado = codigo_empleado.getString(1);

            empleado = DBUtil.getConnection().prepareStatement("INSERT INTO EmpleadoTB( "
                    + "IdEmpleado,"
                    + "TipoDocumento,"
                    + "NumeroDocumento,"
                    + "Apellidos,Nombres,"
                    + "Sexo,"
                    + "FechaNacimiento,"
                    + "Puesto,"
                    + "Rol,"
                    + "Estado,"
                    + "Telefono,"
                    + "Celular,"
                    + "Email,"
                    + "Direccion,"
                    + "Usuario,"
                    + "Clave,"
                    + "Sistema,"
                    + "Huella)\n"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            empleado.setString(1, id_empleado);
            empleado.setInt(2, empleadoTB.getTipoDocumento());
            empleado.setString(3, empleadoTB.getNumeroDocumento());
            empleado.setString(4, empleadoTB.getApellidos());
            empleado.setString(5, empleadoTB.getNombres());
            empleado.setInt(6, empleadoTB.getSexo());
            empleado.setDate(7, empleadoTB.getFechaNacimiento());
            empleado.setInt(8, empleadoTB.getPuesto());
            empleado.setInt(9, empleadoTB.getRol());
            empleado.setInt(10, empleadoTB.getEstado());
            empleado.setString(11, empleadoTB.getTelefono());
            empleado.setString(12, empleadoTB.getCelular());
            empleado.setString(13, empleadoTB.getEmail());
            empleado.setString(14, empleadoTB.getDireccion());
            empleado.setString(15, empleadoTB.getUsuario());
            empleado.setString(16, empleadoTB.getClave());
            empleado.setBoolean(17, empleadoTB.isSistema());
            empleado.setString(18, empleadoTB.getHuella());
            empleado.addBatch();

            empleado.executeBatch();

            DBUtil.getConnection().commit();
            return "register";
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (empleado != null) {
                    empleado.close();
                }
                if (codigo_empleado != null) {
                    codigo_empleado.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException e) {
            }
        }
    }

    public static String UpdateEmpleado(EmpleadoTB empleadoTB) {
        PreparedStatement empleado = null;
        CallableStatement codigo_empleado = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            empleado = DBUtil.getConnection().prepareStatement("UPDATE EmpleadoTB SET TipoDocumento = ?,NumeroDocumento = ?,Apellidos = ?,Nombres = ?,Sexo = ?,FechaNacimiento = ?,Puesto = ?,Rol = ?,Estado = ?,Telefono = ?,Celular = ?,Email = ?,Direccion = ?,Usuario = ?,Clave  = ? WHERE IdEmpleado = ?");

            empleado.setInt(1, empleadoTB.getTipoDocumento());
            empleado.setString(2, empleadoTB.getNumeroDocumento());
            empleado.setString(3, empleadoTB.getApellidos());
            empleado.setString(4, empleadoTB.getNombres());
            empleado.setInt(5, empleadoTB.getSexo());
            empleado.setDate(6, empleadoTB.getFechaNacimiento());
            empleado.setInt(7, empleadoTB.getPuesto());
            empleado.setInt(8, empleadoTB.getRol());
            empleado.setInt(9, empleadoTB.getEstado());
            empleado.setString(10, empleadoTB.getTelefono());
            empleado.setString(11, empleadoTB.getCelular());
            empleado.setString(12, empleadoTB.getEmail());
            empleado.setString(13, empleadoTB.getDireccion());
            empleado.setString(14, empleadoTB.getUsuario());
            empleado.setString(15, empleadoTB.getClave());
            empleado.setString(16, empleadoTB.getIdEmpleado());
            empleado.addBatch();

            empleado.executeBatch();

            DBUtil.getConnection().commit();
            return "update";
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (empleado != null) {
                    empleado.close();
                }
                if (codigo_empleado != null) {
                    codigo_empleado.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException e) {
            }
        }
    }

    public static Object ListEmpleados(int opcion, String buscar, int posicionPagina, int filasPorPagina) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();
            Object[] object = new Object[2];
            ObservableList<EmpleadoTB> empList = FXCollections.observableArrayList();

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Empleados(?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, buscar);
            preparedStatement.setInt(3, posicionPagina);
            preparedStatement.setInt(4, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setId(rsEmps.getRow() + posicionPagina);
                empleadoTB.setIdEmpleado(rsEmps.getString("IdEmpleado"));
                empleadoTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                empleadoTB.setTelefono(rsEmps.getString("Telefono"));
                empleadoTB.setCelular(rsEmps.getString("Celular"));
                empleadoTB.setDireccion(rsEmps.getString("Direccion"));
                empleadoTB.setRolName(rsEmps.getString("Rol"));
                empleadoTB.setEstadoName(rsEmps.getString("Estado"));
                empList.add(empleadoTB);
            }
            object[0] = empList;

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Empleados_Count(?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, buscar);
            rsEmps = preparedStatement.executeQuery();
            Integer total = 0;
            if (rsEmps.next()) {
                total = rsEmps.getInt("Total");
            }
            object[1] = total;

            return object;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static EmpleadoTB GetByIdEmpleados(String value) {
        String selectStmt = "SELECT * FROM EmpleadoTB WHERE IdEmpleado = ?";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        EmpleadoTB empleadoTB = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();

            if (rsEmps.next()) {
                empleadoTB = new EmpleadoTB();
                empleadoTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                empleadoTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                empleadoTB.setSexo(rsEmps.getInt("Sexo"));
                empleadoTB.setFechaNacimiento(rsEmps.getDate("FechaNacimiento"));
                empleadoTB.setPuesto(rsEmps.getInt("Puesto"));
                empleadoTB.setEstado(rsEmps.getInt("Estado"));
                empleadoTB.setTelefono(rsEmps.getString("Telefono"));
                empleadoTB.setCelular(rsEmps.getString("Celular"));
                empleadoTB.setEmail(rsEmps.getString("Email"));
                empleadoTB.setDireccion(rsEmps.getString("Direccion"));
                empleadoTB.setUsuario(rsEmps.getString("Usuario"));
                empleadoTB.setClave(rsEmps.getString("Clave"));
                empleadoTB.setRol(rsEmps.getInt("Rol"));
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empleadoTB;
    }

    public static Object GetValidateUser(String user, String clave) {
        String selectStmt = "{CALL Sp_Validar_Ingreso(?,?)}";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, clave);
            rsEmps = preparedStatement.executeQuery();
            EmpleadoTB empleadoTB = new EmpleadoTB();
            if (rsEmps.next()) {
                empleadoTB.setIdEmpleado(rsEmps.getString("IdEmpleado"));
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                empleadoTB.setRolName(rsEmps.getString("RolName"));
                empleadoTB.setEstado(rsEmps.getInt("Estado"));
                empleadoTB.setRol(rsEmps.getInt("Rol"));
                return empleadoTB;
            } else {
                throw new Exception("No se encontro al cliente, intente nuevamente.");
            }

        } catch (SQLException ex) {
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
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

    }

//    public static void UpdateImage() {
//        PreparedStatement preparedUpdate = null;
//        PreparedStatement preparedStatement = null;
//        try {
//            preparedUpdate = DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET NuevaImagen = ? WHERE IdSuministro = ?");
//
//            preparedStatement = DBUtil.getConnection().prepareStatement("SELECT IdSuministro,Imagen FROM SuministroTB where NuevaImagen is null");
//            ResultSet rsEmps = preparedStatement.executeQuery();
//            File selectFile = null;
//            while (rsEmps.next()) {
//                selectFile = new File(rsEmps.getString("Imagen"));
//                if (selectFile != null && selectFile.exists()) {
//                    preparedUpdate.setBytes(1, Tools.getImageBytes(selectFile));
//                    preparedUpdate.setString(2, rsEmps.getString("IdSuministro"));
//                    preparedUpdate.execute();
//                    Tools.println(Tools.getImageBytes(selectFile));
//                }
//            }
//
//            Tools.println("bien");
//        } catch (SQLException ex) {
//            Tools.println(ex.getLocalizedMessage());
//
//        }
//
//    }
    public static ObservableList<EmpleadoTB> ListEmployeeInProProduction(String value) {
        String selectStmt = "{call Sp_Listar_Empleados(?)}";
        //String selectStmt = "select * from EmpleadoTB";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<EmpleadoTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setId(rsEmps.getInt("Filas"));
                //empleadoTB.setId(rsEmps.getRow());
                empleadoTB.setIdEmpleado(rsEmps.getString("IdEmpleado"));
                empleadoTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                empleadoTB.setTelefono(rsEmps.getString("Telefono"));
                empleadoTB.setCelular(rsEmps.getString("Celular"));
                empleadoTB.setPuestoName(rsEmps.getString("Puesto"));
                empleadoTB.setEstadoName(rsEmps.getString("Estado"));

                CheckBox checkBox = new CheckBox();
                checkBox.getStyleClass().add("check-box-contenido");
                checkBox.setText("");
                empleadoTB.setValidarEm(checkBox);

                empList.add(empleadoTB);
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

    public static List<EmpleadoTB> Lista_Empleado_For_Compras() {
        List<EmpleadoTB> empleadoTBs = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementEmpleados = null;
            try {
                statementEmpleados = DBUtil.getConnection().prepareStatement("SELECT IdEmpleado,Apellidos,Nombres FROM EmpleadoTB ORDER BY Apellidos ASC");
                ResultSet resultSet = statementEmpleados.executeQuery();
                while (resultSet.next()) {
                    EmpleadoTB empleadoTB = new EmpleadoTB();
                    empleadoTB.setIdEmpleado(resultSet.getString("IdEmpleado"));
                    empleadoTB.setNombres(resultSet.getString("Apellidos").toUpperCase());
                    empleadoTB.setApellidos(resultSet.getString("Nombres").toUpperCase());
                    empleadoTBs.add(empleadoTB);
                }
            } catch (SQLException ex) {

            } finally {
                try {
                    if (statementEmpleados != null) {
                        statementEmpleados.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        }
        return empleadoTBs;
    }

    public static String DeleteEmpleadoById(String idEmpleado) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidation = null;
            PreparedStatement statementEmpleado = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM EmpleadoTB WHERE IdEmpleado = ? AND Sistema = 1");
                statementValidation.setString(1, idEmpleado);
                if (statementValidation.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "sistema";
                } else {
                    statementValidation = DBUtil.getConnection().prepareCall("SELECT * FROM CajaTB WHERE IdUsuario = ?");
                    statementValidation.setString(1, idEmpleado);
                    if (statementValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "caja";
                    } else {
                        statementValidation = DBUtil.getConnection().prepareCall("SELECT * FROM CompraTB WHERE Usuario = ?");
                        statementValidation.setString(1, idEmpleado);
                        if (statementValidation.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            result = "compra";
                        } else {
                            statementEmpleado = DBUtil.getConnection().prepareStatement("DELETE FROM EmpleadoTB WHERE IdEmpleado = ?");
                            statementEmpleado.setString(1, idEmpleado);
                            statementEmpleado.addBatch();
                            statementEmpleado.executeBatch();
                            DBUtil.getConnection().commit();
                            result = "deleted";
                        }
                    }
                }
            } catch (SQLException ex) {
                try {
                    result = ex.getLocalizedMessage();
                    DBUtil.getConnection().rollback();
                } catch (SQLException e) {
                    result = e.getLocalizedMessage();
                }
            } finally {
                try {
                    if (statementValidation != null) {
                        statementValidation.close();
                    }
                    if (statementEmpleado != null) {
                        statementEmpleado.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo conectar el servidor, revise su conexión.";
        }
        return result;
    }

    public static ArrayList<String> CountSuministros(long sleep) throws InterruptedException {
        ArrayList<String> arrayList = new ArrayList();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidation = null;
            try {
                statementValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM SuministroTB");
                ResultSet resultSet = statementValidation.executeQuery();
                Thread.sleep(sleep);
                while (resultSet.next()) {
                    arrayList.add(resultSet.getString("IdSuministro"));
                }
            } catch (SQLException ex) {
                Tools.println("Error sql: " + ex.getLocalizedMessage());
            } finally {
                try {
                    if (statementValidation != null) {
                        statementValidation.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        }
        return arrayList;
    }

    public static List<EmpleadoTB> getSearchComboBoxEmpleados(String buscar) {
        String selectStmt = "SELECT IdEmpleado,NumeroDocumento,Apellidos,Nombres FROM EmpleadoTB WHERE NumeroDocumento LIKE ? OR Apellidos LIKE ? OR Nombres LIKE ?";
        PreparedStatement preparedStatement = null;
        List<EmpleadoTB> empleadoTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, buscar + "%");
            preparedStatement.setString(2, buscar + "%");
            preparedStatement.setString(3, buscar + "%");
            try (ResultSet rsEmps = preparedStatement.executeQuery()) {
                while (rsEmps.next()) {
                    EmpleadoTB empleadoTB = new EmpleadoTB();
                    empleadoTB.setIdEmpleado(rsEmps.getString("IdEmpleado"));
                    empleadoTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                    empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                    empleadoTB.setNombres(rsEmps.getString("Nombres"));
                    empleadoTBs.add(empleadoTB);
                }
            }
        } catch (SQLException e) {

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {

            }
        }
        return empleadoTBs;
    }

}
