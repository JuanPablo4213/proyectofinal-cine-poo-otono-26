package cinemex.controlador;

import java.io.InputStream;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AcercaDeControlador extends ControladorBase {
    @FXML private ImageView imgTmdb;

    @FXML
    private void initialize() {
        InputStream logo = getClass().getResourceAsStream("/cinemex/img/tmdb-logo.png");
        if (logo != null) {
            imgTmdb.setImage(new Image(logo));
        }
    }
}
