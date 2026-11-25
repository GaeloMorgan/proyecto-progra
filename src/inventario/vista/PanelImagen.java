package inventario.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * Panel que muestra la imagen del producto seleccionado, ajustada al espacio disponible
 * sin deformarla. Si el producto no tiene imagen muestra un mensaje.
 */
public class PanelImagen extends JPanel {

    private BufferedImage imagen;
    private String mensaje = "";

    /** Crea el panel vacío. */
    public PanelImagen() {
        setBorder(BorderFactory.createTitledBorder("Imagen del producto"));
        setPreferredSize(new Dimension(210, 220));
    }

    /**
     * Muestra una imagen del disco.
     *
     * @param archivo archivo de imagen, o null si el producto no tiene
     * @param mensajeSinImagen texto que se muestra cuando no hay imagen
     */
    public void mostrar(File archivo, String mensajeSinImagen) {
        imagen = null;
        if (archivo != null) {
            try {
                imagen = ImageIO.read(archivo);
            } catch (IOException e) {
                imagen = null;
            }
        }
        mensaje = mensajeSinImagen;
        repaint();
    }

    /**
     * Quita la imagen y muestra un mensaje.
     *
     * @param mensajeSinImagen texto a mostrar
     */
    public void limpiar(String mensajeSinImagen) {
        mostrar(null, mensajeSinImagen);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Insets margen = getInsets();
        int x = margen.left + 6;
        int y = margen.top + 6;
        int ancho = getWidth() - margen.left - margen.right - 12;
        int alto = getHeight() - margen.top - margen.bottom - 12;
        if (ancho <= 0 || alto <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (imagen != null) {
            double escala = Math.min(ancho / (double) imagen.getWidth(), alto / (double) imagen.getHeight());
            int w = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
            int h = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
            g2.drawImage(imagen, x + (ancho - w) / 2, y + (alto - h) / 2, w, h, null);
        } else {
            g2.setColor(Color.GRAY);
            String texto = mensaje;
            // Si el mensaje no cabe se acorta para que no se corte por los lados.
            while (texto.length() > 3 && g2.getFontMetrics().stringWidth(texto) > ancho) {
                texto = texto.substring(0, texto.length() - 2) + "…";
            }
            int anchoTexto = g2.getFontMetrics().stringWidth(texto);
            g2.drawString(texto, x + Math.max(0, (ancho - anchoTexto) / 2), y + alto / 2);
        }
        g2.dispose();
    }
}
