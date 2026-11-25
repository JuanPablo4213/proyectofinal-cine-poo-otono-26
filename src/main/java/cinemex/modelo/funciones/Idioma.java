package cinemex.modelo.funciones;

public enum Idioma {
    ESPANOL("Español"),
    SUBTITULADA("Subtitulada");

    private final String _texto;

    Idioma(String texto) {
        this._texto = texto;
    }

    @Override
    public String toString() {
        return _texto;
    }
}
