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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.ClienteTB;
import model.CotizacionDetalleTB;
import model.CotizacionTB;
import model.EmpleadoTB;
import model.ImpuestoTB;
import model.MonedaTB;
import model.SuministroTB;
import model.TipoDocumentoTB;
import model.VentaTB;

public class CotizacionADO {

    public static Object CrudCotizacion(CotizacionTB cotizacionTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement statementCodigoCotizacion = null;
        PreparedStatement statementValidacion = null;
        PreparedStatement statementCotizacion = null;
        PreparedStatement statementDetalleCotizacion = null;
        PreparedStatement statementDetalleCotizacionBorrar = null;

        try {
            String result[] = new String[2];
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidacion = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CotizacionTB WHERE IdCotizacion = ?");
            statementValidacion.setString(1, cotizacionTB.getIdCotizacion());
            if (statementValidacion.executeQuery().next()) {
                statementCotizacion = dbf.getConnection().prepareStatement("UPDATE CotizacionTB SET "
                        + "IdCliente=?"
                        + ",IdVendedor=?"
                        + ",IdMoneda=?"
                        + ",FechaCotizacion=?"
                        + ",HoraCotizacion=?"
                        + ",FechaVencimiento=?"
                        + ",HoraVencimiento=?"
                        + ",Estado=?"
                        + ",Observaciones=?"
                        + ",IdVenta=? "
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
                statementCotizacion.setString(10, cotizacionTB.getIdVenta());
                statementCotizacion.setString(11, cotizacionTB.getIdCotizacion());
                statementCotizacion.addBatch();

                statementDetalleCotizacionBorrar = dbf.getConnection()
                        .prepareStatement("DELETE FROM DetalleCotizacionTB WHERE IdCotizacion = ?");
                statementDetalleCotizacionBorrar.setString(1, cotizacionTB.getIdCotizacion());
                statementDetalleCotizacionBorrar.addBatch();
                statementDetalleCotizacionBorrar.executeBatch();

                statementDetalleCotizacion = dbf.getConnection().prepareStatement("INSERT INTO DetalleCotizacionTB"
                        + "(IdCotizacion"
                        + ",IdSuministro"
                        + ",Cantidad"
                        + ",Precio"
                        + ",Descuento"
                        + ",IdImpuesto"
                        + ",IdMedida"
                        + ",Uso)"
                        + "VALUES(?,?,?,?,?,?,?,?)");

                for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                    statementDetalleCotizacion.setString(1, cotizacionTB.getIdCotizacion());
                    statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                    statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                    statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                    statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                    statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                    statementDetalleCotizacion.setInt(7, detalleCotizacionTB.getSuministroTB().getUnidadCompra());
                    statementDetalleCotizacion.setBoolean(8, detalleCotizacionTB.isUso());
                    statementDetalleCotizacion.addBatch();
                }

                statementCotizacion.executeBatch();
                statementDetalleCotizacion.executeBatch();
                dbf.getConnection().commit();
                result[0] = "1";
                result[1] = cotizacionTB.getIdCotizacion();
                return result;
            } else {

                statementCodigoCotizacion = dbf.getConnection()
                        .prepareCall("{? = call Fc_Cotizacion_Codigo_Alfanumerico()}");
                statementCodigoCotizacion.registerOutParameter(1, java.sql.Types.VARCHAR);
                statementCodigoCotizacion.execute();
                String idCotizacion = statementCodigoCotizacion.getString(1);

                statementCotizacion = dbf.getConnection().prepareStatement("INSERT INTO CotizacionTB"
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

                statementDetalleCotizacion = dbf.getConnection().prepareStatement("INSERT INTO DetalleCotizacionTB"
                        + "(IdCotizacion"
                        + ",IdSuministro"
                        + ",Cantidad"
                        + ",Precio"
                        + ",Descuento"
                        + ",IdImpuesto"
                        + ",IdMedida"
                        + ",Uso)"
                        + "VALUES(?,?,?,?,?,?,?,?)");

                for (CotizacionDetalleTB detalleCotizacionTB : cotizacionTB.getCotizacionDetalleTBs()) {
                    statementDetalleCotizacion.setString(1, idCotizacion);
                    statementDetalleCotizacion.setString(2, detalleCotizacionTB.getIdSuministro());
                    statementDetalleCotizacion.setDouble(3, detalleCotizacionTB.getCantidad());
                    statementDetalleCotizacion.setDouble(4, detalleCotizacionTB.getPrecio());
                    statementDetalleCotizacion.setDouble(5, detalleCotizacionTB.getDescuento());
                    statementDetalleCotizacion.setInt(6, detalleCotizacionTB.getIdImpuesto());
                    statementDetalleCotizacion.setInt(7, detalleCotizacionTB.getSuministroTB().getUnidadCompra());
                    statementDetalleCotizacion.setBoolean(8, detalleCotizacionTB.isUso());
                    statementDetalleCotizacion.addBatch();
                }

                statementCotizacion.executeBatch();
                statementDetalleCotizacion.executeBatch();
                dbf.getConnection().commit();
                result[0] = "0";
                result[1] = idCotizacion;
                return result;
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Listar_Cotizacion(int opcion, String buscar, String fechaInicio, String fechaFinal,
            int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementCotizaciones = null;
        PreparedStatement statementTotales = null;
        PreparedStatement statementUso = null;

        ResultSet resultCotizacion = null;
        ResultSet resultTotales = null;
        ResultSet resultUse = null;

        try {
            dbf.dbConnect();
            Object[] object = new Object[2];

            ObservableList<CotizacionTB> cotizacionTBs = FXCollections.observableArrayList();
            statementCotizaciones = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Cotizacion(?,?,?,?,?,?)}");
            statementCotizaciones.setInt(1, opcion);
            statementCotizaciones.setString(2, buscar);
            statementCotizaciones.setString(3, fechaInicio);
            statementCotizaciones.setString(4, fechaFinal);
            statementCotizaciones.setInt(5, posicionPagina);
            statementCotizaciones.setInt(6, filasPorPagina);
            resultCotizacion = statementCotizaciones.executeQuery();
            while (resultCotizacion.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(resultCotizacion.getRow());
                cotizacionTB.setIdCotizacion(resultCotizacion.getString("IdCotizacion"));
                cotizacionTB.setFechaCotizacion(resultCotizacion.getDate("FechaCotizacion").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraCotizacion(resultCotizacion.getTime("HoraCotizacion").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                cotizacionTB.setObservaciones(resultCotizacion.getString("Observaciones"));
                cotizacionTB.setEstado(resultCotizacion.getInt("Estado"));

                VentaTB ventaTB = new VentaTB();
                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setNombre(resultCotizacion.getString("Comprobante"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);
                ventaTB.setSerie(resultCotizacion.getString("Serie"));
                ventaTB.setNumeracion(resultCotizacion.getString("Numeracion"));

                statementUso = dbf.getConnection()
                        .prepareStatement("{CALL Sp_Total_Productos_Usado_Cotizacion_ById(?)}");
                statementUso.setString(1, resultCotizacion.getString("IdCotizacion"));
                resultUse = statementUso.executeQuery();

                int uso = 0;
                if (resultUse.next()) {
                    uso = resultUse.getInt("Uso");
                }

                Label lblEstado = new Label(cotizacionTB.getEstado() == 1 ? "SIN USO"
                        : uso == 0 ? "FALTANTES"
                                : "ASOCIADO \n" + ventaTB.getSerie() + "-"
                                        + Tools.formatNumber(ventaTB.getNumeracion()));
                lblEstado.getStyleClass().add("labelRoboto12");
                lblEstado.setAlignment(Pos.CENTER_LEFT);
                lblEstado.setStyle(uso == 1 ? "-fx-text-fill:#0a6f25;" : "-fx-text-fill:#da1414;");
                lblEstado.setPrefHeight(44);
                cotizacionTB.setLblEstado(lblEstado);

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(resultCotizacion.getString("Apellidos"));
                empleadoTB.setNombres(resultCotizacion.getString("Nombres"));
                cotizacionTB.setEmpleadoTB(empleadoTB);

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(resultCotizacion.getString("NumeroDocumento"));
                clienteTB.setInformacion(resultCotizacion.getString("Informacion"));
                cotizacionTB.setClienteTB(clienteTB);

                cotizacionTB.setMonedaTB(new MonedaTB(resultCotizacion.getString("SimboloMoneda")));
                cotizacionTB.setTotal(resultCotizacion.getDouble("Total"));
                cotizacionTBs.add(cotizacionTB);
            }

            statementTotales = dbf.getConnection().prepareStatement("{call Sp_Listar_Cotizacion_Count(?,?,?,?)}");
            statementTotales.setInt(1, opcion);
            statementTotales.setString(2, buscar);
            statementTotales.setString(3, fechaInicio);
            statementTotales.setString(4, fechaFinal);
            resultTotales = statementTotales.executeQuery();
            Integer cantidadTotal = 0;
            if (resultTotales.next()) {
                cantidadTotal = resultTotales.getInt("Total");
            }

            object[0] = cotizacionTBs;
            object[1] = cantidadTotal;

            return object;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementCotizaciones != null) {
                    statementCotizaciones.close();
                }
                if (statementTotales != null) {
                    statementTotales.close();
                }
                if (statementUso != null) {
                    statementUso.close();
                }
                if (resultCotizacion != null) {
                    resultCotizacion.close();
                }
                if (resultTotales != null) {
                    resultTotales.close();
                }
                if (resultUse != null) {
                    resultUse.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Cotizacion_ById(String idCotizacion, boolean venta) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementCotizacione = null;
        PreparedStatement statementDetalleCotizacione = null;
        ResultSet result = null;
        try {
            dbf.dbConnect();

            statementCotizacione = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_Cotizacion_ById(?)}");
            statementCotizacione.setString(1, idCotizacion);
            result = statementCotizacione.executeQuery();
            if (result.next()) {
                CotizacionTB cotizacionTB = new CotizacionTB();
                cotizacionTB.setId(result.getRow());
                cotizacionTB.setIdCotizacion(result.getString("IdCotizacion"));
                cotizacionTB.setFechaCotizacion(result.getDate("FechaCotizacion").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraCotizacion(result.getTime("HoraCotizacion").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                cotizacionTB.setFechaVencimiento(result.getDate("FechaVencimiento").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cotizacionTB.setHoraVencimiento(result.getTime("HoraVencimiento").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
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

                cotizacionTB.setIdVenta(result.getString("IdVenta"));

                if (cotizacionTB.getIdVenta().equalsIgnoreCase("")) {
                    cotizacionTB.setIdVenta("");
                } else {
                    VentaTB ventaTB = new VentaTB();
                    ventaTB.setSerie(result.getString("Serie"));
                    ventaTB.setNumeracion(result.getString("Numeracion"));
                    cotizacionTB.setVentaTB(ventaTB);
                }

                result.close();

                statementDetalleCotizacione = dbf.getConnection()
                        .prepareStatement("{CALL Sp_Obtener_Detalle_Cotizacion_ById(?)}");
                statementDetalleCotizacione.setString(1, idCotizacion);
                result = statementDetalleCotizacione.executeQuery();
                ArrayList<CotizacionDetalleTB> cotizacionTBs = new ArrayList<>();
                while (result.next()) {
                    if (venta) {
                        if (!result.getBoolean("Uso")) {
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
                            cotizacionDetalleTB.setUso(result.getBoolean("Uso"));

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
                    } else {
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
                        cotizacionDetalleTB.setUso(result.getBoolean("Uso"));

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
                }
                cotizacionTB.setCotizacionDetalleTBs(cotizacionTBs);

                return cotizacionTB;
            } else {
                throw new Exception("No se puedo contrar la cotización, intente nuevamente por favor.");
            }

        } catch (SQLException | ClassNotFoundException ex) {
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String removeCotizacionById(String idCotizacion) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementCotizacion = null;
        PreparedStatement statementCotizacionDetalle = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CotizacionTB WHERE IdCotizacion = ?");
            statementValidate.setString(1, idCotizacion);
            if (statementValidate.executeQuery().next()) {
                statementCotizacion = dbf.getConnection()
                        .prepareStatement("DELETE FROM CotizacionTB WHERE IdCotizacion = ?");
                statementCotizacion.setString(1, idCotizacion);
                statementCotizacion.addBatch();
                statementCotizacion.executeBatch();

                statementCotizacionDetalle = dbf.getConnection()
                        .prepareStatement("DELETE FROM DetalleCotizacionTB WHERE IdCotizacion = ?");
                statementCotizacionDetalle.setString(1, idCotizacion);
                statementCotizacionDetalle.addBatch();
                statementCotizacionDetalle.executeBatch();

                dbf.getConnection().commit();
                return "deleted";
            } else {
                dbf.getConnection().rollback();
                return "noid";
            }
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
                if (statementCotizacion != null) {
                    statementCotizacion.close();
                }
                if (statementCotizacionDetalle != null) {
                    statementCotizacionDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
