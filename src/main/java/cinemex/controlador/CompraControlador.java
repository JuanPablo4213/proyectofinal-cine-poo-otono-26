package cinemex.controlador;

import cinemex.base.ExcepcionCine;
import cinemex.modelo.cines.POJOAsiento;
import cinemex.modelo.cines.POJOSala;
import cinemex.modelo.compras.IMetodoPago;
import cinemex.modelo.compras.POJOBoleto;
import cinemex.modelo.compras.POJOCompra;
import cinemex.modelo.compras.PagoPayPal;
import cinemex.modelo.compras.PagoTarjeta;
import cinemex.modelo.compras.TipoBoleto;
import cinemex.modelo.funciones.POJOFuncion;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Compra de boletos (diagramas de actividades 2 y de secuencia):
 * mapa de asientos -> tipos de boleto -> pago -> ServicioCompras.confirmarCompra.
 */
public class CompraControlador extends ControladorBase {
    @FXML private StackPane contPoster;
    @FXML private Label lblTitulo;
    @FXML private Label lblFuncion;
    @FXML private GridPane mapa;
    @FXML private Label lblSeleccion;
    @FXML private Spinner<Integer> spAdulto;
    @FXML private Spinner<Integer> spNino;
    @FXML private Spinner<Integer> spMayor;
    @FXML private Label lblPrecioAdulto;
    @FXML private Label lblPrecioNino;
    @FXML private Label lblPrecioMayor;
    @FXML private Label lblTotal;
    @FXML private Label lblAviso;
    @FXML private RadioButton rbTarjeta;
    @FXML private RadioButton rbPayPal;
    @FXML private VBox panelTarjeta;
    @FXML private VBox panelPayPal;
    @FXML private TextField txtTarjeta;
    @FXML private TextField txtCvv;
    @FXML private TextField txtCorreoPayPal;
    @FXML private Button btnPagar;

    private int _funcionId;
    private POJOFuncion _funcion;
    private final List<POJOAsiento> _seleccionados = new ArrayList<>();
    private final List<ToggleButton> _botones = new ArrayList<>();

    public void setFuncionId(int funcionId) {
        this._funcionId = funcionId;
    }

    @FXML
    private void initialize() {
        ToggleGroup pago = new ToggleGroup();
        rbTarjeta.setToggleGroup(pago);
        rbPayPal.setToggleGroup(pago);
        rbTarjeta.setSelected(true);
        pago.selectedToggleProperty().addListener((o, a, n) -> cambiarPago());
        for (Spinner<Integer> s : List.of(spAdulto, spNino, spMayor)) {
            s.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, POJOCompra.MAX_BOLETOS, 0));
            s.valueProperty().addListener((o, a, n) -> actualizarResumen());
        }
        cambiarPago();
    }

    @Override
    protected void alIniciar() {
        try {
            _funcion = ctx.funciones().buscarPorId(_funcionId);
        } catch (RuntimeException e) {
            mostrarError(e);
        }
        if (_funcion == null) {
            lblTitulo.setText("La función no existe");
            btnPagar.setDisable(true);
            return;
        }
        POJOSala sala = _funcion.getSala();
        contPoster.getChildren().setAll(Tarjetas.poster(_funcion.getPelicula(), 120, 180));
        lblTitulo.setText(_funcion.getPelicula().getTitulo());
        lblFuncion.setText(_funcion.getCine() + "  ·  " + sala + "  ·  " + Formatos.fecha(_funcion.getFechaHora())
                + " " + Formatos.hora(_funcion.getFechaHora()) + "  ·  " + _funcion.getIdioma());
        lblPrecioAdulto.setText(Formatos.dinero(sala.calcularPrecio(TipoBoleto.ADULTO)));
        lblPrecioNino.setText(Formatos.dinero(sala.calcularPrecio(TipoBoleto.NINO)));
        lblPrecioMayor.setText(Formatos.dinero(sala.calcularPrecio(TipoBoleto.ADULTO_MAYOR)));
        dibujarMapa();
        actualizarResumen();
    }

    /** Un botón por asiento: filas = letras, columnas = números. Los vendidos salen deshabilitados. */
    private void dibujarMapa() {
        mapa.getChildren().clear();
        _botones.clear();
        _seleccionados.clear();
        POJOSala sala = _funcion.getSala();
        for (int f = 0; f < sala.getFilas(); f++) {
            char fila = (char) ('A' + f);
            Label etiqueta = new Label(String.valueOf(fila));
            etiqueta.getStyleClass().add("texto-suave");
            mapa.add(etiqueta, 0, f);
            for (int c = 1; c <= sala.getColumnas(); c++) {
                POJOAsiento a = sala.buscarAsiento(fila, c);
                ToggleButton b = new ToggleButton(String.valueOf(c));
                b.getStyleClass().setAll("asiento");
                if (a == null || !_funcion.estaDisponible(a)) {
                    b.setDisable(true);
                    b.getStyleClass().add("asiento-ocupado");
                } else {
                    b.setOnAction(e -> alternar(a, b));
                }
                // un pasillo a la mitad, como en las salas reales
                int columna = c > sala.getColumnas() / 2 ? c + 1 : c;
                mapa.add(b, columna, f);
                _botones.add(b);
            }
        }
        mapa.setAlignment(Pos.CENTER);
    }

    private void alternar(POJOAsiento a, ToggleButton b) {
        if (b.isSelected()) {
            if (_seleccionados.size() >= POJOCompra.MAX_BOLETOS) {
                b.setSelected(false);
                lblAviso.setText("Máximo " + POJOCompra.MAX_BOLETOS + " boletos por compra.");
                return;
            }
            _seleccionados.add(a);
            // por comodidad: cada asiento nuevo cuenta como adulto
            spAdulto.getValueFactory().setValue(Math.min(POJOCompra.MAX_BOLETOS,
                    _seleccionados.size() - spNino.getValue() - spMayor.getValue()));
        } else {
            _seleccionados.remove(a);
            ajustarTipos();
        }
        actualizarResumen();
    }

    /** Si quitan asientos, se quitan boletos (primero adultos) para que cuadre. */
    private void ajustarTipos() {
        int sobran = spAdulto.getValue() + spNino.getValue() + spMayor.getValue() - _seleccionados.size();
        for (Spinner<Integer> s : List.of(spAdulto, spNino, spMayor)) {
            int quitar = Math.min(sobran, s.getValue());
            s.getValueFactory().setValue(s.getValue() - quitar);
            sobran -= quitar;
        }
    }

    private void actualizarResumen() {
        if (_funcion == null) {
            return;
        }
        POJOSala sala = _funcion.getSala();
        lblSeleccion.setText(_seleccionados.isEmpty() ? "Elige tus asientos en el mapa"
                : "Asientos: " + String.join(", ", _seleccionados.stream().map(POJOAsiento::getCodigo).toList()));
        double total = spAdulto.getValue() * sala.calcularPrecio(TipoBoleto.ADULTO)
                + spNino.getValue() * sala.calcularPrecio(TipoBoleto.NINO)
                + spMayor.getValue() * sala.calcularPrecio(TipoBoleto.ADULTO_MAYOR);
        lblTotal.setText("Total: " + Formatos.dinero(total));
        int boletos = spAdulto.getValue() + spNino.getValue() + spMayor.getValue();
        boolean cuadra = boletos == _seleccionados.size() && boletos > 0;
        lblAviso.setText(_seleccionados.isEmpty() || cuadra ? ""
                : "Tienes " + _seleccionados.size() + " asientos y " + boletos + " boletos: deben ser iguales.");
        btnPagar.setDisable(!cuadra);
    }

    private void cambiarPago() {
        boolean tarjeta = rbTarjeta.isSelected();
        panelTarjeta.setVisible(tarjeta);
        panelTarjeta.setManaged(tarjeta);
        panelPayPal.setVisible(!tarjeta);
        panelPayPal.setManaged(!tarjeta);
    }

    /** Arma la compra con ServicioCompras y la confirma (todo o nada). */
    @FXML
    private void pagar() {
        try {
            POJOCompra compra = ctx.compras().iniciarCompra(ctx.usuarios().getClienteActual(), _funcion);
            List<TipoBoleto> tipos = new ArrayList<>();
            agregarTipos(tipos, TipoBoleto.ADULTO, spAdulto.getValue());
            agregarTipos(tipos, TipoBoleto.NINO, spNino.getValue());
            agregarTipos(tipos, TipoBoleto.ADULTO_MAYOR, spMayor.getValue());
            for (int i = 0; i < _seleccionados.size(); i++) {
                ctx.compras().agregarBoleto(compra, _seleccionados.get(i), tipos.get(i));
            }
            IMetodoPago metodo = rbTarjeta.isSelected()
                    ? new PagoTarjeta(txtTarjeta.getText(), txtCvv.getText())
                    : new PagoPayPal(txtCorreoPayPal.getText());
            ctx.compras().confirmarCompra(compra, metodo);
            mostrarConfirmacion(compra);
            ctx.navegador().ir("mis-compras");
        } catch (ExcepcionCine e) {
            mostrarError(e);
            // si otro cliente ganó un asiento, se vuelve a dibujar el mapa con lo ocupado
            ctx.funciones().cargarOcupados(_funcion);
            dibujarMapa();
            actualizarResumen();
        }
    }

    private static void agregarTipos(List<TipoBoleto> lista, TipoBoleto tipo, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            lista.add(tipo);
        }
    }

    private void mostrarConfirmacion(POJOCompra compra) {
        StringBuilder sb = new StringBuilder();
        sb.append(_funcion.getPelicula().getTitulo()).append('\n')
          .append(_funcion.getCine()).append(" · ").append(_funcion.getSala()).append('\n')
          .append(Formatos.fecha(_funcion.getFechaHora())).append(" a las ").append(Formatos.hora(_funcion.getFechaHora()))
          .append("\n\n");
        for (POJOBoleto b : compra.getBoletos()) {
            sb.append("  ").append(b.getAsiento().getCodigo()).append("  ").append(b.getTipo())
              .append("  ").append(Formatos.dinero(b.getPrecio())).append('\n');
        }
        sb.append("\nTotal: ").append(Formatos.dinero(compra.calcularTotal()));
        if (compra.getComision() > 0) {
            sb.append("  (+ comisión ").append(Formatos.dinero(compra.getComision())).append(")");
        }
        sb.append("\nPagado con ").append(compra.getMetodo().getNombre()).append(" ").append(compra.getMetodo().getReferencia());
        mostrarInfo("¡Compra confirmada! Folio " + compra.getId(), sb.toString());
    }

    @FXML
    private void regresar() {
        ctx.navegador().regresar();
    }
}
