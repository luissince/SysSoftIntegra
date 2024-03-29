package service;

import controller.tools.Tools;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import model.EmpleadoTB;
import model.ImpuestoTB;
import model.MonedaTB;
import model.OrdenCompraDetalleTB;
import model.OrdenCompraTB;
import model.PreciosTB;
import model.ProveedorTB;
import model.SuministroTB;

public class OrdenCompraADO {

    public static Object ListarOrdenCompra(int opcion, String search, String fechaInicio, String fechaFinal, int posicionPagina, int filasPorPagina) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementOrdenCompra = null;
        PreparedStatement statementOrdenCompraCount = null;
        ResultSet resultSetOrdenCompra = null;
        try {
            dbf.dbConnect();
            Object[] object = new Object[2];
            ObservableList<OrdenCompraTB> empList = FXCollections.observableArrayList();
            statementOrdenCompra = dbf.getConnection().prepareStatement("{call Sp_Listar_OrdenCompra(?,?,?,?,?,?)}");
            statementOrdenCompra.setInt(1, opcion);
            statementOrdenCompra.setString(2, search);
            statementOrdenCompra.setString(3, fechaInicio);
            statementOrdenCompra.setString(4, fechaFinal);
            statementOrdenCompra.setInt(5, posicionPagina);
            statementOrdenCompra.setInt(6, filasPorPagina);
            resultSetOrdenCompra = statementOrdenCompra.executeQuery();
            while (resultSetOrdenCompra.next()) {
                OrdenCompraTB ordenCompraTB = new OrdenCompraTB();
                ordenCompraTB.setId(resultSetOrdenCompra.getRow() + posicionPagina);
                ordenCompraTB.setIdOrdenCompra(resultSetOrdenCompra.getString("IdOrdenCompra"));
                ordenCompraTB.setNumeracion(resultSetOrdenCompra.getInt("Numeracion"));
                ordenCompraTB.setFechaRegistro(resultSetOrdenCompra.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ordenCompraTB.setHoraRegistro(resultSetOrdenCompra.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ordenCompraTB.setFechaVencimiento(resultSetOrdenCompra.getDate("FechaVencimiento").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ordenCompraTB.setHoraVencimiento(resultSetOrdenCompra.getTime("HoraVencimiento").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ordenCompraTB.setObservacion(resultSetOrdenCompra.getString("Observacion"));

                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setNumeroDocumento(resultSetOrdenCompra.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(resultSetOrdenCompra.getString("RazonSocial"));

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setSimbolo(resultSetOrdenCompra.getString("Simbolo"));
                monedaTB.setAbreviado(resultSetOrdenCompra.getString("Abreviado"));
                ordenCompraTB.setMonedaTB(monedaTB);

                ordenCompraTB.setTotal(resultSetOrdenCompra.getDouble("Total"));

                ordenCompraTB.setProveedorTB(proveedorTB);
                empList.add(ordenCompraTB);
            }
            object[0] = empList;

            statementOrdenCompraCount = dbf.getConnection().prepareStatement("{call Sp_Listar_OrdenCompra_Count(?,?,?,?)}");
            statementOrdenCompraCount.setInt(1, opcion);
            statementOrdenCompraCount.setString(2, search);
            statementOrdenCompraCount.setString(3, fechaInicio);
            statementOrdenCompraCount.setString(4, fechaFinal);
            resultSetOrdenCompra = statementOrdenCompraCount.executeQuery();
            Integer total = 0;
            if (resultSetOrdenCompra.next()) {
                total = resultSetOrdenCompra.getInt("Total");
            }
            object[1] = total;

            return object;
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementOrdenCompra != null) {
                    statementOrdenCompra.close();
                }
                if (statementOrdenCompraCount != null) {
                    statementOrdenCompraCount.close();
                }
                if (resultSetOrdenCompra != null) {
                    resultSetOrdenCompra.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object Obtener_Orden_Compra_ById(String idOrdenCompra) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementOrdenCompra = null;
        PreparedStatement statementDetalleOrden = null;
        PreparedStatement preparedPrecio = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            statementOrdenCompra = dbf.getConnection().prepareStatement("{call Sp_Obtener_OrdenCompra_ById(?)}");
            statementOrdenCompra.setString(1, idOrdenCompra);
            resultSet = statementOrdenCompra.executeQuery();
            if (resultSet.next()) {
                OrdenCompraTB ordenCompraTB = new OrdenCompraTB();
                ordenCompraTB.setIdOrdenCompra(resultSet.getString("IdOrdenCompra"));
                ordenCompraTB.setNumeracion(resultSet.getInt("Numeracion"));
                ordenCompraTB.setFechaRegistro(resultSet.getDate("FechaRegistro").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ordenCompraTB.setHoraRegistro(resultSet.getTime("HoraRegistro").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                ordenCompraTB.setFechaVencimiento(resultSet.getDate("FechaVencimiento").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ordenCompraTB.setHoraVencimiento(resultSet.getTime("HoraVencimiento").toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

                ProveedorTB proveedorTB = new ProveedorTB();
                proveedorTB.setIdProveedor(resultSet.getString("IdProveedor"));
                proveedorTB.setNumeroDocumento(resultSet.getString("NumeroDocumento"));
                proveedorTB.setRazonSocial(resultSet.getString("RazonSocial"));
                proveedorTB.setTelefono(resultSet.getString("Telefono"));
                proveedorTB.setCelular(resultSet.getString("Celular"));
                proveedorTB.setEmail(resultSet.getString("Email"));
                proveedorTB.setDireccion(resultSet.getString("Direccion"));
                ordenCompraTB.setProveedorTB(proveedorTB);

                ordenCompraTB.setObservacion(resultSet.getString("Observacion"));

                EmpleadoTB empleadoTB = new EmpleadoTB();
                empleadoTB.setApellidos(resultSet.getString("Apellidos"));
                empleadoTB.setNombres(resultSet.getString("Nombres"));
                ordenCompraTB.setEmpleadoTB(empleadoTB);

                MonedaTB monedaTB = new MonedaTB();
                monedaTB.setIdMoneda(resultSet.getInt("IdMoneda"));
                monedaTB.setNombre(resultSet.getString("Nombre"));
                monedaTB.setSimbolo(resultSet.getString("Simbolo"));
                monedaTB.setAbreviado(resultSet.getString("Abreviado"));
                ordenCompraTB.setMonedaTB(monedaTB);

                ArrayList<OrdenCompraDetalleTB> compraDetalleTBs = new ArrayList();

                statementDetalleOrden = dbf.getConnection().prepareStatement("{call Sp_Obtener_Detalle_OrdenCompra_ById(?)}");
                statementDetalleOrden.setString(1, idOrdenCompra);
                resultSet = statementDetalleOrden.executeQuery();
                while (resultSet.next()) {
                    OrdenCompraDetalleTB compraDetalleTB = new OrdenCompraDetalleTB();
                    compraDetalleTB.setId(resultSet.getRow());
                    compraDetalleTB.setIdOrdenCompra(resultSet.getString("IdOrdenCompra"));
                    compraDetalleTB.setIdSuministro(resultSet.getString("IdSuministro"));

                    ImpuestoTB impuestoTB = new ImpuestoTB();
                    impuestoTB.setIdImpuesto(resultSet.getInt("IdImpuesto"));
                    impuestoTB.setNombre(resultSet.getString("NombreImpuesto"));
                    impuestoTB.setValor(resultSet.getDouble("Valor"));
                    compraDetalleTB.setImpuestoTB(impuestoTB);

                    compraDetalleTB.setCantidad(resultSet.getDouble("Cantidad"));
                    compraDetalleTB.setCosto(resultSet.getDouble("Costo"));
                    compraDetalleTB.setDescuento(resultSet.getDouble("Descuento"));
                    compraDetalleTB.setIdImpuesto(resultSet.getInt("IdImpuesto"));

                    compraDetalleTB.setObservacion(resultSet.getString("Observacion"));

                    //SUMINISTRO
                    SuministroTB suministroTB = new SuministroTB();
                    suministroTB.setIdSuministro(resultSet.getString("IdSuministro"));
                    suministroTB.setClave(resultSet.getString("Clave"));
                    suministroTB.setNombreMarca(resultSet.getString("NombreMarca"));
                    suministroTB.setUnidadCompraName(resultSet.getString("UnidadCompra"));

                    preparedPrecio = dbf.getConnection().prepareStatement("SELECT s.TipoPrecio,s.PrecioCompra,i.IdImpuesto,i.Nombre,i.Valor "
                            + "FROM "
                            + "SuministroTB AS s "
                            + "INNER JOIN ImpuestoTB AS i ON i.IdImpuesto = s.Impuesto "
                            + "WHERE s.IdSuministro = ?");
                    preparedPrecio.setString(1, resultSet.getString("IdSuministro"));
                    ResultSet rsEmps = preparedPrecio.executeQuery();
                    rsEmps.next();
                    suministroTB.setTipoPrecio(rsEmps.getBoolean("TipoPrecio"));
                    suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioCompra"));
                    suministroTB.setIdImpuesto(rsEmps.getInt("IdImpuesto"));
                    suministroTB.setImpuestoTB(new ImpuestoTB(rsEmps.getInt("IdImpuesto"), rsEmps.getString("Nombre"), rsEmps.getDouble("Valor")));

                    ArrayList<PreciosTB> preciosTBs = new ArrayList();
                    preparedPrecio = dbf.getConnection().prepareStatement("SELECT * FROM PreciosTB WHERE IdSuministro = ?");
                    preparedPrecio.setString(1, resultSet.getString("IdSuministro"));
                    rsEmps = preparedPrecio.executeQuery();
                    while (rsEmps.next()) {
                        PreciosTB preciosTB = new PreciosTB();
                        preciosTB.setIdPrecios(rsEmps.getInt("IdPrecios"));
                        preciosTB.setNombre(rsEmps.getString("Nombre"));
                        preciosTB.setValor(rsEmps.getDouble("Valor"));
                        preciosTB.setFactor(rsEmps.getDouble("Factor"));

                        TextField tfNombre = new TextField(rsEmps.getString("Nombre"));
                        tfNombre.getStyleClass().add("text-field-normal");
                        tfNombre.setOnKeyReleased(event -> {
                            preciosTB.setNombre(tfNombre.getText());
                        });

                        TextField tfValor = new TextField(Tools.roundingValue(rsEmps.getDouble("Valor"), 2));
                        tfValor.getStyleClass().add("text-field-normal");
                        tfValor.setOnKeyReleased(event -> {
                            preciosTB.setValor(!Tools.isNumeric(tfValor.getText()) ? 0 : Double.parseDouble(tfValor.getText()));
                        });
                        tfValor.setOnKeyTyped((KeyEvent event) -> {
                            char c = event.getCharacter().charAt(0);
                            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                                event.consume();
                            }
                            if (c == '.' && tfValor.getText().contains(".")) {
                                event.consume();
                            }
                        });

                        TextField tfFactor = new TextField(Tools.roundingValue(rsEmps.getDouble("Factor"), 2));
                        tfFactor.getStyleClass().add("text-field-normal");
                        tfFactor.setOnKeyReleased(event -> {
                            preciosTB.setFactor(!Tools.isNumeric(tfFactor.getText()) ? 1 : Double.parseDouble(tfFactor.getText()));
                        });
                        tfFactor.setOnKeyTyped((KeyEvent event) -> {
                            char c = event.getCharacter().charAt(0);
                            if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                                event.consume();
                            }
                            if (c == '.' && tfFactor.getText().contains(".")) {
                                event.consume();
                            }
                        });

                        Button button = new Button();
                        button.getStyleClass().add("buttonDark");
                        ImageView view = new ImageView(new Image("/view/image/remove.png"));
                        view.setFitWidth(22);
                        view.setFitHeight(22);
                        button.setGraphic(view);

                        preciosTB.setTxtNombre(tfNombre);
                        preciosTB.setTxtValor(tfValor);
                        preciosTB.setTxtFactor(tfFactor);
                        preciosTB.setBtnOpcion(button);

                        preciosTBs.add(preciosTB);
                    }
                    suministroTB.setPreciosTBs(preciosTBs);

                    compraDetalleTB.setSuministroTB(suministroTB);

                    Button button = new Button("X");
                    button.getStyleClass().add("buttonDark");
                    compraDetalleTB.setBtnRemove(button);

                    compraDetalleTBs.add(compraDetalleTB);
                }
                ordenCompraTB.setOrdenCompraDetalleTBs(compraDetalleTBs);

                return ordenCompraTB;
            } else {
                return "No se pudo cargar correctamente los datos, intente nuevamente.";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (statementOrdenCompra != null) {
                    statementOrdenCompra.close();
                }
                if (statementDetalleOrden != null) {
                    statementDetalleOrden.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedPrecio != null) {
                    preparedPrecio.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String InsertarOrdenCompra(OrdenCompraTB ordenCompraTB) {
        DBUtil dbf = new DBUtil();
        PreparedStatement statementValidate = null;
        PreparedStatement statementOrdenCompra = null;
        PreparedStatement statementOrdenDealle = null;
        PreparedStatement statementNumeracion = null;
        CallableStatement statementCodigo = null;
        ResultSet resultSet = null;
        try {
            dbf.dbConnect();
            dbf.getConnection().setAutoCommit(false);

            statementValidate = dbf.getConnection().prepareStatement("SELECT * FROM OrdenCompraTB WHERE IdOrdenCompra = ?");
            statementValidate.setString(1, ordenCompraTB.getIdOrdenCompra());
            resultSet = statementValidate.executeQuery();
            if (resultSet.next()) {

                statementOrdenCompra = dbf.getConnection().prepareStatement("UPDATE OrdenCompraTB "
                        + "SET IdProveedor = ?,"
                        + "IdEmpledo = ?,"
                        + "IdMoneda = ?,"
                        + "TipoCambio = ?,"
                        + "FechaRegistro = ?,"
                        + "HoraRegistro = ?,"
                        + "FechaVencimiento = ?,"
                        + "HoraVencimiento = ?,"
                        + "Observacion=? "
                        + "WHERE IdOrdenCompra = ?");
                statementOrdenCompra.setString(1, ordenCompraTB.getIdProveedor());
                statementOrdenCompra.setString(2, ordenCompraTB.getIdEmpleado());
                statementOrdenCompra.setInt(3, ordenCompraTB.getIdMoneda());
                statementOrdenCompra.setDouble(4, ordenCompraTB.getTipoCambio());
                statementOrdenCompra.setString(5, ordenCompraTB.getFechaRegistro());
                statementOrdenCompra.setString(6, ordenCompraTB.getHoraVencimiento());
                statementOrdenCompra.setString(7, ordenCompraTB.getFechaVencimiento());
                statementOrdenCompra.setString(8, ordenCompraTB.getHoraVencimiento());
                statementOrdenCompra.setString(9, ordenCompraTB.getObservacion());
                statementOrdenCompra.setString(10, ordenCompraTB.getIdOrdenCompra());
                statementOrdenCompra.addBatch();
                statementOrdenCompra.executeBatch();

                statementOrdenDealle = dbf.getConnection().prepareStatement("DELETE FROM OrdenCompraDatalleTB WHERE IdOrdenCompra = ?");
                statementOrdenDealle.setString(1, ordenCompraTB.getIdOrdenCompra());
                statementOrdenDealle.addBatch();
                statementOrdenDealle.executeBatch();

                statementOrdenDealle = dbf.getConnection().prepareStatement("INSERT INTO OrdenCompraDatalleTB("
                        + "IdOrdenCompra,"
                        + "IdSuministro,"
                        + "Cantidad,"
                        + "Descuento,"
                        + "Costo,"
                        + "IdImpuesto,"
                        + "Observacion"
                        + ")VALUES(?,?,?,?,?,?,?)");
                for (OrdenCompraDetalleTB detalleTB : ordenCompraTB.getOrdenCompraDetalleTBs()) {
                    statementOrdenDealle.setString(1, ordenCompraTB.getIdOrdenCompra());
                    statementOrdenDealle.setString(2, detalleTB.getIdSuministro());
                    statementOrdenDealle.setDouble(3, detalleTB.getCantidad());
                    statementOrdenDealle.setDouble(4, detalleTB.getDescuento());
                    statementOrdenDealle.setDouble(5, detalleTB.getCosto());
                    statementOrdenDealle.setInt(6, detalleTB.getIdImpuesto());
                    statementOrdenDealle.setString(7, detalleTB.getObservacion());
                    statementOrdenDealle.addBatch();
                }
                statementOrdenDealle.executeBatch();

                dbf.getConnection().commit();
                return "updated";
            } else {
                statementCodigo = dbf.getConnection().prepareCall("{? = call Fc_OrdenCompra_Codigo_Alfanumerico()}");
                statementCodigo.registerOutParameter(1, java.sql.Types.VARCHAR);
                statementCodigo.execute();
                String idOrdenCompra = statementCodigo.getString(1);

                statementNumeracion = dbf.getConnection().prepareStatement("SELECT MAX(Numeracion) AS Total FROM OrdenCompraTB");
                resultSet = statementNumeracion.executeQuery();

                int numeracion;
                if (resultSet.next()) {
                    numeracion = resultSet.getInt("Total") + 1;
                } else {
                    numeracion = 1;
                }

                statementOrdenCompra = dbf.getConnection().prepareStatement("INSERT INTO OrdenCompraTB("
                        + "IdOrdenCompra,"
                        + "Numeracion,"
                        + "IdProveedor,"
                        + "IdEmpledo,"
                        + "IdMoneda,"
                        + "TipoCambio,"
                        + "FechaRegistro,"
                        + "HoraRegistro,"
                        + "FechaVencimiento,"
                        + "HoraVencimiento,"
                        + "Observacion"
                        + ")VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                statementOrdenCompra.setString(1, idOrdenCompra);
                statementOrdenCompra.setInt(2, numeracion);
                statementOrdenCompra.setString(3, ordenCompraTB.getIdProveedor());
                statementOrdenCompra.setString(4, ordenCompraTB.getIdEmpleado());
                statementOrdenCompra.setInt(5, ordenCompraTB.getIdMoneda());
                statementOrdenCompra.setDouble(6, ordenCompraTB.getTipoCambio());
                statementOrdenCompra.setString(7, ordenCompraTB.getFechaRegistro());
                statementOrdenCompra.setString(8, ordenCompraTB.getHoraRegistro());
                statementOrdenCompra.setString(9, ordenCompraTB.getFechaVencimiento());
                statementOrdenCompra.setString(10, ordenCompraTB.getHoraVencimiento());
                statementOrdenCompra.setString(11, ordenCompraTB.getObservacion());
                statementOrdenCompra.addBatch();
                statementOrdenCompra.executeBatch();

                statementOrdenDealle = dbf.getConnection().prepareStatement("INSERT INTO OrdenCompraDatalleTB("
                        + "IdOrdenCompra,"
                        + "IdSuministro,"
                        + "Cantidad,"
                        + "Descuento,"
                        + "Costo,"
                        + "IdImpuesto,"
                        + "Observacion"
                        + ")VALUES(?,?,?,?,?,?,?)");
                for (OrdenCompraDetalleTB detalleTB : ordenCompraTB.getOrdenCompraDetalleTBs()) {
                    statementOrdenDealle.setString(1, idOrdenCompra);
                    statementOrdenDealle.setString(2, detalleTB.getIdSuministro());
                    statementOrdenDealle.setDouble(3, detalleTB.getCantidad());
                    statementOrdenDealle.setDouble(4, detalleTB.getDescuento());
                    statementOrdenDealle.setDouble(5, detalleTB.getCosto());
                    statementOrdenDealle.setInt(6, detalleTB.getIdImpuesto());
                    statementOrdenDealle.setString(7, detalleTB.getObservacion());
                    statementOrdenDealle.addBatch();
                }
                statementOrdenDealle.executeBatch();

                dbf.getConnection().commit();
                return "inserted";
            }
        } catch (SQLException | ClassNotFoundException ex) {
            try {
                dbf.getConnection().rollback();
                return ex.getLocalizedMessage();
            } catch (SQLException exq) {
                return exq.getLocalizedMessage();
            }
        } finally {
            try {
                if (statementOrdenCompra != null) {
                    statementOrdenCompra.close();
                }
                if (statementOrdenDealle != null) {
                    statementOrdenDealle.close();
                }
                if (statementNumeracion != null) {
                    statementNumeracion.close();
                }
                if (statementCodigo != null) {
                    statementCodigo.close();
                }
                if (statementValidate != null) {
                    statementValidate.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
                dbf.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static String ActualizarOrdenCompra() {
        return "";
    }

    public static String EliminarOrdenCompra() {
        return "";
    }

}
