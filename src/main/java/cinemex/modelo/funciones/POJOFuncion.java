package cinemex.modelo.funciones;

import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.peliculas.POJOPelicula;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Una función = película + sala + fecha y hora (RN02). Aquí vive la lista de
 * asientos ya vendidos: la silla F7 puede estar libre a las 4 pm y ocupada a las 7 pm.
 */
public class POJOFuncion {
    /** Minutos de limpieza entre una función y la siguiente en la misma sala (RN03). */
    public static final int MINUTOS_LIMPIEZA = 20;

    private int _id;
    private POJOPelicula _pelicula;
    private POJOCine _cine;
    private POJOSala _sala;
    private LocalDateTime _fechaHora;
    private Idioma _idioma;
    private List<POJOAsiento> _asientosOcupados;

    public POJOFuncion(int id, POJOPelicula pelicula, POJOCine cine, POJOSala sala,
                       LocalDateTime fechaHora, Idioma idioma) {
        this._id = id;
        this._pelicula = pelicula;
        this._cine = cine;
        this._sala = sala;
        this._fechaHora = fechaHora;
        this._idioma = idioma;
        this._asientosOcupados = new ArrayList<>();
    }

    /** El asiento existe en la sala y no está vendido. */
    public boolean estaDisponible(POJOAsiento a) {
        return a != null && _sala.getAsientos().contains(a) && !_asientosOcupados.contains(a);
    }

    /** Marca el asiento como vendido. false si ya estaba ocupado. */
    public boolean ocupar(POJOAsiento a) {
        if (!estaDisponible(a)) {
            return false;
        }
        _asientosOcupados.add(a);
        return true;
    }

    /** Libera el asiento (al cancelar una compra, RN08). */
    public void liberar(POJOAsiento a) {
        _asientosOcupados.remove(a);
    }

    public List<POJOAsiento> getAsientosDisponibles() {
        List<POJOAsiento> libres = new ArrayList<>();
        for (POJOAsiento a : _sala.getAsientos()) {
            if (!_asientosOcupados.contains(a)) {
                libres.add(a);
            }
        }
        return libres;
    }

    /** Hora en que la sala vuelve a estar libre: inicio + duración + limpieza. */
    public LocalDateTime calcularHoraFin() {
        return _fechaHora.plusMinutes(_pelicula.getDuracionMin() + MINUTOS_LIMPIEZA);
    }

    public boolean yaEmpezo() {
        return !LocalDateTime.now().isBefore(_fechaHora);
    }

    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public POJOPelicula getPelicula() { return _pelicula; }
    public POJOCine getCine() { return _cine; }
    public POJOSala getSala() { return _sala; }
    public LocalDateTime getFechaHora() { return _fechaHora; }
    public Idioma getIdioma() { return _idioma; }
    public List<POJOAsiento> getAsientosOcupados() { return _asientosOcupados; }
    public void setAsientosOcupados(List<POJOAsiento> ocupados) { this._asientosOcupados = new ArrayList<>(ocupados); }

    @Override
    public String toString() {
        return _pelicula.getTitulo() + " - " + _cine + " " + _sala + " - " + _fechaHora;
    }
}
