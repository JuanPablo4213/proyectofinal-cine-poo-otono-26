package cinemex.modelo.cines;

public class POJOAsiento {
    private int _id;
    private char _fila;
    private int _numero;

    public POJOAsiento(int id, char fila, int numero) {
        this._id = id;
        this._fila = fila;
        this._numero = numero;
    }

    public String getCodigo() {
        return "" + _fila + _numero;
    }

    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public char getFila() { return _fila; }
    public int getNumero() { return _numero; }

    @Override
    public boolean equals(Object o) {
        return o instanceof POJOAsiento a && a._fila == _fila && a._numero == _numero;
    }

    @Override
    public int hashCode() {
        return _fila * 100 + _numero;
    }

    @Override
    public String toString() {
        return getCodigo();
    }
}
