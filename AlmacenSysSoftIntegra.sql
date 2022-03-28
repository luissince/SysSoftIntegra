use [SysSoftIntegra]
go


truncate table AlmacenTB
truncate table Banco
truncate table BancoHistorialTB
truncate table [dbo].[CajaTB]
truncate table [dbo].[CantidadTB]
truncate table [dbo].[ClienteTB]
truncate table [dbo].[CompraCreditoTB]
truncate table [dbo].[CompraTB]
truncate table [dbo].[ComprobanteTB]
truncate table [dbo].[CotizacionTB]
truncate table [dbo].[DetalleCompraTB]
truncate table [dbo].[DetalleCotizacionTB]
truncate table [dbo].[DetalleVentaTB]
truncate table [dbo].[EmpleadoTB]
truncate table [dbo].[EmpresaTB]
truncate table [dbo].[EtiquetaTB]
truncate table [dbo].[FormulaDetalleTB]
truncate table [dbo].[FormulaTB]
truncate table [dbo].[GuiaRemisionDetalleTB]
truncate table [dbo].[GuiaRemisionTB]
truncate table [dbo].[HistorialSuministroLlevar]
truncate table [dbo].[ImagenTB]
truncate table [dbo].[ImpuestoTB]
truncate table [dbo].[IngresoTB]
truncate table [dbo].[KardexSuministroTB]
truncate table [dbo].[LoteTB]
truncate table [dbo].[MermaDetalleTB]
truncate table [dbo].[MermaTB]
truncate table [dbo].[MonedaTB]
truncate table [dbo].[MovimientoCajaTB]
truncate table [dbo].[MovimientoInventarioDetalleTB]
truncate table [dbo].[MovimientoInventarioTB]
truncate table [dbo].[NotaCreditoDetalleTB]
truncate table [dbo].[NotaCreditoTB]
truncate table [dbo].[PlazosTB]
truncate table [dbo].[PreciosTB]
truncate table [dbo].[ProduccionDetalleTB]
truncate table [dbo].[ProduccionHistorialTB]
truncate table [dbo].[ProduccionTB]
truncate table [dbo].[ProveedorTB]
truncate table [dbo].[SuministroTB]
truncate table [dbo].[TicketTB]
truncate table [dbo].[TipoDocumentoTB]
truncate table [dbo].[TrasladoHistorialTB]
truncate table [dbo].[TrasladoTB]
truncate table [dbo].[VentaCreditoTB]
truncate table [dbo].[VentaTB]
truncate table [dbo].[OrdenCompraTB]
truncate table [dbo].[OrdenCompraDatalleTB]
go


----------------------------------------------------------------------------------------
--																					  --
----------------------------------------------------------------------------------------

truncate table [dbo].[Banco]
truncate table [dbo].[BancoHistorialTB]
truncate table [dbo].[CajaTB]
truncate table [dbo].[CompraCreditoTB]
truncate table [dbo].[CompraTB]
truncate table [dbo].[ComprobanteTB]
truncate table [dbo].[CotizacionTB]
truncate table [dbo].[DetalleCompraTB]
truncate table [dbo].[DetalleCotizacionTB]
truncate table [dbo].[DetalleVentaTB]
truncate table [dbo].[FormulaDetalleTB]
truncate table [dbo].[FormulaTB]
truncate table [dbo].[GuiaRemisionDetalleTB]
truncate table [dbo].[GuiaRemisionTB]
truncate table [dbo].[ImagenTB]
truncate table [dbo].[IngresoTB]
truncate table [dbo].[KardexSuministroTB]
truncate table [dbo].[LoteTB]
truncate table [dbo].[MermaDetalleTB]
truncate table [dbo].[MermaTB]
truncate table [dbo].[MovimientoCajaTB]
truncate table [dbo].[MovimientoInventarioDetalleTB]
truncate table [dbo].[MovimientoInventarioTB]
truncate table [dbo].[NotaCreditoDetalleTB]
truncate table [dbo].[NotaCreditoTB]
truncate table [dbo].[OrdenCompraDatalleTB]
truncate table [dbo].[OrdenCompraTB]
truncate table [dbo].[PedidoDetalleTB]
truncate table [dbo].[PedidoTB]
truncate table [dbo].[PlazosTB]
truncate table [dbo].[ProduccionDetalleTB]
truncate table [dbo].[ProduccionHistorialTB]
truncate table [dbo].[ProduccionTB]
truncate table [dbo].[TrasladoHistorialTB]
truncate table [dbo].[TrasladoTB]
truncate table [dbo].[VentaCreditoTB]
truncate table [dbo].[VentaTB]
update SuministroTB set Cantidad = 0
update CantidadTB set Cantidad = 0
go

select * from Banco
go

/*
create table AlmacenTB(
	IdAlmacen int identity,
	Nombre varchar(100),
	IdUbigeo int,
	Direccion varchar(max),
	Fecha date,
	Hora time,
	IdUsuario varchar(12),
	primary key(IdAlmacen)
)
go


create table CantidadTB(
	IdAlmacen int,
	IdSuministro varchar(12),
	StockMinimo float,
	StockMaximo float,
	Cantidad float,
	primary key(IdAlmacen,IdSuministro)
)

alter table CompraTB
add column IdAlmacen int

alter table KardexSuministroTB
add column IdAlmacen int

alter table CompraTB
add column IdAlmacen int

create table TrasladoTB(
	IdTraslado varchar(12) primary key,
	Fecha date,
	Hora time,
	Observacion varchar(max),
	Estado tinyint,
	IdUsuario varchar(12)
)
go

CREATE TABLE [dbo].[TrasladoHistorialTB](
	[IdTraslado] [varchar](12) NOT NULL,
	[IdSuministro] [varchar](12) NOT NULL,
	[IdAlmacenOrigen] [int] NULL,
	[IdAlmacenDestino] [int] NULL,
	[Cantidad] [float] NULL,
PRIMARY KEY CLUSTERED 
(
	[IdTraslado] ASC,
	[IdSuministro] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO


AGREGAR A LA TABLA TipoDocumentoTB
Estado BIT


CAMBIAR LA TABLA ALMACEN
IdAlmacen int no autoincremental
0 ALMACEN PRINCIPAL 1002 AV. LAS PERRAS DEL MAR 2021-08-31 09:43:57 EM0001

table GuiaRemisionTB
add campo
IdVenta varchar(12)


tabla SuministroTB
agregar campos
add Libre boolean
add Venta boolean
add Insumo boolean
nuevo cambios anular esos campos
 
tabla EmpresaTB
agregar el campo
ImagenRuta varchar(max)

alter table ComprobanteTB
IdTipoDocumento primary key
go

tabla ImagenTB
agregar el campo
Ruta varchar(max)

tabla KardexSuministroTB
agregar el campo
IdEmpleado varchar(12)

tabla TipoDocumentoTB
agregar el campo
Campo bit
NumeroCampo smallint

tabla EmpleadoTB
agregar el campo
Huella varchar(max)

tabla TrasladoTB
agregar el campo
Tipo tinyint

tabla VentaTB
agregar el campo
Deposito float
NumeroOperacion varchar(300)

tabla TrasladoTB
agregar campos
Correlativo int
Numeracion int

tabla ProduccionTB
agregar campo
IdPedido varchar(12)
Correlativo int

tabla CompraTB
agregar campos
Efectivo bit
Tarjeta bit
Deposito bit

tabla ProduccionDetalleTB
agregar campo
Medida float

tabla MermaDetalleTB
agregar campo
Medida float

tabla ProduccionTB
agregar campo
CantidadVariable bit

tabla [dbo].[PedidoTB]
cambiar campo IdProveedor a
IdCliente varchar(12)
agregar campo
Estado tinyint
quitar el campo
IdFormaPago int

tabla [dbo].[PedidoDetalleTB]
quitar todos los campo y dejar
[IdPedido] float
[IdSuministro] varchar(12)
[Cantidad] float

Eliminar la Tabla [dbo].[PedidoTB]

tabla [dbo].[ProduccionTB]
quitar el campo idPedido

tabla [dbo].[TrasladoHistorialTB]
quitar los campos IdAlmacenOrigen IdAlmacenDestino

tabla TrasladoTB
agregar campo
IdAlmacen int

tabla EmpleadoTB
cambiar campo
Huella varchar(max)

tabla IngresoTB
agregar campo
IdCliente varchar(12)

tabla NotaCreditoDetalleTB
agregar campo
IdImpuesto int

table ProduccionTB
quitar campo
IdMedidaProducir int

tabla MantenimientoTB
cambiar campo
codigo 0020
MEDIDA A PRODUCTIR
A
TIPO MERMA

table MermaDetalleTB
agregar campo
TipoMerma int
Reusado bit

tabla EmpresaTB
agregar campo
Terminos varchar(max),
Condiciones varchar(max),

table Banco
agregar campo
Mostrar bit

table SuministroTB
agregar campo
Descripcion varchar(max)

table ProveedorTB
quitar campos
[Pais]
[Ciudad]
[Provincia]
[Distrito] 

TABLA [dbo].[TipoTicketTB]
agregar campo
14 ORDEN DE COMPRA

TABLA [dbo].[DetalleCotizacionTB]
agregar campo
IdMedida int

TABLA [dbo].[VentaTB]
quitar campos
[SubTotal]
[Descuento]
[Impuesto]
[Total]

TABLA [dbo].[DetalleVentaTB]
quitar campos
[Importe]

TABLA [dbo].[CotizacionTB]
agregar campo
IdVenta varchar(12)

crear tabla PedidoTB

create table PedidoTB(
	IdPedido varchar(12) primary key,
	IdCliente varchar(12),
	IdVendedor varchar(12),
	IdMoneda int,
	FechaPedido date,
	HoraPedido time,
	FechaVencimiento date,
	HoraVencimiento time,
	Estado tinyint,
	Observacion varchar(300)
)
go

create table PedidoDetalleTB(
	IdPedido varchar(12),
	IdSuministro varchar(12),
	Cantidad float,
	Precio float,
	Descuento float

	primary key(IdPedido,IdSuministro)
)
go

crear tabla OrdenCompraTB

create table OrdenCompraTB(
	IdOrdenCompra varchar(12) primary key,
	Numeracion int,
	IdProveedor varchar(12),
	IdEmpledo varchar(12),
	FechaRegistro date,
	HoraRegistro time,
	FechaVencimiento date,
	HoraVencimiento time,
	Observacion varchar(max)
)
go

create table OrdenCompraDatalleTB(
	IdOrdenCompra varchar(12),
	IdSuministro varchar(12),
	Cantidad float,
	Descuento float,
	Costo float,
	IdImpuesto int,
	Observacion varchar(max),
)
go

tabla MovimientoInventarioTB
agregar campo IdAlmacen int

tabla TipoTicketTB
agregar datos
15 NOTA DE CREDITO

tabla NotaCreditoDetalleTB
agregar campo
IdImpuesto int

tabla EmpleadoTB
quitar campo
Pais
Ciudad
Provincia
Distrito
cambiar campo
Huella varchar(max)


tabla CompraTB
usar campo
Comprobante int

tabla MantenimientoTB
la fila 0009
cambiar el nomre
ESTADO DE LA VENTA

tabla MantenimientoTB
la fila 0015
cambiar nombre
COMPROBANTE DE COMPRA
estado 1


tabla DetalleVentaTB
agregar campo
IdMedida int


tabla VentaTB
agregar campo
TipoCredito tinyint

tabla OrdenCompraTB
agregar campo
IdMoneda int

tabla Moneda
cambiar tipo de campo
TipoCambio float

tabla OrdenCompraTB
agregar campo
TipoCambio float
*/


/*
declare @procName varchar(500)
declare cur cursor 

for SELECT 'DROP PROCEDURE [' + SCHEMA_NAME(p.schema_id) + '].[' + p.NAME + ']'
FROM sys.procedures p 
open cur
fetch next from cur into @procName
while @@fetch_status = 0
begin
    exec( @procName )
	print @procName
    fetch next from cur into @procName
end
close cur
deallocate cur

for SELECT 'DROP FUNCTION [' + SCHEMA_NAME(p.schema_id) + '].[' + p.NAME + ']'
FROM sys.objects p where name like 'Fc%'
open cur
fetch next from cur into @procName
while @@fetch_status = 0
begin
    exec( @procName )
	print @procName
    fetch next from cur into @procName
end
close cur
deallocate cur
*/


print YEAR(DATEADD(YEAR,-1,GETDATE()))
GO

/*4557 8805 2514 8791 */


SELECT TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
ORDER BY TABLE_NAME ASC
GO

SELECT COLUMN_NAME,DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dbo'
AND TABLE_NAME = 'AlmacenTB'
ORDER BY ORDINAL_POSITION
go

select * from MonedaTB
go

select * from CompraTB
go

select * from OrdenCompraTB
go