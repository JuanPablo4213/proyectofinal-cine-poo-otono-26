package cinemex.modelo.peliculas;

/** Pasa si la película tiene ese género. */
public class FiltroGenero implements IFiltroPelicula {
    private POJOGenero _genero;

    public FiltroGenero(POJOGenero genero) {
        this._genero = genero;
    }

    @Override
    public boolean cumple(POJOPelicula p) {
        return p.getGeneros().contains(_genero);
    }
}
