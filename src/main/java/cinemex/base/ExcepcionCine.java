package cinemex.base;

/**
 * Error de negocio con un mensaje que se le puede mostrar al usuario tal cual
 * (por ejemplo "El asiento F7 ya está ocupado"). Los controladores lo atrapan
 * y lo muestran en una alerta, así nunca sale una excepción en consola.
 */
public class ExcepcionCine extends RuntimeException {
    public ExcepcionCine(String mensaje) {
        super(mensaje);
    }

    public ExcepcionCine(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
