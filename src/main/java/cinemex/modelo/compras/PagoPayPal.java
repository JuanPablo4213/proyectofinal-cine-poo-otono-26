package cinemex.modelo.compras;

/** Pago simulado con PayPal: valida el correo. Comisión del 3.5%. */
public class PagoPayPal implements IMetodoPago {
    private static final double COMISION = 0.035;
    private String _correo;

    public PagoPayPal(String correo) {
        this._correo = correo == null ? "" : correo.trim();
    }

    @Override
    public boolean validar() {
        return _correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    @Override
    public double calcularComision(double monto) {
        return Math.round(monto * COMISION * 100) / 100.0;
    }

    @Override
    public String getNombre() {
        return "PayPal";
    }

    @Override
    public String getReferencia() {
        return _correo;
    }
}
