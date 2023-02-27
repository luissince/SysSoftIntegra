package service;

import controller.tools.ImageViewTicket;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.scene.layout.HBox;
import model.TicketTB;

public class TicketADO {

    public static String CrudTicket(TicketTB ticketTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement callableTicket = null;
        PreparedStatement statementValidar = null;
        PreparedStatement statementTicket = null;
        PreparedStatement statementImagen = null;
        PreparedStatement statementImagenBorrar = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidar = dbf.getConnection().prepareStatement("SELECT idTicket FROM TicketTB WHERE idTicket = ?");
            statementValidar.setInt(1, ticketTB.getId());
            if (statementValidar.executeQuery().next()) {
                statementValidar.close();
                statementValidar = dbf.getConnection()
                        .prepareStatement("SELECT nombre FROM TicketTB WHERE idTicket <> ? and nombre = ?");
                statementValidar.setInt(1, ticketTB.getId());
                statementValidar.setString(2, ticketTB.getNombreTicket());
                if (statementValidar.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {

                    statementTicket = dbf.getConnection()
                            .prepareStatement("UPDATE TicketTB SET nombre = ?,tipo=?,ruta = ? WHERE idTicket = ?");
                    statementTicket.setString(1, ticketTB.getNombreTicket());
                    statementTicket.setInt(2, ticketTB.getTipo());
                    statementTicket.setString(3, ticketTB.getRuta());
                    statementTicket.setInt(4, ticketTB.getId());
                    statementTicket.addBatch();

                    statementImagenBorrar = dbf.getConnection()
                            .prepareStatement("DELETE FROM ImagenTB WHERE IdRelacionado = ?");
                    statementImagenBorrar.setString(1, ticketTB.getId() + "");
                    statementImagenBorrar.addBatch();

                    statementImagen = dbf.getConnection()
                            .prepareStatement("INSERT INTO ImagenTB(Imagen,IdRelacionado,IdSubRelacion)VALUES(?,?,?)");

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
                    dbf.getConnection().commit();
                    return "updated";
                }

            } else {
                statementValidar.close();
                statementValidar = dbf.getConnection().prepareStatement("SELECT nombre FROM TicketTB WHERE nombre = ?");
                statementValidar.setString(1, ticketTB.getNombreTicket());
                if (statementValidar.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                } else {

                    callableTicket = dbf.getConnection().prepareCall("{? = call Fc_Ticket_Codigo_Numerico()}");
                    callableTicket.registerOutParameter(1, java.sql.Types.VARCHAR);
                    callableTicket.execute();
                    int idTicket = callableTicket.getInt(1);

                    statementTicket = dbf.getConnection().prepareStatement(
                            "INSERT INTO TicketTB(idTicket,nombre,tipo,predeterminado,ruta)VALUES(?,?,?,?,?)");
                    statementTicket.setInt(1, idTicket);
                    statementTicket.setString(2, ticketTB.getNombreTicket());
                    statementTicket.setInt(3, ticketTB.getTipo());
                    statementTicket.setBoolean(4, false);
                    statementTicket.setString(5, ticketTB.getRuta());
                    statementTicket.addBatch();

                    statementImagen = dbf.getConnection()
                            .prepareStatement("INSERT INTO ImagenTB(Imagen,IdRelacionado,IdSubRelacion)VALUES(?,?,?)");

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
                    dbf.getConnection().commit();
                    return "registered";
                }
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
            }
            return ex.getLocalizedMessage();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String ClonarTicket(TicketTB ticketTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementTicket = null;
        PreparedStatement statementValue = null;
        PreparedStatement statementImagen = null;
        CallableStatement callableTicket = null;
        ResultSet resultSet = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(true);

            statementValue = dbf.getConnection().prepareStatement("SELECT * FROM TicketTB WHERE idTicket = ?");
            statementValue.setInt(1, ticketTB.getIdTicket());
            resultSet = statementValue.executeQuery();
            if (resultSet.next()) {
                callableTicket = dbf.getConnection().prepareCall("{? = call Fc_Ticket_Codigo_Numerico()}");
                callableTicket.registerOutParameter(1, java.sql.Types.VARCHAR);
                callableTicket.execute();
                int idTicket = callableTicket.getInt(1);

                statementTicket = dbf.getConnection().prepareStatement(
                        "INSERT INTO TicketTB(idTicket,nombre,tipo,predeterminado,ruta)VALUES(?,?,?,?,?)");
                statementTicket.setInt(1, idTicket);
                statementTicket.setString(2, ticketTB.getNombreTicket());
                statementTicket.setInt(3, ticketTB.getTipo());
                statementTicket.setBoolean(4, false);
                statementTicket.setString(5, resultSet.getString("ruta"));
                statementTicket.addBatch();

                statementValue.close();
                statementValue = dbf.getConnection()
                        .prepareStatement("SELECT Imagen,IdSubRelacion FROM ImagenTB WHERE IdRelacionado = ?");
                statementValue.setInt(1, ticketTB.getIdTicket());

                resultSet = statementValue.executeQuery();

                statementImagen = dbf.getConnection()
                        .prepareStatement("INSERT INTO ImagenTB(Imagen,IdRelacionado,IdSubRelacion)VALUES(?,?,?)");
                while (resultSet.next()) {
                    statementImagen.setBytes(1, resultSet.getBytes("Imagen"));
                    statementImagen.setString(2, idTicket + "");
                    statementImagen.setString(3, resultSet.getString("IdSubRelacion"));
                    statementImagen.addBatch();
                }

                statementTicket.executeBatch();
                statementImagen.executeBatch();
                dbf.getConnection().commit();
                return "inserted";
            } else {
                dbf.getConnection().rollback();
                return "No se pudo clonar por problemas del id intente nuevamente.";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException ex1) {
                return ex1.getLocalizedMessage();
            }
        } finally {
            try {
                if (callableTicket != null) {
                    callableTicket.close();
                }
                if (statementTicket != null) {
                    statementTicket.close();
                }
                if (statementValue != null) {
                    statementValue.close();
                }
                if (statementImagen != null) {
                    statementImagen.close();
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

    public static ArrayList<TicketTB> ListTicketOpcion(int tipoTicket, boolean todos) {
        DBUtil dbf = new DBUtil();
        ArrayList<TicketTB> list = new ArrayList<>();

        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            statementLista = dbf.getConnection().prepareStatement("{call Sp_Listar_Ticket_By_Tipo_Opcion(?,?)}");
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
        } catch (SQLException | ClassNotFoundException ex) {

        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }

        }

        return list;
    }

    public static Object ListTicket() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            ArrayList<TicketTB> list = new ArrayList<>();
            statementLista = dbf.getConnection().prepareStatement("SELECT idTicket,nombre FROM TicketTB");
            resultSet = statementLista.executeQuery();
            while (resultSet.next()) {
                TicketTB ticketTB = new TicketTB();
                ticketTB.setIdTicket(resultSet.getInt("idTicket"));
                ticketTB.setNombreTicket(resultSet.getString("nombre"));
                list.add(ticketTB);
            }

            return list;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
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

    public static Object ListTipoTicket() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();

            ArrayList<TicketTB> list = new ArrayList<>();

            statementLista = dbf.getConnection().prepareStatement("SELECT idTipoTicket,Nombre FROM TipoTicketTB");
            resultSet = statementLista.executeQuery();
            while (resultSet.next()) {
                TicketTB ticketTB = new TicketTB();
                ticketTB.setId(resultSet.getInt("idTipoTicket"));
                ticketTB.setNombreTicket(resultSet.getString("Nombre"));
                list.add(ticketTB);
            }

            return list;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
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

    public static Object GetTicketRuta(int idTicket) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection()
                    .prepareStatement("SELECT idTicket,ruta FROM TicketTB WHERE tipo = ? and predeterminado = 1");
            statementLista.setInt(1, idTicket);
            resultSet = statementLista.executeQuery();
            if (resultSet.next()) {
                TicketTB ticketTB = new TicketTB();
                ticketTB.setId(resultSet.getInt("idTicket"));
                ticketTB.setRuta(resultSet.getString("ruta"));
                return ticketTB;
            } else {
                return null;
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
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

    public static Object GetTicketRutaById(int idTicket) {
        DBUtil dbf = new DBUtil();

        PreparedStatement statementLista = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection()
                    .prepareStatement("SELECT idTicket,ruta FROM TicketTB WHERE idTicket = ?");
            statementLista.setInt(1, idTicket);
            resultSet = statementLista.executeQuery();
            if (resultSet.next()) {
                TicketTB ticketTB = new TicketTB();
                ticketTB.setId(resultSet.getInt("idTicket"));
                ticketTB.setRuta(resultSet.getString("ruta"));
                return ticketTB;
            } else {
                return null;
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
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

    public static String ChangeDefaultState(int idTicket, int tipoTicket) {
        DBUtil dbf = new DBUtil();
        String result = null;

        PreparedStatement statementSelect = null;
        PreparedStatement statementUpdate = null;
        PreparedStatement statementState = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementSelect = dbf.getConnection()
                    .prepareStatement("SELECT predeterminado FROM TicketTB WHERE tipo = ? AND predeterminado = 1");
            statementSelect.setInt(1, tipoTicket);
            if (statementSelect.executeQuery().next()) {
                statementUpdate = dbf.getConnection()
                        .prepareStatement("UPDATE TicketTB SET predeterminado = 0 WHERE tipo = ?");
                statementUpdate.setInt(1, tipoTicket);
                statementUpdate.addBatch();

                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE TicketTB SET predeterminado = 1 WHERE idTicket = ?");
                statementState.setInt(1, idTicket);
                statementState.addBatch();

                statementUpdate.executeBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            } else {
                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE TicketTB SET predeterminado = 1 WHERE idTicket = ?");
                statementState.setInt(1, idTicket);
                statementState.addBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }

        return result;
    }

    public static String DeleteTicket(int idTicket) {
        DBUtil dbf = new DBUtil();
        String result = "";

        PreparedStatement statementValidate = null;
        PreparedStatement statementTicket = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM TicketTB WHERE idTicket = ? and predeterminado = 1");
            statementValidate.setInt(1, idTicket);
            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                result = "predeterminated";
            } else {
                statementTicket = dbf.getConnection().prepareStatement("delete TicketTB where idTicket = ?");
                statementTicket.setInt(1, idTicket);
                statementTicket.addBatch();
                statementTicket.executeBatch();
                dbf.getConnection().commit();
                result = "deleted";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
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
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }

        return result;
    }

}
