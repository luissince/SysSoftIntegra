package service;

import static controller.tools.ObjectGlobal.ID_CONCEPTO_VENTA;
import controller.tools.Session;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import model.BancoTB;
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
import model.TipoDocumentoTB;
import model.UbigeoTB;
import model.VentaCreditoTB;
import model.VentaTB;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class VentaADO {

    public static ResultTransaction registrarVenta(VentaTB ventaTB, List<IngresoTB> ingresoTBs, boolean privilegio) {
        // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

        // Objectos que realizan las peticiones T-SQL
        CallableStatement callableObtenerSerieNumeracion = null;
        CallableStatement callableObtenerCodigoVenta = null;
        CallableStatement callableObtenerCodigoIngreso = null;
        CallableStatement callableObtenerCodigoCliente = null;
        PreparedStatement preparedPrivilegios = null;
        PreparedStatement preparedVarificarCliente = null;
        PreparedStatement preparedProcesoCliente = null;
        PreparedStatement preparedRegistrarVenta = null;
        PreparedStatement preparedActualizarCotizacion = null;
        PreparedStatement preparedRegistrarIngreso = null;
        PreparedStatement preparedRegistrarBancoDetalle = null;
        PreparedStatement preparedRegistrarVentaDetalle = null;
        PreparedStatement preparedActualizarSuministro = null;
        PreparedStatement preparedRegistrarKardex = null;
        PreparedStatement preparedActualizarCotizacionDetalle = null;

        // Clase que retorna la respuesta del proceso
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            // Crea la conexión a la base de datos
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Genera un código por venta
            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            // Validar si los productos a vender no tengan stock 0
            int countValidate = 0;
            ArrayList<String> listarFaltantes = new ArrayList<>();

            preparedPrivilegios = dbf.getConnection().prepareStatement("SELECT Cantidad "
                    + "FROM SuministroTB "
                    + "WHERE IdSuministro = ?");

            for (SuministroTB suministroTB : ventaTB.getSuministroTBs()) {
                preparedPrivilegios.setString(1, suministroTB.getIdSuministro());

                ResultSet resultPrivilegios = preparedPrivilegios.executeQuery();
                if (resultPrivilegios.next()) {
                    double cantidadActual;

                    if (suministroTB.getValorInventario() == 1) {
                        cantidadActual = suministroTB.getCantidad() + suministroTB.getBonificacion();
                    } else {
                        cantidadActual = suministroTB.getCantidad();
                    }

                    double cantidadReal = resultPrivilegios.getDouble("Cantidad");

                    if (cantidadActual > cantidadReal) {
                        countValidate++;

                        String clave = suministroTB.getClave();
                        String nombre = suministroTB.getNombreMarca();
                        String informacion = Tools.roundingValue(cantidadReal, 2) + ") Faltante("
                                + Tools.roundingValue(cantidadActual - cantidadReal, 2) + ")";

                        listarFaltantes.add(clave + " - " + nombre + " - Cantidad actual(" + informacion);
                    }
                }
            }

            // Valida si se cumple el stock exista para continuar.
            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(listarFaltantes);
            } else {
                // Verifica si hay cliente con el mismo número de documento
                preparedVarificarCliente = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente "
                                + "FROM ClienteTB "
                                + "WHERE NumeroDocumento = ?");
                preparedVarificarCliente.setString(1, ventaTB.getClienteTB().getNumeroDocumento());
                if (preparedVarificarCliente.executeQuery().next()) {
                    // Si existe lo edita
                    ResultSet resultSet = preparedVarificarCliente.executeQuery();
                    resultSet.next();

                    preparedProcesoCliente = dbf.getConnection().prepareStatement("UPDATE ClienteTB SET "
                            + "TipoDocumento=?,"
                            + "Informacion = ?,"
                            + "Celular=?,"
                            + "Email=?,"
                            + "Direccion=?,"
                            + "FechaModificacion=GETDATE(),"
                            + "HoraModificacion=GETDATE(),"
                            + "IdEmpleado=? "
                            + "WHERE IdCliente = ?");
                    preparedProcesoCliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    preparedProcesoCliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    preparedProcesoCliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    preparedProcesoCliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    preparedProcesoCliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    preparedProcesoCliente.setString(6, Session.USER_ID);
                    preparedProcesoCliente.setString(7, resultSet.getString("IdCliente"));
                    preparedProcesoCliente.addBatch();

                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                } else {
                    // Si no exite lo agrega
                    callableObtenerCodigoCliente = dbf.getConnection()
                            .prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                    callableObtenerCodigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                    callableObtenerCodigoCliente.execute();
                    String idCliente = callableObtenerCodigoCliente.getString(1);

                    preparedProcesoCliente = dbf.getConnection().prepareStatement("INSERT INTO ClienteTB "
                            + "(IdCliente,"
                            + "TipoDocumento,"
                            + "NumeroDocumento,"
                            + "Informacion,"
                            + "Telefono,"
                            + "Celular,"
                            + "Email,"
                            + "Direccion,"
                            + "Representante,"
                            + "Estado,"
                            + "Predeterminado,"
                            + "Sistema,"
                            + "FechaCreacion,"
                            + "HoraCreacion,"
                            + "IdEmpleado) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");

                    preparedProcesoCliente.setString(1, idCliente);
                    preparedProcesoCliente.setInt(2, ventaTB.getClienteTB().getTipoDocumento());
                    preparedProcesoCliente.setString(3, ventaTB.getClienteTB().getNumeroDocumento());
                    preparedProcesoCliente.setString(4, ventaTB.getClienteTB().getInformacion().toUpperCase());
                    preparedProcesoCliente.setString(5, "");
                    preparedProcesoCliente.setString(6, ventaTB.getClienteTB().getCelular());
                    preparedProcesoCliente.setString(7, ventaTB.getClienteTB().getEmail());
                    preparedProcesoCliente.setString(8, ventaTB.getClienteTB().getDireccion().toUpperCase());
                    preparedProcesoCliente.setString(9, "");
                    preparedProcesoCliente.setInt(10, 1);
                    preparedProcesoCliente.setBoolean(11, false);
                    preparedProcesoCliente.setBoolean(12, false);
                    preparedProcesoCliente.setString(13, Session.USER_ID);
                    preparedProcesoCliente.addBatch();

                    ventaTB.setIdCliente(idCliente);
                }

                callableObtenerSerieNumeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");
                callableObtenerSerieNumeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
                callableObtenerSerieNumeracion.setInt(2, ventaTB.getIdComprobante());
                callableObtenerSerieNumeracion.execute();
                String[] serieNumeracion = callableObtenerSerieNumeracion.getString(1).split("-");

                callableObtenerCodigoVenta = dbf.getConnection()
                        .prepareCall("{? = call Fc_Venta_Codigo_Alfanumerico()}");
                callableObtenerCodigoVenta.registerOutParameter(1, java.sql.Types.VARCHAR);
                callableObtenerCodigoVenta.execute();
                String idVenta = callableObtenerCodigoVenta.getString(1);

                callableObtenerCodigoIngreso = dbf.getConnection()
                        .prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
                callableObtenerCodigoIngreso.registerOutParameter(1, java.sql.Types.VARCHAR);
                callableObtenerCodigoIngreso.execute();
                String idIngreso = callableObtenerCodigoIngreso.getString(1);

                // Se registra la venta
                preparedRegistrarVenta = dbf.getConnection().prepareStatement("INSERT INTO VentaTB"
                        + "(IdVenta"
                        + ",Cliente"
                        + ",Vendedor"
                        + ",Comprobante"
                        + ",Moneda"
                        + ",Serie"
                        + ",Numeracion"
                        + ",FechaVenta"
                        + ",HoraVenta"
                        + ",FechaVencimiento"
                        + ",HoraVencimiento"
                        + ",Tipo"
                        + ",Estado"
                        + ",Observaciones"
                        + ",Efectivo"
                        + ",Vuelto"
                        + ",Tarjeta"
                        + ",Codigo"
                        + ",Deposito"
                        + ",TipoCredito"
                        + ",NumeroOperacion"
                        + ",Procedencia) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)");

                preparedRegistrarVenta.setString(1, idVenta);
                preparedRegistrarVenta.setString(2, ventaTB.getIdCliente());
                preparedRegistrarVenta.setString(3, ventaTB.getVendedor());
                preparedRegistrarVenta.setInt(4, ventaTB.getIdComprobante());
                preparedRegistrarVenta.setInt(5, ventaTB.getIdMoneda());
                preparedRegistrarVenta.setString(6, serieNumeracion[0]);
                preparedRegistrarVenta.setString(7, serieNumeracion[1]);
                preparedRegistrarVenta.setString(8, ventaTB.getFechaVenta());
                preparedRegistrarVenta.setString(9, ventaTB.getHoraVenta());
                preparedRegistrarVenta.setString(10, ventaTB.getFechaVenta());
                preparedRegistrarVenta.setString(11, ventaTB.getHoraVenta());
                preparedRegistrarVenta.setInt(12, ventaTB.getTipo());
                preparedRegistrarVenta.setInt(13, ventaTB.getEstado());
                preparedRegistrarVenta.setString(14, ventaTB.getObservaciones());
                preparedRegistrarVenta.setDouble(15, ventaTB.getEfectivo());
                preparedRegistrarVenta.setDouble(16, ventaTB.getVuelto());
                preparedRegistrarVenta.setDouble(17, ventaTB.getTarjeta());
                preparedRegistrarVenta.setString(18, Integer.toString(dig5) + serieNumeracion[1]);
                preparedRegistrarVenta.setDouble(19, ventaTB.getDeposito());
                preparedRegistrarVenta.setInt(20, ventaTB.getTipoCredito());
                preparedRegistrarVenta.setString(21, ventaTB.getNumeroOperacion());
                preparedRegistrarVenta.addBatch();

                // Se actualiza la cotización si la venta usa
                preparedActualizarCotizacion = dbf.getConnection().prepareStatement("UPDATE CotizacionTB SET "
                        + "Estado = 2, "
                        + "IdVenta = ? "
                        + "WHERE IdCotizacion = ?");
                if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                    preparedActualizarCotizacion.setString(1, idVenta);
                    preparedActualizarCotizacion.setString(2, ventaTB.getIdCotizacion());
                    preparedActualizarCotizacion.addBatch();
                }

                // Registra su ingreso
                preparedRegistrarIngreso = dbf.getConnection().prepareStatement("INSERT INTO IngresoTB "
                        + "(IdIngreso,"
                        + "IdProcedencia,"
                        + "IdVentaCredito,"
                        + "IdUsuario,"
                        + "IdCliente,"
                        + "IdConcepto,"
                        + "IdBanco,"
                        + "Detalle,"
                        + "Fecha,"
                        + "Hora,"
                        + "Estado,"
                        + "Monto,"
                        + "Vuelto,"
                        + "Operacion) "
                        + "VALUES(?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?,?,?,?)");

                // Registrar al banco detalle
                preparedRegistrarBancoDetalle = dbf.getConnection().prepareStatement("INSERT INTO BancoHistorialTB "
                        + "(IdBanco,"
                        + "IdProcedencia,"
                        + "IdEmpleado,"
                        + "Descripcion,"
                        + "Fecha,"
                        + "Hora,"
                        + "Estado,"
                        + "Entrada,"
                        + "Salida)"
                        + "VALUES(?,?,?,?,GETDATE(),GETDATE(),?,?,?)");

                for (IngresoTB ingresoTB : ingresoTBs) {
                    preparedRegistrarIngreso.setString(1, idIngreso);
                    preparedRegistrarIngreso.setString(2, idVenta);
                    preparedRegistrarIngreso.setString(3, "");
                    preparedRegistrarIngreso.setString(4, ventaTB.getVendedor());
                    preparedRegistrarIngreso.setString(5, ventaTB.getIdCliente());
                    preparedRegistrarIngreso.setString(6, ingresoTB.getIdConcepto());
                    preparedRegistrarIngreso.setString(7, ingresoTB.getIdBanco());
                    preparedRegistrarIngreso.setString(8,
                            "INGRESO POR VENTA DEL COMPROBANTE " + serieNumeracion[0] + "-" + serieNumeracion[1]);

                    preparedRegistrarIngreso.setBoolean(9, ingresoTB.isEstado());
                    preparedRegistrarIngreso.setDouble(10, ingresoTB.getMonto());
                    preparedRegistrarIngreso.setDouble(11, ingresoTB.getVuelto());
                    preparedRegistrarIngreso.setString(12, ingresoTB.getOperacion());
                    preparedRegistrarIngreso.addBatch();

                    preparedRegistrarBancoDetalle.setString(1, ingresoTB.getIdBanco());
                    preparedRegistrarBancoDetalle.setString(2, idIngreso);
                    preparedRegistrarBancoDetalle.setString(3, ventaTB.getVendedor());
                    preparedRegistrarBancoDetalle.setString(4,
                            "INGRESO POR VENTA DEL COMPROBANTE " + serieNumeracion[0] + "-" + serieNumeracion[1]);
                    preparedRegistrarBancoDetalle.setBoolean(5, true);
                    preparedRegistrarBancoDetalle.setDouble(6, ingresoTB.getMonto());
                    preparedRegistrarBancoDetalle.setDouble(7, 0);
                    preparedRegistrarBancoDetalle.addBatch();

                    // Obtener el número de ingreso actual
                    String prefijo = "IN";
                    String numeroIngreso = idIngreso.replace(prefijo, "");
                    int incremental = Integer.parseInt(numeroIngreso) + 1;

                    // Generar el nuevo ID de ingreso con ceros delante
                    idIngreso = prefijo + String.format("%04d", incremental);
                }

                // Registrar el detalle de la venta
                preparedRegistrarVentaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB "
                        + "(IdVenta"
                        + ",IdArticulo"
                        + ",Cantidad"
                        + ",CostoVenta"
                        + ",PrecioVenta"
                        + ",Descuento"
                        + ",IdOperacion"
                        + ",IdImpuesto"
                        + ",NombreImpuesto"
                        + ",ValorImpuesto"
                        + ",Bonificacion"
                        + ",PorLlevar"
                        + ",Estado"
                        + ",IdMedida) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                // Actualiza la cantidad de cada suministro
                preparedActualizarSuministro = dbf.getConnection().prepareStatement("UPDATE SuministroTB SET "
                        + "Cantidad = Cantidad - ? "
                        + "WHERE IdSuministro = ?");

                // Registrar la salida de suministro
                preparedRegistrarKardex = dbf.getConnection().prepareStatement("INSERT INTO KardexSuministroTB "
                        + "(IdSuministro,"
                        + "Fecha,"
                        + "Hora,"
                        + "Tipo,"
                        + "Movimiento,"
                        + "Detalle,"
                        + "Cantidad,"
                        + "Costo,"
                        + "Total,"
                        + "IdAlmacen,"
                        + "IdEmpleado) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                // Actualiza los suministros ligados a la cotización
                preparedActualizarCotizacionDetalle = dbf.getConnection()
                        .prepareStatement("UPDATE DetalleCotizacionTB SET "
                                + "Uso = ? "
                                + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                    if (ventaTB.getEstado() == 4) {
                        double cantidad = sm.getCantidad();
                        double precio = sm.getPrecioVentaGeneral();

                        preparedRegistrarVentaDetalle.setString(1, idVenta);
                        preparedRegistrarVentaDetalle.setString(2, sm.getIdSuministro());
                        preparedRegistrarVentaDetalle.setDouble(3, cantidad);
                        preparedRegistrarVentaDetalle.setDouble(4, sm.getCostoCompra());
                        preparedRegistrarVentaDetalle.setDouble(5, precio);
                        preparedRegistrarVentaDetalle.setDouble(6, sm.getDescuento());
                        preparedRegistrarVentaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                        preparedRegistrarVentaDetalle.setDouble(8, sm.getIdImpuesto());
                        preparedRegistrarVentaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        preparedRegistrarVentaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                        preparedRegistrarVentaDetalle.setDouble(11, sm.getBonificacion());
                        preparedRegistrarVentaDetalle.setDouble(12, 0);
                        preparedRegistrarVentaDetalle.setString(13, "L");
                        preparedRegistrarVentaDetalle.setInt(14, sm.getUnidadCompra());
                        preparedRegistrarVentaDetalle.addBatch();
                    } else {
                        double cantidad = sm.getCantidad();
                        double precio = sm.getPrecioVentaGeneral();

                        preparedRegistrarVentaDetalle.setString(1, idVenta);
                        preparedRegistrarVentaDetalle.setString(2, sm.getIdSuministro());
                        preparedRegistrarVentaDetalle.setDouble(3, cantidad);
                        preparedRegistrarVentaDetalle.setDouble(4, sm.getCostoCompra());
                        preparedRegistrarVentaDetalle.setDouble(5, precio);
                        preparedRegistrarVentaDetalle.setDouble(6, sm.getDescuento());
                        preparedRegistrarVentaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                        preparedRegistrarVentaDetalle.setDouble(8, sm.getIdImpuesto());
                        preparedRegistrarVentaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        preparedRegistrarVentaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                        preparedRegistrarVentaDetalle.setDouble(11, sm.getBonificacion());
                        preparedRegistrarVentaDetalle.setDouble(12, cantidad);
                        preparedRegistrarVentaDetalle.setString(13, "C");
                        preparedRegistrarVentaDetalle.setInt(14, sm.getUnidadCompra());
                        preparedRegistrarVentaDetalle.addBatch();

                        if (sm.isInventario()) {
                            double cantidadKardex = sm.getCantidad();
                            if (sm.getValorInventario() == 1) {
                                cantidadKardex += sm.getBonificacion();
                            }

                            preparedActualizarSuministro.setDouble(1, cantidadKardex);
                            preparedActualizarSuministro.setString(2, sm.getIdSuministro());
                            preparedActualizarSuministro.addBatch();

                            String observacion = "VENTA CON SERIE Y NUMERACIÓN: " + serieNumeracion[0] + "-"
                                    + serieNumeracion[1];
                            if (sm.getBonificacion() > 0) {
                                observacion += " (BONIFICACIÓN: " + sm.getBonificacion() + ")";
                            }

                            preparedRegistrarKardex.setString(1, sm.getIdSuministro());
                            preparedRegistrarKardex.setString(2, ventaTB.getFechaVenta());
                            preparedRegistrarKardex.setString(3, Tools.getTime());
                            preparedRegistrarKardex.setShort(4, (short) 2);
                            preparedRegistrarKardex.setInt(5, 1);
                            preparedRegistrarKardex.setString(6, observacion);
                            preparedRegistrarKardex.setDouble(7, cantidadKardex);
                            preparedRegistrarKardex.setDouble(8, sm.getCostoCompra());
                            preparedRegistrarKardex.setDouble(9, cantidadKardex * sm.getCostoCompra());
                            preparedRegistrarKardex.setInt(10, 0);
                            preparedRegistrarKardex.setString(11, Session.USER_ID);
                            preparedRegistrarKardex.addBatch();
                        }
                    }

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        preparedActualizarCotizacionDetalle.setBoolean(1, true);
                        preparedActualizarCotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                        preparedActualizarCotizacionDetalle.setString(3, sm.getIdSuministro());
                        preparedActualizarCotizacionDetalle.addBatch();
                    }
                }

                preparedProcesoCliente.executeBatch();
                preparedRegistrarVenta.executeBatch();
                preparedActualizarCotizacion.executeBatch();
                preparedRegistrarIngreso.executeBatch();
                preparedRegistrarBancoDetalle.executeBatch();
                preparedRegistrarVentaDetalle.executeBatch();
                preparedActualizarSuministro.executeBatch();
                preparedRegistrarKardex.executeBatch();
                preparedActualizarCotizacionDetalle.executeBatch();

                dbf.getConnection().commit();

                resultTransaction.setCode("register");
                resultTransaction.setResult(idVenta);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex.getLocalizedMessage());
            } catch (SQLException ex1) {
                resultTransaction.setCode("error");
                resultTransaction.setResult(ex1.getLocalizedMessage());
            } finally {
                try {
                    if (callableObtenerSerieNumeracion != null) {
                        callableObtenerSerieNumeracion.close();
                    }
                    if (callableObtenerCodigoVenta != null) {
                        callableObtenerCodigoVenta.close();
                    }
                    if (callableObtenerCodigoIngreso != null) {
                        callableObtenerCodigoIngreso.close();
                    }
                    if (callableObtenerCodigoCliente != null) {
                        callableObtenerCodigoCliente.close();
                    }
                    if (preparedPrivilegios != null) {
                        preparedPrivilegios.close();
                    }
                    if (preparedVarificarCliente != null) {
                        preparedVarificarCliente.close();
                    }
                    if (preparedProcesoCliente != null) {
                        preparedProcesoCliente.close();
                    }
                    if (preparedRegistrarVenta != null) {
                        preparedRegistrarVenta.close();
                    }
                    if (preparedActualizarCotizacion != null) {
                        preparedActualizarCotizacion.close();
                    }
                    if (preparedRegistrarIngreso != null) {
                        preparedRegistrarIngreso.close();
                    }
                    if (preparedRegistrarBancoDetalle != null) {
                        preparedRegistrarBancoDetalle.close();
                    }
                    if (preparedRegistrarVentaDetalle != null) {
                        preparedRegistrarVentaDetalle.close();
                    }
                    if (preparedActualizarSuministro != null) {
                        preparedActualizarSuministro.close();
                    }
                    if (preparedRegistrarKardex != null) {
                        preparedRegistrarKardex.close();
                    }
                    if (preparedActualizarCotizacionDetalle != null) {
                        preparedActualizarCotizacionDetalle.close();
                    }
                    dbf.dbDisconnect();
                } catch (SQLException e) {
                }
            }
        }
        return resultTransaction;
    }

    @Deprecated
    public static ResultTransaction registrarVentaContado(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        CallableStatement codigo_ingreso = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement ingreso = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<>();
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
                                + Tools.roundingValue(cb, 2) + ") Faltante(" + Tools.roundingValue(ca - cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {
                cliente = dbf.getConnection().prepareStatement(
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());

                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente = ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, Session.USER_ID);
                    cliente.setString(7, resultSet.getString("IdCliente"));
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
                    cliente.setString(13, Session.USER_ID);
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

                codigo_ingreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
                codigo_ingreso.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_ingreso.execute();

                String id_ingreso = codigo_ingreso.getString(1);

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
                        .prepareStatement("UPDATE CotizacionTB SET Estado = 2, IdVenta=?  WHERE IdCotizacion = ?");

                cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                        + "SET Uso = ? "
                        + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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

                    ventaDetalle.setString(1, id_venta);
                    ventaDetalle.setString(2, sm.getIdSuministro());
                    ventaDetalle.setDouble(3, cantidad);
                    ventaDetalle.setDouble(4, sm.getCostoCompra());
                    ventaDetalle.setDouble(5, precio);
                    ventaDetalle.setDouble(6, sm.getDescuento());
                    ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                    ventaDetalle.setDouble(8, sm.getIdImpuesto());
                    ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                    ventaDetalle.setDouble(11, sm.getBonificacion());
                    ventaDetalle.setDouble(12, cantidad);
                    ventaDetalle.setString(13, "C");
                    ventaDetalle.setInt(14, sm.getUnidadCompra());
                    ventaDetalle.addBatch();

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

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        cotizacionDetalle.setBoolean(1, true);
                        cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                        cotizacionDetalle.setString(3, sm.getIdSuministro());
                        cotizacionDetalle.addBatch();
                    }
                }

                ingreso = dbf.getConnection().prepareStatement(
                        "INSERT INTO IngresoTB( "
                        + "IdIngreso, "
                        + "IdProcedencia, "
                        + "IdUsuario, "
                        + "IdCliente, "
                        + "Detalle, "
                        + "Procedencia, "
                        + "Fecha, "
                        + "Hora, "
                        + "Forma, "
                        + "Monto) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)");

                if (ventaTB.getDeposito() > 0) {
                    ingreso.setString(1, id_ingreso);
                    ingreso.setString(2, id_venta);
                    ingreso.setString(3, ventaTB.getVendedor());
                    ingreso.setString(4, ventaTB.getIdCliente());
                    ingreso.setString(5, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(6, 1);
                    ingreso.setString(7, ventaTB.getFechaVenta());
                    ingreso.setString(8, ventaTB.getHoraVenta());
                    ingreso.setInt(9, 3);
                    ingreso.setDouble(10, ventaTB.getDeposito());
                    ingreso.addBatch();

                } else {
                    if (ventaTB.getEfectivo() > 0) {
                        ingreso.setString(1, id_ingreso);
                        ingreso.setString(2, id_venta);
                        ingreso.setString(3, ventaTB.getVendedor());
                        ingreso.setString(4, ventaTB.getIdCliente());
                        ingreso.setString(5, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        ingreso.setInt(6, 1);
                        ingreso.setString(7, ventaTB.getFechaVenta());
                        ingreso.setString(8, ventaTB.getHoraVenta());
                        ingreso.setInt(9, 1);
                        ingreso.setDouble(10, ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                        ingreso.addBatch();
                    }

                    if (ventaTB.getTarjeta() > 0) {
                        ingreso.setString(1, id_ingreso);
                        ingreso.setString(2, id_venta);
                        ingreso.setString(3, ventaTB.getVendedor());
                        ingreso.setString(4, ventaTB.getIdCliente());
                        ingreso.setString(5, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  "
                                + id_comprabante[0] + "-" + id_comprabante[1]);
                        ingreso.setInt(6, 1);
                        ingreso.setString(7, ventaTB.getFechaVenta());
                        ingreso.setString(8, ventaTB.getHoraVenta());
                        ingreso.setInt(9, 2);
                        ingreso.setDouble(10, ventaTB.getTarjeta());
                        ingreso.addBatch();
                    }
                }

                cliente.executeBatch();
                venta.executeBatch();
                ingreso.executeBatch();
                suministro_update.executeBatch();
                ventaDetalle.executeBatch();
                suministro_kardex.executeBatch();
                cotizacion.executeBatch();
                cotizacionDetalle.executeBatch();

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
                if (cotizacion != null) {
                    cotizacion.close();
                }
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
                }
                if (suministro_update != null) {
                    suministro_update.close();
                }
                if (ventaDetalle != null) {
                    ventaDetalle.close();
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
                if (codigo_ingreso != null) {
                    codigo_ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    @Deprecated
    public static ResultTransaction registrarVentaCredito(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        CallableStatement codigo_ingreso = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement venta_credito_codigo = null;
        PreparedStatement venta_credito = null;
        PreparedStatement ingreso = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<>();
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
                                + Tools.roundingValue(cb, 2) + ") Faltante(" + Tools.roundingValue(ca - cb, 2) + ")");
                    }
                }
            }

            if (privilegio && countValidate > 0) {
                dbf.getConnection().rollback();
                resultTransaction.setCode("nocantidades");
                resultTransaction.setArrayResult(arrayResult);
            } else {
                cliente = dbf.getConnection().prepareStatement(
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente =  ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, Session.USER_ID);
                    cliente.setString(7, resultSet.getString("IdCliente"));
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
                    cliente.setString(13, Session.USER_ID);
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

                codigo_ingreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
                codigo_ingreso.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_ingreso.execute();

                String id_ingreso = codigo_ingreso.getString(1);

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

                cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                        + "SET Uso = ? "
                        + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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
                        "INSERT INTO IngresoTB( "
                        + "IdIngreso, "
                        + "IdProcedencia, "
                        + "IdUsuario, "
                        + "IdCliente, "
                        + "Detalle, "
                        + "Procedencia, "
                        + "Fecha, "
                        + "Hora, "
                        + "Forma, "
                        + "Monto) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)");

                if (ventaTB.getVentaCreditoTBs() != null) {
                    venta_credito_codigo = dbf.getConnection()
                            .prepareStatement("SELECT IdVentaCredito FROM VentaCreditoTB ");
                    ResultSet resultSet = venta_credito_codigo.executeQuery();
                    ArrayList<Integer> listCodigos = new ArrayList<>();
                    while (resultSet.next()) {
                        listCodigos.add(Integer.valueOf(resultSet.getString("IdVentaCredito").replace("VC", "")));
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
                        venta_credito.setShort(6, (short) 0);
                        venta_credito.setString(7, ventaTB.getVendedor());
                        venta_credito.setString(8, "");
                        venta_credito.addBatch();
                    }
                }

                for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                    // double cantidad = sm.getValorInventario() == 2 ? sm.getImporteNeto() /
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getCantidad();
                    double cantidad = sm.getCantidad();

                    // double precio = sm.getValorInventario() == 2 ?
                    // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                    double precio = sm.getPrecioVentaGeneral();

                    ventaDetalle.setString(1, id_venta);
                    ventaDetalle.setString(2, sm.getIdSuministro());
                    ventaDetalle.setDouble(3, cantidad);
                    ventaDetalle.setDouble(4, sm.getCostoCompra());
                    ventaDetalle.setDouble(5, precio);
                    ventaDetalle.setDouble(6, sm.getDescuento());
                    ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                    ventaDetalle.setDouble(8, sm.getIdImpuesto());
                    ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                    ventaDetalle.setDouble(11, sm.getBonificacion());
                    ventaDetalle.setDouble(12, cantidad);
                    ventaDetalle.setString(13, "C");
                    ventaDetalle.setInt(14, sm.getUnidadCompra());
                    ventaDetalle.addBatch();

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

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        cotizacionDetalle.setBoolean(1, true);
                        cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                        cotizacionDetalle.setString(3, sm.getIdSuministro());
                        cotizacionDetalle.addBatch();
                    }
                }

                cliente.executeBatch();
                venta.executeBatch();
                ventaDetalle.executeBatch();
                suministro_update.executeBatch();
                suministro_kardex.executeBatch();
                cotizacion.executeBatch();
                cotizacionDetalle.executeBatch();
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
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (ventaDetalle != null) {
                    ventaDetalle.close();
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
                if (codigo_ingreso != null) {
                    codigo_ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    @Deprecated
    public static ResultTransaction registrarVentaAdelantado(VentaTB ventaTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        CallableStatement codigo_ingreso = null;
        PreparedStatement venta = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement ingreso = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            cliente = dbf.getConnection().prepareStatement(
                    "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
            ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
            clienteVerificar = dbf.getConnection()
                    .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
            clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
            if (clienteVerificar.executeQuery().next()) {
                ResultSet resultSet = clienteVerificar.executeQuery();
                resultSet.next();
                ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                cliente = dbf.getConnection().prepareStatement(
                        "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente =  ?");
                cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                cliente.setString(6, Session.USER_ID);
                cliente.setString(7, resultSet.getString("IdCliente"));
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
                cliente.setString(13, Session.USER_ID);
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

            codigo_ingreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
            codigo_ingreso.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_ingreso.execute();

            String id_ingreso = codigo_ingreso.getString(1);

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

            cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                    + "SET Uso = ? "
                    + "WHERE IdCotizacion = ? AND IdSuministro = ?");

            ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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

                ventaDetalle.setString(1, id_venta);
                ventaDetalle.setString(2, sm.getIdSuministro());
                ventaDetalle.setDouble(3, cantidad);
                ventaDetalle.setDouble(4, sm.getCostoCompra());
                ventaDetalle.setDouble(5, precio);
                ventaDetalle.setDouble(6, sm.getDescuento());
                ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                ventaDetalle.setDouble(8, sm.getIdImpuesto());
                ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                ventaDetalle.setDouble(11, sm.getBonificacion());
                ventaDetalle.setDouble(12, 0);
                ventaDetalle.setString(13, "L");
                ventaDetalle.setInt(14, sm.getUnidadCompra());
                ventaDetalle.addBatch();

                if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                    cotizacionDetalle.setBoolean(1, true);
                    cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                    cotizacionDetalle.setString(3, sm.getIdSuministro());
                    cotizacionDetalle.addBatch();
                }
            }

            ingreso = dbf.getConnection().prepareStatement(
                    "INSERT INTO IngresoTB( "
                    + "IdIngreso, "
                    + "IdProcedencia, "
                    + "IdUsuario, "
                    + "IdCliente, "
                    + "Detalle, "
                    + "Procedencia, "
                    + "Fecha, "
                    + "Hora, "
                    + "Forma, "
                    + "Monto) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?)");

            if (ventaTB.getDeposito() > 0) {
                ingreso.setString(1, id_ingreso);
                ingreso.setString(2, id_venta);
                ingreso.setString(3, ventaTB.getVendedor());
                ingreso.setString(4, ventaTB.getIdCliente());
                ingreso.setString(5, "VENTA CON DEPOSITO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                        + "-" + id_comprabante[1]);
                ingreso.setInt(6, 1);
                ingreso.setString(7, ventaTB.getFechaVenta());
                ingreso.setString(8, ventaTB.getHoraVenta());
                ingreso.setInt(9, 3);
                ingreso.setDouble(10, ventaTB.getDeposito());
                ingreso.addBatch();
            } else {

                if (ventaTB.getEfectivo() > 0) {
                    ingreso.setString(1, id_ingreso);
                    ingreso.setString(2, id_venta);
                    ingreso.setString(3, ventaTB.getVendedor());
                    ingreso.setString(4, ventaTB.getIdCliente());
                    ingreso.setString(5, "VENTA CON EFECTIVO DE SERIE Y NUMERACIÓN DEL COMPROBANTE " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(6, 1);
                    ingreso.setString(7, ventaTB.getFechaVenta());
                    ingreso.setString(8, ventaTB.getHoraVenta());
                    ingreso.setInt(9, 1);
                    ingreso.setDouble(10, ventaTB.getTarjeta() > 0 ? ventaTB.getEfectivo() : ventaTB.getTotal());
                    ingreso.addBatch();
                }

                if (ventaTB.getTarjeta() > 0) {
                    ingreso.setString(1, id_ingreso);
                    ingreso.setString(2, id_venta);
                    ingreso.setString(3, ventaTB.getVendedor());
                    ingreso.setString(4, ventaTB.getIdCliente());
                    ingreso.setString(5, "VENTA CON TAJETA DE SERIE Y NUMERACIÓN DEL COMPROBANTE  " + id_comprabante[0]
                            + "-" + id_comprabante[1]);
                    ingreso.setInt(6, 1);
                    ingreso.setString(7, ventaTB.getFechaVenta());
                    ingreso.setString(8, ventaTB.getHoraVenta());
                    ingreso.setInt(9, 2);
                    ingreso.setDouble(10, ventaTB.getTarjeta());
                    ingreso.addBatch();
                }
            }

            cliente.executeBatch();
            venta.executeBatch();
            ingreso.executeBatch();
            ventaDetalle.executeBatch();
            cotizacion.executeBatch();
            cotizacionDetalle.executeBatch();

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
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
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
                if (ventaDetalle != null) {
                    ventaDetalle.close();
                }
                if (codigo_venta != null) {
                    codigo_venta.close();
                }
                if (ingreso != null) {
                    ingreso.close();
                }
                if (codigo_ingreso != null) {
                    codigo_ingreso.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
            }
        }
        return resultTransaction;
    }

    @Deprecated
    public static ResultTransaction registrarVentaContadoPosVenta(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<>();
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
                                + Tools.roundingValue(cb, 2) + ") Faltante(" + Tools.roundingValue(ca - cb, 2) + ")");
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
                            "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                    ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                    clienteVerificar = dbf.getConnection()
                            .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                    clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    if (clienteVerificar.executeQuery().next()) {
                        ResultSet resultSet = clienteVerificar.executeQuery();
                        resultSet.next();
                        ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                        cliente = dbf.getConnection().prepareStatement(
                                "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente =  ?");
                        cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                        cliente.setString(6, Session.USER_ID);
                        cliente.setString(7, resultSet.getString("IdCliente"));
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
                        cliente.setString(13, Session.USER_ID);
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

                    cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                            + "SET Uso = ? "
                            + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                    ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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

                        ventaDetalle.setString(1, id_venta);
                        ventaDetalle.setString(2, sm.getIdSuministro());
                        ventaDetalle.setDouble(3, cantidad);
                        ventaDetalle.setDouble(4, sm.getCostoCompra());
                        ventaDetalle.setDouble(5, precio);
                        ventaDetalle.setDouble(6, sm.getDescuento());
                        ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                        ventaDetalle.setDouble(8, sm.getIdImpuesto());
                        ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                        ventaDetalle.setDouble(11, sm.getBonificacion());
                        ventaDetalle.setDouble(12, cantidad);
                        ventaDetalle.setString(13, "C");
                        ventaDetalle.setInt(14, sm.getUnidadCompra());
                        ventaDetalle.addBatch();

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

                        if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                            cotizacionDetalle.setBoolean(1, true);
                            cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                            cotizacionDetalle.setString(3, sm.getIdSuministro());
                            cotizacionDetalle.addBatch();
                        }
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
                    ventaDetalle.executeBatch();
                    suministro_kardex.executeBatch();
                    cotizacion.executeBatch();
                    cotizacionDetalle.executeBatch();

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
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
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
                if (ventaDetalle != null) {
                    ventaDetalle.close();
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

    @Deprecated
    public static ResultTransaction registrarVentaCreditoPosVenta(VentaTB ventaTB, boolean privilegio) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement venta_credito_codigo = null;
        PreparedStatement venta_credito = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement suministro_update = null;
        PreparedStatement suministro_kardex = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
        ResultTransaction resultTransaction = new ResultTransaction();
        resultTransaction.setResult("Error en completar la petición intente nuevamente por favor.");
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            Random rd = new Random();
            int dig5 = rd.nextInt(90000) + 10000;

            int countValidate = 0;
            ArrayList<String> arrayResult = new ArrayList<>();
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
                                + Tools.roundingValue(cb, 2) + ") Faltante(" + Tools.roundingValue(ca - cb, 2) + ")");
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
                            "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                    ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                    clienteVerificar = dbf.getConnection()
                            .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                    clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                    if (clienteVerificar.executeQuery().next()) {
                        ResultSet resultSet = clienteVerificar.executeQuery();
                        resultSet.next();
                        ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                        cliente = dbf.getConnection().prepareStatement(
                                "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente =  ?");
                        cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                        cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                        cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                        cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                        cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                        cliente.setString(6, Session.USER_ID);
                        cliente.setString(7, resultSet.getString("IdCliente"));
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
                        cliente.setString(13, Session.USER_ID);
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

                    cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                            + "SET Uso = ? "
                            + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                    ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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
                            + "IdEmpleado)"
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

                    suministro_update = dbf.getConnection()
                            .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad - ? WHERE IdSuministro = ?");

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
                        ArrayList<Integer> listCodigos = new ArrayList<>();
                        while (resultSet.next()) {
                            listCodigos.add(Integer.valueOf(resultSet.getString("IdVentaCredito").replace("VC", "")));
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
                            venta_credito.setShort(6, (short) 0);
                            venta_credito.setString(7, ventaTB.getVendedor());
                            venta_credito.setString(8, "");
                            venta_credito.addBatch();
                        }
                    }

                    for (SuministroTB sm : ventaTB.getSuministroTBs()) {
                        double cantidad = sm.getCantidad();

                        // double precio = sm.getValorInventario() == 2 ?
                        // sm.getPrecioVentaGeneralAuxiliar() : sm.getPrecioVentaGeneral();
                        double precio = sm.getPrecioVentaGeneral();

                        ventaDetalle.setString(1, id_venta);
                        ventaDetalle.setString(2, sm.getIdSuministro());
                        ventaDetalle.setDouble(3, cantidad);
                        ventaDetalle.setDouble(4, sm.getCostoCompra());
                        ventaDetalle.setDouble(5, precio);
                        ventaDetalle.setDouble(6, sm.getDescuento());
                        ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                        ventaDetalle.setDouble(8, sm.getIdImpuesto());
                        ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                        ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                        ventaDetalle.setDouble(11, sm.getBonificacion());
                        ventaDetalle.setDouble(12, cantidad);
                        ventaDetalle.setString(13, "C");
                        ventaDetalle.setInt(14, sm.getUnidadCompra());
                        ventaDetalle.addBatch();

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

                        if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                            cotizacionDetalle.setBoolean(1, true);
                            cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                            cotizacionDetalle.setString(3, sm.getIdSuministro());
                            cotizacionDetalle.addBatch();
                        }
                    }

                    cliente.executeBatch();
                    venta.executeBatch();
                    ventaDetalle.executeBatch();
                    suministro_update.executeBatch();
                    suministro_kardex.executeBatch();
                    cotizacion.executeBatch();
                    cotizacionDetalle.executeBatch();
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
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
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
                if (ventaDetalle != null) {
                    ventaDetalle.close();
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

    @Deprecated
    public static ResultTransaction registrarVentaAdelantadoPosVenta(VentaTB ventaTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement serie_numeracion = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigo_venta = null;
        PreparedStatement venta = null;
        PreparedStatement ventaVerificar = null;
        PreparedStatement clienteVerificar = null;
        PreparedStatement cliente = null;
        PreparedStatement ventaDetalle = null;
        PreparedStatement movimiento_caja = null;
        PreparedStatement cotizacion = null;
        PreparedStatement cotizacionDetalle = null;
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
                        "INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema,FechaCreacion,HoraCreacion,IdEmpleado)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                ventaTB.setIdCliente(ventaTB.getClienteTB().getIdCliente());
                clienteVerificar = dbf.getConnection()
                        .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE NumeroDocumento = ?");
                clienteVerificar.setString(1, ventaTB.getClienteTB().getNumeroDocumento().trim());
                if (clienteVerificar.executeQuery().next()) {
                    ResultSet resultSet = clienteVerificar.executeQuery();
                    resultSet.next();
                    ventaTB.setIdCliente(resultSet.getString("IdCliente"));

                    cliente = dbf.getConnection().prepareStatement(
                            "UPDATE ClienteTB SET TipoDocumento=?,Informacion = ?,Celular=?,Email=?,Direccion=?,FechaModificacion=GETDATE(),HoraModificacion=GETDATE(),IdEmpleado=? WHERE IdCliente =  ?");
                    cliente.setInt(1, ventaTB.getClienteTB().getTipoDocumento());
                    cliente.setString(2, ventaTB.getClienteTB().getInformacion().trim().toUpperCase());
                    cliente.setString(3, ventaTB.getClienteTB().getCelular().trim());
                    cliente.setString(4, ventaTB.getClienteTB().getEmail().trim());
                    cliente.setString(5, ventaTB.getClienteTB().getDireccion().trim());
                    cliente.setString(6, Session.USER_ID);
                    cliente.setString(7, resultSet.getString("IdCliente"));
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
                    cliente.setString(13, Session.USER_ID);
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

                cotizacionDetalle = dbf.getConnection().prepareStatement("UPDATE DetalleCotizacionTB "
                        + "SET Uso = ? "
                        + "WHERE IdCotizacion = ? AND IdSuministro = ?");

                ventaDetalle = dbf.getConnection().prepareStatement("INSERT INTO DetalleVentaTB\n"
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

                    ventaDetalle.setString(1, id_venta);
                    ventaDetalle.setString(2, sm.getIdSuministro());
                    ventaDetalle.setDouble(3, cantidad);
                    ventaDetalle.setDouble(4, sm.getCostoCompra());
                    ventaDetalle.setDouble(5, precio);
                    ventaDetalle.setDouble(6, sm.getDescuento());
                    ventaDetalle.setDouble(7, sm.getImpuestoTB().getOperacion());
                    ventaDetalle.setDouble(8, sm.getIdImpuesto());
                    ventaDetalle.setString(9, sm.getImpuestoTB().getNombreImpuesto());
                    ventaDetalle.setDouble(10, sm.getImpuestoTB().getValor());
                    ventaDetalle.setDouble(11, sm.getBonificacion());
                    ventaDetalle.setDouble(12, 0);
                    ventaDetalle.setString(13, "L");
                    ventaDetalle.setInt(14, sm.getUnidadCompra());
                    ventaDetalle.addBatch();

                    if (!ventaTB.getIdCotizacion().equalsIgnoreCase("")) {
                        cotizacionDetalle.setBoolean(1, true);
                        cotizacionDetalle.setString(2, ventaTB.getIdCotizacion());
                        cotizacionDetalle.setString(3, sm.getIdSuministro());
                        cotizacionDetalle.addBatch();
                    }
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
                ventaDetalle.executeBatch();
                cotizacion.executeBatch();
                cotizacionDetalle.executeBatch();

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
                if (cotizacionDetalle != null) {
                    cotizacionDetalle.close();
                }
                if (clienteVerificar != null) {
                    clienteVerificar.close();
                }
                if (cliente != null) {
                    cliente.close();
                }
                if (ventaDetalle != null) {
                    ventaDetalle.close();
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

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(rsEmps.getString("DocumentoCliente"));
                clienteTB.setInformacion(rsEmps.getString("Cliente"));
                ventaTB.setClienteTB(clienteTB);

                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setCodigoAlterno(rsEmps.getString("TipoComprobante"));
                tipoDocumentoTB.setNombre(rsEmps.getString("Comprobante"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

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

    public static Object listarVentas(int opcion, String value, String fechaInicial, String fechaFinal,
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

                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setCodigoAlterno(rsEmps.getString("TipoComprobante"));
                tipoDocumentoTB.setNombre(rsEmps.getString("Comprobante"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

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
                if (preparedStatementCount != null) {
                    preparedStatementCount.close();
                }
                if (preparedStatementSumar != null) {
                    preparedStatementSumar.close();
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

                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setCodigoAlterno(rsEmps.getString("TipoComprobante"));
                tipoDocumentoTB.setNombre(rsEmps.getString("Comprobante"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

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

    public static Object obtenerVentaPorIdVenta(String idVenta) {
        DBUtil dbf = new DBUtil();
        CallableStatement statementVenta = null;
        PreparedStatement statementEmpleado = null;
        PreparedStatement statementEmpresa = null;
        PreparedStatement statementVentaDetalle = null;
        PreparedStatement statementIngresos = null;
        try {
            dbf.dbConnect();
            statementVenta = dbf.getConnection().prepareCall("{call Sp_Obtener_Venta_ById(?)}");
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

                // Agregar el tipo de documento
                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setCodigoAlterno(resultSetVenta.getString("CodigoAlterno"));
                tipoDocumentoTB.setNombre(resultSetVenta.getString("Comprobante"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTB);

                ventaTB.setIdMoneda(resultSetVenta.getInt("IdMoneda"));
                ventaTB.setIdComprobante(resultSetVenta.getInt("IdComprobante"));
                ventaTB.setSerie(resultSetVenta.getString("Serie"));
                ventaTB.setNumeracion(resultSetVenta.getString("Numeracion"));
                ventaTB.setObservaciones(resultSetVenta.getString("Observaciones"));
                ventaTB.setTipo(resultSetVenta.getInt("Tipo"));
                ventaTB.setEstado(resultSetVenta.getInt("Estado"));
                ventaTB.setEfectivo(0);
                ventaTB.setVuelto(0);
                ventaTB.setTarjeta(0);
                ventaTB.setDeposito(0);
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

                // Obtener la lista de ingresos
                statementIngresos = dbf.getConnection().prepareStatement("select \n"
                        + "i.Fecha,\n"
                        + "i.Hora,\n"
                        + "i.Detalle,\n"
                        + "i.Monto,\n"
                        + "i.Vuelto,\n"
                        + "b.NombreCuenta\n"
                        + "from IngresoTB as i\n"
                        + "inner join Banco as b on b.IdBanco = i.IdBanco\n"
                        + "where i.IdProcedencia = ?");
                statementIngresos.setString(1, idVenta);
                ResultSet resultSetIngresos = statementIngresos.executeQuery();
                ArrayList<IngresoTB> ingresoTBs = new ArrayList<>();
                while (resultSetIngresos.next()) {
                    IngresoTB ingresoTB = new IngresoTB();
                    ingresoTB.setId(resultSetIngresos.getRow());
                    ingresoTB.setFecha(resultSetIngresos.getDate("Fecha").toLocalDate()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    ingresoTB.setHora(resultSetIngresos.getTime("Hora").toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                    BancoTB bancoTB = new BancoTB();
                    bancoTB.setNombreCuenta(resultSetIngresos.getString("NombreCuenta"));
                    ingresoTB.setBancoTB(bancoTB);

                    ingresoTB.setDetalle(resultSetIngresos.getString("Detalle"));
                    ingresoTB.setMonto(resultSetIngresos.getDouble("Monto"));
                    ingresoTB.setVuelto(resultSetIngresos.getDouble("Vuelto"));
                    ingresoTBs.add(ingresoTB);
                }
                ventaTB.setIngresoTBs(ingresoTBs);

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
                if (statementIngresos != null) {
                    statementIngresos.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String anularVenta(VentaTB ventaTB, String motivo) {
        // Instancia la clase encargada de trabajar con JDBC
        DBUtil dbf = new DBUtil();

        // Objectos que realizan las peticiones T-SQL
        PreparedStatement statementValidar = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementSuministro = null;
        PreparedStatement statementKardex = null;
        PreparedStatement statementBuscarIngreso = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementBancoHistorial = null;

        try {
            // Crea la conexión a la base de datos
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Valida si la venta ya se cuentra anulada
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ? and Estado = ?");
            statementValidar.setString(1, ventaTB.getIdVenta());
            statementValidar.setInt(2, 3);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "scrambled";
            }

            // Valida si la venta tiene crédito asociados
            statementValidar.close();
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaCreditoTB WHERE IdVenta = ?");
            statementValidar.setString(1, ventaTB.getIdVenta());
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "ventacredito";
            }

            // Se obtiene los datos de la venta y se almacena el estado
            statementValidar.close();
            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT Serie,Numeracion,Estado FROM VentaTB WHERE IdVenta = ?");
            statementValidar.setString(1, ventaTB.getIdVenta());
            ResultSet resultSet = statementValidar.executeQuery();
            resultSet.next();
            String serie = resultSet.getString("Serie");
            String numeracion = resultSet.getString("Numeracion");
            int estado = resultSet.getInt("Estado");
            resultSet.close();

            // Se actualiza el estado a anulado.
            statementVenta = dbf.getConnection().prepareStatement("UPDATE VentaTB SET "
                    + "Estado = ?,"
                    + "Observaciones = ? "
                    + "WHERE IdVenta = ?");
            statementVenta.setInt(1, 3);
            statementVenta.setString(2,
                    Session.USER_NAME.toUpperCase() + " ANULÓ LA VENTA POR EL MOTIVO: " + motivo.toUpperCase());
            statementVenta.setString(3, ventaTB.getIdVenta());
            statementVenta.addBatch();
            statementVenta.executeBatch();

            // Se anulara el ingreso y el detalle banco
            statementIngreso = dbf.getConnection().prepareStatement("UPDATE IngresoTB SET "
                    + "Detalle=?,"
                    + "Estado = 0 "
                    + "WHERE IdIngreso = ?");
            statementBancoHistorial = dbf.getConnection().prepareStatement("UPDATE BancoHistorialTB SET "
                    + "Descripcion=?,"
                    + "Estado = 0 "
                    + "WHERE IdProcedencia = ?");

            statementBuscarIngreso = dbf.getConnection()
                    .prepareStatement("SELECT IdIngreso FROM IngresoTB WHERE IdProcedencia = ?");
            statementBuscarIngreso.setString(1, ventaTB.getIdVenta());
            ResultSet resultSetBuscarIngreso = statementBuscarIngreso.executeQuery();
            while (resultSetBuscarIngreso.next()) {
                String detalle = Session.USER_NAME.toUpperCase() + " ANULÓ LA VENTA " + serie + "-" + numeracion
                        + " POR EL MOTIVO: " + motivo.toUpperCase();

                statementIngreso.setString(1, detalle);
                statementIngreso.setString(2, resultSetBuscarIngreso.getString("IdIngreso"));
                statementIngreso.addBatch();

                statementBancoHistorial.setString(1, detalle);
                statementBancoHistorial.setString(2, resultSetBuscarIngreso.getString("IdIngreso"));
                statementBancoHistorial.addBatch();
            }
            resultSetBuscarIngreso.close();

            // Se regresa la cantidad al inventario
            statementSuministro = dbf.getConnection()
                    .prepareStatement("UPDATE SuministroTB SET Cantidad = Cantidad + ? WHERE IdSuministro = ?");

            // Se registra el movimiento del suministro
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

            // Si es estado es distintio a 4 se quito productos de almacen
            if (estado != 4) {
                for (SuministroTB stb : ventaTB.getSuministroTBs()) {
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
                    statementKardex.setString(11, Session.USER_ID);
                    statementKardex.addBatch();
                }
            }

            statementVenta.executeBatch();
            statementSuministro.executeBatch();
            statementKardex.executeBatch();
            statementIngreso.executeBatch();
            statementBancoHistorial.executeBatch();

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
                if (statementBancoHistorial != null) {
                    statementBancoHistorial.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
                return e.getLocalizedMessage();
            }
        }
    }

    @Deprecated
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
                    + "IdAlmacen,"
                    + "IdEmpleado) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");

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
                    statementKardex.setString(11, Session.USER_ID);
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
        CallableStatement codigo_ingreso = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            codigo_ingreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
            codigo_ingreso.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigo_ingreso.execute();

            String id_ingreso = codigo_ingreso.getString(1);

            ingreso = dbf.getConnection().prepareStatement(
                    "INSERT INTO IngresoTB(IdProcedencia,IdUsuario,Detalle,Procedencia,Fecha,Hora,Forma,Monto)VALUES(?,?,?,?,?,?,?,?,?)");
            ingreso.setString(1, id_ingreso);
            ingreso.setString(2, ingresoTB.getIdProcedencia());
            ingreso.setString(3, ingresoTB.getIdUsuario());
            ingreso.setString(4, ingresoTB.getDetalle());
//            ingreso.setInt(5, ingresoTB.getProcedencia());
            ingreso.setString(6, ingresoTB.getFecha());
            ingreso.setString(7, ingresoTB.getHora());
//            ingreso.setInt(8, ingresoTB.getForma());
            ingreso.setDouble(9, ingresoTB.getMonto());
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
                if (codigo_ingreso != null) {
                    codigo_ingreso.close();
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

    public static Object obtenerVentaDetalleCreditoPorId(String idVenta) {
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
                clienteTB.setIdCliente(resultSet.getString("IdCliente"));
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
                ArrayList<SuministroTB> empList = new ArrayList<>();
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

    public static String[] registrarVentaCreditoIngreso(VentaCreditoTB ventaCreditoTB, IngresoTB ingresoTB) {
        // Creamos el objeto encargado de iniciar la consulta a la base de datos.
        DBUtil dbf = new DBUtil();

        // Variables encargadas de realizar consultas a la base de datos
        CallableStatement codigoIngreso = null;
        PreparedStatement statementValidate = null;
        PreparedStatement statementVenta = null;
        PreparedStatement statementVentaActualizar = null;
        PreparedStatement statementAbono = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementBancoHistorial = null;
        PreparedStatement statementVentaTotal = null;
        CallableStatement statementCodigo = null;

        try {
            // Crear una conexión e iniciar la transacción
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Valida si la venta ya se encuentra anulada
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM VentaTB WHERE Estado = 3 AND IdVenta = ?");
            statementValidate.setString(1, ventaCreditoTB.getIdVenta());
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return new String[]{"0", "No se pueden realizar cobrar a una venta anulada."};
            }

            // Se obtiene el código de ingreso
            codigoIngreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
            codigoIngreso.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoIngreso.execute();
            String idIngreso = codigoIngreso.getString(1);

            // Validamoa si el código enviado corresponde a una venta
            statementValidate.close();
            statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE IdVenta = ?");
            statementValidate.setString(1, ventaCreditoTB.getIdVenta());
            if (!statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return new String[]{"0",
                    "Problemas al encontrar le venta con el id indicado " + ventaCreditoTB.getIdVenta()};
            }

            // Se obtiene el monto total de la venta
            statementVentaTotal = dbf.getConnection().prepareStatement("SELECT "
                    + "SUM(dv.Cantidad*(dv.PrecioVenta - dv.Descuento)) as Total "
                    + "FROM VentaTB AS v "
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = v.IdVenta "
                    + "WHERE v.IdVenta = ?");
            statementVentaTotal.setString(1, ventaCreditoTB.getIdVenta());
            ResultSet resultSetVentaTotal = statementVentaTotal.executeQuery();
            resultSetVentaTotal.next();

            // Asignamos el monto de la consulta ejecutada
            double total = Double.parseDouble(Tools.roundingValue(resultSetVentaTotal.getDouble("Total"), 2));

            // Obtener le código del crédito
            statementCodigo = dbf.getConnection()
                    .prepareCall("{? = call Fc_Venta_Credito_Codigo_Alfanumerico()}");
            statementCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementCodigo.execute();
            String idVentaCredito = statementCodigo.getString(1);

            // Creamos y ejecutamos la consulta para el registro de la venta al crédito
            statementAbono = dbf.getConnection().prepareStatement("INSERT INTO VentaCreditoTB "
                    + "(IdVenta,"
                    + "IdVentaCredito,"
                    + "Monto,"
                    + "FechaPago,"
                    + "HoraPago,"
                    + "Estado,"
                    + "IdUsuario,"
                    + "Observacion)"
                    + "VALUES(?,?,?,GETDATE(),GETDATE(),?,?,?)");
            statementAbono.setString(1, ventaCreditoTB.getIdVenta());
            statementAbono.setString(2, idVentaCredito);
            statementAbono.setDouble(3, ventaCreditoTB.getMonto());
            statementAbono.setShort(4, ventaCreditoTB.getEstado());
            statementAbono.setString(5, ventaCreditoTB.getIdUsuario());
            statementAbono.setString(6, ventaCreditoTB.getObservacion());
            statementAbono.addBatch();

            /*
             * Registramos la consulta de ingresos en un solo proceso
             */
            statementVenta = dbf.getConnection().prepareStatement("SELECT Serie,Numeracion FROM VentaTB WHERE IdVenta = ?");
            statementVenta.setString(1, ventaCreditoTB.getIdVenta());
            ResultSet resultSetVenta = statementVenta.executeQuery();
            resultSetVenta.next();
            String serie = resultSetVenta.getString("Serie");
            String numeracion = resultSetVenta.getString("Numeracion");

            statementIngreso = dbf.getConnection().prepareStatement("INSERT INTO IngresoTB "
                    + "(IdIngreso,"
                    + "IdProcedencia,"
                    + "IdVentaCredito,"
                    + "IdUsuario,"
                    + "IdCliente,"
                    + "IdBanco,"
                    + "Detalle,"
                    + "Procedencia,"
                    + "Fecha,"
                    + "Hora,"
                    + "Forma,"
                    + "Estado,"
                    + "Monto,"
                    + "Vuelto,"
                    + "Operacion) "
                    + "VALUES(?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?,?,?,?,?)");
            statementIngreso.setString(1, idIngreso);
            statementIngreso.setString(2, ingresoTB.getIdProcedencia());
            statementIngreso.setString(3, idVentaCredito);
            statementIngreso.setString(4, ingresoTB.getIdUsuario());
            statementIngreso.setString(5, ventaCreditoTB.getVentaTB().getClienteTB().getIdCliente());
            statementIngreso.setString(6, ingresoTB.getIdBanco());
            statementIngreso.setString(7, "INGRESO POR VENTA AL CRÉDITO DEL COMPROBANTE " + serie + "-" + numeracion);
            statementIngreso.setInt(8, 1);
            statementIngreso.setInt(9, 3);
            statementIngreso.setBoolean(10, ingresoTB.isEstado());
            statementIngreso.setDouble(11, ingresoTB.getMonto());
            statementIngreso.setDouble(12, ingresoTB.getVuelto());
            statementIngreso.setString(13, ingresoTB.getOperacion());
            statementIngreso.addBatch();

            statementBancoHistorial = dbf.getConnection().prepareStatement("INSERT INTO BancoHistorialTB "
                    + "(IdBanco,"
                    + "IdProcedencia,"
                    + "IdEmpleado,"
                    + "Descripcion,"
                    + "Fecha,"
                    + "Hora,"
                    + "Estado,"
                    + "Entrada,"
                    + "Salida)"
                    + "VALUES(?,?,?,?,GETDATE(),GETDATE(),?,?,?)");
            statementBancoHistorial.setString(1, ingresoTB.getIdBanco());
            statementBancoHistorial.setString(2, idIngreso);
            statementBancoHistorial.setString(3, ingresoTB.getIdUsuario());
            statementBancoHistorial.setString(4,
                    "INGRESO POR VENTA AL CRÉDITO DEL COMPROBANTE " + serie + "-" + numeracion);
            statementBancoHistorial.setBoolean(5, true);
            statementBancoHistorial.setDouble(6, ingresoTB.getMonto());
            statementBancoHistorial.setDouble(7, 0);
            statementBancoHistorial.addBatch();

            // Se obtiene la lista de los montos cobrados
            statementValidate = dbf.getConnection().prepareStatement("SELECT Monto "
                    + "FROM VentaCreditoTB "
                    + "WHERE IdVenta = ?");
            statementValidate.setString(1, ventaCreditoTB.getIdVenta());
            ResultSet resultSetVentaCredito = statementValidate.executeQuery();
            double montoTotal = 0;
            while (resultSetVentaCredito.next()) {
                montoTotal += resultSetVentaCredito.getDouble("Monto");
            }
            resultSetVentaCredito.close();

            // Se actualiza el estado de la venta si se cumple la condición
            // donde el monto cobrado mas el monto a cobrar es mayor o igual al total de la
            // venta
            statementVentaActualizar = dbf.getConnection().prepareStatement("UPDATE VentaTB "
                    + "SET Estado = 1 "
                    + "WHERE IdVenta = ?");
            if ((montoTotal + ventaCreditoTB.getMonto()) >= total) {
                statementVentaActualizar.setString(1, ventaCreditoTB.getIdVenta());
                statementVentaActualizar.addBatch();
            }

            // Ejecutamos los batch y un commit para completar la transacción
            statementAbono.executeBatch();
            statementIngreso.executeBatch();
            statementBancoHistorial.executeBatch();
            statementVentaActualizar.executeBatch();
            dbf.getConnection().commit();

            // Retornamos si se completo correctamente el proceso
            return new String[]{"1", "Se completo correctamente el proceso.", idVentaCredito};

        } catch (SQLException | NullPointerException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return new String[]{"0", ex.getLocalizedMessage()};
        } finally {
            try {
                if (codigoIngreso != null) {
                    codigoIngreso.close();
                }
                if (statementAbono != null) {
                    statementAbono.close();
                }
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                if (statementBancoHistorial != null) {
                    statementBancoHistorial.close();
                }
                if (statementCodigo != null) {
                    statementCodigo.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementVentaActualizar != null) {
                    statementVentaActualizar.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static String removerIngreso(String idVenta, String idVentaCredito) {
        // Creamos el objeto encargado de iniciar la consulta a la base de datos.
        DBUtil dbf = new DBUtil();

        // Variables encargadas de realizar consultas a la base de datos
        PreparedStatement statementValidate = null;
        PreparedStatement statementVentaCredito = null;
        PreparedStatement statementBuscarIngreso = null;
        PreparedStatement statementIngreso = null;
        PreparedStatement statementBancoHistorial = null;
        PreparedStatement statementVentaTotal = null;
        PreparedStatement statementVenta = null;

        try {
            // Crear una conexión e iniciar la transacción
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            // Validamos si la venta esta anulada
            statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB "
                    + "WHERE IdVenta = ? AND Estado = 3");
            statementValidate.setString(1, idVenta);
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "La venta se encuentra anulada, es por ello que no se puede continuar.";
            }

            // Obtenemos el tipo de crédito
            statementValidate.close();
            statementValidate = dbf.getConnection().prepareStatement("SELECT TipoCredito "
                    + "FROM VentaTB "
                    + "WHERE IdVenta = ?");
            statementValidate.setString(1, idVenta);
            ResultSet resultSet = statementValidate.executeQuery();
            resultSet.next();
            int tipoCredito = resultSet.getInt("TipoCredito");
            resultSet.close();

            if (tipoCredito == 0) {
                // Validamos que la venta al crédito exista
                statementValidate.close();
                statementValidate = dbf.getConnection()
                        .prepareStatement("SELECT * FROM VentaCreditoTB WHERE IdVentaCredito = ?");
                statementValidate.setString(1, idVentaCredito);
                if (!statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "No se encontró registro del plazo a anular.";
                }
            } else {
                // Validamos que la venta al crédito no este cobrada
                statementValidate.close();
                statementValidate = dbf.getConnection()
                        .prepareStatement("SELECT * FROM VentaCreditoTB WHERE IdVentaCredito = ? AND Estado = 0");
                statementValidate.setString(1, idVenta);
                if (statementValidate.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "El plazo ya se encuentra anulado, actualiza la vista.";
                }
            }

            // Iniciamos el proceso de anulación
            if (tipoCredito == 0) {
                // Plazos variables
                statementVentaCredito = dbf.getConnection()
                        .prepareStatement("DELETE FROM VentaCreditoTB WHERE IdVentaCredito = ? AND IdVenta = ?");
                statementVentaCredito.setString(1, idVentaCredito);
                statementVentaCredito.setString(2, idVenta);
                statementVentaCredito.addBatch();
            } else {
                // Plazos fijos
                statementVentaCredito = dbf.getConnection().prepareStatement(
                        "UPDATE VentaCreditoTB SET Estado = 0 WHERE IdVentaCredito = ? AND IdVenta = ?");
                statementVentaCredito.setString(1, idVentaCredito);
                statementVentaCredito.setString(2, idVenta);
                statementVentaCredito.addBatch();
            }

            // Anulamos el ingreso y banco historial
            statementIngreso = dbf.getConnection()
                    .prepareStatement("UPDATE IngresoTB SET "
                            + "Detalle=?,"
                            + "Estado = 0 "
                            + "WHERE IdProcedencia = ?");
            statementBancoHistorial = dbf.getConnection().prepareStatement("UPDATE BancoHistorialTB SET "
                    + "Descripcion=?,"
                    + "Estado = 0 "
                    + "WHERE IdProcedencia = ?");

            statementBuscarIngreso = dbf.getConnection().prepareStatement("SELECT IdIngreso FROM IngresoTB "
                    + "WHERE IdProcedencia = ?");
            statementBuscarIngreso.setString(1, idVentaCredito);
            ResultSet resultSetBuscarIngreso = statementBuscarIngreso.executeQuery();
            while (resultSetBuscarIngreso.next()) {
                String detalle = Session.USER_NAME.toUpperCase() + " ANULÓ EL COBRO DE LA VENTA LA CRÉDITO N°-"
                        + idVentaCredito;

                statementIngreso.setString(1, detalle);
                statementIngreso.setString(2, resultSetBuscarIngreso.getString("IdIngreso"));
                statementIngreso.addBatch();

                statementBancoHistorial.setString(1, detalle);
                statementBancoHistorial.setString(2, resultSetBuscarIngreso.getString("IdIngreso"));
                statementBancoHistorial.addBatch();
            }

            // Se obtiene el monto total de la venta
            statementVentaTotal = dbf.getConnection().prepareStatement("SELECT "
                    + "SUM(dv.Cantidad*(dv.PrecioVenta - dv.Descuento)) as Total "
                    + "FROM VentaTB AS v "
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = v.IdVenta "
                    + "WHERE v.IdVenta = ?");
            statementVentaTotal.setString(1, idVenta);
            ResultSet resultSetVentaTotal = statementVentaTotal.executeQuery();
            resultSetVentaTotal.next();
            double total = Double.parseDouble(Tools.roundingValue(resultSetVentaTotal.getDouble("Total"), 2));
            resultSetVentaTotal.close();

            // Se obtiene la lista de los montos cobrados
            statementValidate.close();
            statementValidate = dbf.getConnection().prepareStatement("SELECT ISNULL(SUM(Monto),0) AS Monto "
                    + "FROM VentaCreditoTB "
                    + "WHERE IdVenta = ?");
            statementValidate.setString(1, idVenta);
            ResultSet resultSetVentaCredito = statementValidate.executeQuery();
            resultSetVentaCredito.next();
            double montoCobrado = Double.parseDouble(Tools.roundingValue(resultSetVentaCredito.getDouble("Monto"), 2));
            resultSetVentaCredito.close();

            // Se obtiene el monto de la venta al crédito anulado
            statementValidate.close();
            statementValidate = dbf.getConnection().prepareStatement("SELECT ISNULL(SUM(Monto),0) AS Monto "
                    + "FROM IngresoTB "
                    + "WHERE IdProcedencia = ?");
            statementValidate.setString(1, idVentaCredito);
            ResultSet resultSetIngreso = statementValidate.executeQuery();
            resultSetIngreso.next();
            double montoIngreso = Double.parseDouble(Tools.roundingValue(resultSetIngreso.getDouble("Monto"), 2));
            resultSetIngreso.close();

            // Actualizamos la venta a estado pendiente si el monto
            // cobrado menos el ingreso es menor que el total
            statementVenta = dbf.getConnection().prepareStatement("UPDATE VentaTB SET Estado = 2 WHERE IdVenta = ?");
            if ((montoCobrado - montoIngreso) < total) {
                statementVenta.setString(1, idVenta);
                statementVenta.addBatch();
            }

            statementIngreso.executeBatch();
            statementBancoHistorial.executeBatch();
            statementBancoHistorial.executeBatch();
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
                if (statementIngreso != null) {
                    statementIngreso.close();
                }
                if (statementBancoHistorial != null) {
                    statementBancoHistorial.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static ModeloObject ActualizarIngresoPorId(VentaCreditoTB ventaCreditoTB, IngresoTB ingresoTB,
            MovimientoCajaTB movimientoCajaTB) {
        DBUtil dbf = new DBUtil();

        ModeloObject result = new ModeloObject();
        CallableStatement codigo_ingreso = null;
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

                codigo_ingreso = dbf.getConnection().prepareCall("{? = call Fc_Ingreso_Codigo_Alfanumerico()}");
                codigo_ingreso.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigo_ingreso.execute();

                String id_ingreso = codigo_ingreso.getString(1);

                statementIngreso = dbf.getConnection().prepareStatement("INSERT INTO IngresoTB(\n"
                        + "IdIngreso,\n"
                        + "IdProcedencia,\n"
                        + "IdUsuario,\n"
                        + "Detalle,\n"
                        + "Procedencia,\n"
                        + "Fecha,\n"
                        + "Hora,\n"
                        + "Forma,\n"
                        + "Monto)\n"
                        + "VALUES(?,?,?,?,?,?,?,?,?)");
                if (ingresoTB != null) {
                    statementIngreso.setString(1, id_ingreso);
                    statementIngreso.setString(2, ventaCreditoTB.getIdVentaCredito());
                    statementIngreso.setString(3, ingresoTB.getIdUsuario());
                    statementIngreso.setString(4, ingresoTB.getDetalle());
//                    statementIngreso.setInt(5, ingresoTB.getProcedencia());
                    statementIngreso.setString(6, ingresoTB.getFecha());
                    statementIngreso.setString(7, ingresoTB.getHora());
//                    statementIngreso.setInt(8, ingresoTB.getForma());
                    statementIngreso.setDouble(9, ingresoTB.getMonto());
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
                if (codigo_ingreso != null) {
                    codigo_ingreso.close();
                }
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
                                + "IdAlmacen,"
                                + "IdEmpleado) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
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
                        statementKardex.setString(11, Session.USER_ID);
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
                            + "IdAlmacen,"
                            + "IdEmpleado) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
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
                    statementKardex.setString(11, Session.USER_ID);
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
                ArrayList<HistorialSuministroSalidaTB> suministroSalidas = new ArrayList<>();
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
