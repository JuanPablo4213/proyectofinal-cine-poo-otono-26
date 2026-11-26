package cinemex.modelo.cines;

import cinemex.modelo.compras.TipoBoleto;

public class POJOSalaX4D extends POJOSala {
    private static final double PRECIO_BASE = 120;
    private double _cargoEfectos;

    public POJOSalaX4D(int id, int cineId, int numero, int filas, int columnas, double cargoEfectos) {
        super(id, cineId, numero, filas, columnas);
        this._cargoEfectos = cargoEfectos;
    }

    @Override
    public double calcularPrecio(TipoBoleto tipo) {
        return PRECIO_BASE * tipo.getFactor() + _cargoEfectos;
    }

    @Override
    public String getTipoSala() {
        return "X4D";
    }

    public double getCargoEfectos() { return _cargoEfectos; }
}
