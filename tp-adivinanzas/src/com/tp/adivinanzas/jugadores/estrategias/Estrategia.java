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
}
