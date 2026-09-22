package com.tp.adivinanzas.jugadores.estrategias;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;

/**
 * Abstraccion de la que depende JugadorMaquina (Dependency Inversion: el
 * jugador de alto nivel no conoce EstrategiaAsertiva ni EstrategiaConservadora,
 * solo este contrato). Implementada en B4.
 */
public interface Estrategia {
    /**
     * Elige, entre los filtros todavia no preguntados, cual conviene preguntar
     * a continuacion dado el conjunto actual de candidatos.
     */
    Filtro elegirFiltro(List<Personaje> candidatos, List<Filtro> filtrosDisponibles);

    /**
     * Elige que personaje arriesgar como respuesta final dado el conjunto
     * actual de candidatos.
     */
    Personaje elegirPersonaje(List<Personaje> candidatos);

    /**
     * Decide si en este turno conviene arriesgar un personaje o seguir
     * preguntando. Es el segundo eje donde las dos maquinas se diferencian:
     * no solo eligen distinto QUE preguntar, sino tambien CUANDO dejar de
     * preguntar. Default conservador para no romper implementaciones previas.
     */
    default boolean debeArriesgar(List<Personaje> candidatos) {
        return candidatos != null && candidatos.size() <= 1;
    }

    /**
     * Nombre legible de la estrategia, para la traza del modo maquina vs
     * maquina y para los reportes de simulacion.
     */
    default String getNombre() {
        return getClass().getSimpleName();
    }
}
