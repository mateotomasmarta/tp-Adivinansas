package com.tp.adivinanzas.motor;

import java.util.Objects;

/**
 * Evento inmutable que contiene una pregunta publica y su respuesta.
 */
public final class ResultadoPregunta {
    private final Pregunta pregunta;
    private final boolean respuesta;

    public ResultadoPregunta(Pregunta pregunta, boolean respuesta) {
        this.pregunta = Objects.requireNonNull(pregunta, "La pregunta es obligatoria.");
        this.respuesta = respuesta;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public boolean isRespuestaAfirmativa() {
        return respuesta;
    }
}
