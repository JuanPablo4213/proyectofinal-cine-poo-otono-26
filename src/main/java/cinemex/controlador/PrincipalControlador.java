package cinemex.controlador;

import cinemex.modelo.usuarios.POJOUsuario;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;

/** La ventana principal: barra de navegación arriba y la pantalla actual en el centro. */
public class PrincipalControlador extends ControladorBase {
    @FXML private StackPane contenido;
    @FXML private Label lblUsuario;
    @FXML private Button btnSesion;
    @FXML private Button btnMisCompras;
    @FXML private MenuButton menuAdmin;

    @Override
    protected void alIniciar() {
        actualizarSesion();
    }

    /** Muestra el nombre del usuario y el menú de administración solo si es admin (RN10). */
    public void actualizarSesion() {
        POJOUsuario u = ctx.usuarios().getUsuarioActual();
        boolean admin = ctx.usuarios().esAdministrador();
        menuAdmin.setVisible(admin);
        menuAdmin.setManaged(admin);
        boolean cliente = ctx.usuarios().getClienteActual() != null;
        btnMisCompras.setVisible(cliente);
        btnMisCompras.setManaged(cliente);
        lblUsuario.setText(u == null ? "" : "Hola, " + u.getNombre().split(" ")[0] + " (" + u.getRol() + ")");
        btnSesion.setText(u == null ? "Iniciar sesión" : "Cerrar sesión");
    }

    Parent getContenido() {
        return contenido.getChildren().isEmpty() ? null : (Parent) contenido.getChildren().get(0);
    }

    void setContenido(Node nodo) {
        contenido.getChildren().setAll(nodo);
    }

    @FXML private void irCartelera() { ctx.navegador().ir("cartelera"); }
    @FXML private void irMiCine() { ctx.navegador().ir("mi-cine"); }
    @FXML private void irMisCompras() { ctx.navegador().requiereSesion(() -> ctx.navegador().ir("mis-compras")); }
    @FXML private void irAdminPeliculas() { ctx.navegador().ir("admin-peliculas"); }
    @FXML private void irAdminCines() { ctx.navegador().ir("admin-cines"); }
    @FXML private void irAdminFunciones() { ctx.navegador().ir("admin-funciones"); }
    @FXML private void irAcercaDe() { ctx.navegador().ir("acerca-de"); }

    @FXML
    private void sesion() {
        if (ctx.usuarios().getUsuarioActual() == null) {
            ctx.navegador().ir("login");
        } else {
            ctx.usuarios().cerrarSesion();
            actualizarSesion();
            ctx.navegador().ir("cartelera");
        }
    }
}
