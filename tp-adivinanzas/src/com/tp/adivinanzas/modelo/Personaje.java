package com.tp.adivinanzas.modelo;

import java.util.Objects;

public final class Personaje {
    private final int id;
    private final String nombre;
    private final Genero genero;
    private final boolean calvo;
    private final boolean lentes;
    private final ColorPelo colorPelo;
    private final boolean barba;

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

    private void validarCoherenciaPelo() {
        if (calvo && colorPelo != ColorPelo.NINGUNO) {
            throw new IllegalArgumentException("Un personaje calvo debe tener color de pelo NINGUNO.");
        }
        if (!calvo && colorPelo == ColorPelo.NINGUNO) {
            throw new IllegalArgumentException("Un personaje no calvo debe tener un color de pelo real.");
        }
    }

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

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + genero + ", calvo=" + calvo
                + ", lentes=" + lentes + ", pelo=" + colorPelo + ", barba=" + barba + ")";
    }
}
