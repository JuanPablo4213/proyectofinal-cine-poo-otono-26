package cinemex.modelo.compras;

/** Pago simulado con tarjeta: 16 dígitos y CVV de 3. Sin comisión. */
public class PagoTarjeta implements IMetodoPago {
    private String _numeroTarjeta;
    private String _cvv;

    public PagoTarjeta(String numeroTarjeta, String cvv) {
        this._numeroTarjeta = numeroTarjeta == null ? "" : numeroTarjeta.replace(" ", "");
        this._cvv = cvv == null ? "" : cvv.trim();
    }

    @Override
    public boolean validar() {
        return _numeroTarjeta.matches("\\d{16}") && _cvv.matches("\\d{3}");
    }

    @Override
    public double calcularComision(double monto) {
        return 0;
    }

    @Override
    public String getNombre() {
        return "Tarjeta";
    }

    @Override
    public String getReferencia() {
        String ultimos = _numeroTarjeta.length() >= 4 ? _numeroTarjeta.substring(_numeroTarjeta.length() - 4) : "";
        return "**** " + ultimos;   // nunca se guarda el número completo
    }
}
