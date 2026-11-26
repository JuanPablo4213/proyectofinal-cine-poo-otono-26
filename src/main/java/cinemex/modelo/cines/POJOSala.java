package cinemex.modelo.cines;

import cinemex.modelo.compras.TipoBoleto;
import java.util.ArrayList;
import java.util.List;

public abstract class POJOSala {
    protected int id;
    protected int cineId;
    protected int numero;
    protected int filas;
    protected int columnas;
    protected List<POJOAsiento> asientos;

    public POJOSala(int id, int cineId, int numero, int filas, int columnas) {
        this.id = id;
        this.cineId = cineId;
        this.numero = numero;
        this.filas = filas;
        this.columnas = columnas;
        this.asientos = new ArrayList<>();
        for (int f = 0; f < filas; f++) {
            for (int c = 1; c <= columnas; c++) {
                asientos.add(new POJOAsiento(0, (char) ('A' + f), c));
            }
        }
    }

    public abstract double calcularPrecio(TipoBoleto tipo);

    public abstract String getTipoSala();

    public POJOAsiento buscarAsiento(char fila, int numero) {
        for (POJOAsiento a : asientos) {
            if (a.getFila() == fila && a.getNumero() == numero) {
                return a;
            }
        }
        return null;
    }

    public int getCapacidad() {
        return filas * columnas;
    }

    public List<POJOAsiento> getAsientos() { return asientos; }

    public void setAsientos(List<POJOAsiento> asientos) { this.asientos = new ArrayList<>(asientos); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCineId() { return cineId; }
    public int getNumero() { return numero; }
    public int getFilas() { return filas; }
    public int getColumnas() { return columnas; }

    @Override
    public String toString() {
        return "Sala " + numero + " (" + getTipoSala() + ")";
    }
}
