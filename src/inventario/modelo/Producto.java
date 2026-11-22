package inventario.modelo;

import java.util.Objects;

/**
 * Representa un producto del inventario.
 * La clave se conserva como String para no perder ceros a la izquierda.
 */
public final class Producto {

    private final String clave;
    private String nombre;
    private int cantidad;
    private double precio;

    /**
     * Crea un producto con los datos obligatorios del proyecto.
     *
     * @param clave clave numérica de exactamente seis dígitos
     * @param nombre nombre del producto
     * @param cantidad unidades disponibles; debe ser cero o mayor
     * @param precio precio unitario; debe ser mayor que cero
     * @throws IllegalArgumentException si algún dato no cumple las reglas
     */
    public Producto(String clave, String nombre, int cantidad, double precio) {
        validarClave(clave);
        this.clave = clave;
        setNombre(nombre);
        setCantidad(cantidad);
        setPrecio(precio);
    }

    /**
     * Obtiene la clave inmutable del producto.
     *
     * @return clave de seis dígitos
     */
    public String getClave() {
        return clave;
    }

    /**
     * Obtiene el nombre actual del producto.
     *
     * @return nombre del producto
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el nombre del producto.
     * Se evita el separador | porque puede utilizarse en productos.txt.
     *
     * @param nombre nuevo nombre
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        String limpio = nombre.trim();
        if (limpio.contains("|")) {
            throw new IllegalArgumentException("El nombre no puede contener el carácter |.");
        }
        this.nombre = limpio;
    }

    /**
     * Obtiene la cantidad disponible.
     *
     * @return unidades disponibles
     */
    public int getCantidad() {
        return cantidad;
    }

    /**
     * Cambia las existencias del producto.
     *
     * @param cantidad nueva cantidad, igual o mayor que cero
     */
    public void setCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa.");
        }
        this.cantidad = cantidad;
    }

    /**
     * Obtiene el precio unitario actual.
     *
     * @return precio unitario
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * Cambia el precio unitario.
     *
     * @param precio nuevo precio, mayor que cero y finito
     */
    public void setPrecio(double precio) {
        if (!Double.isFinite(precio) || precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.precio = precio;
    }

    /**
     * Valida que la clave contenga exactamente seis dígitos del 0 al 9.
     *
     * @param clave clave que se revisará
     */
    public static void validarClave(String clave) {
        if (clave == null || clave.length() != 6) {
            throw new IllegalArgumentException("La clave debe contener exactamente seis dígitos.");
        }
        for (int i = 0; i < clave.length(); i++) {
            char caracter = clave.charAt(i);
            if (caracter < '0' || caracter > '9') {
                throw new IllegalArgumentException("La clave solo puede contener dígitos.");
            }
        }
    }

    /**
     * Compara dos productos por su clave.
     *
     * @param otro producto a comparar
     * @return resultado de comparar las claves
     */
    public int compararPorClave(Producto otro) {
        Objects.requireNonNull(otro, "El producto no puede ser null.");
        return clave.compareTo(otro.clave);
    }

    /**
     * Dos productos son iguales cuando tienen la misma clave.
     *
     * @param objeto objeto a comparar
     * @return true si las claves coinciden
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Producto)) {
            return false;
        }
        Producto otro = (Producto) objeto;
        return clave.equals(otro.clave);
    }

    /** @return hash basado en la clave del producto */
    @Override
    public int hashCode() {
        return clave.hashCode();
    }

    /** @return representación breve del producto */
    @Override
    public String toString() {
        return clave + " - " + nombre;
    }
}
