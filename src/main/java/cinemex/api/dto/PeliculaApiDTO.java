package cinemex.api.dto;

import java.util.List;

/** Así viene una película en el JSON de la API del grupo (ver /api/peliculas/openapi.json). */
public class PeliculaApiDTO {
    public int id;
    public String titulo;
    public String sinopsis;
    public String fechaEstreno;
    public Integer duracion;
    public List<String> generos;
    public String clasificacion;
    public String cartel;
    public Double calificacion;
    public String estado;
}
