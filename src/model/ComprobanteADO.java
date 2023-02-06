
package model;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class ComprobanteADO {
    
    public static String GetSerieNumeracionEspecifico(int idTipoDocumento) {
        CallableStatement callableStatement = null;
        try {
            DBUtil.dbConnect();
            callableStatement = DBUtil.getConnection().prepareCall("{? = call Fc_Serie_Numero_Venta(?)}");          
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.setInt(2, idTipoDocumento);
            callableStatement.execute();
            return callableStatement.getString(1);

        } catch (SQLException ex) {
            System.out.println("ComprobanteADO GetSerieNumeracionEspecifico:" + ex.getLocalizedMessage());
        } finally {
            try {
                if (callableStatement != null) {
                    callableStatement.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }

        }

        return "";
    }

}
