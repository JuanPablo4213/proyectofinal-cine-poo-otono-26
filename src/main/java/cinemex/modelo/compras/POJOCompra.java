package cinemex.modelo.compras;

import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.funciones.POJOFuncion;
import cinemex.modelo.usuarios.POJOCliente;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class POJOCompra {
    public static final int MAX_BOLETOS = 10;

    private int _id;
    private POJOCliente _cliente;
    private POJOFuncion _funcion;
    private List<POJOBoleto> _boletos;
    private IMetodoPago _metodo;
    private EstadoCompra _estado;
    private LocalDateTime _fecha;
    private double _comision;

    public POJOCompra(int id, POJOCliente cliente, POJOFuncion funcion) {
        this._id = id;
        this._cliente = cliente;
        this._funcion = funcion;
        this._boletos = new ArrayList<>();
        this._estado = EstadoCompra.PENDIENTE;
        this._fecha = LocalDateTime.now();
    }

    public boolean agregarBoleto(POJOBoleto b) {
        if (_boletos.size() >= MAX_BOLETOS || b.getFuncion() != _funcion || tieneAsiento(b.getAsiento())) {
            return false;
        }
        _boletos.add(b);
        return true;
    }

    public boolean tieneAsiento(POJOAsiento a) {
        for (POJOBoleto b : _boletos) {
            if (b.getAsiento().equals(a)) {
                return true;
            }
        }
        return false;
    }

    public double calcularTotal() {
        double total = 0;
        for (POJOBoleto b : _boletos) {
            total += b.getPrecio();
        }
        return total;
    }

    public String getAsientosTexto() {
        return _boletos.stream().map(b -> b.getAsiento().getCodigo()).collect(Collectors.joining(", "));
    }

    public int getId() { return _id; }
    public void setId(int id) { this._id = id; }
    public POJOCliente getCliente() { return _cliente; }
    public POJOFuncion getFuncion() { return _funcion; }
    public List<POJOBoleto> getBoletos() { return _boletos; }
    public IMetodoPago getMetodo() { return _metodo; }
    public void setMetodo(IMetodoPago m) { this._metodo = m; }
    public EstadoCompra getEstado() { return _estado; }
    public void setEstado(EstadoCompra e) { this._estado = e; }
    public LocalDateTime getFecha() { return _fecha; }
    public void setFecha(LocalDateTime fecha) { this._fecha = fecha; }
    public double getComision() { return _comision; }
    public void setComision(double comision) { this._comision = comision; }
}
