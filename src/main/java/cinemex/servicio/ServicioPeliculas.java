package cinemex.servicio;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.peliculas.Clasificacion;
import cinemex.modelo.peliculas.IFiltroPelicula;
import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicioPeliculas extends BaseServicio {
    public boolean guardar(POJOPelicula obj) {
        if (obj == null || obj.getTmdbId() <= 0) {
            throw new ExcepcionCine("La película no tiene id de TMDB.");
        }
        requerido(obj.getTitulo(), "Título");
        Connection c = con();
        try {
            c.setAutoCommit(false);
            POJOPelicula existente = buscarPorTmdbId(obj.getTmdbId());
            if (existente == null) {
                insertar(c, obj);
            } else {
                obj.setId(existente.getId());

                if (obj.getClasificacion() == Clasificacion.SC) {
                    obj.setClasificacion(existente.getClasificacion());
                }
                obj.setEnCartelera(existente.isEnCartelera());
                actualizarDatos(c, obj);
            }
            guardarGeneros(c, obj);
            c.commit();
            return true;
        } catch (SQLException e) {
            rollback(c);
            throw errorBD(e);
        } finally {
            autoCommit(c);
        }
    }

    public boolean actualizar(POJOPelicula obj) {
        String sql = "UPDATE peliculas SET clasificacion = ?, en_cartelera = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, obj.getClasificacion().name());
            ps.setBoolean(2, obj.isEnCartelera());
            ps.setInt(3, obj.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public boolean eliminar(int id) {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM peliculas WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (esErrorIntegridad(e)) {
                throw new ExcepcionCine("No se puede eliminar: la película tiene funciones programadas. "
                        + "Mejor quítala de cartelera.");
            }
            throw errorBD(e);
        }
    }

    public POJOPelicula buscarPorId(int id) {
        List<POJOPelicula> r = consultar("SELECT * FROM peliculas WHERE id = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    public POJOPelicula buscarPorTmdbId(int tmdbId) {
        List<POJOPelicula> r = consultar("SELECT * FROM peliculas WHERE tmdb_id = ?", tmdbId);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<POJOPelicula> listar() {
        return consultar("SELECT * FROM peliculas ORDER BY titulo");
    }

    public List<POJOPelicula> listarCartelera() {
        return consultar("SELECT * FROM peliculas WHERE en_cartelera = TRUE ORDER BY titulo");
    }

    public List<POJOPelicula> buscar(List<IFiltroPelicula> filtros) {
        List<POJOPelicula> resultado = new ArrayList<>();
        for (POJOPelicula p : listarCartelera()) {
            if (filtros.stream().allMatch(f -> f.cumple(p))) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    public void sincronizarGeneros(List<POJOGenero> generos) {
        String sql = "INSERT INTO generos (id, nombre) VALUES (?, ?) ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (POJOGenero g : generos) {
                ps.setInt(1, g.getId());
                ps.setString(2, g.getNombre());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    public List<POJOGenero> listarGeneros() {
        List<POJOGenero> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement("SELECT id, nombre FROM generos ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new POJOGenero(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return lista;
    }

    private void insertar(Connection c, POJOPelicula p) throws SQLException {
        String sql = "INSERT INTO peliculas (tmdb_id, titulo, sinopsis, duracion_min, clasificacion, poster_path, "
                + "fecha_estreno, en_cartelera) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getTmdbId());
            ps.setString(2, p.getTitulo());
            ps.setString(3, p.getSinopsis());
            ps.setInt(4, p.getDuracionMin());
            ps.setString(5, p.getClasificacion().name());
            ps.setString(6, p.getPosterPath());
            ps.setDate(7, p.getFechaEstreno() == null ? null : Date.valueOf(p.getFechaEstreno()));
            ps.setBoolean(8, true);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
            p.setEnCartelera(true);
        }
    }

    private void actualizarDatos(Connection c, POJOPelicula p) throws SQLException {
        String sql = "UPDATE peliculas SET titulo = ?, sinopsis = ?, duracion_min = ?, clasificacion = ?, "
                + "poster_path = ?, fecha_estreno = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getTitulo());
            ps.setString(2, p.getSinopsis());
            ps.setInt(3, p.getDuracionMin());
            ps.setString(4, p.getClasificacion().name());
            ps.setString(5, p.getPosterPath());
            ps.setDate(6, p.getFechaEstreno() == null ? null : Date.valueOf(p.getFechaEstreno()));
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    private void guardarGeneros(Connection c, POJOPelicula p) throws SQLException {
        try (PreparedStatement borrar = c.prepareStatement("DELETE FROM pelicula_genero WHERE pelicula_id = ?")) {
            borrar.setInt(1, p.getId());
            borrar.executeUpdate();
        }
        String altaGenero = "INSERT INTO generos (id, nombre) VALUES (?, ?) ON DUPLICATE KEY UPDATE nombre = VALUES(nombre)";
        String relacion = "INSERT INTO pelicula_genero (pelicula_id, genero_id) VALUES (?, ?)";
        try (PreparedStatement psG = c.prepareStatement(altaGenero);
             PreparedStatement psR = c.prepareStatement(relacion)) {
            for (POJOGenero g : p.getGeneros()) {
                psG.setInt(1, g.getId());
                psG.setString(2, g.getNombre());
                psG.executeUpdate();
                psR.setInt(1, p.getId());
                psR.setInt(2, g.getId());
                psR.executeUpdate();
            }
        }
    }

    private List<POJOPelicula> consultar(String sql, Object... params) {
        List<POJOPelicula> lista = new ArrayList<>();
        Map<Integer, POJOPelicula> porId = new HashMap<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fecha = rs.getDate("fecha_estreno");
                    POJOPelicula p = new POJOPelicula(rs.getInt("id"), rs.getInt("tmdb_id"), rs.getString("titulo"),
                            rs.getString("sinopsis"), rs.getInt("duracion_min"),
                            Clasificacion.valueOf(rs.getString("clasificacion")), rs.getString("poster_path"),
                            fecha == null ? null : fecha.toLocalDate());
                    p.setEnCartelera(rs.getBoolean("en_cartelera"));
                    lista.add(p);
                    porId.put(p.getId(), p);
                }
            }
            if (!porId.isEmpty()) {
                cargarGeneros(porId);
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return lista;
    }

    private void cargarGeneros(Map<Integer, POJOPelicula> porId) throws SQLException {
        String sql = "SELECT pg.pelicula_id, g.id, g.nombre FROM pelicula_genero pg "
                + "JOIN generos g ON g.id = pg.genero_id ORDER BY g.nombre";
        try (PreparedStatement ps = con().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                POJOPelicula p = porId.get(rs.getInt("pelicula_id"));
                if (p != null) {
                    p.getGeneros().add(new POJOGenero(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        }
    }
}
