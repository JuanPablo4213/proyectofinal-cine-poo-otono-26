package cinemex.api.dto;

import java.util.List;

public class PaginaApiDTO {
    public int pagina;
    public int totalPaginas;
    public int totalResultados;
    public List<PeliculaApiDTO> peliculas;
}
