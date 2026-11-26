package cinemex.catalogo;

import cinemex.base.ExcepcionCine;

/** Error al consultar el catálogo de películas, con mensaje para el usuario. */
public class ExcepcionCatalogo extends ExcepcionCine {
    public ExcepcionCatalogo(String mensaje) {
        super(mensaje);
    }

    public ExcepcionCatalogo(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
