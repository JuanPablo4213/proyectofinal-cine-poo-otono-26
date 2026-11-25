package cinemex.tmdb.dto;

import java.util.List;

/**
 * Así viene una película en el JSON de TMDB. Gson llena estos campos solo
 * (poster_path -> posterPath). Es un DTO: NO sale del paquete tmdb, se convierte a POJOPelicula.
 */
public class PeliculaDTO {
    public int id;
    public String title;
    public String overview;
    public String posterPath;
    public String releaseDate;
    public double voteAverage;
    public List<Integer> genreIds;       // en listas (popular, now_playing, ...)
    public Integer runtime;              // solo en el detalle
    public List<GeneroDTO> genres;       // solo en el detalle
    public ReleaseDatesDTO releaseDates; // solo en el detalle (append_to_response=release_dates)
}
