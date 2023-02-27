package service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.LoginTB;

public class LoginADO {

    public  static boolean ValidateLogin(LoginTB loginTB){
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        try{
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("select * from LoginTB where Usuario = ? and Clave = ?");
            preparedStatement.setString(1,loginTB.getUsuario());
            preparedStatement.setString(2,loginTB.getClave());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }catch (SQLException | ClassNotFoundException e){
            System.out.println(e.getLocalizedMessage());
        }finally {
            try{
                if (preparedStatement != null)preparedStatement.close();
                dbf.dbDisconnect();
            }catch (SQLException ex){

            }

        }
        return false;
    }
}
