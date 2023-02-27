package service;

import controller.tools.SearchComboBox;
import controller.tools.Session;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import model.EmpleadoTB;
import model.MermaTB;
import model.ProduccionTB;
import model.SuministroTB;

public class ProduccionADO {

    public static String Crud_Produccion(ProduccionTB produccionTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement statementProduccionCodigo = null;
        CallableStatement statementMermaCodigo = null;
        PreparedStatement statementValidate = null;
        PreparedStatement statementCorrelativo = null;
        PreparedStatement statementProducion = null;
        PreparedStatement statementProduccionDetalle = null;
        PreparedStatement statementProduccionHistorial = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementSuministroKardex = null;
        PreparedStatement statementInventario = null;
        PreparedStatement statementInventarioKardex = null;
        PreparedStatement statementMerma = null;
        PreparedStatement statementMermaDetalle = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidate = dbf.getConnection().prepareCall("SELECT * FROM ProduccionTB WHERE IdProduccion = ?");
            statementValidate.setString(1, produccionTB.getIdProduccion());
            if (statementValidate.executeQuery().next()) {

                statementValidate = dbf.getConnection().prepareCall("SELECT * FROM ProduccionTB WHERE IdProduccion = ? AND Estado <> 2");
                statementValidate.setString(1, produccionTB.getIdProduccion());
                if (statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "state";
                } else {
                    statementProducion = dbf.getConnection().prepareStatement("UPDATE ProduccionTB SET Dias = Dias + ?, Horas = Horas + ?, Minutos = Minutos + ?,CostoAdicioanal = ?,Cantidad = ?,CantidadVariable = ?,Estado = ?,Descripcion=? WHERE IdProduccion = ?");
                    statementProducion.setInt(1, produccionTB.getDias());
                    statementProducion.setInt(2, produccionTB.getHoras());
                    statementProducion.setInt(3, produccionTB.getMinutos());
                    statementProducion.setDouble(4, produccionTB.getCostoAdicional());
                    statementProducion.setDouble(5, produccionTB.getCantidad());
                    statementProducion.setBoolean(6, produccionTB.isCantidadVariable());
                    statementProducion.setInt(7, produccionTB.getEstado());
                    statementProducion.setString(8, produccionTB.getDescripcion());
                    statementProducion.setString(9, produccionTB.getIdProduccion());
                    statementProducion.addBatch();

                    statementProduccionDetalle = dbf.getConnection().prepareStatement("DELETE FROM ProduccionDetalleTB WHERE IdProduccion = ?");
                    statementProduccionDetalle.setString(1, produccionTB.getIdProduccion());
                    statementProduccionDetalle.addBatch();
                    statementProduccionDetalle.executeBatch();

                    statementValidate = dbf.getConnection().prepareStatement("SELECT PrecioCompra FROM SuministroTB WHERE IdSuministro = ? ");
                    statementProduccionDetalle = dbf.getConnection().prepareStatement("INSERT INTO ProduccionDetalleTB(IdProduccion,IdProducto,Cantidad,Costo,Medida)VALUES(?,?,?,?,?)");

                    double costoMateriaPrima = 0;
                    double cantidadTotal = 0;

                    for (SuministroTB suministroTB : produccionTB.getSuministroInsumos()) {
                        statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                        ResultSet resultSet = statementValidate.executeQuery();
                        if (resultSet.next()) {
                            statementProduccionDetalle.setString(1, produccionTB.getIdProduccion());
                            statementProduccionDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                            statementProduccionDetalle.setDouble(3, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                            statementProduccionDetalle.setDouble(4, resultSet.getDouble("PrecioCompra"));
                            statementProduccionDetalle.setDouble(5, Tools.isNumeric(suministroTB.getTxtPeso().getText()) ? Double.parseDouble(suministroTB.getTxtPeso().getText()) : 0);
                            statementProduccionDetalle.addBatch();

                            cantidadTotal += Double.parseDouble(suministroTB.getTxtCantidad().getText());
                            costoMateriaPrima += resultSet.getDouble("PrecioCompra") * Double.parseDouble(suministroTB.getTxtCantidad().getText());
                        }
                    }

                    statementProduccionHistorial = dbf.getConnection().prepareStatement("INSERT INTO ProduccionHistorialTB(IdProduccion,IdEmpleado,Fecha,Hora,Movimiento,Dias,Horas,Minutos,Descripcion) VALUES(?,?,?,?,?,?,?,?,?)");
                    statementProduccionHistorial.setString(1, produccionTB.getIdProduccion());
                    statementProduccionHistorial.setString(2, Session.USER_ID);
                    statementProduccionHistorial.setString(3, produccionTB.getFechaRegistro());
                    statementProduccionHistorial.setString(4, produccionTB.getHoraRegistro());
                    statementProduccionHistorial.setDouble(5, cantidadTotal);
                    statementProduccionHistorial.setInt(6, produccionTB.getDias());
                    statementProduccionHistorial.setInt(7, produccionTB.getHoras());
                    statementProduccionHistorial.setInt(8, produccionTB.getMinutos());
                    statementProduccionHistorial.setString(9, produccionTB.getDescripcion());
                    statementProduccionHistorial.addBatch();

                    statementSuministro = dbf.getConnection().prepareStatement("UPDATE SuministroTB set Cantidad = Cantidad + ?, PrecioCompra = ? where IdSuministro = ?");
                    statementSuministroKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB(IdSuministro,Fecha,Hora,Tipo,Movimiento,Detalle,Cantidad,Costo,Total,IdAlmacen,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                    statementInventario = dbf.getConnection().prepareStatement("UPDATE SuministroTB set Cantidad = (Cantidad - ?) where IdSuministro = ?");
                    statementInventarioKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB(IdSuministro,Fecha,Hora,Tipo,Movimiento,Detalle,Cantidad,Costo, Total,IdAlmacen,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                    statementMerma = dbf.getConnection().prepareStatement("INSERT INTO MermaTB(IdMerma,IdProduccion,IdUsuario) VALUES(?,?,?)");
                    statementMermaDetalle = dbf.getConnection().prepareStatement("INSERT INTO MermaDetalleTB(IdMerma,IdProducto,IdTipoMerma,Cantidad,Costo,Medida) VALUES(?,?,?,?,?,?)");

                    if (produccionTB.getEstado() == 1) {
                        double costoProducto = (costoMateriaPrima + produccionTB.getCostoAdicional()) / produccionTB.getCantidad();

                        for (SuministroTB suministroTB : produccionTB.getSuministroInsumos()) {
                            statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                            ResultSet resultSet = statementValidate.executeQuery();
                            if (resultSet.next()) {

                                statementInventario.setDouble(1, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                                statementInventario.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                                statementInventario.addBatch();

                                statementInventarioKardex.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                                statementInventarioKardex.setString(2, Tools.getDate());
                                statementInventarioKardex.setString(3, Tools.getTime());
                                statementInventarioKardex.setShort(4, (short) 2);
                                statementInventarioKardex.setInt(5, 1);
                                statementInventarioKardex.setString(6, "SALIDA POR PRODUCCIÓN DEL LA NUMERACIÓN " + produccionTB.getIdProduccion());
                                statementInventarioKardex.setDouble(7, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                                statementInventarioKardex.setDouble(8, resultSet.getDouble("PrecioCompra"));
                                statementInventarioKardex.setDouble(9, Double.parseDouble(suministroTB.getTxtCantidad().getText()) * resultSet.getDouble("PrecioCompra"));
                                statementInventarioKardex.setInt(10, 0);
                                statementInventarioKardex.setString(11, Session.USER_ID);
                                statementInventarioKardex.addBatch();
                            }
                        }

                        double merma = 0;
                        if (!produccionTB.getSuministroMermas().isEmpty()) {
                            statementMermaCodigo = dbf.getConnection().prepareCall("{? = call Fc_Merma_Codigo_Alfanumerico()}");
                            statementMermaCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                            statementMermaCodigo.execute();
                            String id_merma = statementMermaCodigo.getString(1);

                            statementMerma.setString(1, id_merma);
                            statementMerma.setString(2, produccionTB.getIdProduccion());
                            statementMerma.setString(3, Session.USER_ID);
                            statementMerma.addBatch();

                            for (SuministroTB suministroTB : produccionTB.getSuministroMermas()) {
                                statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                                ResultSet resultSet = statementValidate.executeQuery();
                                if (resultSet.next()) {
                                    statementMermaDetalle.setString(1, id_merma);
                                    statementMermaDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                                    statementMermaDetalle.setInt(3, suministroTB.getCbTipoMerma().getSelectionModel().getSelectedItem().getIdDetalle());
                                    statementMermaDetalle.setDouble(4, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                                    statementMermaDetalle.setDouble(5, costoProducto);
                                    statementMermaDetalle.setDouble(6, Tools.isNumeric(suministroTB.getTxtPeso().getText()) ? Double.parseDouble(suministroTB.getTxtPeso().getText()) : 0);
                                    statementMermaDetalle.addBatch();

                                    merma += Double.parseDouble(suministroTB.getTxtCantidad().getText());
                                }
                            }
                        }

                        statementSuministro.setDouble(1, produccionTB.getCantidad() - merma);
                        statementSuministro.setDouble(2, costoProducto);
                        statementSuministro.setString(3, produccionTB.getIdProducto());
                        statementSuministro.addBatch();

                        statementSuministroKardex.setString(1, produccionTB.getIdProducto());
                        statementSuministroKardex.setString(2, Tools.getDate());
                        statementSuministroKardex.setString(3, Tools.getTime());
                        statementSuministroKardex.setShort(4, (short) 1);
                        statementSuministroKardex.setInt(5, 2);
                        statementSuministroKardex.setString(6, "INGRESO POR PRODUCCIÓN DEL LA NUMERACIÓN " + produccionTB.getIdProduccion());
                        statementSuministroKardex.setDouble(7, produccionTB.getCantidad() - merma);
                        statementSuministroKardex.setDouble(8, costoProducto);
                        statementSuministroKardex.setDouble(9, (produccionTB.getCantidad() - merma) * costoProducto);
                        statementSuministroKardex.setInt(10, 0);
                        statementSuministroKardex.setString(11, Session.USER_ID);
                        statementSuministroKardex.addBatch();

                    }

                    statementProducion.executeBatch();
                    statementProduccionDetalle.executeBatch();
                    statementProduccionHistorial.executeBatch();
                    statementMerma.executeBatch();
                    statementMermaDetalle.executeBatch();
                    statementSuministro.executeBatch();
                    statementSuministroKardex.executeBatch();
                    statementInventario.executeBatch();
                    statementInventarioKardex.executeBatch();
                    dbf.getConnection().commit();
                    return "updated";
                }

            } else {

                statementProduccionCodigo = dbf.getConnection().prepareCall("{? = call Fc_Produccion_Codigo_Alfanumerico()}");
                statementProduccionCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                statementProduccionCodigo.execute();
                String id_produccion = statementProduccionCodigo.getString(1);

                statementCorrelativo = dbf.getConnection().prepareStatement("SELECT MAX(Correlativo) AS Correlativo FROM ProduccionTB");
                int correlativo = 1;
                ResultSet resultCorr = statementCorrelativo.executeQuery();
                if (resultCorr.next()) {
                    correlativo = resultCorr.getInt("Correlativo") + 1;
                }

                statementProducion = dbf.getConnection().prepareStatement("INSERT INTO ProduccionTB("
                        + "IdProduccion,"
                        + "Proyecto,"
                        + "FechaInico,"
                        + "HoraInicio,"
                        + "Dias,"
                        + "Horas,"
                        + "Minutos,"
                        + "IdProducto,"
                        + "TipoOrden,"
                        + "IdEncargado,"
                        + "Descripcion,"
                        + "FechaRegistro,"
                        + "HoraRegistro,"
                        + "Cantidad,"
                        + "CantidadVariable,"
                        + "CostoAdicioanal,"
                        + "Correlativo,"
                        + "Estado"
                        + ")VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                statementProducion.setString(1, id_produccion);
                statementProducion.setString(2, produccionTB.getProyecto());
                statementProducion.setString(3, produccionTB.getFechaInicio());
                statementProducion.setString(4, produccionTB.getHoraInicio());
                statementProducion.setInt(5, produccionTB.getDias());
                statementProducion.setInt(6, produccionTB.getHoras());
                statementProducion.setInt(7, produccionTB.getMinutos());
                statementProducion.setString(8, produccionTB.getIdProducto());
                statementProducion.setBoolean(9, produccionTB.isTipoOrden());
                statementProducion.setString(10, produccionTB.getIdEncargado());
                statementProducion.setString(11, produccionTB.getDescripcion());
                statementProducion.setString(12, produccionTB.getFechaRegistro());
                statementProducion.setString(13, produccionTB.getHoraRegistro());
                statementProducion.setDouble(14, produccionTB.getCantidad());
                statementProducion.setBoolean(15, produccionTB.isCantidadVariable());
                statementProducion.setDouble(16, produccionTB.getCostoAdicional());
                statementProducion.setInt(17, correlativo);
                statementProducion.setInt(18, produccionTB.getEstado());
                statementProducion.addBatch();

                statementValidate = dbf.getConnection().prepareStatement("SELECT PrecioCompra FROM SuministroTB WHERE IdSuministro = ? ");
                statementProduccionDetalle = dbf.getConnection().prepareStatement("INSERT INTO ProduccionDetalleTB(IdProduccion,IdProducto,Cantidad,Costo,Medida)VALUES(?,?,?,?,?)");

                double costoMateriaPrima = 0;
                double cantidadTotal = 0;

                for (SuministroTB suministroTB : produccionTB.getSuministroInsumos()) {
                    statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                    ResultSet resultSet = statementValidate.executeQuery();
                    if (resultSet.next()) {
                        statementProduccionDetalle.setString(1, id_produccion);
                        statementProduccionDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                        statementProduccionDetalle.setDouble(3, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                        statementProduccionDetalle.setDouble(4, resultSet.getDouble("PrecioCompra"));
                        statementProduccionDetalle.setDouble(5, Tools.isNumeric(suministroTB.getTxtPeso().getText()) ? Double.parseDouble(suministroTB.getTxtPeso().getText()) : 0);
                        statementProduccionDetalle.addBatch();

                        cantidadTotal += Double.parseDouble(suministroTB.getTxtCantidad().getText());
                        costoMateriaPrima += resultSet.getDouble("PrecioCompra") * Double.parseDouble(suministroTB.getTxtCantidad().getText());
                    }
                }

                statementProduccionHistorial = dbf.getConnection().prepareStatement("INSERT INTO ProduccionHistorialTB(IdProduccion,IdEmpleado,Fecha,Hora,Movimiento,Dias,Horas,Minutos,Descripcion) VALUES(?,?,?,?,?,?,?,?,?)");
                statementProduccionHistorial.setString(1, id_produccion);
                statementProduccionHistorial.setString(2, Session.USER_ID);
                statementProduccionHistorial.setString(3, produccionTB.getFechaRegistro());
                statementProduccionHistorial.setString(4, produccionTB.getHoraRegistro());
                statementProduccionHistorial.setDouble(5, cantidadTotal);
                statementProduccionHistorial.setInt(6, produccionTB.getDias());
                statementProduccionHistorial.setInt(7, produccionTB.getHoras());
                statementProduccionHistorial.setInt(8, produccionTB.getMinutos());
                statementProduccionHistorial.setString(9, produccionTB.getDescripcion());
                statementProduccionHistorial.addBatch();

                statementSuministro = dbf.getConnection().prepareStatement("UPDATE SuministroTB set Cantidad = Cantidad + ?, PrecioCompra = ? where IdSuministro = ?");
                statementSuministroKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB(IdSuministro,Fecha,Hora,Tipo,Movimiento,Detalle,Cantidad,Costo,Total,IdAlmacen,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                statementInventario = dbf.getConnection().prepareStatement("UPDATE SuministroTB set Cantidad = (Cantidad - ?) where IdSuministro = ?");
                statementInventarioKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB(IdSuministro,Fecha,Hora,Tipo,Movimiento,Detalle,Cantidad,Costo, Total,IdAlmacen,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                statementMerma = dbf.getConnection().prepareStatement("INSERT INTO MermaTB(IdMerma,IdProduccion,IdUsuario) VALUES(?,?,?)");
                statementMermaDetalle = dbf.getConnection().prepareStatement("INSERT INTO MermaDetalleTB(IdMerma,IdProducto,IdTipoMerma,Cantidad,Costo,Medida) VALUES(?,?,?,?,?,?)");

                if (produccionTB.getEstado() == 1) {
                    double costoProducto = (costoMateriaPrima + produccionTB.getCostoAdicional()) / produccionTB.getCantidad();

                    for (SuministroTB suministroTB : produccionTB.getSuministroInsumos()) {
                        statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                        ResultSet resultSet = statementValidate.executeQuery();
                        if (resultSet.next()) {

                            statementInventario.setDouble(1, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                            statementInventario.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                            statementInventario.addBatch();

                            statementInventarioKardex.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                            statementInventarioKardex.setString(2, Tools.getDate());
                            statementInventarioKardex.setString(3, Tools.getTime());
                            statementInventarioKardex.setShort(4, (short) 2);
                            statementInventarioKardex.setInt(5, 1);
                            statementInventarioKardex.setString(6, "SALIDA POR PRODUCCIÓN DEL LA NUMERACIÓN " + id_produccion);
                            statementInventarioKardex.setDouble(7, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                            statementInventarioKardex.setDouble(8, resultSet.getDouble("PrecioCompra"));
                            statementInventarioKardex.setDouble(9, Double.parseDouble(suministroTB.getTxtCantidad().getText()) * resultSet.getDouble("PrecioCompra"));
                            statementInventarioKardex.setInt(10, 0);
                            statementInventarioKardex.setString(11, Session.USER_ID);
                            statementInventarioKardex.addBatch();
                        }
                    }

                    double merma = 0;
                    if (!produccionTB.getSuministroMermas().isEmpty()) {
                        statementMermaCodigo = dbf.getConnection().prepareCall("{? = call Fc_Merma_Codigo_Alfanumerico()}");
                        statementMermaCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                        statementMermaCodigo.execute();
                        String id_merma = statementMermaCodigo.getString(1);

                        statementMerma.setString(1, id_merma);
                        statementMerma.setString(2, id_produccion);
                        statementMerma.setString(3, Session.USER_ID);
                        statementMerma.addBatch();

                        for (SuministroTB suministroTB : produccionTB.getSuministroMermas()) {
                            statementValidate.setString(1, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                            ResultSet resultSet = statementValidate.executeQuery();
                            if (resultSet.next()) {
                                statementMermaDetalle.setString(1, id_merma);
                                statementMermaDetalle.setString(2, suministroTB.getCbSuministro().getSelectionModel().getSelectedItem().getIdSuministro());
                                statementMermaDetalle.setInt(3, suministroTB.getCbTipoMerma().getSelectionModel().getSelectedItem().getIdDetalle());
                                statementMermaDetalle.setDouble(4, Double.parseDouble(suministroTB.getTxtCantidad().getText()));
                                statementMermaDetalle.setDouble(5, costoProducto);
                                statementMermaDetalle.setDouble(6, Tools.isNumeric(suministroTB.getTxtPeso().getText()) ? Double.parseDouble(suministroTB.getTxtPeso().getText()) : 0);
                                statementMermaDetalle.addBatch();

                                merma += Double.parseDouble(suministroTB.getTxtCantidad().getText());
                            }
                        }
                    }

                    statementSuministro.setDouble(1, produccionTB.getCantidad() - merma);
                    statementSuministro.setDouble(2, costoProducto);
                    statementSuministro.setString(3, produccionTB.getIdProducto());
                    statementSuministro.addBatch();

                    statementSuministroKardex.setString(1, produccionTB.getIdProducto());
                    statementSuministroKardex.setString(2, Tools.getDate());
                    statementSuministroKardex.setString(3, Tools.getTime());
                    statementSuministroKardex.setShort(4, (short) 1);
                    statementSuministroKardex.setInt(5, 2);
                    statementSuministroKardex.setString(6, "INGRESO POR PRODUCCIÓN DEL LA NUMERACIÓN " + id_produccion);
                    statementSuministroKardex.setDouble(7, produccionTB.getCantidad() - merma);
                    statementSuministroKardex.setDouble(8, costoProducto);
                    statementSuministroKardex.setDouble(9, (produccionTB.getCantidad() - merma) * costoProducto);
                    statementSuministroKardex.setInt(10, 0);
                    statementSuministroKardex.setString(11, Session.USER_ID);
                    statementSuministroKardex.addBatch();

                }

                statementProducion.executeBatch();
                statementProduccionDetalle.executeBatch();
                statementProduccionHistorial.executeBatch();
                statementInventario.executeBatch();
                statementInventarioKardex.executeBatch();
                statementSuministro.executeBatch();
                statementSuministroKardex.executeBatch();
                statementMerma.executeBatch();
                statementMermaDetalle.executeBatch();
                dbf.getConnection().commit();
                return "inserted";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMermaCodigo != null) {
                    statementMermaCodigo.close();
                }
                if (statementProducion != null) {
                    statementProducion.close();
                }
                if (statementProduccionDetalle != null) {
                    statementProduccionDetalle.close();
                }
                if (statementProduccionCodigo != null) {
                    statementProduccionCodigo.close();
                }
                if (statementProduccionHistorial != null) {
                    statementProduccionHistorial.close();
                }
                if (statementInventario != null) {
                    statementInventario.close();
                }
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementSuministroKardex != null) {
                    statementSuministroKardex.close();
                }
                if (statementInventarioKardex != null) {
                    statementInventarioKardex.close();
                }
                if (statementMerma != null) {
                    statementMerma.close();
                }
                if (statementMermaDetalle != null) {
                    statementMermaDetalle.close();
                }
                if (statementCorrelativo != null) {
                    statementCorrelativo.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {

            }
        }
    }

    public static Object ListarProduccion(int tipo, String fechaInicio, String fechaFinal, String busqueda, int estado, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementListar = null;
        PreparedStatement statementDetalle = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];

            ObservableList<ProduccionTB> produccionTBs = FXCollections.observableArrayList();
            statementListar = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Produccion(?,?,?,?,?,?,?)}");
            statementListar.setInt(1, tipo);
            statementListar.setString(2, fechaInicio);
            statementListar.setString(3, fechaFinal);
            statementListar.setString(4, busqueda);
            statementListar.setInt(5, estado);
            statementListar.setInt(6, posicionPagina);
            statementListar.setInt(7, filasPorPagina);
            ResultSet resultSet = statementListar.executeQuery();
            while (resultSet.next()) {
                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setId(resultSet.getRow() + posicionPagina);
                produccionTB.setIdProduccion(resultSet.getString("IdProduccion"));
                produccionTB.setProyecto(resultSet.getString("Proyecto"));
                produccionTB.setFechaRegistro(resultSet.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM//yyyy")));
                produccionTB.setHoraRegistro(resultSet.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                produccionTB.setFechaInicio(resultSet.getDate("FechaInico").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM//yyyy")));
                produccionTB.setHoraInicio(resultSet.getTime("HoraInicio").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                produccionTB.setDias(resultSet.getInt("Dias"));
                produccionTB.setHoras(resultSet.getInt("Horas"));
                produccionTB.setMinutos(resultSet.getInt("Minutos"));
                produccionTB.setCantidad(resultSet.getDouble("Cantidad"));
                produccionTB.setCantidadVariable(resultSet.getBoolean("CantidadVariable"));
                produccionTB.setCostoAdicional(resultSet.getDouble("CostoAdicioanal"));
                produccionTB.setCosto(0);

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setClave(resultSet.getString("Clave"));
                suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                suministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                produccionTB.setSuministroTB(suministroTB);

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                empleadoTB.setApellidos(resultSet.getString("Apellidos"));
                empleadoTB.setNombres(resultSet.getString("Nombres"));
                produccionTB.setEmpleadoTB(empleadoTB);

                Label label = new Label();
                switch (resultSet.getInt("Estado")) {
                    case 3:
                        produccionTB.setEstado(3);
                        label.setText("ANULADO");
                        label.getStyleClass().add("label-proceso");
                        break;
                    case 2:
                        produccionTB.setEstado(2);
                        label.setText("EN PRODUCCIÓN");
                        label.getStyleClass().add("label-medio");
                        break;
                    default:
                        produccionTB.setEstado(1);
                        label.setText("COMPLETADO");
                        label.getStyleClass().add("label-asignacion");
                        break;
                }
                produccionTB.setLblEstado(label);

                statementDetalle = dbf.getConnection().prepareStatement("select SUM(Cantidad*Costo) AS Costo from ProduccionDetalleTB where IdProduccion = ?");
                statementDetalle.setString(1, resultSet.getString("IdProduccion"));
                ResultSet setDetalle = statementDetalle.executeQuery();
                double costo = 0;
                if (setDetalle.next()) {
                    costo += setDetalle.getDouble("Costo");
                }
                costo += produccionTB.getCostoAdicional();
                produccionTB.setCosto(costo);

                produccionTBs.add(produccionTB);
            }

            statementListar = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Produccion_Count(?,?,?,?,?)}");
            statementListar.setInt(1, tipo);
            statementListar.setString(2, fechaInicio);
            statementListar.setString(3, fechaFinal);
            statementListar.setString(4, busqueda);
            statementListar.setInt(5, estado);
            resultSet = statementListar.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSet.next()) {
                cantidadTotal = resultSet.getInt("Total");
            }

            objects[0] = produccionTBs;
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementListar != null) {
                    statementListar.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Produccion_ById(String idProduccion) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementProduccion = null;
        PreparedStatement statementDetalleProduccion = null;
        PreparedStatement statementMerma = null;
        PreparedStatement statementMermaDetalle = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementProduccion = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Produccion_ById(?)}");
            statementProduccion.setString(1, idProduccion);
            resultSet = statementProduccion.executeQuery();
            if (resultSet.next()) {
                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setFechaRegistro(resultSet.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setFechaInicio(resultSet.getDate("FechaInico").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setDias(resultSet.getInt("Dias"));
                produccionTB.setHoras(resultSet.getInt("Horas"));
                produccionTB.setMinutos(resultSet.getInt("Minutos"));
                produccionTB.setTipoOrden(resultSet.getBoolean("TipoOrden"));
                produccionTB.setDescripcion(resultSet.getString("Descripcion"));
                produccionTB.setIdProducto(resultSet.getString("IdProducto"));
                produccionTB.setCantidad(resultSet.getInt("Cantidad"));
                produccionTB.setCantidadVariable(resultSet.getBoolean("CantidadVariable"));
                produccionTB.setCostoAdicional(resultSet.getDouble("CostoAdicioanal"));
                SuministroTB newSuministroTB = new SuministroTB(resultSet.getString("IdProducto"), resultSet.getString("Clave"), resultSet.getString("NombreMarca"));
                newSuministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                produccionTB.setSuministroTB(newSuministroTB);
                produccionTB.setEmpleadoTB(new EmpleadoTB(resultSet.getString("IdEmpleado"), resultSet.getString("Apellidos"), resultSet.getString("Nombres")));
                produccionTB.setEstado(resultSet.getInt("Estado"));

                statementDetalleProduccion = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Produccion_ById(?)}");
                statementDetalleProduccion.setString(1, idProduccion);
                resultSet = statementDetalleProduccion.executeQuery();
                ArrayList<SuministroTB> suministroInsumos = new ArrayList();
                while (resultSet.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(resultSet.getRow());
                    suministroTB.setClave(resultSet.getString("Clave"));
                    suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                    suministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                    suministroTB.setCostoCompra(resultSet.getDouble("Costo"));
                    suministroTB.setCantidad(resultSet.getDouble("Cantidad"));
                    suministroInsumos.add(suministroTB);
                }
                produccionTB.setSuministroInsumos(suministroInsumos);

                statementMerma = dbf.getConnection().prepareStatement("SELECT IdMerma,IdProduccion,IdUsuario FROM MermaTB WHERE IdProduccion = ?");
                statementMerma.setString(1, idProduccion);
                resultSet = statementMerma.executeQuery();
                if (resultSet.next()) {
                    MermaTB mermaTB = new MermaTB();
                    mermaTB.setIdMerma(resultSet.getString("IdMerma"));
                    mermaTB.setIdProduccion(idProduccion);

                    statementMermaDetalle = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Merma_ById(?)}");
                    statementMermaDetalle.setString(1, resultSet.getString("IdMerma"));
                    resultSet = statementMermaDetalle.executeQuery();
                    ArrayList<SuministroTB> suministroMerma = new ArrayList();
                    while (resultSet.next()) {
                        SuministroTB suministroTB = new SuministroTB();
                        suministroTB.setId(resultSet.getRow());
                        suministroTB.setClave(resultSet.getString("Clave"));
                        suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                        suministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                        suministroTB.setCostoCompra(resultSet.getDouble("Costo"));
                        suministroTB.setCantidad(resultSet.getDouble("Cantidad"));
                        suministroMerma.add(suministroTB);
                    }
                    mermaTB.setSuministroTBs(suministroMerma);
                    produccionTB.setMermaTB(mermaTB);
                }
                return produccionTB;
            } else {
                throw new Exception("No se pudo obtener los datos de la producción");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementProduccion != null) {
                    statementProduccion.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementDetalleProduccion != null) {
                    statementDetalleProduccion.close();
                }
                if (statementMerma != null) {
                    statementMerma.close();
                }
                if (statementMermaDetalle != null) {
                    statementMermaDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Produccion_Editor_ById(String idProduccion) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementProduccion = null;
        PreparedStatement statementDetalleProduccion = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementProduccion = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Produccion_ById(?)}");
            statementProduccion.setString(1, idProduccion);
            resultSet = statementProduccion.executeQuery();
            if (resultSet.next()) {
                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setFechaRegistro(resultSet.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setHoraRegistro(resultSet.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                produccionTB.setFechaInicio(resultSet.getDate("FechaInico").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setDias(resultSet.getInt("Dias"));
                produccionTB.setHoras(resultSet.getInt("Horas"));
                produccionTB.setMinutos(resultSet.getInt("Minutos"));
                produccionTB.setTipoOrden(resultSet.getBoolean("TipoOrden"));
                produccionTB.setDescripcion(resultSet.getString("Descripcion"));
                produccionTB.setIdProducto(resultSet.getString("IdProducto"));
                produccionTB.setCantidad(resultSet.getInt("Cantidad"));
                produccionTB.setCostoAdicional(resultSet.getDouble("CostoAdicioanal"));
                SuministroTB newSuministroTB = new SuministroTB(resultSet.getString("IdProducto"), resultSet.getString("Clave"), resultSet.getString("NombreMarca"));
                newSuministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                produccionTB.setSuministroTB(newSuministroTB);
                produccionTB.setEmpleadoTB(new EmpleadoTB(resultSet.getString("IdEmpleado"), resultSet.getString("Apellidos"), resultSet.getString("Nombres")));
                produccionTB.setEstado(resultSet.getInt("Estado"));
                produccionTB.setCantidadVariable(resultSet.getBoolean("CantidadVariable"));

                statementDetalleProduccion = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Produccion_ById(?)}");
                statementDetalleProduccion.setString(1, idProduccion);
                resultSet = statementDetalleProduccion.executeQuery();
                ArrayList<SuministroTB> suministroTBs = new ArrayList();
                while (resultSet.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(resultSet.getRow());
                    suministroTB.setIdSuministro(resultSet.getString("IdSuministro"));
                    suministroTB.setClave(resultSet.getString("Clave"));
                    suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                    suministroTB.setUnidadCompraName(resultSet.getString("Medida"));
                    suministroTB.setCostoCompra(resultSet.getDouble("Costo"));
                    suministroTB.setCantidad(resultSet.getDouble("Cantidad"));
                    suministroTB.setPeso(resultSet.getDouble("Peso"));

                    ComboBox<SuministroTB> comboBox = new ComboBox();
                    comboBox.setPromptText("-- Selecionar --");
                    comboBox.setPrefWidth(220);
                    comboBox.setPrefHeight(30);
                    comboBox.setMaxWidth(Double.MAX_VALUE);
                    suministroTB.setCbSuministro(comboBox);

                    SearchComboBox<SuministroTB> searchComboBox = new SearchComboBox<>(suministroTB.getCbSuministro(), false);
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyPressed(t -> {
                        if (t.getCode() == KeyCode.ENTER) {
                            if (!searchComboBox.getSearchComboBoxSkin().getItemView().getItems().isEmpty()) {
                                searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().select(0);
                                searchComboBox.getSearchComboBoxSkin().getItemView().requestFocus();
                            }
                        } else if (t.getCode() == KeyCode.ESCAPE) {
                            searchComboBox.getComboBox().hide();
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getSearchBox().setOnKeyReleased(t -> {
                        if (!Tools.isText(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText())) {
                            searchComboBox.getComboBox().getItems().clear();
                            List<SuministroTB> list = SuministroADO.getSearchComboBoxSuministros(searchComboBox.getSearchComboBoxSkin().getSearchBox().getText().trim(), false);
                            list.forEach(p -> suministroTB.getCbSuministro().getItems().add(p));
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getItemView().setOnKeyPressed(t -> {
                        switch (t.getCode()) {
                            case ENTER:
                            case SPACE:
                            case ESCAPE:
                                searchComboBox.getComboBox().hide();
                                break;
                            case UP:
                            case DOWN:
                            case LEFT:
                            case RIGHT:
                                break;
                            default:
                                searchComboBox.getSearchComboBoxSkin().getSearchBox().requestFocus();
                                searchComboBox.getSearchComboBoxSkin().getSearchBox().selectAll();
                                break;
                        }
                    });
                    searchComboBox.getSearchComboBoxSkin().getItemView().getSelectionModel().selectedItemProperty().addListener((p, o, item) -> {
                        if (item != null) {
                            searchComboBox.getComboBox().getSelectionModel().select(item);
                            if (searchComboBox.getSearchComboBoxSkin().isClickSelection()) {
                                searchComboBox.getComboBox().hide();
                            }
                        }
                    });
                    suministroTB.setSearchComboBoxSuministro(searchComboBox);
                    suministroTB.getCbSuministro().getItems().add(new SuministroTB(suministroTB.getIdSuministro(), suministroTB.getClave(), suministroTB.getNombreMarca()));
                    suministroTB.getCbSuministro().getSelectionModel().select(0);

                    TextField textField = new TextField(Tools.roundingValue(suministroTB.getCantidad(), 2));
                    textField.setPromptText("0.00");
                    textField.getStyleClass().add("text-field-normal");
                    textField.setPrefWidth(220);
                    textField.setPrefHeight(30);
                    textField.setOnKeyTyped(event -> {
                        char c = event.getCharacter().charAt(0);
                        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                            event.consume();
                        }
                        if (c == '.' && textField.getText().contains(".")) {
                            event.consume();
                        }
                    });
                    suministroTB.setTxtCantidad(textField);

                    TextField fieldPeso = new TextField(Tools.roundingValue(suministroTB.getPeso(), 2));
                    fieldPeso.setPromptText("0.00");
                    fieldPeso.getStyleClass().add("text-field-normal");
                    fieldPeso.setPrefWidth(220);
                    fieldPeso.setPrefHeight(30);
                    fieldPeso.setOnKeyTyped(event -> {
                        char c = event.getCharacter().charAt(0);
                        if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                            event.consume();
                        }
                        if (c == '.' && fieldPeso.getText().contains(".")) {
                            event.consume();
                        }
                    });
                    suministroTB.setTxtPeso(fieldPeso);

                    Button button = new Button();
                    button.getStyleClass().add("buttonLightError");
                    button.setAlignment(Pos.CENTER);
                    button.setPrefWidth(Control.USE_COMPUTED_SIZE);
                    button.setPrefHeight(Control.USE_COMPUTED_SIZE);
                    ImageView imageView = new ImageView(new Image("/view/image/remove-gray.png"));
                    imageView.setFitWidth(20);
                    imageView.setFitHeight(20);
                    button.setGraphic(imageView);

                    suministroTB.setBtnRemove(button);
                    suministroTBs.add(suministroTB);
                }
                produccionTB.setSuministroInsumos(suministroTBs);
                return produccionTB;
            } else {
                throw new Exception("No se pudo obtener los datos de la producción");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementProduccion != null) {
                    statementProduccion.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementDetalleProduccion != null) {
                    statementDetalleProduccion.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String AnularProduccion(String idProduccion) {
        DBUtil dbf = new DBUtil();
//        PreparedStatement statementValidate = null;
        PreparedStatement statementRegistrar = null;
//        PreparedStatement statementDetalle = null;
        String result = "";
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
//            if (estado == 2) {
            statementRegistrar = dbf.getConnection().prepareStatement("UPDATE ProduccionTB SET Estado = 3 WHERE IdProduccion = ?");
            statementRegistrar.setString(1, idProduccion);
            statementRegistrar.addBatch();

            statementRegistrar.executeBatch();
            dbf.getConnection().commit();
            result = "anulado";
            //            } 
            //            else if (estado == 1) {
            //                statementRegistrar = dbf.getConnection().prepareStatement("UPDATE ProduccionTB SET Estado = 3 WHERE IdProduccion = ?");
            //                statementRegistrar.setString(1, idProduccion);
            //                statementRegistrar.addBatch();
            //
            //                statementValidate = dbf.getConnection().prepareStatement("select IdProducto, CantidadUtilizada from ProduccionDetalleTB WHERE IdProduccion = ?");
            //                statementDetalle = dbf.getConnection().prepareStatement("UPDATE InsumoTB set Cantidad = (Cantidad + ?) where IdInsumo = ?");
            //
            //                statementValidate.setString(1, idProduccion);
            //                ResultSet resultSet = statementValidate.executeQuery();
            //                while (resultSet.next()) {
            //                    statementDetalle.setDouble(1, resultSet.getDouble("CantidadUtilizada"));
            //                    statementDetalle.setString(2, resultSet.getString("IdProducto"));
            //                    statementDetalle.addBatch();
            //                }
            //
            //                statementRegistrar.executeBatch();
            //                statementDetalle.executeBatch();
            //                dbf.getConnection().commit();
            //                result = "anulado";
            //            } 
//            else if(estado == 1){
//                result = "No se puede anular una producción que ya se encuentra en estado COMPLETADO";
//            } else {
//                result = "Dicha producción ya se encuentra anulada";
//            }

            return result;
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementRegistrar != null) {
                    statementRegistrar.close();
                }
//                if (statementDetalle != null) {
//                    statementDetalle.close();
//                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static Object Reporte_Produccion(String fechaInicio, String fechaFinal, int estado, String idEncargado) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementProduccion = null;
        PreparedStatement statementDetalle = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ArrayList<ProduccionTB> arrayList = new ArrayList();
            statementProduccion = dbf.getConnection().prepareStatement("{call Sp_Reporte_Produccion(?,?,?,?)}}");
            statementProduccion.setString(1, fechaInicio);
            statementProduccion.setString(2, fechaFinal);
            statementProduccion.setInt(3, estado);
            statementProduccion.setString(4, idEncargado);
            resultSet = statementProduccion.executeQuery();
            while (resultSet.next()) {
                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setId(resultSet.getRow());
                produccionTB.setIdProduccion(resultSet.getString("IdProduccion"));
                produccionTB.setFechaRegistro(resultSet.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setHoraRegistro(resultSet.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                produccionTB.setFechaInicio(resultSet.getDate("FechaInico").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setHoraInicio(resultSet.getTime("HoraInicio").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                produccionTB.setCostoAdicional(resultSet.getDouble("CostoAdicioanal"));
                produccionTB.setProductoName(resultSet.getString("Clave") + "\n" + resultSet.getString("NombreMarca"));

                produccionTB.setEncargadoName(resultSet.getString("Apellidos") + "\n" + resultSet.getString("Nombres"));
                produccionTB.setCantidad(resultSet.getDouble("Cantidad"));
                produccionTB.setEstado(resultSet.getInt("Estado"));
                produccionTB.setCantidadVariable(resultSet.getBoolean("CantidadVariable"));

                statementDetalle = dbf.getConnection().prepareStatement("select SUM(Cantidad*Costo) AS Costo from ProduccionDetalleTB where IdProduccion = ?");
                statementDetalle.setString(1, resultSet.getString("IdProduccion"));
                double costo = 0;
                try (ResultSet setDetalle = statementDetalle.executeQuery()) {
                    if (setDetalle.next()) {
                        costo += setDetalle.getDouble("Costo");
                    }
                }
                costo += produccionTB.getCostoAdicional();
                produccionTB.setCosto(costo);
                arrayList.add(produccionTB);
            }
            return arrayList;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementProduccion != null) {
                    statementProduccion.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
}
