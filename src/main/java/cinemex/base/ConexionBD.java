package cinemex.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Una sola conexión a MySQL compartida por todo el programa (como en el diagrama de clases).
 * Los Servicios la piden con ConexionBD.obtener() y usan PreparedStatement (RNF02).
 */
public class ConexionBD {
    private static Connection conexion;

    private ConexionBD() { }

    /** Abre la conexión si no existe o se cerró, y la regresa. */
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

    /** Se llama al cerrar la aplicación. */
    public static void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ignorada) {
            // al salir no importa
        }
    }
}
