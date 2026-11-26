package cinemex.tmdb;

import cinemex.base.Config;
import cinemex.catalogo.IClientePeliculas;
import cinemex.catalogo.PaginaPeliculas;
import cinemex.modelo.peliculas.Clasificacion;
import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import cinemex.tmdb.dto.GeneroDTO;
import cinemex.tmdb.dto.GenerosDTO;
import cinemex.tmdb.dto.PaginaDTO;
import cinemex.tmdb.dto.PeliculaDTO;
import cinemex.tmdb.dto.ReleaseDatesDTO;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente de The Movie Database. Hace las peticiones con HttpClient, convierte el JSON
 * con Gson y regresa objetos del MODELO (POJOPelicula, POJOGenero): el JSON nunca llega a las vistas.
 *
 * Estos métodos tardan (van a internet): los controladores los llaman en segundo plano.
 */
public class ClienteTMDB implements IClientePeliculas {
    private static final String URL_BASE = "https://api.themoviedb.org/3";
    private static final String IDIOMA = "es-MX";
    private static final String REGION = "MX";

    private final HttpClient _http;
    private final Gson _gson;
    private final String _token;
    private final String _apiKey;
    private Map<Integer, POJOGenero> _generos;   // caché: id -> género

    public ClienteTMDB(String token, String apiKey) {
        this._token = token == null ? "" : token.trim();
        this._apiKey = apiKey == null ? "" : apiKey.trim();
        this._http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this._gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    /** Lee tmdb.token / tmdb.apiKey de config.properties. */
    public static ClienteTMDB desdeConfig() {
        return new ClienteTMDB(Config.get("tmdb.token"), Config.get("tmdb.apiKey"));
    }

    // ------------------------------------------------------- los 6 endpoints

    /** /3/movie/popular */
    @Override
    public PaginaPeliculas populares(int pagina) {
        return lista("/movie/popular", parametros("page", pagina));
    }

    /** /3/movie/now_playing (región México) */
    @Override
    public PaginaPeliculas enCartelera(int pagina) {
        return lista("/movie/now_playing", parametros("page", pagina, "region", REGION));
    }

    /** /3/movie/upcoming (región México) */
    @Override
    public PaginaPeliculas proximosEstrenos(int pagina) {
        return lista("/movie/upcoming", parametros("page", pagina, "region", REGION));
    }

    /** /3/search/movie */
    @Override
    public PaginaPeliculas buscar(String texto, int pagina) {
        if (texto == null || texto.isBlank()) {
            throw new ExcepcionTMDB("Escribe el título de una película para buscar.");
        }
        return lista("/search/movie", parametros("query", texto.trim(), "page", pagina, "include_adult", "false"));
    }

    /**
     * /3/movie/{movie_id}: sinopsis, duración, géneros, póster y clasificación.
     * Con append_to_response=release_dates viene la clasificación de cada país; usamos la de México.
     */
    @Override
    public POJOPelicula detalle(int tmdbId) {
        String json = get("/movie/" + tmdbId, parametros("append_to_response", "release_dates"));
        return convertirDetalle(json);
    }

    /** /3/genre/movie/list */
    @Override
    public List<POJOGenero> generos() {
        String json = get("/genre/movie/list", parametros());
        GenerosDTO dto = leer(json, GenerosDTO.class);
        List<POJOGenero> lista = new ArrayList<>();
        Map<Integer, POJOGenero> cache = new LinkedHashMap<>();
        if (dto.genres != null) {
            for (GeneroDTO g : dto.genres) {
                POJOGenero genero = new POJOGenero(g.id, g.name);
                lista.add(genero);
                cache.put(g.id, genero);
            }
        }
        _generos = cache;
        return lista;
    }

    @Override
    public String getNombre() {
        return "TMDB";
    }

    // ------------------------------------------------------- conversión JSON -> modelo

    PaginaPeliculas convertirPagina(String json) {
        PaginaDTO dto = leer(json, PaginaDTO.class);
        List<POJOPelicula> peliculas = new ArrayList<>();
        if (dto.results != null) {
            for (PeliculaDTO p : dto.results) {
                POJOPelicula pelicula = aPelicula(p);
                if (p.genreIds != null) {
                    List<POJOGenero> generos = new ArrayList<>();
                    for (Integer id : p.genreIds) {
                        POJOGenero g = _generos == null ? null : _generos.get(id);
                        generos.add(g != null ? g : new POJOGenero(id, "Género " + id));
                    }
                    pelicula.setGeneros(generos);
                }
                peliculas.add(pelicula);
            }
        }
        return new PaginaPeliculas(Math.max(dto.page, 1), Math.max(dto.totalPages, 1), dto.totalResults, peliculas);
    }

    POJOPelicula convertirDetalle(String json) {
        PeliculaDTO dto = leer(json, PeliculaDTO.class);
        POJOPelicula p = aPelicula(dto);
        List<POJOGenero> generos = new ArrayList<>();
        if (dto.genres != null) {
            for (GeneroDTO g : dto.genres) {
                generos.add(new POJOGenero(g.id, g.name));
            }
        }
        p.setGeneros(generos);
        return p;
    }

    private POJOPelicula aPelicula(PeliculaDTO dto) {
        POJOPelicula p = new POJOPelicula(0, dto.id, dto.title, dto.overview,
                dto.runtime == null ? 0 : dto.runtime, clasificacionMexico(dto.releaseDates),
                dto.posterPath, fecha(dto.releaseDate));
        p.setCalificacion(dto.voteAverage);
        return p;
    }

    /** Busca la certificación de México (iso_3166_1 = "MX") entre las fechas de estreno. */
    private Clasificacion clasificacionMexico(ReleaseDatesDTO fechas) {
        if (fechas == null || fechas.results == null) {
            return Clasificacion.SC;
        }
        for (ReleaseDatesDTO.Pais pais : fechas.results) {
            if (REGION.equals(pais.pais) && pais.releaseDates != null) {
                for (ReleaseDatesDTO.Fecha f : pais.releaseDates) {
                    Clasificacion c = Clasificacion.desdeTexto(f.certification);
                    if (c != Clasificacion.SC) {
                        return c;
                    }
                }
            }
        }
        return Clasificacion.SC;
    }

    private static LocalDate fecha(String texto) {
        try {
            return texto == null || texto.isBlank() ? null : LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ------------------------------------------------------- HTTP

    private PaginaPeliculas lista(String ruta, Map<String, String> params) {
        if (_generos == null) {
            try {
                generos();   // para poder ponerle nombre a los genre_ids de cada película
            } catch (ExcepcionTMDB e) {
                _generos = Map.of();
            }
        }
        return convertirPagina(get(ruta, params));
    }

    /** Arma la URL, hace el GET y traduce los errores a mensajes para el usuario. */
    private String get(String ruta, Map<String, String> params) {
        if (_token.isEmpty() && _apiKey.isEmpty()) {
            throw new ExcepcionTMDB("Falta tu llave de TMDB en config.properties (tmdb.token o tmdb.apiKey).");
        }
        params.put("language", IDIOMA);
        if (_token.isEmpty()) {
            params.put("api_key", _apiKey);
        }
        StringBuilder url = new StringBuilder(URL_BASE).append(ruta).append('?');
        params.forEach((k, v) -> url.append(k).append('=')
                .append(URLEncoder.encode(v, StandardCharsets.UTF_8)).append('&'));

        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(url.toString()))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET();
        if (!_token.isEmpty()) {
            peticion.header("Authorization", "Bearer " + _token);
        }
        HttpResponse<String> respuesta;
        try {
            respuesta = _http.send(peticion.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ExcepcionTMDB("No hay conexión con TMDB. Revisa tu internet e inténtalo de nuevo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionTMDB("La petición a TMDB se canceló.", e);
        }
        switch (respuesta.statusCode()) {
            case 200:
                return respuesta.body();
            case 401:
                throw new ExcepcionTMDB("La llave de TMDB no es válida. Revisa config.properties.");
            case 404:
                throw new ExcepcionTMDB("TMDB no encontró esa película.");
            case 429:
                throw new ExcepcionTMDB("Demasiadas peticiones a TMDB. Espera unos segundos.");
            default:
                throw new ExcepcionTMDB("TMDB respondió con un error (" + respuesta.statusCode() + ").");
        }
    }

    private <T> T leer(String json, Class<T> clase) {
        try {
            T obj = _gson.fromJson(json, clase);
            if (obj == null) {
                throw new ExcepcionTMDB("TMDB mandó una respuesta vacía.");
            }
            return obj;
        } catch (JsonParseException e) {
            throw new ExcepcionTMDB("No se pudo leer la respuesta de TMDB.", e);
        }
    }

    private static Map<String, String> parametros(Object... pares) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            m.put(String.valueOf(pares[i]), String.valueOf(pares[i + 1]));
        }
        return m;
    }
}
