package model;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AlmacenADO {

    public static Object ListarAlmacen(String buscar, int posicionPagina, int filasPorPagina) {
        PreparedStatement statementAlmacen = null;
        ResultSet resultSet = null;
        try {
            DBUtil.dbConnect();
            Object[] object = new Object[2];

            ObservableList<AlmacenTB> empList = FXCollections.observableArrayList();
            statementAlmacen = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Almacen(?,?,?)}");
            statementAlmacen.setString(1, buscar);
            statementAlmacen.setInt(2, posicionPagina);
            statementAlmacen.setInt(3, filasPorPagina);
            resultSet = statementAlmacen.executeQuery();
            while (resultSet.next()) {
                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setId(resultSet.getRow() + posicionPagina);
                almacenTB.setIdAlmacen(resultSet.getInt("IdAlmacen"));
                almacenTB.setNombre(resultSet.getString("Nombre"));

                UbigeoTB ubigeoTB = new UbigeoTB();
                ubigeoTB.setIdUbigeo(resultSet.getInt("IdUbigeo"));
                ubigeoTB.setUbigeo(resultSet.getString("Ubigeo"));
                ubigeoTB.setDepartamento(resultSet.getString("Departamento"));
                ubigeoTB.setProvincia(resultSet.getString("Provincia"));
                ubigeoTB.setDistrito(resultSet.getString("Distrito"));
                almacenTB.setUbigeoTB(ubigeoTB);

                almacenTB.setDireccion(resultSet.getString("Direccion"));
                almacenTB.setFecha(resultSet.getDate("Fecha").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                almacenTB.setHora(resultSet.getTime("Hora").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss a")));

                empList.add(almacenTB);
            }
            object[0] = empList;

            statementAlmacen = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Almacen_Count(?)}");
            statementAlmacen.setString(1, buscar);
            resultSet = statementAlmacen.executeQuery();
            Integer cantidadTotal = 0;
            if (resultSet.next()) {
                cantidadTotal = resultSet.getInt("Total");
            }
            object[1] = cantidadTotal;

            return object;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementAlmacen != null) {
                    statementAlmacen.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ex) {

            }
        }
    }

    public static String CrudAlmacen(AlmacenTB almacenTB) {
        PreparedStatement statementValidate = null;
        PreparedStatement statementAlmacen = null;
        CallableStatement statementCodigoAlmacen = null;
        PreparedStatement statementProducto = null;
        PreparedStatement statementCantidad = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM AlmacenTB WHERE IdAlmacen = ?");
            statementValidate.setInt(1, almacenTB.getIdAlmacen());
            if (statementValidate.executeQuery().next()) {
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM AlmacenTB WHERE IdAlmacen <> ? AND Nombre = ?");
                statementValidate.setInt(1, almacenTB.getIdAlmacen());
                statementValidate.setString(2, almacenTB.getNombre());
                if (statementValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    return "name";
                } else {
                    statementAlmacen = DBUtil.getConnection().prepareStatement("UPDATE AlmacenTB SET Nombre = ?,IdUbigeo = ?,Direccion = ?,Fecha = ?,Hora = ?,IdUsuario = ? WHERE IdAlmacen = ?");
                    statementAlmacen.setString(1, almacenTB.getNombre());
                    statementAlmacen.setInt(2, almacenTB.getIdUbigeo());
                    statementAlmacen.setString(3, almacenTB.getDireccion());
                    statementAlmacen.setString(4, almacenTB.getFecha());
                    statementAlmacen.setString(5, almacenTB.getHora());
                    statementAlmacen.setString(6, almacenTB.getIdUsuario());
                    statementAlmacen.setInt(7, almacenTB.getIdAlmacen());
                    statementAlmacen.addBatch();

                    statementAlmacen.executeBatch();
                    DBUtil.getConnection().commit();
                    return "updated";
                }

            } else {
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM AlmacenTB WHERE Nombre = ?");
                statementValidate.setString(1, almacenTB.getNombre());
                if (statementValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    return "name";
                } else {
                    statementCodigoAlmacen = DBUtil.getConnection().prepareCall("{? = call Fc_Almacen_Codigo_Numerico()}");
                    statementCodigoAlmacen.registerOutParameter(1, java.sql.Types.INTEGER);
                    statementCodigoAlmacen.execute();
                    int idAlmacen = statementCodigoAlmacen.getInt(1);

                    statementAlmacen = DBUtil.getConnection().prepareStatement("INSERT INTO AlmacenTB(IdAlmacen,Nombre,IdUbigeo,Direccion,Fecha,Hora,IdUsuario) VALUES(?,?,?,?,?,?,?)");
                    statementAlmacen.setInt(1, idAlmacen);
                    statementAlmacen.setString(2, almacenTB.getNombre());
                    statementAlmacen.setInt(3, almacenTB.getIdUbigeo());
                    statementAlmacen.setString(4, almacenTB.getDireccion());
                    statementAlmacen.setString(5, almacenTB.getFecha());
                    statementAlmacen.setString(6, almacenTB.getHora());
                    statementAlmacen.setString(7, almacenTB.getIdUsuario());
                    statementAlmacen.addBatch();

                    statementProducto = DBUtil.getConnection().prepareStatement("SELECT IdSuministro FROM SuministroTB");
                    ResultSet resultSet = statementProducto.executeQuery();
                    statementCantidad = DBUtil.getConnection().prepareStatement("INSERT INTO CantidadTB(IdAlmacen,IdSuministro,StockMinimo,StockMaximo,Cantidad) VALUES(?,?,?,?,?)");
                    while (resultSet.next()) {
                        statementCantidad.setInt(1, idAlmacen);
                        statementCantidad.setString(2, resultSet.getString("IdSuministro"));
                        statementCantidad.setDouble(3, 0);
                        statementCantidad.setDouble(4, 0);
                        statementCantidad.setDouble(5, 0);
                        statementCantidad.addBatch();
                    }
                    statementCantidad.executeBatch();

                    statementAlmacen.executeBatch();
                    DBUtil.getConnection().commit();
                    return "inserted";
                }
            }

        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementAlmacen != null) {
                    statementAlmacen.close();
                }
                if (statementCodigoAlmacen != null) {
                    statementCodigoAlmacen.close();
                }
                if (statementProducto != null) {
                    statementProducto.close();
                }
                if (statementCantidad != null) {
                    statementCantidad.close();
                }
            } catch (SQLException ex) {

            }
        }
    }

    public static String DeletedAlmacenById(int idAlmacen) {
        PreparedStatement statementValidate = null;
        PreparedStatement statementAlmacen = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM CompraTB WHERE IdAlmacen = ?");
            statementValidate.setInt(1, idAlmacen);
            if (statementValidate.executeQuery().next()) {
                DBUtil.getConnection().rollback();
                return "compra";
            } else {
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM KardexSuministroTB WHERE IdAlmacen = ?");
                statementValidate.setInt(1, idAlmacen);
                if (statementValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    return "kardex";
                } else {
                    statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM TrasladoHistorialTB WHERE IdAlmacenOrigen = ?");
                    statementValidate.setInt(1, idAlmacen);
                    if (statementValidate.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        return "historial";
                    } else {
                        statementValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM TrasladoHistorialTB WHERE IdAlmacenDestino = ?");
                        statementValidate.setInt(1, idAlmacen);
                        if (statementValidate.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            return "historial";
                        } else {
                            if (idAlmacen == 0) {
                                DBUtil.getConnection().rollback();
                                return "principal";
                            } else {
                                statementAlmacen = DBUtil.getConnection().prepareStatement("DELETE FROM AlmacenTB WHERE IdAlmacen = ?");
                                statementAlmacen.setInt(1, idAlmacen);
                                statementAlmacen.addBatch();

                                statementAlmacen.executeBatch();
                                DBUtil.getConnection().commit();
                                return "deleted";
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {

            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (statementAlmacen != null) {
                    statementAlmacen.close();
                }
            } catch (SQLException ex) {

            }
        }
    }

    public static List<AlmacenTB> GetSearchComboBoxAlmacen() {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<AlmacenTB> almacenTBs = new ArrayList<>();

        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("SELECT IdAlmacen,Nombre,Direccion FROM AlmacenTB");
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setIdAlmacen(rsEmps.getInt("IdAlmacen"));
                almacenTB.setNombre(rsEmps.getString("Nombre"));
                almacenTB.setDireccion(rsEmps.getString("Direccion"));
                almacenTBs.add(almacenTB);
            }
        } catch (SQLException e) {
            System.out.println("Error en GetSearchComboBoxUbigeo(): " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return almacenTBs;
    }

    public static List<AlmacenTB> GetSearchComboBoxAlmacen(int idAlmacen) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<AlmacenTB> almacenTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("SELECT IdAlmacen,Nombre,Direccion FROM AlmacenTB WHERE IdAlmacen <> ?");
            preparedStatement.setInt(1, idAlmacen);
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setIdAlmacen(rsEmps.getInt("IdAlmacen"));
                almacenTB.setNombre(rsEmps.getString("Nombre"));
                almacenTB.setDireccion(rsEmps.getString("Direccion"));
                almacenTBs.add(almacenTB);
            }
        } catch (SQLException e) {
            System.out.println("Error en GetSearchComboBoxUbigeo(): " + e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return almacenTBs;
    }

}
