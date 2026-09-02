package com.tp.adivinanzas.motor;

import com.tp.adivinanzas.jugadores.JugadorMaquina;

import java.util.Objects;

/**
 * Observador concreto que permite a Maquina 2 conocer las preguntas de
 * Maquina 1 dirigidas al mismo rival.
 */
public final class ObservadorMaquina implements ObservadorPreguntas {
    private final JugadorMaquina maquina;
    private final String autorObservado;
    private final String destinatarioCompartido;

    public ObservadorMaquina(JugadorMaquina maquina, String autorObservado, String destinatarioCompartido) {
        this.maquina = Objects.requireNonNull(maquina, "La maquina es obligatoria.");
        this.autorObservado = validarNombre(autorObservado, "El autor observado es obligatorio.");
        this.destinatarioCompartido = validarNombre(destinatarioCompartido,
                "El destinatario compartido es obligatorio.");
    }

    @Override
    public boolean debeObservar(Pregunta pregunta) {
        return autorObservado.equals(pregunta.getAutor())
                && destinatarioCompartido.equals(pregunta.getDestinatario());
    }

    @Override
    public void alResponderPregunta(ResultadoPregunta resultado) {
        maquina.incorporarConocimiento(resultado.getPregunta().getFiltro(),
                resultado.isRespuestaAfirmativa());
    }

    private static String validarNombre(String nombre, String mensaje) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return nombre;
    }
}
