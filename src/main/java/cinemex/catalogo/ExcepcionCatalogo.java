package cinemex.catalogo;

import cinemex.base.ExcepcionCine;

public class ExcepcionCatalogo extends ExcepcionCine {
    public ExcepcionCatalogo(String mensaje) {
        super(mensaje);
    }

    public ExcepcionCatalogo(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
