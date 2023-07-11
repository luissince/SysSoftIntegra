package service;

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
import model.EmpleadoTB;

public class EmpleadoADO {

    public static String InsertEmpleado(EmpleadoTB empleadoTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement empleado = null;
        CallableStatement codigo_empleado = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigo_empleado = dbf.getConnection().prepareCall("{? = call Fc_Empleado_Codigo_Alfanumerico()}");
            codigo_empleado.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_empleado.execute();
            String id_empleado = codigo_empleado.getString(1);

            empleado = dbf.getConnection().prepareStatement("INSERT INTO EmpleadoTB( "
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

            dbf.getConnection().commit();
            return "register";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
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
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
    }

    public static String UpdateEmpleado(EmpleadoTB empleadoTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement empleado = null;
        CallableStatement codigo_empleado = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            empleado = dbf.getConnection().prepareStatement(
                    "UPDATE EmpleadoTB SET TipoDocumento = ?,NumeroDocumento = ?,Apellidos = ?,Nombres = ?,Sexo = ?,FechaNacimiento = ?,Puesto = ?,Rol = ?,Estado = ?,Telefono = ?,Celular = ?,Email = ?,Direccion = ?,Usuario = ?,Clave  = ? WHERE IdEmpleado = ?");

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

            dbf.getConnection().commit();
            return "update";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
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
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
    }

    public static Object ListEmpleados(int opcion, String buscar, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] object = new Object[2];
            ObservableList<EmpleadoTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Empleados(?,?,?,?)}");
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

            rsEmps.close();
            preparedStatementCount = dbf.getConnection().prepareStatement("{call Sp_Listar_Empleados_Count(?,?)}");
            preparedStatementCount.setInt(1, opcion);
            preparedStatementCount.setString(2, buscar);
            rsEmps = preparedStatementCount.executeQuery();
            Integer total = 0;
            if (rsEmps.next()) {
                total = rsEmps.getInt("Total");
            }
            object[1] = total;

            return object;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (preparedStatementCount != null) {
                    preparedStatementCount.close();
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

    public static EmpleadoTB GetByIdEmpleados(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        EmpleadoTB empleadoTB = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("SELECT * FROM EmpleadoTB WHERE IdEmpleado = ?");
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
        return empleadoTB;
    }

    public static Object iniciarSesion(String user, String clave) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{CALL Sp_Validar_Ingreso(?,?)}");
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, clave);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setIdEmpleado(rsEmps.getString("IdEmpleado"));
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                empleadoTB.setRolName(rsEmps.getString("RolName"));
                empleadoTB.setEstado(rsEmps.getInt("Estado"));
                empleadoTB.setRol(rsEmps.getInt("Rol"));
                return empleadoTB;
            }
            throw new Exception("No se encontro al cliente, intente nuevamente.");
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

    // public static void UpdateImage() {
    // PreparedStatement preparedUpdate = null;
    // PreparedStatement preparedStatement = null;
    // try {
    // preparedUpdate = dbf.getConnection().prepareStatement("UPDATE SuministroTB
    // SET NuevaImagen = ? WHERE IdSuministro = ?");
    //
    // preparedStatement = dbf.getConnection().prepareStatement("SELECT
    // IdSuministro,Imagen FROM SuministroTB where NuevaImagen is null");
    // ResultSet rsEmps = preparedStatement.executeQuery();
    // File selectFile = null;
    // while (rsEmps.next()) {
    // selectFile = new File(rsEmps.getString("Imagen"));
    // if (selectFile != null && selectFile.exists()) {
    // preparedUpdate.setBytes(1, Tools.getImageBytes(selectFile));
    // preparedUpdate.setString(2, rsEmps.getString("IdSuministro"));
    // preparedUpdate.execute();
    // Tools.println(Tools.getImageBytes(selectFile));
    // }
    // }
    //
    // Tools.println("bien");
    // } catch (SQLException ex) {
    // Tools.println(ex.getLocalizedMessage());
    //
    // }
    //
    // }
    public static ObservableList<EmpleadoTB> ListEmployeeInProProduction(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<EmpleadoTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Empleados(?)}");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setId(rsEmps.getInt("Filas"));
                // empleadoTB.setId(rsEmps.getRow());
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

    public static List<EmpleadoTB> Lista_Empleado_For_Compras() {
        DBUtil dbf = new DBUtil();
        List<EmpleadoTB> empleadoTBs = new ArrayList<>();

        PreparedStatement statementEmpleados = null;
        try {
            dbf.dbConnect();
            statementEmpleados = dbf.getConnection()
                    .prepareStatement("SELECT IdEmpleado,Apellidos,Nombres FROM EmpleadoTB ORDER BY Apellidos ASC");
            ResultSet resultSet = statementEmpleados.executeQuery();
            while (resultSet.next()) {
                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setIdEmpleado(resultSet.getString("IdEmpleado"));
                empleadoTB.setNombres(resultSet.getString("Apellidos").toUpperCase());
                empleadoTB.setApellidos(resultSet.getString("Nombres").toUpperCase());
                empleadoTBs.add(empleadoTB);
            }
        } catch (SQLException | ClassNotFoundException ex) {

        } finally {
            try {
                if (statementEmpleados != null) {
                    statementEmpleados.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

        return empleadoTBs;
    }

    public static String DeleteEmpleadoById(String idEmpleado) {
        DBUtil dbf = new DBUtil();
        String result = "";

        PreparedStatement statementValidation = null;
        PreparedStatement statementEmpleado = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidation = dbf.getConnection()
                    .prepareStatement("SELECT * FROM EmpleadoTB WHERE IdEmpleado = ? AND Sistema = 1");
            statementValidation.setString(1, idEmpleado);
            if (statementValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                result = "sistema";
            } else {
                statementValidation = dbf.getConnection().prepareCall("SELECT * FROM CajaTB WHERE IdUsuario = ?");
                statementValidation.setString(1, idEmpleado);
                if (statementValidation.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    result = "caja";
                } else {
                    statementValidation = dbf.getConnection().prepareCall("SELECT * FROM CompraTB WHERE Usuario = ?");
                    statementValidation.setString(1, idEmpleado);
                    if (statementValidation.executeQuery().next()) {
                        dbf.getConnection().rollback();
                        result = "compra";
                    } else {
                        statementEmpleado = dbf.getConnection()
                                .prepareStatement("DELETE FROM EmpleadoTB WHERE IdEmpleado = ?");
                        statementEmpleado.setString(1, idEmpleado);
                        statementEmpleado.addBatch();
                        statementEmpleado.executeBatch();
                        dbf.getConnection().commit();
                        result = "deleted";
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                result = ex.getLocalizedMessage();
                dbf.getConnection().rollback();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static ArrayList<String> CountSuministros(long sleep) throws InterruptedException {
        DBUtil dbf = new DBUtil();
        ArrayList<String> arrayList = new ArrayList<>();

        PreparedStatement statementValidation = null;
        try {
            dbf.dbConnect();
            statementValidation = dbf.getConnection().prepareStatement("SELECT * FROM SuministroTB");
            ResultSet resultSet = statementValidation.executeQuery();
            Thread.sleep(sleep);
            while (resultSet.next()) {
                arrayList.add(resultSet.getString("IdSuministro"));
            }
        } catch (SQLException | ClassNotFoundException ex) {
        } finally {
            try {
                if (statementValidation != null) {
                    statementValidation.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

        return arrayList;
    }

    public static List<EmpleadoTB> getSearchComboBoxEmpleados(String buscar) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        List<EmpleadoTB> empleadoTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("SELECT "
                    + "IdEmpleado,"
                    + "NumeroDocumento,"
                    + "Apellidos,"
                    + "Nombres "
                    + "FROM EmpleadoTB "
                    + "WHERE ? = '' "
                    + "OR NumeroDocumento LIKE ? "
                    + "OR Apellidos LIKE ? "
                    + "OR Nombres LIKE ?");
            preparedStatement.setString(1, buscar);
            preparedStatement.setString(2, buscar + "%");
            preparedStatement.setString(3, buscar + "%");
            preparedStatement.setString(4, buscar + "%");
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
        } catch (SQLException | ClassNotFoundException e) {

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {

            }
        }
        return empleadoTBs;
    }

}
