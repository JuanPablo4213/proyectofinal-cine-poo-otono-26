package cinemex.modelo.peliculas;

public class FiltroClasificacion implements IFiltroPelicula {
    private Clasificacion _clasificacion;

    public FiltroClasificacion(Clasificacion clasificacion) {
        this._clasificacion = clasificacion;
    }

    @Override
    public boolean cumple(POJOPelicula p) {
        return p.getClasificacion() == _clasificacion;
    }
}
