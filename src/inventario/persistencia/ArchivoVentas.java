package inventario.persistencia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Guarda en un archivo de texto cada venta realizada.
 * Cada línea tiene el formato: clave,nombre,cantidad,precioUnitario,total,fecha
 * (la fecha se escribe como aaaa-mm-dd hh:mm).
 */
public class ArchivoVentas {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String ruta;

    /** Crea el manejador del archivo con la ruta normal del proyecto. */
    public ArchivoVentas() {
        this.ruta = "datos/ventas.txt";
    }

    /**
     * Crea el manejador del archivo con una ruta distinta (sirve para pruebas).
     *
     * @param ruta ruta del archivo de ventas
     */
    public ArchivoVentas(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Agrega al final del archivo una venta. Las ventas anteriores se conservan.
     *
     * @param clave clave del producto vendido
     * @param nombre nombre del producto
     * @param cantidad unidades vendidas
     * @param precioUnitario precio por unidad al momento de la venta
     * @param fecha fecha y hora en que se hizo la venta
     * @return verdadero si se guardó bien, falso si hubo un error
     */
    public boolean registrarVenta(String clave, String nombre, int cantidad, double precioUnitario,
            LocalDateTime fecha) {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        double total = Math.round(cantidad * precioUnitario * 100.0) / 100.0;
        try (PrintWriter escritor = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8, true))) {
            escritor.println(clave + "," + nombre + "," + cantidad + ","
                    + String.format(Locale.US, "%.2f", precioUnitario) + ","
                    + String.format(Locale.US, "%.2f", total) + "," + fecha.format(FORMATO_FECHA));
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar la venta: " + e.getMessage());
            return false;
        }
    }
}
