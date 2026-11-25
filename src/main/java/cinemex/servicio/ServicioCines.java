package cinemex.servicio;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.cines.POJOSalaPlatino;
import cinemex.modelo.cines.POJOSalaTradicional;
import cinemex.modelo.cines.POJOSalaX4D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Cines, sus salas y sus asientos. */
public class ServicioCines extends BaseServicio {

    /** Nuevo (id = 0) o actualizar (id > 0). */
    public boolean guardar(POJOCine obj) {
        String nombre = requerido(obj.getNombre(), "Nombre");
        String ciudad = requerido(obj.getCiudad(), "Ciudad");
        String direccion = requerido(obj.getDireccion(), "Dirección");
        boolean nuevo = obj.getId() == 0;
        String sql = nuevo
                ? "INSERT INTO cines (nombre, ciudad, direccion) VALUES (?, ?, ?)"
                : "UPDATE cines SET nombre = ?, ciudad = ?, direccion = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, ciudad);
            ps.setString(3, direccion);
            if (!nuevo) {
                ps.setInt(4, obj.getId());
            }
            ps.executeUpdate();
            if (nuevo) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        obj.setId(rs.getInt(1));
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /** Borra el cine con sus salas (composición). Falla si alguna sala tiene funciones. */
    public boolean eliminar(int id) {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM cines WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (esErrorIntegridad(e)) {
                throw new ExcepcionCine("No se puede eliminar: el cine tiene salas con funciones programadas.");
            }
            throw errorBD(e);
        }
    }

    public POJOCine buscarPorId(int id) {
        List<POJOCine> r = consultar("SELECT * FROM cines WHERE id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<POJOCine> listar() {
        return consultar("SELECT * FROM cines ORDER BY ciudad, nombre");
    }

    public List<POJOCine> listarPorCiudad(String ciudad) {
        return consultar("SELECT * FROM cines WHERE ciudad = ? ORDER BY nombre", ciudad);
    }

    public List<String> listarCiudades() {
        List<String> ciudades = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement("SELECT DISTINCT ciudad FROM cines ORDER BY ciudad");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ciudades.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return ciudades;
    }

    /**
     * Fábrica de salas: el ÚNICO lugar que sabe qué subclase crear según el tipo.
     * Lo usan la pantalla de administración y la lectura desde MySQL.
     */
    public POJOSala crearSala(String tipo, int id, int cineId, int numero, int filas, int columnas, double cargoEfectos) {
        if (filas < 1 || filas > 26 || columnas < 1 || columnas > 30) {
            throw new ExcepcionCine("Una sala debe tener de 1 a 26 filas y de 1 a 30 columnas.");
        }
        switch (tipo.toUpperCase()) {
            case "PLATINO":
                return new POJOSalaPlatino(id, cineId, numero, filas, columnas);
            case "X4D":
                return new POJOSalaX4D(id, cineId, numero, filas, columnas, cargoEfectos);
            default:
                return new POJOSalaTradicional(id, cineId, numero, filas, columnas);
        }
    }

    /** Guarda la sala con todos sus asientos en una sola transacción. */
    public boolean agregarSala(POJOCine cine, POJOSala sala) {
        if (!cine.agregarSala(sala)) {
            throw new ExcepcionCine("El cine ya tiene una sala " + sala.getNumero() + ".");
        }
        Connection c = con();
        try {
            c.setAutoCommit(false);
            String sql = "INSERT INTO salas (cine_id, numero, tipo, filas, columnas, cargo_efectos) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cine.getId());
                ps.setInt(2, sala.getNumero());
                ps.setString(3, sala.getTipoSala().toUpperCase());
                ps.setInt(4, sala.getFilas());
                ps.setInt(5, sala.getColumnas());
                ps.setDouble(6, sala instanceof POJOSalaX4D x ? x.getCargoEfectos() : 0);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    sala.setId(rs.getInt(1));
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO asientos (sala_id, fila, numero) VALUES (?, ?, ?)")) {
                for (POJOAsiento a : sala.getAsientos()) {
                    ps.setInt(1, sala.getId());
                    ps.setString(2, String.valueOf(a.getFila()));
                    ps.setInt(3, a.getNumero());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            cargarAsientos(sala);
            return true;
        } catch (SQLException e) {
            rollback(c);
            cine.getSalas().remove(sala);
            if (esErrorIntegridad(e)) {
                throw new ExcepcionCine("El cine ya tiene una sala " + sala.getNumero() + ".");
            }
            throw errorBD(e);
        } finally {
            autoCommit(c);
        }
    }

    /** Borra la sala y sus asientos. Falla si tiene funciones. */
    public boolean eliminarSala(int salaId) {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM salas WHERE id = ?")) {
            ps.setInt(1, salaId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (esErrorIntegridad(e)) {
                throw new ExcepcionCine("No se puede eliminar: la sala tiene funciones programadas.");
            }
            throw errorBD(e);
        }
    }

    /** Reemplaza los asientos generados en memoria por los de MySQL (con su id real). */
    public void cargarAsientos(POJOSala sala) {
        List<POJOAsiento> asientos = new ArrayList<>();
        String sql = "SELECT id, fila, numero FROM asientos WHERE sala_id = ? ORDER BY fila, numero";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, sala.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    asientos.add(new POJOAsiento(rs.getInt("id"), rs.getString("fila").charAt(0), rs.getInt("numero")));
                }
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        sala.setAsientos(asientos);
    }

    // ------------------------------------------------------------------ privados

    private List<POJOCine> consultar(String sql, Object... params) {
        Map<Integer, POJOCine> cines = new LinkedHashMap<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    POJOCine cine = new POJOCine(rs.getInt("id"), rs.getString("nombre"),
                            rs.getString("ciudad"), rs.getString("direccion"));
                    cines.put(cine.getId(), cine);
                }
            }
            if (!cines.isEmpty()) {
                cargarSalas(cines);
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return new ArrayList<>(cines.values());
    }

    private void cargarSalas(Map<Integer, POJOCine> cines) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT * FROM salas ORDER BY cine_id, numero");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                POJOCine cine = cines.get(rs.getInt("cine_id"));
                if (cine != null) {
                    cine.agregarSala(crearSala(rs.getString("tipo"), rs.getInt("id"), cine.getId(), rs.getInt("numero"),
                            rs.getInt("filas"), rs.getInt("columnas"), rs.getDouble("cargo_efectos")));
                }
            }
        }
    }
}
