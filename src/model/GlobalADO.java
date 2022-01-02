package model;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

public class GlobalADO {

    public static ArrayList<Double> ReporteGlobal(String fechaInicial, String fechaFinal) {
        PreparedStatement preparedGlobal = null;
        ResultSet resultSet = null;
        ArrayList<Double> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
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
        }

        return list;
    }

    public static ArrayList<Object> DashboardLoad(String fechaActual) {
        PreparedStatement preparedLista = null;
        ResultSet resultLista = null;
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

        ArrayList<SuministroTB> listaProductos = new ArrayList<>();

        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            try {
                preparedLista = DBUtil.getConnection().prepareStatement("SELECT ISNULL(sum(dv.Cantidad*(dv.PrecioVenta-dv.Descuento)),0) AS Total FROM VentaTB as v INNER JOIN DetalleVentaTB as dv on dv.IdVenta = v.IdVenta LEFT JOIN NotaCreditoTB as nc on nc.IdVenta = v.IdVenta WHERE CAST(v.FechaVenta AS DATE) = ? AND v.Tipo = 1 AND v.Estado <> 3 AND nc.IdNotaCredito IS NULL");
                preparedLista.setString(1, fechaActual);
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    ventasTotales = resultLista.getDouble("Total");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT SUM(d.Importe) AS Total FROM CompraTB as c inner join DetalleCompraTB as d on d.IdCompra = c.IdCompra where c.FechaCompra = ? and c.EstadoCompra = 1");
                preparedLista.setString(1, fechaActual);
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    comprasTotales = resultLista.getDouble("Total");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM SuministroTB");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    articulos = resultLista.getDouble("Total");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM ClienteTB");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    clientes = resultLista.getDouble("Total");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM ProveedorTB");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    proveedores = resultLista.getDouble("Total");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT COUNT(*) AS Total FROM EmpleadoTB");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    trabajadores = resultLista.getDouble("Total");
                }

                // CANTIDADES
                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Negativos' from SuministroTB where Cantidad <= 0");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    cantidad_negativas = resultLista.getInt("Productos Negativos");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Intermedios' from SuministroTB where Cantidad > 0 and Cantidad < StockMinimo");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    cantidad_intermedias = resultLista.getInt("Productos Intermedios");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Necesarios' from SuministroTB where Cantidad >= StockMinimo and Cantidad < StockMaximo");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    cantidad_necesarias = resultLista.getInt("Productos Necesarios");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as 'Productos Execedentes' from SuministroTB where Cantidad >= StockMaximo");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    cantidad_excedentes = resultLista.getInt("Productos Execedentes");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("SELECT TOP 10 dt.IdArticulo, s.NombreMarca, s.NuevaImagen as NuevaImagen,dt.PrecioVenta, SUM(dt.Cantidad) as Cantidad\n"
                        + "from DetalleVentaTB as dt \n"
                        + "inner join VentaTB as v on v.IdVenta=dt.IdVenta\n"
                        + "inner join SuministroTB as s on dt.IdArticulo=s.IdSuministro \n"
                        + "where MONTH(v.FechaVenta) = MONTH(GETDATE()) AND YEAR(v.FechaVenta) = YEAR(GETDATE())\n"
                        + "group by dt.IdArticulo, s.NombreMarca, NuevaImagen,dt.PrecioVenta  \n"
                        + "order by Cantidad DESC");
                resultLista = preparedLista.executeQuery();
                while (resultLista.next()) {

                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setNombreMarca(resultLista.getString("NombreMarca"));
                    suministroTB.setPrecioVentaGeneral(resultLista.getDouble("PrecioVenta"));
                    suministroTB.setCantidad(resultLista.getDouble("Cantidad"));
                    suministroTB.setNuevaImagen(resultLista.getBytes("NuevaImagen"));
                    listaProductos.add(suministroTB);
                }

                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as VentasCobrar from VentaTB as v left join NotaCreditoTB as nc on nc.IdVenta = v.IdVenta where v.Tipo = 2 and v.Estado = 2 and nc.IdNotaCredito is null");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    ventas_cobrar = resultLista.getInt("VentasCobrar");
                }

                preparedLista = DBUtil.getConnection().prepareStatement("select COUNT(*) as ComprasPagar from CompraTB where TipoCompra = 2 and EstadoCompra = 2");
                resultLista = preparedLista.executeQuery();
                if (resultLista.next()) {
                    compras_pagar = resultLista.getInt("ComprasPagar");
                }

                arrayList.add(Tools.roundingValue(ventasTotales, 2));
                arrayList.add(Tools.roundingValue(comprasTotales, 2));
                arrayList.add(Tools.roundingValue(articulos, 0));
                arrayList.add(Tools.roundingValue(clientes, 0));
                arrayList.add(Tools.roundingValue(proveedores, 0));
                arrayList.add(Tools.roundingValue(trabajadores, 0));

                //CANTIDAD
                arrayList.add(cantidad_negativas);
                arrayList.add(cantidad_intermedias);
                arrayList.add(cantidad_necesarias);
                arrayList.add(cantidad_excedentes);

                //PRODUCTOS MAS VENDIDOS
                arrayList.add(listaProductos);

                //VENTAS Y COMPRAR
                arrayList.add(ventas_cobrar);
                arrayList.add(compras_pagar);
            } catch (SQLException ex) {
                Tools.println(ex.getLocalizedMessage());
                arrayList.add(Tools.roundingValue(ventasTotales, 2));
                arrayList.add(Tools.roundingValue(comprasTotales, 2));
                arrayList.add(Tools.roundingValue(articulos, 0));
                arrayList.add(Tools.roundingValue(clientes, 0));
                arrayList.add(Tools.roundingValue(proveedores, 0));
                arrayList.add(Tools.roundingValue(trabajadores, 0));

                //CANTIDAD
                arrayList.add(cantidad_negativas);
                arrayList.add(cantidad_intermedias);
                arrayList.add(cantidad_necesarias);
                arrayList.add(cantidad_excedentes);

                //PRODUCTOS MAS VENDIDOS
                arrayList.add(listaProductos);

                //VENTAS Y COMPRAR
                arrayList.add(ventas_cobrar);
                arrayList.add(compras_pagar);
            } finally {
                try {
                    if (preparedLista != null) {
                        preparedLista.close();
                    }
                    if (resultLista != null) {
                        resultLista.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        } else {
            arrayList.add(Tools.roundingValue(ventasTotales, 2));
            arrayList.add(Tools.roundingValue(comprasTotales, 2));
            arrayList.add(Tools.roundingValue(articulos, 0));
            arrayList.add(Tools.roundingValue(clientes, 0));
            arrayList.add(Tools.roundingValue(proveedores, 0));
            arrayList.add(Tools.roundingValue(trabajadores, 0));
        }
        return arrayList;
    }

    public static String RegistrarInicioPrograma(EmpresaTB empresaTB, MonedaTB monedaTB, EmpleadoTB empleadoTB, ImpuestoTB impuestoTB, TipoDocumentoTB tipoDocumentoTicket, ClienteTB clienteTB, AlmacenTB almacenTB) {
        PreparedStatement statementEmpresa = null;
        PreparedStatement statementMoneda = null;
        PreparedStatement statementEmpleado = null;
        PreparedStatement statementImpuesto = null;
        PreparedStatement statementTipoDocumentoTicket = null;
        PreparedStatement statementCliente = null;
        PreparedStatement statementAlmacen = null;
        CallableStatement codigoEmpleado = null;
        CallableStatement codigoCliente = null;
        CallableStatement statementCodigoAlmacen = null;

        String result = "";
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
            statementMoneda.setBoolean(5, monedaTB.getPredeterminado());
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
                    + "           ,Pais"
                    + "           ,Ciudad"
                    + "           ,Provincia"
                    + "           ,Distrito"
                    + "           ,Usuario"
                    + "           ,Clave"
                    + "           ,Sistema)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

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
            statementEmpleado.setString(15, "");
            statementEmpleado.setInt(16, 0);
            statementEmpleado.setInt(17, 0);
            statementEmpleado.setInt(18, 0);
            statementEmpleado.setString(19, empleadoTB.getUsuario());
            statementEmpleado.setString(20, empleadoTB.getClave());
            statementEmpleado.setBoolean(21, true);
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

            statementTipoDocumentoTicket = DBUtil.getConnection().prepareStatement("INSERT INTO TipoDocumentoTB(Nombre,Serie,Numeracion,CodigoAlterno,Facturacion,Predeterminado,Sistema,Guia,NotaCredito,Estado,Campo,NumeroCampo)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
            statementTipoDocumentoTicket.setString(1, tipoDocumentoTicket.getNombre());
            statementTipoDocumentoTicket.setString(2, tipoDocumentoTicket.getSerie());
            statementTipoDocumentoTicket.setInt(3, tipoDocumentoTicket.getNumeracion());
            statementTipoDocumentoTicket.setString(4, tipoDocumentoTicket.getCodigoAlterno());
            statementTipoDocumentoTicket.setBoolean(5, tipoDocumentoTicket.isFactura());
            statementTipoDocumentoTicket.setBoolean(6, tipoDocumentoTicket.isPredeterminado());
            statementTipoDocumentoTicket.setBoolean(7, tipoDocumentoTicket.isSistema());
            statementTipoDocumentoTicket.setBoolean(8, tipoDocumentoTicket.isGuia());
            statementTipoDocumentoTicket.setBoolean(9, tipoDocumentoTicket.isNotaCredito());
            statementTipoDocumentoTicket.setBoolean(10, tipoDocumentoTicket.isEstado());
            statementTipoDocumentoTicket.setBoolean(11, tipoDocumentoTicket.isCampo());
            statementTipoDocumentoTicket.setInt(12, tipoDocumentoTicket.getNumeroCampo());
            statementTipoDocumentoTicket.addBatch();

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

            statementCodigoAlmacen = DBUtil.getConnection().prepareCall("{? = call Fc_Almacen_Codigo_Numerico()}");
            statementCodigoAlmacen.registerOutParameter(1, java.sql.Types.INTEGER);
            statementCodigoAlmacen.execute();
            int idAlmacen = statementCodigoAlmacen.getInt(1);

            statementAlmacen = DBUtil.getConnection().prepareStatement("INSERT INTO AlmacenTB(IdAlmacen,Nombre,IdUbigeo,Direccion,Fecha,Hora,IdUsuario) VALUES(?,?,?,?,?,?,?)");
            statementAlmacen.setInt(1, idAlmacen);
            statementAlmacen.setString(2, almacenTB.getNombre());
            statementAlmacen.setInt(3, almacenTB.getIdUbigeo());
            statementAlmacen.setString(4, almacenTB.getDireccion());
            statementAlmacen.setString(5, almacenTB.getFecha());
            statementAlmacen.setString(6, almacenTB.getHora());
            statementAlmacen.setString(7, almacenTB.getIdUsuario());
            statementAlmacen.addBatch();

            statementEmpresa.executeBatch();
            statementMoneda.executeBatch();
            statementEmpleado.executeBatch();
            statementImpuesto.executeBatch();
            statementTipoDocumentoTicket.executeBatch();
            statementAlmacen.executeBatch();
            statementCliente.executeBatch();
            DBUtil.getConnection().commit();
            result = "inserted";
        } catch (SQLException | ParseException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
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
                if (statementImpuesto != null) {
                    statementImpuesto.close();
                }
                if (statementTipoDocumentoTicket != null) {
                    statementTipoDocumentoTicket.close();
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
                if (statementCodigoAlmacen != null) {
                    statementCodigoAlmacen.close();
                }
                if (statementAlmacen != null) {
                    statementAlmacen.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return result;
    }
}
