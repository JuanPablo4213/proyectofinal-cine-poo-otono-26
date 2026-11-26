package cinemex.controlador;

import cinemex.base.ExcepcionCine;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public abstract class ControladorBase {
    protected Contexto ctx;

    void setContexto(Contexto ctx) {
        this.ctx = ctx;
    }

    protected void alIniciar() { }

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
