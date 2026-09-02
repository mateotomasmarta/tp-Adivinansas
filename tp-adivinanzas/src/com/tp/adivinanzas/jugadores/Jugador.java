package com.tp.adivinanzas.jugadores;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.Objects;

/**
 * Template Method: el "oraculo" {@link #responder} es final. Define el unico
 * paso del algoritmo que no puede variar entre subclases, precisamente porque
 * es el que garantiza que nadie -ni el jugador humano ni la maquina- pueda
 * mentir sobre su propio personaje secreto. Lo que si varia por subclase son
 * los pasos de decision ({@link #elegirPregunta} y {@link #arriesgarPersonaje}),
 * que dependen de si hay una persona o una Estrategia detras.
 */
public abstract class Jugador {
    private final String nombre;
    private final Personaje personajeSecreto;

    protected Jugador(String nombre, Personaje personajeSecreto) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio.");
        }
        this.nombre = nombre;
        this.personajeSecreto = Objects.requireNonNull(personajeSecreto, "El personaje secreto es obligatorio.");
    }

    public final boolean responder(Filtro filtro) {
        Objects.requireNonNull(filtro, "El filtro es obligatorio.");
        return filtro.cumple(personajeSecreto);
    }

    /**
     * Comprueba un intento sin entregar el personaje secreto al rival.
     */
    public boolean responderAdivinanza(Personaje personaje) {
        Objects.requireNonNull(personaje, "El personaje es obligatorio.");
        return personajeSecreto.equals(personaje);
    }

    public abstract Filtro elegirPregunta();

    public abstract Personaje arriesgarPersonaje();

    public String getNombre() {
        return nombre;
    }

    public Personaje getPersonajeSecreto() {
        return personajeSecreto;
    }
}
