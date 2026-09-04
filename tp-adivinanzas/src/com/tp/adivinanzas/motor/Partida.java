package com.tp.adivinanzas.motor;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.Jugador;
import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.modelo.Personaje;

/**
 * Coordina una partida entre dos jugadores. No decide estrategias, no realiza
 * entrada/salida y no persiste resultados.
 */
public final class Partida {
    private final Jugador[] jugadores;
    private final HistorialPreguntas historialPreguntas;
    private int indiceTurno;
    private int turnosJugados;
    private EstadoPartida estado;
    private Jugador ganador;

    public Partida(Jugador primerJugador, Jugador segundoJugador) {
        this(primerJugador, segundoJugador, new HistorialPreguntas());
    }

    public Partida(Jugador primerJugador, Jugador segundoJugador, HistorialPreguntas historialPreguntas) {
        if (primerJugador == null || segundoJugador == null) {
            throw new IllegalArgumentException("Los dos jugadores son obligatorios.");
        }
        if (primerJugador == segundoJugador) {
            throw new IllegalArgumentException("Los participantes deben ser jugadores diferentes.");
        }
        if (primerJugador.getNombre().equals(segundoJugador.getNombre())) {
            throw new IllegalArgumentException("Los participantes deben tener nombres diferentes.");
        }
        if (historialPreguntas == null) {
            throw new IllegalArgumentException("El historial es obligatorio.");
        }

        this.jugadores = new Jugador[] { primerJugador, segundoJugador };
        this.historialPreguntas = historialPreguntas;
        this.indiceTurno = 0;
        this.turnosJugados = 0;
        this.estado = EstadoPartida.EN_CURSO;
    }

    /**
     * Realiza una pregunta al rival del jugador que tiene el turno.
     */
    public ResultadoPregunta realizarPregunta(Filtro filtro) {
        verificarEnCurso();
        if (filtro == null) {
            throw new IllegalArgumentException("El filtro es obligatorio.");
        }

        Jugador autor = getJugadorActual();
        Jugador destinatario = getRivalActual();
        boolean respuesta = destinatario.responder(filtro);

        if (autor instanceof JugadorMaquina) {
            ((JugadorMaquina) autor).incorporarConocimiento(filtro, respuesta);
        }

        Pregunta pregunta = new Pregunta(turnosJugados + 1, autor.getNombre(),
                destinatario.getNombre(), filtro);
        ResultadoPregunta resultadoPregunta = new ResultadoPregunta(pregunta, respuesta);
        historialPreguntas.registrar(resultadoPregunta);

        completarTurnoSinVictoria();
        return resultadoPregunta;
    }

    /**
     * Intenta adivinar el personaje del rival del jugador que tiene el turno.
     */
    public boolean realizarAdivinanza(Personaje candidato) {
        verificarEnCurso();
        if (candidato == null) {
            throw new IllegalArgumentException("El personaje es obligatorio.");
        }

        Jugador autor = getJugadorActual();
        Jugador destinatario = getRivalActual();
        boolean acierto = destinatario.responderAdivinanza(candidato);

        turnosJugados++;
        if (acierto) {
            ganador = autor;
            estado = EstadoPartida.FINALIZADA;
        } else {
            if (autor instanceof JugadorMaquina) {
                ((JugadorMaquina) autor).descartarCandidato(candidato);
            }
            cambiarTurno();
        }

        return acierto;
    }

    private void completarTurnoSinVictoria() {
        turnosJugados++;
        cambiarTurno();
    }

    private void cambiarTurno() {
        indiceTurno = 1 - indiceTurno;
    }

    private void verificarEnCurso() {
        if (estado == EstadoPartida.FINALIZADA) {
            throw new IllegalStateException("La partida ya finalizo.");
        }
    }

    public Jugador getJugadorActual() {
        return jugadores[indiceTurno];
    }

    public Jugador getRivalActual() {
        return jugadores[1 - indiceTurno];
    }

    public int getTurnosJugados() {
        return turnosJugados;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public HistorialPreguntas getHistorialPreguntas() {
        return historialPreguntas;
    }
}
