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

    public static String InsertarGuiaRemision(GuiaRemisionTB guiaRemisionTB) {
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
                    + ",estado)"
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            statementGuiaRemision.setObject(1, idGuiaRemision);
            statementGuiaRemision.setObject(2, guiaRemisionTB.getIdComprobante());
            statementGuiaRemision.setObject(3, id_comprabante[0]);
            statementGuiaRemision.setObject(4, id_comprabante[1]);
            statementGuiaRemision.setObject(5, guiaRemisionTB.getIdCliente());
            statementGuiaRemision.setObject(6, guiaRemisionTB.getIdVendedor());
            statementGuiaRemision.setObject(7, Tools.getDate());
            statementGuiaRemision.setObject(8, Tools.getTime());
            statementGuiaRemision.setObject(9, guiaRemisionTB.getIdModalidadTraslado());
            statementGuiaRemision.setObject(10, guiaRemisionTB.getIdMotivoTraslado());
            statementGuiaRemision.setObject(11, guiaRemisionTB.getFechaTraslado());
            statementGuiaRemision.setObject(12, Tools.getTime());
            statementGuiaRemision.setObject(13, guiaRemisionTB.getIdPesoCarga());
            statementGuiaRemision.setObject(14, guiaRemisionTB.getPesoCarga());
            statementGuiaRemision.setObject(15, guiaRemisionTB.getIdVehiculo());
            statementGuiaRemision.setObject(16, guiaRemisionTB.getIdConductor());
            statementGuiaRemision.setObject(17, guiaRemisionTB.getDireccionPartida());
            statementGuiaRemision.setObject(18, guiaRemisionTB.getIdUbigeoPartida());
            statementGuiaRemision.setObject(19, guiaRemisionTB.getDireccionLlegada());
            statementGuiaRemision.setObject(20, guiaRemisionTB.getIdUbigeoLlegada());
            statementGuiaRemision.setObject(21, guiaRemisionTB.getIdVenta());
            statementGuiaRemision.setObject(22, guiaRemisionTB.getEstado());
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

    public static Object CargarGuiaRemision(int opcion, String buscar, String fechaInicio,
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

    public static Object ObtenerGuiaRemisionById(String idGuiaRemision) {
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
