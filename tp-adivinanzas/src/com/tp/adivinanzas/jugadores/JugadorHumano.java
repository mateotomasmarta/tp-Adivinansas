package com.tp.adivinanzas.jugadores;

import com.tp.adivinanzas.filtros.CatalogoFiltros;
import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;

import java.util.Objects;

/**
 * Version del jugador respaldada por una persona real. No hace I/O de
 * consola: delega la interaccion (mostrar opciones, leer la eleccion) en
 * InterfazHumano, que implementa ConsolaJuego (B8).
 */
public final class JugadorHumano extends Jugador {
    private final InterfazHumano interfaz;

    public JugadorHumano(String nombre, Personaje personajeSecreto, InterfazHumano interfaz) {
        super(nombre, personajeSecreto);
        this.interfaz = Objects.requireNonNull(interfaz, "La interfaz humana es obligatoria.");
    }

    @Override
    public Filtro elegirPregunta() {
        return interfaz.elegirFiltro(getNombre(), CatalogoFiltros.listarTodos());
    }

    @Override
    public Personaje arriesgarPersonaje() {
        return interfaz.elegirPersonaje(getNombre(), RepositorioPersonajes.getInstancia().listarTodos());
    }
}
