package com.tp.adivinanzas.motor;

import com.tp.adivinanzas.filtros.CatalogoFiltros;
import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.jugadores.estrategias.EstrategiaAsertiva;
import com.tp.adivinanzas.jugadores.estrategias.Estrategia;
import com.tp.adivinanzas.modelo.Personaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Modo espectador: ejecuta una partida entre dos maquinas mostrando el
 * razonamiento completo de cada turno, no solo el resultado.
 *
 * El recorrido de turnos es RECURSIVO a proposito, porque la partida es un
 * camino descendente por el arbol de decision:
 *   - caso base: la partida finalizo;
 *   - paso recursivo: se juega un turno y se recursa sobre el estado siguiente.
 * Cada llamada trabaja sobre un espacio de candidatos estrictamente menor, asi
 * que el problema se REDUCE en cada paso (a diferencia de MergeSort, que lo
 * DIVIDE en dos subproblemas). La profundidad esperada es O(log n) para la
 * estrategia asertiva, muy lejos del limite de pila: no hay riesgo real de
 * StackOverflowError, pero igual se acota con LIMITE_TURNOS para que un bug en
 * una estrategia no derive en recursion infinita.
 */
public class SimuladorMaquinaVsMaquina {
    private static final int LIMITE_TURNOS = 60;
    private static final String LINEA = "------------------------------------------------------------";

    private final Estrategia estrategiaMaquina1;
    private final Estrategia estrategiaMaquina2;

    public SimuladorMaquinaVsMaquina(Estrategia estrategiaMaquina1, Estrategia estrategiaMaquina2) {
        this.estrategiaMaquina1 = estrategiaMaquina1;
        this.estrategiaMaquina2 = estrategiaMaquina2;
    }

    public void simular(JugadorMaquina maquina1, JugadorMaquina maquina2) {
        Partida partida = new Partida(maquina1, maquina2);

        System.out.println(LINEA);
        System.out.println("SIMULACION MAQUINA VS MAQUINA");
        System.out.println(LINEA);
        System.out.println(maquina1.getNombre() + " -> estrategia " + estrategiaMaquina1.getNombre());
        System.out.println(maquina2.getNombre() + " -> estrategia " + estrategiaMaquina2.getNombre());
        System.out.println("Universo inicial: " + maquina1.getCandidatos().size() + " candidatos para cada una.");

        simularTurnoRecursivo(partida, maquina1, maquina2, 1);
    }

    /**
     * Procesa un turno y recursa sobre el siguiente.
     */
    private void simularTurnoRecursivo(Partida partida, JugadorMaquina maquina1,
                                       JugadorMaquina maquina2, int numeroTurno) {
        // CASO BASE 1: alguien adivino.
        if (partida.getEstado() == EstadoPartida.FINALIZADA) {
            System.out.println(LINEA);
            System.out.println("GANADOR: " + partida.getGanador().getNombre());
            System.out.println("Turnos jugados: " + partida.getTurnosJugados());
            System.out.println(LINEA);
            return;
        }

        // CASO BASE 2: red de seguridad ante una estrategia que no converge.
        if (numeroTurno > LIMITE_TURNOS) {
            System.out.println("Se alcanzo el limite de " + LIMITE_TURNOS + " turnos sin desenlace.");
            return;
        }

        JugadorMaquina atacante = (JugadorMaquina) partida.getJugadorActual();
        JugadorMaquina defensor = (JugadorMaquina) partida.getRivalActual();
        Estrategia estrategia = (atacante == maquina1) ? estrategiaMaquina1 : estrategiaMaquina2;

        List<Personaje> candidatos = atacante.getCandidatos();

        System.out.println();
        System.out.println("TURNO " + numeroTurno + " - juega " + atacante.getNombre());
        System.out.println("  Candidatos posibles: " + candidatos.size() + " " + resumir(candidatos));

        if (estrategia.debeArriesgar(candidatos)) {
            arriesgar(partida, atacante, defensor, estrategia, candidatos);
        } else {
            preguntar(partida, atacante, defensor, estrategia, candidatos);
        }

        // PASO RECURSIVO.
        simularTurnoRecursivo(partida, maquina1, maquina2, numeroTurno + 1);
    }

    private void arriesgar(Partida partida, JugadorMaquina atacante, JugadorMaquina defensor,
                           Estrategia estrategia, List<Personaje> candidatos) {
        Personaje intento = estrategia.elegirPersonaje(candidatos);

        if (candidatos.size() == 1) {
            System.out.println("  Queda un unico candidato: arriesga con certeza.");
        } else {
            System.out.printf("  Decide arriesgar con %d candidatos (probabilidad de acierto 1/%d = %.0f%%).%n",
                    candidatos.size(), candidatos.size(), 100.0 / candidatos.size());
        }

        System.out.println("  " + atacante.getNombre() + " arriesga: es " + intento.getNombre() + "?");
        boolean acierto = partida.realizarAdivinanza(intento);

        if (acierto) {
            System.out.println("  " + defensor.getNombre() + " responde: SI. Acerto.");
        } else {
            System.out.println("  " + defensor.getNombre() + " responde: NO. Pierde el turno y descarta el candidato.");
        }
    }

    private void preguntar(Partida partida, JugadorMaquina atacante, JugadorMaquina defensor,
                           Estrategia estrategia, List<Personaje> candidatos) {
        List<Filtro> disponibles = filtrosDisponiblesPara(atacante);
        boolean evaluaCortes = estrategia instanceof EstrategiaAsertiva;
        mostrarEvaluacion(candidatos, disponibles, evaluaCortes);

        Filtro filtro = atacante.elegirPregunta();
        int antes = candidatos.size();

        int peorCasoElegido = EstrategiaAsertiva.peorCasoDe(candidatos, filtro);
        int mejorPeorCasoPosible = mejorPeorCaso(candidatos, disponibles);

        System.out.println("  Pregunta elegida: " + filtro.getDescripcion()
                + "  [peor caso " + peorCasoElegido
                + (peorCasoElegido == mejorPeorCasoPosible
                        ? ", que es el mejor disponible]"
                        : ", pudiendo haber elegido uno de " + mejorPeorCasoPosible + "]"));

        ResultadoPregunta resultado = partida.realizarPregunta(filtro);
        int despues = atacante.getCandidatos().size();

        System.out.println("  " + defensor.getNombre() + " responde: "
                + (resultado.isRespuestaAfirmativa() ? "SI" : "NO"));
        System.out.println("  Descarta " + (antes - despues) + " candidatos, le quedan " + despues + ".");
    }

    /**
     * Muestra la tabla de evaluacion que usa el criterio minimax: cuantos
     * candidatos caen de cada lado y cual es el peor caso resultante. Es la
     * parte que la consigna pide "presenciar": deja ver POR QUE la maquina
     * elige una pregunta y no otra.
     */
    private void mostrarEvaluacion(List<Personaje> candidatos, List<Filtro> disponibles, boolean evaluaCortes) {
        if (evaluaCortes) {
            System.out.println("  Evalua cada corte y elige el de menor peor caso (SI / NO -> peor caso):");
        } else {
            System.out.println("  Tabla de cortes posibles -- ESTA ESTRATEGIA NO LA CONSULTA (SI / NO -> peor caso):");
        }

        for (Filtro filtro : disponibles) {
            int afirmativos = EstrategiaAsertiva.contarAfirmativos(candidatos, filtro);
            int negativos = candidatos.size() - afirmativos;
            int peorCaso = Math.max(afirmativos, negativos);
            boolean inutil = afirmativos == 0 || negativos == 0;

            System.out.printf("    %-42s %2d / %2d -> %2d%s%n",
                    filtro.getDescripcion(), afirmativos, negativos, peorCaso,
                    inutil ? "   (no descarta a nadie)" : "");
        }
    }

    /** Mejor peor caso alcanzable este turno: sirve para medir cuanto pierde
     *  la estrategia que no evalua los cortes. */
    private int mejorPeorCaso(List<Personaje> candidatos, List<Filtro> disponibles) {
        int mejor = Integer.MAX_VALUE;
        for (Filtro filtro : disponibles) {
            mejor = Math.min(mejor, EstrategiaAsertiva.peorCasoDe(candidatos, filtro));
        }
        return mejor;
    }

    private List<Filtro> filtrosDisponiblesPara(JugadorMaquina maquina) {
        List<Filtro> disponibles = new ArrayList<Filtro>(CatalogoFiltros.listarTodos());
        disponibles.removeAll(maquina.getFiltrosUsados());
        return disponibles;
    }

    private String resumir(List<Personaje> candidatos) {
        if (candidatos.size() > 6) {
            return "";
        }

        StringBuilder nombres = new StringBuilder("(");
        for (int i = 0; i < candidatos.size(); i++) {
            if (i > 0) {
                nombres.append(", ");
            }
            nombres.append(candidatos.get(i).getNombre());
        }
        return nombres.append(")").toString();
    }
}
