package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.MermaTB;
import model.ProduccionTB;
import model.SuministroTB;

public class MermaADO {

    public static Object ListarMerma(int opcion, String buscar, int tipoMerma, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementMerma = null;
        PreparedStatement statementDetalle = null;
        ResultSet resultSetMerma = null;
        ResultSet resultSetDetalle = null;

        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];

            statementMerma = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Merma(?,?,?,?,?)}");
            statementMerma.setInt(1, opcion);
            statementMerma.setString(2, buscar);
            statementMerma.setInt(3, tipoMerma);
            statementMerma.setInt(4, posicionPagina);
            statementMerma.setInt(5, filasPorPagina);
            resultSetMerma = statementMerma.executeQuery();
            ObservableList<MermaTB> empList = FXCollections.observableArrayList();
            while (resultSetMerma.next()) {
                MermaTB mermaTB = new MermaTB();
                mermaTB.setId(resultSetMerma.getRow() + posicionPagina);
                mermaTB.setIdMerma(resultSetMerma.getString("IdMerma"));
                mermaTB.setCantidad(resultSetMerma.getDouble("Cantidad"));
                mermaTB.setCosto(resultSetMerma.getDouble("Costo"));
                mermaTB.setTipoMermaNombre(resultSetMerma.getString("TipoMerma"));

                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setFechaInicio(resultSetMerma.getDate("FechaInico").toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setHoraInicio(resultSetMerma.getTime("HoraInicio").toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                mermaTB.setProduccionTB(produccionTB);

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(resultSetMerma.getString("IdSuministro"));
                suministroTB.setClave(resultSetMerma.getString("Clave"));
                suministroTB.setNombreMarca(resultSetMerma.getString("NombreMarca"));
                suministroTB.setUnidadCompraName(resultSetMerma.getString("Unidad"));
                mermaTB.setSuministroTB(suministroTB);

                empList.add(mermaTB);
            }
            objects[0] = empList;

            statementDetalle = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Merma_Count(?,?,?)}");
            statementDetalle.setInt(1, opcion);
            statementDetalle.setString(2, buscar);
            statementDetalle.setInt(3, tipoMerma);
            resultSetDetalle = statementDetalle.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSetDetalle.next()) {
                cantidadTotal = resultSetDetalle.getInt("Total");
            }
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMerma != null) {
                    statementMerma.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
                }
                if (resultSetMerma != null) {
                    resultSetMerma.close();
                }
                if (resultSetDetalle != null) {
                    resultSetDetalle.close();
                }

                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ReporteMerma(int tipoMerma, String idSuministro, boolean agrupar) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementMerma = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            ArrayList<MermaTB> mermaTBs = new ArrayList<>();
            statementMerma = dbf.getConnection().prepareStatement("{CALL Sp_Reporte_General_Merma(?,?,?)}");
            statementMerma.setInt(1, tipoMerma);
            statementMerma.setString(2, idSuministro);
            statementMerma.setBoolean(3, agrupar);
            resultSet = statementMerma.executeQuery();
            while (resultSet.next()) {
                MermaTB mermaTB = new MermaTB();
                mermaTB.setId(resultSet.getRow());
                mermaTB.setProductoReporte(resultSet.getString("Clave") + "\n" + resultSet.getString("NombreMarca"));
                mermaTB.setTipoMermaReporte(resultSet.getString("TipoMerma"));
                mermaTB.setCostoReporte(resultSet.getDouble("Costo"));
                mermaTB.setCantidadReporte(resultSet.getDouble("Cantidad"));
                mermaTB.setUnidadReporte(resultSet.getString("Unidad"));
                mermaTBs.add(mermaTB);
            }
            return mermaTBs;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMerma != null) {
                    statementMerma.close();
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
