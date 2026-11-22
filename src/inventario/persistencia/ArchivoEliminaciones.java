package inventario.persistencia;

import inventario.modelo.Producto;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Esta clase guarda en un archivo de texto los productos que se eliminan,
 * junto con el motivo y la fecha.
 * Cada línea tiene el formato: clave,nombre,cantidad,precio,fecha,motivo
 *
 * @author lucaschivis
 */
public class ArchivoEliminaciones {

    private String ruta;

    /**
     * Crea el manejador del archivo con la ruta normal del proyecto.
     */
    public ArchivoEliminaciones() {
        this.ruta = "datos/eliminaciones.txt";
    }

    /**
     * Crea el manejador del archivo con una ruta distinta (sirve para pruebas).
     * @param ruta ruta del archivo de eliminaciones
     */
    public ArchivoEliminaciones(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Agrega al final del archivo el producto eliminado, el motivo y la fecha de hoy.
     * Las eliminaciones anteriores se conservan.
     * @param producto producto que se eliminó
     * @param motivo razón por la que se eliminó (ya validada, puede llevar comas)
     * @return verdadero si se guardó bien, falso si hubo un error
     */
    public boolean registrarEliminacion(Producto producto, String motivo) {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        LocalDate fecha = LocalDate.now();
        // El true del FileWriter hace que agregue al final sin borrar lo anterior
        try (PrintWriter escritor = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8, true))) {
            escritor.println(producto.getClave() + "," + producto.getNombre() + ","
                    + producto.getCantidad() + "," + producto.getPrecio() + ","
                    + fecha + "," + motivo);
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar la eliminación: " + e.getMessage());
            return false;
        }
    }
}