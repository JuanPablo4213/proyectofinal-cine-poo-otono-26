package cinemex.servicio;

import cinemex.base.ConexionBD;
import cinemex.base.ExcepcionCine;
import java.sql.Connection;
import java.sql.SQLException;

abstract class BaseServicio {
    protected Connection con() {
        return ConexionBD.obtener();
    }

    protected boolean esErrorIntegridad(SQLException e) {
        return e.getSQLState() != null && e.getSQLState().startsWith("23");
    }

    protected ExcepcionCine errorBD(SQLException e) {
        return new ExcepcionCine("Error de base de datos: " + e.getMessage(), e);
    }

    protected void rollback(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignorada) {
        }
    }

    protected void autoCommit(Connection c) {
        try {
            c.setAutoCommit(true);
        } catch (SQLException ignorada) {
        }
    }

    protected static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ExcepcionCine("El campo \"" + campo + "\" es obligatorio.");
        }
        return valor.trim();
    }
}
