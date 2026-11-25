package cinemex.controlador;

import cinemex.base.ExcepcionCine;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Padre de todos los controladores. Reemplaza a IMenu del diseño: cada pantalla
 * recibe el Contexto (los Servicios) y tiene ayudas para mostrar mensajes.
 */
public abstract class ControladorBase {
    protected Contexto ctx;

    void setContexto(Contexto ctx) {
        this.ctx = ctx;
    }

    /** Se llama cuando ya hay Contexto y datos (initialize() de FXML corre antes). */
    protected void alIniciar() { }

    /** Muestra un error entendible; si no es de negocio, un mensaje genérico. */
    protected void mostrarError(Throwable e) {
        String mensaje = e instanceof ExcepcionCine ? e.getMessage()
                : "Ocurrió un error inesperado: " + e.getMessage();
        mostrarError(mensaje);
    }

    protected void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Cinemex");
        a.showAndWait();
    }

    protected void mostrarInfo(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setHeaderText(titulo);
        a.setTitle("Cinemex");
        a.showAndWait();
    }

    protected boolean confirmar(String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.setTitle("Cinemex");
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.YES;
    }

    /** Ejecuta una acción del Servicio y si lanza error lo muestra. Regresa true si salió bien. */
    protected boolean intentar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (RuntimeException e) {
            mostrarError(e);
            return false;
        }
    }

    protected <T> void enSegundoPlano(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        Tareas.enSegundoPlano(trabajo, alTerminar, alFallar);
    }
}
