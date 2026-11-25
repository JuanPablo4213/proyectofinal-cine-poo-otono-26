package cinemex.controlador;

import cinemex.modelo.compras.TipoBoleto;
import cinemex.modelo.funciones.POJOFuncion;
import cinemex.modelo.peliculas.POJOPelicula;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Detalle de una película (/3/movie/{id}). Si el administrador la importa, se guarda
 * en MySQL y ya se le pueden programar funciones; el cliente ve aquí las funciones y compra.
 */
public class DetalleControlador extends ControladorBase {
    @FXML private StackPane contPoster;
    @FXML private Label lblTitulo;
    @FXML private Label lblDatos;
    @FXML private Label lblClasificacion;
    @FXML private Label lblGeneros;
    @FXML private Label lblSinopsis;
    @FXML private Label lblEstado;
    @FXML private Button btnImportar;
    @FXML private VBox panelFunciones;
    @FXML private TableView<POJOFuncion> tablaFunciones;
    @FXML private ProgressIndicator progreso;

    private int _tmdbId;
    private POJOPelicula _peliculaTMDB;   // lo que regresó TMDB
    private POJOPelicula _peliculaLocal;  // la de MySQL, si ya está importada

    public void setTmdbId(int tmdbId) {
        this._tmdbId = tmdbId;
    }

    @FXML
    private void initialize() {
        Tablas.columna(tablaFunciones, "Día", 170, f -> Formatos.fecha(f.getFechaHora()));
        Tablas.columna(tablaFunciones, "Hora", 70, f -> Formatos.hora(f.getFechaHora()));
        Tablas.columna(tablaFunciones, "Cine", 190, f -> f.getCine());
        Tablas.columna(tablaFunciones, "Sala", 150, f -> f.getSala());
        Tablas.columna(tablaFunciones, "Idioma", 110, f -> f.getIdioma());
        Tablas.columna(tablaFunciones, "Adulto desde", 110,
                f -> Formatos.dinero(f.getSala().calcularPrecio(TipoBoleto.ADULTO)));
        tablaFunciones.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                comprar();
            }
        });
    }

    @Override
    protected void alIniciar() {
        try {
            _peliculaLocal = ctx.peliculas().buscarPorTmdbId(_tmdbId);
        } catch (RuntimeException e) {
            lblEstado.setText("Sin conexión a MySQL: " + e.getMessage());
        }
        if (_peliculaLocal != null) {
            mostrar(_peliculaLocal);   // se ve algo de inmediato mientras responde TMDB
        }
        progreso.setVisible(true);
        enSegundoPlano(() -> ctx.tmdb().detalle(_tmdbId), p -> {
            progreso.setVisible(false);
            _peliculaTMDB = p;
            if (_peliculaLocal != null) {
                p.setEnCartelera(_peliculaLocal.isEnCartelera());
            }
            mostrar(p);
            actualizarEstado();
        }, error -> {
            progreso.setVisible(false);
            if (_peliculaLocal == null) {
                lblTitulo.setText("No se pudo cargar la película");
            }
            lblEstado.setText(error.getMessage());
            lblEstado.getStyleClass().setAll("label", "mensaje-error");
            actualizarEstado();
        });
        actualizarEstado();
    }

    private void mostrar(POJOPelicula p) {
        contPoster.getChildren().setAll(Tarjetas.poster(p, 300, 450));
        lblTitulo.setText(p.getTitulo());
        String datos = p.getAnio();
        if (p.getDuracionMin() > 0) {
            datos += "  ·  " + Formatos.duracion(p.getDuracionMin());
        }
        if (p.getCalificacion() > 0) {
            datos += String.format("  ·  ★ %.1f / 10", p.getCalificacion());
        }
        lblDatos.setText(datos);
        lblClasificacion.setText(p.getClasificacion().name());
        lblClasificacion.setTooltip(new javafx.scene.control.Tooltip(p.getClasificacion().getDescripcion()));
        lblGeneros.setText(p.getGenerosTexto());
        lblSinopsis.setText(p.getSinopsis() == null || p.getSinopsis().isBlank() ? "Sin sinopsis en español." : p.getSinopsis());
    }

    /** Botón de importar (solo admin) y funciones de mi cine. */
    private void actualizarEstado() {
        boolean admin = ctx.usuarios().esAdministrador();
        boolean importada = _peliculaLocal != null;
        btnImportar.setVisible(admin && _peliculaTMDB != null);
        btnImportar.setManaged(admin && _peliculaTMDB != null);
        btnImportar.setText(importada ? "Actualizar datos desde TMDB" : "Importar a mi cine");
        panelFunciones.setVisible(importada);
        panelFunciones.setManaged(importada);
        if (!lblEstado.getStyleClass().contains("mensaje-error")) {
            lblEstado.setText(importada
                    ? (_peliculaLocal.isEnCartelera() ? "En la cartelera de tu cine" : "Importada, pero fuera de cartelera")
                    : "Esta película no se proyecta en nuestros cines" + (admin ? " (impórtala para programar funciones)." : "."));
        }
        if (importada) {
            try {
                tablaFunciones.getItems().setAll(ctx.funciones().listarPorPelicula(_peliculaLocal));
            } catch (RuntimeException e) {
                mostrarError(e);
            }
        }
    }

    @FXML
    private void importar() {
        if (intentar(() -> ctx.peliculas().guardar(_peliculaTMDB))) {
            _peliculaLocal = ctx.peliculas().buscarPorTmdbId(_tmdbId);
            mostrarInfo("Listo", "\"" + _peliculaTMDB.getTitulo() + "\" está en la base de tu cine. "
                    + "Ya le puedes programar funciones en Administración → Funciones.");
            actualizarEstado();
        }
    }

    @FXML
    private void comprar() {
        POJOFuncion f = tablaFunciones.getSelectionModel().getSelectedItem();
        if (f == null) {
            mostrarError("Selecciona una función de la tabla.");
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

    @FXML
    private void regresar() {
        ctx.navegador().regresar();
    }
}
