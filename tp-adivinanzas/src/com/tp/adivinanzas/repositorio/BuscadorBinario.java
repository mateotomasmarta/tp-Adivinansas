package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;
import java.util.Optional;

/**
 * Busqueda binaria de personajes por id.
 *
 * Solo funciona correctamente si la lista recibida esta ordenada por id de
 * menor a mayor.
 */
public final class BuscadorBinario {
    private BuscadorBinario() {
    }

    /**
     * Busca el personaje partiendo el rango a la mitad en cada iteracion.
     *
     * Costo: O(log n), porque en cada paso descarta la mitad del rango.
     */
    public static Optional<Personaje> buscarPorId(List<Personaje> personajes, int idBuscado) {
        int inicio = 0;
        int fin = personajes.size() - 1;

        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;
            Personaje actual = personajes.get(medio);

            if (actual.getId() == idBuscado) {
                return Optional.of(actual);
            }
            if (actual.getId() < idBuscado) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }

        return Optional.empty();
    }
}
