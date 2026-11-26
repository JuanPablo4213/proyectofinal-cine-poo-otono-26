package cinemex.modelo.usuarios;

import cinemex.base.Seguridad;

public abstract class POJOUsuario {
    protected int id;
    protected String nombre;
    protected String email;
    protected String passwordHash;
    protected String sal;

    public POJOUsuario(int id, String nombre, String email, String passwordHash, String sal) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.sal = sal;
    }

    public abstract String getRol();

    public boolean validarPassword(String password) {
        return password != null && Seguridad.hash(sal, password).equals(passwordHash);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getSal() { return sal; }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + getRol() + ") " + email;
    }
}
