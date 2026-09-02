package com.tp.adivinanzas.motor;

/**
 * Observador de respuestas publicas a preguntas realizadas durante una partida.
 */
public interface ObservadorPreguntas {
    boolean debeObservar(Pregunta pregunta);

    void alResponderPregunta(ResultadoPregunta resultado);
}
