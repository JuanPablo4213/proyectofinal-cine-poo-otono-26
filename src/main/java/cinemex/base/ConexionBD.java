package cinemex.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static Connection conexion;

    private ConexionBD() { }

    public static Connection obtener() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                String url = Config.get("db.url");
                if (url.isEmpty()) {
                    throw new ExcepcionCine("Falta db.url en config.properties");
                }
                conexion = DriverManager.getConnection(url, Config.get("db.usuario"), Config.get("db.password"));
            }
            return conexion;
        } catch (SQLException e) {
            throw new ExcepcionCine("No se pudo conectar a MySQL. ¿Está corriendo Docker? (" + e.getMessage() + ")", e);
        }
    }

    public static void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ignorada) {
        }
    }
}
