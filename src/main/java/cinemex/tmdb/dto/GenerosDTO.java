package cinemex.tmdb.dto;

import java.util.List;

/** Respuesta de /3/genre/movie/list: {"genres":[{"id":28,"name":"Acción"}, ...]} */
public class GenerosDTO {
    public List<GeneroDTO> genres;
}
