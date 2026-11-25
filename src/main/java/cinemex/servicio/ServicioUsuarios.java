package cinemex.servicio;

import cinemex.base.ExcepcionCine;
import cinemex.base.Seguridad;
import cinemex.modelo.usuarios.POJOAdministrador;
import cinemex.modelo.usuarios.POJOCliente;
import cinemex.modelo.usuarios.POJOUsuario;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Registro, inicio de sesión y la sesión actual (_usuarioActual). */
public class ServicioUsuarios extends BaseServicio {
    private POJOUsuario _usuarioActual;

    /** Guarda un usuario nuevo. Rechaza correos repetidos (RN09). */
    public boolean guardar(POJOUsuario obj) {
        requerido(obj.getNombre(), "Nombre");
        String email = requerido(obj.getEmail(), "Correo").toLowerCase();
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ExcepcionCine("El correo no es válido.");
        }
        if (buscarPorEmail(email) != null) {
            throw new ExcepcionCine("Ya existe una cuenta con el correo " + email + ".");
        }
        String sql = "INSERT INTO usuarios (nombre, email, password_hash, sal, rol, telefono, num_empleado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, obj.getNombre().trim());
            ps.setString(2, email);
            ps.setString(3, obj.getPasswordHash());
            ps.setString(4, obj.getSal());
            boolean esCliente = obj instanceof POJOCliente;
            ps.setString(5, esCliente ? "CLIENTE" : "ADMINISTRADOR");
            ps.setString(6, esCliente ? ((POJOCliente) obj).getTelefono() : null);
            ps.setString(7, esCliente ? null : ((POJOAdministrador) obj).getNumEmpleado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    obj.setId(rs.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /** Crea un cliente con su contraseña ya convertida en hash y lo guarda. */
    public POJOCliente registrarCliente(String nombre, String email, String telefono, String password) {
        if (password == null || password.length() < 6) {
            throw new ExcepcionCine("La contraseña debe tener al menos 6 caracteres.");
        }
        String sal = Seguridad.nuevaSal();
        POJOCliente cliente = new POJOCliente(0, nombre, email == null ? null : email.trim().toLowerCase(),
                Seguridad.hash(sal, password), sal, telefono);
        guardar(cliente);
        return cliente;
    }

    public POJOUsuario buscarPorId(int id) {
        return buscarUno("SELECT * FROM usuarios WHERE id = ?", id);
    }

    public POJOUsuario buscarPorEmail(String email) {
        return buscarUno("SELECT * FROM usuarios WHERE email = ?", email == null ? "" : email.trim().toLowerCase());
    }

    public List<POJOUsuario> listar() {
        List<POJOUsuario> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement("SELECT * FROM usuarios ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return lista;
    }

    /** Si el correo y la contraseña coinciden guarda la sesión y regresa al usuario; si no, null. */
    public POJOUsuario iniciarSesion(String email, String password) {
        POJOUsuario u = buscarPorEmail(email);
        if (u != null && u.validarPassword(password)) {
            _usuarioActual = u;
            return u;
        }
        return null;
    }

    public void cerrarSesion() {
        _usuarioActual = null;
    }

    public POJOUsuario getUsuarioActual() {
        return _usuarioActual;
    }

    /** El usuario conectado si es cliente; null si no hay sesión o es administrador. */
    public POJOCliente getClienteActual() {
        return _usuarioActual instanceof POJOCliente c ? c : null;
    }

    public boolean esAdministrador() {
        return _usuarioActual instanceof POJOAdministrador;
    }

    private POJOUsuario buscarUno(String sql, Object valor) {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
    }

    /** Fila de MySQL -> subclase correcta según la columna "rol". */
    private POJOUsuario mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        String sal = rs.getString("sal");
        if ("ADMINISTRADOR".equals(rs.getString("rol"))) {
            return new POJOAdministrador(id, nombre, email, hash, sal, rs.getString("num_empleado"));
        }
        return new POJOCliente(id, nombre, email, hash, sal, rs.getString("telefono"));
    }
}
