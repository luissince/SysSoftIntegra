package model;

import controller.tools.Tools;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MermaADO {

    public static Object ListarMerma(int opcion, String buscar, int tipoMerma, int posicionPagina, int filasPorPagina) {
        PreparedStatement statementMerma = null;
        PreparedStatement statementDetalle = null;
        ResultSet resultSet = null;

        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];

            statementMerma = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Merma(?,?,?,?,?)}");
            statementMerma.setInt(1, opcion);
            statementMerma.setString(2, buscar);
            statementMerma.setInt(3, tipoMerma);
            statementMerma.setInt(4, posicionPagina);
            statementMerma.setInt(5, filasPorPagina);
            resultSet = statementMerma.executeQuery();
            ObservableList<MermaTB> empList = FXCollections.observableArrayList();
            while (resultSet.next()) {
                MermaTB mermaTB = new MermaTB();
                mermaTB.setId(resultSet.getRow() + posicionPagina);
                mermaTB.setIdMerma(resultSet.getString("IdMerma"));
                mermaTB.setCantidad(resultSet.getDouble("Cantidad"));
                mermaTB.setCosto(resultSet.getDouble("Costo"));
                mermaTB.setTipoMermaNombre(resultSet.getString("TipoMerma"));

                ProduccionTB produccionTB = new ProduccionTB();
                produccionTB.setFechaInicio(resultSet.getDate("FechaInico").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                produccionTB.setHoraInicio(resultSet.getTime("HoraInicio").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                mermaTB.setProduccionTB(produccionTB);

                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(resultSet.getString("IdSuministro"));
                suministroTB.setClave(resultSet.getString("Clave"));
                suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                suministroTB.setUnidadCompraName(resultSet.getString("Unidad"));
                mermaTB.setSuministroTB(suministroTB);

                empList.add(mermaTB);
            }
            objects[0] = empList;

            statementDetalle = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Merma_Count(?,?,?)}");
            statementDetalle.setInt(1, opcion);
            statementDetalle.setString(2, buscar);
            statementDetalle.setInt(3, tipoMerma);
            resultSet = statementDetalle.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSet.next()) {
                cantidadTotal = resultSet.getInt("Total");
            }
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMerma != null) {
                    statementMerma.close();
                }
                if (statementDetalle != null) {
                    statementDetalle.close();
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

    public static Object ReporteMerma(int tipoMerma,String idSuministro,boolean agrupar) {
        PreparedStatement statementMerma = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            ArrayList<MermaTB> mermaTBs = new ArrayList();
            statementMerma = DBUtil.getConnection().prepareStatement("{CALL Sp_Reporte_General_Merma(?,?,?)}");
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
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementMerma != null) {
                    statementMerma.close();
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

}
