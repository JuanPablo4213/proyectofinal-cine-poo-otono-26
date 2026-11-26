package cinemex.api;

import cinemex.api.dto.ErrorApiDTO;
import cinemex.api.dto.PaginaApiDTO;
import cinemex.api.dto.PeliculaApiDTO;
import cinemex.base.Config;
import cinemex.catalogo.ExcepcionCatalogo;
import cinemex.catalogo.IClientePeliculas;
import cinemex.catalogo.PaginaPeliculas;
import cinemex.modelo.peliculas.Clasificacion;
import cinemex.modelo.peliculas.FiltroTitulo;
import cinemex.modelo.peliculas.IFiltroPelicula;
import cinemex.modelo.peliculas.POJOGenero;
import cinemex.modelo.peliculas.POJOPelicula;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClienteCinemexAPI implements IClientePeliculas {
    public static final String URL_DEFAULT =
            "https://cinemex-api-bye8gdg4chbsa7e3.northcentralus-01.azurewebsites.net/api/peliculas";
    private static final int PAGINAS_PARA_BUSCAR = 2;
    private static final int RESULTADOS_POR_PAGINA = 20;

    private static final Map<String, Integer> IDS_GENERO = Map.ofEntries(
            Map.entry("accion", 28), Map.entry("aventura", 12), Map.entry("animacion", 16),
            Map.entry("comedia", 35), Map.entry("crimen", 80), Map.entry("documental", 99),
            Map.entry("drama", 18), Map.entry("familia", 10751), Map.entry("fantasia", 14),
            Map.entry("historia", 36), Map.entry("terror", 27), Map.entry("musica", 10402),
            Map.entry("misterio", 9648), Map.entry("romance", 10749), Map.entry("ciencia ficcion", 878),
            Map.entry("pelicula de tv", 10770), Map.entry("suspense", 53), Map.entry("suspenso", 53),
            Map.entry("belica", 10752), Map.entry("guerra", 10752), Map.entry("western", 37));

    private final String _urlBase;
    private final HttpClient _http;
    private final Gson _gson = new Gson();

    public ClienteCinemexAPI(String urlBase) {
        String url = (urlBase == null || urlBase.isBlank()) ? URL_DEFAULT : urlBase.trim();
        this._urlBase = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this._http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static ClienteCinemexAPI desdeConfig() {
        return new ClienteCinemexAPI(Config.get("api.url"));
    }

    @Override
    public String getNombre() {
        return "API Cinemex";
    }

    @Override
    public PaginaPeliculas populares(int pagina) {
        return convertirPagina(get("/populares?page=" + pagina));
    }

    @Override
    public PaginaPeliculas enCartelera(int pagina) {
        return convertirPagina(get("/cartelera?page=" + pagina));
    }

    @Override
    public PaginaPeliculas proximosEstrenos(int pagina) {
        return convertirPagina(get("/proximos-estrenos?page=" + pagina));
    }

    @Override
    public POJOPelicula detalle(int id) {
        return aPelicula(leer(get("/" + id), PeliculaApiDTO.class));
    }

    @Override
    public PaginaPeliculas buscar(String texto, int pagina) {
        if (texto == null || texto.isBlank()) {
            throw new ExcepcionCatalogo("Escribe el título de una película para buscar.");
        }
        IFiltroPelicula filtro = new FiltroTitulo(texto.trim());
        List<POJOPelicula> encontradas = new ArrayList<>();
        for (POJOPelicula p : todasLasListas()) {
            if (filtro.cumple(p)) {
                encontradas.add(p);
            }
        }
        int totalPaginas = Math.max(1, (encontradas.size() + RESULTADOS_POR_PAGINA - 1) / RESULTADOS_POR_PAGINA);
        int p = Math.min(Math.max(pagina, 1), totalPaginas);
        int desde = (p - 1) * RESULTADOS_POR_PAGINA;
        int hasta = Math.min(desde + RESULTADOS_POR_PAGINA, encontradas.size());
        return new PaginaPeliculas(p, totalPaginas, encontradas.size(), new ArrayList<>(encontradas.subList(desde, hasta)));
    }

    @Override
    public List<POJOGenero> generos() {
        Map<Integer, POJOGenero> unicos = new LinkedHashMap<>();
        for (POJOPelicula p : primeraPaginaDeCadaLista()) {
            for (POJOGenero g : p.getGeneros()) {
                unicos.putIfAbsent(g.getId(), g);
            }
        }
        List<POJOGenero> lista = new ArrayList<>(unicos.values());
        lista.sort(Comparator.comparing(POJOGenero::getNombre));
        return lista;
    }

    PaginaPeliculas convertirPagina(String json) {
        PaginaApiDTO dto = leer(json, PaginaApiDTO.class);
        List<POJOPelicula> peliculas = new ArrayList<>();
        if (dto.peliculas != null) {
            for (PeliculaApiDTO p : dto.peliculas) {
                peliculas.add(aPelicula(p));
            }
        }
        return new PaginaPeliculas(Math.max(dto.pagina, 1), Math.max(dto.totalPaginas, 1), dto.totalResultados, peliculas);
    }

    POJOPelicula aPelicula(PeliculaApiDTO dto) {
        POJOPelicula p = new POJOPelicula(0, dto.id, dto.titulo, dto.sinopsis,
                dto.duracion == null ? 0 : dto.duracion, Clasificacion.desdeTexto(dto.clasificacion),
                dto.cartel, fecha(dto.fechaEstreno));
        p.setCalificacion(dto.calificacion == null ? 0 : dto.calificacion);
        List<POJOGenero> generos = new ArrayList<>();
        if (dto.generos != null) {
            for (String nombre : dto.generos) {
                if (nombre != null && !nombre.isBlank()) {
                    generos.add(new POJOGenero(idGenero(nombre), nombre.trim()));
                }
            }
        }
        p.setGeneros(generos);
        return p;
    }

    static int idGenero(String nombre) {
        String clave = Normalizer.normalize(nombre.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        Integer id = IDS_GENERO.get(clave);
        return id != null ? id : 1_000_000 + Math.abs(clave.hashCode() % 1_000_000);
    }

    private static LocalDate fecha(String texto) {
        if (texto == null || texto.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(texto.substring(0, 10));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private List<POJOPelicula> todasLasListas() {
        Map<Integer, POJOPelicula> unicas = new LinkedHashMap<>();
        for (int pagina = 1; pagina <= PAGINAS_PARA_BUSCAR; pagina++) {
            agregar(unicas, populares(pagina));
            agregar(unicas, enCartelera(pagina));
            agregar(unicas, proximosEstrenos(pagina));
        }
        return new ArrayList<>(unicas.values());
    }

    private List<POJOPelicula> primeraPaginaDeCadaLista() {
        Map<Integer, POJOPelicula> unicas = new LinkedHashMap<>();
        agregar(unicas, populares(1));
        agregar(unicas, enCartelera(1));
        agregar(unicas, proximosEstrenos(1));
        return new ArrayList<>(unicas.values());
    }

    private static void agregar(Map<Integer, POJOPelicula> destino, PaginaPeliculas pagina) {
        for (POJOPelicula p : pagina.getPeliculas()) {
            destino.putIfAbsent(p.getTmdbId(), p);
        }
    }

    private String get(String ruta) {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(_urlBase + ruta))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = _http.send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ExcepcionCatalogo("No hay conexión con la API de películas. Revisa tu internet e inténtalo de nuevo.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionCatalogo("La petición a la API se canceló.", e);
        }
        int codigo = respuesta.statusCode();
        if (codigo == 200) {
            return respuesta.body();
        }
        String detalle = mensajeDeError(respuesta.body());
        if (codigo == 404) {
            throw new ExcepcionCatalogo("La API no encontró esa película.");
        }
        if (codigo == 400) {
            throw new ExcepcionCatalogo("Petición inválida a la API" + detalle);
        }
        throw new ExcepcionCatalogo("La API de películas respondió con un error (" + codigo + ")" + detalle);
    }

    private String mensajeDeError(String cuerpo) {
        try {
            ErrorApiDTO e = _gson.fromJson(cuerpo, ErrorApiDTO.class);
            return e != null && e.error != null ? ": " + e.error : ".";
        } catch (JsonParseException ex) {
            return ".";
        }
    }

    private <T> T leer(String json, Class<T> clase) {
        try {
            T obj = _gson.fromJson(json, clase);
            if (obj == null) {
                throw new ExcepcionCatalogo("La API mandó una respuesta vacía.");
            }
            return obj;
        } catch (JsonParseException e) {
            throw new ExcepcionCatalogo("No se pudo leer la respuesta de la API.", e);
        }
    }
}
