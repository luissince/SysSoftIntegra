package model;

import controller.tools.Session;
import controller.tools.Tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SuministroADO {

    public static String CrudSuministro(SuministroTB suministroTB, ObservableList<PreciosTB> tvPrecios) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {

            CallableStatement codigoSuministro = null;
            PreparedStatement preparedSuministro = null;
            PreparedStatement preparedValidation = null;
            PreparedStatement preparedPrecios = null;
            PreparedStatement preparedAlmacen = null;
            PreparedStatement preparedCantidad = null;

            try {
                DBUtil.getConnection().setAutoCommit(false);

                preparedValidation = DBUtil.getConnection().prepareStatement("select IdSuministro from SuministroTB where IdSuministro = ?");
                preparedValidation.setString(1, suministroTB.getIdSuministro());
                if (preparedValidation.executeQuery().next()) {
                    preparedValidation = DBUtil.getConnection().prepareStatement("select Clave from SuministroTB where IdSuministro <> ? and Clave = ?");
                    preparedValidation.setString(1, suministroTB.getIdSuministro());
                    preparedValidation.setString(2, suministroTB.getClave());
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        preparedValidation = DBUtil.getConnection().prepareStatement("select NombreMarca from SuministroTB where IdSuministro <> ? and NombreMarca = ?");
                        preparedValidation.setString(1, suministroTB.getIdSuministro());
                        preparedValidation.setString(2, suministroTB.getNombreMarca());
                        if (preparedValidation.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            result = "duplicatename";
                        } else {
                            preparedSuministro = DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET Origen=?,Clave=?,ClaveAlterna=UPPER(?), NombreMarca=UPPER(?),NombreGenerico=UPPER(?),Categoria=?,Marca=?,Presentacion=?,StockMinimo=?,StockMaximo=?,PrecioCompra=?,PrecioVentaGeneral=?,UnidadCompra=?,UnidadVenta = ?,Estado=?,Lote=?,Inventario=?,NuevaImagen=?,Impuesto=?, ValorInventario = ?,ClaveSat = ?,TipoPrecio=?,Descripcion=? WHERE IdSuministro = ? ");

                            preparedSuministro.setInt(1, suministroTB.getOrigen());
                            preparedSuministro.setString(2, suministroTB.getClave());
                            preparedSuministro.setString(3, suministroTB.getClaveAlterna());
                            preparedSuministro.setString(4, suministroTB.getNombreMarca());
                            preparedSuministro.setString(5, suministroTB.getNombreGenerico());
                            preparedSuministro.setInt(6, suministroTB.getCategoria());
                            preparedSuministro.setInt(7, suministroTB.getMarcar());
                            preparedSuministro.setInt(8, suministroTB.getPresentacion());
                            preparedSuministro.setDouble(9, suministroTB.getStockMinimo());
                            preparedSuministro.setDouble(10, suministroTB.getStockMaximo());
                            preparedSuministro.setDouble(11, suministroTB.getCostoCompra());

                            preparedSuministro.setDouble(12, suministroTB.getPrecioVentaGeneral());

                            preparedSuministro.setInt(13, suministroTB.getUnidadCompra());
                            preparedSuministro.setInt(14, suministroTB.getUnidadVenta());
                            preparedSuministro.setInt(15, suministroTB.getEstado());
                            preparedSuministro.setBoolean(16, suministroTB.isLote());
                            preparedSuministro.setBoolean(17, suministroTB.isInventario());
                            //------------------------------------------------------------
                            preparedSuministro.setBytes(18, suministroTB.getNuevaImagen());
                            //
                            preparedSuministro.setInt(19, suministroTB.getIdImpuesto());
                            preparedSuministro.setShort(20, suministroTB.getValorInventario());
                            preparedSuministro.setString(21, suministroTB.getClaveSat());
                            preparedSuministro.setBoolean(22, suministroTB.isTipoPrecio());
                            preparedSuministro.setString(23, suministroTB.getDescripcion());
                            preparedSuministro.setString(24, suministroTB.getIdSuministro());

                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            preparedPrecios = DBUtil.getConnection().prepareStatement("DELETE FROM PreciosTB WHERE IdSuministro = ?");
                            preparedPrecios.setString(1, suministroTB.getIdSuministro());
                            preparedPrecios.addBatch();
                            preparedPrecios.executeBatch();

                            preparedPrecios = DBUtil.getConnection().prepareStatement("INSERT INTO PreciosTB(IdArticulo, IdSuministro, Nombre, Valor, Factor,Estado) VALUES(?,?,?,?,?,?)");
                            for (int i = 0; i < tvPrecios.size(); i++) {
                                preparedPrecios.setString(1, "");
                                preparedPrecios.setString(2, suministroTB.getIdSuministro());
                                preparedPrecios.setString(3, tvPrecios.get(i).getNombre().toUpperCase());
                                preparedPrecios.setDouble(4, tvPrecios.get(i).getValor());
                                preparedPrecios.setDouble(5, tvPrecios.get(i).getFactor() <= 0 ? 1 : tvPrecios.get(i).getFactor());
                                preparedPrecios.setBoolean(6, true);
                                preparedPrecios.addBatch();
                            }
                            preparedPrecios.executeBatch();

                            DBUtil.getConnection().commit();
                            result = "updated";
                        }
                    }
                } else {
                    preparedValidation = DBUtil.getConnection().prepareStatement("select Clave from SuministroTB where Clave = ?");
                    preparedValidation.setString(1, suministroTB.getClave());
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "duplicate";
                    } else {
                        preparedValidation = DBUtil.getConnection().prepareStatement("select NombreMarca from SuministroTB where NombreMarca = ?");
                        preparedValidation.setString(1, suministroTB.getNombreMarca());
                        if (preparedValidation.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            result = "duplicatename";
                        } else {
                            codigoSuministro = DBUtil.getConnection().prepareCall("{? = call Fc_Suministro_Codigo_Alfanumerico()}");
                            codigoSuministro.registerOutParameter(1, java.sql.Types.VARCHAR);
                            codigoSuministro.execute();
                            String idSuministro = codigoSuministro.getString(1);

                            preparedSuministro = DBUtil.getConnection().prepareStatement("INSERT INTO "
                                    + "SuministroTB"
                                    + "(IdSuministro,"
                                    + "Origen,"
                                    + "Clave,"
                                    + "ClaveAlterna,"
                                    + "NombreMarca,"
                                    + "NombreGenerico,"
                                    + "Categoria,"
                                    + "Marca,"
                                    + "Presentacion,"
                                    + "StockMinimo,"
                                    + "StockMaximo,"
                                    + "PrecioCompra,"
                                    + "PrecioVentaGeneral,"
                                    + "Cantidad,"
                                    + "UnidadCompra,"
                                    + "UnidadVenta,"
                                    + "Estado,"
                                    + "Lote,"
                                    + "Inventario,"
                                    + "NuevaImagen,"
                                    + "Impuesto,"
                                    + "ValorInventario,"
                                    + "Imagen,"
                                    + "ClaveSat,"
                                    + "TipoPrecio,"
                                    + "Descripcion)"
                                    + "values(?,?,?,UPPER(?),UPPER(?),UPPER(?),UPPER(?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                            preparedSuministro.setString(1, idSuministro);
                            preparedSuministro.setInt(2, suministroTB.getOrigen());
                            preparedSuministro.setString(3, suministroTB.getClave());
                            preparedSuministro.setString(4, suministroTB.getClaveAlterna());
                            preparedSuministro.setString(5, suministroTB.getNombreMarca());
                            preparedSuministro.setString(6, suministroTB.getNombreGenerico());
                            preparedSuministro.setInt(7, suministroTB.getCategoria());
                            preparedSuministro.setInt(8, suministroTB.getMarcar());
                            preparedSuministro.setInt(9, suministroTB.getPresentacion());
                            preparedSuministro.setDouble(10, suministroTB.getStockMinimo());
                            preparedSuministro.setDouble(11, suministroTB.getStockMaximo());
                            preparedSuministro.setDouble(12, suministroTB.getCostoCompra());

                            preparedSuministro.setDouble(13, suministroTB.getPrecioVentaGeneral());

                            preparedSuministro.setDouble(14, 0);
                            preparedSuministro.setInt(15, suministroTB.getUnidadCompra());
                            preparedSuministro.setInt(16, suministroTB.getUnidadVenta());
                            preparedSuministro.setInt(17, suministroTB.getEstado());
                            preparedSuministro.setBoolean(18, suministroTB.isLote());
                            preparedSuministro.setBoolean(19, suministroTB.isInventario());
                            //------------------------------------------------------------
                            preparedSuministro.setBytes(20, suministroTB.getNuevaImagen());
//                            preparedSuministro.setString(20, suministroTB.getImagenFile() != null
//                                    ? selectFileImage("./img/" + idSuministro + "." + Tools.getFileExtension(suministroTB.getImagenFile()), suministroTB.getImagenFile())
//                                    : (suministroTB.getImagenTB().equalsIgnoreCase("") ? ""
//                                    : selectFileImage("./img/" + idSuministro + "." + Tools.getFileExtension(new File(suministroTB.getImagenTB())), new File(suministroTB.getImagenTB())))
//                            );
                            //
                            preparedSuministro.setInt(21, suministroTB.getIdImpuesto());
                            preparedSuministro.setShort(22, suministroTB.getValorInventario());
                            preparedSuministro.setString(23, "");
                            preparedSuministro.setString(24, suministroTB.getClaveSat());
                            preparedSuministro.setBoolean(25, suministroTB.isTipoPrecio());
                            preparedSuministro.setString(26, suministroTB.getDescripcion());

                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            preparedPrecios = DBUtil.getConnection().prepareStatement("INSERT INTO PreciosTB(IdArticulo, IdSuministro, Nombre, Valor, Factor,Estado) VALUES(?,?,?,?,?,?)");

                            for (int i = 0; i < tvPrecios.size(); i++) {
                                preparedPrecios.setString(1, "");
                                preparedPrecios.setString(2, idSuministro);
                                preparedPrecios.setString(3, tvPrecios.get(i).getNombre().toUpperCase());
                                preparedPrecios.setDouble(4, tvPrecios.get(i).getValor());
                                preparedPrecios.setDouble(5, tvPrecios.get(i).getFactor() <= 0 ? 1 : tvPrecios.get(i).getFactor());
                                preparedPrecios.setBoolean(6, true);
                                preparedPrecios.addBatch();
                            }
                            preparedPrecios.executeBatch();

                            preparedAlmacen = DBUtil.getConnection().prepareStatement("SELECT IdAlmacen FROM AlmacenTB");
                            ResultSet resultSet = preparedAlmacen.executeQuery();
                            preparedCantidad = DBUtil.getConnection().prepareStatement("INSERT INTO CantidadTB(IdAlmacen,IdSuministro,StockMinimo,StockMaximo,Cantidad) VALUES(?,?,?,?,?)");
                            while (resultSet.next()) {
                                if (resultSet.getInt("IdAlmacen") != 0) {
                                    preparedCantidad.setInt(1, resultSet.getInt("IdAlmacen"));
                                    preparedCantidad.setString(2, idSuministro);
                                    preparedCantidad.setDouble(3, suministroTB.getStockMinimo());
                                    preparedCantidad.setDouble(4, suministroTB.getStockMaximo());
                                    preparedCantidad.setDouble(5, 0);
                                    preparedCantidad.addBatch();
                                }
                            }
                            preparedCantidad.executeBatch();

                            DBUtil.getConnection().commit();
                            result = "registered";
                        }
                    }
                }
            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                } catch (SQLException exr) {
                }
                result = ex.getLocalizedMessage();
            } finally {
                try {
                    if (preparedSuministro != null) {
                        preparedSuministro.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                    if (codigoSuministro != null) {
                        codigoSuministro.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                    if (preparedPrecios != null) {
                        preparedPrecios.close();
                    }
                    if (preparedAlmacen != null) {
                        preparedAlmacen.close();
                    }
                    if (preparedCantidad != null) {
                        preparedCantidad.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se puedo establecer una conexión, intente nuevamente.";
        }
        return result;
    }

    public static Object CrudMasivoSuministro(SuministroTB suministroTB) {

        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            CallableStatement codigoSuministro = null;
            PreparedStatement preparedSuministro = null;
            PreparedStatement preparedValidation = null;
            PreparedStatement preparedPrecios = null;
            PreparedStatement preparedKardex = null;

            try {
                DBUtil.getConnection().setAutoCommit(false);

                preparedValidation = DBUtil.getConnection().prepareStatement("select Clave from SuministroTB where Clave = ?");
                preparedValidation.setString(1, suministroTB.getClave());
                if (preparedValidation.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    suministroTB.setMensaje("Ya existe un producto con el mismo código.");
                    return suministroTB;
                } else {
                    preparedValidation = DBUtil.getConnection().prepareStatement("select NombreMarca from SuministroTB where NombreMarca = ?");
                    preparedValidation.setString(1, suministroTB.getNombreMarca());
                    if (preparedValidation.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        suministroTB.setMensaje("Ya existe un producto con el mismo nombre.");
                        return suministroTB;
                    } else {
                        codigoSuministro = DBUtil.getConnection().prepareCall("{? = call Fc_Suministro_Codigo_Alfanumerico()}");
                        codigoSuministro.registerOutParameter(1, java.sql.Types.VARCHAR);
                        codigoSuministro.execute();
                        String idSuministro = codigoSuministro.getString(1);

                        preparedSuministro = DBUtil.getConnection().prepareStatement("INSERT INTO "
                                + "SuministroTB"
                                + "(IdSuministro,"
                                + "Origen,"
                                + "Clave,"
                                + "ClaveAlterna,"
                                + "NombreMarca,"
                                + "NombreGenerico,"
                                + "Categoria,"
                                + "Marca,"
                                + "Presentacion,"
                                + "StockMinimo,"
                                + "StockMaximo,"
                                + "PrecioCompra,"
                                + "PrecioVentaGeneral,"
                                + "Cantidad,"
                                + "UnidadCompra,"
                                + "UnidadVenta,"
                                + "Estado,"
                                + "Lote,"
                                + "Inventario,"
                                + "NuevaImagen,"
                                + "Impuesto,"
                                + "ValorInventario,"
                                + "Imagen,"
                                + "ClaveSat,TipoPrecio)"
                                + "values(?,?,?,UPPER(?),UPPER(?),UPPER(?),UPPER(?),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                        preparedSuministro.setString(1, idSuministro);
                        preparedSuministro.setInt(2, suministroTB.getOrigen());
                        preparedSuministro.setString(3, suministroTB.getClave());
                        preparedSuministro.setString(4, suministroTB.getClaveAlterna());
                        preparedSuministro.setString(5, suministroTB.getNombreMarca());
                        preparedSuministro.setString(6, suministroTB.getNombreGenerico());
                        preparedSuministro.setInt(7, suministroTB.getCategoria());
                        preparedSuministro.setInt(8, suministroTB.getMarcar());
                        preparedSuministro.setInt(9, suministroTB.getPresentacion());
                        preparedSuministro.setDouble(10, suministroTB.getStockMinimo());
                        preparedSuministro.setDouble(11, suministroTB.getStockMaximo());
                        preparedSuministro.setDouble(12, suministroTB.getCostoCompra());

                        preparedSuministro.setDouble(13, suministroTB.getPrecioVentaGeneral());

                        preparedSuministro.setDouble(14, suministroTB.getCantidad());
                        preparedSuministro.setInt(15, suministroTB.getUnidadCompra());
                        preparedSuministro.setInt(16, suministroTB.getUnidadVenta());
                        preparedSuministro.setInt(17, suministroTB.getEstado());
                        preparedSuministro.setBoolean(18, suministroTB.isLote());
                        preparedSuministro.setBoolean(19, suministroTB.isInventario());
                        preparedSuministro.setBytes(20, suministroTB.getNuevaImagen());

                        preparedSuministro.setInt(21, suministroTB.getIdImpuesto());
                        preparedSuministro.setShort(22, suministroTB.getValorInventario());
                        preparedSuministro.setString(23, "");
                        preparedSuministro.setString(24, suministroTB.getClaveSat());
                        preparedSuministro.setBoolean(25, suministroTB.isTipoPrecio());
                        preparedSuministro.addBatch();
                        preparedSuministro.executeBatch();

                        preparedPrecios = DBUtil.getConnection().prepareStatement("INSERT INTO PreciosTB(IdArticulo, IdSuministro, Nombre, Valor, Factor,Estado) VALUES(?,?,?,?,?,?)");

                        for (PreciosTB preciosTB : suministroTB.getPreciosTBs()) {
                            preparedPrecios.setString(1, "");
                            preparedPrecios.setString(2, idSuministro);
                            preparedPrecios.setString(3, preciosTB.getNombre().toUpperCase());
                            preparedPrecios.setDouble(4, preciosTB.getValor());
                            preparedPrecios.setDouble(5, preciosTB.getFactor() <= 0 ? 1 : preciosTB.getFactor());
                            preparedPrecios.setBoolean(6, true);
                            preparedPrecios.addBatch();
                        }
                        preparedPrecios.executeBatch();

                        preparedKardex = DBUtil.getConnection().prepareStatement("INSERT INTO "
                                + "KardexSuministroTB("
                                + "IdSuministro,"
                                + "Fecha,"
                                + "Hora,"
                                + "Tipo,"
                                + "Movimiento,"
                                + "Detalle,"
                                + "Cantidad, "
                                + "Costo, "
                                + "Total,"
                                + "IdAlmacen, "
                                + "IdEmpleado) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
                        if (suministroTB.getCantidad() > 0) {
                            preparedKardex.setString(1, idSuministro);
                            preparedKardex.setString(2, Tools.getDate());
                            preparedKardex.setString(3, Tools.getTime());
                            preparedKardex.setShort(4, (short) 1);
                            preparedKardex.setInt(5, 2);
                            preparedKardex.setString(6, "INVENTARIO INICIAL");
                            preparedKardex.setDouble(7, suministroTB.getCantidad());
                            preparedKardex.setDouble(8, suministroTB.getCostoCompra());
                            preparedKardex.setDouble(9, suministroTB.getCantidad() * suministroTB.getCostoCompra());
                            preparedKardex.setInt(10, 0);
                            preparedKardex.setString(11, Session.USER_ID);
                            preparedKardex.addBatch();
                        }
                        preparedKardex.executeBatch();

                        DBUtil.getConnection().commit();
                        return "registered";
                    }
                }

            } catch (SQLException ex) {
                try {
                    DBUtil.getConnection().rollback();
                } catch (SQLException exr) {
                }
                suministroTB.setMensaje(ex.getLocalizedMessage());
                return suministroTB;
            } finally {
                try {
                    if (preparedSuministro != null) {
                        preparedSuministro.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                    if (codigoSuministro != null) {
                        codigoSuministro.close();
                    }
                    if (preparedValidation != null) {
                        preparedValidation.close();
                    }
                    if (preparedPrecios != null) {
                        preparedPrecios.close();
                    }
                    if (preparedKardex != null) {
                        preparedKardex.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {
                    suministroTB.setMensaje(ex.getLocalizedMessage());
                    return suministroTB;
                }
            }
        } else {
            suministroTB.setMensaje("No se puedo establecer una conexión, intente nuevamente.");
            return suministroTB;
        }
    }

    public static String DeletedInventarioInicial() {
        PreparedStatement preparedValidar = null;
        PreparedStatement preparedSuministro = null;
        PreparedStatement preparedPrecio = null;
        PreparedStatement preparedKardex = null;
        PreparedStatement preparedCantidad = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);

            preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM VentaTB");
            if (preparedValidar.executeQuery().next()) {
                DBUtil.getConnection().rollback();
                return "venta";
            } else {

                preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM CompraTB");
                if (preparedValidar.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    return "compra";
                } else {

                    preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM CotizacionTB");
                    if (preparedValidar.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        return "cotizacion";
                    } else {

                        preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM ProduccionTB");
                        if (preparedValidar.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            return "produccion";
                        } else {

                            preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM FormulaTB");
                            if (preparedValidar.executeQuery().next()) {
                                DBUtil.getConnection().rollback();
                                return "formula";
                            } else {

                                preparedValidar = DBUtil.getConnection().prepareStatement("SELECT * FROM MovimientoInventarioTB");
                                if (preparedValidar.executeQuery().next()) {
                                    DBUtil.getConnection().rollback();
                                    return "movimiento_inventario";
                                } else {

                                    preparedSuministro = DBUtil.getConnection().prepareStatement("TRUNCATE TABLE SuministroTB");
                                    preparedSuministro.addBatch();
                                    preparedSuministro.executeBatch();

                                    preparedPrecio = DBUtil.getConnection().prepareStatement("TRUNCATE TABLE PreciosTB");
                                    preparedPrecio.addBatch();
                                    preparedPrecio.executeBatch();

                                    preparedKardex = DBUtil.getConnection().prepareStatement("TRUNCATE TABLE KardexSuministroTB");
                                    preparedKardex.addBatch();
                                    preparedKardex.executeBatch();

                                    preparedCantidad = DBUtil.getConnection().prepareStatement("TRUNCATE TABLE CantidadTB");
                                    preparedCantidad.addBatch();
                                    preparedCantidad.executeBatch();

                                    DBUtil.getConnection().commit();
                                    return "ok";
                                }
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
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedValidar != null) {
                    preparedValidar.close();
                }
                if (preparedSuministro != null) {
                    preparedSuministro.close();
                }
                if (preparedPrecio != null) {
                    preparedPrecio.close();
                }
                if (preparedKardex != null) {
                    preparedKardex.close();
                }
                if (preparedCantidad != null) {
                    preparedCantidad.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
    }

    public static String selectFileImage(String ruta, File selectFile) {
        String rutaValidada = ruta;
        File fcom = new File(ruta);
        if (fcom.exists()) {
            fcom.delete();
        }

        FileInputStream inputStream = null;
        byte[] buffer = new byte[1024];
        try (FileOutputStream outputStream = new FileOutputStream(ruta)) {
            inputStream = new FileInputStream(selectFile.getAbsolutePath());
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != 1) {
                outputStream.write(buffer, 0, byteRead);
                outputStream.flush();
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                rutaValidada = "";
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    rutaValidada = "";
                }
            }
        }
        return rutaValidada;
    }

    public static Object Listar_Suministros_Lista_View(short tipo, String value, int posicionPagina, int filasPorPagina) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];

            ObservableList<SuministroTB> empList = FXCollections.observableArrayList();
            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Suministros_Lista_View(?,?,?,?)}");
            preparedStatement.setShort(1, tipo);
            preparedStatement.setString(2, value);
            preparedStatement.setInt(3, posicionPagina);
            preparedStatement.setInt(4, filasPorPagina);
            rsEmps = preparedStatement.executeQuery();

            while (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setId(rsEmps.getRow() + posicionPagina);
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setCategoriaName(rsEmps.getString("Categoria"));
                suministroTB.setMarcaName(rsEmps.getString("Marca"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));

                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));

                suministroTB.setUnidadCompra(rsEmps.getInt("UnidadCompra"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraName"));
                suministroTB.setUnidadVenta(rsEmps.getInt("UnidadVenta"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));

                suministroTB.setIdImpuesto(rsEmps.getInt("Impuesto"));

                ImpuestoTB impuestoTB = new ImpuestoTB();
                impuestoTB.setIdImpuesto(rsEmps.getInt("Impuesto"));
                impuestoTB.setOperacion(rsEmps.getInt("Operacion"));
                impuestoTB.setNombreImpuesto(rsEmps.getString("ImpuestoNombre"));
                impuestoTB.setValor(rsEmps.getDouble("Valor"));
                suministroTB.setImpuestoTB(impuestoTB);

                suministroTB.setLote(rsEmps.getBoolean("Lote"));
                suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));

                ImageView image = new ImageView(new Image(suministroTB.getValorInventario() == 1 ? "/view/image/unidad.png" : "/view/image/balanza.png"));
                image.setFitWidth(32);
                image.setFitHeight(32);
                suministroTB.setImageValorInventario(image);
                suministroTB.setNuevaImagen(rsEmps.getBytes("NuevaImagen"));

                Label label = new Label(Tools.roundingValue(suministroTB.getCantidad(), 2) + " " + suministroTB.getUnidadCompraName());
                label.getStyleClass().add("labelRoboto13");
                label.setStyle(suministroTB.getCantidad() > 0 ? "-fx-text-fill:#020203;" : "-fx-text-fill:red;");
                suministroTB.setLblCantidad(label);

                empList.add(suministroTB);
            }

            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Suministros_Lista_View_Count(?,?)}");
            preparedStatement.setShort(1, tipo);
            preparedStatement.setString(2, value);
            rsEmps = preparedStatement.executeQuery();
            Integer integer = 0;
            if (rsEmps.next()) {
                integer = rsEmps.getInt("Total");
            }

            objects[0] = empList;
            objects[1] = integer;

            return objects;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
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
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ListarSuministros(short opcion, String clave, String nombreMarca, int categoria, int marca, int posicionPagina, int filasPorPagina) {
        PreparedStatement preparedSuministros = null;
        PreparedStatement preparedTotales = null;
        ResultSet rsEmps = null;
        try {
            DBUtil.dbConnect();
            Object[] objects = new Object[2];
            ObservableList<SuministroTB> empList = FXCollections.observableArrayList();
            preparedSuministros = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Suministros(?,?,?,?,?,?,?)}");
            preparedSuministros.setShort(1, opcion);
            preparedSuministros.setString(2, clave);
            preparedSuministros.setString(3, nombreMarca);
            preparedSuministros.setInt(4, categoria);
            preparedSuministros.setInt(5, marca);
            preparedSuministros.setInt(6, posicionPagina);
            preparedSuministros.setInt(7, filasPorPagina);
            rsEmps = preparedSuministros.executeQuery();
            while (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setId(rsEmps.getRow() + posicionPagina);
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setClaveAlterna(rsEmps.getString("ClaveAlterna"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setNombreGenerico(rsEmps.getString("NombreGenerico"));
                suministroTB.setStockMinimo(rsEmps.getDouble("StockMinimo"));
                suministroTB.setStockMaximo(rsEmps.getDouble("StockMaximo"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraNombre"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                suministroTB.setCategoriaName(rsEmps.getString("Categoria"));
                suministroTB.setMarcaName(rsEmps.getString("Marca"));
                suministroTB.setEstadoName(rsEmps.getString("Estado"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));
                suministroTB.setNuevaImagen(rsEmps.getBytes("NuevaImagen"));

                Label label = new Label(Tools.roundingValue(suministroTB.getCantidad(), 2));
                label.getStyleClass().add("labelRoboto13");
                label.setStyle(suministroTB.getCantidad() > 0 ? "-fx-text-fill:blue;" : "-fx-text-fill:red;");
                suministroTB.setLblCantidad(label);

                suministroTB.setImpuestoTB(new ImpuestoTB(rsEmps.getString("ImpuestoNombre"), rsEmps.getDouble("Valor")));

                empList.add(suministroTB);
            }

            preparedTotales = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Suministros_Count(?,?,?,?,?)}");
            preparedTotales.setShort(1, opcion);
            preparedTotales.setString(2, clave);
            preparedTotales.setString(3, nombreMarca);
            preparedTotales.setInt(4, categoria);
            preparedTotales.setInt(5, marca);
            rsEmps = preparedTotales.executeQuery();
            Integer integer = 0;
            if (rsEmps.next()) {
                integer = rsEmps.getInt("Total");
            }

            objects[0] = empList;
            objects[1] = integer;

            return objects;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedSuministros != null) {
                    preparedSuministros.close();
                }
                if (preparedTotales != null) {
                    preparedTotales.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }

    }

    public static SuministroTB GetSuministroById(String value) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        SuministroTB suministroTB = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Suministro_By_Id(?)}");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setOrigen(rsEmps.getInt("Origen"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setClaveAlterna(rsEmps.getString("ClaveAlterna"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setNombreGenerico(rsEmps.getString("NombreGenerico"));
                suministroTB.setCategoria(rsEmps.getInt("Categoria"));
                suministroTB.setCategoriaName(rsEmps.getString("CategoriaNombre"));
                suministroTB.setMarcar(rsEmps.getInt("Marca"));
                suministroTB.setMarcaName(rsEmps.getString("MarcaNombre"));
                suministroTB.setPresentacion(rsEmps.getInt("Presentacion"));
                suministroTB.setPresentacionName(rsEmps.getString("PresentacionNombre"));
                suministroTB.setUnidadCompra(rsEmps.getInt("UnidadCompra"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraNombre"));
                suministroTB.setUnidadVenta(rsEmps.getInt("UnidadVenta"));
                suministroTB.setStockMinimo(rsEmps.getDouble("StockMinimo"));
                suministroTB.setStockMaximo(rsEmps.getDouble("StockMaximo"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));

                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));

                suministroTB.setEstado(rsEmps.getInt("Estado"));
                suministroTB.setLote(rsEmps.getBoolean("Lote"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));
                suministroTB.setNuevaImagen(rsEmps.getBytes("NuevaImagen"));

                suministroTB.setIdImpuesto(rsEmps.getInt("Impuesto"));
                ImpuestoTB impuestoTB = new ImpuestoTB();
                impuestoTB.setIdImpuesto(rsEmps.getInt("Impuesto"));
                impuestoTB.setNombreImpuesto(rsEmps.getString("ImpuestoNombre"));

                suministroTB.setClaveSat(rsEmps.getString("ClaveSat"));
                suministroTB.setTipoPrecio(rsEmps.getBoolean("TipoPrecio"));
                suministroTB.setDescripcion(rsEmps.getString("Descripcion"));
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);

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
        return suministroTB;
    }

    public static Object[] Validate_Lote_By_IdSuministro(String idSuministro) {
        Object[] result = new Object[2];
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement statementValidate = null;
            try {
                statementValidate = DBUtil.getConnection().prepareStatement("SELECT Cantidad,Lote FROM SuministroTB WHERE IdSuministro = ?");
                statementValidate.setString(1, idSuministro);
                ResultSet resultSet = statementValidate.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getDouble("Cantidad") > 0) {
                        result[0] = "mayor";
                        result[1] = resultSet.getBoolean("Lote");
                    }
                }
            } catch (SQLException ex) {
                result[0] = ex.getLocalizedMessage();
            } finally {
                try {
                    if (statementValidate != null) {
                        statementValidate.close();
                    }
                    DBUtil.dbDisconnect();
                } catch (SQLException ex) {

                }
            }
        } else {
            result[0] = "No se puedo establecer una conexión con el servidor, intente nuevamente.";
        }
        return result;
    }

    public static String RemoverSuministro(String idSuministro) {
        String result = "";
        DBUtil.dbConnect();
        if (DBUtil.getConnection() != null) {
            PreparedStatement preparedValidate = null;
            PreparedStatement preparedSuministro = null;
            try {
                DBUtil.getConnection().setAutoCommit(false);
                preparedValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM DetalleCompraTB WHERE IdArticulo = ?");
                preparedValidate.setString(1, idSuministro);
                if (preparedValidate.executeQuery().next()) {
                    DBUtil.getConnection().rollback();
                    result = "compra";
                } else {
                    preparedValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM KardexSuministroTB WHERE IdSuministro = ?");
                    preparedValidate.setString(1, idSuministro);
                    if (preparedValidate.executeQuery().next()) {
                        DBUtil.getConnection().rollback();
                        result = "kardex_compra";
                    } else {
                        preparedValidate = DBUtil.getConnection().prepareStatement("SELECT * FROM DetalleVentaTB WHERE IdArticulo = ?");
                        preparedValidate.setString(1, idSuministro);
                        if (preparedValidate.executeQuery().next()) {
                            DBUtil.getConnection().rollback();
                            result = "venta";
                        } else {
                            preparedSuministro = DBUtil.getConnection().prepareStatement("DELETE FROM SuministroTB WHERE IdSuministro = ?");
                            preparedSuministro.setString(1, idSuministro);
                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            preparedSuministro = DBUtil.getConnection().prepareStatement("DELETE FROM PreciosTB WHERE IdSuministro = ?");
                            preparedSuministro.setString(1, idSuministro);
                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            preparedSuministro = DBUtil.getConnection().prepareStatement("DELETE FROM KardexSuministroTB WHERE IdSuministro = ?");
                            preparedSuministro.setString(1, idSuministro);
                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            preparedSuministro = DBUtil.getConnection().prepareStatement("DELETE FROM CantidadTB WHERE IdSuministro = ?");
                            preparedSuministro.setString(1, idSuministro);
                            preparedSuministro.addBatch();
                            preparedSuministro.executeBatch();

                            DBUtil.getConnection().commit();
                            result = "removed";
                        }
                    }
                }

            } catch (SQLException ex) {
                try {
                    result = ex.getLocalizedMessage();
                    DBUtil.getConnection().rollback();
                } catch (SQLException e) {
                    result = e.getLocalizedMessage();
                }
            } finally {
                try {
                    if (preparedValidate != null) {
                        preparedValidate.close();
                    }
                    if (preparedSuministro != null) {
                        preparedSuministro.close();
                    }
                } catch (SQLException ex) {
                    result = ex.getLocalizedMessage();
                }
            }
        } else {
            result = "No se pudo establecer la conexión, revise el estado.";
        }
        return result;
    }

    public static ObservableList<PreciosTB> GetItemPriceList(String idSuministro) {
        PreparedStatement statementVendedor = null;
        ObservableList<PreciosTB> empList = FXCollections.observableArrayList();
        try {
            DBUtil.dbConnect();
            statementVendedor = DBUtil.getConnection().prepareStatement("{CALL Sp_Listar_Precios_By_IdSuministro(?)}");
            statementVendedor.setString(1, idSuministro);
            try (ResultSet resultSet = statementVendedor.executeQuery()) {
                while (resultSet.next()) {
                    PreciosTB preciosTB = new PreciosTB();
                    preciosTB.setId(resultSet.getRow());
                    preciosTB.setIdPrecios(resultSet.getInt("IdPrecios"));
                    preciosTB.setNombre(resultSet.getString("Nombre").toUpperCase());
                    preciosTB.setValor(resultSet.getDouble("Valor"));
                    preciosTB.setFactor(resultSet.getDouble("Factor") <= 0 ? 1 : resultSet.getDouble("Factor"));
                    empList.add(preciosTB);
                }
            }

        } catch (SQLException ex) {

        } finally {
            try {
                if (statementVendedor != null) {
                    statementVendedor.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {

            }
        }
        return empList;
    }

    public static SuministroTB Get_Suministro_By_Search(String value) {
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        SuministroTB suministroTB = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Suministro_By_Search(?)}");
            preparedStatement.setString(1, value);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setMarcaName(rsEmps.getString("Marca"));
                suministroTB.setPresentacionName(rsEmps.getString("Presentacion"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                suministroTB.setUnidadCompra(rsEmps.getInt("UnidadCompra"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraName"));
                suministroTB.setUnidadVenta(rsEmps.getInt("UnidadVenta"));
                suministroTB.setLote(rsEmps.getBoolean("Lote"));

                suministroTB.setIdImpuesto(rsEmps.getInt("Impuesto"));

                ImpuestoTB impuestoTB = new ImpuestoTB();
                impuestoTB.setIdImpuesto(rsEmps.getInt("Impuesto"));
                impuestoTB.setOperacion(rsEmps.getInt("Operacion"));
                impuestoTB.setNombreImpuesto(rsEmps.getString("ImpuestoNombre"));
                impuestoTB.setValor(rsEmps.getDouble("Valor"));
                suministroTB.setImpuestoTB(impuestoTB);

                suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
            }
        } catch (SQLException e) {
            System.out.println("La operación de selección de SQL ha fallado: " + e);
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
        return suministroTB;
    }

    public static List<SuministroTB> getSearchComboBoxSuministros(String buscar, boolean directo) {
        String selectStmt = directo ? "SELECT\n"
                + "s.IdSuministro,\n"
                + "s.Clave,\n"
                + "s.NombreMarca,\n"
                + "d.Nombre AS Unidad\n"
                + "FROM SuministroTB AS s INNER JOIN DetalleTB AS d ON d.IdDetalle = s.UnidadCompra AND d.IdMantenimiento = '0013'\n"
                + "WHERE s.IdSuministro = ?"
                : "SELECT\n"
                + "s.IdSuministro,\n"
                + "s.Clave,\n"
                + "s.NombreMarca,\n"
                + "d.Nombre AS Unidad\n"
                + "FROM SuministroTB AS s INNER JOIN DetalleTB AS d ON d.IdDetalle = s.UnidadCompra AND d.IdMantenimiento = '0013'\n"
                + "WHERE s.Clave LIKE CONCAT(?,'%') OR s.NombreMarca LIKE CONCAT(?,'%')";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        List<SuministroTB> suministroTBs = new ArrayList<>();
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            if (directo) {
                preparedStatement.setString(1, buscar);
            } else {
                preparedStatement.setString(1, buscar);
                preparedStatement.setString(2, buscar);
            }
            rsEmps = preparedStatement.executeQuery();
            while (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setUnidadCompraName(rsEmps.getString("Unidad"));
                suministroTBs.add(suministroTB);
            }

        } catch (SQLException e) {

        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
            } catch (SQLException e) {

            }
        }
        return suministroTBs;
    }

    public static String UpdatedInventarioStockMM(int idAlmacen, String idSuministro, String stockMinimo, String stockMaximo) {
        PreparedStatement preparedUpdatedSuministro = null;

        try {
            DBUtil.dbConnect();
            DBUtil.getConnection().setAutoCommit(false);
            if (idAlmacen == 0) {
                preparedUpdatedSuministro = DBUtil.getConnection().prepareStatement("UPDATE SuministroTB SET StockMinimo = ? , StockMaximo = ? WHERE IdSuministro = ?");
                preparedUpdatedSuministro.setDouble(1, Double.parseDouble(stockMinimo));
                preparedUpdatedSuministro.setDouble(2, Double.parseDouble(stockMaximo));
                preparedUpdatedSuministro.setString(3, idSuministro);
                preparedUpdatedSuministro.addBatch();

                preparedUpdatedSuministro.executeBatch();
                DBUtil.getConnection().commit();
                return "updated";
            } else {
                preparedUpdatedSuministro = DBUtil.getConnection().prepareStatement("UPDATE CantidadTB SET StockMinimo = ? , StockMaximo = ? WHERE IdAlmacen = ? AND IdSuministro = ?");
                preparedUpdatedSuministro.setDouble(1, Double.parseDouble(stockMinimo));
                preparedUpdatedSuministro.setDouble(2, Double.parseDouble(stockMaximo));
                preparedUpdatedSuministro.setInt(3, idAlmacen);
                preparedUpdatedSuministro.setString(4, idSuministro);
                preparedUpdatedSuministro.addBatch();

                preparedUpdatedSuministro.executeBatch();
                DBUtil.getConnection().commit();
                return "updated";
            }
        } catch (SQLException ex) {
            try {
                DBUtil.getConnection().rollback();
            } catch (SQLException e) {
            }
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedUpdatedSuministro != null) {
                    preparedUpdatedSuministro.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException e) {
            }
        }
    }

    public static Object List_Suministros_Movimiento(String idSuministro) {
        String selectStmt = "{call Sp_Get_Suministro_For_Movimiento(?)}";
        PreparedStatement preparedStatement = null;
        ResultSet rsEmps = null;
        try {
            DBUtil.dbConnect();
            preparedStatement = DBUtil.getConnection().prepareStatement(selectStmt);
            preparedStatement.setString(1, idSuministro);
            rsEmps = preparedStatement.executeQuery();
            if (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setId(rsEmps.getRow());
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompraNombre"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                suministroTB.setValorInventario(rsEmps.getShort("ValorInventario"));
                suministroTB.setDiferencia(suministroTB.getCantidad());
                suministroTB.setMovimiento(0);

                TextField tf = new TextField(Tools.roundingValue(0.00, 0));
                tf.getStyleClass().add("text-field-normal");
                tf.setOnKeyTyped(event -> {
                    char c = event.getCharacter().charAt(0);
                    if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                        event.consume();
                    }
                    if (c == '.' && tf.getText().contains(".") || c == '-') {
                        event.consume();
                    }
                });
                suministroTB.setTxtMovimiento(tf);

                TextField tfp = new TextField(Tools.roundingValue(1.00, 0));
                tfp.getStyleClass().add("text-field-normal");
                tfp.setOnKeyTyped(event -> {
                    char c = event.getCharacter().charAt(0);
                    if ((c < '0' || c > '9') && (c != '\b') && (c != '.')) {
                        event.consume();
                    }
                    if (c == '.' && tfp.getText().contains(".") || c == '-') {
                        event.consume();
                    }
                });
                suministroTB.setTxtPeso(tfp);

                CheckBox checkbox = new CheckBox("");
                checkbox.getStyleClass().add("check-box-contenido");
                checkbox.setSelected(true);
                checkbox.setDisable(true);
                suministroTB.setValidar(checkbox);

                Button button = new Button();
                button.getStyleClass().add("buttonDark");
                ImageView view = new ImageView(new Image("/view/image/remove.png"));
                view.setFitWidth(22);
                view.setFitHeight(22);
                button.setGraphic(view);
                suministroTB.setBtnRemove(button);

                return suministroTB;
            } else {
                return "No se encontro un producto relacionado, intente nuevamente.";
            }
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
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
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object GetReporteGeneralInventario(int idInventario, int idUnidad, int idCategoria, int idMarca, int idPresentacion, int existencia) {
        PreparedStatement preparedStatementSuministros = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();

            ArrayList<SuministroTB> empList = new ArrayList();
            preparedStatementSuministros = DBUtil.getConnection().prepareStatement("{call Sp_Reporte_General_Inventario(?,?,?,?,?,?)}");
            preparedStatementSuministros.setInt(1, idInventario);
            preparedStatementSuministros.setInt(2, idUnidad);
            preparedStatementSuministros.setInt(3, idCategoria);
            preparedStatementSuministros.setInt(4, idMarca);
            preparedStatementSuministros.setInt(5, idPresentacion);
            preparedStatementSuministros.setInt(6, existencia);
            rsEmps = preparedStatementSuministros.executeQuery();
            while (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setId(rsEmps.getRow());
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setClaveAlterna(rsEmps.getString("ClaveAlterna"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompra"));
                suministroTB.setEstado(rsEmps.getInt("Estado"));
                suministroTB.setEstadoName(rsEmps.getString("EstadoName"));
                suministroTB.setStockMinimo(rsEmps.getDouble("StockMinimo"));
                suministroTB.setStockMaximo(rsEmps.getDouble("StockMaximo"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                suministroTB.setCategoriaName(rsEmps.getString("Categoria"));
                suministroTB.setMarcaName(rsEmps.getString("Marca"));
                suministroTB.setPresentacionName(rsEmps.getString("Presentacion"));
                empList.add(suministroTB);
            }
            return empList;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatementSuministros != null) {
                    preparedStatementSuministros.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

    public static Object ListInventario(String producto, int tipoExistencia, String nameProduct, int opcion, int categoria, int marca, int idAlmacen, int posicionPaginacion, int filasPorPagina) {
        PreparedStatement preparedStatementSuministros = null;
        PreparedStatement preparedStatementTotales = null;
        ResultSet rsEmps = null;

        try {
            DBUtil.dbConnect();

            Object[] object = new Object[3];
            ObservableList<SuministroTB> empList = FXCollections.observableArrayList();

            preparedStatementSuministros = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Inventario_Suministros(?,?,?,?,?,?,?,?,?)}");
            preparedStatementSuministros.setString(1, producto);
            preparedStatementSuministros.setInt(2, tipoExistencia);
            preparedStatementSuministros.setString(3, nameProduct);
            preparedStatementSuministros.setInt(4, opcion);
            preparedStatementSuministros.setInt(5, categoria);
            preparedStatementSuministros.setInt(6, marca);
            preparedStatementSuministros.setInt(7, idAlmacen);
            preparedStatementSuministros.setInt(8, posicionPaginacion);
            preparedStatementSuministros.setInt(9, filasPorPagina);
            rsEmps = preparedStatementSuministros.executeQuery();
            while (rsEmps.next()) {
                SuministroTB suministroTB = new SuministroTB();
                suministroTB.setId(rsEmps.getRow() + posicionPaginacion);
                suministroTB.setIdSuministro(rsEmps.getString("IdSuministro"));
                suministroTB.setClave(rsEmps.getString("Clave"));
                suministroTB.setClaveAlterna(rsEmps.getString("ClaveAlterna"));
                suministroTB.setNombreMarca(rsEmps.getString("NombreMarca"));
                suministroTB.setCostoCompra(rsEmps.getDouble("PrecioCompra"));
                suministroTB.setPrecioVentaGeneral(rsEmps.getDouble("PrecioVentaGeneral"));
                suministroTB.setCantidad(rsEmps.getDouble("Cantidad"));
                suministroTB.setUnidadCompraName(rsEmps.getString("UnidadCompra"));
                suministroTB.setEstado(rsEmps.getInt("Estado"));
                suministroTB.setEstadoName(rsEmps.getString("EstadoName"));
                suministroTB.setStockMinimo(rsEmps.getDouble("StockMinimo"));
                suministroTB.setStockMaximo(rsEmps.getDouble("StockMaximo"));
                suministroTB.setInventario(rsEmps.getBoolean("Inventario"));
                suministroTB.setCategoriaName(rsEmps.getString("Categoria"));
                suministroTB.setMarcaName(rsEmps.getString("Marca"));

                AlmacenTB almacenTB = new AlmacenTB();
                almacenTB.setIdAlmacen(rsEmps.getInt("IdAlmacen"));
                almacenTB.setNombre(rsEmps.getString("NombreAlmacen"));
                almacenTB.setDireccion(rsEmps.getString("Direccion"));
                suministroTB.setAlmacenTB(almacenTB);

                Label lblCantidad = new Label(Tools.roundingValue(suministroTB.getCantidad(), 2) + " " + suministroTB.getUnidadCompraName());
                lblCantidad.getStyleClass().add("label-existencia");
                lblCantidad.getStyleClass().add(
                        suministroTB.getCantidad() <= 0
                        ? "label-existencia-negativa"
                        : suministroTB.getCantidad() > 0 && suministroTB.getCantidad() < suministroTB.getStockMinimo()
                        ? "label-existencia-intermedia"
                        : suministroTB.getCantidad() >= suministroTB.getStockMinimo() && suministroTB.getCantidad() < suministroTB.getStockMaximo()
                        ? "label-existencia-normal"
                        : "label-existencia-Excedentes");
                suministroTB.setLblCantidad(lblCantidad);

                Button btnTraspaso = new Button();
                btnTraspaso.getStyleClass().add("buttonLightDefault");
                ImageView view = new ImageView(new Image("/view/image/traslado.png"));
                view.setFitWidth(22);
                view.setFitHeight(22);
                btnTraspaso.setGraphic(view);
                suministroTB.setBtnTraspaso(btnTraspaso);

                empList.add(suministroTB);
            }
            object[0] = empList;

            preparedStatementTotales = DBUtil.getConnection().prepareStatement("{call Sp_Listar_Inventario_Suministros_Count(?,?,?,?,?,?,?)}");
            preparedStatementTotales.setString(1, producto);
            preparedStatementTotales.setInt(2, tipoExistencia);
            preparedStatementTotales.setString(3, nameProduct);
            preparedStatementTotales.setInt(4, opcion);
            preparedStatementTotales.setInt(5, categoria);
            preparedStatementTotales.setInt(6, marca);
            preparedStatementTotales.setInt(7, idAlmacen);
            rsEmps = preparedStatementTotales.executeQuery();
            Integer integer = 0;
            if (rsEmps.next()) {
                integer = rsEmps.getInt("Total");
            }
            object[1] = integer;

            return object;
        } catch (SQLException ex) {
            return ex.getLocalizedMessage();
        } finally {
            try {
                if (preparedStatementSuministros != null) {
                    preparedStatementSuministros.close();
                }
                if (preparedStatementTotales != null) {
                    preparedStatementTotales.close();
                }
                if (rsEmps != null) {
                    rsEmps.close();
                }
                DBUtil.dbDisconnect();
            } catch (SQLException ex) {
                return ex.getLocalizedMessage();
            }
        }
    }

}
