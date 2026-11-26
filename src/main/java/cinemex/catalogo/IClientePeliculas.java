package cinemex.catalogo;

import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import java.util.List;

public interface IClientePeliculas {
    PaginaPeliculas populares(int pagina);

    PaginaPeliculas enCartelera(int pagina);

    PaginaPeliculas proximosEstrenos(int pagina);

    PaginaPeliculas buscar(String texto, int pagina);

    POJOPelicula detalle(int id);

    List<POJOGenero> generos();

    String getNombre();
}
