package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HistorialSuministroSalidaADO {

    public static Object ListarHistorialSuministroSalida(String idVenta, String idSuministro) {
        PreparedStatement statementLista = null;
        try {
            DBUtil.dbConnect();
            ObservableList<HistorialSuministroSalidaTB> suministroSalidas = FXCollections.observableArrayList();
            statementLista = DBUtil.getConnection().prepareStatement("SELECT IdHistorialSuministroLlevar,Fecha,Hora,Cantidad,Observacion FROM HistorialSuministroLlevar WHERE IdVenta = ? AND IdSuministro = ?");
            statementLista.setString(1, idVenta);
            statementLista.setString(2, idSuministro);
            try (ResultSet rs = statementLista.executeQuery()) {
                while (rs.next()) {
                    HistorialSuministroSalidaTB historialSuministroSalida = new HistorialSuministroSalidaTB();
                    historialSuministroSalida.setId(rs.getRow());
                    historialSuministroSalida.setIdHistorialSuministroLlevar(rs.getInt("IdHistorialSuministroLlevar"));
                    historialSuministroSalida.setFecha(rs.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    historialSuministroSalida.setHora(rs.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));
                    historialSuministroSalida.setCantidad(rs.getDouble("Cantidad"));
                    historialSuministroSalida.setObservacion(rs.getString("Observacion"));
                    suministroSalidas.add(historialSuministroSalida);
                }
            }
            return suministroSalidas;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }
}
