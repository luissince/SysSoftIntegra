package service;

import controller.tools.Session;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.BancoHistorialTB;
import model.BancoTB;
import model.EmpleadoTB;
import model.MonedaTB;
import model.RolTB;

public class BancoADO {

    public static Object listarBancos(String buscar, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] object = new Object[2];
            ObservableList<BancoTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Bancos(?,?,?)}");
            preparedStatement.setString(1, buscar);
            preparedStatement.setInt(2, posicionPagina);
            preparedStatement.setInt(3, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                BancoTB bancoTB = new BancoTB();
                bancoTB.setId(rsEmps.getRow() + posicionPagina);
                bancoTB.setIdBanco(rsEmps.getString("IdBanco"));
                bancoTB.setNombreCuenta(rsEmps.getString("NombreCuenta"));
                bancoTB.setNumeroCuenta(rsEmps.getString("NumeroCuenta"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                bancoTB.setMonedaTB(monedaTB);

                bancoTB.setSaldoInicial(rsEmps.getDouble("SaldoInicial"));
                bancoTB.setDescripcion(rsEmps.getString("Descripcion"));
                bancoTB.setSistema(rsEmps.getBoolean("Sistema"));
                bancoTB.setFormaPago(rsEmps.getShort("FormaPago"));
                bancoTB.setMostrar(rsEmps.getBoolean("Mostrar"));
                empList.add(bancoTB);
            }
            object[0] = empList;

            rsEmps.close();
            preparedStatementCount = dbf.getConnection().prepareStatement("{call Sp_Listar_Bancos_Count(?)}");
            preparedStatementCount.setString(1, buscar);
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

    public static BancoTB obtenerBancoPorId(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        BancoTB bancoTB = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement(
                    "SELECT NombreCuenta,NumeroCuenta,IdMoneda,SaldoInicial,Descripcion,FormaPago,Mostrar FROM Banco WHERE IdBanco = ?");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                bancoTB = new BancoTB();
                bancoTB.setNombreCuenta(rsEmps.getString("NombreCuenta"));
                bancoTB.setNumeroCuenta(rsEmps.getString("NumeroCuenta"));
                bancoTB.setIdMoneda(rsEmps.getInt("IdMoneda"));
                bancoTB.setSaldoInicial(rsEmps.getDouble("SaldoInicial"));
                bancoTB.setDescripcion(rsEmps.getString("Descripcion"));
                bancoTB.setFormaPago(rsEmps.getShort("FormaPago"));
                bancoTB.setMostrar(rsEmps.getBoolean("Mostrar"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Obtener_Banco_Por_Id - La operación de selección de SQL ha fallado: " + e);
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
        return bancoTB;
    }

    public static String registrarBanco(BancoTB bancoTB) {
        // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

        // Objectos que realizan las peticiones T-SQL
        CallableStatement codigoBanco = null;
        PreparedStatement preparedBanco = null;
        PreparedStatement preparedValidation = null;
        try {
            // Crea la conexión a la base de datos y se inicia el commit
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Se valida que el número de cuenta no tengo el mismo nombre de otros
            preparedValidation = dbf.getConnection().prepareStatement("SELECT * FROM Banco "
                    + "WHERE NombreCuenta = ?");
            preparedValidation.setString(1, bancoTB.getNombreCuenta());
            if (preparedValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "duplicate";
            }

            // Generar el código único del banco
            codigoBanco = dbf.getConnection().prepareCall("{? = call Fc_Banco_Codigo_Alfanumerico()}");
            codigoBanco.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoBanco.execute();
            String idBanco = codigoBanco.getString(1);

            // Procedimiento para registrar un nuevo banco
            preparedBanco = dbf.getConnection().prepareStatement("INSERT INTO Banco "
                    + "(IdBanco,"
                    + "NombreCuenta,"
                    + "NumeroCuenta,"
                    + "IdMoneda,"
                    + "SaldoInicial,"
                    + "FechaCreacion,"
                    + "HoraCreacion,"
                    + "Descripcion,"
                    + "Sistema,"
                    + "FormaPago,"
                    + "Mostrar)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
            preparedBanco.setString(1, idBanco);
            preparedBanco.setString(2, bancoTB.getNombreCuenta());
            preparedBanco.setString(3, bancoTB.getNumeroCuenta());
            preparedBanco.setInt(4, bancoTB.getIdMoneda());
            preparedBanco.setDouble(5, bancoTB.getSaldoInicial());
            preparedBanco.setString(6, bancoTB.getFecha());
            preparedBanco.setString(7, bancoTB.getHora());
            preparedBanco.setString(8, bancoTB.getDescripcion());
            preparedBanco.setBoolean(9, false);
            preparedBanco.setShort(10, bancoTB.getFormaPago());
            preparedBanco.setBoolean(11, bancoTB.isMostrar());
            preparedBanco.addBatch();

            // Ejecutar y guardar la consulta
            preparedBanco.executeBatch();
            dbf.getConnection().commit();
            return "inserted";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (codigoBanco != null) {
                    codigoBanco.close();
                }
                if (preparedBanco != null) {
                    preparedBanco.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String actualizarBanco(BancoTB bancoTB) {
        // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

        // Objectos que realizan las peticiones T-SQL
        CallableStatement codigoBanco = null;
        PreparedStatement preparedBanco = null;
        PreparedStatement preparedValidation = null;
        try {
            // Crea la conexión a la base de datos
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Se ha enviado un id incorrecto
            preparedValidation = dbf.getConnection().prepareStatement("SELECT * FROM Banco WHERE IdBanco = ?");
            preparedValidation.setString(1, bancoTB.getIdBanco());
            if (!preparedValidation.executeQuery().next()) {
                return "noid";
            }

            // Se valida que el nombre no exista
            preparedValidation.close();
            preparedValidation = dbf.getConnection().prepareStatement("SELECT * FROM Banco "
                    + "WHERE IdBanco <> ? and NombreCuenta = ?");
            preparedValidation.setString(1, bancoTB.getIdBanco());
            preparedValidation.setString(2, bancoTB.getNombreCuenta());
            if (preparedValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "duplicate";
            }

            // Proceso para actualizar el banco
            preparedBanco = dbf.getConnection().prepareStatement("UPDATE Banco "
                    + "SET NombreCuenta = ?,"
                    + "NumeroCuenta = ?,"
                    + "IdMoneda = ?,"
                    + "SaldoInicial = ?,"
                    + "Descripcion = ?,"
                    + "FormaPago = ?,"
                    + "Mostrar = ? "
                    + "WHERE IdBanco = ?");
            preparedBanco.setString(1, bancoTB.getNombreCuenta());
            preparedBanco.setString(2, bancoTB.getNumeroCuenta());
            preparedBanco.setInt(3, bancoTB.getIdMoneda());
            preparedBanco.setDouble(4, bancoTB.getSaldoInicial());
            preparedBanco.setString(5, bancoTB.getDescripcion());
            preparedBanco.setShort(6, bancoTB.getFormaPago());
            preparedBanco.setBoolean(7, bancoTB.isMostrar());
            preparedBanco.setString(8, bancoTB.getIdBanco());
            preparedBanco.addBatch();

            // Ejecuta y guarda el proceso
            preparedBanco.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (codigoBanco != null) {
                    codigoBanco.close();
                }
                if (preparedBanco != null) {
                    preparedBanco.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String removerBanco(String idBanco) {
          // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

          // Objectos que realizan las peticiones T-SQL
        PreparedStatement preparedBanco = null;
        PreparedStatement preparedValidation = null;
        try {
            // Crea la conexión a la base de datos y iniciar la transacción
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Validar si la cuenta bancaría es propia del sistema
            preparedValidation = dbf.getConnection()
                    .prepareStatement("SELECT * FROM Banco WHERE IdBanco = ? AND Sistema = 1");
            preparedValidation.setString(1, idBanco);
            if (preparedValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "sistema";
            }

            // Validar si la cuenta banciar esta sujeta a un historial bancario
            preparedValidation.close();
            preparedValidation = dbf.getConnection()
                    .prepareStatement("SELECT * FROM BancoHistorialTB WHERE IdBanco = ?");
            preparedValidation.setString(1, idBanco);
            if (preparedValidation.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "historial";
            }

            // Proceso para la eliminación
            preparedBanco = dbf.getConnection().prepareStatement("DELETE FROM Banco WHERE IdBanco = ?");
            preparedBanco.setString(1, idBanco);
            preparedBanco.addBatch();

            // Ejecuta y procesa los scrips
            preparedBanco.executeBatch();
            dbf.getConnection().commit();
            return "deleted";

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedBanco != null) {
                    preparedBanco.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static List<BancoTB> GetBancoComboBox() {
        DBUtil dbf = new DBUtil();
        List<BancoTB> list = new ArrayList<>();

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statement = dbf.getConnection()
                    .prepareStatement("select IdBanco,NombreCuenta from Banco order by HoraCreacion ASC");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                list.add(
                        new BancoTB(resultSet.getString("IdBanco"), resultSet.getString("NombreCuenta").toUpperCase()));
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Error¨Plazos: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                System.out.println("Error Plazos: " + ex.getLocalizedMessage());
            }
        }

        return list;
    }

    public static Object ObtenerListarBancos() {
        DBUtil dbf = new DBUtil();

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statement = dbf.getConnection().prepareStatement(
                    "SELECT IdBanco,NombreCuenta,FormaPago FROM Banco");
            resultSet = statement.executeQuery();
            ObservableList<BancoTB> empList = FXCollections.observableArrayList();
            while (resultSet.next()) {
                BancoTB bancoTB = new BancoTB();
                bancoTB.setIdBanco(resultSet.getString("IdBanco"));
                bancoTB.setNombreCuenta(resultSet.getString("NombreCuenta"));
                bancoTB.setFormaPago(resultSet.getShort("FormaPago"));
                empList.add(bancoTB);
            }
            return empList;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object obtenerBancoHistorial(String idBanco, int posicionPagina, int filasPorPagina) {
        // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

        // Objectos que realizan las peticiones T-SQL
        PreparedStatement preparedStatementBancoSumados = null;
        PreparedStatement preparedStatementBancoHistorial = null;
        PreparedStatement preparedStatementBancoHistorialCount = null;

        try {
            // Crea la conexión a la base de datos
            dbf.dbConnect();

            // El objeto que retorna la respuesta del proceso
            Object[] objects = new Object[3];

            // Obtenemos la lista del historial bancario
            preparedStatementBancoHistorial = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Banco_Historial(?,?,?)}");
            preparedStatementBancoHistorial.setString(1, idBanco);
            preparedStatementBancoHistorial.setInt(2, posicionPagina);
            preparedStatementBancoHistorial.setInt(3, filasPorPagina);
            ResultSet rsEmpsBancoHistorial = preparedStatementBancoHistorial.executeQuery();
            ObservableList<BancoHistorialTB> bancoHistorialTBs = FXCollections.observableArrayList();
            while (rsEmpsBancoHistorial.next()) {
                BancoHistorialTB bancoHistorialTB = new BancoHistorialTB();
                bancoHistorialTB.setId(rsEmpsBancoHistorial.getRow());
                bancoHistorialTB.setIdBanco(rsEmpsBancoHistorial.getString("IdBanco"));
                bancoHistorialTB.setDescripcion(rsEmpsBancoHistorial.getString("Descripcion").toUpperCase());
                bancoHistorialTB.setFecha(rsEmpsBancoHistorial.getDate("Fecha").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                bancoHistorialTB.setHora(rsEmpsBancoHistorial.getTime("Hora").toLocalTime()
                        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
                bancoHistorialTB.setEstado(rsEmpsBancoHistorial.getBoolean("Estado"));
                bancoHistorialTB.setEntrada(rsEmpsBancoHistorial.getDouble("Entrada"));
                bancoHistorialTB.setSalida(rsEmpsBancoHistorial.getDouble("Salida"));

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(rsEmpsBancoHistorial.getString("Apellidos"));
                empleadoTB.setNombres(rsEmpsBancoHistorial.getString("Nombres"));
                RolTB rolTB = new RolTB();
                rolTB.setNombre(rsEmpsBancoHistorial.getString("Rol"));
                empleadoTB.setRolTB(rolTB);
                bancoHistorialTB.setEmpleadoTB(empleadoTB);

                bancoHistorialTBs.add(bancoHistorialTB);
            }
            rsEmpsBancoHistorial.close();

            // Obtenemos el total de registros
            preparedStatementBancoHistorialCount = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Banco_Historial_Count(?)}");
            preparedStatementBancoHistorialCount.setString(1, idBanco);
            ResultSet rsEmpsBancoHistorialCount = preparedStatementBancoHistorialCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmpsBancoHistorialCount.next()) {
                cantidadTotal = rsEmpsBancoHistorialCount.getInt("Total");
            }
            rsEmpsBancoHistorialCount.close();

            // Obtenemos la suma total de entradas y salidas
            preparedStatementBancoSumados = dbf.getConnection()
                    .prepareStatement("{call Sp_Sumar_Bancos_Realizados(?)}");
            preparedStatementBancoSumados.setString(1, idBanco);
            ResultSet rsEmpsBancoSumado = preparedStatementBancoSumados.executeQuery();
            String sumaTotal = "M 00.00";
            if (rsEmpsBancoSumado.next()) {
                sumaTotal = rsEmpsBancoSumado.getString("Simbolo") + " "
                        + Tools.roundingValue(rsEmpsBancoSumado.getDouble("SaldoInicial"), 2);
            }
            rsEmpsBancoSumado.close();

            //
            objects[0] = bancoHistorialTBs;
            objects[1] = cantidadTotal;
            objects[2] = sumaTotal;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatementBancoHistorial != null) {
                    preparedStatementBancoHistorial.close();
                }
                if (preparedStatementBancoSumados != null) {
                    preparedStatementBancoSumados.close();
                }
                if (preparedStatementBancoHistorialCount != null) {
                    preparedStatementBancoHistorialCount.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String Insertar_Dinero(BancoHistorialTB bancoHistorialTB) {
        DBUtil dbf = new DBUtil();
        String result = "";

        PreparedStatement preparedBanco = null;
        PreparedStatement preparedBancoHistorial = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedBanco = dbf.getConnection()
                    .prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial + ? WHERE IdBanco = ?");
            preparedBanco.setDouble(1, bancoHistorialTB.getEntrada());
            preparedBanco.setString(2, bancoHistorialTB.getIdBanco());
            preparedBanco.addBatch();

            preparedBancoHistorial = dbf.getConnection().prepareStatement(
                    "INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");
            preparedBancoHistorial.setString(1, bancoHistorialTB.getIdBanco());
            preparedBancoHistorial.setString(2, Session.USER_ID);
            preparedBancoHistorial.setString(3, "");
            preparedBancoHistorial.setString(4, bancoHistorialTB.getDescripcion());
            preparedBancoHistorial.setString(5, bancoHistorialTB.getFecha());
            preparedBancoHistorial.setString(6, bancoHistorialTB.getHora());
            preparedBancoHistorial.setDouble(7, bancoHistorialTB.getEntrada());
            preparedBancoHistorial.setDouble(8, 0);
            preparedBancoHistorial.addBatch();

            preparedBanco.executeBatch();
            preparedBancoHistorial.executeBatch();
            dbf.getConnection().commit();
            result = "inserted";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedBanco != null) {
                    preparedBanco.close();
                }
                if (preparedBancoHistorial != null) {
                    preparedBancoHistorial.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static String Retirar_Dinero(BancoHistorialTB bancoHistorialTB) {
        DBUtil dbf = new DBUtil();
        String result = "";

        PreparedStatement preparedBanco = null;
        PreparedStatement preparedBancoHistorial = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedBanco = dbf.getConnection()
                    .prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial - ? WHERE IdBanco = ?");
            preparedBanco.setDouble(1, bancoHistorialTB.getSalida());
            preparedBanco.setString(2, bancoHistorialTB.getIdBanco());
            preparedBanco.addBatch();

            preparedBancoHistorial = dbf.getConnection().prepareStatement(
                    "INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");
            preparedBancoHistorial.setString(1, bancoHistorialTB.getIdBanco());
            preparedBancoHistorial.setString(2, Session.USER_ID);
            preparedBancoHistorial.setString(3, "");
            preparedBancoHistorial.setString(4, bancoHistorialTB.getDescripcion());
            preparedBancoHistorial.setString(5, bancoHistorialTB.getFecha());
            preparedBancoHistorial.setString(6, bancoHistorialTB.getHora());
            preparedBancoHistorial.setDouble(7, 0);
            preparedBancoHistorial.setDouble(8, bancoHistorialTB.getSalida());
            preparedBancoHistorial.addBatch();

            preparedBanco.executeBatch();
            preparedBancoHistorial.executeBatch();
            dbf.getConnection().commit();
            result = "inserted";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedBanco != null) {
                    preparedBanco.close();
                }
                if (preparedBancoHistorial != null) {
                    preparedBancoHistorial.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static boolean ValidarBanco(String idBanco, String NombreBanco) {
        DBUtil dbf = new DBUtil();
        boolean cajaValida = false;
        PreparedStatement statementBanco = null;
        try {
            dbf.dbConnect();
            statementBanco = dbf.getConnection()
                    .prepareStatement("SELECT * FROM Banco WHERE IdBanco = ? AND NombreCuenta = ?");
            statementBanco.setString(1, idBanco);
            statementBanco.setString(2, NombreBanco);
            try (ResultSet result = statementBanco.executeQuery()) {
                if (result.next()) {
                    cajaValida = true;
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            cajaValida = false;
        } finally {
            try {
                if (statementBanco != null) {
                    statementBanco.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
            }
        }
        return cajaValida;
    }

    public static String Listar_Banco_Mostrar() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementBanco = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementBanco = dbf.getConnection().prepareStatement("SELECT \n"
                    + "b.NombreCuenta,\n"
                    + "b.NumeroCuenta,\n"
                    + "m.Simbolo \n"
                    + "FROM Banco AS b INNER JOIN MonedaTB AS m ON m.IdMoneda = b.IdMoneda\n"
                    + "WHERE b.Mostrar = 1");
            resultSet = statementBanco.executeQuery();
            String cuentasBancos = "";
            while (resultSet.next()) {
                cuentasBancos += resultSet.getString("NombreCuenta") + " " + resultSet.getString("Simbolo") + " N° "
                        + resultSet.getString("NumeroCuenta") + "\n";
            }

            return cuentasBancos;
        } catch (SQLException | ClassNotFoundException ex) {
            return "";
        } finally {
            try {
                if (statementBanco != null) {
                    statementBanco.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return "";
            }
        }
    }

}
