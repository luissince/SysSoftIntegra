package service;

import controller.tools.Session;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.ClienteTB;
import model.DetalleVentaTB;
import model.EmpleadoTB;
import model.EmpresaTB;
import model.HistorialSuministroSalidaTB;
import model.ImpuestoTB;
import model.IngresoTB;
import model.ModeloObject;
import model.MonedaTB;
import model.MovimientoCajaTB;
import model.NotaCreditoTB;
import model.ResultTransaction;
import model.SuministroTB;
import model.UbigeoTB;
import model.VentaCreditoTB;
import model.VentaTB;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VentaADO {

    public static ResultTransaction registrarVentaContado(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement comprobante = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement ingreso = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<String>();
            ventaVerificar = dbf.getConnection()
                    .prepareStatement("SELECT Cantidad FROM SuministroTB WHERE IdSuministro = ?");
            for (int i = 0; i < ventaTB.getSuministroTBs().size(); i++) {
                ventaVerificar.setString(1, ventaTB.getSuministroTBs().get(i).getIdSuministro());
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    double ca = ventaTB.getSuministroTBs().get(i).getValorInventario() == 1
                            ? ventaTB.getSuministroTBs().get(i).getCantidad()
                                    + ventaTB.getSuministroTBs().get(i).getBonificacion()
                            : ventaTB.getSuministroTBs().get(i).getCantidad();
                    double cb = resultValidate.getDouble("Cantidad");
                    if (ca > cb) {
                        countValidate++;
                        arrayResult.add(ventaTB.getSuministroTBs().get(i).getClave() + " - "
                                + ventaTB.getSuministroTBs().get(i).getNombreMarca() + " - Cantidad actual("
                                + Tools.roundingValue(cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {
                cliente = dbf.getConnection().prepareStatement(
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente = ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, resultSet.getString("IdCliente"));
                    cliente.addBatch();

                } else {
                    codigoCliente = dbf.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                    codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigoCliente.execute();
                    String idCliente = codigoCliente.getString(1);

                    cliente.setString(1, idCliente);
                    cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(5, "");
                    cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                    cliente.setString(9, "");
                    cliente.setInt(10, 1);
                    cliente.setBoolean(11, false);
                    cliente.setBoolean(12, false);
                    cliente.addBatch();

                    ventaTB.setIdCliente(idCliente);
                }

                serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                serie_numeracion.setInt(2, ventaTB.getIdComprobante());
                serie_numeracion.execute();
                String[] id_comprabante = serie_numeracion.getString(1).split("-");

                codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_venta.execute();

                String id_venta = codigo_venta.getString(1);

                venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                        + "(IdVenta\n"
                        + ",Cliente\n"
                        + ",Vendedor\n"
                        + ",Comprobante\n"
                        + ",Moneda\n"
                        + ",Serie\n"
                        + ",Numeracion\n"
                        + ",FechaVenta\n"
                        + ",HoraVenta\n"
                        + ",FechaVencimiento\n"
                        + ",HoraVencimiento\n"
                        + ",Tipo\n"
                        + ",Estado\n"
                        + ",Observaciones\n"
                        + ",Efectivo\n"
                        + ",Vuelto\n"
                        + ",Tarjeta\n"
                        + ",Codigo\n"
                        + ",Deposito\n"
                        + ",TipoCredito\n"
                        + ",NumeroOperacion\n"
                        + ",Procedencia)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)");

                comprobante = dbf.getConnection().prepareStatement(
                        "INSERT INTO ComprobanteTB(IdTipoDocumento,Serie,Numeracion,FechaRegistro)VALUES(?,?,?,?)");

                cotizacion = dbf.getConnection()
                        .prepareStatement("UPDATE CotizacionTB SET Estado = 2, IdVenta=?  WHERE IdCotizacion = ?");

                detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                        + "(IdVenta\n"
                        + ",IdArticulo\n"
                        + ",Cantidad\n"
                        + ",CostoVenta\n"
                        + ",PrecioVenta\n"
                        + ",Descuento\n"
                        + ",IdOperacion\n"
                        + ",IdImpuesto\n"
                        + ",NombreImpuesto\n"
                        + ",ValorImpuesto\n"
                        + ",Bonificacion\n"
                        + ",PorLlevar\n"
                        + ",Estado\n"
                        + ",IdMedida)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                suministro_kardex = dbf.getConnection().prepareStatement("INSERT INTO "
                        + "KardexSuministroTB( "
                        + "IdSuministro, "
                        + "Fecha, "
                        + "Hora, "
                        + "Tipo, "
                        + "Movimiento, "
                        + "Detalle, "
                        + "Cantidad, "
                        + "Costo, "
                        + "Total, "
                        + "IdAlmacen, "
                        + "IdEmpleado) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                venta.setString(1, id_venta);
                venta.setString(2, ventaTB.getIdCliente());
                venta.setString(3, ventaTB.getVendedor());
                venta.setInt(4, ventaTB.getIdComprobante());
                venta.setInt(5, ventaTB.getIdMoneda());
                venta.setString(6, id_comprabante[0]);
                venta.setString(7, id_comprabante[1]);
                venta.setString(8, ventaTB.getFechaVenta());
                venta.setString(9, ventaTB.getHoraVenta());
                venta.setString(10, ventaTB.getFechaVenta());
                venta.setString(11, ventaTB.getHoraVenta());
                venta.setInt(12, ventaTB.getTipo());
                venta.setInt(13, ventaTB.getEstado());
                venta.setString(14, ventaTB.getObservaciones());
                venta.setDouble(15, ventaTB.getEfectivo());
                venta.setDouble(16, ventaTB.getVuelto());
                venta.setDouble(17, ventaTB.getTarjeta());
                venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
                venta.setDouble(19, ventaTB.getDeposito());
                venta.setInt(20, ventaTB.getTipoCredito());
                venta.setString(21, ventaTB.getNumeroOperacion());
                venta.addBatch();

                comprobante.setInt(1, ventaTB.getIdComprobante());
                comprobante.setString(2, id_comprabante[0]);
                comprobante.setString(3, id_comprabante[1]);
                comprobante.setString(4, ventaTB.getFechaVenta());
                comprobante.addBatch();

                if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                    cotizacion.setString(1, id_venta);
                    cotizacion.setString(2, ventaTB.getIdCotizacion());
                    cotizacion.addBatch();
                }

                suministro_update = dbf.getConnection().prepareStatement("UPDATE "
                        + "SuministroTB "
                        + "SET Cantidad = Cantidad - ? "
                        + "WHERE IdSuministro = ?");

                for (SuministroTB sm : ventaTB.getSuministroTBs()) {

                    // double cantidad = sm.getValorInventario() == 2 ? sm.getImporteNeto() /
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getCantidad();
                    double cantidad = sm.getCantidad();
                    // double precio = sm.getValorInventario() == 2 ?
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                    double precio = sm.getPrecioVentaGeneral();

                    detalle_venta.setString(1, id_venta);
                    detalle_venta.setString(2, sm.getIdSuministro());
                    detalle_venta.setDouble(3, cantidad);
                    detalle_venta.setDouble(4, sm.getCostoCompra());
                    detalle_venta.setDouble(5, precio);
                    detalle_venta.setDouble(6, sm.getDescuento());
                    detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                    detalle_venta.setDouble(8, sm.getIdImpuesto());
                    detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                    detalle_venta.setDouble(11, sm.getBonificacion());
                    detalle_venta.setDouble(12, cantidad);
                    detalle_venta.setString(13, "C");
                    detalle_venta.setInt(14, sm.getUnidadCompra());
                    detalle_venta.addBatch();

                    if (sm.isInventario() && sm.getValorInventario() == 1) {
                        suministro_update.setDouble(1, sm.getCantidad() + sm.getBonificacion());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    } else if (sm.isInventario() && sm.getValorInventario() == 2) {
                        // suministro_update.setDouble(1, sm.getImporteNeto() /
                        // sm.getPrecioVentaGeneralAuxiliar());
                        suministro_update.setDouble(1, sm.getCantidad());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    } else if (sm.isInventario() && sm.getValorInventario() == 3) {
                        suministro_update.setDouble(1, sm.getCantidad());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    }

                    double cantidadKardex = sm.getValorInventario() == 1
                            ? sm.getCantidad() + sm.getBonificacion()
                            : sm.getValorInventario() == 2
                                    // ? sm.getImporteNeto() / sm.getPrecioVentaGeneralAuxiliar()
                                    ? sm.getCantidad()
                                    : sm.getCantidad();

                    suministro_kardex.setString(1, sm.getIdSuministro());
                    suministro_kardex.setString(2, ventaTB.getFechaVenta());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 2);
                    suministro_kardex.setInt(5, 1);
                    suministro_kardex.setString(6, "VENTA CON SERIE Y NUMERACIÓN: " + id_comprabante[0] + "-"
                            + id_comprabante[1]
                            + (sm.getBonificacion() <= 0 ? "" : "(BONIFICACIÓN: " + sm.getBonificacion() + ")"));
                    suministro_kardex.setDouble(7, cantidadKardex);
                    suministro_kardex.setDouble(8, sm.getCostoCompra());
                    suministro_kardex.setDouble(9, cantidadKardex * sm.getCostoCompra());
                    suministro_kardex.setInt(10, 0);
                    suministro_kardex.setString(11, Session.USER_ID);
                    suministro_kardex.addBatch();
                }

                ingreso = dbf.getConnection().prepareStatement(
                        "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?)");

                if (ventaTB.getDeposito() > 0) {
                    ingreso.setString(1, id_venta);
                    ingreso.setString(2, ventaTB.getVendedor());
                    ingreso.setString(3, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(4, 1);
                    ingreso.setString(5, ventaTB.getFechaVenta());
                    ingreso.setString(6, ventaTB.getHoraVenta());
                    ingreso.setInt(7, 3);
                    ingreso.setDouble(8, ventaTB.getDeposito());
                    ingreso.addBatch();

                } else {
                    if (ventaTB.getEfectivo() > 0) {
                        ingreso.setString(1, id_venta);
                        ingreso.setString(2, ventaTB.getVendedor());
                        ingreso.setString(3, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        ingreso.setInt(4, 1);
                        ingreso.setString(5, ventaTB.getFechaVenta());
                        ingreso.setString(6, ventaTB.getHoraVenta());
                        ingreso.setInt(7, 1);
                        ingreso.setDouble(8, ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                        ingreso.addBatch();
                    }

                    if (ventaTB.getTarjeta() > 0) {
                        ingreso.setString(1, id_venta);
                        ingreso.setString(2, ventaTB.getVendedor());
                        ingreso.setString(3, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        ingreso.setInt(4, 1);
                        ingreso.setString(5, ventaTB.getFechaVenta());
                        ingreso.setString(6, ventaTB.getHoraVenta());
                        ingreso.setInt(7, 2);
                        ingreso.setDouble(8, ventaTB.getTarjeta());
                        ingreso.addBatch();
                    }
                }

                cliente.executeBatch();
                venta.executeBatch();
                ingreso.executeBatch();
                comprobante.executeBatch();
                suministro_update.executeBatch();
                detalle_venta.executeBatch();
                suministro_kardex.executeBatch();
                cotizacion.executeBatch();

                dbf.getConnection().commit();
                resultTransaction.setCode("register");
                resultTransaction.setResult(id_venta);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (ventaVerificar != null) {
                    ventaVerificar.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (comprobante != null) {
                    comprobante.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                if (ingreso != null) {
                    ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static ResultTransaction registrarVentaCredito(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement venta_credito_codigo = null;
        PreparedStatement venta_credito = null;
        PreparedStatement ingreso = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<String>();
            ventaVerificar = dbf.getConnection()
                    .prepareStatement("SELECT Cantidad FROM SuministroTB WHERE IdSuministro = ?");
            for (int i = 0; i < ventaTB.getSuministroTBs().size(); i++) {
                ventaVerificar.setString(1, ventaTB.getSuministroTBs().get(i).getIdSuministro());
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    double ca = ventaTB.getSuministroTBs().get(i).getValorInventario() == 1
                            ? ventaTB.getSuministroTBs().get(i).getCantidad()
                                    + ventaTB.getSuministroTBs().get(i).getBonificacion()
                            : ventaTB.getSuministroTBs().get(i).getCantidad();
                    double cb = resultValidate.getDouble("Cantidad");
                    if (ca > cb) {
                        countValidate++;
                        arrayResult.add(ventaTB.getSuministroTBs().get(i).getClave() + " - "
                                + ventaTB.getSuministroTBs().get(i).getNombreMarca() + " - Cantidad actual("
                                + Tools.roundingValue(cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {
                cliente = dbf.getConnection().prepareStatement(
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente =  ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, resultSet.getString("IdCliente"));
                    cliente.addBatch();

                } else {
                    codigoCliente = dbf.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                    codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigoCliente.execute();
                    String idCliente = codigoCliente.getString(1);

                    cliente.setString(1, idCliente);
                    cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(5, "");
                    cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                    cliente.setString(9, "");
                    cliente.setInt(10, 1);
                    cliente.setBoolean(11, false);
                    cliente.setBoolean(12, false);
                    cliente.addBatch();

                    ventaTB.setIdCliente(idCliente);
                }

                serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                serie_numeracion.setInt(2, ventaTB.getIdComprobante());
                serie_numeracion.execute();
                String[] id_comprabante = serie_numeracion.getString(1).split("-");

                codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_venta.execute();

                String id_venta = codigo_venta.getString(1);

                venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                        + "(IdVenta\n"
                        + ",Cliente\n"
                        + ",Vendedor\n"
                        + ",Comprobante\n"
                        + ",Moneda\n"
                        + ",Serie\n"
                        + ",Numeracion\n"
                        + ",FechaVenta\n"
                        + ",HoraVenta\n"
                        + ",FechaVencimiento\n"
                        + ",HoraVencimiento\n"
                        + ",Tipo\n"
                        + ",Estado\n"
                        + ",Observaciones\n"
                        + ",Efectivo\n"
                        + ",Vuelto\n"
                        + ",Tarjeta\n"
                        + ",Codigo\n"
                        + ",Deposito\n"
                        + ",TipoCredito\n"
                        + ",NumeroOperacion\n"
                        + ",Procedencia)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)");

                cotizacion = dbf.getConnection()
                        .prepareStatement("UPDATE CotizacionTB SET Estado = 2,IdVenta=?  WHERE IdCotizacion = ?");

                detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                        + "(IdVenta\n"
                        + ",IdArticulo\n"
                        + ",Cantidad\n"
                        + ",CostoVenta\n"
                        + ",PrecioVenta\n"
                        + ",Descuento\n"
                        + ",IdOperacion\n"
                        + ",IdImpuesto\n"
                        + ",NombreImpuesto\n"
                        + ",ValorImpuesto\n"
                        + ",Bonificacion\n"
                        + ",PorLlevar\n"
                        + ",Estado\n"
                        + ",IdMedida)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                suministro_update = dbf.getConnection().prepareStatement("UPDATE "
                        + "SuministroTB "
                        + "SET Cantidad = Cantidad - ? "
                        + "WHERE IdSuministro = ?");

                suministro_kardex = dbf.getConnection().prepareStatement("INSERT INTO "
                        + "KardexSuministroTB("
                        + "IdSuministro,"
                        + "Fecha,"
                        + "Hora,"
                        + "Tipo,"
                        + "Movimiento,"
                        + "Detalle,"
                        + "Cantidad,"
                        + "Costo, "
                        + "Total,"
                        + "IdAlmacen,"
                        + "IdEmpleado) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                venta.setString(1, id_venta);
                venta.setString(2, ventaTB.getIdCliente());
                venta.setString(3, ventaTB.getVendedor());
                venta.setInt(4, ventaTB.getIdComprobante());
                venta.setInt(5, ventaTB.getIdMoneda());
                venta.setString(6, id_comprabante[0]);
                venta.setString(7, id_comprabante[1]);
                venta.setString(8, ventaTB.getFechaVenta());
                venta.setString(9, ventaTB.getHoraVenta());
                venta.setString(10, ventaTB.getFechaVencimiento());
                venta.setString(11, ventaTB.getHoraVencimiento());
                venta.setInt(12, ventaTB.getTipo());
                venta.setInt(13, ventaTB.getEstado());
                venta.setString(14, ventaTB.getObservaciones());
                venta.setDouble(15, ventaTB.getEfectivo());
                venta.setDouble(16, ventaTB.getVuelto());
                venta.setDouble(17, ventaTB.getTarjeta());
                venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
                venta.setDouble(19, ventaTB.getDeposito());
                venta.setInt(20, ventaTB.getTipoCredito());
                venta.setString(21, ventaTB.getNumeroOperacion());
                venta.addBatch();

                if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                    cotizacion.setString(1, id_venta);
                    cotizacion.setString(2, ventaTB.getIdCotizacion());
                    cotizacion.addBatch();
                }

                venta_credito = dbf.getConnection().prepareStatement(
                        "INSERT INTO VentaCreditoTB(IdVenta,IdVentaCredito,Monto,FechaPago,HoraPago,Estado,IdUsuario,Observacion)VALUES(?,?,?,?,?,?,?,?)");
                ingreso = dbf.getConnection().prepareStatement(
                        "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?)");

                if (ventaTB.getVentaCreditoTBs() != null) {
                    venta_credito_codigo = dbf.getConnection()
                            .prepareStatement("SELECT IdVentaCredito FROM VentaCreditoTB ");
                    ResultSet resultSet = venta_credito_codigo.executeQuery();
                    ArrayList<Integer> listCodigos = new ArrayList<Integer>();
                    while (resultSet.next()) {
                        listCodigos.add(Integer.parseInt(resultSet.getString("IdVentaCredito").replace("VC", "")));
                    }

                    int valorActual = 0;
                    if (!listCodigos.isEmpty()) {
                        valorActual = Collections.max(listCodigos);
                        valorActual++;
                    }

                    for (VentaCreditoTB creditoTB : ventaTB.getVentaCreditoTBs()) {
                        String idCodigoCredito;
                        if (valorActual > 0) {
                            int incremental = valorActual;
                            String codigoGenerado;
                            if (incremental <= 9) {
                                codigoGenerado = "VC000" + incremental;
                            } else if (incremental >= 10 && incremental <= 99) {
                                codigoGenerado = "VC00" + incremental;
                            } else if (incremental >= 100 && incremental <= 999) {
                                codigoGenerado = "VC0" + incremental;
                            } else {
                                codigoGenerado = "VC" + incremental;
                            }
                            valorActual++;
                            idCodigoCredito = codigoGenerado;
                        } else {
                            valorActual = 2;
                            idCodigoCredito = "VC0001";
                        }

                        venta_credito.setString(1, id_venta);
                        venta_credito.setString(2, idCodigoCredito);
                        venta_credito.setDouble(3, Double.parseDouble(creditoTB.getTfMonto().getText()));
                        venta_credito.setString(4, Tools.getDatePicker(creditoTB.getDpFecha()));
                        venta_credito.setString(5, creditoTB.getHoraPago());
                        venta_credito.setShort(6, creditoTB.getCbMontoInicial().isSelected() ? (short) 1 : (short) 0);
                        venta_credito.setString(7, ventaTB.getVendedor());
                        venta_credito.setString(8,
                                creditoTB.getCbMontoInicial().isSelected() ? "PAGO ADELANTADO DE LA VENTA AL CRÉDITO"
                                        : "");
                        venta_credito.addBatch();

                        if (creditoTB.getCbMontoInicial().isSelected()) {
                            ingreso.setString(1, idCodigoCredito);
                            ingreso.setString(2, ventaTB.getVendedor());
                            ingreso.setString(3, "PAGO ADELANTADO DE LA VENTA AL CRÉDITO");
                            ingreso.setInt(4, 5);
                            ingreso.setString(5, Tools.getDate());
                            ingreso.setString(6, Tools.getTime());
                            ingreso.setInt(7,
                                    creditoTB.getCbForma().getSelectionModel().getSelectedIndex() == 0 ? 1
                                            : creditoTB.getCbForma().getSelectionModel().getSelectedIndex() == 1 ? 2
                                                    : 3);
                            ingreso.setDouble(8, Double.parseDouble(creditoTB.getTfMonto().getText()));
                            ingreso.addBatch();
                        }
                    }
                }

                for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                    // double cantidad = sm.getValorInventario() == 2 ? sm.getImporteNeto() /
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getCantidad();
                    double cantidad = sm.getCantidad();

                    // double precio = sm.getValorInventario() == 2 ?
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                    double precio = sm.getPrecioVentaGeneral();

                    detalle_venta.setString(1, id_venta);
                    detalle_venta.setString(2, sm.getIdSuministro());
                    detalle_venta.setDouble(3, cantidad);
                    detalle_venta.setDouble(4, sm.getCostoCompra());
                    detalle_venta.setDouble(5, precio);
                    detalle_venta.setDouble(6, sm.getDescuento());
                    detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                    detalle_venta.setDouble(8, sm.getIdImpuesto());
                    detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                    detalle_venta.setDouble(11, sm.getBonificacion());
                    detalle_venta.setDouble(12, cantidad);
                    detalle_venta.setString(13, "C");
                    detalle_venta.setInt(14, sm.getUnidadCompra());
                    detalle_venta.addBatch();

                    if (sm.isInventario() && sm.getValorInventario() == 1) {
                        suministro_update.setDouble(1, sm.getCantidad() + sm.getBonificacion());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    } else if (sm.isInventario() && sm.getValorInventario() == 2) {
                        // suministro_update.setDouble(1, sm.getImporteNeto() /
                        // sm.getPrecioVentaGeneralAuxiliar());
                        suministro_update.setDouble(1, sm.getCantidad());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    } else if (sm.isInventario() && sm.getValorInventario() == 3) {
                        suministro_update.setDouble(1, sm.getCantidad());
                        suministro_update.setString(2, sm.getIdSuministro());
                        suministro_update.addBatch();
                    }

                    double cantidadKardex = sm.getValorInventario() == 1
                            ? sm.getCantidad() + sm.getBonificacion()
                            : sm.getValorInventario() == 2
                                    // ? sm.getImporteNeto() / sm.getPrecioVentaGeneralAuxiliar()
                                    ? sm.getCantidad()
                                    : sm.getCantidad();

                    suministro_kardex.setString(1, sm.getIdSuministro());
                    suministro_kardex.setString(2, ventaTB.getFechaVenta());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 2);
                    suministro_kardex.setInt(5, 1);
                    suministro_kardex.setString(6, "VENTA CON SERIE Y NUMERACIÓN: " + id_comprabante[0] + "-"
                            + id_comprabante[1]
                            + (sm.getBonificacion() <= 0 ? "" : "(BONIFICACIÓN: " + sm.getBonificacion() + ")"));
                    suministro_kardex.setDouble(7, cantidadKardex);
                    suministro_kardex.setDouble(8, sm.getCostoCompra());
                    suministro_kardex.setDouble(9, cantidadKardex * sm.getCostoCompra());
                    suministro_kardex.setInt(10, 0);
                    suministro_kardex.setString(11, Session.USER_ID);
                    suministro_kardex.addBatch();
                }

                cliente.executeBatch();
                venta.executeBatch();
                detalle_venta.executeBatch();
                suministro_update.executeBatch();
                suministro_kardex.executeBatch();
                cotizacion.executeBatch();
                venta_credito.executeBatch();
                ingreso.executeBatch();

                dbf.getConnection().commit();
                resultTransaction.setCode("register");
                resultTransaction.setResult(id_venta);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (venta_credito != null) {
                    venta_credito.close();
                }
                if (venta_credito_codigo != null) {
                    venta_credito_codigo.close();
                }
                if (ingreso != null) {
                    ingreso.close();
                }
                if (ventaVerificar != null) {
                    ventaVerificar.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static ResultTransaction registrarVentaAdelantado(VentaTB ventaTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement ingreso = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            cliente = dbf.getConnection().prepareStatement(
                    "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
            clienteVerificar = dbf.getConnection()
                    .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
            clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
            if (clienteVerificar.executeQuery().next()) {
                ResultSet resultSet = clienteVerificar.executeQuery();
                resultSet.next();
                ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                cliente = dbf.getConnection().prepareStatement(
                        "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente =  ?");
                cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                cliente.setString(6, resultSet.getString("IdCliente"));
                cliente.addBatch();

            } else {
                codigoCliente = dbf.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigoCliente.execute();
                String idCliente = codigoCliente.getString(1);

                cliente.setString(1, idCliente);
                cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                cliente.setString(5, "");
                cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                cliente.setString(9, "");
                cliente.setInt(10, 1);
                cliente.setBoolean(11, false);
                cliente.setBoolean(12, false);
                cliente.addBatch();

                ventaTB.setIdCliente(idCliente);
            }

            serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
            serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
            serie_numeracion.setInt(2, ventaTB.getIdComprobante());
            serie_numeracion.execute();
            String[] id_comprabante = serie_numeracion.getString(1).split("-");

            codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
            codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_venta.execute();

            String id_venta = codigo_venta.getString(1);

            venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                    + "(IdVenta\n"
                    + ",Cliente\n"
                    + ",Vendedor\n"
                    + ",Comprobante\n"
                    + ",Moneda\n"
                    + ",Serie\n"
                    + ",Numeracion\n"
                    + ",FechaVenta\n"
                    + ",HoraVenta\n"
                    + ",FechaVencimiento\n"
                    + ",HoraVencimiento\n"
                    + ",Tipo\n"
                    + ",Estado\n"
                    + ",Observaciones\n"
                    + ",Efectivo\n"
                    + ",Vuelto\n"
                    + ",Tarjeta\n"
                    + ",Codigo\n"
                    + ",Deposito\n"
                    + ",TipoCredito\n"
                    + ",NumeroOperacion\n"
                    + ",Procedencia)\n"
                    + "VALUES\n"
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)");

            cotizacion = dbf.getConnection()
                    .prepareStatement("UPDATE CotizacionTB SET Estado = 2,IdVenta=?  WHERE IdCotizacion = ?");

            detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                    + "(IdVenta\n"
                    + ",IdArticulo\n"
                    + ",Cantidad\n"
                    + ",CostoVenta\n"
                    + ",PrecioVenta\n"
                    + ",Descuento\n"
                    + ",IdOperacion\n"
                    + ",IdImpuesto\n"
                    + ",NombreImpuesto\n"
                    + ",ValorImpuesto\n"
                    + ",Bonificacion\n"
                    + ",PorLlevar\n"
                    + ",Estado\n"
                    + ",IdMedida)\n"
                    + "VALUES\n"
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            venta.setString(1, id_venta);
            venta.setString(2, ventaTB.getIdCliente());
            venta.setString(3, ventaTB.getVendedor());
            venta.setInt(4, ventaTB.getIdComprobante());
            venta.setInt(5, ventaTB.getIdMoneda());
            venta.setString(6, id_comprabante[0]);
            venta.setString(7, id_comprabante[1]);
            venta.setString(8, ventaTB.getFechaVenta());
            venta.setString(9, ventaTB.getHoraVenta());
            venta.setString(10, ventaTB.getFechaVenta());
            venta.setString(11, ventaTB.getHoraVenta());
            venta.setInt(12, ventaTB.getTipo());
            venta.setInt(13, ventaTB.getEstado());
            venta.setString(14, ventaTB.getObservaciones());
            venta.setDouble(15, ventaTB.getEfectivo());
            venta.setDouble(16, ventaTB.getVuelto());
            venta.setDouble(17, ventaTB.getTarjeta());
            venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
            venta.setDouble(19, ventaTB.getDeposito());
            venta.setInt(20, ventaTB.getTipoCredito());
            venta.setString(21, ventaTB.getNumeroOperacion());
            venta.addBatch();

            if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                cotizacion.setString(1, id_venta);
                cotizacion.setString(2, ventaTB.getIdCotizacion());
                cotizacion.addBatch();
            }

            for (SuministroTB sm : ventaTB.getSuministroTBs()) {

                double cantidad = sm.getCantidad();
                // double precio = sm.getValorInventario() == 2 ?
                // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                double precio = sm.getPrecioVentaGeneral();

                detalle_venta.setString(1, id_venta);
                detalle_venta.setString(2, sm.getIdSuministro());
                detalle_venta.setDouble(3, cantidad);
                detalle_venta.setDouble(4, sm.getCostoCompra());
                detalle_venta.setDouble(5, precio);
                detalle_venta.setDouble(6, sm.getDescuento());
                detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                detalle_venta.setDouble(8, sm.getIdImpuesto());
                detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                detalle_venta.setDouble(11, sm.getBonificacion());
                detalle_venta.setDouble(12, 0);
                detalle_venta.setString(13, "L");
                detalle_venta.setInt(14, sm.getUnidadCompra());
                detalle_venta.addBatch();
            }

            ingreso = dbf.getConnection().prepareStatement(
                    "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?)");

            if (ventaTB.getDeposito() > 0) {
                ingreso.setString(1, id_venta);
                ingreso.setString(2, ventaTB.getVendedor());
                ingreso.setString(3, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                        + "-" + id_comprabante[1]);
                ingreso.setInt(4, 1);
                ingreso.setString(5, ventaTB.getFechaVenta());
                ingreso.setString(6, ventaTB.getHoraVenta());
                ingreso.setInt(7, 3);
                ingreso.setDouble(8, ventaTB.getDeposito());
                ingreso.addBatch();
            } else {

                if (ventaTB.getEfectivo() > 0) {
                    ingreso.setString(1, id_venta);
                    ingreso.setString(2, ventaTB.getVendedor());
                    ingreso.setString(3, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(4, 1);
                    ingreso.setString(5, ventaTB.getFechaVenta());
                    ingreso.setString(6, ventaTB.getHoraVenta());
                    ingreso.setInt(7, 1);
                    ingreso.setDouble(8, ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                    ingreso.addBatch();
                }

                if (ventaTB.getTarjeta() > 0) {
                    ingreso.setString(1, id_venta);
                    ingreso.setString(2, ventaTB.getVendedor());
                    ingreso.setString(3, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(4, 1);
                    ingreso.setString(5, ventaTB.getFechaVenta());
                    ingreso.setString(6, ventaTB.getHoraVenta());
                    ingreso.setInt(7, 2);
                    ingreso.setDouble(8, ventaTB.getTarjeta());
                    ingreso.addBatch();
                }
            }

            cliente.executeBatch();
            venta.executeBatch();
            ingreso.executeBatch();
            detalle_venta.executeBatch();
            cotizacion.executeBatch();

            dbf.getConnection().commit();
            resultTransaction.setCode("register");
            resultTransaction.setResult(id_venta);
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                if (ingreso != null) {
                    ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static ResultTransaction registrarVentaContadoPosVenta(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<String>();
            ventaVerificar = dbf.getConnection()
                    .prepareStatement("SELECT Cantidad FROM SuministroTB WHERE IdSuministro = ?");
            for (int i = 0; i < ventaTB.getSuministroTBs().size(); i++) {
                ventaVerificar.setString(1, ventaTB.getSuministroTBs().get(i).getIdSuministro());
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    double ca = ventaTB.getSuministroTBs().get(i).getValorInventario() == 1
                            ? ventaTB.getSuministroTBs().get(i).getCantidad()
                                    + ventaTB.getSuministroTBs().get(i).getBonificacion()
                            : ventaTB.getSuministroTBs().get(i).getCantidad();
                    double cb = resultValidate.getDouble("Cantidad");
                    if (ca > cb) {
                        countValidate++;
                        arrayResult.add(ventaTB.getSuministroTBs().get(i).getClave() + " - "
                                + ventaTB.getSuministroTBs().get(i).getNombreMarca() + " - Cantidad actual("
                                + Tools.roundingValue(cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {

                ventaVerificar = dbf.getConnection()
                        .prepareStatement("SELECT * FROM CajaTB WHERE IdCaja = ? AND Estado = 0");
                ventaVerificar.setString(1, Session.CAJA_ID);
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    dbf.getConnection().rollback();
                    resultTransaction.setCode("caja");
                    resultTransaction.setResult("La caja actual se encuentra cerrado.");
                } else {

                    cliente = dbf.getConnection().prepareStatement(
                            "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                    ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                    clienteVerificar = dbf.getConnection()
                            .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                    clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    if (clienteVerificar.executeQuery().next()) {
                        ResultSet resultSet = clienteVerificar.executeQuery();
                        resultSet.next();
                        ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                        cliente = dbf.getConnection().prepareStatement(
                                "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente =  ?");
                        cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                        cliente.setString(6, resultSet.getString("IdCliente"));
                        cliente.addBatch();

                    } else {
                        codigoCliente = dbf.getConnection()
                                .prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                        codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                        codigoCliente.execute();
                        String idCliente = codigoCliente.getString(1);

                        cliente.setString(1, idCliente);
                        cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(5, "");
                        cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                        cliente.setString(9, "");
                        cliente.setInt(10, 1);
                        cliente.setBoolean(11, false);
                        cliente.setBoolean(12, false);
                        cliente.addBatch();

                        ventaTB.setIdCliente(idCliente);
                    }

                    serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                    serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                    serie_numeracion.setInt(2, ventaTB.getIdComprobante());
                    serie_numeracion.execute();
                    String[] id_comprabante = serie_numeracion.getString(1).split("-");

                    codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                    codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigo_venta.execute();

                    String id_venta = codigo_venta.getString(1);

                    venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                            + "(IdVenta\n"
                            + ",Cliente\n"
                            + ",Vendedor\n"
                            + ",Comprobante\n"
                            + ",Moneda\n"
                            + ",Serie\n"
                            + ",Numeracion\n"
                            + ",FechaVenta\n"
                            + ",HoraVenta\n"
                            + ",FechaVencimiento\n"
                            + ",HoraVencimiento\n"
                            + ",Tipo\n"
                            + ",Estado\n"
                            + ",Observaciones\n"
                            + ",Efectivo\n"
                            + ",Vuelto\n"
                            + ",Tarjeta\n"
                            + ",Codigo\n"
                            + ",Deposito\n"
                            + ",TipoCredito\n"
                            + ",NumeroOperacion\n"
                            + ",Procedencia)\n"
                            + "VALUES\n"
                            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,2)");

                    cotizacion = dbf.getConnection()
                            .prepareStatement("UPDATE CotizacionTB SET Estado = 2,IdVenta=?  WHERE IdCotizacion = ?");

                    detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                            + "(IdVenta\n"
                            + ",IdArticulo\n"
                            + ",Cantidad\n"
                            + ",CostoVenta\n"
                            + ",PrecioVenta\n"
                            + ",Descuento\n"
                            + ",IdOperacion\n"
                            + ",IdImpuesto\n"
                            + ",NombreImpuesto\n"
                            + ",ValorImpuesto\n"
                            + ",Bonificacion\n"
                            + ",PorLlevar\n"
                            + ",Estado\n"
                            + ",IdMedida)\n"
                            + "VALUES\n"
                            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                    suministro_kardex = dbf.getConnection().prepareStatement("INSERT INTO "
                            + "KardexSuministroTB("
                            + "IdSuministro,"
                            + "Fecha,"
                            + "Hora,"
                            + "Tipo,"
                            + "Movimiento,"
                            + "Detalle,"
                            + "Cantidad,"
                            + "Costo, "
                            + "Total,"
                            + "IdAlmacen,"
                            + "IdEmpleado) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                    venta.setString(1, id_venta);
                    venta.setString(2, ventaTB.getIdCliente());
                    venta.setString(3, ventaTB.getVendedor());
                    venta.setInt(4, ventaTB.getIdComprobante());
                    venta.setInt(5, ventaTB.getIdMoneda());
                    venta.setString(6, id_comprabante[0]);
                    venta.setString(7, id_comprabante[1]);
                    venta.setString(8, ventaTB.getFechaVenta());
                    venta.setString(9, ventaTB.getHoraVenta());
                    venta.setString(10, ventaTB.getFechaVenta());
                    venta.setString(11, ventaTB.getHoraVenta());
                    venta.setInt(12, ventaTB.getTipo());
                    venta.setInt(13, ventaTB.getEstado());
                    venta.setString(14, ventaTB.getObservaciones());
                    venta.setDouble(15, ventaTB.getEfectivo());
                    venta.setDouble(16, ventaTB.getVuelto());
                    venta.setDouble(17, ventaTB.getTarjeta());
                    venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
                    venta.setDouble(19, ventaTB.getDeposito());
                    venta.setInt(20, ventaTB.getTipoCredito());
                    venta.setString(21, ventaTB.getNumeroOperacion());
                    venta.addBatch();

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        cotizacion.setString(1, id_venta);
                        cotizacion.setString(2, ventaTB.getIdCotizacion());
                        cotizacion.addBatch();
                    }

                    suministro_update = dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                    for (SuministroTB sm : ventaTB.getSuministroTBs()) {

                        // double cantidad = sm.getValorInventario() == 2 ? sm.getImporteNeto() /
                        // sm.getPrecioVentaGeneralAuxiliar() : sm.getCantidad();
                        double cantidad = sm.getCantidad();
                        // double precio = sm.getValorInventario() == 2 ?
                        // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                        double precio = sm.getPrecioVentaGeneral();

                        detalle_venta.setString(1, id_venta);
                        detalle_venta.setString(2, sm.getIdSuministro());
                        detalle_venta.setDouble(3, cantidad);
                        detalle_venta.setDouble(4, sm.getCostoCompra());
                        detalle_venta.setDouble(5, precio);
                        detalle_venta.setDouble(6, sm.getDescuento());
                        detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                        detalle_venta.setDouble(8, sm.getIdImpuesto());
                        detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                        detalle_venta.setDouble(11, sm.getBonificacion());
                        detalle_venta.setDouble(12, cantidad);
                        detalle_venta.setString(13, "C");
                        detalle_venta.setInt(14, sm.getUnidadCompra());
                        detalle_venta.addBatch();

                        if (sm.isInventario() && sm.getValorInventario() == 1) {
                            suministro_update.setDouble(1, sm.getCantidad() + sm.getBonificacion());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        } else if (sm.isInventario() && sm.getValorInventario() == 2) {
                            // suministro_update.setDouble(1, sm.getImporteNeto() /
                            // sm.getPrecioVentaGeneralAuxiliar());
                            suministro_update.setDouble(1, sm.getCantidad());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        } else if (sm.isInventario() && sm.getValorInventario() == 3) {
                            suministro_update.setDouble(1, sm.getCantidad());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        }

                        double cantidadKardex = sm.getValorInventario() == 1
                                ? sm.getCantidad() + sm.getBonificacion()
                                : sm.getValorInventario() == 2
                                        // ? sm.getImporteNeto() / sm.getPrecioVentaGeneralAuxiliar()
                                        ? sm.getCantidad()
                                        : sm.getCantidad();

                        suministro_kardex.setString(1, sm.getIdSuministro());
                        suministro_kardex.setString(2, ventaTB.getFechaVenta());
                        suministro_kardex.setString(3, Tools.getTime());
                        suministro_kardex.setShort(4, (short) 2);
                        suministro_kardex.setInt(5, 1);
                        suministro_kardex.setString(6, "VENTA CON SERIE Y NUMERACIÓN: " + id_comprabante[0] + "-"
                                + id_comprabante[1]
                                + (sm.getBonificacion() <= 0 ? "" : "(BONIFICACIÓN: " + sm.getBonificacion() + ")"));
                        suministro_kardex.setDouble(7, cantidadKardex);
                        suministro_kardex.setDouble(8, sm.getCostoCompra());
                        suministro_kardex.setDouble(9, cantidadKardex * sm.getCostoCompra());
                        suministro_kardex.setInt(10, 0);
                        suministro_kardex.setString(11, Session.USER_ID);
                        suministro_kardex.addBatch();
                    }

                    movimiento_caja = dbf.getConnection().prepareStatement(
                            "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");

                    if (ventaTB.getDeposito() > 0) {

                        movimiento_caja.setString(1, Session.CAJA_ID);
                        movimiento_caja.setString(2, ventaTB.getFechaVenta());
                        movimiento_caja.setString(3, ventaTB.getHoraVenta());
                        movimiento_caja.setString(4, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE  "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        movimiento_caja.setShort(5, (short) 6);
                        movimiento_caja.setDouble(6, ventaTB.getDeposito());
                        movimiento_caja.setString(7, id_venta);
                        movimiento_caja.addBatch();

                    } else {

                        if (ventaTB.getEfectivo() > 0) {
                            movimiento_caja.setString(1, Session.CAJA_ID);
                            movimiento_caja.setString(2, ventaTB.getFechaVenta());
                            movimiento_caja.setString(3, ventaTB.getHoraVenta());
                            movimiento_caja.setString(4, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE "
                                    + id_comprabante[0] + "-" + id_comprabante[1]);
                            movimiento_caja.setShort(5, (short) 2);
                            movimiento_caja.setDouble(6,
                                    ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                            movimiento_caja.setString(7, id_venta);
                            movimiento_caja.addBatch();
                        }

                        if (ventaTB.getTarjeta() > 0) {
                            movimiento_caja.setString(1, Session.CAJA_ID);
                            movimiento_caja.setString(2, ventaTB.getFechaVenta());
                            movimiento_caja.setString(3, ventaTB.getHoraVenta());
                            movimiento_caja.setString(4, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  "
                                    + id_comprabante[0] + "-" + id_comprabante[1]);
                            movimiento_caja.setShort(5, (short) 3);
                            movimiento_caja.setDouble(6, ventaTB.getTarjeta());
                            movimiento_caja.setString(7, id_venta);
                            movimiento_caja.addBatch();
                        }
                    }

                    cliente.executeBatch();
                    venta.executeBatch();
                    movimiento_caja.executeBatch();
                    suministro_update.executeBatch();
                    detalle_venta.executeBatch();
                    suministro_kardex.executeBatch();
                    cotizacion.executeBatch();

                    dbf.getConnection().commit();
                    resultTransaction.setCode("register");
                    resultTransaction.setResult(id_venta);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (ventaVerificar != null) {
                    ventaVerificar.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }

                if (cliente != null) {
                    cliente.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                if (movimiento_caja != null) {
                    movimiento_caja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static ResultTransaction registrarVentaCreditoPosVenta(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement venta_credito_codigo = null;
        PreparedStatement venta_credito = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<String>();
            ventaVerificar = dbf.getConnection()
                    .prepareStatement("SELECT Cantidad FROM SuministroTB WHERE IdSuministro = ?");
            for (int i = 0; i < ventaTB.getSuministroTBs().size(); i++) {
                ventaVerificar.setString(1, ventaTB.getSuministroTBs().get(i).getIdSuministro());
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    double ca = ventaTB.getSuministroTBs().get(i).getValorInventario() == 1
                            ? ventaTB.getSuministroTBs().get(i).getCantidad()
                                    + ventaTB.getSuministroTBs().get(i).getBonificacion()
                            : ventaTB.getSuministroTBs().get(i).getCantidad();
                    double cb = resultValidate.getDouble("Cantidad");
                    if (ca > cb) {
                        countValidate++;
                        arrayResult.add(ventaTB.getSuministroTBs().get(i).getClave() + " - "
                                + ventaTB.getSuministroTBs().get(i).getNombreMarca() + " - Cantidad actual("
                                + Tools.roundingValue(cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {
                ventaVerificar = dbf.getConnection()
                        .prepareStatement("SELECT * FROM CajaTB WHERE IdCaja = ? AND Estado = 0");
                ventaVerificar.setString(1, Session.CAJA_ID);
                ResultSet resultValidate = ventaVerificar.executeQuery();
                if (resultValidate.next()) {
                    dbf.getConnection().rollback();
                    resultTransaction.setCode("caja");
                    resultTransaction.setResult("La caja actual se encuentra cerrado.");
                } else {

                    cliente = dbf.getConnection().prepareStatement(
                            "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                    ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                    clienteVerificar = dbf.getConnection()
                            .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                    clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    if (clienteVerificar.executeQuery().next()) {
                        ResultSet resultSet = clienteVerificar.executeQuery();
                        resultSet.next();
                        ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                        cliente = dbf.getConnection().prepareStatement(
                                "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente =  ?");
                        cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                        cliente.setString(6, resultSet.getString("IdCliente"));
                        cliente.addBatch();

                    } else {
                        codigoCliente = dbf.getConnection()
                                .prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                        codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                        codigoCliente.execute();
                        String idCliente = codigoCliente.getString(1);

                        cliente.setString(1, idCliente);
                        cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(5, "");
                        cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                        cliente.setString(9, "");
                        cliente.setInt(10, 1);
                        cliente.setBoolean(11, false);
                        cliente.setBoolean(12, false);
                        cliente.addBatch();

                        ventaTB.setIdCliente(idCliente);
                    }

                    serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                    serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                    serie_numeracion.setInt(2, ventaTB.getIdComprobante());
                    serie_numeracion.execute();
                    String[] id_comprabante = serie_numeracion.getString(1).split("-");

                    codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                    codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigo_venta.execute();

                    String id_venta = codigo_venta.getString(1);

                    venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                            + "(IdVenta\n"
                            + ",Cliente\n"
                            + ",Vendedor\n"
                            + ",Comprobante\n"
                            + ",Moneda\n"
                            + ",Serie\n"
                            + ",Numeracion\n"
                            + ",FechaVenta\n"
                            + ",HoraVenta\n"
                            + ",FechaVencimiento\n"
                            + ",HoraVencimiento\n"
                            + ",Tipo\n"
                            + ",Estado\n"
                            + ",Observaciones\n"
                            + ",Efectivo\n"
                            + ",Vuelto\n"
                            + ",Tarjeta\n"
                            + ",Codigo\n"
                            + ",Deposito\n"
                            + ",TipoCredito\n"
                            + ",NumeroOperacion\n"
                            + ",Procedencia)\n"
                            + "VALUES\n"
                            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,2)");

                    cotizacion = dbf.getConnection()
                            .prepareStatement("UPDATE CotizacionTB SET Estado = 2,IdVenta=?  WHERE IdCotizacion = ?");

                    detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                            + "(IdVenta\n"
                            + ",IdArticulo\n"
                            + ",Cantidad\n"
                            + ",CostoVenta\n"
                            + ",PrecioVenta\n"
                            + ",Descuento\n"
                            + ",IdOperacion\n"
                            + ",IdImpuesto\n"
                            + ",NombreImpuesto\n"
                            + ",ValorImpuesto\n"
                            + ",Bonificacion\n"
                            + ",PorLlevar\n"
                            + ",Estado\n"
                            + ",IdMedida)\n"
                            + "VALUES\n"
                            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                    suministro_update = dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

                    suministro_kardex = dbf.getConnection().prepareStatement("INSERT INTO "
                            + "KardexSuministroTB("
                            + "IdSuministro,"
                            + "Fecha,"
                            + "Hora,"
                            + "Tipo,"
                            + "Movimiento,"
                            + "Detalle,"
                            + "Cantidad,"
                            + "Costo, "
                            + "Total,"
                            + "IdAlmacen,"
                            + "IdEmpleado)"
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                    venta.setString(1, id_venta);
                    venta.setString(2, ventaTB.getIdCliente());
                    venta.setString(3, ventaTB.getVendedor());
                    venta.setInt(4, ventaTB.getIdComprobante());
                    venta.setInt(5, ventaTB.getIdMoneda());
                    venta.setString(6, id_comprabante[0]);
                    venta.setString(7, id_comprabante[1]);
                    venta.setString(8, ventaTB.getFechaVenta());
                    venta.setString(9, ventaTB.getHoraVenta());
                    venta.setString(10, ventaTB.getFechaVencimiento());
                    venta.setString(11, ventaTB.getHoraVencimiento());
                    venta.setInt(12, ventaTB.getTipo());
                    venta.setInt(13, ventaTB.getEstado());
                    venta.setString(14, ventaTB.getObservaciones());
                    venta.setDouble(15, ventaTB.getEfectivo());
                    venta.setDouble(16, ventaTB.getVuelto());
                    venta.setDouble(17, ventaTB.getTarjeta());
                    venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
                    venta.setDouble(19, ventaTB.getDeposito());
                    venta.setInt(20, ventaTB.getTipoCredito());
                    venta.setString(21, ventaTB.getNumeroOperacion());
                    venta.addBatch();

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        cotizacion.setString(1, id_venta);
                        cotizacion.setString(2, ventaTB.getIdCotizacion());
                        cotizacion.addBatch();
                    }

                    venta_credito = dbf.getConnection().prepareStatement(
                            "INSERT INTO VentaCreditoTB(IdVenta,IdVentaCredito,Monto,FechaPago,HoraPago,Estado,IdUsuario,Observacion)VALUES(?,?,?,?,?,?,?,?)");
                    movimiento_caja = dbf.getConnection().prepareStatement(
                            "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");

                    if (ventaTB.getVentaCreditoTBs() != null) {
                        venta_credito_codigo = dbf.getConnection()
                                .prepareStatement("SELECT IdVentaCredito FROM VentaCreditoTB ");
                        ResultSet resultSet = venta_credito_codigo.executeQuery();
                        ArrayList<Integer> listCodigos = new ArrayList<Integer>();
                        while (resultSet.next()) {
                            listCodigos.add(Integer.parseInt(resultSet.getString("IdVentaCredito").replace("VC", "")));
                        }

                        int valorActual = 0;
                        if (!listCodigos.isEmpty()) {
                            valorActual = Collections.max(listCodigos);
                            valorActual++;
                        }

                        for (VentaCreditoTB creditoTB : ventaTB.getVentaCreditoTBs()) {
                            String idCodigoCredito;
                            if (valorActual > 0) {
                                int incremental = valorActual;
                                String codigoGenerado;
                                if (incremental <= 9) {
                                    codigoGenerado = "VC000" + incremental;
                                } else if (incremental >= 10 && incremental <= 99) {
                                    codigoGenerado = "VC00" + incremental;
                                } else if (incremental >= 100 && incremental <= 999) {
                                    codigoGenerado = "VC0" + incremental;
                                } else {
                                    codigoGenerado = "VC" + incremental;
                                }
                                valorActual++;
                                idCodigoCredito = codigoGenerado;
                            } else {
                                valorActual = 2;
                                idCodigoCredito = "VC0001";
                            }

                            venta_credito.setString(1, id_venta);
                            venta_credito.setString(2, idCodigoCredito);
                            venta_credito.setDouble(3, Double.parseDouble(creditoTB.getTfMonto().getText()));
                            venta_credito.setString(4, Tools.getDatePicker(creditoTB.getDpFecha()));
                            venta_credito.setString(5, creditoTB.getHoraPago());
                            venta_credito.setShort(6,
                                    creditoTB.getCbMontoInicial().isSelected() ? (short) 1 : (short) 0);
                            venta_credito.setString(7, ventaTB.getVendedor());
                            venta_credito.setString(8,
                                    creditoTB.getCbMontoInicial().isSelected()
                                            ? "PAGO ADELANTADO DE LA VENTA AL CRÉDITO"
                                            : "");
                            venta_credito.addBatch();

                            if (creditoTB.getCbMontoInicial().isSelected()) {
                                movimiento_caja.setString(1, Session.CAJA_ID);
                                movimiento_caja.setString(2, ventaTB.getFechaVenta());
                                movimiento_caja.setString(3, ventaTB.getHoraVenta());
                                movimiento_caja.setString(4, "PAGO ADELANTADO DE LA VENTA AL CRÉDITO DEL COMPROBANTE "
                                        + id_comprabante[0] + "-" + id_comprabante[1]);
                                movimiento_caja.setInt(5,
                                        creditoTB.getCbForma().getSelectionModel().getSelectedIndex() == 0 ? 2
                                                : creditoTB.getCbForma().getSelectionModel().getSelectedIndex() == 1 ? 3
                                                        : 6);
                                movimiento_caja.setDouble(6, Double.parseDouble(creditoTB.getTfMonto().getText()));
                                movimiento_caja.setString(7, idCodigoCredito);
                                movimiento_caja.addBatch();
                            }
                        }
                    }

                    for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                        double cantidad = sm.getCantidad();

                        // double precio = sm.getValorInventario() == 2 ?
                        // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                        double precio = sm.getPrecioVentaGeneral();

                        detalle_venta.setString(1, id_venta);
                        detalle_venta.setString(2, sm.getIdSuministro());
                        detalle_venta.setDouble(3, cantidad);
                        detalle_venta.setDouble(4, sm.getCostoCompra());
                        detalle_venta.setDouble(5, precio);
                        detalle_venta.setDouble(6, sm.getDescuento());
                        detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                        detalle_venta.setDouble(8, sm.getIdImpuesto());
                        detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                        detalle_venta.setDouble(11, sm.getBonificacion());
                        detalle_venta.setDouble(12, cantidad);
                        detalle_venta.setString(13, "C");
                        detalle_venta.setInt(14, sm.getUnidadCompra());
                        detalle_venta.addBatch();

                        if (sm.isInventario() && sm.getValorInventario() == 1) {
                            suministro_update.setDouble(1, sm.getCantidad() + sm.getBonificacion());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        } else if (sm.isInventario() && sm.getValorInventario() == 2) {
                            // suministro_update.setDouble(1, sm.getImporteNeto() /
                            // sm.getPrecioVentaGeneralAuxiliar());
                            suministro_update.setDouble(1, sm.getCantidad());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        } else if (sm.isInventario() && sm.getValorInventario() == 3) {
                            suministro_update.setDouble(1, sm.getCantidad());
                            suministro_update.setString(2, sm.getIdSuministro());
                            suministro_update.addBatch();
                        }

                        double cantidadKardex = sm.getValorInventario() == 1
                                ? sm.getCantidad() + sm.getBonificacion()
                                : sm.getValorInventario() == 2
                                        // ? sm.getImporteNeto() / sm.getPrecioVentaGeneralAuxiliar()
                                        ? sm.getCantidad()
                                        : sm.getCantidad();

                        suministro_kardex.setString(1, sm.getIdSuministro());
                        suministro_kardex.setString(2, ventaTB.getFechaVenta());
                        suministro_kardex.setString(3, Tools.getTime());
                        suministro_kardex.setShort(4, (short) 2);
                        suministro_kardex.setInt(5, 1);
                        suministro_kardex.setString(6, "VENTA CON SERIE Y NUMERACIÓN: " + id_comprabante[0] + "-"
                                + id_comprabante[1]
                                + (sm.getBonificacion() <= 0 ? "" : "(BONIFICACIÓN: " + sm.getBonificacion() + ")"));
                        suministro_kardex.setDouble(7, cantidadKardex);
                        suministro_kardex.setDouble(8, sm.getCostoCompra());
                        suministro_kardex.setDouble(9, cantidadKardex * sm.getCostoCompra());
                        suministro_kardex.setInt(10, 0);
                        suministro_kardex.setString(11, Session.USER_ID);
                        suministro_kardex.addBatch();
                    }

                    cliente.executeBatch();
                    venta.executeBatch();
                    detalle_venta.executeBatch();
                    suministro_update.executeBatch();
                    suministro_kardex.executeBatch();
                    cotizacion.executeBatch();
                    venta_credito.executeBatch();
                    movimiento_caja.executeBatch();

                    dbf.getConnection().commit();
                    resultTransaction.setCode("register");
                    resultTransaction.setResult(id_venta);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (venta_credito_codigo != null) {
                    venta_credito_codigo.close();
                }
                if (venta_credito != null) {
                    venta_credito.close();
                }
                if (movimiento_caja != null) {
                    movimiento_caja.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (ventaVerificar != null) {
                    ventaVerificar.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }

                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static ResultTransaction registrarVentaAdelantadoPosVenta(VentaTB ventaTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement detalle_venta = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement cotizacion = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            ventaVerificar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CajaTB WHERE IdCaja = ? AND Estado = 0");
            ventaVerificar.setString(1, Session.CAJA_ID);
            ResultSet resultValidate = ventaVerificar.executeQuery();
            if (resultValidate.next()) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("caja");
                resultTransaction.setResult("La caja actual se encuentra cerrado.");
            } else {

                cliente = dbf.getConnection().prepareStatement(
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=? WHERE IdCliente =  ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, resultSet.getString("IdCliente"));
                    cliente.addBatch();

                } else {
                    codigoCliente = dbf.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                    codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigoCliente.execute();
                    String idCliente = codigoCliente.getString(1);

                    cliente.setString(1, idCliente);
                    cliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(5, "");
                    cliente.setString(6, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(7, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(8, ventaTB.getClienteTB().getDireccion().trim().toUpperCase());
                    cliente.setString(9, "");
                    cliente.setInt(10, 1);
                    cliente.setBoolean(11, false);
                    cliente.setBoolean(12, false);
                    cliente.addBatch();

                    ventaTB.setIdCliente(idCliente);
                }

                serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                serie_numeracion.setInt(2, ventaTB.getIdComprobante());
                serie_numeracion.execute();
                String[] id_comprabante = serie_numeracion.getString(1).split("-");

                codigo_venta = dbf.getConnection().prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                codigo_venta.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_venta.execute();

                String id_venta = codigo_venta.getString(1);

                venta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB\n"
                        + "(IdVenta\n"
                        + ",Cliente\n"
                        + ",Vendedor\n"
                        + ",Comprobante\n"
                        + ",Moneda\n"
                        + ",Serie\n"
                        + ",Numeracion\n"
                        + ",FechaVenta\n"
                        + ",HoraVenta\n"
                        + ",FechaVencimiento\n"
                        + ",HoraVencimiento\n"
                        + ",Tipo\n"
                        + ",Estado\n"
                        + ",Observaciones\n"
                        + ",Efectivo\n"
                        + ",Vuelto\n"
                        + ",Tarjeta\n"
                        + ",Codigo\n"
                        + ",Deposito\n"
                        + ",TipoCredito\n"
                        + ",NumeroOperacion\n"
                        + ",Procedencia)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,2)");

                cotizacion = dbf.getConnection()
                        .prepareStatement("UPDATE CotizacionTB SET Estado = 2,IdVenta=?  WHERE IdCotizacion = ?");

                detalle_venta = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
                        + "(IdVenta\n"
                        + ",IdArticulo\n"
                        + ",Cantidad\n"
                        + ",CostoVenta\n"
                        + ",PrecioVenta\n"
                        + ",Descuento\n"
                        + ",IdOperacion\n"
                        + ",IdImpuesto\n"
                        + ",NombreImpuesto\n"
                        + ",ValorImpuesto\n"
                        + ",Bonificacion\n"
                        + ",PorLlevar\n"
                        + ",Estado\n"
                        + ",IdMedida)\n"
                        + "VALUES\n"
                        + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                venta.setString(1, id_venta);
                venta.setString(2, ventaTB.getIdCliente());
                venta.setString(3, ventaTB.getVendedor());
                venta.setInt(4, ventaTB.getIdComprobante());
                venta.setInt(5, ventaTB.getIdMoneda());
                venta.setString(6, id_comprabante[0]);
                venta.setString(7, id_comprabante[1]);
                venta.setString(8, ventaTB.getFechaVenta());
                venta.setString(9, ventaTB.getHoraVenta());
                venta.setString(10, ventaTB.getFechaVenta());
                venta.setString(11, ventaTB.getHoraVenta());
                venta.setInt(12, ventaTB.getTipo());
                venta.setInt(13, ventaTB.getEstado());
                venta.setString(14, ventaTB.getObservaciones());
                venta.setDouble(15, ventaTB.getEfectivo());
                venta.setDouble(16, ventaTB.getVuelto());
                venta.setDouble(17, ventaTB.getTarjeta());
                venta.setString(18, Integer.toString(dig5) + id_comprabante[1]);
                venta.setDouble(19, ventaTB.getDeposito());
                venta.setInt(20, ventaTB.getTipoCredito());
                venta.setString(21, ventaTB.getNumeroOperacion());
                venta.addBatch();

                if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                    cotizacion.setString(1, id_venta);
                    cotizacion.setString(2, ventaTB.getIdCotizacion());
                    cotizacion.addBatch();
                }

                for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                    double cantidad = sm.getCantidad();
                    // double precio = sm.getValorInventario() == 2 ?
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                    double precio = sm.getPrecioVentaGeneral();

                    detalle_venta.setString(1, id_venta);
                    detalle_venta.setString(2, sm.getIdSuministro());
                    detalle_venta.setDouble(3, cantidad);
                    detalle_venta.setDouble(4, sm.getCostoCompra());
                    detalle_venta.setDouble(5, precio);
                    detalle_venta.setDouble(6, sm.getDescuento());
                    detalle_venta.setDouble(7, sm.getImpuestoTB().getOperacion());
                    detalle_venta.setDouble(8, sm.getIdImpuesto());
                    detalle_venta.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    detalle_venta.setDouble(10, sm.getImpuestoTB().getValor());
                    detalle_venta.setDouble(11, sm.getBonificacion());
                    detalle_venta.setDouble(12, 0);
                    detalle_venta.setString(13, "L");
                    detalle_venta.setInt(14, sm.getUnidadCompra());
                    detalle_venta.addBatch();
                }

                movimiento_caja = dbf.getConnection().prepareStatement(
                        "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");
                if (ventaTB.getDeposito() > 0) {
                    movimiento_caja.setString(1, Session.CAJA_ID);
                    movimiento_caja.setString(2, ventaTB.getFechaVenta());
                    movimiento_caja.setString(3, ventaTB.getHoraVenta());
                    movimiento_caja.setString(4, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE "
                            + id_comprabante[0] + "-" + id_comprabante[1]);
                    movimiento_caja.setShort(5, (short) 6);
                    movimiento_caja.setDouble(6, ventaTB.getDeposito());
                    movimiento_caja.setString(7, id_venta);
                    movimiento_caja.addBatch();
                } else {
                    if (ventaTB.getEfectivo() > 0) {
                        movimiento_caja.setString(1, Session.CAJA_ID);
                        movimiento_caja.setString(2, ventaTB.getFechaVenta());
                        movimiento_caja.setString(3, ventaTB.getHoraVenta());
                        movimiento_caja.setString(4, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        movimiento_caja.setShort(5, (short) 2);
                        movimiento_caja.setDouble(6,
                                ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                        movimiento_caja.setString(7, id_venta);
                        movimiento_caja.addBatch();
                    }

                    if (ventaTB.getTarjeta() > 0) {
                        movimiento_caja.setString(1, Session.CAJA_ID);
                        movimiento_caja.setString(2, ventaTB.getFechaVenta());
                        movimiento_caja.setString(3, ventaTB.getHoraVenta());
                        movimiento_caja.setString(4, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        movimiento_caja.setShort(5, (short) 3);
                        movimiento_caja.setDouble(6, ventaTB.getTarjeta());
                        movimiento_caja.setString(7, id_venta);
                        movimiento_caja.addBatch();
                    }
                }

                cliente.executeBatch();
                venta.executeBatch();
                movimiento_caja.executeBatch();
                detalle_venta.executeBatch();
                cotizacion.executeBatch();

                dbf.getConnection().commit();
                resultTransaction.setCode("register");
                resultTransaction.setResult(id_venta);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            }
        } finally {
            try {
                if (ventaVerificar != null) {
                    ventaVerificar.close();
                }
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (venta != null) {
                    venta.close();
                }
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (detalle_venta != null) {
                    detalle_venta.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                if (movimiento_caja != null) {
                    movimiento_caja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    public static Object Listar_Ventas_All(int opcion, String value, String fechaInicial, String fechaFinal, int estado,
            int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<VentaTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Ventas_All(?,?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, 0);
            preparedStatement.setInt(6, estado);
            preparedStatement.setInt(7, posicionPagina);
            preparedStatement.setInt(8, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(rsEmps.getRow() + posicionPagina);
                ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                ventaTB.setFechaVenta(
                        rsEmps.getDate("FechaVenta").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        rsEmps.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ventaTB.setClienteTB(new ClienteTB(rsEmps.getString("DocumentoCliente"), rsEmps.getString("Cliente")));
                ventaTB.setComprobanteName(rsEmps.getString("Comprobante"));
                ventaTB.setSerie(rsEmps.getString("Serie"));
                ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                ventaTB.setTipo(rsEmps.getInt("Tipo"));
                ventaTB.setEstado(rsEmps.getInt("Estado"));
                ventaTB.setTotal(rsEmps.getDouble("Total"));
                ventaTB.setObservaciones(rsEmps.getString("Observaciones"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(rsEmps.getInt("Moneda"));
                monedaTB.setNombre(rsEmps.getString("NombreMoneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                monedaTB.setAbreviado(rsEmps.getString("TipoMoneda"));
                ventaTB.setMonedaTB(monedaTB);

                if (rsEmps.getInt("IdNotaCredito") == 1) {
                    NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
                    notaCreditoTB.setSerie(rsEmps.getString("SerieNotaCredito"));
                    notaCreditoTB.setNumeracion(rsEmps.getString("NumeracionNotaCredito"));
                    ventaTB.setNotaCreditoTB(notaCreditoTB);
                }

                Label label = new Label();
                if (ventaTB.getNotaCreditoTB() != null) {
                    label.setText("MODIFICADO");
                    label.getStyleClass().add("label-proceso");
                } else {
                    if (ventaTB.getTipo() == 1 && ventaTB.getEstado() == 1
                            || ventaTB.getTipo() == 2 && ventaTB.getEstado() == 1) {
                        label.setText("COBRADO");
                        label.getStyleClass().add("label-asignacion");
                    } else if (ventaTB.getTipo() == 2 && ventaTB.getEstado() == 2) {
                        label.setText("POR COBRAR");
                        label.getStyleClass().add("label-medio");
                    } else if (ventaTB.getTipo() == 1 && ventaTB.getEstado() == 4) {
                        label.setText("POR LLEVAR");
                        label.getStyleClass().add("label-ultimo");
                    } else {
                        label.setText("ANULADO");
                        label.getStyleClass().add("label-proceso");
                    }
                }

                if (ventaTB.getTipo() == 1) {
                    ventaTB.setTipoName("CONTADO");
                } else {
                    ventaTB.setTipoName("CRÉDITO");
                }
                ventaTB.setEstadoLabel(label);
                empList.add(ventaTB);
            }

            preparedStatement.close();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Ventas_All_Count(?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, 0);
            preparedStatement.setInt(6, estado);
            rsEmps.close();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Listar_Ventas_Libres(int opcion, String value, String fechaInicial, String fechaFinal,
            int comprobante, int estado, String usuario, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        PreparedStatement preparedStatementSumar = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[3];
            ObservableList<VentaTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Ventas(?,?,?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, comprobante);
            preparedStatement.setInt(6, estado);
            preparedStatement.setString(7, usuario);
            preparedStatement.setInt(8, posicionPagina);
            preparedStatement.setInt(9, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(rsEmps.getRow() + posicionPagina);
                ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                ventaTB.setFechaVenta(
                        rsEmps.getDate("FechaVenta").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        rsEmps.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(rsEmps.getString("DocumentoCliente"));
                clienteTB.setInformacion(rsEmps.getString("Cliente"));
                ventaTB.setClienteTB(clienteTB);

                ventaTB.setComprobanteName(rsEmps.getString("Comprobante"));
                ventaTB.setSerie(rsEmps.getString("Serie"));
                ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                ventaTB.setTipo(rsEmps.getInt("Tipo"));
                ventaTB.setEstado(rsEmps.getInt("Estado"));
                ventaTB.setTotal(rsEmps.getDouble("Total"));
                ventaTB.setObservaciones(rsEmps.getString("Observaciones"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                monedaTB.setNombre(rsEmps.getString("NombreMoneda"));
                monedaTB.setAbreviado(rsEmps.getString("TipoMoneda"));
                ventaTB.setMonedaTB(monedaTB);

                if (rsEmps.getInt("IdNotaCredito") == 1) {
                    NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
                    notaCreditoTB.setSerie(rsEmps.getString("SerieNotaCredito"));
                    notaCreditoTB.setNumeracion(rsEmps.getString("NumeracionNotaCredito"));
                    ventaTB.setNotaCreditoTB(notaCreditoTB);
                }

                Label label = new Label();
                if (ventaTB.getNotaCreditoTB() != null) {
                    label.setText("MODIFICADO");
                    label.getStyleClass().add("label-proceso");
                } else {
                    if (ventaTB.getEstado() == 3) {
                        label.setText("ANULADO");
                        label.getStyleClass().add("label-proceso");
                    } else if (ventaTB.getTipo() == 2 && ventaTB.getEstado() == 2) {
                        label.setText("POR COBRAR");
                        label.getStyleClass().add("label-medio");
                    } else if (ventaTB.getTipo() == 1 && ventaTB.getEstado() == 4) {
                        label.setText("POR LLEVAR");
                        label.getStyleClass().add("label-ultimo");
                    } else {
                        label.setText("COBRADO");
                        label.getStyleClass().add("label-asignacion");
                    }
                }

                if (ventaTB.getTipo() == 1) {
                    ventaTB.setTipoName("CONTADO");
                } else {
                    ventaTB.setTipoName("CRÉDITO");
                }
                ventaTB.setEstadoLabel(label);
                empList.add(ventaTB);
            }

            preparedStatementCount = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Ventas_Count(?,?,?,?,?,?,?)}");
            preparedStatementCount.setInt(1, opcion);
            preparedStatementCount.setString(2, value);
            preparedStatementCount.setString(3, fechaInicial);
            preparedStatementCount.setString(4, fechaFinal);
            preparedStatementCount.setInt(5, comprobante);
            preparedStatementCount.setInt(6, estado);
            preparedStatementCount.setString(7, usuario);
            rsEmps.close();
            rsEmps = preparedStatementCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            preparedStatementSumar = dbf.getConnection().prepareStatement("{call Sp_Sumar_Ventas_Realizadas(?,?,?,?)}");
            preparedStatementSumar.setString(1, fechaInicial);
            preparedStatementSumar.setString(2, fechaFinal);
            preparedStatementSumar.setString(3, usuario);
            preparedStatementSumar.setInt(4, 1);
            rsEmps = preparedStatementSumar.executeQuery();
            double montoTotal = 0;
            if (rsEmps.next()) {
                montoTotal = rsEmps.getDouble("Monto");
            }

            objects[0] = empList;
            objects[1] = cantidadTotal;
            objects[2] = montoTotal;

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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static Object Listar_Ventas_Pos(short opcion, String value, String fechaInicial, String fechaFinal,
            int comprobante, int estado, String usuario, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        PreparedStatement preparedStatementSuma = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[3];
            ObservableList<VentaTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Pos_Ventas(?,?,?,?,?,?,?,?,?)}");
            preparedStatement.setShort(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, comprobante);
            preparedStatement.setInt(6, estado);
            preparedStatement.setString(7, usuario);
            preparedStatement.setInt(8, posicionPagina);
            preparedStatement.setInt(9, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(rsEmps.getRow() + posicionPagina);
                ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                ventaTB.setFechaVenta(
                        rsEmps.getDate("FechaVenta").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        rsEmps.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ventaTB.setComprobanteName(rsEmps.getString("Comprobante"));
                ventaTB.setSerie(rsEmps.getString("Serie"));
                ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                ventaTB.setTipo(rsEmps.getInt("Tipo"));
                ventaTB.setEstado(rsEmps.getInt("Estado"));
                ventaTB.setTotal(rsEmps.getDouble("Total"));
                ventaTB.setObservaciones(rsEmps.getString("Observaciones"));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(rsEmps.getString("DocumentoCliente"));
                clienteTB.setInformacion(rsEmps.getString("Cliente"));
                ventaTB.setClienteTB(clienteTB);

                if (rsEmps.getInt("IdNotaCredito") == 1) {
                    NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
                    notaCreditoTB.setSerie(rsEmps.getString("SerieNotaCredito"));
                    notaCreditoTB.setNumeracion(rsEmps.getString("NumeracionNotaCredito"));
                    ventaTB.setNotaCreditoTB(notaCreditoTB);
                }

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(rsEmps.getString("NombreMoneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                monedaTB.setAbreviado(rsEmps.getString("TipoMoneda"));
                ventaTB.setMonedaTB(monedaTB);

                Label label = new Label();
                if (ventaTB.getNotaCreditoTB() != null) {
                    label.setText("MODIFICADO");
                    label.getStyleClass().add("label-proceso");
                } else {
                    if (ventaTB.getEstado() == 3) {
                        label.setText("ANULADO");
                        label.getStyleClass().add("label-proceso");
                    } else if (ventaTB.getTipo() == 2 && ventaTB.getEstado() == 2) {
                        label.setText("POR COBRAR");
                        label.getStyleClass().add("label-medio");
                    } else if (ventaTB.getTipo() == 1 && ventaTB.getEstado() == 4) {
                        label.setText("POR LLEVAR");
                        label.getStyleClass().add("label-ultimo");
                    } else {
                        label.setText("COBRADO");
                        label.getStyleClass().add("label-asignacion");
                    }
                }

                if (ventaTB.getTipo() == 1) {
                    ventaTB.setTipoName("CONTADO");
                } else {
                    ventaTB.setTipoName("CRÉDITO");
                }
                ventaTB.setEstadoLabel(label);
                empList.add(ventaTB);
            }

            preparedStatementCount = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Pos_Ventas_Count(?,?,?,?,?,?,?)}");
            preparedStatementCount.setShort(1, opcion);
            preparedStatementCount.setString(2, value);
            preparedStatementCount.setString(3, fechaInicial);
            preparedStatementCount.setString(4, fechaFinal);
            preparedStatementCount.setInt(5, comprobante);
            preparedStatementCount.setInt(6, estado);
            preparedStatementCount.setString(7, usuario);
            rsEmps.close();
            rsEmps = preparedStatementCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            preparedStatementSuma = dbf.getConnection().prepareStatement("{call Sp_Sumar_Ventas_Realizadas(?,?,?,?)}");
            preparedStatementSuma.setString(1, fechaInicial);
            preparedStatementSuma.setString(2, fechaFinal);
            preparedStatementSuma.setString(3, usuario);
            preparedStatementSuma.setInt(4, 2);
            rsEmps = preparedStatementSuma.executeQuery();
            double montoTotal = 0;
            if (rsEmps.next()) {
                montoTotal = rsEmps.getDouble("Monto");
            }

            objects[0] = empList;
            objects[1] = cantidadTotal;
            objects[2] = montoTotal;

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
                if (preparedStatementCount != null) {
                    preparedStatementCount.close();
                }
                if (preparedStatementSuma != null) {
                    preparedStatementSuma.close();
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

    public static Object ListVentasMostrarLibres(boolean tipo, int opcion, String search, String idEmpleado,
            int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] object = new Object[2];

            ObservableList<VentaTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Ventas_Mostrar(?,?,?,?,?,?)}");
            preparedStatement.setBoolean(1, tipo);
            preparedStatement.setInt(2, opcion);
            preparedStatement.setString(3, search);
            preparedStatement.setString(4, idEmpleado);
            preparedStatement.setInt(5, posicionPagina);
            preparedStatement.setInt(6, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(rsEmps.getRow() + posicionPagina);
                ventaTB.setClienteTB(new ClienteTB(rsEmps.getString("NumeroDocumento"), rsEmps.getString("Cliente")));
                ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                ventaTB.setFechaVenta(
                        rsEmps.getDate("FechaVenta").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        rsEmps.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ventaTB.setSerie(rsEmps.getString("Serie"));
                ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                ventaTB.setTotal(rsEmps.getDouble("Total"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(rsEmps.getInt("IdMoneda"));
                monedaTB.setNombre(rsEmps.getString("NombreMoneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                monedaTB.setAbreviado(rsEmps.getString("Abreviado"));
                ventaTB.setMonedaTB(monedaTB);

                if (rsEmps.getInt("IdNotaCredito") == 1) {
                    NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
                    notaCreditoTB.setSerie(rsEmps.getString("SerieNotaCredito"));
                    notaCreditoTB.setNumeracion(rsEmps.getString("NumeracionNotaCredito"));
                    ventaTB.setNotaCreditoTB(notaCreditoTB);
                }

                Button btnImprimir = new Button();
                ImageView imageViewPrint = new ImageView(new Image("/view/image/print.png"));
                imageViewPrint.setFitWidth(24);
                imageViewPrint.setFitHeight(24);
                btnImprimir.setGraphic(imageViewPrint);
                btnImprimir.getStyleClass().add("buttonLightError");
                ventaTB.setBtnImprimir(btnImprimir);

                Button btnAddVenta = new Button();
                ImageView imageViewAccept = new ImageView(new Image("/view/image/accept.png"));
                imageViewAccept.setFitWidth(24);
                imageViewAccept.setFitHeight(24);
                btnAddVenta.setGraphic(imageViewAccept);
                btnAddVenta.getStyleClass().add("buttonLightError");
                ventaTB.setBtnAgregar(btnAddVenta);

                Button btnPlusVenta = new Button();
                ImageView imageViewPlus = new ImageView(new Image("/view/image/plus.png"));
                imageViewPlus.setFitWidth(24);
                imageViewPlus.setFitHeight(24);
                btnPlusVenta.setGraphic(imageViewPlus);
                btnPlusVenta.getStyleClass().add("buttonLightError");
                ventaTB.setBtnSumar(btnPlusVenta);

                empList.add(ventaTB);
            }

            preparedStatement.close();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Ventas_Mostrar_Count(?,?,?,?)}");
            preparedStatement.setBoolean(1, tipo);
            preparedStatement.setInt(2, opcion);
            preparedStatement.setString(3, search);
            preparedStatement.setString(4, idEmpleado);
            rsEmps.close();
            rsEmps = preparedStatement.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            object[0] = empList;
            object[1] = cantidadTotal;

            return object;
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Listar_Ventas_Mostrar_Pos(boolean tipo, int opcion, String search, String idEmpleado,
            int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] object = new Object[2];

            ObservableList<VentaTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Pos_Venta_Mostrar(?,?,?,?,?,?)}");
            preparedStatement.setBoolean(1, tipo);
            preparedStatement.setInt(2, opcion);
            preparedStatement.setString(3, search);
            preparedStatement.setString(4, idEmpleado);
            preparedStatement.setInt(5, posicionPagina);
            preparedStatement.setInt(6, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(rsEmps.getRow() + posicionPagina);
                ventaTB.setIdVenta(rsEmps.getString("IdVenta"));
                ventaTB.setFechaVenta(
                        rsEmps.getDate("FechaVenta").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        rsEmps.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ventaTB.setSerie(rsEmps.getString("Serie"));
                ventaTB.setNumeracion(rsEmps.getString("Numeracion"));
                ventaTB.setTotal(rsEmps.getDouble("Total"));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Cliente"));
                ventaTB.setClienteTB(clienteTB);

                if (rsEmps.getInt("IdNotaCredito") == 1) {
                    NotaCreditoTB notaCreditoTB = new NotaCreditoTB();
                    notaCreditoTB.setSerie(rsEmps.getString("SerieNotaCredito"));
                    notaCreditoTB.setNumeracion(rsEmps.getString("NumeracionNotaCredito"));
                    ventaTB.setNotaCreditoTB(notaCreditoTB);
                }

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(rsEmps.getString("NombreMoneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                monedaTB.setAbreviado(rsEmps.getString("Abreviado"));
                ventaTB.setMonedaTB(monedaTB);

                Button btnImprimir = new Button();
                ImageView imageViewPrint = new ImageView(new Image("/view/image/print.png"));
                imageViewPrint.setFitWidth(24);
                imageViewPrint.setFitHeight(24);
                btnImprimir.setGraphic(imageViewPrint);
                btnImprimir.getStyleClass().add("buttonLightError");
                ventaTB.setBtnImprimir(btnImprimir);

                Button btnAddVenta = new Button();
                ImageView imageViewAccept = new ImageView(new Image("/view/image/accept.png"));
                imageViewAccept.setFitWidth(24);
                imageViewAccept.setFitHeight(24);
                btnAddVenta.setGraphic(imageViewAccept);
                btnAddVenta.getStyleClass().add("buttonLightError");
                ventaTB.setBtnAgregar(btnAddVenta);

                Button btnPlusVenta = new Button();
                ImageView imageViewPlus = new ImageView(new Image("/view/image/plus.png"));
                imageViewPlus.setFitWidth(24);
                imageViewPlus.setFitHeight(24);
                btnPlusVenta.setGraphic(imageViewPlus);
                btnPlusVenta.getStyleClass().add("buttonLightError");
                ventaTB.setBtnSumar(btnPlusVenta);

                empList.add(ventaTB);
            }

            preparedStatement.close();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_Pos_Ventas_Mostrar_Count(?,?,?,?)}");
            preparedStatement.setBoolean(1, tipo);
            preparedStatement.setInt(2, opcion);
            preparedStatement.setString(3, search);
            preparedStatement.setString(4, idEmpleado);
            rsEmps.close();
            rsEmps = preparedStatement.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            object[0] = empList;
            object[1] = cantidadTotal;

            return object;
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Venta_ById(String idVenta) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementVenta = null;
        PreparedStatement statementEmpleado = null;
        PreparedStatement statementEmpresa = null;
        PreparedStatement statementVentaDetalle = null;
        try {
            dbf.dbConnect();
            statementVenta = dbf.getConnection().prepareStatement("{call Sp_Obtener_Venta_ById(?)}");
            statementVenta.setString(1, idVenta);
            ResultSet resultSetVenta = statementVenta.executeQuery();
            if (resultSetVenta.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setIdVenta(idVenta);
                ventaTB.setFechaVenta(resultSetVenta.getDate("FechaVenta").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(resultSetVenta.getTime("HoraVenta").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                // Cliente start
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(resultSetVenta.getString("IdCliente"));
                clienteTB.setIdAuxiliar(resultSetVenta.getString("IdAuxiliar"));
                clienteTB.setTipoDocumento(resultSetVenta.getInt("TipoDocumento"));
                clienteTB.setTipoDocumentoName(resultSetVenta.getString("NombreDocumento"));
                clienteTB.setNumeroDocumento(resultSetVenta.getString("NumeroDocumento"));
                clienteTB.setInformacion(resultSetVenta.getString("Informacion"));
                clienteTB.setTelefono(resultSetVenta.getString("Telefono"));
                clienteTB.setCelular(resultSetVenta.getString("Celular"));
                clienteTB.setEmail(resultSetVenta.getString("Email"));
                clienteTB.setDireccion(resultSetVenta.getString("Direccion"));
                clienteTB.setIdMotivoTraslado(resultSetVenta.getInt("IdMotivoTraslado"));

                UbigeoTB ubigeoTB = new UbigeoTB();
                ubigeoTB.setIdUbigeo(resultSetVenta.getInt("IdUbigeo"));
                ubigeoTB.setUbigeo(resultSetVenta.getString("CodigoUbigeo"));
                ubigeoTB.setDepartamento(resultSetVenta.getString("Departamento"));
                ubigeoTB.setProvincia(resultSetVenta.getString("Provincia"));
                ubigeoTB.setDistrito(resultSetVenta.getString("Distrito"));
                clienteTB.setUbigeoTB(ubigeoTB);

                ventaTB.setClienteTB(clienteTB);
                // Cliente end
                ventaTB.setIdMoneda(resultSetVenta.getInt("IdMoneda"));
                ventaTB.setCodigoAlterno(resultSetVenta.getString("CodigoAlterno"));
                ventaTB.setIdComprobante(resultSetVenta.getInt("IdComprobante"));
                ventaTB.setComprobanteName(resultSetVenta.getString("Comprobante"));
                ventaTB.setSerie(resultSetVenta.getString("Serie"));
                ventaTB.setNumeracion(resultSetVenta.getString("Numeracion"));
                ventaTB.setObservaciones(resultSetVenta.getString("Observaciones"));
                ventaTB.setTipo(resultSetVenta.getInt("Tipo"));
                ventaTB.setEstado(resultSetVenta.getInt("Estado"));
                ventaTB.setEfectivo(resultSetVenta.getDouble("Efectivo"));
                ventaTB.setVuelto(resultSetVenta.getDouble("Vuelto"));
                ventaTB.setTarjeta(resultSetVenta.getDouble("Tarjeta"));
                ventaTB.setDeposito(resultSetVenta.getDouble("Deposito"));
                ventaTB.setCodigo(resultSetVenta.getString("Codigo"));

                if (ventaTB.getTipo() == 1) {
                    ventaTB.setTipoName("CONTADO");
                } else {
                    ventaTB.setTipoName("CRÉDITO");
                }

                if (ventaTB.getEstado() == 3) {
                    ventaTB.setEstadoName("ANULADO");
                } else if (ventaTB.getTipo() == 2 && ventaTB.getEstado() == 2) {
                    ventaTB.setEstadoName("POR COBRAR");
                } else if (ventaTB.getTipo() == 1 && ventaTB.getEstado() == 4) {
                    ventaTB.setEstadoName("POR LLEVAR");
                } else {
                    ventaTB.setEstadoName("COBRADO");
                }

                // moneda start
                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(resultSetVenta.getString("Nombre"));
                monedaTB.setAbreviado(resultSetVenta.getString("Abreviado"));
                monedaTB.setSimbolo(resultSetVenta.getString("Simbolo"));
                ventaTB.setMonedaTB(monedaTB);
                // moneda end

                // empleado start
                statementEmpleado = dbf.getConnection().prepareStatement("SELECT "
                        + "e.Apellidos, "
                        + "e.Nombres "
                        + "FROM VentaTB AS v INNER JOIN EmpleadoTB AS e "
                        + "ON v.Vendedor = e.IdEmpleado "
                        + "WHERE v.IdVenta = ?");
                statementEmpleado.setString(1, idVenta);
                try (ResultSet resultSetEmpleado = statementEmpleado.executeQuery()) {
                    if (resultSetEmpleado.next()) {
                        EmpleadoTB empleadoTB = new EmpleadoTB();
                        empleadoTB.setApellidos(resultSetEmpleado.getString("Apellidos"));
                        empleadoTB.setNombres(resultSetEmpleado.getString("Nombres"));
                        ventaTB.setEmpleadoTB(empleadoTB);
                    }
                }

                // empleado end
                // detalle venta start
                statementVentaDetalle = dbf.getConnection()
                        .prepareStatement("{call Sp_Listar_Ventas_Detalle_By_Id(?)}");
                statementVentaDetalle.setString(1, idVenta);
                ResultSet resultSetLista = statementVentaDetalle.executeQuery();
                ArrayList<SuministroTB> empList = new ArrayList<>();
                while (resultSetLista.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(resultSetLista.getRow());
                    suministroTB.setIdSuministro(resultSetLista.getString("IdSuministro"));
                    suministroTB.setClave(resultSetLista.getString("Clave"));
                    suministroTB.setNombreMarca(resultSetLista.getString("NombreMarca"));
                    suministroTB.setBonificacionTexto((resultSetLista.getDouble("Bonificacion") <= 0 ? ""
                            : "BONIFICACIÓN: " + resultSetLista.getDouble("Bonificacion")));
                    suministroTB.setInventario(resultSetLista.getBoolean("Inventario"));
                    suministroTB.setValorInventario(resultSetLista.getShort("ValorInventario"));
                    suministroTB.setUnidadCompra(resultSetLista.getInt("IdUnidadCompra"));
                    suministroTB.setUnidadCompraName(resultSetLista.getString("UnidadCompra"));
                    suministroTB.setEstadoName(resultSetLista.getString("Estado"));

                    suministroTB.setPorLlevar(resultSetLista.getDouble("PorLlevar"));
                    suministroTB.setDescuento(resultSetLista.getDouble("Descuento"));
                    suministroTB.setCantidad(resultSetLista.getDouble("Cantidad"));
                    suministroTB.setBonificacion(resultSetLista.getDouble("Bonificacion"));
                    suministroTB.setCostoCompra(resultSetLista.getDouble("CostoVenta"));
                    suministroTB.setPrecioVentaGeneral(resultSetLista.getDouble("PrecioVenta"));

                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(resultSetLista.getInt("IdImpuesto"));
                    impuestoTB.setOperacion(resultSetLista.getInt("Operacion"));
                    impuestoTB.setNombreImpuesto(resultSetLista.getString("NombreImpuesto"));
                    impuestoTB.setValor(resultSetLista.getDouble("ValorImpuesto"));
                    suministroTB.setImpuestoTB(impuestoTB);

                    suministroTB.setInventario(resultSetLista.getBoolean("Inventario"));
                    suministroTB.setUnidadVenta(resultSetLista.getInt("UnidadVenta"));
                    suministroTB.setValorInventario(resultSetLista.getShort("ValorInventario"));

                    empList.add(suministroTB);
                }
                ventaTB.setSuministroTBs(empList);

                statementEmpresa = dbf.getConnection().prepareStatement("SELECT "
                        + "TOP 1 d.IdAuxiliar,"
                        + "e.NumeroDocumento,"
                        + "e.RazonSocial,"
                        + "e.NombreComercial,"
                        + "e.Domicilio,"
                        + "e.Telefono,"
                        + "e.Email "
                        + "FROM EmpresaTB AS e "
                        + "INNER JOIN DetalleTB AS d "
                        + "ON e.TipoDocumento = d.IdDetalle AND d.IdMantenimiento = '0003'");
                ResultSet resultSetEmpresa = statementEmpresa.executeQuery();
                resultSetEmpresa.next();
                EmpresaTB empresaTB = new EmpresaTB();
                empresaTB.setNumeroDocumento(resultSetEmpresa.getString("NumeroDocumento"));
                empresaTB.setRazonSocial(resultSetEmpresa.getString("RazonSocial"));
                empresaTB.setNombreComercial(resultSetEmpresa.getString("NombreComercial"));
                empresaTB.setDomicilio(resultSetEmpresa.getString("Domicilio"));
                ventaTB.setEmpresaTB(empresaTB);

                return ventaTB;
            } else {
                throw new Exception("No se puedo obtener el detalle de la venta, intente nuvemante por favor.");
            }
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementEmpleado != null) {
                    statementEmpleado.close();
                }
                if (statementVentaDetalle != null) {
                    statementVentaDetalle.close();
                }
                if (statementEmpresa != null) {
                    statementEmpresa.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String Anular_Venta_ById(String idVenta, ArrayList<SuministroTB> arrList, String motivo) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementKardex = null;
        PreparedStatement statementIngreso = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ? and Estado = ?");
            statementValidar.setString(1, idVenta);
            statementValidar.setInt(2, 3);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "scrambled";
            }

            statementValidar.close();
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaCreditoTB WHERE IdVenta = ?");
            statementValidar.setString(1, idVenta);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "ventacredito";
            }

            statementValidar.close();
            statementValidar = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ?");
            statementValidar.setString(1, idVenta);
            ResultSet resultSet = statementValidar.executeQuery();
            resultSet.next();

            statementVenta = dbf.getConnection()
                    .prepareStatement("UPDATE VentaTB SET Estado = ?, Observaciones = ? WHERE IdVenta = ?");
            statementVenta.setInt(1, 3);
            statementVenta.setString(2, Session.USER_NAME + " ANULÓ LA VENTA POR EL MOTIVO: " + motivo.toUpperCase());
            statementVenta.setString(3, idVenta);
            statementVenta.addBatch();
            statementVenta.executeBatch();

            statementSuministro = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?");

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
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            if (resultSet.getInt("Estado") != 4) {
                for (SuministroTB stb : arrList) {
                    if (stb.isInventario() && stb.getValorInventario() == 1) {
                        statementSuministro.setDouble(1, stb.getCantidad() + stb.getBonificacion());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    } else if (stb.isInventario() && stb.getValorInventario() == 2) {
                        statementSuministro.setDouble(1, stb.getCantidad());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    } else if (stb.isInventario() && stb.getValorInventario() == 3) {
                        statementSuministro.setDouble(1, stb.getCantidad());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    }

                    double cantidadTotal = stb.getValorInventario() == 1
                            ? stb.getCantidad() + stb.getBonificacion()
                            : stb.getValorInventario() == 2
                                    ? stb.getCantidad()
                                    : stb.getCantidad();

                    statementKardex.setString(1, stb.getIdSuministro());
                    statementKardex.setString(2, Tools.getDate());
                    statementKardex.setString(3, Tools.getTime());
                    statementKardex.setShort(4, (short) 1);
                    statementKardex.setInt(5, 2);
                    statementKardex.setString(6, "DEVOLUCIÓN DE PRODUCTO");
                    statementKardex.setDouble(7, cantidadTotal);
                    statementKardex.setDouble(8, stb.getCostoCompra());
                    statementKardex.setDouble(9, cantidadTotal * stb.getCostoCompra());
                    statementKardex.setInt(10, 0);
                    statementKardex.addBatch();
                }
            }

            statementIngreso = dbf.getConnection().prepareStatement("DELETE FROM IngresoTB WHERE IdProcedencia = ?");
            statementIngreso.setString(1, idVenta);
            statementIngreso.addBatch();

            statementVenta.executeBatch();
            statementSuministro.executeBatch();
            statementKardex.executeBatch();
            statementIngreso.executeBatch();

            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementKardex != null) {
                    statementKardex.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
        }
    }

    public static String Anular_PosVenta_ById(String idVenta, ArrayList<SuministroTB> arrList, String motivo) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidar = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementKardex = null;
        PreparedStatement statementMovimientoCaja = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // statementValidar = dbf.getConnection().prepareStatement("SELECT IdCaja
            // FROM CajaTB WHERE IdUsuario = ? AND Estado = 1");
            // statementValidar.setString(1, Session.USER_ID);
            // if (statementValidar.executeQuery().next()) {
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ? and Estado = ?");
            statementValidar.setString(1, idVenta);
            statementValidar.setInt(2, 3);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "scrambled";
            }

            statementValidar.close();
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaCreditoTB WHERE IdVenta = ?");
            statementValidar.setString(1, idVenta);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "ventacredito";
            }

            statementValidar.close();
            statementValidar = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ?");
            statementValidar.setString(1, idVenta);
            ResultSet resultSet = statementValidar.executeQuery();
            resultSet.next();

            statementVenta = dbf.getConnection()
                    .prepareStatement("UPDATE VentaTB SET Estado = ?, Observaciones = ? WHERE IdVenta = ?");
            statementVenta.setInt(1, 3);
            statementVenta.setString(2,
                    Session.USER_NAME.toUpperCase() + "ANULÓ LA VENTA POR EL MOTIVO -> " + motivo.toUpperCase());
            statementVenta.setString(3, idVenta);
            statementVenta.addBatch();
            statementVenta.executeBatch();

            statementSuministro = dbf.getConnection().prepareStatement("UPDATE SuministroTB "
                    + "SET Cantidad = Cantidad + ? "
                    + "WHERE IdSuministro = ?");

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
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            if (resultSet.getInt("Estado") != 4) {
                for (SuministroTB stb : arrList) {
                    if (stb.isInventario() && stb.getValorInventario() == 1) {
                        statementSuministro.setDouble(1, stb.getCantidad() + stb.getBonificacion());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    } else if (stb.isInventario() && stb.getValorInventario() == 2) {
                        statementSuministro.setDouble(1, stb.getCantidad());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    } else if (stb.isInventario() && stb.getValorInventario() == 3) {
                        statementSuministro.setDouble(1, stb.getCantidad());
                        statementSuministro.setString(2, stb.getIdSuministro());
                        statementSuministro.addBatch();
                    }

                    double cantidadTotal = stb.getValorInventario() == 1
                            ? stb.getCantidad() + stb.getBonificacion()
                            : stb.getValorInventario() == 2
                                    ? stb.getCantidad()
                                    : stb.getCantidad();

                    statementKardex.setString(1, stb.getIdSuministro());
                    statementKardex.setString(2, Tools.getDate());
                    statementKardex.setString(3, Tools.getTime());
                    statementKardex.setShort(4, (short) 1);
                    statementKardex.setInt(5, 2);
                    statementKardex.setString(6, "DEVOLUCIÓN DE PRODUCTO");
                    statementKardex.setDouble(7, cantidadTotal);
                    statementKardex.setDouble(8, stb.getCostoCompra());
                    statementKardex.setDouble(9, cantidadTotal * stb.getCostoCompra());
                    statementKardex.setInt(10, 0);
                    statementKardex.addBatch();
                }
            }

            statementMovimientoCaja = dbf.getConnection()
                    .prepareStatement("DELETE FROM MovimientoCajaTB WHERE IdProcedencia = ?");
            statementMovimientoCaja.setString(1, idVenta);
            statementMovimientoCaja.addBatch();

            statementVenta.executeBatch();
            statementSuministro.executeBatch();
            statementKardex.executeBatch();
            statementMovimientoCaja.executeBatch();

            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            Tools.println(ex.getLocalizedMessage());
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementKardex != null) {
                    statementKardex.close();
                }
                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String movimientoIngreso(IngresoTB ingresoTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement ingreso = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            ingreso = dbf.getConnection().prepareStatement(
                    "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?)");
            ingreso.setString(1, ingresoTB.getIdProcedencia());
            ingreso.setString(2, ingresoTB.getIdUsuario());
            ingreso.setString(3, ingresoTB.getDetalle());
            ingreso.setInt(4, ingresoTB.getProcedencia());
            ingreso.setString(5, ingresoTB.getFecha());
            ingreso.setString(6, ingresoTB.getHora());
            ingreso.setInt(7, ingresoTB.getForma());
            ingreso.setDouble(8, ingresoTB.getMonto());
            ingreso.addBatch();

            ingreso.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (ingreso != null) {
                    ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String movimientoCaja(MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementMovimientoCaja = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementMovimientoCaja = dbf.getConnection().prepareStatement(
                    "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto)VALUES(?,?,?,?,?,?)");
            statementMovimientoCaja.setString(1, Session.CAJA_ID);
            statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
            statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
            statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario());
            statementMovimientoCaja.setInt(5, movimientoCajaTB.getTipoMovimiento());
            statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
            statementMovimientoCaja.addBatch();

            statementMovimientoCaja.executeBatch();
            dbf.getConnection().commit();
            return "updated";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Reporte_Genetal_Ventas(int procedencia, String fechaInicial, String fechaFinal,
            int tipoDocumento, String cliente, String vendedor, int tipo, boolean metodo, int valormetodo) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ArrayList<VentaTB> arrayList = new ArrayList<>();
            preparedStatement = dbf.getConnection()
                    .prepareCall("{call Sp_Reporte_General_Ventas(?,?,?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, procedencia);
            preparedStatement.setString(2, fechaInicial);
            preparedStatement.setString(3, fechaFinal);
            preparedStatement.setInt(4, tipoDocumento);
            preparedStatement.setString(5, cliente);
            preparedStatement.setString(6, vendedor);
            preparedStatement.setInt(7, tipo);
            preparedStatement.setBoolean(8, metodo);
            preparedStatement.setInt(9, valormetodo);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setFechaVenta(resultSet.getDate("FechaVenta").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setIdCliente(resultSet.getString("NumeroDocumento") + "\n" + resultSet.getString("Cliente"));
                ventaTB.setClienteTB(
                        new ClienteTB(resultSet.getString("NumeroDocumento"), resultSet.getString("Cliente")));
                ventaTB.setComprobanteName(resultSet.getString("Nombre"));
                ventaTB.setSerie(resultSet.getString("Serie"));
                ventaTB.setNumeracion(resultSet.getString("Numeracion"));
                ventaTB.setTipo(resultSet.getInt("Tipo"));
                ventaTB.setTipoName(resultSet.getString("TipoName"));
                ventaTB.setEstado(resultSet.getInt("Estado"));
                ventaTB.setEstadoName(resultSet.getString("EstadoName"));
                ventaTB.setFormaName(resultSet.getString("FormaName"));
                ventaTB.setEfectivo(resultSet.getDouble("Efectivo"));
                ventaTB.setTarjeta(resultSet.getDouble("Tarjeta"));
                ventaTB.setDeposito(resultSet.getDouble("Deposito"));
                ventaTB.setTotal(resultSet.getDouble("Total"));
                if (resultSet.getInt("IdNotaCredito") == 1) {
                    ventaTB.setNotaCreditoTB(new NotaCreditoTB());
                }

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("NombreMoneda"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                ventaTB.setMonedaTB(monedaTB);

                arrayList.add(ventaTB);
            }
            return arrayList;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
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

    public static Object Listar_Ventas_Credito(int opcion, String buscar, boolean mostrar, String fechaInicial,
            String fechaFinal, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();

        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            Object[] objects = new Object[2];
            preparedStatement = dbf.getConnection().prepareCall("{call Sp_Listar_Ventas_Credito(?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, buscar);
            preparedStatement.setBoolean(3, mostrar);
            preparedStatement.setString(4, fechaInicial);
            preparedStatement.setString(5, fechaFinal);
            preparedStatement.setInt(6, posicionPagina);
            preparedStatement.setInt(7, filasPorPagina);
            resultSet = preparedStatement.executeQuery();
            ObservableList<VentaTB> arrayList = FXCollections.observableArrayList();
            while (resultSet.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setId(resultSet.getRow() + posicionPagina);
                ventaTB.setIdVenta(resultSet.getString("IdVenta"));
                ventaTB.setSerie(resultSet.getString("Serie"));
                ventaTB.setNumeracion(resultSet.getString("Numeracion"));
                ventaTB.setFechaVenta(resultSet.getDate("FechaVenta").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        resultSet.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ventaTB.setEstado(resultSet.getInt("Estado"));
                ventaTB.setEstadoName(
                        ventaTB.getEstado() == 3 ? "CANCELADO" : ventaTB.getEstado() == 2 ? "POR PAGAR" : "PAGADO");
                ventaTB.setMontoTotal(resultSet.getDouble("MontoTotal"));
                ventaTB.setMontoCobrado(resultSet.getDouble("MontoCobrado"));
                ventaTB.setMontoRestante(ventaTB.getMontoTotal() - ventaTB.getMontoCobrado());

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                clienteTB.setInformacion(resultSet.getString("Informacion"));
                ventaTB.setClienteTB(clienteTB);

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("NombreMoneda"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                ventaTB.setMonedaTB(monedaTB);

                Label label = new Label(
                        ventaTB.getEstado() == 3 ? "ANULADO" : ventaTB.getEstado() == 2 ? "POR COBRAR" : "COBRADO");
                label.getStyleClass().add(ventaTB.getEstado() == 2 ? "label-medio"
                        : ventaTB.getEstado() == 3 ? "label-proceso" : "label-asignacion");
                ventaTB.setEstadoLabel(label);

                Button btnVisualizar = new Button();
                ImageView imageViewVisualizar = new ImageView(new Image("/view/image/search_caja.png"));
                imageViewVisualizar.setFitWidth(24);
                imageViewVisualizar.setFitHeight(24);
                btnVisualizar.setGraphic(imageViewVisualizar);
                btnVisualizar.getStyleClass().add("buttonLightWarning");
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                hBox.setStyle(";-fx-spacing:0.8333333333333334em;");
                hBox.getChildren().add(btnVisualizar);
                ventaTB.setHbOpciones(hBox);

                arrayList.add(ventaTB);
            }

            objects[0] = arrayList;

            resultSet.close();
            preparedStatementCount = dbf.getConnection()
                    .prepareCall("{call Sp_Listar_Ventas_Credito_Count(?,?,?,?,?)}");
            preparedStatementCount.setInt(1, opcion);
            preparedStatementCount.setString(2, buscar);
            preparedStatementCount.setBoolean(3, mostrar);
            preparedStatementCount.setString(4, fechaInicial);
            preparedStatementCount.setString(5, fechaFinal);
            resultSet = preparedStatementCount.executeQuery();
            Integer integer = 0;
            if (resultSet.next()) {
                integer = resultSet.getInt("Total");
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
                if (preparedStatementCount != null) {
                    preparedStatementCount.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

    }

    public static Object Listar_Ventas_Detalle_Credito_ById(String idVenta) {
        DBUtil dbf = new DBUtil();

        PreparedStatement preparedStatement = null;
        PreparedStatement statementVentaDetalle = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareCall("{call Sp_Obtener_Venta_ById_For_Credito(?)}");
            preparedStatement.setString(1, idVenta);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setIdVenta(idVenta);

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                clienteTB.setInformacion(resultSet.getString("Informacion"));
                clienteTB.setCelular(resultSet.getString("Celular"));
                clienteTB.setEmail(resultSet.getString("Email"));
                clienteTB.setDireccion(resultSet.getString("Direccion"));
                ventaTB.setClienteTB(clienteTB);

                ventaTB.setSerie(resultSet.getString("Serie"));
                ventaTB.setNumeracion(resultSet.getString("Numeracion"));
                ventaTB.setEstado(resultSet.getInt("Estado"));
                ventaTB.setEstadoName(resultSet.getString("EstadoName"));
                ventaTB.setTipoCredito(resultSet.getInt("TipoCredito"));
                ventaTB.setMontoTotal(resultSet.getDouble("MontoTotal"));
                ventaTB.setMontoCobrado(resultSet.getDouble("MontoCobrado"));
                ventaTB.setMontoRestante(resultSet.getDouble("MontoTotal") - resultSet.getDouble("MontoCobrado"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("Nombre"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                ventaTB.setMonedaTB(monedaTB);

                resultSet.close();
                preparedStatement.close();
                preparedStatement = dbf.getConnection().prepareCall("{call Sp_Listar_Detalle_Venta_Credito(?)}");
                preparedStatement.setString(1, idVenta);

                resultSet = preparedStatement.executeQuery();
                ArrayList<VentaCreditoTB> ventaCreditoTBs = new ArrayList<>();
                while (resultSet.next()) {
                    VentaCreditoTB ventaCreditoTB = new VentaCreditoTB();
                    ventaCreditoTB.setId(resultSet.getRow());
                    ventaCreditoTB.setIdVentaCredito(resultSet.getString("IdVentaCredito"));
                    ventaCreditoTB.setFechaPago(resultSet.getDate("FechaPago").toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    ventaCreditoTB.setHoraPago(resultSet.getTime("HoraPago").toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    ventaCreditoTB.setEstado(resultSet.getShort("Estado"));
                    ventaCreditoTB.setObservacion(resultSet.getString("Observacion"));
                    ventaCreditoTB.setEmpleadoTB(new EmpleadoTB(resultSet.getString("NumeroDocumento"),
                            resultSet.getString("Apellidos"), resultSet.getString("Nombres")));
                    ventaCreditoTB.setMonto(resultSet.getDouble("Monto"));

                    Button btnPagar = new Button();
                    ImageView imageViewVisualizar = new ImageView(new Image("/view/image/recibo.png"));
                    imageViewVisualizar.setFitWidth(24);
                    imageViewVisualizar.setFitHeight(24);
                    btnPagar.setGraphic(imageViewVisualizar);
                    btnPagar.getStyleClass().add("buttonLightSuccess");
                    ventaCreditoTB.setBtnPagar(btnPagar);

                    Button btnQuitar = new Button();
                    ImageView imageViewRemove = new ImageView(new Image("/view/image/remove-gray.png"));
                    imageViewRemove.setFitWidth(24);
                    imageViewRemove.setFitHeight(24);
                    btnQuitar.setGraphic(imageViewRemove);
                    btnQuitar.getStyleClass().add("buttonLightError");
                    ventaCreditoTB.setBtnQuitar(btnQuitar);

                    ventaCreditoTBs.add(ventaCreditoTB);
                }
                ventaTB.setVentaCreditoTBs(ventaCreditoTBs);

                statementVentaDetalle = dbf.getConnection()
                        .prepareStatement("{call Sp_Listar_Ventas_Detalle_By_Id(?)}");
                statementVentaDetalle.setString(1, idVenta);
                ResultSet resultSetLista = statementVentaDetalle.executeQuery();
                ArrayList<SuministroTB> empList = new ArrayList<SuministroTB>();
                while (resultSetLista.next()) {
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setId(resultSetLista.getRow());
                    suministroTB.setIdSuministro(resultSetLista.getString("IdSuministro"));
                    suministroTB.setClave(resultSetLista.getString("Clave"));
                    suministroTB.setNombreMarca(resultSetLista.getString("NombreMarca"));
                    suministroTB.setBonificacion(resultSetLista.getDouble("Bonificacion"));
                    suministroTB.setInventario(resultSetLista.getBoolean("Inventario"));
                    suministroTB.setValorInventario(resultSetLista.getShort("ValorInventario"));
                    suministroTB.setUnidadCompraName(resultSetLista.getString("UnidadCompra"));
                    suministroTB.setEstadoName(resultSetLista.getString("Estado"));

                    suministroTB.setPorLlevar(resultSetLista.getDouble("PorLlevar"));
                    suministroTB.setCantidad(resultSetLista.getDouble("Cantidad"));
                    suministroTB.setBonificacion(resultSetLista.getDouble("Bonificacion"));
                    suministroTB.setCostoCompra(resultSetLista.getDouble("CostoVenta"));

                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(resultSetLista.getInt("IdImpuesto"));
                    impuestoTB.setOperacion(resultSetLista.getInt("Operacion"));
                    impuestoTB.setNombreImpuesto(resultSetLista.getString("NombreImpuesto"));
                    impuestoTB.setValor(resultSetLista.getDouble("ValorImpuesto"));
                    suministroTB.setImpuestoTB(impuestoTB);

                    suministroTB.setDescuento(resultSetLista.getDouble("Descuento"));
                    suministroTB.setDescuentoCalculado(resultSetLista.getDouble("Descuento"));
                    suministroTB.setDescuentoSumado(resultSetLista.getDouble("Descuento"));

                    suministroTB.setPrecioVentaGeneralUnico(resultSetLista.getDouble("PrecioVenta"));
                    suministroTB.setPrecioVentaGeneralReal(resultSetLista.getDouble("PrecioVenta"));
                    suministroTB.setPrecioVentaGeneral(resultSetLista.getDouble("PrecioVenta"));
                    suministroTB.setPrecioVentaGeneralAuxiliar(resultSetLista.getDouble("PrecioVenta"));

                    suministroTB.setInventario(resultSetLista.getBoolean("Inventario"));
                    suministroTB.setUnidadVenta(resultSetLista.getInt("UnidadVenta"));
                    suministroTB.setValorInventario(resultSetLista.getShort("ValorInventario"));

                    empList.add(suministroTB);
                }
                ventaTB.setSuministroTBs(empList);

                return ventaTB;
            } else {
                throw new Exception("No se puedo obtener los datos, intente nuevamente.");
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
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementVentaDetalle != null) {
                    statementVentaDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static ModeloObject RegistrarAbono(VentaCreditoTB ventaCreditoTB, IngresoTB ingresoTB,
            MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();

        ModeloObject result = new ModeloObject();
        PreparedStatement statementValidate = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementAbono = null;
        PreparedStatement statementIngreso = null;
        CallableStatement statementCodigo = null;
        PreparedStatement statementMovimientoCaja = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaTB WHERE Estado = 3 AND IdVenta = ?");
            statementValidate.setString(1, ventaCreditoTB.getIdVenta());
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();

                result.setId((short) 2);
                result.setMessage("No se pueden realizar cobrar a una venta anulada.");
                result.setState("error");
                return result;
            } else {

                statementValidate.close();
                statementValidate = dbf.getConnection().prepareStatement("SELECT "
                        + "sum(dv.Cantidad*(dv.PrecioVenta - dv.Descuento)) as Total from VentaTB as v "
                        + "inner join DetalleVentaTB as dv on dv.IdVenta = v.IdVenta "
                        + "where v.IdVenta = ?");
                statementValidate.setString(1, ventaCreditoTB.getIdVenta());
                ResultSet resultSet = statementValidate.executeQuery();
                if (resultSet.next()) {
                    double total = Double.parseDouble(Tools.roundingValue(resultSet.getDouble("Total"), 2));

                    statementCodigo = dbf.getConnection()
                            .prepareCall("{? = call Fc_Venta_Credito_Codigo_Alfanumerico()}");
                    statementCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                    statementCodigo.execute();
                    String idVentaCredito = statementCodigo.getString(1);

                    statementAbono = dbf.getConnection().prepareStatement(
                            "INSERT INTO VentaCreditoTB(IdVenta,IdVentaCredito,Monto,FechaPago,HoraPago,Estado,IdUsuario,Observacion)VALUES(?,?,?,?,?,?,?,?)");
                    statementAbono.setString(1, ventaCreditoTB.getIdVenta());
                    statementAbono.setString(2, idVentaCredito);
                    statementAbono.setDouble(3, ventaCreditoTB.getMonto());
                    statementAbono.setString(4, ventaCreditoTB.getFechaPago());
                    statementAbono.setString(5, ventaCreditoTB.getHoraPago());
                    statementAbono.setShort(6, ventaCreditoTB.getEstado());
                    statementAbono.setString(7, ventaCreditoTB.getIdUsuario());
                    statementAbono.setString(8, ventaCreditoTB.getObservacion());
                    statementAbono.addBatch();

                    statementIngreso = dbf.getConnection().prepareStatement(
                            "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?)");
                    if (ingresoTB != null) {
                        statementIngreso.setString(1, idVentaCredito);
                        statementIngreso.setString(2, ingresoTB.getIdUsuario());
                        statementIngreso.setString(3, ingresoTB.getDetalle());
                        statementIngreso.setInt(4, ingresoTB.getProcedencia());
                        statementIngreso.setString(5, ingresoTB.getFecha());
                        statementIngreso.setString(6, ingresoTB.getHora());
                        statementIngreso.setInt(7, ingresoTB.getForma());
                        statementIngreso.setDouble(8, ingresoTB.getMonto());
                        statementIngreso.addBatch();
                    }

                    statementMovimientoCaja = dbf.getConnection().prepareStatement(
                            "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");
                    if (movimientoCajaTB != null) {
                        statementMovimientoCaja.setString(1, movimientoCajaTB.getIdCaja());
                        statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
                        statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
                        statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario());
                        statementMovimientoCaja.setInt(5, movimientoCajaTB.getTipoMovimiento());
                        statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
                        statementMovimientoCaja.setString(7, idVentaCredito);
                        statementMovimientoCaja.addBatch();
                    }

                    statementValidate = dbf.getConnection()
                            .prepareStatement("SELECT Monto FROM VentaCreditoTB WHERE IdVenta = ?");
                    statementValidate.setString(1, ventaCreditoTB.getIdVenta());
                    resultSet = statementValidate.executeQuery();

                    double montoTotal = 0;
                    while (resultSet.next()) {
                        montoTotal += resultSet.getDouble("Monto");
                    }

                    statementVenta = dbf.getConnection()
                            .prepareStatement("UPDATE VentaTB SET Estado = 1 WHERE IdVenta = ?");
                    if ((montoTotal + ventaCreditoTB.getMonto()) >= total) {
                        statementVenta.setString(1, ventaCreditoTB.getIdVenta());
                        statementVenta.addBatch();
                    }

                    statementAbono.executeBatch();
                    statementIngreso.executeBatch();
                    statementVenta.executeBatch();
                    statementMovimientoCaja.executeBatch();
                    dbf.getConnection().commit();

                    result.setId((short) 1);
                    result.setIdResult(idVentaCredito);
                    result.setMessage("Se completo correctamente el proceso.");
                    result.setState("inserted");
                    return result;
                } else {
                    dbf.getConnection().rollback();
                    result.setId((short) 2);
                    result.setMessage(
                            "Problemas al encontrar le venta con el id indicado " + ventaCreditoTB.getIdVenta());
                    result.setState("error");
                    return result;
                }
            }
        } catch (SQLException | NullPointerException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result.setId((short) 2);
            result.setMessage(ex.getLocalizedMessage());
            result.setState("error");
            return result;
        } finally {
            try {
                if (statementAbono != null) {
                    statementAbono.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                if (statementCodigo != null) {
                    statementCodigo.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static String RemoverIngreso(String idVenta, String idVentaCredito) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementVentaCredito = null;
        PreparedStatement statementMovientoCaja = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementVenta = null;
        ResultSet resultSet = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT TipoCredito FROM VentaTB WHERE IdVenta = ?");
            statementValidate.setString(1, idVenta);
            resultSet = statementValidate.executeQuery();
            resultSet.next();

            statementIngreso = dbf.getConnection().prepareStatement("DELETE FROM IngresoTB WHERE IdProcedencia = ?");
            statementIngreso.setString(1, idVentaCredito);
            statementIngreso.addBatch();

            statementMovientoCaja = dbf.getConnection()
                    .prepareStatement("DELETE FROM MovimientoCajaTB WHERE IdProcedencia = ? ");
            statementMovientoCaja.setString(1, idVentaCredito);
            statementMovientoCaja.addBatch();

            if (resultSet.getInt("TipoCredito") == 0) {
                statementVentaCredito = dbf.getConnection()
                        .prepareStatement("DELETE FROM VentaCreditoTB WHERE IdVentaCredito = ? AND IdVenta = ?");
                statementVentaCredito.setString(1, idVentaCredito);
                statementVentaCredito.setString(2, idVenta);
                statementVentaCredito.addBatch();
            } else {
                statementVentaCredito = dbf.getConnection().prepareStatement(
                        "UPDATE VentaCreditoTB SET Estado = 0 WHERE IdVentaCredito = ? AND IdVenta = ?");
                statementVentaCredito.setString(1, idVentaCredito);
                statementVentaCredito.setString(2, idVenta);
                statementVentaCredito.addBatch();
            }

            statementVenta = dbf.getConnection().prepareStatement("UPDATE VentaTB SET Estado = 2 WHERE IdVenta = ?");
            statementVenta.setString(1, idVenta);
            statementVenta.addBatch();

            statementIngreso.executeBatch();
            statementMovientoCaja.executeBatch();
            statementVentaCredito.executeBatch();
            statementVenta.executeBatch();
            dbf.getConnection().commit();
            return "removed";
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
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementVentaCredito != null) {
                    statementVentaCredito.close();
                }
                if (statementMovientoCaja != null) {
                    statementMovientoCaja.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
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

    public static ModeloObject RegistrarAbonoUpdateById(VentaCreditoTB ventaCreditoTB, IngresoTB ingresoTB,
            MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();

        ModeloObject result = new ModeloObject();
        PreparedStatement statementValidate = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementAbono = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementMovimientoCaja = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidate = dbf.getConnection().prepareStatement("SELECT "
                    + "sum(dv.Cantidad*(dv.PrecioVenta - dv.Descuento)) as Total from VentaTB as v "
                    + "inner join DetalleVentaTB as dv on dv.IdVenta = v.IdVenta "
                    + "where v.IdVenta = ?");
            statementValidate.setString(1, ventaCreditoTB.getIdVenta());
            ResultSet resultSet = statementValidate.executeQuery();
            if (resultSet.next()) {
                double total = Double.parseDouble(Tools.roundingValue(resultSet.getDouble("Total"), 2));

                statementAbono = dbf.getConnection().prepareStatement(
                        "UPDATE VentaCreditoTB SET Estado = ?,Observacion=? WHERE IdVentaCredito = ?");
                statementAbono.setInt(1, ventaCreditoTB.getEstado());
                statementAbono.setString(2, ventaCreditoTB.getObservacion());
                statementAbono.setString(3, ventaCreditoTB.getIdVentaCredito());
                statementAbono.addBatch();

                statementIngreso = dbf.getConnection().prepareStatement("INSERT INTO IngresoTB(\n"
                        + "IdProcedencia,\n"
                        + "IdUsuario,\n"
                        + "Detalle,\n"
                        + "Procedencia,\n"
                        + "Fecha,\n"
                        + "Hora,\n"
                        + "Forma,\n"
                        + "Monto)\n"
                        + "VALUES(?,?,?,?,?,?,?,?)");
                if (ingresoTB != null) {
                    statementIngreso.setString(1, ventaCreditoTB.getIdVentaCredito());
                    statementIngreso.setString(2, ingresoTB.getIdUsuario());
                    statementIngreso.setString(3, ingresoTB.getDetalle());
                    statementIngreso.setInt(4, ingresoTB.getProcedencia());
                    statementIngreso.setString(5, ingresoTB.getFecha());
                    statementIngreso.setString(6, ingresoTB.getHora());
                    statementIngreso.setInt(7, ingresoTB.getForma());
                    statementIngreso.setDouble(8, ingresoTB.getMonto());
                    statementIngreso.addBatch();
                }

                statementMovimientoCaja = dbf.getConnection().prepareStatement("INSERT INTO MovimientoCajaTB(\n"
                        + "IdCaja,\n"
                        + "FechaMovimiento,\n"
                        + "HoraMovimiento,\n"
                        + "Comentario,\n"
                        + "TipoMovimiento,\n"
                        + "Monto,\n"
                        + "IdProcedencia)\n"
                        + "VALUES(?,?,?,?,?,?,?)");
                if (movimientoCajaTB != null) {
                    statementMovimientoCaja.setString(1, movimientoCajaTB.getIdCaja());
                    statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
                    statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
                    statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario());
                    statementMovimientoCaja.setInt(5, movimientoCajaTB.getTipoMovimiento());
                    statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
                    statementMovimientoCaja.setString(7, ventaCreditoTB.getIdVentaCredito());
                    statementMovimientoCaja.addBatch();
                }

                statementValidate.close();
                statementValidate = dbf.getConnection()
                        .prepareStatement("SELECT Monto FROM VentaCreditoTB WHERE IdVenta = ? AND Estado = 1");
                statementValidate.setString(1, ventaCreditoTB.getIdVenta());
                resultSet = statementValidate.executeQuery();

                double montoTotal = 0;
                while (resultSet.next()) {
                    montoTotal += resultSet.getDouble("Monto");
                }

                statementVenta = dbf.getConnection()
                        .prepareStatement("UPDATE VentaTB SET Estado = 1 WHERE IdVenta = ?");
                if ((montoTotal + ventaCreditoTB.getMonto()) >= total) {
                    statementVenta.setString(1, ventaCreditoTB.getIdVenta());
                    statementVenta.addBatch();
                }

                statementAbono.executeBatch();
                statementIngreso.executeBatch();
                statementVenta.executeBatch();
                statementMovimientoCaja.executeBatch();
                dbf.getConnection().commit();

                result.setId((short) 1);
                result.setIdResult(ventaCreditoTB.getIdVentaCredito());
                result.setMessage("Se completo correctamente el proceso.");
                result.setState("inserted");
                return result;
            } else {
                dbf.getConnection().rollback();
                result.setId((short) 2);
                result.setMessage("Problemas al encontrar le venta con el id indicado " + ventaCreditoTB.getIdVenta());
                result.setState("error");
                return result;
            }
        } catch (SQLException | NullPointerException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            result.setId((short) 2);
            result.setMessage(ex.getLocalizedMessage());
            result.setState("error");
            return result;
        } finally {
            try {
                if (statementAbono != null) {
                    statementAbono.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
            }
        }
    }

    public static String Update_Producto_Para_Llevar(String idVenta, String idSuministro, String comprobante,
            double cantidad, double costo, String observacion, boolean completo) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementVenta = null;
        PreparedStatement statementValidar = null;
        PreparedStatement statementHistorial = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementActualizar = null;
        PreparedStatement statementKardex = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementSuministro = dbf.getConnection()
                    .prepareStatement("SELECT * FROM DetalleVentaTB WHERE IdVenta = ? AND IdArticulo = ?");
            statementSuministro.setString(1, idVenta);
            statementSuministro.setString(2, idSuministro);
            ResultSet resultSet = statementSuministro.executeQuery();
            if (resultSet.next()) {
                double cantidadTotal = resultSet.getDouble("Cantidad") + resultSet.getDouble("Bonificacion");
                if (completo) {
                    statementHistorial = dbf.getConnection().prepareStatement(
                            "SELECT * FROM HistorialSuministroLlevar WHERE IdVenta = ? AND IdSuministro = ?");
                    statementHistorial.setString(1, idVenta);
                    statementHistorial.setString(2, idSuministro);
                    if (statementHistorial.executeQuery().next()) {
                        dbf.getConnection().rollback();
                        return "historial";
                    } else {
                        statementSuministro.close();
                        statementSuministro = dbf.getConnection().prepareStatement(
                                "UPDATE DetalleVentaTB SET PorLlevar = Cantidad+Bonificacion, Estado = 'C' WHERE IdVenta = ? AND IdArticulo = ?");
                        statementSuministro.setString(1, idVenta);
                        statementSuministro.setString(2, idSuministro);
                        statementSuministro.addBatch();

                        statementActualizar = dbf.getConnection().prepareStatement(
                                "UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");
                        statementActualizar.setDouble(1, cantidadTotal);
                        statementActualizar.setString(2, idSuministro);
                        statementActualizar.addBatch();

                        statementKardex = dbf.getConnection().prepareStatement("INSERT INTO "
                                + "KardexSuministroTB("
                                + "IdSuministro,"
                                + "Fecha,"
                                + "Hora,"
                                + "Tipo,"
                                + "Movimiento,"
                                + "Detalle,"
                                + "Cantidad,"
                                + "Costo, "
                                + "Total,"
                                + "IdAlmacen) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?)");
                        statementKardex.setString(1, idSuministro);
                        statementKardex.setString(2, Tools.getDate());
                        statementKardex.setString(3, Tools.getTime());
                        statementKardex.setShort(4, (short) 2);
                        statementKardex.setInt(5, 1);
                        statementKardex.setString(6, "SALIDA DEL PRODUCTO DE LA VENTA " + comprobante);
                        statementKardex.setDouble(7, cantidadTotal);
                        statementKardex.setDouble(8, costo);
                        statementKardex.setDouble(9, cantidadTotal * costo);
                        statementKardex.setInt(10, 0);
                        statementKardex.addBatch();

                        statementHistorial.close();
                        statementHistorial = dbf.getConnection().prepareStatement(
                                "INSERT INTO HistorialSuministroLlevar(IdVenta,IdSuministro, Fecha, Hora, Cantidad, Observacion)VALUES(?,?,?,?,?,?)");
                        statementHistorial.setString(1, idVenta);
                        statementHistorial.setString(2, idSuministro);
                        statementHistorial.setString(3, Tools.getDate());
                        statementHistorial.setString(4, Tools.getTime());
                        statementHistorial.setDouble(5, cantidadTotal);
                        statementHistorial.setString(6, observacion);
                        statementHistorial.addBatch();

                        statementValidar = dbf.getConnection().prepareStatement(
                                "SELECT Estado FROM DetalleVentaTB WHERE IdVenta = ? AND IdArticulo <> ?");
                        statementValidar.setString(1, idVenta);
                        statementValidar.setString(2, idSuministro);
                        resultSet = statementValidar.executeQuery();

                        int allenviado = 0;
                        while (resultSet.next()) {
                            if (resultSet.getString("Estado").equalsIgnoreCase("L")) {
                                allenviado++;
                            }
                        }

                        statementVenta = dbf.getConnection()
                                .prepareStatement("UPDATE VentaTB SET Estado = 1 WHERE IdVenta = ?");
                        if (allenviado == 0) {
                            statementVenta.setString(1, idVenta);
                            statementVenta.addBatch();
                        }

                        statementSuministro.executeBatch();
                        statementActualizar.executeBatch();
                        statementKardex.executeBatch();
                        statementHistorial.executeBatch();
                        statementVenta.executeBatch();
                        dbf.getConnection().commit();
                        return "update";
                    }
                } else {

                    resultSet.close();
                    statementHistorial = dbf.getConnection().prepareStatement(
                            "SELECT Cantidad FROM HistorialSuministroLlevar WHERE IdVenta = ? AND IdSuministro = ?");
                    statementHistorial.setString(1, idVenta);
                    statementHistorial.setString(2, idSuministro);

                    resultSet = statementHistorial.executeQuery();
                    double cantidadActual = 0;
                    while (resultSet.next()) {
                        cantidadActual += resultSet.getDouble("Cantidad");
                    }

                    statementVenta = dbf.getConnection()
                            .prepareStatement("UPDATE VentaTB SET Estado = 1 WHERE IdVenta = ?");
                    statementValidar = dbf.getConnection().prepareStatement(
                            "SELECT Estado FROM DetalleVentaTB WHERE IdVenta = ? AND IdArticulo <> ?");

                    if ((cantidad + cantidadActual) >= cantidadTotal) {
                        statementSuministro.close();
                        statementSuministro = dbf.getConnection().prepareStatement(
                                "UPDATE DetalleVentaTB SET PorLlevar = PorLlevar + ?, Estado = 'C' WHERE IdVenta = ? AND IdArticulo = ?");
                        statementSuministro.setDouble(1, cantidad);
                        statementSuministro.setString(2, idVenta);
                        statementSuministro.setString(3, idSuministro);
                        statementSuministro.addBatch();

                        statementValidar.setString(1, idVenta);
                        statementValidar.setString(2, idSuministro);
                        resultSet = statementValidar.executeQuery();

                        int allenviado = 0;
                        while (resultSet.next()) {
                            if (resultSet.getString("Estado").equalsIgnoreCase("L")) {
                                allenviado++;
                            }
                        }

                        if (allenviado == 0) {
                            statementVenta.setString(1, idVenta);
                            statementVenta.addBatch();
                        }
                    } else {
                        statementSuministro.close();
                        statementSuministro = dbf.getConnection().prepareStatement(
                                "UPDATE DetalleVentaTB SET PorLlevar = PorLlevar + ? WHERE IdVenta = ? AND IdArticulo = ?");
                        statementSuministro.setDouble(1, cantidad);
                        statementSuministro.setString(2, idVenta);
                        statementSuministro.setString(3, idSuministro);
                        statementSuministro.addBatch();
                    }

                    statementActualizar = dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");
                    statementActualizar.setDouble(1, cantidad);
                    statementActualizar.setString(2, idSuministro);
                    statementActualizar.addBatch();

                    statementKardex = dbf.getConnection().prepareStatement("INSERT INTO "
                            + "KardexSuministroTB("
                            + "IdSuministro,"
                            + "Fecha,"
                            + "Hora,"
                            + "Tipo,"
                            + "Movimiento,"
                            + "Detalle,"
                            + "Cantidad,"
                            + "Costo, "
                            + "Total,"
                            + "IdAlmacen) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?)");
                    statementKardex.setString(1, idSuministro);
                    statementKardex.setString(2, Tools.getDate());
                    statementKardex.setString(3, Tools.getTime());
                    statementKardex.setShort(4, (short) 2);
                    statementKardex.setInt(5, 1);
                    statementKardex.setString(6, "SALIDA DEL PRODUCTO DE LA VENTA " + comprobante);
                    statementKardex.setDouble(7, cantidad);
                    statementKardex.setDouble(8, costo);
                    statementKardex.setDouble(9, cantidad * costo);
                    statementKardex.setInt(10, 0);
                    statementKardex.addBatch();

                    statementHistorial.close();
                    statementHistorial = dbf.getConnection().prepareStatement(
                            "INSERT INTO HistorialSuministroLlevar(IdVenta,IdSuministro, Fecha, Hora, Cantidad, Observacion)VALUES(?,?,?,?,?,?)");
                    statementHistorial.setString(1, idVenta);
                    statementHistorial.setString(2, idSuministro);
                    statementHistorial.setString(3, Tools.getDate());
                    statementHistorial.setString(4, Tools.getTime());
                    statementHistorial.setDouble(5, cantidad);
                    statementHistorial.setString(6, observacion);
                    statementHistorial.addBatch();

                    statementSuministro.executeBatch();
                    statementActualizar.executeBatch();
                    statementKardex.executeBatch();
                    statementHistorial.executeBatch();
                    statementVenta.executeBatch();
                    dbf.getConnection().commit();
                    return "update";
                }

            } else {
                dbf.getConnection().rollback();
                return "noproduct";
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementActualizar != null) {
                    statementActualizar.close();
                }
                if (statementValidar != null) {
                    statementValidar.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
                if (statementHistorial != null) {
                    statementHistorial.close();
                }
                if (statementKardex != null) {
                    statementKardex.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static Object Listar_Historial_Suministro_Llevar(String idVenta, String idSuministro) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementVenta = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[3];
            statementVenta = dbf.getConnection().prepareStatement("SELECT "
                    + "v.Serie,"
                    + "v.Numeracion,"
                    + "v.FechaVenta,"
                    + "v.HoraVenta,"
                    + "c.NumeroDocumento,"
                    + "c.Informacion,"
                    + "c.Telefono,"
                    + "c.Celular,"
                    + "c.Email,"
                    + "c.Direccion "
                    + "FROM VentaTB AS v "
                    + "INNER JOIN ClienteTB AS c ON c.IdCliente = v.Cliente "
                    + "WHERE IdVenta = ?");
            statementVenta.setString(1, idVenta);

            resultSet = statementVenta.executeQuery();
            if (resultSet.next()) {
                VentaTB ventaTB = new VentaTB();
                ventaTB.setSerie(resultSet.getString("Serie"));
                ventaTB.setNumeracion(resultSet.getString("Numeracion"));
                ventaTB.setFechaVenta(resultSet.getDate("FechaVenta").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ventaTB.setHoraVenta(
                        resultSet.getTime("HoraVenta").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                clienteTB.setInformacion(resultSet.getString("Informacion"));
                clienteTB.setTelefono(resultSet.getString("Telefono"));
                clienteTB.setCelular(resultSet.getString("Celular"));
                clienteTB.setEmail(resultSet.getString("Email"));
                clienteTB.setDireccion(resultSet.getString("Direccion"));
                ventaTB.setClienteTB(clienteTB);

                resultSet.close();
                statementVenta.close();
                statementVenta = dbf.getConnection().prepareStatement(
                        "SELECT s.Clave,s.NombreMarca,d.Cantidad,d.Bonificacion FROM DetalleVentaTB AS d INNER JOIN SuministroTB AS s ON s.IdSuministro = d.IdArticulo WHERE d.IdVenta = ? AND d.IdArticulo = ?");
                statementVenta.setString(1, idVenta);
                statementVenta.setString(2, idSuministro);

                resultSet = statementVenta.executeQuery();
                resultSet.next();

                DetalleVentaTB detalleVentaTB = new DetalleVentaTB();
                detalleVentaTB.setCantidad(resultSet.getDouble("Cantidad") + resultSet.getDouble("Bonificacion"));
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setClave(resultSet.getString("Clave"));
                suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                detalleVentaTB.setSuministroTB(suministroTB);

                statementVenta = dbf.getConnection().prepareStatement("SELECT "
                        + "Fecha,"
                        + "Hora,"
                        + "Cantidad,"
                        + "Observacion "
                        + "FROM HistorialSuministroLlevar "
                        + "WHERE IdVenta = ? AND IdSuministro = ? ");
                statementVenta.setString(1, idVenta);
                statementVenta.setString(2, idSuministro);
                ArrayList<HistorialSuministroSalidaTB> suministroSalidas = new ArrayList<HistorialSuministroSalidaTB>();
                resultSet = statementVenta.executeQuery();
                while (resultSet.next()) {
                    HistorialSuministroSalidaTB suministroSalida = new HistorialSuministroSalidaTB();
                    suministroSalida.setId(resultSet.getRow());
                    suministroSalida.setFecha(
                            resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    suministroSalida.setHora(
                            resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                    suministroSalida.setCantidad(resultSet.getDouble("Cantidad"));
                    suministroSalida.setObservacion(resultSet.getString("Observacion"));
                    suministroSalidas.add(suministroSalida);
                }

                objects[0] = ventaTB;
                objects[1] = detalleVentaTB;
                objects[2] = suministroSalidas;

                return objects;
            } else {
                throw new Exception("No se pudo obtener los datos, intente nuevamente.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementVenta != null) {
                    statementVenta.close();
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

    public static Object ListarNotificaciones() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementFactura = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            JSONArray array = new JSONArray();

            statementFactura = dbf.getConnection().prepareStatement("SELECT \n"
                    + "v.Serie,\n"
                    + "td.Nombre,\n"
                    + "case v.Estado \n"
                    + "when 3 \n"
                    + "then 'Dar de Baja'\n"
                    + "else 'Por Declarar' end as Estado,\n"
                    + "count(v.Serie) AS Cantidad\n"
                    + "FROM VentaTB AS v \n"
                    + "INNER JOIN TipoDocumentoTB AS td ON td.IdTipoDocumento = v.Comprobante            \n"
                    + "WHERE\n"
                    + "td.Facturacion = 1 AND ISNULL(v.Xmlsunat,'') <> '0' AND ISNULL(v.Xmlsunat,'') <> '1032'\n"
                    + "OR\n"
                    + "td.Facturacion = 1 AND ISNULL(v.Xmlsunat,'') = '0' AND v.Estado = 3\n"
                    + "GROUP BY v.Serie,td.Nombre,v.Estado");
            resultSet = statementFactura.executeQuery();
            while (resultSet.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("Nombre", resultSet.getString("Nombre"));
                jsono.put("Estado", resultSet.getString("Estado"));
                jsono.put("Cantidad", resultSet.getInt("Cantidad"));
                array.add(jsono);
            }

            resultSet.close();
            statementFactura.close();
            statementFactura = dbf.getConnection().prepareStatement("SELECT \n"
                    + "nc.Serie,\n"
                    + "td.Nombre,\n"
                    + "case nc.Estado \n"
                    + "when 3\n"
                    + "then 'Dar de Baja'\n"
                    + "else 'Por Declarar' end as Estado,\n"
                    + "count(nc.Serie) AS Cantidad\n"
                    + "FROM NotaCreditoTB AS nc\n"
                    + "INNER JOIN TipoDocumentoTB AS td ON td.IdTipoDocumento = nc.Comprobante            \n"
                    + "WHERE\n"
                    + "td.Facturacion = 1 AND ISNULL(nc.Xmlsunat,'') <> '0' AND ISNULL(nc.Xmlsunat,'') <> '1032'\n"
                    + "GROUP BY nc.Serie,td.Nombre,nc.Estado");
            resultSet = statementFactura.executeQuery();
            while (resultSet.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("Nombre", resultSet.getString("Nombre"));
                jsono.put("Estado", resultSet.getString("Estado"));
                jsono.put("Cantidad", resultSet.getInt("Cantidad"));
                array.add(jsono);
            }
            return array;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementFactura != null) {
                    statementFactura.close();
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
