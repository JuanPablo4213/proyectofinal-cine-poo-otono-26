package cinemex.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static final String ARCHIVO = "config.properties";
    private static Properties _props;

    private Config() { }

    public static String get(String llave) {
        if (_props == null) {
            cargar();
        }
        return _props.getProperty(llave, "").trim();
    }

    private static void cargar() {
        _props = new Properties();
        Path ruta = Path.of(ARCHIVO);
        try {
            if (Files.exists(ruta)) {
                try (InputStream in = new FileInputStream(ruta.toFile())) {
                    _props.load(in);
                }
            } else {
                try (InputStream in = Config.class.getResourceAsStream("/" + ARCHIVO)) {
                    if (in != null) {
                        _props.load(in);
                    }
                }
            }
        } catch (IOException e) {
            throw new ExcepcionCine("No se pudo leer " + ARCHIVO, e);
        }
    }
}
