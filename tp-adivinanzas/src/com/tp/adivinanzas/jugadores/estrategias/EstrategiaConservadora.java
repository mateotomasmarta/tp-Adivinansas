package com.tp.adivinanzas.jugadores.estrategias;

import com.tp.adivinanzas.filtros.CatalogoFiltros;
import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Estrategia de guion fijo: la contracara deliberada de EstrategiaAsertiva.
 *
 * Se diferencia en los dos ejes de decision del juego:
 *
 * 1) QUE preguntar: recorre un orden de preferencia definido de antemano y toma
 *    el primer filtro de ese guion que todavia no haya usado, SIN mirar como
 *    parte el conjunto actual de candidatos. Puede elegir filtros muy
 *    desbalanceados (por ejemplo 4 contra 19) e incluso filtros que en ese
 *    momento no descartan a nadie, quemando el turno.
 *
 * 2) CUANDO arriesgar: es impaciente. Apenas los candidatos bajan del umbral
 *    tira una adivinanza al azar en vez de terminar de discriminar. Acierta con
 *    probabilidad 1/k y, si falla, regala el turno.
 *
 * Complejidad de elegirFiltro: O(f) por el recorrido del guion, contra O(n*f)
 * de la asertiva. Ahorra en el costo de decidir y lo paga en cantidad de turnos,
 * que es la metrica que realmente importa.
 *
 * Propiedad clave para la defensa: su rendimiento depende enteramente de que
 * tan bueno sea el guion que le toco. La asertiva, en cambio, es invariante al
 * orden del catalogo porque lo evalua entero en cada turno.
 */
public final class EstrategiaConservadora implements Estrategia {
    private static final int UMBRAL_ARRIESGAR_POR_DEFECTO = 3;

    private final int umbralArriesgar;
    private final Random random;
    private final List<Filtro> guion;
    private final String etiquetaGuion;

    public EstrategiaConservadora() {
        this(UMBRAL_ARRIESGAR_POR_DEFECTO, new Random(), CatalogoFiltros.listarTodos(), "catalogo");
    }

    /**
     * Constructor con semilla fija: permite reproducir exactamente la misma
     * partida en las simulaciones y en la defensa.
     */
    public EstrategiaConservadora(int umbralArriesgar, Random random) {
        this(umbralArriesgar, random, CatalogoFiltros.listarTodos(), "catalogo");
    }

    /**
     * Constructor completo: permite inyectar un guion distinto para demostrar
     * empiricamente la sensibilidad de esta estrategia al orden de preguntas.
     */
    public EstrategiaConservadora(int umbralArriesgar, Random random, List<Filtro> guion, String etiquetaGuion) {
        if (umbralArriesgar < 1) {
            throw new IllegalArgumentException("El umbral para arriesgar debe ser positivo.");
        }
        Objects.requireNonNull(guion, "El guion de preguntas es obligatorio.");
        if (guion.isEmpty()) {
            throw new IllegalArgumentException("El guion no puede estar vacio.");
        }
        this.umbralArriesgar = umbralArriesgar;
        this.random = Objects.requireNonNull(random, "El generador aleatorio es obligatorio.");
        this.guion = Collections.unmodifiableList(new ArrayList<Filtro>(guion));
        this.etiquetaGuion = etiquetaGuion == null ? "personalizado" : etiquetaGuion;
    }

    @Override
    public Filtro elegirFiltro(List<Personaje> candidatos, List<Filtro> filtrosDisponibles) {
        Objects.requireNonNull(candidatos, "Los candidatos son obligatorios.");
        Objects.requireNonNull(filtrosDisponibles, "Los filtros disponibles son obligatorios.");
        if (filtrosDisponibles.isEmpty()) {
            throw new IllegalStateException("No quedan filtros disponibles para preguntar.");
        }

        // O(f): primer filtro del guion que siga disponible. Nunca mira candidatos.
        for (Filtro filtro : guion) {
            if (filtrosDisponibles.contains(filtro)) {
                return filtro;
            }
        }

        return filtrosDisponibles.get(0);
    }

    @Override
    public boolean debeArriesgar(List<Personaje> candidatos) {
        return candidatos != null && !candidatos.isEmpty() && candidatos.size() <= umbralArriesgar;
    }

    @Override
    public Personaje elegirPersonaje(List<Personaje> candidatos) {
        if (candidatos == null || candidatos.isEmpty()) {
            throw new IllegalStateException("No quedan candidatos para arriesgar.");
        }
        if (candidatos.size() == 1) {
            return candidatos.get(0);
        }

        // Sin criterio de desempate: elige al azar entre los que quedan.
        return candidatos.get(random.nextInt(candidatos.size()));
    }

    @Override
    public String getNombre() {
        return "Conservadora (guion " + etiquetaGuion + ", umbral " + umbralArriesgar + ")";
    }
}
