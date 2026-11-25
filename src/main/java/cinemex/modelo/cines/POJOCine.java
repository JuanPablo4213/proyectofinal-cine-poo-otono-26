package cinemex.modelo.cines;

import java.util.ArrayList;
import java.util.List;

/** Un complejo de cine. Se compone de salas. */
public class POJOCine {
    private int _id;
    private String _nombre;
    private String _ciudad;
    private String _direccion;
    private List<POJOSala> _salas;

    public POJOCine(int id, String nombre, String ciudad, String direccion) {
        this._id = id;
        this._nombre = nombre;
        this._ciudad = ciudad;
        this._direccion = direccion;
        this._salas = new ArrayList<>();
    }

    /** Agrega la sala si no hay otra con el mismo número. */
    public boolean agregarSala(POJOSala s) {
        for (POJOSala existente : _salas) {
            if (existente.getNumero() == s.getNumero()) {
                return false;
            }
        }
        _salas.add(s);
        return true;
    }

    /** true si la sala pertenece a este cine. */
    public boolean tieneSala(POJOSala s) {
        return s != null && s.getCineId() == _id;
    }

    public List<POJOSala> getSalas() { return _salas; }
    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public String getNombre() { return _nombre; }
    public void setNombre(String nombre) { this._nombre = nombre; }
    public String getCiudad() { return _ciudad; }
    public void setCiudad(String ciudad) { this._ciudad = ciudad; }
    public String getDireccion() { return _direccion; }
    public void setDireccion(String direccion) { this._direccion = direccion; }

    @Override
    public String toString() {
        return _nombre;
    }
}
