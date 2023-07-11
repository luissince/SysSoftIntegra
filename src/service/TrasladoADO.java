package service;

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
import model.AlmacenTB;
import model.EmpleadoTB;
import model.SuministroTB;
import model.TrasladoHistorialTB;
import model.TrasladoTB;
import model.VentaTB;

public class TrasladoADO {

    public static String CrudTrasladoInventario(TrasladoTB trasladoTB, ObservableList<SuministroTB> suministroTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        CallableStatement statementCodigoTraslado = null;
        PreparedStatement statementCorrelativo = null;
        PreparedStatement statementTraslado = null;
        PreparedStatement statementTrasladoDetalle = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementKardex = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementCodigoTraslado = dbf.getConnection().prepareCall("{? = call Fc_Traslado_Codigo_Alfanumerico()}");
            statementCodigoTraslado.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigoTraslado.execute();
            String idTraslado = statementCodigoTraslado.getString(1);

            statementCorrelativo = dbf.getConnection()
                    .prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM TrasladoTB");
            ResultSet resultSet = statementCorrelativo.executeQuery();
            int correlativo = 1;
            if (resultSet.next()) {
                correlativo = resultSet.getInt("Correlativo") + 1;
            }

            statementTraslado = dbf.getConnection().prepareStatement("INSERT INTO TrasladoTB\n"
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

            statementTrasladoDetalle = dbf.getConnection().prepareStatement("INSERT INTO "
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

            statementKardex = dbf.getConnection().prepareStatement("INSERT INTO "
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
                statementSuministro = dbf.getConnection().prepareStatement("UPDATE "
                        + "SuministroTB "
                        + "SET "
                        + "Cantidad = Cantidad - ? "
                        + "WHERE IdSuministro = ?");
            } else {
                statementSuministro = dbf.getConnection().prepareStatement("UPDATE "
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

    }

    public static String CrudTrasladoGuia(TrasladoTB trasladoTB, ObservableList<SuministroTB> suministroTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedValidate = null;
        CallableStatement statementCodigoTraslado = null;
        PreparedStatement statementCorrelativo = null;
        PreparedStatement statementNumeracion = null;
        PreparedStatement statementTraslado = null;
        PreparedStatement statementTrasladoDetalle = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementCodigoTraslado = dbf.getConnection().prepareCall("{? = call Fc_Traslado_Codigo_Alfanumerico()}");
            statementCodigoTraslado.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigoTraslado.execute();
            String idTraslado = statementCodigoTraslado.getString(1);

            statementCorrelativo = dbf.getConnection()
                    .prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM TrasladoTB");
            ResultSet resultSet = statementCorrelativo.executeQuery();
            int correlativo = 1;
            if (resultSet.next()) {
                correlativo = resultSet.getInt("Correlativo") + 1;
            }

            statementNumeracion = dbf.getConnection()
                    .prepareStatement("SELECT MAX(Numeracion) AS Numeracion FROM TrasladoTB");
            int numeracion = 0;
            if (trasladoTB.isUsarNumeracion()) {
                resultSet = statementNumeracion.executeQuery();
                if (resultSet.next()) {
                    numeracion = resultSet.getInt("Numeracion") + 1;
                } else {
                    numeracion = trasladoTB.getNumeracion();
                }
            }

            statementTraslado = dbf.getConnection().prepareStatement("INSERT INTO TrasladoTB\n"
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

            statementTrasladoDetalle = dbf.getConnection().prepareStatement(
                    "INSERT INTO TrasladoHistorialTB(IdTraslado,IdSuministro,Cantidad,Peso) VALUES(?,?,?,?)");
            for (SuministroTB sm : suministroTB) {
                statementTrasladoDetalle.setString(1, idTraslado);
                statementTrasladoDetalle.setString(2, sm.getIdSuministro());
                statementTrasladoDetalle.setDouble(3, Double.parseDouble(sm.getTxtMovimiento().getText()));
                statementTrasladoDetalle.setDouble(4, Double.parseDouble(sm.getTxtPeso().getText()));
                statementTrasladoDetalle.addBatch();
            }

            statementTraslado.executeBatch();
            statementTrasladoDetalle.executeBatch();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static Object ListarTraslados(int opcion, String fechaInicial, String fechaFinal, int posicionPagina,
            int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<TrasladoTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Traslado(?,?,?,?,?)}");
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
                trasladoTB.setFecha(
                        rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHora(
                        rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                trasladoTB.setObservacion(rsEmps.getString("Observacion"));
                trasladoTB.setEstado(rsEmps.getInt("Estado"));
                trasladoTB.setTipo(rsEmps.getInt("Tipo"));
                trasladoTB.setEmpleadoTB(new EmpleadoTB(rsEmps.getString("IdUsuario"), rsEmps.getString("Apellidos"),
                        rsEmps.getString("Nombres")));
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
                btnDetalle.getStyleClass().add("buttonLightWarning");
                ImageView ivDetalle = new ImageView(new Image("/view/image/search.png"));
                ivDetalle.setFitWidth(22);
                ivDetalle.setFitHeight(22);
                btnDetalle.setGraphic(ivDetalle);
                trasladoTB.setBtnDetalle(btnDetalle);

                Button btnReporte = new Button();
                btnReporte.getStyleClass().add("buttonLightWarning");
                ImageView ivReporte = new ImageView(new Image("/view/image/reports.png"));
                ivReporte.setFitWidth(22);
                ivReporte.setFitHeight(22);
                btnReporte.setGraphic(ivReporte);
                trasladoTB.setBtnReporte(btnReporte);

                empList.add(trasladoTB);
            }

            rsEmps.close();
            preparedStatementCount = dbf.getConnection().prepareStatement("{call Sp_Listar_Traslado_Count(?,?,?)}");
            preparedStatementCount.setInt(1, opcion);
            preparedStatementCount.setString(2, fechaInicial);
            preparedStatementCount.setString(3, fechaFinal);
            rsEmps = preparedStatementCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            objects[0] = empList;
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
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

    public static Object ObtenerTrasladoById(String idTraslado) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementHistorial = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Traslado_ById(?)}");
            preparedStatement.setString(1, idTraslado);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                TrasladoTB trasladoTB = new TrasladoTB();
                trasladoTB.setIdTraslado(rsEmps.getString("IdTraslado"));
                trasladoTB.setFecha(
                        rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHora(
                        rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                trasladoTB.setFechaTraslado(rsEmps.getDate("FechaTraslado").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                trasladoTB.setHoraTraslado(
                        rsEmps.getTime("HoraTraslado").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
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

                rsEmps.close();
                preparedStatementHistorial = dbf.getConnection()
                        .prepareStatement("{CALL Sp_Obtener_Traslado_Historial_ById(?)}");
                preparedStatementHistorial.setString(1, idTraslado);
                rsEmps = preparedStatementHistorial.executeQuery();
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
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (preparedStatementHistorial != null) {
                    preparedStatementHistorial.close();
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

    public static Object ObtenerTrasladosReporteForDate(String fechaInicio, String fechaFinal) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("select\n"
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

    public static ArrayList<SuministroTB> ObtenerDetalleVenta(String idVenta) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedDetalle = null;
        ResultSet resultSet = null;
        PreparedStatement preparedSuministro = null;
        ResultSet rsEmps = null;
        ArrayList<SuministroTB> suministroTBs = new ArrayList<>();
        try {
            dbf.dbConnect();

            preparedDetalle = dbf.getConnection()
                    .prepareStatement("SELECT IdArticulo,Cantidad FROM DetalleVentaTB WHERE IdVenta = ?");
            preparedDetalle.setString(1, idVenta);
            resultSet = preparedDetalle.executeQuery();
            while (resultSet.next()) {

                preparedSuministro = dbf.getConnection().prepareStatement("{call Sp_Get_Suministro_For_Movimiento(?)}");
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

        } catch (SQLException | ClassNotFoundException ex) {
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return suministroTBs;
    }

}
