package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UtilidadADO {

    public static Object listUtilidadVenta(String fechaInicial, String fechaFinal, String idSuministro, int idCategoria, int idMarca, int idPresentacion) {
        String selectStmt = "{call Sp_Listar_Utilidad(?,?,?,?,?,?)}";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();
            ArrayList<Utilidad> list = new ArrayList<>();

            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, fechaInicial);
            preparedStatement.setString(2, fechaFinal);
            preparedStatement.setString(3, idSuministro);
            preparedStatement.setInt(4, idCategoria);
            preparedStatement.setInt(5, idMarca);
            preparedStatement.setInt(6, idPresentacion);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                Utilidad utilidad = new Utilidad();
                utilidad.setId(rsEmps.getRow());
                utilidad.setIdSuministro(rsEmps.getString("IdSuministro"));
                utilidad.setClave(rsEmps.getString("Clave"));
                utilidad.setNombreMarca(rsEmps.getString("NombreMarca"));
                utilidad.setCantidad(rsEmps.getDouble("Cantidad"));
                utilidad.setMedida(rsEmps.getString("UnidadCompraNombre"));

                utilidad.setCostoVenta(rsEmps.getDouble("Costo"));
                utilidad.setCostoVentaTotal(rsEmps.getDouble("CostoTotal"));

                utilidad.setPrecioVenta(rsEmps.getDouble("Precio"));
                utilidad.setPrecioVentaTotal(rsEmps.getDouble("PrecioTotal"));

                utilidad.setUtilidad(rsEmps.getDouble("Utilidad"));

                utilidad.setValorInventario(rsEmps.getBoolean("ValorInventario"));
                utilidad.setSimboloMoneda(rsEmps.getString("Simbolo"));

                list.add(utilidad);
            }
            return list;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
