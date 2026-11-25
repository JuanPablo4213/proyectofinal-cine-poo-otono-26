package cinemex.servicio;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.compras.EstadoCompra;
import cinemex.modelo.compras.IMetodoPago;
import cinemex.modelo.compras.POJOBoleto;
import cinemex.modelo.compras.POJOCompra;
import cinemex.modelo.compras.TipoBoleto;
import cinemex.modelo.funciones.POJOFuncion;
import cinemex.modelo.usuarios.POJOCliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** El cerebro del flujo de compra: iniciar, agregar boletos, pagar y cancelar. */
public class ServicioCompras extends BaseServicio {
    public static final int MAX_BOLETOS = POJOCompra.MAX_BOLETOS;

    private final ServicioFunciones _funciones;

    public ServicioCompras(ServicioFunciones funciones) {
        this._funciones = funciones;
    }

    /** Crea la compra en estado PENDIENTE (todavía no aparta asientos, RN06). */
    public POJOCompra iniciarCompra(POJOCliente c, POJOFuncion f) {
        if (c == null) {
            throw new ExcepcionCine("Inicia sesión como cliente para comprar boletos.");
        }
        if (f == null) {
            throw new ExcepcionCine("La función no existe.");
        }
        if (f.yaEmpezo()) {
            throw new ExcepcionCine("Esa función ya empezó.");
        }
        return new POJOCompra(0, c, f);
    }

    /** Revisa que el asiento esté libre, le pide el precio a la sala (polimorfismo) y lo agrega. */
    public boolean agregarBoleto(POJOCompra compra, POJOAsiento a, TipoBoleto tipo) {
        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new ExcepcionCine("La compra ya no se puede modificar.");
        }
        POJOFuncion f = compra.getFuncion();
        if (!f.estaDisponible(a)) {
            throw new ExcepcionCine("El asiento " + a.getCodigo() + " ya está ocupado.");
        }
        if (compra.getBoletos().size() >= MAX_BOLETOS) {
            throw new ExcepcionCine("Máximo " + MAX_BOLETOS + " boletos por compra.");
        }
        double precio = Math.round(f.getSala().calcularPrecio(tipo) * 100) / 100.0;
        return compra.agregarBoleto(new POJOBoleto(0, f, a, tipo, precio));
    }

    /**
     * Paga la compra. Solo si el pago es válido se ocupan los asientos (RN06), y todo
     * se guarda en una transacción: o se guardan todos los boletos o ninguno (RNF04).
     */
    public boolean confirmarCompra(POJOCompra compra, IMetodoPago m) {
        if (compra.getEstado() != EstadoCompra.PENDIENTE || compra.getBoletos().isEmpty()) {
            throw new ExcepcionCine("Selecciona al menos un asiento.");
        }
        if (m == null || !m.validar()) {
            throw new ExcepcionCine("Los datos de pago no son válidos.");
        }
        POJOFuncion f = compra.getFuncion();
        if (f.yaEmpezo()) {
            throw new ExcepcionCine("La función ya empezó.");
        }
        // Se vuelve a leer qué está ocupado: otro cliente pudo pagar mientras elegíamos (RN01)
        _funciones.cargarOcupados(f);
        for (POJOBoleto b : compra.getBoletos()) {
            if (!f.estaDisponible(b.getAsiento())) {
                throw new ExcepcionCine("El asiento " + b.getAsiento().getCodigo()
                        + " lo acaba de comprar otra persona. Elige otro.");
            }
        }
        double total = compra.calcularTotal();
        double comision = m.calcularComision(total);

        Connection c = con();
        try {
            c.setAutoCommit(false);
            String sqlCompra = "INSERT INTO compras (usuario_id, funcion_id, metodo_pago, referencia, estado, fecha, total, comision) "
                    + "VALUES (?, ?, ?, ?, 'PAGADA', ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlCompra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, compra.getCliente().getId());
                ps.setInt(2, f.getId());
                ps.setString(3, m.getNombre());
                ps.setString(4, m.getReferencia());
                ps.setTimestamp(5, Timestamp.valueOf(compra.getFecha()));
                ps.setDouble(6, total);
                ps.setDouble(7, comision);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    compra.setId(rs.getInt(1));
                }
            }
            String sqlBoleto = "INSERT INTO boletos (compra_id, funcion_id, asiento_id, tipo, precio) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sqlBoleto, Statement.RETURN_GENERATED_KEYS)) {
                for (POJOBoleto b : compra.getBoletos()) {
                    ps.setInt(1, compra.getId());
                    ps.setInt(2, f.getId());
                    ps.setInt(3, b.getAsiento().getId());
                    ps.setString(4, b.getTipo().name());
                    ps.setDouble(5, b.getPrecio());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        b.setId(rs.getInt(1));
                    }
                }
            }
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            compra.setId(0);
            if (esErrorIntegridad(e)) {
                // la llave única (funcion, asiento, vigente) evitó vender un asiento dos veces
                throw new ExcepcionCine("Uno de tus asientos lo acaba de comprar otra persona. Elige otro.");
            }
            throw errorBD(e);
        } finally {
            autoCommit(c);
        }
        for (POJOBoleto b : compra.getBoletos()) {
            f.ocupar(b.getAsiento());
        }
        compra.setMetodo(m);
        compra.setComision(comision);
        compra.setEstado(EstadoCompra.PAGADA);
        return true;
    }

    /** Solo compras pagadas y antes de que empiece la función (RN08). Libera los asientos. */
    public boolean cancelar(int id) {
        POJOCompra compra = buscarPorId(id);
        if (compra == null) {
            throw new ExcepcionCine("La compra no existe.");
        }
        if (compra.getEstado() != EstadoCompra.PAGADA) {
            throw new ExcepcionCine("Solo se pueden cancelar compras pagadas.");
        }
        if (compra.getFuncion().yaEmpezo()) {
            throw new ExcepcionCine("La función ya empezó; no se puede cancelar.");
        }
        Connection c = con();
        try {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE boletos SET vigente = NULL WHERE compra_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE compras SET estado = 'CANCELADA' WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            throw errorBD(e);
        } finally {
            autoCommit(c);
        }
        for (POJOBoleto b : compra.getBoletos()) {
            compra.getFuncion().liberar(b.getAsiento());
        }
        compra.setEstado(EstadoCompra.CANCELADA);
        return true;
    }

    public POJOCompra buscarPorId(int id) {
        List<POJOCompra> r = consultar("SELECT * FROM compras WHERE id = ?", null, id);
        return r.isEmpty() ? null : r.get(0);
    }

    /** Historial del cliente ("Mis compras"), las más recientes primero. */
    public List<POJOCompra> listarPorCliente(POJOCliente c) {
        return consultar("SELECT * FROM compras WHERE usuario_id = ? ORDER BY fecha DESC", c, c.getId());
    }

    // ------------------------------------------------------------------ privados

    private List<POJOCompra> consultar(String sql, POJOCliente cliente, Object... params) {
        List<POJOCompra> lista = new ArrayList<>();
        Map<Integer, POJOFuncion> funciones = new HashMap<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int funcionId = rs.getInt("funcion_id");
                    POJOFuncion f = funciones.computeIfAbsent(funcionId, _funciones::buscarPorId);
                    POJOCompra compra = new POJOCompra(rs.getInt("id"), cliente, f);
                    compra.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    compra.setComision(rs.getDouble("comision"));
                    compra.setEstado(EstadoCompra.valueOf(rs.getString("estado")));
                    lista.add(compra);
                }
            }
            for (POJOCompra compra : lista) {
                cargarBoletos(compra);
            }
        } catch (SQLException e) {
            throw errorBD(e);
        }
        return lista;
    }

    private void cargarBoletos(POJOCompra compra) throws SQLException {
        String sql = "SELECT b.id, b.tipo, b.precio, a.id AS asiento_id, a.fila, a.numero FROM boletos b "
                + "JOIN asientos a ON a.id = b.asiento_id WHERE b.compra_id = ? ORDER BY a.fila, a.numero";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, compra.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    POJOAsiento a = new POJOAsiento(rs.getInt("asiento_id"), rs.getString("fila").charAt(0), rs.getInt("numero"));
                    // se agrega directo a la lista: estos boletos ya existen, no pasan por la validación
                    compra.getBoletos().add(new POJOBoleto(rs.getInt("id"), compra.getFuncion(), a,
                            TipoBoleto.valueOf(rs.getString("tipo")), rs.getDouble("precio")));
                }
            }
        }
    }
}
