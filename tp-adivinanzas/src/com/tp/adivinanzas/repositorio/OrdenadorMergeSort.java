package com.tp.adivinanzas.repositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementacion propia de MergeSort.
 *
 * Se usa para cumplir la consigna de algoritmos: divide la lista en mitades,
 * ordena recursivamente y despues mezcla manteniendo el orden.
 */
public final class OrdenadorMergeSort {
    private OrdenadorMergeSort() {
    }

    /**
     * Ordena una lista segun el comparador recibido sin modificar la lista
     * original.
     *
     * Costo temporal: O(n log n) en mejor, promedio y peor caso.
     * Costo espacial: O(n) por las listas auxiliares.
     */
    public static <T> List<T> ordenar(List<T> elementos, Comparator<T> comparador) {
        Objects.requireNonNull(elementos, "La lista a ordenar es obligatoria.");
        Objects.requireNonNull(comparador, "El comparador es obligatorio.");

        if (elementos.size() <= 1) {
            return new ArrayList<T>(elementos);
        }

        int mitad = elementos.size() / 2;
        List<T> izquierda = ordenar(elementos.subList(0, mitad), comparador);
        List<T> derecha = ordenar(elementos.subList(mitad, elementos.size()), comparador);

        return mezclar(izquierda, derecha, comparador);
    }

    /**
     * Combina dos listas ya ordenadas en una sola lista ordenada.
     *
     * Costo: O(n), donde n es la suma de ambas listas.
     * Es estable: si dos elementos empatan, conserva primero el de la izquierda.
     */
    private static <T> List<T> mezclar(List<T> izquierda, List<T> derecha, Comparator<T> comparador) {
        List<T> resultado = new ArrayList<T>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;

        while (i < izquierda.size() && j < derecha.size()) {
            if (comparador.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i));
                i++;
            } else {
                resultado.add(derecha.get(j));
                j++;
            }
        }

        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i));
            i++;
        }

        while (j < derecha.size()) {
            resultado.add(derecha.get(j));
            j++;
        }

        return resultado;
    }
}
