package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;

public class CotizacionADO {

    public static Object CrudCotizacion(CotizacionTB cotizacionTB) {

        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            CallableStatement statementCodigoCotizacion = null;
            PreparedStatement statementValidacion = null;
            PreparedStatement statementCotizacion = null;
            PreparedStatement statementDetalleCotizacion = null;
            PreparedStatement statementDetalleCotizacionBorrar = null;
            try {
                String result[] = new String[2];
                DBUtil.getConnection().setAutoCommit(false);

                statementValidacion = DBUtil.getConnection().prepareStatement("SELECT * FROM CotizacionTB WHERE IdCotizacion = ?");
                statementValidacion.setString(1, cotizacionTB.getIdCotizacion());
                if (statementValidacion.executeQuery().next()) {

                    statementCotizacion = DBUtil.getConnection().prepareStatement("UPDATE CotizacionTB SET "
                            + "IdCliente=?"
                            + ",IdVendedor=?"
                            + ",IdMoneda=?"
                            + ",FechaCotizacion=?"
                            + ",HoraCotizacion=?"
                            + ",FechaVencimiento=?"
                            + ",HoraVencimiento=?"
                            + ",Estado=?"
                            + ",Observaciones=? "
                            + "WHERE IdCotizacion = ?");

                    statementCotizacion.setString(1, cotizacionTB.getIdCliente());
                    statementCotizacion.setString(2, cotizacionTB.getIdVendedor());
                    statementCotizacion.setInt(3, cotizacionTB.getIdMoneda());
                    statementCotizacion.setString(4, cotizacionTB.getFechaCotizacion());
                    statementCotizacion.setString(5, cotizacionTB.getHoraCotizacion());
                    statementCotizacion.setString(6, cotizacionTB.getFechaVencimiento());
                    statementCotizacion.setString(7, cotizacionTB.getHoraVencimiento());
                    statementCotizacion.setShort(8, cotizacionTB.getEstado());
                    statementCotizacion.setString(9, cotizacionTB.getObservaciones());
                    statementCotizacion.setString(10, cotizacionTB.getIdCotizacion());
                    statementCotizacion.addBatch();

                    statementDetalleCotizacionBorrar = DBUtil.getConnection().prepareStatement("DELETE FROM DetalleCotizacionTB WHERE IdCotizacion = ?");
                    statementDetalleCotizacionBorrar.setString(1, cotizacionTB.getIdCotizacion());
                    statementDetalleCotizacionBorrar.addBatch();
                    statementDetalleCotizacionBorrar.executeBatch();

                    statementDetalleCotizacion = DBUtil.getConnection().prepareStatement("INSERT INTO DetalleCotizacionTB"
                            + "(IdCotizacion"
                            + ",IdSuministro"
                            + ",Cantidad"
                            + ",Precio"
                            + ",Descuento"
                            + ",IdImpuesto)"
                            + "VALUES(?,?,?,?,?,?)");

                    for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                        statementDetalleCotizacion.setString(1, cotizacionTB.getIdCotizacion());
                        statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                        statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                        statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                        statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                        statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                        statementDetalleCotizacion.addBatch();
                    }

                    statementCotizacion.executeBatch();
                    statementDetalleCotizacion.executeBatch();
                    DBUtil.getConnection().commit();
                    result[0] = "1";
                    result[1] = cotizacionTB.getIdCotizacion();
                    return result;
                } else {

                    statementCodigoCotizacion = DBUtil.getConnection().prepareCall("{? = call Fc_Cotizacion_Codigo_Alfanumerico()}");
                    statementCodigoCotizacion.registerOutParameter(1, java.sql.Types.VARCHAR);
                    statementCodigoCotizacion.execute();
                    String idCotizacion = statementCodigoCotizacion.getString(1);

                    statementCotizacion = DBUtil.getConnection().prepareStatement("INSERT INTO CotizacionTB"
                            + "(IdCotizacion"
                            + ",IdCliente"
                            + ",IdVendedor"
                            + ",IdMoneda"
                            + ",FechaCotizacion"
                            + ",HoraCotizacion"
                            + ",FechaVencimiento"
                            + ",HoraVencimiento"
                            + ",Estado"
                            + ",Observaciones)"
                            + "VALUES(?,?,?,?,?,?,?,?,?,?)");
                    statementCotizacion.setString(1, idCotizacion);
                    statementCotizacion.setString(2, cotizacionTB.getIdCliente());
                    statementCotizacion.setString(3, cotizacionTB.getIdVendedor());
                    statementCotizacion.setInt(4, cotizacionTB.getIdMoneda());
                    statementCotizacion.setString(5, cotizacionTB.getFechaCotizacion());
                    statementCotizacion.setString(6, cotizacionTB.getHoraCotizacion());
                    statementCotizacion.setString(7, cotizacionTB.getFechaVencimiento());
                    statementCotizacion.setString(8, cotizacionTB.getHoraVencimiento());
                    statementCotizacion.setShort(9, cotizacionTB.getEstado());
                    statementCotizacion.setString(10, cotizacionTB.getObservaciones());
                    statementCotizacion.addBatch();

                    statementDetalleCotizacion = DBUtil.getConnection().prepareStatement("INSERT INTO DetalleCotizacionTB"
                            + "(IdCotizacion"
                            + ",IdSuministro"
                            + ",Cantidad"
                            + ",Precio"
                            + ",Descuento"
                            + ",IdImpuesto)"
                            + "VALUES(?,?,?,?,?,?)");

                    for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                        statementDetalleCotizacion.setString(1, idCotizacion);
                        statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                        statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                        statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                        statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                        statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                        statementDetalleCotizacion.addBatch();
                    }

                    statementCotizacion.executeBatch();
                    statementDetalleCotizacion.executeBatch();
                    DBUtil.getConnection().commit();
                    result[0] = "0";
                    result[1] = idCotizacion;
                    return result;
                }
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                } catch (SQLException e) {
                    return e.getLocalizedMessage();
                }
                return ex.getLocalizedMessage();
            } catch (Exception ex) {
                return ex.getLocalizedMessage();
            } finally {
                try {
                    if (statementCodigoCotizacion != null) {
                        statementCodigoCotizacion.close();
                    }
                    if (statementValidacion != null) {
                        statementValidacion.close();
                    }
                    if (statementCotizacion != null) {
                        statementCotizacion.close();
                    }
                    if (statementDetalleCotizacion != null) {
                        statementDetalleCotizacion.close();
                    }
                    if (statementDetalleCotizacionBorrar != null) {
                        statementDetalleCotizacionBorrar.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    return ex.getLocalizedMessage();
                }
            }
        } else {
            return "No se pudo conectar con el servidor, intente nuevamente";
        }
    }

    public static Object ListarCotizacion(int opcion, String buscar, String fechaInicio, String fechaFinal, int posicionPagina, int filasPorPagina) {
        PreparedStatement statementCotizaciones = null;
        ResultSet result = null;
        try {
            DBUtil.dbConnect();
            Object[] object = new Object[2];

            ObservableList<CotizacionTB> cotizacionTBs = FXCollections.observableArrayList();
            statementCotizaciones = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Cotizacion(?,?,?,?,?,?)}");
            statementCotizaciones.setInt(1, opcion);
            statementCotizaciones.setString(2, buscar);
            statementCotizaciones.setString(3, fechaInicio);
            statementCotizaciones.setString(4, fechaFinal);
            statementCotizaciones.setInt(5, posicionPagina);
            statementCotizaciones.setInt(6, filasPorPagina);
            result = statementCotizaciones.executeQuery();
            while (result.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(result.getRow());
                cotizacionTB.setIdCotizacion(result.getString("IdCotizacion"));
                cotizacionTB.setFechaCotizacion(result.getDate("FechaCotizacion").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraCotizacion(result.getTime("HoraCotizacion").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cotizacionTB.setEmpleadoTB(new EmpleadoTB(result.getString("Apellidos"), result.getString("Nombres")));
                cotizacionTB.setClienteTB(new ClienteTB(result.getString("Informacion")));
                cotizacionTB.setMonedaTB(new MonedaTB(result.getString("SimboloMoneda")));
                cotizacionTB.setImporteNeto(result.getDouble("Total"));
                cotizacionTBs.add(cotizacionTB);
            }

            statementCotizaciones = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Cotizacion_Count(?,?,?,?)}");
            statementCotizaciones.setInt(1, opcion);
            statementCotizaciones.setString(2, buscar);
            statementCotizaciones.setString(3, fechaInicio);
            statementCotizaciones.setString(4, fechaFinal);
            result = statementCotizaciones.executeQuery();
            Integer cantidadTotal = 0;
            if (result.next()) {
                cantidadTotal = result.getInt("Total");
            }

            object[0] = cotizacionTBs;
            object[1] = cantidadTotal;

            return object;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCotizaciones != null) {
                    statementCotizaciones.close();
                }
                if (result != null) {
                    result.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object CargarCotizacionVenta(String idCotizacion) {

        ObservableList<SuministroTB> cotizacionTBs = FXCollections.observableArrayList();
        PreparedStatement statementCotizacione = null;
        PreparedStatement statementDetalleCotizacione = null;
        ResultSet result = null;
        try {
            DBUtil.dbConnect();
            statementCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Cotizacion_ById(?)}");
            statementCotizacione.setString(1, idCotizacion);
            result = statementCotizacione.executeQuery();
            if (result.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(result.getRow());
                cotizacionTB.setIdCotizacion(result.getString("IdCotizacion"));
                cotizacionTB.setClienteTB(new ClienteTB(result.getString("IdCliente"), result.getInt("TipoDocumento"), result.getString("NumeroDocumento"), result.getString("Informacion"), result.getString("Celular"), result.getString("Email"), result.getString("Direccion")));
                cotizacionTB.setIdMoneda(result.getInt("IdMoneda"));
                cotizacionTB.setObservaciones(result.getString("Observaciones"));

                statementDetalleCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Cotizacion_ById(?)}");
                statementDetalleCotizacione.setString(1, idCotizacion);
                result = statementDetalleCotizacione.executeQuery();
                while (result.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(result.getRow());
                    suministroTB.setIdSuministro(result.getString("IdSuministro"));
                    suministroTB.setClave(result.getString("Clave"));
                    suministroTB.setNombreMarca(result.getString("NombreMarca"));
//                    suministroTB.setUnidadCompraName(result.getString("UnidadCompraNombre"));
                    suministroTB.setCantidad(result.getDouble("Cantidad"));
                    suministroTB.setBonificacion(0);
                    suministroTB.setCostoCompra(result.getDouble("PrecioCompra"));

                    double valor_sin_impuesto = result.getDouble("Precio") / ((result.getDouble("Valor") / 100.00) + 1);
                    double descuento = suministroTB.getDescuento();
                    double porcentajeRestante = valor_sin_impuesto * (descuento / 100.00);
                    double preciocalculado = valor_sin_impuesto - porcentajeRestante;

                    suministroTB.setDescuento(result.getDouble("Descuento"));
                    suministroTB.setDescuentoCalculado(porcentajeRestante);
                    suministroTB.setDescuentoSumado(porcentajeRestante * suministroTB.getCantidad());

                    suministroTB.setPrecioVentaGeneralUnico(valor_sin_impuesto);
                    suministroTB.setPrecioVentaGeneralReal(preciocalculado);

                    suministroTB.setIdImpuesto(result.getInt("Impuesto"));
                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(result.getInt("Impuesto"));
                    impuestoTB.setOperacion(result.getInt("Operacion"));
                    impuestoTB.setNombreImpuesto(result.getString("ImpuestoNombre"));
                    impuestoTB.setValor(result.getDouble("Valor"));
                    suministroTB.setImpuestoTB(impuestoTB);

                    double impuesto = Tools.calculateTax(suministroTB.getImpuestoTB().getValor(), suministroTB.getPrecioVentaGeneralReal());
                    suministroTB.setImpuestoSumado(suministroTB.getCantidad() * impuesto);
                    suministroTB.setPrecioVentaGeneral(suministroTB.getPrecioVentaGeneralReal() + impuesto);
                    suministroTB.setPrecioVentaGeneralAuxiliar(suministroTB.getPrecioVentaGeneral());

                    suministroTB.setImporteBruto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralUnico());
                    suministroTB.setSubImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneralReal());
                    suministroTB.setImporteNeto(suministroTB.getCantidad() * suministroTB.getPrecioVentaGeneral());

                    suministroTB.setInventario(result.getBoolean("Inventario"));
                    suministroTB.setUnidadVenta(result.getInt("UnidadVenta"));
                    suministroTB.setValorInventario(result.getShort("ValorInventario"));

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");

                    suministroTB.setBtnRemove(button);
                    cotizacionTBs.add(suministroTB);
                    cotizacionTB.setDetalleSuministroTBs(cotizacionTBs);
                }
                return cotizacionTB;
            } else {
                throw new Exception("No se puedo contrar la cotización, intente nuevamente por favor.");
            }

        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCotizacione != null) {
                    statementCotizacione.close();
                }
                if (statementDetalleCotizacione != null) {
                    statementDetalleCotizacione.close();
                }
                if (result != null) {
                    result.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object CargarCotizacionEditar(String idCotizacion) {
        PreparedStatement statementCotizacione = null;
        PreparedStatement statementDetalleCotizacione = null;
        ResultSet result = null;
        try {
            DBUtil.dbConnect();

            statementCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Cotizacion_ById(?)}");
            statementCotizacione.setString(1, idCotizacion);
            result = statementCotizacione.executeQuery();
            if (result.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(result.getRow());
                cotizacionTB.setIdCotizacion(result.getString("IdCotizacion"));
                cotizacionTB.setFechaCotizacion(result.getString("FechaCotizacion"));
                cotizacionTB.setFechaVencimiento(result.getString("FechaVencimiento"));
                cotizacionTB.setClienteTB(new ClienteTB(result.getString("IdCliente"), result.getInt("TipoDocumento"), result.getString("NumeroDocumento"), result.getString("Informacion"), result.getString("Celular"), result.getString("Email"), result.getString("Direccion")));
                cotizacionTB.setIdMoneda(result.getInt("IdMoneda"));
                cotizacionTB.setObservaciones(result.getString("Observaciones"));

                statementDetalleCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Cotizacion_ById(?)}");
                statementDetalleCotizacione.setString(1, idCotizacion);
                result = statementDetalleCotizacione.executeQuery();
                ObservableList<SuministroTB> cotizacionTBs = FXCollections.observableArrayList();
                while (result.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(result.getRow());
                    suministroTB.setIdSuministro(result.getString("IdSuministro"));
                    suministroTB.setClave(result.getString("Clave"));
                    suministroTB.setNombreMarca(result.getString("NombreMarca"));
//                    suministroTB.setUnidadCompraName(result.getString("UnidadCompraNombre"));
                    suministroTB.setCantidad(result.getDouble("Cantidad"));
                    suministroTB.setCostoCompra(result.getDouble("PrecioCompra"));
                    suministroTB.setDescuento(result.getDouble("Descuento"));
                    suministroTB.setPrecioVentaGeneral(result.getDouble("Precio"));
                    double descuento = suministroTB.getDescuento();
                    double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
                    suministroTB.setImporteNeto(suministroTB.getCantidad() * precioDescuento);

                    suministroTB.setIdImpuesto(result.getInt("Impuesto"));
                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(result.getInt("Impuesto"));
                    impuestoTB.setOperacion(result.getInt("Operacion"));
                    impuestoTB.setNombreImpuesto(result.getString("ImpuestoNombre"));
                    impuestoTB.setValor(result.getDouble("Valor"));
                    suministroTB.setImpuestoTB(impuestoTB);

                    suministroTB.setInventario(result.getBoolean("Inventario"));
                    suministroTB.setUnidadVenta(result.getInt("UnidadVenta"));
                    suministroTB.setValorInventario(result.getShort("ValorInventario"));

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");
                    suministroTB.setBtnRemove(button);

                    cotizacionTBs.add(suministroTB);
                }
                cotizacionTB.setDetalleSuministroTBs(cotizacionTBs);
                return cotizacionTB;
            } else {
                throw new Exception("No se puedo contrar la cotización, intente nuevamente por favor.");
            }

        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCotizacione != null) {
                    statementCotizacione.close();
                }
                if (statementDetalleCotizacione != null) {
                    statementDetalleCotizacione.close();
                }
                if (result != null) {
                    result.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object CargarCotizacionById(String idCotizacion) {
        PreparedStatement statementCotizacione = null;
        PreparedStatement statementDetalleCotizacione = null;
        ResultSet result = null;
        try {
            DBUtil.dbConnect();
            statementCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Cotizacion_Reporte_ById(?)}");
            statementCotizacione.setString(1, idCotizacion);
            result = statementCotizacione.executeQuery();
            if (result.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(result.getRow());
                cotizacionTB.setIdCotizacion(idCotizacion);
                cotizacionTB.setFechaCotizacion(result.getDate("FechaCotizacion").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraCotizacion(result.getTime("HoraCotizacion").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cotizacionTB.setEmpleadoTB(new EmpleadoTB(result.getString("Apellidos"), result.getString("Nombres")));
                cotizacionTB.setClienteTB(new ClienteTB(result.getString("IdCliente"), result.getInt("TipoDocumento"), result.getString("NumeroDocumento"), result.getString("Informacion"), result.getString("Telefono"), result.getString("Celular"), result.getString("Email"), result.getString("Direccion")));
                cotizacionTB.setMonedaTB(new MonedaTB(result.getString("Nombre"), result.getString("Simbolo")));
                cotizacionTB.setObservaciones(result.getString("Observaciones"));
                cotizacionTB.setImporteBruto(result.getDouble("SubTotal"));
                cotizacionTB.setDescuento(result.getDouble("Descuento"));
                cotizacionTB.setSubImporteNeto(result.getDouble("SubImporte"));
                cotizacionTB.setImpuesto(result.getDouble("Impuesto"));
                cotizacionTB.setImporteNeto(result.getDouble("Total"));

                ObservableList<SuministroTB> cotizacionTBs = FXCollections.observableArrayList();
                statementDetalleCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Cotizacion_Reporte_ById(?)}");
                statementDetalleCotizacione.setString(1, idCotizacion);
                result = statementDetalleCotizacione.executeQuery();
                while (result.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(result.getRow());
                    suministroTB.setIdSuministro(result.getString("IdSuministro"));
                    suministroTB.setClave(result.getString("Clave"));
                    suministroTB.setNombreMarca(result.getString("NombreMarca"));
                    suministroTB.setUnidadCompraName(result.getString("UnidadCompraNombre"));
                    suministroTB.setCantidad(result.getDouble("Cantidad"));
                    suministroTB.setPrecioVentaGeneral(result.getDouble("Precio"));
                    suministroTB.setDescuento(result.getDouble("Descuento"));

                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setValor(result.getDouble("Valor"));
                    suministroTB.setImpuestoTB(impuestoTB);

                    double descuento = suministroTB.getDescuento();
                    double precioDescuento = suministroTB.getPrecioVentaGeneral() - descuento;
                    suministroTB.setImporteNeto(suministroTB.getCantidad() * precioDescuento);

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");

                    suministroTB.setBtnRemove(button);
                    cotizacionTBs.add(suministroTB);
                }
                cotizacionTB.setDetalleSuministroTBs(cotizacionTBs);

                return cotizacionTB;
            } else {
                throw new Exception("No se pudo encontrar datos para mostrar.");
            }

        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCotizacione != null) {
                    statementCotizacione.close();
                }
                if (statementDetalleCotizacione != null) {
                    statementDetalleCotizacione.close();
                }
                if (result != null) {
                    result.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String removeCotizacionById(String idCotizacion) {
        PreparedStatement statementValidate = null;
        PreparedStatement statementCotizacion = null;
        PreparedStatement statementCotizacionDetalle = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM CotizacionTB WHERE IdCotizacion = ?");
            statementValidate.setString(1, idCotizacion);
            if (statementValidate.executeQuery().next()) {
                statementCotizacion = DBUtil.getConnection().prepareStatement("DELETE FROM CotizacionTB WHERE IdCotizacion = ?");
                statementCotizacion.setString(1, idCotizacion);
                statementCotizacion.addBatch();
                statementCotizacion.executeBatch();

                statementCotizacionDetalle = DBUtil.getConnection().prepareStatement("DELETE FROM DetalleCotizacionTB WHERE IdCotizacion = ?");
                statementCotizacionDetalle.setString(1, idCotizacion);
                statementCotizacionDetalle.addBatch();
                statementCotizacionDetalle.executeBatch();

                DBUtil.getConnection().commit();
                return "deleted";
            } else {
                DBUtil.getConnection().rollback();
                return "noid";
            }
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
                if (statementCotizacion != null) {
                    statementCotizacion.close();
                }
                if (statementCotizacionDetalle != null) {
                    statementCotizacionDetalle.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
