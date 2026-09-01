package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;
import java.util.Optional;

public final class BuscadorBinario {
    private BuscadorBinario() {
    }

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
