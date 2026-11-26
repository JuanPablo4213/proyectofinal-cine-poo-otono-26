package cinemex.controlador;

import cinemex.modelo.peliculas.Clasificacion;
import cinemex.modelo.peliculas.POJOPelicula;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class AdminPeliculasControlador extends ControladorBase {
    @FXML private TableView<POJOPelicula> tabla;
    @FXML private ComboBox<Clasificacion> cmbClasificacion;
    @FXML private CheckBox chkCartelera;
    @FXML private Label lblSeleccion;

    @FXML
    private void initialize() {
        Tablas.columna(tabla, "Id", 50, p -> p.getId());
        Tablas.columna(tabla, "TMDB", 80, p -> p.getTmdbId());
        Tablas.columna(tabla, "Título", 260, p -> p.getTitulo());
        Tablas.columna(tabla, "Duración", 100, p -> Formatos.duracion(p.getDuracionMin()));
        Tablas.columna(tabla, "Clasif.", 70, p -> p.getClasificacion().name());
        Tablas.columna(tabla, "Géneros", 230, p -> p.getGenerosTexto());
        Tablas.columna(tabla, "En cartelera", 100, p -> p.isEnCartelera() ? "Sí" : "No");
        cmbClasificacion.getItems().setAll(Clasificacion.values());
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> seleccionar(p));
    }

    @Override
    protected void alIniciar() {
        cargar();
    }

    private void cargar() {
        try {
            tabla.getItems().setAll(ctx.peliculas().listar());
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    private void seleccionar(POJOPelicula p) {
        lblSeleccion.setText(p == null ? "Selecciona una película de la tabla" : p.getTitulo());
        if (p != null) {
            cmbClasificacion.setValue(p.getClasificacion());
            chkCartelera.setSelected(p.isEnCartelera());
        }
    }

    @FXML
    private void guardar() {
        POJOPelicula p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) {
            mostrarError("Selecciona una película.");
            return;
        }
        p.setClasificacion(cmbClasificacion.getValue());
        p.setEnCartelera(chkCartelera.isSelected());
        if (intentar(() -> ctx.peliculas().actualizar(p))) {
            cargar();
        }
    }

    @FXML
    private void eliminar() {
        POJOPelicula p = tabla.getSelectionModel().getSelectedItem();
        if (p != null && confirmar("¿Eliminar \"" + p.getTitulo() + "\" de la base de tu cine?")
                && intentar(() -> ctx.peliculas().eliminar(p.getId()))) {
            cargar();
        }
    }

    @FXML
    private void verDetalle() {
        POJOPelicula p = tabla.getSelectionModel().getSelectedItem();
        if (p != null) {
            ctx.navegador().ir("detalle", (DetalleControlador d) -> d.setTmdbId(p.getTmdbId()));
        }
    }

    @FXML
    private void importarNueva() {
        mostrarInfo("Importar películas", "Busca la película en la Cartelera, ábrela y presiona \"Importar a mi cine\".");
        ctx.navegador().ir("cartelera");
    }
}
