package cinemex.controlador;

import cinemex.modelo.compras.POJOCompra;
import cinemex.modelo.usuarios.POJOCliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/** Historial del cliente y cancelación (RN08). */
public class MisComprasControlador extends ControladorBase {
    @FXML private TableView<POJOCompra> tabla;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        Tablas.columna(tabla, "Folio", 60, c -> c.getId());
        Tablas.columna(tabla, "Película", 220, c -> c.getFuncion().getPelicula().getTitulo());
        Tablas.columna(tabla, "Cine", 170, c -> c.getFuncion().getCine());
        Tablas.columna(tabla, "Sala", 140, c -> c.getFuncion().getSala());
        Tablas.columna(tabla, "Función", 160, c -> Formatos.fechaHora(c.getFuncion().getFechaHora()));
        Tablas.columna(tabla, "Asientos", 120, c -> c.getAsientosTexto());
        Tablas.columna(tabla, "Total", 90, c -> Formatos.dinero(c.calcularTotal()));
        Tablas.columna(tabla, "Estado", 100, c -> c.getEstado());
        Tablas.columna(tabla, "Comprada", 150, c -> Formatos.fechaHora(c.getFecha()));
    }

    @Override
    protected void alIniciar() {
        cargar();
    }

    private void cargar() {
        POJOCliente cliente = ctx.usuarios().getClienteActual();
        if (cliente == null) {
            lblMensaje.setText("Inicia sesión como cliente para ver tus compras.");
            return;
        }
        try {
            tabla.getItems().setAll(ctx.compras().listarPorCliente(cliente));
            lblMensaje.setText(tabla.getItems().isEmpty() ? "Todavía no tienes compras." : "");
        } catch (RuntimeException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        POJOCompra c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) {
            mostrarError("Selecciona una compra.");
            return;
        }
        if (confirmar("¿Cancelar la compra " + c.getId() + " (" + c.getAsientosTexto() + ")? Los asientos se liberan.")
                && intentar(() -> ctx.compras().cancelar(c.getId()))) {
            mostrarInfo("Compra cancelada", "Se liberaron los asientos " + c.getAsientosTexto() + ".");
            cargar();
        }
    }
}
