
package inventario.historial;

import java.util.Stack;
import java.util.ArrayList;

/**
 * Clase que permite almacenar el historial de operaciones
 * realizadas durante la ejecucion del sistema.
 *
 * Utiliza una pila que funciona mediante el principio LIFO
 * (Last In, First Out), donde la ultima operacion agregada
 * es la primera en mostrarse.
 *
 * El historial se mantiene en memoria y desaparece
 * al cerrar el programa.
 *
 * @author Gael
 */
public class HistorialPila {

    private Stack<String> historial;

    /**
     * Constructor que inicializa una pila vacia
     * para almacenar las operaciones.
     */
    public HistorialPila() {
        historial = new Stack<>();
    }

    /**
     * Agrega una nueva operacion a la pila del historial.
     * La operacion se almacena en la parte superior.
     *
     * @param operacion nombre de la operacion realizada
     * @throws IllegalArgumentException si la operacion
     *         es null o esta vacia
     */
    public void agregarOperacion(String operacion) {

        if (operacion == null || operacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La operacion no puede estar vacia" );
        }
        historial.push(operacion);
    }

    /**
     * Obtiene las operaciones almacenadas en la pila,
     * comenzando por la mas reciente hasta la mas antigua.
     *
     * Este metodo no elimina ni modifica los elementos
     * almacenados en el historial.
     *
     * @return lista de operaciones ordenadas
     *         desde la mas reciente hasta la mas antigua
     */
    public ArrayList<String> obtenerHistorial() {

        ArrayList<String> operaciones = new ArrayList<>();

        for (int i = historial.size() - 1; i >= 0; i--) {
            operaciones.add(historial.get(i));
        }

        return operaciones;
    }

    /**
     * Comprueba si la pila del historial contiene
     * alguna operacion registrada.
     *
     * @return true si el historial esta vacio,
     * false si contiene operaciones
     */
    public boolean estaVacio() {
        return historial.isEmpty();
    }
}
