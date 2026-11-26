package cinemex.controlador;

import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.compras.TipoBoleto;
import cinemex.modelo.funciones.POJOFuncion;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class MiCineControlador extends ControladorBase {
    @FXML private ComboBox<String> cmbCiudad;
    @FXML private ComboBox<POJOCine> cmbCine;
    @FXML private DatePicker dpDia;
    @FXML private TableView<POJOFuncion> tabla;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        Tablas.columna(tabla, "Hora", 70, f -> Formatos.hora(f.getFechaHora()));
        Tablas.columna(tabla, "Película", 260, f -> f.getPelicula().getTitulo());
        Tablas.columna(tabla, "Clasif.", 60, f -> f.getPelicula().getClasificacion().name());
        Tablas.columna(tabla, "Duración", 100, f -> Formatos.duracion(f.getPelicula().getDuracionMin()));
        Tablas.columna(tabla, "Sala", 150, f -> f.getSala());
        Tablas.columna(tabla, "Idioma", 110, f -> f.getIdioma());
        Tablas.columna(tabla, "Adulto", 90, f -> Formatos.dinero(f.getSala().calcularPrecio(TipoBoleto.ADULTO)));
        tabla.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                comprar();
            }
        });
        dpDia.setValue(LocalDate.now().plusDays(1));
        cmbCiudad.setOnAction(e -> cargarCines());
        cmbCine.setOnAction(e -> buscar());
        dpDia.setOnAction(e -> buscar());
    }

    @Override
    protected void alIniciar() {
        try {
            cmbCiudad.getItems().setAll(ctx.cines().listarCiudades());
            if (!cmbCiudad.getItems().isEmpty()) {
                cmbCiudad.setValue(cmbCiudad.getItems().get(0));
                cargarCines();
            }
        } catch (RuntimeException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    private void cargarCines() {
        if (cmbCiudad.getValue() == null) {
            return;
        }
        cmbCine.getItems().setAll(ctx.cines().listarPorCiudad(cmbCiudad.getValue()));
        if (!cmbCine.getItems().isEmpty()) {
            cmbCine.setValue(cmbCine.getItems().get(0));
        }
        buscar();
    }

    private void buscar() {
        POJOCine cine = cmbCine.getValue();
        LocalDate dia = dpDia.getValue();
        if (cine == null || dia == null) {
            tabla.getItems().clear();
            return;
        }
        try {
            tabla.getItems().setAll(ctx.funciones().listarPorCineYDia(cine, dia));
            lblMensaje.setText(tabla.getItems().isEmpty()
                    ? "No hay funciones en " + cine + " el " + Formatos.fecha(dia) + ". Prueba otro día o cine."
                    : tabla.getItems().size() + " funciones · " + cine.getDireccion());
        } catch (RuntimeException e) {
            lblMensaje.setText(e.getMessage());
        }
    }

    @FXML
    private void verPelicula() {
        POJOFuncion f = tabla.getSelectionModel().getSelectedItem();
        if (f == null) {
            mostrarError("Selecciona una función.");
            return;
        }
        ctx.navegador().ir("detalle", (DetalleControlador d) -> d.setTmdbId(f.getPelicula().getTmdbId()));
    }

    @FXML
    private void comprar() {
        POJOFuncion f = tabla.getSelectionModel().getSelectedItem();
        if (f == null) {
            mostrarError("Selecciona una función.");
            return;
        }
        ctx.navegador().requiereSesion(() -> {
            if (ctx.usuarios().getClienteActual() == null) {
                mostrarError("Para comprar boletos inicia sesión con una cuenta de cliente.");
                return;
            }
            ctx.navegador().ir("compra", (CompraControlador c) -> c.setFuncionId(f.getId()));
        });
    }
}
