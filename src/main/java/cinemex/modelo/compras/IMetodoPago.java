package cinemex.modelo.compras;

/** Contrato de cualquier forma de pago (igual que en Airbnb). */
public interface IMetodoPago {
    boolean validar();

    double calcularComision(double monto);

    /** "Tarjeta" o "PayPal", para guardarlo en la base. */
    String getNombre();

    /** Dato seguro para guardar: últimos 4 dígitos o el correo (RNF02). */
    String getReferencia();
}
