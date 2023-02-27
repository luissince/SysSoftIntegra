package service;

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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.AjusteInventarioTB;
import model.AlmacenTB;
import model.MovimientoInventarioDetalleTB;
import model.SuministroTB;

public class AjusteInventarioADO {

    public static String Crud_Movimiento_Inventario(AjusteInventarioTB inventarioTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementMovimiento = null;
        PreparedStatement statementMovimientoDetalle = null;
        PreparedStatement suministroUpdate = null;
        PreparedStatement suministroKardex = null;
        PreparedStatement suministroUpdateAlmacen = null;
        CallableStatement codigoMovimiento = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigoMovimiento = dbf.getConnection()
                    .prepareCall("{? = call Fc_MovimientoInventario_Codigo_Alfanumerico()}");
            codigoMovimiento.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoMovimiento.execute();
            String idMovimiento = codigoMovimiento.getString(1);

            statementMovimiento = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "MovimientoInventarioTB("
                    + "IdMovimientoInventario,"
                    + "Fecha,"
                    + "Hora,"
                    + "TipoAjuste,"
                    + "TipoMovimiento,"
                    + "Observacion,"
                    + "Suministro,"
                    + "Estado,"
                    + "CodigoVerificacion,"
                    + "IdAlmacen)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");
            statementMovimiento.setString(1, idMovimiento);
            statementMovimiento.setString(2, inventarioTB.getFecha());
            statementMovimiento.setString(3, inventarioTB.getHora());
            statementMovimiento.setBoolean(4, inventarioTB.isTipoAjuste());
            statementMovimiento.setInt(5, inventarioTB.getTipoMovimiento());
            statementMovimiento.setString(6, inventarioTB.getObservacion());
            statementMovimiento.setBoolean(7, inventarioTB.isSuministro());
            statementMovimiento.setInt(8, inventarioTB.getEstado());
            statementMovimiento.setString(9, "");
            statementMovimiento.setInt(10, inventarioTB.getIdAlmacen());
            statementMovimiento.addBatch();

            suministroUpdate = inventarioTB.isTipoAjuste()
                    ? dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?")
                    : dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

            suministroUpdateAlmacen = dbf.getConnection().prepareStatement(
                    inventarioTB.isTipoAjuste()
                            ? "UPDATE CantidadTB SET Cantidad = Cantidad + ? WHERE IdAlmacen = ? AND IdSuministro = ?"
                            : "UPDATE CantidadTB SET Cantidad = Cantidad - ? WHERE IdAlmacen = ? AND IdSuministro = ?");

            suministroKardex = dbf.getConnection().prepareStatement("INSERT INTO "
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
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            statementMovimientoDetalle = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "MovimientoInventarioDetalleTB("
                    + "IdMovimientoInventario,"
                    + "IdSuministro,"
                    + "Cantidad,"
                    + "Costo,"
                    + "Precio)"
                    + "VALUES(?,?,?,?,?)");

            for (SuministroTB suministroTB : inventarioTB.getSuministroTBs()) {
                if (suministroTB.getValidar().isSelected()) {
                    statementMovimientoDetalle.setString(1, idMovimiento);
                    statementMovimientoDetalle.setString(2, suministroTB.getIdSuministro());
                    statementMovimientoDetalle.setDouble(3, suministroTB.getMovimiento());
                    statementMovimientoDetalle.setDouble(4, suministroTB.getCostoCompra());
                    statementMovimientoDetalle.setDouble(5, suministroTB.getPrecioVentaGeneral());
                    statementMovimientoDetalle.addBatch();

                    if (inventarioTB.getIdAlmacen() == 0) {
                        suministroUpdate.setDouble(1, suministroTB.getMovimiento());
                        suministroUpdate.setString(2, suministroTB.getIdSuministro());
                        suministroUpdate.addBatch();

                        suministroKardex.setString(1, suministroTB.getIdSuministro());
                        suministroKardex.setString(2, Tools.getDate());
                        suministroKardex.setString(3, Tools.getTime());
                        suministroKardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                        suministroKardex.setInt(5, inventarioTB.getTipoMovimiento());
                        suministroKardex.setString(6, inventarioTB.getObservacion());
                        suministroKardex.setDouble(7, suministroTB.getMovimiento());
                        suministroKardex.setDouble(8, suministroTB.getCostoCompra());
                        suministroKardex.setDouble(9, suministroTB.getMovimiento() * suministroTB.getCostoCompra());
                        suministroKardex.setInt(10, inventarioTB.getIdAlmacen());
                        suministroKardex.addBatch();
                    } else {
                        suministroUpdateAlmacen.setDouble(1, suministroTB.getMovimiento());
                        suministroUpdateAlmacen.setInt(2, inventarioTB.getIdAlmacen());
                        suministroUpdateAlmacen.setString(3, suministroTB.getIdSuministro());
                        suministroUpdateAlmacen.addBatch();

                        suministroKardex.setString(1, suministroTB.getIdSuministro());
                        suministroKardex.setString(2, Tools.getDate());
                        suministroKardex.setString(3, Tools.getTime());
                        suministroKardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                        suministroKardex.setInt(5, inventarioTB.getTipoMovimiento());
                        suministroKardex.setString(6, inventarioTB.getObservacion());
                        suministroKardex.setDouble(7, suministroTB.getMovimiento());
                        suministroKardex.setDouble(8, suministroTB.getCostoCompra());
                        suministroKardex.setDouble(9, suministroTB.getMovimiento() * suministroTB.getCostoCompra());
                        suministroKardex.setInt(10, inventarioTB.getIdAlmacen());
                        suministroKardex.addBatch();
                    }
                }
            }

            statementMovimiento.executeBatch();
            statementMovimientoDetalle.executeBatch();
            suministroUpdate.executeBatch();
            suministroUpdateAlmacen.executeBatch();
            suministroKardex.executeBatch();
            dbf.getConnection().commit();
            return "registered";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMovimiento != null) {
                    statementMovimiento.close();
                }
                if (statementMovimientoDetalle != null) {
                    statementMovimientoDetalle.close();
                }
                if (codigoMovimiento != null) {
                    codigoMovimiento.close();
                }
                if (suministroUpdate != null) {
                    suministroUpdate.close();
                }
                if (suministroUpdateAlmacen != null) {
                    suministroUpdateAlmacen.close();
                }
                if (suministroKardex != null) {
                    suministroKardex.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static Object Listar_Movimiento_Inventario(int opcion, int movimiento, String fechaInicial,
            String fechaFinal, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<AjusteInventarioTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Movimiento_Inventario(?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setInt(2, movimiento);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, posicionPagina);
            preparedStatement.setInt(6, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                AjusteInventarioTB movimientoInventarioTB = new AjusteInventarioTB();
                movimientoInventarioTB.setId(rsEmps.getRow() + posicionPagina);
                movimientoInventarioTB.setIdMovimientoInventario(rsEmps.getString("IdMovimientoInventario"));
                movimientoInventarioTB.setFecha(rsEmps.getDate("Fecha").toLocalDate()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
                movimientoInventarioTB.setHora(
                        rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                movimientoInventarioTB.setTipoAjuste(rsEmps.getBoolean("TipoAjuste"));
                movimientoInventarioTB.setTipoMovimientoName(rsEmps.getString("TipoMovimiento"));
                movimientoInventarioTB.setObservacion(rsEmps.getString("Observacion"));
                movimientoInventarioTB.setInformacion(rsEmps.getString("Informacion"));
                movimientoInventarioTB.setEstadoName(rsEmps.getString("Estado"));

                Label label = new Label(movimientoInventarioTB.getEstadoName());
                if (movimientoInventarioTB.getEstadoName().equalsIgnoreCase("EN PROCESO")) {
                    label.getStyleClass().add("label-medio");
                } else if (movimientoInventarioTB.getEstadoName().equalsIgnoreCase("COMPLETADO")) {
                    label.getStyleClass().add("label-asignacion");
                } else if (movimientoInventarioTB.getEstadoName().equalsIgnoreCase("CANCELADO")) {
                    label.getStyleClass().add("label-proceso");
                }

                movimientoInventarioTB.setLblEstado(label);

                Button btnDetalle = new Button();
                ImageView viDetalle = new ImageView(new Image("/view/image/search.png"));
                viDetalle.setFitWidth(22);
                viDetalle.setFitHeight(22);
                btnDetalle.setGraphic(viDetalle);
                btnDetalle.getStyleClass().add("buttonLightWarning");
                movimientoInventarioTB.setBtnDetalle(btnDetalle);

                Button btnReporte = new Button();
                ImageView vwReporte = new ImageView(new Image("/view/image/reports.png"));
                vwReporte.setFitWidth(22);
                vwReporte.setFitHeight(22);
                btnReporte.setGraphic(vwReporte);
                btnReporte.getStyleClass().add("buttonLightWarning");
                movimientoInventarioTB.setBtnReporte(btnReporte);

                empList.add(movimientoInventarioTB);
            }
            objects[0] = empList;

            preparedStatement = dbf.getConnection()
                    .prepareStatement("{CALL Sp_Listar_Movimiento_Inventario_Count(?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setInt(2, movimiento);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            rsEmps = preparedStatement.executeQuery();
            Integer integer = 0;
            if (rsEmps.next()) {
                integer = rsEmps.getInt("Total");
            }
            objects[1] = integer;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
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

    public static Object Obtener_Movimiento_Inventario_By_Id(String idMovimiento) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementList = null;
        try {
            dbf.dbConnect();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Get_Movimiento_Inventario_By_Id(?)}");
            preparedStatement.setString(1, idMovimiento);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                AjusteInventarioTB inventarioTB = new AjusteInventarioTB();

                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setNombre(resultSet.getString("Almacen"));
                inventarioTB.setAlmacenTB(almacenTB);

                inventarioTB.setTipoMovimientoName(resultSet.getString("TipoMovimiento"));
                inventarioTB.setFecha(
                        resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                inventarioTB.setHora(
                        resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                inventarioTB.setTipoAjuste(resultSet.getBoolean("TipoAjuste"));
                inventarioTB.setTipoMovimiento(resultSet.getInt("TipoMovimientoId"));
                inventarioTB.setObservacion(resultSet.getString("Observacion"));
                inventarioTB.setEstadoName(resultSet.getString("Estado"));

                ArrayList<MovimientoInventarioDetalleTB> movimientoInventarioDetalleTBs = new ArrayList<>();

                preparedStatementList = dbf.getConnection()
                        .prepareStatement("{call Sp_Listar_Movimiento_Inventario_Detalle_By_Id(?)}");
                preparedStatementList.setString(1, idMovimiento);
                ResultSet rsEmps = preparedStatementList.executeQuery();
                while (rsEmps.next()) {
                    MovimientoInventarioDetalleTB detalleTB = new MovimientoInventarioDetalleTB();
                    detalleTB.setId(rsEmps.getRow());
                    detalleTB.setIdMovimientoInventario(rsEmps.getString("IdMovimientoInventario"));
                    detalleTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                    detalleTB.setCantidad(rsEmps.getDouble("Cantidad"));
                    detalleTB.setCosto(rsEmps.getDouble("Costo"));
                    detalleTB.setPrecio(rsEmps.getDouble("Precio"));

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setClave(rsEmps.getString("Clave"));
                    suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompra"));
                    suministroTB.setCategoriaName(rsEmps.getString("Categoria"));
                    suministroTB.setMarcaName(rsEmps.getString("Marca"));
                    detalleTB.setSuministroTB(suministroTB);
                    movimientoInventarioDetalleTBs.add(detalleTB);
                }
                inventarioTB.setMovimientoInventarioDetalleTBs(movimientoInventarioDetalleTBs);

                return inventarioTB;
            } else {
                return "No se puedo obtener la información requerida, intente nuevamente.";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (preparedStatementList != null) {
                    preparedStatementList.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String RegistrarMovimientoSuministro(AjusteInventarioTB inventarioTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementMovimiento = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidar = dbf.getConnection().prepareStatement(
                    "SELECT * FROM MovimientoInventarioTB WHERE IdMovimientoInventario = ? AND Estado <> 0");
            statementValidar.setString(1, inventarioTB.getIdMovimientoInventario());
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "inserted";
            } else {
                statementValidar = dbf.getConnection().prepareStatement(
                        "SELECT * FROM MovimientoInventarioTB WHERE IdMovimientoInventario = ? AND CodigoVerificacion <> ?");
                statementValidar.setString(1, inventarioTB.getIdMovimientoInventario());
                statementValidar.setString(2, inventarioTB.getCodigoVerificacion());
                if (statementValidar.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "nocde";
                } else {

                    statementMovimiento = dbf.getConnection().prepareStatement(
                            "UPDATE MovimientoInventarioTB SET Estado = 1 WHERE IdMovimientoInventario = ?");
                    statementMovimiento.setString(1, inventarioTB.getIdMovimientoInventario());
                    statementMovimiento.addBatch();

                    suministro_update = inventarioTB.isTipoAjuste()
                            ? dbf.getConnection().prepareStatement(
                                    "UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?")
                            : dbf.getConnection().prepareStatement(
                                    "UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                    suministro_kardex = dbf.getConnection().prepareStatement("INSERT INTO "
                            + "KardexSuministroTB("
                            + "IdSuministro,"
                            + "Fecha,"
                            + "Hora,"
                            + "Tipo,"
                            + "Movimiento,"
                            + "Detalle,"
                            + "Cantidad, "
                            + "Costo, "
                            + "Total) "
                            + "VALUES(?,?,?,?,?,?,?,?,?)");

                    for (MovimientoInventarioDetalleTB detalleTB : inventarioTB.getMovimientoInventarioDetalleTBs()) {
                        suministro_update.setDouble(1, detalleTB.getCantidad());
                        suministro_update.setString(2, detalleTB.getIdSuministro());
                        suministro_update.addBatch();

                        suministro_kardex.setString(1, detalleTB.getIdSuministro());
                        suministro_kardex.setString(2, Tools.getDate());
                        suministro_kardex.setString(3, Tools.getTime());
                        suministro_kardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                        suministro_kardex.setInt(5, inventarioTB.getTipoMovimiento());
                        suministro_kardex.setString(6, inventarioTB.getObservacion());
                        suministro_kardex.setDouble(7, detalleTB.getCantidad());
                        suministro_kardex.setDouble(8, detalleTB.getCosto());
                        suministro_kardex.setDouble(9, detalleTB.getCantidad() * detalleTB.getCosto());
                        suministro_kardex.addBatch();
                    }

                    statementMovimiento.executeBatch();
                    suministro_update.executeBatch();
                    suministro_kardex.executeBatch();
                    dbf.getConnection().commit();
                    return "updated";
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMovimiento != null) {
                    statementMovimiento.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (statementValidar != null) {
                    statementValidar.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
