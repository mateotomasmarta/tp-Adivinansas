package com.tp.adivinanzas;

import com.tp.adivinanzas.filtros.Filtro;
import com.tp.adivinanzas.jugadores.Jugador;
import com.tp.adivinanzas.jugadores.JugadorHumano;
import com.tp.adivinanzas.jugadores.JugadorMaquina;
import com.tp.adivinanzas.jugadores.estrategias.Estrategia;
import com.tp.adivinanzas.jugadores.estrategias.EstrategiaAsertiva;
import com.tp.adivinanzas.jugadores.estrategias.EstrategiaConservadora;
import com.tp.adivinanzas.modelo.Personaje;
import com.tp.adivinanzas.motor.EstadoPartida;
import com.tp.adivinanzas.motor.HistorialPreguntas;
import com.tp.adivinanzas.motor.ObservadorMaquina;
import com.tp.adivinanzas.motor.Partida;
import com.tp.adivinanzas.motor.ResultadoPregunta;
import com.tp.adivinanzas.motor.SimuladorMaquinaVsMaquina;
import com.tp.adivinanzas.persistencia.RecordArchivoDAO;
import com.tp.adivinanzas.persistencia.RecordDAO;
import com.tp.adivinanzas.repositorio.RepositorioPersonajes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Facade de consola: es el unico punto que conoce a todos los subsistemas
 * (repositorio, jugadores, estrategias, motor, persistencia) y los cablea
 * entre si. Ninguna clase del dominio depende de esta.
 */
public class Main {
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RepositorioPersonajes repositorio = RepositorioPersonajes.getInstancia();
        List<Personaje> personajes = repositorio.listarTodos();

        RecordDAO recordDAO = new RecordArchivoDAO();
        recordDAO.cargar();

        System.out.println("=== ADIVINA QUIEN ===");
        System.out.print("Ingresa tu nombre de jugador: ");
        String nombreJugador = scanner.nextLine().trim();
        if (nombreJugador.isEmpty()) {
            nombreJugador = "Jugador 1";
        }

        ConsolaJuego consola = new ConsolaJuego(scanner);
        boolean salir = false;

        while (!salir) {
            System.out.println();
            System.out.println("--- MENU PRINCIPAL ---");
            System.out.println("Usuario actual: " + nombreJugador);
            System.out.println("1. Jugar contra las maquinas");
            System.out.println("2. Simulacion maquina vs maquina");
            System.out.println("3. Ver marcador de records");
            System.out.println("4. Salir");

            int opcion = consola.pedirEntero(1, 4);

            switch (opcion) {
                case 1:
                    jugarHumanoVsMaquinas(nombreJugador, consola, personajes, recordDAO);
                    break;
                case 2:
                    simularMaquinaVsMaquina(personajes);
                    break;
                case 3:
                    mostrarRecords(recordDAO);
                    break;
                case 4:
                    salir = true;
                    System.out.println("Gracias por jugar.");
                    break;
                default:
                    break;
            }
        }

        scanner.close();
    }

    /**
     * El humano juega primero contra Maquina 1.
     * Mientras se desarrolla esa partida, Maquina 2 observa las preguntas y
     * respuestas
     * obtenidas por Maquina 1.
     * Si el humano vence a Maquina 1, juega una segunda partida contra Maquina 2,
     * que conserva el conocimiento adquirido previamente.
     */
    private static void jugarHumanoVsMaquinas(String nombreJugador, ConsolaJuego consola,
            List<Personaje> personajes, RecordDAO recordDAO) {
        System.out.println();
        System.out.println("Elegi tu personaje secreto. No lo vas a poder cambiar durante la partida.");
        Personaje secretoHumano = consola.elegirPersonaje(nombreJugador, personajes);

        // Cada maquina elige un personaje distinto entre si y distinto del humano.
        List<Personaje> secretos = elegirDistintos(personajes, 2, secretoHumano);

        Estrategia estrategiaM1 = new EstrategiaAsertiva();
        Estrategia estrategiaM2 = new EstrategiaConservadora();

        JugadorHumano humano = new JugadorHumano(nombreJugador, secretoHumano, consola);
        JugadorMaquina maquina1 = new JugadorMaquina("Maquina 1", secretos.get(0), estrategiaM1, personajes);
        JugadorMaquina maquina2 = new JugadorMaquina("Maquina 2", secretos.get(1), estrategiaM2, personajes);

        // OBSERVER: Maquina 2 recibe las preguntas y respuestas que Maquina 1
        // obtiene del humano. De esta forma adquiere conocimiento antes de
        // enfrentarse al jugador.
        HistorialPreguntas historial = new HistorialPreguntas();
        historial.agregarObservador(new ObservadorMaquina(maquina2, "Maquina 1", nombreJugador));

        Partida partida = new Partida(humano, maquina1, historial);

        System.out.println();
        System.out.println("Empieza la partida. Tu objetivo: adivinar el personaje de Maquina 1.");
        System.out.println("Maquina 2 escucha las preguntas de Maquina 1 (ventaja informativa).");

        while (partida.getEstado() == EstadoPartida.EN_CURSO) {
            Jugador actual = partida.getJugadorActual();

            if (actual == humano) {
                turnoHumano(partida, humano, consola, personajes);
            } else {
                turnoMaquina(partida, maquina1, estrategiaM1);
            }
        }

        System.out.println();
        System.out.println("Gano: " + partida.getGanador().getNombre()
                + " en " + partida.getTurnosJugados() + " turnos.");

        System.out.println("Candidatos que le quedaban a Maquina 2 escuchando: "
                + maquina2.getCandidatos().size());

        if (partida.getGanador() != humano) {
            System.out.println("El personaje de Maquina 1 era: "
                    + maquina1.getPersonajeSecreto().getNombre());
            return;
        }

        System.out.println();
        System.out.println("Venciste a Maquina 1.");
        System.out.println("Ahora te enfrentas a Maquina 2.");
        System.out.println("Maquina 2 conserva la informacion aprendida de Maquina 1.");

        Partida partida2 = new Partida(humano, maquina2);

        System.out.println();
        System.out.println("Empieza la segunda partida.");
        System.out.println("Tu objetivo: adivinar el personaje de Maquina 2.");

        while (partida2.getEstado() == EstadoPartida.EN_CURSO) {
            Jugador actual = partida2.getJugadorActual();

            if (actual == humano) {
                turnoHumano(partida2, humano, consola, personajes);
            } else {
                turnoMaquina(partida2, maquina2, estrategiaM2);
            }
        }

        System.out.println();
        System.out.println("Gano: " + partida2.getGanador().getNombre()
                + " en " + partida2.getTurnosJugados() + " turnos.");

        if (partida2.getGanador() == humano) {
            recordDAO.registrarVictoria(nombreJugador);

            System.out.println("Venciste a las dos maquinas.");
            System.out.println("Victoria registrada en el marcador.");
        } else {
            System.out.println("El personaje de Maquina 2 era: "
                    + maquina2.getPersonajeSecreto().getNombre());
        }
    }

    private static void turnoHumano(Partida partida, JugadorHumano humano, ConsolaJuego consola,
            List<Personaje> personajes) {
        System.out.println();
        System.out.println("--- Tu turno ---");
        System.out.println("1. Hacer una pregunta");
        System.out.println("2. Arriesgar el personaje");

        int opcion = consola.pedirEntero(1, 2);

        if (opcion == 1) {
            Filtro filtro = humano.elegirPregunta();
            ResultadoPregunta resultado = partida.realizarPregunta(filtro);
            consola.mostrarResultadoTurno(resultado);
        } else {
            Personaje intento = consola.elegirPersonaje("Tu apuesta", personajes);
            boolean acierto = partida.realizarAdivinanza(intento);
            System.out.println(acierto ? "Acertaste!" : "No era ese personaje.");
        }
    }

    private static void turnoMaquina(Partida partida, JugadorMaquina maquina, Estrategia estrategia) {
        System.out.println();
        System.out.println("--- Turno de " + maquina.getNombre() + " ---");
        System.out.println("Le quedan " + maquina.getCandidatos().size() + " candidatos posibles.");

        if (estrategia.debeArriesgar(maquina.getCandidatos())) {
            Personaje intento = maquina.arriesgarPersonaje();
            System.out.println(maquina.getNombre() + " arriesga: tu personaje es " + intento.getNombre() + "?");
            boolean acierto = partida.realizarAdivinanza(intento);
            System.out.println(acierto ? "Adivino." : "Fallo, pierde el turno.");
        } else {
            Filtro filtro = maquina.elegirPregunta();
            System.out.println(maquina.getNombre() + " pregunta: " + filtro.getDescripcion());
            ResultadoPregunta resultado = partida.realizarPregunta(filtro);
            System.out.println("Respuesta: " + (resultado.isRespuestaAfirmativa() ? "SI" : "NO"));
        }
    }

    private static void simularMaquinaVsMaquina(List<Personaje> personajes) {
        List<Personaje> secretos = elegirDistintos(personajes, 2, null);

        Estrategia estrategiaM1 = new EstrategiaAsertiva();
        Estrategia estrategiaM2 = new EstrategiaConservadora();

        JugadorMaquina maquina1 = new JugadorMaquina("Maquina 1", secretos.get(0), estrategiaM1, personajes);
        JugadorMaquina maquina2 = new JugadorMaquina("Maquina 2", secretos.get(1), estrategiaM2, personajes);

        System.out.println();
        System.out.println("Personaje secreto de Maquina 1: " + secretos.get(0).getNombre());
        System.out.println("Personaje secreto de Maquina 2: " + secretos.get(1).getNombre());

        new SimuladorMaquinaVsMaquina(estrategiaM1, estrategiaM2).simular(maquina1, maquina2);
    }

    private static void mostrarRecords(RecordDAO recordDAO) {
        System.out.println();
        System.out.println("--- MARCADOR ---");
        recordDAO.cargar();
        recordDAO.mostrar();
    }

    /**
     * Sortea personajes distintos entre si y distintos de uno ya tomado.
     * La consigna exige que cada maquina elija un personaje diferente.
     */
    private static List<Personaje> elegirDistintos(List<Personaje> personajes, int cantidad, Personaje excluido) {
        List<Personaje> disponibles = new ArrayList<Personaje>(personajes);
        if (excluido != null) {
            disponibles.remove(excluido);
        }

        List<Personaje> elegidos = new ArrayList<Personaje>();
        for (int i = 0; i < cantidad; i++) {
            elegidos.add(disponibles.remove(RANDOM.nextInt(disponibles.size())));
        }
        return elegidos;
    }

}
