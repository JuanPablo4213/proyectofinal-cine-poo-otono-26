package cinemex.controlador;

import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Atajo para crear columnas de TableView a partir de un getter del POJO. */
public final class Tablas {
    private Tablas() { }

    public static <T> TableColumn<T, String> columna(TableView<T> tabla, String titulo, double ancho,
                                                    Function<T, Object> valor) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> {
            Object v = valor.apply(c.getValue());
            return new ReadOnlyStringWrapper(v == null ? "" : v.toString());
        });
        col.setPrefWidth(ancho);
        tabla.getColumns().add(col);
        return col;
    }
}
