package model;

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
import static model.DBUtil.getConnection;

public class BancoADO {

    public static ObservableList<BancoTB> Listar_Bancos(String value) {
        String selectStmt = "{call Sp_Listar_Bancos(?)}";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<BancoTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                BancoTB bancoTB = new BancoTB();
                bancoTB.setId(rsEmps.getRow());
                bancoTB.setIdBanco(rsEmps.getString("IdBanco"));
                bancoTB.setNombreCuenta(rsEmps.getString("NombreCuenta"));
                bancoTB.setNumeroCuenta(rsEmps.getString("NumeroCuenta"));
                bancoTB.setSimboloMoneda(rsEmps.getString("Simbolo"));
                bancoTB.setSaldoInicial(rsEmps.getDouble("SaldoInicial"));
                bancoTB.setDescripcion(rsEmps.getString("Descripcion"));
                bancoTB.setSistema(rsEmps.getBoolean("Sistema"));
                bancoTB.setFormaPago(rsEmps.getShort("FormaPago"));
                bancoTB.setMostrar(rsEmps.getBoolean("Mostrar"));
                empList.add(bancoTB);
            }
        } catch (SQLException e) {
            System.out.println("Listar_Bancos - La operación de selección de SQL ha fallado: " + e);

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

    public static BancoTB Obtener_Banco_Por_Id(String value) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        BancoTB bancoTB = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("SELECT NombreCuenta,NumeroCuenta,IdMoneda,SaldoInicial,Descripcion,FormaPago,Mostrar FROM Banco WHERE IdBanco = ?");
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
        } catch (SQLException e) {
            System.out.println("Obtener_Banco_Por_Id - La operación de selección de SQL ha fallado: " + e);
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
        return bancoTB;
    }

    public static String Proceso_Banco(BancoTB bancoTB) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            CallableStatement codigoBanco = null;
            PreparedStatement preparedBanco = null;
            PreparedStatement preparedBancoHistorial = null;
            PreparedStatement preparedValidation = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                preparedValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM Banco WHERE IdBanco = ?");
                preparedValidation.setString(1, bancoTB.getIdBanco());
                if (preparedValidation.executeQuery().next()) {
                    preparedValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM Banco WHERE IdBanco <> ? and NombreCuenta = ?");
                    preparedValidation.setString(1, bancoTB.getIdBanco());
                    preparedValidation.setString(2, bancoTB.getNombreCuenta());
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        preparedBanco = DBUtil.getConnection().prepareStatement("UPDATE Banco SET NombreCuenta = ?,NumeroCuenta = ?,IdMoneda = ?,SaldoInicial = ?,Descripcion = ?,FormaPago = ?,Mostrar = ? WHERE IdBanco = ?");
                        preparedBanco.setString(1, bancoTB.getNombreCuenta());
                        preparedBanco.setString(2, bancoTB.getNumeroCuenta());
                        preparedBanco.setInt(3, bancoTB.getIdMoneda());
                        preparedBanco.setDouble(4, bancoTB.getSaldoInicial());
                        preparedBanco.setString(5, bancoTB.getDescripcion());
                        preparedBanco.setShort(6, bancoTB.getFormaPago());
                        preparedBanco.setBoolean(7, bancoTB.isMostrar());
                        preparedBanco.setString(8, bancoTB.getIdBanco());
                        preparedBanco.addBatch();

                        preparedBancoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");

                        if (bancoTB.getSaldoInicial() > 0) {
                            preparedBancoHistorial.setString(1, bancoTB.getIdBanco());
                            preparedBancoHistorial.setString(2, Session.USER_ID);
                            preparedBancoHistorial.setString(3, "");
                            preparedBancoHistorial.setString(4, "Apertura de cuenta");
                            preparedBancoHistorial.setString(5, Tools.getDate());
                            preparedBancoHistorial.setString(6, Tools.getTime());
                            preparedBancoHistorial.setDouble(7, bancoTB.getSaldoInicial());
                            preparedBancoHistorial.setDouble(8, 0);
                            preparedBancoHistorial.addBatch();
                        }

                        preparedBanco.executeBatch();
                        preparedBancoHistorial.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "updated";
                    }
                } else {
                    preparedValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM Banco WHERE NombreCuenta = ?");
                    preparedValidation.setString(1, bancoTB.getNombreCuenta());
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        codigoBanco = DBUtil.getConnection().prepareCall("{? = call Fc_Banco_Codigo_Alfanumerico()}");
                        codigoBanco.registerOutParameter(1, java.sql.Types.VARCHAR);
                        codigoBanco.execute();
                        String idBanco = codigoBanco.getString(1);

                        preparedBanco = DBUtil.getConnection().prepareStatement("INSERT INTO Banco (IdBanco,NombreCuenta,NumeroCuenta,IdMoneda,SaldoInicial,FechaCreacion,HoraCreacion,Descripcion,Sistema,FormaPago,Mostrar)VALUES(?,?,?,?,?,?,?,?,?,?,?)");
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

                        preparedBancoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");

                        if (bancoTB.getSaldoInicial() > 0) {
                            preparedBancoHistorial.setString(1, idBanco);
                            preparedBancoHistorial.setString(2, Session.USER_ID);
                            preparedBancoHistorial.setString(3, "");
                            preparedBancoHistorial.setString(4, "Apertura de cuenta");
                            preparedBancoHistorial.setString(5, Tools.getDate());
                            preparedBancoHistorial.setString(6, Tools.getTime());
                            preparedBancoHistorial.setDouble(7, bancoTB.getSaldoInicial());
                            preparedBancoHistorial.setDouble(8, 0);
                            preparedBancoHistorial.addBatch();
                        }

                        preparedBanco.executeBatch();
                        preparedBancoHistorial.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "inserted";
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
                    if (codigoBanco != null) {
                        codigoBanco.close();
                    }
                    if (preparedBanco != null) {
                        preparedBanco.close();
                    }
                    if (preparedBancoHistorial != null) {
                        preparedBancoHistorial.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer una conexión con el servidor, intente nuevamente.";
        }
        return result;
    }

    public static String Deleted_Banco(String idBanco) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedBanco = null;
            PreparedStatement preparedValidation = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                preparedValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM Banco WHERE IdBanco = ? AND Sistema = 1");
                preparedValidation.setString(1, idBanco);
                if (preparedValidation.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "sistema";
                } else {
                    preparedValidation = DBUtil.getConnection().prepareStatement("SELECT * FROM BancoHistorialTB WHERE IdBanco = ?");
                    preparedValidation.setString(1, idBanco);
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "historial";
                    } else {
                        preparedBanco = DBUtil.getConnection().prepareStatement("DELETE FROM Banco WHERE IdBanco = ?");
                        preparedBanco.setString(1, idBanco);
                        preparedBanco.addBatch();
                        preparedBanco.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "deleted";
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
                    if (preparedBanco != null) {
                        preparedBanco.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer una conexión con el servidor, intente nuevamente.";
        }
        return result;
    }

    public static List<BancoTB> GetBancoComboBox() {
        List<BancoTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = DBUtil.getConnection().prepareStatement("select IdBanco,NombreCuenta from Banco order by HoraCreacion ASC");
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    list.add(new BancoTB(resultSet.getString("IdBanco"), resultSet.getString("NombreCuenta").toUpperCase()));
                }
            } catch (SQLException ex) {
                System.out.println("Error¨Plazos: " + ex.getLocalizedMessage());
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    System.out.println("Error Plazos: " + ex.getLocalizedMessage());
                }
            }
        }
        return list;
    }

    public static List<BancoTB> GetBancoComboBoxForma(short forma) {
        List<BancoTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = DBUtil.getConnection().prepareStatement("select IdBanco,NombreCuenta from Banco where FormaPago = ? order by HoraCreacion ASC");
                statement.setShort(1, forma);
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    list.add(new BancoTB(resultSet.getString("IdBanco"), resultSet.getString("NombreCuenta").toUpperCase()));
                }
            } catch (SQLException ex) {
                System.out.println("Error¨Plazos: " + ex.getLocalizedMessage());
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    System.out.println("Error Plazos: " + ex.getLocalizedMessage());
                }
            }
        }
        return list;
    }

    public static ArrayList<Object> Listar_Bancos_Historial(String idBanco) {
        PreparedStatement preparedStatementBanco = null;
        PreparedStatement preparedStatementBancoHistorial = null;

        ArrayList<Object> arrayList = new ArrayList<>();
        ObservableList<BancoHistorialTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();

            preparedStatementBanco = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Total_Banco(?)}");
            preparedStatementBanco.setString(1, idBanco);
            ResultSet rsEmpsBanco = preparedStatementBanco.executeQuery();
            if (rsEmpsBanco.next()) {
                BancoTB bancoTB = new BancoTB();
                bancoTB.setSimboloMoneda(rsEmpsBanco.getString("Simbolo"));
                bancoTB.setSaldoInicial(rsEmpsBanco.getDouble("SaldoInicial"));
                arrayList.add(bancoTB);
            }

            preparedStatementBancoHistorial = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Banco_Historial(?)}");
            preparedStatementBancoHistorial.setString(1, idBanco);
            ResultSet rsEmpsBancoHistorial = preparedStatementBancoHistorial.executeQuery();

            while (rsEmpsBancoHistorial.next()) {
                BancoHistorialTB bancoHistorialTB = new BancoHistorialTB();
                bancoHistorialTB.setId(rsEmpsBancoHistorial.getRow());
                bancoHistorialTB.setIdBanco(rsEmpsBancoHistorial.getString("IdBanco"));
                bancoHistorialTB.setEmpleadoTB(new EmpleadoTB(rsEmpsBancoHistorial.getString("Empleado")));
                bancoHistorialTB.setDescripcion(rsEmpsBancoHistorial.getString("Descripcion").toUpperCase());
                bancoHistorialTB.setFecha(rsEmpsBancoHistorial.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                bancoHistorialTB.setHora(rsEmpsBancoHistorial.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
                bancoHistorialTB.setEntrada(rsEmpsBancoHistorial.getDouble("Entrada"));
                bancoHistorialTB.setSalida(rsEmpsBancoHistorial.getDouble("Salida"));
                empList.add(bancoHistorialTB);
            }
            arrayList.add(empList);
        } catch (SQLException e) {
            System.out.println("Listar_Bancos_Historial - La operación de selección de SQL ha fallado: " + e);

        } finally {
            try {
                if (preparedStatementBancoHistorial != null) {
                    preparedStatementBancoHistorial.close();
                }
                if (preparedStatementBanco != null) {
                    preparedStatementBanco.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return arrayList;
    }

    public static String Insertar_Dinero(BancoHistorialTB bancoHistorialTB) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedBanco = null;
            PreparedStatement preparedBancoHistorial = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);

                preparedBanco = getConnection().prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial + ? WHERE IdBanco = ?");
                preparedBanco.setDouble(1, bancoHistorialTB.getEntrada());
                preparedBanco.setString(2, bancoHistorialTB.getIdBanco());
                preparedBanco.addBatch();

                preparedBancoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");
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
                DBUtil.getConnection().commit();
                result = "inserted";
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
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
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer una conexión con el servidor, intente nuevamente.";
        }
        return result;
    }

    public static String Retirar_Dinero(BancoHistorialTB bancoHistorialTB) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedBanco = null;
            PreparedStatement preparedBancoHistorial = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);

                preparedBanco = getConnection().prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial - ? WHERE IdBanco = ?");
                preparedBanco.setDouble(1, bancoHistorialTB.getSalida());
                preparedBanco.setString(2, bancoHistorialTB.getIdBanco());
                preparedBanco.addBatch();

                preparedBancoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");
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
                DBUtil.getConnection().commit();
                result = "inserted";
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
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
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer una conexión con el servidor, intente nuevamente.";
        }
        return result;
    }

    public static boolean ValidarBanco(String idBanco, String NombreBanco) {
        boolean cajaValida = false;
        PreparedStatement statementBanco = null;
        try {
            DBUtil.dbConnect();
            statementBanco = DBUtil.getConnection().prepareStatement("SELECT * FROM Banco WHERE IdBanco = ? AND NombreCuenta = ?");
            statementBanco.setString(1, idBanco);
            statementBanco.setString(2, NombreBanco);
            try (ResultSet result = statementBanco.executeQuery()) {
                if (result.next()) {
                    cajaValida = true;
                }
            }
        } catch (SQLException ex) {
            cajaValida = false;
        } finally {
            try {
                if (statementBanco != null) {
                    statementBanco.close();
                }
            } catch (SQLException ex) {
            }
        }
        return cajaValida;
    }

    public static Object Listar_Banco_Mostrar() {
        PreparedStatement statementBanco = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            statementBanco = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "b.NombreCuenta,\n"
                    + "b.NumeroCuenta,\n"
                    + "m.Simbolo \n"
                    + "FROM Banco AS b INNER JOIN MonedaTB AS m ON m.IdMoneda = b.IdMoneda\n"
                    + "WHERE b.Mostrar = 1");
            resultSet = statementBanco.executeQuery();
            ArrayList<BancoTB> bancoTBs = new ArrayList();
            while (resultSet.next()) {
                BancoTB bancoTB = new BancoTB();
                bancoTB.setNombreCuenta(resultSet.getString("NombreCuenta"));
                bancoTB.setNumeroCuenta(resultSet.getString("NumeroCuenta"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                bancoTB.setMonedaTB(monedaTB);
                bancoTBs.add(bancoTB);
            }

            return bancoTBs;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementBanco != null) {
                    statementBanco.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
