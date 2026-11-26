package cinemex.modelo.compras;

public enum TipoBoleto {
    ADULTO("Adulto", 1.0),
    NINO("Niño", 0.7),
    ADULTO_MAYOR("Adulto mayor", 0.7);

    private final String _texto;
    private final double _factor;

    TipoBoleto(String texto, double factor) {
        this._texto = texto;
        this._factor = factor;
    }

    public double getFactor() { return _factor; }

    @Override
    public String toString() {
        return _texto;
    }
}
