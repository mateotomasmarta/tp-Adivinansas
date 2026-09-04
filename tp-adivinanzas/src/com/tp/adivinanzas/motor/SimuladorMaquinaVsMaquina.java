package com.tp.adivinanzas.motor;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.modelo.Personaje;

public class SimuladorMaquinavsMaquina {

    public void simular(JugadorMaquina m1, JugadorMaquina m2) {
        Partida partida = new Partida(m1, m2);

        System.out.println("\n==========================================");
        System.out.println("   SIMULACIÓN MÁQUINA VS MÁQUINA");
        System.out.println("==========================================");
        System.out.println("Máquina 1: " + m1.getNombre());
        System.out.println("Máquina 2: " + m2.getNombre());
        System.out.println("==========================================\n");

        simularTurnoRecursivo(partida, 1);
    }

     //Método recursivo que procesa cada turno y genera la traza de ejecución.  
    private void simularTurnoRecursivo(Partida partida, int numeroTurno) {
        // CASO BASE: La partida finalizó (alguien adivinó correctamente)
        if (partida.getEstado() == EstadoPartida.FINALIZADA) {
            System.out.println("\n==========================================");
            System.out.println("GANADOR: " + partida.getGanador().getNombre());
            System.out.println("Total de turnos jugados: " + partida.getTurnosJugados());
            System.out.println("==========================================");
            return;
        }

        JugadorMaquina atacante = (JugadorMaquina) partida.getJugadorActual();
        JugadorMaquina defensor = (JugadorMaquina) partida.getRivalActual();

        System.out.println("--- TURNO " + numeroTurno + " [" + atacante.getNombre() + "] ---");
        System.out.println("Candidatos restantes: " + atacante.getCandidatosRestantes().size());

        // Regla de decisión: Si le queda 1 solo candidato, arriesga la adivinanza
        if (atacante.getCandidatosRestantes().size() == 1) {
            Personaje candidato = atacante.getCandidatosRestantes().get(0);
            System.out.println(atacante.getNombre() + " arriesga: '¿Tu personaje es " + candidato.getNombre() + "?'");

            boolean acierto = partida.realizarAdivinanza(candidato);

            if (acierto) {
                System.out.println("CORRECTO. Adivinó el personaje.");
            } else {
                System.out.println("INCORRECTO. Se descarta la opción.");
            }
        } else {
            // Si tiene más de un candidato, selecciona y aplica el mejor filtro
            Filtro filtro = atacante.seleccionarMejorFiltro();

            if (filtro == null) {
                //si no hay más preguntas útiles disponibles
                if (!atacante.getCandidatosRestantes().isEmpty()) {
                    Personaje candidato = atacante.getCandidatosRestantes().get(0);
                    System.out.println("Sin preguntas útiles. Arriesga a: " + candidato.getNombre());
                    partida.realizarAdivinanza(candidato);
                } else {
                    System.out.println(atacante.getNombre() + " se quedó sin opciones.");
                    return;
                }
            } else {
                System.out.println("Pregunta: " + filtro.getDescripcion());

                // 'Partida' evalúa la pregunta, actualiza el conocimiento del atacante y cambia el turno
                ResultadoPregunta resultado = partida.realizarPregunta(filtro);

                System.out.println("Respuesta de " + defensor.getNombre() + ": " 
                        + (resultado.isRespuestaAfirmativa() ? "SÍ" : "NO"));
                System.out.println("Candidatos ajustados a: " + atacante.getCandidatosRestantes().size());
            }
        }
        // PASO RECURSIVO: Avanza al siguiente turno
        simularTurnoRecursivo(partida, numeroTurno + 1);
    }
}