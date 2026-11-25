package cinemex.tmdb;

import cinemex.base.ExcepcionCine;

/** Error al hablar con TMDB, con un mensaje entendible para el usuario. */
public class ExcepcionTMDB extends ExcepcionCine {
    public ExcepcionTMDB(String mensaje) {
        super(mensaje);
    }

    public ExcepcionTMDB(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
