package inventario.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Panel lateral que muestra el historial de funciones usadas, con la más reciente arriba.
 */
public class PanelHistorial extends JPanel {

    private final DefaultTableModel modelo;

    /** Crea el panel con una tabla de una sola columna llamada "Función". */
    public PanelHistorial() {
        super(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder("Historial"));
        setPreferredSize(new Dimension(210, 100));

        modelo = new DefaultTableModel(new Object[] {"Función"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFocusable(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));

        JLabel nota = new JLabel("Se borra al cerrar el programa", SwingConstants.CENTER);
        nota.setFont(nota.getFont().deriveFont(Font.ITALIC, 11f));
        nota.setForeground(Color.GRAY);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(nota, BorderLayout.SOUTH);
    }

    /**
     * Reemplaza el contenido del panel.
     *
     * @param acciones funciones realizadas, con la más reciente al inicio
     */
    public void actualizar(List<String> acciones) {
        modelo.setRowCount(0);
        for (String accion : acciones) {
            modelo.addRow(new Object[] {accion});
        }
    }
}
