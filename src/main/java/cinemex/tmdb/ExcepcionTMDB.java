package cinemex.tmdb;

import cinemex.catalogo.ExcepcionCatalogo;

/** Error al hablar con TMDB, con un mensaje entendible para el usuario. */
public class ExcepcionTMDB extends ExcepcionCatalogo {
    public ExcepcionTMDB(String mensaje) {
        super(mensaje);
    }

    public ExcepcionTMDB(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
