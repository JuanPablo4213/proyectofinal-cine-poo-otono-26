package cinemex.modelo.usuarios;

import cinemex.base.Seguridad;

/**
 * Clase abstracta: nadie es "un usuario" a secas, es Cliente o Administrador.
 * Los atributos son protected para que las subclases los usen directo.
 */
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

    /** Cada subclase dice qué rol tiene (polimorfismo). */
    public abstract String getRol();

    /** Compara la contraseña escrita contra el hash guardado. */
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
