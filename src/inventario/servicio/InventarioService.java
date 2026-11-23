package inventario.servicio;

import inventario.algoritmos.MergeSort;
import inventario.algoritmos.QuickSort;
import inventario.arbol.ArbolBinarioBusqueda;
import inventario.historial.HistorialPila;
import inventario.modelo.Producto;
import inventario.persistencia.ArchivoEliminaciones;
import inventario.persistencia.ArchivoProductos;
import inventario.validaciones.ValidadorProducto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de unión entre la interfaz gráfica y los demás módulos: árbol, archivos,
 * validaciones, algoritmos de ordenamiento e historial.
 *
 * <p>Los cambios (agregar, editar, eliminar) solo se hacen en el árbol; el archivo se
 * actualiza hasta que se llama a {@link #guardarCambios()}.</p>
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

    private ArbolBinarioBusqueda arbol = new ArbolBinarioBusqueda();
    private final ArchivoProductos archivoProductos;
    private final ArchivoEliminaciones archivoEliminaciones;
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
        cambiosPendientes = false;
        return omitidas;
    }

    /**
     * Escribe el inventario actual en productos.txt y registra en el archivo de
     * eliminaciones los productos borrados desde el último guardado.
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
        clave = limpiar(clave);
        if (!validador.validarClave(clave)) {
            throw new IllegalArgumentException("La clave debe tener exactamente 6 dígitos.");
        }
        validarDatos(nombre, cantidad, precio);
        Producto nuevo = new Producto(clave, nombre, Integer.parseInt(limpiar(cantidad)),
                Double.parseDouble(limpiar(precio)));
        if (!arbol.insertar(nuevo)) {
            throw new IllegalArgumentException("Ya existe un producto con la clave " + clave + ".");
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
        Producto producto = arbol.buscarPorClave(limpiar(clave));
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con la clave " + clave + ".");
        }
        validarDatos(nombre, cantidad, precio);
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
