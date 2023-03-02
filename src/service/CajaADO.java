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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import model.BancoHistorialTB;
import model.CajaTB;
import model.EmpleadoTB;
import model.IngresoTB;
import model.MovimientoCajaTB;

public class CajaADO {

    public static String AperturarCaja(CajaTB cajaTB, MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();
        String result = "";
        CallableStatement statementCodigoCaja = null;
        PreparedStatement statementCaja = null;
        PreparedStatement statementMovimientoCaja = null;

        try {
            dbf.dbConnect();

            dbf.getConnection().setAutoCommit(false);

            statementCodigoCaja = dbf.getConnection().prepareCall("{? = call Fc_Caja_Codigo_Alfanumerico()}");
            statementCodigoCaja.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigoCaja.execute();
            String idCaja = statementCodigoCaja.getString(1);

            statementCaja = dbf.getConnection().prepareStatement("INSERT INTO CajaTB("
                    + "IdCaja,"
                    + "FechaApertura,"
                    + "FechaCierre,"
                    + "HoraApertura,"
                    + "HoraCierre,"
                    + "Estado,"
                    + "Contado,"
                    + "Calculado,"
                    + "Diferencia,"
                    + "IdUsuario)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");
            statementCaja.setString(1, idCaja);
            statementCaja.setString(2, cajaTB.getFechaApertura());
            statementCaja.setString(3, cajaTB.getFechaApertura());
            statementCaja.setString(4, cajaTB.getHoraApertura());
            statementCaja.setString(5, cajaTB.getHoraApertura());
            statementCaja.setBoolean(6, cajaTB.isEstado());
            statementCaja.setDouble(7, cajaTB.getContado());
            statementCaja.setDouble(8, cajaTB.getCalculado());
            statementCaja.setDouble(9, cajaTB.getDiferencia());
            statementCaja.setString(10, cajaTB.getIdUsuario());
            statementCaja.addBatch();

            statementMovimientoCaja = dbf.getConnection().prepareStatement("INSERT INTO MovimientoCajaTB("
                    + "IdCaja,"
                    + "FechaMovimiento,"
                    + "HoraMovimiento,"
                    + "Comentario,"
                    + "TipoMovimiento,"
                    + "Monto)VALUES(?,?,?,?,?,?)");
            statementMovimientoCaja.setString(1, idCaja);
            statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
            statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
            statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario());
            statementMovimientoCaja.setShort(5, (short) 1);
            statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
            statementMovimientoCaja.addBatch();

            statementCaja.executeBatch();
            statementMovimientoCaja.executeBatch();
            dbf.getConnection().commit();
            Session.CAJA_ID = idCaja;
            result = "registrado";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                result = ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (statementCodigoCaja != null) {
                    statementCodigoCaja.close();
                }
                if (statementCaja != null) {
                    statementCaja.close();
                }

                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }

                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static Object ValidarCreacionCaja(String idUsuario) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
            statementValidar.setString(1, idUsuario);

            if (statementValidar.executeQuery().next()) {
                statementValidar.close();
                statementValidar = dbf.getConnection().prepareStatement(
                        "SELECT IdCaja,FechaApertura,HoraApertura FROM CajaTB WHERE IdUsuario = ? AND Estado = 1 AND CAST(FechaApertura AS DATE) = CAST(GETDATE() AS DATE)");
                statementValidar.setString(1, idUsuario);

                if (statementValidar.executeQuery().next()) {
                    resultSet = statementValidar.executeQuery();
                    resultSet.next();
                    CajaTB cajaTB = new CajaTB();
                    cajaTB.setId(2);
                    cajaTB.setIdCaja(resultSet.getString("IdCaja"));
                    cajaTB.setFechaApertura(resultSet.getDate("FechaApertura").toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    cajaTB.setHoraApertura(resultSet.getTime("HoraApertura").toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm a")));
                    return cajaTB;
                }

                statementValidar.close();
                statementValidar = dbf.getConnection().prepareStatement(
                        "SELECT IdCaja,FechaApertura,HoraApertura FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
                statementValidar.setString(1, idUsuario);
                resultSet = statementValidar.executeQuery();
                resultSet.next();
                CajaTB cajaTB = new CajaTB();
                cajaTB.setId(3);
                cajaTB.setIdCaja(resultSet.getString("IdCaja"));
                cajaTB.setFechaApertura(resultSet.getDate("FechaApertura").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cajaTB.setHoraApertura(
                        resultSet.getTime("HoraApertura").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));

                return cajaTB;
            }

            CajaTB cajaTB = new CajaTB();
            cajaTB.setId(1);
            cajaTB.setIdCaja("");
            return cajaTB;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
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

    public static Object ValidarAperturaCajaParaCerrar(String idUsuario) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementMovimientoBase = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
            statementValidar.setString(1, idUsuario);

            if (statementValidar.executeQuery().next()) {

                statementValidar.close();
                statementValidar = dbf.getConnection().prepareStatement("SELECT \n"
                        + "IdCaja, \n"
                        + "FechaApertura, \n"
                        + "HoraApertura \n"
                        + "FROM CajaTB \n"
                        + "WHERE IdUsuario = ? AND Estado = 1 AND CAST(FechaApertura AS DATE) = CAST(GETDATE() AS DATE)");
                statementValidar.setString(1, idUsuario);
                if (statementValidar.executeQuery().next()) {
                    resultSet = statementValidar.executeQuery();
                    if (resultSet.next()) {
                        ArrayList<Object> objects = new ArrayList<>();
                        CajaTB cajaTB = new CajaTB();
                        cajaTB.setId(2);
                        cajaTB.setIdCaja(resultSet.getString("IdCaja"));
                        cajaTB.setFechaApertura(resultSet.getDate("FechaApertura").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        cajaTB.setHoraApertura(resultSet.getTime("HoraApertura").toLocalTime()
                                .format(DateTimeFormatter.ofPattern("hh:mm a")));

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "Monto \n"
                                + "FROM MovimientoCajaTB \n"
                                + "WHERE IdCaja = ? AND TipoMovimiento = 1");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double apertura = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                apertura += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Monto"), 2));
                            }
                            cajaTB.setContado(apertura);
                        }

                        ArrayList<Double> arrayList = new ArrayList<>();

                        statementMovimientoBase.close();
                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS VentaEfectivo \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 2 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaEfectivo += Double.parseDouble(
                                        Tools.roundingValue(setMovimientoBase.getDouble("VentaEfectivo"), 2));
                            }
                            arrayList.add(ventaEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS VentaTarjeta \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 3 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaTarjeta += Double.parseDouble(
                                        Tools.roundingValue(setMovimientoBase.getDouble("VentaTarjeta"), 2));
                            }
                            arrayList.add(ventaTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Deposito \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 6 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Deposito"), 2));
                            }
                            arrayList.add(ventaDeposito);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Ingresos \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 4 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoEfectivo += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Ingresos \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 7 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoTarjeta += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Ingresos \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 8 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoDeposito);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Salidas \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 5 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaEfectivo += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Salidas \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 9 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaTarjeta += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement("SELECT \n"
                                + "m.Monto AS Salidas \n"
                                + "FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 10 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaDeposito);
                        }
                        objects.add(cajaTB);
                        objects.add(arrayList);
                        return objects;
                    } else {
                        return "No hay registros para mostrar datos.";
                    }

                } else {
                    statementValidar = dbf.getConnection().prepareStatement(
                            "SELECT IdCaja,FechaApertura,HoraApertura FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
                    statementValidar.setString(1, idUsuario);
                    resultSet = statementValidar.executeQuery();
                    if (resultSet.next()) {
                        ArrayList<Object> objects = new ArrayList<>();
                        CajaTB cajaTB = new CajaTB();
                        cajaTB.setId(3);
                        cajaTB.setIdCaja(resultSet.getString("IdCaja"));
                        cajaTB.setFechaApertura(resultSet.getDate("FechaApertura").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        cajaTB.setHoraApertura(resultSet.getTime("HoraApertura").toLocalTime()
                                .format(DateTimeFormatter.ofPattern("hh:mm a")));

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT Monto FROM MovimientoCajaTB WHERE IdCaja = ? AND TipoMovimiento = 1");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double apertura = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                apertura += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Monto"), 2));
                            }
                            cajaTB.setContado(apertura);
                        }

                        ArrayList<Double> arrayList = new ArrayList<>();

                        statementMovimientoBase.close();
                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS VentaEfectivo FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 2 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaEfectivo += Double.parseDouble(
                                        Tools.roundingValue(setMovimientoBase.getDouble("VentaEfectivo"), 2));
                            }
                            arrayList.add(ventaEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS VentaTarjeta FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 3 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaTarjeta += Double.parseDouble(
                                        Tools.roundingValue(setMovimientoBase.getDouble("VentaTarjeta"), 2));
                            }
                            arrayList.add(ventaTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Deposito FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 6 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ventaDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ventaDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Deposito"), 2));
                            }
                            arrayList.add(ventaDeposito);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Ingresos FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 4 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoEfectivo += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Ingresos FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 7 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoTarjeta += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Ingresos FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 8 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double ingresoDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                ingresoDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                            }
                            arrayList.add(ingresoDeposito);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Salidas FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 5 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaEfectivo = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaEfectivo += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaEfectivo);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Salidas FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 9 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaTarjeta = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaTarjeta += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaTarjeta);
                        }

                        statementMovimientoBase = dbf.getConnection().prepareStatement(
                                "SELECT m.Monto AS Salidas FROM MovimientoCajaTB AS m \n"
                                + "LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta \n"
                                + "WHERE m.IdCaja = ? AND m.TipoMovimiento = 10 AND v.IdNotaCredito IS NULL");
                        statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                        double salidaDeposito = 0;
                        try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                            while (setMovimientoBase.next()) {
                                salidaDeposito += Double
                                        .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                            }
                            arrayList.add(salidaDeposito);
                        }
                        objects.add(cajaTB);
                        objects.add(arrayList);
                        return objects;
                    } else {
                        return "No hay registros para mostrar datos.";
                    }
                }
            } else {
                return "No tiene ninguna caja aperturada.";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementMovimientoBase != null) {
                    statementMovimientoBase.close();
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

    public static Object ListarMovimientoPorById(String idCaja) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementMovimiento = null;
        PreparedStatement statementMovimientoBase = null;
        ResultSet resultSet = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[3];

            ArrayList<Double> arrayTotales = new ArrayList<>();
            ArrayList<MovimientoCajaTB> arratyLista = new ArrayList<>();

            statementValidar = dbf.getConnection().prepareStatement("{call Sp_Obtener_Caja_Aperturada_By_Id(?)}");
            statementValidar.setString(1, idCaja);
            resultSet = statementValidar.executeQuery();
            if (resultSet.next()) {
                CajaTB cajaTB = new CajaTB();
                cajaTB.setIdCaja(resultSet.getString("IdCaja"));
                cajaTB.setFechaApertura(resultSet.getDate("FechaApertura").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cajaTB.setHoraApertura(
                        resultSet.getTime("HoraApertura").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                cajaTB.setFechaCierre(resultSet.getDate("FechaCierre").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cajaTB.setHoraCierre(
                        resultSet.getTime("HoraCierre").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                cajaTB.setContado(resultSet.getDouble("Contado"));
                cajaTB.setCalculado(resultSet.getDouble("Calculado"));
                cajaTB.setDiferencia(resultSet.getDouble("Diferencia"));
                cajaTB.setEmpleadoTB(new EmpleadoTB(resultSet.getString("NumeroDocumento"),
                        resultSet.getString("Apellidos"), resultSet.getString("Nombres"),
                        resultSet.getString("Celular"), resultSet.getString("Direccion")));

                statementMovimientoBase = dbf.getConnection()
                        .prepareStatement("SELECT Monto FROM MovimientoCajaTB WHERE IdCaja = ? AND TipoMovimiento = 1");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double apertura = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        apertura += Double.parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Monto"), 2));
                    }
                    arrayTotales.add(apertura);
                }
                //
                statementMovimientoBase.close();
                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS VentaEfectivo FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE m.IdCaja = ? AND m.TipoMovimiento = 2 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ventaEfectivo = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ventaEfectivo += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("VentaEfectivo"), 2));
                    }
                    arrayTotales.add(ventaEfectivo);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS VentaTarjeta FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 3 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ventaTarjeta = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ventaTarjeta += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("VentaTarjeta"), 2));
                    }
                    arrayTotales.add(ventaTarjeta);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Ingresos  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 6 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ventaDeposito = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ventaDeposito += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                    }
                    arrayTotales.add(ventaDeposito);
                }

                //
                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Ingresos  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 4 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ingresoEfectivo = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ingresoEfectivo += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                    }
                    arrayTotales.add(ingresoEfectivo);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Ingresos  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 7 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ingresoTarjeta = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ingresoTarjeta += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                    }
                    arrayTotales.add(ingresoTarjeta);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Ingresos  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 8 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double ingresoDeposito = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        ingresoDeposito += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Ingresos"), 2));
                    }
                    arrayTotales.add(ingresoDeposito);
                }

                //
                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Salidas  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 5 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double salidaEfectivo = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        salidaEfectivo += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                    }
                    arrayTotales.add(salidaEfectivo);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Salidas  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 9 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double salidaTarjeta = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        salidaTarjeta += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                    }
                    arrayTotales.add(salidaTarjeta);
                }

                statementMovimientoBase = dbf.getConnection().prepareStatement(
                        "SELECT m.Monto AS Salidas  FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE IdCaja = ? AND TipoMovimiento = 10 AND v.IdNotaCredito is null");
                statementMovimientoBase.setString(1, cajaTB.getIdCaja());
                double salidaDeposito = 0;
                try (ResultSet setMovimientoBase = statementMovimientoBase.executeQuery()) {
                    while (setMovimientoBase.next()) {
                        salidaDeposito += Double
                                .parseDouble(Tools.roundingValue(setMovimientoBase.getDouble("Salidas"), 2));
                    }
                    arrayTotales.add(salidaDeposito);
                }

                //
                statementMovimiento = dbf.getConnection().prepareStatement(
                        "SELECT m.FechaMovimiento,m.HoraMovimiento,m.Comentario,m.TipoMovimiento,m.Monto FROM MovimientoCajaTB AS m LEFT JOIN NotaCreditoTB AS v ON m.IdProcedencia = v.IdVenta WHERE m.IdCaja = ? AND v.IdNotaCredito IS NULL");
                statementMovimiento.setString(1, idCaja);
                try (ResultSet resultSetMovimiento = statementMovimiento.executeQuery()) {
                    while (resultSetMovimiento.next()) {
                        MovimientoCajaTB movimientoCajaTB = new MovimientoCajaTB();
                        movimientoCajaTB.setId(resultSetMovimiento.getRow());
                        movimientoCajaTB.setFechaMovimiento(resultSetMovimiento.getDate("FechaMovimiento").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        movimientoCajaTB.setHoraMovimiento(resultSetMovimiento.getTime("HoraMovimiento").toLocalTime()
                                .format(DateTimeFormatter.ofPattern("hh:mm a")));
                        movimientoCajaTB.setComentario(resultSetMovimiento.getString("Comentario"));
                        movimientoCajaTB.setTipoMovimiento(resultSetMovimiento.getShort("TipoMovimiento"));
                        movimientoCajaTB.setMonto(resultSetMovimiento.getDouble("Monto"));
                        arratyLista.add(movimientoCajaTB);
                    }
                }

                objects[0] = cajaTB;
                objects[1] = arrayTotales;
                objects[2] = arratyLista;

                return objects;
            } else {
                throw new Exception("No se pudo obtener datos de la consulta, intente nuevamente.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementMovimiento != null) {
                    statementMovimiento.close();
                }
                if (statementMovimientoBase != null) {
                    statementMovimientoBase.close();
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

    public static String CerrarAperturaCaja(String idCaja, BancoHistorialTB bancoHistorialEfectivo,
            BancoHistorialTB bancoHistorialTarjeta, double contado, double calculado) {
        DBUtil dbf = new DBUtil();
        String cajaTB = "";
        PreparedStatement statementCaja = null;

        PreparedStatement statementBancoEfectivo = null;
        PreparedStatement statementHistorialBancoEfectivo = null;

        PreparedStatement statementBancoTarjeta = null;
        PreparedStatement statementHistorialBancoTarjeta = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementCaja = dbf.getConnection().prepareStatement(
                    "UPDATE CajaTB SET FechaCierre = ?,HoraCierre = ?,Contado = ?,Calculado = ?,Diferencia = ?,Estado=? WHERE IdCaja = ?");
            statementCaja.setString(1, Tools.getDate());
            statementCaja.setString(2, Tools.getTime("HH:mm:ss"));
            statementCaja.setDouble(3, contado);
            statementCaja.setDouble(4, calculado);
            statementCaja.setDouble(5, contado - calculado);
            statementCaja.setBoolean(6, false);
            statementCaja.setString(7, idCaja);
            statementCaja.addBatch();

            statementBancoEfectivo = dbf.getConnection()
                    .prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial + ? WHERE IdBanco = ?");
            statementHistorialBancoEfectivo = dbf.getConnection().prepareStatement(
                    "INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");

            if (bancoHistorialEfectivo != null) {
                statementBancoEfectivo.setDouble(1, bancoHistorialEfectivo.getEntrada());
                statementBancoEfectivo.setString(2, bancoHistorialEfectivo.getIdBanco());
                statementBancoEfectivo.addBatch();

                statementHistorialBancoEfectivo.setString(1, bancoHistorialEfectivo.getIdBanco());
                statementHistorialBancoEfectivo.setString(2, bancoHistorialEfectivo.getIdEmpleado());
                statementHistorialBancoEfectivo.setString(3, "");
                statementHistorialBancoEfectivo.setString(4, bancoHistorialEfectivo.getDescripcion());
                statementHistorialBancoEfectivo.setString(5, bancoHistorialEfectivo.getFecha());
                statementHistorialBancoEfectivo.setString(6, bancoHistorialEfectivo.getHora());
                statementHistorialBancoEfectivo.setDouble(7, bancoHistorialEfectivo.getEntrada());
                statementHistorialBancoEfectivo.setDouble(8, bancoHistorialEfectivo.getSalida());
                statementHistorialBancoEfectivo.addBatch();
            }

            statementBancoTarjeta = dbf.getConnection()
                    .prepareStatement("UPDATE Banco SET SaldoInicial = SaldoInicial + ? WHERE IdBanco = ?");
            statementHistorialBancoTarjeta = dbf.getConnection().prepareStatement(
                    "INSERT INTO BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");

            if (bancoHistorialTarjeta != null) {
                statementBancoTarjeta.setDouble(1, bancoHistorialTarjeta.getEntrada());
                statementBancoTarjeta.setString(2, bancoHistorialTarjeta.getIdBanco());
                statementBancoTarjeta.addBatch();

                statementHistorialBancoTarjeta.setString(1, bancoHistorialTarjeta.getIdBanco());
                statementHistorialBancoTarjeta.setString(2, bancoHistorialTarjeta.getIdEmpleado());
                statementHistorialBancoTarjeta.setString(3, "");
                statementHistorialBancoTarjeta.setString(4, bancoHistorialTarjeta.getDescripcion());
                statementHistorialBancoTarjeta.setString(5, bancoHistorialTarjeta.getFecha());
                statementHistorialBancoTarjeta.setString(6, bancoHistorialTarjeta.getHora());
                statementHistorialBancoTarjeta.setDouble(7, bancoHistorialTarjeta.getEntrada());
                statementHistorialBancoTarjeta.setDouble(8, bancoHistorialTarjeta.getSalida());
                statementHistorialBancoTarjeta.addBatch();
            }
            statementCaja.executeBatch();
            statementBancoEfectivo.executeBatch();
            statementHistorialBancoEfectivo.executeBatch();
            statementBancoTarjeta.executeBatch();
            statementHistorialBancoTarjeta.executeBatch();
            dbf.getConnection().commit();
            cajaTB = "completed";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            cajaTB = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCaja != null) {
                    statementCaja.close();
                }
                if (statementBancoEfectivo != null) {
                    statementBancoEfectivo.close();
                }
                if (statementHistorialBancoEfectivo != null) {
                    statementHistorialBancoEfectivo.close();
                }
                if (statementBancoTarjeta != null) {
                    statementBancoTarjeta.close();
                }
                if (statementHistorialBancoTarjeta != null) {
                    statementHistorialBancoTarjeta.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return cajaTB;
    }

    public static String AjustarCaja(String idCaja) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementAjuste = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidar = dbf.getConnection().prepareStatement("SELECT "
                    + "CASE "
                    + "WHEN TipoMovimiento = 5 THEN -Monto "
                    + "ELSE Monto END AS Monto "
                    + "FROM MovimientoCajaTB "
                    + "WHERE "
                    + "IdCaja = ? AND ( "
                    + "TipoMovimiento = 1 "
                    + "OR "
                    + "TipoMovimiento = 2 "
                    + "OR "
                    + "TipoMovimiento = 4 "
                    + "OR "
                    + "TipoMovimiento = 5 "
                    + ") ");
            statementValidar.setString(1, idCaja);
            resultSet = statementValidar.executeQuery();

            double contado = 0;
            while (resultSet.next()) {
                contado += resultSet.getDouble("Monto");
            }

            statementAjuste = dbf.getConnection()
                    .prepareStatement("UPDATE CajaTB SET Contado=?,Calculado=?  WHERE IdCaja = ?");
            statementAjuste.setDouble(1, contado);
            statementAjuste.setDouble(2, contado);
            statementAjuste.setString(3, idCaja);
            statementAjuste.addBatch();

            statementAjuste.executeBatch();
            dbf.getConnection().commit();
            return "upload";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                ex.getLocalizedMessage();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementAjuste != null) {
                    statementAjuste.close();
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

    public static Object ListarCajasAperturadas(String fechaInicial, String fechaFinal) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ObservableList<CajaTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection().prepareStatement("{call Sp_ListarCajasAperturadas(?,?)}");
            statementLista.setString(1, fechaInicial);
            statementLista.setString(2, fechaFinal);

            ResultSet result = statementLista.executeQuery();
            while (result.next()) {
                CajaTB cajaTB = new CajaTB();
                cajaTB.setIdCaja(result.getString("IdCaja"));
                cajaTB.setFechaApertura(result.getDate("FechaApertura").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cajaTB.setHoraApertura(
                        result.getTime("HoraApertura").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cajaTB.setFechaCierre(
                        result.getDate("FechaCierre").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cajaTB.setHoraCierre(
                        result.getTime("HoraCierre").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cajaTB.setEstado(result.getBoolean("Estado"));
                cajaTB.setContado(result.getDouble("Contado"));
                cajaTB.setCalculado(result.getDouble("Calculado"));
                cajaTB.setDiferencia(result.getDouble("Diferencia"));
                cajaTB.setEmpleadoTB(new EmpleadoTB(result.getString("Apellidos"), result.getString("Nombres")));
                Label label = new Label(cajaTB.isEstado() ? "APERTURADO" : "CERRADO");
                label.getStyleClass().add("labelRoboto13");
                label.setTextFill(Color.web(cajaTB.isEstado() ? "#008c1e" : "#bb0202"));
                cajaTB.setLabelEstado(label);
                empList.add(cajaTB);
            }
            return empList;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static ObservableList<CajaTB> ListarCajasAperturadasByUser(String idUsuario) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ObservableList<CajaTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection().prepareStatement("{call Sp_ListarCajasAperturadasPorUsuario(?)}");
            statementLista.setString(1, idUsuario);

            ResultSet result = statementLista.executeQuery();
            while (result.next()) {
                CajaTB cajaTB = new CajaTB();
                cajaTB.setId(result.getRow());
                cajaTB.setIdCaja(result.getString("IdCaja"));
                cajaTB.setFechaApertura(result.getDate("FechaApertura").toLocalDate()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
                cajaTB.setHoraApertura(
                        result.getTime("HoraApertura").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cajaTB.setFechaCierre(result.getDate("FechaCierre").toLocalDate()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
                cajaTB.setHoraCierre(
                        result.getTime("HoraCierre").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cajaTB.setEstado(result.getBoolean("Estado"));
                cajaTB.setContado(result.getDouble("Contado"));
                cajaTB.setCalculado(result.getDouble("Calculado"));
                cajaTB.setDiferencia(result.getDouble("Diferencia"));
                cajaTB.setEmpleadoTB(new EmpleadoTB(result.getString("Apellidos"), result.getString("Nombres")));
                empList.add(cajaTB);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Error en la funcion ListarCajasAperturadas:" + ex.getLocalizedMessage());
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

    public static String CrudListaCaja(String idListaCaja, String nombre, boolean estado) {
        DBUtil dbf = new DBUtil();
        String result = "";

        CallableStatement codigoListaCaja = null;
        PreparedStatement statementValidar = null;
        PreparedStatement statementCaja = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidar = dbf.getConnection().prepareStatement("SELECT * FROM ListaCajaTB WHERE IdListaCaja = ?");
            statementValidar.setString(1, idListaCaja);
            if (statementValidar.executeQuery().next()) {
                statementCaja = dbf.getConnection().prepareStatement("UPDATE ListaCajaTB\n"
                        + "   SET Nombre = ? \n"
                        + "      ,Estado = ? \n"
                        + " WHERE IdListaCaja = ?");
                statementCaja.setString(1, nombre);
                statementCaja.setBoolean(2, estado);
                statementCaja.setString(3, idListaCaja);
                statementCaja.addBatch();
                statementCaja.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            } else {
                codigoListaCaja = dbf.getConnection().prepareCall("{? = call [Fc_Lista_Caja_Codigo_Alfanumerico]()}");
                codigoListaCaja.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigoListaCaja.execute();
                String idCaja = codigoListaCaja.getString(1);

                statementCaja = dbf.getConnection()
                        .prepareStatement("INSERT INTO ListaCajaTB(IdListaCaja,Nombre,Estado)VALUES(?,?,?)");
                statementCaja.setString(1, idCaja);
                statementCaja.setString(2, nombre);
                statementCaja.setBoolean(3, estado);
                statementCaja.addBatch();
                statementCaja.executeBatch();
                dbf.getConnection().commit();
                result = "insert";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (codigoListaCaja != null) {
                    codigoListaCaja.close();
                }
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementCaja != null) {
                    statementCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;
    }

    public static Object ReporteGeneralMovimientoCaja(String fechaInicio, String fechaFinal, int usuario,
            String idUusuario) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementMovimiento = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();

            statementMovimiento = dbf.getConnection()
                    .prepareStatement("{call Sp_Reporte_General_Movimiento_Caja(?,?,?,?)}");
            statementMovimiento.setString(1, fechaInicio);
            statementMovimiento.setString(2, fechaFinal);
            statementMovimiento.setInt(3, usuario);
            statementMovimiento.setString(4, idUusuario);
            resultSet = statementMovimiento.executeQuery();
            while (resultSet.next()) {
                IngresoTB ingresoTB = new IngresoTB();
                ingresoTB.setId(resultSet.getRow());
                ingresoTB.setTransaccion(resultSet.getString("Transaccion"));
                ingresoTB.setCantidad(resultSet.getInt("Cantidad"));
                ingresoTB.setFormaIngreso(resultSet.getString("FormaIngreso"));
                ingresoTB.setEfectivo(resultSet.getDouble("Efectivo"));
                ingresoTB.setTarjeta(resultSet.getDouble("Tarjeta"));
                ingresoTB.setDeposito(resultSet.getDouble("Deposito"));
                ingresoTBs.add(ingresoTB);
            }

            return ingresoTBs;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMovimiento != null) {
                    statementMovimiento.close();
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

}
