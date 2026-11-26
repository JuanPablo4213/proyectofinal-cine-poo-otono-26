package cinemex.catalogo;

import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import java.util.List;

/**
 * Contrato de cualquier fuente del catálogo de películas.
 * Hay dos implementaciones: ClienteCinemexAPI (la API del grupo, la que se usa)
 * y ClienteTMDB (directo a themoviedb.org). Los controladores solo conocen esta
 * interfaz, así que cambiar de fuente no toca la interfaz gráfica (polimorfismo).
 */
public interface IClientePeliculas {
    PaginaPeliculas populares(int pagina);

    PaginaPeliculas enCartelera(int pagina);

    PaginaPeliculas proximosEstrenos(int pagina);

    PaginaPeliculas buscar(String texto, int pagina);

    POJOPelicula detalle(int id);

    List<POJOGenero> generos();

    /** Nombre para mostrar en pantalla ("API Cinemex" o "TMDB"). */
    String getNombre();
}
