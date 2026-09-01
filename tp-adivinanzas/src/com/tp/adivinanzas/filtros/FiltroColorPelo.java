package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.ColorPelo;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.Objects;

public final class FiltroColorPelo implements Filtro {
    private final ColorPelo colorPelo;

    public FiltroColorPelo(ColorPelo colorPelo) {
        this.colorPelo = Objects.requireNonNull(colorPelo, "El color de pelo es obligatorio.");
    }

    @Override
    public boolean cumple(Personaje personaje) {
        return personaje.getColorPelo() == colorPelo;
    }

    @Override
    public String getDescripcion() {
        return "¿Tu personaje tiene el pelo " + colorPelo + "?";
    }
}
