package cinemex.controlador;

import cinemex.modelo.cines.POJOCine;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.cines.POJOSalaX4D;
import cinemex.modelo.compras.TipoBoleto;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminCinesControlador extends ControladorBase {
    @FXML private TableView<POJOCine> tablaCines;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtDireccion;
    @FXML private TableView<POJOSala> tablaSalas;
    @FXML private Label lblSalas;
    @FXML private TextField txtNumero;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtFilas;
    @FXML private TextField txtColumnas;
    @FXML private TextField txtCargo;

    @FXML
    private void initialize() {
        Tablas.columna(tablaCines, "Id", 45, c -> c.getId());
        Tablas.columna(tablaCines, "Nombre", 170, c -> c.getNombre());
        Tablas.columna(tablaCines, "Ciudad", 130, c -> c.getCiudad());
        Tablas.columna(tablaCines, "Dirección", 220, c -> c.getDireccion());
        Tablas.columna(tablaCines, "Salas", 55, c -> c.getSalas().size());

        Tablas.columna(tablaSalas, "Sala", 55, s -> s.getNumero());
        Tablas.columna(tablaSalas, "Tipo", 100, s -> s.getTipoSala());
        Tablas.columna(tablaSalas, "Filas", 55, s -> s.getFilas());
        Tablas.columna(tablaSalas, "Columnas", 75, s -> s.getColumnas());
        Tablas.columna(tablaSalas, "Asientos", 75, s -> s.getCapacidad());
        Tablas.columna(tablaSalas, "Adulto", 80, s -> Formatos.dinero(s.calcularPrecio(TipoBoleto.ADULTO)));
        Tablas.columna(tablaSalas, "Cargo X4D", 85, s -> s instanceof POJOSalaX4D x ? Formatos.dinero(x.getCargoEfectos()) : "");

        cmbTipo.getItems().setAll("TRADICIONAL", "PLATINO", "X4D");
        cmbTipo.setValue("TRADICIONAL");
        cmbTipo.setOnAction(e -> txtCargo.setDisable(!"X4D".equals(cmbTipo.getValue())));
        txtCargo.setDisable(true);
        tablaCines.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> seleccionarCine(c));
    }

    @Override
    protected void alIniciar() {
        cargar(0);
    }

    private void cargar(int idSeleccionar) {
        try {
            tablaCines.getItems().setAll(ctx.cines().listar());
        } catch (RuntimeException e) {
            mostrarError(e);
            return;
        }
        tablaCines.getItems().stream().filter(c -> c.getId() == idSeleccionar).findFirst()
                .ifPresent(c -> tablaCines.getSelectionModel().select(c));
    }

    private void seleccionarCine(POJOCine c) {
        if (c == null) {
            tablaSalas.getItems().clear();
            lblSalas.setText("Salas (selecciona un cine)");
            return;
        }
        txtNombre.setText(c.getNombre());
        txtCiudad.setText(c.getCiudad());
        txtDireccion.setText(c.getDireccion());
        tablaSalas.getItems().setAll(c.getSalas());
        lblSalas.setText("Salas de " + c.getNombre());
    }

    @FXML
    private void nuevoCine() {
        tablaCines.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtCiudad.clear();
        txtDireccion.clear();
        txtNombre.requestFocus();
    }

    @FXML
    private void guardarCine() {
        POJOCine sel = tablaCines.getSelectionModel().getSelectedItem();
        POJOCine cine = sel != null ? sel : new POJOCine(0, null, null, null);
        cine.setNombre(txtNombre.getText());
        cine.setCiudad(txtCiudad.getText());
        cine.setDireccion(txtDireccion.getText());
        if (intentar(() -> ctx.cines().guardar(cine))) {
            cargar(cine.getId());
        }
    }

    @FXML
    private void eliminarCine() {
        POJOCine c = tablaCines.getSelectionModel().getSelectedItem();
        if (c != null && confirmar("¿Eliminar " + c.getNombre() + " con todas sus salas?")
                && intentar(() -> ctx.cines().eliminar(c.getId()))) {
            nuevoCine();
            cargar(0);
        }
    }

    @FXML
    private void agregarSala() {
        POJOCine cine = tablaCines.getSelectionModel().getSelectedItem();
        if (cine == null) {
            mostrarError("Primero selecciona el cine.");
            return;
        }
        try {
            int numero = entero(txtNumero, "Número de sala");
            int filas = entero(txtFilas, "Filas");
            int columnas = entero(txtColumnas, "Columnas");
            double cargo = txtCargo.isDisabled() || txtCargo.getText().isBlank() ? 0 : Double.parseDouble(txtCargo.getText());
            POJOSala sala = ctx.cines().crearSala(cmbTipo.getValue(), 0, cine.getId(), numero, filas, columnas, cargo);
            ctx.cines().agregarSala(cine, sala);
            txtNumero.clear();
            cargar(cine.getId());
        } catch (NumberFormatException e) {
            mostrarError("Revisa los números de la sala.");
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    @FXML
    private void eliminarSala() {
        POJOCine cine = tablaCines.getSelectionModel().getSelectedItem();
        POJOSala s = tablaSalas.getSelectionModel().getSelectedItem();
        if (s != null && confirmar("¿Eliminar la " + s + " y sus " + s.getCapacidad() + " asientos?")
                && intentar(() -> ctx.cines().eliminarSala(s.getId()))) {
            cargar(cine == null ? 0 : cine.getId());
        }
    }

    private static int entero(TextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new cinemex.base.ExcepcionCine("\"" + nombre + "\" debe ser un número entero.");
        }
    }
}
