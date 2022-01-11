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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class AjusteInventarioADO {

    public static String Crud_Movimiento_Inventario(AjusteInventarioTB inventarioTB, TableView<SuministroTB> tableView) {
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementMovimiento = null;
            PreparedStatement statementMovimientoDetalle = null;
            PreparedStatement suministro_update = null;
            PreparedStatement suministro_kardex = null;
            CallableStatement codigoMovimiento = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);

                codigoMovimiento = DBUtil.getConnection().prepareCall("{? = call Fc_MovimientoInventario_Codigo_Alfanumerico()}");
                codigoMovimiento.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigoMovimiento.execute();
                String idMovimiento = codigoMovimiento.getString(1);

                statementMovimiento = DBUtil.getConnection().prepareStatement("INSERT INTO "
                        + "MovimientoInventarioTB("
                        + "IdMovimientoInventario,"
                        + "Fecha,"
                        + "Hora,"
                        + "TipoAjuste,"
                        + "TipoMovimiento,"
                        + "Observacion,"
                        + "Suministro,"
                        + "Estado,"
                        + "CodigoVerificacion)"
                        + "VALUES(?,?,?,?,?,?,?,?,?)");
                statementMovimiento.setString(1, idMovimiento);
                statementMovimiento.setString(2, inventarioTB.getFecha());
                statementMovimiento.setString(3, inventarioTB.getHora());
                statementMovimiento.setBoolean(4, inventarioTB.isTipoAjuste());
                statementMovimiento.setInt(5, inventarioTB.getTipoMovimiento());
                statementMovimiento.setString(6, inventarioTB.getObservacion());
                statementMovimiento.setBoolean(7, inventarioTB.isSuministro());
//                statementMovimiento.setBoolean(8, inventarioTB.isArticulo());
//                statementMovimiento.setString(9, inventarioTB.getProveedor() == null ? "" : inventarioTB.getProveedor());
                statementMovimiento.setInt(8, inventarioTB.getEstado());
                statementMovimiento.setString(9, "");
                statementMovimiento.addBatch();

                suministro_update = inventarioTB.isTipoAjuste()
                        ? DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?")
                        : DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                suministro_kardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
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

                statementMovimientoDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO "
                        + "MovimientoInventarioDetalleTB("
                        + "IdMovimientoInventario,"
                        + "IdSuministro,"
                        + "Cantidad,"
                        + "Costo,"
                        + "Precio)"
                        + "VALUES(?,?,?,?,?)");

                for (int i = 0; i < tableView.getItems().size(); i++) {
                    if (tableView.getItems().get(i).getValidar().isSelected()) {
                        statementMovimientoDetalle.setString(1, idMovimiento);
                        statementMovimientoDetalle.setString(2, tableView.getItems().get(i).getIdSuministro());
                        statementMovimientoDetalle.setDouble(3, tableView.getItems().get(i).getMovimiento());
                        statementMovimientoDetalle.setDouble(4, tableView.getItems().get(i).getCostoCompra());
                        statementMovimientoDetalle.setDouble(5, tableView.getItems().get(i).getPrecioVentaGeneral());
                        statementMovimientoDetalle.addBatch();

                        if (inventarioTB.getEstado() == 1) {
                            suministro_update.setDouble(1, tableView.getItems().get(i).getMovimiento());
                            suministro_update.setString(2, tableView.getItems().get(i).getIdSuministro());
                            suministro_update.addBatch();

                            suministro_kardex.setString(1, tableView.getItems().get(i).getIdSuministro());
                            suministro_kardex.setString(2, Tools.getDate());
                            suministro_kardex.setString(3, Tools.getTime());
                            suministro_kardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                            suministro_kardex.setInt(5, inventarioTB.getTipoMovimiento());
                            suministro_kardex.setString(6, inventarioTB.getObservacion());
                            suministro_kardex.setDouble(7, tableView.getItems().get(i).getMovimiento());
                            suministro_kardex.setDouble(8, tableView.getItems().get(i).getCostoCompra());
                            suministro_kardex.setDouble(9, tableView.getItems().get(i).getMovimiento() * tableView.getItems().get(i).getCostoCompra());
                            suministro_kardex.setInt(10, 0);
                            suministro_kardex.addBatch();
                        }
                    }
                }

                statementMovimiento.executeBatch();
                statementMovimientoDetalle.executeBatch();
                suministro_update.executeBatch();
                suministro_kardex.executeBatch();
                DBUtil.getConnection().commit();
                return "registered";
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
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
                    if (suministro_update != null) {
                        suministro_update.close();
                    }
                    if (suministro_kardex != null) {
                        suministro_kardex.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        } else {
            return "No se puedo completar la petición por un problema con su conexión, intente nuevamente.";
        }
    }

    public static String Crud_Movimiento_Inventario_Con_Caja(MovimientoCajaTB movimientoCajaTB, AjusteInventarioTB inventarioTB, TableView<SuministroTB> tableView) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidar = null;
            PreparedStatement statementMovimiento = null;
            PreparedStatement statementMovimientoDetalle = null;
            PreparedStatement suministro_update = null;
            PreparedStatement suministro_kardex = null;
            CallableStatement codigoMovimiento = null;
            PreparedStatement statementMovimientoCaja = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);

                statementValidar = DBUtil.getConnection().prepareStatement("SELECT IdCaja FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
                statementValidar.setString(1, Session.USER_ID);
                if (statementValidar.executeQuery().next()) {

                    ResultSet resultSet = statementValidar.executeQuery();
                    resultSet.next();
                    String idCaja = resultSet.getString("IdCaja");

                    codigoMovimiento = DBUtil.getConnection().prepareCall("{? = call Fc_MovimientoInventario_Codigo_Alfanumerico()}");
                    codigoMovimiento.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigoMovimiento.execute();
                    String idMovimiento = codigoMovimiento.getString(1);

                    statementMovimiento = DBUtil.getConnection().prepareStatement("INSERT INTO "
                            + "MovimientoInventarioTB("
                            + "IdMovimientoInventario,"
                            + "Fecha,"
                            + "Hora,"
                            + "TipoAjuste,"
                            + "TipoMovimiento,"
                            + "Observacion,"
                            + "Suministro,"
                            + "Estado,"
                            + "CodigoVerificacion)"
                            + "VALUES(?,?,?,?,?,?,?,?,?)");
                    statementMovimiento.setString(1, idMovimiento);
                    statementMovimiento.setString(2, inventarioTB.getFecha());
                    statementMovimiento.setString(3, inventarioTB.getHora());
                    statementMovimiento.setBoolean(4, inventarioTB.isTipoAjuste());
                    statementMovimiento.setInt(5, inventarioTB.getTipoMovimiento());
                    statementMovimiento.setString(6, inventarioTB.getObservacion());
                    statementMovimiento.setBoolean(7, inventarioTB.isSuministro());
//                    statementMovimiento.setBoolean(8, inventarioTB.isArticulo());
//                    statementMovimiento.setString(9, inventarioTB.getProveedor() == null ? "" : inventarioTB.getProveedor());
                    statementMovimiento.setInt(8, inventarioTB.getEstado());
                    statementMovimiento.setString(9, "");
                    statementMovimiento.addBatch();

                    suministro_update = inventarioTB.isTipoAjuste() ? DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?")
                            : DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                    suministro_kardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
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

                    statementMovimientoDetalle = DBUtil.getConnection().prepareStatement("INSERT INTO "
                            + "MovimientoInventarioDetalleTB("
                            + "IdMovimientoInventario,"
                            + "IdSuministro,"
                            + "Cantidad,"
                            + "Costo,"
                            + "Precio)"
                            + "VALUES(?,?,?,?,?)");

                    for (int i = 0; i < tableView.getItems().size(); i++) {
                        if (tableView.getItems().get(i).getValidar().isSelected()) {
                            statementMovimientoDetalle.setString(1, idMovimiento);
                            statementMovimientoDetalle.setString(2, tableView.getItems().get(i).getIdSuministro());
                            statementMovimientoDetalle.setDouble(3, tableView.getItems().get(i).getMovimiento());
                            statementMovimientoDetalle.setDouble(4, tableView.getItems().get(i).getCostoCompra());
                            statementMovimientoDetalle.setDouble(5, tableView.getItems().get(i).getPrecioVentaGeneral());
                            statementMovimientoDetalle.addBatch();

                            if (inventarioTB.getEstado() == 1) {
                                suministro_update.setDouble(1, tableView.getItems().get(i).getMovimiento());
                                suministro_update.setString(2, tableView.getItems().get(i).getIdSuministro());
                                suministro_update.addBatch();

                                suministro_kardex.setString(1, tableView.getItems().get(i).getIdSuministro());
                                suministro_kardex.setString(2, Tools.getDate());
                                suministro_kardex.setString(3, Tools.getTime());
                                suministro_kardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                                suministro_kardex.setInt(5, inventarioTB.getTipoMovimiento());
                                suministro_kardex.setString(6, inventarioTB.getObservacion());
                                suministro_kardex.setDouble(7, tableView.getItems().get(i).getMovimiento());
                                suministro_kardex.setDouble(8, tableView.getItems().get(i).getCostoCompra());
                                suministro_kardex.setDouble(9, tableView.getItems().get(i).getMovimiento() * tableView.getItems().get(i).getCostoCompra());
                                suministro_kardex.addBatch();
                            }
                        }
                    }

                    statementMovimientoCaja = DBUtil.getConnection().prepareStatement("INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto)VALUES(?,?,?,?,?,?)");
                    statementMovimientoCaja.setString(1, idCaja);
                    statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
                    statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
                    statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario());
                    statementMovimientoCaja.setInt(5, movimientoCajaTB.getTipoMovimiento());
                    statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
                    statementMovimientoCaja.addBatch();

                    statementMovimiento.executeBatch();
                    statementMovimientoDetalle.executeBatch();
                    suministro_update.executeBatch();
                    suministro_kardex.executeBatch();
                    statementMovimientoCaja.executeBatch();
                    DBUtil.getConnection().commit();
                    result = "registered";
                } else {
                    DBUtil.getConnection().rollback();
                    result = "nocaja";
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
                    if (statementMovimiento != null) {
                        statementMovimiento.close();
                    }
                    if (statementMovimientoDetalle != null) {
                        statementMovimientoDetalle.close();
                    }
                    if (codigoMovimiento != null) {
                        codigoMovimiento.close();
                    }
                    if (suministro_update != null) {
                        suministro_update.close();
                    }
                    if (suministro_kardex != null) {
                        suministro_kardex.close();
                    }
                    if (statementMovimientoCaja != null) {
                        statementMovimientoCaja.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo completar la petición por un problema con su conexión, intente nuevamente.";
        }
        return result;
    }

    public static Object Listar_Movimiento_Inventario(int opcion, int movimiento, String fechaInicial, String fechaFinal, int posicionPagina, int filasPorPagina) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<AjusteInventarioTB> empList = FXCollections.observableArrayList();

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Movimiento_Inventario(?,?,?,?,?,?)}");
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
                movimientoInventarioTB.setFecha(rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
                movimientoInventarioTB.setHora(rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                movimientoInventarioTB.setTipoAjuste(rsEmps.getBoolean("TipoAjuste"));
                movimientoInventarioTB.setTipoMovimientoName(rsEmps.getString("TipoMovimiento"));
                movimientoInventarioTB.setObservacion(rsEmps.getString("Observacion"));
                movimientoInventarioTB.setInformacion(rsEmps.getString("Informacion"));
//                movimientoInventarioTB.setProveedor(rsEmps.getString("Proveedor").toUpperCase());
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

                Button btn = new Button();
                btn.getStyleClass().add("buttonLightWarning");
                btn.setText("Ver");
                movimientoInventarioTB.setValidar(btn);
                empList.add(movimientoInventarioTB);
            }
            objects[0] = empList;

            preparedStatement = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Movimiento_Inventario_Count(?,?,?,?)}");
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

    public static ArrayList<Object> Obtener_Movimiento_Inventario_By_Id(String idMovimiento) {
        ArrayList<Object> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedStatement = null;
            PreparedStatement preparedStatementList = null;

            AjusteInventarioTB inventarioTB = null;
            ObservableList<MovimientoInventarioDetalleTB> empList = FXCollections.observableArrayList();
            try {
                preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Get_Movimiento_Inventario_By_Id(?)}");
                preparedStatement.setString(1, idMovimiento);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    inventarioTB = new AjusteInventarioTB();
                    inventarioTB.setTipoMovimientoName(resultSet.getString("TipoMovimiento"));
                    inventarioTB.setFecha(resultSet.getString("Fecha"));
                    inventarioTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    inventarioTB.setTipoAjuste(resultSet.getBoolean("TipoAjuste"));
                    inventarioTB.setTipoMovimiento(resultSet.getInt("TipoMovimientoId"));
                    inventarioTB.setObservacion(resultSet.getString("Observacion"));
//                    inventarioTB.setProveedor(resultSet.getString("Proveedor").toUpperCase());
                    inventarioTB.setEstadoName(resultSet.getString("Estado"));
                }
                list.add(inventarioTB);

                preparedStatementList = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Movimiento_Inventario_Detalle_By_Id(?)}");
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

                    CheckBox checkBox = new CheckBox();
                    checkBox.setDisable(true);
                    checkBox.getStyleClass().add("check-box-movimiento");
                    detalleTB.setVerificar(checkBox);

                    CheckBox checkBoxPrecios = new CheckBox();
                    checkBoxPrecios.getStyleClass().add("check-box-movimiento");
                    detalleTB.setActualizarPrecio(checkBoxPrecios);

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setClave(rsEmps.getString("Clave"));
                    suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    detalleTB.setSuministroTB(suministroTB);
                    empList.add(detalleTB);
                }

                list.add(empList);

            } catch (SQLException ex) {
                System.out.println("Error en:" + ex.getLocalizedMessage());
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (preparedStatementList != null) {
                        preparedStatementList.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        }
        return list;
    }

    public static String RegistrarMovimientoSuministro(AjusteInventarioTB inventarioTB, ObservableList<MovimientoInventarioDetalleTB> tableView) {
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidar = null;
            PreparedStatement statementMovimiento = null;
            PreparedStatement suministro_update = null;
            PreparedStatement suministro_kardex = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);

                statementValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM MovimientoInventarioTB WHERE IdMovimientoInventario = ? AND Estado <> 0");
                statementValidar.setString(1, inventarioTB.getIdMovimientoInventario());
                if (statementValidar.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    return "inserted";
                } else {
                    statementValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM MovimientoInventarioTB WHERE IdMovimientoInventario = ? AND CodigoVerificacion <> ?");
                    statementValidar.setString(1, inventarioTB.getIdMovimientoInventario());
                    statementValidar.setString(2, inventarioTB.getCodigoVerificacion());
                    if (statementValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        return "nocde";
                    } else {

                        statementMovimiento = DBUtil.getConnection().prepareStatement("UPDATE MovimientoInventarioTB SET Estado = 1 WHERE IdMovimientoInventario = ?");
                        statementMovimiento.setString(1, inventarioTB.getIdMovimientoInventario());
                        statementMovimiento.addBatch();

                        suministro_update = inventarioTB.isTipoAjuste() ? DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?")
                                : DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                        suministro_kardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
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

                        for (int i = 0; i < tableView.size(); i++) {
                            suministro_update.setDouble(1, tableView.get(i).getCantidad());
                            suministro_update.setString(2, tableView.get(i).getIdSuministro());
                            suministro_update.addBatch();

                            suministro_kardex.setString(1, tableView.get(i).getIdSuministro());
                            suministro_kardex.setString(2, Tools.getDate());
                            suministro_kardex.setString(3, Tools.getTime());
                            suministro_kardex.setShort(4, inventarioTB.isTipoAjuste() ? (short) 1 : (short) 2);
                            suministro_kardex.setInt(5, inventarioTB.getTipoMovimiento());
                            suministro_kardex.setString(6, inventarioTB.getObservacion());
                            suministro_kardex.setDouble(7, tableView.get(i).getCantidad());
                            suministro_kardex.setDouble(8, tableView.get(i).getCosto());
                            suministro_kardex.setDouble(9, tableView.get(i).getCantidad() * tableView.get(i).getCosto());
                            suministro_kardex.addBatch();
                        }

                        statementMovimiento.executeBatch();
                        suministro_update.executeBatch();
                        suministro_kardex.executeBatch();
                        DBUtil.getConnection().commit();
                        return "updated";
                    }
                }
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
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
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        } else {
            return "No se puedo completar la petición por un problema con su conexión, intente nuevamente.";
        }
    }

}
