package cinemex.servicio;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.funciones.Idioma;
import cinemex.modelo.funciones.POJOFuncion;
import cinemex.modelo.peliculas.POJOPelicula;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Horarios: qué película se proyecta en qué sala y cuándo. */
public class ServicioFunciones extends BaseServicio {
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ServicioPeliculas _peliculas;
    private final ServicioCines _cines;

    public ServicioFunciones(ServicioPeliculas peliculas, ServicioCines cines) {
        this._peliculas = peliculas;
        this._cines = cines;
    }

    /**
     * Nueva (id = 0) o actualizar (id > 0). Reglas:
     * RN02 película + sala + fecha; RN03 no se empalma con otra función de la misma sala.
     */
    public boolean guardar(POJOFuncion obj) {
        if (obj.getPelicula() == null || !obj.getPelicula().estaImportada()) {
            throw new ExcepcionCine("Elige una película de tu cartelera.");
        }
        if (obj.getSala() == null) {
            throw new ExcepcionCine("Elige una sala.");
        }
        if (obj.getFechaHora() == null || obj.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new ExcepcionCine("La función debe ser en el futuro.");
        }
        if (obj.getId() > 0 && contarBoletos(obj.getId()) > 0) {
            throw new ExcepcionCine("No se puede modificar: la función ya tiene boletos vendidos.");
        }
        validarEmpalme(obj);
        boolean nueva = obj.getId() == 0;
        String sql = nueva
                ? "INSERT INTO funciones (pelicula_id, sala_id, fecha_hora, idioma) VALUES (?, ?, ?, ?)"
                : "UPDATE funciones SET pelicula_id = ?, sala_id = ?, fecha_hora = ?, idioma = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, obj.getPelicula().getId());
            ps.setInt(2, obj.getSala().getId());
            ps.setTimestamp(3, Timestamp.valueOf(obj.getFechaHora()));
            ps.setString(4, obj.getIdioma().name());
            if (!nueva) {
                ps.setInt(5, obj.getId());
            }
            ps.executeUpdate();
            if (nueva) {
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

    /** Solo se puede borrar una función sin boletos vendidos. */
    public boolean eliminar(int id) {
        if (contarBoletos(id) > 0) {
            throw new ExcepcionCine("No se puede eliminar: la función ya tiene boletos vendidos.");
        }
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM funciones WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /** La función con los asientos reales de su sala y cuáles ya están vendidos (para el mapa). */
    public POJOFuncion buscarPorId(int id) {
        List<POJOFuncion> r = consultar("SELECT * FROM funciones WHERE id = ?", id);
        if (r.isEmpty()) {
            return null;
        }
        POJOFuncion f = r.get(0);
        _cines.cargarAsientos(f.getSala());
        cargarOcupados(f);
        return f;
    }

    /** Todas, las más recientes primero (pantalla de administración). */
    public List<POJOFuncion> listar() {
        return consultar("SELECT * FROM funciones ORDER BY fecha_hora DESC");
    }

    /** Funciones futuras de una película. */
    public List<POJOFuncion> listarPorPelicula(POJOPelicula p) {
        return consultar("SELECT * FROM funciones WHERE pelicula_id = ? AND fecha_hora > NOW() ORDER BY fecha_hora",
                p.getId());
    }

    /** Funciones de una película en un cine y día, ordenadas por hora. */
    public List<POJOFuncion> listarPorCineYPelicula(POJOCine c, POJOPelicula p, LocalDate dia) {
        List<POJOFuncion> resultado = new ArrayList<>();
        for (POJOFuncion f : listarPorCineYDia(c, dia)) {
            if (f.getPelicula().getId() == p.getId()) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    /** Todo lo que se proyecta en un cine ese día (pantalla "Mi cine"). */
    public List<POJOFuncion> listarPorCineYDia(POJOCine c, LocalDate dia) {
        List<POJOFuncion> resultado = new ArrayList<>();
        String sql = "SELECT * FROM funciones WHERE fecha_hora >= ? AND fecha_hora < ? AND fecha_hora > NOW() "
                + "ORDER BY fecha_hora";
        for (POJOFuncion f : consultar(sql, Timestamp.valueOf(dia.atStartOfDay()),
                Timestamp.valueOf(dia.plusDays(1).atStartOfDay()))) {
            if (c.tieneSala(f.getSala())) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    /** Asientos vendidos (boletos vigentes) de la función. */
    public void cargarOcupados(POJOFuncion f) {
        List<POJOAsiento> ocupados = new ArrayList<>();
        String sql = "SELECT a.id, a.fila, a.numero FROM boletos b JOIN asientos a ON a.id = b.asiento_id "
                + "WHERE b.funcion_id = ? AND b.vigente = 1";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, f.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ocupados.add(new POJOAsiento(rs.getInt("id"), rs.getString("fila").charAt(0), rs.getInt("numero")));
                }
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        f.setAsientosOcupados(ocupados);
    }

    public int contarBoletos(int funcionId) {
        try (PreparedStatement ps = con().prepareStatement(
                "SELECT COUNT(*) FROM boletos WHERE funcion_id = ? AND vigente = 1")) {
            ps.setInt(1, funcionId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    // ------------------------------------------------------------------ privados

    /** RN03: dos funciones de la misma sala no pueden empalmarse (incluye 20 min de limpieza). */
    private void validarEmpalme(POJOFuncion nueva) {
        String sql = "SELECT * FROM funciones WHERE sala_id = ? AND id <> ? AND fecha_hora BETWEEN ? AND ?";
        List<POJOFuncion> cercanas = consultar(sql, nueva.getSala().getId(), nueva.getId(),
                Timestamp.valueOf(nueva.getFechaHora().minusHours(8)),
                Timestamp.valueOf(nueva.getFechaHora().plusHours(8)));
        for (POJOFuncion otra : cercanas) {
            boolean empalma = nueva.getFechaHora().isBefore(otra.calcularHoraFin())
                    && otra.getFechaHora().isBefore(nueva.calcularHoraFin());
            if (empalma) {
                throw new ExcepcionCine("Se empalma con \"" + otra.getPelicula().getTitulo() + "\" de "
                        + otra.getFechaHora().format(HORA) + " a " + otra.calcularHoraFin().format(HORA)
                        + " en la misma sala.");
            }
        }
    }

    /** Arma las funciones con su película, cine y sala ya como objetos. */
    private List<POJOFuncion> consultar(String sql, Object... params) {
        Map<Integer, POJOPelicula> peliculas = new HashMap<>();
        for (POJOPelicula p : _peliculas.listar()) {
            peliculas.put(p.getId(), p);
        }
        Map<Integer, POJOSala> salas = new HashMap<>();
        Map<Integer, POJOCine> cineDeSala = new HashMap<>();
        for (POJOCine c : _cines.listar()) {
            for (POJOSala s : c.getSalas()) {
                salas.put(s.getId(), s);
                cineDeSala.put(s.getId(), c);
            }
        }
        List<POJOFuncion> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int salaId = rs.getInt("sala_id");
                    lista.add(new POJOFuncion(rs.getInt("id"), peliculas.get(rs.getInt("pelicula_id")),
                            cineDeSala.get(salaId), salas.get(salaId), rs.getTimestamp("fecha_hora").toLocalDateTime(),
                            Idioma.valueOf(rs.getString("idioma"))));
                }
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return lista;
    }
}
