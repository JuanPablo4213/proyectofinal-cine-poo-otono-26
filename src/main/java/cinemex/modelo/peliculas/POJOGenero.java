package cinemex.modelo.peliculas;

public class POJOGenero {
    private int _id;
    private String _nombre;

    public POJOGenero(int id, String nombre) {
        this._id = id;
        this._nombre = nombre;
    }

    public int getId() { return _id; }
    public String getNombre() { return _nombre; }

    @Override
    public boolean equals(Object o) {
        return o instanceof POJOGenero g && g._id == _id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(_id);
    }

    @Override
    public String toString() {
        return _nombre;
    }
}
