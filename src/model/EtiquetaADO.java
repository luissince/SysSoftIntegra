package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EtiquetaADO {

    public static String CrudEtiquetas(EtiquetaTB etiquetaTB) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementTicket = null;
            PreparedStatement statementValidar = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                statementValidar = DBUtil.getConnection().prepareStatement("SELECT idEtiqueta FROM EtiquetaTB WHERE idEtiqueta = ?");
                statementValidar.setInt(1, etiquetaTB.getIdEtiqueta());
                if (statementValidar.executeQuery().next()) {
                    statementValidar = DBUtil.getConnection().prepareStatement("SELECT nombre FROM EtiquetaTB WHERE idEtiqueta <> ? and nombre = ?");
                    statementValidar.setInt(1, etiquetaTB.getIdEtiqueta());
                    statementValidar.setString(2, etiquetaTB.getNombre());
                    if (statementValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        statementTicket = DBUtil.getConnection().prepareStatement("UPDATE EtiquetaTB SET nombre = ?,medida = ?,ruta = ?,imagen = ? WHERE idEtiqueta = ?");
                        statementTicket.setString(1, etiquetaTB.getNombre());
                        statementTicket.setString(2, etiquetaTB.getMedida());
                        statementTicket.setString(3, etiquetaTB.getRuta());
                        statementTicket.setBytes(4, etiquetaTB.getImagen());
                        statementTicket.setInt(5, etiquetaTB.getIdEtiqueta());
                        statementTicket.addBatch();

                        statementTicket.executeBatch();
                        DBUtil.getConnection().commit();
                        result = "updated";
                    }

                } else {
                    statementValidar = DBUtil.getConnection().prepareStatement("SELECT nombre FROM EtiquetaTB WHERE nombre = ?");
                    statementValidar.setString(1, etiquetaTB.getNombre());
                    if (statementValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        statementTicket = DBUtil.getConnection().prepareStatement("INSERT INTO EtiquetaTB(nombre,tipo,medida,predeterminado,ruta,imagen)VALUES(?,?,?,?,?,?)");
                        statementTicket.setString(1, etiquetaTB.getNombre());
                        statementTicket.setInt(2, etiquetaTB.getTipo());
                        statementTicket.setString(3, etiquetaTB.getMedida());
                        statementTicket.setBoolean(4, etiquetaTB.isPredeterminado());
                        statementTicket.setString(5, etiquetaTB.getRuta());
                        statementTicket.setBytes(6, etiquetaTB.getImagen());
                        statementTicket.addBatch();
                        statementTicket.executeBatch();
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
                    if (statementTicket != null) {
                        statementTicket.close();
                    }
                    if (statementValidar != null) {
                        statementValidar.close();
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

    public static ArrayList<EtiquetaTB> ListTipoEtiquetas() {
        ArrayList<EtiquetaTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementLista = null;
            ResultSet resultSet = null;
            try {
                statementLista = DBUtil.getConnection().prepareStatement("SELECT * FROM TipoEtiquetaTB");
                resultSet = statementLista.executeQuery();
                while (resultSet.next()) {
                    EtiquetaTB etiquetaTB = new EtiquetaTB();
                    etiquetaTB.setIdEtiqueta(resultSet.getInt(1));
                    etiquetaTB.setNombre(resultSet.getString(2));
                    list.add(etiquetaTB);
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

    public static ArrayList<EtiquetaTB> ListEtiquetas(int type) {
        ArrayList<EtiquetaTB> list = new ArrayList<>();
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementLista = null;
            ResultSet resultSet = null;
            try {
                statementLista = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Etiquetas_By_Type(?)}");
                statementLista.setInt(1, type);
                resultSet = statementLista.executeQuery();
                while (resultSet.next()) {
                    EtiquetaTB etiquetaTB = new EtiquetaTB();
                    etiquetaTB.setIdEtiqueta(resultSet.getInt("idEtiqueta"));
                    etiquetaTB.setNombre(resultSet.getString("nombre"));
                    etiquetaTB.setNombreTipo(resultSet.getString("nombretipo"));
                    etiquetaTB.setPredeterminado(resultSet.getBoolean("predeterminado"));
                    etiquetaTB.setMedida(resultSet.getString("medida"));
                    etiquetaTB.setRuta(resultSet.getString("ruta"));
                    etiquetaTB.setImagen(resultSet.getBytes("imagen"));
                    list.add(etiquetaTB);
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

    public static String dropEtiqueta(int idEtiqueta) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedStatement = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                preparedStatement = DBUtil.getConnection().prepareCall("DELETE FROM EtiquetaTB WHERE idEtiqueta = ?");
                preparedStatement.setInt(1, idEtiqueta);
                preparedStatement.addBatch();
                preparedStatement.executeBatch();
                DBUtil.getConnection().commit();
                result = "removed";
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                } catch (SQLException e) {
                }
                result = ex.getLocalizedMessage();
            } finally {
                try {
                    if (preparedStatement != null) {
                        preparedStatement.close();
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

}
