package inventario.algoritmos;

import inventario.modelo.Producto;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase que permite ordenar una lista de productos
 * alfabeticamente por nombre utilizando MergeSort.
 *
 * @author Gael
 */
public class MergeSort {

    /**
     * Ordena los productos alfabeticamente por nombre,
     * sin distinguir entre mayusculas y minusculas.
     *
     * @param productos lista de productos a ordenar
     * @throws IllegalArgumentException si la lista es null
     */
    public void ordenarPorNombre(List<Producto> productos){
        if (productos == null){
            throw new IllegalArgumentException("La lista no puede ser null");
        }
        if (productos.size() > 1){
                mergeSort(productos, 0, productos.size() - 1);
        }
    }

    /**
     * Divide recursivamente la lista en dos mitades
     * hasta obtener secciones de un solo elemento.
     * Posteriormente combina las secciones ordenadas.
     *
     * @param productos lista de productos
     * @param inicio posicion inicial de la seccion
     * @param fin posicion final de la seccion
     */
    private void mergeSort(List<Producto> productos, int inicio, int fin){
        if (inicio < fin ){
            int mitad = inicio + (fin - inicio) /2; 

            mergeSort(productos, inicio, mitad);
            mergeSort(productos, mitad + 1, fin);

            mezclar(productos, inicio,mitad,fin);
        }
    }

    /**
     * Combina dos mitades previamente ordenadas,
     * comparando los nombres de los productos.
     *
     * @param productos lista de productos
     * @param inicio posicion inicial
     * @param mitad posicion que divide las dos secciones
     * @param fin posicion final
     */
    private void mezclar(List<Producto> productos, int inicio, int mitad, int fin){
        List<Producto> izquierda = new ArrayList<>();
        List<Producto> derecha = new ArrayList<>();

        //Guardar los productos de la mitad izquierda
        for (int i = inicio; i <= mitad; i++){
            izquierda.add(productos.get(i));
        }

        //Guardar los productos de la mitad derecha
        for(int j = mitad + 1; j <= fin; j++){
            derecha.add(productos.get(j));
        }

        int i = 0;
        int j = 0;
        int k = inicio;

        //comparar los nombres de ambas mitades
        while (i < izquierda.size() && j < derecha.size()){
            if(izquierda.get(i).getNombre().compareToIgnoreCase(derecha.get(j).getNombre())<= 0){
                productos.set(k, izquierda.get(i));
                i++;
            } else {
                productos.set(k, derecha.get(j));
                j++;
            }
            k++;
        }

        //Agregar los elementos restantes de la izquierda
        while(i < izquierda.size()){
            productos.set(k,izquierda.get(i));
            i++;
            k++;
        }

        // agregar los elementos restantes de la derecha
        while(j < derecha.size()){
            productos.set(k, derecha.get(j));
            j++;
            k++;
        }
    }
}
