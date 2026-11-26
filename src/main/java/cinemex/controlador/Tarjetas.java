package cinemex.controlador;

import cinemex.modelo.peliculas.POJOPelicula;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class Tarjetas {
    public static final double ANCHO = 170;
    public static final double ALTO = 255;

    private Tarjetas() { }

    public static StackPane poster(POJOPelicula p, double ancho, double alto) {
        StackPane caja = new StackPane();
        caja.setPrefSize(ancho, alto);
        caja.setMinSize(ancho, alto);
        caja.setMaxSize(ancho, alto);
        Label sinPoster = new Label("Sin póster");
        sinPoster.setPrefSize(ancho, alto);
        sinPoster.setAlignment(Pos.CENTER);
        sinPoster.getStyleClass().add("sin-poster");
        caja.getChildren().add(sinPoster);
        if (p.getUrlPoster() != null) {
            ImageView img = new ImageView(new Image(p.getUrlPoster(), ancho, alto, false, true, true));
            img.setFitWidth(ancho);
            img.setFitHeight(alto);
            caja.getChildren().add(img);
        }
        return caja;
    }

    public static VBox tarjeta(POJOPelicula p, Runnable alDarClic) {
        Label titulo = new Label(p.getTitulo());
        titulo.setWrapText(true);
        titulo.setMaxWidth(ANCHO);
        titulo.getStyleClass().add("tarjeta-titulo");

        String sub = p.getFechaEstreno() == null ? "" : p.getFechaEstreno().toString();
        if (p.getCalificacion() > 0) {
            sub += String.format("  ★ %.1f", p.getCalificacion());
        }
        Label detalle = new Label(sub);
        detalle.getStyleClass().add("tarjeta-sub");

        VBox tarjeta = new VBox(6, poster(p, ANCHO, ALTO), titulo, detalle);
        tarjeta.setPrefWidth(ANCHO);
        tarjeta.setMaxWidth(ANCHO);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.setOnMouseClicked(e -> alDarClic.run());
        return tarjeta;
    }
}
