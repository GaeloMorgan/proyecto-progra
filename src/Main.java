import inventario.servicio.InventarioService;
import inventario.vista.VentanaPrincipal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class Main {

    
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

    
    private static void configurarApariencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
           
        }
        UIManager.put("OptionPane.okButtonText", "Aceptar");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sí");
        UIManager.put("OptionPane.noButtonText", "No");
    }
}
