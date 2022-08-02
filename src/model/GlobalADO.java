package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GlobalADO {

    public static ArrayList<Double> ReporteGlobal(String fechaInicial, String fechaFinal) {
        PreparedStatement preparedGlobal = null;
        ResultSet resultSet = null;
        ArrayList<Double> list = new ArrayList<>();

        DBUtil.dbConnect();
        try {

            // ventas contado
            preparedGlobal = DBUtil.getConnection().prepareStatement("SELECT ISNULL(sum(dv.Cantidad*(dv.PrecioVenta-dv.Descuento)),0) AS Total FROM VentaTB as v INNER JOIN DetalleVentaTB as dv on dv.IdVenta = v.IdVenta LEFT JOIN NotaCreditoTB as nc on nc.IdVenta = v.IdVenta WHERE v.FechaVenta between ? AND ? AND v.Tipo = 1 AND v.Estado <> 3 AND nc.IdNotaCredito IS NULL");
            preparedGlobal.setString(1, fechaInicial);
            preparedGlobal.setString(2, fechaFinal);
            resultSet = preparedGlobal.executeQuery();
            double ventasContado = 0;
            if (resultSet.next()) {
                ventasContado += resultSet.getDouble("Total");
            }
            list.add(ventasContado);

            // ventas credito
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as VentaTB from VentaTB where Tipo = 2 and Estado = 2 and FechaVenta between ? and ? ");
            preparedGlobal.setString(1, fechaInicial);
            preparedGlobal.setString(2, fechaFinal);
            resultSet = preparedGlobal.executeQuery();
            double ventasCredito = 0;
            if (resultSet.next()) {
                ventasCredito += resultSet.getDouble("Total");
            }
            list.add(ventasCredito);

            // ventas anuladas                
            double ventasAnuladas = 0;
            list.add(ventasAnuladas);

            // utilidad
            preparedGlobal = DBUtil.getConnection().prepareStatement("select\n"
                    + "case\n"
                    + "when a.ValorInventario = 1 then (dv.Cantidad * dv.PrecioVenta)- (dv.Cantidad * dv.CostoVenta )\n"
                    + "when a.ValorInventario = 2 then (dv.Cantidad * dv.PrecioVenta )- (dv.Cantidad * dv.CostoVenta )\n"
                    + "when a.ValorInventario = 3 then (dv.Cantidad * dv.PrecioVenta )- (dv.Cantidad * dv.CostoVenta )\n"
                    + "end as Utilidad\n"
                    + "from DetalleVentaTB as dv\n"
                    + "inner join SuministroTB as a on dv.IdArticulo = a.IdSuministro \n"
                    + "inner join VentaTB as v on v.IdVenta = dv.IdVenta\n"
                    + "left join NotaCreditoTB as nc on nc.IdVenta = v.IdVenta\n"
                    + "inner join MonedaTB as m on m.IdMoneda = v.Moneda\n"
                    + "where v.Estado <> 3 and v.FechaVenta between ? and ? and nc.IdNotaCredito is null");
            preparedGlobal.setString(1, fechaInicial);
            preparedGlobal.setString(2, fechaFinal);
            resultSet = preparedGlobal.executeQuery();
            double utilidad = 0;
            while (resultSet.next()) {
                utilidad += resultSet.getObject("Utilidad") == null ? 0 : resultSet.getDouble("Utilidad");
            }
            list.add(utilidad);

            // compras al contado
            preparedGlobal = DBUtil.getConnection().prepareStatement("SELECT SUM(d.Importe) AS Total FROM CompraTB as c inner join DetalleCompraTB as d on d.IdCompra = c.IdCompra where c.FechaCompra between ? and ? and c.EstadoCompra = 1");
            preparedGlobal.setString(1, fechaInicial);
            preparedGlobal.setString(2, fechaFinal);
            resultSet = preparedGlobal.executeQuery();
            double totalcontado = 0;
            if (resultSet.next()) {
                totalcontado = resultSet.getObject("totalcontado") == null ? 0 : resultSet.getDouble("totalcontado");
            }
            list.add(totalcontado);

//              compras al credito
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as ComprasPagar from CompraTB where TipoCompra = 2 and EstadoCompra = 2 and c.FechaCompra between ? and ?");
            preparedGlobal.setString(1, fechaInicial);
            preparedGlobal.setString(2, fechaFinal);
            resultSet = preparedGlobal.executeQuery();
            double totalcredito = 0;
            if (resultSet.next()) {
                totalcredito = resultSet.getObject("totalcredito") == null ? 0 : resultSet.getDouble("totalcredito");
            }
            list.add(totalcredito);

            // compras anuladas         
            double comprasAnuladas = 0;

            list.add(comprasAnuladas);

            // CUENTAS
            double ventas_cobrar = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as VentasCobrar from VentaTB where Tipo = 2 and Estado = 2");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                ventas_cobrar = resultSet.getObject("VentasCobrar") == null ? 0 : resultSet.getInt("VentasCobrar");
            }
            list.add(ventas_cobrar);

            double compras_pagar = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as ComprasPagar from CompraTB where TipoCompra = 2 and EstadoCompra = 2");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                compras_pagar = resultSet.getObject("ComprasPagar") == null ? 0 : resultSet.getInt("ComprasPagar");
            }
            list.add(compras_pagar);

            // CANTIDADES
            double cantidad_negativas = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Negativos' from SuministroTB where Cantidad <= 0");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                cantidad_negativas = resultSet.getObject("Productos Negativos") == null ? 0 : resultSet.getDouble("Productos Negativos");
            }
            list.add(cantidad_negativas);

            double cantidad_intermedias = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Intermedios' from SuministroTB where Cantidad > 0 and Cantidad < StockMinimo");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                cantidad_intermedias = resultSet.getObject("Productos Intermedios") == null ? 0 : resultSet.getInt("Productos Intermedios");
            }
            list.add(cantidad_intermedias);

            double cantidad_necesarias = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Necesarios' from SuministroTB where Cantidad >= StockMinimo and Cantidad < StockMaximo");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                cantidad_necesarias = resultSet.getObject("Productos Necesarios") == null ? 0 : resultSet.getInt("Productos Necesarios");
            }
            list.add(cantidad_necesarias);

            double cantidad_excedentes = 0;
            preparedGlobal = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Execedentes' from SuministroTB where Cantidad >= StockMaximo");
            resultSet = preparedGlobal.executeQuery();
            if (resultSet.next()) {
                cantidad_excedentes = resultSet.getObject("Productos Execedentes") == null ? 0 : resultSet.getInt("Productos Execedentes");
            }
            list.add(cantidad_excedentes);

        } catch (SQLException ex) {
            System.out.println(ex.getLocalizedMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedGlobal != null) {
                    preparedGlobal.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
            }
        }
        return list;
    }

    public static Object DashboardLoad(String fechaActual) {
        ResultSet resultLista = null;

        try {
            DBUtil.dbConnect();
            ArrayList<Object> arrayList = new ArrayList<>();
            double ventasTotales = 0;
            double comprasTotales = 0;
            double articulos = 0;
            double clientes = 0;
            double proveedores = 0;
            double trabajadores = 0;

            int cantidad_negativas = 0;
            int cantidad_intermedias = 0;
            int cantidad_necesarias = 0;
            int cantidad_excedentes = 0;

            int ventas_cobrar = 0;
            int compras_pagar = 0;

            try (PreparedStatement ptVentas = DBUtil.getConnection().prepareStatement("SELECT\n"
                    + "ISNULL(SUM(dv.Cantidad * (dv.PrecioVenta-dv.Descuento)),0) AS Total\n"
                    + "FROM VentaTB AS v\n"
                    + "LEFT JOIN NotaCreditoTB AS n ON v.IdVenta = n.IdVenta\n"
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = v.IdVenta\n"
                    + "WHERE v.Estado <> 3  AND n.IdNotaCredito IS NULL AND v.FechaVenta = CAST(GETDATE() AS DATE)")) {
                resultLista = ptVentas.executeQuery();
                if (resultLista.next()) {
                    ventasTotales = resultLista.getDouble("Total");
                }
            }

            try (PreparedStatement ptCompras = DBUtil.getConnection().prepareStatement("SELECT\n"
                    + "ISNULL(sum(d.Cantidad*(d.PrecioCompra-d.Descuento)),0) AS Total \n"
                    + "FROM CompraTB AS c \n"
                    + "INNER JOIN DetalleCompraTB AS d ON d.IdCompra = c.IdCompra\n"
                    + "WHERE c.FechaCompra =  CAST(GETDATE() AS DATE)")) {
                resultLista = ptCompras.executeQuery();
                if (resultLista.next()) {
                    comprasTotales = resultLista.getDouble("Total");
                }
            }

            try (PreparedStatement ptCuentasCobrar = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total\n"
                    + "FROM VentaTB WHERE Tipo = 2 AND Estado = 2")) {
                resultLista = ptCuentasCobrar.executeQuery();
                if (resultLista.next()) {
                    ventas_cobrar = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptCuentasPagar = DBUtil.getConnection().prepareStatement("SELECT ISNULL(COUNT(*),0) AS Total\n"
                    + "FROM CompraTB WHERE TipoCompra = 2 AND EstadoCompra = 2")) {
                resultLista = ptCuentasPagar.executeQuery();
                if (resultLista.next()) {
                    compras_pagar = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptProdcuctoTotal = DBUtil.getConnection().prepareStatement("SELECT ISNULL(COUNT(*),0) AS Total FROM SuministroTB")) {
                resultLista = ptProdcuctoTotal.executeQuery();
                if (resultLista.next()) {
                    articulos = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptClienteTotal = DBUtil.getConnection().prepareStatement("SELECT ISNULL(COUNT(*),0) AS Total FROM ClienteTB")) {
                resultLista = ptClienteTotal.executeQuery();
                if (resultLista.next()) {
                    clientes = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptProveedorTotal = DBUtil.getConnection().prepareStatement("SELECT ISNULL(COUNT(*),0) AS Total FROM ProveedorTB")) {
                resultLista = ptProveedorTotal.executeQuery();
                if (resultLista.next()) {
                    proveedores = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptTrabajadoresTotal = DBUtil.getConnection().prepareStatement("SELECT ISNULL(COUNT(*),0) AS Total FROM EmpleadoTB")) {
                resultLista = ptTrabajadoresTotal.executeQuery();
                if (resultLista.next()) {
                    trabajadores = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptPNegativos = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM SuministroTB WHERE Cantidad <= 0")) {
                resultLista = ptPNegativos.executeQuery();
                if (resultLista.next()) {
                    cantidad_negativas = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptPIntermedio = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM SuministroTB WHERE Cantidad > 0 AND Cantidad < StockMinimo")) {
                resultLista = ptPIntermedio.executeQuery();
                if (resultLista.next()) {
                    cantidad_intermedias = resultLista.getInt("Total");
                }
            }

            try (PreparedStatement ptPNecesario = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM SuministroTB WHERE Cantidad >= StockMinimo AND Cantidad < StockMaximo")) {
                resultLista = ptPNecesario.executeQuery();
                if (resultLista.next()) {
                    cantidad_necesarias = resultLista.getInt("Total");
                }
            }

            PreparedStatement ptPExcedentes = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM SuministroTB WHERE Cantidad >= StockMaximo");
            resultLista = ptPExcedentes.executeQuery();
            if (resultLista.next()) {
                cantidad_excedentes = resultLista.getInt("Total");
            }
            ptPExcedentes.close();

            PreparedStatement ptHistorialVentas = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "MONTH(vt.FechaVenta) AS Mes, \n"
                    + "SUM(dv.Cantidad * (dv.PrecioVenta-dv.Descuento)) AS Monto\n"
                    + "FROM VentaTB AS vt \n"
                    + "LEFT JOIN NotaCreditoTB AS nc ON nc.IdVenta = vt.IdVenta\n"
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = vt.IdVenta\n"
                    + "WHERE \n"
                    + "vt.Estado <> 3 AND nc.IdNotaCredito IS NULL AND YEAR(vt.FechaVenta) = YEAR(GETDATE())\n"
                    + "GROUP BY MONTH(vt.FechaVenta)");
            resultLista = ptHistorialVentas.executeQuery();
            JSONArray arrayHVentas = new JSONArray();
            while (resultLista.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("mes", resultLista.getInt("Mes"));
                jsono.put("monto", resultLista.getDouble("Monto"));
                arrayHVentas.add(jsono);
            }
            ptHistorialVentas.close();

            PreparedStatement ptHistorialVentasTipos = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "MONTH(vt.FechaVenta) AS Mes, \n"
                    + "case td.Facturacion when 1 then SUM(dv.Cantidad * (dv.PrecioVenta-dv.Descuento)) else 0 end Sunat,\n"
                    + "case td.Facturacion when 0 then SUM(dv.Cantidad * (dv.PrecioVenta-dv.Descuento)) else 0 end Libre\n"
                    + "FROM VentaTB AS vt \n"
                    + "INNER JOIN TipoDocumentoTB AS td on td.IdTipoDocumento = vt.Comprobante\n"
                    + "LEFT JOIN NotaCreditoTB AS nc ON nc.IdVenta = vt.IdVenta\n"
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = vt.IdVenta\n"
                    + "WHERE \n"
                    + "vt.Estado <> 3 AND nc.IdNotaCredito IS NULL AND YEAR(vt.FechaVenta) = YEAR(GETDATE()) \n"
                    + "or\n"
                    + "vt.Estado <> 3 AND nc.IdNotaCredito IS NULL AND YEAR(vt.FechaVenta) = YEAR(GETDATE()) AND td.Facturacion = 1\n"
                    + "GROUP BY \n"
                    + "MONTH(vt.FechaVenta),\n"
                    + "td.Facturacion");
            resultLista = ptHistorialVentasTipos.executeQuery();
            JSONArray arrayHVentasTipos = new JSONArray();
            while (resultLista.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("mes", resultLista.getInt("Mes"));
                jsono.put("sunat", resultLista.getDouble("Sunat"));
                jsono.put("libre", resultLista.getDouble("Libre"));
                arrayHVentasTipos.add(jsono);
            }
            ptHistorialVentasTipos.close();

            PreparedStatement ptTipoVenta = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "CASE \n"
                    + "WHEN v.Estado <> 2 AND v.Efectivo > 0 AND v.Tarjeta = 0 THEN 'EFECTIVO' \n"
                    + "WHEN v.Estado <> 2 AND v.Tarjeta  > 0 AND v.Efectivo = 0 THEN 'TARJETA'\n"
                    + "WHEN v.Estado <> 2 AND v.Efectivo > 0 AND v.Tarjeta > 0 THEN 'MIXTO'\n"
                    + "WHEN v.Estado <> 2 AND v.Deposito > 0 THEN 'DEPOSITO' \n"
                    + "ELSE 'NINGUNO' END AS FormaName,\n"
                    + "v.Tipo,\n"
                    + "v.Estado,\n"
                    + "v.Efectivo,\n"
                    + "v.Tarjeta,\n"
                    + "v.Deposito,\n"
                    + "sum(dv.Cantidad * (dv.PrecioVenta-dv.Descuento)) AS Total,\n"
                    + "iif(n.IdNotaCredito IS NULL,0,1) AS IdNotaCredito\n"
                    + "FROM VentaTB AS v\n"
                    + "LEFT JOIN NotaCreditoTB AS n ON v.IdVenta = n.IdVenta\n"
                    + "INNER JOIN DetalleVentaTB AS dv ON dv.IdVenta = v.IdVenta\n"
                    + "WHERE MONTH(v.FechaVenta) = MONTH(GETDATE()) AND YEAR(v.FechaVenta) = YEAR(GETDATE())\n"
                    + "GROUP BY\n"
                    + "v.Tipo,\n"
                    + "v.Estado,\n"
                    + "v.Efectivo,\n"
                    + "v.Tarjeta,\n"
                    + "v.Deposito,\n"
                    + "v.Estado,\n"
                    + "v.FechaVenta,\n"
                    + "v.HoraVenta,\n"
                    + "n.IdNotaCredito");
            resultLista = ptTipoVenta.executeQuery();
            JSONArray arrayTipoVenta = new JSONArray();
            while (resultLista.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("FormaName", resultLista.getString("FormaName"));
                jsono.put("Tipo", resultLista.getInt("Tipo"));
                jsono.put("Estado", resultLista.getInt("Estado"));
                jsono.put("Efectivo", resultLista.getDouble("Efectivo"));
                jsono.put("Tarjeta", resultLista.getDouble("Tarjeta"));
                jsono.put("Deposito", resultLista.getDouble("Deposito"));
                jsono.put("Total", resultLista.getDouble("Total"));
                jsono.put("IdNotaCredito", resultLista.getInt("IdNotaCredito"));
                arrayTipoVenta.add(jsono);
            }
            ptTipoVenta.close();

            PreparedStatement ptProductosMasVecesVendidos = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "TOP 10\n"
                    + "s.IdSuministro,\n"
                    + "s.Clave,\n"
                    + "s.NombreMarca,\n"
                    + "isnull(dc.Nombre,'') AS Categoria,\n"
                    + "isnull(dm.Nombre,'') AS Marca,\n"
                    + "isnull(du.Nombre,'') AS Medida,\n"
                    + "count(s.IdSuministro) as Cantidad\n"
                    + "FROM DetalleVentaTB dv \n"
                    + "INNER JOIN VentaTB AS v ON v.IdVenta = dv.IdVenta\n"
                    + "INNER JOIN SuministroTB AS s ON s.IdSuministro = dv.IdArticulo\n"
                    + "LEFT JOIN DetalleTB AS dc ON dc.IdDetalle = s.Categoria AND dc.IdMantenimiento = '0006'\n"
                    + "LEFT JOIN DetalleTB AS dm ON dm.IdDetalle = s.Marca AND dm.IdMantenimiento = '0007'\n"
                    + "LEFT JOIN DetalleTB AS du ON du.IdDetalle = s.UnidadCompra AND du.IdMantenimiento = '0013'\n"
                    + "WHERE MONTH(FechaVenta) = MONTH(GETDATE()) AND YEAR(FechaVenta) = YEAR(GETDATE())\n"
                    + "GROUP BY\n"
                    + "s.IdSuministro,\n"
                    + "s.Clave,\n"
                    + "s.NombreMarca,\n"
                    + "dc.Nombre,\n"
                    + "dm.Nombre,\n"
                    + "du.Nombre\n"
                    + "ORDER BY Cantidad DESC");
            resultLista = ptProductosMasVecesVendidos.executeQuery();
            JSONArray arrayProductosMasVecesVendidos = new JSONArray();
            while (resultLista.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("Clave", resultLista.getString("Clave"));
                jsono.put("NombreMarca", resultLista.getString("NombreMarca"));
                jsono.put("Categoria", resultLista.getString("Categoria"));
                jsono.put("Marca", resultLista.getString("Marca"));
                jsono.put("Medida", resultLista.getString("Medida"));
                jsono.put("Cantidad", resultLista.getString("Cantidad"));
                arrayProductosMasVecesVendidos.add(jsono);
            }
            ptProductosMasVecesVendidos.close();

            PreparedStatement ptProductosMasCantidadesVendidos = DBUtil.getConnection().prepareStatement("SELECT \n"
                    + "TOP 10\n"
                    + "s.IdSuministro,\n"
                    + "s.Clave,\n"
                    + "s.NombreMarca,\n"
                    + "isnull(dc.Nombre,'') AS Categoria,\n"
                    + "isnull(dm.Nombre,'') AS Marca,\n"
                    + "isnull(du.Nombre,'') AS Medida,\n"
                    + "sum(dv.Cantidad) AS Suma\n"
                    + "FROM DetalleVentaTB dv \n"
                    + "INNER JOIN VentaTB AS v ON v.IdVenta = dv.IdVenta\n"
                    + "INNER JOIN SuministroTB as s ON s.IdSuministro = dv.IdArticulo\n"
                    + "LEFT JOIN DetalleTB AS dc ON dc.IdDetalle = s.Categoria AND dc.IdMantenimiento = '0006'\n"
                    + "LEFT JOIN DetalleTB AS dm ON dm.IdDetalle = s.Marca AND dm.IdMantenimiento = '0007'\n"
                    + "LEFT JOIN DetalleTB AS du ON du.IdDetalle = s.UnidadCompra AND du.IdMantenimiento = '0013'\n"
                    + "WHERE MONTH(FechaVenta) = MONTH(GETDATE()) AND YEAR(FechaVenta) = YEAR(GETDATE())\n"
                    + "GROUP BY\n"
                    + "s.IdSuministro,\n"
                    + "s.Clave,\n"
                    + "s.NombreMarca,\n"
                    + "dc.Nombre,\n"
                    + "dm.Nombre,\n"
                    + "du.Nombre\n"
                    + "ORDER BY Suma DESC");
            resultLista = ptProductosMasCantidadesVendidos.executeQuery();
            JSONArray arrayProductosMasCantidadVendidos = new JSONArray();
            while (resultLista.next()) {
                JSONObject jsono = new JSONObject();
                jsono.put("Clave", resultLista.getString("Clave"));
                jsono.put("NombreMarca", resultLista.getString("NombreMarca"));
                jsono.put("Categoria", resultLista.getString("Categoria"));
                jsono.put("Marca", resultLista.getString("Marca"));
                jsono.put("Medida", resultLista.getString("Medida"));
                jsono.put("Suma", resultLista.getString("Suma"));
                arrayProductosMasCantidadVendidos.add(jsono);
            }
            ptProductosMasCantidadesVendidos.close();

            //
            arrayList.add(ventasTotales);
            arrayList.add(comprasTotales);
            arrayList.add(ventas_cobrar);
            arrayList.add(compras_pagar);

            //
            arrayList.add(articulos);
            arrayList.add(clientes);
            arrayList.add(proveedores);
            arrayList.add(trabajadores);

            //CANTIDAD
            arrayList.add(cantidad_negativas);
            arrayList.add(cantidad_intermedias);
            arrayList.add(cantidad_necesarias);
            arrayList.add(cantidad_excedentes);

            arrayList.add(arrayHVentas);
            arrayList.add(arrayHVentasTipos);
            arrayList.add(arrayTipoVenta);

            arrayList.add(arrayProductosMasVecesVendidos);
            arrayList.add(arrayProductosMasCantidadVendidos);

            return arrayList;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (resultLista != null) {
                    resultLista.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String RegistrarInicioPrograma(EmpresaTB empresaTB, MonedaTB monedaTB, EmpleadoTB empleadoTB, ImpuestoTB impuestoTB, TipoDocumentoTB tipoDocumentoTicket, ClienteTB clienteTB, AlmacenTB almacenTB, ProveedorTB proveedorTB) {
        PreparedStatement statementEmpresa = null;
        PreparedStatement statementMoneda = null;
        PreparedStatement statementEmpleado = null;
        PreparedStatement statementImpuesto = null;
        PreparedStatement statementTipoDocumento = null;
        PreparedStatement statementCliente = null;
        PreparedStatement statementAlmacen = null;
        PreparedStatement statementProveedor = null;
        CallableStatement codigoEmpleado = null;
        CallableStatement codigoCliente = null;
        CallableStatement codigoAlmacen = null;
        CallableStatement codigoProveedor = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            statementEmpresa = DBUtil.getConnection().prepareStatement("INSERT INTO EmpresaTB("
                    + "GiroComercial,"
                    + "Nombre,"
                    + "Telefono,"
                    + "Celular,"
                    + "PaginaWeb,"
                    + "Email,"
                    + "Terminos,"
                    + "Condiciones,"
                    + "Domicilio,"
                    + "TipoDocumento,"
                    + "NumeroDocumento,"
                    + "RazonSocial,"
                    + "NombreComercial,"
                    + "Image,"
                    + "Ubigeo,"
                    + "UsuarioSol,"
                    + "ClaveSol,"
                    + "CertificadoRuta,"
                    + "CertificadoClave)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statementEmpresa.setInt(1, empresaTB.getGiroComerial());
            statementEmpresa.setString(2, empresaTB.getNombre());
            statementEmpresa.setString(3, empresaTB.getTelefono());
            statementEmpresa.setString(4, empresaTB.getCelular());
            statementEmpresa.setString(5, empresaTB.getPaginaWeb());
            statementEmpresa.setString(6, empresaTB.getEmail());
            statementEmpresa.setString(7, empresaTB.getTerminos());
            statementEmpresa.setString(8, empresaTB.getCondiciones());
            statementEmpresa.setString(9, empresaTB.getDomicilio());
            statementEmpresa.setInt(10, empresaTB.getTipoDocumento());
            statementEmpresa.setString(11, empresaTB.getNumeroDocumento());
            statementEmpresa.setString(12, empresaTB.getRazonSocial());
            statementEmpresa.setString(13, empresaTB.getNombreComercial());
            statementEmpresa.setBytes(14, empresaTB.getImage());
            statementEmpresa.setInt(15, empresaTB.getIdUbigeo());
            statementEmpresa.setString(16, empresaTB.getUsuarioSol());
            statementEmpresa.setString(17, empresaTB.getClaveSol());
            statementEmpresa.setString(18, empresaTB.getCertificadoRuta());
            statementEmpresa.setString(19, empresaTB.getCertificadoClave());
            statementEmpresa.addBatch();

            statementMoneda = DBUtil.getConnection().prepareStatement("INSERT INTO MonedaTB("
                    + "Nombre,"
                    + "Abreviado,"
                    + "Simbolo,"
                    + "TipoCambio,"
                    + "Predeterminado,"
                    + "Sistema)VALUES(?,?,?,?,?,?)");
            statementMoneda.setString(1, monedaTB.getNombre());
            statementMoneda.setString(2, monedaTB.getAbreviado());
            statementMoneda.setString(3, monedaTB.getSimbolo());
            statementMoneda.setDouble(4, monedaTB.getTipoCambio());
            statementMoneda.setBoolean(5, monedaTB.isPredeterminado());
            statementMoneda.setBoolean(6, monedaTB.getSistema());
            statementMoneda.addBatch();

            codigoEmpleado = DBUtil.getConnection().prepareCall("{? = call Fc_Empleado_Codigo_Alfanumerico()}");
            codigoEmpleado.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoEmpleado.execute();
            String idEmpleado = codigoEmpleado.getString(1);

            statementEmpleado = DBUtil.getConnection().prepareStatement("INSERT INTO EmpleadoTB"
                    + "           (IdEmpleado"
                    + "           ,TipoDocumento"
                    + "           ,NumeroDocumento"
                    + "           ,Apellidos"
                    + "           ,Nombres"
                    + "           ,Sexo"
                    + "           ,FechaNacimiento"
                    + "           ,Puesto"
                    + "           ,Rol"
                    + "           ,Estado"
                    + "           ,Telefono"
                    + "           ,Celular"
                    + "           ,Email"
                    + "           ,Direccion"
                    + "           ,Usuario"
                    + "           ,Clave"
                    + "           ,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            statementEmpleado.setString(1, idEmpleado);
            statementEmpleado.setInt(2, empleadoTB.getTipoDocumento());
            statementEmpleado.setString(3, empleadoTB.getNumeroDocumento());
            statementEmpleado.setString(4, empleadoTB.getApellidos());
            statementEmpleado.setString(5, empleadoTB.getNombres());
            statementEmpleado.setInt(6, 0);
            statementEmpleado.setDate(7, Tools.getCurrentDate());
            statementEmpleado.setInt(8, 1);
            statementEmpleado.setInt(9, 1);
            statementEmpleado.setInt(10, 1);
            statementEmpleado.setString(11, "");
            statementEmpleado.setString(12, "");
            statementEmpleado.setString(13, "");
            statementEmpleado.setString(14, "");
            statementEmpleado.setString(15, empleadoTB.getUsuario());
            statementEmpleado.setString(16, empleadoTB.getClave());
            statementEmpleado.setBoolean(17, true);
            statementEmpleado.addBatch();

            statementImpuesto = DBUtil.getConnection().prepareStatement("INSERT INTO ImpuestoTB(Operacion,Nombre,Valor,Codigo,Numeracion,NombreImpuesto,Letra,Categoria,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?)");
            statementImpuesto.setInt(1, impuestoTB.getOperacion());
            statementImpuesto.setString(2, impuestoTB.getNombre());
            statementImpuesto.setDouble(3, impuestoTB.getValor());
            statementImpuesto.setString(4, impuestoTB.getCodigo());
            statementImpuesto.setString(5, impuestoTB.getNumeracion());
            statementImpuesto.setString(6, impuestoTB.getNombreImpuesto());
            statementImpuesto.setString(7, impuestoTB.getLetra());
            statementImpuesto.setString(8, impuestoTB.getCategoria());
            statementImpuesto.setBoolean(9, impuestoTB.isPredeterminado());
            statementImpuesto.setBoolean(10, impuestoTB.isSistema());
            statementImpuesto.addBatch();

            statementTipoDocumento = DBUtil.getConnection().prepareStatement("INSERT INTO TipoDocumentoTB(Nombre,Serie,Numeracion,CodigoAlterno,Facturacion,Predeterminado,Sistema,Guia,NotaCredito,Estado,Campo,NumeroCampo)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            statementTipoDocumento.setString(1, tipoDocumentoTicket.getNombre());
            statementTipoDocumento.setString(2, tipoDocumentoTicket.getSerie());
            statementTipoDocumento.setInt(3, tipoDocumentoTicket.getNumeracion());
            statementTipoDocumento.setString(4, tipoDocumentoTicket.getCodigoAlterno());
            statementTipoDocumento.setBoolean(5, tipoDocumentoTicket.isFactura());
            statementTipoDocumento.setBoolean(6, tipoDocumentoTicket.isPredeterminado());
            statementTipoDocumento.setBoolean(7, tipoDocumentoTicket.isSistema());
            statementTipoDocumento.setBoolean(8, tipoDocumentoTicket.isGuia());
            statementTipoDocumento.setBoolean(9, tipoDocumentoTicket.isNotaCredito());
            statementTipoDocumento.setBoolean(10, tipoDocumentoTicket.isEstado());
            statementTipoDocumento.setBoolean(11, tipoDocumentoTicket.isCampo());
            statementTipoDocumento.setInt(12, tipoDocumentoTicket.getNumeroCampo());
            statementTipoDocumento.addBatch();

            codigoCliente = DBUtil.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
            codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoCliente.execute();
            String idCliente = codigoCliente.getString(1);

            statementCliente = DBUtil.getConnection().prepareStatement("INSERT INTO ClienteTB(IdCliente,TipoDocumento,NumeroDocumento,Informacion,Telefono,Celular,Email,Direccion,Representante,Estado,Predeterminado,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            statementCliente.setString(1, idCliente);
            statementCliente.setInt(2, clienteTB.getTipoDocumento());
            statementCliente.setString(3, clienteTB.getNumeroDocumento());
            statementCliente.setString(4, clienteTB.getInformacion());
            statementCliente.setString(5, clienteTB.getTelefono());
            statementCliente.setString(6, clienteTB.getCelular());
            statementCliente.setString(7, clienteTB.getEmail());
            statementCliente.setString(8, clienteTB.getDireccion());
            statementCliente.setString(9, clienteTB.getRepresentante());
            statementCliente.setInt(10, clienteTB.getEstado());
            statementCliente.setBoolean(11, clienteTB.isPredeterminado());
            statementCliente.setBoolean(12, clienteTB.isSistema());
            statementCliente.addBatch();

            codigoAlmacen = DBUtil.getConnection().prepareCall("{? = call Fc_Almacen_Codigo_Numerico()}");
            codigoAlmacen.registerOutParameter(1, java.sql.Types.INTEGER);
            codigoAlmacen.execute();
            int idAlmacen = codigoAlmacen.getInt(1);

            statementAlmacen = DBUtil.getConnection().prepareStatement("INSERT INTO AlmacenTB(IdAlmacen,Nombre,IdUbigeo,Direccion,Fecha,Hora,IdUsuario) VALUES(?,?,?,?,?,?,?)");
            statementAlmacen.setInt(1, idAlmacen);
            statementAlmacen.setString(2, almacenTB.getNombre());
            statementAlmacen.setInt(3, almacenTB.getIdUbigeo());
            statementAlmacen.setString(4, almacenTB.getDireccion());
            statementAlmacen.setString(5, almacenTB.getFecha());
            statementAlmacen.setString(6, almacenTB.getHora());
            statementAlmacen.setString(7, almacenTB.getIdUsuario());
            statementAlmacen.addBatch();

            codigoProveedor = DBUtil.getConnection().prepareCall("{? = call Fc_Proveedor_Codigo_Alfanumerico()}");
            codigoProveedor.registerOutParameter(1, java.sql.Types.VARCHAR);
            codigoProveedor.execute();
            String idProveedor = codigoProveedor.getString(1);

            statementProveedor = DBUtil.getConnection().prepareCall("INSERT INTO "
                    + "ProveedorTB("
                    + "IdProveedor,"
                    + "TipoDocumento,"
                    + "NumeroDocumento,"
                    + "RazonSocial,"
                    + "NombreComercial,"
                    + "Ambito,"
                    + "Estado,"
                    + "Telefono,"
                    + "Celular,"
                    + "Email,"
                    + "PaginaWeb,"
                    + "Direccion,"
                    + "UsuarioRegistro,"
                    + "FechaRegistro,"
                    + "Representante)"
                    + "values(?,?,?,UPPER(?),UPPER(?),?,?,?,?,?,?,?,?,?,?)");
            statementProveedor.setString(1, idProveedor);
            statementProveedor.setInt(2, proveedorTB.getTipoDocumento());
            statementProveedor.setString(3, proveedorTB.getNumeroDocumento());
            statementProveedor.setString(4, proveedorTB.getRazonSocial());
            statementProveedor.setString(5, proveedorTB.getNombreComercial());
            statementProveedor.setInt(6, proveedorTB.getAmbito());
            statementProveedor.setInt(7, proveedorTB.getEstado());
            statementProveedor.setString(8, proveedorTB.getTelefono());
            statementProveedor.setString(9, proveedorTB.getCelular());
            statementProveedor.setString(10, proveedorTB.getEmail());
            statementProveedor.setString(11, proveedorTB.getPaginaWeb());
            statementProveedor.setString(12, proveedorTB.getDireccion());
            statementProveedor.setString(13, "");
            statementProveedor.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            statementProveedor.setString(15, proveedorTB.getRepresentante());
            statementProveedor.addBatch();

            statementEmpresa.executeBatch();
            statementMoneda.executeBatch();
            statementEmpleado.executeBatch();
            statementImpuesto.executeBatch();
            statementTipoDocumento.executeBatch();
            statementAlmacen.executeBatch();
            statementCliente.executeBatch();
            statementProveedor.executeBatch();
            DBUtil.getConnection().commit();

            return "inserted";
        } catch (SQLException | ParseException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementEmpresa != null) {
                    statementEmpresa.close();
                }
                if (statementMoneda != null) {
                    statementMoneda.close();
                }
                if (statementEmpleado != null) {
                    statementEmpleado.close();
                }
                if (statementProveedor != null) {
                    statementProveedor.close();
                }
                if (statementImpuesto != null) {
                    statementImpuesto.close();
                }
                if (statementTipoDocumento != null) {
                    statementTipoDocumento.close();
                }
                if (codigoEmpleado != null) {
                    codigoEmpleado.close();
                }
                if (statementCliente != null) {
                    statementCliente.close();
                }
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (codigoAlmacen != null) {
                    codigoAlmacen.close();
                }
                if (codigoProveedor != null) {
                    codigoProveedor.close();
                }
                if (statementAlmacen != null) {
                    statementAlmacen.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
}
