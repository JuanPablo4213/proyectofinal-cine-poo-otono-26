package cinemex.modelo.usuarios;

public class POJOCliente extends POJOUsuario {
    private String _telefono;

    public POJOCliente(int id, String nombre, String email, String passwordHash, String sal, String telefono) {
        super(id, nombre, email, passwordHash, sal);
        this._telefono = telefono;
    }

    @Override
    public String getRol() {
        return "Cliente";
    }

    public String getTelefono() { return _telefono; }
}
