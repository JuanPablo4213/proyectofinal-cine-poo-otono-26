package cinemex.controlador;

import cinemex.modelo.usuarios.POJOCliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Iniciar sesión y crear cuenta de cliente. */
public class LoginControlador extends ControladorBase {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblErrorLogin;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmailNuevo;
    @FXML private TextField txtTelefono;
    @FXML private PasswordField txtPasswordNuevo;
    @FXML private PasswordField txtConfirmar;
    @FXML private Label lblErrorRegistro;

    private Runnable _alEntrar;

    /** Lo que se hace después de entrar (por ejemplo, volver a la compra). */
    public void setAlEntrar(Runnable accion) {
        this._alEntrar = accion;
    }

    @FXML
    private void entrar() {
        lblErrorLogin.setText("");
        try {
            if (ctx.usuarios().iniciarSesion(txtEmail.getText(), txtPassword.getText()) == null) {
                lblErrorLogin.setText("Correo o contraseña incorrectos.");
                return;
            }
        } catch (RuntimeException e) {
            lblErrorLogin.setText(e.getMessage());
            return;
        }
        terminar();
    }

    @FXML
    private void registrar() {
        lblErrorRegistro.setText("");
        if (!txtPasswordNuevo.getText().equals(txtConfirmar.getText())) {
            lblErrorRegistro.setText("Las contraseñas no coinciden.");
            return;
        }
        try {
            POJOCliente c = ctx.usuarios().registrarCliente(txtNombre.getText(), txtEmailNuevo.getText(),
                    txtTelefono.getText(), txtPasswordNuevo.getText());
            ctx.usuarios().iniciarSesion(c.getEmail(), txtPasswordNuevo.getText());
        } catch (RuntimeException e) {
            lblErrorRegistro.setText(e.getMessage());
            return;
        }
        terminar();
    }

    private void terminar() {
        ctx.navegador().actualizarBarra();
        if (_alEntrar != null) {
            ctx.navegador().regresar();
            _alEntrar.run();
        } else {
            ctx.navegador().ir("cartelera");
        }
    }
}
