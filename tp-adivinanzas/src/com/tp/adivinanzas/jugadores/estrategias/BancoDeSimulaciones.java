package com.tp.adivinanzas.jugadores.estrategias;

import com.tp.adivinanzas.filtros.CatalogoFiltros;
import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Banco de medicion empirica de las dos estrategias.
 *
 * Aisla el algoritmo del resto del juego: no usa Partida ni jugadores, solo
 * hace que una Estrategia intente identificar un personaje secreto conocido y
 * cuenta cuantos turnos necesita. Asi la metrica mide la calidad de la
 * decision y no el orden de los turnos ni la suerte del sorteo inicial.
 *
 * Los resultados alimentan la seccion de analisis de eficiencia de la
 * documentacion (B9).
 */
public final class BancoDeSimulaciones {
    private static final int LIMITE_TURNOS = 50;

    private BancoDeSimulaciones() {
    }

    /** Resultado agregado de correr una estrategia contra varios secretos. */
    public static final class Reporte {
        private final String estrategia;
        private final int partidas;
        private final int turnosTotales;
        private final int turnosMinimos;
        private final int turnosMaximos;
        private final int adivinanzasFallidas;

        Reporte(String estrategia, int partidas, int turnosTotales,
                int turnosMinimos, int turnosMaximos, int adivinanzasFallidas) {
            this.estrategia = estrategia;
            this.partidas = partidas;
            this.turnosTotales = turnosTotales;
            this.turnosMinimos = turnosMinimos;
            this.turnosMaximos = turnosMaximos;
            this.adivinanzasFallidas = adivinanzasFallidas;
        }

        public double getPromedioTurnos() {
            return (double) turnosTotales / partidas;
        }

        public int getTurnosMinimos() {
            return turnosMinimos;
        }

        public int getTurnosMaximos() {
            return turnosMaximos;
        }

        public int getAdivinanzasFallidas() {
            return adivinanzasFallidas;
        }

        @Override
        public String toString() {
            return String.format(
                    "%-46s promedio=%.2f  min=%d  max=%d  fallos=%d  (%d partidas)",
                    estrategia, getPromedioTurnos(), turnosMinimos, turnosMaximos,
                    adivinanzasFallidas, partidas);
        }
    }

    /**
     * Corre la estrategia una vez contra cada uno de los 23 personajes como
     * secreto. Cubrir todos los secretos evita que el promedio dependa del azar.
     */
    public static Reporte medir(Estrategia estrategia, int repeticiones) {
        List<Personaje> universo = RepositorioPersonajes.getInstancia().listarTodos();

        int turnosTotales = 0;
        int minimo = Integer.MAX_VALUE;
        int maximo = 0;
        int fallos = 0;
        int partidas = 0;

        for (int i = 0; i < repeticiones; i++) {
            for (Personaje secreto : universo) {
                int[] resultado = simularBusqueda(estrategia, universo, secreto);
                int turnos = resultado[0];

                turnosTotales += turnos;
                fallos += resultado[1];
                minimo = Math.min(minimo, turnos);
                maximo = Math.max(maximo, turnos);
                partidas++;
            }
        }

        return new Reporte(estrategia.getNombre(), partidas, turnosTotales, minimo, maximo, fallos);
    }

    /**
     * Simula la busqueda de un secreto concreto.
     * Devuelve {turnos consumidos, adivinanzas fallidas}.
     */
    private static int[] simularBusqueda(Estrategia estrategia, List<Personaje> universo, Personaje secreto) {
        List<Personaje> candidatos = new ArrayList<Personaje>(universo);
        List<Filtro> disponibles = new ArrayList<Filtro>(CatalogoFiltros.listarTodos());

        int turnos = 0;
        int fallos = 0;

        while (turnos < LIMITE_TURNOS) {
            boolean sinPreguntas = disponibles.isEmpty();

            if (estrategia.debeArriesgar(candidatos) || sinPreguntas) {
                Personaje intento = estrategia.elegirPersonaje(candidatos);
                turnos++;

                if (intento.equals(secreto)) {
                    return new int[] {turnos, fallos};
                }

                fallos++;
                candidatos.remove(intento);
                continue;
            }

            Filtro filtro = estrategia.elegirFiltro(candidatos, disponibles);
            disponibles.remove(filtro);
            boolean respuesta = filtro.cumple(secreto);
            turnos++;

            List<Personaje> restantes = new ArrayList<Personaje>();
            for (Personaje candidato : candidatos) {
                if (filtro.cumple(candidato) == respuesta) {
                    restantes.add(candidato);
                }
            }
            candidatos = restantes;
        }

        throw new IllegalStateException("La estrategia no converge: revisar condicion de corte.");
    }

    public static void main(String[] args) {
        int repeticiones = args.length > 0 ? Integer.parseInt(args[0]) : 40;

        List<Filtro> catalogo = CatalogoFiltros.listarTodos();
        List<Filtro> invertido = new ArrayList<Filtro>(catalogo);
        Collections.reverse(invertido);

        System.out.println("Universo: " + RepositorioPersonajes.getInstancia().cantidad() + " personajes");
        System.out.println("Filtros disponibles: " + catalogo.size());
        System.out.println("Cota teorica inferior: techo(log2(23)) = 5 preguntas + 1 adivinanza = 6 turnos");
        System.out.println();

        System.out.println("[A] Comparacion pura del criterio de eleccion (umbral 1: preguntan hasta aislar)");
        System.out.println("  " + medir(new EstrategiaAsertiva(), 1));
        System.out.println("  " + medir(new EstrategiaConservadora(1, new Random(42), catalogo, "catalogo"), repeticiones));
        System.out.println("  " + medir(new EstrategiaConservadora(1, new Random(42), invertido, "invertido"), repeticiones));
        System.out.println();

        System.out.println("[B] Efecto de arriesgar antes de tiempo (umbral 3)");
        System.out.println("  " + medir(new EstrategiaConservadora(3, new Random(42), catalogo, "catalogo"), repeticiones));
        System.out.println("  " + medir(new EstrategiaConservadora(3, new Random(42), invertido, "invertido"), repeticiones));
    }
}
