package inventario.vista;

import inventario.modelo.Producto;
import inventario.servicio.InventarioService;
import inventario.validaciones.ValidadorProducto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Ventana de diálogo para agregar o editar un producto, incluida su imagen opcional.
 * Si se crea con un producto, funciona en modo edición y la clave queda bloqueada.
 */
public class FormularioProducto extends JDialog {

    private final InventarioService servicio;
    private final Producto productoEditado;
    private final ValidadorProducto validador = new ValidadorProducto();

    private final JTextField txtClave = new JTextField(14);
    private final JTextField txtNombre = new JTextField(14);
    private final JTextField txtCantidad = new JTextField(14);
    private final JTextField txtPrecio = new JTextField(14);
    private final JLabel lblError = new JLabel(" ");
    private final JLabel lblImagen = new JLabel("Sin imagen", SwingConstants.CENTER);

    private File imagenElegida;
    private boolean quitarImagen;
    private boolean aceptado;
    private String claveResultado;

    /**
     * Crea el formulario.
     *
     * @param padre ventana sobre la que se centra
     * @param servicio servicio del inventario
     * @param productoEditado producto a modificar, o null para agregar uno nuevo
     */
    public FormularioProducto(Frame padre, InventarioService servicio, Producto productoEditado) {
        super(padre, productoEditado == null ? "Agregar producto" : "Editar producto", true);
        this.servicio = servicio;
        this.productoEditado = productoEditado;
        construirInterfaz();
        if (productoEditado != null) {
            cargarDatos();
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private void construirInterfaz() {
        ((AbstractDocument) txtClave.getDocument()).setDocumentFilter(new FiltroNumerico(6, false));
        ((AbstractDocument) txtCantidad.getDocument()).setDocumentFilter(new FiltroNumerico(9, false));
        ((AbstractDocument) txtPrecio.getDocument()).setDocumentFilter(new FiltroNumerico(12, true));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBorder(BorderFactory.createEmptyBorder(14, 16, 4, 16));
        agregarFila(campos, 0, "Clave (6 dígitos):", txtClave);
        agregarFila(campos, 1, "Nombre:", txtNombre);
        agregarFila(campos, 2, "Cantidad:", txtCantidad);
        agregarFila(campos, 3, "Precio ($):", txtPrecio);
        agregarFila(campos, 4, "Imagen:", crearSelectorImagen());

        lblError.setForeground(new Color(0xB00020));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        c.insets = new Insets(8, 0, 0, 0);
        c.anchor = GridBagConstraints.WEST;
        campos.add(lblError, c);

        if (productoEditado != null) {
            txtClave.setEditable(false);
            txtClave.setBackground(new Color(0xE6E6E6));
            txtClave.setToolTipText("La clave no se puede modificar");
        }

        JButton btnGuardar = new JButton(productoEditado == null ? "Agregar" : "Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> aceptar());
        btnCancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        getRootPane().setDefaultButton(btnGuardar);
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

    /** Vista previa de la imagen con los botones para elegirla o quitarla. */
    private JPanel crearSelectorImagen() {
        lblImagen.setPreferredSize(new Dimension(96, 96));
        lblImagen.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblImagen.setForeground(Color.GRAY);

        JButton btnElegir = new JButton("Elegir...");
        JButton btnQuitar = new JButton("Quitar");
        btnElegir.addActionListener(e -> elegirImagen());
        btnQuitar.addActionListener(e -> {
            imagenElegida = null;
            quitarImagen = true;
            mostrarVistaPrevia(null);
        });
        JPanel botones = new JPanel();
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        botones.add(btnElegir);
        botones.add(javax.swing.Box.createVerticalStrut(6));
        botones.add(btnQuitar);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selector.add(lblImagen);
        selector.add(javax.swing.Box.createHorizontalStrut(10));
        selector.add(botones);
        return selector;
    }

    private void elegirImagen() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Elegir imagen del producto");
        selector.setApproveButtonText("Elegir");
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes (png, jpg, gif, bmp)",
                "png", "jpg", "jpeg", "gif", "bmp"));
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File archivo = selector.getSelectedFile();
        if (!mostrarVistaPrevia(archivo)) {
            lblError.setText("El archivo elegido no es una imagen válida.");
            pack();
            return;
        }
        lblError.setText(" ");
        imagenElegida = archivo;
        quitarImagen = false;
    }

    /**
     * Muestra la imagen reducida en el cuadro de vista previa.
     *
     * @param archivo imagen a mostrar, o null para dejar el cuadro vacío
     * @return false si el archivo no se pudo leer como imagen
     */
    private boolean mostrarVistaPrevia(File archivo) {
        if (archivo == null) {
            lblImagen.setIcon(null);
            lblImagen.setText("Sin imagen");
            return true;
        }
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                return false;
            }
            double escala = Math.min(90.0 / imagen.getWidth(), 90.0 / imagen.getHeight());
            int ancho = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
            lblImagen.setText("");
            lblImagen.setIcon(new ImageIcon(imagen.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH)));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 0, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.insets = new Insets(4, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    private void cargarDatos() {
        txtClave.setText(productoEditado.getClave());
        txtNombre.setText(productoEditado.getNombre());
        txtCantidad.setText(String.valueOf(productoEditado.getCantidad()));
        txtPrecio.setText(String.valueOf(productoEditado.getPrecio()));
        mostrarVistaPrevia(servicio.obtenerImagen(productoEditado.getClave()));
    }

    /** Valida campo por campo y, si todo es correcto, pide al servicio agregar o editar. */
    private void aceptar() {
        String clave = txtClave.getText().trim();
        String nombre = txtNombre.getText().trim();
        String cantidad = txtCantidad.getText().trim();
        String precio = txtPrecio.getText().trim();

        if (productoEditado == null && !validador.validarClave(clave)) {
            mostrarError("La clave debe tener exactamente 6 dígitos.", txtClave);
        } else if (!validador.validarNombre(nombre)) {
            mostrarError("El nombre no puede estar vacío ni contener comas.", txtNombre);
        } else if (!validador.validarCantidad(cantidad)) {
            mostrarError("La cantidad debe ser un entero igual o mayor que 0.", txtCantidad);
        } else if (!validador.validarPrecio(precio)) {
            mostrarError("El precio debe ser un número mayor que 0.", txtPrecio);
        } else {
            try {
                if (productoEditado == null) {
                    servicio.agregar(clave, nombre, cantidad, precio, imagenElegida);
                } else {
                    servicio.editar(productoEditado.getClave(), nombre, cantidad, precio,
                            imagenElegida, quitarImagen);
                }
                aceptado = true;
                claveResultado = clave;
                dispose();
            } catch (IllegalArgumentException ex) {
                mostrarError(ex.getMessage(), productoEditado == null ? txtClave : txtNombre);
            }
        }
    }

    private void mostrarError(String mensaje, JTextField campo) {
        lblError.setText(mensaje);
        pack();
        campo.requestFocusInWindow();
        campo.selectAll();
    }

    /**
     * Indica si el usuario terminó la operación con éxito.
     *
     * @return true si se agregó o editó el producto
     */
    public boolean fueAceptado() {
        return aceptado;
    }

    /**
     * Clave del producto agregado o editado.
     *
     * @return clave de seis dígitos, o null si se canceló
     */
    public String getClaveResultado() {
        return claveResultado;
    }

    /**
     * Filtro que solo deja escribir dígitos (y opcionalmente un punto decimal)
     * con una longitud máxima.
     */
    static class FiltroNumerico extends DocumentFilter {
        private final int maximo;
        private final boolean permitirPunto;

        FiltroNumerico(int maximo, boolean permitirPunto) {
            this.maximo = maximo;
            this.permitirPunto = permitirPunto;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String texto, AttributeSet atributos)
                throws BadLocationException {
            replace(fb, offset, 0, texto, atributos);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int longitud, String texto, AttributeSet atributos)
                throws BadLocationException {
            String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
            String nuevo = actual.substring(0, offset) + (texto == null ? "" : texto)
                    + actual.substring(offset + longitud);
            if (esValido(nuevo)) {
                super.replace(fb, offset, longitud, texto, atributos);
            }
        }

        private boolean esValido(String texto) {
            if (texto.length() > maximo) {
                return false;
            }
            int puntos = 0;
            for (int i = 0; i < texto.length(); i++) {
                char c = texto.charAt(i);
                if (c == '.' && permitirPunto) {
                    puntos++;
                } else if (c < '0' || c > '9') {
                    return false;
                }
            }
            return puntos <= 1;
        }
    }
}
