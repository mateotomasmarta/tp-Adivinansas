package com.tp.adivinanzas.jugadores;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;

/**
 * Abstraccion de la que depende JugadorHumano para resolver sus pasos
 * variables (Dependency Inversion, igual que Estrategia para JugadorMaquina).
 * JugadorHumano no hace I/O de consola por si mismo: le delega la interaccion
 * con la persona a quien implemente esta interfaz. La implementacion real
 * (imprimir menus, leer con Scanner) es responsabilidad de ConsolaJuego (B8).
 */
public interface InterfazHumano {
    Filtro elegirFiltro(String nombreJugador, List<Filtro> disponibles);

    Personaje elegirPersonaje(String nombreJugador, List<Personaje> candidatos);
}
