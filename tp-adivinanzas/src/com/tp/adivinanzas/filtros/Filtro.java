package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Personaje;

/**
 * Specification: cada Filtro encapsula una unica condicion sobre un Personaje
 * y sabe describirse a si mismo como pregunta de si/no. Los filtros se pueden
 * componer con {@link #y}, {@link #o} y {@link #negar} sin modificar las
 * implementaciones concretas (Open/Closed).
 */
public interface Filtro {
    boolean cumple(Personaje personaje);

    String getDescripcion();

    default Filtro y(Filtro otro) {
        Filtro base = this;
        return new Filtro() {
            @Override
            public boolean cumple(Personaje personaje) {
                return base.cumple(personaje) && otro.cumple(personaje);
            }

            @Override
            public String getDescripcion() {
                return base.getDescripcion() + " Y " + otro.getDescripcion();
            }
        };
    }

    default Filtro o(Filtro otro) {
        Filtro base = this;
        return new Filtro() {
            @Override
            public boolean cumple(Personaje personaje) {
                return base.cumple(personaje) || otro.cumple(personaje);
            }

            @Override
            public String getDescripcion() {
                return base.getDescripcion() + " O " + otro.getDescripcion();
            }
        };
    }

    default Filtro negar() {
        Filtro base = this;
        return new Filtro() {
            @Override
            public boolean cumple(Personaje personaje) {
                return !base.cumple(personaje);
            }

            @Override
            public String getDescripcion() {
                return "NO (" + base.getDescripcion() + ")";
            }
        };
    }
}
