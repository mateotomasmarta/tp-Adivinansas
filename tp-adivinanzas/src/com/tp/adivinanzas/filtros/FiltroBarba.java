package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.Personaje;

/**
 * No estaba en el set original de filtros, pero es necesaria: el atributo
 * barba se agrego en el modelo (B0) unicamente para que los 23 personajes
 * sean distinguibles. Si nunca se puede preguntar por barba, dos personajes
 * identicos en genero/calvicie/lentes/colorPelo pero distintos en barba
 * serian indistinguibles para la maquina y esa garantia del modelo no serviria.
 */
public final class FiltroBarba implements Filtro {
    private final boolean barba;

    public FiltroBarba(boolean barba) {
        this.barba = barba;
    }

    @Override
    public boolean cumple(Personaje personaje) {
        return personaje.tieneBarba() == barba;
    }

    @Override
    public String getDescripcion() {
        return barba ? "¿Tu personaje tiene barba?" : "¿Tu personaje NO tiene barba?";
    }
}
