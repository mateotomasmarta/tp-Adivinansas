package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Personaje;

public final class FiltroCalvicie implements Filtro {
    private final boolean calvo;

    public FiltroCalvicie(boolean calvo) {
        this.calvo = calvo;
    }

    @Override
    public boolean cumple(Personaje personaje) {
        return personaje.isCalvo() == calvo;
    }

    @Override
    public String getDescripcion() {
        return calvo ? "¿Tu personaje es calvo?" : "¿Tu personaje NO es calvo?";
    }
}
