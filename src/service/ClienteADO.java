package service;

import controller.tools.Session;
import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ClienteTB;
import model.UbigeoTB;

public class ClienteADO {

    public static String CrudCliente(ClienteTB clienteTB) {
        DBUtil dbf = new DBUtil();
        CallableStatement codigoCliente = null;
        PreparedStatement preparedCliente = null;
        PreparedStatement preparedValidation = null;

        CallableStatement codigoConductor = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            preparedValidation = dbf.getConnection()
                    .prepareStatement("SELECT IdCliente FROM ClienteTB WHERE IdCliente = ?");
            preparedValidation.setString(1, clienteTB.getIdCliente());

            if (preparedValidation.executeQuery().next()) {

                preparedValidation.close();
                preparedValidation = dbf.getConnection().prepareStatement(
                        "SELECT NumeroDocumento FROM ClienteTB WHERE IdCliente <> ? AND NumeroDocumento = ?");
                preparedValidation.setString(1, clienteTB.getIdCliente());
                preparedValidation.setString(2, clienteTB.getNumeroDocumento());

                if (preparedValidation.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                }

                preparedCliente = dbf.getConnection().prepareStatement("UPDATE ClienteTB SET\n"
                        + "TipoDocumento=?,\n"
                        + "NumeroDocumento=?,\n"
                        + "Informacion=UPPER(?),\n"
                        + "Telefono=?,\n"
                        + "Celular=?,\n"
                        + "Email=?,\n"
                        + "Direccion=?,\n"
                        + "Representante=?,\n"
                        + "Estado=?,\n"
                        + "IdMotivoTraslado=?,\n"
                        + "IdUbigeo=?,\n"
                        + "FechaModificacion=GETDATE(),\n"
                        + "HoraModificacion=GETDATE(),\n"
                        + "IdEmpleado=?\n"
                        + "WHERE IdCliente = ?");

                preparedCliente.setInt(1, clienteTB.getTipoDocumento());
                preparedCliente.setString(2, clienteTB.getNumeroDocumento());
                preparedCliente.setString(3, clienteTB.getInformacion());
                preparedCliente.setString(4, clienteTB.getTelefono());
                preparedCliente.setString(5, clienteTB.getCelular());
                preparedCliente.setString(6, clienteTB.getEmail());
                preparedCliente.setString(7, clienteTB.getDireccion());
                preparedCliente.setString(8, clienteTB.getRepresentante());
                preparedCliente.setInt(9, clienteTB.getEstado());
                preparedCliente.setInt(10, clienteTB.getIdMotivoTraslado());
                preparedCliente.setInt(11, clienteTB.getIdUbigeo());
                preparedCliente.setString(12, Session.USER_ID);
                preparedCliente.setString(13, clienteTB.getIdCliente());
                preparedCliente.addBatch();

                preparedCliente.executeBatch();
                dbf.getConnection().commit();
                return "updated";
            } else {
                preparedValidation.close();
                preparedValidation = dbf.getConnection()
                        .prepareStatement("select NumeroDocumento from ClienteTB where NumeroDocumento = ?");
                preparedValidation.setString(1, clienteTB.getNumeroDocumento());

                if (preparedValidation.executeQuery().next()) {
                    dbf.getConnection().rollback();
                    return "duplicate";
                }

                codigoCliente = dbf.getConnection().prepareCall("{? = call Fc_Cliente_Codigo_Alfanumerico()}");
                codigoCliente.registerOutParameter(1, java.sql.Types.VARCHAR);
                codigoCliente.execute();
                String idCliente = codigoCliente.getString(1);

                preparedCliente = dbf.getConnection().prepareStatement("INSERT INTO ClienteTB(\n"
                        + "IdCliente,\n"
                        + "TipoDocumento,\n"
                        + "NumeroDocumento,\n"
                        + "Informacion,\n"
                        + "Telefono,\n"
                        + "Celular,\n"
                        + "Email,\n"
                        + "Direccion,\n"
                        + "Representante,\n"
                        + "Estado,\n"
                        + "Predeterminado,\n"
                        + "Sistema,\n"
                        + "IdMotivoTraslado,\n"
                        + "IdUbigeo,\n"
                        + "FechaCreacion,\n"
                        + "HoraCreacion,\n"
                        + "IdEmpleado)"
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),GETDATE(),?)");
                preparedCliente.setString(1, idCliente);
                preparedCliente.setInt(2, clienteTB.getTipoDocumento());
                preparedCliente.setString(3, clienteTB.getNumeroDocumento());
                preparedCliente.setString(4, clienteTB.getInformacion());
                preparedCliente.setString(5, clienteTB.getTelefono());
                preparedCliente.setString(6, clienteTB.getCelular());
                preparedCliente.setString(7, clienteTB.getEmail());
                preparedCliente.setString(8, clienteTB.getDireccion());
                preparedCliente.setString(9, clienteTB.getRepresentante());
                preparedCliente.setInt(10, clienteTB.getEstado());
                preparedCliente.setBoolean(11, clienteTB.isPredeterminado());
                preparedCliente.setBoolean(12, clienteTB.isSistema());
                preparedCliente.setInt(13, clienteTB.getIdMotivoTraslado());
                preparedCliente.setInt(14, clienteTB.getIdUbigeo());
                preparedCliente.setString(15, Session.USER_ID);
                preparedCliente.addBatch();

                preparedCliente.executeBatch();
                dbf.getConnection().commit();
                return "registered";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException exr) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (codigoCliente != null) {
                    codigoCliente.close();
                }
                if (preparedValidation != null) {
                    preparedValidation.close();
                }
                if (preparedCliente != null) {
                    preparedCliente.close();
                }
                if (codigoConductor != null) {
                    codigoConductor.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ListCliente(String buscar, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatementCount = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            Object[] objects = new Object[2];

            ObservableList<ClienteTB> empList = FXCollections.observableArrayList();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Clientes(?,?,?)}");
            preparedStatement.setString(1, buscar);
            preparedStatement.setInt(2, posicionPagina);
            preparedStatement.setInt(3, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setId(rsEmps.getRow() + posicionPagina);
                clienteTB.setIdCliente(rsEmps.getString("IdCliente"));
                clienteTB.setTipoDocumentoName(rsEmps.getString("TipoDocumento"));
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Informacion"));
                clienteTB.setTelefono(rsEmps.getString("Telefono"));
                clienteTB.setCelular(rsEmps.getString("Celular"));
                clienteTB.setDireccion(rsEmps.getString("Direccion"));
                clienteTB.setRepresentante(rsEmps.getString("Representante"));
                clienteTB.setEstadoName(rsEmps.getString("Estado"));
                clienteTB.setPredeterminado(rsEmps.getBoolean("Predeterminado"));
                clienteTB.setImagePredeterminado(rsEmps.getBoolean("Predeterminado")
                        ? new ImageView(new Image("/view/image/checked.png", 22, 22, false, false))
                        : new ImageView(new Image("/view/image/unchecked.png", 22, 22, false, false)));

                empList.add(clienteTB);
            }

            rsEmps.close();
            preparedStatementCount = dbf.getConnection().prepareStatement("{CALL Sp_Listar_Clientes_Count(?)}");
            preparedStatementCount.setString(1, buscar);
            rsEmps = preparedStatementCount.executeQuery();
            Integer cantidadTotal = 0;
            if (rsEmps.next()) {
                cantidadTotal = rsEmps.getInt("Total");
            }

            objects[0] = empList;
            objects[1] = cantidadTotal;

            return objects;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (preparedStatementCount != null) {
                    preparedStatementCount.close();
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

    public static ObservableList<ClienteTB> ListClienteVenta(String value) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        ObservableList<ClienteTB> empList = FXCollections.observableArrayList();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Listar_Clientes_Venta(?)}");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setId(rsEmps.getRow());
                clienteTB.setIdCliente(rsEmps.getString("IdCliente"));
                clienteTB.setTipoDocumentoName(rsEmps.getString("Documento"));
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Informacion"));
                clienteTB.setDireccion(rsEmps.getString("Direccion"));
                empList.add(clienteTB);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ListClienteVenta - La operación de selección de SQL ha fallado: " + e);

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

            }
        }
        return empList;
    }

    public static Object GetByIdCliente(String idCliente) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection().prepareStatement("{call Sp_Get_Cliente_By_Id(?)}");
            preparedStatement.setString(1, idCliente);
            rsEmps = preparedStatement.executeQuery();

            if (rsEmps.next()) {
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(rsEmps.getString("IdCliente"));
                clienteTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Informacion"));
                clienteTB.setTelefono(rsEmps.getString("Telefono"));
                clienteTB.setCelular(rsEmps.getString("Celular"));
                clienteTB.setEmail(rsEmps.getString("Email"));
                clienteTB.setDireccion(rsEmps.getString("Direccion"));
                clienteTB.setEstado(rsEmps.getInt("Estado"));
                clienteTB.setIdMotivoTraslado(rsEmps.getInt("IdMotivoTraslado"));
                clienteTB.setIdUbigeo(rsEmps.getInt("IdUbigeo"));

                UbigeoTB ubigeoTB = new UbigeoTB();
                ubigeoTB.setIdUbigeo(rsEmps.getInt("IdUbigeo"));
                ubigeoTB.setUbigeo(rsEmps.getString("CodigoUbigeo"));
                ubigeoTB.setDepartamento(rsEmps.getString("Departamento"));
                ubigeoTB.setProvincia(rsEmps.getString("Provincia"));
                ubigeoTB.setDistrito(rsEmps.getString("Distrito"));
                clienteTB.setUbigeoTB(ubigeoTB);

                return clienteTB;
            } else {
                throw new Exception("Datos no encontrados.");
            }
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

    public static List<ClienteTB> GetSearchComboBoxCliente(int opcion, String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<ClienteTB> clienteTBs = new ArrayList<>();
        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Cliente_Informacion_NumeroDocumento(?,?)}");
            preparedStatement.setInt(1, opcion);
            preparedStatement.setString(2, search);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(rsEmps.getString("IdCliente"));
                clienteTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Informacion"));
                clienteTB.setDireccion(rsEmps.getString("Direccion"));
                clienteTB.setCelular(rsEmps.getString("Celular"));

                clienteTBs.add(clienteTB);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error en GetSearchComboBoxCliente(): " + e);
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

            }
        }
        return clienteTBs;
    }

    public static Object GetSearchClienteNumeroDocumento(short opcion, String search) {
        DBUtil dbf = new DBUtil();
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;

        try {
            dbf.dbConnect();
            preparedStatement = dbf.getConnection()
                    .prepareStatement("{call Sp_Obtener_Cliente_Informacion_NumeroDocumento(?,?)}");
            preparedStatement.setShort(1, opcion);
            preparedStatement.setString(2, search);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                ClienteTB clienteTB = new ClienteTB();
                clienteTB.setIdCliente(rsEmps.getString("IdCliente"));
                clienteTB.setTipoDocumento(rsEmps.getInt("TipoDocumento"));
                clienteTB.setNumeroDocumento(rsEmps.getString("NumeroDocumento"));
                clienteTB.setInformacion(rsEmps.getString("Informacion"));
                clienteTB.setCelular(rsEmps.getString("Celular"));
                clienteTB.setEmail(rsEmps.getString("Email"));
                clienteTB.setDireccion(rsEmps.getString("Direccion"));

                return clienteTB;
            }
            throw new Exception("No se encontro los datos.");
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

    public static ClienteTB GetClientePredetermined() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statement = null;
        ClienteTB clienteTB = null;
        try {
            dbf.dbConnect();
            statement = dbf.getConnection().prepareStatement(
                    "SELECT ci.IdCliente,ci.TipoDocumento,ci.Informacion, ci.NumeroDocumento, ci.Celular,ci.Email,ci.Direccion FROM ClienteTB AS ci WHERE Predeterminado = 1");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                clienteTB = new ClienteTB();
                clienteTB.setIdCliente(resultSet.getString("IdCliente"));
                clienteTB.setTipoDocumento(resultSet.getInt("TipoDocumento"));
                clienteTB.setInformacion(resultSet.getString("Informacion"));
                clienteTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                clienteTB.setCelular(resultSet.getString("Celular"));
                clienteTB.setEmail(resultSet.getString("Email"));
                clienteTB.setDireccion(resultSet.getString("Direccion"));
            }
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("Error Cliente: " + ex.getLocalizedMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException e) {
                System.out.println("Error Moneda: " + e.getLocalizedMessage());
            }
        }
        return clienteTB;
    }

    public static String RemoveCliente(String idCliente) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementCliente = null;

        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM VentaTB WHERE Cliente = ?");
            statementValidate.setString(1, idCliente);

            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "venta";
            }

            statementValidate.close();
            statementValidate = dbf.getConnection()
                    .prepareStatement("SELECT * FROM ClienteTB WHERE IdCliente = ? AND Sistema = 1");
            statementValidate.setString(1, idCliente);

            if (statementValidate.executeQuery().next()) {
                dbf.getConnection().rollback();
                return "sistema";
            }

            statementCliente = dbf.getConnection()
                    .prepareStatement("DELETE FROM ClienteTB WHERE IdCliente = ?");
            statementCliente.setString(1, idCliente);
            statementCliente.addBatch();

            statementCliente.executeBatch();
            dbf.getConnection().commit();
            return "deleted";
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementCliente != null) {
                    statementCliente.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getMessage();
            }
        }
    }

    public static String ChangeDefaultState(boolean state, String idCliente) {
        DBUtil dbf = new DBUtil();
        String result = null;

        PreparedStatement statementSelect = null;
        PreparedStatement statementUpdate = null;
        PreparedStatement statementState = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);
            statementSelect = dbf.getConnection()
                    .prepareStatement("SELECT Predeterminado FROM ClienteTB WHERE Predeterminado = 1");
            if (statementSelect.executeQuery().next()) {
                statementUpdate = dbf.getConnection()
                        .prepareStatement("UPDATE ClienteTB SET Predeterminado = 0 WHERE Predeterminado = 1");
                statementUpdate.addBatch();

                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE ClienteTB SET Predeterminado = ? WHERE IdCliente = ?");
                statementState.setBoolean(1, state);
                statementState.setString(2, idCliente);
                statementState.addBatch();

                statementUpdate.executeBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            } else {
                statementState = dbf.getConnection()
                        .prepareStatement("UPDATE ClienteTB SET Predeterminado = ? WHERE IdCliente = ?");
                statementState.setBoolean(1, state);
                statementState.setString(2, idCliente);
                statementState.addBatch();
                statementState.executeBatch();
                dbf.getConnection().commit();
                result = "updated";
            }

        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                result = ex.getLocalizedMessage();
            } catch (SQLException e) {
                result = e.getLocalizedMessage();
            }

        } finally {
            try {
                if (statementSelect != null) {
                    statementSelect.close();
                }
                if (statementUpdate != null) {
                    statementUpdate.close();
                }
                if (statementState != null) {
                    statementState.close();
                }
                dbf.dbDisconnect();
                ClienteTB clienteTB = ClienteADO.GetClientePredetermined();
                if (clienteTB != null) {
                    Session.CLIENTE_ID = clienteTB.getIdCliente();
                    Session.CLIENTE_TIPO_DOCUMENTO = clienteTB.getTipoDocumento();
                    Session.CLIENTE_DATOS = clienteTB.getInformacion();
                    Session.CLIENTE_NUMERO_DOCUMENTO = clienteTB.getNumeroDocumento();
                    Session.CLIENTE_CELULAR = clienteTB.getCelular();
                    Session.CLIENTE_EMAIL = clienteTB.getEmail();
                    Session.CLIENTE_DIRECCION = clienteTB.getDireccion();
                } else {
                    Session.CLIENTE_ID = "";
                    Session.CLIENTE_TIPO_DOCUMENTO = 0;
                    Session.CLIENTE_DATOS = "";
                    Session.CLIENTE_NUMERO_DOCUMENTO = "";
                    Session.CLIENTE_CELULAR = "";
                    Session.CLIENTE_EMAIL = "";
                    Session.CLIENTE_DIRECCION = "";
                }
            } catch (SQLException ex) {
                result = ex.getLocalizedMessage();
            }
        }
        return result;

    }

    public static ArrayList<ClienteTB> ListarClienteInformacion() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ArrayList<ClienteTB> arrayList = new ArrayList<>();

        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection().prepareStatement("SELECT NumeroDocumento,Informacion FROM ClienteTB");
            try (ResultSet resultSet = statementLista.executeQuery()) {
                while (resultSet.next()) {
                    ClienteTB clienteTB = new ClienteTB();
                    clienteTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                    clienteTB.setInformacion(resultSet.getString("Informacion"));
                    clienteTB.setTipoSelect(true);
                    arrayList.add(clienteTB);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return arrayList;
    }

    public static ArrayList<String> ListarClienteNumeroDocumento() {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementLista = null;
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            dbf.dbConnect();
            statementLista = dbf.getConnection().prepareStatement("SELECT NumeroDocumento FROM ClienteTB");
            try (ResultSet resultSet = statementLista.executeQuery()) {
                while (resultSet.next()) {
                    arrayList.add(resultSet.getString("NumeroDocumento"));
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
        } finally {
            try {
                if (statementLista != null) {
                    statementLista.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return arrayList;
    }

}
