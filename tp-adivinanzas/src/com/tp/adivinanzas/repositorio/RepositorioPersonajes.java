package com.tp.adivinanzas.repositorio;

import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repositorio central de personajes.
 *
 * Usa Singleton para que toda la aplicacion trabaje con el mismo tablero de
 * personajes. Al construirlo, pide personajes a la fabrica, los ordena y luego
 * les asigna IDs definitivos.
 */
public final class RepositorioPersonajes {
    private static final RepositorioPersonajes INSTANCIA = new RepositorioPersonajes();

    private final List<Personaje> personajes;

    /**
     * Carga los personajes, los ordena por genero con MergeSort y asigna IDs
     * del 1 al n. El id no lo define la fabrica: se asigna aca, despues del
     * ordenamiento.
     *
     * Costo: O(n log n) por el MergeSort + O(n) por asignar IDs.
     */
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

    /**
     * Acceso global a la unica instancia del repositorio.
     *
     * Costo: O(1).
     */
    public static RepositorioPersonajes getInstancia() {
        return INSTANCIA;
    }

    /**
     * Devuelve la lista ya cargada e inmodificable.
     *
     * Costo: O(1), no copia la lista.
     */
    public List<Personaje> listarTodos() {
        return personajes;
    }

    /**
     * Busca por id usando busqueda binaria.
     *
     * Precondicion: la lista debe estar ordenada por id.
     * Costo: O(log n).
     */
    public Optional<Personaje> buscarPorId(int id) {
        return BuscadorBinario.buscarPorId(personajes, id);
    }

    /**
     * Recorre todos los personajes y devuelve los que cumplen el predicado.
     *
     * Costo: O(n).
     */
    public List<Personaje> filtrar(Predicate<Personaje> filtro) {
        List<Personaje> resultado = new ArrayList<Personaje>();
        for (Personaje personaje : personajes) {
            if (filtro.test(personaje)) {
                resultado.add(personaje);
            }
        }
        return resultado;
    }

    /**
     * Cantidad total de personajes cargados.
     *
     * Costo: O(1).
     */
    public int cantidad() {
        return personajes.size();
    }
}
