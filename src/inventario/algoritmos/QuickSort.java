package inventario.algoritmos;

import inventario.modelo.Producto;
import java.util.List;

/**
 * Clase que permite ordenar una lista de productos
 * por precio utilizando el algoritmo QuickSort.
 *
 * @author Gael
 */
public class QuickSort {
     /**
     * Ordena los productos de menor a mayor precio.
     *
     * @param productos Lista de productos a ordenar.
     */
    public void ordenarPorPrecio(List<Producto> productos){
        if(productos == null){
            throw new IllegalArgumentException("La lista no puede ser null");
        }
        if (productos.size() > 1){
            quickSort(productos, 0, productos.size() - 1);
        }
    }

    /**
     * Aplica QuickSort de manera recursiva, dividiendo
     * la lista en secciones mediante un pivote.
     *
     * @param productos lista de productos
     * @param inicio posicion inicial de la seccion
     * @param fin posicion final de la seccion
     */
    private void quickSort(List <Producto> productos, int inicio, int fin){
        if (inicio < fin){
            int pivote = particionar (productos,inicio,fin);

            quickSort(productos, inicio, pivote -1);
            quickSort(productos, pivote + 1, fin);
        }
    }

    /**
     * Organiza los productos alrededor de un pivote,
     * tomando el precio del ultimo elemento.
     *
     * @param productos lista de productos
     * @param inicio posicion inicial
     * @param fin posicion final
     * @return posicion definitiva del pivote
     */
    private int particionar(List<Producto> productos, int inicio, int fin){
        
        double pivote = productos.get(fin).getPrecio();
        int i = inicio - 1; 

        for (int j = inicio; j < fin; j++){
            if(productos.get(j).getPrecio()<=pivote){
                i++;
                intercambiar(productos,i,j);
            }
        }
        intercambiar(productos, i + 1, fin);

        return i + 1;
    }
    
    /**
     * Intercambia la posicion de dos productos
     * dentro de la lista.
     *
     * @param productos lista de productos
     * @param i posicion del primer producto
     * @param j posicion del segundo producto
     */
    private void intercambiar(List<Producto> productos, int i, int j){
        Producto temporal = productos.get(i);

        productos.set(i,productos.get(j));
        productos.set(j, temporal);
    }
}
