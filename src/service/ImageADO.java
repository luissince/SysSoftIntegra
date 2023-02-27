package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.ImagenTB;

public class ImageADO {

    public static ArrayList<ImagenTB> ListaImagePorIdRelacionado(int idTicket) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedImagen = null;
        ArrayList<ImagenTB> arrayList = new ArrayList<>();
        try {
            dbf.dbConnect();

            preparedImagen = dbf.getConnection().prepareStatement("SELECT Imagen,IdSubRelacion FROM ImagenTB WHERE IdRelacionado = ?");
            preparedImagen.setString(1, idTicket + "");
            ResultSet resultSet = preparedImagen.executeQuery();
            while (resultSet.next()) {
                ImagenTB imagenTB = new ImagenTB();
                imagenTB.setImagen(resultSet.getBytes("Imagen"));
                imagenTB.setIdSubRelacion(resultSet.getString("IdSubRelacion")); 
                arrayList.add(imagenTB);
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
            }
        } finally {
            try {
                if (preparedImagen != null) {
                    preparedImagen.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
            }
        }
        return arrayList;
    }

}
