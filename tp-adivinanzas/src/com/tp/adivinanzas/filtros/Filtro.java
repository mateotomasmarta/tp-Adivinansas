package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Personaje;

/**
 * Specification: cada Filtro encapsula una unica condicion sobre un Personaje
 * y sabe describirse a si mismo como pregunta de si/no.
 */
public interface Filtro {
    boolean cumple(Personaje personaje);

    String getDescripcion();
}
