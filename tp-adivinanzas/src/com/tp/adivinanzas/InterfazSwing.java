package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.InterfazHumano;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.List;

/**
 * Implementacion de InterfazHumano respaldada por VentanaJuego (la app),
 * no por popups. JugadorHumano no distingue si el otro lado es una consola
 * o una GUI: solo pide un Filtro o un Personaje y espera la respuesta.
 */
public class InterfazSwing implements InterfazHumano {
    private final VentanaJuego ventana;

    public InterfazSwing(VentanaJuego ventana) {
        this.ventana = ventana;
    }

    @Override
    public Filtro elegirFiltro(String nombreJugador, List<Filtro> disponibles) {
        ventana.log("\n" + nombreJugador + ", elegi una pregunta:");
        return ventana.pedirEleccion(disponibles, Filtro::getDescripcion);
    }

    @Override
    public Personaje elegirPersonaje(String nombreJugador, List<Personaje> disponibles) {
        ventana.log("\n" + nombreJugador + ", elegi un personaje:");
        return ventana.pedirEleccion(disponibles, this::describir);
    }

    /**
     * Igual que elegirPersonaje, pero con un boton "Atras" que devuelve null
     * en vez de forzar una eleccion. No forma parte de InterfazHumano porque
     * solo tiene sentido cuando arriesgar no gasta el turno todavia (el
     * humano recien esta decidiendo, no comprometido con partida.realizarAdivinanza).
     */
    public Personaje elegirPersonajeOVolver(String nombreJugador, List<Personaje> disponibles) {
        ventana.log("\n" + nombreJugador + ", elegi un personaje (o volver):");
        return ventana.pedirEleccion(disponibles, this::describir, "Atras");
    }

    private String describir(Personaje personaje) {
        return personaje.getId() + " - " + personaje.getNombre()
                + " (" + personaje.getGenero()
                + (personaje.isCalvo() ? ", calvo" : ", pelo " + personaje.getColorPelo())
                + (personaje.tieneLentes() ? ", lentes" : "")
                + (personaje.tieneBarba() ? ", barba" : "") + ")";
    }
}
