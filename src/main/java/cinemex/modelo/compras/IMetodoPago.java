package cinemex.modelo.compras;

public interface IMetodoPago {
    boolean validar();

    double calcularComision(double monto);

    String getNombre();

    String getReferencia();
}
