package cinemex.controlador;

import cinemex.modelo.peliculas.FiltroGenero;
import cinemex.modelo.peliculas.IFiltroPelicula;
import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import cinemex.catalogo.PaginaPeliculas;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

/**
 * Catálogo (API del grupo o TMDB): Populares, En cartelera, Próximamente y Búsqueda.
 * Cada sección se pide en segundo plano y se puede pasar de página.
 */
public class CarteleraControlador extends ControladorBase {
    private enum Seccion { POPULARES, EN_CARTELERA, PROXIMAMENTE, BUSQUEDA }

    private static final POJOGenero TODOS = new POJOGenero(0, "Todos los géneros");

    @FXML private ToggleButton tglPopulares;
    @FXML private ToggleButton tglCartelera;
    @FXML private ToggleButton tglProximamente;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<POJOGenero> cmbGenero;
    @FXML private FlowPane panelPeliculas;
    @FXML private Label lblMensaje;
    @FXML private Label lblPagina;
    @FXML private Label lblTitulo;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private ProgressIndicator progreso;

    private final ToggleGroup _grupo = new ToggleGroup();
    private Seccion _seccion = Seccion.POPULARES;
    private PaginaPeliculas _paginaActual;
    private String _textoBusqueda = "";

    @FXML
    private void initialize() {
        tglPopulares.setToggleGroup(_grupo);
        tglCartelera.setToggleGroup(_grupo);
        tglProximamente.setToggleGroup(_grupo);
        tglPopulares.setSelected(true);
        cmbGenero.getItems().add(TODOS);
        cmbGenero.setValue(TODOS);
        cmbGenero.setOnAction(e -> mostrarPeliculas());
    }

    @Override
    protected void alIniciar() {
        cargarGeneros();
        cargar(1);
    }

    // setSelected(true): si el usuario vuelve a dar clic en la sección activa, que no se "apague"
    @FXML private void verPopulares() { tglPopulares.setSelected(true); cambiarSeccion(Seccion.POPULARES); }
    @FXML private void verCartelera() { tglCartelera.setSelected(true); cambiarSeccion(Seccion.EN_CARTELERA); }
    @FXML private void verProximamente() { tglProximamente.setSelected(true); cambiarSeccion(Seccion.PROXIMAMENTE); }

    @FXML
    private void buscar() {
        _textoBusqueda = txtBuscar.getText().trim();
        if (_textoBusqueda.isEmpty()) {
            lblMensaje.setText("Escribe el título de una película para buscar.");
            return;
        }
        _grupo.selectToggle(null);
        _seccion = Seccion.BUSQUEDA;
        cargar(1);
    }

    @FXML private void paginaAnterior() { cargar(_paginaActual.getPagina() - 1); }
    @FXML private void paginaSiguiente() { cargar(_paginaActual.getPagina() + 1); }

    private void cambiarSeccion(Seccion s) {
        _seccion = s;
        txtBuscar.clear();
        cargar(1);
    }

    /** Pide la página al catálogo en segundo plano para que la ventana no se congele. */
    private void cargar(int pagina) {
        progreso.setVisible(true);
        lblMensaje.setText("Cargando películas...");
        btnAnterior.setDisable(true);
        btnSiguiente.setDisable(true);
        lblTitulo.setText(tituloSeccion());
        Seccion seccion = _seccion;
        String texto = _textoBusqueda;
        enSegundoPlano(() -> {
            switch (seccion) {
                case EN_CARTELERA: return ctx.catalogo().enCartelera(pagina);
                case PROXIMAMENTE: return ctx.catalogo().proximosEstrenos(pagina);
                case BUSQUEDA: return ctx.catalogo().buscar(texto, pagina);
                default: return ctx.catalogo().populares(pagina);
            }
        }, resultado -> {
            progreso.setVisible(false);
            _paginaActual = resultado;
            mostrarPeliculas();
        }, error -> {
            progreso.setVisible(false);
            panelPeliculas.getChildren().clear();
            lblPagina.setText("");
            lblMensaje.setText(error.getMessage());
            lblMensaje.getStyleClass().setAll("label", "mensaje-error");
        });
    }

    /** Dibuja las tarjetas de la página actual aplicando el filtro de género (IFiltroPelicula). */
    private void mostrarPeliculas() {
        if (_paginaActual == null) {
            return;
        }
        List<IFiltroPelicula> filtros = new ArrayList<>();
        POJOGenero genero = cmbGenero.getValue();
        if (genero != null && genero.getId() != 0) {
            filtros.add(new FiltroGenero(genero));
        }
        panelPeliculas.getChildren().clear();
        for (POJOPelicula p : _paginaActual.getPeliculas()) {
            if (filtros.stream().allMatch(f -> f.cumple(p))) {
                panelPeliculas.getChildren().add(Tarjetas.tarjeta(p, () -> abrirDetalle(p)));
            }
        }
        lblMensaje.getStyleClass().setAll("label", "mensaje");
        if (_paginaActual.getPeliculas().isEmpty()) {
            lblMensaje.setText(_seccion == Seccion.BUSQUEDA
                    ? "No se encontraron películas para \"" + _textoBusqueda + "\"."
                    : "La API no regresó películas en esta sección.");
        } else if (panelPeliculas.getChildren().isEmpty()) {
            lblMensaje.setText("Ninguna película de esta página es de " + genero + ". Prueba la siguiente página.");
        } else {
            lblMensaje.setText(_paginaActual.getTotalResultados() + " películas");
        }
        lblPagina.setText("Página " + _paginaActual.getPagina() + " de " + _paginaActual.getTotalPaginas());
        btnAnterior.setDisable(!_paginaActual.tieneAnterior());
        btnSiguiente.setDisable(!_paginaActual.tieneSiguiente());
    }

    /** Catálogo de géneros: de la API al ComboBox y a la tabla "generos" de MySQL. */
    private void cargarGeneros() {
        enSegundoPlano(() -> ctx.catalogo().generos(), generos -> {
            cmbGenero.getItems().setAll(TODOS);
            cmbGenero.getItems().addAll(generos);
            cmbGenero.setValue(TODOS);
            try {
                ctx.peliculas().sincronizarGeneros(generos);
            } catch (RuntimeException e) {
                lblMensaje.setText("Aviso: no se pudieron guardar los géneros en MySQL. " + e.getMessage());
            }
        }, error -> {
            // sin API usamos los géneros guardados en la base
            try {
                cmbGenero.getItems().addAll(ctx.peliculas().listarGeneros());
            } catch (RuntimeException ignorada) {
                // la falta de base de datos ya se avisa en otras pantallas
            }
        });
    }

    private void abrirDetalle(POJOPelicula p) {
        ctx.navegador().ir("detalle", (DetalleControlador d) -> d.setTmdbId(p.getTmdbId()));
    }

    private String tituloSeccion() {
        switch (_seccion) {
            case EN_CARTELERA: return "En cartelera en México";
            case PROXIMAMENTE: return "Próximamente";
            case BUSQUEDA: return "Resultados para \"" + _textoBusqueda + "\"";
            default: return "Populares";
        }
    }
}
