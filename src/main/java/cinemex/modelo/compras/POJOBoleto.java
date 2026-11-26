package cinemex.modelo.compras;

import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.funciones.POJOFuncion;

public class POJOBoleto {
    private int _id;
    private POJOFuncion _funcion;
    private POJOAsiento _asiento;
    private TipoBoleto _tipo;
    private double _precio;

    public POJOBoleto(int id, POJOFuncion funcion, POJOAsiento asiento, TipoBoleto tipo, double precio) {
        this._id = id;
        this._funcion = funcion;
        this._asiento = asiento;
        this._tipo = tipo;
        this._precio = precio;
    }

    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public POJOFuncion getFuncion() { return _funcion; }
    public POJOAsiento getAsiento() { return _asiento; }
    public TipoBoleto getTipo() { return _tipo; }
    public double getPrecio() { return _precio; }

    @Override
    public String toString() {
        return String.format("%s %s $%.2f", _asiento.getCodigo(), _tipo, _precio);
    }
}
