package cinemex.modelo.peliculas;

public enum Clasificacion {
    AA("AA - Todo público, atractiva para niños"),
    A("A - Todo público"),
    B("B - Adolescentes de 12 años en adelante"),
    B15("B15 - Mayores de 15 años"),
    C("C - Adultos de 18 años en adelante"),
    D("D - Exclusiva para adultos"),
    SC("Sin clasificar");

    private final String _descripcion;

    Clasificacion(String descripcion) {
        this._descripcion = descripcion;
    }

    public String getDescripcion() { return _descripcion; }

    public static Clasificacion desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return SC;
        }
        String limpio = texto.trim().toUpperCase().replace("-", "").replace(" ", "");
        for (Clasificacion c : values()) {
            if (c.name().equals(limpio)) {
                return c;
            }
        }
        return SC;
    }
}
