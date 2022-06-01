package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class TipoDocumentoADO {

    public static String CrudTipoDocumento(TipoDocumentoTB documentoTB) {
        String result = null;
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementUpdate = null;
            PreparedStatement statementState = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementState = DBUtil.getConnection().prepareStatement("SELECT * FROM TipoDocumentoTB WHERE IdTipoDocumento = ? ");
                statementState.setInt(1, documentoTB.getIdTipoDocumento());
                if (statementState.executeQuery().next()) {

                    statementState = DBUtil.getConnection().prepareStatement("SELECT * FROM TipoDocumentoTB WHERE IdTipoDocumento <> ? AND Nombre = ? ");
                    statementState.setInt(1, documentoTB.getIdTipoDocumento());
                    statementState.setString(2, documentoTB.getNombre());
                    if (statementState.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        statementUpdate = DBUtil.getConnection().prepareStatement("UPDATE TipoDocumentoTB SET Nombre = ?, Serie = ?,Numeracion=?,CodigoAlterno=?,Guia = ?,Facturacion=?, NotaCredito=?, Estado = ?,Campo = ?,NumeroCampo = ?,idTicket = ? WHERE IdTipoDocumento = ?");
                        statementUpdate.setString(1, documentoTB.getNombre());
                        statementUpdate.setString(2, documentoTB.getSerie());
                        statementUpdate.setInt(3, documentoTB.getNumeracion());
                        statementUpdate.setString(4, documentoTB.getCodigoAlterno());
                        statementUpdate.setBoolean(5, documentoTB.isGuia());
                        statementUpdate.setBoolean(6, documentoTB.isFactura());
                        statementUpdate.setBoolean(7, documentoTB.isNotaCredito());
                        statementUpdate.setBoolean(8, documentoTB.isEstado());
                        statementUpdate.setBoolean(9, documentoTB.isCampo());
                        statementUpdate.setInt(10, documentoTB.getNumeroCampo());
                        statementUpdate.setInt(11, 0);
                        statementUpdate.setInt(12, documentoTB.getIdTipoDocumento());
                        statementUpdate.addBatch();

                        statementUpdate.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "updated";
                    }
                } else {
                    statementState = DBUtil.getConnection().prepareStatement("SELECT * FROM TipoDocumentoTB WHERE  Nombre = ? ");
                    statementState.setString(1, documentoTB.getNombre());
                    if (statementState.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        statementUpdate = DBUtil.getConnection().prepareStatement("INSERT INTO TipoDocumentoTB (Nombre,Serie,Numeracion,Predeterminado,Sistema,CodigoAlterno,Guia,Facturacion,NotaCredito,Estado,Campo,NumeroCampo,idTicket) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        statementUpdate.setString(1, documentoTB.getNombre());
                        statementUpdate.setString(2, documentoTB.getSerie());
                        statementUpdate.setInt(3, documentoTB.getNumeracion());
                        statementUpdate.setBoolean(4, documentoTB.isPredeterminado());
                        statementUpdate.setBoolean(5, false);
                        statementUpdate.setString(6, documentoTB.getCodigoAlterno());
                        statementUpdate.setBoolean(7, documentoTB.isGuia());
                        statementUpdate.setBoolean(8, documentoTB.isFactura());
                        statementUpdate.setBoolean(9, documentoTB.isNotaCredito());
                        statementUpdate.setBoolean(10, documentoTB.isEstado());
                        statementUpdate.setBoolean(11, documentoTB.isCampo());
                        statementUpdate.setInt(12, documentoTB.getNumeroCampo());
                        statementUpdate.setInt(13, 0);
                        statementUpdate.addBatch();

                        statementUpdate.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "inserted";
                    }
                }
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                    result = ex.getLocalizedMessage();
                } catch (SQLException e) {
                    result = e.getLocalizedMessage();
                }
            } finally {
                try {
                    if (statementUpdate != null) {
                        statementUpdate.close();
                    }
                    if (statementState != null) {
                        statementState.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer conexión con el servidor, revice y vuelva a intentarlo.";
        }
        return result;
    }

    public static Object ListTipoDocumento(int opcion, String buscar, int posicionPagina, int filasPorPagina) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<TipoDocumentoTB> observableList = FXCollections.observableArrayList();
            statement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_TipoDocumento(?,?,?,?)}");
            statement.setInt(1, opcion);
            statement.setString(2, buscar);
            statement.setInt(3, posicionPagina);
            statement.setInt(4, filasPorPagina);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                TipoDocumentoTB tipoDocumentoTB = new TipoDocumentoTB();
                tipoDocumentoTB.setId(resultSet.getRow() + posicionPagina);
                tipoDocumentoTB.setIdTipoDocumento(resultSet.getInt("IdTipoDocumento"));
                tipoDocumentoTB.setNombre(resultSet.getString("Nombre"));
                tipoDocumentoTB.setSerie(resultSet.getString("Serie"));
                tipoDocumentoTB.setNumeracion(resultSet.getInt("Numeracion"));
                tipoDocumentoTB.setCodigoAlterno(resultSet.getString("CodigoAlterno"));
                tipoDocumentoTB.setPredeterminado(resultSet.getBoolean("Predeterminado"));
                tipoDocumentoTB.setGuia(resultSet.getBoolean("Guia"));
                tipoDocumentoTB.setFactura(resultSet.getBoolean("Facturacion"));
                tipoDocumentoTB.setNotaCredito(resultSet.getBoolean("NotaCredito"));
                tipoDocumentoTB.setEstado(resultSet.getBoolean("Estado"));
                tipoDocumentoTB.setCampo(resultSet.getBoolean("Campo"));
                tipoDocumentoTB.setNumeroCampo(resultSet.getInt("NumeroCampo"));

                Label lblDestino = new Label(tipoDocumentoTB.isGuia() ? "Guía Remisión" : tipoDocumentoTB.isNotaCredito() ? "Nota de Crédito" : "Ventas");
                lblDestino.getStyleClass().add("labelRoboto13");
                lblDestino.setTextFill(Color.web("#020203"));
                lblDestino.setContentDisplay(ContentDisplay.TOP);
                ImageView ivDestino = new ImageView(new Image(tipoDocumentoTB.isGuia() ? "/view/image/guia_remision.png" : tipoDocumentoTB.isNotaCredito() ? "/view/image/note.png" : "/view/image/sales.png"));
                ivDestino.setFitWidth(22);
                ivDestino.setFitHeight(22);
                lblDestino.setGraphic(ivDestino);
                tipoDocumentoTB.setLblDestino(lblDestino);

                ImageView imageView = new ImageView(new Image(tipoDocumentoTB.isPredeterminado() ? "/view/image/checked.png" : "/view/image/unchecked.png"));
                imageView.setFitWidth(22);
                imageView.setFitHeight(22);
                tipoDocumentoTB.setIvPredeterminado(imageView);

                observableList.add(tipoDocumentoTB);
            }

            statement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_TipoDocumento_Count(?,?)}");
            statement.setInt(1, opcion);
            statement.setString(2, buscar);
            resultSet = statement.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSet.next()) {
                cantidadTotal = resultSet.getInt("Total");
            }
            objects[0] = observableList;
            objects[1] = cantidadTotal;
            return objects;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static String ChangeDefaultState(boolean state, int id) {
        String result = null;
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementSelect = null;
            PreparedStatement statementUpdate = null;
            PreparedStatement statementState = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementSelect = DBUtil.getConnection().prepareStatement("SELECT Predeterminado FROM TipoDocumentoTB WHERE Predeterminado = 1");
                if (statementSelect.executeQuery().next()) {
                    statementUpdate = DBUtil.getConnection().prepareStatement("UPDATE TipoDocumentoTB SET Predeterminado = 0 WHERE Predeterminado = 1");
                    statementUpdate.addBatch();

                    statementState = DBUtil.getConnection().prepareStatement("UPDATE TipoDocumentoTB SET Predeterminado = ? WHERE IdTipoDocumento = ?");
                    statementState.setBoolean(1, state);
                    statementState.setInt(2, id);
                    statementState.addBatch();

                    statementUpdate.executeBatch();
                    statementState.executeBatch();
                    DBUtil.getConnection().commit();
                    result = "updated";
                } else {
                    statementState = DBUtil.getConnection().prepareStatement("UPDATE TipoDocumentoTB SET Predeterminado = ? WHERE IdTipoDocumento = ?");
                    statementState.setBoolean(1, state);
                    statementState.setInt(2, id);
                    statementState.addBatch();
                    statementState.executeBatch();
                    DBUtil.getConnection().commit();
                    result = "updated";
                }

            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                    result = ex.getLocalizedMessage();
                } catch (SQLException e) {
                    result = e.getLocalizedMessage();
                }

            } finally {
                try {
                    if (statementSelect != null) {
                        statementSelect.close();
                    }
                    if (statementUpdate != null) {
                        statementUpdate.close();
                    }
                    if (statementState != null) {
                        statementState.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer conexión con el servidor, revice y vuelva a intentarlo.";
        }
        return result;
    }

    public static List<TipoDocumentoTB> GetDocumentoCombBoxVentas() {
        List<TipoDocumentoTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                statement = DBUtil.getConnection().prepareStatement("SELECT IdTipoDocumento,Nombre,Serie, Predeterminado,Campo,NumeroCampo FROM TipoDocumentoTB WHERE Guia <> 1 AND NotaCredito <> 1 AND Estado = 1");
                resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    TipoDocumentoTB documentoTB = new TipoDocumentoTB();
                    documentoTB.setIdTipoDocumento(resultSet.getInt("IdTipoDocumento"));
                    documentoTB.setNombre(resultSet.getString("Nombre"));
                    documentoTB.setSerie(resultSet.getString("Serie"));
                    documentoTB.setPredeterminado(resultSet.getBoolean("Predeterminado"));
                    documentoTB.setCampo(resultSet.getBoolean("Campo"));
                    documentoTB.setNumeroCampo(resultSet.getInt("NumeroCampo"));
                    list.add(documentoTB);
                }
            } catch (SQLException ex) {
                System.out.println("Error Tipo de documento: " + ex.getLocalizedMessage());
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    System.out.println("Error Tipo de documento: " + ex.getLocalizedMessage());
                }
            }
        }
        return list;
    }

    public static Object GetTipoDocumentoGuiaRemision() {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            ArrayList<TipoDocumentoTB> documentoTBs = new ArrayList<>();
            statement = DBUtil.getConnection().prepareStatement("SELECT IdTipoDocumento,Nombre FROM TipoDocumentoTB WHERE Guia = 1");
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                TipoDocumentoTB documentoTB = new TipoDocumentoTB();
                documentoTB.setIdTipoDocumento(resultSet.getInt("IdTipoDocumento"));
                documentoTB.setNombre(resultSet.getString("Nombre"));
                documentoTBs.add(documentoTB);
            }
            return documentoTBs;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String EliminarTipoDocumento(int idTipoDocumento) {
        PreparedStatement statementValidate = null;
        PreparedStatement statementTipoDocumento = null;
        String result = "";
        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM TipoDocumentoTB WHERE IdTipoDocumento = ? AND Sistema = 1");
            statementValidate.setInt(1, idTipoDocumento);
            if (statementValidate.executeQuery().next()) {
                DBUtil.getConnection().rollback();
                result = "sistema";
            } else {
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE Comprobante = ?");
                statementValidate.setInt(1, idTipoDocumento);
                if (statementValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "venta";
                } else {
                    statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM NotaCreditoTB WHERE Comprobante = ?");
                    statementValidate.setInt(1, idTipoDocumento);
                    if (statementValidate.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "notacredito";
                    } else {
                        statementTipoDocumento = DBUtil.getConnection().prepareStatement("DELETE FROM TipoDocumentoTB WHERE IdTipoDocumento = ?");
                        statementTipoDocumento.setInt(1, idTipoDocumento);
                        statementTipoDocumento.addBatch();
                        statementTipoDocumento.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "removed";
                    }
                }
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            result = ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementTipoDocumento != null) {
                    statementTipoDocumento.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return result;
    }

    public static Object GetIdTicketForTipoDocumento(String idVenta) {
        PreparedStatement statementVenta = null;
        PreparedStatement statementTicket = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();

            statementVenta = DBUtil.getConnection().prepareStatement("SELECT Comprobante FROM VentaTB WHERE IdVenta = ?");
            statementVenta.setString(1, idVenta);            
            resultSet = statementVenta.executeQuery();
            if (resultSet.next()) {

                statementTicket = DBUtil.getConnection().prepareStatement("SELECT idTicket FROM TipoDocumentoTB WHERE IdTipoDocumento = ?");
                statementTicket.setInt(1, resultSet.getInt("Comprobante"));               
                resultSet = statementTicket.executeQuery();
                if (resultSet.next()) {
                    statementTicket = DBUtil.getConnection().prepareStatement("SELECT idTicket,ruta FROM TicketTB WHERE idTicket = ?");
                    statementTicket.setInt(1, resultSet.getInt("idTicket"));                  
                    resultSet = statementTicket.executeQuery();
                    if (resultSet.next()) {
                        TicketTB ticketTB = new TicketTB();
                        ticketTB.setId(resultSet.getInt("idTicket"));
                        ticketTB.setRuta(resultSet.getString("ruta"));

                        return ticketTB;
                    } else {
                        throw new Exception("No se encontro ningún ticket asociado.");
                    }
                } else {
                    throw new Exception("No se encontro ningún comprobante,");
                }
            } else {
                throw new Exception("No se encontro ninguna venta.");
            }
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementTicket != null) {
                    statementTicket.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementVenta != null) {
                    statementVenta.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
