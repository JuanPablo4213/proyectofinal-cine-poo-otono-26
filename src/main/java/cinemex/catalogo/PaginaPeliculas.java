package cinemex.catalogo;

import cinemex.modelo.peliculas.POJOPelicula;
import java.util.List;

/** Una página de resultados ya convertida a objetos del modelo. */
public class PaginaPeliculas {
    private final int _pagina;
    private final int _totalPaginas;
    private final int _totalResultados;
    private final List<POJOPelicula> _peliculas;

    public PaginaPeliculas(int pagina, int totalPaginas, int totalResultados, List<POJOPelicula> peliculas) {
        this._pagina = pagina;
        // TMDB no deja pedir más allá de la página 500
        this._totalPaginas = Math.min(totalPaginas, 500);
        this._totalResultados = totalResultados;
        this._peliculas = peliculas;
    }

    public boolean tieneAnterior() { return _pagina > 1; }
    public boolean tieneSiguiente() { return _pagina < _totalPaginas; }
    public int getPagina() { return _pagina; }
    public int getTotalPaginas() { return _totalPaginas; }
    public int getTotalResultados() { return _totalResultados; }
    public List<POJOPelicula> getPeliculas() { return _peliculas; }
}
