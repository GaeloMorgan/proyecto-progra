package inventario.validaciones;

/**
 * Esta clase revisa que los datos de un producto sean correctos
 * antes de guardarlos en el inventario.
 *
 * @author lucaschivis
 */
public class ValidadorProducto {

    /**
     * Revisa que la clave tenga exactamente 6 dígitos.
     * @param clave texto que escribió el usuario
     * @return verdadero si la clave es válida, falso si no
     */
    public boolean validarClave(String clave) {
        if (clave.length() != 6) {
            return false;
        }
        for (int i = 0; i < clave.length(); i++) {
            char caracter = clave.charAt(i);
            if (caracter < '0' || caracter > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Revisa que el nombre no esté vacío y que no lleve el símbolo |,
     * porque con ese símbolo se separan los datos en el archivo.
     * @param nombre texto que escribió el usuario
     * @return verdadero si el nombre es válido, falso si no
     */
    public boolean validarNombre(String nombre) {
        if (nombre.trim().isEmpty()) {
            return false;
        }
        if (nombre.contains("|")) {
            return false;
        }
        return true;
    }

    /**
     * Revisa que la cantidad sea un número entero de 0 en adelante.
     * @param cantidad texto que escribió el usuario
     * @return verdadero si la cantidad es válida, falso si no
     */
    public boolean validarCantidad(String cantidad) {
        try {
            int numero = Integer.parseInt(cantidad);
            return numero >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Revisa que el precio sea un número mayor que 0.
     * @param precio texto que escribió el usuario
     * @return verdadero si el precio es válido, falso si no
     */
    public boolean validarPrecio(String precio) {
        try {
            double numero = Double.parseDouble(precio);
            return numero > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Revisa que el motivo de eliminación no esté vacío y que no lleve
     * el símbolo |.
     * @param motivo texto que escribió el usuario
     * @return verdadero si el motivo es válido, falso si no
     */
    public boolean validarMotivo(String motivo) {
        if (motivo.trim().isEmpty()) {
            return false;
        }
        if (motivo.contains("|")) {
            return false;
        }
        return true;
    }

    /**
     * Revisa los cuatro datos de un producto al mismo tiempo.
     * @param clave
     * @param nombre 
     * @param cantidad 
     * @param precio 
     * @return verdadero si todos los datos son válidos, falso si alguno no lo es
     */
    public boolean validarProducto(String clave, String nombre, String cantidad, String precio) {
        return validarClave(clave) && validarNombre(nombre)
                && validarCantidad(cantidad) && validarPrecio(precio);
    }
}
