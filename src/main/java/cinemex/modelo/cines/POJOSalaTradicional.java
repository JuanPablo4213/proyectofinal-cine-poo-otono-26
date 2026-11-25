package cinemex.modelo.cines;

import cinemex.modelo.compras.TipoBoleto;

public class POJOSalaTradicional extends POJOSala {
    private static final double PRECIO_BASE = 75;

    public POJOSalaTradicional(int id, int cineId, int numero, int filas, int columnas) {
        super(id, cineId, numero, filas, columnas);
    }

    @Override
    public double calcularPrecio(TipoBoleto tipo) {
        return PRECIO_BASE * tipo.getFactor();
    }

    @Override
    public String getTipoSala() {
        return "Tradicional";
    }
}
