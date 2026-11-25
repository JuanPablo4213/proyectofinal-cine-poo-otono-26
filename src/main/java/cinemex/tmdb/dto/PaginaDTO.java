package cinemex.tmdb.dto;

import java.util.List;

/** Respuesta paginada de TMDB: {"page":1,"results":[...],"total_pages":500,"total_results":10000} */
public class PaginaDTO {
    public int page;
    public int totalPages;
    public int totalResults;
    public List<PeliculaDTO> results;
}
