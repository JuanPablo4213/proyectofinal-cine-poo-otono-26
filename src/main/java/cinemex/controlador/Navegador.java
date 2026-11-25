package cinemex.controlador;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Cambia la pantalla que se ve en el centro de la ventana. Carga el FXML (Vista),
 * le pasa el Contexto a su controlador y guarda un historial para el botón "Regresar".
 */
public class Navegador {
    private static final String RUTA_VISTAS = "/cinemex/vista/";

    private final Stage _ventana;
    private final Contexto _ctx;
    private final Deque<Parent> _historial = new ArrayDeque<>();
    private PrincipalControlador _principal;

    public Navegador(Stage ventana, Contexto ctx) {
        this._ventana = ventana;
        this._ctx = ctx;
        ctx.setNavegador(this);
    }

    /** Abre la ventana principal y muestra la cartelera. */
    public void iniciar() {
        FXMLLoader loader = cargador("principal");
        Parent raiz = cargar(loader);
        _principal = loader.getController();
        _principal.setContexto(_ctx);
        _principal.alIniciar();

        Scene escena = new Scene(raiz, 1240, 800);
        escena.getStylesheets().add(getClass().getResource("/cinemex/estilos.css").toExternalForm());
        _ventana.setScene(escena);
        _ventana.setTitle("Cinemex – Proyecto final POO");
        _ventana.setMinWidth(1000);
        _ventana.setMinHeight(680);
        var icono = getClass().getResourceAsStream("/cinemex/img/icono.png");
        if (icono != null) {
            _ventana.getIcons().add(new Image(icono));
        }
        _ventana.show();
        ir("cartelera");
    }

    /** Muestra la vista indicada (ej. "cartelera" carga /cinemex/vista/cartelera.fxml). */
    public <T extends ControladorBase> T ir(String vista) {
        return ir(vista, c -> { });
    }

    /** Igual, pero deja configurar el controlador (ej. qué película mostrar) antes de iniciar. */
    public <T extends ControladorBase> T ir(String vista, Consumer<T> preparar) {
        FXMLLoader loader = cargador(vista);
        Parent nodo = cargar(loader);
        T controlador = loader.getController();
        controlador.setContexto(_ctx);
        preparar.accept(controlador);
        Parent actual = _principal.getContenido();
        if (actual != null) {
            _historial.push(actual);
        }
        _principal.setContenido(nodo);
        controlador.alIniciar();
        return controlador;
    }

    /** Regresa a la pantalla anterior tal como estaba (misma página, mismos filtros). */
    public void regresar() {
        if (_historial.isEmpty()) {
            ir("cartelera");
        } else {
            _principal.setContenido(_historial.pop());
        }
    }

    /** Si no hay sesión manda al login y, al entrar, ejecuta la acción pendiente. */
    public void requiereSesion(Runnable accion) {
        if (_ctx.usuarios().getUsuarioActual() != null) {
            accion.run();
        } else {
            ir("login", (LoginControlador l) -> l.setAlEntrar(accion));
        }
    }

    /** Actualiza la barra de arriba (nombre del usuario, menú de administración). */
    public void actualizarBarra() {
        _principal.actualizarSesion();
    }

    private FXMLLoader cargador(String vista) {
        return new FXMLLoader(getClass().getResource(RUTA_VISTAS + vista + ".fxml"));
    }

    private Parent cargar(FXMLLoader loader) {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + loader.getLocation(), e);
        }
    }
}
