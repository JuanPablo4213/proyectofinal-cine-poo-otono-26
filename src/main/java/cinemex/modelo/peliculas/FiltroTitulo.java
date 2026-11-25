package cinemex.modelo.peliculas;

/** Pasa si el título contiene el texto (sin importar mayúsculas). */
public class FiltroTitulo implements IFiltroPelicula {
    private String _texto;

    public FiltroTitulo(String texto) {
        this._texto = texto.toLowerCase();
    }

    @Override
    public boolean cumple(POJOPelicula p) {
        return p.getTitulo() != null && p.getTitulo().toLowerCase().contains(_texto);
    }
}
