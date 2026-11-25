package inventario.vista;

import inventario.modelo.Producto;
import inventario.servicio.InventarioService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;

/**
 * Ventana para vender un producto: muestra sus datos, pide la cantidad y calcula el total.
 */
public class DialogoVenta extends JDialog {

    private final InventarioService servicio;
    private final Producto producto;
    private final JTextField txtCantidad = new JTextField("1", 8);
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblError = new JLabel(" ");

    private boolean aceptado;
    private double totalVenta;

    /**
     * Crea el diálogo de venta.
     *
     * @param padre ventana sobre la que se centra
     * @param servicio servicio del inventario
     * @param producto producto que se va a vender
     */
    public DialogoVenta(Frame padre, InventarioService servicio, Producto producto) {
        super(padre, "Vender producto", true);
        this.servicio = servicio;
        this.producto = producto;
        construirInterfaz();
        actualizarTotal();
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private void construirInterfaz() {
        ((AbstractDocument) txtCantidad.getDocument())
                .setDocumentFilter(new FormularioProducto.FiltroNumerico(9, false));
        txtCantidad.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarTotal();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarTotal();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarTotal();
            }
        });

        JLabel nombre = new JLabel(producto.getNombre());
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 15f));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 12);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        campos.add(nombre, c);
        c.gridwidth = 1;
        agregarFila(campos, c, 1, "Clave:", new JLabel(producto.getClave()));
        agregarFila(campos, c, 2, "Existencias:", new JLabel(String.valueOf(producto.getCantidad())));
        agregarFila(campos, c, 3, "Precio unitario:",
                new JLabel(String.format(Locale.US, "$%,.2f", producto.getPrecio())));
        agregarFila(campos, c, 4, "Cantidad a vender:", txtCantidad);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        agregarFila(campos, c, 5, "Total:", lblTotal);
        lblError.setForeground(new Color(0xB00020));
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        campos.add(lblError, c);

        JButton btnVender = new JButton("Vender");
        JButton btnCancelar = new JButton("Cancelar");
        btnVender.addActionListener(e -> vender());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botones.add(btnVender);
        botones.add(btnCancelar);

        getRootPane().setDefaultButton(btnVender);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancelar");
        getRootPane().getActionMap().put("cancelar", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setLayout(new BorderLayout());
        add(campos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void agregarFila(JPanel panel, GridBagConstraints c, int fila, String etiqueta, JComponent valor) {
        c.gridy = fila;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        panel.add(valor, c);
    }

    private void actualizarTotal() {
        String texto = txtCantidad.getText().trim();
        double total = texto.isEmpty() ? 0 : Long.parseLong(texto) * producto.getPrecio();
        lblTotal.setText(String.format(Locale.US, "$%,.2f", total));
    }

    private void vender() {
        try {
            totalVenta = servicio.vender(producto.getClave(), txtCantidad.getText());
            aceptado = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            lblError.setText(ex.getMessage());
            pack();
            txtCantidad.requestFocusInWindow();
            txtCantidad.selectAll();
        }
    }

    /**
     * Indica si la venta se realizó.
     *
     * @return true si se vendió el producto
     */
    public boolean fueAceptado() {
        return aceptado;
    }

    /**
     * Total cobrado en la venta.
     *
     * @return total de la venta, o 0 si se canceló
     */
    public double getTotal() {
        return aceptado ? totalVenta : 0;
    }
}
