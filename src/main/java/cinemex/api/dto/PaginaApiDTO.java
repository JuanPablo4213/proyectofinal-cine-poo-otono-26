package cinemex.api.dto;

import java.util.List;

/** {"pagina":1,"totalPaginas":10,"totalResultados":200,"peliculas":[...]} */
public class PaginaApiDTO {
    public int pagina;
    public int totalPaginas;
    public int totalResultados;
    public List<PeliculaApiDTO> peliculas;
}
