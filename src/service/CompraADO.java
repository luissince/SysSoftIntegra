package service;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import model.AlmacenTB;
import model.BancoTB;
import model.CompraCreditoTB;
import model.CompraTB;
import model.DetalleCompraTB;
import model.EgresoTB;
import model.ImpuestoTB;
import model.LoteTB;
import model.ModeloObject;
import model.MonedaTB;
import model.MovimientoCajaTB;
import model.PreciosTB;
import model.ProveedorTB;
import model.SuministroTB;

public class CompraADO {

    public static String registrarCompra(CompraTB compraTB, List<EgresoTB> egresoTBs) {
        DBUtil dbf = new DBUtil();

        CallableStatement callableObtenerCodigoCompra = null;
        CallableStatement callableObtenerCodigoEgreso = null;
        PreparedStatement prepareRegistrarCompra = null;
        PreparedStatement prepareRegistrarEgreso = null;
        PreparedStatement prepareRegistrarDetalleCompra = null;
        PreparedStatement prepareActualizarCosto = null;
        PreparedStatement prepareActualizarCantidad = null;
        PreparedStatement prepareActualizarCantidadAlmacen = null;
        PreparedStatement prepareRegistrarKardex = null;
        PreparedStatement prepareActualizarSuministroPrecio = null;
        PreparedStatement prepareRegistrarPrecio = null;
        PreparedStatement prepareBorrarPrecio = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            callableObtenerCodigoCompra = dbf.getConnection().prepareCall("{? = call Fc_Compra_Codigo_Alfanumerico()}");
            callableObtenerCodigoCompra.registerOutParameter(1, java.sql.Types.VARCHAR);
            callableObtenerCodigoCompra.execute();
            String idCompra = callableObtenerCodigoCompra.getString(1);

            callableObtenerCodigoEgreso = dbf.getConnection().prepareCall("{? = call Fc_Egreso_Codigo_Alfanumerico()}");
            callableObtenerCodigoEgreso.registerOutParameter(1, java.sql.Types.VARCHAR);
            callableObtenerCodigoEgreso.execute();
            String idEgreso = callableObtenerCodigoEgreso.getString(1);

            prepareRegistrarCompra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "CompraTB("
                    + "IdCompra,"
                    + "Proveedor,"
                    + "Comprobante,"
                    + "Serie,"
                    + "Numeracion,"
                    + "TipoMoneda,"
                    + "FechaCompra,"
                    + "HoraCompra,"
                    + "FechaVencimiento,"
                    + "HoraVencimiento,"
                    + "Observaciones,"
                    + "Notas,"
                    + "TipoCompra,"
                    + "Efectivo,"
                    + "Tarjeta,"
                    + "Deposito,"
                    + "EstadoCompra,"
                    + "Usuario,"
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            prepareRegistrarCompra.setString(1, idCompra);
            prepareRegistrarCompra.setString(2, compraTB.getIdProveedor());
            prepareRegistrarCompra.setInt(3, compraTB.getIdComprobante());
            prepareRegistrarCompra.setString(4, compraTB.getSerie());
            prepareRegistrarCompra.setString(5, compraTB.getNumeracion());
            prepareRegistrarCompra.setInt(6, compraTB.getIdMoneda());
            prepareRegistrarCompra.setString(7, compraTB.getFechaCompra());
            prepareRegistrarCompra.setString(8, compraTB.getHoraCompra());
            prepareRegistrarCompra.setString(9, compraTB.getFechaVencimiento());
            prepareRegistrarCompra.setString(10, compraTB.getHoraVencimiento());
            prepareRegistrarCompra.setString(11, compraTB.getObservaciones());
            prepareRegistrarCompra.setString(12, compraTB.getNotas());
            prepareRegistrarCompra.setInt(13, compraTB.getTipo());
            prepareRegistrarCompra.setBoolean(14, compraTB.getEfectivo());
            prepareRegistrarCompra.setBoolean(15, compraTB.getTarjeta());
            prepareRegistrarCompra.setBoolean(16, compraTB.getDeposito());
            prepareRegistrarCompra.setInt(17, compraTB.getEstado());
            prepareRegistrarCompra.setString(18, compraTB.getUsuario());
            prepareRegistrarCompra.setInt(19, compraTB.getIdAlmacen());
            prepareRegistrarCompra.addBatch();

            prepareRegistrarEgreso = dbf.getConnection().prepareStatement("INSERT INTO EgresoTB\n"
                    + "(IdEgreso,\n"
                    + "IdProcedencia,\n"
                    + "IdUsuario,\n"
                    + "IdCliente,\n"
                    + "IdBanco,\n"
                    + "Detalle,\n"
                    + "Procedencia,\n"
                    + "Fecha,\n"
                    + "Hora,\n"
                    + "Forma,\n"
                    + "Monto,\n"
                    + "Vuelto,\n"
                    + "Operacion)\n"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");

            for (EgresoTB egresoTB : egresoTBs) {
                prepareRegistrarEgreso.setString(1, idEgreso);
                prepareRegistrarEgreso.setString(2, idCompra);
                prepareRegistrarEgreso.setString(3, egresoTB.getIdUsuario());
                prepareRegistrarEgreso.setString(4, egresoTB.getIdCliente());
                prepareRegistrarEgreso.setString(5, egresoTB.getIdBanco());
                prepareRegistrarEgreso.setString(6, egresoTB.getDetalle());
                prepareRegistrarEgreso.setInt(7, egresoTB.getProcedencia());
                prepareRegistrarEgreso.setString(8, egresoTB.getFecha());
                prepareRegistrarEgreso.setString(9, egresoTB.getHora());
                prepareRegistrarEgreso.setInt(10, egresoTB.getForma());
                prepareRegistrarEgreso.setDouble(11, egresoTB.getMonto());
                prepareRegistrarEgreso.setDouble(12, egresoTB.getVuelto());
                prepareRegistrarEgreso.setString(13, egresoTB.getOperacion());
                prepareRegistrarEgreso.addBatch();

                // Obtener el número de ingreso actual
                String prefijo = "EG";
                String numeroIngreso = idEgreso.replace(prefijo, "");
                int incremental = Integer.parseInt(numeroIngreso) + 1;

                // Generar el nuevo ID de ingreso con ceros delante
                idEgreso = prefijo + String.format("%04d", incremental);
            }

            prepareRegistrarDetalleCompra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "DetalleCompraTB("
                    + "IdCompra,"
                    + "IdArticulo,"
                    + "Cantidad,"
                    + "PrecioCompra,"
                    + "Descuento,"
                    + "IdImpuesto,"
                    + "NombreImpuesto,"
                    + "ValorImpuesto,"
                    + "Importe,"
                    + "Descripcion)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            prepareActualizarCosto = dbf.getConnection().prepareStatement("UPDATE SuministroTB SET "
                    + "PrecioCompra = ? "
                    + "WHERE IdSuministro = ?");

            prepareActualizarCantidad = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET "
                            + "Cantidad = Cantidad + ? "
                            + "WHERE IdSuministro = ?");

            prepareActualizarCantidadAlmacen = dbf.getConnection().prepareStatement("UPDATE CantidadTB SET "
                    + "Cantidad = Cantidad + ? "
                    + "WHERE IdAlmacen = ? AND IdSuministro = ?");

            prepareRegistrarKardex = dbf.getConnection().prepareStatement("INSERT INTO "
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

            prepareActualizarSuministroPrecio = dbf.getConnection().prepareStatement(
                    "UPDATE SuministroTB SET "
                    + "Impuesto=?, "
                    + "PrecioVentaGeneral=?, "
                    + "TipoPrecio=? "
                    + "WHERE IdSuministro = ?");

            prepareRegistrarPrecio = dbf.getConnection().prepareStatement(
                    "INSERT INTO PreciosTB("
                    + "IdArticulo, "
                    + "IdSuministro, "
                    + "Nombre, "
                    + "Valor, "
                    + "Factor,"
                    + "Estado) "
                    + "VALUES(?,?,?,?,?,?)");

            prepareBorrarPrecio = dbf.getConnection()
                    .prepareStatement("DELETE FROM PreciosTB WHERE IdSuministro = ?");

            for (DetalleCompraTB dc : compraTB.getDetalleCompraTBs()) {
                prepareRegistrarDetalleCompra.setString(1, idCompra);
                prepareRegistrarDetalleCompra.setString(2, dc.getIdSuministro());
                prepareRegistrarDetalleCompra.setDouble(3, dc.getCantidad());
                prepareRegistrarDetalleCompra.setDouble(4, dc.getPrecioCompra());
                prepareRegistrarDetalleCompra.setDouble(5, dc.getDescuento());

                prepareRegistrarDetalleCompra.setInt(6, dc.getIdImpuesto());
                prepareRegistrarDetalleCompra.setString(7, dc.getImpuestoTB().getNombre());
                prepareRegistrarDetalleCompra.setDouble(8, dc.getImpuestoTB().getValor());
                prepareRegistrarDetalleCompra.setDouble(9, dc.getPrecioCompra() * dc.getCantidad());
                prepareRegistrarDetalleCompra.setString(10, dc.getDescripcion());
                prepareRegistrarDetalleCompra.addBatch();

                if (compraTB.isActualizarAlmacen()) {
                    prepareActualizarCosto.setDouble(1, dc.getPrecioCompra());
                    prepareActualizarCosto.setString(2, dc.getIdSuministro());
                    prepareActualizarCosto.addBatch();
                }
                // ---------------------------------------------------------------------
                if (compraTB.getIdAlmacen() == 0) {
                    prepareActualizarCantidad.setDouble(1, dc.getCantidad());
                    prepareActualizarCantidad.setString(2, dc.getIdSuministro());
                    prepareActualizarCantidad.addBatch();
                } else {
                    prepareActualizarCantidadAlmacen.setDouble(1, dc.getCantidad());
                    prepareActualizarCantidadAlmacen.setInt(2, compraTB.getIdAlmacen());
                    prepareActualizarCantidadAlmacen.setString(3, dc.getIdSuministro());
                    prepareActualizarCantidadAlmacen.addBatch();
                }

                String detalleKardex = "COMPRA CON SERIE Y NUMERACIÓN: " + compraTB.getSerie().toUpperCase() + "-"
                        + compraTB.getNumeracion().toUpperCase();

                prepareRegistrarKardex.setString(1, dc.getIdSuministro());
                prepareRegistrarKardex.setString(2, Tools.getDate());
                prepareRegistrarKardex.setString(3, Tools.getTime());
                prepareRegistrarKardex.setShort(4, (short) 1);
                prepareRegistrarKardex.setInt(5, 2);
                prepareRegistrarKardex.setString(6, detalleKardex);
                prepareRegistrarKardex.setDouble(7, dc.getCantidad());
                prepareRegistrarKardex.setDouble(8, dc.getPrecioCompra());
                prepareRegistrarKardex.setDouble(9, dc.getCantidad() * dc.getPrecioCompra());
                prepareRegistrarKardex.setInt(10, compraTB.getIdAlmacen());
                prepareRegistrarKardex.addBatch();

                if (!dc.isCambiarPrecio()) {
                    prepareActualizarSuministroPrecio.setInt(1, dc.getSuministroTB().getIdImpuesto());
                    prepareActualizarSuministroPrecio.setDouble(2, dc.getSuministroTB().getPrecioVentaGeneral());
                    prepareActualizarSuministroPrecio.setBoolean(3, dc.getSuministroTB().isTipoPrecio());
                    prepareActualizarSuministroPrecio.setString(4, dc.getIdSuministro());
                    prepareActualizarSuministroPrecio.addBatch();

                    prepareBorrarPrecio.setString(1, dc.getIdSuministro());
                    prepareBorrarPrecio.addBatch();

                    for (PreciosTB t : dc.getSuministroTB().getPreciosTBs()) {
                        prepareRegistrarPrecio.setString(1, "");
                        prepareRegistrarPrecio.setString(2, dc.getIdSuministro());
                        prepareRegistrarPrecio.setString(3, t.getNombre());
                        prepareRegistrarPrecio.setDouble(4, t.getValor());
                        prepareRegistrarPrecio.setDouble(5, t.getFactor() <= 0 ? 1 : t.getFactor());
                        prepareRegistrarPrecio.setBoolean(6, true);
                        prepareRegistrarPrecio.addBatch();
                    }
                }

            }

            prepareRegistrarCompra.executeBatch();
            prepareRegistrarEgreso.executeBatch();
            prepareRegistrarDetalleCompra.executeBatch();
            prepareActualizarCosto.executeBatch();
            prepareActualizarCantidad.executeBatch();
            prepareActualizarCantidadAlmacen.executeBatch();
            prepareRegistrarKardex.executeBatch();
            prepareActualizarSuministroPrecio.executeBatch();
            prepareBorrarPrecio.executeBatch();
            prepareRegistrarPrecio.executeBatch();

            dbf.getConnection().commit();
            return "register";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (callableObtenerCodigoCompra != null) {
                    callableObtenerCodigoCompra.close();
                }
                if (callableObtenerCodigoEgreso != null) {
                    callableObtenerCodigoEgreso.close();
                }
                if (prepareRegistrarCompra != null) {
                    prepareRegistrarCompra.close();
                }
                if (prepareRegistrarEgreso != null) {
                    prepareRegistrarEgreso.close();
                }
                if (prepareRegistrarDetalleCompra != null) {
                    prepareRegistrarDetalleCompra.close();
                }
                if (prepareActualizarCosto != null) {
                    prepareActualizarCosto.close();
                }
                if (prepareActualizarCantidad != null) {
                    prepareActualizarCantidad.close();
                }
                if (prepareActualizarCantidadAlmacen != null) {
                    prepareActualizarCantidadAlmacen.close();
                }
                if (prepareRegistrarKardex != null) {
                    prepareRegistrarKardex.close();
                }
                if (prepareActualizarSuministroPrecio != null) {
                    prepareActualizarSuministroPrecio.close();
                }
                if (prepareRegistrarPrecio != null) {
                    prepareRegistrarPrecio.close();
                }
                if (prepareBorrarPrecio != null) {
                    prepareBorrarPrecio.close();
                }

                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    @Deprecated
    public static String Compra_Contado(EgresoTB egresoTB, MovimientoCajaTB movimientoCajaTB, CompraTB compraTB,
            TableView<DetalleCompraTB> tableView, ObservableList<LoteTB> loteTBs) {
        DBUtil dbf = new DBUtil();

        CallableStatement codigo_compra = null;
        CallableStatement codigo_egreso = null;
        PreparedStatement compra = null;
        PreparedStatement detalle_compra = null;
        PreparedStatement suministro_precios_remover = null;
        PreparedStatement suministro_precios_insertar = null;
        PreparedStatement suministro_almacen_actualizar = null;
        PreparedStatement suministro_cantidad = null;
        PreparedStatement suministro_costo = null;
        PreparedStatement suministro_precio = null;
        PreparedStatement suministro_kardex = null;
        // PreparedStatement lote_compra = null;
        PreparedStatement moviento_caja = null;
        PreparedStatement egreso = null;
        // PreparedStatement preparedBanco = null;
        // PreparedStatement preparedBancoHistorial = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigo_compra = dbf.getConnection().prepareCall("{? = call Fc_Compra_Codigo_Alfanumerico()}");
            codigo_compra.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_compra.execute();
            String id_compra = codigo_compra.getString(1);

            codigo_egreso = dbf.getConnection().prepareCall("{? = call Fc_Egreso_Codigo_Alfanumerico()}");
            codigo_egreso.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_egreso.execute();

            String id_egreso = codigo_egreso.getString(1);

            compra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "CompraTB("
                    + "IdCompra,"
                    + "Proveedor,"
                    + "Comprobante,"
                    + "Serie,"
                    + "Numeracion,"
                    + "TipoMoneda,"
                    + "FechaCompra,"
                    + "HoraCompra,"
                    + "FechaVencimiento,"
                    + "HoraVencimiento,"
                    + "Observaciones,"
                    + "Notas,"
                    + "TipoCompra,"
                    + "Efectivo,"
                    + "Tarjeta,"
                    + "Deposito,"
                    + "EstadoCompra,"
                    + "Usuario,"
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            detalle_compra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "DetalleCompraTB("
                    + "IdCompra,"
                    + "IdArticulo,"
                    + "Cantidad,"
                    + "PrecioCompra,"
                    + "Descuento,"
                    + "IdImpuesto,"
                    + "NombreImpuesto,"
                    + "ValorImpuesto,"
                    + "Importe,"
                    + "Descripcion)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            suministro_precios_remover = dbf.getConnection()
                    .prepareStatement("DELETE FROM PreciosTB WHERE IdSuministro = ?");

            suministro_precios_insertar = dbf.getConnection().prepareStatement(
                    "INSERT INTO PreciosTB(IdArticulo, IdSuministro, Nombre, Valor, Factor,Estado) VALUES(?,?,?,?,?,?)");

            suministro_costo = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET PrecioCompra = ? WHERE IdSuministro = ?");

            suministro_cantidad = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?");

            suministro_precio = dbf.getConnection().prepareStatement(
                    "UPDATE SuministroTB SET Impuesto=?, PrecioVentaGeneral=?, TipoPrecio=? WHERE IdSuministro = ?");

            suministro_almacen_actualizar = dbf.getConnection().prepareStatement(
                    "UPDATE CantidadTB SET Cantidad = Cantidad + ? WHERE IdAlmacen = ? AND IdSuministro = ?");

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
                    + "Total,"
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            /*
             * lote_compra = getConnection().prepareStatement("INSERT INTO "
             * + "LoteTB("
             * + "NumeroLote,"
             * + "FechaCaducidad,"
             * + "ExistenciaInicial,"
             * + "ExistenciaActual,"
             * + "IdArticulo,"
             * + "IdCompra) "
             * + "VALUES(?,?,?,?,?,?)");
             */
            // preparedBanco = DBUtil.getConnection().prepareStatement("UPDATE Banco SET
            // SaldoInicial = SaldoInicial - ? WHERE IdBanco = ?");
            // preparedBancoHistorial = DBUtil.getConnection().prepareStatement("INSERT INTO
            // BancoHistorialTB(IdBanco,IdEmpleado,IdProcedencia,Descripcion,Fecha,Hora,Entrada,Salida)VALUES(?,?,?,?,?,?,?,?)");
            // if (bancoHistorialTB != null) {
            // preparedBanco.setDouble(1, bancoHistorialTB.getSalida());
            // preparedBanco.setString(2, bancoHistorialTB.getIdBanco());
            // preparedBanco.addBatch();
            //
            // preparedBancoHistorial.setString(1, bancoHistorialTB.getIdBanco());
            // preparedBancoHistorial.setString(2, bancoHistorialTB.getIdEmpleado());
            // preparedBancoHistorial.setString(3, "");
            // preparedBancoHistorial.setString(4, bancoHistorialTB.getDescripcion());
            // preparedBancoHistorial.setString(5, bancoHistorialTB.getFecha());
            // preparedBancoHistorial.setString(6, bancoHistorialTB.getHora());
            // preparedBancoHistorial.setDouble(7, 0);
            // preparedBancoHistorial.setDouble(8, bancoHistorialTB.getSalida());
            // preparedBancoHistorial.addBatch();
            // }
            egreso = dbf.getConnection().prepareStatement(
                    "INSERT INTO EgresoTB(\n"
                    + "IdEgreso,\n"
                    + "IdProcedencia,\n"
                    + "IdUsuario,\n"
                    + "Detalle,\n"
                    + "Procedencia,\n"
                    + "Fecha,\n"
                    + "Hora,\n"
                    + "Forma,\n"
                    + "Monto)\n"
                    + "VALUES(?,?,?,?,?,?,?,?,?)");
            if (egresoTB != null) {
                egreso.setString(1, id_egreso);
                egreso.setString(2, id_compra);
                egreso.setString(3, egresoTB.getIdUsuario());
                egreso.setString(4, egresoTB.getDetalle());
                egreso.setInt(5, egresoTB.getProcedencia());
                egreso.setString(6, egresoTB.getFecha());
                egreso.setString(7, egresoTB.getHora());
                egreso.setInt(8, egresoTB.getForma());
                egreso.setDouble(8, egresoTB.getMonto());
                egreso.addBatch();
            }

            moviento_caja = dbf.getConnection().prepareStatement(
                    "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");
            if (movimientoCajaTB != null) {
                moviento_caja.setString(1, movimientoCajaTB.getIdCaja());
                moviento_caja.setString(2, movimientoCajaTB.getFechaMovimiento());
                moviento_caja.setString(3, movimientoCajaTB.getHoraMovimiento());
                moviento_caja.setString(4, movimientoCajaTB.getComentario().toUpperCase());
                moviento_caja.setInt(5, movimientoCajaTB.getTipoMovimiento());
                moviento_caja.setDouble(6, movimientoCajaTB.getMonto());
                moviento_caja.setString(7, id_compra);
                moviento_caja.addBatch();
            }

            compra.setString(1, id_compra);
            compra.setString(2, compraTB.getIdProveedor());
            compra.setInt(3, compraTB.getIdComprobante());
            compra.setString(4, compraTB.getSerie());
            compra.setString(5, compraTB.getNumeracion());
            compra.setInt(6, compraTB.getIdMoneda());
            compra.setString(7, compraTB.getFechaCompra());
            compra.setString(8, compraTB.getHoraCompra());
            compra.setString(9, compraTB.getFechaVencimiento());
            compra.setString(10, compraTB.getHoraVencimiento());
            compra.setString(11, compraTB.getObservaciones());
            compra.setString(12, compraTB.getNotas());
            compra.setInt(13, compraTB.getTipo());
            compra.setBoolean(14, compraTB.getEfectivo());
            compra.setBoolean(15, compraTB.getTarjeta());
            compra.setBoolean(16, compraTB.getDeposito());
            compra.setInt(17, compraTB.getEstado());
            compra.setString(18, compraTB.getUsuario());
            compra.setInt(19, compraTB.getIdAlmacen());
            compra.addBatch();

            for (DetalleCompraTB dc : tableView.getItems()) {
                detalle_compra.setString(1, id_compra);
                detalle_compra.setString(2, dc.getIdSuministro());
                detalle_compra.setDouble(3, dc.getCantidad());
                detalle_compra.setDouble(4, dc.getPrecioCompra());
                detalle_compra.setDouble(5, dc.getDescuento());

                detalle_compra.setInt(6, dc.getIdImpuesto());
                detalle_compra.setString(7, dc.getImpuestoTB().getNombre());
                detalle_compra.setDouble(8, dc.getImpuestoTB().getValor());
                detalle_compra.setDouble(9, dc.getPrecioCompra() * dc.getCantidad());
                detalle_compra.setString(10, dc.getDescripcion());
                detalle_compra.addBatch();

                if (compraTB.isActualizarAlmacen()) {
                    suministro_costo.setDouble(1, dc.getPrecioCompra());
                    suministro_costo.setString(2, dc.getIdSuministro());
                    suministro_costo.addBatch();
                }
                // ---------------------------------------------------------------------
                if (compraTB.getIdAlmacen() == 0) {
                    suministro_cantidad.setDouble(1, dc.getCantidad());
                    suministro_cantidad.setString(2, dc.getIdSuministro());
                    suministro_cantidad.addBatch();

                    suministro_kardex.setString(1, dc.getIdSuministro());
                    suministro_kardex.setString(2, Tools.getDate());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 1);
                    suministro_kardex.setInt(5, 2);
                    suministro_kardex.setString(6, "COMPRA CON SERIE Y NUMERACIÓN: " + compraTB.getSerie().toUpperCase()
                            + "-" + compraTB.getNumeracion().toUpperCase());
                    suministro_kardex.setDouble(7, dc.getCantidad());
                    suministro_kardex.setDouble(8, dc.getPrecioCompra());
                    suministro_kardex.setDouble(9, dc.getCantidad() * dc.getPrecioCompra());
                    suministro_kardex.setInt(10, compraTB.getIdAlmacen());
                    suministro_kardex.addBatch();
                } else {
                    suministro_almacen_actualizar.setDouble(1, dc.getCantidad());
                    suministro_almacen_actualizar.setInt(2, compraTB.getIdAlmacen());
                    suministro_almacen_actualizar.setString(3, dc.getIdSuministro());
                    suministro_almacen_actualizar.addBatch();

                    suministro_kardex.setString(1, dc.getIdSuministro());
                    suministro_kardex.setString(2, Tools.getDate());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 1);
                    suministro_kardex.setInt(5, 2);
                    suministro_kardex.setString(6, "COMPRA CON SERIE Y NUMERACIÓN: " + compraTB.getSerie().toUpperCase()
                            + "-" + compraTB.getNumeracion().toUpperCase());
                    suministro_kardex.setDouble(7, dc.getCantidad());
                    suministro_kardex.setDouble(8, dc.getPrecioCompra());
                    suministro_kardex.setDouble(9, dc.getCantidad() * dc.getPrecioCompra());
                    suministro_kardex.setInt(10, compraTB.getIdAlmacen());
                    suministro_kardex.addBatch();
                }

                if (!dc.isCambiarPrecio()) {
                    suministro_precio.setInt(1, dc.getSuministroTB().getIdImpuesto());
                    suministro_precio.setDouble(2, dc.getSuministroTB().getPrecioVentaGeneral());
                    suministro_precio.setBoolean(3, dc.getSuministroTB().isTipoPrecio());
                    suministro_precio.setString(4, dc.getIdSuministro());
                    suministro_precio.addBatch();

                    suministro_precios_remover.setString(1, dc.getIdSuministro());
                    suministro_precios_remover.addBatch();

                    for (PreciosTB t : dc.getSuministroTB().getPreciosTBs()) {
                        suministro_precios_insertar.setString(1, "");
                        suministro_precios_insertar.setString(2, dc.getIdSuministro());
                        suministro_precios_insertar.setString(3, t.getNombre());
                        suministro_precios_insertar.setDouble(4, t.getValor());
                        suministro_precios_insertar.setDouble(5, t.getFactor() <= 0 ? 1 : t.getFactor());
                        suministro_precios_insertar.setBoolean(6, true);
                        suministro_precios_insertar.addBatch();
                    }
                }

            }

            // for (int i = 0; i < loteTBs.size(); i++) {
            // lote_compra.setString(1, loteTBs.get(i).getNumeroLote().equalsIgnoreCase("")
            // ? id_compra + loteTBs.get(i).getIdArticulo() :
            // loteTBs.get(i).getNumeroLote());
            // lote_compra.setDate(2, Date.valueOf(loteTBs.get(i).getFechaCaducidad()));
            // lote_compra.setDouble(3, loteTBs.get(i).getExistenciaInicial());
            // lote_compra.setDouble(4, loteTBs.get(i).getExistenciaActual());
            // lote_compra.setString(5, loteTBs.get(i).getIdArticulo());
            // lote_compra.setString(6, id_compra);
            // lote_compra.addBatch();
            // }
            egreso.executeBatch();
            moviento_caja.executeBatch();
            compra.executeBatch();
            detalle_compra.executeBatch();
            suministro_cantidad.executeBatch();
            suministro_precio.executeBatch();
            suministro_kardex.executeBatch();
            suministro_costo.executeBatch();
            suministro_precios_remover.executeBatch();
            suministro_precios_insertar.executeBatch();
            suministro_almacen_actualizar.executeBatch();
            // preparedBanco.executeBatch();
            // preparedBancoHistorial.executeBatch();
            // lote_compra.executeBatch();
            dbf.getConnection().commit();
            return "register";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (codigo_compra != null) {
                    codigo_compra.close();
                }
                if (compra != null) {
                    compra.close();
                }
                if (detalle_compra != null) {
                    detalle_compra.close();
                }
                if (suministro_almacen_actualizar != null) {
                    suministro_almacen_actualizar.close();
                }
                if (suministro_costo != null) {
                    suministro_costo.close();
                }
                if (codigo_egreso != null) {
                    codigo_egreso.close();
                }
                // if (preparedBanco != null) {
                // preparedBanco.close();
                // }
                // if (preparedBancoHistorial != null) {
                // preparedBancoHistorial.close();
                // }
                if (suministro_cantidad != null) {
                    suministro_cantidad.close();
                }
                if (suministro_precio != null) {
                    suministro_precio.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                if (suministro_precios_remover != null) {
                    suministro_precios_remover.close();
                }
                if (suministro_precios_insertar != null) {
                    suministro_precios_insertar.close();
                }
                if (egreso != null) {
                    egreso.close();
                }
                if (moviento_caja != null) {
                    moviento_caja.close();
                }
                /*
                 * if (lote_compra != null) {
                 * lote_compra.close();
                 * }
                 */
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    @Deprecated
    public static String Compra_Credito(CompraTB compraTB, TableView<DetalleCompraTB> tableView,
            ObservableList<LoteTB> loteTBs) {
        DBUtil dbf = new DBUtil();
        CallableStatement codigo_compra = null;
        CallableStatement codigo_credito = null;
        PreparedStatement compra = null;
        PreparedStatement detalle_compra = null;
        PreparedStatement suministro_precios_remover = null;
        PreparedStatement suministro_precios_insertar = null;
        PreparedStatement suministro_almacen_actualizar = null;
        PreparedStatement suministro_cantidad = null;
        PreparedStatement suministro_costo = null;
        PreparedStatement suministro_precio = null;
        PreparedStatement suministro_kardex = null;
        // PreparedStatement lote_compra = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            codigo_compra = dbf.getConnection().prepareCall("{? = call Fc_Compra_Codigo_Alfanumerico()}");
            codigo_compra.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_compra.execute();
            String id_compra = codigo_compra.getString(1);

            compra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "CompraTB("
                    + "IdCompra,"
                    + "Proveedor,"
                    + "Comprobante,"
                    + "Serie,"
                    + "Numeracion,"
                    + "TipoMoneda,"
                    + "FechaCompra,"
                    + "HoraCompra,"
                    + "FechaVencimiento,"
                    + "HoraVencimiento,"
                    + "Observaciones,"
                    + "Notas,"
                    + "TipoCompra,"
                    + "Efectivo,"
                    + "Tarjeta,"
                    + "Deposito,"
                    + "EstadoCompra,"
                    + "Usuario,"
                    + "IdAlmacen) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            detalle_compra = dbf.getConnection().prepareStatement("INSERT INTO "
                    + "DetalleCompraTB("
                    + "IdCompra,"
                    + "IdArticulo,"
                    + "Cantidad,"
                    + "PrecioCompra,"
                    + "Descuento,"
                    + "IdImpuesto,"
                    + "NombreImpuesto,"
                    + "ValorImpuesto,"
                    + "Importe,"
                    + "Descripcion)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            suministro_precios_remover = dbf.getConnection()
                    .prepareStatement("DELETE FROM PreciosTB WHERE IdSuministro = ?");

            suministro_precios_insertar = dbf.getConnection().prepareStatement(
                    "INSERT INTO PreciosTB(IdArticulo, IdSuministro, Nombre, Valor, Factor,Estado) VALUES(?,?,?,?,?,?)");

            suministro_costo = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET PrecioCompra = ? WHERE IdSuministro = ?");

            suministro_cantidad = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?");

            suministro_precio = dbf.getConnection().prepareStatement(
                    "UPDATE SuministroTB SET Impuesto=?, PrecioVentaGeneral=?, TipoPrecio=? WHERE IdSuministro = ?");

            suministro_almacen_actualizar = dbf.getConnection().prepareStatement(
                    "UPDATE CantidadTB SET Cantidad = Cantidad + ? WHERE IdAlmacen = ? AND IdSuministro = ?");

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
                    + "Total,"
                    + "IdAlmacen,"
                    + "IdEmpleado) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

            // lote_compra = getConnection().prepareStatement("INSERT INTO "
            // + "LoteTB("
            // + "NumeroLote,"
            // + "FechaCaducidad,"
            // + "ExistenciaInicial,"
            // + "ExistenciaActual,"
            // + "IdArticulo,"
            // + "IdCompra) "
            // + "VALUES(?,?,?,?,?,?)");
            compra.setString(1, id_compra);
            compra.setString(2, compraTB.getIdProveedor());
            compra.setInt(3, compraTB.getIdComprobante());
            compra.setString(4, compraTB.getSerie());
            compra.setString(5, compraTB.getNumeracion());
            compra.setInt(6, compraTB.getIdMoneda());
            compra.setString(7, compraTB.getFechaCompra());
            compra.setString(8, compraTB.getHoraCompra());
            compra.setString(9, compraTB.getFechaVencimiento());
            compra.setString(10, compraTB.getHoraVencimiento());
            compra.setString(11, compraTB.getObservaciones());
            compra.setString(12, compraTB.getNotas());
            compra.setInt(13, compraTB.getTipo());
            compra.setBoolean(14, compraTB.getEfectivo());
            compra.setBoolean(15, compraTB.getTarjeta());
            compra.setBoolean(16, compraTB.getDeposito());
            compra.setInt(17, compraTB.getEstado());
            compra.setString(18, compraTB.getUsuario());
            compra.setInt(19, compraTB.getIdAlmacen());
            compra.addBatch();

            for (DetalleCompraTB dc : tableView.getItems()) {
                detalle_compra.setString(1, id_compra);
                detalle_compra.setString(2, dc.getIdSuministro());
                detalle_compra.setDouble(3, dc.getCantidad());
                detalle_compra.setDouble(4, dc.getPrecioCompra());
                detalle_compra.setDouble(5, dc.getDescuento());

                detalle_compra.setInt(6, dc.getIdImpuesto());
                detalle_compra.setString(7, dc.getImpuestoTB().getNombre());
                detalle_compra.setDouble(8, dc.getImpuestoTB().getValor());
                detalle_compra.setDouble(9, dc.getPrecioCompra() * dc.getCantidad());
                detalle_compra.setString(10, dc.getDescripcion());
                detalle_compra.addBatch();

                if (compraTB.isActualizarAlmacen()) {
                    suministro_costo.setDouble(1, dc.getPrecioCompra());
                    suministro_costo.setString(2, dc.getIdSuministro());
                    suministro_costo.addBatch();
                }
                // ---------------------------------------------------------------------
                if (compraTB.getIdAlmacen() == 0) {
                    suministro_cantidad.setDouble(1, dc.getCantidad());
                    suministro_cantidad.setString(2, dc.getIdSuministro());
                    suministro_cantidad.addBatch();

                    suministro_kardex.setString(1, dc.getIdSuministro());
                    suministro_kardex.setString(2, Tools.getDate());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 1);
                    suministro_kardex.setInt(5, 2);
                    suministro_kardex.setString(6, "COMPRA CON SERIE Y NUMERACIÓN: " + compraTB.getSerie().toUpperCase()
                            + "-" + compraTB.getNumeracion().toUpperCase());
                    suministro_kardex.setDouble(7, dc.getCantidad());
                    suministro_kardex.setDouble(8, dc.getPrecioCompra());
                    suministro_kardex.setDouble(9, dc.getCantidad() * dc.getPrecioCompra());
                    suministro_kardex.setInt(10, compraTB.getIdAlmacen());
                    suministro_kardex.setString(11, Session.USER_ID);
                    suministro_kardex.addBatch();
                } else {
                    suministro_almacen_actualizar.setDouble(1, dc.getCantidad());
                    suministro_almacen_actualizar.setInt(2, compraTB.getIdAlmacen());
                    suministro_almacen_actualizar.setString(3, dc.getIdSuministro());
                    suministro_almacen_actualizar.addBatch();

                    suministro_kardex.setString(1, dc.getIdSuministro());
                    suministro_kardex.setString(2, Tools.getDate());
                    suministro_kardex.setString(3, Tools.getTime());
                    suministro_kardex.setShort(4, (short) 1);
                    suministro_kardex.setInt(5, 2);
                    suministro_kardex.setString(6, "COMPRA CON SERIE Y NUMERACIÓN: " + compraTB.getSerie().toUpperCase()
                            + "-" + compraTB.getNumeracion().toUpperCase());
                    suministro_kardex.setDouble(7, dc.getCantidad());
                    suministro_kardex.setDouble(8, dc.getPrecioCompra());
                    suministro_kardex.setDouble(9, dc.getCantidad() * dc.getPrecioCompra());
                    suministro_kardex.setInt(10, compraTB.getIdAlmacen());
                    suministro_kardex.addBatch();
                }

                if (!dc.isCambiarPrecio()) {
                    suministro_precio.setInt(1, dc.getSuministroTB().getIdImpuesto());
                    suministro_precio.setDouble(2, dc.getSuministroTB().getPrecioVentaGeneral());
                    suministro_precio.setBoolean(3, dc.getSuministroTB().isTipoPrecio());
                    suministro_precio.setString(4, dc.getIdSuministro());
                    suministro_precio.addBatch();

                    suministro_precios_remover.setString(1, dc.getIdSuministro());
                    suministro_precios_remover.addBatch();

                    for (PreciosTB t : dc.getSuministroTB().getPreciosTBs()) {
                        suministro_precios_insertar.setString(1, "");
                        suministro_precios_insertar.setString(2, dc.getIdSuministro());
                        suministro_precios_insertar.setString(3, t.getNombre());
                        suministro_precios_insertar.setDouble(4, t.getValor());
                        suministro_precios_insertar.setDouble(5, t.getFactor() <= 0 ? 1 : t.getFactor());
                        suministro_precios_insertar.setBoolean(6, true);
                        suministro_precios_insertar.addBatch();
                    }
                }
            }

            compra.executeBatch();
            detalle_compra.executeBatch();
            suministro_cantidad.executeBatch();
            suministro_precio.executeBatch();
            suministro_kardex.executeBatch();
            suministro_costo.executeBatch();
            suministro_precios_remover.executeBatch();
            suministro_precios_insertar.executeBatch();
            suministro_almacen_actualizar.executeBatch();
            // lote_compra.executeBatch();
            dbf.getConnection().commit();
            return "register";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (codigo_compra != null) {
                    codigo_compra.close();
                }
                if (compra != null) {
                    compra.close();
                }
                if (detalle_compra != null) {
                    detalle_compra.close();
                }
                if (suministro_almacen_actualizar != null) {
                    suministro_almacen_actualizar.close();
                }
                if (suministro_costo != null) {
                    suministro_costo.close();
                }
                if (suministro_costo != null) {
                    suministro_costo.close();
                }
                if (codigo_credito != null) {
                    codigo_credito.close();
                }
                if (suministro_cantidad != null) {
                    suministro_cantidad.close();
                }
                if (suministro_precio != null) {
                    suministro_precio.close();
                }
                if (suministro_kardex != null) {
                    suministro_kardex.close();
                }
                // if (lote_compra != null) {
                // lote_compra.close();
                // }
                if (suministro_precios_remover != null) {
                    suministro_precios_remover.close();
                }
                if (suministro_precios_insertar != null) {
                    suministro_precios_insertar.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object listaComprasDetalle(int opcion, String value, String fechaInicial, String fechaFinal,
            int comprobante, int estadoCompra, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<CompraTB> empList = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Compras(?,?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, comprobante);
            preparedStatement.setInt(6, estadoCompra);
            preparedStatement.setInt(7, posicionPagina);
            preparedStatement.setInt(8, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                CompraTB compraTB = new CompraTB();
                compraTB.setId(rsEmps.getRow());
                compraTB.setIdCompra(rsEmps.getString("IdCompra"));
                compraTB.setFechaCompra(
                        rsEmps.getDate("FechaCompra").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                compraTB.setHoraCompra(
                        rsEmps.getTime("HoraCompra").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                compraTB.setComprobante(rsEmps.getString("Comprobante"));
                compraTB.setSerie(rsEmps.getString("Serie"));
                compraTB.setNumeracion(rsEmps.getString("Numeracion"));

                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(rsEmps.getString("RazonSocial"));
                compraTB.setProveedorTB(proveedorTB);

                compraTB.setTipo(rsEmps.getInt("TipoCompra"));
                compraTB.setTipoName(rsEmps.getString("Tipo"));

                compraTB.setEstado(rsEmps.getInt("EstadoCompra"));
                compraTB.setEstadoName(rsEmps.getString("Estado"));
                Label label = new Label(compraTB.getEstadoName());
                label.getStyleClass()
                        .add(compraTB.getEstado() == 1 ? "label-asignacion"
                                : compraTB.getEstado() == 2 ? "label-medio"
                                : compraTB.getEstado() == 3 ? "label-proceso" : "label-ultimo");
                compraTB.setEstadoLabel(label);

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(rsEmps.getString("Moneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                compraTB.setMonedaTB(monedaTB);

                compraTB.setTotal(rsEmps.getDouble("Total"));
                empList.add(compraTB);
            }
            objects[0] = empList;

            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Compras_Count(?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setString(3, fechaInicial);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, comprobante);
            preparedStatement.setInt(6, estadoCompra);
            rsEmps = preparedStatement.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }
            objects[1] = cantidadTotal;

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

    public static Object obtenerCompraPorId(String idCompra) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedCompra = null;
        PreparedStatement preparedProveedor = null;
        PreparedStatement preparedDetalleCompra = null;
        PreparedStatement preparedCredito = null;
        PreparedStatement preparedPrecio = null;
        PreparedStatement preparedEgresos = null;
        ResultSet resultSet = null;

        try {
            dbf.dbConnect();
            preparedCompra = dbf.getConnection().prepareStatement("{call Sp_Obtener_Compra_ById(?)}");
            preparedCompra.setString(1, idCompra);
            resultSet = preparedCompra.executeQuery();

            if (resultSet.next()) {
                // compra
                CompraTB compraTB = new CompraTB();
                compraTB.setIdCompra(idCompra);
                compraTB.setFechaCompra(resultSet.getDate("FechaCompra").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                compraTB.setHoraCompra(resultSet.getTime("HoraCompra").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                compraTB.setFechaVencimiento(resultSet.getDate("FechaVencimiento").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                compraTB.setHoraVencimiento(resultSet.getTime("HoraVencimiento").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                compraTB.setIdComprobante(resultSet.getInt("IdComprobante"));
                compraTB.setComprobante(resultSet.getString("Comprobante"));
                compraTB.setSerie(resultSet.getString("Serie").toUpperCase());
                compraTB.setNumeracion(resultSet.getString("Numeracion"));
                compraTB.setTipo(resultSet.getInt("TipoCompra"));
                compraTB.setTipoName(resultSet.getString("Tipo"));
                compraTB.setEstado(resultSet.getInt("EstadoCompra"));
                compraTB.setEstadoName(resultSet.getString("Estado"));
                compraTB.setTotal(resultSet.getDouble("Total"));
                compraTB.setObservaciones(resultSet.getString("Observaciones"));
                compraTB.setNotas(resultSet.getString("Notas"));
                compraTB.setIdAlmacen(resultSet.getInt("IdAlmacen"));

                // almacen
                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setIdAlmacen(resultSet.getInt("IdAlmacen"));
                almacenTB.setNombre(resultSet.getString("AlmacenNombre"));
                compraTB.setAlmacenTB(almacenTB);

                // proveedor
                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setIdProveedor(resultSet.getString("IdProveedor"));
                proveedorTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(resultSet.getString("RazonSocial"));
                proveedorTB.setTelefono(resultSet.getString("Telefono"));
                proveedorTB.setCelular(resultSet.getString("Celular"));
                proveedorTB.setDireccion(resultSet.getString("Direccion"));
                proveedorTB.setEmail(resultSet.getString("Email"));
                compraTB.setProveedorTB(proveedorTB);

                // moneda 
                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(resultSet.getString("Moneda"));
                monedaTB.setAbreviado(resultSet.getString("Abreviado"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                compraTB.setMonedaTB(monedaTB);

                /**
                 *
                 */
                ArrayList<DetalleCompraTB> empList = new ArrayList();
                preparedDetalleCompra = dbf.getConnection().prepareStatement("{call Sp_Listar_Detalle_Compra(?)}");
                preparedDetalleCompra.setString(1, idCompra);
                resultSet = preparedDetalleCompra.executeQuery();
                while (resultSet.next()) {
                    DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                    detalleCompraTB.setId(resultSet.getRow());
                    detalleCompraTB.setIdSuministro(resultSet.getString("IdSuministro"));
                    detalleCompraTB.setCantidad(resultSet.getDouble("Cantidad"));
                    detalleCompraTB.setPrecioCompra(resultSet.getDouble("PrecioCompra"));
                    detalleCompraTB.setDescuento(resultSet.getDouble("Descuento"));
                    detalleCompraTB.setIdImpuesto(resultSet.getInt("IdImpuesto"));

                    // impuesto
                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(resultSet.getInt("IdImpuesto"));
                    impuestoTB.setNombre(resultSet.getString("NombreImpuesto"));
                    impuestoTB.setValor(resultSet.getDouble("ValorImpuesto"));
                    detalleCompraTB.setImpuestoTB(impuestoTB);

                    // suministro
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setClave(resultSet.getString("Clave"));
                    suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                    suministroTB.setUnidadVenta(resultSet.getInt("UnidadVenta"));
                    suministroTB.setUnidadCompraName(resultSet.getString("UnidadCompra"));

                    preparedPrecio = dbf.getConnection()
                            .prepareStatement("SELECT s.TipoPrecio,s.PrecioCompra,i.IdImpuesto,i.Nombre,i.Valor "
                                    + "FROM "
                                    + "SuministroTB AS s "
                                    + "INNER JOIN ImpuestoTB AS i ON i.IdImpuesto = s.Impuesto "
                                    + "WHERE s.IdSuministro = ?");
                    preparedPrecio.setString(1, resultSet.getString("IdSuministro"));
                    ResultSet rsEmps = preparedPrecio.executeQuery();
                    rsEmps.next();
                    suministroTB.setTipoPrecio(rsEmps.getBoolean("TipoPrecio"));
                    suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioCompra"));
                    suministroTB.setIdImpuesto(rsEmps.getInt("IdImpuesto"));
                    suministroTB.setImpuestoTB(new ImpuestoTB(rsEmps.getInt("IdImpuesto"), rsEmps.getString("Nombre"),
                            rsEmps.getDouble("Valor")));

                    // 
                    ArrayList<PreciosTB> preciosTBs = new ArrayList();
                    preparedPrecio = dbf.getConnection()
                            .prepareStatement("SELECT * FROM PreciosTB WHERE IdSuministro = ?");
                    preparedPrecio.setString(1, resultSet.getString("IdSuministro"));
                    rsEmps = preparedPrecio.executeQuery();
                    while (rsEmps.next()) {
                        PreciosTB preciosTB = new PreciosTB();
                        preciosTB.setIdPrecios(rsEmps.getInt("IdPrecios"));
                        preciosTB.setNombre(rsEmps.getString("Nombre"));
                        preciosTB.setValor(rsEmps.getDouble("Valor"));
                        preciosTB.setFactor(rsEmps.getDouble("Factor"));

                        TextField tfNombre = new TextField(rsEmps.getString("Nombre"));
                        tfNombre.getStyleClass().add("text-field-normal");
                        tfNombre.setOnKeyReleased(event -> {
                            preciosTB.setNombre(tfNombre.getText());
                        });

                        TextField tfValor = new TextField(Tools.roundingValue(rsEmps.getDouble("Valor"), 2));
                        tfValor.getStyleClass().add("text-field-normal");
                        tfValor.setOnKeyReleased(event -> {
                            preciosTB.setValor(
                                    !Tools.isNumeric(tfValor.getText()) ? 0 : Double.parseDouble(tfValor.getText()));
                        });
                        tfValor.setOnKeyTyped((KeyEvent event) -> {
                            char c = event.getCharacter().charAt(0);
                            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                                event.consume();
                            }
                            if (c == '.' && tfValor.getText().contains(".")) {
                                event.consume();
                            }
                        });

                        TextField tfFactor = new TextField(Tools.roundingValue(rsEmps.getDouble("Factor"), 2));
                        tfFactor.getStyleClass().add("text-field-normal");
                        tfFactor.setOnKeyReleased(event -> {
                            preciosTB.setFactor(
                                    !Tools.isNumeric(tfFactor.getText()) ? 1 : Double.parseDouble(tfFactor.getText()));
                        });
                        tfFactor.setOnKeyTyped((KeyEvent event) -> {
                            char c = event.getCharacter().charAt(0);
                            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                                event.consume();
                            }
                            if (c == '.' && tfFactor.getText().contains(".")) {
                                event.consume();
                            }
                        });

                        Button button = new Button();
                        button.getStyleClass().add("buttonDark");
                        ImageView view = new ImageView(new Image("/view/image/remove.png"));
                        view.setFitWidth(22);
                        view.setFitHeight(22);
                        button.setGraphic(view);

                        preciosTB.setTxtNombre(tfNombre);
                        preciosTB.setTxtValor(tfValor);
                        preciosTB.setTxtFactor(tfFactor);
                        preciosTB.setBtnOpcion(button);

                        preciosTBs.add(preciosTB);
                    }
                    suministroTB.setPreciosTBs(preciosTBs);
                    detalleCompraTB.setSuministroTB(suministroTB);

                    // BUTTON
                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");
                    detalleCompraTB.setBtnRemove(button);

                    empList.add(detalleCompraTB);
                }
                compraTB.setDetalleCompraTBs(empList);

                /**
                 *
                 */
                ArrayList<CompraCreditoTB> listComprasCredito = new ArrayList();
                preparedCredito = dbf.getConnection().prepareStatement("{call Sp_Listar_Detalle_Compra_Credito(?)}");
                preparedCredito.setString(1, idCompra);
                resultSet = preparedCredito.executeQuery();
                while (resultSet.next()) {
                    CompraCreditoTB creditoTB = new CompraCreditoTB();
                    creditoTB.setMonto(resultSet.getDouble("Monto"));
                    creditoTB.setFechaPago(resultSet.getDate("FechaPago").toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    creditoTB.setHoraPago(resultSet.getTime("HoraPago").toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    creditoTB.setEstado(resultSet.getBoolean("Estado"));
                    listComprasCredito.add(creditoTB);
                }
                compraTB.setCompraCreditoTBs(listComprasCredito);

                /**
                 * Obtener la lista de egresos o gastos asocados a la compra
                 *
                 * @idCompra
                 */
                ArrayList<EgresoTB> egresoTBs = new ArrayList<>();
                preparedEgresos = dbf.getConnection().prepareStatement("select \n"
                        + "e.IdEgreso,\n"
                        + "b.NombreCuenta,\n"
                        + "e.Detalle,\n"
                        + "e.Fecha,\n"
                        + "e.Hora,\n"
                        + "e.Monto,\n"
                        + "e.Vuelto \n"
                        + "from \n"
                        + "EgresoTB as e\n"
                        + "inner join Banco as b on e.IdBanco = b.IdBanco\n"
                        + "where e.IdProcedencia = ?");
                preparedEgresos.setString(1, idCompra);
                resultSet = preparedEgresos.executeQuery();
                while (resultSet.next()) {
                    EgresoTB egresoTB = new EgresoTB();
                    egresoTB.setId(resultSet.getRow());
                    egresoTB.setIdEgreso(resultSet.getString("IdEgreso"));

                    BancoTB bancoTB = new BancoTB();
                    bancoTB.setNombreCuenta(resultSet.getString("NombreCuenta"));
                    egresoTB.setBancoTB(bancoTB);

                    egresoTB.setDetalle(resultSet.getString("Detalle"));
                    egresoTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    egresoTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    egresoTB.setMonto(resultSet.getDouble("Monto"));
                    egresoTB.setVuelto(resultSet.getDouble("Vuelto"));

                    egresoTBs.add(egresoTB);
                }
                compraTB.setEgresoTBs(egresoTBs);

                return compraTB;
            } else {
                throw new Exception("Error en obtener los datos de la compra.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedCompra != null) {
                    preparedCompra.close();
                }
                if (preparedProveedor != null) {
                    preparedProveedor.close();
                }
                if (preparedDetalleCompra != null) {
                    preparedDetalleCompra.close();
                }
                if (preparedCredito != null) {
                    preparedCredito.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedPrecio != null) {
                    preparedPrecio.close();
                }
                if (preparedEgresos != null) {
                    preparedEgresos.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String anularCompra(CompraTB compraTB, String observacion) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementCompra = null;
        PreparedStatement statementCantidad = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementSuministroKardex = null;
        try {
            dbf.dbConnect();

            dbf.getConnection().setAutoCommit(false);

            // Comprueba si la compra ya ha sido anulada
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CompraTB WHERE IdCompra = ? AND EstadoCompra = ?");
            statementValidate.setString(1, compraTB.getIdCompra());
            statementValidate.setInt(2, 3);
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "cancel";
            }

            // Comprueba si la compra tiene ligados creditos
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CompraCreditoTB WHERE IdCompra = ?");
            statementValidate.setString(1, compraTB.getIdCompra());
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "credito";
            }

            // Cambiar el estado de la compra a anulado
            statementCompra = dbf.getConnection().prepareStatement(
                    "UPDATE CompraTB SET EstadoCompra = ?,Observaciones = ? WHERE IdCompra = ?");
            statementCompra.setInt(1, 3);
            statementCompra.setString(2, observacion);
            statementCompra.setString(3, compraTB.getIdCompra());
            statementCompra.addBatch();

            // Anulado el egreso
            statementIngreso = dbf.getConnection()
                    .prepareStatement("UPDATE EgresoTB SET Estado = 0 WHERE IdProcedencia = ?");
            statementIngreso.setString(1, compraTB.getIdCompra());
            statementIngreso.addBatch();

            // Menora la cantidad del suministro
            statementSuministro = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

            // Actualiza la cantidad de almacen
            statementCantidad = dbf.getConnection().prepareStatement(
                    "UPDATE CantidadTB SET Cantidad = Cantidad - ? WHERE IdAlmacen = ? AND IdSuministro = ?");

            // Insertar al nuevo registro del kardex
            statementSuministroKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB("
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

            //
            for (DetalleCompraTB detalleCompraTB : compraTB.getDetalleCompraTBs()) {
                if (compraTB.getIdAlmacen() == 0) {
                    statementSuministro.setDouble(1, detalleCompraTB.getCantidad());
                    statementSuministro.setString(2, detalleCompraTB.getIdSuministro());
                    statementSuministro.addBatch();
                } else {
                    statementCantidad.setDouble(1, detalleCompraTB.getCantidad());
                    statementCantidad.setInt(2, compraTB.getIdAlmacen());
                    statementCantidad.setString(3, detalleCompraTB.getIdSuministro());
                    statementCantidad.addBatch();
                }

                statementSuministroKardex.setString(1, detalleCompraTB.getIdSuministro());
                statementSuministroKardex.setString(2, Tools.getDate());
                statementSuministroKardex.setString(3, Tools.getTime());
                statementSuministroKardex.setShort(4, (short) 2);
                statementSuministroKardex.setInt(5, 1);
                statementSuministroKardex.setString(6, "COMPRA CANCELADA");
                statementSuministroKardex.setDouble(7, detalleCompraTB.getCantidad());
                statementSuministroKardex.setDouble(8, detalleCompraTB.getPrecioCompra());
                statementSuministroKardex.setDouble(9,
                        detalleCompraTB.getCantidad() * detalleCompraTB.getPrecioCompra());
                statementSuministroKardex.setInt(10, compraTB.getIdAlmacen());
                statementSuministroKardex.addBatch();
            }

            statementCompra.executeBatch();
            statementIngreso.executeBatch();
            statementSuministro.executeBatch();
            statementCantidad.executeBatch();
            statementSuministroKardex.executeBatch();
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
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementCompra != null) {
                    statementCompra.close();
                }
                if (statementSuministro != null) {
                    statementSuministro.close();
                }
                if (statementSuministroKardex != null) {
                    statementSuministroKardex.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                if (statementCantidad != null) {
                    statementCantidad.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Compra_ById_For_Credito(String idCompra) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedProveedor = null;
        PreparedStatement preparedCompraCredito = null;
        PreparedStatement preparedDetalleCompra = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            preparedProveedor = dbf.getConnection().prepareStatement("{call Sp_Obtener_Compra_ById_For_Credito(?)}");
            preparedProveedor.setString(1, idCompra);
            resultSet = preparedProveedor.executeQuery();
            if (resultSet.next()) {
                CompraTB compraTB = new CompraTB();
                compraTB.setIdCompra(idCompra);
                compraTB.setSerie(resultSet.getString("Serie"));
                compraTB.setNumeracion(resultSet.getString("Numeracion"));
                compraTB.setEstado(resultSet.getInt("EstadoCompra"));
                compraTB.setEstadoName(resultSet.getString("Estado"));
                compraTB.setMontoTotal(resultSet.getDouble("MontoTotal"));
                compraTB.setMontoPagado(resultSet.getDouble("MontoPagado"));
                compraTB.setMontoRestante(compraTB.getMontoTotal() - compraTB.getMontoPagado());

                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(resultSet.getString("RazonSocial"));
                proveedorTB.setTelefono(resultSet.getString("Telefono"));
                proveedorTB.setCelular(resultSet.getString("Celular"));
                proveedorTB.setDireccion(resultSet.getString("Direccion"));
                proveedorTB.setEmail(resultSet.getString("Email"));
                compraTB.setProveedorTB(proveedorTB);

                preparedCompraCredito = dbf.getConnection()
                        .prepareStatement("{call Sp_Listar_Detalle_Compra_Credito(?)}");
                preparedCompraCredito.setString(1, idCompra);

                resultSet = preparedCompraCredito.executeQuery();
                ArrayList<CompraCreditoTB> ventaCreditoTBs = new ArrayList<>();
                while (resultSet.next()) {
                    CompraCreditoTB compraCreditoTB = new CompraCreditoTB();
                    compraCreditoTB.setId(resultSet.getRow());
                    compraCreditoTB.setIdCompraCredito(resultSet.getString("IdCompraCredito"));
                    compraCreditoTB.setFechaPago(resultSet.getDate("FechaPago").toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    compraCreditoTB.setHoraPago(resultSet.getTime("HoraPago").toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                    compraCreditoTB.setEstado(resultSet.getBoolean("Estado"));
                    compraCreditoTB.setObservacion(resultSet.getString("Observacion"));
                    compraCreditoTB.setMonto(resultSet.getDouble("Monto"));

                    Button btnImprimir = new Button();
                    ImageView imageViewVisualizar = new ImageView(new Image("/view/image/print.png"));
                    imageViewVisualizar.setFitWidth(24);
                    imageViewVisualizar.setFitHeight(24);
                    btnImprimir.setGraphic(imageViewVisualizar);
                    btnImprimir.getStyleClass().add("buttonLightSuccess");
                    compraCreditoTB.setBtnImprimir(btnImprimir);

                    Button btnRemove = new Button();
                    ImageView imageViewRemove = new ImageView(new Image("/view/image/remove-gray.png"));
                    imageViewRemove.setFitWidth(24);
                    imageViewRemove.setFitHeight(24);
                    btnRemove.setGraphic(imageViewRemove);
                    btnRemove.getStyleClass().add("buttonLightError");
                    compraCreditoTB.setBtnRemove(btnRemove);

                    ventaCreditoTBs.add(compraCreditoTB);
                }
                compraTB.setCompraCreditoTBs(ventaCreditoTBs);

                preparedDetalleCompra = dbf.getConnection().prepareStatement("{call Sp_Listar_Detalle_Compra(?)}");
                preparedDetalleCompra.setString(1, idCompra);
                ResultSet rsEmps = preparedDetalleCompra.executeQuery();
                ArrayList<DetalleCompraTB> empList = new ArrayList<>();
                while (rsEmps.next()) {
                    DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                    detalleCompraTB.setId(rsEmps.getRow());
                    detalleCompraTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                    //
                    SuministroTB suministrosTB = new SuministroTB();
                    suministrosTB.setClave(rsEmps.getString("Clave"));
                    suministrosTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                    suministrosTB.setUnidadVenta(rsEmps.getInt("UnidadVenta"));
                    suministrosTB.setUnidadCompraName(rsEmps.getString("UnidadCompra"));
                    detalleCompraTB.setSuministroTB(suministrosTB);
                    //
                    detalleCompraTB.setCantidad(rsEmps.getDouble("Cantidad"));
                    detalleCompraTB.setPrecioCompra(rsEmps.getDouble("PrecioCompra"));
                    detalleCompraTB.setDescuento(rsEmps.getDouble("Descuento"));
                    detalleCompraTB.setIdImpuesto(rsEmps.getInt("IdImpuesto"));

                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(rsEmps.getInt("IdImpuesto"));
                    impuestoTB.setNombre(rsEmps.getString("NombreImpuesto"));
                    impuestoTB.setValor(rsEmps.getDouble("ValorImpuesto"));
                    detalleCompraTB.setImpuestoTB(impuestoTB);

                    empList.add(detalleCompraTB);
                }
                compraTB.setDetalleCompraTBs(empList);

                return compraTB;
            } else {
                throw new Exception("No se pudo carga la información, intente nuevamente.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedCompraCredito != null) {
                    preparedCompraCredito.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedProveedor != null) {
                    preparedProveedor.close();
                }
                if (preparedDetalleCompra != null) {
                    preparedDetalleCompra.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static ModeloObject Registrar_Amortizacion(CompraCreditoTB compraCreditoTB, EgresoTB egresoTB,
            MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();

        ModeloObject result = new ModeloObject();
        CallableStatement codigo_egreso = null;
        CallableStatement callableIdCompraCredito = null;
        PreparedStatement statementValidate = null;
        PreparedStatement statementCompra = null;
        PreparedStatement statementCompraCredito = null;
        PreparedStatement statementEgreso = null;
        PreparedStatement statementMovimientoCaja = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM CompraTB WHERE IdCompra = ? AND EstadoCompra = 3");
            statementValidate.setString(1, compraCreditoTB.getIdCompra());
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();

                result.setId((short) 2);
                result.setMessage("No se pueden realizar pagos a una compra anulada.");
                result.setState("error");

                return result;
            } else {

                statementValidate = dbf.getConnection().prepareStatement("SELECT "
                        + "sum(dc.Cantidad * (dc.PrecioCompra-dc.Descuento)) as Total "
                        + "FROM CompraTB AS c "
                        + "INNER JOIN DetalleCompraTB AS dc ON dc.IdCompra = c.IdCompra "
                        + "where c.IdCompra = ?");
                statementValidate.setString(1, compraCreditoTB.getIdCompra());
                ResultSet resultSet = statementValidate.executeQuery();
                if (resultSet.next()) {
                    double total = Double.parseDouble(Tools.roundingValue(resultSet.getDouble("Total"), 2));

                    callableIdCompraCredito = dbf.getConnection()
                            .prepareCall("{? = call Fc_Compra_Credito_Codigo_Alfanumerico()}");
                    callableIdCompraCredito.registerOutParameter(1, java.sql.Types.VARCHAR);
                    callableIdCompraCredito.execute();
                    String idCompraCreditoCredito = callableIdCompraCredito.getString(1);

                    codigo_egreso = dbf.getConnection().prepareCall("{? = call Fc_Egreso_Codigo_Alfanumerico()}");
                    codigo_egreso.registerOutParameter(1, java.sql.Types.VARCHAR);
                    codigo_egreso.execute();

                    String id_egreso = codigo_egreso.getString(1);

                    statementEgreso = dbf.getConnection().prepareStatement(
                            "INSERT INTO IngresoTB(IdIngreso,IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)\n"
                            + "VALUES(?,?,?,?,?,?,?,?,?)");
                    if (egresoTB != null) {
                        statementEgreso.setString(1, id_egreso);
                        statementEgreso.setString(2, idCompraCreditoCredito);
                        statementEgreso.setString(3, egresoTB.getIdUsuario());
                        statementEgreso.setString(4, egresoTB.getDetalle().toUpperCase());
                        statementEgreso.setInt(5, egresoTB.getProcedencia());
                        statementEgreso.setString(6, egresoTB.getFecha());
                        statementEgreso.setString(7, egresoTB.getHora());
                        statementEgreso.setInt(8, egresoTB.getForma());
                        statementEgreso.setDouble(9, egresoTB.getMonto());
                        statementEgreso.addBatch();
                    }

                    statementMovimientoCaja = dbf.getConnection().prepareStatement(
                            "INSERT INTO MovimientoCajaTB(IdCaja,FechaMovimiento,HoraMovimiento,Comentario,TipoMovimiento,Monto,IdProcedencia)VALUES(?,?,?,?,?,?,?)");
                    if (movimientoCajaTB != null) {
                        statementMovimientoCaja.setString(1, movimientoCajaTB.getIdCaja());
                        statementMovimientoCaja.setString(2, movimientoCajaTB.getFechaMovimiento());
                        statementMovimientoCaja.setString(3, movimientoCajaTB.getHoraMovimiento());
                        statementMovimientoCaja.setString(4, movimientoCajaTB.getComentario().toUpperCase());
                        statementMovimientoCaja.setInt(5, movimientoCajaTB.getTipoMovimiento());
                        statementMovimientoCaja.setDouble(6, movimientoCajaTB.getMonto());
                        statementMovimientoCaja.setString(7, idCompraCreditoCredito);
                        statementMovimientoCaja.addBatch();
                    }

                    statementCompraCredito = dbf.getConnection().prepareStatement(
                            "INSERT INTO CompraCreditoTB(IdCompra,IdCompraCredito,Monto,FechaPago,HoraPago,Estado,IdUsuario,Observacion) VALUES(?,?,?,?,?,?,?,?)");
                    statementCompraCredito.setString(1, compraCreditoTB.getIdCompra());
                    statementCompraCredito.setString(2, idCompraCreditoCredito);
                    statementCompraCredito.setDouble(3, compraCreditoTB.getMonto());
                    statementCompraCredito.setString(4, compraCreditoTB.getFechaPago());
                    statementCompraCredito.setString(5, compraCreditoTB.getHoraPago());
                    statementCompraCredito.setBoolean(6, compraCreditoTB.isEstado());
                    statementCompraCredito.setString(7, compraCreditoTB.getIdEmpleado());
                    statementCompraCredito.setString(8, compraCreditoTB.getObservacion());
                    statementCompraCredito.addBatch();

                    statementValidate = dbf.getConnection()
                            .prepareStatement("SELECT Monto FROM CompraCreditoTB WHERE IdCompra = ?");
                    statementValidate.setString(1, compraCreditoTB.getIdCompra());
                    resultSet = statementValidate.executeQuery();
                    double montoTotal = 0;
                    while (resultSet.next()) {
                        montoTotal += resultSet.getDouble("Monto");
                    }

                    statementCompra = dbf.getConnection()
                            .prepareStatement("UPDATE CompraTB SET EstadoCompra = 1 WHERE IdCompra = ?");
                    if ((montoTotal + compraCreditoTB.getMonto()) >= total) {
                        statementCompra.setString(1, compraCreditoTB.getIdCompra());
                        statementCompra.addBatch();
                    }

                    statementCompraCredito.executeBatch();
                    statementEgreso.executeBatch();
                    statementMovimientoCaja.executeBatch();
                    statementCompra.executeBatch();
                    dbf.getConnection().commit();

                    result.setId((short) 1);
                    result.setState("inserted");
                    result.setIdResult(idCompraCreditoCredito);
                    result.setMessage("Se completo correctamente el proceso.");

                    return result;
                } else {
                    dbf.getConnection().rollback();

                    result.setId((short) 2);
                    result.setMessage(
                            "Problemas al encontrar la compra con el id indicado " + compraCreditoTB.getIdCompra());
                    result.setState("error");

                    return result;
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException ex) {

            }
            result.setId((short) 2);
            result.setMessage(e.getLocalizedMessage());
            result.setState("error");

            return result;
        } finally {
            try {
                if (callableIdCompraCredito != null) {
                    callableIdCompraCredito.close();
                }
                if (statementCompraCredito != null) {
                    statementCompraCredito.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementCompra != null) {
                    statementCompra.close();
                }
                if (codigo_egreso != null) {
                    codigo_egreso.close();
                }
                if (statementEgreso != null) {
                    statementEgreso.close();
                }
                if (statementMovimientoCaja != null) {
                    statementMovimientoCaja.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static String RemoverEgreso(String idVenta, String idCompraCredito) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementCompraCredito = null;
        PreparedStatement statementMovientoCaja = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementCompra = null;
        ResultSet resultSet = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementIngreso = dbf.getConnection().prepareStatement("DELETE FROM IngresoTB WHERE IdProcedencia = ?");
            statementIngreso.setString(1, idCompraCredito);
            statementIngreso.addBatch();

            statementMovientoCaja = dbf.getConnection()
                    .prepareStatement("DELETE FROM MovimientoCajaTB WHERE IdProcedencia = ? ");
            statementMovientoCaja.setString(1, idCompraCredito);
            statementMovientoCaja.addBatch();

            statementCompraCredito = dbf.getConnection()
                    .prepareStatement("DELETE FROM CompraCreditoTB WHERE IdCompraCredito = ? AND IdCompra = ?");
            statementCompraCredito.setString(1, idCompraCredito);
            statementCompraCredito.setString(2, idVenta);
            statementCompraCredito.addBatch();

            statementCompra = dbf.getConnection()
                    .prepareStatement("UPDATE CompraTB SET EstadoCompra = 2 WHERE IdCompra = ?");
            statementCompra.setString(1, idVenta);
            statementCompra.addBatch();

            statementIngreso.executeBatch();
            statementMovientoCaja.executeBatch();
            statementCompraCredito.executeBatch();
            statementCompra.executeBatch();
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
                if (statementCompraCredito != null) {
                    statementCompraCredito.close();
                }
                if (statementCompra != null) {
                    statementCompra.close();
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

    public static Object GetReporteGenetalCompras(String fechaInicial, String fechaFinal, int idComprobante,
            String idProveedor, int tipoCompra, boolean forma, int metodo) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ArrayList<CompraTB> arrayList = new ArrayList<>();

            preparedStatement = dbf.getConnection().prepareCall("{call Sp_Reporte_General_Compras(?,?,?,?,?,?,?)}");
            preparedStatement.setString(1, fechaInicial);
            preparedStatement.setString(2, fechaFinal);
            preparedStatement.setInt(3, idComprobante);
            preparedStatement.setString(4, idProveedor);
            preparedStatement.setInt(5, tipoCompra);
            preparedStatement.setBoolean(6, forma);
            preparedStatement.setInt(7, metodo);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CompraTB compraTB = new CompraTB();
                compraTB.setFechaCompra(resultSet.getDate("FechaCompra").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                compraTB.setIdProveedor(
                        resultSet.getString("NumeroDocumento") + "\n" + resultSet.getString("Proveedor").toUpperCase());
                compraTB.setProveedorTB(new ProveedorTB(resultSet.getString("NumeroDocumento"),
                        resultSet.getString("Proveedor").toUpperCase()));
                compraTB.setComprobante(resultSet.getString("Comprobante").toUpperCase());
                compraTB.setSerie(resultSet.getString("Serie").toUpperCase());
                compraTB.setNumeracion(resultSet.getString("Numeracion"));
                compraTB.setTipoName(resultSet.getString("Tipo"));
                compraTB.setEstado(resultSet.getInt("EstadoCompra"));
                compraTB.setEstadoName(resultSet.getString("EstadoName"));
                compraTB.setFormaName(resultSet.getString("FormaName"));
                compraTB.setEfectivo(resultSet.getBoolean("Efectivo"));
                compraTB.setTarjeta(resultSet.getBoolean("Tarjeta"));
                compraTB.setDeposito(resultSet.getBoolean("Deposito"));
                compraTB.setTotal(resultSet.getDouble("Total"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(resultSet.getString("Moneda"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                compraTB.setMonedaTB(monedaTB);

                arrayList.add(compraTB);
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

    public static Object ListComprasCredito(int opcion, String search, boolean mostrar, String fechaInicio,
            String fechaFinal, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<CompraTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Compras_Credito(?,?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, search);
            preparedStatement.setBoolean(3, mostrar);
            preparedStatement.setString(4, fechaInicio);
            preparedStatement.setString(5, fechaFinal);
            preparedStatement.setInt(6, posicionPagina);
            preparedStatement.setInt(7, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                CompraTB compraTB = new CompraTB();
                compraTB.setId(rsEmps.getRow() + posicionPagina);
                compraTB.setIdCompra(rsEmps.getString("IdCompra"));
                compraTB.setIdProveedor(rsEmps.getString("NumeroDocumento") + "\n" + rsEmps.getString("RazonSocial"));
                compraTB.setFechaCompra(
                        rsEmps.getDate("FechaCompra").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                compraTB.setHoraCompra(
                        rsEmps.getTime("HoraCompra").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                compraTB.setSerie(rsEmps.getString("Serie").toUpperCase());
                compraTB.setNumeracion(rsEmps.getString("Numeracion").toUpperCase());
                compraTB.setProveedorTB(
                        new ProveedorTB(rsEmps.getString("NumeroDocumento"), rsEmps.getString("RazonSocial")));

                compraTB.setEstado(rsEmps.getInt("EstadoCompra"));
                compraTB.setEstadoName(rsEmps.getString("Estado"));
                Label label = new Label(compraTB.getEstadoName());
                label.getStyleClass()
                        .add(compraTB.getEstado() == 1 ? "label-asignacion"
                                : compraTB.getEstado() == 2 ? "label-medio"
                                : compraTB.getEstado() == 3 ? "label-proceso" : "label-ultimo");

                compraTB.setEstadoLabel(label);

                compraTB.setMontoTotal(rsEmps.getDouble("MontoTotal"));
                compraTB.setMontoPagado(rsEmps.getDouble("MontoPagado"));
                compraTB.setMontoRestante(compraTB.getMontoTotal() - compraTB.getMontoPagado());

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setNombre(rsEmps.getString("Moneda"));
                monedaTB.setSimbolo(rsEmps.getString("Simbolo"));
                compraTB.setMonedaTB(monedaTB);

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                hBox.setStyle(";-fx-spacing:0.8333333333333334em;");

                Button btnVisualizar = new Button();
                ImageView imageViewVisualizar = new ImageView(new Image("/view/image/search_caja.png"));
                imageViewVisualizar.setFitWidth(24);
                imageViewVisualizar.setFitHeight(24);
                btnVisualizar.setGraphic(imageViewVisualizar);
                btnVisualizar.getStyleClass().add("buttonLightWarning");

                hBox.getChildren().add(btnVisualizar);
                compraTB.setHbOpciones(hBox);

                empList.add(compraTB);
            }

            objects[0] = empList;

            preparedStatement = dbf.getConnection().prepareCall("{call Sp_Listar_Compras_Credito_Count(?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, search);
            preparedStatement.setBoolean(3, mostrar);
            preparedStatement.setString(4, fechaInicio);
            preparedStatement.setString(5, fechaFinal);
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

    public static Object Listar_Detalle_Compra_ByIdSuministro(String idSuministro) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementDetalle = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ObservableList<DetalleCompraTB> detalleCompraTBs = FXCollections.observableArrayList();

            statementDetalle = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Historial_Costo_ByIdSuministro(?)}");
            statementDetalle.setString(1, idSuministro);
            resultSet = statementDetalle.executeQuery();
            while (resultSet.next()) {
                DetalleCompraTB detalleCompraTB = new DetalleCompraTB();
                detalleCompraTB.setId(resultSet.getRow());
                detalleCompraTB.setDescripcion(
                        resultSet.getString("NumeroDocumento") + "\n" + resultSet.getString("RazonSocial"));
                detalleCompraTB.setPrecioCompra(resultSet.getDouble("PrecioCompra"));
                detalleCompraTB.setCantidad(resultSet.getDouble("Cantidad"));
                detalleCompraTB.setFechaRegistro(resultSet.getDate("FechaCompra").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                detalleCompraTB.setHoraRegistro(resultSet.getTime("HoraCompra").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                detalleCompraTB.setObservacion(
                        resultSet.getString("Serie").toUpperCase() + "-" + resultSet.getString("Numeracion"));
                detalleCompraTBs.add(detalleCompraTB);
            }
            return detalleCompraTBs;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementDetalle != null) {
                    statementDetalle.close();
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
