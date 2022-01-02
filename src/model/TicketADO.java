package model;

import controller.tools.ImageViewTicket;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.scene.layout.HBox;

public class TicketADO {

    public static String CrudTicket(TicketTB ticketTB) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            CallableStatement callableTicket = null;
            PreparedStatement statementValidar = null;
            PreparedStatement statementTicket = null;
            PreparedStatement statementImagen = null;
            PreparedStatement statementImagenBorrar = null;

            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementValidar = DBUtil.getConnection().prepareStatement("SELECT idTicket FROM TicketTB WHERE idTicket = ?");
                statementValidar.setInt(1, ticketTB.getId());
                if (statementValidar.executeQuery().next()) {
                    statementValidar = DBUtil.getConnection().prepareStatement("SELECT nombre FROM TicketTB WHERE idTicket <> ? and nombre = ?");
                    statementValidar.setInt(1, ticketTB.getId());
                    statementValidar.setString(2, ticketTB.getNombreTicket());
                    if (statementValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {

                        statementTicket = DBUtil.getConnection().prepareStatement("UPDATE TicketTB SET nombre = ?,tipo=?,ruta = ? WHERE idTicket = ?");
                        statementTicket.setString(1, ticketTB.getNombreTicket());
                        statementTicket.setInt(2, ticketTB.getTipo());
                        statementTicket.setString(3, ticketTB.getRuta());
                        statementTicket.setInt(4, ticketTB.getId());
                        statementTicket.addBatch();

                        statementImagenBorrar = DBUtil.getConnection().prepareStatement("DELETE FROM ImagenTB WHERE IdRelacionado = ?");
                        statementImagenBorrar.setString(1, ticketTB.getId() + "");
                        statementImagenBorrar.addBatch();

                        statementImagen = DBUtil.getConnection().prepareStatement("INSERT INTO ImagenTB(Imagen,IdRelacionado,IdSubRelacion)VALUES(?,?,?)");

                        for (int c = 0; c < ticketTB.getApCabecera().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApCabecera().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, ticketTB.getId() + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        for (int c = 0; c < ticketTB.getApDetalle().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApDetalle().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, ticketTB.getId() + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        for (int c = 0; c < ticketTB.getApPie().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApPie().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, ticketTB.getId() + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        statementTicket.executeBatch();
                        statementImagenBorrar.executeBatch();
                        statementImagen.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "updated";
                    }

                } else {
                    statementValidar = DBUtil.getConnection().prepareStatement("SELECT nombre FROM TicketTB WHERE nombre = ?");
                    statementValidar.setString(1, ticketTB.getNombreTicket());
                    if (statementValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {

                        callableTicket = DBUtil.getConnection().prepareCall("{? = call Fc_Ticket_Codigo_Numerico()}");
                        callableTicket.registerOutParameter(1, java.sql.Types.VARCHAR);
                        callableTicket.execute();
                        int idTicket = callableTicket.getInt(1);

                        statementTicket = DBUtil.getConnection().prepareStatement("INSERT INTO TicketTB(idTicket,nombre,tipo,predeterminado,ruta)VALUES(?,?,?,?,?)");
                        statementTicket.setInt(1, idTicket);
                        statementTicket.setString(2, ticketTB.getNombreTicket());
                        statementTicket.setInt(3, ticketTB.getTipo());
                        statementTicket.setBoolean(4, false);
                        statementTicket.setString(5, ticketTB.getRuta());
                        statementTicket.addBatch();

                        statementImagen = DBUtil.getConnection().prepareStatement("INSERT INTO ImagenTB(Imagen,IdRelacionado,IdSubRelacion)VALUES(?,?,?)");

                        for (int c = 0; c < ticketTB.getApCabecera().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApCabecera().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, idTicket + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        for (int c = 0; c < ticketTB.getApDetalle().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApDetalle().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, idTicket + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        for (int c = 0; c < ticketTB.getApPie().getChildren().size(); c++) {
                            HBox hBox = (HBox) ticketTB.getApPie().getChildren().get(c);
                            if (hBox.getChildren().size() == 1) {
                                if (hBox.getChildren().get(0) instanceof ImageViewTicket) {
                                    ImageViewTicket imageViewTicket = (ImageViewTicket) hBox.getChildren().get(0);
                                    statementImagen.setBytes(1, imageViewTicket.getUrl());
                                    statementImagen.setString(2, idTicket + "");
                                    statementImagen.setString(3, imageViewTicket.getId());
                                    statementImagen.addBatch();
                                }
                            }
                        }

                        statementTicket.executeBatch();
                        statementImagen.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "registered";
                    }
                }

            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                } catch (SQLException exr) {
                }
                result = ex.getLocalizedMessage();
            } finally {
                try {
                    if (callableTicket != null) {
                        callableTicket.close();
                    }
                    if (statementTicket != null) {
                        statementTicket.close();
                    }
                    if (statementValidar != null) {
                        statementValidar.close();
                    }
                    if (statementImagen != null) {
                        statementImagen.close();
                    }
                    if (statementImagenBorrar != null) {
                        statementImagenBorrar.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }

        } else {
            result = "No se puedo conectac al servidor, intente nuevamente.";
        }
        return result;
    }

    public static ArrayList<TicketTB> ListTicket(int tipoTicket, boolean todos) {
        ArrayList<TicketTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementLista = null;
            ResultSet resultSet = null;
            try {
                statementLista = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Ticket_By_Tipo_Opcion(?,?)}");
                statementLista.setInt(1, tipoTicket);
                statementLista.setBoolean(2, todos);
                resultSet = statementLista.executeQuery();
                while (resultSet.next()) {
                    TicketTB ticketTB = new TicketTB();
                    ticketTB.setId(resultSet.getInt(1));
                    ticketTB.setNombreTicket(resultSet.getString(2));
                    ticketTB.setTipo(resultSet.getInt(3));
                    ticketTB.setPredeterminado(resultSet.getBoolean(4));
                    ticketTB.setRuta(resultSet.getString(5));
                    list.add(ticketTB);
                }
            } catch (SQLException ex) {

            } finally {
                try {
                    if (statementLista != null) {
                        statementLista.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }

            }
        }
        return list;
    }

    public static ArrayList<TicketTB> ListTipoTicket() {
        ArrayList<TicketTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementLista = null;
            ResultSet resultSet = null;
            try {
                statementLista = DBUtil.getConnection().prepareStatement("SELECT idTipoTicket,Nombre FROM TipoTicketTB");
                resultSet = statementLista.executeQuery();
                while (resultSet.next()) {
                    TicketTB ticketTB = new TicketTB();
                    ticketTB.setId(resultSet.getInt("idTipoTicket"));
                    ticketTB.setNombreTicket(resultSet.getString("Nombre"));
                    list.add(ticketTB);
                }
            } catch (SQLException ex) {

            } finally {
                try {
                    if (statementLista != null) {
                        statementLista.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }

            }
        }
        return list;
    }

    public static TicketTB GetTicketRuta(int idTicket) {
        TicketTB ticketTB = null;
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementLista = null;
            ResultSet resultSet = null;
            try {
                statementLista = DBUtil.getConnection().prepareStatement("SELECT idTicket,ruta FROM TicketTB WHERE tipo = ? and predeterminado = 1");
                statementLista.setInt(1, idTicket);
                resultSet = statementLista.executeQuery();
                if (resultSet.next()) {
                    ticketTB = new TicketTB();
                    ticketTB.setId(resultSet.getInt("idTicket"));
                    ticketTB.setRuta(resultSet.getString("ruta"));
                }
            } catch (SQLException ex) {

            } finally {
                try {
                    if (statementLista != null) {
                        statementLista.close();
                    }
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }

            }
        }
        return ticketTB;
    }

    public static String ChangeDefaultState(int idTicket,int tipoTicket) {
        String result = null;
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementSelect = null;
            PreparedStatement statementUpdate = null;
            PreparedStatement statementState = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementSelect = DBUtil.getConnection().prepareStatement("SELECT predeterminado FROM TicketTB WHERE tipo = ? AND predeterminado = 1");
                statementSelect.setInt(1, tipoTicket);
                if (statementSelect.executeQuery().next()) {
                    statementUpdate = DBUtil.getConnection().prepareStatement("UPDATE TicketTB SET predeterminado = 0 WHERE tipo = ?");
                    statementUpdate.setInt(1, tipoTicket);
                    statementUpdate.addBatch();

                    statementState = DBUtil.getConnection().prepareStatement("UPDATE TicketTB SET predeterminado = 1 WHERE idTicket = ?");
                    statementState.setInt(1, idTicket);
                    statementState.addBatch();

                    statementUpdate.executeBatch();
                    statementState.executeBatch();
                    DBUtil.getConnection().commit();
                    result = "updated";
                } else {
                    statementState = DBUtil.getConnection().prepareStatement("UPDATE TicketTB SET predeterminado = 1 WHERE idTicket = ?");
                    statementState.setInt(1, idTicket);
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

    public static String DeleteTicket(int idTicket) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidate = null;
            PreparedStatement statementTicket = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM TicketTB WHERE idTicket = ? and predeterminado = 1");
                statementValidate.setInt(1, idTicket);
                if (statementValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "predeterminated";
                } else {
                    statementTicket = DBUtil.getConnection().prepareStatement("delete TicketTB where idTicket = ?");
                    statementTicket.setInt(1, idTicket);
                    statementTicket.addBatch();
                    statementTicket.executeBatch();
                    DBUtil.getConnection().commit();
                    result = "deleted";
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
                    if (statementTicket != null) {
                        statementTicket.close();
                    }
                } catch (SQLException ex) {

                }
            }
        } else {
            result = "No se puedo establecer conexión con el servidor, revice y vuelva a intentarlo.";

        }
        return result;
    }

}
