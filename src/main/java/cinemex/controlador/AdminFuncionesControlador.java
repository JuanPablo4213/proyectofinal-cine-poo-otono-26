package cinemex.controlador;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.funciones.Idioma;
import cinemex.modelo.funciones.POJOFuncion;
import cinemex.modelo.peliculas.POJOPelicula;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminFuncionesControlador extends ControladorBase {
    @FXML private TableView<POJOFuncion> tabla;
    @FXML private ComboBox<POJOPelicula> cmbPelicula;
    @FXML private ComboBox<POJOCine> cmbCine;
    @FXML private ComboBox<POJOSala> cmbSala;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<Idioma> cmbIdioma;

    @FXML
    private void initialize() {
        Tablas.columna(tabla, "Id", 45, f -> f.getId());
        Tablas.columna(tabla, "Película", 220, f -> f.getPelicula().getTitulo());
        Tablas.columna(tabla, "Cine", 160, f -> f.getCine());
        Tablas.columna(tabla, "Sala", 140, f -> f.getSala());
        Tablas.columna(tabla, "Fecha", 170, f -> Formatos.fecha(f.getFechaHora()));
        Tablas.columna(tabla, "Inicio", 60, f -> Formatos.hora(f.getFechaHora()));
        Tablas.columna(tabla, "Termina", 70, f -> Formatos.hora(f.calcularHoraFin()));
        Tablas.columna(tabla, "Idioma", 100, f -> f.getIdioma());
        Tablas.columna(tabla, "Vendidos", 75, f -> ctx.funciones().contarBoletos(f.getId()));
        cmbIdioma.getItems().setAll(Idioma.values());
        cmbIdioma.setValue(Idioma.ESPANOL);
        dpFecha.setValue(LocalDate.now().plusDays(1));
        cmbCine.setOnAction(e -> {
            POJOCine c = cmbCine.getValue();
            cmbSala.getItems().setAll(c == null ? java.util.List.of() : c.getSalas());
            if (!cmbSala.getItems().isEmpty()) {
                cmbSala.setValue(cmbSala.getItems().get(0));
            }
        });
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, f) -> seleccionar(f));
    }

    @Override
    protected void alIniciar() {
        try {
            cmbPelicula.getItems().setAll(ctx.peliculas().listarCartelera());
            cmbCine.getItems().setAll(ctx.cines().listar());
            cargar();
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void cargar() {
        tabla.getItems().setAll(ctx.funciones().listar());
    }

    private void seleccionar(POJOFuncion f) {
        if (f == null) {
            return;
        }
        cmbPelicula.getItems().stream().filter(p -> p.getId() == f.getPelicula().getId()).findFirst()
                .ifPresent(cmbPelicula::setValue);
        cmbCine.getItems().stream().filter(c -> c.getId() == f.getCine().getId()).findFirst().ifPresent(c -> {
            cmbCine.setValue(c);
            cmbCine.getOnAction().handle(null);
            cmbSala.getItems().stream().filter(s -> s.getId() == f.getSala().getId()).findFirst()
                    .ifPresent(cmbSala::setValue);
        });
        dpFecha.setValue(f.getFechaHora().toLocalDate());
        txtHora.setText(Formatos.hora(f.getFechaHora()));
        cmbIdioma.setValue(f.getIdioma());
    }

    @FXML
    private void nueva() {
        tabla.getSelectionModel().clearSelection();
        guardarComo(0);
    }

    @FXML
    private void actualizar() {
        POJOFuncion f = tabla.getSelectionModel().getSelectedItem();
        if (f == null) {
            mostrarError("Selecciona la función a modificar.");
            return;
        }
        guardarComo(f.getId());
    }

    private void guardarComo(int id) {
        try {
            if (dpFecha.getValue() == null) {
                throw new ExcepcionCine("Elige la fecha.");
            }
            LocalDateTime inicio = LocalDateTime.of(dpFecha.getValue(), leerHora());
            POJOFuncion f = new POJOFuncion(id, cmbPelicula.getValue(), cmbCine.getValue(), cmbSala.getValue(),
                    inicio, cmbIdioma.getValue());
            ctx.funciones().guardar(f);
            cargar();
            mostrarInfo("Función guardada", f.getPelicula().getTitulo() + " · " + f.getCine() + " " + f.getSala()
                    + "\n" + Formatos.fecha(inicio) + " de " + Formatos.hora(inicio) + " a " + Formatos.hora(f.calcularHoraFin()));
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    @FXML
    private void eliminar() {
        POJOFuncion f = tabla.getSelectionModel().getSelectedItem();
        if (f != null && confirmar("¿Eliminar la función " + f.getId() + "?")
                && intentar(() -> ctx.funciones().eliminar(f.getId()))) {
            cargar();
        }
    }

    private LocalTime leerHora() {
        try {
            return LocalTime.parse(txtHora.getText().trim().length() == 4 ? "0" + txtHora.getText().trim() : txtHora.getText().trim());
        } catch (DateTimeParseException e) {
            throw new ExcepcionCine("Escribe la hora como HH:mm, por ejemplo 19:30.");
        }
    }
}
