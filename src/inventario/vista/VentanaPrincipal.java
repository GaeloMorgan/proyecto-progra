package inventario.vista;

import inventario.modelo.Producto;
import inventario.servicio.InventarioService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal del sistema de inventario.
 *
 * <p>Muestra la tabla de productos, los botones de operaciones (agregar, editar, eliminar,
 * guardar), las tres formas de mostrar el inventario, la búsqueda por clave o nombre y el
 * historial de funciones. Toda la lógica se delega en {@link InventarioService}.</p>
 */
public class VentanaPrincipal extends JFrame {

    private static final String TITULO = "Inventario de productos";
    private static final Color COLOR_ENCABEZADO = new Color(0x1F3A5F);
    private static final Color COLOR_PENDIENTE = new Color(0xB35900);
    private static final Color COLOR_GUARDADO = new Color(0x2E7D32);

    /** Formas de mostrar el inventario. */
    private enum Vista { INVENTARIO, PRECIO, NOMBRE }

    private final InventarioService servicio;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private final PanelHistorial panelHistorial = new PanelHistorial();
    private final PanelImagen panelImagen = new PanelImagen();

    private final JLabel lblVista = new JLabel();
    private final JLabel lblConteo = new JLabel();
    private final JLabel lblGuardado = new JLabel();
    private final JComboBox<String> cmbCriterio = new JComboBox<String>(new String[] {"Clave", "Nombre"});
    private final JTextField txtBusqueda = new JTextField(14);
    private final JButton btnGuardar = new JButton("Guardar cambios");

    private Vista vistaActual = Vista.INVENTARIO;

    /**
     * Crea y arma la ventana principal.
     *
     * @param servicio servicio del inventario ya cargado desde el archivo
     */
    public VentanaPrincipal(InventarioService servicio) {
        super(TITULO);
        this.servicio = servicio;

        modeloTabla = new DefaultTableModel(new Object[] {"Clave", "Nombre", "Cantidad", "Precio"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 2 ? Integer.class : columna == 3 ? Double.class : String.class;
            }
        };
        tabla = crearTabla();
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarImagen();
            }
        });

        setLayout(new BorderLayout());
        add(crearParteSuperior(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(panelHistorialConMargen(), BorderLayout.EAST);
        add(crearBarraEstado(), BorderLayout.SOUTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });
        setMinimumSize(new Dimension(900, 560));
        setSize(1040, 680);
        setLocationRelativeTo(null);

        mostrarVista(Vista.INVENTARIO, true);
    }

    // ------------------------------------------------------------------ construcción

    private JTable crearTabla() {
        JTable t = new JTable(modeloTabla);
        t.setRowHeight(26);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(false); // el orden lo deciden QuickSort y MergeSort, no la tabla
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
        t.getColumnModel().getColumn(0).setPreferredWidth(90);
        t.getColumnModel().getColumn(1).setPreferredWidth(320);
        t.getColumnModel().getColumn(2).setPreferredWidth(90);
        t.getColumnModel().getColumn(3).setPreferredWidth(110);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        t.getColumnModel().getColumn(2).setCellRenderer(derecha);
        t.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(valor instanceof Double ? String.format(Locale.US, "$%,.2f", (Double) valor) : "");
            }
        });

        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && t.rowAtPoint(e.getPoint()) >= 0) {
                    editarProducto();
                }
            }
        });
        return t;
    }

    private JPanel crearParteSuperior() {
        JLabel titulo = new JLabel("  " + TITULO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(Color.WHITE);
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(COLOR_ENCABEZADO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        encabezado.add(titulo, BorderLayout.WEST);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnVender = new JButton("Vender");
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD));
        btnAgregar.addActionListener(e -> agregarProducto());
        btnEditar.addActionListener(e -> editarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnVender.addActionListener(e -> venderProducto());
        btnGuardar.addActionListener(e -> guardarCambios());

        JPanel operaciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        operaciones.add(btnAgregar);
        operaciones.add(btnEditar);
        operaciones.add(btnEliminar);
        operaciones.add(btnVender);
        operaciones.add(Box.createHorizontalStrut(16));
        operaciones.add(btnGuardar);

        JButton btnInventario = new JButton("Inventario");
        JButton btnPrecio = new JButton("Por precio");
        JButton btnNombre = new JButton("Por nombre");
        btnInventario.setToolTipText("Recorrido inorden (por clave)");
        btnPrecio.setToolTipText("Recorrido inorden + QuickSort");
        btnNombre.setToolTipText("Recorrido preorden + MergeSort");
        btnInventario.addActionListener(e -> mostrarVista(Vista.INVENTARIO, true));
        btnPrecio.addActionListener(e -> mostrarVista(Vista.PRECIO, true));
        btnNombre.addActionListener(e -> mostrarVista(Vista.NOMBRE, true));

        JPanel vistas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        vistas.add(new JLabel("Mostrar:"));
        vistas.add(btnInventario);
        vistas.add(btnPrecio);
        vistas.add(btnNombre);

        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiarBusqueda());
        txtBusqueda.addActionListener(e -> buscar());

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        busqueda.add(new JLabel("Buscar por:"));
        busqueda.add(cmbCriterio);
        busqueda.add(txtBusqueda);
        busqueda.add(btnBuscar);
        busqueda.add(btnLimpiar);

        JPanel fila2 = new JPanel(new BorderLayout());
        fila2.add(vistas, BorderLayout.WEST);
        fila2.add(busqueda, BorderLayout.EAST);

        JPanel herramientas = new JPanel();
        herramientas.setLayout(new BoxLayout(herramientas, BoxLayout.Y_AXIS));
        herramientas.add(operaciones);
        herramientas.add(fila2);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(herramientas, BorderLayout.CENTER);
        return superior;
    }

    private JPanel crearCentro() {
        JScrollPane scroll = new JScrollPane(tabla);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
        centro.add(scroll, BorderLayout.CENTER);
        return centro;
    }

    private JPanel panelHistorialConMargen() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
        contenedor.add(panelImagen, BorderLayout.NORTH);
        contenedor.add(panelHistorial, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearBarraEstado() {
        lblGuardado.setFont(lblGuardado.getFont().deriveFont(Font.BOLD));
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.add(lblConteo);
        derecha.add(lblGuardado);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 8)));
        barra.add(lblVista, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    // ------------------------------------------------------------------ mostrar datos

    /**
     * Muestra el inventario en la forma pedida.
     *
     * @param vista forma de mostrar los productos
     * @param registrar true para anotar la función en el historial
     */
    private void mostrarVista(Vista vista, boolean registrar) {
        List<Producto> lista;
        String descripcion;
        switch (vista) {
            case PRECIO:
                lista = registrar ? servicio.mostrarPorPrecio() : servicio.obtenerPorPrecio();
                descripcion = "Ordenado por precio (recorrido inorden + QuickSort)";
                break;
            case NOMBRE:
                lista = registrar ? servicio.mostrarPorNombre() : servicio.obtenerPorNombre();
                descripcion = "Ordenado por nombre (recorrido preorden + MergeSort)";
                break;
            default:
                lista = registrar ? servicio.mostrarInventario() : servicio.obtenerInventario();
                descripcion = "Inventario completo (recorrido inorden, por clave)";
                break;
        }
        vistaActual = vista;
        llenarTabla(lista, descripcion);
        actualizarEstado();
    }

    private void llenarTabla(List<Producto> productos, String descripcion) {
        modeloTabla.setRowCount(0);
        for (Producto p : productos) {
            modeloTabla.addRow(new Object[] {p.getClave(), p.getNombre(), p.getCantidad(), p.getPrecio()});
        }
        lblVista.setText(descripcion);
        lblConteo.setText(productos.size() + (productos.size() == 1 ? " producto" : " productos"));
    }

    /** Actualiza el historial, el indicador de cambios pendientes y el título de la ventana. */
    private void actualizarEstado() {
        panelHistorial.actualizar(servicio.getHistorial());
        boolean pendientes = servicio.hayCambiosPendientes();
        lblGuardado.setText(pendientes ? "Cambios sin guardar" : "Todo guardado");
        lblGuardado.setForeground(pendientes ? COLOR_PENDIENTE : COLOR_GUARDADO);
        btnGuardar.setEnabled(pendientes);
        setTitle(pendientes ? TITULO + " *" : TITULO);
    }

    private void seleccionarClave(String clave) {
        for (int fila = 0; fila < modeloTabla.getRowCount(); fila++) {
            if (clave.equals(modeloTabla.getValueAt(fila, 0))) {
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                break;
            }
        }
        actualizarImagen();
    }

    /** Muestra en el panel lateral la imagen del producto seleccionado. */
    private void actualizarImagen() {
        String clave = claveSeleccionada();
        if (clave == null) {
            panelImagen.limpiar("Selecciona un producto");
        } else {
            panelImagen.mostrar(servicio.obtenerImagen(clave), "Sin imagen");
        }
    }

    private String claveSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : (String) modeloTabla.getValueAt(fila, 0);
    }

    // ------------------------------------------------------------------ acciones

    private void agregarProducto() {
        FormularioProducto formulario = new FormularioProducto(this, servicio, null);
        formulario.setVisible(true);
        if (formulario.fueAceptado()) {
            mostrarVista(vistaActual, false);
            seleccionarClave(formulario.getClaveResultado());
        }
    }

    private void editarProducto() {
        String clave = claveSeleccionada();
        if (clave == null) {
            aviso("Selecciona primero un producto de la tabla.");
            return;
        }
        Producto producto = servicio.obtenerProducto(clave);
        if (producto == null) {
            aviso("El producto seleccionado ya no existe.");
            mostrarVista(vistaActual, false);
            return;
        }
        FormularioProducto formulario = new FormularioProducto(this, servicio, producto);
        formulario.setVisible(true);
        if (formulario.fueAceptado()) {
            mostrarVista(vistaActual, false);
            seleccionarClave(clave);
        }
    }

    /** Abre el diálogo de venta del producto seleccionado. */
    private void venderProducto() {
        String clave = claveSeleccionada();
        if (clave == null) {
            aviso("Selecciona primero el producto que quieres vender.");
            return;
        }
        Producto producto = servicio.obtenerProducto(clave);
        if (producto == null) {
            aviso("El producto seleccionado ya no existe.");
            mostrarVista(vistaActual, false);
            return;
        }
        DialogoVenta dialogo = new DialogoVenta(this, servicio, producto);
        dialogo.setVisible(true);
        if (dialogo.fueAceptado()) {
            mostrarVista(vistaActual, false);
            seleccionarClave(clave);
            JOptionPane.showMessageDialog(this,
                    String.format(Locale.US, "Venta registrada. Total: $%,.2f", dialogo.getTotal()),
                    "Venta realizada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Pide la clave (ya escrita si hay una fila seleccionada) y el motivo, y elimina el producto. */
    private void eliminarProducto() {
        String seleccionada = claveSeleccionada();
        JTextField txtClave = new JTextField(seleccionada == null ? "" : seleccionada, 14);
        ((javax.swing.text.AbstractDocument) txtClave.getDocument())
                .setDocumentFilter(new FormularioProducto.FiltroNumerico(6, false));
        JTextField txtMotivo = new JTextField(22);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Clave del producto:"), c);
        c.gridx = 1;
        panel.add(txtClave, c);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(new JLabel("Motivo de eliminación:"), c);
        c.gridx = 1;
        panel.add(txtMotivo, c);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Eliminar producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }
        String clave = txtClave.getText().trim();
        Producto producto = servicio.obtenerProducto(clave);
        if (producto == null) {
            error(clave.length() == 6 ? "No existe un producto con la clave " + clave + "."
                    : "La clave debe tener exactamente 6 dígitos.");
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Eliminar \"" + producto.getNombre() + "\" (clave " + clave + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.eliminar(clave, txtMotivo.getText());
            mostrarVista(vistaActual, false);
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void buscar() {
        String texto = txtBusqueda.getText().trim();
        if (texto.isEmpty()) {
            aviso("Escribe la clave o el nombre que quieres buscar.");
            return;
        }
        List<Producto> resultado;
        if (cmbCriterio.getSelectedIndex() == 0) {
            try {
                Producto encontrado = servicio.buscarPorClave(texto);
                resultado = encontrado == null ? Collections.<Producto>emptyList()
                        : Collections.singletonList(encontrado);
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
                return;
            }
        } else {
            resultado = servicio.buscarPorNombre(texto);
        }
        llenarTabla(resultado, "Resultados de la búsqueda por " + cmbCriterio.getSelectedItem().toString().toLowerCase()
                + ": \"" + texto + "\"");
        actualizarEstado();
        if (resultado.isEmpty()) {
            aviso("No se encontraron productos.");
        } else {
            tabla.setRowSelectionInterval(0, 0);
        }
    }

    private void limpiarBusqueda() {
        txtBusqueda.setText("");
        mostrarVista(vistaActual, false);
    }

    private void guardarCambios() {
        try {
            servicio.guardarCambios();
            actualizarEstado();
            JOptionPane.showMessageDialog(this, "Los cambios se guardaron correctamente.",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            error("No se pudieron guardar los cambios:\n" + ex.getMessage());
        }
    }

    /** Cierra la aplicación; si hay cambios pendientes pregunta si se desean guardar. */
    private void salir() {
        if (servicio.hayCambiosPendientes()) {
            Object[] opciones = {"Guardar y salir", "Salir sin guardar", "Cancelar"};
            int respuesta = JOptionPane.showOptionDialog(this,
                    "Hay cambios sin guardar. ¿Quieres guardarlos antes de salir?",
                    "Cambios pendientes", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, opciones, opciones[0]);
            if (respuesta == 0) {
                try {
                    servicio.guardarCambios();
                } catch (IOException ex) {
                    error("No se pudieron guardar los cambios:\n" + ex.getMessage());
                    return;
                }
            } else if (respuesta != 1) {
                return;
            }
        }
        dispose();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Aviso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
