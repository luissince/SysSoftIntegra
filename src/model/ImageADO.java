package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ImageADO {

    public static ArrayList<ImagenTB> ListaImagePorIdRelacionado(int idTicket) {
        PreparedStatement preparedImagen = null;
        ArrayList<ImagenTB> arrayList = new ArrayList<>();
        try {
            DBUtil.dbConnect();

            preparedImagen = DBUtil.getConnection().prepareStatement("SELECT Imagen,IdSubRelacion FROM ImagenTB WHERE IdRelacionado = ?");
            preparedImagen.setString(1, idTicket + "");
            ResultSet resultSet = preparedImagen.executeQuery();
            while (resultSet.next()) {
                ImagenTB imagenTB = new ImagenTB();
                imagenTB.setImagen(resultSet.getBytes("Imagen"));
                imagenTB.setIdSubRelacion(resultSet.getString("IdSubRelacion")); 
                arrayList.add(imagenTB);
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException exr) {
            }
        } finally {
            try {
                if (preparedImagen != null) {
                    preparedImagen.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
            }
        }
        return arrayList;
    }

}
