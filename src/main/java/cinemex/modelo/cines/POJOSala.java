package cinemex.modelo.cines;

import cinemex.modelo.compras.TipoBoleto;
import java.util.ArrayList;
import java.util.List;

/**
 * Sala abstracta. Cada tipo (Tradicional, Platino, X4D) calcula su precio distinto:
 * quien pide el precio no necesita saber qué sala es (polimorfismo).
 */
public abstract class POJOSala {
    protected int id;
    protected int cineId;
    protected int numero;
    protected int filas;
    protected int columnas;
    protected List<POJOAsiento> asientos;

    /** Al crear la sala se generan sus asientos (composición): A1, A2 ... hasta la última fila. */
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

    /** Precio de un boleto en esta sala según el tipo de boleto (RN07). */
    public abstract double calcularPrecio(TipoBoleto tipo);

    /** "Tradicional", "Platino" o "X4D". */
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

    /** Los Servicios reemplazan los asientos generados por los de MySQL (con su id real). */
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
