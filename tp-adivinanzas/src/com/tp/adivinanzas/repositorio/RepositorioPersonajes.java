package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class RepositorioPersonajes {
    private static final RepositorioPersonajes INSTANCIA = new RepositorioPersonajes();

    private final List<Personaje> personajes;

    private RepositorioPersonajes() {
        FabricaPersonajes fabrica = new FabricaPersonajes();
        List<Personaje> personajesSinId = fabrica.crearPersonajes();

        List<Personaje> ordenadosPorGenero = OrdenadorMergeSort.ordenar(
                personajesSinId,
                Comparator.comparing(Personaje::getGenero)
        );

        List<Personaje> personajesConId = new ArrayList<Personaje>();
        for (int i = 0; i < ordenadosPorGenero.size(); i++) {
            personajesConId.add(ordenadosPorGenero.get(i).conId(i + 1));
        }

        this.personajes = Collections.unmodifiableList(personajesConId);
    }

    public static RepositorioPersonajes getInstancia() {
        return INSTANCIA;
    }

    public List<Personaje> listarTodos() {
        return personajes;
    }

    public Optional<Personaje> buscarPorId(int id) {
        return BuscadorBinario.buscarPorId(personajes, id);
    }

    public List<Personaje> filtrar(Predicate<Personaje> filtro) {
        List<Personaje> resultado = new ArrayList<Personaje>();
        for (Personaje personaje : personajes) {
            if (filtro.test(personaje)) {
                resultado.add(personaje);
            }
        }
        return resultado;
    }

    public int cantidad() {
        return personajes.size();
    }
}
