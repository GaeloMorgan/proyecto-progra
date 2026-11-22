package inventario.arbol;

import inventario.modelo.Producto;

/** Nodo que almacena un producto y sus dos posibles hijos. */
public class NodoArbol {

    private Producto producto;
    private NodoArbol izquierdo;
    private NodoArbol derecho;

    /**
     * Crea un nodo para el producto indicado.
     *
     * @param producto producto que almacenará el nodo
     */
    public NodoArbol(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null.");
        }
        this.producto = producto;
    }

    /**
     * Obtiene el producto almacenado en el nodo.
     *
     * @return producto del nodo
     */
    public Producto getProducto() {
        return producto;
    }

    /**
     * Reemplaza el producto del nodo. Solo es accesible desde el paquete arbol
     * para que los cambios estructurales sean controlados por el árbol.
     *
     * @param producto nuevo producto
     */
    void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null.");
        }
        this.producto = producto;
    }

    /**
     * Obtiene el hijo izquierdo.
     *
     * @return hijo izquierdo o null
     */
    public NodoArbol getIzquierdo() {
        return izquierdo;
    }

    /** @param izquierdo nuevo hijo izquierdo */
    void setIzquierdo(NodoArbol izquierdo) {
        this.izquierdo = izquierdo;
    }

    /**
     * Obtiene el hijo derecho.
     *
     * @return hijo derecho o null
     */
    public NodoArbol getDerecho() {
        return derecho;
    }

    /** @param derecho nuevo hijo derecho */
    void setDerecho(NodoArbol derecho) {
        this.derecho = derecho;
    }
}
