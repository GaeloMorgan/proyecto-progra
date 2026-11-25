import inventario.servicio.InventarioService;
import inventario.vista.VentanaPrincipal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada del sistema de inventario. Carga el archivo de productos al árbol
 * y abre la ventana principal.
 */
public class Main {

    /**
     * Inicia la aplicación.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> iniciar());
    }

    private static void iniciar() {
        configurarApariencia();
        InventarioService servicio = new InventarioService();
        int omitidas = servicio.cargarInventario();
        if (omitidas > 0) {
            JOptionPane.showMessageDialog(null, omitidas + " producto(s) del archivo se omitieron "
                    + "por tener una clave repetida.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
        new VentanaPrincipal(servicio).setVisible(true);
    }

    /** Usa el estilo Nimbus y traduce los botones estándar de los cuadros de diálogo. */
    private static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Si no se puede aplicar Nimbus se usa el estilo predeterminado.
        }
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
    }
}
