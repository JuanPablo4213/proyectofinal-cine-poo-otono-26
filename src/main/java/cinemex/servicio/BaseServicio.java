package cinemex.servicio;

import cinemex.base.ConexionBD;
import cinemex.base.ExcepcionCine;
import java.sql.Connection;
import java.sql.SQLException;

/** Utilidades que comparten todos los Servicios para hablar con MySQL. */
abstract class BaseServicio {

    protected Connection con() {
        return ConexionBD.obtener();
    }

    /** true si el error es de integridad (llave duplicada o llave foránea), SQLState 23xxx. */
    protected boolean esErrorIntegridad(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    protected ExcepcionCine errorBD(SQLException e) {
        return new ExcepcionCine("Error de base de datos: " + e.getMessage(), e);
    }

    /** Deshace la transacción en curso sin lanzar otra excepción. */
    protected void rollback(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignorada) {
            // ya estamos manejando otro error
        }
    }

    protected void autoCommit(Connection c) {
        try {
            c.setAutoCommit(true);
        } catch (SQLException ignorada) {
            // la siguiente operación lo reintentará
        }
    }

    protected static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ExcepcionCine("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }
}
