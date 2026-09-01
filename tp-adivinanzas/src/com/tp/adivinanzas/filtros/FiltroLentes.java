package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Personaje;

public final class FiltroLentes implements Filtro {
    private final boolean lentes;

    public FiltroLentes(boolean lentes) {
        this.lentes = lentes;
    }

    @Override
    public boolean cumple(Personaje personaje) {
        return personaje.tieneLentes() == lentes;
    }

    @Override
    public String getDescripcion() {
        return lentes ? "¿Tu personaje usa lentes?" : "¿Tu personaje NO usa lentes?";
    }
}
