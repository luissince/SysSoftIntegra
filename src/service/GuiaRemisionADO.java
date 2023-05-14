package service;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import model.ClienteTB;
import model.ConductorTB;
import model.DetalleTB;
import model.GuiaRemisionDetalleTB;
import model.GuiaRemisionTB;
import model.ModalidadTrasladoTB;
import model.TipoDocumentoTB;
import model.UbigeoTB;
import model.VehiculoTB;
import model.VentaTB;

public class GuiaRemisionADO {

    public static String registrarGuiaRemision(GuiaRemisionTB guiaRemisionTB) {
        DBUtil dbf = new DBUtil();

        CallableStatement serie_numeracion = null;
        CallableStatement statementIdGuiaRemision = null;
        PreparedStatement statementGuiaRemision = null;
        PreparedStatement statementGuiaRemisionDetalle = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            serie_numeracion = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_GuiaRemision(?)}");
            serie_numeracion.registerOutParameter(1, java.sql.Types.VARCHAR);
            serie_numeracion.setInt(2, guiaRemisionTB.getIdComprobante());
            serie_numeracion.execute();
            String[] id_comprabante = serie_numeracion.getString(1).split("-");

            statementIdGuiaRemision = dbf.getConnection()
                    .prepareCall("{? = call Fc_GuiaRemision_Codigo_Alfanumerico()}");
            statementIdGuiaRemision.registerOutParameter(1, java.sql.Types.VARCHAR);
            statementIdGuiaRemision.execute();
            String idGuiaRemision = statementIdGuiaRemision.getString(1);

            statementGuiaRemision = dbf.getConnection().prepareStatement("INSERT INTO GuiaRemisionTB"
                    + "(IdGuiaRemision"
                    + ",Comprobante"
                    + ",Serie"
                    + ",Numeracion"
                    + ",IdCliente"
                    + ",IdVendedor"
                    + ",FechaRegistro"
                    + ",HoraRegistro"
                    + ",IdModalidadTraslado"
                    + ",IdMotivoTraslado"
                    + ",FechaTraslado"
                    + ",HoraTraslado"
                    + ",IdPesoCarga"
                    + ",PesoCarga"
                    + ",IdVehiculo"
                    + ",IdConductor"
                    + ",DireccionPartida"
                    + ",IdUbigeoPartida"
                    + ",DireccionLlegada"
                    + ",IdUbigeoLlegada"
                    + ",IdVenta"
                    + ",Estado)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statementGuiaRemision.setString(1, idGuiaRemision);
            statementGuiaRemision.setInt(2, guiaRemisionTB.getIdComprobante());
            statementGuiaRemision.setString(3, id_comprabante[0]);
            statementGuiaRemision.setString(4, id_comprabante[1]);
            statementGuiaRemision.setString(5, guiaRemisionTB.getIdCliente());
            statementGuiaRemision.setString(6, guiaRemisionTB.getIdVendedor());
            statementGuiaRemision.setString(7, Tools.getDate());
            statementGuiaRemision.setString(8, Tools.getTime());
            statementGuiaRemision.setString(9, guiaRemisionTB.getIdModalidadTraslado());
            statementGuiaRemision.setInt(10, guiaRemisionTB.getIdMotivoTraslado());
            statementGuiaRemision.setString(11, guiaRemisionTB.getFechaTraslado());
            statementGuiaRemision.setString(12, Tools.getTime());
            statementGuiaRemision.setInt(13, guiaRemisionTB.getIdPesoCarga());
            statementGuiaRemision.setDouble(14, guiaRemisionTB.getPesoCarga());
            statementGuiaRemision.setString(15, guiaRemisionTB.getIdVehiculo());
            statementGuiaRemision.setString(16, guiaRemisionTB.getIdConductor());
            statementGuiaRemision.setString(17, guiaRemisionTB.getDireccionPartida());
            statementGuiaRemision.setInt(18, guiaRemisionTB.getIdUbigeoPartida());
            statementGuiaRemision.setString(19, guiaRemisionTB.getDireccionLlegada());
            statementGuiaRemision.setInt(20, guiaRemisionTB.getIdUbigeoLlegada());
            statementGuiaRemision.setString(21, guiaRemisionTB.getIdVenta());
            statementGuiaRemision.setInt(22, guiaRemisionTB.getEstado());
            statementGuiaRemision.addBatch();

            statementGuiaRemisionDetalle = dbf.getConnection().prepareStatement("INSERT INTO GuiaRemisionDetalleTB"
                    + "(IdGuiaRemision"
                    + ",IdSuministro"
                    + ",Codigo"
                    + ",Descripcion"
                    + ",Unidad"
                    + ",Cantidad"
                    + ",Peso)"
                    + "VALUES(?,?,?,?,?,?,?)");
            ObservableList<GuiaRemisionDetalleTB> list = guiaRemisionTB.getGuiaRemisionDetalle();
            for (GuiaRemisionDetalleTB guiad : list) {
                statementGuiaRemisionDetalle.setString(1, idGuiaRemision);
                statementGuiaRemisionDetalle.setString(2, guiad.getIdSuministro());
                statementGuiaRemisionDetalle.setString(3, guiad.getTxtCodigo().getText());
                statementGuiaRemisionDetalle.setString(4, guiad.getTxtDescripcion().getText());
                statementGuiaRemisionDetalle.setString(5, guiad.getTxtUnidad().getText());
                statementGuiaRemisionDetalle.setDouble(6, Double.parseDouble(guiad.getTxtCantidad().getText()));
                statementGuiaRemisionDetalle.setDouble(7, Double.parseDouble(guiad.getTxtPeso().getText()));
                statementGuiaRemisionDetalle.addBatch();
            }

            statementGuiaRemision.executeBatch();
            statementGuiaRemisionDetalle.executeBatch();
            dbf.getConnection().commit();
            return "register/" + idGuiaRemision;
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage() + "/";
            } catch (SQLException e) {
                return e.getLocalizedMessage() + "/";
            }
        } finally {
            try {
                if (serie_numeracion != null) {
                    serie_numeracion.close();
                }
                if (statementIdGuiaRemision != null) {
                    statementIdGuiaRemision.close();
                }
                if (statementGuiaRemision != null) {
                    statementGuiaRemision.close();
                }
                if (statementGuiaRemisionDetalle != null) {
                    statementGuiaRemisionDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage() + "/";
            }
        }
    }

    public static String actualizarGuiaRemision(GuiaRemisionTB guiaRemisionTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementGuiaRemision = null;
        PreparedStatement statementGuiaRemisionLimpiar = null;
        PreparedStatement statementGuiaRemisionDetalle = null;

        try {
            dbf.dbConnect();

            statementGuiaRemision = dbf.getConnection().prepareStatement("UPDATE GuiaRemisionTB SET\n" +
                    "Comprobante = ?,\n" +
                    "IdCliente = ?,\n" +
                    "IdVendedor = ?,\n" +
                    "FechaTraslado = ?,\n" +
                    "HoraTraslado = ?,\n" +
                    "IdModalidadTraslado = ?,\n" +
                    "IdMotivoTraslado = ?,\n" +
                    "IdPesoCarga = ?,\n" +
                    "PesoCarga = ?,\n" +
                    "IdVehiculo = ?,\n" +
                    "IdConductor = ?,\n" +
                    "DireccionPartida = ?,\n" +
                    "IdUbigeoPartida = ?,\n" +
                    "DireccionLlegada = ?,\n" +
                    "IdUbigeoLlegada = ?\n" +
                    "WHERE IdGuiaRemision = ?");
            statementGuiaRemision.setInt(1, guiaRemisionTB.getIdComprobante());
            statementGuiaRemision.setString(2, guiaRemisionTB.getIdCliente());
            statementGuiaRemision.setString(3, guiaRemisionTB.getIdVehiculo());
            statementGuiaRemision.setString(4, guiaRemisionTB.getFechaTraslado());
            statementGuiaRemision.setString(5, guiaRemisionTB.getHoraTraslado());
            statementGuiaRemision.setString(6, guiaRemisionTB.getIdModalidadTraslado());
            statementGuiaRemision.setInt(7, guiaRemisionTB.getIdMotivoTraslado());
            statementGuiaRemision.setInt(8, guiaRemisionTB.getIdPesoCarga());
            statementGuiaRemision.setDouble(9, guiaRemisionTB.getPesoCarga());
            statementGuiaRemision.setString(10, guiaRemisionTB.getIdVehiculo());
            statementGuiaRemision.setString(11, guiaRemisionTB.getIdConductor());
            statementGuiaRemision.setString(12, guiaRemisionTB.getDireccionPartida());
            statementGuiaRemision.setInt(13, guiaRemisionTB.getIdUbigeoPartida());
            statementGuiaRemision.setString(14, guiaRemisionTB.getDireccionLlegada());
            statementGuiaRemision.setInt(15, guiaRemisionTB.getIdUbigeoLlegada());
            statementGuiaRemision.setString(16, guiaRemisionTB.getIdGuiaRemision());
            statementGuiaRemision.addBatch();

            statementGuiaRemisionLimpiar = dbf.getConnection()
                    .prepareStatement("DELETE FROM GuiaRemisionDetalleTB WHERE IdGuiaRemision = ?");
            statementGuiaRemisionLimpiar.setString(1, guiaRemisionTB.getIdGuiaRemision());
            statementGuiaRemisionLimpiar.addBatch();
            statementGuiaRemisionLimpiar.executeBatch();

            statementGuiaRemisionDetalle = dbf.getConnection().prepareStatement("INSERT INTO GuiaRemisionDetalleTB"
                    + "(IdGuiaRemision"
                    + ",IdSuministro"
                    + ",Codigo"
                    + ",Descripcion"
                    + ",Unidad"
                    + ",Cantidad"
                    + ",Peso)"
                    + "VALUES(?,?,?,?,?,?,?)");
            ObservableList<GuiaRemisionDetalleTB> list = guiaRemisionTB.getGuiaRemisionDetalle();
            for (GuiaRemisionDetalleTB guiad : list) {
                statementGuiaRemisionDetalle.setString(1, guiaRemisionTB.getIdGuiaRemision());
                statementGuiaRemisionDetalle.setString(2, guiad.getIdSuministro());
                statementGuiaRemisionDetalle.setString(3, guiad.getTxtCodigo().getText());
                statementGuiaRemisionDetalle.setString(4, guiad.getTxtDescripcion().getText());
                statementGuiaRemisionDetalle.setString(5, guiad.getTxtUnidad().getText());
                statementGuiaRemisionDetalle.setDouble(6, Double.parseDouble(guiad.getTxtCantidad().getText()));
                statementGuiaRemisionDetalle.setDouble(7, Double.parseDouble(guiad.getTxtPeso().getText()));
                statementGuiaRemisionDetalle.addBatch();
            }

            statementGuiaRemision.executeBatch();
            statementGuiaRemisionDetalle.executeBatch();
            statementGuiaRemision.getConnection().commit();
            return "updated/" + guiaRemisionTB.getIdGuiaRemision();
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage() + "/";
            } catch (SQLException e) {
                return e.getLocalizedMessage() + "/";
            }
        } finally {
            try {
                if (statementGuiaRemision != null) {
                    statementGuiaRemision.close();
                }
                if (statementGuiaRemisionLimpiar != null) {
                    statementGuiaRemisionLimpiar.close();
                }
                if (statementGuiaRemisionDetalle != null) {
                    statementGuiaRemisionDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage() + "/";
            }
        }
    }

    public static Object listarGuiaRemision(int opcion, String buscar, String fechaInicio,
            String fechaFinal, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<GuiaRemisionTB> remisionTBs = FXCollections.observableArrayList();

            preparedStatement = dbf.getConnection().prepareStatement("{CALL Sp_Listar_GuiaRemision(?,?,?,?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, buscar);
            preparedStatement.setString(3, fechaInicio);
            preparedStatement.setString(4, fechaFinal);
            preparedStatement.setInt(5, posicionPagina);
            preparedStatement.setInt(6, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                GuiaRemisionTB guiaRemisionTB = new GuiaRemisionTB();
                guiaRemisionTB.setId(rsEmps.getRow());
                guiaRemisionTB.setIdGuiaRemision(rsEmps.getString("IdGuiaRemision"));

                TipoDocumentoTB tipoDocumentoTBGuia = new TipoDocumentoTB();
                tipoDocumentoTBGuia.setNombre(rsEmps.getString("ComprobanteGuia"));
                guiaRemisionTB.setTipoDocumentoTB(tipoDocumentoTBGuia);

                guiaRemisionTB.setSerie(rsEmps.getString("Serie"));
                guiaRemisionTB.setNumeracion(rsEmps.getString("Numeracion"));

                guiaRemisionTB.setFechaRegistro(rsEmps.getDate("FechaRegistro").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                guiaRemisionTB.setHoraRegistro(rsEmps.getTime("HoraRegistro").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                guiaRemisionTB.setFechaTraslado(rsEmps.getDate("FechaTraslado").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                guiaRemisionTB.setHoraTraslado(rsEmps.getTime("HoraTraslado").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                guiaRemisionTB.setEstado(rsEmps.getInt("Estado"));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumentoCliente"));
                clienteTB.setInformacion(rsEmps.getString("InformacionCliente"));
                guiaRemisionTB.setClienteTB(clienteTB);

                VentaTB ventaTB = new VentaTB();
                TipoDocumentoTB tipoDocumentoTBVenta = new TipoDocumentoTB();
                tipoDocumentoTBVenta.setNombre(rsEmps.getString("ComprobanteRefencia"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoTBVenta);
                ventaTB.setSerie(rsEmps.getString("SerieReferencia"));
                ventaTB.setNumeracion(rsEmps.getString("NumeracionReferencia"));
                guiaRemisionTB.setVentaTB(ventaTB);

                if (guiaRemisionTB.getEstado() == 1) {
                    Label label = new Label();
                    label.setText("ACTIVO");
                    label.getStyleClass().add("label-asignacion");
                    guiaRemisionTB.setEstadoLabel(label);
                } else {
                    Label label = new Label();
                    label.setText("ANULADO");
                    label.getStyleClass().add("label-proceso");
                    guiaRemisionTB.setEstadoLabel(label);
                }
                remisionTBs.add(guiaRemisionTB);
            }

            preparedStatementCount = dbf.getConnection()
                    .prepareStatement("{call Sp_Listar_GuiaRemision_Count(?,?,?,?)}");
            preparedStatementCount.setInt(1, opcion);
            preparedStatementCount.setString(2, buscar);
            preparedStatementCount.setString(3, fechaInicio);
            preparedStatementCount.setString(4, fechaFinal);
            rsEmps.close();
            rsEmps = preparedStatementCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            objects[0] = remisionTBs;
            objects[1] = cantidadTotal;

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
                if (rsEmps != null) {
                    rsEmps.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object obtenerGuiaRemisionById(String idGuiaRemision) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementGuiaRemision = null;
        PreparedStatement statementGuiaRemisionDetalle = null;
        try {
            dbf.dbConnect();

            statementGuiaRemision = dbf.getConnection().prepareStatement("{CALL Sp_Obtener_GuiaRemision_ById(?)}");
            statementGuiaRemision.setString(1, idGuiaRemision);
            ResultSet result = statementGuiaRemision.executeQuery();
            if (result.next()) {
                GuiaRemisionTB guiaRemisionTB = new GuiaRemisionTB();
                guiaRemisionTB.setIdGuiaRemision(result.getString("IdGuiaRemision"));

                TipoDocumentoTB tipoDocumentoGuia = new TipoDocumentoTB();
                tipoDocumentoGuia.setNombre(result.getString("ComprobanteGuia"));

                guiaRemisionTB.setTipoDocumentoTB(tipoDocumentoGuia);
                guiaRemisionTB.setSerie(result.getString("Serie"));
                guiaRemisionTB.setNumeracion(result.getString("Numeracion"));

                guiaRemisionTB.setFechaRegistro(result.getDate("FechaRegistro").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                guiaRemisionTB.setHoraRegistro(
                        result.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                guiaRemisionTB.setFechaTraslado(result.getDate("FechaTraslado").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                guiaRemisionTB.setHoraTraslado(
                        result.getTime("HoraTraslado").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                ModalidadTrasladoTB modalidadTrasladoTB = new ModalidadTrasladoTB();
                modalidadTrasladoTB.setNombre(result.getString("ModalidadTraslado"));
                guiaRemisionTB.setModalidadTrasladoTB(modalidadTrasladoTB);

                DetalleTB detalleMotivoTraslado = new DetalleTB();
                detalleMotivoTraslado.setNombre(result.getString("MotivoTraslado").toUpperCase());
                guiaRemisionTB.setDetalleMotivoTrasladoTB(detalleMotivoTraslado);

                guiaRemisionTB.setPesoCarga(result.getDouble("PesoCarga"));
                DetalleTB detallePeso = new DetalleTB();
                detallePeso.setIdAuxiliar(result.getString("CodigoPeso"));
                guiaRemisionTB.setDetallePesoCargaTB(detallePeso);

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setNumeroDocumento(result.getString("NumeroDocumento"));
                clienteTB.setInformacion(result.getString("Informacion"));
                clienteTB.setCelular(result.getString("Celular"));
                clienteTB.setEmail(result.getString("Email"));
                clienteTB.setDireccion(result.getString("Direccion"));
                guiaRemisionTB.setClienteTB(clienteTB);

                if (result.getString("NumeroDocumentoCo") != null) {
                    ConductorTB conductorTB = new ConductorTB();
                    conductorTB.setNumeroDocumento(result.getString("NumeroDocumentoCo"));
                    conductorTB.setInformacion(result.getString("InformacionCo"));
                    conductorTB.setCelular(result.getString("CelularCo"));
                    conductorTB.setLicenciaConducir(result.getString("LicenciaConducir"));
                    guiaRemisionTB.setConductorTB(conductorTB);
                } else {
                    ConductorTB conductorTB = new ConductorTB();
                    conductorTB.setNumeroDocumento("");
                    conductorTB.setInformacion("");
                    conductorTB.setCelular("");
                    conductorTB.setLicenciaConducir("");
                    guiaRemisionTB.setConductorTB(conductorTB);
                }

                if (result.getString("NumeroPlaca") != null) {
                    VehiculoTB vehiculoTB = new VehiculoTB();
                    vehiculoTB.setNumeroPlaca(result.getString("NumeroPlaca"));
                    guiaRemisionTB.setVehiculoTB(vehiculoTB);
                } else {
                    VehiculoTB vehiculoTB = new VehiculoTB();
                    vehiculoTB.setNumeroPlaca("");
                    guiaRemisionTB.setVehiculoTB(vehiculoTB);
                }

                guiaRemisionTB.setDireccionPartida(result.getString("DireccionPartida").toUpperCase());
                UbigeoTB ubigeoPartida = new UbigeoTB();
                ubigeoPartida.setDepartamento(result.getString("DepartamentoPartida"));
                ubigeoPartida.setProvincia(result.getString("ProvinciaPartida"));
                ubigeoPartida.setDistrito(result.getString("DistritoPartida"));
                guiaRemisionTB.setUbigeoPartidaTB(ubigeoPartida);

                guiaRemisionTB.setDireccionLlegada(result.getString("DireccionLlegada").toUpperCase());
                UbigeoTB ubigeoDetalle = new UbigeoTB();
                ubigeoDetalle.setDepartamento(result.getString("DepartamentoLlegada"));
                ubigeoDetalle.setProvincia(result.getString("ProvinciaLlegada"));
                ubigeoDetalle.setDistrito(result.getString("DistritoLlegada"));
                guiaRemisionTB.setUbigeoLlegadaTB(ubigeoDetalle);

                VentaTB ventaTB = new VentaTB();
                TipoDocumentoTB tipoDocumentoVenta = new TipoDocumentoTB();
                tipoDocumentoVenta.setNombre(result.getString("ComprobanteFactura"));
                ventaTB.setTipoDocumentoTB(tipoDocumentoVenta);
                ventaTB.setSerie(result.getString("SerieFactura"));
                ventaTB.setNumeracion(result.getString("NumeracionFactura"));
                guiaRemisionTB.setVentaTB(ventaTB);

                statementGuiaRemisionDetalle = dbf.getConnection()
                        .prepareStatement("{CALL Sp_Obtener_GuiaRemision_Detalle_ById(?)}");
                statementGuiaRemisionDetalle.setString(1, idGuiaRemision);
                result = statementGuiaRemisionDetalle.executeQuery();
                ObservableList<GuiaRemisionDetalleTB> observableList = FXCollections.observableArrayList();
                while (result.next()) {
                    GuiaRemisionDetalleTB guiaRemisionDetalleTB = new GuiaRemisionDetalleTB();
                    guiaRemisionDetalleTB.setId(result.getRow());
                    guiaRemisionDetalleTB.setCodigo(result.getString("Clave"));
                    guiaRemisionDetalleTB.setDescripcion(result.getString("NombreMarca"));
                    guiaRemisionDetalleTB.setUnidad(result.getString("Unidad"));
                    guiaRemisionDetalleTB.setCantidad(result.getDouble("Cantidad"));
                    guiaRemisionDetalleTB.setPeso(result.getDouble("Peso"));
                    observableList.add(guiaRemisionDetalleTB);
                }
                guiaRemisionTB.setGuiaRemisionDetalle(observableList);
                return guiaRemisionTB;
            } else {
                throw new Exception("No se pudo obtener los datos para generar la guía de remisión.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementGuiaRemision != null) {
                    statementGuiaRemision.close();
                }
                if (statementGuiaRemisionDetalle != null) {
                    statementGuiaRemisionDetalle.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object obtenerGuiaRemisionParaEditar(String idGuiaRemision) {
        DBUtil dbf = new DBUtil();
        try {
            dbf.dbConnect();

            PreparedStatement preparedGuiaRemision = dbf.getConnection().prepareCall("select \n" +
                    "gui.IdGuiaRemision,\n" +
                    "gui.Comprobante,\n" +
                    "gui.IdModalidadTraslado,\n" +
                    "gui.IdMotivoTraslado,\n" +
                    "gui.FechaTraslado,\n" +
                    "gui.HoraTraslado,\n" +
                    "gui.IdPesoCarga,\n" +
                    "gui.PesoCarga,\n" +

                    "cl.IdCliente,\n" +
                    "cl.NumeroDocumento as NumeroDocumentoCliente,\n" +
                    "cl.Informacion as InformacionCliente,\n" +

                    "isnull(ve.IdVehiculo,'') as IdVehiculo,\n" +
                    "isnull(ve.Marca,'') as Marca,\n" +
                    "isnull(ve.NumeroPlaca,'') as NumeroPlaca,\n" +

                    "isnull(co.IdConductor,'') as IdConductor,\n" +
                    "isnull(co.NumeroDocumento,'') as NumeroDocumentoConductor,\n" +
                    "isnull(co.Informacion,'') as InformacionConductor,\n" +

                    "gui.DireccionPartida,\n" +
                    "ubp.IdUbigeo as IdUbigeoPartida,\n" +
                    "ubp.Ubigeo as UbigeoPartida,\n" +
                    "ubp.Departamento as DepartamentoPartida,\n" +
                    "ubp.Distrito as DistritoPartida,\n" +
                    "ubp.Provincia as ProvinciaPartida,\n" +

                    "gui.DireccionLlegada,\n" +
                    "ubl.IdUbigeo as IdUbigeoLlegada,\n" +
                    "ubl.Ubigeo as UbigeoLlegada,\n" +
                    "ubl.Departamento as DepartamentoLlegada,\n" +
                    "ubl.Distrito as DistritoLlegada,\n" +
                    "ubl.Provincia as ProvinciaLlegada,\n" +

                    "vt.Comprobante,\n" +
                    "vt.Serie,\n" +
                    "vt.Numeracion\n" +

                    "from GuiaRemisionTB as gui\n" +
                    "inner join ClienteTB as cl on cl.IdCliente = gui.IdCliente\n" +
                    "left join VehiculoTB as ve on ve.IdVehiculo = gui.IdVehiculo\n" +
                    "left join ConductorTB as co on co.IdConductor = gui.IdConductor\n" +
                    "inner join UbigeoTB as ubp on ubp.IdUbigeo = gui.IdUbigeoPartida\n" +
                    "inner join UbigeoTB as ubl on ubl.IdUbigeo = gui.IdUbigeoLlegada\n" +
                    "inner join VentaTB as vt on vt.IdVenta = gui.IdVenta\n" +
                    "where gui.IdGuiaRemision = ?");
            preparedGuiaRemision.setString(1, idGuiaRemision);
            ResultSet rsGuiaRemision = preparedGuiaRemision.executeQuery();
            if (rsGuiaRemision.next()) {
                GuiaRemisionTB guiaRemisionTB = new GuiaRemisionTB();
                guiaRemisionTB.setIdGuiaRemision(rsGuiaRemision.getString("IdGuiaRemision"));
                guiaRemisionTB.setIdComprobante(rsGuiaRemision.getInt("Comprobante"));
                guiaRemisionTB.setIdModalidadTraslado(rsGuiaRemision.getString("IdModalidadTraslado"));
                guiaRemisionTB.setIdMotivoTraslado(rsGuiaRemision.getInt("IdMotivoTraslado"));
                guiaRemisionTB.setFechaTraslado(rsGuiaRemision.getString("FechaTraslado"));
                guiaRemisionTB.setHoraTraslado(rsGuiaRemision.getString("HoraTraslado"));
                guiaRemisionTB.setIdPesoCarga(rsGuiaRemision.getInt("IdPesoCarga"));
                guiaRemisionTB.setPesoCarga(rsGuiaRemision.getInt("PesoCarga"));

                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(rsGuiaRemision.getString("IdCliente"));
                clienteTB.setNumeroDocumento(rsGuiaRemision.getString("NumeroDocumentoCliente"));
                clienteTB.setInformacion(rsGuiaRemision.getString("InformacionCliente"));
                guiaRemisionTB.setClienteTB(clienteTB);

                if (rsGuiaRemision.getString("IdVehiculo") != "") {
                    VehiculoTB vehicleTB = new VehiculoTB();
                    vehicleTB.setIdVehiculo(rsGuiaRemision.getString("IdVehiculo"));
                    vehicleTB.setMarca(rsGuiaRemision.getString("Marca"));
                    vehicleTB.setNumeroPlaca(rsGuiaRemision.getString("NumeroPlaca"));
                    guiaRemisionTB.setVehiculoTB(vehicleTB);
                }

                if (rsGuiaRemision.getString("IdConductor") != "") {
                    ConductorTB conductorTB = new ConductorTB();
                    conductorTB.setIdConductor(rsGuiaRemision.getString("IdConductor"));
                    conductorTB.setNumeroDocumento(rsGuiaRemision.getString("NumeroDocumentoConductor"));
                    conductorTB.setInformacion(rsGuiaRemision.getString("InformacionConductor"));
                    guiaRemisionTB.setConductorTB(conductorTB);
                }

                guiaRemisionTB.setDireccionPartida(rsGuiaRemision.getString("DireccionPartida"));
                UbigeoTB ubigeoPartidaTB = new UbigeoTB();
                ubigeoPartidaTB.setIdUbigeo(rsGuiaRemision.getInt("IdUbigeoPartida"));
                ubigeoPartidaTB.setUbigeo(rsGuiaRemision.getString("UbigeoPartida"));
                ubigeoPartidaTB.setDepartamento(rsGuiaRemision.getString("DepartamentoPartida"));
                ubigeoPartidaTB.setProvincia(rsGuiaRemision.getString("DistritoPartida"));
                ubigeoPartidaTB.setDistrito(rsGuiaRemision.getString("ProvinciaPartida"));
                guiaRemisionTB.setUbigeoPartidaTB(ubigeoPartidaTB);

                guiaRemisionTB.setDireccionLlegada(rsGuiaRemision.getString("DireccionLlegada"));
                UbigeoTB ubigeoLlegadaTB = new UbigeoTB();
                ubigeoLlegadaTB.setIdUbigeo(rsGuiaRemision.getInt("IdUbigeoLlegada"));
                ubigeoLlegadaTB.setUbigeo(rsGuiaRemision.getString("UbigeoLlegada"));
                ubigeoLlegadaTB.setDepartamento(rsGuiaRemision.getString("DepartamentoLlegada"));
                ubigeoLlegadaTB.setProvincia(rsGuiaRemision.getString("DistritoLlegada"));
                ubigeoLlegadaTB.setDistrito(rsGuiaRemision.getString("ProvinciaLlegada"));
                guiaRemisionTB.setUbigeoLlegadaTB(ubigeoLlegadaTB);

                VentaTB ventaTB = new VentaTB();
                ventaTB.setIdComprobante(rsGuiaRemision.getInt("Comprobante"));
                ventaTB.setSerie(rsGuiaRemision.getString("Serie"));
                ventaTB.setNumeracion(rsGuiaRemision.getString("Numeracion"));
                guiaRemisionTB.setVentaTB(ventaTB);

                preparedGuiaRemision.close();
                rsGuiaRemision.close();

                PreparedStatement preparedGuiaRemisionDetalle = dbf.getConnection().prepareStatement("select\n" +
                        "gui.IdGuiaRemision,\n" +
                        "gui.IdSuministro,\n" +
                        "gui.Codigo,\n" +
                        "gui.Descripcion,\n" +
                        "gui.Unidad,\n" +
                        "gui.Cantidad,\n" +
                        "gui.Peso\n" +
                        "from GuiaRemisionDetalleTB as gui\n" +
                        "where gui.IdGuiaRemision = ?");
                preparedGuiaRemisionDetalle.setString(1, idGuiaRemision);
                ResultSet rsGuiaRemisionDetalle = preparedGuiaRemisionDetalle.executeQuery();
                ObservableList<GuiaRemisionDetalleTB> oGuiaRemisionDetalleTBs = FXCollections.observableArrayList();
                while (rsGuiaRemisionDetalle.next()) {
                    GuiaRemisionDetalleTB guiaRemisionDetalleTB = new GuiaRemisionDetalleTB();
                    guiaRemisionDetalleTB.setIdGuiaRemision(rsGuiaRemisionDetalle.getString("IdGuiaRemision"));
                    guiaRemisionDetalleTB.setIdSuministro(rsGuiaRemisionDetalle.getString("IdSuministro"));
                    guiaRemisionDetalleTB.setCodigo(rsGuiaRemisionDetalle.getString("Codigo"));
                    guiaRemisionDetalleTB.setDescripcion(rsGuiaRemisionDetalle.getString("Descripcion"));
                    guiaRemisionDetalleTB.setUnidad(rsGuiaRemisionDetalle.getString("Unidad"));
                    guiaRemisionDetalleTB.setCantidad(rsGuiaRemisionDetalle.getDouble("Cantidad"));
                    guiaRemisionDetalleTB.setPeso(rsGuiaRemisionDetalle.getDouble("Peso"));
                    oGuiaRemisionDetalleTBs.add(guiaRemisionDetalleTB);
                }
                guiaRemisionTB.setGuiaRemisionDetalle(oGuiaRemisionDetalleTBs);
                preparedGuiaRemisionDetalle.close();
                rsGuiaRemisionDetalle.close();

                return guiaRemisionTB;
            }

            throw new Exception("No se pudo cargar los datos requeridos.");
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String removeGuiaRemisionById(String idGuiaRemision) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementEliminar = null;
        PreparedStatement statementValidar = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidar = dbf.getConnection()
                    .prepareStatement("SELECT * FROM GuiaRemisionTB WHERE IdGuiaRemision = ? AND Estado = 3");
            statementValidar.setString(1, idGuiaRemision);
            if (statementValidar.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "La guía de remisión ya se encuentra anulada.";
            }

            statementEliminar = dbf.getConnection()
                    .prepareStatement("UPDATE GuiaRemisionTB SET Estado = 3 WHERE IdGuiaRemision = ?");
            statementEliminar.setString(1, idGuiaRemision);
            statementEliminar.addBatch();

            statementEliminar.executeBatch();
            dbf.getConnection().commit();
            return "deleted";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage() + "/";
            } catch (SQLException e) {
                return e.getLocalizedMessage() + "/";
            }
        } finally {
            try {
                if (statementEliminar != null) {
                    statementEliminar.close();
                }

                if (statementValidar != null) {
                    statementValidar.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
