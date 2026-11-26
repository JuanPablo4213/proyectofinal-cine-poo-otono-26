package cinemex.modelo.peliculas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Una película. Puede venir de TMDB (id = 0, todavía no está en mi cine)
 * o de MySQL (id > 0, ya fue importada y se le pueden programar funciones).
 */
public class POJOPelicula {
    public static final String URL_POSTER = "https://image.tmdb.org/t/p/w500";

    private int _id;
    private int _tmdbId;
    private String _titulo;
    private String _sinopsis;
    private int _duracionMin;
    private Clasificacion _clasificacion;
    private String _posterPath;
    private LocalDate _fechaEstreno;
    private double _calificacion;
    private boolean _enCartelera;
    private List<POJOGenero> _generos;

    public POJOPelicula(int id, int tmdbId, String titulo, String sinopsis, int duracionMin,
                        Clasificacion clasificacion, String posterPath, LocalDate fechaEstreno) {
        this._id = id;
        this._tmdbId = tmdbId;
        this._titulo = titulo;
        this._sinopsis = sinopsis;
        this._duracionMin = duracionMin;
        this._clasificacion = clasificacion == null ? Clasificacion.SC : clasificacion;
        this._posterPath = posterPath;
        this._fechaEstreno = fechaEstreno;
        this._enCartelera = true;
        this._generos = new ArrayList<>();
    }

    /** true si ya está guardada en MySQL. */
    public boolean estaImportada() {
        return _id > 0;
    }

    /**
     * URL completa del póster, o null si no tiene. La API del grupo ya manda la URL completa;
     * TMDB manda solo el poster_path ("/abc.jpg") y hay que ponerle la base.
     */
    public String getUrlPoster() {
        if (_posterPath == null || _posterPath.isBlank()) {
            return null;
        }
        return _posterPath.startsWith("http") ? _posterPath : URL_POSTER + _posterPath;
    }

    public String getGenerosTexto() {
        return _generos.stream().map(POJOGenero::getNombre).collect(Collectors.joining(", "));
    }

    public String getAnio() {
        return _fechaEstreno == null ? "" : String.valueOf(_fechaEstreno.getYear());
    }

    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public int getTmdbId() { return _tmdbId; }
    public String getTitulo() { return _titulo; }
    public String getSinopsis() { return _sinopsis; }
    public int getDuracionMin() { return _duracionMin; }
    public Clasificacion getClasificacion() { return _clasificacion; }
    public void setClasificacion(Clasificacion c) { this._clasificacion = c; }
    public String getPosterPath() { return _posterPath; }
    public LocalDate getFechaEstreno() { return _fechaEstreno; }
    public double getCalificacion() { return _calificacion; }
    public void setCalificacion(double calificacion) { this._calificacion = calificacion; }
    public boolean isEnCartelera() { return _enCartelera; }
    public void setEnCartelera(boolean valor) { this._enCartelera = valor; }
    public List<POJOGenero> getGeneros() { return _generos; }
    public void setGeneros(List<POJOGenero> generos) { this._generos = new ArrayList<>(generos); }

    @Override
    public String toString() {
        return _titulo + (getAnio().isEmpty() ? "" : " (" + getAnio() + ")");
    }
}
