package cinemex;

import cinemex.api.ClienteCinemexAPI;
import cinemex.base.Config;
import cinemex.base.ConexionBD;
import cinemex.catalogo.IClientePeliculas;
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

        IClientePeliculas catalogo = "tmdb".equalsIgnoreCase(Config.get("peliculas.fuente"))
                ? ClienteTMDB.desdeConfig()
                : ClienteCinemexAPI.desdeConfig();

        Contexto ctx = new Contexto(usuarios, peliculas, cines, funciones, compras, catalogo);
        new Navegador(ventana, ctx).iniciar();
    }

    @Override
    public void stop() {
        ConexionBD.cerrar();
    }
}
