package com.tp.adivinanzas.persistencia;

/**
 * Registro inmutable de un jugador en el marcador: nombre y partidas ganadas.
 */
public class RecordJugador {
    private final String nombre;
    private final int partidasGanadas;

    public RecordJugador(String nombre, int partidasGanadas) {
        this.nombre = nombre;
        this.partidasGanadas = partidasGanadas;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    @Override
    public String toString() {
        return nombre + ": " + partidasGanadas + " victorias";
    }
}
