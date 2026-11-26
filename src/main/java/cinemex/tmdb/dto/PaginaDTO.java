package cinemex.tmdb.dto;

import java.util.List;

public class PaginaDTO {
    public int page;
    public int totalPages;
    public int totalResults;
    public List<PeliculaDTO> results;
}
