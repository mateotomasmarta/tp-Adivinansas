package com.tp.adivinanzas.jugadores.estrategias;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;
import java.util.Objects;

/**
 * Estrategia greedy con criterio minimax.
 *
 * En cada turno evalua TODOS los filtros disponibles y elige el que minimiza
 * el peor caso, es decir, el que deja el subconjunto de candidatos mas chico
 * posible pase lo que pase:
 *
 *     mejorFiltro = argmin( max(|afirmativos|, |negativos|) )
 *
 * Es greedy porque optimiza el turno actual sin simular los turnos siguientes:
 * no construye el arbol de decision completo, solo mira un nivel hacia
 * adelante. El optimo global exigiria explorar todas las secuencias de
 * preguntas (costo exponencial); este criterio local es casi optimo porque la
 * funcion de particion es monotona -partir por la mitad nunca puede empeorar
 * la profundidad esperada del arbol-.
 *
 * Efecto: cada pregunta divide el espacio de busqueda aproximadamente por dos,
 * igual que una busqueda binaria pero sobre un espacio de atributos en lugar
 * de un espacio de indices. Con n = 23 candidatos el techo teorico es
 * techo(log2(23)) = 5 preguntas.
 *
 * Complejidad de elegirFiltro: O(n * f), con n candidatos y f filtros
 * disponibles. Con n = 23 y f = 7 son ~161 evaluaciones por turno: el costo de
 * pensar es despreciable frente al costo de preguntar de mas.
 */
public final class EstrategiaAsertiva implements Estrategia {

    @Override
    public Filtro elegirFiltro(List<Personaje> candidatos, List<Filtro> filtrosDisponibles) {
        validarEntrada(candidatos, filtrosDisponibles);

        Filtro mejorFiltro = null;
        int mejorPeorCaso = Integer.MAX_VALUE;

        for (Filtro filtro : filtrosDisponibles) {
            int peorCaso = peorCasoDe(candidatos, filtro);

            // Desempate estable: ante igual peor caso se conserva el primero
            // encontrado, para que la partida sea reproducible y defendible.
            if (peorCaso < mejorPeorCaso) {
                mejorPeorCaso = peorCaso;
                mejorFiltro = filtro;
            }
        }

        return mejorFiltro;
    }

    /**
     * Solo arriesga cuando queda un unico candidato posible.
     *
     * Justificacion: con k candidatos, arriesgar acierta con probabilidad 1/k y
     * consume un turno. Preguntar tambien consume un turno, pero garantiza
     * reducir el conjunto. Como todos los personajes son distinguibles entre si,
     * si dos candidatos sobrevivieron a las mismas respuestas necesariamente
     * difieren en algun filtro todavia sin usar: siempre existe una pregunta
     * util, y por lo tanto nunca conviene adivinar a ciegas.
     */
    @Override
    public boolean debeArriesgar(List<Personaje> candidatos) {
        return candidatos != null && candidatos.size() == 1;
    }

    @Override
    public Personaje elegirPersonaje(List<Personaje> candidatos) {
        if (candidatos == null || candidatos.isEmpty()) {
            throw new IllegalStateException("No quedan candidatos para arriesgar.");
        }

        // En el caso normal queda uno solo. Si el motor fuerza una adivinanza
        // anticipada, todos los candidatos restantes son equiprobables: se
        // devuelve el primero por determinismo.
        return candidatos.get(0);
    }

    @Override
    public String getNombre() {
        return "Asertiva (greedy minimax)";
    }

    /**
     * Cantidad de candidatos que responderian SI a este filtro.
     * Publico para que el modo maquina vs maquina pueda mostrar la tabla de
     * evaluacion sin duplicar la logica.
     */
    public static int contarAfirmativos(List<Personaje> candidatos, Filtro filtro) {
        int afirmativos = 0;
        for (Personaje candidato : candidatos) {
            if (filtro.cumple(candidato)) {
                afirmativos++;
            }
        }
        return afirmativos;
    }

    /**
     * Tamano del subconjunto que quedaria en el peor de los dos escenarios
     * posibles. Es la funcion objetivo que la estrategia minimiza.
     */
    public static int peorCasoDe(List<Personaje> candidatos, Filtro filtro) {
        int afirmativos = contarAfirmativos(candidatos, filtro);
        int negativos = candidatos.size() - afirmativos;
        return Math.max(afirmativos, negativos);
    }

    private void validarEntrada(List<Personaje> candidatos, List<Filtro> filtrosDisponibles) {
        Objects.requireNonNull(candidatos, "Los candidatos son obligatorios.");
        Objects.requireNonNull(filtrosDisponibles, "Los filtros disponibles son obligatorios.");
        if (candidatos.isEmpty()) {
            throw new IllegalStateException("No hay candidatos: alguna respuesta fue inconsistente.");
        }
        if (filtrosDisponibles.isEmpty()) {
            throw new IllegalStateException("No quedan filtros disponibles para preguntar.");
        }
    }
}
