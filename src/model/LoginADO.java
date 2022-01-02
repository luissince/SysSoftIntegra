package model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginADO {

    public  static boolean ValidateLogin(LoginTB loginTB){
        
        String query = "select * from LoginTB where Usuario = ? and Clave = ?";
        PreparedStatement preparedStatement = null;
        try{
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(query);
            preparedStatement.setString(1,loginTB.getUsuario());
            preparedStatement.setString(2,loginTB.getClave());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }catch (SQLException e){
            System.out.println(e.getLocalizedMessage());
        }finally {
            try{
                if (preparedStatement != null)preparedStatement.close();
                DBUtil.dbDisconnect();
            }catch (SQLException ex){

            }

        }
        return false;
    }
}
