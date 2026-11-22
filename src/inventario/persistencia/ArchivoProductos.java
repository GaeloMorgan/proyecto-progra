package inventario.persistencia;

import inventario.modelo.Producto;
import inventario.validaciones.ValidadorProducto;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Esta clase lee y guarda los productos en el archivo de texto.
 * Cada línea del archivo tiene el formato: clave|nombre|cantidad|precio
 *
 * @author lucaschivis
 */
public class ArchivoProductos {

    private String ruta;

    /**
     * Crea el manejador del archivo con la ruta normal del proyecto.
     */
    public ArchivoProductos() {
        this.ruta = "datos/productos.txt";
    }

    /**
     * Crea el manejador del archivo con una ruta distinta (sirve para pruebas).
     * @param ruta ruta del archivo de productos
     */
    public ArchivoProductos(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Lee el archivo y regresa los productos en una lista.
     * Las líneas con datos incorrectos se saltan. Si el archivo no existe
     * regresa una lista vacía.
     * @return lista con los productos leídos
     */
    public List<Producto> leerProductos() {
        List<Producto> productos = new ArrayList<Producto>();
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return productos;
        }
        // "UTF-8" para que los acentos se lean igual en cualquier computadora
        try (Scanner lector = new Scanner(archivo, "UTF-8")) {
            while (lector.hasNextLine()) {
                Producto producto = convertirLinea(lector.nextLine());
                if (producto != null) {
                    productos.add(producto);
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de productos: " + e.getMessage());
        }
        return productos;
    }

    /**
     * Guarda la lista de productos en el archivo, una línea por producto.
     * Reescribe el archivo completo.
     * @param productos lista de productos que se va a guardar
     * @return verdadero si se guardó bien, falso si hubo un error
     */
    public boolean guardarProductos(List<Producto> productos) {
        File archivo = new File(ruta);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        try (PrintWriter escritor = new PrintWriter(archivo, "UTF-8")) {
            for (Producto producto : productos) {
                escritor.println(producto.getClave() + "|" + producto.getNombre() + "|"
                        + producto.getCantidad() + "|" + producto.getPrecio());
            }
            return true;
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo de productos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convierte una línea del archivo en un producto.
     * @param linea texto con el formato clave|nombre|cantidad|precio
     * @return el producto, o null si la línea está vacía o tiene datos incorrectos
     */
    private Producto convertirLinea(String linea) {
        if (linea.trim().isEmpty()) {
            return null;
        }
        // Corta la línea en cuatro datos: clave, nombre, cantidad y precio
        String[] datos = linea.split("\\|");
        if (datos.length != 4) {
            return null;
        }
        ValidadorProducto validador = new ValidadorProducto();
        if (!validador.validarProducto(datos[0], datos[1], datos[2], datos[3])) {
            return null;
        }
        try {
            return new Producto(datos[0], datos[1],
                    Integer.parseInt(datos[2]), Double.parseDouble(datos[3]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}