package com.tp.adivinanzas.motor;

import com.tp.adivinanzas.filtros.Filtro;

import java.util.Objects;

/**
 * Pregunta inmutable realizada por un jugador a otro en un turno concreto.
 */
public final class Pregunta {
    private final int numeroTurno;
    private final String autor;
    private final String destinatario;
    private final Filtro filtro;

    public Pregunta(int numeroTurno, String autor, String destinatario, Filtro filtro) {
        if (numeroTurno < 1) {
            throw new IllegalArgumentException("El numero de turno debe ser positivo.");
        }
        this.numeroTurno = numeroTurno;
        this.autor = validarNombre(autor, "El autor es obligatorio.");
        this.destinatario = validarNombre(destinatario, "El destinatario es obligatorio.");
        this.filtro = Objects.requireNonNull(filtro, "El filtro es obligatorio.");
    }

    private static String validarNombre(String nombre, String mensaje) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        return nombre;
    }

    public int getNumeroTurno() {
        return numeroTurno;
    }

    public String getAutor() {
        return autor;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public Filtro getFiltro() {
        return filtro;
    }

    public String getDescripcion() {
        return filtro.getDescripcion();
    }
}
