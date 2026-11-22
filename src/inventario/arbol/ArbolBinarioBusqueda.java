package inventario.arbol;

import inventario.modelo.Producto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Árbol binario de búsqueda de productos, indexado por la clave de seis dígitos.
 * La inserción, búsqueda por clave, recorridos y eliminación son recursivos,
 * siguiendo la lógica practicada en clase.
 */
public class ArbolBinarioBusqueda {

    private NodoArbol raiz;

    /** Crea un árbol vacío. */
    public ArbolBinarioBusqueda() {
        raiz = null;
    }

    /**
     * Obtiene la raíz del árbol para su consulta o representación.
     *
     * @return raíz del árbol, o null si está vacío
     */
    public NodoArbol getRaiz() {
        return raiz;
    }

    /**
     * Comprueba si el árbol está vacío.
     *
     * @return true cuando el árbol no contiene productos
     */
    public boolean estaVacio() {
        return raiz == null;
    }

    /**
     * Inserta un producto. No se permiten claves repetidas.
     *
     * @param producto producto que se insertará
     * @return true si se insertó; false si ya existe la clave
     */
    public boolean insertar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("No se puede insertar un producto null.");
        }
        if (raiz == null) {
            raiz = new NodoArbol(producto);
            return true;
        }
        return insertarRecursivo(raiz, producto);
    }

    private boolean insertarRecursivo(NodoArbol actual, Producto producto) {
        int comparacion = producto.getClave().compareTo(actual.getProducto().getClave());
        if (comparacion < 0) {
            if (actual.getIzquierdo() == null) {
                actual.setIzquierdo(new NodoArbol(producto));
                return true;
            }
            return insertarRecursivo(actual.getIzquierdo(), producto);
        }
        if (comparacion > 0) {
            if (actual.getDerecho() == null) {
                actual.setDerecho(new NodoArbol(producto));
                return true;
            }
            return insertarRecursivo(actual.getDerecho(), producto);
        }
        return false;
    }

    /**
     * Busca un producto mediante su clave.
     *
     * @param clave clave de seis dígitos
     * @return producto encontrado o null
     */
    public Producto buscarPorClave(String clave) {
        if (!claveValida(clave)) {
            return null;
        }
        return buscarPorClaveRecursivo(raiz, clave);
    }

    private Producto buscarPorClaveRecursivo(NodoArbol actual, String clave) {
        if (actual == null) {
            return null;
        }
        int comparacion = clave.compareTo(actual.getProducto().getClave());
        if (comparacion == 0) {
            return actual.getProducto();
        }
        if (comparacion < 0) {
            return buscarPorClaveRecursivo(actual.getIzquierdo(), clave);
        }
        return buscarPorClaveRecursivo(actual.getDerecho(), clave);
    }

    /**
     * Busca un producto por su clave usando un nombre de método breve.
     *
     * @param clave clave de seis dígitos
     * @return producto encontrado o null si no existe
     */
    public Producto buscar(String clave) {
        return buscarPorClave(clave);
    }

    /**
     * Comprueba si el árbol contiene la clave indicada.
     *
     * @param clave clave que se consultará
     * @return true si existe un producto con esa clave
     */
    public boolean contiene(String clave) {
        return buscarPorClave(clave) != null;
    }

    /**
     * Busca coincidencias parciales en el nombre, sin distinguir mayúsculas.
     * El recorrido es necesario porque el árbol está ordenado por clave, no por nombre.
     *
     * @param texto fragmento del nombre que se desea localizar
     * @return lista con todas las coincidencias; vacía si no hay resultados
     */
    public List<Producto> buscarPorNombre(String texto) {
        List<Producto> encontrados = new ArrayList<Producto>();
        if (texto == null || texto.trim().isEmpty()) {
            return encontrados;
        }
        String consulta = texto.trim().toLowerCase(Locale.ROOT);
        buscarPorNombreRecursivo(raiz, consulta, encontrados);
        return encontrados;
    }

    private void buscarPorNombreRecursivo(NodoArbol actual, String consulta,
            List<Producto> encontrados) {
        if (actual == null) {
            return;
        }
        buscarPorNombreRecursivo(actual.getIzquierdo(), consulta, encontrados);
        if (actual.getProducto().getNombre().toLowerCase(Locale.ROOT).contains(consulta)) {
            encontrados.add(actual.getProducto());
        }
        buscarPorNombreRecursivo(actual.getDerecho(), consulta, encontrados);
    }

    /**
     * Elimina un producto por su clave. Para dos hijos utiliza el sucesor InOrden.
     *
     * @param clave clave del producto
     * @return true si se eliminó; false si no existía
     */
    public boolean eliminarPorClave(String clave) {
        if (!claveValida(clave) || buscarPorClave(clave) == null) {
            return false;
        }
        raiz = eliminarRecursivo(raiz, clave);
        return true;
    }

    /**
     * Elimina un producto por su clave usando un nombre de método breve.
     *
     * @param clave clave del producto que se eliminará
     * @return true si se eliminó; false si no existe
     */
    public boolean eliminar(String clave) {
        return eliminarPorClave(clave);
    }

    private NodoArbol eliminarRecursivo(NodoArbol actual, String clave) {
        if (actual == null) {
            return null;
        }
        int comparacion = clave.compareTo(actual.getProducto().getClave());
        if (comparacion < 0) {
            actual.setIzquierdo(eliminarRecursivo(actual.getIzquierdo(), clave));
        } else if (comparacion > 0) {
            actual.setDerecho(eliminarRecursivo(actual.getDerecho(), clave));
        } else {
            if (actual.getIzquierdo() == null) {
                return actual.getDerecho();
            }
            if (actual.getDerecho() == null) {
                return actual.getIzquierdo();
            }
            NodoArbol sucesor = obtenerNodoMenor(actual.getDerecho());
            actual.setProducto(sucesor.getProducto());
            actual.setDerecho(eliminarRecursivo(
                    actual.getDerecho(), sucesor.getProducto().getClave()));
        }
        return actual;
    }

    /**
     * Devuelve una lista con el recorrido InOrden (izquierda, raíz, derecha).
     * La lista queda ordenada por clave y puede entregarse al QuickSort por precio.
     *
     * @return nueva lista de productos
     */
    public List<Producto> obtenerInOrden() {
        List<Producto> productos = new ArrayList<Producto>();
        obtenerInOrdenRecursivo(raiz, productos);
        return productos;
    }

    private void obtenerInOrdenRecursivo(NodoArbol actual, List<Producto> productos) {
        if (actual == null) {
            return;
        }
        obtenerInOrdenRecursivo(actual.getIzquierdo(), productos);
        productos.add(actual.getProducto());
        obtenerInOrdenRecursivo(actual.getDerecho(), productos);
    }

    /**
     * Devuelve una lista con el recorrido PreOrden (raíz, izquierda, derecha).
     * Esta lista se entregará al MergeSort por nombre.
     *
     * @return nueva lista de productos
     */
    public List<Producto> obtenerPreOrden() {
        List<Producto> productos = new ArrayList<Producto>();
        obtenerPreOrdenRecursivo(raiz, productos);
        return productos;
    }

    private void obtenerPreOrdenRecursivo(NodoArbol actual, List<Producto> productos) {
        if (actual == null) {
            return;
        }
        productos.add(actual.getProducto());
        obtenerPreOrdenRecursivo(actual.getIzquierdo(), productos);
        obtenerPreOrdenRecursivo(actual.getDerecho(), productos);
    }

    /**
     * Obtiene los productos con el recorrido PostOrden (izquierda, derecha, raíz).
     *
     * @return lista nueva con el recorrido PostOrden
     */
    public List<Producto> obtenerPostOrden() {
        List<Producto> productos = new ArrayList<Producto>();
        obtenerPostOrdenRecursivo(raiz, productos);
        return productos;
    }

    private void obtenerPostOrdenRecursivo(NodoArbol actual, List<Producto> productos) {
        if (actual == null) {
            return;
        }
        obtenerPostOrdenRecursivo(actual.getIzquierdo(), productos);
        obtenerPostOrdenRecursivo(actual.getDerecho(), productos);
        productos.add(actual.getProducto());
    }

    /**
     * Muestra el recorrido InOrden en consola, como en la práctica.
     *
     * @param nodo nodo desde el que inicia el recorrido
     */
    public void mostrarInOrden(NodoArbol nodo) {
        if (nodo != null) {
            mostrarInOrden(nodo.getIzquierdo());
            System.out.println(nodo.getProducto());
            mostrarInOrden(nodo.getDerecho());
        }
    }

    /**
     * Muestra el recorrido PreOrden en consola, como en la práctica.
     *
     * @param nodo nodo desde el que inicia el recorrido
     */
    public void mostrarPreOrden(NodoArbol nodo) {
        if (nodo != null) {
            System.out.println(nodo.getProducto());
            mostrarPreOrden(nodo.getIzquierdo());
            mostrarPreOrden(nodo.getDerecho());
        }
    }

    /**
     * Muestra el recorrido PostOrden en consola, como en la práctica.
     *
     * @param nodo nodo desde el que inicia el recorrido
     */
    public void mostrarPostOrden(NodoArbol nodo) {
        if (nodo != null) {
            mostrarPostOrden(nodo.getIzquierdo());
            mostrarPostOrden(nodo.getDerecho());
            System.out.println(nodo.getProducto());
        }
    }

    /**
     * Obtiene el producto que tiene la clave más pequeña.
     *
     * @return producto con la clave menor o null si el árbol está vacío
     */
    public Producto obtenerMenor() {
        NodoArbol menor = obtenerNodoMenor(raiz);
        return menor == null ? null : menor.getProducto();
    }

    private NodoArbol obtenerNodoMenor(NodoArbol actual) {
        if (actual == null || actual.getIzquierdo() == null) {
            return actual;
        }
        return obtenerNodoMenor(actual.getIzquierdo());
    }

    /**
     * Cuenta todos los nodos del árbol.
     *
     * @return cantidad total de productos guardados en el árbol
     */
    public int contarNodos() {
        return contarNodosRecursivo(raiz);
    }

    private int contarNodosRecursivo(NodoArbol actual) {
        if (actual == null) {
            return 0;
        }
        return 1 + contarNodosRecursivo(actual.getIzquierdo())
                + contarNodosRecursivo(actual.getDerecho());
    }

    /**
     * Conserva el conteo de la práctica: nodos internos, sin incluir raíz ni hojas.
     *
     * @return cantidad de nodos internos que no son la raíz
     */
    public int contarNodoRama() {
        return contarRamaRecursivo(raiz, true);
    }

    /**
     * Cuenta los nodos internos descendientes del nodo recibido, sin contar la raíz
     * general del árbol ni los nodos hoja. Conserva el ejercicio de la práctica.
     *
     * @param nodo nodo desde el que se iniciará el conteo
     * @return cantidad de nodos internos encontrados
     */
    public int contarRama(NodoArbol nodo) {
        return contarRamaDesdeNodo(nodo, nodo == raiz);
    }

    private int contarRamaDesdeNodo(NodoArbol actual, boolean esRaizGeneral) {
        if (actual == null) {
            return 0;
        }
        boolean esHoja = actual.getIzquierdo() == null && actual.getDerecho() == null;
        int cuenta = (!esRaizGeneral && !esHoja) ? 1 : 0;
        return cuenta + contarRamaDesdeNodo(actual.getIzquierdo(), false)
                + contarRamaDesdeNodo(actual.getDerecho(), false);
    }

    private int contarRamaRecursivo(NodoArbol actual, boolean esRaiz) {
        if (actual == null) {
            return 0;
        }
        boolean esHoja = actual.getIzquierdo() == null && actual.getDerecho() == null;
        int cuenta = (!esRaiz && !esHoja) ? 1 : 0;
        return cuenta + contarRamaRecursivo(actual.getIzquierdo(), false)
                + contarRamaRecursivo(actual.getDerecho(), false);
    }

    /**
     * Busca el padre de un producto. Comprueba ambas ramas y utiliza la clave para decidir
     * hacia dónde continuar.
     *
     * @param clave clave del producto del que se busca el padre
     * @return nodo padre o null si no existe o es la raíz
     */
    public NodoArbol buscarPadre(String clave) {
        if (!claveValida(clave) || raiz == null
                || raiz.getProducto().getClave().equals(clave)) {
            return null;
        }
        return buscarPadreRecursivo(raiz, clave);
    }

    private NodoArbol buscarPadreRecursivo(NodoArbol actual, String clave) {
        if (actual == null) {
            return null;
        }
        NodoArbol izquierdo = actual.getIzquierdo();
        NodoArbol derecho = actual.getDerecho();
        if (izquierdo != null && izquierdo.getProducto().getClave().equals(clave)) {
            return actual;
        }
        if (derecho != null && derecho.getProducto().getClave().equals(clave)) {
            return actual;
        }
        if (clave.compareTo(actual.getProducto().getClave()) < 0) {
            return buscarPadreRecursivo(izquierdo, clave);
        }
        return buscarPadreRecursivo(derecho, clave);
    }

    /**
     * Comprueba si un nodo no tiene hijos.
     *
     * @param nodo nodo que se revisará
     * @return true si el nodo existe y no tiene hijos
     */
    public boolean esHoja(NodoArbol nodo) {
        return nodo != null && nodo.getIzquierdo() == null && nodo.getDerecho() == null;
    }

    /**
     * Imprime el árbol girado en consola, como apoyo para revisar su estructura.
     */
    public void mostrarGrafico() {
        mostrarGrafico(raiz, 0);
    }

    /**
     * Imprime el árbol girado a partir del nodo recibido.
     *
     * @param nodo nodo desde el que comienza la representación
     * @param espacios nivel de sangría inicial
     */
    public void mostrarGrafico(NodoArbol nodo, int espacios) {
        if (nodo != null) {
            mostrarGrafico(nodo.getDerecho(), espacios + 1);
            imprimirEspacios(espacios + 1);
            System.out.println(nodo.getProducto().getClave());
            mostrarGrafico(nodo.getIzquierdo(), espacios + 1);
        }
    }

    private void imprimirEspacios(int espacios) {
        for (int i = 0; i < espacios; i++) {
            System.out.print("\t");
        }
    }

    private boolean claveValida(String clave) {
        if (clave == null || clave.length() != 6) {
            return false;
        }
        for (int i = 0; i < clave.length(); i++) {
            if (clave.charAt(i) < '0' || clave.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
