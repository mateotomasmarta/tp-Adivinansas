package com.tp.adivinanzas.filtros;

import com.tp.adivinanzas.modelo.ColorPelo;
import com.tp.adivinanzas.modelo.Genero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enumera todas las preguntas de si/no posibles sobre un Personaje.
 * Cada atributo booleano aporta un unico filtro (preguntar por el negativo
 * no da informacion nueva); colorPelo aporta un filtro por cada color real,
 * porque NINGUNO ya queda determinado por la respuesta de FiltroCalvicie.
 */
public final class CatalogoFiltros {
    private static final List<Filtro> FILTROS = crearFiltros();

    private CatalogoFiltros() {
    }

    public static List<Filtro> listarTodos() {
        return FILTROS;
    }

    private static List<Filtro> crearFiltros() {
        List<Filtro> filtros = new ArrayList<Filtro>();

        filtros.add(new FiltroGenero(Genero.MASCULINO));
        filtros.add(new FiltroCalvicie(true));
        filtros.add(new FiltroLentes(true));
        filtros.add(new FiltroBarba(true));

        for (ColorPelo colorPelo : ColorPelo.values()) {
            if (colorPelo != ColorPelo.NINGUNO) {
                filtros.add(new FiltroColorPelo(colorPelo));
            }
        }

        return Collections.unmodifiableList(filtros);
    }
}
