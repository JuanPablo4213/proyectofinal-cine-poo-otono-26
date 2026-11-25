package cinemex.modelo.peliculas;

/** Contrato de los filtros de búsqueda: ¿esta película pasa el filtro? */
public interface IFiltroPelicula {
    boolean cumple(POJOPelicula p);
}
