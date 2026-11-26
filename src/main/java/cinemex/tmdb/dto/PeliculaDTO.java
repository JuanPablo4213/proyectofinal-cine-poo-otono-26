package cinemex.tmdb.dto;

import java.util.List;

public class PeliculaDTO {
    public int id;
    public String title;
    public String overview;
    public String posterPath;
    public String releaseDate;
    public double voteAverage;
    public List<Integer> genreIds;
    public Integer runtime;
    public List<GeneroDTO> genres;
    public ReleaseDatesDTO releaseDates;
}
