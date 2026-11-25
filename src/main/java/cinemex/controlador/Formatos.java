package cinemex.controlador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos de fecha, hora y dinero en español para mostrar en pantalla. */
public final class Formatos {
    private static final Locale MX = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEE d 'de' MMM yyyy", MX);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", MX);

    private Formatos() { }

    public static String fecha(LocalDate f) { return f == null ? "" : f.format(FECHA); }
    public static String fecha(LocalDateTime f) { return f == null ? "" : f.format(FECHA); }
    public static String hora(LocalDateTime f) { return f == null ? "" : f.format(HORA); }
    public static String fechaHora(LocalDateTime f) { return f == null ? "" : f.format(FECHA_HORA); }
    public static String dinero(double monto) { return String.format("$%,.2f", monto); }

    public static String duracion(int minutos) {
        return minutos <= 0 ? "" : (minutos / 60) + " h " + (minutos % 60) + " min";
    }
}
