package model;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CotizacionADO {

    public static Object CrudCotizacion(CotizacionTB cotizacionTB) {

        CallableStatement statementCodigoCotizacion = null;
        PreparedStatement statementValidacion = null;
        PreparedStatement statementCotizacion = null;
        PreparedStatement statementDetalleCotizacion = null;
        PreparedStatement statementDetalleCotizacionBorrar = null;

        DBUtil.dbConnect();
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
                        + ",Observaciones=?"
                        + ",IdVenta='' "
                        + "WHERE IdCotizacion = ?");

                statementCotizacion.setString(1, cotizacionTB.getIdCliente());
                statementCotizacion.setString(2, cotizacionTB.getIdVendedor());
                statementCotizacion.setInt(3, cotizacionTB.getIdMoneda());
                statementCotizacion.setString(4, cotizacionTB.getFechaCotizacion());
                statementCotizacion.setString(5, cotizacionTB.getHoraCotizacion());
                statementCotizacion.setString(6, cotizacionTB.getFechaVencimiento());
                statementCotizacion.setString(7, cotizacionTB.getHoraVencimiento());
                statementCotizacion.setInt(8, cotizacionTB.getEstado());
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
                        + ",IdImpuesto"
                        + ",IdMedida)"
                        + "VALUES(?,?,?,?,?,?,?)");

                for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                    statementDetalleCotizacion.setString(1, cotizacionTB.getIdCotizacion());
                    statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                    statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                    statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                    statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                    statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                    statementDetalleCotizacion.setInt(7, detalleCotizacionTB.getSuministroTB().getUnidadCompra());
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
                        + ",Observaciones"
                        + ",IdVenta)"
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                statementCotizacion.setString(1, idCotizacion);
                statementCotizacion.setString(2, cotizacionTB.getIdCliente());
                statementCotizacion.setString(3, cotizacionTB.getIdVendedor());
                statementCotizacion.setInt(4, cotizacionTB.getIdMoneda());
                statementCotizacion.setString(5, cotizacionTB.getFechaCotizacion());
                statementCotizacion.setString(6, cotizacionTB.getHoraCotizacion());
                statementCotizacion.setString(7, cotizacionTB.getFechaVencimiento());
                statementCotizacion.setString(8, cotizacionTB.getHoraVencimiento());
                statementCotizacion.setInt(9, cotizacionTB.getEstado());
                statementCotizacion.setString(10, cotizacionTB.getObservaciones());
                statementCotizacion.setString(11, cotizacionTB.getIdVenta());
                statementCotizacion.addBatch();

                statementDetalleCotizacion = DBUtil.getConnection().prepareStatement("INSERT INTO DetalleCotizacionTB"
                        + "(IdCotizacion"
                        + ",IdSuministro"
                        + ",Cantidad"
                        + ",Precio"
                        + ",Descuento"
                        + ",IdImpuesto"
                        + ",IdMedida)"
                        + "VALUES(?,?,?,?,?,?,?)");

                for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                    statementDetalleCotizacion.setString(1, idCotizacion);
                    statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                    statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                    statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                    statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                    statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                    statementDetalleCotizacion.setInt(7, detalleCotizacionTB.getSuministroTB().getUnidadCompra());
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
    }

    public static Object Listar_Cotizacion(int opcion, String buscar, String fechaInicio, String fechaFinal, int posicionPagina, int filasPorPagina) {
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
                cotizacionTB.setObservaciones(result.getString("Observaciones"));
                cotizacionTB.setEstado(result.getInt("Estado"));

                VentaTB ventaTB = new VentaTB();
                ventaTB.setComprobanteName(result.getString("Comprobante"));
                ventaTB.setSerie(result.getString("Serie"));
                ventaTB.setNumeracion(result.getString("Numeracion"));
                cotizacionTB.setIdVenta(buscar);

                Label lblEstado = new Label(cotizacionTB.getEstado() == 1 ? "SIN USO" : ventaTB.getComprobanteName() + "\n" + ventaTB.getSerie() + "-" + ventaTB.getNumeracion());
                lblEstado.getStyleClass().add("labelRoboto13");
                lblEstado.setAlignment(Pos.CENTER_LEFT);
                lblEstado.setStyle(cotizacionTB.getEstado() == 1 ? "-fx-text-fill:#020203;" : "-fx-text-fill:#0a6f25;");
                lblEstado.setMinHeight(30);
                lblEstado.setPrefHeight(30);
                cotizacionTB.setLblEstado(lblEstado);

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(result.getString("Apellidos"));
                empleadoTB.setNombres(result.getString("Nombres"));
                cotizacionTB.setEmpleadoTB(empleadoTB);

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(result.getString("NumeroDocumento"));
                clienteTB.setInformacion(result.getString("Informacion"));
                cotizacionTB.setClienteTB(clienteTB);

                cotizacionTB.setMonedaTB(new MonedaTB(result.getString("SimboloMoneda")));
                cotizacionTB.setTotal(result.getDouble("Total"));
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

    public static Object Obtener_Cotizacion_ById(String idCotizacion) {
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
                cotizacionTB.setFechaCotizacion(result.getDate("FechaCotizacion").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraCotizacion(result.getTime("HoraCotizacion").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                cotizacionTB.setFechaVencimiento(result.getDate("FechaVencimiento").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraVencimiento(result.getTime("HoraVencimiento").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                cotizacionTB.setObservaciones(result.getString("Observaciones"));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(result.getString("IdCliente"));
                clienteTB.setNumeroDocumento(result.getString("NumeroDocumento"));
                clienteTB.setInformacion(result.getString("Informacion"));
                clienteTB.setTelefono(result.getString("Telefono"));
                clienteTB.setCelular(result.getString("Celular"));
                clienteTB.setEmail(result.getString("Email"));
                clienteTB.setDireccion(result.getString("Direccion"));
                cotizacionTB.setClienteTB(clienteTB);

                cotizacionTB.setIdMoneda(result.getInt("IdMoneda"));
                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(result.getInt("IdMoneda"));
                monedaTB.setNombre(result.getString("Moneda"));
                monedaTB.setSimbolo(result.getString("Simbolo"));
                cotizacionTB.setMonedaTB(monedaTB);

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setIdEmpleado(result.getString("IdEmpleado"));
                empleadoTB.setNumeroDocumento(result.getString("NumDocEmpleado"));
                empleadoTB.setApellidos(result.getString("Apellidos"));
                empleadoTB.setNombres(result.getString("Nombres"));
                cotizacionTB.setEmpleadoTB(empleadoTB);

                statementDetalleCotizacione = DBUtil.getConnection().prepareStatement("{CALL Sp_Obtener_Detalle_Cotizacion_ById(?)}");
                statementDetalleCotizacione.setString(1, idCotizacion);
                result = statementDetalleCotizacione.executeQuery();
                ArrayList<CotizacionDetalleTB> cotizacionTBs = new ArrayList();
                while (result.next()) {
                    CotizacionDetalleTB cotizacionDetalleTB = new CotizacionDetalleTB();
                    cotizacionDetalleTB.setId(result.getRow());
                    cotizacionDetalleTB.setIdSuministro(result.getString("IdSuministro"));

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setIdSuministro(result.getString("IdSuministro"));
                    suministroTB.setClave(result.getString("Clave"));
                    suministroTB.setNombreMarca(result.getString("NombreMarca"));
                    suministroTB.setUnidadCompra(result.getInt("IdMedida"));
                    suministroTB.setUnidadCompraName(result.getString("UnidadCompraName"));
                    suministroTB.setInventario(result.getBoolean("Inventario"));
                    suministroTB.setUnidadVenta(result.getInt("UnidadVenta"));
                    suministroTB.setValorInventario(result.getShort("ValorInventario"));
                    suministroTB.setCostoCompra(result.getDouble("PrecioCompra"));
                    cotizacionDetalleTB.setSuministroTB(suministroTB);

                    cotizacionDetalleTB.setCantidad(result.getDouble("Cantidad"));
                    cotizacionDetalleTB.setPrecio(result.getDouble("Precio"));
                    cotizacionDetalleTB.setDescuento(result.getDouble("Descuento"));

                    cotizacionDetalleTB.setIdImpuesto(result.getInt("IdImpuesto"));
                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(result.getInt("IdImpuesto"));
                    impuestoTB.setOperacion(result.getInt("Operacion"));
                    impuestoTB.setNombreImpuesto(result.getString("ImpuestoNombre"));
                    impuestoTB.setValor(result.getDouble("Valor"));
                    cotizacionDetalleTB.setImpuestoTB(impuestoTB);

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");
                    cotizacionDetalleTB.setBtnRemove(button);

                    cotizacionTBs.add(cotizacionDetalleTB);
                }
                cotizacionTB.setCotizacionDetalleTBs(cotizacionTBs);
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
