package inventario.servicio;

import inventario.algoritmos.MergeSort;
import inventario.algoritmos.QuickSort;
import inventario.arbol.ArbolBinarioBusqueda;
import inventario.historial.HistorialPila;
import inventario.modelo.Producto;
import inventario.persistencia.ArchivoEliminaciones;
import inventario.persistencia.ArchivoProductos;
import inventario.persistencia.ArchivoVentas;
import inventario.validaciones.ValidadorProducto;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Punto de unión entre la interfaz gráfica y los demás módulos: árbol, archivos,
 * validaciones, algoritmos de ordenamiento e historial.
 *
 * <p>Los cambios (agregar, editar, eliminar, vender) solo se hacen en el árbol; los archivos
 * se actualizan hasta que se llama a {@link #guardarCambios()}. Las imágenes de los productos
 * se guardan como {@code datos/imagenes/<clave>.png}.</p>
 */
public class InventarioService {

    /** Producto eliminado que aún no se registra en el archivo de eliminaciones. */
    private static class EliminacionPendiente {
        private final Producto producto;
        private final String motivo;

        EliminacionPendiente(Producto producto, String motivo) {
            this.producto = producto;
            this.motivo = motivo;
        }
    }

    /** Venta realizada que aún no se registra en el archivo de ventas. */
    private static class VentaPendiente {
        private final String clave;
        private final String nombre;
        private final int cantidad;
        private final double precio;
        private final LocalDateTime fecha;

        VentaPendiente(String clave, String nombre, int cantidad, double precio, LocalDateTime fecha) {
            this.clave = clave;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
            this.fecha = fecha;
        }
    }

    /** Lado máximo, en píxeles, de las imágenes guardadas. */
    private static final int LADO_MAXIMO_IMAGEN = 600;

    private ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
    private final ArchivoProductos archivoProductos;
    private final ArchivoEliminaciones archivoEliminaciones;
    private final ArchivoVentas archivoVentas;
    private final Path carpetaImagenes;
    private final List<VentaPendiente> ventasPendientes = new ArrayList<VentaPendiente>();
    private final HistorialPila historial = new HistorialPila();
    private final QuickSort quickSort = new QuickSort();
    private final MergeSort mergeSort = new MergeSort();
    private final ValidadorProducto validador = new ValidadorProducto();
    private final List<EliminacionPendiente> eliminacionesPendientes = new ArrayList<EliminacionPendiente>();
    private boolean cambiosPendientes;

    /** Crea el servicio con los archivos predeterminados de la carpeta datos. */
    public InventarioService() {
        this.archivoProductos = new ArchivoProductos();
        this.archivoEliminaciones = new ArchivoEliminaciones();
        this.archivoVentas = new ArchivoVentas();
        this.carpetaImagenes = Paths.get("datos", "imagenes");
    }

    /**
     * Crea el servicio con archivos específicos.
     *
     * @param rutaProductos archivo del inventario
     * @param rutaEliminaciones archivo del registro de eliminaciones
     */
    public InventarioService(String rutaProductos, String rutaEliminaciones) {
        this.archivoProductos = new ArchivoProductos(rutaProductos);
        this.archivoEliminaciones = new ArchivoEliminaciones(rutaEliminaciones);
        Path carpeta = Paths.get(rutaProductos).toAbsolutePath().getParent();
        this.archivoVentas = new ArchivoVentas(carpeta.resolve("ventas.txt").toString());
        this.carpetaImagenes = carpeta.resolve("imagenes");
    }

    // ------------------------------------------------------------------ carga y guardado

    /**
     * Lee el archivo a una lista y la inserta en el árbol usando la clave como índice.
     * Las líneas con datos incorrectos ya las omite {@link ArchivoProductos}; aquí se
     * omiten además los productos con clave repetida.
     *
     * @return cantidad de productos omitidos por tener la clave repetida
     */
    public int cargarInventario() {
        List<Producto> lista = archivoProductos.leerProductos();
        int omitidas = 0;
        arbol = new ArbolBinarioBusqueda();
        for (Producto producto : lista) {
            if (!arbol.insertar(producto)) {
                omitidas++;
            }
        }
        eliminacionesPendientes.clear();
        ventasPendientes.clear();
        cambiosPendientes = false;
        return omitidas;
    }

    /**
     * Escribe el inventario actual en productos.txt y registra en sus archivos las
     * eliminaciones y las ventas hechas desde el último guardado.
     *
     * @throws IOException si algún archivo no se puede escribir
     */
    public void guardarCambios() throws IOException {
        if (!archivoProductos.guardarProductos(arbol.obtenerInOrden())) {
            throw new IOException("No se pudo escribir el archivo de productos.");
        }
        while (!eliminacionesPendientes.isEmpty()) {
            EliminacionPendiente e = eliminacionesPendientes.get(0);
            if (!archivoEliminaciones.registrarEliminacion(e.producto, e.motivo)) {
                throw new IOException("No se pudo escribir el archivo de eliminaciones.");
            }
            eliminacionesPendientes.remove(0);
            if (arbol.buscarPorClave(e.producto.getClave()) == null) {
                borrarImagen(e.producto.getClave()); // el producto ya no existe: su imagen sobra
            }
        }
        while (!ventasPendientes.isEmpty()) {
            VentaPendiente v = ventasPendientes.get(0);
            if (!archivoVentas.registrarVenta(v.clave, v.nombre, v.cantidad, v.precio, v.fecha)) {
                throw new IOException("No se pudo escribir el archivo de ventas.");
            }
            ventasPendientes.remove(0);
        }
        cambiosPendientes = false;
        historial.agregarOperacion("Guardar cambios");
    }

    /**
     * Indica si hay cambios que todavía no se han guardado en el archivo.
     *
     * @return true si existen cambios pendientes
     */
    public boolean hayCambiosPendientes() {
        return cambiosPendientes;
    }

    // ------------------------------------------------------------------ operaciones

    /**
     * Agrega un producto nuevo validando todos los datos.
     *
     * @param clave seis dígitos
     * @param nombre nombre del producto
     * @param cantidad entero mayor o igual a cero
     * @param precio número mayor que cero
     * @throws IllegalArgumentException con un mensaje para el usuario si algún dato es inválido
     *         o si la clave ya existe
     */
    public void agregar(String clave, String nombre, String cantidad, String precio) {
        agregar(clave, nombre, cantidad, precio, null);
    }

    /**
     * Agrega un producto nuevo con imagen opcional.
     *
     * @param clave seis dígitos
     * @param nombre nombre del producto
     * @param cantidad entero mayor o igual a cero
     * @param precio número mayor que cero
     * @param imagen archivo de imagen elegido por el usuario, o null si no tendrá imagen
     * @throws IllegalArgumentException si algún dato es inválido, la clave ya existe o el
     *         archivo no es una imagen válida
     */
    public void agregar(String clave, String nombre, String cantidad, String precio, File imagen) {
        clave = limpiar(clave);
        if (!validador.validarClave(clave)) {
            throw new IllegalArgumentException("La clave debe tener exactamente 6 dígitos.");
        }
        validarDatos(nombre, cantidad, precio);
        BufferedImage foto = imagen == null ? null : leerImagen(imagen);
        Producto nuevo = new Producto(clave, nombre, Integer.parseInt(limpiar(cantidad)),
                Double.parseDouble(limpiar(precio)));
        if (!arbol.insertar(nuevo)) {
            throw new IllegalArgumentException("Ya existe un producto con la clave " + clave + ".");
        }
        try {
            if (foto != null) {
                escribirImagen(clave, foto);
            } else {
                borrarImagen(clave); // evita mostrar la imagen de un producto anterior con la misma clave
            }
        } catch (IOException e) {
            arbol.eliminarPorClave(clave);
            throw new IllegalArgumentException("No se pudo guardar la imagen: " + e.getMessage());
        }
        cambiosPendientes = true;
        historial.agregarOperacion("Agregar producto");
    }

    /**
     * Modifica nombre, cantidad y precio de un producto. La clave no se puede cambiar.
     *
     * @param clave clave del producto a editar
     * @param nombre nuevo nombre
     * @param cantidad nueva cantidad
     * @param precio nuevo precio
     * @throws IllegalArgumentException si el producto no existe o algún dato es inválido
     */
    public void editar(String clave, String nombre, String cantidad, String precio) {
        editar(clave, nombre, cantidad, precio, null, false);
    }

    /**
     * Modifica un producto y, opcionalmente, cambia o quita su imagen.
     *
     * @param clave clave del producto a editar
     * @param nombre nuevo nombre
     * @param cantidad nueva cantidad
     * @param precio nuevo precio
     * @param imagen nueva imagen, o null para conservar la actual
     * @param quitarImagen true para borrar la imagen actual (se ignora si se da una nueva)
     * @throws IllegalArgumentException si el producto no existe o algún dato es inválido
     */
    public void editar(String clave, String nombre, String cantidad, String precio,
            File imagen, boolean quitarImagen) {
        Producto producto = arbol.buscarPorClave(limpiar(clave));
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con la clave " + clave + ".");
        }
        validarDatos(nombre, cantidad, precio);
        BufferedImage foto = imagen == null ? null : leerImagen(imagen);
        try {
            if (foto != null) {
                escribirImagen(producto.getClave(), foto);
            } else if (quitarImagen) {
                borrarImagen(producto.getClave());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo guardar la imagen: " + e.getMessage());
        }
        producto.setNombre(nombre);
        producto.setCantidad(Integer.parseInt(limpiar(cantidad)));
        producto.setPrecio(Double.parseDouble(limpiar(precio)));
        cambiosPendientes = true;
        historial.agregarOperacion("Editar producto");
    }

    /**
     * Elimina un producto por su clave y guarda en memoria el motivo y la fecha para
     * registrarlos en el archivo de eliminaciones al guardar los cambios.
     *
     * @param clave clave del producto
     * @param motivo razón por la que se elimina
     * @throws IllegalArgumentException si el motivo es inválido o el producto no existe
     */
    public void eliminar(String clave, String motivo) {
        clave = limpiar(clave);
        if (!validador.validarClave(clave)) {
            throw new IllegalArgumentException("La clave debe tener exactamente 6 dígitos.");
        }
        if (!validador.validarMotivo(limpiar(motivo))) {
            throw new IllegalArgumentException("Escribe el motivo de la eliminación.");
        }
        Producto producto = arbol.buscarPorClave(clave);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con la clave " + clave + ".");
        }
        arbol.eliminarPorClave(clave);
        eliminacionesPendientes.add(new EliminacionPendiente(producto, limpiar(motivo)));
        cambiosPendientes = true;
        historial.agregarOperacion("Eliminar producto");
    }

    /**
     * Vende unidades de un producto: descuenta las existencias y deja la venta anotada para
     * registrarla en ventas.txt al guardar los cambios.
     *
     * @param clave clave del producto
     * @param cantidad unidades a vender (entero mayor que 0 y no mayor a las existencias)
     * @return total cobrado
     * @throws IllegalArgumentException si el producto no existe, la cantidad es inválida o
     *         no hay suficientes existencias
     */
    public double vender(String clave, String cantidad) {
        clave = limpiar(clave);
        Producto producto = arbol.buscarPorClave(clave);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con la clave " + clave + ".");
        }
        String texto = limpiar(cantidad);
        if (!texto.matches("\\d{1,9}") || Integer.parseInt(texto) <= 0) {
            throw new IllegalArgumentException("La cantidad a vender debe ser un entero mayor que 0.");
        }
        int unidades = Integer.parseInt(texto);
        if (producto.getCantidad() == 0) {
            throw new IllegalArgumentException("El producto no tiene existencias.");
        }
        if (unidades > producto.getCantidad()) {
            throw new IllegalArgumentException("Solo hay " + producto.getCantidad()
                    + " unidad(es) en existencia.");
        }
        double precio = producto.getPrecio();
        producto.setCantidad(producto.getCantidad() - unidades);
        ventasPendientes.add(new VentaPendiente(clave, producto.getNombre(), unidades, precio,
                LocalDateTime.now()));
        cambiosPendientes = true;
        historial.agregarOperacion("Vender producto");
        return Math.round(unidades * precio * 100.0) / 100.0;
    }

    // ------------------------------------------------------------------ imágenes

    /**
     * Obtiene el archivo de imagen de un producto.
     *
     * @param clave clave del producto
     * @return archivo de la imagen, o null si el producto no tiene
     */
    public File obtenerImagen(String clave) {
        File archivo = carpetaImagenes.resolve(limpiar(clave) + ".png").toFile();
        return archivo.isFile() ? archivo : null;
    }

    private BufferedImage leerImagen(File archivo) {
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                throw new IllegalArgumentException(
                        "El archivo elegido no es una imagen válida (usa PNG, JPG, GIF o BMP).");
            }
            return imagen;
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer la imagen: " + e.getMessage());
        }
    }

    /** Guarda la imagen como PNG, reduciéndola si es muy grande. */
    private void escribirImagen(String clave, BufferedImage imagen) throws IOException {
        int mayorLado = Math.max(imagen.getWidth(), imagen.getHeight());
        if (mayorLado > LADO_MAXIMO_IMAGEN) {
            double escala = LADO_MAXIMO_IMAGEN / (double) mayorLado;
            int ancho = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
            BufferedImage reducida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = reducida.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(imagen, 0, 0, ancho, alto, null);
            g.dispose();
            imagen = reducida;
        }
        Files.createDirectories(carpetaImagenes);
        if (!ImageIO.write(imagen, "png", carpetaImagenes.resolve(clave + ".png").toFile())) {
            throw new IOException("formato de imagen no compatible");
        }
    }

    private void borrarImagen(String clave) {
        try {
            Files.deleteIfExists(carpetaImagenes.resolve(clave + ".png"));
        } catch (IOException e) {
            // Si no se puede borrar, la imagen queda en la carpeta sin afectar al inventario.
        }
    }

    // ------------------------------------------------------------------ búsquedas

    /**
     * Busca por clave en el árbol y registra la función en el historial.
     *
     * @param clave clave de seis dígitos
     * @return producto encontrado o null
     * @throws IllegalArgumentException si la clave no tiene seis dígitos
     */
    public Producto buscarPorClave(String clave) {
        clave = limpiar(clave);
        if (!validador.validarClave(clave)) {
            throw new IllegalArgumentException("La clave debe tener exactamente 6 dígitos.");
        }
        historial.agregarOperacion("Buscar por clave");
        return arbol.buscarPorClave(clave);
    }

    /**
     * Busca coincidencias por nombre en el árbol y registra la función en el historial.
     *
     * @param texto parte del nombre
     * @return productos encontrados; lista vacía si no hay
     */
    public List<Producto> buscarPorNombre(String texto) {
        historial.agregarOperacion("Buscar por nombre");
        return arbol.buscarPorNombre(texto);
    }

    /**
     * Consulta un producto sin registrar nada en el historial (uso interno de la interfaz).
     *
     * @param clave clave del producto
     * @return producto o null
     */
    public Producto obtenerProducto(String clave) {
        return arbol.buscarPorClave(limpiar(clave));
    }

    // ------------------------------------------------------------------ listados

    /**
     * Inventario en recorrido inorden (queda por clave). Registra la función en el historial.
     *
     * @return lista de productos
     */
    public List<Producto> mostrarInventario() {
        historial.agregarOperacion("Mostrar inventario");
        return obtenerInventario();
    }

    /**
     * Inventario por precio: recorrido inorden y QuickSort. Registra la función en el historial.
     *
     * @return lista ordenada de menor a mayor precio
     */
    public List<Producto> mostrarPorPrecio() {
        historial.agregarOperacion("Mostrar por precio");
        return obtenerPorPrecio();
    }

    /**
     * Inventario por nombre: recorrido preorden y MergeSort. Registra la función en el historial.
     *
     * @return lista ordenada alfabéticamente
     */
    public List<Producto> mostrarPorNombre() {
        historial.agregarOperacion("Mostrar por nombre");
        return obtenerPorNombre();
    }

    /**
     * Igual que {@link #mostrarInventario()} pero sin registrar en el historial.
     *
     * @return lista de productos por clave
     */
    public List<Producto> obtenerInventario() {
        return arbol.obtenerInOrden();
    }

    /**
     * Igual que {@link #mostrarPorPrecio()} pero sin registrar en el historial.
     *
     * @return lista ordenada por precio
     */
    public List<Producto> obtenerPorPrecio() {
        List<Producto> lista = arbol.obtenerInOrden();
        quickSort.ordenarPorPrecio(lista);
        return lista;
    }

    /**
     * Igual que {@link #mostrarPorNombre()} pero sin registrar en el historial.
     *
     * @return lista ordenada por nombre
     */
    public List<Producto> obtenerPorNombre() {
        List<Producto> lista = arbol.obtenerPreOrden();
        mergeSort.ordenarPorNombre(lista);
        return lista;
    }

    /**
     * Historial de funciones usadas, con la más reciente al inicio.
     *
     * @return copia del historial
     */
    public List<String> getHistorial() {
        return historial.obtenerHistorial();
    }

    // ------------------------------------------------------------------ auxiliares

    private void validarDatos(String nombre, String cantidad, String precio) {
        if (!validador.validarNombre(limpiar(nombre))) {
            throw new IllegalArgumentException("El nombre no puede estar vacío ni contener comas.");
        }
        if (!validador.validarCantidad(limpiar(cantidad))) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero igual o mayor que 0.");
        }
        if (!validador.validarPrecio(limpiar(precio))
                || !Double.isFinite(Double.parseDouble(limpiar(precio)))) {
            throw new IllegalArgumentException("El precio debe ser un número mayor que 0 (usa punto decimal).");
        }
    }

    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
