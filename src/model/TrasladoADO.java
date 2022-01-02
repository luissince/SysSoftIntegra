package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TrasladoADO {

//    public static String CrudTraslado(SuministroTB suministroTB, AlmacenTB almacenTB, TrasladoTB trasladoTB, double cantidad) {
//        PreparedStatement statementValidate = null;
//        PreparedStatement suministroOriginActualizar = null;
//        PreparedStatement suministroOriginKardex = null;
//        PreparedStatement suministroDestino = null;
//        PreparedStatement suministroDestinoInsertar = null;
//        PreparedStatement suministroDestinoActualizar = null;
//        PreparedStatement suministroDestinokardex = null;
//        CallableStatement statementCodigoTraslado = null;
//        PreparedStatement statementTraslado = null;
//        PreparedStatement statementCorrelativo = null;
//        PreparedStatement statementTrasladoHistorial = null;
//
//        try {
//            DBUtil.dbConnect();
//            DBUtil.getConnection().setAutoCommit(false);
//
//            int countValidate = 0;
//            if (suministroTB.getAlmacenTB().getIdAlmacen() == 0) {
//                statementValidate = DBUtil.getConnection().prepareStatement("SELECT Cantidad FROM SuministroTB WHERE IdSuministro = ?");
//                statementValidate.setString(1, suministroTB.getIdSuministro());
//                ResultSet resultValidate = statementValidate.executeQuery();
//                if (resultValidate.next()) {
//                    double ca = cantidad;
//                    double cb = resultValidate.getDouble("Cantidad");
//                    if (ca > cb) {
//                        countValidate++;
//                    }
//                }
//            } else {
//                statementValidate = DBUtil.getConnection().prepareStatement("SELECT Cantidad FROM CantidadTB WHERE IdAlmacen = ? AND IdSuministro = ?");
//                statementValidate.setInt(1, suministroTB.getAlmacenTB().getIdAlmacen());
//                statementValidate.setString(2, suministroTB.getIdSuministro());
//                ResultSet resultValidate = statementValidate.executeQuery();
//                if (resultValidate.next()) {
//                    double ca = cantidad;
//                    double cb = resultValidate.getDouble("Cantidad");
//                    if (ca > cb) {
//                        countValidate++;
//                    }
//                }
//            }
//
//            if (countValidate > 0) {
//                DBUtil.getConnection().rollback();
//                return "cantidad";
//            } else {
//                if (suministroTB.getAlmacenTB().getIdAlmacen() == 0) {
//                    suministroOriginActualizar = DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");
//                    suministroOriginActualizar.setDouble(1, cantidad);
//                    suministroOriginActualizar.setString(2, suministroTB.getIdSuministro());
//                    suministroOriginActualizar.addBatch();
//                } else {
//                    suministroOriginActualizar = DBUtil.getConnection().prepareStatement("UPDATE CantidadTB SET Cantidad = Cantidad - ? WHERE IdAlmacen = ? AND IdSuministro = ? ");
//                    suministroOriginActualizar.setDouble(1, cantidad);
//                    suministroOriginActualizar.setInt(2, suministroTB.getAlmacenTB().getIdAlmacen());
//                    suministroOriginActualizar.setString(3, suministroTB.getIdSuministro());
//                    suministroOriginActualizar.addBatch();
//                }
//
//                suministroOriginKardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
//                        + "KardexSuministroTB("
//                        + "IdSuministro,"
//                        + "Fecha,"
//                        + "Hora,"
//                        + "Tipo,"
//                        + "Movimiento,"
//                        + "Detalle,"
//                        + "Cantidad, "
//                        + "Costo, "
//                        + "Total,"
//                        + "IdAlmacen,"
//                        + "IdEmpleado) "
//                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
//
//                suministroOriginKardex.setString(1, suministroTB.getIdSuministro());
//                suministroOriginKardex.setString(2, Tools.getDate());
//                suministroOriginKardex.setString(3, Tools.getTime());
//                suministroOriginKardex.setShort(4, (short) 2);
//                suministroOriginKardex.setInt(5, 1);
//                suministroOriginKardex.setString(6, "SALIDA POR MOVIMIENTO AL ALMACEN DE DESTINO " + almacenTB.getNombre());
//                suministroOriginKardex.setDouble(7, cantidad);
//                suministroOriginKardex.setDouble(8, suministroTB.getCostoCompra());
//                suministroOriginKardex.setDouble(9, cantidad * suministroTB.getCostoCompra());
//                suministroOriginKardex.setInt(10, suministroTB.getAlmacenTB().getIdAlmacen());
//                suministroOriginKardex.setString(11, Session.USER_ID);
//                suministroOriginKardex.addBatch();
//
//                suministroDestino = DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?");
//
//                suministroDestinoInsertar = DBUtil.getConnection().prepareStatement("INSERT INTO CantidadTB(IdAlmacen,IdSuministro,StockMinimo,StockMaximo,Cantidad) VALUES(?,?,?,?,?)");
//                suministroDestinoActualizar = DBUtil.getConnection().prepareStatement("UPDATE CantidadTB SET Cantidad = Cantidad + ? WHERE IdAlmacen = ? AND IdSuministro = ?");
//
//                if (almacenTB.getIdAlmacen() == 0) {
//                    suministroDestino.setDouble(1, cantidad);
//                    suministroDestino.setString(2, suministroTB.getIdSuministro());
//                    suministroDestino.addBatch();
//                } else {
//
//                    statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM CantidadTB WHERE IdAlmacen = ? AND IdSuministro = ? ");
//                    statementValidate.setInt(1, almacenTB.getIdAlmacen());
//                    statementValidate.setString(2, suministroTB.getIdSuministro());
//                    if (statementValidate.executeQuery().next()) {
//                        suministroDestinoActualizar.setDouble(1, cantidad);
//                        suministroDestinoActualizar.setInt(2, almacenTB.getIdAlmacen());
//                        suministroDestinoActualizar.setString(3, suministroTB.getIdSuministro());
//                        suministroDestinoActualizar.addBatch();
//                    } else {
//                        suministroDestinoInsertar.setInt(1, almacenTB.getIdAlmacen());
//                        suministroDestinoInsertar.setString(2, suministroTB.getIdSuministro());
//                        suministroDestinoInsertar.setDouble(3, 1);
//                        suministroDestinoInsertar.setDouble(4, 10);
//                        suministroDestinoInsertar.setDouble(5, cantidad);
//                        suministroDestinoInsertar.addBatch();
//                    }
//                }
//
//                suministroDestinokardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
//                        + "KardexSuministroTB("
//                        + "IdSuministro,"
//                        + "Fecha,"
//                        + "Hora,"
//                        + "Tipo,"
//                        + "Movimiento,"
//                        + "Detalle,"
//                        + "Cantidad, "
//                        + "Costo, "
//                        + "Total,"
//                        + "IdAlmacen,"
//                        + "IdEmpleado) "
//                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
//
//                suministroDestinokardex.setString(1, suministroTB.getIdSuministro());
//                suministroDestinokardex.setString(2, Tools.getDate());
//                suministroDestinokardex.setString(3, Tools.getTime());
//                suministroDestinokardex.setShort(4, (short) 1);
//                suministroDestinokardex.setInt(5, 2);
//                suministroDestinokardex.setString(6, "INGRESO POR MOVIMIENTO DEL ALMACEN " + suministroTB.getAlmacenTB().getNombre());
//                suministroDestinokardex.setDouble(7, cantidad);
//                suministroDestinokardex.setDouble(8, suministroTB.getCostoCompra());
//                suministroDestinokardex.setDouble(9, cantidad * suministroTB.getCostoCompra());
//                suministroDestinokardex.setInt(10, almacenTB.getIdAlmacen());
//                suministroDestinokardex.setString(11, Session.USER_ID);
//                suministroDestinokardex.addBatch();
//
//                statementCodigoTraslado = DBUtil.getConnection().prepareCall("{? = call Fc_Traslado_Codigo_Alfanumerico()}");
//                statementCodigoTraslado.registerOutParameter(1, java.sql.Types.VARCHAR);
//                statementCodigoTraslado.execute();
//                String idTraslado = statementCodigoTraslado.getString(1);
//
//                statementCorrelativo = DBUtil.getConnection().prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM TrasladoTB");
//                ResultSet resultSet = statementCorrelativo.executeQuery();
//                int correlativo = 1;
//                if (resultSet.next()) {
//                    correlativo = resultSet.getInt("Correlativo") + 1;
//                }
//
//                statementTraslado = DBUtil.getConnection().prepareStatement("INSERT INTO TrasladoTB\n"
//                        + "(IdTraslado\n"
//                        + ",Fecha\n"
//                        + ",Hora\n"
//                        + ",FechaTraslado\n"
//                        + ",HoraTraslado\n"
//                        + ",PuntoPartida\n"
//                        + ",PuntoLlegada\n"
//                        + ",Correlativo\n"
//                        + ",Numeracion\n"
//                        + ",Observacion\n"
//                        + ",Estado\n"
//                        + ",Tipo\n"
//                        + ",IdVenta\n"
//                        + ",IdUsuario)\n"
//                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
//                statementTraslado.setString(1, idTraslado);
//                statementTraslado.setString(2, trasladoTB.getFecha());
//                statementTraslado.setString(3, trasladoTB.getHora());
//                statementTraslado.setString(4, trasladoTB.getFechaTraslado());
//                statementTraslado.setString(5, trasladoTB.getHoraTraslado());
//                statementTraslado.setString(6, trasladoTB.getPuntoPartida());
//                statementTraslado.setString(7, trasladoTB.getPuntoLlegada());
//                statementTraslado.setInt(8, correlativo);
//                statementTraslado.setInt(9, 0);
//                statementTraslado.setString(10, trasladoTB.getObservacion());
//                statementTraslado.setInt(11, trasladoTB.getEstado());
//                statementTraslado.setInt(12, trasladoTB.getTipo());
//                statementTraslado.setString(13, "");
//                statementTraslado.setString(14, trasladoTB.getIdEmpleado());
//                statementTraslado.addBatch();
//
//                statementTrasladoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO TrasladoHistorialTB(IdTraslado,IdSuministro,IdAlmacenOrigen,IdAlmacenDestino,Cantidad,Peso) VALUES(?,?,?,?,?,?)");
//                statementTrasladoHistorial.setString(1, idTraslado);
//                statementTrasladoHistorial.setString(2, suministroTB.getIdSuministro());
//                statementTrasladoHistorial.setInt(3, suministroTB.getAlmacenTB().getIdAlmacen());
//                statementTrasladoHistorial.setInt(4, almacenTB.getIdAlmacen());
//                statementTrasladoHistorial.setDouble(5, cantidad);
//                statementTrasladoHistorial.setDouble(6, 0);
//                statementTrasladoHistorial.addBatch();
//
//                suministroOriginActualizar.executeBatch();
//                suministroOriginKardex.executeBatch();
//
//                suministroDestino.executeBatch();
//                suministroDestinoInsertar.executeBatch();
//                suministroDestinoActualizar.executeBatch();
//                suministroDestinokardex.executeBatch();
//
//                statementTraslado.executeBatch();
//                statementTrasladoHistorial.executeBatch();
//
//                DBUtil.getConnection().commit();
//                return "update";
//            }
//        } catch (SQLException ex) {
//            try {
//                DBUtil.getConnection().rollback();
//            } catch (SQLException e) {
//            }
//            return ex.getLocalizedMessage();
//        } finally {
//            try {
//                if (statementValidate != null) {
//                    statementValidate.close();
//                }
//                if (suministroDestinoInsertar != null) {
//                    suministroDestinoInsertar.close();
//                }
//                if (suministroDestinoActualizar != null) {
//                    suministroDestinoActualizar.close();
//                }
//                if (suministroDestinokardex != null) {
//                    suministroDestinokardex.close();
//                }
//                if (suministroOriginActualizar != null) {
//                    suministroOriginActualizar.close();
//                }
//                if (suministroOriginKardex != null) {
//                    suministroOriginKardex.close();
//                }
//                if (suministroDestino != null) {
//                    suministroDestino.close();
//                }
//                if (statementCodigoTraslado != null) {
//                    statementCodigoTraslado.close();
//                }
//                if (statementTraslado != null) {
//                    statementTraslado.close();
//                }
//                if (statementTrasladoHistorial != null) {
//                    statementTrasladoHistorial.close();
//                }
//                if (statementCorrelativo != null) {
//                    statementCorrelativo.close();
//                }
//                DBUtil.dbDisconnect();
//            } catch (SQLException ex) {
//
//            }
//        }
//    }
    public static String CrudTrasladoInventario(TrasladoTB trasladoTB, ObservableList<SuministroTB> suministroTB) {

        PreparedStatement statementValidate = null;
        CallableStatement statementCodigoTraslado = null;
        PreparedStatement statementCorrelativo = null;
        PreparedStatement statementTraslado = null;
        PreparedStatement statementTrasladoDetalle = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementKardex = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementCodigoTraslado = DBUtil.getConnection().prepareCall("{? = call Fc_Traslado_Codigo_Alfanumerico()}");
            statementCodigoTraslado.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigoTraslado.execute();
            String idTraslado = statementCodigoTraslado.getString(1);

            statementCorrelativo = DBUtil.getConnection().prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM TrasladoTB");
            ResultSet resultSet = statementCorrelativo.executeQuery();
            int correlativo = 1;
            if (resultSet.next()) {
                correlativo = resultSet.getInt("Correlativo") + 1;
            }

            statementTraslado = DBUtil.getConnection().prepareStatement("INSERT INTO TrasladoTB\n"
                    + "(IdTraslado\n"
                    + ",Fecha\n"
                    + ",Hora\n"
                    + ",FechaTraslado\n"
                    + ",HoraTraslado\n"
                    + ",IdAlmacen\n"
                    + ",PuntoPartida\n"
                    + ",PuntoLlegada\n"
                    + ",Correlativo\n"
                    + ",Numeracion\n"
                    + ",Observacion\n"
                    + ",Estado\n"
                    + ",Tipo\n"
                    + ",IdVenta\n"
                    + ",IdUsuario)\n"
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statementTraslado.setString(1, idTraslado);
            statementTraslado.setString(2, trasladoTB.getFecha());
            statementTraslado.setString(3, trasladoTB.getHora());
            statementTraslado.setString(4, trasladoTB.getFechaTraslado());
            statementTraslado.setString(5, trasladoTB.getHoraTraslado());
            statementTraslado.setInt(6, trasladoTB.getIdAlmacen());
            statementTraslado.setString(7, trasladoTB.getPuntoPartida());
            statementTraslado.setString(8, trasladoTB.getPuntoLlegada());
            statementTraslado.setInt(9, correlativo);
            statementTraslado.setInt(10, 0);
            statementTraslado.setString(11, trasladoTB.getObservacion());
            statementTraslado.setInt(12, trasladoTB.getEstado());
            statementTraslado.setInt(13, trasladoTB.getTipo());
            statementTraslado.setString(14, trasladoTB.getIdVenta());
            statementTraslado.setString(15, trasladoTB.getIdEmpleado());
            statementTraslado.addBatch();

            statementTrasladoDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO "
                    + "TrasladoHistorialTB("
                    + "IdTraslado,"
                    + "IdSuministro,"
                    + "Cantidad,Peso) "
                    + "VALUES(?,?,?,?)");

            for (SuministroTB sm : suministroTB) {
                statementTrasladoDetalle.setString(1, idTraslado);
                statementTrasladoDetalle.setString(2, sm.getIdSuministro());
                statementTrasladoDetalle.setDouble(3, Double.parseDouble(sm.getTxtMovimiento().getText()));
                statementTrasladoDetalle.setDouble(4, Double.parseDouble(sm.getTxtPeso().getText()));
                statementTrasladoDetalle.addBatch();
            }

            statementKardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
                    + "KardexSuministroTB("
                    + "IdSuministro,"
                    + "Fecha,"
                    + "Hora,"
                    + "Tipo,"
                    + "Movimiento,"
                    + "Detalle,"
                    + "Cantidad, "
                    + "Costo, "
                    + "Total,"
                    + "IdAlmacen,"
                    + "IdEmpleado) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

            if (trasladoTB.getIdAlmacen() == 0) {
                statementSuministro = DBUtil.getConnection().prepareStatement("UPDATE "
                        + "SuministroTB "
                        + "SET "
                        + "Cantidad = Cantidad - ? "
                        + "WHERE IdSuministro = ?");
            } else {
                statementSuministro = DBUtil.getConnection().prepareStatement("UPDATE "
                        + "CantidadTB SET "
                        + "Cantidad = Cantidad - ? "
                        + "WHERE IdAlmacen = ? AND IdSuministro = ? ");
            }

            for (SuministroTB sm : suministroTB) {
                if (trasladoTB.getIdAlmacen() == 0) {
                    statementSuministro.setDouble(1, Double.parseDouble(sm.getTxtMovimiento().getText()));
                    statementSuministro.setString(2, sm.getIdSuministro());
                    statementSuministro.addBatch();
                } else {
                    statementSuministro.setDouble(1, Double.parseDouble(sm.getTxtMovimiento().getText()));
                    statementSuministro.setInt(2, trasladoTB.getIdAlmacen());
                    statementSuministro.setString(3, sm.getIdSuministro());
                    statementSuministro.addBatch();
                }

                statementKardex.setString(1, sm.getIdSuministro());
                statementKardex.setString(2, Tools.getDate());
                statementKardex.setString(3, Tools.getTime());
                statementKardex.setShort(4, (short) 2);
                statementKardex.setInt(5, 1);
                statementKardex.setString(6, "SALIDA POR TRASLADO DE PRODUCTO");
                statementKardex.setDouble(7, Double.parseDouble(sm.getTxtMovimiento().getText()));
                statementKardex.setDouble(8, sm.getCostoCompra());
                statementKardex.setDouble(9, Double.parseDouble(sm.getTxtMovimiento().getText()) * sm.getCostoCompra());
                statementKardex.setInt(10, trasladoTB.getIdAlmacen());
                statementKardex.setString(11, trasladoTB.getIdEmpleado());
                statementKardex.addBatch();
            }

            statementTraslado.executeBatch();
            statementTrasladoDetalle.executeBatch();
            statementSuministro.executeBatch();
            statementKardex.executeBatch();
            DBUtil.getConnection().commit();
            return "inserted";
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementCodigoTraslado != null) {
                    statementCodigoTraslado.close();
                }
                if (statementTraslado != null) {
                    statementTraslado.close();
                }
                if (statementTrasladoDetalle != null) {
                    statementTrasladoDetalle.close();
                }
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementKardex != null) {
                    statementKardex.close();
                }
                if (statementCorrelativo != null) {
                    statementCorrelativo.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

    }

    public static String CrudTrasladoGuia(TrasladoTB trasladoTB, ObservableList<SuministroTB> suministroTB) {

        PreparedStatement preparedValidate = null;
        CallableStatement statementCodigoTraslado = null;
        PreparedStatement statementCorrelativo = null;
        PreparedStatement statementNumeracion = null;
        PreparedStatement statementTraslado = null;
        PreparedStatement statementTrasladoDetalle = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementCodigoTraslado = DBUtil.getConnection().prepareCall("{? = call Fc_Traslado_Codigo_Alfanumerico()}");
            statementCodigoTraslado.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigoTraslado.execute();
            String idTraslado = statementCodigoTraslado.getString(1);

            statementCorrelativo = DBUtil.getConnection().prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM TrasladoTB");
            ResultSet resultSet = statementCorrelativo.executeQuery();
            int correlativo = 1;
            if (resultSet.next()) {
                correlativo = resultSet.getInt("Correlativo") + 1;
            }

            statementNumeracion = DBUtil.getConnection().prepareStatement("SELECT MAX(Numeracion) AS Numeracion FROM TrasladoTB");
            int numeracion = 0;
            if (trasladoTB.isUsarNumeracion()) {
                resultSet = statementNumeracion.executeQuery();
                if (resultSet.next()) {
                    numeracion = resultSet.getInt("Numeracion") + 1;
                } else {
                    numeracion = trasladoTB.getNumeracion();
                }
            }

            statementTraslado = DBUtil.getConnection().prepareStatement("INSERT INTO TrasladoTB\n"
                    + "(IdTraslado\n"
                    + ",Fecha\n"
                    + ",Hora\n"
                    + ",FechaTraslado\n"
                    + ",HoraTraslado\n"
                    + ",IdAlmacen\n"
                    + ",PuntoPartida\n"
                    + ",PuntoLlegada\n"
                    + ",Correlativo\n"
                    + ",Numeracion\n"
                    + ",Observacion\n"
                    + ",Estado\n"
                    + ",Tipo\n"
                    + ",IdVenta\n"
                    + ",IdUsuario)\n"
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statementTraslado.setString(1, idTraslado);
            statementTraslado.setString(2, trasladoTB.getFecha());
            statementTraslado.setString(3, trasladoTB.getHora());
            statementTraslado.setString(4, trasladoTB.getFechaTraslado());
            statementTraslado.setString(5, trasladoTB.getHoraTraslado());
            statementTraslado.setInt(6, 0);
            statementTraslado.setString(7, trasladoTB.getPuntoPartida());
            statementTraslado.setString(8, trasladoTB.getPuntoLlegada());
            statementTraslado.setInt(9, correlativo);
            statementTraslado.setInt(10, numeracion);
            statementTraslado.setString(11, trasladoTB.getObservacion());
            statementTraslado.setInt(12, trasladoTB.getEstado());
            statementTraslado.setInt(13, trasladoTB.getTipo());
            statementTraslado.setString(14, trasladoTB.getIdVenta());
            statementTraslado.setString(15, trasladoTB.getIdEmpleado());
            statementTraslado.addBatch();

            statementTrasladoDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO TrasladoHistorialTB(IdTraslado,IdSuministro,Cantidad,Peso) VALUES(?,?,?,?)");
            for (SuministroTB sm : suministroTB) {
                statementTrasladoDetalle.setString(1, idTraslado);
                statementTrasladoDetalle.setString(2, sm.getIdSuministro());
                statementTrasladoDetalle.setDouble(3, Double.parseDouble(sm.getTxtMovimiento().getText()));
                statementTrasladoDetalle.setDouble(4, Double.parseDouble(sm.getTxtPeso().getText()));
                statementTrasladoDetalle.addBatch();
            }

            statementTraslado.executeBatch();
            statementTrasladoDetalle.executeBatch();
            DBUtil.getConnection().commit();
            return "inserted";
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedValidate != null) {
                    preparedValidate.close();
                }
                if (statementCodigoTraslado != null) {
                    statementCodigoTraslado.close();
                }
                if (statementTraslado != null) {
                    statementTraslado.close();
                }
                if (statementTrasladoDetalle != null) {
                    statementTrasladoDetalle.close();
                }
                if (statementCorrelativo != null) {
                    statementCorrelativo.close();
                }
                if (statementNumeracion != null) {
                    statementNumeracion.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static Object ListarTraslados(int opcion, String fechaInicial, String fechaFinal, int posicionPagina, int filasPorPagina) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<TrasladoTB> empList = FXCollections.observableArrayList();

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Traslado(?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, fechaInicial);
            preparedStatement.setString(3, fechaFinal);
            preparedStatement.setInt(4, posicionPagina);
            preparedStatement.setInt(5, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                TrasladoTB trasladoTB = new TrasladoTB();
                trasladoTB.setId(rsEmps.getRow() + posicionPagina);
                trasladoTB.setIdTraslado(rsEmps.getString("IdTraslado"));
                trasladoTB.setFecha(rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHora(rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                trasladoTB.setObservacion(rsEmps.getString("Observacion"));
                trasladoTB.setEstado(rsEmps.getInt("Estado"));
                trasladoTB.setTipo(rsEmps.getInt("Tipo"));
                trasladoTB.setEmpleadoTB(new EmpleadoTB(rsEmps.getString("IdUsuario"), rsEmps.getString("Apellidos"), rsEmps.getString("Nombres")));
                trasladoTB.setCorrelativo(rsEmps.getInt("Correlativo"));
                trasladoTB.setNumeracion(rsEmps.getInt("Numeracion"));

                if (!rsEmps.getString("IdVenta").equalsIgnoreCase("")) {
                    VentaTB ventaTB = new VentaTB();
                    ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                    ventaTB.setSerie(rsEmps.getString("Serie"));
                    ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                    trasladoTB.setVentaTB(ventaTB);
                }

                Button btnDetalle = new Button();
                btnDetalle.getStyleClass().add("buttonLight");
                ImageView view = new ImageView(new Image("/view/image/asignacion.png"));
                view.setFitWidth(22);
                view.setFitHeight(22);
                btnDetalle.setGraphic(view);

                trasladoTB.setBtnDetalle(btnDetalle);
                empList.add(trasladoTB);
            }

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Traslado_Count(?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, fechaInicial);
            preparedStatement.setString(3, fechaFinal);
            rsEmps = preparedStatement.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            objects[0] = empList;
            objects[1] = cantidadTotal;

            return objects;
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
                return ex.getLocalizedMessage();
            }
        }

    }

    public static Object ObtenerTrasladoById(String idTraslado) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Traslado_ById(?)}");
            preparedStatement.setString(1, idTraslado);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                TrasladoTB trasladoTB = new TrasladoTB();
                trasladoTB.setIdTraslado(rsEmps.getString("IdTraslado"));
                trasladoTB.setFecha(rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHora(rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                trasladoTB.setFechaTraslado(rsEmps.getDate("FechaTraslado").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHoraTraslado(rsEmps.getTime("HoraTraslado").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                trasladoTB.setPuntoPartida(rsEmps.getString("PuntoPartida"));
                trasladoTB.setPuntoLlegada(rsEmps.getString("PuntoLlegada"));
                trasladoTB.setTipo(rsEmps.getInt("Tipo"));
                trasladoTB.setEstado(rsEmps.getInt("Estado"));
                trasladoTB.setObservacion(rsEmps.getString("Observacion"));
                trasladoTB.setIdVenta(rsEmps.getString("IdVenta"));

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(rsEmps.getString("Apellidos"));
                empleadoTB.setNombres(rsEmps.getString("Nombres"));
                trasladoTB.setEmpleadoTB(empleadoTB);

                preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Traslado_Historial_ById(?)}");
                preparedStatement.setString(1, idTraslado);
                rsEmps = preparedStatement.executeQuery();
                ArrayList<TrasladoHistorialTB> historialTBs = new ArrayList<>();
                while (rsEmps.next()) {
                    TrasladoHistorialTB historialTB = new TrasladoHistorialTB();
                    historialTB.setId(rsEmps.getRow());
                    historialTB.setIdTraslado(rsEmps.getString("IdTraslado"));

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                    suministroTB.setClave(rsEmps.getString("Clave"));
                    suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    historialTB.setSuministroTB(suministroTB);

                    historialTB.setSuministro(rsEmps.getString("Clave") + "\n" + rsEmps.getString("NombreMarca"));
                    historialTB.setMedida(rsEmps.getString("Medida"));
                    historialTB.setCantidad(rsEmps.getDouble("Cantidad"));
                    historialTB.setPeso(rsEmps.getDouble("Peso"));

                    historialTBs.add(historialTB);
                }
                trasladoTB.setHistorialTBs(historialTBs);

                return trasladoTB;
            } else {
                throw new Exception("No se pudo obtener los datos del movimiento, intente nuevamente.");
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
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ObtenerTrasladosReporteForDate(String fechaInicio, String fechaFinal) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("select\n"
                    + "th.IdTraslado,\n"
                    + "th.IdSuministro,\n"
                    + "s.Clave,\n"
                    + "s.NombreMarca,\n"
                    + "ao.Nombre as AlmacenOrigen,\n"
                    + "ad.Nombre as AlmacenDestino,\n"
                    + "th.Cantidad\n"
                    + "from \n"
                    + "TrasladoTB as t \n"
                    + "inner join TrasladoHistorialTB as th on th.IdTraslado = t.IdTraslado\n"
                    + "inner join SuministroTB as s on s.IdSuministro = th.IdSuministro\n"
                    + "inner join AlmacenTB as ao on ao.IdAlmacen = th.IdAlmacenOrigen\n"
                    + "inner join AlmacenTB as ad on ad.IdAlmacen = th.IdAlmacenDestino \n"
                    + "where t.Fecha between ? and ?");

            preparedStatement.setString(1, fechaInicio);
            preparedStatement.setString(2, fechaFinal);
            rsEmps = preparedStatement.executeQuery();

            ArrayList<TrasladoHistorialTB> historialTBs = new ArrayList<>();
            while (rsEmps.next()) {
                TrasladoHistorialTB historialTB = new TrasladoHistorialTB();
                historialTB.setId(rsEmps.getRow());
                historialTB.setIdTraslado(rsEmps.getString("IdTraslado"));

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                historialTB.setSuministroTB(suministroTB);

                AlmacenTB almacenOringenTB = new AlmacenTB();
                almacenOringenTB.setNombre(rsEmps.getString("AlmacenOrigen"));
                historialTB.setAlmacenOrigenTB(almacenOringenTB);

                AlmacenTB almacenDestinoTB = new AlmacenTB();
                almacenDestinoTB.setNombre(rsEmps.getString("AlmacenDestino"));
                historialTB.setAlmacenDestinoTB(almacenDestinoTB);

                historialTB.setCantidad(rsEmps.getDouble("Cantidad"));

                historialTBs.add(historialTB);
            }
            return historialTBs;
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
                return ex.getLocalizedMessage();
            }
        }
    }

    public static ArrayList<SuministroTB> ObtenerDetalleVenta(String idVenta) {
        PreparedStatement preparedDetalle = null;
        ResultSet resultSet = null;
        PreparedStatement preparedSuministro = null;
        ResultSet rsEmps = null;
        ArrayList<SuministroTB> suministroTBs = new ArrayList();
        try {
            DBUtil.dbConnect();

            preparedDetalle = DBUtil.getConnection().prepareStatement("SELECT IdArticulo,Cantidad FROM DetalleVentaTB WHERE IdVenta = ?");
            preparedDetalle.setString(1, idVenta);
            resultSet = preparedDetalle.executeQuery();
            while (resultSet.next()) {

                preparedSuministro = DBUtil.getConnection().prepareStatement("{call Sp_Get_Suministro_For_Movimiento(?)}");
                preparedSuministro.setString(1, resultSet.getString("IdArticulo"));
                rsEmps = preparedSuministro.executeQuery();
                if (rsEmps.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(rsEmps.getRow());
                    suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                    suministroTB.setClave(rsEmps.getString("Clave"));
                    suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                    suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraNombre"));
                    suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                    suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                    suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                    suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));
                    suministroTB.setDiferencia(suministroTB.getCantidad());
                    suministroTB.setMovimiento(0);
                    TextField tf = new TextField(Tools.roundingValue(resultSet.getDouble("Cantidad"), 0));
                    tf.getStyleClass().add("text-field-normal");
                    tf.setOnKeyTyped(event -> {
                        char c = event.getCharacter().charAt(0);
                        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                            event.consume();
                        }
                        if (c == '.' && tf.getText().contains(".") || c == '-') {
                            event.consume();
                        }
                    });

                    suministroTB.setTxtMovimiento(tf);

                    TextField tfp = new TextField(Tools.roundingValue(1.00, 0));
                    tfp.getStyleClass().add("text-field-normal");
                    tfp.setOnKeyTyped(event -> {
                        char c = event.getCharacter().charAt(0);
                        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                            event.consume();
                        }
                        if (c == '.' && tfp.getText().contains(".") || c == '-') {
                            event.consume();
                        }
                    });

                    suministroTB.setTxtPeso(tfp);

                    CheckBox checkbox = new CheckBox("");
                    checkbox.getStyleClass().add("check-box-contenido");
                    checkbox.setSelected(true);
                    checkbox.setDisable(true);

                    Button button = new Button();
                    button.getStyleClass().add("buttonDark");
                    ImageView view = new ImageView(new Image("/view/image/remove.png"));
                    view.setFitWidth(24);
                    view.setFitHeight(24);
                    button.setGraphic(view);
                    suministroTB.setBtnRemove(button);

                    suministroTB.setValidar(checkbox);
                    suministroTBs.add(suministroTB);
                }
            }

        } catch (SQLException ex) {
            Tools.println("Error en ObtenerDetalleVenta: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (preparedDetalle != null) {
                    preparedDetalle.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedSuministro != null) {
                    preparedSuministro.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return suministroTBs;

    }

}
