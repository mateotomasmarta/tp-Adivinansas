package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Genero;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.Objects;

public final class FiltroGenero implements Filtro {
    private final Genero genero;

    public FiltroGenero(Genero genero) {
        this.genero = Objects.requireNonNull(genero, "El genero es obligatorio.");
    }

    @Override
    public boolean cumple(Personaje personaje) {
        return personaje.getGenero() == genero;
    }

    @Override
    public String getDescripcion() {
        return "¿Tu personaje es de genero " + genero + "?";
    }
}
