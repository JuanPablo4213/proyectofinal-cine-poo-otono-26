package cinemex.controlador;

import cinemex.servicio.ServicioCines;
import cinemex.servicio.ServicioCompras;
import cinemex.servicio.ServicioFunciones;
import cinemex.servicio.ServicioPeliculas;
import cinemex.servicio.ServicioUsuarios;
import cinemex.catalogo.IClientePeliculas;

/** Lo que comparten todas las pantallas: los Servicios (Modelo), el catálogo de películas y el Navegador. */
public class Contexto {
    private final ServicioUsuarios _usuarios;
    private final ServicioPeliculas _peliculas;
    private final ServicioCines _cines;
    private final ServicioFunciones _funciones;
    private final ServicioCompras _compras;
    private final IClientePeliculas _catalogo;
    private Navegador _navegador;

    public Contexto(ServicioUsuarios usuarios, ServicioPeliculas peliculas, ServicioCines cines,
                    ServicioFunciones funciones, ServicioCompras compras, IClientePeliculas catalogo) {
        this._usuarios = usuarios;
        this._peliculas = peliculas;
        this._cines = cines;
        this._funciones = funciones;
        this._compras = compras;
        this._catalogo = catalogo;
    }

    public ServicioUsuarios usuarios() { return _usuarios; }
    public ServicioPeliculas peliculas() { return _peliculas; }
    public ServicioCines cines() { return _cines; }
    public ServicioFunciones funciones() { return _funciones; }
    public ServicioCompras compras() { return _compras; }
    /** API Cinemex o TMDB, según config.properties: las pantallas no saben cuál es. */
    public IClientePeliculas catalogo() { return _catalogo; }
    public Navegador navegador() { return _navegador; }

    void setNavegador(Navegador navegador) { this._navegador = navegador; }
}
