package cinemex;

import cinemex.base.ConexionBD;
import cinemex.controlador.Contexto;
import cinemex.controlador.Navegador;
import cinemex.servicio.ServicioCines;
import cinemex.servicio.ServicioCompras;
import cinemex.servicio.ServicioFunciones;
import cinemex.servicio.ServicioPeliculas;
import cinemex.servicio.ServicioUsuarios;
import cinemex.tmdb.ClienteTMDB;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada. Igual que en el diseño: App crea los Servicios UNA sola vez
 * y se los pasa a todas las pantallas (por medio del Contexto), para que todas
 * compartan los mismos datos y la misma sesión.
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ventana) {
        ServicioUsuarios usuarios = new ServicioUsuarios();
        ServicioPeliculas peliculas = new ServicioPeliculas();
        ServicioCines cines = new ServicioCines();
        ServicioFunciones funciones = new ServicioFunciones(peliculas, cines);
        ServicioCompras compras = new ServicioCompras(funciones);

        Contexto ctx = new Contexto(usuarios, peliculas, cines, funciones, compras, ClienteTMDB.desdeConfig());
        new Navegador(ventana, ctx).iniciar();
    }

    @Override
    public void stop() {
        ConexionBD.cerrar();
    }
}
