package cinemex.tmdb;

import cinemex.catalogo.ExcepcionCatalogo;

public class ExcepcionTMDB extends ExcepcionCatalogo {
    public ExcepcionTMDB(String mensaje) {
        super(mensaje);
    }

    public ExcepcionTMDB(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
