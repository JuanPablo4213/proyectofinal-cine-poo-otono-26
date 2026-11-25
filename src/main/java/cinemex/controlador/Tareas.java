package cinemex.controlador;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Corre trabajo lento (peticiones a TMDB) en un hilo aparte para no congelar la ventana.
 * Cuando termina, "alTerminar" o "alFallar" se ejecutan de vuelta en el hilo de JavaFX,
 * así ya pueden tocar la interfaz.
 */
public final class Tareas {
    private Tareas() { }

    public static <T> void enSegundoPlano(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabajo.call();
            }
        };
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alFallar.accept(tarea.getException()));
        Thread hilo = new Thread(tarea, "tarea-cinemex");
        hilo.setDaemon(true);   // si se cierra la app, el hilo no la detiene
        hilo.start();
    }
}
