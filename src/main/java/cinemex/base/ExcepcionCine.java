package cinemex.base;

public class ExcepcionCine extends RuntimeException {
    public ExcepcionCine(String mensaje) {
        super(mensaje);
    }

    public ExcepcionCine(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
