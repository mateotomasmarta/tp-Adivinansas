package com.tp.adivinanzas.modelo;

import java.util.Objects;

/**
 * Modelo inmutable del personaje del juego.
 *
 * La identidad del personaje se define por el id. El resto de atributos son
 * las caracteristicas que usan los filtros para descartar candidatos durante
 * la partida.
 */
public final class Personaje {
    private final int id;
    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean lentes;
    private final ColorPelo colorPelo;
    private final boolean barba;

    /**
     * Crea un personaje validando que los datos sean coherentes.
     *
     * Costo: O(1), porque solo valida una cantidad fija de campos.
     */
    public Personaje(int id, String nombre, Genero genero, boolean calvo, boolean lentes, ColorPelo colorPelo, boolean barba) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del personaje es obligatorio.");
        }
        this.id = id;
        this.nombre = nombre;
        this.genero = Objects.requireNonNull(genero, "El genero es obligatorio.");
        this.calvo = calvo;
        this.lentes = lentes;
        this.colorPelo = Objects.requireNonNull(colorPelo, "El color de pelo es obligatorio.");
        this.barba = barba;

        validarCoherenciaPelo();
    }

    /**
     * Regla de negocio: un calvo siempre tiene color NINGUNO y un no calvo
     * siempre tiene un color real.
     *
     * Costo: O(1).
     */
    private void validarCoherenciaPelo() {
        if (calvo && colorPelo != ColorPelo.NINGUNO) {
            throw new IllegalArgumentException("Un personaje calvo debe tener color de pelo NINGUNO.");
        }
        if (!calvo && colorPelo == ColorPelo.NINGUNO) {
            throw new IllegalArgumentException("Un personaje no calvo debe tener un color de pelo real.");
        }
    }

    /**
     * Devuelve una nueva instancia con otro id, manteniendo el resto de datos.
     * Esto permite asignar IDs en el repositorio sin romper la inmutabilidad.
     *
     * Costo: O(1).
     */
    public Personaje conId(int nuevoId) {
        return new Personaje(nuevoId, nombre, genero, calvo, lentes, colorPelo, barba);
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Genero getGenero() {
        return genero;
    }

    public boolean isCalvo() {
        return calvo;
    }

    public boolean tieneLentes() {
        return lentes;
    }

    public ColorPelo getColorPelo() {
        return colorPelo;
    }

    public boolean tieneBarba() {
        return barba;
    }

    /**
     * Dos personajes se consideran iguales si tienen el mismo id.
     *
     * Costo: O(1).
     */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Personaje)) {
            return false;
        }
        Personaje personaje = (Personaje) otro;
        return id == personaje.id;
    }

    /**
     * Usa el id para ser consistente con equals.
     *
     * Costo: O(1).
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Representacion legible para consola/debug.
     *
     * Costo: O(1), porque concatena una cantidad fija de atributos.
     */
    @Override
    public String toString() {
        return id + " - " + nombre + " (" + genero + ", calvo=" + calvo
                + ", lentes=" + lentes + ", pelo=" + colorPelo + ", barba=" + barba + ")";
    }
}
