package service;

import controller.tools.Tools;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import model.KardexTB;

public class KardexADO {

    public static Object List_Kardex_By_Id_Suministro(int opcion, String value, int idAlmacen) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {

            dbf.dbConnect();
            Object[] objects = new Object[3];
            ObservableList<KardexTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Kardex_Suministro_By_Id(?,?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, value);
            preparedStatement.setInt(3, idAlmacen);

            rsEmps = preparedStatement.executeQuery();

            double cantidad = 0;
            double saldo = 0;

            while (rsEmps.next()) {
                KardexTB kardexTB = new KardexTB();
                kardexTB.setId(rsEmps.getRow());
                kardexTB.setIdArticulo(rsEmps.getString("IdSuministro"));
                kardexTB.setFecha(rsEmps.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
                kardexTB.setHora(rsEmps.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                kardexTB.setTipo(rsEmps.getShort("Tipo"));
                kardexTB.setMovimientoName(rsEmps.getString("Nombre"));
                kardexTB.setDetalle(rsEmps.getString("Detalle"));
                kardexTB.setCantidad(rsEmps.getDouble("Cantidad"));
                kardexTB.setCosto(rsEmps.getDouble("Costo"));
                kardexTB.setTotal(rsEmps.getDouble("Total"));

                cantidad = cantidad + (kardexTB.getTipo() == 1 ? kardexTB.getCantidad() : -kardexTB.getCantidad());

                Label lblCantidadEntrada = new Label(kardexTB.getTipo() == 1 ? Tools.roundingValue(kardexTB.getCantidad(), 2) : "");
                lblCantidadEntrada.getStyleClass().add("labelRobotoBold13");
                lblCantidadEntrada.setStyle("-fx-max-width:Infinity;-fx-max-height:Infinity;-fx-background-color:#c6efd0;-fx-text-fill:#297521;-fx-alignment:center-right;");

                Label lblCantidadSalida = new Label(kardexTB.getTipo() == 2 ? "-" + Tools.roundingValue(kardexTB.getCantidad(), 2) : "");
                lblCantidadSalida.getStyleClass().add("labelRobotoBold13");
                lblCantidadSalida.setStyle("-fx-max-width:Infinity;-fx-max-height:Infinity;-fx-background-color:#ffc6d1;-fx-text-fill:#890d15;-fx-alignment:center-right;");

                Label lblExistencia = new Label(Tools.roundingValue(cantidad, 2));
                lblExistencia.getStyleClass().add("labelRoboto13");
                lblExistencia.setStyle("-fx-text-fill:#020203;");

                Label lblCosto = new Label(Tools.roundingValue(kardexTB.getCosto(), 2));
                lblCosto.getStyleClass().add("labelRoboto13");
                lblCosto.setStyle("-fx-text-fill:#020203;");

                Label lblDebe = new Label(kardexTB.getTipo() == 1 ? Tools.roundingValue(kardexTB.getTotal(), 2) : "");
                lblDebe.getStyleClass().add("labelRoboto13");
                lblDebe.setStyle("-fx-text-fill:#020203;");

                Label lblHaber = new Label(kardexTB.getTipo() == 2 ? "-" + Tools.roundingValue(kardexTB.getTotal(), 2) : "");
                lblHaber.getStyleClass().add("labelRoboto13");
                lblHaber.setStyle("-fx-text-fill:#020203;");

                saldo = saldo + (kardexTB.getTipo() == 1 ? kardexTB.getTotal() : -kardexTB.getTotal());

                Label lblSaldo = new Label(Tools.roundingValue(saldo, 2));
                lblSaldo.getStyleClass().add("labelRoboto13");
                lblSaldo.setStyle("-fx-text-fill:#020203;");

                kardexTB.setLblEntreda(lblCantidadEntrada);
                kardexTB.setLblSalida(lblCantidadSalida);
                kardexTB.setLblExistencia(lblExistencia);
                kardexTB.setLblCosto(lblCosto);
                kardexTB.setLblDebe(lblDebe);
                kardexTB.setLblHaber(lblHaber);
                kardexTB.setLblSaldo(lblSaldo);

                empList.add(kardexTB);
            }

            objects[0] = empList;
            objects[1] = cantidad;
            objects[2] = saldo;
            
            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
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

}
