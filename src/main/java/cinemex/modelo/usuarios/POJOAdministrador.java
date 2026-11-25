package cinemex.modelo.usuarios;

/** Administrador: da de alta películas, cines, salas y funciones (RN10). */
public class POJOAdministrador extends POJOUsuario {
    private String _numEmpleado;

    public POJOAdministrador(int id, String nombre, String email, String passwordHash, String sal, String numEmpleado) {
        super(id, nombre, email, passwordHash, sal);
        this._numEmpleado = numEmpleado;
    }

    @Override
    public String getRol() {
        return "Administrador";
    }

    public String getNumEmpleado() { return _numEmpleado; }
}
