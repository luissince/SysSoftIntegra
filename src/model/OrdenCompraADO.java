package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrdenCompraADO {

    public static Object ListarOrdenCompra(int opcion, String search, String fechaInicio, String fechaFinal, int posicionPagina, int filasPorPagina) {
        PreparedStatement statementOrdenCompra = null;
        PreparedStatement statementOrdenCompraCount = null;
        ResultSet resultSetOrdenCompra = null;
        try {
            DBUtil.dbConnect();
            Object[] object = new Object[2];
            ObservableList<OrdenCompraTB> empList = FXCollections.observableArrayList();
            statementOrdenCompra = DBUtil.getConnection().prepareStatement("{call Sp_Listar_OrdenCompra(?,?,?,?,?,?)}");
            statementOrdenCompra.setInt(1, opcion);
            statementOrdenCompra.setString(2, search);
            statementOrdenCompra.setString(3, fechaInicio);
            statementOrdenCompra.setString(4, fechaFinal);
            statementOrdenCompra.setInt(5, posicionPagina);
            statementOrdenCompra.setInt(6, filasPorPagina);
            resultSetOrdenCompra = statementOrdenCompra.executeQuery();
            while (resultSetOrdenCompra.next()) {
                OrdenCompraTB ordenCompraTB = new OrdenCompraTB();

                empList.add(ordenCompraTB);
            }
            object[0] = empList;

            statementOrdenCompraCount = DBUtil.getConnection().prepareStatement("{call Sp_Listar_OrdenCompra_Count(?,?,?,?)}");
            statementOrdenCompraCount.setInt(1, opcion);
            statementOrdenCompraCount.setString(2, search);
            statementOrdenCompraCount.setString(3, fechaInicio);
            statementOrdenCompraCount.setString(4, fechaFinal);
            resultSetOrdenCompra = statementOrdenCompraCount.executeQuery();
            Integer total = 0;
            if (resultSetOrdenCompra.next()) {
                total = resultSetOrdenCompra.getInt("Total");
            }
            object[1] = total;

            return object;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementOrdenCompra != null) {
                    statementOrdenCompra.close();
                }
                if (statementOrdenCompraCount != null) {
                    statementOrdenCompraCount.close();
                }
                if (resultSetOrdenCompra != null) {
                    resultSetOrdenCompra.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ObtenerOrdenCompraId() {
        return "";
    }

    public static String InsertarOrdenCompra() {
        return "";
    }

    public static String ActualizarOrdenCompra() {
        return "";
    }

    public static String EliminarOrdenCompra() {
        return "";
    }

}
