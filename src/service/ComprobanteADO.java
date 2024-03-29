
package service;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class ComprobanteADO {
    
    public static String GetSerieNumeracionEspecifico(int idTipoDocumento) {
        DBUtil dbf = new DBUtil();
        CallableStatement callableStatement = null;
        try {
            dbf.dbConnect(); 
            callableStatement = dbf.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");          
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, idTipoDocumento);
            callableStatement.execute();
            return callableStatement.getString(1);

        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("ComprobanteADO GetSerieNumeracionEspecifico:" + ex.getLocalizedMessage());
        } finally {
            try {
                if (callableStatement != null) {
                    callableStatement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }

        }

        return "";
    }

}
