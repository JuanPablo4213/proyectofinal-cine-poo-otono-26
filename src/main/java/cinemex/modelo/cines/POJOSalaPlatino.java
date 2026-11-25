package cinemex.modelo.cines;

import cinemex.modelo.compras.TipoBoleto;

public class POJOSalaPlatino extends POJOSala {
    private static final double PRECIO_BASE = 160;

    public POJOSalaPlatino(int id, int cineId, int numero, int filas, int columnas) {
        super(id, cineId, numero, filas, columnas);
    }

    @Override
    public double calcularPrecio(TipoBoleto tipo) {
        return PRECIO_BASE * tipo.getFactor();
    }

    @Override
    public String getTipoSala() {
        return "Platino";
    }
}
